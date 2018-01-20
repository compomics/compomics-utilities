package com.compomics.util.parameters.identification.advanced;

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
        
        if (defaultProteinFDR != otherIdMatchValidationPreferences.getDefaultProteinFDR()) {

            return false;

        }
        
        if (defaultPeptideFDR != otherIdMatchValidationPreferences.getDefaultProteinFDR()) {

            return false;

        }
        
        if (defaultPsmFDR != otherIdMatchValidationPreferences.getDefaultProteinFDR()) {

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
