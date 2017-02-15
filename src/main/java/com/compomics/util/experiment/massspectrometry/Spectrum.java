package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.math.BasicMathFunctions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import org.apache.commons.math.MathException;

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
     * mz indexed Peak list.
     */
    protected HashMap<Double, Peak> peakList;
    /**
     * Intensity indexed Peak map.
     */
    protected HashMap<Double, ArrayList<Peak>> intensityPeakMap = null;
    /**
     * Scan number or range.
     */
    protected String scanNumber;
    /**
     * The time point when the spectrum was recorded (scan start time in mzML
     * files).
     */
    protected double scanStartTime;
    /**
     * The splitter in the key between spectrumFile and spectrumTitle.
     */
    public static final String SPECTRUM_KEY_SPLITTER = "_cus_";
    /**
     * The peak list as an array directly plottable by JFreeChart.
     */
    private double[][] jFreePeakList = null;
    /**
     * The mz values as array. Null until set by the getter.
     */
    private double[] mzValuesAsArray = null;
    /**
     * The mz values sorted in acceding order as array. Null until set by the
     * getter.
     */
    private double[] mzValuesOrderedAsArray = null;
    /**
     * The peak list as an array list formatted as text, e.g. [[303.17334
     * 3181.14],[318.14542 37971.93], ... ].
     */
    private String peakListAsString = null;
    /**
     * The intensity values as array. Null until set by the getter.
     */
    private double[] intensityValuesAsArray = null;
    /**
     * The intensity values as array normalized against the most intense peak.
     * Null until set by the getter.
     */
    private double[] intensityValuesNormaizedAsArray = null; // @TODO: correct typo
    /**
     * The mz and intensity values as array. Null until set by the getter.
     */
    private double[][] mzAndIntensityAsArray = null;
    /**
     * The total intensity.
     */
    private Double totalIntensity;
    /**
     * The maximal intensity.
     */
    private Double maxIntensity;
    /**
     * The maximal m/z.
     */
    private Double maxMz;
    /**
     * The minimal m/z.
     */
    private Double minMz;
    /**
     * Mutex for the setting of the attributes in cache.
     */
    private Semaphore mutex = new Semaphore(1);
    /**
     * Cache for the intensity limit.
     */
    private Double intensityLimit = null;
    /**
     * Intensity level corresponding to the value in cache.
     */
    private double intensityLimitLevel = -1.0;
    /**
     * The binned cumulative function of the distribution of the log of the
     * peaks intensities.
     */
    private SimpleNoiseDistribution binnedCumulativeFunction = null;

    /**
     * Convenience method returning the key for a spectrum.
     *
     * @param spectrumFile the spectrum file
     * @param spectrumTitle the spectrum title
     *
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
        return spectrumKey.substring(0, spectrumKey.indexOf(SPECTRUM_KEY_SPLITTER));
    }

    /**
     * Convenience method to retrieve the name of a spectrum from the spectrum
     * key.
     *
     * @param spectrumKey the spectrum key
     * @return the title of the spectrum
     */
    public static String getSpectrumTitle(String spectrumKey) {
        return spectrumKey.substring(spectrumKey.indexOf(SPECTRUM_KEY_SPLITTER) + SPECTRUM_KEY_SPLITTER.length());
    }

    /**
     * Set the spectrum title.
     *
     * @param spectrumTitle the title to set
     */
    public void setSpectrumTitle(String spectrumTitle) {
        this.spectrumTitle = spectrumTitle;
    }

    /**
     * Returns the key of the spectrum.
     *
     * @return the key of the spectrum
     */
    public String getSpectrumKey() {
        StringBuilder stringBuilder = new StringBuilder(fileName.length() + SPECTRUM_KEY_SPLITTER.length() + spectrumTitle.length());
        stringBuilder.append(fileName);
        stringBuilder.append(SPECTRUM_KEY_SPLITTER);
        stringBuilder.append(spectrumTitle);
        return stringBuilder.toString();
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
     * Format the peaks so that they can be plotted in JFreeChart.
     *
     * @return a table containing the peaks
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double[][] getJFreePeakList() throws InterruptedException {
        if (jFreePeakList == null) {
            mutex.acquire();
            if (jFreePeakList == null) {
                double[] mz = new double[peakList.size()];
                double[] intensity = new double[peakList.size()];
                int cpt = 0;
                for (Peak currentPeak : peakList.values()) {
                    mz[cpt] = currentPeak.mz;
                    intensity[cpt] = currentPeak.intensity;
                    cpt++;
                }

                jFreePeakList = new double[6][mz.length];
                jFreePeakList[0] = mz;
                jFreePeakList[1] = mz;
                jFreePeakList[2] = mz;
                jFreePeakList[3] = intensity;
                jFreePeakList[4] = intensity;
                jFreePeakList[5] = intensity;
            }
            mutex.release();
        }
        return jFreePeakList;
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
    public synchronized void addPeak(Peak aPeak) {
        if (peakList == null) {
            peakList = new HashMap<Double, Peak>();
        }
        this.peakList.put(aPeak.mz, aPeak);
        resetSavedData();
    }

    /**
     * Set the peaks.
     *
     * @param peaks the peaks to set
     */
    public synchronized void setPeaks(ArrayList<Peak> peaks) {

        if (peakList != null) {
            this.peakList.clear();
        } else {
            peakList = new HashMap<Double, Peak>();
        }

        for (Peak p : peaks) {
            double mz = p.mz;
            peakList.put(mz, p);
        }

        resetSavedData();
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
    public synchronized void setScanNumber(String scanNumber) {
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

    /**
     * Sets the peak list.
     *
     * @param peakList HashSet of peaks containing the peaks of the spectrum
     */
    public synchronized void setPeakList(HashMap<Double, Peak> peakList) {
        this.peakList = peakList;
        resetSavedData();
    }

    /**
     * Returns the peak list as an array list formatted as text, e.g.
     * [[303.17334 3181.14],[318.14542 37971.93], ... ].
     *
     * @return the peak list as an array list formatted as text
     *
     * @throws java.lang.InterruptedException thrown if the thread is
     * interrupted
     */
    public String getPeakListAsString() throws InterruptedException {

        if (peakListAsString == null) {

            double[] mzValues = getOrderedMzValues();
            mutex.acquire();

            StringBuilder sb = new StringBuilder();
            sb.append("[");

            for (double mzValue : mzValues) {

                if (sb.length() > 1) {
                    sb.append(",");
                }

                Peak currentPeak = peakList.get(mzValue);

                sb.append("[");
                sb.append(currentPeak.mz);
                sb.append(",");
                sb.append(currentPeak.intensity);
                sb.append("]");
            }

            sb.append("]");

            peakListAsString = sb.toString();
            mutex.release();
        }

        return peakListAsString;
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
     * @param scanStartTime the time point when the spectrum was recorded
     */
    public synchronized void setScanStartTime(double scanStartTime) {
        this.scanStartTime = scanStartTime;
    }

    /**
     * This method will remove the peak list in order to reduce memory
     * consumption of the model.
     */
    public synchronized void removePeakList() {
        if (peakList != null) {
            peakList.clear();
        }
    }

    /**
     * Returns the mz values as an array. Note: the array is not necessarily
     * ordered.
     *
     * @return the mz values as an array
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double[] getMzValuesAsArray() throws InterruptedException {

        if (mzValuesAsArray == null) {
            mutex.acquire();
            if (mzValuesAsArray == null) {
                mzValuesAsArray = new double[peakList.size()];
                int counter = 0;
                for (double currentMz : peakList.keySet()) {
                    mzValuesAsArray[counter++] = currentMz;
                }
            }
            mutex.release();
        }

        return mzValuesAsArray;
    }

    /**
     * Returns a list of the m/z values sorted in ascending order.
     *
     * @return a list of the m/z values sorted in ascending order
     *
     * @throws java.lang.InterruptedException thrown if the thread is
     * interrupted
     */
    public double[] getOrderedMzValues() throws InterruptedException {
        if (mzValuesOrderedAsArray == null) {
            getMzValuesAsArray();
            if (mzValuesOrderedAsArray == null) {
                mutex.acquire();
                mzValuesOrderedAsArray = mzValuesAsArray.clone();
                Arrays.sort(mzValuesOrderedAsArray);
                mutex.release();
            }
        }
        return mzValuesOrderedAsArray;
    }

    /**
     * Setter for the intensityValuesAsArray.
     *
     * @param intensityValuesAsArray the intensity values array
     */
    public synchronized void setIntensityValuesAsArray(double[] intensityValuesAsArray) {
        this.intensityValuesAsArray = intensityValuesAsArray;
        removePeakList();
    }

    /**
     * Returns the intensity values as an array.
     *
     * @return the intensity values as an array
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double[] getIntensityValuesAsArray() throws InterruptedException {

        if (intensityValuesAsArray == null || (intensityValuesAsArray.length != peakList.size())) {
            mutex.acquire();
            if (intensityValuesAsArray == null || (intensityValuesAsArray.length != peakList.size())) {
                intensityValuesAsArray = new double[peakList.size()];
                int counter = 0;
                for (Peak currentPeak : peakList.values()) {
                    intensityValuesAsArray[counter++] = currentPeak.intensity;
                }
            }
            mutex.release();
        }

        return intensityValuesAsArray;
    }

    /**
     * Returns the intensity values as an array normalized against the largest
     * peak.
     *
     * @return the normalized intensity values as an array
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double[] getIntensityValuesNormalizedAsArray() throws InterruptedException {

        if (intensityValuesNormaizedAsArray == null) {

            mutex.acquire();

            if (intensityValuesNormaizedAsArray == null) {

                intensityValuesNormaizedAsArray = new double[peakList.size()];

                double highestIntensity = 0.0;
                int counter = 0;

                for (Peak currentPeak : peakList.values()) {
                    intensityValuesNormaizedAsArray[counter++] = currentPeak.intensity;
                    if (currentPeak.intensity > highestIntensity) {
                        highestIntensity = currentPeak.intensity;
                    }
                }

                if (highestIntensity > 0) {
                    for (int i = 0; i < intensityValuesNormaizedAsArray.length; i++) {
                        intensityValuesNormaizedAsArray[i] = intensityValuesNormaizedAsArray[i] / highestIntensity * 100;
                    }
                }
            }

            mutex.release();
        }

        return intensityValuesNormaizedAsArray;
    }

    /**
     * Returns the m/z and intensity values as an array in increasing order
     * sorted on m/z value.
     *
     * @return the m/z and intensity values as an array
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double[][] getMzAndIntensityAsArray() throws InterruptedException {

        if (mzAndIntensityAsArray == null) {

            double[] orderedMzValues = getOrderedMzValues();
            mutex.acquire();

            if (mzAndIntensityAsArray == null) {

                mzAndIntensityAsArray = new double[2][peakList.size()];
                int counter = 0;

                for (double mz : orderedMzValues) {
                    Peak currentPeak = peakList.get(mz);
                    mzAndIntensityAsArray[0][counter] = currentPeak.mz;
                    mzAndIntensityAsArray[1][counter] = currentPeak.intensity;
                    counter++;
                }
            }

            mutex.release();
        }

        return mzAndIntensityAsArray;
    }

    /**
     * Returns the total intensity of the spectrum.
     *
     * @return the total intensity. 0 if no peak.
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double getTotalIntensity() throws InterruptedException {

        if (totalIntensity == null) {

            mutex.acquire();

            if (totalIntensity == null) {

                totalIntensity = 0.0;

                for (Peak currentPeak : peakList.values()) {
                    totalIntensity += currentPeak.intensity;
                }
            }

            mutex.release();
        }

        return totalIntensity;
    }

    /**
     * Returns the max intensity value.
     *
     * @return the max intensity value. 0 if no peak.
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double getMaxIntensity() throws InterruptedException {

        if (maxIntensity == null) {

            mutex.acquire();

            if (maxIntensity == null) {

                maxIntensity = 0.0;

                for (Peak currentPeak : peakList.values()) {
                    if (currentPeak.intensity > maxIntensity) {
                        maxIntensity = currentPeak.intensity;
                    }
                }
            }

            mutex.release();
        }

        return maxIntensity;
    }

    /**
     * Returns the max mz value.
     *
     * @return the max mz value
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double getMaxMz() throws InterruptedException {

        if (maxMz == null) {

            mutex.acquire();
            if (maxMz == null) {

                if (peakList.keySet().isEmpty()) {
                    maxMz = 0.0;
                } else {
                    maxMz = Collections.max(peakList.keySet());
                }
            }

            mutex.release();
        }

        return maxMz;
    }

    /**
     * Returns the min mz value.
     *
     * @return the min mz value
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public double getMinMz() throws InterruptedException {

        if (minMz == null) {

            mutex.acquire();

            if (minMz == null) {
                if (peakList.keySet().isEmpty()) {
                    minMz = 0.0;
                } else {
                    minMz = Collections.min(peakList.keySet());
                }
            }

            mutex.release();
        }

        return minMz;
    }

    /**
     * Returns an array containing the intensity of all peaks strictly above the
     * provided threshold.
     *
     * @param threshold the lower threshold
     *
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
     * Returns the intensity limit in intensity from a given percentile.
     *
     * @param intensityFraction the fraction of the intensity to use as limit,
     * e.g., 0.75 for the 75% most intense peaks.
     *
     * @return the intensity limit
     */
    public double getIntensityLimit(double intensityFraction) {

        if (intensityLimit == null || intensityLimitLevel != intensityFraction) {
            intensityLimit = estimateIntneistyLimit(intensityFraction);
            intensityLimitLevel = intensityFraction;
        }
        return intensityLimit;
    }

    /**
     * Estimates the intensity limit in intensity from a given percentile.
     *
     * @param intensityFraction the fraction of the intensity to use as limit,
     * e.g., 0.75 for the 75% most intense peaks.
     *
     * @return the intensity limit
     */
    private double estimateIntneistyLimit(double intensityFraction) {
        ArrayList<Double> intensities = new ArrayList<Double>(peakList.size());

        for (Peak peak : peakList.values()) {
            double mz = peak.mz;
            // Skip the low mass region of the spectrum @TODO: skip precursor as well
            if (mz > 200) {
                intensities.add(peak.intensity);
            }
        }

        if (intensities.isEmpty()) {
            return 0;
        }

        return BasicMathFunctions.percentile(intensities, intensityFraction);
    }

    /**
     * Returns a recalibrated peak list.
     *
     * @param mzCorrections the m/z corrections to apply
     *
     * @return the recalibrated list of peaks indexed by m/z
     */
    public HashMap<Double, Peak> getRecalibratedPeakList(HashMap<Double, Double> mzCorrections) {

        HashMap<Double, Peak> result = new HashMap<Double, Peak>(peakList.size());
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

    /**
     * Returns the peak list of this spectrum without matched peaks.
     *
     * @param matches the ion matches
     *
     * @return a peak list which does not contain the peak matched
     */
    public HashMap<Double, Peak> getDesignaledPeakList(ArrayList<IonMatch> matches) {
        HashMap<Double, Peak> result = new HashMap<Double, Peak>(peakList);
        for (IonMatch ionMatch : matches) {
            result.remove(ionMatch.peak.mz);
        }
        return result;
    }

    /**
     * Returns the part of the spectrum contained between mzMin (inclusive) and
     * mzMax (exclusive) as a peak list
     *
     * @param mzMin the minimum m/z value
     * @param mzMax the maximum m/z value
     *
     * @return the part of the spectrum contained between mzMin (inclusive) and
     * mzMax (exclusive)
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public HashMap<Double, Peak> getSubSpectrum(double mzMin, double mzMax) throws InterruptedException {
        HashMap<Double, Peak> result = new HashMap<Double, Peak>();
        for (double mz : getOrderedMzValues()) {
            if (mz >= mzMin && mz < mzMax) {
                result.put(mz, peakList.get(mz));
            } else if (mz >= mzMax) {
                break;
            }
        }
        return result;
    }

    /**
     * Returns the peak list in a map where peaks are indexed by their
     * intensity.
     *
     * @return the peak list in a map where peaks are indexed by their intensity
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public HashMap<Double, ArrayList<Peak>> getIntensityMap() throws InterruptedException {

        if (intensityPeakMap == null) {

            mutex.acquire();

            if (intensityPeakMap == null) {

                intensityPeakMap = new HashMap<Double, ArrayList<Peak>>(peakList.size());

                for (Peak peak : peakList.values()) {

                    double intensity = peak.intensity;
                    ArrayList<Peak> peaksAtIntensity = intensityPeakMap.get(intensity);

                    if (peaksAtIntensity == null) {
                        peaksAtIntensity = new ArrayList<Peak>();
                        intensityPeakMap.put(intensity, peaksAtIntensity);
                    }

                    peaksAtIntensity.add(peak);
                }
            }

            mutex.release();
        }

        return intensityPeakMap;
    }

    /**
     * Returns the number of peaks in the spectrum.
     *
     * @return the number of peaks in the spectrum
     */
    public int getNPeaks() {
        if (peakList == null) {
            return 0;
        }
        return peakList.size();
    }

    /**
     * Returns a boolean indicating whether the spectrum is empty.
     *
     * @return a boolean indicating whether the spectrum is empty
     */
    public boolean isEmpty() {
        return getNPeaks() == 0;
    }

    /**
     * Resets all the saved values to null. Used after altering the peak data.
     */
    private void resetSavedData() {
        jFreePeakList = null;
        peakListAsString = null;
        mzValuesAsArray = null;
        mzValuesOrderedAsArray = null;
        intensityValuesAsArray = null;
        intensityValuesNormaizedAsArray = null;
        binnedCumulativeFunction = null;
        mzAndIntensityAsArray = null;
        totalIntensity = null;
        maxIntensity = null;
        maxMz = null;
        minMz = null;
        intensityPeakMap = null;
        intensityLimit = null;
    }

    /**
     * Returns the intensity of the log of the peaks intensities.
     *
     * @return the intensity of the log of the peaks intensities
     *
     * @throws java.lang.InterruptedException exception thrown if a threading
     * issue occurs
     * @throws org.apache.commons.math.MathException exception thrown whenever
     * an error occurred while estimating probabilities.
     */
    public SimpleNoiseDistribution getIntensityLogDistribution() throws InterruptedException, MathException {
        if (binnedCumulativeFunction == null) {
            mutex.acquire();
            if (binnedCumulativeFunction == null) {
                binnedCumulativeFunction = new SimpleNoiseDistribution(peakList);
            }
            mutex.release();
        }
        return binnedCumulativeFunction;
    }
}
