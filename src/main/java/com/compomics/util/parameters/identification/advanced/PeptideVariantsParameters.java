package com.compomics.util.parameters.identification.advanced;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import com.compomics.util.experiment.identification.protein_inference.fm_index.SNPElement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Preferences for the allowed variants in peptide sequences.
 *
 * @author Marc Vaudel
 */
public class PeptideVariantsParameters extends DbObject {

    /** 
     * Enum indicating all three variant types
     */
    public static enum VariantType {NO_VARIANT, GENERIC, SPECIFIC, FIXED};
    
    /**
     * Boolean indicating whether specific variant counts should be used.
     */
    private VariantType variantType = VariantType.NO_VARIANT;
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
     * SNP positions for fixed variants
     */
    private HashMap<String, ArrayList<SNPElement>> fixedVariants = new HashMap<>();

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
        readDBMode();
        return nAaDeletions;
    }

    /**
     * Sets the number of amino acid deletions allowed.
     *
     * @param nAaDeletions the number of amino acid deletions allowed
     */
    public void setnAaDeletions(int nAaDeletions) {
        writeDBMode();
        this.nAaDeletions = nAaDeletions;
    }

    /**
     * Returns the number of amino acid insertions allowed.
     *
     * @return the number of amino acid insertions allowed
     */
    public int getnAaInsertions() {
        readDBMode();
        return nAaInsertions;
    }

    /**
     * Sets the number of amino acid insertions allowed.
     *
     * @param nAaInsertions the number of amino acid insertions allowed
     */
    public void setnAaInsertions(int nAaInsertions) {
        writeDBMode();
        this.nAaInsertions = nAaInsertions;
    }

    /**
     * Returns the number of amino acid substitutions allowed.
     *
     * @return the number of amino acid substitutions allowed
     */
    public int getnAaSubstitutions() {
        readDBMode();
        return nAaSubstitutions;
    }

    /**
     * Sets the number of amino acid substitutions allowed.
     *
     * @param nAaSubstitutions the number of amino acid substitutions allowed
     */
    public void setnAaSubstitutions(int nAaSubstitutions) {
        writeDBMode();
        this.nAaSubstitutions = nAaSubstitutions;
    }

    /**
     * Returns the number of amino acid swaps allowed.
     *
     * @return the number of amino acid swaps allowed
     */
    public int getnAaSwap() {
        readDBMode();
        return nAaSwap;
    }

    /**
     * Sets the number of amino acid swaps allowed.
     *
     * @param nAaSwap the number of amino acid swaps allowed
     */
    public void setnAaSwap(int nAaSwap) {
        writeDBMode();
        this.nAaSwap = nAaSwap;
    }

    /**
     * Returns the amino acid substitution matrix to use.
     *
     * @return the amino acid substitution matrix to use
     */
    public AaSubstitutionMatrix getAaSubstitutionMatrix() {
        readDBMode();
        return aaSubstitutionMatrix;
    }

    /**
     * Sets the amino acid substitution matrix to use.
     *
     * @param aaSubstitutionMatrix the amino acid substitution matrix to use
     */
    public void setAaSubstitutionMatrix(AaSubstitutionMatrix aaSubstitutionMatrix) {
        writeDBMode();
        this.aaSubstitutionMatrix = aaSubstitutionMatrix;
    }

    /**
     * Returns whether the specific variant count limitations should be used.
     *
     * @return a boolean indicating whether the specific variant count
     * limitations should be used
     */
    public VariantType getVariantType() {
        readDBMode();
        return variantType;
    }

    /**
     * Sets whether the specific variant count limitations should be used.
     *
     * @param variantType a boolean indicating whether the specific variant
     * count limitations should be used
     */
    public void setVatiantType(VariantType variantType) {
        writeDBMode();
        this.variantType = variantType;
    }

    /**
     * Returns the total number of variants allowed.
     *
     * @return the total number of variants allowed
     */
    public int getnVariants() {
        readDBMode();
        return nVariants;
    }

    /**
     * Sets the total number of variants allowed.
     *
     * @param nVariants the total number of variants allowed
     */
    public void setnVariants(int nVariants) {
        writeDBMode();
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
        readDBMode();
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
        if (variantType != peptideVariantsPreferences.getVariantType()) {
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
        readDBMode();

        String newLine = System.getProperty("line.separator");

        StringBuilder output = new StringBuilder();
        output.append("#Variants: ").append(nVariants).append(".").append(newLine);

        if (variantType == VariantType.SPECIFIC) {
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
        peptideVariantsPreferences.setVatiantType(VariantType.NO_VARIANT);
        peptideVariantsPreferences.setAaSubstitutionMatrix(AaSubstitutionMatrix.noSubstitution);
        return peptideVariantsPreferences;
    }
    
    
    
    /**
     * returns the fixed variants
     * @return fixed variants dictionary
     */
    public HashMap<String, ArrayList<SNPElement>> getFixedVariants(){
        return fixedVariants;
    }
    
    /**
     * sets the fixed variants
     * @param fixedVariants fixed variants dictionary
     */
    public void setFixedVariants(HashMap<String, ArrayList<SNPElement>> fixedVariants){
        this.fixedVariants = fixedVariants;
    }
}
