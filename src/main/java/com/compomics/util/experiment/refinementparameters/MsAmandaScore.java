package com.compomics.util.experiment.refinementparameters;

import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This class will contain the MS Amanda score.
 *
 * @author Harald Barsnes
 */
public class MsAmandaScore implements UrParameter {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 6106747917948581247L;

    /**
     * This method returns the family name of the parameter.
     *
     * @return family name
     */
    public String getFamilyName() {
        return "Utilities MS Amanda Refinement Parameters";
    }

    /**
     * This method returns the index of the parameter.
     *
     * @return the index of the parameter
     */
    public int getIndex() {
        return 1;
    }
    /**
     * The MS Amanda score.
     */
    private double score;

    /**
     * Constructor for the MS Amanda score.
     */
    public MsAmandaScore() {
    }

    /**
     * Constructor for the MS Amanda score.
     *
     * @param score MS Amanda score
     */
    public MsAmandaScore(double score) {
        this.score = score;
    }

    /**
     * Getter for MS Amanda score.
     *
     * @return MS Amanda score
     */
    public double getScore() {
        return score;
    }
}
