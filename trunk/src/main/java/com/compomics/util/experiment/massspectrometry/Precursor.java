package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class modilizes the precursor.
 * 
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Precursor extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -2711244157697138296L;
    /**
     * The retention time when the precursor was isolated.
     */
    private double rt;
    /**
     * The measured m/z of the precursor.
     */
    private double mz;
    /**
     * The measured intensity of the precursor.
     */
    private double intensity = 0;
    /**
     * The charge of the precursor.
     */
    private Charge charge;

    /**
     * Constructor for the precursor.
     * 
     * @param rt
     * @param mz
     * @param charge
     */
    public Precursor(double rt, double mz, Charge charge) {
        this.rt = rt;
        this.mz = mz;
        this.charge = charge;
    }
    
    /**
     * Constructor for the precursor.
     * 
     * @param rt
     * @param mz
     * @param intensity 
     * @param charge
     */
    public Precursor(double rt, double mz, double intensity, Charge charge) {
        this.rt = rt;
        this.mz = mz;
        this.intensity = intensity;
        this.charge = charge;
    }

    /**
     * Getter for the retention time.
     * 
     * @return precursor retention time
     */
    public double getRt() {
        return rt;
    }

    /**
     * Getter for the m/z.
     * 
     * @return precursor m/z
     */
    public double getMz() {
        return mz;
    }
    
    /**
     * Getter for the intensity.
     * 
     * @return precursor intensity
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * Getter for the charge.
     * 
     * @return precursor charge
     */
    public Charge getCharge() {
        return charge;
    }
}
