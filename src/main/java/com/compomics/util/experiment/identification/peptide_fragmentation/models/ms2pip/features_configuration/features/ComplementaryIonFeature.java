package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

/**
 * Feature based on a property of the complementary ion.
 *
 * @author Marc Vaudel
 */
public class ComplementaryIonFeature extends IonFeature {
    
    /**
     * Constructor.
     * 
     * @param property the peptide property
     */
    public ComplementaryIonFeature(IonFeature.Property property) {
        this.property = property;
    }

    @Override
    public String getDescription() {
        return property.description.replaceAll("ion", "complementary ion");
    }
}
