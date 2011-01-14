package com.compomics.util.experiment.refinementparameters;

import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This class will contain the mascot score.
 * User: Marc
 * Date: Nov 12, 2010
 * Time: 6:36:48 PM
 */
public class MascotScore implements UrParameter {

    /**
     * This method returns the family name of the parameter
     *
     * @return family name
     */
    public String getFamilyName() {
        return "Utilities Mascot Refinement Parameters";
    }

    /**
     * This method returns the index of the parameter
     *
     * @return the index of the parameter
     */
    public int getIndex() {
        return 1;
    }

    /**
     * The Mascot score
     */
    private double score;

    /**
     * Contructor for the Mascot score
     * @param score Mascot score
     */
    public MascotScore(double score) {
        this.score = score;
    }

    /**
     * Getter for Mascot score
     * @return Mascot score
     */
    public double getScore() {
        return score;
    }
}
