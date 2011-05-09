package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class models a spectrum
 *
 * @author Marc Vaudel
 */
public abstract class Spectrum extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 7152424141470431489L;
    /**
     * spectrum title
     */
    protected String spectrumTitle;
    /**
     * spectrum file name
     */
    protected String fileName;
    /**
     * The MS level
     */
    protected int level;
    /**
     * peak list
     */
    protected HashSet<Peak> peakList;
    /**
     * scan number or range
     */
    protected String scanNumber;
    /**
     * The timepoint when the spectrum was recorded (scan start time in mzML files)
     */
    protected double scanStartTime;

    /**
     * Convenience method returning the key for a spectrum
     * @param spectrumFile  The spectrum file
     * @param spectrumTitle The spectrum title
     * @return  the corresponding spectrum key
     */
    public static String getSpectrumKey(String spectrumFile, String spectrumTitle) {
        return spectrumFile + "_" + spectrumTitle;
    }

    /**
     * Convenience method to retrieve the name of a file from the spectrum key
     * @param spectrumKey   the spectrum key
     * @return  the name of the file containing the spectrum
     */
    public static String getSpectrumFile(String spectrumKey) {
        return spectrumKey.split("_")[0];
    }

    /**
     * Convenience method to retrieve the name of a spectrum from the spectrum key
     * @param spectrumKey   the spectrum key
     * @return  the title of the spectrum
     */
    public static String getSpectrumTitle(String spectrumKey) {
        return spectrumKey.substring(spectrumKey.indexOf("_") + 1);
    }

    /**
     * Returns the key of the spectrum
     * @return the key of the spectrum
     */
    public String getSpectrumKey() {
        return fileName + "_" + spectrumTitle;
    }

    /**
     * returns the spectrum title
     *
     * @return spectrum title
     */
    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    /**
     * format the peaks so they can be plot in JFreeChart
     *
     * @return a table containing the peaks
     */
    public double[][] getJFreePeakList() {
        double[] mz = new double[peakList.size()];
        double[] intensity = new double[peakList.size()];
        int cpt = 0;
        for (Peak currentPeak : peakList) {
            mz[cpt] = currentPeak.mz;
            intensity[cpt] = currentPeak.intensity;
            cpt++;
        }

        double[][] coordinates = new double[6][mz.length];
        coordinates[0] = mz;
        coordinates[1] = mz;
        coordinates[2] = mz;
        coordinates[3] = intensity;
        coordinates[4] = intensity;
        coordinates[5] = intensity;

        return coordinates;
    }

    /**
     * Returns a peak map.
     *
     * @return a peak map
     */
    public HashMap<Double, Peak> getPeakMap() {
        HashMap<Double, Peak> result = new HashMap<Double, Peak>();
        for (Peak peak : peakList) {
            result.put(peak.mz, peak);
        }
        return result;
    }

    /**
     * Getter for the scan number
     * @return the spectrum scan number
     */
    public String getScanNumber() {
        return scanNumber;
    }

    /**
     * Setter for the scan number or range
     * @param scanNumber or range
     */
    public void setScanNumber(String scanNumber) {
        this.scanNumber = scanNumber;
    }

    /**
     * Returns the file name
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns at which level the spectrum was recorded
     * @return at which level the spectrum was recorded
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the peak list
     * @return the peak list
     */
    public HashSet<Peak> getPeakList() {
        return peakList;
    }

    /**
     * Returns the scan start time
     * @return the scan start time
     */
    public double getScanStartTime() {
        return scanStartTime;
    }

    /**
     * Sets the scan start time
     * @param scanStartTime the timepoint when the spectrum was recorded
     */
    public void setScanStartTime(double scanStartTime) {
        this.scanStartTime = scanStartTime;
    }

    /**
     * This method will remove the peak list in order to reduce memory consumption of the model
     */
    public void removePeakList() {
        if (peakList != null) {
            peakList.clear();
        }
    }

    /**
     * Returns the mz values as an array.
     *
     * @return the mz values as an array
     */
    public double[] getMzValuesAsArray() {

        double[] mz = new double[peakList.size()];

        int counter = 0;

        for (Peak currentPeak : peakList) {
            mz[counter] = currentPeak.mz;
            counter++;
        }

        return mz;
    }

    /**
     * Returns the intensity values as an array.
     *
     * @return the intensity values as an array
     */
    public double[] getIntensityValuesAsArray() {

        double[] intensity = new double[peakList.size()];

        int counter = 0;

        for (Peak currentPeak : peakList) {
            intensity[counter] = currentPeak.intensity;
            counter++;
        }

        return intensity;
    }

    /**
     * Returns the total intensity of the spectrum.
     *
     * @return the total intensity
     */
    public double getTotalIntensity() {

        double tempIntensity = 0;

        for (Peak currentPeak : peakList) {
            tempIntensity += currentPeak.intensity;
        }

        return tempIntensity;
    }

    /**
     * Returns the max intensity value.
     *
     * @return the max intensity value
     */
    public double getMaxIntensity() {

        double maxIntensity = Double.MIN_VALUE;

        for (Peak currentPeak : peakList) {
            if (currentPeak.intensity > maxIntensity) {
                maxIntensity = currentPeak.intensity;
            }
        }

        return maxIntensity;
    }

    /**
     * Returns the max mz value.
     *
     * @return the max mz value
     */
    public double getMaxMz() {

        double maxMz = Double.MIN_VALUE;

        for (Peak currentPeak : peakList) {
            if (currentPeak.mz > maxMz) {
                maxMz = currentPeak.mz;
            }
        }

        return maxMz;
    }

    /**
     * Returns the min mz value.
     *
     * @return the min mz value
     */
    public double getMinMz() {

        double minMz = Double.MAX_VALUE;

        for (Peak currentPeak : peakList) {
            if (currentPeak.mz < minMz) {
                minMz = currentPeak.mz;
            }
        }

        return minMz;
    }

    /**
     * Returns an array containing the intensity of all peak above the
     * provided threshold.
     *
     * @param threshold     the lower threshold
     * @return              an array containing the intensity of all peak above the
     *                      provided threshold
     */
    public ArrayList<Double> getPeaksAboveIntensityThreshold(double threshold) {

        ArrayList<Double> peakIntensities = new ArrayList<Double>();

        for (Peak currentPeak : peakList) {
            if (currentPeak.intensity > threshold) {
                peakIntensities.add(currentPeak.intensity);
            }
        }

        return peakIntensities;
    }

    /**
     * Returns the intensity limit. Returns 0 if annotateMostIntensePeaks is set
     * to false,
     *
     * @param annotateMostIntensePeaks  returns 0 if set to false
     * @return                          the intensity limit
     */
    public double getIntensityLimit(boolean annotateMostIntensePeaks) {
        if (annotateMostIntensePeaks) {
            ArrayList<Double> intensities = new ArrayList<Double>();
            for (Peak peak : peakList) {
                intensities.add(peak.intensity);
            }
            Collections.sort(intensities);
            int index = 3 * (intensities.size() - 1) / 4;
            return intensities.get(index);
        }
        return 0;
    }
}
