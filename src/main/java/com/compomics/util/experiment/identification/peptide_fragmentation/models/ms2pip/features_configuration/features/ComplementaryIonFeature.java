package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.IonFeature;

/**
 * Feature based on a property of the complementary ion.
 *
 * @author Marc Vaudel
 */
public class ComplementaryIonFeature extends IonFeature {
    
    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 3;
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

    @Override
    public int getIndex() {
        return index;
    }
}
