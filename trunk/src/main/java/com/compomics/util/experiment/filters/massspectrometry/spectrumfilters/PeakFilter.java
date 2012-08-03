/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.filters.massspectrometry.spectrumfilters;

import com.compomics.util.experiment.filters.massspectrometry.SpectrumFilter;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author vaudel
 */
public class PeakFilter implements SpectrumFilter, Serializable {

    /**
     * Serial number for backward compatibility
     */
    static final long serialVersionUID = 1751883115257153259L;
    /**
     * The m/z to look for
     */
    private double mz;
    /**
     * The m/z to tolerance to use
     */
    private double mzTolerance;
    /**
     * A boolean indicating whether the m/z tolerance is in ppm
     */
    private boolean isPpm;
    /**
     * The intensity to look for
     */
    private double intensity;
    /**
     * The intensity tolerance to look for
     */
    private double intensityTolerance;

    /**
     * Constructor
     *
     * @param mz the m/z to look for
     * @param mzTolerance the m/z tolerance
     * @param isPpm a boolean indicating whether the m/z tolerance is in ppm
     * @param intensity the intensity to look for
     * @param intensityTolerance the intensity relative tolerance (0.1 for 10%)
     */
    public PeakFilter(double mz, double mzTolerance, boolean isPpm, double intensity, double intensityTolerance) {
        this.mz = mz;
        this.mzTolerance = mzTolerance;
        this.isPpm = isPpm;
        this.intensity = intensity;
        this.intensityTolerance = intensityTolerance;
    }

    /**
     * Indicates whether a peak was found in the spectrum at the desired m/z in
     * the given intensity quartile
     *
     * @param spectrum the spectrum to inspect
     * @return a boolean indicating whether a peak was found in the spectrum at
     * the desired m/z in the given intensity quartile
     */
    public boolean validateSpectrum(MSnSpectrum spectrum) {

        ArrayList<Double> intensitiesShortList = new ArrayList<Double>();
        ArrayList<Double> mzArray = new ArrayList<Double>(spectrum.getPeakMap().keySet());

        Collections.sort(mzArray);

        double deltaMz;

        if (isPpm) {
            deltaMz = (mzTolerance / 1000000) * mz;
        } else {
            deltaMz = mzTolerance;
        }

        if (!mzArray.isEmpty() && mz >= mzArray.get(0) - deltaMz
                && mz <= mzArray.get(mzArray.size() - 1) + deltaMz) {

            int indexMin = 0;
            int indexMax = mzArray.size() - 1;

            if (Math.abs(getError(mzArray.get(indexMax))) <= mzTolerance) {
                intensitiesShortList.add(spectrum.getPeakMap().get(mzArray.get(indexMax)).intensity);
            } else if (Math.abs(mzArray.get(indexMin)) <= mzTolerance) {
                intensitiesShortList.add(spectrum.getPeakMap().get(mzArray.get(indexMin)).intensity);
            }
            while (indexMax - indexMin > 1) {

                int index = (indexMax - indexMin) / 2 + indexMin;
                double currentMz = mzArray.get(index);

                if (Math.abs(getError(currentMz)) <= mzTolerance) {
                    intensitiesShortList.add(spectrum.getPeakMap().get(currentMz).intensity);
                }

                if (currentMz < mz) {
                    indexMin = index;
                } else {
                    indexMax = index;
                }
            }
        }
        for (double tempIntensity : intensitiesShortList) {
            if (Math.abs(tempIntensity - intensity) / intensity <= intensityTolerance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the absolute matching error in Da.
     *
     * @return the absolute matching error
     */
    public double getError(double otherMz) {
        if (isPpm) {
            return ((otherMz - mz)
                    / mz) * 1000000;
        } else {
            return otherMz - mz;
        }
    }
}
