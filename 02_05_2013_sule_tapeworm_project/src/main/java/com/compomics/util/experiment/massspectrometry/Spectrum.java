package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class models a spectrum.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public abstract class Spectrum extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 7152424141470431489L;
    /**
     * Spectrum title.
     */
    protected String spectrumTitle;
    /**
     * Spectrum file name.
     */
    protected String fileName;
    /**
     * The MS level.
     */
    protected int level;
    /**
     * Peak list.
     */
    protected HashMap<Double, Peak> peakList;
    
    protected ArrayList<Peak> peaks;
    /**
     * Scan number or range.
     */
    protected String scanNumber;
    /**
     * The timepoint when the spectrum was recorded (scan start time in mzML
     * files).
     */
    protected double scanStartTime;
    /**
     * The splitter in the key between spectrumFile and spectrumTitle.
     */
    public static final String SPECTRUM_KEY_SPLITTER = "_cus_";

    /**
     * Convenience method returning the key for a spectrum.
     *
     * @param spectrumFile The spectrum file
     * @param spectrumTitle The spectrum title
     * @return the corresponding spectrum key
     */
    public static String getSpectrumKey(String spectrumFile, String spectrumTitle) {
        return spectrumFile + SPECTRUM_KEY_SPLITTER + spectrumTitle;
    }

    /**
     * Convenience method to retrieve the name of a file from the spectrum key.
     *
     * @param spectrumKey the spectrum key
     * @return the name of the file containing the spectrum
     */
    public static String getSpectrumFile(String spectrumKey) {
        return spectrumKey.split(SPECTRUM_KEY_SPLITTER)[0];
    }

    /**
     * Convenience method to retrieve the name of a spectrum from the spectrum
     * key.
     *
     * @param spectrumKey the spectrum key
     * @return the title of the spectrum
     */
    public static String getSpectrumTitle(String spectrumKey) {
        return spectrumKey.substring(spectrumKey.indexOf(SPECTRUM_KEY_SPLITTER) + 5).trim();
    }

    /**
     * Returns the key of the spectrum.
     *
     * @return the key of the spectrum
     */
    public String getSpectrumKey() {
        return fileName + SPECTRUM_KEY_SPLITTER + spectrumTitle;
    }

    /**
     * Returns the spectrum title.
     *
     * @return spectrum title
     */
    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    /**
     * Format the peaks so they can be plot in JFreeChart.
     *
     * @return a table containing the peaks
     */
    public double[][] getJFreePeakList() {
        double[] mz = new double[peakList.size()];
        double[] intensity = new double[peakList.size()];
        int cpt = 0;
        for (Peak currentPeak : peakList.values()) {
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
     * Returns a peak map where peaks are indexed by their m/z.
     *
     * @return a peak map
     */
    public HashMap<Double, Peak> getPeakMap() {
        return peakList;
    }

    /**
     * Adds a peak to the spectrum peak list.
     *
     * @param aPeak the peak to add
     */
    public void addPeak(Peak aPeak) {
        if(peakList==null){
            peakList = new HashMap<Double, Peak>();
            peakList.put(aPeak.mz, aPeak);
        }
        this.peakList.put(aPeak.mz, aPeak);
    }

    /**
     * Getter for the scan number.
     *
     * @return the spectrum scan number
     */
    public String getScanNumber() {
        return scanNumber;
    }

    /**
     * Setter for the scan number or range.
     *
     * @param scanNumber or range
     */
    public void setScanNumber(String scanNumber) {
        this.scanNumber = scanNumber;
    }

    /**
     * Returns the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns at which level the spectrum was recorded.
     *
     * @return at which level the spectrum was recorded
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the peak list.
     *
     * @return the peak list
     */
    public Collection<Peak> getPeakList() {
        return peakList.values();
    }

    public void setPeaks(ArrayList<Peak> peaks) {
       // this.peaks.clear();
        this.peakList.clear();
        
        for (Peak p : peaks) {
            double mz = p.mz;
           // this.peaks.add(p);
            peakList.put(mz, p);
        }
    }


    /**
     * Sets the peak list.
     *
     * @param peakList HashSet of peaks containing the peaks of the spectrum
     */
    public void setPeakList(HashMap<Double, Peak> peakList) {
        this.peakList = peakList;
    }

    /**
     * Returns the scan start time.
     *
     * @return the scan start time
     */
    public double getScanStartTime() {
        return scanStartTime;
    }

    /**
     * Sets the scan start time.
     *
     * @param scanStartTime the timepoint when the spectrum was recorded
     */
    public void setScanStartTime(double scanStartTime) {
        this.scanStartTime = scanStartTime;
    }

    public void setSpectrumTitle(String spectrumTitle) {
        this.spectrumTitle = spectrumTitle;
    }
    
    

    /**
     * This method will remove the peak list in order to reduce memory
     * consumption of the model.
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

        for (double currentMz : peakList.keySet()) {
            mz[counter] = currentMz;
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

        for (Peak currentPeak : peakList.values()) {
            intensity[counter] = currentPeak.intensity;
            counter++;
        }

        return intensity;
    }

    /**
     * Returns the m/z and intensity values as an array in increasing order sorted
     * on m/z value.
     *
     * @return the m/z and intensity values as an array
     */
    public double[][] getMzAndIntensityAsArray() {

        double[][] values = new double[2][peakList.size()];
        ArrayList<Double> mz = new ArrayList<Double>(peakList.keySet());
        Collections.sort(mz);

        for (int i = 0; i < mz.size(); i++) {
            Peak currentPeak = peakList.get(mz.get(i));
            values[0][i] = currentPeak.mz;
            values[1][i] = currentPeak.intensity;
        }

        return values;
    }

    /**
     * Returns the total intensity of the spectrum.
     *
     * @return the total intensity
     */
    public double getTotalIntensity() {

        double tempIntensity = 0;

        for (Peak currentPeak : peakList.values()) {
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

        for (Peak currentPeak : peakList.values()) {
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

        for (double currentmz : peakList.keySet()) {
            if (currentmz > maxMz) {
                maxMz = currentmz;
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

        for (double currentmz : peakList.keySet()) {
            if (currentmz < minMz) {
                minMz = currentmz;
            }
        }

        return minMz;
    }

    /**
     * Returns an array containing the intensity of all peak above the provided
     * threshold.
     *
     * @param threshold the lower threshold
     * @return an array containing the intensity of all peak above the provided
     * threshold
     */
    public ArrayList<Double> getPeaksAboveIntensityThreshold(double threshold) {

        ArrayList<Double> peakIntensities = new ArrayList<Double>();

        for (Peak currentPeak : peakList.values()) {
            if (currentPeak.intensity > threshold) {
                peakIntensities.add(currentPeak.intensity);
            }
        }

        return peakIntensities;
    }

    /**
     * Returns the intensity limit.
     *
     * @param intensityLimit the intensity limit in percent, e.g., 0.75
     * @return the intensity limit
     */
    public double getIntensityLimit(double intensityLimit) {

        ArrayList<Double> intensities = new ArrayList<Double>();

        for (Peak peak : peakList.values()) {
            intensities.add(peak.intensity);
        }

        if (intensities.isEmpty()) {
            return 0;
        }

        Collections.sort(intensities);
        int index = (int) ((intensities.size() - 1) * intensityLimit);
        return intensities.get(index);
    }

    /**
     * Returns a recalibrated peak list.
     *
     * @param mzCorrections the m/z corrections to apply
     * @return the recalibrated list of peaks indexed by m/z
     * @throws IllegalArgumentException
     */
    public HashMap<Double, Peak> getRecalibratedPeakList(HashMap<Double, Double> mzCorrections) throws IllegalArgumentException {

        HashMap<Double, Peak> result = new HashMap<Double, Peak>();
        ArrayList<Double> keys = new ArrayList<Double>(mzCorrections.keySet());
        Collections.sort(keys);

        for (Peak peak : peakList.values()) {
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
