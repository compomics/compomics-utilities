package com.compomics.util.preferences;

import com.compomics.util.experiment.filtering.Filter;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class lists the criteria used for quality control of the validated
 * matches.
 *
 * @author Marc Vaudel
 */
public class ValidationQCPreferences implements Serializable {

    /**
     * Indicates whether the database size should be checked.
     */
    private boolean dbSize = true;
    /**
     * Indicates whether the number of hits before the first decoy should be
     * checked.
     */
    private boolean firstDecoy = true;
    /**
     * The margin in number of resolution over the confidence threshold.
     */
    private Double confidenceMargin = 1.0;
    /**
     * List of QC filters for PSMs.
     */
    private ArrayList<Filter> psmFilters;
    /**
     * List of QC filters for peptides.
     */
    private ArrayList<Filter> peptideFilters;
    /**
     * List of QC filters for proteins.
     */
    private ArrayList<Filter> proteinFilters;

    /**
     * Creates a validation quality control preferences object with default
     * settings.
     */
    public ValidationQCPreferences() {
    }

    /**
     * Creates a validation quality control preferences object based on an other
     * ValidationQCPreferences.
     *
     * @param validationQCPreferences an other ValidationQCPreferences
     */
    public ValidationQCPreferences(ValidationQCPreferences validationQCPreferences) {
        this.dbSize = validationQCPreferences.isDbSize();
        this.firstDecoy = validationQCPreferences.isFirstDecoy();
        this.confidenceMargin = validationQCPreferences.getConfidenceMargin();
        psmFilters = new ArrayList<Filter>(validationQCPreferences.getPsmFilters().size());
        for (Filter filter : validationQCPreferences.getPsmFilters()) {
            psmFilters.add(filter.clone());
        }
        peptideFilters = new ArrayList<Filter>(validationQCPreferences.getPeptideFilters().size());
        for (Filter filter : validationQCPreferences.getPeptideFilters()) {
            peptideFilters.add(filter.clone());
        }
        proteinFilters = new ArrayList<Filter>(validationQCPreferences.getProteinFilters().size());
        for (Filter filter : validationQCPreferences.getProteinFilters()) {
            proteinFilters.add(filter.clone());
        }
    }

    /**
     * Indicates whether the database size should be checked.
     *
     * @return true if the database size should be checked
     */
    public boolean isDbSize() {
        return dbSize;
    }

    /**
     * Sets whether the database size should be checked.
     *
     * @param dbSize a boolean indicating whether the database size should be
     * checked
     */
    public void setDbSize(boolean dbSize) {
        this.dbSize = dbSize;
    }

    /**
     * Indicates whether the number of hits before the first decoy should be
     * checked.
     *
     * @return true if the number of hits before the first decoy should be
     * checked
     */
    public boolean isFirstDecoy() {
        return firstDecoy;
    }

    /**
     * Sets whether the number of hits before the first decoy should be checked.
     *
     * @param firstDecoy a boolean indicating whether the number of hits before
     * the first decoy should be checked.
     */
    public void setFirstDecoy(boolean firstDecoy) {
        this.firstDecoy = firstDecoy;
    }

    /**
     * Returns the margin to the threshold to use as factor of the resolution.
     * e.g. for a threshold of 10% and a resolution of 1%, with a factor of 1
     * the threshold will be 11%, with a factor of 2.5 the threshold will be
     * 12.5%.
     *
     * @return the margin to the threshold to use as factor of the resolution
     */
    public Double getConfidenceMargin() {
        return confidenceMargin;
    }

    /**
     * Sets the margin to the threshold to use as factor of the resolution. e.g.
     * for a threshold of 10% and a resolution of 1%, with a factor of 1 the
     * threshold will be 11%, with a factor of 2.5 the threshold will be 12.5%.
     *
     * @param confidenceMargin the margin to the threshold to use as factor of
     * the resolution
     */
    public void setConfidenceMargin(Double confidenceMargin) {
        this.confidenceMargin = confidenceMargin;
    }

    /**
     * Returns the list of PSM quality filters.
     *
     * @return the list of PSM quality filters
     */
    public ArrayList<Filter> getPsmFilters() {
        return psmFilters;
    }

    /**
     * Sets the list of PSM quality filters.
     *
     * @param psmFilters the list of PSM quality filters
     */
    public void setPsmFilters(ArrayList<Filter> psmFilters) {
        this.psmFilters = psmFilters;
    }

    /**
     * Returns the list of peptide quality filters.
     *
     * @return the list of peptide quality filters
     */
    public ArrayList<Filter> getPeptideFilters() {
        return peptideFilters;
    }

    /**
     * Sets the list of peptide quality filters.
     *
     * @param peptideFilters the list of peptide quality filters
     */
    public void setPeptideFilters(ArrayList<Filter> peptideFilters) {
        this.peptideFilters = peptideFilters;
    }

    /**
     * Returns the list of protein quality filters.
     *
     * @return the list of peptide quality filters
     */
    public ArrayList<Filter> getProteinFilters() {
        return proteinFilters;
    }

    /**
     * Sets the list of protein quality filters.
     *
     * @param proteinFilters the list of protein quality filters
     */
    public void setProteinFilters(ArrayList<Filter> proteinFilters) {
        this.proteinFilters = proteinFilters;
    }

    /**
     * Returns true if the two ValidationQCPreferences are the same.
     * 
     * @param validationQCPreferences the ValidationQCPreferences to compare to
     * @return true if the two ValidationQCPreferences are the same
     */
    public boolean isSameAs(ValidationQCPreferences validationQCPreferences) {
        if (dbSize != validationQCPreferences.isDbSize()) {
            return false;
        }
        if (firstDecoy != validationQCPreferences.isFirstDecoy()) {
            return false;
        }
        if (!getConfidenceMargin().equals(validationQCPreferences.getConfidenceMargin())) {
            return false;
        }
        if (psmFilters.size() != validationQCPreferences.getPsmFilters().size()) {
            return false;
        }
        if (peptideFilters.size() != validationQCPreferences.getPeptideFilters().size()) {
            return false;
        }
        if (proteinFilters.size() != validationQCPreferences.getProteinFilters().size()) {
            return false;
        }
        for (Filter psmFilter : psmFilters) {
            boolean found = false;
            for (Filter newFilter : validationQCPreferences.getPsmFilters()) {
                if (newFilter.isSameAs(psmFilter)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        for (Filter psmFilter : peptideFilters) {
            boolean found = false;
            for (Filter newFilter : validationQCPreferences.getPeptideFilters()) {
                if (newFilter.isSameAs(psmFilter)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        for (Filter psmFilter : proteinFilters) {
            boolean found = false;
            for (Filter newFilter : validationQCPreferences.getProteinFilters()) {
                if (newFilter.isSameAs(psmFilter)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }
}
