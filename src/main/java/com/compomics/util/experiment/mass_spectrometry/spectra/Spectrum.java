package com.compomics.util.experiment.mass_spectrometry.spectra;

import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.mass_spectrometry.SimpleNoiseDistribution;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.math.BasicMathFunctions;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * This class models a spectrum.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Spectrum extends ExperimentObject {

    /**
     * Empty default constructor
     */
    public Spectrum() {
    }

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
     * Spectrum key.
     */
    protected String key = null;
    /**
     * The MS level.
     */
    protected int level;
    /**
     * The precursor if any.
     */
    private Precursor precursor;
    /**
     * mz indexed Peak map.
     */
    protected HashMap<Double, Peak> peakMap;
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
     * The mz values sorted in acceding order as array. Null until set by the
     * getter.
     */
    private double[] mzValuesAsArraySorted = null;
    /**
     * The intensity values as array. Null until set by the getter.
     */
    private double[] intensityValuesAsArray = null;
    /**
     * The intensity values as array normalized against the most intense peak.
     * Null until set by the getter.
     */
    private double[] intensityValuesNormalizedAsArray = null;
    /**
     * The mz and intensity values as array. Null until set by the getter.
     */
    private double[][] mzAndIntensityAsArray = null;
    /**
     * The total intensity.
     */
    private double totalIntensity = -1.0;
    /**
     * The maximal intensity.
     */
    private double maxIntensity = -1.0;
    /**
     * The maximal m/z.
     */
    private double maxMz = -1.0;
    /**
     * The minimal m/z.
     */
    private double minMz = -1.0;
    /**
     * Cache for the intensity limit.
     */
    private double intensityLimit = -1.0;
    /**
     * Intensity level corresponding to the value in cache.
     */
    private double intensityLimitLevel = -1.0;
    /**
     * The type of intensity threshold.
     */
    private AnnotationParameters.IntensityThresholdType intensityThresholdType = null;
    /**
     * The binned cumulative function of the distribution of the log of the
     * peaks intensities.
     */
    private SimpleNoiseDistribution binnedCumulativeFunction = null;
    
    /**
     * Constructor for the spectrum.
     *
     * @param level MS level
     * @param precursor precursor
     * @param spectrumTitle spectrum title
     * @param fileName file name
     */
    public Spectrum(int level, Precursor precursor, String spectrumTitle, String fileName) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.fileName = fileName;
    }

    /**
     * Constructor for the spectrum.
     *
     * @param level MS level
     * @param precursor precursor
     * @param spectrumTitle spectrum title
     * @param peakMap set of peaks
     * @param fileName file name
     */
    public Spectrum(int level, Precursor precursor, String spectrumTitle, HashMap<Double, Peak> peakMap, String fileName) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.peakMap = peakMap;
        this.fileName = fileName;
    }

    /**
     * Constructor for the spectrum.
     *
     * @param level MS level
     * @param precursor precursor
     * @param spectrumTitle spectrum title
     * @param peakMap set of peaks
     * @param fileName file name
     * @param scanStartTime The time point when the spectrum was recorded
     */
    public Spectrum(int level, Precursor precursor, String spectrumTitle, HashMap<Double, Peak> peakMap, String fileName, double scanStartTime) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.peakMap = peakMap;
        this.fileName = fileName;
        this.scanStartTime = scanStartTime;
    }

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
     * Returns the key of the spectrum.
     *
     * @return the key of the spectrum
     */
    public String getSpectrumKey() {
        readDBMode();

        if (key == null) {

            StringBuilder stringBuilder = new StringBuilder(fileName.length() + SPECTRUM_KEY_SPLITTER.length() + spectrumTitle.length());
            stringBuilder.append(fileName);
            stringBuilder.append(SPECTRUM_KEY_SPLITTER);
            stringBuilder.append(spectrumTitle);
            key = stringBuilder.toString();

        }

        return key;
    }

    /**
     * Returns the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        readDBMode();
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the file name
     */
    public void setFileName(String fileName) {
        writeDBMode();
        this.fileName = fileName;
    }

    /**
     * Returns the spectrum title.
     *
     * @return spectrum title
     */
    public String getSpectrumTitle() {
        readDBMode();
        return spectrumTitle;
    }

    /**
     * Set the spectrum title.
     *
     * @param spectrumTitle the title to set
     */
    public void setSpectrumTitle(String spectrumTitle) {
        writeDBMode();
        this.spectrumTitle = spectrumTitle;
    }

    /**
     * Getter for the scan number.
     *
     * @return the spectrum scan number
     */
    public String getScanNumber() {
        readDBMode();
        return scanNumber;
    }

    /**
     * Setter for the scan number or range.
     *
     * @param scanNumber or range
     */
    public synchronized void setScanNumber(String scanNumber) {
        writeDBMode();
        this.scanNumber = scanNumber;
    }

    /**
     * Returns a peak map where peaks are indexed by their m/z.
     *
     * @return a peak map
     */
    public HashMap<Double, Peak> getPeakMap() {
        readDBMode();
        return peakMap;
    }

    /**
     * Sets the peak map indexed by m/z.
     *
     * @param peakMap the peak map
     */
    public void setPeakMap(HashMap<Double, Peak> peakMap) {
        writeDBMode();

        this.peakMap = peakMap;

        resetSavedData();
    }

    /**
     * Set the peaks.
     *
     * @param peaks the peaks to set
     */
    public void setPeaks(ArrayList<Peak> peaks) {
        writeDBMode();

        peakMap = peaks.stream().collect(Collectors.toMap(
                peak -> peak.mz,
                peak -> peak,
                (v1, v2) -> {
                    throw new IllegalArgumentException("Two peaks provided with the same mass: " + v1 + ".");
                },
                HashMap::new));

        resetSavedData();
    }

    /**
     * Returns at which level the spectrum was recorded.
     *
     * @return at which level the spectrum was recorded
     */
    public int getLevel() {
        readDBMode();
        return level;
    }

    /**
     * Returns the peak list.
     *
     * @return the peak list
     */
    public Collection<Peak> getPeakList() {
        readDBMode();
        return peakMap.values();
    }

    /**
     * Returns the peak list as an array list formatted as text, e.g.
     * [[303.17334 3181.14],[318.14542 37971.93], ... ].
     *
     * @return the peak list as an array list formatted as text
     */
    public String getPeakListAsString() {
        readDBMode();

        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(
                peakMap.values().stream()
                        .sorted(Peak.AscendingMzComparator)
                        .map(peak -> peak.toString())
                        .collect(Collectors.joining(",")));

        sb.append("]");

        return sb.toString();
    }

    /**
     * Returns the scan start time.
     *
     * @return the scan start time
     */
    public double getScanStartTime() {
        readDBMode();
        return scanStartTime;
    }

    /**
     * Sets the scan start time.
     *
     * @param scanStartTime the time point when the spectrum was recorded
     */
    public void setScanStartTime(double scanStartTime) {
        writeDBMode();
        this.scanStartTime = scanStartTime;
    }

    /**
     * Returns an array of the m/z values sorted in ascending order.
     *
     * @return an array of the m/z values sorted in ascending order
     */
    public double[] getOrderedMzValues() {
        readDBMode();

        if (mzValuesAsArraySorted == null) {

            mzValuesAsArraySorted = peakMap.keySet().stream()
                    .mapToDouble(Double::doubleValue)
                    .sorted()
                    .toArray();

        }

        return mzValuesAsArraySorted;
    }

    /**
     * Setter for the intensityValuesAsArray.
     *
     * @param intensityValuesAsArray the intensity values array
     */
    public void setIntensityValuesAsArray(double[] intensityValuesAsArray) {
        writeDBMode();
        this.intensityValuesAsArray = intensityValuesAsArray;
    }

    /**
     * Returns the intensity values as an array.
     *
     * @return the intensity values as an array
     */
    public double[] getIntensityValuesAsArray() {
        readDBMode();

        if (intensityValuesAsArray == null || (intensityValuesAsArray.length != peakMap.size())) {

            intensityValuesAsArray = Arrays.stream(getOrderedMzValues())
                    .map(mz -> peakMap.get(mz).intensity)
                    .toArray();

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
        readDBMode();

        if (intensityValuesNormalizedAsArray == null) {

            double[] intensityArray = getIntensityValuesAsArray();
            intensityArray = Arrays.copyOf(intensityArray, intensityArray.length);
            double highestIntensity = Arrays.stream(intensityArray).max().orElse(0.0);
            
            if (highestIntensity > 0.0) {
                
                for (int i = 0; i < intensityArray.length; i++) {

                    intensityArray[i] = intensityArray[i] / highestIntensity * 100;

                }
            }

            intensityValuesNormalizedAsArray = intensityArray;
        }

        return intensityValuesNormalizedAsArray;
    }

    /**
     * Returns the m/z and intensity values as an array in increasing order
     * sorted on m/z value.
     *
     * @return the m/z and intensity values as an array
     */
    public double[][] getMzAndIntensityAsArray() {
        readDBMode();

        if (mzAndIntensityAsArray == null) {

            double[] orderedMzValues = getOrderedMzValues();

            double[][] array = new double[2][peakMap.size()];
            int counter = 0;

            for (double mz : orderedMzValues) {
                Peak currentPeak = peakMap.get(mz);
                array[0][counter] = currentPeak.mz;
                array[1][counter] = currentPeak.intensity;
                counter++;
            }

            mzAndIntensityAsArray = array;
        }

        return mzAndIntensityAsArray;
    }

    /**
     * Returns the total intensity of the spectrum.
     *
     * @return the total intensity. 0 if no peak.
     */
    public double getTotalIntensity() {
        readDBMode();

        if (totalIntensity == -1.0) {

            totalIntensity = peakMap.values().stream()
                    .mapToDouble(peak -> peak.intensity)
                    .sum();

        }

        return totalIntensity;
    }

    /**
     * Returns the max intensity value.
     *
     * @return the max intensity value. 0 if no peak.
     */
    public double getMaxIntensity() {
        readDBMode();

        if (maxIntensity == -1.0) {

            maxIntensity = peakMap.values().stream()
                    .mapToDouble(peak -> peak.intensity)
                    .max().orElse(0.0);

        }

        return maxIntensity;
    }

    /**
     * Returns the max mz value.
     *
     * @return the max mz value
     */
    public double getMaxMz() {
        readDBMode();

        if (maxMz == -1.0) {

            maxMz = peakMap.keySet().stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

        }

        return maxMz;
    }

    /**
     * Returns the min mz value.
     *
     * @return the min mz value
     */
    public double getMinMz() {
        readDBMode();

        if (minMz == -1.0) {

            minMz = peakMap.keySet().stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0);

        }

        return minMz;
    }

    /**
     * Returns a DoubleStream of the intensity of all peaks strictly above the
     * provided threshold.
     *
     * @param threshold the lower threshold
     *
     * @return a DoubleStream of the intensity of all peaks strictly above the
     * provided threshold
     */
    public DoubleStream getPeaksAboveIntensityThreshold(double threshold) {
        readDBMode();

        return peakMap.values().stream()
                .mapToDouble(peak -> peak.intensity)
                .filter(intensity -> intensity > threshold);
    }

    /**
     * Returns the limit in intensity according to the given threshold.
     *
     * @param intensityThresholdType the type of intensity threshold
     * @param intensityFraction the threshold value.
     *
     * @return the intensity limit
     */
    public double getIntensityLimit(AnnotationParameters.IntensityThresholdType intensityThresholdType, double intensityFraction) {
        readDBMode();

        if (intensityLimit == -1.0 || intensityThresholdType != this.intensityThresholdType || intensityLimitLevel != intensityFraction) {

            intensityLimit = estimateIntenistyLimit(intensityThresholdType, intensityFraction);
            intensityLimitLevel = intensityFraction;
            this.intensityThresholdType = intensityThresholdType;

        }

        return intensityLimit;
    }

    /**
     * Estimates the intensity limit in intensity from a given percentile.
     *
     * @param intensityThreshold the fraction of the intensity to use as limit,
     * e.g., 0.75 for the 75% most intense peaks.
     *
     * @return the intensity limit
     */
    private double estimateIntenistyLimit(AnnotationParameters.IntensityThresholdType intensityThresholdType, double intensityThreshold) {
        readDBMode();

        if (intensityThreshold == 0) {

            return 0.0;

        } else if (intensityThreshold == 1.0) {

            return getMaxIntensity();

        }

        switch (intensityThresholdType) {

            case snp:

                SimpleNoiseDistribution binnedCumulativeFunction = getIntensityLogDistribution();
                return binnedCumulativeFunction.getIntensityAtP(1 - intensityThreshold);

            case percentile:

                // Skip the low mass region of the spectrum @TODO: skip precursor as well
                ArrayList<Double> intensities = peakMap.keySet().stream()
                        .filter(mz -> mz > 200)
                        .collect(Collectors.toCollection(ArrayList::new));

                if (intensities.isEmpty()) {
                    return 0;
                }

                return BasicMathFunctions.percentile(intensities, intensityThreshold);

            default:
                throw new UnsupportedOperationException("Threshold of type " + intensityThresholdType + " not supported.");
        }
    }

    /**
     * Returns a recalibrated peak list.
     *
     * @param mzCorrections the m/z corrections to apply
     *
     * @return the recalibrated list of peaks indexed by m/z
     */
    public HashMap<Double, Peak> getRecalibratedPeakList(HashMap<Double, Double> mzCorrections) {
        readDBMode();

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

    /**
     * Returns the peak list of this spectrum without matched peaks.
     *
     * @param matches the ion matches
     *
     * @return a peak list which does not contain the peak matched
     */
    public HashMap<Double, Peak> getDesignaledPeakList(ArrayList<IonMatch> matches) {
        readDBMode();

        HashMap<Double, Peak> result = new HashMap<>(peakMap);

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
     */
    public HashMap<Double, Peak> getSubSpectrum(double mzMin, double mzMax) {
        readDBMode();

        return peakMap.entrySet().stream()
                .filter(entry -> entry.getKey() >= mzMin && entry.getKey() < mzMax)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalArgumentException("Two peaks provided with the same mass: " + v1 + ".");
                        },
                        HashMap::new));
    }

    /**
     * Returns the peak list in a map where peaks are indexed by their
     * intensity.
     *
     * @return the peak list in a map where peaks are indexed by their intensity
     */
    public HashMap<Double, ArrayList<Peak>> getIntensityMap() {
        readDBMode();

        if (intensityPeakMap == null) {

            intensityPeakMap = peakMap.values().stream()
                    .collect(Collectors.groupingBy(peak -> peak.intensity,
                            HashMap::new,
                            Collectors.toCollection(ArrayList::new)));

        }

        return intensityPeakMap;
    }

    /**
     * Returns the number of peaks in the spectrum.
     *
     * @return the number of peaks in the spectrum
     */
    public int getNPeaks() {
        readDBMode();

        return peakMap == null ? 0 : peakMap.size();
    }

    /**
     * Returns a boolean indicating whether the spectrum is empty.
     *
     * @return a boolean indicating whether the spectrum is empty
     */
    public boolean isEmpty() {
        readDBMode();
        return getNPeaks() == 0;
    }

    /**
     * Resets all the saved values to null. Used after altering the peak data.
     */
    private void resetSavedData() {
        jFreePeakList = null;
        mzValuesAsArraySorted = null;
        intensityValuesAsArray = null;
        intensityValuesNormalizedAsArray = null;
        binnedCumulativeFunction = null;
        mzAndIntensityAsArray = null;
        totalIntensity = -1.0;
        maxIntensity = -1.0;
        maxMz = -1.0;
        minMz = -1.0;
        intensityPeakMap = null;
        intensityLimit = -1.0;
        intensityThresholdType = null;
    }

    /**
     * Returns the intensity of the log of the peaks intensities.
     *
     * @return the intensity of the log of the peaks intensities
     */
    public SimpleNoiseDistribution getIntensityLogDistribution() {
        readDBMode();

        if (binnedCumulativeFunction == null) {
            binnedCumulativeFunction = new SimpleNoiseDistribution(peakMap);
        }
        return binnedCumulativeFunction;
    }

    /**
     * Returns the precursor.
     *
     * @return precursor charge
     */
    public Precursor getPrecursor() {
        readDBMode();
        return precursor;
    }

    /**
     * Set the precursor.
     *
     * @param precursor the precursor to set
     */
    public void setPrecursor(Precursor precursor) {
        writeDBMode();
        this.precursor = precursor;
    }

    /**
     * Returns the peak list as an mgf bloc.
     *
     * @return the peak list as an mgf bloc
     */
    public String asMgf() {
        return asMgf(null);
    }

    /**
     * Returns the peak list as an mgf bloc. @TODO: move this to the
     * massspectrometry.export package
     *
     * @param additionalTags additional tags which will be added after the BEGIN
     * IONS tag in alphabetic order
     * @return the peak list as an mgf bloc
     */
    public String asMgf(HashMap<String, String> additionalTags) {
        readDBMode();

        StringBuilder result = new StringBuilder();
        String lineBreak = System.getProperty("line.separator");

        result.append("BEGIN IONS").append(lineBreak);

        if (additionalTags != null) {
            ArrayList<String> additionalTagsKeys = new ArrayList<>(additionalTags.keySet());
            Collections.sort(additionalTagsKeys);
            for (String tag : additionalTagsKeys) {
                String attribute = additionalTags.get(tag);
                if (attribute != null && !attribute.equals("")) {
                    result.append(tag).append("=").append(attribute).append(lineBreak);
                }
            }
        }

        result.append("TITLE=").append(spectrumTitle).append(lineBreak);
        result.append("PEPMASS=").append(precursor.getMz()).append("\t").append(precursor.getIntensity()).append(lineBreak);

        if (precursor.hasRTWindow()) {
            result.append("RTINSECONDS=").append(precursor.getRtWindow()[0]).append("-").append(precursor.getRtWindow()[1]).append(lineBreak);
        } else if (precursor.getRt() != -1) {
            result.append("RTINSECONDS=").append(precursor.getRt()).append(lineBreak);
        }

        if (!precursor.getPossibleCharges().isEmpty()) {
            result.append("CHARGE=");
            result.append(
                    precursor.getPossibleCharges().stream()
                            .sorted()
                            .map(charge -> charge.toString())
                            .collect(Collectors.joining(" and "))
            );
            result.append(lineBreak);
        }

        if (scanNumber != null && !scanNumber.equals("")) {
            result.append("SCANS=").append(scanNumber).append(lineBreak);
        }

        // export peak list sorted by mz
        peakMap.values().stream()
                .sorted(Peak.AscendingMzComparator)
                .forEach(peak -> result.append(peak.mz).append(' ').append(peak.intensity).append(lineBreak));

        result.append("END IONS").append(lineBreak).append(lineBreak);

        return result.toString();
    }

    /**
     * Writes the spectrum in the mgf format using the given writer.
     *
     * @param writer1 a buffered writer where the spectrum will be written
     * @throws IOException if an IOException occurs
     */
    public void writeMgf(BufferedWriter writer1) throws IOException {
        writeMgf(writer1, null);
    }

    /**
     * Writes the spectrum in the mgf format using the given writer.
     *
     * @param mgfWriter a buffered writer where the spectrum will be written
     * @param additionalTags additional tags which will be added after the BEGIN
     * IONS tag in alphabetic order
     * @throws IOException if an IOException occurs
     */
    public void writeMgf(BufferedWriter mgfWriter, HashMap<String, String> additionalTags) throws IOException {
        writeDBMode();
        String spectrumAsMgf = asMgf(additionalTags);
        mgfWriter.write(spectrumAsMgf);
    }

    @Override
    public String toString() {
        return getPeakListAsString();
    }
}
