package com.compomics.util.experiment.refinementparameters;

import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This parameter flags the use of the C13 option in Mascot.
 * User: Marc
 * Date: Nov 12, 2010
 * Time: 6:36:39 PM
 */
public class C13 implements UrParameter {

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
        return 0;
    }

    /**
     * boolean indicating that a 1 dalton shift was found between the spectrum precursor and Mascot identification.
     */
    private boolean c13;

    /**
     * Constructor for C13
     * @param c13   boolean indicating whether the C13 option was used.
     */
    public C13(boolean c13) {
        this.c13 = c13;
    }

    /**
     * Getter for C13.
     * @return a boolean indicating whether the C13 option was used
     */
    public boolean isC13() {
        return c13;
    }
}
