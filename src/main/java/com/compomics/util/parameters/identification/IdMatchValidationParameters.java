package com.compomics.util.parameters.identification;

import java.io.Serializable;

/**
 * Generic class grouping the identification matches validation preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IdMatchValidationParameters implements Serializable {

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
     * Boolean indicating whether Peptides should be grouped according to their
     * modification status.
     */
    private Boolean separatePeptides = true;
    /**
     * Boolean indicating whether PSMs should be grouped according to their
     * modification status.
     */
    private Boolean separatePsms = true;
    /**
     * If true, groups of matches of small size will be merged.
     */
    private Boolean mergeSmallSubgroups = true;
    /**
     * The validation quality control preferences.
     */
    private ValidationQcParameters validationQCPreferences = new ValidationQcParameters();

    /**
     * Constructor for default settings.
     */
    public IdMatchValidationParameters() {

    }

    /**
     * Creates a new IdMatchValidationPreferences based on the values of the
     * given IdMatchValidationPreferences.
     *
     * @param idMatchValidationPreferences an IdMatchValidationPreferences to
     * take default values from.
     */
    public IdMatchValidationParameters(IdMatchValidationParameters idMatchValidationPreferences) {
        defaultProteinFDR = idMatchValidationPreferences.getDefaultProteinFDR();
        defaultPeptideFDR = idMatchValidationPreferences.getDefaultPeptideFDR();
        defaultPsmFDR = idMatchValidationPreferences.getDefaultPsmFDR();
        separatePeptides = idMatchValidationPreferences.getSeparatePeptides();
        separatePsms = idMatchValidationPreferences.getSeparatePsms();
        mergeSmallSubgroups = idMatchValidationPreferences.getMergeSmallSubgroups();
        validationQCPreferences = new ValidationQcParameters(idMatchValidationPreferences.getValidationQCPreferences());
    }

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
    public ValidationQcParameters getValidationQCPreferences() {
        return validationQCPreferences;
    }

    /**
     * Sets the validation QC preferences.
     *
     * @param validationQCPreferences the validation QC preferences
     */
    public void setValidationQCPreferences(ValidationQcParameters validationQCPreferences) {
        this.validationQCPreferences = validationQCPreferences;
    }

    /**
     * Indicates whether small subgroups of matches should be merged.
     *
     * @return true if small subgroups of matches should be merged
     */
    public Boolean getMergeSmallSubgroups() {
        if (mergeSmallSubgroups == null) {
            mergeSmallSubgroups = true;
        }
        return mergeSmallSubgroups;
    }

    /**
     * Sets whether small subgroups of matches should be merged.
     *
     * @param mergeSmallSubgroups a boolean indicating whether small subgroups
     * of matches should be merged
     */
    public void setMergeSmallSubgroups(Boolean mergeSmallSubgroups) {
        this.mergeSmallSubgroups = mergeSmallSubgroups;
    }

    /**
     * Returns a boolean indicating whether Peptides should be grouped according
     * to their modification status.
     *
     * @return a boolean indicating whether Peptides should be grouped according
     * to their modification status
     */
    public Boolean getSeparatePeptides() {
        if (separatePeptides == null) {
            separatePeptides = true;
        }
        return separatePeptides;
    }

    /**
     * Sets whether Peptides should be grouped according to their modification
     * status.
     *
     * @param separatePeptides a boolean indicating whether Peptides should be
     * grouped according to their modification status
     */
    public void setSeparatePeptides(Boolean separatePeptides) {
        this.separatePeptides = separatePeptides;
    }

    /**
     * Returns a boolean indicating whether PSMs should be grouped according to
     * their modification status.
     *
     * @return a boolean indicating whether PSMs should be grouped according to
     * their modification status
     */
    public Boolean getSeparatePsms() {
        if (separatePsms == null) {
            separatePsms = true;
        }
        return separatePsms;
    }

    /**
     * Sets whether PSMs should be grouped according to their modification
     * status.
     *
     * @param separatePsms a boolean indicating whether PSMs should be grouped
     * according to their modification status
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
        output.append("Group Small Subgroups: ").append(mergeSmallSubgroups).append(".").append(newLine);

        return output.toString();
    }

    /**
     * Returns true if the objects have identical settings.
     *
     * @param otherIdMatchValidationPreferences the IdMatchValidationPreferences
     * to compare to
     *
     * @return true if the objects have identical settings
     */
    public boolean equals(IdMatchValidationParameters otherIdMatchValidationPreferences) {

        if (otherIdMatchValidationPreferences == null) {
            return false;
        }

        double diff = Math.abs(defaultProteinFDR - otherIdMatchValidationPreferences.getDefaultProteinFDR());
        if (diff > 0.0000000000001) {
            return false;
        }

        diff = Math.abs(defaultPeptideFDR - otherIdMatchValidationPreferences.getDefaultPeptideFDR());
        if (diff > 0.0000000000001) {
            return false;
        }

        diff = Math.abs(defaultPsmFDR - otherIdMatchValidationPreferences.getDefaultPsmFDR());
        if (diff > 0.0000000000001) {
            return false;
        }

        if (separatePeptides.booleanValue() != otherIdMatchValidationPreferences.getSeparatePeptides()) {
            return false;
        }

        if (separatePsms.booleanValue() != otherIdMatchValidationPreferences.getSeparatePsms()) {
            return false;
        }

        if (mergeSmallSubgroups.booleanValue() != otherIdMatchValidationPreferences.getMergeSmallSubgroups()) {
            return false;
        }

        if ((validationQCPreferences == null && otherIdMatchValidationPreferences.getValidationQCPreferences() != null)
                || (validationQCPreferences != null && otherIdMatchValidationPreferences.getValidationQCPreferences() == null)) {
            return false;
        }

        if (validationQCPreferences != null && otherIdMatchValidationPreferences.getValidationQCPreferences() != null
                && !validationQCPreferences.isSameAs(otherIdMatchValidationPreferences.getValidationQCPreferences())) {
            return false;
        }

        return true;
    }
}
