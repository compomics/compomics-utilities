package com.compomics.util.experiment.mass_spectrometry.spectra;

import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.mass_spectrometry.SimpleNoiseDistribution;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.math.BasicMathFunctions;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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
    public final Precursor precursor;
    /**
     * The array of the m/z of the peaks.
     */
    public final double[] mz;
    /**
     * The array of the intensities of the peaks.
     */
    public final double[] intensity;

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
     * Returns the limit in intensity according to the given threshold.
     *
     * @param intensityThresholdType the type of intensity threshold
     * @param intensityThreshold the threshold value
     *
     * @return the intensity limit
     */
    public double getIntensityLimit(
            AnnotationParameters.IntensityThresholdType intensityThresholdType,
            double intensityThreshold
    ) {
        readDBMode();

        if (intensityThreshold == 0) {

            return 0.0;

        } else if (intensityThreshold == 1.0) {

            return getMaxIntensity();

        }

        switch (intensityThresholdType) {

            case snp:

                SimpleNoiseDistribution tempBinnedCumulativeFunction = new SimpleNoiseDistribution(intensity);
                return tempBinnedCumulativeFunction.getIntensityAtP(1 - intensityThreshold);

            case percentile:

                return BasicMathFunctions.percentile(intensity, intensityThreshold);

            default:
                throw new UnsupportedOperationException("Threshold of type " + intensityThresholdType + " not supported.");
        }
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
