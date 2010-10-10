package com.compomics.util.experiment.massspectrometry.filereaders;

import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.Charge;

import java.util.ArrayList;
import java.util.HashSet;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class will read an mgf file.
 * User: Marc
 * Date: Sep 27, 2010
 * Time: 5:08:21 PM
 */
public class MgfReader {

    /**
     * general constructor for an mgf reader
     */
    public MgfReader() {
    }

    /**
     * Reads an mgf file and retrieves a list of spectra
     * @param aFile         the mgf file
     * @return              list of MSnSpectra imported from the file
     * @throws IOException  IOExeption thrown if a problem is encountered reading the file
     */
    public ArrayList<MSnSpectrum> getSpectra(File aFile) throws IOException {
        ArrayList<MSnSpectrum> spectra = new ArrayList<MSnSpectrum>();
        double precursorMass = 0;
        int precursorCharge = 0;
        double rt=-1.0;
        String spectrumTitle = "";
        HashSet<Peak> spectrum = new HashSet<Peak>();
            BufferedReader br = new BufferedReader(new FileReader(aFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("BEGIN IONS")) {
                    spectrum = new HashSet<Peak>();
                } else if (line.startsWith("TITLE")) {
                    spectrumTitle = line.substring(line.indexOf('=')+1);
                } else if (line.startsWith("CHARGE")) {
                    precursorCharge = new Integer(line.substring(line.indexOf('=')+1, line.indexOf('=')+2));
                } else if (line.startsWith("PEPMASS")) {
                    precursorMass = new Double(line.substring(line.indexOf('=')+1));
                } else if (line.startsWith("RTINSECONDS")) {
                    rt = new Double(line.substring(line.indexOf('=')+1));
                } else if (line.equals("END IONS")) {
                    spectra.add(new MSnSpectrum(2, new Precursor(rt, precursorMass, new Charge(Charge.PLUS, precursorCharge)), spectrumTitle, spectrum, aFile.getName()));
                } else if (line.compareTo("")!=0) {
                    Double mz = new Double(line.substring(0, line.indexOf(' ')));
                    Double intensity = new Double(line.substring(line.indexOf(' ')));
                    spectrum.add(new Peak(mz, intensity));
                }
            }
            br.close();
        return spectra;
    }
}
