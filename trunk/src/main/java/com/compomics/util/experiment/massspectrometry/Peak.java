package com.compomics.util.experiment.massspectrometry;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:00:58 AM
 * This class represents a peak.
 */
public class Peak implements Serializable {

    // Attributes
    public double mz;
    public double intensity;

    // Constructor
    public Peak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    // Methods

    public boolean isSameAs(Peak aPeak) {
        return mz == aPeak.mz && intensity == aPeak.intensity;
    }
}
