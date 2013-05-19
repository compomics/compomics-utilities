package com.compomics.util.gui.gene_mapping;

import com.compomics.util.preferences.GenePreferences;

/**
 * Interface for SpeciesDialog parent frames or dialogs.
 *
 * @author Harald Barsnes
 */
public interface SpeciesDialogParent {

    /**
     * Return the gene preferences.
     *
     * @return the gene preferences
     */
    public GenePreferences getGenePreferences();

    /**
     * Clear the gene mappings.
     */
    public void clearGeneMappings();

    /**
     * Update the gene mapping results.
     *
     * @param selectedSpecies the new species
     */
    public void updateGeneMappings(String selectedSpecies);
}
