package com.compomics.util.experiment.massspectrometry;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class models an MS1 spectrum.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:04:58 AM
 */
public class MS1Spectrum extends Spectrum {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -7328539274222920271L;

    /**
     * Constructor for an MS1 spectrum
     */
    public MS1Spectrum() {
    }

    /**
     * constructor for an MS1 spectrum
     *
     * @param spectrumTitle     title of the spectrum
     * @param spectrum          Set of peaks
     * @param fileName          name of the file
     * @param scanStartTime     scan start time
     */
    public MS1Spectrum(String fileName, String spectrumTitle, double scanStartTime, HashMap<Double, Peak> spectrum) {
        this.spectrumTitle = spectrumTitle;
        this.peakList = spectrum;
        this.fileName = fileName;
        this.scanStartTime = scanStartTime;
        this.level = 1;
    }

    @Override
    public void removePeakList() {
        this.peakList.clear();
    }
}
