package com.compomics.util.experiment.massspectrometry;


import java.util.HashSet;
import java.util.Iterator;

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
     * the precursor
     */
    private Precursor precursor;

    /**
     * Constructor for the spectrum
     */
    public MSnSpectrum() {
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
        double mass = (precursor.getMz()+precursor.getCharge().value)/precursor.getCharge().value;
        String result = "BEGIN IONS\n\n";
        result += "TITLE=" + spectrumTitle + "\n";
        result += "PEPMASS=" + mass + "\n";
        result += "RTINSECONDS=" + precursor.getRt() + "\n";
        result += "CHARGE=" + precursor.getCharge().toString() + "\n\n";

        Iterator<Peak> peakIt = peakList.iterator();
        Peak currentPeak;
        while (peakIt.hasNext()) {
            currentPeak = peakIt.next();
            result += currentPeak.mz + " " + currentPeak.intensity + "\n";
        }

        result += "\nEND IONS\n\n\n";

        return result;
    }
}
