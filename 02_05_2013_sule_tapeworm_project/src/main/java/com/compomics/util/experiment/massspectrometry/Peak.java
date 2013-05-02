package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class represents a peak.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:00:58 AM
 */
public class Peak extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -7425947046833405676L;
    /**
     * The mass over charge ratio of the peak.
     */
    public double mz;
    /**
     * The retention time when the peak was recorded.
     */
    public double rt;
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
     * Constructor for a peak.
     *
     * @param mz the mz value of the peak
     * @param intensity the intensity of the peak
     * @param rt the retention time when the peak was recorded
     */
    public Peak(double mz, double intensity, double rt) {
        this.mz = mz;
        this.intensity = intensity;
        this.rt = rt;
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.mz) ^ (Double.doubleToLongBits(this.mz) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.rt) ^ (Double.doubleToLongBits(this.rt) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.intensity) ^ (Double.doubleToLongBits(this.intensity) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Peak other = (Peak) obj;
        if (Double.doubleToLongBits(this.mz) != Double.doubleToLongBits(other.mz)) {
            return false;
        }
        if (Double.doubleToLongBits(this.rt) != Double.doubleToLongBits(other.rt)) {
            return false;
        }
        if (Double.doubleToLongBits(this.intensity) != Double.doubleToLongBits(other.intensity)) {
            return false;
        }
        return true;
    }

    public double getMz() {
        return mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
   
    public int compareTo(Peak p) {
        return Double.compare(this.getIntensity(), p.getIntensity());
    }
    
    
    
}
