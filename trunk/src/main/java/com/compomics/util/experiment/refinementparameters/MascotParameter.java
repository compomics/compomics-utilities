package com.compomics.util.experiment.refinementparameters;

import com.compomics.util.experiment.utils.UrParameter;

/**
 * This class will list all Mascot-specific parameters.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 11:43:28 AM
 */
public class MascotParameter implements UrParameter {

    /**
     * the index of the C13 option
     */
    public static final int C13 = 0;
    /**
     * the index of Mascot score
     */
    public static final int SCORE = 1;
    /**
     * The parameter index
     */
    private int index;

    /**
     * Constructor for a parameter
     * @param index     the index as referenced in static fields
     */
    public MascotParameter(int index) {
        this.index = index;
    }

    /**
     * This method returns the family name of the parameter
     *
     * @return family name
     */
    public String getFamilyName() {
        return "Mascot specific parameters";
    }

    /**
     * This method returns the index of the parameter
     *
     * @return the index of the parameter
     */
    public int getIndex() {
        return index;
    }
}
