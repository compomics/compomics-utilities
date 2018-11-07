package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This is an abstract class for an identification match.
 *
 * @author Marc Vaudel
 */
public abstract class IdentificationMatch extends ExperimentObject {

    /**
     * Empty default constructor
     */
    public IdentificationMatch() {
    }

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -9132138792119651421L;

    /**
     * The type of match.
     */
    public enum MatchType {

        Protein,
        Peptide,
        Spectrum,
        Ion,
        PTM
    }

    /**
     * Returns the type of match.
     *
     * @return the type of match
     */
    public abstract MatchType getType();

    /**
     * Returns the key of a match.
     *
     * @return the key of a match
     */
    public abstract long getKey();
}
