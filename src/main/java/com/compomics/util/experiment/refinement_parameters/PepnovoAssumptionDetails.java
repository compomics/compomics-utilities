package com.compomics.util.experiment.refinement_parameters;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This class contains pepnovo assumption details which are not contained in the
 * tag assumption class which will be saved as additional parameter.
 *
 * @author Marc Vaudel
 */
public class PepnovoAssumptionDetails extends DbObject implements UrParameter {

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
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return rankScore;
    }

    /**
     * Sets the PepNovo rank score.
     *
     * @param rankScore the PepNovo rank score
     */
    public void setRankScore(double rankScore) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.rankScore = rankScore;
    }

    /**
     * Returns the PepNovo mH.
     *
     * @return the PepNovo mH
     */
    public double getMH() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return mH;
    }

    /**
     * Sets the PepNovo provided mH.
     *
     * @param mH the PepNovo mH
     */
    public void setMH(double mH) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.mH = mH;
    }

    @Override
    public long getParameterKey() {
        return getId();
    }
}
