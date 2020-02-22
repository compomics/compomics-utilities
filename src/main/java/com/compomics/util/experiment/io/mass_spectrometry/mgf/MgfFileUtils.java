package com.compomics.util.experiment.io.mass_spectrometry.mgf;

import com.compomics.util.parameters.UtilitiesUserParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * Utilities for the handling of mgf files.
 *
 * @author Marc Vaudel
 */
public class MgfFileUtils {

    /**
     * The file extension for cms files.
     */
    public static final String[] EXTENSIONS = new String[]{".mgf", ".mgf.gz"};

    /**
     * Removes duplicate spectrum titles (the first occurrence is kept).
     *
     * @param mgfFile the MGF file to validate
     * @param waitingHandler a waitingHandler showing the progress, can be null
     *
     * @throws FileNotFoundException Exception thrown whenever the file is not
     * found
     * @throws IOException Exception thrown whenever an error occurs while
     * reading the file
     * @throws UnsupportedEncodingException if the decoding of a spectrum title
     * fails
     */
    public static void removeDuplicateSpectrumTitles(
            File mgfFile,
            WaitingHandler waitingHandler
    ) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        ArrayList<String> spectrumTitles = new ArrayList<>();
        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        String lineBreak = System.getProperty("line.separator");

        try ( BufferedRandomAccessFile br = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100)) {

            long progressUnit = br.length() / 100;

            try ( BufferedWriter bw = new BufferedWriter(new FileWriter(tempSpectrumFile))) {

                String line;
                String currentSpectrum = "";
                boolean includeSpectrum = true;

                while ((line = br.readLine()) != null) {

                    if (line.startsWith("BEGIN IONS")) {
                        currentSpectrum = line + lineBreak;

                        if (waitingHandler != null) {
                            if (waitingHandler.isRunCanceled()) {
                                break;
                            }
                            waitingHandler.setSecondaryProgressCounter((int) (br.getFilePointer() / progressUnit));
                        }

                    } else if (line.startsWith("TITLE")) {
                        currentSpectrum += line + lineBreak;

                        String title = line.substring(line.indexOf('=') + 1);

                        try {
                            title = URLDecoder.decode(title, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            throw new UnsupportedEncodingException("An exception was thrown when trying to decode an mgf title: " + title);
                        }

                        if (!spectrumTitles.contains(title)) {
                            spectrumTitles.add(title);
                            includeSpectrum = true;
                        } else {
                            includeSpectrum = false;
                        }

                    } else if (line.startsWith("END IONS")) {
                        currentSpectrum += line + lineBreak;
                        if (includeSpectrum) {
                            bw.write(currentSpectrum);
                            bw.newLine();
                        }
                    } else {
                        currentSpectrum += line + lineBreak;
                    }
                }

            }
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        }

        // replace the old file
        String orignalFilePath = mgfFile.getAbsolutePath();
        boolean fileDeleted = mgfFile.delete();

        if (!fileDeleted) {
            throw new IOException("Failed to delete the original spectrum file.");
        }

        boolean fileRenamed = tempSpectrumFile.renameTo(new File(orignalFilePath));

        if (!fileRenamed) {
            throw new IOException("Failed to replace the original spectrum file.");
        }
    }

    /**
     * Adds missing spectrum titles.
     *
     * @param mgfFile the MGF file to fix
     * @param waitingHandler a waitingHandler showing the progress, can be null
     *
     * @throws FileNotFoundException Exception thrown whenever the file is not
     * found
     * @throws IOException Exception thrown whenever an error occurs while
     * reading the file
     * @throws UnsupportedEncodingException if the decoding of a spectrum title
     * fails
     */
    public static void addMissingSpectrumTitles(
            File mgfFile,
            WaitingHandler waitingHandler
    ) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        ArrayList<String> spectrumTitles = new ArrayList<>();

        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        try ( BufferedRandomAccessFile br = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100)) {
            String lineBreak = System.getProperty("line.separator");

            long progressUnit = br.length() / 100;

            try ( BufferedWriter bw = new BufferedWriter(new FileWriter(tempSpectrumFile))) {

                String line;
                String currentSpectrum = "";
                String title = null;
                int spectrumCounter = 0;

                while ((line = br.readLine()) != null) {

                    if (line.startsWith("BEGIN IONS")) {
                        spectrumCounter++;

                        if (waitingHandler != null) {
                            if (waitingHandler.isRunCanceled()) {
                                break;
                            }
                            waitingHandler.setSecondaryProgressCounter((int) (br.getFilePointer() / progressUnit));
                        }

                    } else if (line.startsWith("TITLE")) {
                        currentSpectrum += line + lineBreak;

                        title = line.substring(line.indexOf('=') + 1);

                        try {
                            title = URLDecoder.decode(title, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            throw new UnsupportedEncodingException("An exception was thrown when trying to decode an mgf title: " + title);
                        }

                        spectrumTitles.add(title);
                    } else if (line.startsWith("END IONS")) {

                        bw.write("BEGIN IONS" + lineBreak);

                        if (title == null) {
                            title = "Spectrum " + spectrumCounter;
                            while (spectrumTitles.contains(title)) {
                                title = "Spectrum " + ++spectrumCounter;
                            }
                            spectrumTitles.add(title);
                            bw.write("TITLE=" + title + lineBreak);
                        }

                        bw.write(currentSpectrum);
                        bw.write("END IONS" + lineBreak);
                        currentSpectrum = "";
                        title = null;
                    } else {
                        currentSpectrum += line + lineBreak;
                    }
                }
            }
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        }

        // replace the old file
        String orignalFilePath = mgfFile.getAbsolutePath();
        boolean fileDeleted = mgfFile.delete();

        if (!fileDeleted) {
            throw new IOException("Failed to delete the original spectrum file.");
        }

        boolean fileRenamed = tempSpectrumFile.renameTo(new File(orignalFilePath));

        if (!fileRenamed) {
            throw new IOException("Failed to replace the original spectrum file.");
        }
    }

    /**
     * Add missing precursor charges.
     *
     * @param mgfFile the MGF file to fix
     * @param waitingHandler a waitingHandler showing the progress, can be null
     *
     * @throws FileNotFoundException Exception thrown whenever the file is not
     * found
     * @throws IOException Exception thrown whenever an error occurs while
     * reading the file
     * @throws UnsupportedEncodingException if the decoding of a spectrum title
     * fails
     */
    public static void addMissingPrecursorCharges(
            File mgfFile,
            WaitingHandler waitingHandler
    ) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        UtilitiesUserParameters userPreferences = UtilitiesUserParameters.loadUserParameters();

        try ( BufferedRandomAccessFile br = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100)) {

            String lineBreak = System.getProperty("line.separator");

            long progressUnit = br.length() / 100;

            try ( BufferedWriter bw = new BufferedWriter(new FileWriter(tempSpectrumFile))) {

                String line;
                boolean chargeFound = false;
                boolean insideSpectrum = false;

                while ((line = br.readLine()) != null) {

                    if (line.startsWith("BEGIN IONS")) {

                        insideSpectrum = true;
                        chargeFound = false;

                        if (waitingHandler != null) {
                            if (waitingHandler.isRunCanceled()) {
                                break;
                            }
                            waitingHandler.setSecondaryProgressCounter((int) (br.getFilePointer() / progressUnit));
                        }
                    } else if (line.startsWith("END IONS")) {
                        insideSpectrum = false;
                    } else if (line.startsWith("CHARGE")) {
                        chargeFound = true;
                    } else if (!line.equals("")) {

                        if (insideSpectrum && !chargeFound) {

                            try {
                                String values[] = line.split("\\s+");
                                Double.parseDouble(values[0]);
                                Double.parseDouble(values[1]);

                                // we're inside the peak list
                                bw.write("CHARGE=");

                                for (int i = userPreferences.getMinSpectrumChargeRange(); i <= userPreferences.getMaxSpectrumChargeRange(); i++) {
                                    if (i > userPreferences.getMinSpectrumChargeRange()) {
                                        bw.write(" and ");
                                    }
                                    bw.write(i + "+");
                                }

                                bw.write(lineBreak);
                                chargeFound = true;

                            } catch (Exception e1) {
                                // ignore comments and all other lines
                            }
                        }
                    }

                    bw.write(line);
                    bw.write(lineBreak);
                }
            }
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        }

        // replace the old file
        String orignalFilePath = mgfFile.getAbsolutePath();
        boolean fileDeleted = mgfFile.delete();

        if (!fileDeleted) {
            throw new IOException("Failed to delete the original spectrum file.");
        }

        boolean fileRenamed = tempSpectrumFile.renameTo(new File(orignalFilePath));

        if (!fileRenamed) {
            throw new IOException("Failed to replace the original spectrum file.");
        }
    }

    /**
     * Removes zero intensity peaks.
     *
     * @param mgfFile the MGF file to fix
     * @param waitingHandler a waitingHandler showing the progress, can be null
     *
     * @throws FileNotFoundException Exception thrown whenever the file is not
     * found
     * @throws IOException Exception thrown whenever an error occurs while
     * reading the file
     * @throws UnsupportedEncodingException if the decoding of a spectrum title
     * fails
     */
    public static void removeZeroes(
            File mgfFile,
            WaitingHandler waitingHandler
    ) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        try ( BufferedRandomAccessFile br = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100)) {

            long progressUnit = br.length() / 100;

            try ( BufferedWriter bw = new BufferedWriter(new FileWriter(tempSpectrumFile))) {

                String line;
                boolean spectrum = false;

                while ((line = br.readLine()) != null) {

                    if (line.startsWith("BEGIN IONS")) {
                        spectrum = true;

                        if (waitingHandler != null) {
                            if (waitingHandler.isRunCanceled()) {
                                break;
                            }
                            waitingHandler.setSecondaryProgressCounter((int) (br.getFilePointer() / progressUnit));
                        }

                    } else if (line.startsWith("END IONS")) {
                        spectrum = false;
                    }

                    boolean peak = true;
                    boolean zero = false;
                    String[] split = line.split(" ");
                    if (split.length != 2 && split.length != 3) {
                        split = line.split("\t");
                        if (split.length != 2 && split.length != 3) {
                            peak = false;
                        }
                    }
                    if (peak) {
                        try {
                            Double.parseDouble(split[0]);
                        } catch (Exception e) {
                            peak = false;
                        }
                        if (peak) {
                            try {
                                Double intensity = new Double(split[1]);
                                if (intensity == 0.0) {
                                    zero = true;
                                }
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Line not recognized:\n" + line);
                            }
                        }
                    }

                    if (!spectrum || !peak || !zero) {
                        bw.write(line);
                        bw.newLine();
                    }
                }
            }
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        }

        // replace the old file
        String orignalFilePath = mgfFile.getAbsolutePath();
        boolean fileDeleted = mgfFile.delete();

        if (!fileDeleted) {
            throw new IOException("Failed to delete the original spectrum file."); // can sometimes happeen of the file is loaded twice in the gui, e.g., once with cancel for zero removal and one with ok
        }

        boolean fileRenamed = tempSpectrumFile.renameTo(new File(orignalFilePath));

        if (!fileRenamed) {
            throw new IOException("Failed to replace the original spectrum file.");
        }
    }

    /**
     * Renames duplicate spectrum titles. Adds (2), (3) etc, behind the
     * duplicate spectrum titles.
     *
     * @param mgfFile the MGF file to validate
     * @param waitingHandler a waitingHandler showing the progress
     * @throws FileNotFoundException Exception thrown whenever the file is not
     * found
     * @throws IOException Exception thrown whenever an error occurs while
     * reading the file
     * @throws UnsupportedEncodingException if the decoding of a spectrum title
     * fails
     */
    public static void renameDuplicateSpectrumTitles(
            File mgfFile,
            WaitingHandler waitingHandler
    ) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        ArrayList<String> spectrumTitles = new ArrayList<>();
        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        try ( BufferedWriter bw = new BufferedWriter(new FileWriter(tempSpectrumFile))) {

            try ( BufferedReader br = new BufferedReader(new FileReader(mgfFile))) {

                String lineBreak = System.getProperty("line.separator");

                String line = br.readLine();

                while (line != null) {

                    if (line.startsWith("TITLE")) {

                        if (waitingHandler != null) {
                            if (waitingHandler.isRunCanceled()) {
                                break;
                            }
                            //waitingHandler.setSecondaryProgressCounter((int) (br.getFilePointer() / progressUnit)); // @TODO: use the waitingHandler??
                        }

                        String originalTitle = line.substring(line.indexOf('=') + 1);

                        try {
                            originalTitle = URLDecoder.decode(originalTitle, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            throw new UnsupportedEncodingException("An exception was thrown when trying to decode an mgf title: " + originalTitle);
                        }

                        String tempTitle = originalTitle;
                        int counter = 2;
                        while (spectrumTitles.contains(tempTitle)) {
                            tempTitle = originalTitle + " (" + counter++ + ")";
                        }

                        spectrumTitles.add(tempTitle);
                        bw.write("TITLE=" + tempTitle + lineBreak);
                    } else {
                        bw.write(line + lineBreak);
                    }

                    line = br.readLine();
                    
                }
            }
        }

        // replace the old file
        String orignalFilePath = mgfFile.getAbsolutePath();
        boolean fileDeleted = mgfFile.delete();

        if (!fileDeleted) {
            throw new IOException("Failed to delete the original spectrum file.");
        }

        boolean fileRenamed = tempSpectrumFile.renameTo(new File(orignalFilePath));

        if (!fileRenamed) {
            throw new IOException("Failed to replace the original spectrum file.");
        }
    }

    /**
     * Splits an mgf file into smaller ones and returns the indexes of the
     * generated files.
     *
     * @param mgfFile the mgf file to split
     * @param nSpectra the number of spectra allowed in the smaller files
     * @param waitingHandler the waitingHandler showing the progress
     * 
     * @return a list of indexes of the generated files
     * 
     * @throws FileNotFoundException exception thrown whenever a file was not
     * found
     * @throws IOException exception thrown whenever a problem occurred while
     * reading/writing a file
     */
    public static ArrayList<MgfIndex> splitFile(
            File mgfFile, 
            int nSpectra, 
            WaitingHandler waitingHandler
    ) throws FileNotFoundException, IOException {

        String fileName = mgfFile.getName();

        if (fileName.toLowerCase().endsWith(".mgf")) {

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxSecondaryProgressCounter(100);
                waitingHandler.setSecondaryProgressCounter(0);
            }

            String splittedName = fileName.substring(0, fileName.lastIndexOf("."));
            ArrayList<File> splittedFiles = new ArrayList<>();

            int fileCounter = 1, spectrumCounter = 0;
            String currentName = splittedName + "_" + fileCounter + ".mgf";
            File testFile = new File(mgfFile.getParent(), currentName);
            splittedFiles.add(testFile);

            BufferedRandomAccessFile writeBufferedRandomAccessFile = new BufferedRandomAccessFile(testFile, "rw", 1024 * 100);
            BufferedRandomAccessFile readBufferedRandomAccessFile = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
            String lineBreak = System.getProperty("line.separator");

            long sizeOfReadAccessFile = readBufferedRandomAccessFile.length(), lastIndex = 0;
            long progressUnit = sizeOfReadAccessFile / 100;
            String line;

            while ((line = readBufferedRandomAccessFile.getNextLine()) != null) {

                // fix for lines ending with \r
                if (line.endsWith("\r")) {
                    line = line.replace("\r", "");
                }

                if (line.startsWith("BEGIN IONS")) {

                    spectrumCounter++;

                    long readIndex = readBufferedRandomAccessFile.getFilePointer();

                    if (spectrumCounter > nSpectra) {
                        if (sizeOfReadAccessFile - readIndex > (readIndex - lastIndex) / 2) { // try to avoid small leftovers
                            writeBufferedRandomAccessFile.close();
                            currentName = splittedName + "_" + ++fileCounter + ".mgf";
                            testFile = new File(mgfFile.getParent(), currentName);
                            splittedFiles.add(testFile);
                            lastIndex = readIndex;
                            spectrumCounter = 0;
                            writeBufferedRandomAccessFile = new BufferedRandomAccessFile(testFile, "rw", 1024 * 100);
                        }
                    }

                    if (waitingHandler != null) {
                        if (waitingHandler.isRunCanceled()) {
                            break;
                        }
                        waitingHandler.setSecondaryProgressCounter((int) (readIndex / progressUnit));
                    }
                }

                writeBufferedRandomAccessFile.writeBytes(line + lineBreak);
            }

            writeBufferedRandomAccessFile.close();
            readBufferedRandomAccessFile.close();

            // index the new files
            ArrayList<MgfIndex> mgfIndexes = new ArrayList<>();
            for (int i = 0; i < splittedFiles.size(); i++) {
                File newFile = splittedFiles.get(i);

                if (waitingHandler != null) {
                    waitingHandler.setWaitingText("Indexing New Files " + (i + 1) + "/" + splittedFiles.size() + ". Please Wait...");
                }

                mgfIndexes.add(IndexedMgfReader.getMgfIndex(newFile, waitingHandler));
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(true);
            }

            return mgfIndexes;

        } else {
            throw new IllegalArgumentException("Spectrum file format not supported.");
        }
    }
}
