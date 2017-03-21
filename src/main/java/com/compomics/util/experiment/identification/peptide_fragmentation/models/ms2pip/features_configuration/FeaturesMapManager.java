package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAIdentityFeatureAbsolute;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAIdentityFeatureRelative;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyFeatureAbsolute;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyFeatureRelative;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyRelationshipFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.AASequenceFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ComplementaryIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ComplementaryIonFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ModificationFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.IonFeature;
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

        // N-term property features
        AminoAcid.Property[] properties = new AminoAcid.Property[]{AminoAcid.Property.mass, AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        for (AminoAcid.Property property : properties) {
            featuresMap.addFeature(new AAPropertyFeatureAbsolute(0, property));
        }

        // C-term property features
        for (AminoAcid.Property property : properties) {
            featuresMap.addFeature(new AAPropertyFeatureAbsolute(-1, property));
        }

        // Last amino acid property features
        for (AminoAcid.Property property : properties) {
            featuresMap.addFeature(new AAPropertyFeatureRelative(0, property));
        }

        // Forelast amino acid property features
        for (AminoAcid.Property property : properties) {
            featuresMap.addFeature(new AAPropertyFeatureRelative(-1, property));
        }

        // Next amino acid property features
        for (AminoAcid.Property property : properties) {
            featuresMap.addFeature(new AAPropertyFeatureRelative(+1, property));
        }

        // After next amino acid property features
        for (AminoAcid.Property property : properties) {
            featuresMap.addFeature(new AAPropertyFeatureRelative(+2, property));
        }
        
        // N-term identity features
        char[] aminoAcids = {'D', 'E', 'K', 'P', 'R'};
        for (char aminoAcid : aminoAcids) {
            featuresMap.addFeature(new AAIdentityFeatureAbsolute(0, aminoAcid));
        }
        
        // C-term identity features
        for (char aminoAcid : aminoAcids) {
            featuresMap.addFeature(new AAIdentityFeatureAbsolute(-1, aminoAcid));
        }
        
        // Last amino acid identity features
        for (char aminoAcid : aminoAcids) {
            featuresMap.addFeature(new AAIdentityFeatureRelative(0, aminoAcid));
        }
        
        // Next amino acid identity features
        for (char aminoAcid : aminoAcids) {
            featuresMap.addFeature(new AAIdentityFeatureRelative(1, aminoAcid));
        }

        // Peptide amino acid features
        featuresMap.addFeature(new PeptideAminoAcidFeature(AminoAcid.Property.mass, AASequenceFeature.Function.mean));
        properties = new AminoAcid.Property[]{AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        AASequenceFeature.Function[] functions = new AASequenceFeature.Function[]{AASequenceFeature.Function.mean, AASequenceFeature.Function.maximum, AASequenceFeature.Function.minimum};
        for (AminoAcid.Property property : properties) {
            for (AASequenceFeature.Function function : functions) {
                featuresMap.addFeature(new PeptideAminoAcidFeature(property, function));
            }
        }

        // Forward ion amino acid features
        properties = new AminoAcid.Property[]{AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        functions = new AASequenceFeature.Function[]{AASequenceFeature.Function.mean, AASequenceFeature.Function.maximum, AASequenceFeature.Function.minimum};
        for (AminoAcid.Property property : properties) {
            for (AASequenceFeature.Function function : functions) {
                featuresMap.addFeature(new ForwardIonAminoAcidFeature(property, function));
            }
        }

        // Complementary ion amino acid features
        properties = new AminoAcid.Property[]{AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        functions = new AASequenceFeature.Function[]{AASequenceFeature.Function.mean, AASequenceFeature.Function.maximum, AASequenceFeature.Function.minimum, AASequenceFeature.Function.sum};
        for (AminoAcid.Property property : properties) {
            for (AASequenceFeature.Function function : functions) {
                featuresMap.addFeature(new ComplementaryIonAminoAcidFeature(property, function));
            }
        }

        // Forward ion features
        featuresMap.addFeature(new ForwardIonFeature(IonFeature.Property.length));
        featuresMap.addFeature(new ForwardIonFeature(IonFeature.Property.relativeLength));
        featuresMap.addFeature(new ForwardIonFeature(IonFeature.Property.mass));
        featuresMap.addFeature(new ForwardIonFeature(IonFeature.Property.massOverLength));

        // Complementary ion features
        featuresMap.addFeature(new ComplementaryIonFeature(IonFeature.Property.mass));
        featuresMap.addFeature(new ComplementaryIonFeature(IonFeature.Property.massOverLength));

        // Combination of next and current amino acid features
        properties = new AminoAcid.Property[]{AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        AAPropertyRelationshipFeature.Relationship[] relationships = new AAPropertyRelationshipFeature.Relationship[]{AAPropertyRelationshipFeature.Relationship.addition, AAPropertyRelationshipFeature.Relationship.multiplication};
        for (AminoAcid.Property property : properties) {
            AAPropertyFeatureRelative currentAminoAcidFeature = new AAPropertyFeatureRelative(0, property);
            AAPropertyFeatureRelative nextAminoAcidFeature = new AAPropertyFeatureRelative(1, property);
            for (AAPropertyRelationshipFeature.Relationship relationship : relationships) {
                featuresMap.addFeature(new AAPropertyRelationshipFeature(relationship, currentAminoAcidFeature, nextAminoAcidFeature));
            }
            featuresMap.addFeature(new AAPropertyRelationshipFeature(AAPropertyRelationshipFeature.Relationship.subtraction, currentAminoAcidFeature, nextAminoAcidFeature));
            featuresMap.addFeature(new AAPropertyRelationshipFeature(AAPropertyRelationshipFeature.Relationship.subtraction, nextAminoAcidFeature, currentAminoAcidFeature));
        }

        // Combination of N-term and current amino acid features
        properties = new AminoAcid.Property[]{AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        for (AminoAcid.Property property : properties) {
            AAPropertyFeatureRelative currentAminoAcidFeature = new AAPropertyFeatureRelative(0, property);
            AAPropertyFeatureAbsolute nTermAminoAcid = new AAPropertyFeatureAbsolute(0, property);
            featuresMap.addFeature(new AAPropertyRelationshipFeature(AAPropertyRelationshipFeature.Relationship.addition, currentAminoAcidFeature, nTermAminoAcid));
        }

        // Combination of C-term and next amino acid features
        properties = new AminoAcid.Property[]{AminoAcid.Property.basicity, AminoAcid.Property.helicity, AminoAcid.Property.hydrophobicity, AminoAcid.Property.pI};
        for (AminoAcid.Property property : properties) {
            AAPropertyFeatureRelative currentAminoAcidFeature = new AAPropertyFeatureRelative(1, property);
            AAPropertyFeatureAbsolute cTermAminoAcid = new AAPropertyFeatureAbsolute(-1, property);
            featuresMap.addFeature(new AAPropertyRelationshipFeature(AAPropertyRelationshipFeature.Relationship.addition, currentAminoAcidFeature, cTermAminoAcid));
        }
        
        // Modifications features
        featuresMap.addFeature(new ModificationFeature(ModificationFeature.Property.mass));

        return featuresMap;
    }

}
