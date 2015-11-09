package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import com.compomics.util.experiment.io.massspectrometry.MgfReader;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.math.BasicMathFunctions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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
     * Boolean indicating whether the mzValuesAsArray is sorted.
     */
    private Boolean mzOrdered = false;
    /**
     * The intensity values as array. Null until set by the getter.
     */
    private double[] intensityValuesAsArray = null;
    /**
     * The intensity values as array normalized against the most intense peak.
     * Null until set by the getter.
     */
    private double[] intensityValuesNormaizedAsArray = null;
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
     * Convenience method returning the key for a spectrum.
     *
     * @param spectrumFile The spectrum file
     * @param spectrumTitle The spectrum title
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
        return spectrumKey.substring(0, spectrumKey.indexOf(SPECTRUM_KEY_SPLITTER)).trim();
    }

    /**
     * Convenience method to retrieve the name of a spectrum from the spectrum
     * key.
     *
     * @param spectrumKey the spectrum key
     * @return the title of the spectrum
     */
    public static String getSpectrumTitle(String spectrumKey) {
        return spectrumKey.substring(spectrumKey.indexOf(SPECTRUM_KEY_SPLITTER) + SPECTRUM_KEY_SPLITTER.length()).trim();
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
    public void addPeak(Peak aPeak) {
        if (peakList == null) {
            peakList = new HashMap<Double, Peak>();
        }
        this.peakList.put(aPeak.mz, aPeak);
    }

    /**
     * Set the peaks.
     *
     * @param peaks the peaks to set
     */
    public void setPeaks(ArrayList<Peak> peaks) {

        if (peakList != null) {
            this.peakList.clear();
        } else {
            peakList = new HashMap<Double, Peak>();
        }

        for (Peak p : peaks) {
            double mz = p.mz;
            peakList.put(mz, p);
        }
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
     * @param scanStartTime the time point when the spectrum was recorded
     */
    public void setScanStartTime(double scanStartTime) {
        this.scanStartTime = scanStartTime;
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
     * Returns the mz values as an array. Note: the array is not necessarily
     * ordered.
     *
     * @return the mz values as an array
     */
    public double[] getMzValuesAsArray() {

        if (mzValuesAsArray == null) {
            mzValuesAsArray = new double[peakList.size()];

            int counter = 0;

            for (double currentMz : peakList.keySet()) {
                mzValuesAsArray[counter] = currentMz;
                counter++;
            }
        }

        return mzValuesAsArray;
    }

    /**
     * Returns a list of the m/z values sorted in ascending order.
     *
     * @return a list of the m/z values sorted in ascending order
     */
    public double[] getOrderedMzValues() {
        if (mzOrdered == null || !mzOrdered) {
            getMzValuesAsArray();
            Arrays.sort(mzValuesAsArray);
            mzOrdered = true;
        }
        return mzValuesAsArray;
    }

    /**
     * Returns the intensity values as an array.
     *
     * @return the intensity values as an array
     */
    public double[] getIntensityValuesAsArray() {

        if (intensityValuesAsArray == null) {
            intensityValuesAsArray = new double[peakList.size()];

            int counter = 0;

            for (Peak currentPeak : peakList.values()) {
                intensityValuesAsArray[counter] = currentPeak.intensity;
                counter++;
            }
        }

        return intensityValuesAsArray;
    }

    /**
     * Returns the intensity values as an array normalized against the largest
     * peak.
     *
     * @return the normalized intensity values as an array
     */
    public double[] getIntensityValuesNormalizedAsArray() {

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

        return intensityValuesNormaizedAsArray;
    }

    /**
     * Returns the m/z and intensity values as an array in increasing order
     * sorted on m/z value.
     *
     * @return the m/z and intensity values as an array
     */
    public double[][] getMzAndIntensityAsArray() {

        if (mzAndIntensityAsArray == null) {
            mzAndIntensityAsArray = new double[2][peakList.size()];
            int i = 0;
            for (double mz : getOrderedMzValues()) {
                Peak currentPeak = peakList.get(mz);
                mzAndIntensityAsArray[0][i] = currentPeak.mz;
                mzAndIntensityAsArray[1][i] = currentPeak.intensity;
                i++;
            }
        }
        return mzAndIntensityAsArray;
    }

    /**
     * Returns the total intensity of the spectrum.
     *
     * @return the total intensity. 0 if no peak.
     */
    public double getTotalIntensity() {

        if (totalIntensity == null) {
            totalIntensity = 0.0;

            for (Peak currentPeak : peakList.values()) {
                totalIntensity += currentPeak.intensity;
            }
        }

        return totalIntensity;
    }

    /**
     * Returns the max intensity value.
     *
     * @return the max intensity value. 0 if no peak.
     */
    public double getMaxIntensity() {

        if (maxIntensity == null) {
            maxIntensity = 0.0;

            for (Peak currentPeak : peakList.values()) {
                if (currentPeak.intensity > maxIntensity) {
                    maxIntensity = currentPeak.intensity;
                }
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

        if (maxMz == null) {
            if (peakList.keySet().isEmpty()) {
                maxMz = 0.0;
            } else {
                maxMz = Collections.max(peakList.keySet());
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

        if (minMz == null) {
            if (peakList.keySet().isEmpty()) {
                minMz = 0.0;
            } else {
                minMz = Collections.min(peakList.keySet());
            }
        }

        return minMz;
    }

    /**
     * Returns an array containing the intensity of all peaks above the provided
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
     * Returns the intensity limit in intensity from a given percentile.
     *
     * @param intensityFraction the fraction of the intensity to use as limit,
     * e.g., 0.75 for the 75% most intense peaks.
     *
     * @return the intensity limit
     */
    public double getIntensityLimit(double intensityFraction) {

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
     * @return the recalibrated list of peaks indexed by m/z
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     */
    public HashMap<Double, Peak> getRecalibratedPeakList(HashMap<Double, Double> mzCorrections) throws IllegalArgumentException {

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
     * @return the part of the spectrum contained between mzMin (inclusive) and
     * mzMax (exclusive)
     */
    public HashMap<Double, Peak> getSubSpectrum(double mzMin, double mzMax) {
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
     */
    public HashMap<Double, ArrayList<Peak>> getIntensityMap() {
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
}
