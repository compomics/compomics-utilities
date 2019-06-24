package com.compomics.util.parameters.identification.advanced;

import com.compomics.util.db.object.DbObject;


/**
 * Generic class grouping the protein inference preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ProteinInferenceParameters extends DbObject {

    /**
     * Empty default constructor
     */
    public ProteinInferenceParameters() {
    }

    /**
     * Refine based on terminal and pattern modifications.
     */
    private boolean modificationRefinement = true;
    /**
     * Simplify protein groups.
     */
    private boolean simplifyProteinGroups = true;
    /**
     * Simplify groups based on UniProt evidence level.
     */
    private boolean simplifyGroupsEvidence = true;
    /**
     * Simplify groups based on peptide confidenc level.
     */
    private boolean simplifyGroupsConfidence = true;
    /**
     * Simplify groups based on enzymaticity.
     */
    private boolean simplifyGroupsEnzymaticity = true;
    /**
     * Simplify groups based on variant matching.
     */
    private boolean simplifyGroupsVariants = true;
    /**
     * Confidence below which a peptide is considered absent.
     */
    private double confidenceThreshold = 0.05;

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        readDBMode();

        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        output.append("Simplify Groups: ").append(getSimplifyGroups()).append(newLine);
        output.append("Simplify on protein evidence: ").append(getSimplifyGroupsEvidence()).append(newLine);
        output.append("Simplify on peptide confidence: ").append(getSimplifyGroupsEvidence()).append(newLine);
        output.append("Peptide confidence threshold: ").append(getConfidenceThreshold()).append(newLine);
        output.append("Simplify on peptide enzymaticity: ").append(getSimplifyGroupsEnzymaticity()).append(newLine);
        output.append("Simplify on peptide variants: ").append(getSimplifyGroupsEnzymaticity()).append(newLine);

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
        readDBMode();

        if (otherProteinInferencePreferences == null) {
            return false;
        }

        if (getSimplifyGroups() != otherProteinInferencePreferences.getSimplifyGroups()) {
            return false;
        }

        if (getSimplifyGroupsEvidence() != otherProteinInferencePreferences.getSimplifyGroupsEvidence()) {
            return false;
        }

        if (getSimplifyGroupsConfidence() != otherProteinInferencePreferences.getSimplifyGroupsConfidence()) {
            return false;
        }

        if (getSimplifyGroupsEnzymaticity() != otherProteinInferencePreferences.getSimplifyGroupsEnzymaticity()) {
            return false;
        }

        if (getSimplifyGroupsVariants()!= otherProteinInferencePreferences.getSimplifyGroupsVariants()) {
            return false;
        }

        if (getConfidenceThreshold() != otherProteinInferencePreferences.getConfidenceThreshold()) {
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
        readDBMode();
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
        writeDBMode();
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
        readDBMode();
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
        writeDBMode();
        this.simplifyGroupsEvidence = simplifyGroupsEvidence;
    }

    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified based on the peptide confidence.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified based on the peptide confidence
     */
    public boolean getSimplifyGroupsConfidence() {
        readDBMode();
        return simplifyGroupsConfidence;
    }

    /**
     * Sets whether the protein groups should be simplified based on the peptide
     * confidence level.
     *
     * @param simplifyGroupsConfidence whether the protein groups should be
     * simplified based on the peptide confidence level
     */
    public void setSimplifyGroupsConfidence(boolean simplifyGroupsConfidence) {
        writeDBMode();
        this.simplifyGroupsConfidence = simplifyGroupsConfidence;
    }

    /**
     * Returns the confidence below which a peptide is considered absent.
     * 
     * @return the confidence below which a peptide is considered absent
     */
    public double getConfidenceThreshold() {
        readDBMode();
        return confidenceThreshold;
    }

    /**
     * Sets the confidence below which a peptide is considered absent.
     * 
     * @param confidenceThreshold the confidence below which a peptide is considered absent
     */
    public void setConfidenceThreshold(double confidenceThreshold) {
        writeDBMode();
        this.confidenceThreshold = confidenceThreshold;
    }
    
    /**
     * Returns a boolean indicating whether the protein groups should be
     * simplified based on the peptide enzymaticity.
     *
     * @return a boolean indicating whether the protein groups should be
     * simplified based on the peptide enzymaticity
     */
    public boolean getSimplifyGroupsEnzymaticity() {
        readDBMode();
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
        writeDBMode();
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
        readDBMode();
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
        writeDBMode();
        this.simplifyGroupsVariants = simplifyGroupsVariants;
    }

    /**
     * Returns whether the protein mapping should be refined based on terminal and pattern modifications.
     * 
     * @return a boolean indicating whether the protein mapping should be refined based on terminal and pattern modifications
     */
    public boolean isModificationRefinement() {
        readDBMode();
        return modificationRefinement;
    }

    /**
     * Sets whether the protein mapping should be refined based on terminal and pattern modifications.
     * 
     * @param modificationRefinement a boolean indicating whether the protein mapping should be refined based on terminal and pattern modifications
     */
    public void setModificationRefinement(boolean modificationRefinement) {
        writeDBMode();
        this.modificationRefinement = modificationRefinement;
    }
    
    
}
