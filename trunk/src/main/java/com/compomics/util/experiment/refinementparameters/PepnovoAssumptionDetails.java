/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.experiment.refinementparameters;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This class contains pepnovo assumption details which are not contained in the tag assumption class which will be saved as additional parameter.
 *
 * @author Marc
 */
public class PepnovoAssumptionDetails implements UrParameter {
    
    /**
     * The PepNovo rank score.
     */
    private double rankScore;
    /**
     * The PepNovo M+H.
     */
    private double mH;

    /**
     * Constructor.
     */
    public PepnovoAssumptionDetails() {
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

    /**
     * Returns the PepNovo mH.
     *
     * @return the PepNovo mH
     */
    public double getMH() {
        return mH;
    }

    /**
     * Sets the PepNovo provided mH.
     *
     * @param mH the PepNovo mH
     */
    public void setMH(double mH) {
        this.mH = mH;
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
