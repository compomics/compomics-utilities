package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class models an object.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 3:36:26 PM
 */
public class Ratio extends ExperimentObject{

    /**
     * the measured label indexed by its corresponding reporter ion index
     */
    private int measureLabel;

    /**
     * the corresponding ratio
     */
    private double ratio;


    /**
     * constructor for the Ratio
     * @param measureLabel     the measured label indexed by its corresponding reporter ion index
     * @param ratio             the estimated ratio
     */
    public Ratio(int measureLabel, double ratio) {
        this.measureLabel = measureLabel;
        this.ratio = ratio;
    }

    /**
     * returns the measured label indexed by its corresponding reporter ion index
     * @return the measured label indexed by its corresponding reporter ion index
     */
    public int getMeasureSample() {
        return measureLabel;
    }

    /**
     * returns the estimated ratio
     * @return the estimated ratio
     */
    public double getRatio() {
        return ratio;
    }
}
