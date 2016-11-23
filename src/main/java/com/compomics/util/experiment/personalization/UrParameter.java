package com.compomics.util.experiment.personalization;

import java.io.Serializable;

/**
 * This interface is used to reference refinement parameters.
 * 
 * @author Marc Vaudel
 */
public interface UrParameter extends Serializable {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 6808590175195298797L;

    /**
     * This method returns the key of the paramter. The key must be unique to the type of parameter.
     * 
     * @return the parameter key
     */
    public String getParameterKey();
}
