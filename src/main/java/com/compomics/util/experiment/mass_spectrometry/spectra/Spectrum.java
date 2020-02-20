package com.compomics.util.experiment.mass_spectrometry.spectra;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class models a spectrum.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Spectrum extends ExperimentObject {

    /**
     * The precursor if any.
     */
    public Precursor precursor;
    /**
     * The array of the m/z of the peaks.
     */
    public double[] mz;
    /**
     * The array of the intensities of the peaks.
     */
    public double[] intensity;

    /**
     * Empty default constructor.
     */
    public Spectrum() {

        mz = new double[0];
        intensity = new double[0];
        precursor = null;

    }

    /**
     * Constructor.
     * 
     * @param precursor The precursor.
     * @param mz The array of mz of the peaks.
     * @param intensities The array of intensities of the peaks.
     */
    public Spectrum(
            Precursor precursor,
            double[] mz,
            double[] intensities
    ) {

        this.precursor = precursor;
        this.mz = mz;
        this.intensity = intensities;

    }

    /**
     * Returns the peak list as an array list formatted as text, e.g.
     * [[303.17334 3181.14],[318.14542 37971.93], ... ].
     *
     * @return the peak list as an array list formatted as text
     */
    public String getPeakListAsString() {
        readDBMode();

        return IntStream.range(0, mz.length)
                .mapToObj(
                        i -> String.join("", "[", Double.toString(mz[i]), " ", Double.toString(intensity[i]), "]")
                )
                .collect(
                        Collectors.joining(",", "[", "]")
                );
    }

    /**
     * Returns the total intensity of the spectrum.
     *
     * @return the total intensity. 0 if no peak.
     */
    public double getTotalIntensity() {
        readDBMode();

        return Arrays.stream(intensity)
                .sum();

    }

    /**
     * Returns the max intensity value.
     *
     * @return the max intensity value. 0 if no peak.
     */
    public double getMaxIntensity() {
        readDBMode();

        return Arrays.stream(intensity)
                .max()
                .orElse(0.0);

    }

    /**
     * Returns the max mz value.
     *
     * @return the max mz value
     */
    public double getMaxMz() {
        readDBMode();

        return Arrays.stream(mz)
                .max()
                .orElse(0.0);

    }

    /**
     * Returns the min mz value.
     *
     * @return the min mz value
     */
    public double getMinMz() {
        readDBMode();

        return Arrays.stream(intensity)
                .min()
                .orElse(0.0);

    }

    /**
     * Returns the precursor. Null if none.
     *
     * @return The precursor.
     */
    public Precursor getPrecursor() {

        readDBMode();
        return precursor;

    }
    
    /**
     * Returns the number of peaks.
     * 
     * @return the number of peaks
     */
    public int getNPeaks() {
        
        return mz.length;
        
    }

    @Override
    public String toString() {
        
        return "{precursor: " + precursor.toString() + "}{Peaks: " + getPeakListAsString() + "}";
    }
}
