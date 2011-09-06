package com.compomics.util.experiment.massspectrometry;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class models an MSn spectrum.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:00:36 AM
 */
public class MSnSpectrum extends Spectrum {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -7144961253807359274L;
    /**
     * the precursor
     */
    private Precursor precursor;

    /**
     * Constructor for the spectrum
     */
    public MSnSpectrum() {
    }

    /**
     * Minimal constructor for the spectrum. The peak list is not loaded in order to reduce memory consumption.
     *
     * @param level              MS level
     * @param precursor          precursor
     * @param spectrumTitle      spectrum title
     * @param fileName           file name
     */
    public MSnSpectrum(int level, Precursor precursor, String spectrumTitle, String fileName) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.fileName = fileName;
    }

    /**
     * constructor for the spectrum
     *
     * @param level              MS level
     * @param precursor          precursor
     * @param spectrumTitle      spectrum title
     * @param spectrum           set of peaks
     * @param fileName           file name
     */
    public MSnSpectrum(int level, Precursor precursor, String spectrumTitle, HashSet<Peak> spectrum, String fileName) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.peakList = spectrum;
        this.fileName = fileName;
    }

    /**
     * constructor for the spectrum
     *
     * @param level              MS level
     * @param precursor          precursor
     * @param spectrumTitle      spectrum title
     * @param spectrum           set of peaks
     * @param fileName           file name
     * @param scanStartTime      The timepoint when the spectrum was recorded
     */
    public MSnSpectrum(int level, Precursor precursor, String spectrumTitle, HashSet<Peak> spectrum, String fileName, double scanStartTime) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.peakList = spectrum;
        this.fileName = fileName;
        this.scanStartTime = scanStartTime;
    }

    /**
     * returns the precursor
     *
     * @return precursor charge
     */
    public Precursor getPrecursor() {
        return precursor;
    }

    /**
     * return the peak list as mgf bloc
     * 
     * @return the peak list as mgf bloc
     */
    public String asMgf() {    
        String result = "BEGIN IONS\n\n";
        result += "TITLE=" + spectrumTitle + "\n";
        result += "PEPMASS=" + precursor.getMz() + "\n";
        result += "RTINSECONDS=" + precursor.getRt() + "\n";
        result += "CHARGE=" + precursor.getCharge().toString() + "\n\n";

        // add the values to a tree map to get them sorted in mz    
        TreeMap<Double, Double> sortedPeakList = new TreeMap<Double, Double>();

        for (Peak peak : peakList) {
            sortedPeakList.put(peak.mz, peak.intensity);
        }

        for (Map.Entry<Double, Double> entry : sortedPeakList.entrySet()) {
            result += entry.getKey() + " " + entry.getValue() + "\n";
        }

        result += "\nEND IONS\n\n\n";

        return result;
    }
}
