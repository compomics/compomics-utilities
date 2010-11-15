package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.utils.ExperimentObject;

/**
 * This class will models a quantification method.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 3:10:48 PM
 */
public class QuantificationMethod extends ExperimentObject {

    /**
     * Index for ITRAQ 4Plex
     */
    public final static int ITRAQ_4PLEX = 0;
    /**
     * Index for ITRAQ 8Plex
     */
    public final static int ITRAQ_8PLEX = 1;
    /**
     * Index for TMT6
     */
    public final static int TMT6 = 2;
    /**
     * Index for TMT2
     */
    public final static int TMT2 = 3;
    /**
     * the method index
     */
    protected int index;

    /**
     * the method name
     */
    protected String name;

    /**
     * getter for the method index
     * @return the method index
     */
    public int getMethodIndex() {
        return index;
    }

    /**
     * This method returns the method name according to the set method index
     * @return method name
     */
    public String getMethodName() {
                return name;
    }
}
