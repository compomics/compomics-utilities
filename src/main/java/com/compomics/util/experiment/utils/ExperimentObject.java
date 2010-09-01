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
     * @TODO: JavaDoc Missing
     */
    private HashMap<String, Object> urParams = new HashMap<String, Object>();

    /**
     * @TODO: JavaDoc Missing
     *
     * @param parameter
     * @param value
     */
    public void addUrParam(UrParameter parameter, Object value) {
        urParams.put(parameter.getFamilyName() + "_" + parameter.getIndex(), value);
    }

    /**
     * @TODO: JavaDoc Missing
     *
     * @param parameter
     * @return
     */
    public Object getUrParam(UrParameter parameter) {
        return urParams.get(parameter.getFamilyName() + "_" + parameter.getIndex());
    }
}
