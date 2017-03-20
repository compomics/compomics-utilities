package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;

/**
 * Feature based on a peptide property.
 *
 * @author Marc Vaudel
 */
public class PeptideFeature implements Ms2pipFeature {
    
    /**
     * Enum for the possible features.
     */
    public enum Property {
        mass(0, "mass", "The peptide mass."), 
        length(1, "length", "The peptide length."), 
        charge(2, "charge", "The peptide charge.");

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

    @Override
    public String getDescription() {
        return property.description;
    }

}
