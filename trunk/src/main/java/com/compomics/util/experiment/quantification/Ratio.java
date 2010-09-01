package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.utils.ExperimentObject;
import com.compomics.util.experiment.biology.Sample;

/**
 * This class models an object.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 3:36:26 PM
 */
public class Ratio extends ExperimentObject{

    /**
     * the control sample
     */
    private Sample controlSample;

    /**
     * the measured sample
     */
    private Sample measureSample;

    /**
     * the corresponding ratio
     */
    private double ratio;


    /**
     * constructor for the Ratio
     * @param controlSample     the control sample
     * @param measureSample     the measured sample
     * @param ratio             the estimated ratio
     */
    public Ratio(Sample controlSample, Sample measureSample, double ratio) {
        this.controlSample = controlSample;
        this.measureSample = measureSample;
        this.ratio = ratio;
    }

    /**
     * Getter for the control sample
     * @return the control sample
     */
    public Sample getControlSample() {
        return controlSample;
    }

    /**
     * Getter for the measure sample
     * @return the measure sample
     */
    public Sample getMeasureSample() {
        return measureSample;
    }

    /**
     * Getter for the ratio
     * @return the estimated ratio
     */
    public double getRatio() {
        return ratio;
    }
}
