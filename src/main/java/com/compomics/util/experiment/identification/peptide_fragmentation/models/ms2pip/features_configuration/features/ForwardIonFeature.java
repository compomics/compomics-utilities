package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

/**
 * Feature based on a property of the forward ion.
 *
 * @author Marc Vaudel
 */
public class ForwardIonFeature extends IonFeature {
    
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
}
