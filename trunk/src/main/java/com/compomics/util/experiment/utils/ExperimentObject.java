package com.compomics.util.experiment.utils;

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
     * Map containing user refinement parameters
     */
    private HashMap<String, Object> urParams = new HashMap<String, Object>();

    /**
     * Method to add a user refinement parameter
     *
     * @param parameter The parameter
     * @param value     The associated value
     */
    public void addUrParam(UrParameter parameter, Object value) {
        urParams.put(parameter.getFamilyName() + "_" + parameter.getIndex(), value);
    }

    /**
     * Method which returns the refinement parameter
     *
     * @param parameter the desired parameter
     * @return          the value stored. Null if not found.
     */
    public Object getUrParam(UrParameter parameter) {
        return urParams.get(parameter.getFamilyName() + "_" + parameter.getIndex());
    }
}
