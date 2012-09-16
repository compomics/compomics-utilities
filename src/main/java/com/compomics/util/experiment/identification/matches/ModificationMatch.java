package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class models the match between theoretic ptm and identification results.
 * <p/>
 * @author Marc Vaudel
 */
public class ModificationMatch extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 7129515983284796207L;
    /**
     * The theoretic modiffication name. The modification can be accessed via
     * the PTM factory.
     */
    private String theoreticPtm;
    /**
     * Is the modification variable?
     */
    private boolean variable;
    /**
     * The location in the peptide sequence, 1 is the first residue.
     */
    private int modifiedSite;

    /**
     * Constructor for a modification match.
     *
     * @param theoreticPtm the theoretic PTM
     * @param variable true for variable modifications, false otherwise
     * @param modifiedSite the position of the modification in the sequence, 1 is the first residue
     */
    public ModificationMatch(String theoreticPtm, boolean variable, int modifiedSite) {
        this.theoreticPtm = theoreticPtm;
        this.variable = variable;
        this.modifiedSite = modifiedSite;
    }

    /**
     * Returns a boolean indicating if the modification is variable.
     *
     * @return a boolean indicating if the modification is variable
     */
    public boolean isVariable() {
        return variable;
    }

    /**
     * Getter for the theoretic PTM name.
     *
     * @return the theoretic PTM name
     */
    public String getTheoreticPtm() {
        return theoreticPtm;
    }

    /**
     * Sets the theoretic PTM.
     *
     * @param ptm the theoretic PTM name
     */
    public void setTheoreticPtm(String ptm) {
        this.theoreticPtm = ptm;
    }

    /**
     * Getter for the modification site, 1 is the first amino acid.
     *
     * @return the index of the modification in the sequence
     */
    public int getModificationSite() {
        return modifiedSite;
    }
}
