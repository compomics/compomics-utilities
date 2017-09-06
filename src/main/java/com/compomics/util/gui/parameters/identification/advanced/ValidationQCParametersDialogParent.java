package com.compomics.util.gui.parameters.identification.advanced;

import com.compomics.util.experiment.filtering.Filter;

/**
 * Interface allowing the editing of filters from other packages.
 *
 * @author Marc Vaudel
 */
public interface ValidationQCParametersDialogParent {

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
     *
     * @return the edited filter, null if canceled
     */
    public Filter editFilter(Filter filter);
}
