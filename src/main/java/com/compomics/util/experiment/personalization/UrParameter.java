package com.compomics.util.experiment.personalization;

import java.io.Serializable;

/**
 * This interface will be used to reference refined parameters. Utilities Refined
 * parameters are referenced by a family name (for example Peptizer) and an integer
 * indexing the parameter in the family.
 * 
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 11:35:43 AM
 */
public interface UrParameter extends Serializable {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 6808590175195298797L;

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
