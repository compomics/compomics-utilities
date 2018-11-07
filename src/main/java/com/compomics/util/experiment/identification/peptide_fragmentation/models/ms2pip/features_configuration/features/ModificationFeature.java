package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;

/**
 * Feature based on the modification status of an amino acid.
 *
 * @author Marc Vaudel
 */
public class ModificationFeature implements Ms2pipFeature {

    /**
     * Empty default constructor
     */
    public ModificationFeature() {
    }
    
    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 11;

    /**
     * Enum for the possible features.
     */
    public enum Property {
        
        mass("The cumulated mass of the modifications carried by the amino acid.");
        /**
         * The description of the option.
         */
        public final String description;

        /**
         * Constructor.
         *
         * @param description the description of the option
         */
        private Property(String description) {
            this.description = description;
        }
    }

    /**
     * The peptide feature.
     */
    private Property property;
    
    

    /**
     * Constructor.
     *
     * @param property the peptide property
     */
    public ModificationFeature(ModificationFeature.Property property) {
        this.property = property;
    }

    /**
     * Returns the feature property.
     *
     * @return the feature property
     */
    public ModificationFeature.Property getProperty() {
        return property;
    }

    /**
     * Sets the feature property.
     *
     * @param property the feature property
     */
    public void setProperty(ModificationFeature.Property property) {
        this.property = property;
    }

    @Override
    public String getCategory() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        return property.description;
    }

    @Override
    public int getIndex() {
        return index;
    }

}
