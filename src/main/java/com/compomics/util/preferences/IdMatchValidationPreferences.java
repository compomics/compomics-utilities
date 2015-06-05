package com.compomics.util.preferences;

import java.io.Serializable;

/**
 * Generic class grouping the identification matches validation preferences.
 *
 * @author Marc Vaudel
 */
public class IdMatchValidationPreferences implements Serializable {

    /**
     * Serial version UID for backward compatibility.
     */
    static final long serialVersionUID = 4327810348755338485L;
    /**
     * The default protein FDR.
     */
    private double defaultProteinFDR = 1.0;
    /**
     * The default peptide FDR.
     */
    private double defaultPeptideFDR = 1.0;
    /**
     * The default PSM FDR.
     */
    private double defaultPsmFDR = 1.0;
    /**
     * If true groups of matches of small size will be grouped.
     */
    private Boolean groupSmallSubgroups = true;
    /**
     * The validation quality control preferences.
     */
    private ValidationQCPreferences validationQCPreferences = new ValidationQCPreferences();

    /**
     * Returns the default protein FDR.
     *
     * @return the default protein FDR
     */
    public double getDefaultProteinFDR() {
        return defaultProteinFDR;
    }

    /**
     * Sets the default protein FDR.
     *
     * @param defaultProteinFDR the default protein FDR
     */
    public void setDefaultProteinFDR(double defaultProteinFDR) {
        this.defaultProteinFDR = defaultProteinFDR;
    }

    /**
     * Returns the default peptide FDR.
     *
     * @return the default peptide FDR
     */
    public double getDefaultPeptideFDR() {
        return defaultPeptideFDR;
    }

    /**
     * Sets the default peptide FDR.
     *
     * @param defaultPeptideFDR the default peptide FDR
     */
    public void setDefaultPeptideFDR(double defaultPeptideFDR) {
        this.defaultPeptideFDR = defaultPeptideFDR;
    }

    /**
     * Returns the default PSM FDR.
     *
     * @return the default PSM FDR
     */
    public double getDefaultPsmFDR() {
        return defaultPsmFDR;
    }

    /**
     * Sets the default PSM FDR.
     *
     * @param defaultPsmFDR the default PSM FDR
     */
    public void setDefaultPsmFDR(double defaultPsmFDR) {
        this.defaultPsmFDR = defaultPsmFDR;
    }

    /**
     * Returns the validation QC preferences.
     *
     * @return the validation QC preferences
     */
    public ValidationQCPreferences getValidationQCPreferences() {
        if (validationQCPreferences == null) { // Backward compatibility
            validationQCPreferences = new ValidationQCPreferences();
        }
        return validationQCPreferences;
    }

    /**
     * Sets the validation QC preferences.
     *
     * @param validationQCPreferences the validation QC preferences
     */
    public void setValidationQCPreferences(ValidationQCPreferences validationQCPreferences) {
        this.validationQCPreferences = validationQCPreferences;
    }

    /**
     * Indicates whether small subgroups of matches should be grouped together.
     *
     * @return true if small subgroups of matches should be grouped together
     */
    public Boolean getGroupSmallSubgroups() {
        return groupSmallSubgroups;
    }

    /**
     * Sets whether small subgroups of matches should be grouped together.
     *
     * @param groupSmallSubgroups a boolean indicating whether small subgroups
     * of matches should be grouped together
     */
    public void setGroupSmallSubgroups(Boolean groupSmallSubgroups) {
        this.groupSmallSubgroups = groupSmallSubgroups;
    }
}
