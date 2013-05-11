/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.angrypeptide;

import com.compomics.angrypeptide.bijection.BoringToFun;
import com.compomics.util.experiment.biology.ElementaryElement;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.angrypeptide.bijection.MatchingParameters;
import com.compomics.angrypeptide.fun.Shot;
import com.compomics.angrypeptide.fun.Targets;
import com.compomics.util.experiment.massspectrometry.Precursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Marc
 */
public class AngryPeptide {

    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
    /**
     * Path to the example mgf file
     */
    private static final String spectrumFile = "src/test/resources/angrypeptide/demo.mgf";
    /**
     * The matching parameters
     */
    private MatchingParameters matchingParameters = new MatchingParameters();
    private BoringToFun boringToFun;

    /**
     * Constructor
     */
    public AngryPeptide() throws FileNotFoundException, IOException, ClassNotFoundException {

        boringToFun = new BoringToFun(matchingParameters);
        loadSpectra();


    }

    /**
     * Loads the data for the example
     */
    private void loadSpectra() throws FileNotFoundException, IOException, ClassNotFoundException {
        spectrumFactory.addSpectra(new File(spectrumFile));
    }
    
    public double getMaxDistance(int spectrumNumber) throws IOException, MzMLUnmarshallerException {
        Precursor precursor = spectrumFactory.getPrecursor("demo.mgf", spectrumNumber + "");
        
        int precursorCharge;
        if (spectrumNumber == 1) {
            precursorCharge = 3;
        } else {
            precursorCharge = 2;
        }
            
        return precursorCharge * precursor.getMz() - precursorCharge * ElementaryIon.proton.getTheoreticMass();
        
    }

    public Targets getTargets(int spectrumNumber) throws IOException, MzMLUnmarshallerException {

        MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum("demo.mgf", spectrumNumber + "");
        
        int precursorCharge;
        if (spectrumNumber == 1) {
            precursorCharge = 3;
        } else {
            precursorCharge = 2;
        }

        return boringToFun.getTargets(spectrum, precursorCharge);
    }

    public MatchingParameters getMatchingParameters() {
        return matchingParameters;
    }
}
