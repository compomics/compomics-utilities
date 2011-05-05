package com.compomics.util.experiment.personalization;

import java.util.HashMap;
import java.io.Serializable;

/**
 * This abstract class provides customization facilities. Tools-dependant 
 * parameters can be added to classes extending this class.
 * 
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 11:59:18 AM
 */
public abstract class ExperimentObject implements Serializable {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 1929697552061121072L;
    /**
     * Map containing user refinement parameters
     */
    private HashMap<String, UrParameter> urParams = new HashMap<String, UrParameter>();

    /**
     * Method to add a user refinement parameter
     *
     * @param parameter The parameter
     */
    public void addUrParam(UrParameter parameter) {
        urParams.put(parameter.getFamilyName() + "_" + parameter.getIndex(), parameter);
    }

    /**
     * Method which returns the refinement parameter
     *
     * @param parameter the desired parameter
     * @return          the value stored. Null if not found.
     */
    public UrParameter getUrParam(UrParameter parameter) {
        return urParams.get(parameter.getFamilyName() + "_" + parameter.getIndex());
    }
}
