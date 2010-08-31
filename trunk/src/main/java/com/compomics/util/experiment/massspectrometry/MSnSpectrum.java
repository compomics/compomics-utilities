package com.compomics.util.experiment.massspectrometry;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:00:36 AM
 * This class modelizes an MSn spectrum.
 */
public class MSnSpectrum implements Serializable {

    /**
     * The MS level
     */
    private int level;
    /**
     * the precursor mass
     */
    private double precursorMass;
    /**
     * the precursor charge
     */
    private Charge precursorCharge;
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
     * the retention time
     */
    private double rt;


    /**
     * Constructor for the spectrum
     */
    public MSnSpectrum() {

    }

    /**
     * constructor for the spectrum
     * @param level              MS level
     * @param precursorMass      pecursor mass
     * @param precursorCharge    precursor charge
     * @param spectrumTitle      spectrum title
     * @param spectrum           set of peaks
     * @param fileName           file name
     * @param rt                 retention time
     */
    public MSnSpectrum(int level, double precursorMass, Charge precursorCharge, String spectrumTitle, HashSet<Peak> spectrum, String fileName, double rt) {
        this.level = level;
        this.precursorMass = precursorMass;
        this.precursorCharge = precursorCharge;
        this.spectrumTitle = spectrumTitle;
        this.peakList = spectrum;
        this.fileName = fileName;
        this.rt = rt;
    }


    /**
     * return the MS level of the spectrum
     * @return ms level
     */
    public int getLevel() {
        return level;
    }

    /**
     * returns the name of the spectrum file
     * @return name of the file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * returns an iterator on the peaks
     * @return iterator on the peaks
     */
    public Iterator<Peak> iterator() {
        return peakList.iterator();
    }

    /**
     * returns the charge of the precursor
     * @return precursor charge
     */
    public Charge getCharge() {
        return precursorCharge;
    }

    /**
     * returns the mass of the precursor
     * @return precursor mass
     */
    public double getPrecursorMass() {
        return precursorMass;
    }

    /**
     * returns the title of the spectrum
     * @return spectrum title
     */
    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    /**
     * return the retention time when the spectrum was acquired
     * @return retention time
     */
    public double getRT() {
        return rt;
    }

    /**
     * return the peak list in a format readable by JFreeChart
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
     * @return
     */
    public String asMgf() {
        String result = "BEGIN IONS\n\n";
        result += "TITLE=" + spectrumTitle + "\n";
        result += "PEPMASS=" + precursorMass + "\n";
        result += "RTINSECONDS=" + rt + "\n";
        result += "CHARGE=" + precursorCharge.toString() + "\n\n";

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
