package com.compomics.util.experiment.massspectrometry;

import java.io.Serializable;

/**
 * This class represents a peak.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:00:58 AM
 */
public class Peak implements Serializable {

    /**
     * The mass over charge ratio of the peak.
     */
    public double mz;
    /**
     * The intensity of the peak.
     */
    public double intensity;

    /**
     * Constructor for a peak.
     *
     * @param mz the mz value of the peak
     * @param intensity the intensity of the peak
     */
    public Peak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    /**
     * Returns true if the peak has the same mz and intensity.
     *
     * @param aPeak the peal to compare this peak to
     * @return true if the peak has the same mz and intensity
     */
    public boolean isSameAs(Peak aPeak) {
        return mz == aPeak.mz && intensity == aPeak.intensity;
    }
}
