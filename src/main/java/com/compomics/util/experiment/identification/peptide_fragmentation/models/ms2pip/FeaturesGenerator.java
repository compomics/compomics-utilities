package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class computes the ms2PIP features from a peptide.
 *
 * It is a java implementation of
 * https://github.com/sdgroeve/ms2pipXGB/blob/master/ms2pipfeatures_cython.pyx
 * with the permission of Sven Degroeve. No license found, no copyright
 * infringement intended.
 *
 * @author Marc Vaudel
 */
public class FeaturesGenerator {

    /**
     * The PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * Number of amino acids implemented in ms2pip.
     */
    private final static int nImplementedAas = 20;
    /**
     * The chemical properties of the amino acids in a map indexed by their
     * single letter code.
     *
     * @TODO: implement modifications
     */
    private static final HashMap<Character, int[]> chemicalProperties = getChemicalProperties();
    /**
     * The ms2pip index of every amino acid.
     */
    private static final HashMap<Character, Integer> aaIndexes = getAminoAcidIndexes();

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
        for (int i = 0 ; i < sequenceLength ; i++) {
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
     * Returns the ms2pip features for the ions of the given sequence with modifications at the
     * given charge.
     *
     * @param peptideSequence the peptide sequence as char array
     * @param modificationMatches the modification matches
     * @param charge the charge
     *
     * @return the ms2pip features for the b ions
     */
    private int[][] getIonsFeatures(char[] peptideSequence, ArrayList<ModificationMatch> modificationMatches, int charge) {

        // Get the peptide attributes
        int peptideLength = peptideSequence.length;
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

        // Prepare an array for the resutls
        int[][] features = new int[peptideLength - 1][164];

        // Keep track of the mass already iterated
        double iterationEntitiesMass = 0.0;

        // The amino acids at index and following
        char aa = peptideSequence[0];
        char nextAa = peptideSequence[1];

        // Iterate through all amino acids
        for (int i = 0; i < peptideLength - 1; i++) {

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
     * Returns a vector of the sum of the amino acid chemical properties for a
     * peptide sequence.
     *
     * @param peptideSequence the peptide sequence as char array
     *
     * @return a vector of the sum of the amino acid chemical properties
     */
    private int[] getChemTotal(char[] peptideSequence) {

        int[] chemTotal = new int[]{0, 0, 0, 0};

        for (char aa : peptideSequence) {

            int[] chemAA = chemicalProperties.get(aa);

            for (int i = 0; i < chemAA.length; i++) {

                chemTotal[i] += chemAA[i];

            }

        }

        return chemTotal;
    }

    /**
     * Returns the ms2pip index of every amino acid.
     *
     * @return the ms2pip index of every amino acid
     */
    public static HashMap<Character, Integer> getAminoAcidIndexes() {

        HashMap<Character, Integer> indexes = new HashMap<Character, Integer>(nImplementedAas);
        indexes.put('A', 0);
        indexes.put('C', 1);
        indexes.put('D', 2);
        indexes.put('E', 3);
        indexes.put('F', 4);
        indexes.put('G', 5);
        indexes.put('H', 6);
        indexes.put('I', 7);
        indexes.put('K', 8);
        indexes.put('L', 9);
        indexes.put('M', 10);
        indexes.put('N', 11);
        indexes.put('P', 12);
        indexes.put('Q', 13);
        indexes.put('R', 14);
        indexes.put('S', 15);
        indexes.put('T', 16);
        indexes.put('V', 17);
        indexes.put('W', 18);
        indexes.put('Y', 19);

        return indexes;
    }

    /**
     * Returns the chemical properties of the amino acids in a map indexed by
     * their single letter code.
     *
     * @return the chemical properties of the amino acids
     */
    public static HashMap<Character, int[]> getChemicalProperties() {

        HashMap<Character, int[]> chemicalProperties = new HashMap<Character, int[]>(nImplementedAas);

        chemicalProperties.put('A', new int[]{10, 51, 93, 40});
        chemicalProperties.put('C', new int[]{23, 18, 49, 100});
        chemicalProperties.put('D', new int[]{10, 75, 31, 28});
        chemicalProperties.put('E', new int[]{14, 25, 45, 0});
        chemicalProperties.put('F', new int[]{37, 35, 39, 5});
        chemicalProperties.put('G', new int[]{27, 100, 95, 33});
        chemicalProperties.put('H', new int[]{0, 16, 79, 40});
        chemicalProperties.put('I', new int[]{61, 3, 56, 60});
        chemicalProperties.put('K', new int[]{23, 94, 100, 40});
        chemicalProperties.put('L', new int[]{55, 0, 43, 87});
        chemicalProperties.put('M', new int[]{20, 97, 98, 40});
        chemicalProperties.put('N', new int[]{30, 82, 90, 37});
        chemicalProperties.put('P', new int[]{29, 12, 52, 33});
        chemicalProperties.put('Q', new int[]{34, 0, 0, 44});
        chemicalProperties.put('R', new int[]{33, 22, 54, 36});
        chemicalProperties.put('S', new int[]{100, 22, 53, 100});
        chemicalProperties.put('T', new int[]{14, 21, 60, 36});
        chemicalProperties.put('V', new int[]{26, 39, 72, 35});
        chemicalProperties.put('W', new int[]{17, 80, 97, 39});
        chemicalProperties.put('Y', new int[]{39, 98, 69, 39});

        chemicalProperties.put('?', new int[]{21, 95, 100, 40});
        chemicalProperties.put('?', new int[]{30, 70, 75, 36});
        chemicalProperties.put('?', new int[]{35, 28, 47, 40});

        return chemicalProperties;
    }

    /**
     * Convenience class used to store metrics on the peptide.
     */
    private class PeptideMetrics {

        /**
         * The amino acid masses as array.
         */
        private double[] aaMasses;
        /**
         * The modification masses carried by every amino acid as array.
         */
        private double[] modificationsMasses;
        /**
         * The mass of all amino acids and modifications constituting this
         * peptide.
         */
        private double totalEntitiesMass;
        /**
         * The masses of the amino acids constituting making the different b
         * ions.
         */
        private double[] sumAaMasses;
        /**
         * The minimal mass among all amino acids constituting the different b
         * ions.
         */
        private double[] minAaMasses;
        /**
         * The maximal mass among all amino acids constituting the different b
         * ions.
         */
        private double[] maxAaMasses;
        /**
         * The masses of the amino acids constituting making the complement of
         * the different b ions.
         */
        private double[] sumAaMassesComplement;
        /**
         * The minimal mass among all amino acids constituting the complement of
         * the different b ions.
         */
        private double[] minAaMassesComplement;
        /**
         * The maximal mass among all amino acids constituting the complement of
         * the different b ions.
         */
        private double[] maxAaMassesComplement;

        /**
         * Constructor.
         *
         * @param peptideSequence the peptide sequence as char array
         * @param modificationMatches the modification matches of the peptide
         */
        private PeptideMetrics(char[] peptideSequence, ArrayList<ModificationMatch> modificationMatches) {
            fillAtttributes(peptideSequence, modificationMatches);
        }

        /**
         * Fills the attribute of the class.
         *
         * @param peptideSequence the peptide sequence as char array
         * @param modificationMatches the modification matches of the peptide
         */
        private void fillAtttributes(char[] peptideSequence, ArrayList<ModificationMatch> modificationMatches) {

            aaMasses = new double[peptideSequence.length];
            modificationsMasses = new double[peptideSequence.length];
            sumAaMasses = new double[peptideSequence.length - 1];
            minAaMasses = new double[peptideSequence.length - 1];
            maxAaMasses = new double[peptideSequence.length - 1];

            totalEntitiesMass = 0.0;
            double tempAaMasses = 0.0;
            double minMass = Double.MAX_VALUE;
            double maxMass = 0.0;

            for (int i = 0; i < peptideSequence.length - 1; i++) {

                char aa = peptideSequence[i];
                AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                double aaMass = aminoAcid.getMonoisotopicMass();
                aaMasses[i] = aaMass;
                totalEntitiesMass += aaMass;

                tempAaMasses += aaMass;
                if (aaMass < minMass) {
                    minMass = aaMass;
                } 
                if (aaMass > maxMass) {
                    maxMass = aaMass;
                }
                sumAaMasses[i] = tempAaMasses;
                minAaMasses[i] = minMass;
                maxAaMasses[i] = maxMass;
            }

            int lengthMinusOne = peptideSequence.length - 1;
            char aa = peptideSequence[lengthMinusOne];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            double aaMass = aminoAcid.getMonoisotopicMass();
            aaMasses[lengthMinusOne] = aaMass;

            sumAaMassesComplement = new double[lengthMinusOne];
            minAaMassesComplement = new double[lengthMinusOne];
            maxAaMassesComplement = new double[lengthMinusOne];

            int index = lengthMinusOne;
            tempAaMasses = aaMass;
            minMass = aaMass;
            maxMass = aaMass;
            index--;
            sumAaMassesComplement[index] = tempAaMasses;
            minAaMassesComplement[index] = minMass;
            maxAaMassesComplement[index] = maxMass;

            for (int i = index; i > 0; ) {

                aaMass = aaMasses[i];
                tempAaMasses += aaMass;
                if (aaMass < minMass) {
                    minMass = aaMass;
                } else if (aaMass > maxMass) {
                    maxMass = aaMass;
                }
                i--;
                sumAaMassesComplement[i] = tempAaMasses;
                minAaMassesComplement[i] = minMass;
                maxAaMassesComplement[i] = maxMass;

            }

            if (modificationMatches != null) {

                for (ModificationMatch modificationMatch : modificationMatches) {

                    int site = modificationMatch.getModificationSite();
                    String modificationName = modificationMatch.getTheoreticPtm();
                    PTM modification = ptmFactory.getPTM(modificationName);

                    modificationsMasses[site - 1] += modification.getMass();

                }
            }
        }

        /**
         * Returns the amino acid masses as array.
         *
         * @return the amino acid masses as array
         */
        public double[] getAaMasses() {
            return aaMasses;
        }

        /**
         * Returns the modification masses carried by every amino acid as array.
         *
         * @return the modification masses carried by every amino acid as array
         */
        public double[] getModificationsMasses() {
            return modificationsMasses;
        }

        /**
         * Returns the mass of all amino acids and modifications constituting
         * this peptide.
         *
         * @return the mass of all amino acids and modifications constituting
         * this peptide
         */
        public double getTotalEntitiesMass() {
            return totalEntitiesMass;
        }

        /**
         * Returns the masses of the amino acids constituting making the
         * different b ions.
         *
         * @return masses of the amino acids constituting making the different b
         * ions
         */
        public double[] getSumAaMasses() {
            return sumAaMasses;
        }

        /**
         * Returns the minimal mass among all amino acids constituting the
         * different b ions.
         *
         * @return minimal mass among all amino acids constituting the different
         * b ions
         */
        public double[] getMinAaMasses() {
            return minAaMasses;
        }

        /**
         * Returns the maximal mass among all amino acids constituting the
         * different b ions.
         *
         * @return maximal mass among all amino acids constituting the different
         * b ions
         */
        public double[] getMaxAaMasses() {
            return maxAaMasses;
        }

        /**
         * Returns the masses of the amino acids constituting making the
         * complement of the different b ions.
         *
         * @return masses of the amino acids constituting making the complement
         * of the different b ions
         */
        public double[] getSumAaMassesComplement() {
            return sumAaMassesComplement;
        }

        /**
         * Returns the minimal mass among all amino acids constituting the
         * complement of the different b ions.
         *
         * @return minimal mass among all amino acids constituting the
         * complement of the different b ions
         */
        public double[] getMinAaMassesComplement() {
            return minAaMassesComplement;
        }

        /**
         * Returns the maximal mass among all amino acids constituting the
         * complement of the different b ions.
         *
         * @return maximal mass among all amino acids constituting the
         * complement of the different b ions
         */
        public double[] getMaxAaMassesComplement() {
            return maxAaMassesComplement;
        }

    }

    /**
     * Convenience class used to store the values of the chem functions along a
     * peptide sequence.
     */
    private class PeptideChemFunctions {

        /**
         * Chem values of the previous amino acid.
         */
        private int[][] chemPreviousAa;
        /**
         * Chem values of the amino acid.
         */
        private int[][] chemAa;
        /**
         * Chem values of the next amino acid.
         */
        private int[][] chemNextAa;
        /**
         * Chem values of the second next amino acid.
         */
        private int[][] chemSecondNextAa;
        /**
         * Minimal chem value until amino acid.
         */
        private int[][] chemMin;
        /**
         * Maximal chem value until amino acid.
         */
        private int[][] chemMax;
        /**
         * Sum of chem values until amino acid.
         */
        private int[][] chemSum;
        /**
         * Minimal chem value after amino acid.
         */
        private int[][] chemMinComplement;
        /**
         * Maximal chem value after amino acid.
         */
        private int[][] chemMaxComplement;
        /**
         * Sum of chem values after amino acid.
         */
        private int[][] chemSumComplement;

        /**
         * Constructor.
         *
         * @param peptideSequence the peptide sequence
         */
        private PeptideChemFunctions(char[] peptideSequence) {
            fillForwardFunctions(peptideSequence);
            fillRewindFunctions(peptideSequence);
        }

        /**
         * Fills the values for the functions along the peptide sequence.
         *
         * @param peptideSequence the peptide sequence
         */
        private void fillForwardFunctions(char[] peptideSequence) {

            // The chemical properties of the previous amino acid
            int[] chemPreviousAaAtI = chemicalProperties.get(peptideSequence[0]);

            // The chemical properties of the current amino acid
            int[] chemAaAtI = chemPreviousAaAtI;

            // The chemical properties of the next amino acid
            int[] chemNextAaAtI = chemicalProperties.get(peptideSequence[1]);

            // The chemical properties of the sceond next amino acid
            int[] chemSecondNextAaAtI = chemicalProperties.get(peptideSequence[2]);

            // The minimal, maximal, and sum values of the chemical properties of the amino acids constituting b ions
            int chemLength = chemAaAtI.length;
            int[] chemMinAtI = Arrays.copyOf(chemAaAtI, chemLength);
            int[] chemMaxAtI = Arrays.copyOf(chemAaAtI, chemLength);
            int[] chemSumAtI = Arrays.copyOf(chemAaAtI, chemLength);

            // Create matrices to store the results
            int peptideLengthMinusOne = peptideSequence.length - 1;
            chemPreviousAa = new int[peptideLengthMinusOne][chemLength];
            chemAa = new int[peptideLengthMinusOne][chemLength];
            chemNextAa = new int[peptideLengthMinusOne][chemLength];
            chemSecondNextAa = new int[peptideLengthMinusOne][chemLength];
            chemMin = new int[peptideLengthMinusOne][chemLength];
            chemMax = new int[peptideLengthMinusOne][chemLength];
            chemSum = new int[peptideLengthMinusOne][chemLength];

            // Iterate through all amino acids and populate the matrices
            int lastIndex = peptideLengthMinusOne - 2;
            for (int i = 0; i < lastIndex; i++) {

                // Add the value at this amino acid to the matrices
                chemPreviousAa[i] = chemPreviousAaAtI;
                chemAa[i] = chemAaAtI;
                chemNextAa[i] = chemNextAaAtI;
                chemSecondNextAa[i] = chemSecondNextAaAtI;
                chemMin[i] = Arrays.copyOf(chemMinAtI, chemLength);
                chemMax[i] = Arrays.copyOf(chemMaxAtI, chemLength);
                chemSum[i] = Arrays.copyOf(chemSumAtI, chemLength);

                // Get values for the next amino acid
                chemPreviousAaAtI = chemAaAtI;
                chemAaAtI = chemNextAaAtI;
                chemNextAaAtI = chemSecondNextAaAtI;
                chemSecondNextAaAtI = chemicalProperties.get(peptideSequence[i + 3]);
                for (int chemI = 0; chemI < chemLength; chemI++) {
                    int currentChem = chemAaAtI[chemI];
                    if (currentChem > chemMaxAtI[chemI]) {
                        chemMaxAtI[chemI] = currentChem;
                    } else if (currentChem < chemMinAtI[chemI]) {
                        chemMinAtI[chemI] = currentChem;
                    }
                    chemSumAtI[chemI] += currentChem;
                }
            }

            // Add the value at last index
            chemPreviousAa[lastIndex] = chemPreviousAaAtI;
            chemAa[lastIndex] = chemAaAtI;
            chemNextAa[lastIndex] = chemNextAaAtI;
            chemSecondNextAa[lastIndex] = chemSecondNextAaAtI;
            chemMin[lastIndex] = Arrays.copyOf(chemMinAtI, chemLength);
            chemMax[lastIndex] = Arrays.copyOf(chemMaxAtI, chemLength);
            chemSum[lastIndex] = Arrays.copyOf(chemSumAtI, chemLength);

            // Get values for the second last amino acid
            chemPreviousAaAtI = chemAaAtI;
            chemAaAtI = chemNextAaAtI;
            chemNextAaAtI = chemSecondNextAaAtI;
            for (int chemI = 0; chemI < chemLength; chemI++) {
                int currentChem = chemAaAtI[chemI];
                if (currentChem > chemMaxAtI[chemI]) {
                    chemMaxAtI[chemI] = currentChem;
                } else if (currentChem < chemMinAtI[chemI]) {
                    chemMinAtI[chemI] = currentChem;
                }
                chemSumAtI[chemI] += currentChem;
            }

            // Add the value at the second last amino acid
            lastIndex++;
            chemPreviousAa[lastIndex] = chemPreviousAaAtI;
            chemAa[lastIndex] = chemAaAtI;
            chemNextAa[lastIndex] = chemNextAaAtI;
            chemSecondNextAa[lastIndex] = chemSecondNextAaAtI;
            chemMin[lastIndex] = chemMinAtI;
            chemMax[lastIndex] = chemMaxAtI;
            chemSum[lastIndex] = chemSumAtI;
        }

        /**
         * Fills the values of the complementary functions.
         *
         * @param peptideSequence the peptide sequence
         */
        private void fillRewindFunctions(char[] peptideSequence) {

            // The chemical properties of the current amino acid
            int peptideLengthMinusOne = peptideSequence.length - 1;
            int[] chemAaAtI = chemicalProperties.get(peptideSequence[peptideLengthMinusOne]);

            // The minimal, maximal, and sum values of the chemical properties of the amino acids constituting b ions
            int chemLength = chemAaAtI.length;
            int[] chemMinAtI = Arrays.copyOf(chemAaAtI, chemLength);
            int[] chemMaxAtI = Arrays.copyOf(chemAaAtI, chemLength);
            int[] chemSumAtI = Arrays.copyOf(chemAaAtI, chemLength);

            // Create matrices to store the results
            chemMinComplement = new int[peptideLengthMinusOne][chemLength];
            chemMaxComplement = new int[peptideLengthMinusOne][chemLength];
            chemSumComplement = new int[peptideLengthMinusOne][chemLength];

            // Iterate through all amino acids and populate the matrices
            for (int i = peptideLengthMinusOne -1; i > 0; i--) {

                // Set values to the matrices
                chemMinComplement[i] = Arrays.copyOf(chemMinAtI, chemLength);
                chemMaxComplement[i] = Arrays.copyOf(chemMaxAtI, chemLength);
                chemSumComplement[i] = Arrays.copyOf(chemSumAtI, chemLength);

                // Get values for the next amino acid
                chemAaAtI = chemicalProperties.get(peptideSequence[i]);
                for (int chemI = 0; chemI < chemLength; chemI++) {
                    int currentChem = chemAaAtI[chemI];
                    if (currentChem > chemMaxAtI[chemI]) {
                        chemMaxAtI[chemI] = currentChem;
                    } else if (currentChem < chemMinAtI[chemI]) {
                        chemMinAtI[chemI] = currentChem;
                    }
                    chemSumAtI[chemI] += currentChem;
                }
            }

            // Set last values to the matrices
            chemMinComplement[0] = chemMinAtI;
            chemMaxComplement[0] = chemMaxAtI;
            chemSumComplement[0] = chemSumAtI;

        }

        /**
         * Returns the chem values at the previous amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemPreviousAa(int aaIndex) {
            return chemPreviousAa[aaIndex];
        }

        /**
         * Returns the chem values at the amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemAa(int aaIndex) {
            return chemAa[aaIndex];
        }

        /**
         * Returns the chem values at the next amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemNextAa(int aaIndex) {
            return chemNextAa[aaIndex];
        }

        /**
         * Returns the chem values at the second next amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemSecondNextAa(int aaIndex) {
            return chemSecondNextAa[aaIndex];
        }

        /**
         * Returns the minimal chem values until the amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemMin(int aaIndex) {
            return chemMin[aaIndex];
        }

        /**
         * Returns the maximal chem values until the amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemMax(int aaIndex) {
            return chemMax[aaIndex];
        }

        /**
         * Returns the sum of chem values until the amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemSum(int aaIndex) {
            return chemSum[aaIndex];
        }

        /**
         * Returns the minimal chem values after the amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemMinComplement(int aaIndex) {
            return chemMinComplement[aaIndex];
        }

        /**
         * Returns the maximal chem values after the amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemMaxComplement(int aaIndex) {
            return chemMaxComplement[aaIndex];
        }

        /**
         * Returns the sum of chem values after the amino acid.
         *
         * @param aaIndex the index of the amino acid
         *
         * @return the chem values
         */
        private int[] getChemSumComplement(int aaIndex) {
            return chemSumComplement[aaIndex];
        }

    }
}
