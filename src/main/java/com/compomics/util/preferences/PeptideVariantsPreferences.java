package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;

/**
 * Preferences for the allowed variants in peptide sequences.
 *
 * @author Marc Vaudel
 */
public class PeptideVariantsPreferences {

    /**
     * The number of sequence edits allowed.
     */
    private Integer nEdits = 1;
    
    /**
     * The amino acid substitution matrix selected.
     */
    private AaSubstitutionMatrix aaSubstitutionMatrix = AaSubstitutionMatrix.noSubstitution;
    
    /**
     * Constructor.
     */
    public PeptideVariantsPreferences() {
        
    }

    /**
     * Returns the number of sequence edits allowed.
     * 
     * @return the number of sequence edits allowed
     */
    public Integer getnEdits() {
        return nEdits;
    }

    /**
     * Sets the number of sequence edits allowed.
     * 
     * @param nEdits the number of sequence edits allowed
     */
    public void setnEdits(Integer nEdits) {
        this.nEdits = nEdits;
    }

    /**
     * Returns the amino acid substitution matrix to use.
     * 
     * @return the amino acid substitution matrix to use
     */
    public AaSubstitutionMatrix getAaSubstitutionMatrix() {
        return aaSubstitutionMatrix;
    }

    /**
     * Sets the amino acid substitution matrix to use.
     * 
     * @param aaSubstitutionMatrix the amino acid substitution matrix to use
     */
    public void setAaSubstitutionMatrix(AaSubstitutionMatrix aaSubstitutionMatrix) {
        this.aaSubstitutionMatrix = aaSubstitutionMatrix;
    }

    /**
     * Indicates whether another peptide variant preferences is the same as
     * this one.
     *
     * @param peptideVariantsPreferences the other preferences
     *
     * @return whether another peptide variant preferences is the same as this
     * one
     */
    public boolean isSameAs(PeptideVariantsPreferences peptideVariantsPreferences) {
        if (!nEdits.equals(peptideVariantsPreferences.getnEdits())) {
            return false;
        }
        return aaSubstitutionMatrix.isSameAs(peptideVariantsPreferences.getAaSubstitutionMatrix());
    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {

        String newLine = System.getProperty("line.separator");

        StringBuilder output = new StringBuilder();

        output.append("#Edits: ").append(nEdits).append(".").append(newLine);
        output.append("Substitution Matrix: ").append(aaSubstitutionMatrix.toString()).append(".").append(newLine);

        return output.toString();
    }
    
    /**
     * Returns the preferences corresponding to no variants allowed.
     * 
     * @return the preferences corresponding to no variants allowed
     */
    public static PeptideVariantsPreferences getNoVariantPreferences() {
        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setnEdits(0);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.noSubstitution);
        return peptideVariantsPreferences;
    }
    
}
