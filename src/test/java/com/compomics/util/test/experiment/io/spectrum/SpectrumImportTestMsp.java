/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.test.experiment.io.spectrum;


import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
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

        Assert.assertTrue(precursor.getPossibleCharges().get(0).value == 2);
        Assert.assertTrue(precursor.getPossibleCharges().get(0).sign == Charge.NEUTRAL);
//        Assert.assertTrue(precursor.getPossibleCharges().get(1).value == 3);
//        Assert.assertTrue(precursor.getPossibleCharges().get(1).sign == Charge.PLUS);
//        Assert.assertTrue(precursor.getPossibleCharges().get(2).value == 4);
//        Assert.assertTrue(precursor.getPossibleCharges().get(2).sign == Charge.MINUS);
        Assert.assertTrue(precursor.getMz() == 498.5731);
        //Assert.assertTrue(precursor.getRt() == 218.6808);

        precursor = spectrumFactory.getPrecursor("test.msp", " YDDMAAAMK/3");

//        double rtMin = precursor.getRtWindow()[0];
//        double rtMax = precursor.getRtWindow()[1];
        Assert.assertTrue(precursor.getPossibleCharges().get(0).value==3);
        Assert.assertTrue(precursor.getPossibleCharges().get(0).sign == Charge.NEUTRAL);
        Assert.assertTrue(precursor.getMz() == 332.3821);
        
    }
    
}
