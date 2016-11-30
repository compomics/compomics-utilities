package com.compomics.util.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Simple functions to manipulate histograms.
 * A histogram is here represented as a HashMap where the keys are the bin values and the values the occurrence of the bin values
 *
 * @author Marc Vaudel
 */
public class HistogramUtils {
    
    /**
     * Returns the median of a histogram.
     * 
     * @param histogram the histogram
     * 
     * @return the median
     */
    public static Double getMedianValue(HashMap<Double, Integer> histogram) {
        long nValues = 0;
        for (Integer value : histogram.values()) {
            nValues += value;
        }
        return getMedianValue(histogram, nValues);
    }
    
    /**
     * Returns the median of a histogram.
     * 
     * @param histogram the histogram
     * @param nValues the number of values in the histogram
     * 
     * @return the median
     */
    public static Double getMedianValue(HashMap<Double, Integer> histogram, long nValues) {
        ArrayList<Double> values = new ArrayList<Double>(histogram.keySet());
        Collections.sort(values);
        long currentSum = 0;
        long previousSum = 0;
        Double previousValue = 0.0;
        for (Double value : values) {
            Integer currentOccurence = histogram.get(value);
            currentSum += currentOccurence;
            if (2*currentSum >= nValues) {
                if (2*(previousSum + 1) > nValues && previousSum > 0) {
                    return (previousValue + value) / 2;
                }
                return value;
            }
            previousValue = value;
        }
        throw new IllegalArgumentException("Reached the end of the histogram before reaching the median.");
    }
    
    /**
     * Merges histograms in a single histogram by adding the values in each bin.
     * 
     * @param histograms the histograms to merge
     * 
     * @return the merged histogram
     */
    public static HashMap<Double, Integer> mergeHistograms(Collection<HashMap<Double, Integer>> histograms) {
        HashMap<Double, Integer> result = new HashMap<Double, Integer>();
        for (HashMap<Double, Integer> histogram : histograms) {
            for (Double value : histogram.keySet()) {
                Integer histogramValue = histogram.get(value);
                Integer resultValue = result.get(value);
                if (resultValue == null) {
                    result.put(value, histogramValue);
                } else if (Integer.MAX_VALUE - histogramValue < resultValue) {
                    throw new IllegalArgumentException("Reached the maximal capacity of an integer in a histogram bin.");
                } else {
                    result.put(value, resultValue + histogramValue);
                }
            }
        }
        return result;
    }

}
