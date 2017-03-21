package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_generation;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.FeaturesMap;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.MultipleAAPropertyFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.SingleAAPropertyFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAIdentityFeatureAbsolute;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAIdentityFeatureRelative;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyFeatureAbsolute;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyFeatureRelative;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyRelationshipFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ComplementaryIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ComplementaryIonFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ModificationFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.PeptideAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.PeptideFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.AAPropertyFeature;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class computes the ms2PIP features from a peptide.
 *
 * It is a java implementation of
 * https://github.com/mvaudel/ms2pip_c/blob/master/ms2pipfeatures_c.c with the
 * permission of Sven Degroeve. No license found, no copyright infringement
 * intended.
 *
 * @author Marc Vaudel
 */
public class FeaturesGenerator {

    /**
     * The PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The map of the different features to include.
     */
    private final FeaturesMap featuresMap;

    /**
     * Constructor.
     *
     * @param featuresMap a map of the features to include.
     */
    public FeaturesGenerator(FeaturesMap featuresMap) {
        this.featuresMap = featuresMap;
    }

    /**
     * Returns the ms2pip features for the forward ions of the given peptide at
     * the given charge.
     *
     * @param peptide the peptide
     * @param charge the charge
     *
     * @return the ms2pip features for the b ions
     */
    public int[][] getForwardIonsFeatures(Peptide peptide, int charge) {

        char[] peptideSequence = peptide.getSequence().toCharArray();
        ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
        return getIonsFeatures(peptideSequence, modificationMatches, charge);
    }

