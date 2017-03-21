package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAIdentityFeatureAbsolute;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAIdentityFeatureRelative;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyFeatureAbsolute;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyFeatureRelative;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyRelationshipFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ComplementaryIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ComplementaryIonFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.PeptideAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.PeptideFeature;

/**
 * Interface for an ms2pip feature.
 *
 * @author Marc Vaudel
 */
public interface Ms2pipFeature {
    
    /**
     * Array of the implemented features.
     */
    public static final Class[] implementations = {PeptideFeature.class, PeptideAminoAcidFeature.class, ForwardIonFeature.class, ComplementaryIonFeature.class, ForwardIonAminoAcidFeature.class, ComplementaryIonAminoAcidFeature.class,
        AAPropertyFeatureAbsolute.class, AAPropertyFeatureRelative.class, AAPropertyRelationshipFeature.class, AAIdentityFeatureAbsolute.class, AAIdentityFeatureRelative.class};

    /**
     * Returns the category of the feature.
     * 
     * @return the category of the feature
     */
    public String getCategory();

    /**
     * Returns the description of the feature.
     * 
     * @return the description of the feature
     */
    public String getDescription();
    
    /**
     * Returns an integer unique to this class.
     * 
     * @return an integer unique to this class
     */
    public int getIndex();
}
