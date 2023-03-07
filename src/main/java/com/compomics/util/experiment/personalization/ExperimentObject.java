package com.compomics.util.experiment.personalization;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This abstract class provides customization facilities. Tool-dependent
 * parameters can be added to classes extending this class.
 *
 * @author Marc Vaudel
 */
public abstract class ExperimentObject implements Serializable {

    /**
     * Unique identifier.
     */
    private int id;

    /**
     * Empty default constructor.
     */
    public ExperimentObject() {
    }

    /**
     * Returns the id of the object.
     *
     * @return the id of the object
     */
    public int getId() {

        return id;

    }

    /**
     * Sets the id of the object.
     *
     * @param id the id of the object
     */
    public void setId(int id) {

        this.id = id;

    }
    
    /**
     * Value for a key not set.
     */
    public static final int NO_KEY = getHash("#!#_NO_KEY_#!#");
    /**
     * Map containing user refinement parameters.
     */
    private HashMap<Integer, UrParameter> urParams = null;

    /**
     * Adds a user refinement parameter.
     *
     * @param parameter the parameter
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
    public void removeUrParam(int paramterKey) {

        if (urParams != null) {

            urParams.remove(paramterKey);

        }
    }

    /**
     * Creates the parameters map unless done by another thread already.
     */
    private synchronized void createParamsMap() {

        if (urParams == null) {

            urParams = new HashMap<>(1);

        }
    }

    /**
     * Returns the refinement parameter of the same type than the one provided.
     * Null if not found.
     *
     * @param parameter the desired parameter
     *
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

    /**
     * Sets the user parameters map.
     *
     * @param urParams the user parameters map
     */
    public void setUrParams(HashMap<Integer, UrParameter> urParams) {

        this.urParams = urParams;
    }

    /**
     * Returns the user parameters map.
     *
     * @return the user parameters map
     */
    public HashMap<Integer, UrParameter> getUrParams() {

        return urParams;
    }

    /**
     * Convenience method returning the hash of a key.
     *
     * @param key the original key
     *
     * @return the hashed key
     */
    public static int getHash(String key) {
        
        return key.hashCode();

    }
}
