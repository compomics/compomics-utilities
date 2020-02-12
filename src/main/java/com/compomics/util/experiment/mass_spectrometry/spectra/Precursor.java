package com.compomics.util.experiment.mass_spectrometry.spectra;

import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This class models a precursor.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Precursor extends ExperimentObject {

    /**
     * Empty default constructor
     */
    public Precursor() {
        this.rt = Double.NaN;
        this.rtMin = Double.NaN;
        this.rtMax = Double.NaN;
        this.mz = Double.NaN;
        this.intensity = Double.NaN;
        this.possibleCharges = new int[0];
    }
    
    /**
     * The retention time when the precursor was isolated.
     */
    public final double rt;
    /**
     * In case a retention time window is given, the minimum.
     */
    public final double rtMin;
    /**
     * In case a retention time window is given, the maximum.
     */
    public final double rtMax;
    /**
     * The measured m/z of the precursor.
     */
    public final double mz;
    /**
     * The measured intensity of the precursor.
     */
    public final double intensity;
    /**
     * The charge of the precursor.
     */
    public final int[] possibleCharges;

    /**
     * Constructor for the precursor.
     *
     * @param rt the retention time
     * @param mz the m/z
     * @param possibleCharges the possible charges
     */
    public Precursor(
            double rt, 
            double mz, 
            int[] possibleCharges
    ) {
        this.rt = rt;
        this.rtMin = rt;
        this.rtMax = rt;
        this.mz = mz;
        this.intensity = Double.NaN;
        this.possibleCharges = new int[possibleCharges.length];
        System.arraycopy(possibleCharges, 0, this.possibleCharges, 0, possibleCharges.length);
    }

    /**
     * Constructor with retention time window.
     *
     * @param rt the retention time
     * @param mz the m/z
     * @param intensity the intensity
     * @param possibleCharges the possible charges
     * @param rtMin the minimum of the RT window
     * @param rtMax the maximum of the RT window
     */
    public Precursor(
            double rt, 
            double mz, 
            double intensity, 
            int[] possibleCharges, 
            double rtMin, 
            double rtMax
    ) {
        this.rt = rt;
        this.rtMin = rtMin;
        this.rtMax = rtMax;
        this.mz = mz;
        this.intensity = intensity;
        this.possibleCharges = new int[possibleCharges.length];
        System.arraycopy(possibleCharges, 0, this.possibleCharges, 0, possibleCharges.length);
    }

    /**
     * Constructor with retention time window and no reference retention time.
     *
     * @param mz the m/z
     * @param intensity the intensity
     * @param possibleCharges the possible charges
     * @param rtMin the minimum of the RT window in seconds
     * @param rtMax the maximum of the RT window in seconds
     */
    public Precursor(
            double mz, 
            double intensity, 
            int[] possibleCharges, 
            double rtMin, 
            double rtMax
    ) {
        this.rt = (rtMin + rtMax) / 2;
        this.rtMin = rtMin;
        this.rtMax = rtMax;
        this.mz = mz;
        this.intensity = intensity;
        this.possibleCharges = new int[possibleCharges.length];
        System.arraycopy(possibleCharges, 0, this.possibleCharges, 0, possibleCharges.length);
    }

    /**
     * Constructor for the precursor.
     *
     * @param rt the retention time in seconds
     * @param mz the m/z
     * @param intensity the intensity
     * @param possibleCharges the possible charges
     */
    public Precursor(
            double rt, 
            double mz, 
            double intensity, 
            int[] possibleCharges
    ) {
        this.rt = rt;
        rtMin = rt;
        rtMax = rt;
        this.mz = mz;
        this.intensity = intensity;
        this.possibleCharges = new int[possibleCharges.length];
        System.arraycopy(possibleCharges, 0, this.possibleCharges, 0, possibleCharges.length);
    }

    /**
     * Returns the retention time in minutes.
     *
     * @return the retention time in minutes
     */
    public double getRtInMinutes() {
        readDBMode();
        return rt / 60;
    }
    
    /**
     * Returns the possible charges as a string.
     *
     * @return the possible charges as a string
     */
    public String getPossibleChargesAsString() {
        
        readDBMode();
        
        return Arrays.stream(possibleCharges)
                .mapToObj(
                        charge -> Integer.toString(charge)
                )
                .collect(Collectors.joining(", "));
        
    }

    /**
     * Returns the mass of the precursor with the given charge.
     *
     * @param chargeValue the value of the charge
     *
     * @return the mass of the precursor with the given charge
     */
    public double getMass(
            int chargeValue
    ) {
        
        readDBMode();
        return mz * chargeValue - chargeValue * ElementaryIon.proton.getTheoreticMass();
    
    }
}
