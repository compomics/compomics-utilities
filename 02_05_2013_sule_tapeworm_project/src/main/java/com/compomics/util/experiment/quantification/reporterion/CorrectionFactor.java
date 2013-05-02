package com.compomics.util.experiment.quantification.reporterion;

/**
 * This class models the isotope correction factors given by the labels constructor.
 * User: Marc
 * Date: Sep 29, 2010
 * Time: 5:35:27 PM
 */
public class CorrectionFactor {

    /**
     * The concerned ion index
     */
    private int ionId;
    /**
     * the isotope amount at -2Da
     */
    private double minus2;
    /**
     * the isotope amount at -1Da
     */
    private double minus1;
    /**
     * the isotope amount at +1Da
     */
    private double plus1;
    /**
     * the isotope amount at +2Da
     */
    private double plus2;

    /**
     * Constructor for the correction factor
     * @param ionId     The reporter index
     * @param minus2    The relative isotope amount at -2Da
     * @param minus1    The relative isotope amount at -1Da
     * @param plus1     The relative isotope amount at +1Da
     * @param plus2     The relative isotope amount at +2Da
     */
    public CorrectionFactor(int ionId, double minus2, double minus1, double plus1, double plus2) {
        this.ionId = ionId;
        this.minus1 = minus1;
        this.minus2 = minus2;
        this.plus1 = plus1;
        this.plus2 = plus2;
    }

    /**
     * returns the reporter ion id
     * @return  the reporter ion id
     */
    public int getIonId() {
        return ionId;
    }

    /**
     * Sets the reporter ion id
     * @param ionId the reporter ion id
     */
    public void setIonId(int ionId) {
        this.ionId = ionId;
    }

    /**
     * returns the reporter ion -1 Da amount
     * @return the reporter ion -1 Da amount
     */
    public double getMinus1() {
        return minus1;
    }

    /**
     * Sets the reporter ion -1 Da amount
     * @param minus1 the reporter ion -1 Da amount
     */
    public void setMinus1(double minus1) {
        this.minus1 = minus1;
    }

    /**
     * returns the reporter ion -2 Da amount
     * @return the reporter ion -2 Da amount
     */
    public double getMinus2() {
        return minus2;
    }

    /**
     * Sets the reporter ion -2 Da amount
     * @param minus2 the reporter ion -2 Da amount
     */
    public void setMinus2(double minus2) {
        this.minus2 = minus2;
    }

    /**
     * returns the reporter ion +1 Da amount
     * @return the reporter ion +1 Da amount
     */
    public double getPlus1() {
        return plus1;
    }

    /**
     * Sets the reporter ion +1 Da amount
     * @param plus1 the reporter ion +1 Da amount
     */
    public void setPlus1(double plus1) {
        this.plus1 = plus1;
    }

    /**
     * returns the reporter ion +2 Da amount
     * @return the reporter ion +2 Da amount
     */
    public double getPlus2() {
        return plus2;
    }

    /**
     * Sets the reporter ion +2 Da amount
     * @param plus2 the reporter ion +2 Da amount
     */
    public void setPlus2(double plus2) {
        this.plus2 = plus2;
    }
}
