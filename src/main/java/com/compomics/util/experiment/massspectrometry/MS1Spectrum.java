package com.compomics.util.experiment.massspectrometry;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 9:04:58 AM
 * This class modelizes an MS1 spectrum.
 */
public abstract class MS1Spectrum implements Serializable {

    // Attributes

    protected String spectrumTitle;
    protected HashSet<Peak> peakList;
    protected String fileName;
    protected double rt;


    // Constructor

    public MS1Spectrum() {

    }

    public MS1Spectrum(String spectrumTitle, HashSet<Peak> spectrum, String fileName, double rt) {
        this.spectrumTitle = spectrumTitle;
        this.peakList = spectrum;
        this.fileName = fileName;
        this.rt = rt;
    }


    // Attributes

    public Iterator<Peak> iterator() {
        return peakList.iterator();
    }

    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    public double getRT() {
        return rt;
    }

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
}