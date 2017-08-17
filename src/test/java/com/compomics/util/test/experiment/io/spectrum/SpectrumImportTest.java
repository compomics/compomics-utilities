package com.compomics.util.test.experiment.io.spectrum;

import com.compomics.util.experiment.mass_spectrometry.Charge;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.SpectrumFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;

/**
 * This test case will test the mgf import and spectrum annotation
 *
 * @author Marc Vaudel
 */
public class SpectrumImportTest extends TestCase {

    public void testSpectrumImportFromMgf() throws Exception {
        File mgfFile = new File("src/test/resources/experiment/test.mgf");
        SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        spectrumFactory.addSpectra(mgfFile, waitingHandlerCLIImpl);

        Precursor precursor = spectrumFactory.getPrecursor("test.mgf", "controllerType=0 controllerNumber=1 scan=159");

        Assert.assertTrue(precursor.getPossibleCharges().get(0).value == 2);
        Assert.assertTrue(precursor.getPossibleCharges().get(0).sign == Charge.PLUS);
        Assert.assertTrue(precursor.getPossibleCharges().get(1).value == 3);
        Assert.assertTrue(precursor.getPossibleCharges().get(1).sign == Charge.PLUS);
        Assert.assertTrue(precursor.getPossibleCharges().get(2).value == 4);
        Assert.assertTrue(precursor.getPossibleCharges().get(2).sign == Charge.MINUS);
        Assert.assertTrue(precursor.getMz() == 1060.86962890625);
        Assert.assertTrue(precursor.getRt() == 218.6808);

        precursor = spectrumFactory.getPrecursor("test.mgf", "controllerType=0 controllerNumber=1 scan=160");

        double rtMin = precursor.getRtWindow()[0];
        double rtMax = precursor.getRtWindow()[1];
        Assert.assertTrue(rtMin == 218);
        Assert.assertTrue(rtMax == 219.71);
        Assert.assertTrue(Math.abs(precursor.getRt() - 218.855) < 0.0001);
    }
}
