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
     * In case an rt window is given, the minimum
     */
    private Double rtMin;
    /**
     * In case an rt window is given, the maximum
     */
    private Double rtMax;
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
    private ArrayList<Charge> possibleCharges = new ArrayList<Charge>();

    /**
     * Constructor for the precursor.
     * 
     * @param rt                the retention time
     * @param mz                the m/z
     * @param possibleCharges   the possible charges
     */
    public Precursor(double rt, double mz, ArrayList<Charge> possibleCharges) {
        this.rt = rt;
        rtMin = rt;
        rtMax = rt;
        this.mz = mz;
        this.possibleCharges.addAll(possibleCharges);
    }
    
    /**
     * Cronstructor with rt window
     * @param rt                the retention time
     * @param mz                the m/z
     * @param intensity 
     * @param possibleCharges   the possible charges
     * @param rtMin             the minimum of the RT window
     * @param rtMax             the maximum of the RT window
     */
    public Precursor(double rt, double mz, double intensity, ArrayList<Charge> possibleCharges, double rtMin, double rtMax) {
        this.rt = rt;
        this.rtMin = rtMin;
        this.rtMax = rtMax;
        this.mz = mz;
        this.intensity = intensity;
        this.possibleCharges.addAll(possibleCharges);
    }
    
    
    /**
     * Cronstructor with rt window and no reference rt
     * @param mz                the m/z
     * @param intensity 
     * @param possibleCharges   the possible charges
     * @param rtMin             the minimum of the RT window
     * @param rtMax             the maximum of the RT window
     */
    public Precursor(double mz, double intensity, ArrayList<Charge> possibleCharges, double rtMin, double rtMax) {
        this.rt = (rtMin + rtMax) / 2;
        this.rtMin = rtMin;
        this.rtMax = rtMax;
        this.mz = mz;
        this.intensity = intensity;
        this.possibleCharges.addAll(possibleCharges);
    }
    
    /**
     * Constructor for the precursor.
     * 
     * @param rt
     * @param mz
     * @param intensity 
     * @param possibleCharges
     */
    public Precursor(double rt, double mz, double intensity, ArrayList<Charge> possibleCharges) {
        this.rt = rt;
        rtMin = rt;
        rtMax = rt;
        this.mz = mz;
        this.intensity = intensity;
        this.possibleCharges.addAll(possibleCharges);
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
     * Returns a boolean indicating whether the rt window was implemented
     * @return a boolean indicating whether the rt window was implemented
     */
    public boolean hasRTWindow() {
        return rtMin != null && rtMax != null;
    }
    
    /**
     * Returns an array containing the min and max of the RT window
     * @return an array containing the min and max of the RT window
     */
    public double[] getRtWindow() {
        if (rtMin == null) {
            rtMin = rt;
        }
        if (rtMax == null) {
            rtMax = rt;
        }
        return new double[] {rtMin, rtMax};
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
    
    /**
     * Returns the possible charges as a string
     * @return the possible charges as a string
     */
    public String getPossibleChargesAsString() {
        String result = "";
        boolean first = true;
        for (Charge charge : possibleCharges) {
            if (first) {
                first = false;
            } else {
                result += ", ";
            }
            result += charge.toString();
        }
        return result;
    }
    /**
     * Returns a recalibrated precursor
     * @param mzCorrection the m/z correction to apply
     * @param rtCorrection the rt correction to apply
     * @return a new recalibrated precursor
     */
    public Precursor getRecalibratedPrecursor(double mzCorrection, double rtCorrection) {
        return new Precursor(rt - rtCorrection, mz - mzCorrection, intensity, possibleCharges);
    }
}
