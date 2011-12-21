package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;

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
    private ArrayList<Charge> possibleCharges;

    /**
     * Constructor for the precursor.
     * 
     * @param rt                the retention time
     * @param mz                the m/z
     * @param possibleCharges   the possible charges
     */
    public Precursor(double rt, double mz, ArrayList<Charge> possibleCharges) {
        this.rt = rt;
        this.mz = mz;
        this.possibleCharges = possibleCharges;
    }
    
    /**
     * Constructor for the precursor.
     * 
     * @param rt
     * @param mz
     * @param intensity 
     * @param charge
     */
    public Precursor(double rt, double mz, double intensity, ArrayList<Charge> possibleCharges) {
        this.rt = rt;
        this.mz = mz;
        this.intensity = intensity;
        this.possibleCharges = possibleCharges;
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
     * Getter for the possible charges
     * 
     * @return the possible charges
     */
    public ArrayList<Charge> getPossibleCharges() {
        return possibleCharges;
    }
}
