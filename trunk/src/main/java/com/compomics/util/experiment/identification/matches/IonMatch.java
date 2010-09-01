package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.utils.ExperimentObject;

/**
 * This class will model the assignement of a peak to a theoretical ion.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 1:29:41 PM
 */
public class IonMatch extends ExperimentObject {

    /**
     * The matched peak
     */
    public Peak peak;
    /**
     * The matching ion
     */
    public Ion ion;
    /**
     * The error made (in Da)
     */
    private double error;


    /**
     * Constructor for an ion peak
     *
     * @param aPeak the matched peak
     * @param anIon the corresponding ion
     */
    public IonMatch(Peak aPeak, Ion anIon) {
        peak = aPeak;
        ion = anIon;
    }

    /**
     * set the matching error
     *
     * @param error the matching error in Da
     */
    public void setError(double error) {
        this.error = error;
    }

    /**
     * get the matching error
     * 
     * @return the matching error
     */
    public double getError() {
        return error;
    }
}
