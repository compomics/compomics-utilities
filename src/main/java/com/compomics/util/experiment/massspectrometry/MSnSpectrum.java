package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;

/**
 * This class models an MSn spectrum.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:00:36 AM
 */
public class MSnSpectrum extends ExperimentObject {

    /**
     * The MS level
     */
    private int level;
    /**
     * the precursor
     */
    private Precursor precursor;
    /**
     * the spectrum title
     */
    private String spectrumTitle;
    /**
     * the peak list
     */
    private HashSet<Peak> peakList;
    /**
     * the file name
     */
    private String fileName;

    /**
     * the scan number or range
     */
    private String scanNumber;

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
     * @param scanNumber         the spectrum scan number or range
     */
    public MSnSpectrum(int level, Precursor precursor, String spectrumTitle, HashSet<Peak> spectrum, String fileName, String scanNumber) {
        this.level = level;
        this.precursor = precursor;
        this.spectrumTitle = spectrumTitle;
        this.peakList = spectrum;
        this.fileName = fileName;
        this.scanNumber = scanNumber;
    }

    /**
     * Convenience method returning the key for a spectrum
     * @param spectrumFile  The spectrum file
     * @param spectrumTitle The spectrum title
     * @return  the corresponding spectrum key
     */
    public static String getSpectrumKey(String spectrumFile, String spectrumTitle) {
        return spectrumFile + "_" + spectrumTitle;
    }

    /**
     * return the MS level of the spectrum
     *
     * @return ms level
     */
    public int getLevel() {
        return level;
    }

    /**
     * returns the name of the spectrum file
     *
     * @return name of the file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * returns an iterator on the peaks
     *
     * @return iterator on the peaks
     */
    public Iterator<Peak> iterator() {
        return peakList.iterator();
    }

    public HashSet<Peak> getPeakList() {
        return peakList;
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
     * returns the title of the spectrum
     *
     * @return spectrum title
     */
    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    /**
     * return the peak list in a format readable by JFreeChart
     *
     * @return peak list
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

    /**
     * Returns the peak map.
     *
     * @return the peak map
     */
    public HashMap<Double, Peak> getPeakMap() {
        HashMap<Double, Peak> result = new HashMap<Double, Peak>();
        for (Peak peak : peakList) {
            result.put(peak.mz, peak);
        }
        return result;
    }

    /**
     * Getter for the scan number or range
     * @return the scan number or range
     */
    public String getScanNumber() {
        return scanNumber;
    }

    /**
     * Setter for the scan number or range
     * @param scanNumber    the scan number or range of the spectrum
     */
    public void setScanNumber(String scanNumber) {
        this.scanNumber = scanNumber;
    }
    
    /**
     * Returns the key of the spectrum
     * @return the key of the spectrum
     */
    public String getSpectrumKey() {
        return fileName + "_" + spectrumTitle;
    }
}
