package com.compomics.util.experiment.mass_spectrometry.spectra;

import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.mass_spectrometry.SimpleNoiseDistribution;
import com.compomics.util.math.BasicMathFunctions;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Utilities functions to handle spectra.
 *
 * @author Marc Vaudel
 */
public class SpectrumUtil {

    /**
     * Returns the limit in intensity according to the given threshold.
     *
     * @param spectrum The spectrum.
     * @param intensityThresholdType The type of intensity threshold.
     * @param thresholdValue The threshold value.
     *
     * @return the intensity limit
     */
    public static double getIntensityLimit(
            Spectrum spectrum,
            AnnotationParameters.IntensityThresholdType intensityThresholdType,
            double thresholdValue
    ) {

        if (thresholdValue == 0) {

            return 0.0;

        } else if (thresholdValue == 1.0) {

            return spectrum.getMaxIntensity();

        }

        switch (intensityThresholdType) {

            case snp:

                SimpleNoiseDistribution tempBinnedCumulativeFunction = new SimpleNoiseDistribution(spectrum.intensity);
                return tempBinnedCumulativeFunction.getIntensityAtP(1 - thresholdValue);

            case percentile:

                return BasicMathFunctions.percentile(spectrum.intensity, thresholdValue);

            default:
                throw new UnsupportedOperationException("Threshold of type " + intensityThresholdType + " not supported.");
        }
    }

    /**
     * Returns the peaks above intensity threshold as an array of double (mz,
     * intensity).
     *
     * @param spectrum The spectrum.
     * @param intensityThresholdType The type of intensity threshold.
     * @param thresholdValue The threshold value.
     *
     * @return The peaks above intensity threshold.
     */
    public static double[][] getPeaksAboveIntensityThreshold(
            Spectrum spectrum,
            AnnotationParameters.IntensityThresholdType intensityThresholdType,
            double thresholdValue
    ) {

        double intensityThreshold = getIntensityLimit(
                spectrum,
                intensityThresholdType,
                thresholdValue
        );

        ArrayList<Integer> indexes = new ArrayList<>(spectrum.getNPeaks() / 3);

        for (int i = 0; i < spectrum.getNPeaks(); i++) {

            if (spectrum.intensity[i] > intensityThreshold) {

                indexes.add(i);

            }
        }

        double[][] result = new double[indexes.size()][2];

        for (int i = 0; i < indexes.size(); i++) {

            int index = indexes.get(i);
            result[i][0] = spectrum.mz[index];
            result[i][1] = spectrum.intensity[index];

        }

        return result;

    }
    
    /**
     * Returns the start (inclusive) and end (exclusive) indexes of the peaks between mzMin (inclusive) and mzMax (exclusive).
     *
     * @param spectrum The spectrum to inspect.
     * @param mzMin The inclusive lower boundary to use.
     * @param mzMax The exclusive upper boundary to use.
     * 
     * @return The start and end indexes of the peaks between mzMin (inclusive) and mzMax (exclusive).
     */
    public static int[] getWindowIndexes(
            Spectrum spectrum,
            double mzMin,
            double mzMax
    ) {
        
        int windowStart = 0;
        
        for (int i = 0 ; i < spectrum.getNPeaks() ; i++) {
            
            double mz = spectrum.mz[i];
            
            if (mz >= mzMin && mz < mzMax) {
                
                if (windowStart == -1) {
                    
                    windowStart = i;
                    
                }
                
            } else if (mz >= mzMax) {
                
                return new int[]{windowStart, i};
                
            }
        }
        
        return new int[]{windowStart, spectrum.getNPeaks()};
        
    }
    
    /**
     * Returns the number of peaks between the start index (inclusive) and the end index (exclusive) above the intensity threshold (inclusive).
     * 
     * @param spectrum The spectrum.
     * @param startIndex The start index (inclusive).
     * @param endIndex The end index (exclusive).
     * @param intensityThreshold The intensity threshold (inclusive).
     * 
     * @return The number of peaks between the start index (inclusive) and the end index (exclusive) above the intensity threshold (inclusive).
     */
    public static int getNPeaksAboveThreshold(
            Spectrum spectrum,
            int startIndex,
            int endIndex,
            double intensityThreshold
    ) {
        
        return (int) IntStream.range(startIndex, endIndex)
                .mapToDouble(
                        i -> spectrum.intensity[i]
                )
                .filter(
                        intensity -> intensity >= intensityThreshold
                )
                .count();
    }
}
