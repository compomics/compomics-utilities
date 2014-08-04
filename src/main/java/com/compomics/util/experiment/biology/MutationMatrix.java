package com.compomics.util.experiment.biology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class stores and provides mutations expected for amino acids. This class
 * is its own factory.
 *
 * @author Marc
 */
public class MutationMatrix implements Serializable {

    /**
     * The name of this mutation matrix
     */
    private final String name;
    /**
     * The description of the mutation matrix
     */
    private final String description;
    /**
     * Map of the possible amino acid mutations: original aa -> mutated aa
     */
    private final HashMap<Character, HashSet<Character>> mutations = new HashMap<Character, HashSet<Character>>(26);
    /**
     * Reverse map of the possible amino acid mutations: mutated aa -> original
     * aa
     */
    private final HashMap<Character, HashSet<Character>> mutationsReverse = new HashMap<Character, HashSet<Character>>(26);
    /**
     * Mutation matrix allowing for a single base mutation
     */
    public final MutationMatrix singleBaseSubstitution = singleBaseSubstitution();
    /**
     * Mutation matrix allowing for a single base transitions mutation
     */
    public final MutationMatrix transitionsSingleBaseSubstitution = transitionsSingleBaseSubstitution();
    /**
     * Mutation matrix allowing for a single base transversion mutation
     */
    public final MutationMatrix transversalSingleBaseSubstitution = transversalSingleBaseSubstitution();
    /**
     * Mutation matrix grouping synonymous amino acids.
     * Amino acids are grouped according to their side chain properties:
     * - Non-polar aliphatic groups: {'G', 'A', 'V', 'L', 'M', 'I'}
     * - Aromatic groups: {'F', 'Y', 'W'}
     * - Polar neutral groups: {'S', 'T', 'C', 'P', 'N', 'Q'}
     * - Basic groups: {'K', 'R', 'H'}
     * - Acidic groups: {'D', 'E'}
     */
    public final MutationMatrix synonymousMutation = synonymousMutation();
    /**
     * Returns the implemented default mutation matrices
     */
    public final MutationMatrix[] defaultMutationMatrices = new MutationMatrix[] {singleBaseSubstitution, transitionsSingleBaseSubstitution, transversalSingleBaseSubstitution};

