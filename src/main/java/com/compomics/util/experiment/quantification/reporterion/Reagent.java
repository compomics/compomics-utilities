package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.ions.impl.ReporterIon;

/**
 * A reporter ion labeling reagent.
 *
 * @author Marc Vaudel
 */
public class Reagent {

    /**
     * Empty default constructor
     */
    public Reagent() {
    }

    /**
     * The name of the reagent.
     */
    private String name;
    /**
     * The reporter ion to look for in the spectrum.
     */
    private ReporterIon reporterIon;
    /**
     * The isotopic correction factor at -2Da.
     */
    private double minus2;
    /**
     * .
     * The isotopic correction factor at -1Da.
     */
    private double minus1;
    /**
     * The monoisotopic correction factor.
     */
    private double ref;
    /**
     * The isotopic correction factor at +1Da.
     */
    private double plus1;
    /**
     * The isotopic correction factor at +2Da.
     */
    private double plus2;

    /**
     * Returns the name of the reagent.
     *
     * @return the name of the reagent
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the reagent.
     *
     * @param name the name of the reagent
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the reporter ion to look for in the spectrum.
     *
     * @return the reporter ion to look for in the spectrum
     */
    public ReporterIon getReporterIon() {
        return reporterIon;
    }

    /**
     * Sets the reporter ion to look for in the spectrum.
     *
     * @param reporterIon the reporter ion to look for in the spectrum
     */
    public void setReporterIon(ReporterIon reporterIon) {
        this.reporterIon = reporterIon;
    }

    /**
     * Returns the isotopic correction factor at -2 Da.
     *
     * @return the isotopic correction factor at -2 Da
     */
    public double getMinus2() {
        return minus2;
    }

    /**
     * Sets the isotopic correction factor at -2 Da.
     *
     * @param minus2 the isotopic correction factor at -2 Da
     */
    public void setMinus2(double minus2) {
        this.minus2 = minus2;
    }

    /**
     * Returns the isotopic correction factor at -1 Da.
     *
     * @return the isotopic correction factor at -1 Da
     */
    public double getMinus1() {
        return minus1;
    }

    /**
     * Sets the isotopic correction factor at -1 Da.
     *
     * @param minus1 the isotopic correction factor at -1 Da
     */
    public void setMinus1(double minus1) {
        this.minus1 = minus1;
    }

    /**
     * Returns the value used as reference for the correction factors.
     *
     * @return the value used as reference for the correction factors
     */
    public double getRef() {
        return ref;
    }

    /**
     * Sets the value used as reference for the correction factors.
     *
     * @param ref the value used as reference for the correction factors
     */
    public void setRef(double ref) {
        this.ref = ref;
    }

    /**
     * Returns the isotopic correction factor at +1 Da.
     *
     * @return the isotopic correction factor at +1 Da
     */
    public double getPlus1() {
        return plus1;
    }

    /**
     * Sets the isotopic correction factor at +1 Da.
     *
     * @param plus1 the isotopic correction factor at +1 Da
     */
    public void setPlus1(double plus1) {
        this.plus1 = plus1;
    }

    /**
     * Returns the isotopic correction factor at +2 Da.
     *
     * @return the isotopic correction factor at +2 Da
     */
    public double getPlus2() {
        return plus2;
    }

    /**
     * Sets the isotopic correction factor at +2 Da.
     *
     * @param plus2 the isotopic correction factor at +2 Da
     */
    public void setPlus2(double plus2) {
        this.plus2 = plus2;
    }
}
