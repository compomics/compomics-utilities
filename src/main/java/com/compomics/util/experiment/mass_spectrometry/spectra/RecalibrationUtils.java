package com.compomics.util.experiment.mass_spectrometry.spectra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
     * @param mzCorrection the m/z correction to apply
     * @param rtCorrection the retention time correction to apply
     * 
     * @return a new recalibrated precursor
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
     * @param mzCorrections the m/z corrections to apply
     * @param peakMap the original peak map
     *
     * @return the recalibrated list of peaks indexed by m/z
     */
    public static HashMap<Double, Peak> getRecalibratedPeakList(
            HashMap<Double, Double> mzCorrections,
    HashMap<Double, Peak> peakMap) {

        HashMap<Double, Peak> result = new HashMap<>(peakMap.size());
        ArrayList<Double> keys = new ArrayList<>(mzCorrections.keySet());
        Collections.sort(keys);

        for (Peak peak : peakMap.values()) {

            double fragmentMz = peak.mz;
            double key1 = keys.get(0);
            double correction = 0.0;

            if (fragmentMz <= key1) {
                correction = mzCorrections.get(key1);
            } else {

                key1 = keys.get(keys.size() - 1);

                if (fragmentMz >= key1) {
                    correction = mzCorrections.get(key1);
                } else {

                    for (int i = 0; i < keys.size() - 1; i++) {

                        key1 = keys.get(i);

                        if (key1 == fragmentMz) {
                            correction = mzCorrections.get(key1);
                            break;
                        }

                        double key2 = keys.get(i + 1);

                        if (key1 < fragmentMz && fragmentMz < key2) {
                            double y1 = mzCorrections.get(key1);
                            double y2 = mzCorrections.get(key2);
                            correction = y1 + ((fragmentMz - key1) * (y2 - y1) / (key2 - key1));
                            break;
                        }
                    }
                }
            }

            result.put(peak.mz - correction, new Peak(peak.mz - correction, peak.intensity));
        }

        return result;
    }
}
