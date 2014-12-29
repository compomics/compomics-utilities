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
 * 
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 16-Aug-2010
 * Time: 14:36:23
 */
public class IsotopicDistributionSpectrum implements SpectrumFile {

    /**
     * The charge
     */
    private int iCharge;
    /**
     * The peaks
     */
    private HashMap iPeaks = new HashMap();
    /**
     * The precursor MZ
     */
    private double iPrecursorMZ;

    /**
     * Getter for the charge
     * @return int with the charge
     */
    public int getCharge() {
        return iCharge;
    }

    /**
     * Setter for the charge
     * @param aCharge   int with the charge of the precursor ion.
     */
    public void setCharge(int aCharge) {
        iCharge = aCharge;
    }

    /**
     * Getter for the filename
     * @return String with "Isotopic distribution"
     */
    public String getFilename() {
        return "Isotopic distribution";
    }

    /**
     * Setter for the filename
     * @param aFilename String with the filename for the file.
     */
    public void setFilename(String aFilename) {

    }

    /**
     * Getter for the peaks
     * @return HashMap with the peaks (key = mass, value = intensity)
     */
    public HashMap getPeaks() {
        return iPeaks;
    }

    /**
     * Setter for the peaks
     * @param aPeaks HashMap with Doubles as keys (the masses) and Doubles as values (the intensities).
     */
    public void setPeaks(HashMap aPeaks) {
        iPeaks = aPeaks;
    }

    /**
     * Getter for the precursor MZ
     * @return double with the precursor MZ
     */
    public double getPrecursorMZ() {
        return iPrecursorMZ;
    }

    /**
     * Setter for the precursor MZ
     * @param aPrecursorMZ  double with the precursor M/Z
     */
    public void setPrecursorMZ(double aPrecursorMZ) {
        this.iPrecursorMZ = aPrecursorMZ;
    }

    /**
     * Getter for the precursor intensity (here always zero)
     * @return zero
     */
    public double getIntensity() {
        return 0;
    }

    /**
     * Setter for the intensity. Here this will do nothing since there is no precursor
     * @param aIntensity double with the intensity of the precursor ion.
     */
    public void setIntensity(double aIntensity) {

    }

    /**
     * Method calculates the total intensity of the peaks
     * @return double with the sum of all the peak intensities
     */
    public double getTotalIntensity() {
        Iterator iter = this.iPeaks.values().iterator();
        double totalIntensity = 0.0;
        while (iter.hasNext()) {
            totalIntensity += (Double) iter.next();
        }
        return round(totalIntensity);
    }

    /**
     * Method that find the intensity of the most intense peak
     * @return double with the intensity of the most intense peak
     */
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

    /**
     * Method to write to a givern stream
     * @param   aOut    OutputStream to write the file to. This Stream
     *                  will <b>NOT</b> be closed by this method.
     * @throws IOException if an IOException occurs
     */
    public void writeToStream(OutputStream aOut) throws IOException {

    }

    /**
     * Method to write to a given file
     * @param   aParentDir  File with the parent directory to put the file in.
     * @throws IOException if an IOException occurs
     */
    public void writeToFile(File aParentDir) throws IOException {

    }

    /**
     * Method that compares an IsotopicDistributionSpectrum
     * @param o Another IsotopicDistributionSpectrum
     * @return int (always zero)
     */
    public int compareTo(Object o) {
        return 0;
    }

    /**
     * This will round the given double
     * @param aTotalIntensity The double to round
     * @return double
     */
    private double round(final double aTotalIntensity) {
        BigDecimal bd = new BigDecimal(aTotalIntensity).setScale(2, RoundingMode.UP);
        return bd.doubleValue();
    }
}
