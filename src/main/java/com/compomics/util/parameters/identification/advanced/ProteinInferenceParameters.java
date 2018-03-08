package com.compomics.util.parameters.identification.advanced;

import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import java.io.File;
import java.io.Serializable;

/**
 * Generic class grouping the protein inference preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ProteinInferenceParameters implements Serializable {

    /**
     * Serial version UID for backward compatibility.
     */
    static final long serialVersionUID = 447785006299636157L;
    /**
     * The parameters used to parse the fasta file.
     */
    private FastaParameters fastaParameters = null;
    /**
     * Simplify protein groups.
     */
    private boolean simplifyProteinGroups = true;
    /**
     * Simplify groups based on UniProt evidence level.
     */
    private boolean simplifyGroupsEvidence = true;
    /**
     * Simplify groups of uncharacterized proteins.
     */
    private boolean simplifyGroupsUncharacterized = true;
    /**
     * Simplify groups based on enzymaticity.
     */
    private boolean simplifyGroupsEnzymaticity = true;
    /**
     * Simplify groups based on variant matching.
     */
    private boolean simplifyGroupsVariants = true;

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {

        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        output.append("DB: ");
        output.append("Simplify Groups: ").append(getSimplifyGroups()).append(newLine);
        output.append("Simplify on evidence: ").append(getSimplifyGroupsEvidence()).append(newLine);
        output.append("Simplify uncharacterized: ").append(getSimplifyGroupsUncharacterized()).append(newLine);
        output.append("Simplify on enzymaticity: ").append(getSimplifyGroupsEnzymaticity()).append(newLine);
        output.append("Simplify on variants: ").append(getSimplifyGroupsEnzymaticity()).append(newLine);

        return output.toString();
    }

    /**
     * Returns true if the objects have identical settings.
     *
     * @param otherProteinInferencePreferences the ProteinInferencePreferences
     * to compare to
     *
     * @return true if the objects have identical settings
     */
    public boolean equals(ProteinInferenceParameters otherProteinInferencePreferences) {

        if (otherProteinInferencePreferences == null) {
            return false;
        }

        if (getSimplifyGroups() != otherProteinInferencePreferences.getSimplifyGroups()) {
            return false;
        }

        if (getSimplifyGroupsEvidence() != otherProteinInferencePreferences.getSimplifyGroupsEvidence()) {
            return false;
        }

        if (getSimplifyGroupsUncharacterized() != otherProteinInferencePreferences.getSimplifyGroupsUncharacterized()) {
            return false;
        }

        if (getSimplifyGroupsEnzymaticity() != otherProteinInferencePreferences.getSimplifyGroupsEnzymaticity()) {
            return false;
        }

        if (getSimplifyGroupsVariants()!= otherProteinInferencePreferences.getSimplifyGroupsVariants()) {
            return false;
        }

        return true;
    }

    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified
     */
    public boolean getSimplifyGroups() {
        return simplifyProteinGroups;
    }

    /**
     * Sets whether the protein groups should be simplified based on the
     * PeptideShaker confidence.
     *
     * @param simplifyProteinGroups whether the protein groups should be
     * simplified
     */
    public void setSimplifyGroups(boolean simplifyProteinGroups) {
        this.simplifyProteinGroups = simplifyProteinGroups;
    }

    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified based on the Uniprot evidence level.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified based on the Uniprot evidence level
     */
    public boolean getSimplifyGroupsEvidence() {
        return simplifyGroupsEvidence;
    }

    /**
     * Sets whether the protein groups should be simplified based on the Uniprot
     * evidence level.
     *
     * @param simplifyGroupsEvidence whether the protein groups should be
     * simplified based on the Uniprot evidence level
     */
    public void setSimplifyGroupsEvidence(boolean simplifyGroupsEvidence) {
        this.simplifyGroupsEvidence = simplifyGroupsEvidence;
    }

    /**
     * Returns a boolean indicating whether the protein groups consisting of
     * uncharacterized proteins.
     *
     * @return a boolean indicating whether the protein groups consisting of
     * uncharacterized proteins
     */
    public boolean getSimplifyGroupsUncharacterized() {
        return simplifyGroupsUncharacterized;
    }

    /**
     * Sets whether the protein groups consisting of uncharacterized proteins.
     *
     * @param simplifyGroupsUncharacterized whether the protein groups
     * consisting of uncharacterized proteins
     */
    public void setSimplifyGroupsUncharacterized(boolean simplifyGroupsUncharacterized) {
        this.simplifyGroupsUncharacterized = simplifyGroupsUncharacterized;
    }

    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified based on the peptide enzymaticity.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified based on the peptide enzymaticity
     */
    public boolean getSimplifyGroupsEnzymaticity() {
        return simplifyGroupsEnzymaticity;
    }

    /**
     * Sets whether the protein groups should be simplified based on the peptide
     * enzymaticity.
     *
     * @param simplifyGroupsEnzymaticity whether the protein groups should be
     * simplified based on the peptide enzymaticity
     */
    public void setSimplifyGroupsEnzymaticity(boolean simplifyGroupsEnzymaticity) {
        this.simplifyGroupsEnzymaticity = simplifyGroupsEnzymaticity;
    }

    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified based on the peptide variant matching.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified based on the peptide variant matching
     */
    public boolean getSimplifyGroupsVariants() {
        return simplifyGroupsVariants;
    }

    /**
     * Sets whether the protein groups should be simplified based on the peptide
     * variant matching.
     *
     * @param simplifyGroupsVariants whether the protein groups should be
     * simplified based on the peptide variant matching
     */
    public void setSimplifyGroupsVariants(boolean simplifyGroupsVariants) {
        this.simplifyGroupsVariants = simplifyGroupsVariants;
    }

    /**
     * Returns the parameters to use to parse the fasta file.
     * 
     * @return the parameters to use to parse the fasta file
     */
    public FastaParameters getFastaParameters() {
        return fastaParameters;
    }

    /**
     * Sets the parameters to use to parse the fasta file.
     * 
     * @param fastaParameters the parameters to use to parse the fasta file
     */
    public void setFastaParameters(FastaParameters fastaParameters) {
        this.fastaParameters = fastaParameters;
    }
}
