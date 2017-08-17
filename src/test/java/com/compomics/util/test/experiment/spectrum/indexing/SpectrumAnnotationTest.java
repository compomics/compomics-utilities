package com.compomics.util.test.experiment.spectrum.indexing;

import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.indexes.SpectrumIndex;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class tests the spectrum annotation.
 *
 * @author Marc Vaudel
 */
public class SpectrumAnnotationTest extends TestCase {

    /**
     * This test evaluates the SpectrumIndex.
     */
    public void testFindPeak() {
        
        HashMap<Double, Peak> peakList = new HashMap<>();
        peakList.put(1012.5, new Peak(1012.5, 12345));
        peakList.put(1012.8, new Peak(1012.8, 54321));
        
        SpectrumIndex spectrumIndex = new SpectrumIndex(peakList, 0.0, 0.05, false);
        ArrayList<Peak> matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.isEmpty());
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.52);
        Assert.assertTrue(matchingPeaks.size() == 1);
        Assert.assertTrue(matchingPeaks.get(0).mz == 1012.5);
        
        spectrumIndex = new SpectrumIndex(peakList, 0.0, 0.5, false);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.isEmpty());
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.52);
        Assert.assertTrue(matchingPeaks.size() == 2);
        
        spectrumIndex = new SpectrumIndex(peakList, 20000.0, 0.5, false);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.isEmpty());
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.52);
        Assert.assertTrue(matchingPeaks.size() == 1);
        Assert.assertTrue(matchingPeaks.get(0).mz == 1012.8);
        
        peakList.put(1012.51, new Peak(1012.51, 12354));
        
        spectrumIndex = new SpectrumIndex(peakList, 0.0, 20, true);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.isEmpty());
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.52);
        Assert.assertTrue(matchingPeaks.size() == 2);
        
        spectrumIndex = new SpectrumIndex(peakList, 0.0, 10, true);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.isEmpty());
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.49);
        Assert.assertTrue(matchingPeaks.size() == 1);
        Assert.assertTrue(matchingPeaks.get(0).mz == 1012.5);
        
    } 

    
    
}
