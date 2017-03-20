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
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyFeatureAbsolute;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyFeatureRelative;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.AAPropertyRelationshipFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ComplementaryIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.ForwardIonAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.PeptideAminoAcidFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.PeptideFeature;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final static double waterMass = 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();
    /**
     * The PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * Array of supported amino acids in single letter code.
     */
    public static final char[] supportedAminoAcids = {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y'};
    /**
     * The ms2pip index of every amino acid.
     */
    private final HashMap<Character, Integer> aaIndexes = getAminoAcidIndexes();
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
     * Returns the ms2pip features for the b ions of the given peptide at the
     * given charge.
     *
     * @param peptide the peptide
     * @param charge the charge
     *
     * @return the ms2pip features for the b ions
     */
    public int[][] getBIonsFeatures(Peptide peptide, int charge) {

        char[] peptideSequence = peptide.getSequence().toCharArray();
        ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();

        return getIonsFeatures(peptideSequence, modificationMatches, charge);
    }

    /**
     * Returns the ms2pip features for the y ions of the given peptide at the
     * given charge.
     *
     * @param peptide the peptide
     * @param charge the charge
     *
     * @return the ms2pip features for the b ions
     */
    public int[][] getYIonsFeatures(Peptide peptide, int charge) {

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
        int[][] peptideFeatures = new int[peptideSequence.length - 1][featuresMap.getnFeatures()];

        // Iterate the sequence
        for (int i = 0; i < peptideSequence.length - 1; i++) {

            // The peptide features
            Ms2pipFeature[] features = featuresMap.getFeatures(PeptideFeature.class.getName());
            for (Ms2pipFeature ms2pipFeature : features) {
                PeptideFeature peptideFeature = (PeptideFeature) ms2pipFeature;
                
            }

            peptideLength = peptideSequence.length;
        PeptideMetrics peptideMetrics = new PeptideMetrics(peptideSequence, modificationMatches);
            double[] aaMasses = peptideMetrics.getAaMasses();
            double[] modificationsMasses = peptideMetrics.getModificationsMasses();
            double[] aasMasses = peptideMetrics.getSumAaMasses();
            double[] aasMinMasses = peptideMetrics.getMinAaMasses();
            double[] aasMaxMasses = peptideMetrics.getMaxAaMasses();
            double[] aasMassesComplement = peptideMetrics.getSumAaMassesComplement();
            double[] aasMinMassesComplement = peptideMetrics.getMinAaMassesComplement();
            double[] aasMaxMassesComplement = peptideMetrics.getMaxAaMassesComplement();

            // Get the sum of chemical properties of the entire sequence
            int[] chemTotal = getChemTotal(peptideSequence);

            // The number of chemical properties tested
            int nChem = chemTotal.length;

            // Get the chemical attributes of the two first and two last amino acids
            int[] chem0 = chemicalProperties.get(peptideSequence[0]);
            int[] chem1 = chemicalProperties.get(peptideSequence[1]);
            int[] chemLast = chemicalProperties.get(peptideSequence[peptideLength - 1]);
            int[] chemPenultimate = chemicalProperties.get(peptideSequence[peptideLength - 2]);

            // Normalize the chemical propertise by the length of the sequence
            for (int chemI = 0; chemI < nChem; chemI++) {
                chemTotal[chemI] = chemTotal[chemI] / peptideLength;
            }

            // Get the chem functions along the amino acid sequence
            PeptideChemFunctions peptideChemFunctions = new PeptideChemFunctions(peptideSequence);

            // Get first and last amino acid indexes
            int firstAaIndex = aaIndexes.get(peptideSequence[0]);
            int lastAaIndex = aaIndexes.get(peptideSequence[peptideLength - 1]);

            // Keep track of the mass already iterated
            double iterationEntitiesMass = 0.0;

            // The amino acids at index and following
            char aa = peptideSequence[0];
            char nextAa = peptideSequence[1];

            // Iterate through all amino acids
            // Get the mass of the entities constituting this ion
            iterationEntitiesMass += aaMasses[i];
            double modificationsMass = modificationsMasses[i];
            iterationEntitiesMass += modificationsMass;

            // Get the chem functions for this amino acid
            int[] chemPreviousAa = peptideChemFunctions.getChemPreviousAa(i);
            int[] chemAa = peptideChemFunctions.getChemAa(i);
            int[] chemNextAa = peptideChemFunctions.getChemNextAa(i);
            int[] chemSecondNextAa = peptideChemFunctions.getChemSecondNextAa(i);
            int[] chemMin = peptideChemFunctions.getChemMin(i);
            int[] chemMax = peptideChemFunctions.getChemMax(i);
            int[] chemSum = peptideChemFunctions.getChemSum(i);
            int[] chemMinComplement = peptideChemFunctions.getChemMinComplement(i);
            int[] chemMaxComplement = peptideChemFunctions.getChemMaxComplement(i);
            int[] chemSumComplement = peptideChemFunctions.getChemSumComplement(i);

            // Create a vector of features for every amino acid and populate it
            int[] featuresAtAa = new int[164];
            int j = 0;

            // The total peptide length
            featuresAtAa[j] = peptideLength;

            // The b ion number
            featuresAtAa[++j] = i;

            // The ion number relative to the peptide length in percent
            featuresAtAa[++j] = (int) (100.0 * i / peptideLength);

            // The sum of the masses of the peptide entities
            featuresAtAa[++j] = (int) peptideMetrics.getTotalEntitiesMass();

            // The sum of the chemical properties of the peptide
            for (int chemI = 0; chemI < nChem; chemI++) {
                featuresAtAa[++j] = chemTotal[chemI];
            }

            // The sum of the masses of the entities consituting this ion
            featuresAtAa[++j] = (int) iterationEntitiesMass;

            // The complement of the iteration mass in the total mass
            featuresAtAa[++j] = (int) (peptideMetrics.getTotalEntitiesMass() - iterationEntitiesMass);

            // The charge
            featuresAtAa[++j] = charge;

            // The mass of the modifications carried by the amino acid
            featuresAtAa[++j] = (int) modificationsMass;

            // Iterate throught the chemical properties
            for (int chemI = 0; chemI < nChem; chemI++) {

                // The first amino acid
                featuresAtAa[++j] = chem0[chemI];

                // The second amino acid
                featuresAtAa[j + nChem] = chem1[chemI];

                // The penultimate amino acid
                featuresAtAa[j + 2 * nChem] = chemPenultimate[chemI];

                // The last amino acid
                featuresAtAa[j + 3 * nChem] = chemLast[chemI];

                // The current amino acid
                featuresAtAa[j + 4 * nChem] = chemAa[chemI];

                // The previous amino acid
                featuresAtAa[j + 5 * nChem] = chemPreviousAa[chemI];

                // The next amino acid
                featuresAtAa[j + 6 * nChem] = chemNextAa[chemI];

                // The second next amino acid
                featuresAtAa[j + 7 * nChem] = chemSecondNextAa[chemI];

                // The sum of the chemical properties until the amino acid
                int chemSumI = chemSum[chemI];
                featuresAtAa[j + 8 * nChem] = chemSumI;

                // The previous feature normalized to the length of the ion
                featuresAtAa[j + 9 * nChem] = chemSumI / (i + 1);

                // The maximal chemical property until the amino acid
                featuresAtAa[j + 10 * nChem] = chemMax[chemI];

                // The minimal chemical property until the amino acid
                featuresAtAa[j + 11 * nChem] = chemMin[chemI];

                // The sum of the chemical properties after the amino acid
                chemSumI = chemSumComplement[chemI];
                featuresAtAa[j + 8 * nChem] = chemSumI;

                // The previous feature normalized to the length of the complementary ion
                featuresAtAa[j + 9 * nChem] = chemSumI / (peptideLength - (i + 1));

                // The maximal chemical property after the amino acid
                featuresAtAa[j + 10 * nChem] = chemMaxComplement[chemI];

                // The minimal chemical property after the amino acid
                featuresAtAa[j + 11 * nChem] = chemMinComplement[chemI];

            }
            j += 11 * nChem;

            j = 75; // nothing between 60 and 76?

            // The mass of the amino acids constituting this ion
            double aasMass = aasMasses[i];
            featuresAtAa[++j] = (int) aasMass;

            // The mass of the amino acids constituting this ion normalized by the length of the ion
            featuresAtAa[++j] = (int) (aasMass / (i + 1));

            // The maximal mass among the amino acids constituting this ion
            featuresAtAa[++j] = (int) aasMaxMasses[i];

            // The minimal mass among the amino acids constituting this ion
            featuresAtAa[++j] = (int) aasMinMasses[i];

            // The mass of the amino acids complementary to this ion
            aasMass = aasMassesComplement[i];
            featuresAtAa[++j] = (int) aasMass;

            // The mass of the amino acids complementary to this ion normalized by the length of the ion
            featuresAtAa[++j] = (int) (aasMass / (peptideLength - (i + 1)));

            // The maximal mass among the amino acids complementary to this ion
            featuresAtAa[++j] = (int) aasMaxMassesComplement[i];

            // The minimal mass among the amino acids complementary to this ion
            featuresAtAa[++j] = (int) aasMinMassesComplement[i];

            // The mass of the different amin
            // The first amino acid index
            j++;
            featuresAtAa[j + firstAaIndex] = 1;

            // The last amino acid index
            j += nImplementedAas;
            featuresAtAa[j + lastAaIndex] = 1;

            // The amino acid index
            j += nImplementedAas;
            int aaIndex = aaIndexes.get(aa);
            featuresAtAa[j + aaIndex] = 1;

            // The next amino acid index
            j += nImplementedAas;
            aaIndex = aaIndexes.get(nextAa);
            featuresAtAa[j + aaIndex] = 1;

            // Save the features for this ion
            features[i] = featuresAtAa;

            // Go to next amino acid
            aa = nextAa;
            nextAa = peptideSequence[i + 1];
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
        AminoAcid.Property[] aminoAcidProperties = AminoAcid.Property.values();
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
        AminoAcid.Property[] peptideAminoAcidProperties = new AminoAcid.Property[indexes.size()];
        int cpt = 0;
        for (int index : indexes) {
            peptideAminoAcidProperties[cpt++] = aminoAcidProperties[index];
        }
        return peptideAminoAcidProperties;
    }

    /**
     * Returns the index of every amino acid in the array of supported amino
     * acids in a map.
     *
     * @return the index of every amino acid
     */
    public static HashMap<Character, Integer> getAminoAcidIndexes() {

        HashMap<Character, Integer> indexes = new HashMap<Character, Integer>(supportedAminoAcids.length);

        for (int i = 0; i < supportedAminoAcids.length; i++) {
            char aa = supportedAminoAcids[i];
            indexes.put(aa, i);
        }

        return indexes;
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
         * The mass of complementary ions derived from a sequence.
         */
        private double[] complementaryIonMass;
        /**
         * The properties of all amino acids in a sequence.
         */
        private double[][] aminoAcidProperties;

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
            complementaryIonMass[peptideSequence.length - 1] = waterMass + aminoAcid.getMonoisotopicMass();
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

                // Ion mass
                complementaryIonMass[complementaryI] = complementaryIonMass[complementaryI + 1] + aminoAcid.getMonoisotopicMass();

                // Min, max and sum of the different complementary ion properties needed
                for (AminoAcid.Property property : complementaryIonAminoAcidProperties) {
                    double value = aminoAcid.getProperty(property);
                    double previousValue = minComplementaryIonAminoAcidProperties[complementaryI - 1][property.ordinal()];
                    minComplementaryIonAminoAcidProperties[complementaryI][property.ordinal()] = value < previousValue ? value : previousValue;
                    previousValue = maxComplementaryIonAminoAcidProperties[complementaryI - 1][property.ordinal()];
                    maxComplementaryIonAminoAcidProperties[complementaryI][property.ordinal()] = value > previousValue ? value : previousValue;
                    sumComplementaryIonAminoAcidProperties[complementaryI][property.ordinal()] = sumComplementaryIonAminoAcidProperties[complementaryI - 1][property.ordinal()] + value;
                }
            }

            // Iterate modifications
            for (ModificationMatch modificationMatch : modificationMatches) {

                String modificationName = modificationMatch.getTheoreticPtm();
                PTM modification = ptmFactory.getPTM(modificationName);
                double modificationMass = modification.getMass();
                int modificationSite = modificationMatch.getModificationSite();

                peptideMass += modificationMass;
                forwardIonMass[modificationSite - 1] += modificationMass;
            }
        }
    }
}
