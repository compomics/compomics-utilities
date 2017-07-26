package com.compomics.util.experiment.massspectrometry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

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
     * @param scanStartTime The time point when the spectrum was recorded
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
     * Set the precursor.
     *
     * @param precursor the precursor to set
     */
    public void setPrecursor(Precursor precursor) {
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
     * Returns the peak list as an mgf bloc. @TODO: move this to the massspectrometry.export package
     *
     * @param additionalTags additional tags which will be added after the BEGIN
     * IONS tag in alphabetic order
     * @return the peak list as an mgf bloc
     */
    public String asMgf(HashMap<String, String> additionalTags) {

        StringBuilder results = new StringBuilder();
        String lineBreak = System.getProperty("line.separator");
        
        results.append("BEGIN IONS").append(lineBreak);

        if (additionalTags != null) {
            ArrayList<String> additionalTagsKeys = new ArrayList<>(additionalTags.keySet());
            Collections.sort(additionalTagsKeys);
            for (String tag : additionalTagsKeys) {
                String attribute = additionalTags.get(tag);
                if (attribute != null && !attribute.equals("")) {
                    results.append(tag).append("=").append(attribute).append(lineBreak);
                }
            }
        }

        results.append("TITLE=").append(spectrumTitle).append(lineBreak);
        results.append("PEPMASS=").append(precursor.getMz()).append("\t").append(precursor.getIntensity()).append(lineBreak);

        if (precursor.hasRTWindow()) {
            results.append("RTINSECONDS=").append(precursor.getRtWindow()[0]).append("-").append(precursor.getRtWindow()[1]).append(lineBreak);
        } else if (precursor.getRt() != -1) {
            results.append("RTINSECONDS=").append(precursor.getRt()).append(lineBreak);
        }

        if (!precursor.getPossibleCharges().isEmpty()) {
            results.append("CHARGE=");
            boolean first = true;
            for (Charge charge : precursor.getPossibleCharges()) {
                if (first) {
                    first = false;
                } else {
                    results.append(" and ");
                }
                results.append(charge.toString());
            }
            results.append(lineBreak);
        }

        if (scanNumber != null && !scanNumber.equals("")) {
            results.append("SCANS=").append(scanNumber).append(lineBreak);
        }

        // add the values to a tree map to get them sorted in mz    
        TreeMap<Double, Double> sortedPeakList = new TreeMap<>();

        for (Peak peak : peakList.values()) {
            sortedPeakList.put(peak.mz, peak.intensity);
        }

        for (Map.Entry<Double, Double> entry : sortedPeakList.entrySet()) {
            results.append(entry.getKey()).append(" ").append(entry.getValue()).append(lineBreak);
        }

        results.append("END IONS").append(lineBreak).append(lineBreak);

        return results.toString();
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
        String spectrumAsMgf = asMgf(additionalTags);
        mgfWriter.write(spectrumAsMgf);
    }
}
