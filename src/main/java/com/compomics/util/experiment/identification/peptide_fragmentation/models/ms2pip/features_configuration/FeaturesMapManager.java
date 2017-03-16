package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AminoAcidSequenceFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ComplementaryIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.IonFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.PeptideAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.PeptideFeature;

/**
 * This class is used to manage the features maps.
 *
 * @author Marc Vaudel
 */
public class FeaturesMapManager {

    /**
     * Returns the default features map used in the c implementation, see
     * https://github.com/mvaudel/ms2pip_c.
     *
     * @return the default features map used in the c implementation
     */
    public static FeaturesMap getDefaultFeaturesMap() {

        FeaturesMap featuresMap = new FeaturesMap();

        // Peptide features
        featuresMap.addFeature(new PeptideFeature(PeptideFeature.Property.mass));
        featuresMap.addFeature(new PeptideFeature(PeptideFeature.Property.length));

        // Peptide amino acid features
        AminoAcid.Property[] properties = new AminoAcid.Property[]{AminoAcid.Property.mass, AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        AminoAcidSequenceFeature.Function[] functions = new AminoAcidSequenceFeature.Function[]{AminoAcidSequenceFeature.Function.mean, AminoAcidSequenceFeature.Function.maximum, AminoAcidSequenceFeature.Function.minimum};
        for (AminoAcid.Property property : properties) {
            for (AminoAcidSequenceFeature.Function function : functions) {
                featuresMap.addFeature(new PeptideAminoAcidFeature(property, function));
            }
        }

        // Forward ion amino acid features
        properties = new AminoAcid.Property[]{AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        functions = new AminoAcidSequenceFeature.Function[]{AminoAcidSequenceFeature.Function.maximum, AminoAcidSequenceFeature.Function.minimum};
        for (AminoAcid.Property property : properties) {
            for (AminoAcidSequenceFeature.Function function : functions) {
                featuresMap.addFeature(new ForwardIonAminoAcidFeature(property, function));
            }
        }

        // Complementary ion amino acid features
        properties = new AminoAcid.Property[]{AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        functions = new AminoAcidSequenceFeature.Function[]{AminoAcidSequenceFeature.Function.maximum, AminoAcidSequenceFeature.Function.minimum};
        for (AminoAcid.Property property : properties) {
            for (AminoAcidSequenceFeature.Function function : functions) {
                featuresMap.addFeature(new ComplementaryIonAminoAcidFeature(property, function));
            }
        }

        // Forward ions features
        featuresMap.addFeature(new ForwardIonFeature(IonFeature.Property.length));
        featuresMap.addFeature(new ForwardIonFeature(IonFeature.Property.relativeLength));

        return featuresMap;
    }

}
