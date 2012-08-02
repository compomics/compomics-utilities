/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.filters.massspectrometry.spectrumfilters;

import com.compomics.util.experiment.filters.massspectrometry.SpectrumFilter;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class checks whether a peak is present in a spectrum among the most
 * intense peaks
 *
 * @author Marc
 */
public class MzFilter implements SpectrumFilter {

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
     * The intensity quantile to look into
     */
    private double intensityQuantile;
    /**
     * Filter with similar properties
     */
    private MzFilter brotherFilter = null;
    /**
     * array of m/z to be inspected
     */
    private ArrayList<Double> mzArray;
    /**
     * The key of the inspected spectrum
     */
    private String spectrumKey = "";

    /**
     * Constructor
     *
     * @param mz the m/z to look for
     * @param mzTolerance the m/z tolerance
     * @param isPpm a boolean indicating whether the m/z tolerance is in ppm
     * @param intensityQuantile the intensity quantile to look into
     */
    public MzFilter(double mz, double mzTolerance, boolean isPpm, double intensityQuantile) {
        this.mz = mz;
        this.mzTolerance = mzTolerance;
        this.isPpm = isPpm;
        this.intensityQuantile = intensityQuantile;
    }

    /**
     * Creates a filter from a similar filter
     *
     * @param brotherFilter another filter
     * @param mz the other m/z to look for
     */
    public MzFilter(MzFilter brotherFilter, double mz) {
        this.mz = mz;
        this.brotherFilter = brotherFilter;
        this.mzTolerance = brotherFilter.getMzTolerance();
        this.isPpm = brotherFilter.isPpm;
        this.intensityQuantile = brotherFilter.getIntensityQuantile();
    }

    /**
     * Returns the m/z tolerance to search with
     *
     * @return the m/z tolerance
     */
    public double getMzTolerance() {
        return mzTolerance;
    }

    /**
     * Returns a boolean indicating whether the m/z tolerance is in ppm
     *
     * @return a boolean indicating whether the m/z tolerance is in ppm
     */
    public boolean isPpm() {
        return isPpm;
    }

    /**
     * Returns the intensity quantile to look into
     *
     * @return the intensity quantile
     */
    public double getIntensityQuantile() {
        return intensityQuantile;
    }

    /**
     * Returns the key of the spectrum loaded
     *
     * @return the key of the spectrum loaded
     */
    public String getSpectrumLoaded() {
        return spectrumKey;
    }

    /**
     * Returns the mzArray
     *
     * @return the mzArray
     */
    public ArrayList<Double> getMzArray() {
        return mzArray;
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

        setSpectrum(spectrum);

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
                return true;
            } else if (Math.abs(mzArray.get(indexMin)) <= mzTolerance) {
                return true;
            }
            while (indexMax - indexMin > 1) {

                int index = (indexMax - indexMin) / 2 + indexMin;
                double currentMz = mzArray.get(index);

                if (Math.abs(getError(currentMz)) <= mzTolerance) {
                    return true;
                }

                if (currentMz < mz) {
                    indexMin = index;
                } else {
                    indexMax = index;
                }
            }
        }
        return false;
    }

    /**
     * Sets a new spectrum to look into.
     *
     * @param spectrum The spectrum to inspect
     * @param intensityLimit the minimal intensity to account for
     */
    private void setSpectrum(MSnSpectrum spectrum) {
        if (!spectrumKey.equals(spectrum.getSpectrumKey())) {
            spectrumKey = spectrum.getSpectrumKey();

            if (brotherFilter != null && brotherFilter.getSpectrumLoaded().equals(spectrum.getSpectrumKey())) {
                this.mzArray = brotherFilter.getMzArray();
            } else if (intensityQuantile == 0) {
                HashMap<Double, Peak> peakMap = spectrum.getPeakMap();
                mzArray = new ArrayList<Double>(peakMap.keySet());
                Collections.sort(mzArray);
            } else {
                mzArray = new ArrayList<Double>();
                HashMap<Double, ArrayList<Double>> intensitiesMap = new HashMap<Double, ArrayList<Double>>();
                ArrayList<Double> intensities = new ArrayList<Double>();
                for (Peak peak : spectrum.getPeakList()) {
                    intensities.add(peak.intensity);
                    if (!intensitiesMap.containsKey(peak.intensity)) {
                        intensitiesMap.put(peak.intensity, new ArrayList<Double>());
                    }
                    intensitiesMap.get(peak.intensity).add(peak.mz);
                }
                Collections.sort(intensities);
                int index = (int) intensityQuantile * intensities.size();
                double threshold = intensities.get(index);
                for (double intensity : intensitiesMap.keySet()) {
                    if (intensity >= threshold) {
                        mzArray.addAll(intensitiesMap.get(intensity));
                    }
                }
                Collections.sort(mzArray);
            }
        }
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
