package com.compomics.util.experiment.identification;

import java.io.Serializable;

/**
 * The advocate of a hit can be a search engine, a rescoring algorithm, etc.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 1:55:22 PM
 */
public interface Advocate extends Serializable {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -9081265337103997591L;
    /**
     * Mascot index
     */
    public static final int MASCOT = 0;
    /**
     * OMSSA index
     */
    public static final int OMSSA = 1;
    /**
     * X!Tandem index
     */
    public static final int XTANDEM = 2;
    /**
     * Peptizer index
     */
    public static final int PEPTIZER = 3;
    /**
     * Andromeda index
     */
    public static final int ANDROMEDA = 4;
    /**
     * PeptideShaker
     */
    public static final int PEPTIDE_SHAKER = 5;

    /**
     * getter for the name of the Advocate
     *
     * @return the name of the advocate
     */
    public String getName();

    /**
     * getter for the index of the advocate
     * 
     * @return the index of the advocate
     */
    public int getId();
}
