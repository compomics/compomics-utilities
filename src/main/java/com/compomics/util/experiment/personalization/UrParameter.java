package com.compomics.util.experiment.personalization;


/**
 * This interface is used to reference refinement parameters.
 * 
 * @author Marc Vaudel
 */
public interface UrParameter {

    /**
     * This method returns the key of the parameter. The key must be unique to the type of parameter.
     * 
     * @return the parameter key
     */
    public long getParameterKey();
}
