package com.compomics.util.experiment.refinement_parameters;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This class contains pepnovo assumption details which are not contained in the
 * tag assumption class which will be saved as additional parameter.
 *
 * @author Marc Vaudel
 */
public class PepnovoAssumptionDetails extends ExperimentObject implements UrParameter {

    /**
     * Serial number used for serialization and object key.
     */
    private static final long serialVersionUID = -4163506699889716493L;
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
    public long getParameterKey() {
        
        return serialVersionUID;
        
    }
}
