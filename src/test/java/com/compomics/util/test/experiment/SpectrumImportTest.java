package com.compomics.util.test.experiment;

import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumCollection;
import java.io.File;
import junit.framework.TestCase;

/**
 * This class can be used to test the loading of an mzML file
 *
 * @author Marc
 */
public class SpectrumImportTest extends TestCase {

/**
    public void testMZMLReading() {
        try {
        File mzmlFile = new File("testFiles/test.mzML");
        SpectrumCollection spectrumCollection = new SpectrumCollection();
        spectrumCollection.addSpectra(mzmlFile);
        String spectrumTitle = "controllerType=0 controllerNumber=1 scan=240";
        String spectrumKey = Spectrum.getSpectrumKey(mzmlFile.getName(), spectrumTitle);

        double precursorRT = ((MSnSpectrum) spectrumCollection.getSpectrum(spectrumKey)).getPrecursor().getRt();
        boolean result = precursorRT == 4.731466666666666;
        }catch (Exception e) {
            int debug = 0;
            e.printStackTrace();
        }
    }

    public void testMgfReading() {
        try {
        File mgfFile = new File("testFiles/test.mgf");
        SpectrumCollection spectrumCollection = new SpectrumCollection();
        spectrumCollection.addSpectra(mgfFile);
        String spectrumTitle = "574.772094726563_116.7691";
        String spectrumKey = Spectrum.getSpectrumKey(mgfFile.getName(), spectrumTitle);

        double precursorRT = ((MSnSpectrum) spectrumCollection.getSpectrum(spectrumKey)).getPrecursor().getRt();
        boolean result = precursorRT == 4.731466666666666;
        }catch (Exception e) {
            int debug = 0;
            e.printStackTrace();
        }
    }**/


}
