/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This is an abstract class for an identification match
 *
 * @author marc
 */
public abstract class IdentificationMatch extends ExperimentObject {

    /**
     * Serial number for backward compatibility
     */
    static final long serialVersionUID = -9132138792119651421L;

    /**
     * the type of match
     */
    public enum MatchType {

        Protein,
        Peptide,
        Spectrum,
        Ion,
        PTM
    }

    /**
     * Returns the type of match
     * @return the type of match
     */
    public abstract MatchType getType();

    /**
     * returns the key of a match
     * @return the key of a match
     */
    public abstract String getKey();
}
