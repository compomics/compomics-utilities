package com.compomics.util.denovo;

import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This class contains the de novo specific details of a spectrum match.
 *
 * @author Marc Vaudel
 */
public class PeptideAssumptionDetails implements UrParameter {

    /**
     * The mass gap from the N-terminal to the start of the PepNovo sequence.
     */
    private double nTermGap;
    /**
     * The mass gap from the C-terminal to the end of the PepNovo sequence.
     */
    private double cTermGap;
    /**
     * The PepNovo rank score.
     */
    private double rankScore;

    /**
     * Constructor.
     */
    public PeptideAssumptionDetails() {
    }

    /**
     * Returns the N-term gap.
     *
     * @return the N-term gap
     */
    public double getNTermGap() {
        return nTermGap;
    }

    /**
     * Sets the N-term gap.
     *
     * @param nTermGap the N-term gap
     */
    public void setNTermGap(double nTermGap) {
        this.nTermGap = nTermGap;
    }

    /**
     * Returns the C-term gap.
     *
     * @return the C-term gap
     */
    public double getCTermGap() {
        return cTermGap;
    }

    /**
     * Sets the C-term gap.
     *
     * @param cTermGap the C-term gap
     */
    public void setCTermGap(double cTermGap) {
        this.cTermGap = cTermGap;
    }

    /**
     * Returns the PepNovo rank score.
     *
     * @return the PepNovo rank score
     */
    public double getRankScore() {
        return rankScore;
    }

    /**
     * Sets the PepNovo rank score.
     *
     * @param rankScore the PepNovo rank score
     */
    public void setRankScore(double rankScore) {
        this.rankScore = rankScore;
    }

    @Override
    public String getFamilyName() {
        return "deNovo";
    }

    @Override
    public int getIndex() {
        return 1;
    }
}
