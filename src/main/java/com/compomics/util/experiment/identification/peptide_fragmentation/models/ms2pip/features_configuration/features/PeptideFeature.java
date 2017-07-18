package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;

/**
 * Feature based on a peptide property.
 *
 * @author Marc Vaudel
 */
public class PeptideFeature implements Ms2pipFeature {
    
    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 0;

    /**
     * Enum for the possible features.
     */
    public enum Property {
        
        mass("The peptide mass."),
        length("The peptide length."),
        charge("The peptide charge.");
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
    public PeptideFeature(Property property) {
        this.property = property;
    }

    /**
     * Returns the feature property;
     *
     * @return the feature property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Sets the feature property.
     *
     * @param property the feature property
     */
    public void setProperty(Property property) {
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
