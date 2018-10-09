package com.compomics.util.parameters.identification.advanced;

import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;

/**
 * Preferences for the allowed variants in peptide sequences.
 *
 * @author Marc Vaudel
 */
public class PeptideVariantsParameters {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -236026128063733907L;
    /**
     * Boolean indicating whether specific variant counts should be used.
     */
    private boolean useSpecificCount = false;
    /**
     * Total number of variants allowed per peptide.
     */
    private int nVariants = 0;
    /**
     * The number of amino acid deletions allowed.
     */
    private int nAaDeletions = 0;
    /**
     * The number of amino acid insertions allowed.
     */
    private int nAaInsertions = 0;
    /**
     * The number of amino acid substitutions allowed.
     */
    private int nAaSubstitutions = 0;
    /**
     * The number of amino acid swap allowed.
     */
    private int nAaSwap = 0;
    /**
     * The amino acid substitution matrix selected.
     */
    private AaSubstitutionMatrix aaSubstitutionMatrix = AaSubstitutionMatrix.noSubstitution;

    /**
     * Constructor.
     */
    public PeptideVariantsParameters() {

    }

    /**
     * Returns the number of amino acid deletions allowed.
     *
     * @return the number of amino acid deletions allowed
     */
    public int getnAaDeletions() {
        return nAaDeletions;
    }

    /**
     * Sets the number of amino acid deletions allowed.
     *
     * @param nAaDeletions the number of amino acid deletions allowed
     */
    public void setnAaDeletions(int nAaDeletions) {
        this.nAaDeletions = nAaDeletions;
    }

    /**
     * Returns the number of amino acid insertions allowed.
     *
     * @return the number of amino acid insertions allowed
     */
    public int getnAaInsertions() {
        return nAaInsertions;
    }

    /**
     * Sets the number of amino acid insertions allowed.
     *
     * @param nAaInsertions the number of amino acid insertions allowed
     */
    public void setnAaInsertions(int nAaInsertions) {
        this.nAaInsertions = nAaInsertions;
    }

    /**
     * Returns the number of amino acid substitutions allowed.
     *
     * @return the number of amino acid substitutions allowed
     */
    public int getnAaSubstitutions() {
        return nAaSubstitutions;
    }

    /**
     * Sets the number of amino acid substitutions allowed.
     *
     * @param nAaSubstitutions the number of amino acid substitutions allowed
     */
    public void setnAaSubstitutions(int nAaSubstitutions) {
        this.nAaSubstitutions = nAaSubstitutions;
    }

    /**
     * Returns the number of amino acid swaps allowed.
     *
     * @return the number of amino acid swaps allowed
     */
    public int getnAaSwap() {
        return nAaSwap;
    }

    /**
     * Sets the number of amino acid swaps allowed.
     *
     * @param nAaSwap the number of amino acid swaps allowed
     */
    public void setnAaSwap(int nAaSwap) {
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
     * Returns whether the specific variant count limitations should be used.
     *
     * @return a boolean indicating whether the specific variant count
     * limitations should be used
     */
    public boolean getUseSpecificCount() {
        return useSpecificCount;
    }

    /**
     * Sets whether the specific variant count limitations should be used.
     *
     * @param useSpecificCount a boolean indicating whether the specific variant
     * count limitations should be used
     */
    public void setUseSpecificCount(boolean useSpecificCount) {
        this.useSpecificCount = useSpecificCount;
    }

    /**
     * Returns the total number of variants allowed.
     *
     * @return the total number of variants allowed
     */
    public int getnVariants() {
        return nVariants;
    }

    /**
     * Sets the total number of variants allowed.
     *
     * @param nVariants the total number of variants allowed
     */
    public void setnVariants(int nVariants) {
        this.nVariants = nVariants;
    }

    /**
     * Indicates whether another peptide variant preferences is the same as this
     * one.
     *
     * @param peptideVariantsPreferences the other preferences
     *
     * @return whether another peptide variant preferences is the same as this
     * one
     */
    public boolean isSameAs(PeptideVariantsParameters peptideVariantsPreferences) {
        if (nAaDeletions != peptideVariantsPreferences.getnAaDeletions()) {
            return false;
        }
        if (nAaInsertions != peptideVariantsPreferences.getnAaInsertions()) {
            return false;
        }
        if (nAaSubstitutions != peptideVariantsPreferences.getnAaSubstitutions()) {
            return false;
        }
        if (nAaSwap != peptideVariantsPreferences.getnAaSwap()) {
            return false;
        }
        if (useSpecificCount != peptideVariantsPreferences.getUseSpecificCount()) {
            return false;
        }
        if (nVariants != peptideVariantsPreferences.getnVariants()) {
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
        output.append("#Variants: ").append(nVariants).append(".").append(newLine);

        if (useSpecificCount) {
            output.append("AA Deletions: ").append(nAaDeletions).append(".").append(newLine);
            output.append("AA Insertions: ").append(nAaInsertions).append(".").append(newLine);
            output.append("AA Substitutions: ").append(nAaSubstitutions).append(".").append(newLine);
            output.append("AA Swap: ").append(nAaSwap).append(".").append(newLine);
            output.append("Substitution Matrix: ").append(aaSubstitutionMatrix.toString()).append(".").append(newLine);
        }

        return output.toString();
    }

    /**
     * Returns the preferences corresponding to no variants allowed.
     *
     * @return the preferences corresponding to no variants allowed
     */
    public static PeptideVariantsParameters getNoVariantPreferences() {
        PeptideVariantsParameters peptideVariantsPreferences = new PeptideVariantsParameters();
        peptideVariantsPreferences.setnVariants(0);
        peptideVariantsPreferences.setUseSpecificCount(false);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.noSubstitution);
        return peptideVariantsPreferences;
    }
}
