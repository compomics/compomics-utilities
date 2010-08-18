package com.compomics.util.general;

import com.compomics.util.interfaces.SpectrumFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is an implementation of the SpectrumFile specific for the isotopic distribution
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 16-Aug-2010
 * Time: 14:36:23
 */

public class IsotopicDistributionSpectrum implements SpectrumFile {
    private int iCharge;
    private HashMap iPeaks = new HashMap();
    private double iPrecursorMass;

    public int getCharge() {
        return iCharge;
    }

    public void setCharge(int aCharge) {
        iCharge = aCharge;
    }

    public String getFilename() {
        return "Isotopic distribution";
    }

    public void setFilename(String aFilename) {

    }

    public HashMap getPeaks() {
        return iPeaks;
    }

    public void setPeaks(HashMap aPeaks) {
        iPeaks = aPeaks;
    }

    public double getPrecursorMZ() {
        return iPrecursorMass;
    }

    public void setPrecursorMZ(double aPrecursorMZ) {
        this.iPrecursorMass = aPrecursorMZ;
    }

    public double getIntensity() {
        return 0;
    }

    public void setIntensity(double aIntensity) {

    }

    public double getTotalIntensity() {
        Iterator iter = this.iPeaks.values().iterator();
        double totalIntensity = 0.0;
        while (iter.hasNext()) {
            totalIntensity += (Double) iter.next();
        }
        return round(totalIntensity);
    }


    public double getHighestIntensity() {
        Iterator iter = this.iPeaks.values().iterator();
        double highestIntensity = -1.0;
        while (iter.hasNext()) {
            double temp = (Double) iter.next();
            if (temp > highestIntensity) {
                highestIntensity = temp;
            }
        }
        return round(highestIntensity);
    }

    public void writeToStream(OutputStream aOut) throws IOException {

    }

    public void writeToFile(File aParentDir) throws IOException {

    }

    public int compareTo(Object o) {
        return 0;
    }

    private double round(final double aTotalIntensity) {
        BigDecimal bd = new BigDecimal(aTotalIntensity).setScale(2, RoundingMode.UP);
        return bd.doubleValue();
    }

}
