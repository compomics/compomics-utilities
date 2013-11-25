package com.compomics.util.experiment.identification;

import java.io.Serializable;

/**
 * The advocate of a hit can be a search engine, a re-scoring algorithm, etc.
 *
 * @author Marc Vaudel
 */
public interface Advocate extends Serializable {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -9081265337103997591L;
    /**
     * Mascot index.
     */
    public static final int MASCOT = 0;
    /**
     * OMSSA index.
     */
    public static final int OMSSA = 1;
    /**
     * X!Tandem index.
     */
    public static final int XTANDEM = 2;
    /**
     * Peptizer index.
     */
    public static final int PEPTIZER = 3;
    /**
     * Andromeda index.
     */
    public static final int ANDROMEDA = 4;
    /**
     * PeptideShaker.
     */
    public static final int PEPTIDE_SHAKER = 5;
    /**
     * PepNovo
     */
    public static final int PEPNOVO = 6;
    /**
     * DeNovoGUI
     */
    public static final int DENOVOGUI = 7;

    /**
     * Getter for the name of the Advocate.
     *
     * @return the name of the advocate
     */
    public String getName();

    /**
     * Getter for the index of the advocate.
     *
     * @return the index of the advocate
     */
    public int getId();

}
