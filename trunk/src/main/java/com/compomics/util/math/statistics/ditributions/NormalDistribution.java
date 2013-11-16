/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.math.statistics.ditributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.Distribution;
import java.util.ArrayList;

/**
 * This class represtents a normal distribution
 *
 * @author Marc
 */
public class NormalDistribution implements Distribution {
 
    /**
     * The mean
     */
    private double mean;
    /**
     * The standard deviation
     */
    private double std;
    /**
     * Constructor
     * 
     * @param mean the mean
     * @param std the standard deviation
     */
    public NormalDistribution(double mean, double std) {
        this.mean = mean;
        this.std = std;
    }
    
    /**
     * Returns the normal distribution corresponding to a given list of double calibrated on mean and standard deviation.
     * 
     * @param input the input as a list of double
     * 
     * @return the normal distribution calibrated on the mean and standard deviation
     */
    public static NormalDistribution getNormalDistribution(ArrayList<Double> input) {
        return new NormalDistribution(BasicMathFunctions.mean(input), BasicMathFunctions.std(input));
    }
    
    /**
     * Returns the normal distribution corresponding to a given list of double calibrated on median and 34.1% percentile to median distance
     * 
     * @param input the input as list of double
     * 
     * @return a normal distribution calibrated on median and 34.1% percentile to median distance
     */
    public static NormalDistribution getRobustNormalDistribution(ArrayList<Double> input) {
        double std = (BasicMathFunctions.percentile(input, 0.841) - BasicMathFunctions.percentile(input, 0.159))/2;
        return new NormalDistribution(BasicMathFunctions.median(input), std);
    }

    @Override
    public Double getProbabilityAt(double x) {
        return Math.pow(Math.E, -Math.pow(x-mean, 2)/(2*Math.pow(std, 2)))/(std*Math.pow(2*Math.PI, 0.5));
    }

    @Override
    public Double getCumulativeProbabilityAt(double x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getMaxValueForProbability(double p) {
        return mean + Math.pow(-2*Math.pow(std, 2)*Math.log(std*p*Math.pow(2*Math.PI, 0.5)), 0.5);
    }

    @Override
    public Double getMinValueForProbability(double p) {
        return mean - Math.pow(-2*Math.pow(std, 2)*Math.log(std*p*Math.pow(2*Math.PI, 0.5)), 0.5);
    }

    @Override
    public Double getValueAtCumulativeProbability(double p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
