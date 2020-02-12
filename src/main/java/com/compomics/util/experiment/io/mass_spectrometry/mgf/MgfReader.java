package com.compomics.util.experiment.io.mass_spectrometry.mgf;

import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.UtilitiesUserParameters;
import com.compomics.util.waiting.WaitingHandler;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This class will read an MGF file.
 * 
 * @deprecated for testing only
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MgfReader {

    /**
     * General constructor for an mgf reader.
     */
    public MgfReader() {
    }

    /**
     * Returns the index of all spectra in the given mgf file.
     *
     * @param mgfFile the given mgf file
     * @return the index of all spectra
     * @throws FileNotFoundException Exception thrown whenever the file is not
     * found
     * @throws IOException Exception thrown whenever an error occurs while
     * reading the file
     */
    public static MgfIndex getIndexMap(File mgfFile) throws FileNotFoundException, IOException {
        return getIndexMap(mgfFile, null);
    }

    /**
     * Returns the index of all spectra in the given MGF file.
     *
     * @param mgfFile the given MGF file
     * @param waitingHandler a waitingHandler showing the progress
     * @return the index of all spectra
     * @throws FileNotFoundException Exception thrown whenever the file is not
     * found
     * @throws IOException Exception thrown whenever an error occurs while
     * reading the file
     */
    public static MgfIndex getIndexMap(File mgfFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        HashMap<String, Long> indexes = new HashMap<>();
        HashMap<String, Integer> spectrumIndexes = new HashMap<>();
        HashMap<Integer, Double> precursorMzMap = new HashMap<>();
        LinkedHashSet<String> spectrumTitles = new LinkedHashSet<>();
        HashMap<String, Integer> duplicateTitles = new HashMap<>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
        long currentIndex = 0;
        String title = null;
        int spectrumCounter = 0;
        double maxRT = -1, minRT = Double.MAX_VALUE, maxMz = -1, maxIntensity = 0;
        int maxCharge = 0, maxPeakCount = 0, peakCount = 0;
        boolean peakPicked = true;
        boolean precursorChargesMissing = false;

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String line;
        boolean insideSpectrum = false;
        boolean chargeTagFound = false;

        while ((line = bufferedRandomAccessFile.getNextLine()) != null) {

            // fix for lines ending with \r
            if (line.endsWith("\r")) {
                line = line.replace("\r", "");
            }

            if (line.equals("BEGIN IONS")) {
                insideSpectrum = true;
                chargeTagFound = false;
                currentIndex = bufferedRandomAccessFile.getFilePointer();
                spectrumCounter++;
                peakCount = 0;
                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                    waitingHandler.setSecondaryProgressCounter((int) (currentIndex / progressUnit));
                }
            } else if (line.startsWith("TITLE")) {

                title = line.substring(line.indexOf('=') + 1);

                try {
                    title = URLDecoder.decode(title, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    if (waitingHandler != null) {
                        waitingHandler.appendReport("An exception was thrown when trying to decode an mgf title: " + title, true, true);
                    }
                    System.out.println("An exception was thrown when trying to decode an mgf title: " + title);
                    e.printStackTrace();
                }
                Integer nDuplicates = duplicateTitles.get(title);
                if (nDuplicates != null || spectrumTitles.contains(title)) {
                    if (nDuplicates == null) {
                        nDuplicates = 0;
                        System.err.println("Warning: Spectrum title " + title + " is not unique in " + mgfFile.getName() + "!");
                    }
                    duplicateTitles.put(title, ++nDuplicates);
                    title += "_" + nDuplicates;
                }
                spectrumTitles.add(title);
                indexes.put(title, currentIndex);
                spectrumIndexes.put(title, spectrumCounter - 1);
            } else if (line.startsWith("CHARGE")) {
                int[] precursorCharges = parseCharges(line);
                for (int charge : precursorCharges) {
                    if (charge > maxCharge) {
                        maxCharge = charge;
                    }
                }
                chargeTagFound = true;
            } else if (line.startsWith("PEPMASS")) {
                String temp = line.substring(line.indexOf("=") + 1);
                String[] values = temp.split("\\s");
                double precursorMz = Double.parseDouble(values[0]);

                if (precursorMz > maxMz) {
                    maxMz = precursorMz;
                }

                if (values.length > 1) {
                    double precursorIntensity = Double.parseDouble(values[1]);

                    if (precursorIntensity > maxIntensity) {
                        maxIntensity = precursorIntensity;
                    }
                }

                precursorMzMap.put(spectrumCounter - 1, precursorMz);

            } else if (line.startsWith("RTINSECONDS")) {

                String rtInput = "";

                try {
                    rtInput = line.substring(line.indexOf('=') + 1);
                    String[] rtWindow = rtInput.split("-");

                    if (rtWindow.length == 1) {
                        String tempRt = rtWindow[0];
                        // possible fix for values like RTINSECONDS=PT121.250000S
                        if (tempRt.startsWith("PT") && tempRt.endsWith("S")) {
                            tempRt = tempRt.substring(2, tempRt.length() - 1);
                        }
                        double rt = new Double(tempRt);
                        if (rt > maxRT) {
                            maxRT = rt;
                        }
                        if (rt < minRT) {
                            minRT = rt;
                        }
                    } else if (rtWindow.length == 2 && !rtWindow[0].equals("")) {
                        double rt1 = new Double(rtWindow[0]);
                        if (rt1 > maxRT) {
                            maxRT = rt1;
                        }
                        if (rt1 < minRT) {
                            minRT = rt1;
                        }
                        double rt2 = new Double(rtWindow[1]);
                        if (rt2 > maxRT) {
                            maxRT = rt2;
                        }
                        if (rt2 < minRT) {
                            minRT = rt2;
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse retention time: " + rtInput);
                }
            } else if (line.startsWith("END IONS")) {
                insideSpectrum = false;
                if (title != null) {
                    if (peakCount > maxPeakCount) {
                        maxPeakCount = peakCount;
                    }
                }
                title = null;
                if (!chargeTagFound) {
                    precursorChargesMissing = true;
                }
            } else if (insideSpectrum && !line.equals("")) {
                try {
                    String values[] = line.split("\\s+");
                    //Double mz = new Double(values[0]);
                    Double intensity = new Double(values[1]);
                    if (peakPicked && intensity == 0) {
                        peakPicked = false;
                    }
                    peakCount++;
                } catch (Exception e1) {
                    // ignore comments and all other lines
                }
            }
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        }

        bufferedRandomAccessFile.close();

        if (minRT == Double.MAX_VALUE) {
            minRT = 0;
        }

        // convert the spectrum titles to an arraylist
        ArrayList<String> spectrumTitlesAsArrayList = new ArrayList<>(spectrumTitles.size()); // @TODO: is there a faster way of doing this?
        for (String temp : spectrumTitles) {
            spectrumTitlesAsArrayList.add(temp);
        }

        return new MgfIndex(spectrumTitlesAsArrayList, duplicateTitles, indexes, spectrumIndexes, precursorMzMap, mgfFile.getName(), minRT, maxRT,
                maxMz, maxIntensity, maxCharge, maxPeakCount, peakPicked, precursorChargesMissing, mgfFile.lastModified(), spectrumCounter);
    }

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
    public static void removeDuplicateSpectrumTitles(File mgfFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        ArrayList<String> spectrumTitles = new ArrayList<>();
        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        BufferedRandomAccessFile br = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
        String lineBreak = System.getProperty("line.separator");

        try {
            long progressUnit = br.length() / 100;
            FileWriter fw = new FileWriter(tempSpectrumFile);

            try {
                BufferedWriter bw = new BufferedWriter(fw);

                try {
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

                } finally {
                    bw.close();
                }
            } finally {
                fw.close();
            }
        } finally {
            br.close();
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
    public static void addMissingSpectrumTitles(File mgfFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        ArrayList<String> spectrumTitles = new ArrayList<>();

        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        BufferedRandomAccessFile br = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
        String lineBreak = System.getProperty("line.separator");

        try {
            long progressUnit = br.length() / 100;

            FileWriter fw = new FileWriter(tempSpectrumFile);
            try {
                BufferedWriter bw = new BufferedWriter(fw);
                try {

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
                } finally {
                    bw.close();
                }
            } finally {
                fw.close();
            }
        } finally {
            br.close();
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
    public static void addMissingPrecursorCharges(File mgfFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        UtilitiesUserParameters userPreferences = UtilitiesUserParameters.loadUserParameters();

        BufferedRandomAccessFile br = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
        String lineBreak = System.getProperty("line.separator");

        try {
            long progressUnit = br.length() / 100;

            FileWriter fw = new FileWriter(tempSpectrumFile);

            try {
                BufferedWriter bw = new BufferedWriter(fw);
                try {

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
                                    new Double(values[0]);
                                    new Double(values[1]);

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
                } finally {
                    bw.close();
                }
            } finally {
                fw.close();
            }
        } finally {
            br.close();
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
    public static void removeZeroes(File mgfFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(100);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        BufferedRandomAccessFile br = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);

        try {
            long progressUnit = br.length() / 100;

            FileWriter fw = new FileWriter(tempSpectrumFile);
            try {
                BufferedWriter bw = new BufferedWriter(fw);
                try {

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
                                new Double(split[0]);
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
                } finally {
                    bw.close();
                }
            } finally {
                fw.close();
            }
        } finally {
            br.close();
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
    public static void renameDuplicateSpectrumTitles(File mgfFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, UnsupportedEncodingException {

        ArrayList<String> spectrumTitles = new ArrayList<>();
        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        FileWriter fw = new FileWriter(tempSpectrumFile);
        BufferedWriter bw = new BufferedWriter(fw);
        FileReader fr = new FileReader(mgfFile);
        BufferedReader br = new BufferedReader(fr);
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

        br.close();
        fr.close();

        bw.close();
        fw.close();

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
     * @return a list of indexes of the generated files
     * @throws FileNotFoundException exception thrown whenever a file was not
     * found
     * @throws IOException exception thrown whenever a problem occurred while
     * reading/writing a file
     */
    public ArrayList<MgfIndex> splitFile(File mgfFile, int nSpectra, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

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

                mgfIndexes.add(getIndexMap(newFile, waitingHandler));
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

    /**
     * Returns the next spectrum starting from the given index.
     *
     * @param bufferedRandomAccessFile The random access file of the inspected
     * mgf file
     * @param index The index where to start looking for the spectrum
     * @param fileName The name of the MGF file
     * 
     * @return The next spectrum encountered
     * 
     * @throws IOException Exception thrown whenever an error is encountered
     * while reading the spectrum
     */
    public static Spectrum getSpectrum(BufferedRandomAccessFile bufferedRandomAccessFile, long index, String fileName) throws IOException {

        // @TODO get fileName from the random access file?
        bufferedRandomAccessFile.seek(index);
        double precursorMz = 0, precursorIntensity = 0, rt = -1.0, rt1 = -1, rt2 = -1;
        int[] precursorCharges = null;
        String spectrumTitle = "";
        boolean insideSpectrum = false;
        ArrayList<Double> mzList = new ArrayList<>(0);
        ArrayList<Double> intensityList = new ArrayList<>(0);

        String line;
        while ((line = bufferedRandomAccessFile.getNextLine()) != null) {

            // fix for lines ending with \r
            if (line.endsWith("\r")) {
                line = line.replace("\r", "");
            }

            if (line.startsWith("BEGIN IONS")) {
                insideSpectrum = true;
                mzList = new ArrayList<>();
        intensityList = new ArrayList<>();
            } else if (line.startsWith("TITLE")) {
                insideSpectrum = true;
                spectrumTitle = line.substring(line.indexOf('=') + 1);
                try {
                    spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    System.out.println("An exception was thrown when trying to decode an mgf title: " + spectrumTitle);
                    e.printStackTrace();
                }
            } else if (line.startsWith("CHARGE")) {
                precursorCharges = parseCharges(line);
            } else if (line.startsWith("PEPMASS")) {
                String temp = line.substring(line.indexOf("=") + 1);
                String[] values = temp.split("\\s");
                precursorMz = Double.parseDouble(values[0]);
                if (values.length > 1) {
                    precursorIntensity = Double.parseDouble(values[1]);
                } else {
                    precursorIntensity = 0.0;
                }
            } else if (line.startsWith("RTINSECONDS")) {
                try {
                    String rtInput = line.substring(line.indexOf('=') + 1);
                    String[] rtWindow = rtInput.split("-");
                    if (rtWindow.length == 1) {
                        String tempRt = rtWindow[0];
                        // possible fix for values like RTINSECONDS=PT121.250000S
                        if (tempRt.startsWith("PT") && tempRt.endsWith("S")) {
                            tempRt = tempRt.substring(2, tempRt.length() - 1);
                        }
                        rt = new Double(tempRt);
                    } else if (rtWindow.length == 2) {
                        rt1 = new Double(rtWindow[0]);
                        rt2 = new Double(rtWindow[1]);
                    }
                } catch (Exception e) {
                    System.out.println("An exception was thrown when trying to decode the retention time: " + spectrumTitle);
                    e.printStackTrace();
                    // ignore exception, RT will not be parsed
                }
            } else if (line.startsWith("TOLU")) {
                // peptide tolerance unit not implemented
            } else if (line.startsWith("TOL")) {
                // peptide tolerance not implemented
            } else if (line.startsWith("SEQ")) {
                // sequence qualifier not implemented
            } else if (line.startsWith("COMP")) {
                // composition qualifier not implemented
            } else if (line.startsWith("ETAG")) {
                // error tolerant search sequence tag not implemented
            } else if (line.startsWith("TAG")) {
                // sequence tag not implemented
            } else if (line.startsWith("SCANS")) {
                // scan number not implemented
            } else if (line.startsWith("INSTRUMENT")) {
                // ion series not implemented
            } else if (line.startsWith("END IONS")) {
                insideSpectrum = false;
                Precursor precursor;
                if (rt1 != -1 && rt2 != -1) {
                    precursor = new Precursor(precursorMz, precursorIntensity, precursorCharges, rt1, rt2);
                } else {
                    precursor = new Precursor(rt, precursorMz, precursorIntensity, precursorCharges);
                }
                double[] mzArray = mzList.stream()
                        .mapToDouble(
                                a -> a
                        )
                        .toArray();
                double[] intensityArray = intensityList.stream()
                        .mapToDouble(
                                a -> a
                        )
                        .toArray();
                Spectrum spectrum = new Spectrum(precursor, mzArray, intensityArray);
                
                return spectrum;
                
            } else if (insideSpectrum && !line.equals("")) {
                try {
                    String values[] = line.split("\\s+");
                    double mz = Double.parseDouble(values[0]);
                    mzList.add(mz);
                    double intensity = Double.parseDouble(values[1]);
                    intensityList.add(intensity);
                } catch (Exception e1) {
                    // ignore comments and all other lines
                }
            }
        }

        throw new IllegalArgumentException("End of the file reached before encountering the tag \"END IONS\".");
    }

    /**
     * Parses the charge line of an MGF files.
     *
     * @param chargeLine the charge line
     * @return the possible charges found
     */
    private static int[] parseCharges(String chargeLine) {

        ArrayList<Integer> result = new ArrayList<>(1);
        String tempLine = chargeLine.substring(chargeLine.indexOf("=") + 1);
        String[] chargesAnd = tempLine.split(" and ");
        ArrayList<String> chargesAsString = new ArrayList<>();

        for (String charge : chargesAnd) {
            for (String charge2 : charge.split(",")) {
                chargesAsString.add(charge2.trim());
            }
        }

        for (String chargeAsString : chargesAsString) {

            chargeAsString = chargeAsString.trim();

            if (!chargeAsString.isEmpty()) {
                try {
                    if (chargeAsString.endsWith("+")) {
                        int value = Integer.parseInt(chargeAsString.substring(0, chargeAsString.length() - 1));
                        result.add(value);
                    } else if (chargeAsString.endsWith("-")) {
                        int value = Integer.parseInt(chargeAsString.substring(0, chargeAsString.length() - 1));
                        result.add(value);
                    } else if (!chargeAsString.equalsIgnoreCase("Mr")) {
                        int value = Integer.parseInt(chargeAsString);
                        result.add(value);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("\'" + chargeAsString + "\' could not be processed as a valid precursor charge!");
                }
            }
        }

        // if empty, add a default charge of 1
        if (result.isEmpty()) {
            result.add(1);
        }

        return result.stream()
                .mapToInt(a -> a)
                .toArray();
    }

    /**
     * Returns the next precursor starting from the given index.
     *
     * @param bufferedRandomAccessFile the random access file of the inspected
     * mgf file
     * @param index the index where to start looking for the spectrum
     * @param fileName the name of the mgf file
     * 
     * @return the next spectrum encountered
     * 
     * @throws IOException thrown whenever an error is encountered while reading
     * the spectrum
     */
    public static Precursor getPrecursor(BufferedRandomAccessFile bufferedRandomAccessFile, Long index, String fileName) throws IOException {

        // @TODO: get fileName from the random access file?
        bufferedRandomAccessFile.seek(index);
        String line, title = null;
        double precursorMz = 0, precursorIntensity = 0, rt = -1.0, rt1 = -1, rt2 = -1;
        int[] precursorCharges = null;

        while ((line = bufferedRandomAccessFile.getNextLine()) != null) {

            // fix for lines ending with \r
            if (line.endsWith("\r")) {
                line = line.replace("\r", "");
            }

            if (line.startsWith("TITLE")) {
                title = line.substring(line.indexOf("=") + 1);
                try {
                    title = URLDecoder.decode(title, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    System.out.println("An exception was thrown when trying to decode an mgf title: " + title);
                    e.printStackTrace();
                }
            } else if (line.startsWith("CHARGE")) {
                precursorCharges = parseCharges(line);
            } else if (line.startsWith("PEPMASS")) {
                String temp = line.substring(line.indexOf("=") + 1);
                String[] values = temp.split("\\s");
                precursorMz = Double.parseDouble(values[0]);
                if (values.length > 1) {
                    precursorIntensity = Double.parseDouble(values[1]);
                } else {
                    precursorIntensity = 0.0;
                }
            } else if (line.startsWith("RTINSECONDS")) {
                try {
                    String rtInput = line.substring(line.indexOf('=') + 1);
                    String[] rtWindow = rtInput.split("-");
                    if (rtWindow.length == 1) {
                        String tempRt = rtWindow[0];
                        if (tempRt.startsWith("PT") && tempRt.endsWith("S")) { // possible fix for values like RTINSECONDS=PT121.250000S
                            tempRt = tempRt.substring(2, tempRt.length() - 1);
                        }
                        rt = new Double(tempRt);
                    } else if (rtWindow.length == 2) {
                        rt1 = new Double(rtWindow[0]);
                        rt2 = new Double(rtWindow[1]);
                    }
                } catch (Exception e) {
                    System.out.println("An exception was thrown when trying to decode the retention time: " + title);
                    e.printStackTrace(); // ignore exception, RT will not be parsed
                }
            } else if (!line.isEmpty()) {
                if (line.startsWith("END IONS") || (!line.contains("#") && !line.contains("="))) {
                    if (rt1 != -1 && rt2 != -1) {
                        return new Precursor(precursorMz, precursorIntensity, precursorCharges, rt1, rt2);
                    }
                    return new Precursor(rt, precursorMz, precursorIntensity, precursorCharges);
                }
            }
        }

        throw new IllegalArgumentException("End of the file reached before encountering the tag \"END IONS\". File: " + fileName + ", title: " + title);
    }
}
