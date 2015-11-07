package com.compomics.util.preferences;

import java.io.Serializable;

/**
 * Generic class grouping the identification matches validation preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
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
    private Boolean separatePeptides = true;
    private Boolean separatePsms = true;
    /**
     * If true, groups of matches of small size will be grouped.
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
        if (groupSmallSubgroups == null) {
            groupSmallSubgroups = true;
        }
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

    /**
     * Returns a boolean indicating whether Peptides should be grouped according to their modification status.
     * 
     * @return a boolean indicating whether Peptides should be grouped according to their modification status
     */
    public Boolean getSeparatePeptides() {
        if (separatePeptides == null) {
            separatePeptides = true;
        }
        return separatePeptides;
    }

    /**
     * Sets whether Peptides should be grouped according to their modification status.
     * 
     * @param separatePeptides a boolean indicating whether Peptides should be grouped according to their modification status
     */
    public void setSeparatePeptides(Boolean separatePeptides) {
        this.separatePeptides = separatePeptides;
    }

    /**
     * Returns a boolean indicating whether PSMs should be grouped according to their modification status.
     * 
     * @return a boolean indicating whether PSMs should be grouped according to their modification status
     */
    public Boolean getSeparatePsms() {
        if (separatePsms == null) {
            separatePsms = true;
        }
        return separatePsms;
    }

    /**
     * Sets whether PSMs should be grouped according to their modification status.
     * 
     * @param separatePsms a boolean indicating whether PSMs should be grouped according to their modification status
     */
    public void setSeparatePsms(Boolean separatePsms) {
        this.separatePsms = separatePsms;
    }
    
    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        
        String newLine = System.getProperty("line.separator");
        
        StringBuilder output = new StringBuilder();
        
        output.append("Protein FDR: ").append(defaultProteinFDR).append(".").append(newLine);
        output.append("Peptide FDR: ").append(defaultPeptideFDR).append(".").append(newLine);
        output.append("PSM FDR: ").append(defaultPsmFDR).append(".").append(newLine);
        output.append("Group Small Subgroups: ").append(groupSmallSubgroups).append(".").append(newLine);

        return output.toString();
    }
}
