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
     * @throws Exception    Exeption thrown if a problem is encountered reading the file
     */
    public ArrayList<MSnSpectrum> getSpectra(File aFile) throws Exception {

        ArrayList<MSnSpectrum> spectra = new ArrayList<MSnSpectrum>();
        double precursorMass = 0, precursorIntensity = 0, rt = -1.0;
        int precursorCharge = 1;
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
                precursorCharge = new Integer(line.substring(line.indexOf('=') + 1, line.indexOf('=') + 2));
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
                } catch (Exception e) {
                    throw new Exception("Cannot parse retention time.");
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
                    throw new Exception("Cannot parse scan number.");
                }
            } else if (line.startsWith("INSTRUMENT")) {
                // ion series not implemented
            } else if (line.equals("END IONS")) {
                MSnSpectrum msnSpectrum = new MSnSpectrum(2, new Precursor(
                        rt, precursorMass, precursorIntensity, new Charge(Charge.PLUS, precursorCharge)), spectrumTitle, spectrum, aFile.getName());
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
        HashMap<String, Long> indexes = new HashMap<String, Long>();

        RandomAccessFile randomAccessFile = new RandomAccessFile(mgfFile, "r");
        String line;
        long currentIndex = 0;

        while ((line = randomAccessFile.readLine()) != null) {
            line = line.trim();
            if (line.equals("BEGIN IONS")) {
                currentIndex = randomAccessFile.getFilePointer();
            } else if (line.startsWith("TITLE")) {
                indexes.put(line.substring(line.indexOf('=') + 1), currentIndex);
            }
        }

        randomAccessFile.close();

        return new MgfIndex(indexes, mgfFile.getName().toLowerCase());
    }

    /**
     * Returns the next spectrum starting from the given index
     * @param randomAccessFile  The random access file of the inspected mgf file
     * @param index             The index where to start looking for the spectrum
     * @param fileName          The name of the mgf file (@TODO get this from the random access file?)
     * @return                  The next spectrum encountered
     * @throws IOException      Exception thrown whenever an error is encountered while reading the spectrum
     * @throws Exception        Exception thrown whenever the file is not of a compatible format
     */
    public static MSnSpectrum getSpectrum(RandomAccessFile randomAccessFile, long index, String fileName) throws IOException, Exception {
        randomAccessFile.seek(index);
        String line;
        double precursorMass = 0, precursorIntensity = 0, rt = -1.0;
        int precursorCharge = 1;
        String scanNumber = "", spectrumTitle = "";
        HashSet<Peak> spectrum = new HashSet<Peak>();
        while ((line = randomAccessFile.readLine()) != null) {
            line = line.trim();
            if (line.equals("BEGIN IONS")) {
                spectrum = new HashSet<Peak>();
            } else if (line.startsWith("TITLE")) {
                spectrumTitle = line.substring(line.indexOf('=') + 1);
            } else if (line.startsWith("CHARGE")) {
                precursorCharge = new Integer(line.substring(line.indexOf('=') + 1, line.indexOf('=') + 2));
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
                } catch (Exception e) {
                    throw new Exception("Cannot parse retention time.");
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
                    throw new Exception("Cannot parse scan number.");
                }
            } else if (line.startsWith("INSTRUMENT")) {
                // ion series not implemented
            } else if (line.equals("END IONS")) {
                MSnSpectrum msnSpectrum = new MSnSpectrum(2, new Precursor(
                        rt, precursorMass, precursorIntensity, new Charge(Charge.PLUS, precursorCharge)), spectrumTitle, spectrum, fileName);
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
        throw new Exception("End of the file reached before encountering the tag \"END IONS\".");
    }

    /**
     * Returns the next precursor starting from the given index
     * @param randomAccessFile  The random access file of the inspected mgf file
     * @param index             The index where to start looking for the spectrum
     * @param fileName          The name of the mgf file (@TODO get this from the random access file?)
     * @return                  The next spectrum encountered
     * @throws IOException      Exception thrown whenever an error is encountered while reading the spectrum
     * @throws Exception        Exception thrown whenever the file is not of a compatible format
     */
    public static Precursor getPrecursor(RandomAccessFile randomAccessFile, long index, String fileName) throws IOException, Exception {
        randomAccessFile.seek(index);
        String line;
        double precursorMass = 0, precursorIntensity = 0, rt = -1.0;
        int precursorCharge = 1;
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
                precursorCharge = new Integer(line.substring(line.indexOf('=') + 1, line.indexOf('=') + 2));
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
                } catch (Exception e) {
                    throw new Exception("Cannot parse retention time.");
                }
            } else {
                return new Precursor(rt, precursorMass, precursorIntensity, new Charge(Charge.PLUS, precursorCharge));
            }
        }
        throw new Exception("End of the file reached before encountering the tag \"END IONS\".");
    }
}
