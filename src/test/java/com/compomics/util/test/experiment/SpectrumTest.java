package com.compomics.util.test.experiment;

import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import java.io.File;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This test case will test the mgf import and spectrum annotation
 *
 * @author Marc
 */
public class SpectrumTest extends TestCase {

    public void testSpectrumImport() throws Exception {
        File mgfFile = new File("src/test/resources/experiment/test.mgf");
        SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

        spectrumFactory.addSpectra(mgfFile);

        Precursor precursor = spectrumFactory.getPrecursor("test.mgf", "controllerType=0 controllerNumber=1 scan=159");

        Assert.assertTrue(precursor.getPossibleCharges().get(0).value == 2);
        Assert.assertTrue(precursor.getPossibleCharges().get(0).sign == Charge.PLUS);
        Assert.assertTrue(precursor.getPossibleCharges().get(1).value == 3);
        Assert.assertTrue(precursor.getPossibleCharges().get(1).sign == Charge.PLUS);
        Assert.assertTrue(precursor.getPossibleCharges().get(2).value == 4);
        Assert.assertTrue(precursor.getPossibleCharges().get(2).sign == Charge.MINUS);
        Assert.assertTrue(precursor.getMz() == 1060.86962890625);
        Assert.assertTrue(precursor.getRt() == 218.6808);
    }
}
