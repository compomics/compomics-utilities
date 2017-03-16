package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_generation;

/**
 * Interface for an ms2pip feature.
 *
 * @author Marc Vaudel
 */
public interface Ms2pipFeature {

    /**
     * Returns the category of the feature.
     * 
     * @return the category of the feature
     */
    public String getCategory();

    /**
     * Returns the description of the feature.
     * 
     * @return the description of the feature
     */
    public String getDescription();
}
