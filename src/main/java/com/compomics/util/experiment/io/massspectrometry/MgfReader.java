package com.compomics.util.experiment.io.massspectrometry;

import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;
import javax.swing.JProgressBar;

/**
 * This class will read an mgf file.
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
     * Reads an mgf file and retrieves a list of spectra.
     *
     * @param aFile         the mgf file
     * @return              list of MSnSpectra imported from the file
     * @throws FileNotFoundException    Exeption thrown if a problem is encountered reading the file
     * @throws IOException    Exception thrown if a problem is encountered reading the file
     * @throws IllegalArgumentException thrown when a parameter in the file cannot be parsed correctly 
     */
    public ArrayList<MSnSpectrum> getSpectra(File aFile) throws FileNotFoundException, IOException, IllegalArgumentException {

        ArrayList<MSnSpectrum> spectra = new ArrayList<MSnSpectrum>();
        double precursorMass = 0, precursorIntensity = 0, rt = -1.0;
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>();
        String scanNumber = "", spectrumTitle = "";
        HashSet<Peak> spectrum = new HashSet<Peak>();
        BufferedReader br = new BufferedReader(new FileReader(aFile));
        String line = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.equals("BEGIN IONS")) {
                spectrum = new HashSet<Peak>();
            } else if (line.startsWith("TITLE")) {
                spectrumTitle = line.substring(line.indexOf('=') + 1);
            } else if (line.startsWith("CHARGE")) {
                precursorCharges = parseCharges(line);
            } else if (line.startsWith("PEPMASS")) {
                String temp = line.substring(line.indexOf("=") + 1);
                String[] values = temp.split("\\s");
                precursorMass = Double.parseDouble(values[0]);
                if (values.length > 1) {
                    precursorIntensity = Double.parseDouble(values[1]);
                } else {
                    precursorIntensity = 0.0;
                }
            } else if (line.startsWith("RTINSECONDS")) {
                try {
                    String value = line.substring(line.indexOf('=') + 1);
                    String[] temp = Pattern.compile("\\D").split(value);
                    rt = new Double(temp[0]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse retention time.");
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
                try {
                    scanNumber = line.substring(line.indexOf('=') + 1);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse scan number.");
                }
            } else if (line.startsWith("INSTRUMENT")) {
                // ion series not implemented
            } else if (line.equals("END IONS")) {
                MSnSpectrum msnSpectrum = new MSnSpectrum(2, new Precursor(
                        rt, precursorMass, precursorIntensity, precursorCharges), spectrumTitle, spectrum, aFile.getName());
                msnSpectrum.setScanNumber(scanNumber);
                spectra.add(msnSpectrum);
            } else if (!line.equals("")) {
                try {
                    Double mz = new Double(line.substring(0, line.indexOf(' ')));
                    Double intensity = new Double(line.substring(line.indexOf(' ')));
                    spectrum.add(new Peak(mz, intensity));
                } catch (Exception e1) {
                    // try with tab separated
                    try {
                        Double mz = new Double(line.substring(0, line.indexOf('\t')));
                        Double intensity = new Double(line.substring(line.indexOf('\t')));
                        spectrum.add(new Peak(mz, intensity));
                    } catch (Exception e2) {
                        // ignore comments and all other lines
                    }
                }
            }
        }

        br.close();
        return spectra;
    }

    /**
     * Returns the index of all spectra in the given mgf file
     * @param mgfFile                   the given mgf file
     * @return                          the index of all spectra
     * @throws FileNotFoundException    Exception thrown whenever the file is not found
     * @throws IOException              Exception thrown whenever an error occurs while reading the file
     */
    public static MgfIndex getIndexMap(File mgfFile) throws FileNotFoundException, IOException {
        return getIndexMap(mgfFile, null);
    }

    /**
     * Returns the index of all spectra in the given mgf file
     * @param mgfFile                   the given mgf file
     * @param progressBar               a progress bar showing the progress
     * @return                          the index of all spectra
     * @throws FileNotFoundException    Exception thrown whenever the file is not found
     * @throws IOException              Exception thrown whenever an error occurs while reading the file
     */
    public static MgfIndex getIndexMap(File mgfFile, JProgressBar progressBar) throws FileNotFoundException, IOException {
        HashMap<String, Long> indexes = new HashMap<String, Long>();

        RandomAccessFile randomAccessFile = new RandomAccessFile(mgfFile, "r");
        String line;
        long currentIndex = 0;

        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(true);
            progressBar.setMaximum(100);
            progressBar.setValue(0);
        }

        long progressUnit = randomAccessFile.length() / 100;

        while ((line = randomAccessFile.readLine()) != null) {
            line = line.trim();
            if (line.equals("BEGIN IONS")) {
                currentIndex = randomAccessFile.getFilePointer();

                if (progressBar != null) {
                    progressBar.setValue((int) (currentIndex / progressUnit));
                }
            } else if (line.startsWith("TITLE")) {
                indexes.put(line.substring(line.indexOf('=') + 1).trim(), currentIndex);
            }
        }

        if (progressBar != null) {
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(false);
        }

        randomAccessFile.close();

        return new MgfIndex(indexes, mgfFile.getName());
    }

    /**
     * Splits an mgf file into smaller ones and returns the indexes of the generated files
     * @param mgfFile                   the mgf file to split
     * @param nSpectra                  the number of spectra allowed in the smaller files
     * @param progressBar               the progress bar showing the progress
     * @return  a list of indexes of the generated files
     * @throws FileNotFoundException    exception thrown whenever a file was not found
     * @throws IOException              exception thrown whenever a problem occurred while reading/writing a file
     */
    public static ArrayList<MgfIndex> splitFile(File mgfFile, int nSpectra, JProgressBar progressBar) throws FileNotFoundException, IOException {
        String fileName = mgfFile.getName();

        if (fileName.endsWith(".mgf")) {

            ArrayList<MgfIndex> mgfIndexes = new ArrayList<MgfIndex>();

            String splittedName = fileName.substring(0, fileName.lastIndexOf("."));

            RandomAccessFile readAccessFile = new RandomAccessFile(mgfFile, "r");
            String line;
            long readIndex, writeIndex = 0;
            if (progressBar != null) {
                progressBar.setIndeterminate(false);
                progressBar.setStringPainted(true);
                progressBar.setMaximum(100);
                progressBar.setValue(0);
            }
            int fileCounter = 1;
            int spectrumCounter = 0;
            long typicalSize = 0;

            HashMap<String, Long> indexes = new HashMap<String, Long>();
            String currentName = splittedName + "_" + fileCounter + ".mgf";
            File testFile = new File(mgfFile.getParent(), currentName);
            RandomAccessFile writeFile = new RandomAccessFile(testFile, "rw");

            long progressUnit = readAccessFile.length() / 100;
            while ((line = readAccessFile.readLine()) != null) {
                line = line.trim();
                if (line.equals("BEGIN IONS")) {
                    spectrumCounter++;
                    writeIndex = writeFile.getFilePointer();
                    readIndex = readAccessFile.getFilePointer();

                    if (spectrumCounter > nSpectra) {

                        typicalSize = Math.max(writeIndex, typicalSize);

                        if (readAccessFile.length() - readIndex > typicalSize / 2) { // try to avoid small leftovers

                            writeFile.close();
                            mgfIndexes.add(new MgfIndex(indexes, currentName));

                            fileCounter++;
                            currentName = splittedName + "_" + fileCounter + ".mgf";
                            testFile = new File(mgfFile.getParent(), currentName);
                            writeFile = new RandomAccessFile(testFile, "rw");
                            spectrumCounter = 0;
                            indexes = new HashMap<String, Long>();
                        }
                    }

                    if (progressBar != null) {
                        progressBar.setValue((int) (writeIndex / progressUnit));
                    }
                } else if (line.startsWith("TITLE")) {
                    indexes.put(line.substring(line.indexOf('=') + 1).trim(), writeIndex);
                }
                writeFile.writeBytes(line + "\n");
            }

            writeFile.close();
            mgfIndexes.add(new MgfIndex(indexes, currentName));
            if (progressBar != null) {
                progressBar.setIndeterminate(true);
                progressBar.setStringPainted(false);
            }

            readAccessFile.close();
            writeFile.close();
            return mgfIndexes;

        } else {
            throw new IllegalArgumentException("Spectrum file format not supported.");
        }
    }

    /**
     * Returns the next spectrum starting from the given index
     * @param randomAccessFile  The random access file of the inspected mgf file
     * @param index             The index where to start looking for the spectrum
     * @param fileName          The name of the mgf file (@TODO get this from the random access file?)
     * @return                  The next spectrum encountered
     * @throws IOException      Exception thrown whenever an error is encountered while reading the spectrum
     * @throws IllegalArgumentException Exception thrown whenever the file is not of a compatible format
     */
    public static MSnSpectrum getSpectrum(RandomAccessFile randomAccessFile, long index, String fileName) throws IOException, IllegalArgumentException {
        randomAccessFile.seek(index);
        String line;
        double precursorMass = 0, precursorIntensity = 0, rt = -1.0;
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>();
        String scanNumber = "", spectrumTitle = "";
        HashSet<Peak> spectrum = new HashSet<Peak>();
        while ((line = randomAccessFile.readLine()) != null) {
            line = line.trim();
            if (line.equals("BEGIN IONS")) {
                spectrum = new HashSet<Peak>();
            } else if (line.startsWith("TITLE")) {
                spectrumTitle = line.substring(line.indexOf('=') + 1);
            } else if (line.startsWith("CHARGE")) {
                precursorCharges = parseCharges(line);
            } else if (line.startsWith("PEPMASS")) {
                String temp = line.substring(line.indexOf("=") + 1);
                String[] values = temp.split("\\s");
                precursorMass = Double.parseDouble(values[0]);
                if (values.length > 1) {
                    precursorIntensity = Double.parseDouble(values[1]);
                } else {
                    precursorIntensity = 0.0;
                }
            } else if (line.startsWith("RTINSECONDS")) {
                try {
                    String value = line.substring(line.indexOf('=') + 1);
                    String[] temp = Pattern.compile("\\D").split(value);
                    rt = new Double(temp[0]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse retention time.");
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
                try {
                    scanNumber = line.substring(line.indexOf('=') + 1);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot parse scan number.");
                }
            } else if (line.startsWith("INSTRUMENT")) {
                // ion series not implemented
            } else if (line.equals("END IONS")) {
                MSnSpectrum msnSpectrum = new MSnSpectrum(2, new Precursor(
                        rt, precursorMass, precursorIntensity, precursorCharges), spectrumTitle, spectrum, fileName);
                msnSpectrum.setScanNumber(scanNumber);
                return msnSpectrum;
            } else if (!line.equals("")) {
                try {
                    Double mz = new Double(line.substring(0, line.indexOf(' ')));
                    Double intensity = new Double(line.substring(line.indexOf(' ')));
                    spectrum.add(new Peak(mz, intensity));
                } catch (Exception e1) {
                    // try with tab separated
                    try {
                        Double mz = new Double(line.substring(0, line.indexOf('\t')));
                        Double intensity = new Double(line.substring(line.indexOf('\t')));
                        spectrum.add(new Peak(mz, intensity));
                    } catch (Exception e2) {
                        // ignore comments and all other lines
                    }
                }
            }
        }
        throw new IllegalArgumentException("End of the file reached before encountering the tag \"END IONS\".");
    }
    
    /**
     * Parses the charge line of an mgf files
     * @param chargeLine    the charge line
     * @return the possible charges found
     */
    private static ArrayList<Charge> parseCharges(String chargeLine) {
        ArrayList<Charge> result = new ArrayList<Charge>();
        String tempLine = chargeLine.substring(chargeLine.indexOf("=")+1);
        String[] charges = tempLine.split(" and ");
        Integer value;
        for (String charge : charges) {
            charge = charge.trim();
            if (charge.endsWith("+")) {
                value = new Integer(charge.substring(0, charge.length()-1));
                result.add(new Charge(Charge.PLUS, value));
            } else if (charge.endsWith("-")) {
                value = new Integer(charge.substring(0, charge.length()-1));
                result.add(new Charge(Charge.MINUS, value));
            } else {
                result.add(new Charge(Charge.PLUS, new Integer(charge)));
            }
        }
        
        return result;
    }

    /**
     * Returns the next precursor starting from the given index
     * @param randomAccessFile  The random access file of the inspected mgf file
     * @param index             The index where to start looking for the spectrum
     * @param fileName          The name of the mgf file (@TODO get this from the random access file?)
     * @return                  The next spectrum encountered
     * @throws IOException      Exception thrown whenever an error is encountered while reading the spectrum
     * @throws IllegalArgumentException        Exception thrown whenever the file is not of a compatible format
     */
    public static Precursor getPrecursor(RandomAccessFile randomAccessFile, long index, String fileName) throws IOException, IllegalArgumentException {

        randomAccessFile.seek(index);
        String line;
        double precursorMass = 0, precursorIntensity = 0, rt = -1.0;
        int precursorCharge = 1;
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>();
        
        while ((line = randomAccessFile.readLine()) != null) {
            line = line.trim();
            if (line.equals("BEGIN IONS")
                    || line.startsWith("TITLE")
                    || line.startsWith("TOLU")
                    || line.startsWith("TOL")
                    || line.startsWith("SEQ")
                    || line.startsWith("COMP")
                    || line.startsWith("ETAG")
                    || line.startsWith("TAG")
                    || line.startsWith("SCANS")
                    || line.startsWith("INSTRUMENT")) {
            } else if (line.startsWith("CHARGE")) {
                precursorCharges = parseCharges(line);
            } else if (line.startsWith("PEPMASS")) {
                String temp = line.substring(line.indexOf("=") + 1);
                String[] values = temp.split("\\s");
                precursorMass = Double.parseDouble(values[0]);
                if (values.length > 1) {
                    precursorIntensity = Double.parseDouble(values[1]);
                } else {
                    precursorIntensity = 0.0;
                }
            } else if (line.startsWith("RTINSECONDS")) {
                rt = new Double(line.substring(line.indexOf('=') + 1));
            } else {
                return new Precursor(rt, precursorMass, precursorIntensity, precursorCharges);
            }
        }
        throw new IllegalArgumentException("End of the file reached before encountering the tag \"END IONS\".");
    }
}
