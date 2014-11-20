package com.compomics.util.experiment.massspectrometry;

import java.io.Serializable;

/**
 * Enum for the different fragmentation methods.
 *
 * @author Marc Vaudel
 */
public enum FragmentationMethod implements Serializable {

    CID("CID"), HCD("HCD"), ETD("ETD");

    /**
     * The name of the fragmentation method.
     */
    public final String name;

    /**
     * Constructor.
     *
     * @param name the name of the fragmentation method
     */
    private FragmentationMethod(String name) {
        this.name = name;
    }
}
