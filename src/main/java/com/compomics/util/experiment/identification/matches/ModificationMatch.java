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
     * The location in the sequence, 1 is the first residue.
     * 
     * @deprecated use the index in the AminoAcidPattern instead
     */
    private int modifiedSite;
    /**
     * A boolean indicating whether the modification is confidently localized
     * onto the sequence. Not applicable to fixed or terminal modifications.
     */
    private boolean confident = false;
    /**
     * A boolean indicating whether the modification is inferred from another
     * peptide. Not applicable to fixed or terminal modifications.
     */
    private boolean inferred = false;

    /**
     * Constructor for a modification match.
     *
     * @param theoreticPtm the theoretic PTM
     * @param variable true for variable modifications, false otherwise
     * @param modifiedSite the position of the modification in the sequence, 1
     * is the first residue
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
     * @deprecated use the index in the AminoAcidPattern instead
     * @return the index of the modification in the sequence
     */
    public int getModificationSite() {
        return modifiedSite;
    }

    /**
     * Setter for the modification site, 1 is the first amino acid.
     *
     * @deprecated use the index in the AminoAcidPattern instead
     * @param site the index of the modification in the sequence
     */
    public void setModificationSite(int site) {
        this.modifiedSite = site;
    }

    /**
     * Returns a boolean indicating whether the modification is confidently
     * localized on the sequence.
     *
     * @return a boolean indicating whether the modification is confidently
     * localized on the sequence
     */
    public boolean isConfident() {
        return confident;
    }

    /**
     * Sets whether the modification is confidently localized on the sequence.
     *
     * @param confident a boolean indicating whether the modification is
     * confidently localized on the sequence
     */
    public void setConfident(boolean confident) {
        this.confident = confident;
    }

    /**
     * Returns a boolean indicating whether the modification is inferred from
     * another peptide.
     *
     * @return a boolean indicating whether the modification is inferred from
     * another peptide
     */
    public boolean isInferred() {
        return inferred;
    }

    /**
     * Sets whether the modification is inferred from another peptide.
     *
     * @param inferred a boolean indicating whether the modification is
     * inferred from another peptide
     */
    public void setInferred(boolean inferred) {
        this.inferred = inferred;
    }
    
    /**
     * Indicates whether this modification match is the same of another one. The match is only compared based on the theoretic PTM and the variability. The localization and its confidence is not taken into account.
     * 
     * @param anotherModificationMatch another modification match
     * 
     * @return a boolean indicating whether both modification matches are the same.
     */
    public boolean isSameAs(ModificationMatch anotherModificationMatch) {
        if (!theoreticPtm.equals(anotherModificationMatch.getTheoreticPtm())) {
            return false;
        }
        if (variable != anotherModificationMatch.isVariable()) {
            return false;
        }
        return true;
    }
}
