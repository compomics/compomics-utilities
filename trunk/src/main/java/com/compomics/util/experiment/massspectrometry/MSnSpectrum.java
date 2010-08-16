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

    // Attributes

    private int level;
    private double precursorMass;
    private Charge precursorCharge;
    private String spectrumTitle;
    private HashSet<Peak> peakList;
    private String fileName;
    private double rt;


    // Constructor

    public MSnSpectrum() {

    }

    public MSnSpectrum(int level, double precursorMass, Charge precursorCharge, String spectrumTitle, HashSet<Peak> spectrum, String fileName, double rt) {
        this.level = level;
        this.precursorMass = precursorMass;
        this.precursorCharge = precursorCharge;
        this.spectrumTitle = spectrumTitle;
        this.peakList = spectrum;
        this.fileName = fileName;
        this.rt = rt;
    }


    // Methods

    public int getLevel() {
        return level;
    }

    public String getFileName() {
        return fileName;
    }

    public Iterator<Peak> iterator() {
        return peakList.iterator();
    }

    public Charge getCharge() {
        return precursorCharge;
    }

    public double getPrecursorMass() {
        return precursorMass;
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
