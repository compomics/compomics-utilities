package com.compomics.util.experiment.utils;

/**
 * This interface will be used to reference refined parameters. Utilities Refined
 * parameters are referenced by a family name (for example Peptizer) and an integer
 * indexing the parameter in the family.
 * 
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 11:35:43 AM
 */
public interface UrParameter {
    
    /**
     * This method returns the family name of the parameter
     * @return family name
     */
    public String getFamilyName();

    /**
     * This method returns the index of the parameter
     * @return the index of the parameter
     */
    public int getIndex();
}
