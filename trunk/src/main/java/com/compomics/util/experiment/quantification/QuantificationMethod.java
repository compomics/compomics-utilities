package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.utils.ExperimentObject;

/**
 * This class will modelize a quantification method.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 3:10:48 PM
 */
public abstract class QuantificationMethod extends ExperimentObject {

    /**
     * Index for ITRAQ 4Plex
     */
    public final static int ITRAQ_4PLEX = 0;
    /**
     * Index for ITRAQ 8Plex
     */
    public final static int ITRAQ_8PLEX = 1;
    /**
     * Index for TMT
     */
    public final static int TMT = 2;

    /**
     * the method index
     */
    protected int index;

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
        switch(index) {
            case ITRAQ_4PLEX:
                return "iTRAQ 4Plex";
            case ITRAQ_8PLEX:
                return "iTRAQ 8Plex";
            case TMT:
                return "TMT";
            default:
                return "unknown";
        }
    }
}
