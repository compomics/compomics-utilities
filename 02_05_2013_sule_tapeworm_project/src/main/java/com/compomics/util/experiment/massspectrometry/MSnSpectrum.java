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

    public void setPrecursor(Precursor precursor) {
        this.precursor = precursor;
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
        String result = "BEGIN IONS" + System.getProperty("line.separator");
        result += "TITLE=" + spectrumTitle + System.getProperty("line.separator");
        result += "PEPMASS=" + precursor.getMz() + "\t" + precursor.getIntensity() + System.getProperty("line.separator");
        if (precursor.hasRTWindow()) {
            result += "RTINSECONDS=" + precursor.getRtWindow()[0] + "-" + precursor.getRtWindow()[1] + System.getProperty("line.separator");
        } else if (precursor.getRt() != -1) {
            result += "RTINSECONDS=" + precursor.getRt() + System.getProperty("line.separator");
        }
        if (!precursor.getPossibleCharges().isEmpty()) {
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
        result += System.getProperty("line.separator");
        }
        if (scanNumber != null && !scanNumber.equals("")) {
            result += "SCANS=" + scanNumber + System.getProperty("line.separator");
        }

        // add the values to a tree map to get them sorted in mz    
        TreeMap<Double, Double> sortedPeakList = new TreeMap<Double, Double>();

        for (Peak peak : peakList.values()) {
            sortedPeakList.put(peak.mz, peak.intensity);
        }

        for (Map.Entry<Double, Double> entry : sortedPeakList.entrySet()) {
            result += entry.getKey() + " " + entry.getValue() + System.getProperty("line.separator");
        }

        result += "END IONS" + System.getProperty("line.separator") + System.getProperty("line.separator");

        return result;
    }

    /**
     * Writes the spectrum in the mgf format using the given writer
     *
     * @param writer1 a buffered writer where the spectrum will be written
     * @throws IOException
     */
    public void writeMgf(BufferedWriter writer1) throws IOException {
        writer1.write("BEGIN IONS" + System.getProperty("line.separator"));
        writer1.write("TITLE=" + spectrumTitle + System.getProperty("line.separator"));
        writer1.write("PEPMASS=" + precursor.getMz() + "\t" + precursor.getIntensity() + System.getProperty("line.separator"));
        if (precursor.hasRTWindow()) {
            writer1.write("RTINSECONDS=" + precursor.getRtWindow()[0] + "-" + precursor.getRtWindow()[1] + System.getProperty("line.separator"));
        } else if (precursor.getRt() != -1) {
            writer1.write("RTINSECONDS=" + precursor.getRt() + System.getProperty("line.separator"));
        }
        writer1.write("CHARGE=");
        boolean first = true;
        for (Charge charge : precursor.getPossibleCharges()) {
            if (first) {
                first = false;
            } else {
                writer1.write(" and ");
            }
            writer1.write(charge.toString());
        }
        writer1.write(System.getProperty("line.separator"));

        ArrayList<Double> mzArray = new ArrayList<Double>(peakList.keySet());
        Collections.sort(mzArray);
        for (Double mz : mzArray) {
            writer1.write(mz + " " + peakList.get(mz).intensity + System.getProperty("line.separator"));
        }
        writer1.write("END IONS" + System.getProperty("line.separator") + System.getProperty("line.separator"));
    }
}