package com.compomics.util.experiment.io.massspectrometry;

import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class will read an mgf file.
 * 
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MgfReader {

    /**
     * general constructor for an mgf reader
     */
    public MgfReader() {
    }

    /**
     * Reads an mgf file and retrieves a list of spectra
     *
     * @param aFile the mgf file
     * @return list of MSnSpectra imported from the file
     * @throws Exception Exeption thrown if a problem is encountered reading the file
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

                    // @TODO: verify that this is the best way of doing this
                
                    if (line.lastIndexOf(" ") != -1) {
                        String[] temp = line.split(" ");
                        precursorMass = new Double(temp[0].substring(temp[0].indexOf('=') + 1));
                        precursorIntensity = new Double(temp[1]);
                    } else if (line.lastIndexOf("\t") != -1) {
                        String[] temp = line.split("\t");
                        precursorMass = new Double(temp[0].substring(temp[0].indexOf('=') + 1));
                        precursorIntensity = new Double(temp[1]);
                    } else {
                        precursorMass = new Double(line.substring(line.indexOf('=') + 1));
                    }
            } else if (line.startsWith("RTINSECONDS")) {
                try {
                    rt = new Double(line.substring(line.indexOf('=') + 1));
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
}
