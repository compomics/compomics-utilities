/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.test.experiment.io.spectrum;

import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.SpectrumFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import java.io.File;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 * @author Genet
 */
public class SpectrumImportTestMsp extends TestCase {
    
     public void testSpectrumImportFromMgf() throws Exception {
        File mgfFile = new File("src/test/resources/experiment/test.msp");
        SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        spectrumFactory.addSpectra(mgfFile, waitingHandlerCLIImpl);

        Precursor precursor = spectrumFactory.getPrecursor("test.msp", " YDDMAAAMK/2");

        Assert.assertTrue(precursor.getPossibleCharges().get(0) == 2);
        
        Assert.assertTrue(precursor.getMz() == 498.5731);
        //Assert.assertTrue(precursor.getRt() == 218.6808);

        precursor = spectrumFactory.getPrecursor("test.msp", " YDDMAAAMK/3");

//        double rtMin = precursor.getRtWindow()[0];
//        double rtMax = precursor.getRtWindow()[1];
        Assert.assertTrue(precursor.getPossibleCharges().get(0)==3);
        Assert.assertTrue(precursor.getMz() == 332.3821);
        
    }
    
}
