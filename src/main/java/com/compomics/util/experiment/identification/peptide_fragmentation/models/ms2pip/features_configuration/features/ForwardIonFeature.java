package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.IonFeature;

/**
 * Feature based on a property of the forward ion.
 *
 * @author Marc Vaudel
 */
public class ForwardIonFeature extends IonFeature {

    /**
     * Empty default constructor
     */
    public ForwardIonFeature() {
    }
    
    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 2;
    /**
     * Constructor.
     * 
     * @param property the peptide property
     */
    public ForwardIonFeature(IonFeature.Property property) {
        this.property = property;
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
