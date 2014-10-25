package com.compomics.util.experiment.personalization;

import java.util.HashMap;
import java.io.Serializable;

/**
 * This abstract class provides customization facilities. Tool dependent
 * parameters can be added to classes extending this class.
 *
 * @author Marc Vaudel
 */
public abstract class ExperimentObject implements Serializable, Cloneable {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 1929697552061121072L;
    /**
     * Map containing user refinement parameters.
     */
    private HashMap<String, UrParameter> urParams = new HashMap<String, UrParameter>(0);

    /**
     * Method to add a user refinement parameter.
     *
     * @param parameter The parameter
     */
    public void addUrParam(UrParameter parameter) {
        urParams.put(getParameterKey(parameter), parameter);
    }

    /**
     * Method which returns the refinement parameter.
     *
     * @param parameter the desired parameter
     * @return the value stored. Null if not found.
     */
    public UrParameter getUrParam(UrParameter parameter) {
        return urParams.get(getParameterKey(parameter));
    }

    /**
     * Returns the key of a personalization parameter.
     *
     * @param parameter the desired parameter
     * @return the corresponding Key
     */
    public static String getParameterKey(UrParameter parameter) {
        return parameter.getFamilyName() + "|" + parameter.getIndex();
    }
}
