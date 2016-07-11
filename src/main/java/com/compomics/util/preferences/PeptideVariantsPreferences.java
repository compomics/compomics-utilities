package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import java.io.Serializable;

/**
 * Preferences for the allowed variants in peptide sequences.
 *
 * @author Marc Vaudel
 */
public class PeptideVariantsPreferences implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -236026128063733907L;
    /**
     * The number of amino acid deletions allowed.
     */
    private Integer nAaDeletions = 0;
    /**
     * The number of amino acid insertions allowed.
     */
    private Integer nAaInsertions = 0;
    /**
     * The number of amino acid substitutions allowed.
     */
    private Integer nAaSubstitutions = 1;
    /**
     * The number of amino acid swap allowed.
     */
    private Integer nAaSwap = 0;
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
     * Returns the number of amino acid deletions allowed.
     * 
     * @return the number of amino acid deletions allowed
     */
    public Integer getnAaDeletions() {
        return nAaDeletions;
    }

    /**
     * Sets the number of amino acid deletions allowed.
     * 
     * @param nAaDeletions the number of amino acid deletions allowed
     */
    public void setnAaDeletions(Integer nAaDeletions) {
        this.nAaDeletions = nAaDeletions;
    }

    /**
     * Returns the number of amino acid insertions allowed.
     * 
     * @return the number of amino acid insertions allowed
     */
    public Integer getnAaInsertions() {
        return nAaInsertions;
    }

    /**
     * Sets the number of amino acid insertions allowed.
     * 
     * @param nAaInsertions the number of amino acid insertions allowed
     */
    public void setnAaInsertions(Integer nAaInsertions) {
System.out.println("set insertions: " + nAaInsertions);
        this.nAaInsertions = nAaInsertions;
    }

    /**
     * Returns the number of amino acid substitutions allowed.
     * 
     * @return the number of amino acid substitutions allowed
     */
    public Integer getnAaSubstitutions() {
        return nAaSubstitutions;
    }

    /**
     * Sets the number of amino acid substitutions allowed.
     * 
     * @param nAaSubstitutions the number of amino acid substitutions allowed
     */
    public void setnAaSubstitutions(Integer nAaSubstitutions) {
        this.nAaSubstitutions = nAaSubstitutions;
    }

    /**
     * Returns the number of amino acid swaps allowed.
     * 
     * @return the number of amino acid swaps allowed
     */
    public Integer getnAaSwap() {
        return nAaSwap;
    }

    /**
     * Sets the number of amino acid swaps allowed.
     * 
     * @param nAaSwap the number of amino acid swaps allowed
     */
    public void setnAaSwap(Integer nAaSwap) {
        this.nAaSwap = nAaSwap;
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
        if (!nAaDeletions.equals(peptideVariantsPreferences.getnAaDeletions())) {
            return false;
        }
        if (!nAaInsertions.equals(peptideVariantsPreferences.getnAaInsertions())) {
            return false;
        }
        if (!nAaSubstitutions.equals(peptideVariantsPreferences.getnAaSubstitutions())) {
            return false;
        }
        if (!nAaSwap.equals(peptideVariantsPreferences.getnAaSwap())) {
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

        output.append("AA Deletions: ").append(nAaDeletions).append(".").append(newLine);
        output.append("AA Insertions: ").append(nAaInsertions).append(".").append(newLine);
        output.append("AA Substitutions: ").append(nAaSubstitutions).append(".").append(newLine);
        output.append("AA Swap: ").append(nAaSwap).append(".").append(newLine);
        output.append("Substitution Matrix: ").append(aaSubstitutionMatrix.toString()).append(".").append(newLine);

        return output.toString();
    }
    
    /**
     * Returns the preferences corresponding to no variants allowed.
     * 
     * @return the preferences corresponding to no variants allowed
     */
    public static PeptideVariantsPreferences getNoVariantPreferences() {
System.out.println("reset");
        PeptideVariantsPreferences peptideVariantsPreferences = new PeptideVariantsPreferences();
        peptideVariantsPreferences.setnAaDeletions(0);
        peptideVariantsPreferences.setnAaInsertions(0);
        peptideVariantsPreferences.setnAaSubstitutions(0);
        peptideVariantsPreferences.setnAaSwap(0);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.noSubstitution);
        return peptideVariantsPreferences;
    }
}
