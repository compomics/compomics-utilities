/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.io.massspectrometry;

import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.waiting.WaitingHandler;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 *
 * @author Genet
 */
public class MspReader extends MgfReader{
    
    public MspReader() {
    }

    public static MSnSpectrum getSpectrum(BufferedReader br, String fileName) throws IOException {

        String line;
        HashMap<Double, Peak> spectrum = new HashMap<Double, Peak>();
        double precursorMz = 0;
        double precursorIntensity = 0;//not assigned in msp file format case
        double rt = -1.0;//not assigned in msp file format case
        double rt1 = -1.0;//not assigned in msp file format case
        double rt2 = -1.0;//not assigned in msp file format case
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>();
        String scanNumber = "";//not assigned in msp file format case
        String spectrumTitle = "";//msp spetrum name should be assigned for spetrumTitle as there is no spectrum title in msp file format

        String spectrumName = "";//added for msp format
        int numberofPeaks = 0;//added for msp format
        double molecularWeight = 0.0;//added for msp format

        boolean insideSpectrum = false;

        while ((line = br.readLine()) != null) {

            // fix for lines ending with \r
            if (line.endsWith("\r")) {
                line = line.replace("\r", "");
            }

            if (line.startsWith("Name:")) {
                // reset the spectrum details
                insideSpectrum = true;
                spectrumName = line.substring(line.indexOf(':') + 1, line.indexOf('/'));

                try {
                    spectrumName = URLDecoder.decode(spectrumName, "utf-8");
                    precursorCharges = parseCharges(line.substring(line.indexOf('/' + 1)));
                } catch (UnsupportedEncodingException e) {
                    System.out.println("An exception was thrown when trying to decode the msp title '" + spectrumName + "'.");
                    e.printStackTrace();
                }
            } else if (line.startsWith("MW:")) {
                try {
                    molecularWeight = Double.parseDouble(line.substring(line.indexOf(':') + 1));
                } catch (Exception e) {
                    System.out.println("An exception was thrown when trying to decode the msp Molecular Weight'" + molecularWeight + "'.");
                    e.printStackTrace();
                }
            } else if (line.startsWith("Comment")) {
                String temp = line.substring(line.indexOf("Parent=" + 7), line.indexOf("" + 1));
                //String[] values = temp.split("\\s");
                precursorMz = Double.parseDouble(temp);
                if (line.contains("Scan")) {
                    scanNumber = line.substring(line.indexOf("Scan=" + 5), line.indexOf(""));

                }
                if (line.contains("Origfile")) {
                    spectrumTitle = line.substring(line.indexOf("Origfile=" + 9), line.indexOf(""));
                }

            } else if (line.startsWith("Num peaks:")) {
                String temp = line.substring(line.indexOf('=') + 1);
                try {
                    numberofPeaks = Integer.parseInt(temp);
                } catch (Exception e) {
                    System.out.println("An exception was thrown when trying to decode the number of peaks " + numberofPeaks + ".");
                    e.printStackTrace();
                    // ignore exception, RT will not be parsed
                }
            } else if (line.equals("")) {
                insideSpectrum = false;
                Precursor precursor;
                if (rt1 != -1 && rt2 != -1) {
                    precursor = new Precursor(precursorMz, precursorIntensity, precursorCharges, rt1, rt2);
                } else {
                    precursor = new Precursor(rt, precursorMz, precursorIntensity, precursorCharges);
                }
                MSnSpectrum msnSpectrum = new MSnSpectrum(2, precursor, spectrumTitle, spectrum, fileName);
                if (scanNumber.length() > 0) {
                    msnSpectrum.setScanNumber(scanNumber);
                }
                return msnSpectrum;
            } else if (insideSpectrum) {
                try {
                    String values[] = line.split("\\s+");
                    Double mz = new Double(values[0]);
                    Double intensity = new Double(values[1]);
                    spectrum.put(mz, new Peak(mz, intensity));
                } catch (Exception e1) {
                    // ignore comments and all other lines
                }
            }
        }

        return null;

    }   
       
    
    /**
     * Reads an MGF file and retrieves a list of spectra.
     *
     * @param aFile the mgf file
     * @return list of MSnSpectra imported from the file
     * @throws FileNotFoundException Exception thrown if a problem is
     * encountered reading the file
     * @throws IOException Exception thrown if a problem is encountered reading
     * the file
     * @throws IllegalArgumentException thrown when a parameter in the file
     * cannot be parsed correctly
     */
    @Override
    public ArrayList<MSnSpectrum> getSpectra(File aFile) throws FileNotFoundException, IOException, IllegalArgumentException {

        ArrayList<MSnSpectrum> spectra = new ArrayList<MSnSpectrum>();
        BufferedReader br = new BufferedReader(new FileReader(aFile));
        try {
            MSnSpectrum spectrum;
            while ((spectrum = getSpectrum(br, aFile.getName())) != null) {
                spectra.add(spectrum);
            }
        } finally {
            br.close();
        }
        return spectra;
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

        HashMap<String, Long> indexes = new HashMap<String, Long>();
        HashMap<String, Integer> spectrumIndexes = new HashMap<String, Integer>();
        HashMap<Integer, Double> precursorMzMap = new HashMap<Integer, Double>();
        LinkedHashSet<String> spectrumTitles = new LinkedHashSet<String>();
        HashMap<String, Integer> duplicateTitles = new HashMap<String, Integer>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
        long currentIndex = 0;
        String title = null,spectrumName=null;
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
            
            if (line.equals("Name:")) {
                insideSpectrum = true;
                chargeTagFound = false;
                
                currentIndex = bufferedRandomAccessFile.getFilePointer();
                spectrumCounter++;
                peakCount = 0;
                spectrumName = line.substring(line.indexOf(':') + 1, line.indexOf('/'));

                try {
                    spectrumName = URLDecoder.decode(spectrumName, "utf-8");
                    ArrayList<Charge> precursorCharges = parseCharges(line.substring(line.indexOf('/' + 1)));
                    for (Charge charge : precursorCharges) {
                    if (charge.value > maxCharge) {
                        maxCharge = charge.value;
                    }
                }
                chargeTagFound = true;
                    
                } catch (UnsupportedEncodingException e) {
                    System.out.println("An exception was thrown when trying to decode the msp title '" + spectrumName + "'.");
                    e.printStackTrace();
                }
                
                
                
                
                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                    waitingHandler.setSecondaryProgressCounter((int) (currentIndex / progressUnit));
                }
            } else if (line.startsWith("Comment")) {                
                
                
                
                String temp = line.substring(line.indexOf("Parent=" + 7), line.indexOf("" + 1));
                
                double precursorMz =  Double.parseDouble(temp);

                if (precursorMz > maxMz) {
                    maxMz = precursorMz;
                }


                precursorMzMap.put(spectrumCounter - 1, precursorMz);

            } else if (line.startsWith("")) {
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
            } else if (insideSpectrum) {
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
//
//        if (minRT == Double.MAX_VALUE) {
//            minRT = 0;
//        }

        // convert the spectrum titles to an arraylist
        ArrayList<String> spectrumTitlesAsArrayList = new ArrayList<String>(); // @TODO: is there a faster way of doing this?
        for (String temp : spectrumTitles) {
            spectrumTitlesAsArrayList.add(temp);
        }

        return new MgfIndex(spectrumTitlesAsArrayList, duplicateTitles, indexes, spectrumIndexes, precursorMzMap, mgfFile.getName(), minRT, maxRT,
                maxMz, maxIntensity, maxCharge, maxPeakCount, peakPicked, precursorChargesMissing, mgfFile.lastModified(), spectrumCounter);
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

        ArrayList<String> spectrumTitles = new ArrayList<String>();

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
                    String title = null, spectrumName=null;
                    int spectrumCounter = 0;

                    while ((line = br.readLine()) != null) {

                        if (line.startsWith("Name:")) {
                            spectrumCounter++;
                            currentSpectrum += line + lineBreak;

                            title = line.substring(line.indexOf('=') + 1);
                            try {
                                title = URLDecoder.decode(title, "utf-8");
                                spectrumName=title;
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                throw new UnsupportedEncodingException("An exception was thrown when trying to decode an mgf title: " + title);
                            }
                              spectrumTitles.add(title);
                            

                            if (waitingHandler != null) {
                                if (waitingHandler.isRunCanceled()) {
                                    break;
                                }
                                waitingHandler.setSecondaryProgressCounter((int) (br.getFilePointer() / progressUnit));
                            }

                        }else if (line.startsWith("")) {

                           bw.write("Name: ");

                            if (title == null) {
                                title = "Spectrum " + spectrumCounter;
                                while (spectrumTitles.contains(title)) {
                                    title = "Spectrum " + ++spectrumCounter;
                                }
                                spectrumTitles.add(title);
                                bw.write("TITLE=" + title + lineBreak);
                            }

                            bw.write(currentSpectrum);
                            bw.write("" + lineBreak);
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

                        if (line.startsWith("Name:")) {
                            spectrum = true;

                            if (waitingHandler != null) {
                                if (waitingHandler.isRunCanceled()) {
                                    break;
                                }
                                waitingHandler.setSecondaryProgressCounter((int) (br.getFilePointer() / progressUnit));
                            }

                        } else if (line.startsWith("")) {
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

        ArrayList<String> spectrumTitles = new ArrayList<String>();
        File tempSpectrumFile = new File(mgfFile.getParentFile(), mgfFile.getName() + "_temp");

        FileWriter fw = new FileWriter(tempSpectrumFile);
        BufferedWriter bw = new BufferedWriter(fw);
        FileReader fr = new FileReader(mgfFile);
        BufferedReader br = new BufferedReader(fr);
        String lineBreak = System.getProperty("line.separator");

        String line = br.readLine();

        while (line != null) {

            if (line.startsWith("Name:")) {

                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                    //waitingHandler.setSecondaryProgressCounter((int) (br.getFilePointer() / progressUnit)); // @TODO: use the waitingHandler??
                }

                String originalTitle = line.substring(line.indexOf(':') + 1);

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
                bw.write("Name:" + tempTitle + lineBreak);
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

        if (fileName.toLowerCase().endsWith(".msp")) {

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxSecondaryProgressCounter(100);
                waitingHandler.setSecondaryProgressCounter(0);
            }

            String splittedName = fileName.substring(0, fileName.lastIndexOf("."));
            ArrayList<File> splittedFiles = new ArrayList<File>();

            int fileCounter = 1, spectrumCounter = 0;
            String currentName = splittedName + "_" + fileCounter + ".msp";
            File testFile = new File(mgfFile.getParent(), currentName);
            splittedFiles.add(testFile);

            BufferedRandomAccessFile writeBufferedRandomAccessFile = new BufferedRandomAccessFile(testFile, "rw", 1024 * 100);
            BufferedRandomAccessFile readBufferedRandomAccessFile = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
            String lineBreak = System.getProperty("line.separator");

            long sizeOfReadAccessFile = readBufferedRandomAccessFile.length(), lastIndex = 0;
            long progressUnit = sizeOfReadAccessFile / 100;
            String line;

            while ((line = readBufferedRandomAccessFile.getNextLine()) != null) {

                if (line.startsWith("Name:")) {

                    spectrumCounter++;

                    long readIndex = readBufferedRandomAccessFile.getFilePointer();

                    if (spectrumCounter > nSpectra) {
                        if (sizeOfReadAccessFile - readIndex > (readIndex - lastIndex) / 2) { // try to avoid small leftovers
                            writeBufferedRandomAccessFile.close();
                            currentName = splittedName + "_" + ++fileCounter + ".msp";
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
            ArrayList<MgfIndex> mgfIndexes = new ArrayList<MgfIndex>();
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
     * @return The next spectrum encountered
     * @throws IOException Exception thrown whenever an error is encountered
     * while reading the spectrum
     * @throws IllegalArgumentException Exception thrown whenever the file is
     * not of a compatible format
     */
    public static MSnSpectrum getSpectrum(BufferedRandomAccessFile bufferedRandomAccessFile, long index, String fileName) throws IOException, IllegalArgumentException {

         //get fileName from the random access file?
         
        bufferedRandomAccessFile.seek(index);
        String line;
        HashMap<Double, Peak> spectrum = new HashMap<Double, Peak>();
        double precursorMz = 0;
        double precursorIntensity = 0;//not assigned in msp file format case
        double rt = -1.0;//not assigned in msp file format case
        double rt1 = -1.0;//not assigned in msp file format case
        double rt2 = -1.0;//not assigned in msp file format case
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>();
        String scanNumber = "";//not assigned in msp file format case
        String spectrumTitle = "";//msp spetrum name should be assigned for spetrumTitle as there is no spectrum title in msp file format

        String spectrumName = "";//added for msp format
        int numberofPeaks = 0;//added for msp format
        double molecularWeight = 0.0;//added for msp format
        
       
        boolean insideSpectrum = false;

        while ((line = bufferedRandomAccessFile.getNextLine()) != null) {

            // fix for lines ending with \r
            if (line.endsWith("\r")) {
                line = line.replace("\r", "");
            }

            if (line.startsWith("Name:")) {
                // reset the spectrum details
                insideSpectrum = true;
                spectrumName = line.substring(line.indexOf(':') + 1, line.indexOf('/'));

                try {
                    spectrumName = URLDecoder.decode(spectrumName, "utf-8");
                    precursorCharges = parseCharges(line.substring(line.indexOf('/' + 1)));
                } catch (UnsupportedEncodingException e) {
                    System.out.println("An exception was thrown when trying to decode the msp title '" + spectrumName + "'.");
                    e.printStackTrace();
                }
            } else if (line.startsWith("MW:")) {
                try {
                    molecularWeight = Double.parseDouble(line.substring(line.indexOf(':') + 1));
                } catch (Exception e) {
                    System.out.println("An exception was thrown when trying to decode the msp Molecular Weight'" + molecularWeight + "'.");
                    e.printStackTrace();
                }
            } else if (line.startsWith("Comment")) {
                String temp = line.substring(line.indexOf("Parent=" + 7), line.indexOf("" + 1));
                //String[] values = temp.split("\\s");
                precursorMz = Double.parseDouble(temp);
                if (line.contains("Scan")) {
                    scanNumber = line.substring(line.indexOf("Scan=" + 5), line.indexOf(""));

                }
                if (line.contains("Origfile")) {
                    spectrumTitle = line.substring(line.indexOf("Origfile=" + 9), line.indexOf(""));
                }

            } else if (line.startsWith("Num peaks:")) {
                String temp = line.substring(line.indexOf('=') + 1);
                try {
                    numberofPeaks = Integer.parseInt(temp);
                } catch (Exception e) {
                    System.out.println("An exception was thrown when trying to decode the number of peaks " + numberofPeaks + ".");
                    e.printStackTrace();
                    // ignore exception, RT will not be parsed
                }
            } else if (line.equals("")) {
                insideSpectrum = false;
                Precursor precursor;
                if (rt1 != -1 && rt2 != -1) {
                    precursor = new Precursor(precursorMz, precursorIntensity, precursorCharges, rt1, rt2);
                } else {
                    precursor = new Precursor(rt, precursorMz, precursorIntensity, precursorCharges);
                }
                MSnSpectrum msnSpectrum = new MSnSpectrum(2, precursor, spectrumTitle, spectrum, fileName);
                if (scanNumber.length() > 0) {
                    msnSpectrum.setScanNumber(scanNumber);
                }
                return msnSpectrum;
            } else if (insideSpectrum) {
                try {
                    String values[] = line.split("\\s+");
                    Double mz = new Double(values[0]);
                    Double intensity = new Double(values[1]);
                    spectrum.put(mz, new Peak(mz, intensity));
                } catch (Exception e1) {
                    // ignore comments and all other lines
                }
            }
        }

        throw new IllegalArgumentException("End of the file reached before encountering the tag \"END IONS\".");
    }

  

    /**
     * Returns the next precursor starting from the given index.
     *
     * @param bufferedRandomAccessFile The random access file of the inspected
     * mgf file
     * @param index The index where to start looking for the spectrum
     * @param fileName The name of the mgf file
     * @return The next spectrum encountered
     * @throws IOException Exception thrown whenever an error is encountered
     * while reading the spectrum
     * @throws IllegalArgumentException Exception thrown whenever the file is
     * not of a compatible format
     */
    public static Precursor getPrecursor(BufferedRandomAccessFile bufferedRandomAccessFile, Long index, String fileName) throws IOException, IllegalArgumentException {

        // @TODO: get fileName from the random access file?
        bufferedRandomAccessFile.seek(index);
        String line, title = null, spectrumName=null;
        double precursorMz = 0, precursorIntensity = 0, rt = -1.0, rt1 = -1, rt2 = -1;
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>(1);

        while ((line = bufferedRandomAccessFile.getNextLine()) != null) {
            
            // fix for lines ending with \r
            if (line.endsWith("\r")) {
                line = line.replace("\r", "");
            }
            
            if (line.startsWith("Name:")) {
                title = line.substring(line.indexOf(":") + 1);
                precursorCharges = parseCharges(line.substring(line.indexOf('/'+1)));
                try {
                    title = URLDecoder.decode(title, "utf-8");
                    spectrumName=title;
                } catch (UnsupportedEncodingException e) {
                    System.out.println("An exception was thrown when trying to decode an mgf title: " + title);
                    e.printStackTrace();
                }
            }  else if (line.startsWith("Comment")) {
                String temp = line.substring(line.indexOf("Parent=" + 7), line.indexOf("" + 1));
               
                precursorMz = Double.parseDouble(temp);                
                precursorIntensity = 0.0;
               
            }  else if (!line.isEmpty()) {
                if (line.startsWith("")) {
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