    /**
     * Returns the ms2pip features for the complementary ions of the given
     * peptide at the given charge.
     *
     * @param peptide the peptide
     * @param charge the charge
     *
     * @return the ms2pip features for the b ions
     */
    public int[][] getComplementaryIonsFeatures(Peptide peptide, int charge) {

        char[] peptideSequence = peptide.getSequence().toCharArray();
        int sequenceLength = peptideSequence.length;
        char[] reversedSequence = new char[sequenceLength];
        for (int i = 0; i < sequenceLength; i++) {
            reversedSequence[i] = peptideSequence[sequenceLength - i - 1];
        }
        ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
        ArrayList<ModificationMatch> reversedModificationMatches = new ArrayList<ModificationMatch>(modificationMatches.size());
        for (ModificationMatch modificationMatch : modificationMatches) {
            ModificationMatch reversedModificationMatch = new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), sequenceLength - modificationMatch.getModificationSite() + 1);
            reversedModificationMatches.add(reversedModificationMatch);
        }

        return getIonsFeatures(reversedSequence, reversedModificationMatches, charge);
    }

    /**
     * Returns the ms2pip features for the ions of the given sequence with
     * modifications at the given charge.
     *
     * @param peptideSequence the peptide sequence as char array
     * @param modificationMatches the modification matches
     * @param charge the charge
     *
     * @return the ms2pip features for the b ions
     */
    private int[][] getIonsFeatures(char[] peptideSequence, ArrayList<ModificationMatch> modificationMatches, int charge) {

        // Get the properties needed for peptides, ions, and amino acids
        AminoAcid.Property[] peptideProperties = getAaProperties(PeptideAminoAcidFeature.class);
        AminoAcid.Property[] forwardIonProperties = getAaProperties(ForwardIonAminoAcidFeature.class);
        AminoAcid.Property[] complementaryIonProperties = getAaProperties(ComplementaryIonAminoAcidFeature.class);
        AminoAcid.Property[] individualAaProperties = getAaProperties(AAPropertyFeatureAbsolute.class, AAPropertyFeatureRelative.class, AAPropertyRelationshipFeature.class);

        // Get the properties along the peptide sequence
        PeptideAttributes peptideAttributes = new PeptideAttributes(peptideSequence, modificationMatches, peptideProperties, forwardIonProperties, complementaryIonProperties, individualAaProperties);

        // Prepare an array for the resutls
        int[][] features = new int[peptideSequence.length - 1][featuresMap.getnFeatures()];

        // Iterate the sequence
        for (int i = 0; i < peptideSequence.length - 1; i++) {

            // Iterate the different features categories
            int featureIndex = 0;
            for (String category : featuresMap.getCategories()) {

                // Iterate the features for this category
                for (Ms2pipFeature ms2pipFeature : featuresMap.getFeatures(category)) {

                    // Add the feature value to the array
                    features[i][featureIndex++] = getFeatureValue(ms2pipFeature, peptideSequence, charge, peptideAttributes, i);
                }
            }
        }

        return features;

    }

    /**
     * Extracts the amino acid properties needed in the features map for the
     * given categories.
     *
     * @param featureClasses the categories of interest
     *
     * @return the amino acid properties needed
     */
    private AminoAcid.Property[] getAaProperties(Class... categories) {
        HashSet<Integer> indexes = new HashSet<Integer>(4);
        for (Class category : categories) {
            Ms2pipFeature[] features = featuresMap.getFeatures(category.getName());
            for (Ms2pipFeature ms2pipFeature : features) {
                if (ms2pipFeature instanceof SingleAAPropertyFeature) {
                    SingleAAPropertyFeature singleAAPropertyFeature = (SingleAAPropertyFeature) ms2pipFeature;
                    AminoAcid.Property property = singleAAPropertyFeature.getAminoAcidProperty();
                    indexes.add(property.ordinal());
                } else if (ms2pipFeature instanceof MultipleAAPropertyFeature) {
                    MultipleAAPropertyFeature multipleAAPropertyFeature = (MultipleAAPropertyFeature) ms2pipFeature;
                    for (AminoAcid.Property property : multipleAAPropertyFeature.getAminoAcidProperties()) {
                        indexes.add(property.ordinal());
                    }
                } else {
                    throw new UnsupportedOperationException("Properties extraction not implemented for feature of class " + ms2pipFeature.getCategory() + ".");
                }
            }
        }
        AminoAcid.Property[] aminoAcidProperties = AminoAcid.Property.values();
        AminoAcid.Property[] peptideAminoAcidProperties = new AminoAcid.Property[indexes.size()];
        int cpt = 0;
        for (int index : indexes) {
            peptideAminoAcidProperties[cpt++] = aminoAcidProperties[index];
        }
        return peptideAminoAcidProperties;
    }

    /**
     * Returns the value for the given feature.
     *
     * @param ms2pipFeature the ms2pip feature of interest
     * @param peptideSequence the peptide sequence as char array
     * @param charge the charge
     * @param peptideAttributes the peptide attributes
     * @param aaIndex the current index of the amino acid on the peptide
     * sequence
     *
     * @return the value of the feature
     */
    private int getFeatureValue(Ms2pipFeature ms2pipFeature, char[] peptideSequence, int charge, PeptideAttributes peptideAttributes, int aaIndex) {

        switch (ms2pipFeature.getIndex()) {
            case PeptideFeature.index:
                PeptideFeature peptideFeature = (PeptideFeature) ms2pipFeature;
                return getPeptideFeature(peptideFeature, peptideSequence.length, charge, peptideAttributes);
            case PeptideAminoAcidFeature.index:
                PeptideAminoAcidFeature peptideAminoAcidFeature = (PeptideAminoAcidFeature) ms2pipFeature;
                return getPeptideAminoAcidFeature(peptideAminoAcidFeature, peptideSequence.length, peptideAttributes);
            case ForwardIonFeature.index:
                ForwardIonFeature forwardIonFeature = (ForwardIonFeature) ms2pipFeature;
                return getForwardIonFeature(forwardIonFeature, peptideSequence.length, peptideAttributes, aaIndex);
            case ForwardIonAminoAcidFeature.index:
                ForwardIonAminoAcidFeature forwardIonAminoAcidFeature = (ForwardIonAminoAcidFeature) ms2pipFeature;
                return getForwardIonAminoAcidFeature(forwardIonAminoAcidFeature, peptideAttributes, aaIndex);
            case ComplementaryIonFeature.index:
                ComplementaryIonFeature complementaryIonFeature = (ComplementaryIonFeature) ms2pipFeature;
                return getComplementaryIonFeature(complementaryIonFeature, peptideSequence.length, peptideAttributes, aaIndex);
            case ComplementaryIonAminoAcidFeature.index:
                ComplementaryIonAminoAcidFeature complementaryIonAminoAcidFeature = (ComplementaryIonAminoAcidFeature) ms2pipFeature;
                return getComplementaryIonAminoAcidFeature(complementaryIonAminoAcidFeature, peptideSequence.length, peptideAttributes, aaIndex);
            case AAPropertyFeatureAbsolute.index:
                AAPropertyFeatureAbsolute aaPropertyFeatureAbsolute = (AAPropertyFeatureAbsolute) ms2pipFeature;
                return getAAPropertyFeatureAbsolute(aaPropertyFeatureAbsolute, peptideSequence.length, peptideAttributes);
            case AAPropertyFeatureRelative.index:
                AAPropertyFeatureRelative aaPropertyFeatureRelative = (AAPropertyFeatureRelative) ms2pipFeature;
                return getAAPropertyFeatureRelative(aaPropertyFeatureRelative, peptideSequence.length, peptideAttributes, aaIndex);
            case AAPropertyRelationshipFeature.index:
                AAPropertyRelationshipFeature aaPropertyRelationshipFeature = (AAPropertyRelationshipFeature) ms2pipFeature;
                return getAAPropertyRelationshipFeature(aaPropertyRelationshipFeature, peptideSequence.length, peptideAttributes, aaIndex);
            case AAIdentityFeatureAbsolute.index:
                AAIdentityFeatureAbsolute aaIdentityFeatureAbsolute = (AAIdentityFeatureAbsolute) ms2pipFeature;
                return getAAIdentityFeatureAbsolute(aaIdentityFeatureAbsolute, peptideSequence);
            case AAIdentityFeatureRelative.index:
                AAIdentityFeatureRelative aaIdentityFeatureRelative = (AAIdentityFeatureRelative) ms2pipFeature;
                return getAAIdentityFeatureRelative(aaIdentityFeatureRelative, peptideSequence, aaIndex);
            case ModificationFeature.index:
                ModificationFeature modificationFeature = (ModificationFeature) ms2pipFeature;
                return getModificationFeature(modificationFeature, peptideAttributes, aaIndex);
            default:
                throw new UnsupportedOperationException("Feature " + ms2pipFeature.getClass().getName() + " not implemented.");
        }

    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param sequenceLength the peptide sequence length
     * @param charge the charge
     * @param peptideAttributes the peptide attributes
     *
     * @return the requested feature
     */
    private int getPeptideFeature(PeptideFeature feature, int sequenceLength, int charge, PeptideAttributes peptideAttributes) {

        switch (feature.getProperty()) {
            case charge:
                return charge;
            case length:
                return sequenceLength;
            case mass:
                return (int) peptideAttributes.getPeptideMass();
            default:
                throw new UnsupportedOperationException("Feature " + feature.getDescription() + " not implemented.");
        }
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param sequenceLength the peptide sequence length
     * @param peptideAttributes the peptide attributes
     *
     * @return the requested feature
     */
    private int getPeptideAminoAcidFeature(PeptideAminoAcidFeature feature, int sequenceLength, PeptideAttributes peptideAttributes) {

        switch (feature.getFunction()) {
            case sum:
                return (int) peptideAttributes.getSumPeptideAminoAcidProperties(feature.getAminoAcidProperty());
            case mean:
                double value = peptideAttributes.getSumPeptideAminoAcidProperties(feature.getAminoAcidProperty());
                return (int) (value / sequenceLength);
            case minimum:
                return (int) peptideAttributes.getMinPeptideAminoAcidProperties(feature.getAminoAcidProperty());
            case maximum:
                return (int) peptideAttributes.getMaxPeptideAminoAcidProperties(feature.getAminoAcidProperty());
            default:
                throw new UnsupportedOperationException("Feature " + feature.getDescription() + " not implemented.");
        }
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param sequenceLength the peptide sequence length
     * @param peptideAttributes the peptide attributes
     * @param aaIndex the index on the sequence
     *
     * @return the requested feature
     */
    private int getForwardIonFeature(ForwardIonFeature feature, int sequenceLength, PeptideAttributes peptideAttributes, int aaIndex) {

        switch (feature.getFeature()) {
            case mass:
                return (int) peptideAttributes.getForwardIonMass(aaIndex);
            case massOverLength:
                double value = peptideAttributes.getForwardIonMass(aaIndex);
                int ionLength = aaIndex + 1;
                return (int) (value / ionLength);
            case length:
                return aaIndex + 1;
            case relativeLength:
                return (int) (100.0 * (aaIndex + 1) / sequenceLength);
            default:
                throw new UnsupportedOperationException("Feature " + feature.getDescription() + " not implemented.");
        }
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param peptideAttributes the peptide attributes
     * @param aaIndex the index on the sequence
     *
     * @return the requested feature
     */
    private int getForwardIonAminoAcidFeature(ForwardIonAminoAcidFeature feature, PeptideAttributes peptideAttributes, int aaIndex) {

        switch (feature.getFunction()) {
            case sum:
                return (int) peptideAttributes.getSumForwardIonAminoAcidProperties(feature.getAminoAcidProperty(), aaIndex);
            case mean:
                double value = peptideAttributes.getSumForwardIonAminoAcidProperties(feature.getAminoAcidProperty(), aaIndex);
                int ionLength = aaIndex + 1;
                return (int) (value / ionLength);
            case minimum:
                return (int) peptideAttributes.getMinForwardIonAminoAcidProperties(feature.getAminoAcidProperty(), aaIndex);
            case maximum:
                return (int) peptideAttributes.getMaxForwardIonAminoAcidProperties(feature.getAminoAcidProperty(), aaIndex);
            default:
                throw new UnsupportedOperationException("Feature " + feature.getDescription() + " not implemented.");
        }
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param sequenceLength the peptide sequence length
     * @param peptideAttributes the peptide attributes
     * @param aaIndex the index on the sequence
     *
     * @return the requested feature
     */
    private int getComplementaryIonFeature(ComplementaryIonFeature feature, int sequenceLength, PeptideAttributes peptideAttributes, int aaIndex) {

        switch (feature.getFeature()) {
            case mass:
                double value = peptideAttributes.getPeptideMass() - peptideAttributes.getForwardIonMass(aaIndex);
                return (int) value;
            case massOverLength:
                value = peptideAttributes.getPeptideMass() - peptideAttributes.getForwardIonMass(aaIndex);
                int ionLength = sequenceLength - aaIndex - 1;
                return (int) (value / ionLength);
            case length:
                return aaIndex + 1;
            case relativeLength:
                ionLength = sequenceLength - aaIndex - 1;
                return (int) (100.0 * (ionLength) / sequenceLength);
            default:
                throw new UnsupportedOperationException("Feature " + feature.getDescription() + " not implemented.");
        }
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param sequenceLength the peptide sequence length
     * @param peptideAttributes the peptide attributes
     * @param aaIndex the index on the sequence
     *
     * @return the requested feature
     */
    private int getComplementaryIonAminoAcidFeature(ComplementaryIonAminoAcidFeature feature, int sequenceLength, PeptideAttributes peptideAttributes, int aaIndex) {

        switch (feature.getFunction()) {
            case sum:
                return (int) peptideAttributes.getSumComplementaryIonAminoAcidProperties(feature.getAminoAcidProperty(), aaIndex);
            case mean:
                double value = peptideAttributes.getSumComplementaryIonAminoAcidProperties(feature.getAminoAcidProperty(), aaIndex);
                int ionLength = sequenceLength - aaIndex - 1;
                return (int) (value / ionLength);
            case minimum:
                return (int) peptideAttributes.getMinComplementaryIonAminoAcidProperties(feature.getAminoAcidProperty(), aaIndex);
            case maximum:
                return (int) peptideAttributes.getMaxComplementaryIonAminoAcidProperties(feature.getAminoAcidProperty(), aaIndex);
            default:
                throw new UnsupportedOperationException("Feature " + feature.getDescription() + " not implemented.");
        }
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param sequenceLength the peptide sequence length
     * @param peptideAttributes the peptide attributes
     *
     * @return the requested feature
     */
    private int getAAPropertyFeatureAbsolute(AAPropertyFeatureAbsolute feature, int sequenceLength, PeptideAttributes peptideAttributes) {

        int sequenceIndex = getSequenceIndexAbsolute(feature.getAaIndex(), sequenceLength);
        return (int) peptideAttributes.getAminoAcidProperties(feature.getAminoAcidProperty(), sequenceIndex);
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param sequenceLength the peptide sequence length
     * @param peptideAttributes the peptide attributes
     * @param aaIndex the index on the sequence
     *
     * @return the requested feature
     */
    private int getAAPropertyFeatureRelative(AAPropertyFeatureRelative feature, int sequenceLength, PeptideAttributes peptideAttributes, int aaIndex) {

        int sequenceIndex = getSequenceIndexRelative(feature.getAaIndex(), sequenceLength, aaIndex);
        return (int) peptideAttributes.getAminoAcidProperties(feature.getAminoAcidProperty(), sequenceIndex);
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param sequenceLength the peptide sequence length
     * @param peptideAttributes the peptide attributes
     * @param aaIndex the index on the sequence
     *
     * @return the requested feature
     */
    private int getAAPropertyRelationshipFeature(AAPropertyRelationshipFeature feature, int sequenceLength, PeptideAttributes peptideAttributes, int aaIndex) {

        AAPropertyFeature aaPropertyFeature1 = feature.getAminoAcidFeature1();
        double value1;
        if (aaPropertyFeature1 instanceof AAPropertyFeatureAbsolute) {
            AAPropertyFeatureAbsolute subFeature = (AAPropertyFeatureAbsolute) aaPropertyFeature1;
            int sequenceIndex = getSequenceIndexAbsolute(subFeature.getAaIndex(), sequenceLength);
            value1 = peptideAttributes.getAminoAcidProperties(subFeature.getAminoAcidProperty(), sequenceIndex);
        } else if (aaPropertyFeature1 instanceof AAPropertyFeatureRelative) {
            AAPropertyFeatureRelative subFeature = (AAPropertyFeatureRelative) aaPropertyFeature1;
            int sequenceIndex = getSequenceIndexRelative(subFeature.getAaIndex(), sequenceLength, aaIndex);
            value1 = peptideAttributes.getAminoAcidProperties(subFeature.getAminoAcidProperty(), sequenceIndex);
        } else {
            throw new UnsupportedOperationException("Feature " + aaPropertyFeature1.getDescription() + " not implemented.");
        }

        AAPropertyFeature aaPropertyFeature2 = feature.getAminoAcidFeature2();
        double value2;
        if (aaPropertyFeature2 instanceof AAPropertyFeatureAbsolute) {
            AAPropertyFeatureAbsolute subFeature = (AAPropertyFeatureAbsolute) aaPropertyFeature2;
            int sequenceIndex = getSequenceIndexAbsolute(subFeature.getAaIndex(), sequenceLength);
            value2 = peptideAttributes.getAminoAcidProperties(subFeature.getAminoAcidProperty(), sequenceIndex);
        } else if (aaPropertyFeature2 instanceof AAPropertyFeatureRelative) {
            AAPropertyFeatureRelative subFeature = (AAPropertyFeatureRelative) aaPropertyFeature2;
            int sequenceIndex = getSequenceIndexRelative(subFeature.getAaIndex(), sequenceLength, aaIndex);
            value2 = peptideAttributes.getAminoAcidProperties(subFeature.getAminoAcidProperty(), sequenceIndex);
        } else {
            throw new UnsupportedOperationException("Feature " + aaPropertyFeature2.getDescription() + " not implemented.");
        }

        switch (feature.getRelationship()) {
            case addition:
                return (int) (value1 + value2);
            case multiplication:
                return (int) (value1 * value2);
            case subtraction:
                return (int) (value1 - value2);
            default:
                throw new UnsupportedOperationException("Operation " + feature.getRelationship() + " not implemented.");
        }
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param peptideSequence the peptide sequence as char array
     *
     * @return the requested feature
     */
    private int getAAIdentityFeatureAbsolute(AAIdentityFeatureAbsolute feature, char[] peptideSequence) {

        int sequenceIndex = getSequenceIndexAbsolute(feature.getAaIndex(), peptideSequence.length);
        return peptideSequence[sequenceIndex] == feature.getAminoAcid() ? 1 : 0;
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param peptideSequence the peptide sequence as char array
     * @param aaIndex the index on the sequence
     *
     * @return the requested feature
     */
    private int getAAIdentityFeatureRelative(AAIdentityFeatureRelative feature, char[] peptideSequence, int aaIndex) {

        int sequenceIndex = getSequenceIndexRelative(feature.getAaIndex(), peptideSequence.length, aaIndex);
        return peptideSequence[sequenceIndex] == feature.getAminoAcid() ? 1 : 0;
    }

    /**
     * Returns the requested feature value.
     *
     * @param feature the ms2pip feature
     * @param peptideAttributes the peptide attributes
     * @param aaIndex the index on the sequence
     *
     * @return the requested feature
     */
    private int getModificationFeature(ModificationFeature feature, PeptideAttributes peptideAttributes, int aaIndex) {

        switch (feature.getProperty()) {
            case mass:
                return (int) peptideAttributes.getModificationMass(aaIndex);
            default:
                throw new UnsupportedOperationException("Feature " + feature.getDescription() + " not implemented.");
        }
    }

    /**
     * Returns the peptide sequence index based on the index of an absolute
     * feature.
     *
     * @param featureIndex the index of the feature
     * @param sequenceLength the peptide sequence length
     *
     * @return the peptide sequence index
     */
    private int getSequenceIndexAbsolute(int featureIndex, int sequenceLength) {

        int sequenceIndex = featureIndex;
        if (sequenceIndex < 0) {
            sequenceIndex = sequenceLength + sequenceIndex;
        }
        return sequenceIndex;
    }

    /**
     * Returns the peptide sequence index based on the index of an relative
     * feature.
     *
     * @param featureIndex the index of the feature
     * @param sequenceLength the peptide sequence length
     * @param aaIndex the current index on the sequence
     *
     * @return the peptide sequence index
     */
    private int getSequenceIndexRelative(int featureIndex, int sequenceLength, int aaIndex) {

        int sequenceIndex = aaIndex + featureIndex;
        if (sequenceIndex < 0) {
            sequenceIndex = 0;
        }
        if (sequenceIndex >= sequenceLength) {
            sequenceIndex = sequenceLength - 1;
        }
        return sequenceIndex;
    }

    /**
     * This class estimates and stores reference attributes of the peptide.
     */
    private class PeptideAttributes {

        /**
         * The peptide mass.
         */
        private double peptideMass;
        /**
         * The minimal value of amino acid properties along a peptide sequence.
         */
        private double[] minPeptideAminoAcidProperties;
        /**
         * The maximal value of amino acid properties along a peptide sequence.
         */
        private double[] maxPeptideAminoAcidProperties;
        /**
         * The sum of amino acid properties along a peptide sequence.
         */
        private double[] sumPeptideAminoAcidProperties;
        /**
         * The minimal value of amino acid properties on forward ions derived
         * from a sequence.
         */
        private double[][] minForwardIonAminoAcidProperties;
        /**
         * The maximal value of amino acid properties on forward ions derived
         * from a sequence.
         */
        private double[][] maxForwardIonAminoAcidProperties;
        /**
         * The sum of amino acid properties on forward ions derived from a
         * sequence.
         */
        private double[][] sumForwardIonAminoAcidProperties;
        /**
         * The minimal value of amino acid properties on complementary ions
         * derived from a sequence.
         */
        private double[][] minComplementaryIonAminoAcidProperties;
        /**
         * The maximal value of amino acid properties on complementary ions
         * derived from a sequence.
         */
        private double[][] maxComplementaryIonAminoAcidProperties;
        /**
         * The sum of amino acid properties on complementary ions derived from a
         * sequence.
         */
        private double[][] sumComplementaryIonAminoAcidProperties;
        /**
         * The mass of forward ions derived from a sequence.
         */
        private double[] forwardIonMass;
        /**
         * The properties of all amino acids in a sequence.
         */
        private double[][] aminoAcidProperties;
        /**
         * The mass added by modifications on the amino acids.
         */
        private double[] modificationsMasses;

        /**
         * Constructor.
         *
         * @param peptideSequence a peptide sequence as char array
         * @param modificationMatches the modifications carried by the sequence
         * @param peptideAminoAcidProperties
         * @param forwardIonAminoAcidProperties
         * @param complementaryIonAminoAcidProperties
         * @param individualAminoAcidProperties
         */
        private PeptideAttributes(char[] peptideSequence, ArrayList<ModificationMatch> modificationMatches,
                AminoAcid.Property[] peptideAminoAcidProperties, AminoAcid.Property[] forwardIonAminoAcidProperties,
                AminoAcid.Property[] complementaryIonAminoAcidProperties, AminoAcid.Property[] individualAminoAcidProperties) {

            // Values used during iteration
            peptideMass = 0.0;

            // Initialize arrays
            int nPossibleProperties = AminoAcid.Property.getNProperties();
            minPeptideAminoAcidProperties = new double[nPossibleProperties];
            maxPeptideAminoAcidProperties = new double[nPossibleProperties];
            sumPeptideAminoAcidProperties = new double[nPossibleProperties];
            minForwardIonAminoAcidProperties = new double[peptideSequence.length][nPossibleProperties];
            maxForwardIonAminoAcidProperties = new double[peptideSequence.length][nPossibleProperties];
            sumForwardIonAminoAcidProperties = new double[peptideSequence.length][nPossibleProperties];
            minComplementaryIonAminoAcidProperties = new double[peptideSequence.length][nPossibleProperties];
            maxComplementaryIonAminoAcidProperties = new double[peptideSequence.length][nPossibleProperties];
            sumComplementaryIonAminoAcidProperties = new double[peptideSequence.length][nPossibleProperties];

            forwardIonMass = new double[peptideSequence.length];
            modificationsMasses = new double[peptideSequence.length];

            aminoAcidProperties = new double[peptideSequence.length][nPossibleProperties];

            // Set initial forward values using the first amino acid
            char aa = peptideSequence[0];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            // Peptide and ion mass
            peptideMass += aminoAcid.getMonoisotopicMass();
            forwardIonMass[0] = peptideMass;
            // Peptide amino acid properties
            for (AminoAcid.Property property : peptideAminoAcidProperties) {
                double value = aminoAcid.getProperty(property);
                minPeptideAminoAcidProperties[property.ordinal()] = value;
                maxPeptideAminoAcidProperties[property.ordinal()] = value;
                sumPeptideAminoAcidProperties[property.ordinal()] = value;
            }
            // Forward ion amino acid properties
            for (AminoAcid.Property property : forwardIonAminoAcidProperties) {
                double value = aminoAcid.getProperty(property);
                minForwardIonAminoAcidProperties[0][property.ordinal()] = value;
                maxForwardIonAminoAcidProperties[0][property.ordinal()] = value;
                sumForwardIonAminoAcidProperties[0][property.ordinal()] = value;
            }
            // Individual amino acid properties
            for (AminoAcid.Property property : individualAminoAcidProperties) {
                double value = aminoAcid.getProperty(property);
                aminoAcidProperties[0][property.ordinal()] = value;
            }

            // Set initial complementary values using the first amino acid
            aa = peptideSequence[peptideSequence.length - 1];
            aminoAcid = AminoAcid.getAminoAcid(aa);
            // Complementary ion amino acid properties
            for (AminoAcid.Property property : complementaryIonAminoAcidProperties) {
                double value = aminoAcid.getProperty(property);
                minComplementaryIonAminoAcidProperties[peptideSequence.length - 1][property.ordinal()] = value;
                maxComplementaryIonAminoAcidProperties[peptideSequence.length - 1][property.ordinal()] = value;
                sumComplementaryIonAminoAcidProperties[peptideSequence.length - 1][property.ordinal()] = value;
            }

            // Iterate the amino acids and gather the different metrics needed
            for (int forwardI = 1; forwardI < peptideSequence.length; forwardI++) {

                aa = peptideSequence[forwardI];
                aminoAcid = AminoAcid.getAminoAcid(aa);

                // Peptide and ion mass
                peptideMass += aminoAcid.getMonoisotopicMass();
                forwardIonMass[forwardI] = peptideMass;

                // Min, max and sum of the different peptide properties needed
                for (AminoAcid.Property property : peptideAminoAcidProperties) {
                    double value = aminoAcid.getProperty(property);
                    if (value < minPeptideAminoAcidProperties[property.ordinal()]) {
                        minPeptideAminoAcidProperties[property.ordinal()] = value;
                    }
                    if (value > maxPeptideAminoAcidProperties[property.ordinal()]) {
                        maxPeptideAminoAcidProperties[property.ordinal()] = value;
                    }
                    sumPeptideAminoAcidProperties[property.ordinal()] += value;
                }

                // Min, max and sum of the different forward ion properties needed
                for (AminoAcid.Property property : forwardIonAminoAcidProperties) {
                    double value = aminoAcid.getProperty(property);
                    double previousValue = minForwardIonAminoAcidProperties[forwardI - 1][property.ordinal()];
                    minForwardIonAminoAcidProperties[forwardI][property.ordinal()] = value < previousValue ? value : previousValue;
                    previousValue = maxForwardIonAminoAcidProperties[forwardI - 1][property.ordinal()];
                    maxForwardIonAminoAcidProperties[forwardI][property.ordinal()] = value > previousValue ? value : previousValue;
                    sumForwardIonAminoAcidProperties[forwardI][property.ordinal()] = sumForwardIonAminoAcidProperties[forwardI - 1][property.ordinal()] + value;
                }

                // Individual amino acid properties
                for (AminoAcid.Property property : individualAminoAcidProperties) {
                    double value = aminoAcid.getProperty(property);
                    aminoAcidProperties[forwardI][property.ordinal()] = value;
                }

                // Complementary amino acid
                int complementaryI = peptideSequence.length - 1 - forwardI;
                aa = peptideSequence[complementaryI];
                aminoAcid = AminoAcid.getAminoAcid(aa);

                // Min, max and sum of the different complementary ion properties needed
                for (AminoAcid.Property property : complementaryIonAminoAcidProperties) {
                    double value = aminoAcid.getProperty(property);
                    double previousValue = minComplementaryIonAminoAcidProperties[complementaryI + 1][property.ordinal()];
                    minComplementaryIonAminoAcidProperties[complementaryI][property.ordinal()] = value < previousValue ? value : previousValue;
                    previousValue = maxComplementaryIonAminoAcidProperties[complementaryI + 1][property.ordinal()];
                    maxComplementaryIonAminoAcidProperties[complementaryI][property.ordinal()] = value > previousValue ? value : previousValue;
                    sumComplementaryIonAminoAcidProperties[complementaryI][property.ordinal()] = sumComplementaryIonAminoAcidProperties[complementaryI + 1][property.ordinal()] + value;
                }
            }

            // Iterate modifications
            if (modificationMatches != null) {
                for (ModificationMatch modificationMatch : modificationMatches) {

                    String modificationName = modificationMatch.getTheoreticPtm();
                    PTM modification = ptmFactory.getPTM(modificationName);
                    double modificationMass = modification.getMass();
                    int modificationSite = modificationMatch.getModificationSite();

                    peptideMass += modificationMass;

                    modificationsMasses[modificationSite - 1] += modificationMass;

                    for (int i = modificationSite - 1; i < peptideSequence.length; i++) {
                        forwardIonMass[i] += modificationMass;
                    }
                }
            }
        }

        /**
         * Returns the peptide mass.
         *
         * @return the peptide mass
         */
        public double getPeptideMass() {
            return peptideMass;
        }

        /**
         * Returns the minimal amino acid property along the peptide sequence.
         *
         * @param property the amino acid property
         *
         * @return the minimal amino acid property along the peptide sequence
         */
        public double getMinPeptideAminoAcidProperties(AminoAcid.Property property) {
            return minPeptideAminoAcidProperties[property.ordinal()];
        }

        /**
         * Returns the maximal amino acid property along the peptide sequence.
         *
         * @param property the amino acid property
         *
         * @return the maximal amino acid property along the peptide sequence
         */
        public double getMaxPeptideAminoAcidProperties(AminoAcid.Property property) {
            return maxPeptideAminoAcidProperties[property.ordinal()];
        }

        /**
         * Returns the summed amino acid property along the peptide sequence.
         *
         * @param property the amino acid property
         *
         * @return the summed amino acid property along the peptide sequence
         */
        public double getSumPeptideAminoAcidProperties(AminoAcid.Property property) {
            return sumPeptideAminoAcidProperties[property.ordinal()];
        }

        /**
         * Returns the minimal amino acid property along the peptide sequence
         * until the given index.
         *
         * @param aaIndex an index on the amino acid sequence
         * @param property the amino acid property
         *
         * @return the minimal amino acid property along the peptide sequence
         * until the given index
         */
        public double getMinForwardIonAminoAcidProperties(AminoAcid.Property property, int aaIndex) {
            return minForwardIonAminoAcidProperties[aaIndex][property.ordinal()];
        }

        /**
         * Returns the maximal amino acid property along the peptide sequence
         * until the given index.
         *
         * @param aaIndex an index on the amino acid sequence
         * @param property the amino acid property
         *
         * @return the maximal amino acid property along the peptide sequence
         * until the given index
         */
        public double getMaxForwardIonAminoAcidProperties(AminoAcid.Property property, int aaIndex) {
            return maxForwardIonAminoAcidProperties[aaIndex][property.ordinal()];
        }

        /**
         * Returns the summed amino acid property along the peptide sequence
         * until the given index.
         *
         * @param aaIndex an index on the amino acid sequence
         * @param property the amino acid property
         *
         * @return the summed amino acid property along the peptide sequence
         * until the given index
         */
        public double getSumForwardIonAminoAcidProperties(AminoAcid.Property property, int aaIndex) {
            return sumForwardIonAminoAcidProperties[aaIndex][property.ordinal()];
        }

        /**
         * Returns the minimal amino acid property along the peptide sequence
         * from the given index to the end.
         *
         * @param aaIndex an index on the amino acid sequence
         * @param property the amino acid property
         *
         * @return the minimal amino acid property along the peptide sequence
         * from the given index to the end
         */
        public double getMinComplementaryIonAminoAcidProperties(AminoAcid.Property property, int aaIndex) {
            return minComplementaryIonAminoAcidProperties[aaIndex][property.ordinal()];
        }

        /**
         * Returns the maximal amino acid property along the peptide sequence
         * from the given index to the end.
         *
         * @param aaIndex an index on the amino acid sequence
         * @param property the amino acid property
         *
         * @return the maximal amino acid property along the peptide sequence
         * from the given index to the end
         */
        public double getMaxComplementaryIonAminoAcidProperties(AminoAcid.Property property, int aaIndex) {
            return maxComplementaryIonAminoAcidProperties[aaIndex][property.ordinal()];
        }

        /**
         * Returns the summed amino acid property along the peptide sequence
         * from the given index to the end.
         *
         * @param aaIndex an index on the amino acid sequence
         * @param property the amino acid property
         *
         * @return the summed amino acid property along the peptide sequence
         * from the given index to the end
         */
        public double getSumComplementaryIonAminoAcidProperties(AminoAcid.Property property, int aaIndex) {
            return sumComplementaryIonAminoAcidProperties[aaIndex][property.ordinal()];
        }

        /**
         * Returns the mass of the amino acid sequence until the given index.
         *
         * @param aaIndex an index on the amino acid sequence
         *
         * @return the mass of the amino acid sequence until the given index
         */
        public double getForwardIonMass(int aaIndex) {
            return forwardIonMass[aaIndex];
        }

        /**
         * Returns the value of the property of interest of the amino acid at
         * the given index.
         *
         * @param aaIndex an index on the amino acid sequence
         * @param property the amino acid property
         *
         * @return the value of the property of interest of the amino acid at
         * the given index
         */
        public double getAminoAcidProperties(AminoAcid.Property property, int aaIndex) {
            return aminoAcidProperties[aaIndex][property.ordinal()];
        }

        /**
         * Returns the mass added by modifications at a given amino acid.
         *
         * @param aaIndex the amino acid index on the sequence
         *
         * @return the mass added by modifications
         */
        public double getModificationMass(int aaIndex) {
            return modificationsMasses[aaIndex];
        }

    }
}
