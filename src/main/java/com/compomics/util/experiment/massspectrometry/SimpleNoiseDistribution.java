package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.math.statistics.distributions.NonSymmetricalNormalDistribution;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.util.FastMath;

/**
 * This class makes a simple modeling of the noise using a normal distribution on the log of the peak intensities and returns the associated binned cumulative probability for a given intensity.
 *
 * @author Marc Vaudel
 */
public class SimpleNoiseDistribution {

    private final static int nBins = 100;

    private final double binSize;

    private int[] orderedBins;
    private double[] pLog;

    public SimpleNoiseDistribution(HashMap<Double, Peak> peakList) throws MathException {

        ArrayList<Double> intensitiesLog = new ArrayList<Double>(peakList.size());
        for (Peak peak : peakList.values()) {
            double log = FastMath.log10(peak.intensity);
            intensitiesLog.add(log);
        }
        Collections.sort(intensitiesLog);
        NonSymmetricalNormalDistribution intensityLogDistribution = NonSymmetricalNormalDistribution.getRobustNonSymmetricalNormalDistributionFromSortedList(intensitiesLog);

        orderedBins = new int[nBins - 1];
        pLog = new double[nBins - 1];
        binSize = 1.0 / nBins;
        
        for (int i = 1; i < nBins; i++) {
            double p = binSize * i;
            double x = intensityLogDistribution.getValueAtDescendingCumulativeProbability(p);
            orderedBins[i - 1] = (int) FastMath.pow(10, x);
            pLog[i-1] = FastMath.log10(p);
        }
    }

    public double getBinnedCumulativeProbability(double intensity) {

        for (int i = 0; i < orderedBins.length; i++) {
            int bin = orderedBins[i];
            if (intensity > bin) {
                return binSize * i;
            }
        }
        return 1.0;
    }

    public double getBinnedCumulativeProbabilityLog(double intensity) {

        for (int i = 0; i < orderedBins.length; i++) {
            int bin = orderedBins[i];
            if (intensity > bin) {
                return pLog[i];
            }
        }
        return 0.0;
    }
}
