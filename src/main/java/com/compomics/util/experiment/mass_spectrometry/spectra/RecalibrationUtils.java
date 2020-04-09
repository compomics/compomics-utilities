package com.compomics.util.experiment.mass_spectrometry.spectra;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Utility functions for spectrum recalibration.
 *
 * @author Marc Vaudel
 */
public class RecalibrationUtils {

    /**
     * Returns a recalibrated precursor.
     *
     * @param precursor The precursor to recalibrate.
     * @param mzCorrection The m/z correction to apply.
     * @param rtCorrection The retention time correction to apply.
     *
     * @return A new precursor with recalibrated mz and rt.
     */
    public static Precursor getRecalibratedPrecursor(
            Precursor precursor,
            double mzCorrection,
            double rtCorrection
    ) {

        return new Precursor(
                precursor.rt - rtCorrection,
                precursor.mz - mzCorrection,
                precursor.intensity,
                precursor.possibleCharges
        );
    }

    /**
     * Returns a recalibrated peak list.
     *
     * @param mzCorrections The corrections to apply.
     * @param originalMz The original m/z array.
     *
     * @return A new array of recalibrated m/z.
     */
    public static double[] getRecalibratedMz(
            TreeMap<Double, Double> mzCorrections,
            double[] originalMz
    ) {

        double[] result = new double[originalMz.length];

        for (int i = 0; i < originalMz.length; i++) {

            double fragmentMz = originalMz[i];
            double correction = getCorrection(fragmentMz, mzCorrections);
            
            result[i] = fragmentMz - correction;
        }

        return result;
    }

    /**
     * Returns the correction to use for the given fragment m/z.
     * 
     * @param fragmentMz the fragment m/z.
     * @param mzCorrections The binned m/z corrections.
     * 
     * @return The correction to use for the given fragment m/z.
     */
    private static double getCorrection(double fragmentMz, TreeMap<Double, Double> mzCorrections) {

        Entry<Double, Double> entry = mzCorrections.firstEntry();

        if (fragmentMz <= entry.getKey() || mzCorrections.size() == 1) {

            return entry.getValue();

        } else {

            entry = mzCorrections.lastEntry();

            if (fragmentMz >= entry.getKey()) {

                return entry.getValue();

            } else {

                ArrayList<Entry<Double, Double>> entryList = new ArrayList<>(mzCorrections.entrySet());

                for (int i = 0; i < entryList.size() - 1; i++) {

                    Entry<Double, Double> entry1 = entryList.get(i);

                    if (entry1.getKey() == fragmentMz) {

                        return entry1.getKey();
                    }

                    Entry<Double, Double> entry2 = entryList.get(i + 1);

                    if (entry1.getKey() < fragmentMz && fragmentMz < entry2.getKey()) {

                        return entry1.getValue() + ((fragmentMz - entry1.getKey()) * (entry2.getValue() - entry1.getValue()) / (entry2.getKey() - entry1.getKey()));

                    }
                }
            }
        }
        
        throw new IllegalArgumentException("Could not find correction for fragment m/z.");

    }
}
