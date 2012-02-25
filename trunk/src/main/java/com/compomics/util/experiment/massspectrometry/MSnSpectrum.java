package com.compomics.util.experiment.massspectrometry;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class models an MSn spectrum.
 *
 * @author Marc Vaudel
 */
public class MSnSpectrum extends Spectrum {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -7144961253807359274L;
    /**
     * The precursor.
     */
    private Precursor precursor;

    /**
     * Constructor for the spectrum.
     */
    public MSnSpectrum() {
    }

    /**
     * Minimal constructor for the spectrum. The peak list is not loaded in
     * order to reduce memory consumption.
     *
     * @param level MS level
     * @param precursor precursor
     * @param spectrumTitle spectrum title
     * @param fileName file name
     */
    public MSnSpectrum(int level, Precursor precursor, String spectrumTitle, String fileName) {
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
    public MSnSpectrum(int level, Precursor precursor, String spectrumTitle, HashMap<Double, Peak> peakMap, String fileName) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.peakList = peakMap;
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
     * @param scanStartTime The timepoint when the spectrum was recorded
     */
    public MSnSpectrum(int level, Precursor precursor, String spectrumTitle, HashMap<Double, Peak> peakMap, String fileName, double scanStartTime) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.peakList = peakMap;
        this.fileName = fileName;
        this.scanStartTime = scanStartTime;
    }

    /**
     * Returns the precursor.
     *
     * @return precursor charge
     */
    public Precursor getPrecursor() {
        return precursor;
    }

    /**
     * Returns the peak list as mgf bloc.
     *
     * @return the peak list as mgf bloc
     */
    public String asMgf() {
        String result = "BEGIN IONS\n\n";
        result += "TITLE=" + spectrumTitle + "\n";
        result += "PEPMASS=" + precursor.getMz() + "\n";
        result += "RTINSECONDS=" + precursor.getRt() + "\n";
        result += "CHARGE=";
        boolean first = true;
        for (Charge charge : precursor.getPossibleCharges()) {
            if (first) {
                first = false;
            } else {
                result += " and ";
            }
            result += charge.toString();
        }
        result += "\n\n";

        // add the values to a tree map to get them sorted in mz    
        TreeMap<Double, Double> sortedPeakList = new TreeMap<Double, Double>();

        for (Peak peak : peakList.values()) {
            sortedPeakList.put(peak.mz, peak.intensity);
        }

        for (Map.Entry<Double, Double> entry : sortedPeakList.entrySet()) {
            result += entry.getKey() + " " + entry.getValue() + "\n";
        }

        result += "\nEND IONS\n\n\n";

        return result;
    }
}
