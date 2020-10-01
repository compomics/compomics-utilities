package com.compomics.util.experiment.mass_spectrometry.spectra;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class models a spectrum. Note: throughout the code, it is assumed that
 * the m/z array is sorted by ascending m/z. Only minimal sanity check is
 * conducted.
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
     * The array of the m/z of the peaks. Note: throughout the code, it is
     * assumed that the m/z array is sorted by ascending m/z. Only minimal
     * sanity check is conducted.
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
     * Constructor. Note: throughout the code, it is assumed that the m/z array
     * is sorted by ascending m/z. Only minimal sanity check is conducted.
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
        

        return Arrays.stream(intensity)
                .sum();

    }

    /**
     * Returns the max intensity value.
     *
     * @return the max intensity value. 0 if no peak.
     */
    public double getMaxIntensity() {
        

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
        

        return mz[mz.length - 1];

    }

    /**
     * Returns the min mz value.
     *
     * @return the min mz value
     */
    public double getMinMz() {
        

        return mz[0];

    }

    /**
     * Returns the precursor. Null if none.
     *
     * @return The precursor.
     */
    public Precursor getPrecursor() {

        
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

    /**
     * Returns a boolean indicating whether the spectrum is identical to the
     * other spectrum. Precursors are compared using the isSameAs method. M/z
     * and intensities must be in the same order with exact same double values.
     *
     * @param otherSpectrum The other spectrum.
     *
     * @return A boolean indicating whether the spectrum is identical to the
     * other spectrum.
     */
    public boolean isSameAs(
            Spectrum otherSpectrum
    ) {

        if (precursor == null && otherSpectrum.precursor != null
                || precursor != null && otherSpectrum.precursor == null
                || precursor != null && otherSpectrum.precursor != null && !precursor.isSameAs(otherSpectrum.precursor)) {

            return false;

        }

        if (getNPeaks() != otherSpectrum.getNPeaks()) {

            return false;

        }

        for (int i = 0; i < getNPeaks(); i++) {

            if (mz[i] != otherSpectrum.mz[i]
                    || intensity[i] != otherSpectrum.intensity[i]) {
                return false;
            }

        }

        return true;

    }

    @Override
    public String toString() {

        return "{precursor: " + precursor.toString() + "}{Peaks: " + getPeakListAsString() + "}";
    }
}
