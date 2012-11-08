package com.compomics.util.experiment.io.massspectrometry;

import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.gui.waiting.WaitingHandler;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This class will read an MGF file.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MgfReader {

    /**
     * The pattern used to pick up double values.
     */
    private static Pattern doublePattern = Pattern.compile("\\D");

    /**
     * General constructor for an mgf reader.
     */
    public MgfReader() {
    }

    /**
     * Reads an MGF file and retrieves a list of spectra.
     *
     * @param aFile the mgf file
     * @return list of MSnSpectra imported from the file
     * @throws FileNotFoundException Exeption thrown if a problem is encountered
     * reading the file
     * @throws IOException Exception thrown if a problem is encountered reading
     * the file
     * @throws IllegalArgumentException thrown when a parameter in the file
     * cannot be parsed correctly
     */
    public ArrayList<MSnSpectrum> getSpectra(File aFile) throws FileNotFoundException, IOException, IllegalArgumentException {

        ArrayList<MSnSpectrum> spectra = new ArrayList<MSnSpectrum>();
        double precursorMz = 0, precursorIntensity = 0, rt = -1.0, rt1 = -1.0, rt2 = -1.0;
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>();
        String scanNumber = "", spectrumTitle = "";
        HashMap<Double, Peak> spectrum = new HashMap<Double, Peak>();
        BufferedReader br = new BufferedReader(new FileReader(aFile));
        String line;

        while ((line = br.readLine()) != null) {

            line = line.trim();

            if (line.equals("BEGIN IONS")) {
                spectrum = new HashMap<Double, Peak>();
            } else if (line.startsWith("TITLE")) {
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
                        rt = new Double(rtWindow[0]);
                    } else if (rtWindow.length == 2) {
                        rt1 = new Double(rtWindow[0]);
                        rt2 = new Double(rtWindow[1]);
                    }
                } catch (Exception e) {
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
                try {
                    scanNumber = line.substring(line.indexOf('=') + 1);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse scan number.");
                }
            } else if (line.startsWith("INSTRUMENT")) {
                // ion series not implemented
            } else if (line.equals("END IONS")) {
                Precursor precursor;
                if (rt1 != -1 && rt2 != -1) {
                    precursor = new Precursor(precursorMz, precursorIntensity, precursorCharges, rt1, rt2);
                } else {
                    precursor = new Precursor(rt, precursorMz, precursorIntensity, precursorCharges);
                }
                MSnSpectrum msnSpectrum = new MSnSpectrum(2, precursor, spectrumTitle, spectrum, aFile.getName());
                msnSpectrum.setScanNumber(scanNumber);
                spectra.add(msnSpectrum);
            } else if (!line.equals("")) {
                try {
                    Double mz = new Double(line.substring(0, line.indexOf(' ')));
                    Double intensity = new Double(line.substring(line.indexOf(' ')));
                    spectrum.put(mz, new Peak(mz, intensity));
                } catch (Exception e1) {
                    // try with tab separated
                    try {
                        Double mz = new Double(line.substring(0, line.indexOf('\t')));
                        Double intensity = new Double(line.substring(line.indexOf('\t')));
                        spectrum.put(mz, new Peak(mz, intensity));
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

        HashMap<String, Long> indexes = new HashMap<String, Long>();
        ArrayList<String> spectrumTitles = new ArrayList<String>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
        long beginIndex = 0, currentIndex = 0;
        String title = null;
        int cpt = 0;
        double maxRT = -1, minRT = Double.MAX_VALUE, maxMz = -1, maxIntensity = 0;

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressValue(100);
            waitingHandler.setSecondaryProgressValue(0);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String line;

        while ((line = bufferedRandomAccessFile.getNextLine()) != null) {

            line = line.trim();

            if (line.equals("BEGIN IONS")) {
                currentIndex = bufferedRandomAccessFile.getFilePointer();
                beginIndex = currentIndex;
                cpt++;
                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                    waitingHandler.setSecondaryProgressValue((int) (currentIndex / progressUnit));
                }
            } else if (line.startsWith("TITLE")) {
                title = line.substring(line.indexOf('=') + 1).trim();
                try {
                    title = URLDecoder.decode(title, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    System.out.println("An exception was thrown when trying to decode an mgf title: " + title);
                    e.printStackTrace();
                }
                spectrumTitles.add(title);
                indexes.put(title, currentIndex);
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

            } else if (line.startsWith("RTINSECONDS")) {

                String rtInput = "";

                try {
                    rtInput = line.substring(line.indexOf('=') + 1);
                    String[] rtWindow = rtInput.split("-");

                    if (rtWindow.length == 1) {
                        double rt = new Double(rtWindow[0]);
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
                    throw new IllegalArgumentException("Cannot parse retention time:" + rtInput);
                }
            } else if (line.equals("END IONS")) {
                if (title == null) {
                    title = cpt + "";
                    indexes.put(title, beginIndex);
                    spectrumTitles.add(title);
                }
            }
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(true);
        }

        bufferedRandomAccessFile.close();

        if (minRT == Double.MAX_VALUE) {
            minRT = 0;
        }

        return new MgfIndex(spectrumTitles, indexes, mgfFile.getName(), minRT, maxRT, maxMz, maxIntensity);
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

        // @TODO: possible to make this method even faster?? 
        // replacing RandomAccessFile with BufferedRandomAccessFile helped a lot, but might still be room for more

        String fileName = mgfFile.getName();

        if (fileName.endsWith(".mgf")) {

            ArrayList<MgfIndex> mgfIndexes = new ArrayList<MgfIndex>();
            ArrayList<String> spectrumTitles = new ArrayList<String>();
            String title = null;
            String splittedName = fileName.substring(0, fileName.lastIndexOf("."));

            BufferedRandomAccessFile readBufferedRandomAccessFile = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
            long writeIndex = 0, beginIndex = 0;

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressDialogIndeterminate(false);
                waitingHandler.setMaxSecondaryProgressValue(100);
                waitingHandler.setSecondaryProgressValue(0);
            }

            int fileCounter = 1, spectrumCounter = 0;
            long typicalSize = 0;
            double maxRT = -1, minRT = Double.MAX_VALUE, maxMz = -1, maxIntensity = 0;

            HashMap<String, Long> indexes = new HashMap<String, Long>();
            String currentName = splittedName + "_" + fileCounter + ".mgf";
            File testFile = new File(mgfFile.getParent(), currentName);
            BufferedRandomAccessFile writeBufferedRandomAccessFile = new BufferedRandomAccessFile(testFile, "rw", 1024 * 100);

            long sizeOfReadAccessFile = readBufferedRandomAccessFile.length();
            long progressUnit = sizeOfReadAccessFile / 100;
            String line;

            while ((line = readBufferedRandomAccessFile.getNextLine()) != null) {

                line = line.trim();

                if (line.equals("BEGIN IONS")) {

                    spectrumCounter++;
                    writeIndex = writeBufferedRandomAccessFile.getFilePointer();
                    beginIndex = writeIndex;

                    long readIndex = readBufferedRandomAccessFile.getFilePointer();

                    if (spectrumCounter > nSpectra) {

                        typicalSize = Math.max(writeIndex, typicalSize);

                        if (sizeOfReadAccessFile - readIndex > typicalSize / 2) { // try to avoid small leftovers

                            writeBufferedRandomAccessFile.close();
                            mgfIndexes.add(new MgfIndex(spectrumTitles, indexes, currentName, minRT, maxRT, maxMz, maxIntensity));

                            currentName = splittedName + "_" + ++fileCounter + ".mgf";
                            testFile = new File(mgfFile.getParent(), currentName);
                            writeBufferedRandomAccessFile = new BufferedRandomAccessFile(testFile, "rw", 1024 * 100);
                            writeIndex = 0;
                            spectrumCounter = 0;
                            maxRT = -1;
                            minRT = Double.MAX_VALUE;
                            indexes = new HashMap<String, Long>();
                            spectrumTitles = new ArrayList<String>();
                        }
                    }

                    if (waitingHandler != null) {
                        if (waitingHandler.isRunCanceled()) {
                            break;
                        }
                        waitingHandler.setSecondaryProgressValue((int) (readIndex / progressUnit));
                    }

                } else if (line.startsWith("TITLE")) {
                    title = line.substring(line.indexOf('=') + 1).trim();
                    try {
                        title = URLDecoder.decode(title, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        System.out.println("An exception was thrown when trying to decode an mgf title: " + title);
                        e.printStackTrace();
                    }
                    spectrumTitles.add(title);
                    indexes.put(title, writeIndex);
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

                } else if (line.startsWith("RTINSECONDS")) {
                    try {
                        String rtInput = line.substring(line.indexOf('=') + 1);
                        String[] rtWindow = rtInput.split("-");
                        if (rtWindow.length == 1) {
                            double rt = new Double(rtWindow[0]);
                            if (rt > maxRT) {
                                maxRT = rt;
                            }
                            if (rt < minRT) {
                                minRT = rt;
                            }
                        } else if (rtWindow.length == 2) {
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
                        throw new IllegalArgumentException("Cannot parse retention time.");
                    }
                } else if (line.equals("END IONS")) {
                    if (title == null) {
                        title = spectrumCounter + "";
                        indexes.put(title, beginIndex);
                        spectrumTitles.add(title);
                    }
                }
                writeBufferedRandomAccessFile.writeBytes(line + "\n");
            }

            mgfIndexes.add(new MgfIndex(spectrumTitles, indexes, currentName, minRT, maxRT, maxMz, maxIntensity));

            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressDialogIndeterminate(true);
            }

            readBufferedRandomAccessFile.close();
            writeBufferedRandomAccessFile.close();
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
     * @param fileName The name of the MGF file (@TODO get this from the random
     * access file?)
     * @return The next spectrum encountered
     * @throws IOException Exception thrown whenever an error is encountered
     * while reading the spectrum
     * @throws IllegalArgumentException Exception thrown whenever the file is
     * not of a compatible format
     */
    public static MSnSpectrum getSpectrum(BufferedRandomAccessFile bufferedRandomAccessFile, long index, String fileName) throws IOException, IllegalArgumentException {

        bufferedRandomAccessFile.seek(index);
        double precursorMz = 0, precursorIntensity = 0, rt = -1.0, rt1 = -1, rt2 = -1;
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>();
        String scanNumber = "", spectrumTitle = "";
        HashMap<Double, Peak> spectrum = new HashMap<Double, Peak>();
        String line;

        while ((line = bufferedRandomAccessFile.getNextLine()) != null) {

            line = line.trim();

            if (line.equals("BEGIN IONS")) {
                spectrum = new HashMap<Double, Peak>();
            } else if (line.startsWith("TITLE")) {
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
                    // @TODO: ought to be replaced by code below, but this failes the SpectrumTest... (@Harald: the Pattern is wrong, it should include the '.')
                    String rtInput = line.substring(line.indexOf('=') + 1);
                    String[] rtWindow = rtInput.split("-");
                    if (rtWindow.length == 1) {
                        rt = new Double(rtWindow[0]);
                    } else if (rtWindow.length == 2) {
                        rt1 = new Double(rtWindow[0]);
                        rt2 = new Double(rtWindow[1]);
                    }
//                try {
//                    String value = line.substring(line.indexOf('=') + 1);
//                    String[] temp = doublePattern.split(value);
//                    rt = new Double(temp[0]);
//                } catch (NumberFormatException e) {
//                    throw new IllegalArgumentException("Cannot parse retention time.");
//                }
                } catch (Exception e) {
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
                try {
                    scanNumber = line.substring(line.indexOf('=') + 1);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot parse scan number.");
                }
            } else if (line.startsWith("INSTRUMENT")) {
                // ion series not implemented
            } else if (line.equals("END IONS")) {
                Precursor precursor;
                if (rt1 != -1 && rt2 != -1) {
                    precursor = new Precursor(precursorMz, precursorIntensity, precursorCharges, rt1, rt2);
                } else {
                    precursor = new Precursor(rt, precursorMz, precursorIntensity, precursorCharges);
                }
                MSnSpectrum msnSpectrum = new MSnSpectrum(2, precursor, spectrumTitle, spectrum, fileName);
                msnSpectrum.setScanNumber(scanNumber);
                return msnSpectrum;
            } else if (!line.equals("")) {
                try {
                    Double mz = new Double(line.substring(0, line.indexOf(' ')));
                    Double intensity = new Double(line.substring(line.indexOf(' ')));
                    spectrum.put(mz, new Peak(mz, intensity));
                } catch (Exception e1) {
                    // try with tab separated
                    try {
                        Double mz = new Double(line.substring(0, line.indexOf('\t')));
                        Double intensity = new Double(line.substring(line.indexOf('\t')));
                        spectrum.put(mz, new Peak(mz, intensity));
                    } catch (Exception e2) {
                        // ignore comments and all other lines
                    }
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
    private static ArrayList<Charge> parseCharges(String chargeLine) {

        ArrayList<Charge> result = new ArrayList<Charge>(1);
        String tempLine = chargeLine.substring(chargeLine.indexOf("=") + 1);
        String[] chargesAnd = tempLine.split(" and ");
        ArrayList<String> charges = new ArrayList<String>();

        for (String charge : chargesAnd) {
            for (String charge2 : charge.split(",")) {
                charges.add(charge2.trim());
            }
        }

        for (String charge : charges) {

            Integer value;
            charge = charge.trim();

            if (charge.endsWith("+")) {
                value = new Integer(charge.substring(0, charge.length() - 1));
                result.add(new Charge(Charge.PLUS, value));
            } else if (charge.endsWith("-")) {
                value = new Integer(charge.substring(0, charge.length() - 1));
                result.add(new Charge(Charge.MINUS, value));
            } else if (!charge.equalsIgnoreCase("Mr")) {
                result.add(new Charge(Charge.PLUS, new Integer(charge)));
            }
        }

        // if empty, add a default charge of 1
        if (result.isEmpty()) {
            result.add(new Charge(Charge.PLUS, 1));
        }

        return result;
    }

    /**
     * Returns the next precursor starting from the given index.
     *
     * @param bufferedRandomAccessFile The random access file of the inspected
     * mgf file
     * @param index The index where to start looking for the spectrum
     * @param fileName The name of the mgf file (@TODO get this from the random
     * access file?)
     * @return The next spectrum encountered
     * @throws IOException Exception thrown whenever an error is encountered
     * while reading the spectrum
     * @throws IllegalArgumentException Exception thrown whenever the file is
     * not of a compatible format
     */
    public static Precursor getPrecursor(BufferedRandomAccessFile bufferedRandomAccessFile, Long index, String fileName) throws IOException, IllegalArgumentException {

        bufferedRandomAccessFile.seek(index);
        String line, title = null;
        double precursorMz = 0, precursorIntensity = 0, rt = -1.0, rt1 = -1, rt2 = -1;
        ArrayList<Charge> precursorCharges = new ArrayList<Charge>(1);

        while ((line = bufferedRandomAccessFile.getNextLine()) != null) {

            line = line.trim();

            if (!line.equals("")) {

                if (line.equals("BEGIN IONS")
                        || line.startsWith("TOLU")
                        || line.startsWith("TOL")
                        || line.startsWith("SEQ")
                        || line.startsWith("COMP")
                        || line.startsWith("ETAG")
                        || line.startsWith("TAG")
                        || line.startsWith("SCANS")
                        || line.startsWith("INSTRUMENT")) {
                    // not supported yet
                } else if (line.startsWith("TITLE")) {
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
                        // @TODO: ought to be replaced by code below, but this failes the SpectrumTest... (@Harald: the Pattern is wrong, it should include the '.')
                        String rtInput = line.substring(line.indexOf('=') + 1);
                        String[] rtWindow = rtInput.split("-");
                        if (rtWindow.length == 1) {
                            rt = new Double(rtWindow[0]);
                        } else if (rtWindow.length == 2) {
                            rt1 = new Double(rtWindow[0]);
                            rt2 = new Double(rtWindow[1]);
                        }
//                try {
//                    String value = line.substring(line.indexOf('=') + 1);
//                    String[] temp = doublePattern.split(value);
//                    rt = new Double(temp[0]);
//                } catch (NumberFormatException e) {
//                    throw new IllegalArgumentException("Cannot parse retention time.");
//                }
                    } catch (Exception e) {
                        // ignore exception, RT will not be parsed
                    }
                } else {
                    if (rt1 != -1 && rt2 != -1) {
                        return new Precursor(precursorMz, precursorIntensity, precursorCharges, rt1, rt2);
                    }
                    return new Precursor(rt, precursorMz, precursorIntensity, precursorCharges);
                }
            }
        }
        throw new IllegalArgumentException("End of the file reached before encountering the tag \"END IONS\". File: " + fileName + ", title: " + title);
    }

    /**
     * Writes an apl file from an MGF file.
     *
     * @param mgfFile the mgf file
     * @param aplFile the target apl file
     * @param fragmentation the fragmentation method used
     * @throws FileNotFoundException exception thrown whenever a file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading/writing a file
     * @throws IllegalArgumentException exception thrown whenever the mgf file
     * is truncated in the middle of a spectrum
     */
    public static void writeAplFile(File mgfFile, File aplFile, String fragmentation) throws FileNotFoundException, IOException, IllegalArgumentException {

        if (fragmentation == null) {
            fragmentation = "Unknown";
        }

        Writer aplWriter = new BufferedWriter(new FileWriter(aplFile));
        MgfIndex mgfIndex = getIndexMap(mgfFile);
        HashMap<Double, ArrayList<String>> spectrumTitleMap = new HashMap<Double, ArrayList<String>>();
        BufferedRandomAccessFile mgfRFile = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);

        for (String title : mgfIndex.getSpectrumTitles()) {
            Precursor precursor = getPrecursor(mgfRFile, mgfIndex.getIndex(title), mgfFile.getName());
            if (!spectrumTitleMap.containsKey(precursor.getMz())) {
                spectrumTitleMap.put(precursor.getMz(), new ArrayList<String>());
            }
            spectrumTitleMap.get(precursor.getMz()).add(title);
        }

        ArrayList<Double> masses = new ArrayList<Double>(spectrumTitleMap.keySet());
        Collections.sort(masses);

        for (double mz : masses) {

            for (String title : spectrumTitleMap.get(mz)) {

                MSnSpectrum spectrum = getSpectrum(mgfRFile, mgfIndex.getIndex(title), mgfFile.getName());
                aplWriter.write("peaklist start\n");
                aplWriter.write("mz=" + mz + "\n");
                aplWriter.write("fragmentation=" + fragmentation + "\n");
                aplWriter.write("charge=" + spectrum.getPrecursor().getPossibleCharges().get(0).value + "\n"); //@TODO what if many/no charge is present?
                aplWriter.write("header=" + spectrum.getSpectrumTitle() + "\n");
                HashMap<Double, Peak> peakMap = spectrum.getPeakMap();
                ArrayList<Double> fragmentMasses = new ArrayList<Double>(peakMap.keySet());
                Collections.sort(fragmentMasses);

                for (double fragmentMass : fragmentMasses) {
                    aplWriter.write(fragmentMass + "\t" + peakMap.get(fragmentMass).intensity + "\n");
                }

                aplWriter.write("peaklist end\n\n");
            }
        }

        mgfRFile.close();
        aplWriter.close();
    }
}
