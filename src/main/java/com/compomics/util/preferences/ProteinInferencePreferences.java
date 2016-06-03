package com.compomics.util.preferences;

import java.io.File;
import java.io.Serializable;

/**
 * Generic class grouping the protein inference preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ProteinInferencePreferences implements Serializable {

    /**
     * Serial version UID for backward compatibility.
     */
    static final long serialVersionUID = 447785006299636157L;
    /**
     * The database to use for protein inference.
     */
    private File proteinSequenceDatabase;
    /**
     * Simplify protein groups.
     */
    private Boolean simplifyProteinGroups = true;
    /**
     * Simplify groups based on PeptideShaker score.
     */
    private Boolean simplifyGroupsScore = true;
    /**
     * Simplify groups based on UniProt evidence level.
     */
    private Boolean simplifyGroupsEvidence = true;
    /**
     * Simplify groups based on enzymaticity.
     */
    private Boolean simplifyGroupsEnzymaticity = true;
    /**
     * Simplify groups of uncharacterized proteins.
     */
    private Boolean simplifyGroupsUncharacterized = true;

    /**
     * Returns the path to the database used.
     *
     * @return the path to the database used
     */
    public File getProteinSequenceDatabase() {
        return proteinSequenceDatabase;
    }

    /**
     * Sets the path to the database used.
     *
     * @param proteinSequenceDatabase the path to the database used
     */
    public void setProteinSequenceDatabase(File proteinSequenceDatabase) {
        this.proteinSequenceDatabase = proteinSequenceDatabase;
    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {

        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        output.append("DB: ");
        if (proteinSequenceDatabase != null) {
            output.append(proteinSequenceDatabase.getName());
        } else {
            output.append("not set");
        }
        output.append(".").append(newLine);
        output.append("Simplify Groups: ").append(getSimplifyGroups()).append(newLine);
        output.append("Simplify on score: ").append(getSimplifyGroupsScore()).append(newLine);
        output.append("Simplify on enzymaticity: ").append(getSimplifyGroupsEnzymaticity()).append(newLine);
        output.append("Simplify on evidence: ").append(getSimplifyGroupsEvidence()).append(newLine);
        output.append("Simplify uncharacterized: ").append(getSimplifyGroupsUncharacterized()).append(newLine);

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
    public boolean equals(ProteinInferencePreferences otherProteinInferencePreferences) {

        if (otherProteinInferencePreferences == null) {
            return false;
        }

        if ((proteinSequenceDatabase != null && otherProteinInferencePreferences.getProteinSequenceDatabase() == null)
                || (proteinSequenceDatabase == null && otherProteinInferencePreferences.getProteinSequenceDatabase() != null)) {
            return false;
        }

        if (!getSimplifyGroups().equals(otherProteinInferencePreferences.getSimplifyGroups())) {
            return false;
        }

        if (!getSimplifyGroupsScore().equals(otherProteinInferencePreferences.getSimplifyGroupsScore())) {
            return false;
        }

        if (!getSimplifyGroupsEnzymaticity().equals(otherProteinInferencePreferences.getSimplifyGroupsEnzymaticity())) {
            return false;
        }

        if (!getSimplifyGroupsEvidence().equals(otherProteinInferencePreferences.getSimplifyGroupsEvidence())) {
            return false;
        }

        if (!getSimplifyGroupsUncharacterized().equals(otherProteinInferencePreferences.getSimplifyGroupsUncharacterized())) {
            return false;
        }

        if (proteinSequenceDatabase != null && !proteinSequenceDatabase.equals(otherProteinInferencePreferences.getProteinSequenceDatabase())) {
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
    public Boolean getSimplifyGroups() {
        if (simplifyProteinGroups == null) { // Backward compatibility
            simplifyProteinGroups = true;
        }
        return simplifyProteinGroups;
    }

    /**
     * Sets whether the protein groups should be simplified based on the
     * PeptideShaker confidence.
     *
     * @param simplifyProteinGroups whether the protein groups should be
     * simplified
     */
    public void setSimplifyGroups(Boolean simplifyProteinGroups) {
        this.simplifyProteinGroups = simplifyProteinGroups;
    }

    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified based on the PeptideShaker score.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified based on the PeptideShaker score
     */
    public Boolean getSimplifyGroupsScore() {
        if (simplifyGroupsScore == null) { // Backward compatibility
            simplifyGroupsScore = true;
        }
        return simplifyGroupsScore;
    }

    /**
     * Sets whether the protein groups should be simplified based on the
     * PeptideShaker score.
     *
     * @param simplifyGroupsScore whether the protein groups should be
     * simplified based on the PeptideShaker score
     */
    public void setSimplifyGroupsScore(Boolean simplifyGroupsScore) {
        this.simplifyGroupsScore = simplifyGroupsScore;
    }

    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified based on the Uniprot evidence level.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified based on the Uniprot evidence level
     */
    public Boolean getSimplifyGroupsEvidence() {
        if (simplifyGroupsEvidence == null) { // Backward compatibility
            simplifyGroupsEvidence = true;
        }
        return simplifyGroupsEvidence;
    }

    /**
     * Sets whether the protein groups should be simplified based on the Uniprot
     * evidence level.
     *
     * @param simplifyGroupsEvidence whether the protein groups should be
     * simplified based on the Uniprot evidence level
     */
    public void setSimplifyGroupsEvidence(Boolean simplifyGroupsEvidence) {
        this.simplifyGroupsEvidence = simplifyGroupsEvidence;
    }

    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified based on the peptide enzymaticity.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified based on the peptide enzymaticity
     */
    public Boolean getSimplifyGroupsEnzymaticity() {
        if (simplifyGroupsEnzymaticity == null) { // Backward compatibility
            simplifyGroupsEnzymaticity = true;
        }
        return simplifyGroupsEnzymaticity;
    }

    /**
     * Sets whether the protein groups should be simplified based on the peptide
     * enzymaticity.
     *
     * @param simplifyGroupsEnzymaticity whether the protein groups should be
     * simplified based on the peptide enzymaticity
     */
    public void setSimplifyGroupsEnzymaticity(Boolean simplifyGroupsEnzymaticity) {
        this.simplifyGroupsEnzymaticity = simplifyGroupsEnzymaticity;
    }

    /**
     * Returns a boolean indicating whether the protein groups consisting of
     * uncharacterized proteins.
     *
     * @return a boolean indicating whether the protein groups consisting of
     * uncharacterized proteins
     */
    public Boolean getSimplifyGroupsUncharacterized() {
        if (simplifyGroupsUncharacterized == null) { // Backward compatibility
            simplifyGroupsUncharacterized = true;
        }
        return simplifyGroupsUncharacterized;
    }

    /**
     * Sets whether the protein groups consisting of uncharacterized proteins.
     *
     * @param simplifyGroupsUncharacterized whether the protein groups
     * consisting of uncharacterized proteins
     */
    public void setSimplifyGroupsUncharacterized(Boolean simplifyGroupsUncharacterized) {
        this.simplifyGroupsUncharacterized = simplifyGroupsUncharacterized;
    }
}
