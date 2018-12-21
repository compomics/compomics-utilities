package com.compomics.util.gui.filtering;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.identification.filtering.MatchFilter;
import com.compomics.util.experiment.identification.filtering.MatchFilter.FilterType;
import com.compomics.util.experiment.identification.filtering.PeptideFilter;
import com.compomics.util.experiment.identification.filtering.ProteinFilter;
import com.compomics.util.experiment.identification.filtering.PsmFilter;
import java.util.HashMap;

/**
 * This class groups the display filter preferences.
 *
 * @author Marc Vaudel
 */
public class FilterParameters extends DbObject {

    /**
     * The protein star filters.
     */
    private HashMap<String, ProteinFilter> proteinStarFilters = new HashMap<>();
    /**
     * The protein hide filters.
     */
    private HashMap<String, ProteinFilter> proteinHideFilters = new HashMap<>();
    /**
     * The peptide star filters.
     */
    private HashMap<String, PeptideFilter> peptideStarFilters = new HashMap<>();
    /**
     * The peptide hide filters.
     */
    private HashMap<String, PeptideFilter> peptideHideFilters = new HashMap<>();
    /**
     * The psm star filters.
     */
    private HashMap<String, PsmFilter> psmStarFilters = new HashMap<>();
    /**
     * The psm hide filters.
     */
    private HashMap<String, PsmFilter> psmHideFilters = new HashMap<>();

    /**
     * Constructors.
     */
    public FilterParameters() {
    }

    /**
     * Returns the protein hide filters.
     *
     * @return the protein hide filters
     */
    public HashMap<String, ProteinFilter> getProteinHideFilters() {
        readDBMode();
        return proteinHideFilters;
    }

    /**
     * Sets the protein hide filters.
     *
     * @param proteinHideFilters the protein hide filters
     */
    public void setProteinHideFilters(HashMap<String, ProteinFilter> proteinHideFilters) {
        writeDBMode();
        this.proteinHideFilters = proteinHideFilters;
    }

    /**
     * Returns the protein star filters.
     *
     * @return the protein star filters
     */
    public HashMap<String, ProteinFilter> getProteinStarFilters() {
        readDBMode();
        return proteinStarFilters;
    }

    /**
     * Sets the protein star filters.
     *
     * @param proteinStarFilters the protein star filters
     */
    public void setProteinStarFilters(HashMap<String, ProteinFilter> proteinStarFilters) {
        writeDBMode();
        this.proteinStarFilters = proteinStarFilters;
    }

    /**
     * Returns the peptide hide filters.
     *
     * @return the peptide hide filters
     */
    public HashMap<String, PeptideFilter> getPeptideHideFilters() {
        readDBMode();
        return peptideHideFilters;
    }

    /**
     * Sets the peptide hide filters.
     *
     * @param peptideHideFilters the peptide hide filters
     */
    public void setPeptideHideFilters(HashMap<String, PeptideFilter> peptideHideFilters) {
        writeDBMode();
        this.peptideHideFilters = peptideHideFilters;
    }

    /**
     * Returns the peptide star filters.
     *
     * @return the peptide star filters
     */
    public HashMap<String, PeptideFilter> getPeptideStarFilters() {
        readDBMode();
        return peptideStarFilters;
    }

    /**
     * Sets the peptide star filters.
     *
     * @param peptideStarFilters the peptide star filters
     */
    public void setPeptideStarFilters(HashMap<String, PeptideFilter> peptideStarFilters) {
        writeDBMode();
        this.peptideStarFilters = peptideStarFilters;
    }

    /**
     * Returns the psm hide filters.
     *
     * @return the psm hide filters
     */
    public HashMap<String, PsmFilter> getPsmHideFilters() {
        readDBMode();
        return psmHideFilters;
    }

    /**
     * Sets the psm hide filters.
     *
     * @param psmHideFilters the psm hide filters
     */
    public void setPsmHideFilters(HashMap<String, PsmFilter> psmHideFilters) {
        writeDBMode();
        this.psmHideFilters = psmHideFilters;
    }

    /**
     * Returns the psm star filters.
     *
     * @return the psm star filters
     */
    public HashMap<String, PsmFilter> getPsmStarFilters() {
        readDBMode();
        return psmStarFilters;
    }

    /**
     * Sets the psm star filters.
     *
     * @param psmStarFilters the psm star filters
     */
    public void setPsmStarFilters(HashMap<String, PsmFilter> psmStarFilters) {
        writeDBMode();
        this.psmStarFilters = psmStarFilters;
    }

    /**
     * Adds a starring filter (previous filter with same name will silently be
     * overwritten).
     *
     * @param matchFilter the new filter
     */
    public void addStarringFilter(MatchFilter matchFilter) {
        writeDBMode();
        if (matchFilter.getType().equals(FilterType.PROTEIN)) {
            proteinStarFilters.put(matchFilter.getName(), (ProteinFilter) matchFilter);
        } else if (matchFilter.getType().equals(FilterType.PEPTIDE)) {
            peptideStarFilters.put(matchFilter.getName(), (PeptideFilter) matchFilter);
        } else if (matchFilter.getType().equals(FilterType.PSM)) {
            psmStarFilters.put(matchFilter.getName(), (PsmFilter) matchFilter);
        }
    }

    /**
     * Adds a hiding filter (previous filter with same name will silently be
     * overwritten).
     *
     * @param matchFilter the new filter
     */
    public void addHidingFilter(MatchFilter matchFilter) {
        writeDBMode();
        if (matchFilter.getType().equals(FilterType.PROTEIN)) {
            proteinHideFilters.put(matchFilter.getName(), (ProteinFilter) matchFilter);
        } else if (matchFilter.getType().equals(FilterType.PEPTIDE)) {
            peptideHideFilters.put(matchFilter.getName(), (PeptideFilter) matchFilter);
        } else if (matchFilter.getType().equals(FilterType.PSM)) {
            psmHideFilters.put(matchFilter.getName(), (PsmFilter) matchFilter);
        }
    }

    /**
     * Returns a boolean indicating whether the name of this filter is already
     * taken or not.
     *
     * @param filtername the name of the new filter
     * @return a boolean indicating whether the name of this filter is already
     * taken or not
     */
    public boolean filterExists(String filtername) {
        readDBMode();
        return proteinHideFilters.containsKey(filtername) || proteinStarFilters.containsKey(filtername)
                || peptideHideFilters.containsKey(filtername) || peptideStarFilters.containsKey(filtername)
                || psmHideFilters.containsKey(filtername) || psmStarFilters.containsKey(filtername);
    }
}
