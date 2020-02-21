package com.compomics.util.test.experiment.io.spectrum.cms;

import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileReader;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileWriter;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.File;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for the Compomics Mass Spectrometry (cms) file writer and reader.
 *
 * @author Marc Vaudel
 */
public class CmsFileTest extends TestCase {

    @Test
    public void testGetAllSpectrumMatches() throws Exception {

        String title1 = "Spectrum 1";
        Spectrum spectrum1 = new Spectrum(
                new Precursor(
                        123.0,
                        456.0,
                        new int[]{1, 2, 3}
                ),
                new double[]{1.0, 2.0, 3.0},
                new double[]{4.0, 5.0, 6.0}
        );
        String title2 = "Spectrum 2";
        Spectrum spectrum2 = new Spectrum(
                new Precursor(
                        456.0,
                        789.0,
                        new int[]{4, 5, 6}
                ),
                new double[]{4.0, 5.0, 6.0},
                new double[]{7.0, 8.0, 9.0}
        );
        String title3 = "Spectrum 3";
        Spectrum spectrum3 = new Spectrum(
                new Precursor(
                        789.0,
                        123.0,
                        new int[]{7, 8, 9}
                ),
                new double[]{7.0, 8.0, 9.0},
                new double[]{1.0, 2.0, 3.0}
        );
        
        File cmsFile = new File("src/test/resources/experiment/test.cms");
        
        try (CmsFileWriter writer = new CmsFileWriter(cmsFile)) {
            
            writer.addSpectrum(title1, spectrum1);
            writer.addSpectrum(title2, spectrum2);
            writer.addSpectrum(title3, spectrum3);
            
        }
        
        try (CmsFileReader reader = new CmsFileReader(cmsFile)) {
            
            Spectrum cmsSpectrum1 = reader.getSpectrum(title1);
            
            Assert.assertTrue(
                    "Extracted spectrum is not identical to original spectrum", 
                    spectrum1.isSameAs(cmsSpectrum1)
                    );
            
            Spectrum cmsSpectrum2 = reader.getSpectrum(title2);
            
            Assert.assertTrue(
                    "Extracted spectrum is not identical to original spectrum", 
                    spectrum2.isSameAs(cmsSpectrum2)
                    );
            
            Spectrum cmsSpectrum3 = reader.getSpectrum(title3);
            
            Assert.assertTrue(
                    "Extracted spectrum is not identical to original spectrum", 
                    spectrum3.isSameAs(cmsSpectrum3)
                    );
            
        }

    }
}
