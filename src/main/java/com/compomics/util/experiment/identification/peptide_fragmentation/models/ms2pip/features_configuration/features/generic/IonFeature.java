package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;

/**
 * Feature based on a property of an ion.
 *
 * @author Marc Vaudel
 */
public abstract class IonFeature implements Ms2pipFeature {

    /**
     * Enum for the possible features.
     */
    public enum Property {
        mass(0, "mass", "The ion mass."),
        massOverLength(1, "massOverLength", "The ion mass relative to the ion length."),
        length(2, "length", "The ion length."),
        relativeLength(3, "relativeLength", "The length of the ion relative to the length of the peptide.");

        /**
         * The index of the option.
         */
        public final int index;
        /**
         * The name of the option.
         */
        public final String name;
        /**
         * The description of the option.
         */
        public final String description;

        /**
         * Constructor.
         *
         * @param index the index of the option
         * @param name the name of the option
         * @param description the description of the option
         */
        private Property(int index, String name, String description) {
            this.index = index;
            this.name = name;
            this.description = description;
        }
    }

    /**
     * The peptide feature.
     */
    protected Property property;

    /**
     * Returns the peptide feature;
     *
     * @return the peptide feature
     */
    public Property getFeature() {
        return property;
    }

    /**
     * Sets the peptide feature.
     *
     * @param feature the peptide feature
     */
    public void setFeature(Property feature) {
        this.property = feature;
    }

    @Override
    public String getCategory() {
        return this.getClass().getName();
    }
}
