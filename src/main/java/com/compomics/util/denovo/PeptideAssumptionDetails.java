/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.denovo;

import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This class contains the de novo specific details of a spectrum match
 *
 * @author Marc
 */
public class PeptideAssumptionDetails implements UrParameter {

    
    /**
     * The mass gap from the N-terminal to the start of the resources novo sequence.
     */
    private double nTermGap;

    /**
     * The mass gap from the C-terminal to the end of the resources novo sequence.
     */
    private double cTermGap;
    /**
     * The pep novo score
     */
    private double pepNovoScore;
    
    /**
     * Constructor
     */
    public PeptideAssumptionDetails() {
        
    }

    /**
     * Returns the N-term Gap
     * @return the N-term Gap
     */
    public double getnTermGap() {
        return nTermGap;
    }

    /**
     * Sets the N-term Gap
     * @param nTermGap the N-term Gap
     */
    public void setnTermGap(double nTermGap) {
        this.nTermGap = nTermGap;
    }

    /**
     * Returns the C-term Gap
     * @return the C-term Gap
     */
    public double getcTermGap() {
        return cTermGap;
    }

    /**
     * Sets the C-term Gap
     * @param cTermGap the C-term Gap
     */
    public void setcTermGap(double cTermGap) {
        this.cTermGap = cTermGap;
    }

    /**
     * Returns the pep novo score
     * @return the pep novo score
     */
    public double getPepNovoScore() {
        return pepNovoScore;
    }

    /**
     * Sets the pep novo score
     * @param pepNovoScore the pep novo score
     */
    public void setPepNovoScore(double pepNovoScore) {
        this.pepNovoScore = pepNovoScore;
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