    /**
     * Constructor
     *
     * @param name the name of this mutation matrix
     * @param description the description of the mutation matrix
     */
    public MutationMatrix(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Adds a possible mutation.
     *
     * @param originalAa the original amino acid represented by its single
     * letter code
     * @param mutatedAa the mutated amino acid represented by its single letter
     * code
     */
    public void addMutation(Character originalAa, Character mutatedAa) {
        HashSet<Character> mutatedAas = mutations.get(originalAa);
        if (mutatedAas == null) {
            mutatedAas = new HashSet<Character>();
            mutations.put(originalAa, mutatedAas);
        }
        if (!mutatedAas.contains(mutatedAa)) {
            mutatedAas.add(mutatedAa);
        }
        HashSet<Character> originalAas = mutationsReverse.get(originalAa);
        if (originalAas == null) {
            originalAas = new HashSet<Character>();
            mutationsReverse.put(mutatedAa, originalAas);
        }
        if (!originalAas.contains(originalAa)) {
            originalAas.add(originalAa);
        }
    }

    /**
     * Returns the possible mutated amino acids for the given amino acid as a
     * list of their single letter code. Null if none found.
     *
     * @param originalAminoAcid the amino acid of interest
     *
     * @return the possible mutated amino acids
     */
    public HashSet<Character> getMutatedAminoAcids(Character originalAminoAcid) {
        return mutations.get(originalAminoAcid);
    }

    /**
     * Returns the possible original amino acids for the given mutated amino
     * acid as a list of their single letter code. Null if none found.
     *
     * @param mutatedAminoAcid the mutated amino acid of interest
     *
     * @return the possible original amino acids for the given mutated amino
     * acid
     */
    public HashSet<Character> getOriginalAminoAcids(Character mutatedAminoAcid) {
        return mutationsReverse.get(mutatedAminoAcid);
    }

    /**
     * Returns the amino acids where a mutation has been registered.
     *
     * @return the amino acids where a mutation has been registered
     */
    public Set<Character> getOriginalAminoAcids() {
        return mutations.keySet();
    }

    /**
     * Returns the possible mutated amino acids.
     *
     * @return the possible mutated amino acids
     */
    public Set<Character> getMutatedAminoAcids() {
        return mutations.keySet();
    }

    /**
     * Adds the content of a mutation matrix in this matrix
     *
     * @param otherMatrix the other matrix to add
     */
    public void add(MutationMatrix otherMatrix) {
        for (Character originalAa : otherMatrix.getOriginalAminoAcids()) {
            for (Character mutatedAa : otherMatrix.getMutatedAminoAcids(originalAa)) {
                addMutation(originalAa, mutatedAa);
            }
        }
    }

    /**
     * Returns the mutation matrix allowing for a single base mutation.
     *
     * @return the mutation matrix allowing for a single base mutation
     */
    private static MutationMatrix singleBaseSubstitution() {
        MutationMatrix result = new MutationMatrix("Single Base Mutation", "Single base substitutions");
        char[] bases = {'A', 'T', 'G', 'C'};
        for (char originalAa : AminoAcid.getAminoAcids()) {
            if (originalAa != 'X') {
                AminoAcid aminoAcid = AminoAcid.getAminoAcid(originalAa);
                for (String geneCode : aminoAcid.getStandardGeneticCode()) {
                    StringBuilder geneCodeStringBuilder = new StringBuilder(geneCode);
                    for (int i = 0; i < geneCode.length(); i++) {
                        char originalBase = geneCode.charAt(i);
                        for (char base : bases) {
                            geneCodeStringBuilder.setCharAt(i, base);
                            String newCode = geneCodeStringBuilder.toString();
                            AminoAcid mutatedAminoAcid = AminoAcid.getAminoAcidFromGeneticCode(newCode);
                            char mutatedAa = mutatedAminoAcid.getSingleLetterCodeAsChar();
                            if (originalAa != mutatedAa) {
                                result.addMutation(originalAa, mutatedAa);
                            }
                        }
                        geneCodeStringBuilder.setCharAt(i, originalBase);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the mutation matrix allowing for a single base transitions mutation.
     *
     * @return the mutation matrix allowing for a single base transitions mutation
     */
    private static MutationMatrix transitionsSingleBaseSubstitution() {
        MutationMatrix result = new MutationMatrix("Transition Mutation", "Single base transitions substitutions.");
        char[] purines = {'A', 'G'}, pyrimidines = {'T', 'C'};
        for (char originalAa : AminoAcid.getAminoAcids()) {
            if (originalAa != 'X') {
                AminoAcid aminoAcid = AminoAcid.getAminoAcid(originalAa);
                for (String geneCode : aminoAcid.getStandardGeneticCode()) {
                    StringBuilder geneCodeStringBuilder = new StringBuilder(geneCode);
                    for (int i = 0; i < geneCode.length(); i++) {
                        char originalBase = geneCode.charAt(i);
                        char[] bases;
                        if (originalBase == purines[0] || originalBase == purines[1]) {
                            bases = purines;
                        } else if (originalBase == pyrimidines[0] || originalBase == pyrimidines[1]) {
                            bases = pyrimidines;
                        } else {
                            throw new IllegalArgumentException(originalBase + " not recognized for transitions substitution.");
                        }
                        for (char base : bases) {
                            geneCodeStringBuilder.setCharAt(i, base);
                            String newCode = geneCodeStringBuilder.toString();
                            AminoAcid mutatedAminoAcid = AminoAcid.getAminoAcidFromGeneticCode(newCode);
                            char mutatedAa = mutatedAminoAcid.getSingleLetterCodeAsChar();
                            if (originalAa != mutatedAa) {
                                result.addMutation(originalAa, mutatedAa);
                            }
                        }
                        geneCodeStringBuilder.setCharAt(i, originalBase);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the mutation matrix allowing for a single base transversion mutation.
     *
     * @return the mutation matrix allowing for a single base transversion mutation
     */
    private static MutationMatrix transversalSingleBaseSubstitution() {
        MutationMatrix result = new MutationMatrix("Transversion Mutation", "Single base transversion substitutions.");
        char[] purines = {'A', 'G'}, pyrimidines = {'T', 'C'};
        for (char originalAa : AminoAcid.getAminoAcids()) {
            if (originalAa != 'X') {
                AminoAcid aminoAcid = AminoAcid.getAminoAcid(originalAa);
                for (String geneCode : aminoAcid.getStandardGeneticCode()) {
                    StringBuilder geneCodeStringBuilder = new StringBuilder(geneCode);
                    for (int i = 0; i < geneCode.length(); i++) {
                        char originalBase = geneCode.charAt(i);
                        char[] bases;
                        if (originalBase == purines[0] || originalBase == purines[1]) {
                            bases = pyrimidines;
                        } else if (originalBase == pyrimidines[0] || originalBase == pyrimidines[1]) {
                            bases = purines;
                        } else {
                            throw new IllegalArgumentException(originalBase + " not recognized for transversion substitutions.");
                        }
                        for (char base : bases) {
                            geneCodeStringBuilder.setCharAt(i, base);
                            String newCode = geneCodeStringBuilder.toString();
                            AminoAcid mutatedAminoAcid = AminoAcid.getAminoAcidFromGeneticCode(newCode);
                            char mutatedAa = mutatedAminoAcid.getSingleLetterCodeAsChar();
                            if (originalAa != mutatedAa) {
                                result.addMutation(originalAa, mutatedAa);
                            }
                        }
                        geneCodeStringBuilder.setCharAt(i, originalBase);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a mutation matrix grouping synonymous amino acids.
     * Amino acids are grouped according to their side chain properties:
     * - Non-polar aliphatic groups: {'G', 'A', 'V', 'L', 'M', 'I'}
     * - Aromatic groups: {'F', 'Y', 'W'}
     * - Polar neutral groups: {'S', 'T', 'C', 'P', 'N', 'Q'}
     * - Basic groups: {'K', 'R', 'H'}
     * - Acidic groups: {'D', 'E'}
     * 
     * @return a mutation matrix grouping synonymous amino acids
     */
    private static MutationMatrix synonymousMutation() {
        MutationMatrix result = new MutationMatrix("Synonymous Mutation", "Mutations keeping amino acid properties.");
        char[] nonPolarAliphatic = new char[]{'G', 'A', 'V', 'L', 'M', 'I'};
        for (char originalAminoAcid : nonPolarAliphatic) {
            for (char mutatedAminoAcid : nonPolarAliphatic) {
                if (originalAminoAcid != mutatedAminoAcid) {
                    result.addMutation(originalAminoAcid, mutatedAminoAcid);
                }
            }
            result.addMutation('J', originalAminoAcid);
        }
        char[] aromatic = new char[]{'F', 'Y', 'W'};
        for (char originalAminoAcid : aromatic) {
            for (char mutatedAminoAcid : aromatic) {
                if (originalAminoAcid != mutatedAminoAcid) {
                    result.addMutation(originalAminoAcid, mutatedAminoAcid);
                }
            }
        }
        char[] polarNeutral = new char[]{'S', 'T', 'C', 'P', 'N', 'Q'};
        for (char originalAminoAcid : polarNeutral) {
            for (char mutatedAminoAcid : polarNeutral) {
                if (originalAminoAcid != mutatedAminoAcid) {
                    result.addMutation(originalAminoAcid, mutatedAminoAcid);
                }
            }
            result.addMutation('B', originalAminoAcid);
            result.addMutation('Z', originalAminoAcid);
        }
        char[] basic = new char[]{'K', 'R', 'H'};
        for (char originalAminoAcid : basic) {
            for (char mutatedAminoAcid : basic) {
                if (originalAminoAcid != mutatedAminoAcid) {
                    result.addMutation(originalAminoAcid, mutatedAminoAcid);
                }
            }
        }
        char[] acidic = new char[]{'D', 'E'};
        for (char originalAminoAcid : acidic) {
            for (char mutatedAminoAcid : acidic) {
                if (originalAminoAcid != mutatedAminoAcid) {
                    result.addMutation(originalAminoAcid, mutatedAminoAcid);
                }
            }
            result.addMutation('B', originalAminoAcid);
            result.addMutation('Z', originalAminoAcid);
        }
        return result;
    }

}
