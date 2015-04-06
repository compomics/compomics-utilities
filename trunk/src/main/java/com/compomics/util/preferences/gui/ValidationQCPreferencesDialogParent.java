package com.compomics.util.preferences.gui;

import com.compomics.util.experiment.filtering.Filter;

/**
 * Interface allowing the editing of filters from other packages.
 *
 * @author Marc Vaudel
 */
public interface ValidationQCPreferencesDialogParent {

    /**
     * Allows the creation of a PSM filter.
     *
     * @return the new filter, null if none created
     */
    public Filter createPsmFilter();

    /**
     * Allows the creation of a peptide filter.
     *
     * @return the new filter, null if none created
     */
    public Filter createPeptideFilter();

    /**
     * Allows the creation of a protein filter.
     *
     * @return the new filter, null if none created
     */
    public Filter createProteinFilter();

    /**
     * Allows the edition of a filter.
     *
     * @param filter the filter to edit
     */
    public void editFilter(Filter filter);
}
