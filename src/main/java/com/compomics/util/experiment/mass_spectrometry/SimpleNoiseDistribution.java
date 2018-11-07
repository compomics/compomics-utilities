package com.compomics.util.experiment.mass_spectrometry;

import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.math.statistics.distributions.NonSymmetricalNormalDistribution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.apache.commons.math.util.FastMath;

/**
 * This class makes a simple modeling of the noise using a normal distribution
 * on the log of the peak intensities and returns the associated binned
 * cumulative probability for a given intensity.
 *
 * @author Marc Vaudel
 */
public class SimpleNoiseDistribution {

    /**
     * Empty default constructor
     */
    public SimpleNoiseDistribution() {
        binSize = 0;
        orderedBins = null;
        pLog = null;
        intensityLogDistribution = null;
    }

    /**
     * The number of bins.
     */
    private final static int nBins = 100;
    /**
     * The bin size.
     */
    private final double binSize;
    /**
     * The ordered bins.
     */
    private final int[] orderedBins;
    /**
     * The p log values.
     */
    private final double[] pLog;
    /**
     * The noise distribution.
     */
    private final NonSymmetricalNormalDistribution intensityLogDistribution;

    /**
     * Constructor.
     * 
     * @param peakList the peak list
     */
    public SimpleNoiseDistribution(HashMap<Double, Peak> peakList) {

        ArrayList<Double> intensitiesLog = peakList.values().stream()
                .map(peak -> FastMath.log10(peak.intensity))
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
        
        intensityLogDistribution = NonSymmetricalNormalDistribution.getRobustNonSymmetricalNormalDistributionFromSortedList(intensitiesLog);

        orderedBins = new int[nBins - 1];
        pLog = new double[nBins - 1];
        binSize = 1.0 / nBins;

        for (int i = 1; i < nBins; i++) {
            
            double p = binSize * i;
            double x = intensityLogDistribution.getValueAtDescendingCumulativeProbability(p);
            orderedBins[i - 1] = (int) FastMath.pow(10, x);
            pLog[i - 1] = FastMath.log10(p);
            
        }
    }

    /**
     * Get the binned cumulative probability.
     * 
     * @param intensity the intensity
     * 
     * @return the binned cumulative probability
     */
    public double getBinnedCumulativeProbability(double intensity) {
        
        for (int i = 0; i < orderedBins.length; i++) {
            
            int bin = orderedBins[i];
            
            if (intensity > bin) {
                
                return binSize * i;
                
            }
        }
        
        return 1.0;
    }

    /**
     * Get the binned logged cumulative probability.
     * 
     * @param intensity the intensity
     * 
     * @return the binned cumulative logged probability
     */
    public double getBinnedCumulativeProbabilityLog(double intensity) {
        
        for (int i = 0; i < orderedBins.length; i++) {
            
            int bin = orderedBins[i];
            
            if (intensity > bin) {
                
                return pLog[i];
                
            }
        }
        
        return 0.0;
    }
    
    /**
     * Returns the log10 intensity at a given upper tale cumulative probability.
     * 
     * @param p the probability
     * 
     * @return the log10 intensity
     */
    public double getLogIntensityAtP(double p) {
        return intensityLogDistribution.getValueAtDescendingCumulativeProbability(p);
    }
    
    /**
     * Returns the intensity at a given upper tale cumulative probability.
     * 
     * @param p the probability
     * 
     * @return the intensity
     */
    public double getIntensityAtP(double p) {
        return FastMath.pow(10, getLogIntensityAtP(p));
    }
}
