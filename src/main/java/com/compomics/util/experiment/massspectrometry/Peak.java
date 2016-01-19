package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.Comparator;

/**
 * This class represents a peak.
 *
 * @author Marc Vaudel
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

    /**
     * Returns the mz.
     *
     * @return the mz
     */
    public double getMz() {
        return mz;
    }

    /**
     * Set the mz.
     *
     * @param mz the value to set
     */
    public void setMz(double mz) {
        this.mz = mz;
    }

    /**
     * Returns the intensity.
     *
     * @return the intensity
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * Set the intensity.
     *
     * @param intensity the intensity to set
     */
    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    /**
     * Compare two peaks in regards to their intensity.
     *
     * @param p the peak to compare against
     * @return 0 if numerically equal; a value less than 0 if the intensity of
     * this peak is numerically less than the intensity of the peak we are
     * comparing against; and a value greater than 0 if the intensity of this
     * peak is numerically greater than the intensity of the peak we are
     * comparing against.
     */
    public int compareTo(Peak p) {
        return Double.compare(this.getIntensity(), p.getIntensity());
    }

    /**
     * Returns the mass of the compound with the given charge.
     *
     * @param chargeValue the value of the charge
     *
     * @return the mass of the compound with the given charge
     */
    public double getMass(int chargeValue) {
        return mz * chargeValue - chargeValue * ElementaryIon.proton.getTheoreticMass();
    }

    /**
     * This comparator compares 2 Peak instances on ascending intensity.
     */
    public static final Comparator<Peak> AscendingIntensityComparator
            = new Comparator<Peak>() {
                @Override
                public int compare(Peak o1, Peak o2) {
                    return o1.getIntensity() < o2.getIntensity() ? -1 : o1.getIntensity() == o2.getIntensity() ? 0 : 1;
                }
            };

    /**
     * This comparator compares 2 Peak instances on descending intensity.
     */
    public static final Comparator<Peak> DescendingIntensityComparator
            = new Comparator<Peak>() {
                @Override
                public int compare(Peak o1, Peak o2) {
                    return o1.getIntensity() > o2.getIntensity() ? -1 : o1.getIntensity() == o2.getIntensity() ? 0 : 1;
                }
            };

    /**
     * This comparator compares 2 Peak instances on ascending m/z value.
     */
    public static final Comparator<Peak> AscendingMzComparator
            = new Comparator<Peak>() {
                @Override
                public int compare(Peak o1, Peak o2) {
                    return o1.getMz() < o2.getMz() ? -1 : o1.getMz() == o2.getMz() ? 0 : 1;
                }
            };
}
