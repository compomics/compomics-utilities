package com.compomics.util.preferences.gui;

import com.compomics.util.experiment.filtering.Filter;

/**
 * Interface allowing the editing of filters from other packages
 *
 * @author Marc Vaudel
 */
public interface ValidationQCPreferencesDialogParent {

    /**
     * Allows the creation of a psm filter.
     * 
     * @return the new filter, null if none created
     */
    public Filter createPsmFilter();
    /**
     * Allows the edition of a psm filter.
     * 
     * @param filter the psm filter to edit
     */
    public void editPsmFilter(Filter filter);
    /**
     * Allows the creation of a peptide filter.
     * 
     * @return the new filter, null if none created
     */
    public Filter createPeptideFilter();
    /**
     * Allows the edition of a peptide filter.
     * 
     * @param filter the peptide filter to edit
     */
    public void editPeptideFilter(Filter filter);
    /**
     * Allows the creation of a protein filter.
     * 
     * @return the new filter, null if none created
     */
    public Filter createProteinFilter();
    /**
     * Allows the edition of a protein filter.
     * 
     * @param filter the protein filter to edit
     */
    public void editProteinFilter(Filter filter);
    
}
