package com.compomics.util.parameters.identification.advanced;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.filtering.Filter;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * This class lists the criteria used for quality control of the validated
 * matches.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ValidationQcParameters extends DbObject {

    /**
     * Indicates whether the database size should be checked.
     */
    private boolean dbSize = false;
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
    public ValidationQcParameters() {
    }

    /**
     * Creates a validation quality control preferences object based on an other
     * ValidationQCPreferences.
     *
     * @param validationQCPreferences an other ValidationQCPreferences
     */
    public ValidationQcParameters(
            ValidationQcParameters validationQCPreferences
    ) {

        this.dbSize = validationQCPreferences.isDbSize();
        this.firstDecoy = validationQCPreferences.isFirstDecoy();
        this.confidenceMargin = validationQCPreferences.getConfidenceMargin();

        if (validationQCPreferences.getPsmFilters() != null) {

            psmFilters = new ArrayList<>(validationQCPreferences.getPsmFilters().size());

            for (Filter filter : validationQCPreferences.getPsmFilters()) {

                psmFilters.add(filter.clone());

            }
        }

        if (validationQCPreferences.getPeptideFilters() != null) {

            peptideFilters = new ArrayList<>(validationQCPreferences.getPeptideFilters().size());

            for (Filter filter : validationQCPreferences.getPeptideFilters()) {

                peptideFilters.add(filter.clone());

            }
        }

        if (validationQCPreferences.getProteinFilters() != null) {

            proteinFilters = new ArrayList<>(validationQCPreferences.getProteinFilters().size());

            for (Filter filter : validationQCPreferences.getProteinFilters()) {

                proteinFilters.add(filter.clone());

            }
        }
    }

    /**
     * Indicates whether the database size should be checked.
     *
     * @return true if the database size should be checked
     */
    public boolean isDbSize() {
        readDBMode();
        return dbSize;
    }

    /**
     * Sets whether the database size should be checked.
     *
     * @param dbSize a boolean indicating whether the database size should be
     * checked
     */
    public void setDbSize(boolean dbSize) {
        writeDBMode();
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
        readDBMode();
        return firstDecoy;
    }

    /**
     * Sets whether the number of hits before the first decoy should be checked.
     *
     * @param firstDecoy a boolean indicating whether the number of hits before
     * the first decoy should be checked.
     */
    public void setFirstDecoy(boolean firstDecoy) {
        writeDBMode();
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
        readDBMode();
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
        writeDBMode();
        this.confidenceMargin = confidenceMargin;
    }

    /**
     * Returns the list of PSM quality filters.
     *
     * @return the list of PSM quality filters
     */
    public ArrayList<Filter> getPsmFilters() {
        readDBMode();
        return psmFilters;
    }

    /**
     * Sets the list of PSM quality filters.
     *
     * @param psmFilters the list of PSM quality filters
     */
    public void setPsmFilters(ArrayList<Filter> psmFilters) {
        writeDBMode();
        this.psmFilters = psmFilters;
    }

    /**
     * Returns the list of peptide quality filters.
     *
     * @return the list of peptide quality filters
     */
    public ArrayList<Filter> getPeptideFilters() {
        readDBMode();
        return peptideFilters;
    }

    /**
     * Sets the list of peptide quality filters.
     *
     * @param peptideFilters the list of peptide quality filters
     */
    public void setPeptideFilters(ArrayList<Filter> peptideFilters) {
        writeDBMode();
        this.peptideFilters = peptideFilters;
    }

    /**
     * Returns the list of protein quality filters.
     *
     * @return the list of peptide quality filters
     */
    public ArrayList<Filter> getProteinFilters() {
        readDBMode();
        return proteinFilters;
    }

    /**
     * Sets the list of protein quality filters.
     *
     * @param proteinFilters the list of protein quality filters
     */
    public void setProteinFilters(ArrayList<Filter> proteinFilters) {
        writeDBMode();
        this.proteinFilters = proteinFilters;
    }

    /**
     * Returns true if the two ValidationQCPreferences are the same.
     *
     * @param validationQCPreferences the ValidationQCPreferences to compare to
     * @return true if the two ValidationQCPreferences are the same
     */
    public boolean isSameAs(
            ValidationQcParameters validationQCPreferences
    ) {
        readDBMode();

        if (dbSize != validationQCPreferences.isDbSize()) {
            return false;
        }

        if (firstDecoy != validationQCPreferences.isFirstDecoy()) {
            return false;
        }

        if (!getConfidenceMargin().equals(validationQCPreferences.getConfidenceMargin())) {
            return false;
        }

        if ((psmFilters != null && validationQCPreferences.getPsmFilters() == null)
                || (psmFilters == null && validationQCPreferences.getPsmFilters() != null)) {
            return false;
        }

        if (psmFilters != null && validationQCPreferences.getPsmFilters() != null
                && psmFilters.size() != validationQCPreferences.getPsmFilters().size()) {
            return false;
        }

        if ((peptideFilters != null && validationQCPreferences.getPeptideFilters() == null)
                || (peptideFilters == null && validationQCPreferences.getPeptideFilters() != null)) {
            return false;
        }

        if (peptideFilters != null && validationQCPreferences.getPeptideFilters() != null
                && peptideFilters.size() != validationQCPreferences.getPeptideFilters().size()) {
            return false;
        }

        if ((proteinFilters != null && validationQCPreferences.getProteinFilters() == null)
                || (proteinFilters == null && validationQCPreferences.getProteinFilters() != null)) {
            return false;
        }

        if (proteinFilters != null && validationQCPreferences.getProteinFilters() != null
                && proteinFilters.size() != validationQCPreferences.getProteinFilters().size()) {
            return false;
        }

        if (psmFilters != null) {

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
        }

        if (peptideFilters != null) {

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
        }

        if (proteinFilters != null) {

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
        }

        return true;
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

        output.append("DB Size Check: ").append(dbSize).append(".").append(newLine);
        output.append("First Target Check: ").append(firstDecoy).append(".").append(newLine);
        output.append("Confidence Check: ").append(confidenceMargin != 0.0).append(".").append(newLine); // @TODO: double value hidden as a boolean..?

        if (proteinFilters != null && !proteinFilters.isEmpty()) {

            output.append("Protein Filters: ").append(newLine);
            String filtersList = proteinFilters.stream()
                    .map(Filter::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));

            output.append(filtersList).append(".").append(newLine);
        }

        if (peptideFilters != null && !peptideFilters.isEmpty()) {

            output.append("Peptide Filters: ").append(newLine);
            String filtersList = peptideFilters.stream()
                    .map(Filter::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));

            output.append(filtersList).append(".").append(newLine);
        }

        if (psmFilters != null && !psmFilters.isEmpty()) {

            output.append("PSM Filters: ").append(newLine);
            String filtersList = psmFilters.stream()
                    .map(Filter::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));

            output.append(filtersList).append(".").append(newLine);
        }

        return output.toString();
    }
}
