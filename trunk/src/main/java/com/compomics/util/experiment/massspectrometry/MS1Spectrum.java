package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.HashSet;
import java.util.Iterator;

/**
 * This class models an MS1 spectrum.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:04:58 AM
 */
public class MS1Spectrum extends ExperimentObject {

    /**
     * spectrum title
     */
    private String spectrumTitle;
    /**
     * peak list
     */
    private HashSet<Peak> peakList;
    /**
     * spectrum file name
     */
    private String fileName;
    /**
     * scan number or range
     */
    private String scanNumber;
    /**
     * retention time
     */
    private double rt;

    /**
     * Constructor for an MS1 spectrum
     */
    public MS1Spectrum() {

    }

    /**
     * constructor for an MS1 spectrum
     *
     * @param spectrumTitle title of the spectrum
     * @param spectrum      Set of peaks
     * @param fileName      name of the file
     * @param rt            retention time
     */
    public MS1Spectrum(String spectrumTitle, HashSet<Peak> spectrum, String fileName, double rt) {
        this.spectrumTitle = spectrumTitle;
        this.peakList = spectrum;
        this.fileName = fileName;
        this.rt = rt;
    }

    /**
     * returns an iterator on the peaks
     *
     * @return iterator on the peaks
     */
    public Iterator<Peak> iterator() {
        return peakList.iterator();
    }

    /**
     * returns the spectrum title
     *
     * @return spectrum title
     */
    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    /**
     * returns the retention time
     *
     * @return retention time
     */
    public double getRT() {
        return rt;
    }

    /**
     * format the peaks so they can be plot in JFreeChart
     * 
     * @return a table containing the peaks
     */
    public double[][] getJFreePeakList() {
        double[] mz = new double[peakList.size()];
        double[] intensity = new double[peakList.size()];
        Iterator<Peak> peakIt = peakList.iterator();
        Peak currentPeak;
        int cpt = 0;
        while (peakIt.hasNext()) {
            currentPeak = peakIt.next();
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
     * Getter for the scan number
     * @return the spectrum scan number
     */
    public String getScanNumber() {
        return scanNumber;
    }

    /**
     * Setter for the scan number or range
     * @param scanNumber or range
     */
    public void setScanNumber(String scanNumber) {
        this.scanNumber = scanNumber;
    }

    /**
     * returns the key of the spectrum
     * @return the key of the spectrum
     */
    public String getSpectrumKey() {
        return fileName + "_" + spectrumTitle;
    }
}