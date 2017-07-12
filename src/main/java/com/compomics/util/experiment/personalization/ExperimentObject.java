package com.compomics.util.experiment.personalization;

import com.compomics.util.IdObject;
import java.util.HashMap;
import java.io.Serializable;

/**
 * This abstract class provides customization facilities. Tool dependent
 * parameters can be added to classes extending this class.
 *
 * @author Marc Vaudel
 */
public abstract class ExperimentObject extends IdObject implements Serializable, Cloneable {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 1929697552061121072L;
    /**
     * Map containing user refinement parameters.
     */
    private HashMap<String, UrParameter> urParams = null;

    /**
     * Adds a user refinement parameter.
     *
     * @param parameter The parameter
     */
    public void addUrParam(UrParameter parameter) {
        if (urParams == null) {
            createParamsMap();
        }
        urParams.put(parameter.getParameterKey(), parameter);
    }
    
    /**
     * Removes a user parameter from the user parameters map.
     * 
     * @param paramterKey the key of the parameter
     */
    public void removeUrParam(String paramterKey) {
        if (urParams != null) {
            urParams.remove(paramterKey);
        }
    }
    
    /**
     * Creates the parameters map unless done by another thread already.
     */
    private synchronized void createParamsMap() {
        if (urParams == null) {
            urParams = new HashMap<String, UrParameter>(1);
        }
    }

    /**
     * Returns the refinement parameter of the same type than the one provided. Null if not found.
     *
     * @param parameter the desired parameter
     * @return the value stored. Null if not found.
     */
    public UrParameter getUrParam(UrParameter parameter) {
        if (urParams == null) {
            return null;
        }
        return urParams.get(parameter.getParameterKey());
    }
    
    /**
     * Clears the loaded parameters.
     */
    public void clearParametersMap() {
        urParams = null;
    }
}
