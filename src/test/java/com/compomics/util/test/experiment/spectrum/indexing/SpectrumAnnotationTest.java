package com.compomics.util.test.experiment.spectrum.indexing;

import com.compomics.util.experiment.mass_spectrometry.indexes.SpectrumIndex;
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
        
        double[] mz = new double[]{1012.5, 1012.8};
        double[] intensity = new double[]{12345, 54321};
        
        SpectrumIndex spectrumIndex = new SpectrumIndex(
                mz,
                intensity, 
                0.0, 
                0.05, 
                false
        );
        int[] matchingPeaks = spectrumIndex.getMatchingPeaks(1000.0);
        Assert.assertTrue(matchingPeaks.length == 0);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.52);
        Assert.assertTrue(matchingPeaks.length == 1);
        Assert.assertTrue(matchingPeaks[0] == 0);
        
        spectrumIndex = new SpectrumIndex(
                mz, 
                intensity,
                0.0, 
                0.5, 
                false
        );
        matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.length == 0);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.52);
        Assert.assertTrue(matchingPeaks.length == 2);
        
        spectrumIndex = new SpectrumIndex(
                mz, 
                intensity,
                20000.0, 
                0.5, 
                false
        );
        matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.length == 0);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.52);
        Assert.assertTrue(matchingPeaks.length == 1);
        Assert.assertTrue(matchingPeaks[0] == 1);
        
        mz = new double[]{1012.5, 1012.51, 1012.8};
        intensity = new double[]{12345, 12354, 54321};
        
        spectrumIndex = new SpectrumIndex(
                mz, 
                intensity,
                0.0, 
                20, 
                true
        );
        matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.length == 0);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.52);
        Assert.assertTrue(matchingPeaks.length == 2);
        
        spectrumIndex = new SpectrumIndex(
                mz, 
                intensity,
                0.0, 
                10, 
                true
        );
        matchingPeaks = spectrumIndex.getMatchingPeaks(1000);
        Assert.assertTrue(matchingPeaks.length == 0);
        matchingPeaks = spectrumIndex.getMatchingPeaks(1012.49);
        Assert.assertTrue(matchingPeaks.length == 1);
        Assert.assertTrue(matchingPeaks[0] == 0);
        
    } 

    
    
}
