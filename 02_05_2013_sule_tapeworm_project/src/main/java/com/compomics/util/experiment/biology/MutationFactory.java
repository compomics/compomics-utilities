package com.compomics.util.experiment.biology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds the all possible one-letter peptide sequence mutations.
 *
 * @author Thilo Muth
 */
public class MutationFactory {

    /**
     * List of amino acids.
     */
    private static List<AminoAcid> aminoAcids;
    /**
     * List of mutations.
     */
    private static List<Mutation> mutations;
    /**
     * Mapping from mutation as string to mutation object.
     */
    private static Map<String, Mutation> mutationMap;

    /**
     * Private constructor for the singleton convention.
     */
    private MutationFactory() {
    }

    /**
     * Returns the requested mutation by lookup in the mutation map.
     *
     * @param mutation The mutation as parameter.
     * @return The requested mutation.
     */
    public static Mutation getMutation(Mutation mutation) {
        if (mutationMap == null) {
            createMutations();
        }
        return mutationMap.get(mutation.toString());
    }

    /**
     * This method creates all possible mutations for the 20 essential amino
     * acids.
     *
     * @return All possible mutation for the 20 essential amino acids.
     */
    public static List<Mutation> createMutations() {
        // Create essential amino acids.
        if (aminoAcids == null) {
            createAminoAcids();
        }
        // Create mutations
        if (mutations == null) {
            mutations = new ArrayList<Mutation>();
            mutationMap = new HashMap<String, Mutation>();
            for (AminoAcid aa1 : aminoAcids) {
                for (AminoAcid aa2 : aminoAcids) {
                    if (!aa1.equals(aa2)) {
                        Mutation mutation = new Mutation(aa1, aa2);
                        if (getMutation(mutation) == null) {
                            mutations.add(mutation);
                            mutationMap.put(mutation.toString(), mutation);
                        }
                    }
                }
            }
        }
        return mutations;
    }

    /**
     * Returns the mutation as a string.
     * 
     * @return the mutation as a string
     */
    public static List<String> getMutationsAsString() {
        if (mutationMap == null) {
            createMutations();
        }
        return new ArrayList(mutationMap.keySet());
    }

    /**
     * This method creates the 20 essential amino acids.
     */
    private static void createAminoAcids() {
        aminoAcids = new ArrayList<AminoAcid>();
        aminoAcids.add(AminoAcid.A);
        aminoAcids.add(AminoAcid.C);
        aminoAcids.add(AminoAcid.D);
        aminoAcids.add(AminoAcid.E);
        aminoAcids.add(AminoAcid.F);
        aminoAcids.add(AminoAcid.G);
        aminoAcids.add(AminoAcid.H);
        aminoAcids.add(AminoAcid.I);
        aminoAcids.add(AminoAcid.K);
        aminoAcids.add(AminoAcid.L);
        aminoAcids.add(AminoAcid.M);
        aminoAcids.add(AminoAcid.N);
        aminoAcids.add(AminoAcid.P);
        aminoAcids.add(AminoAcid.Q);
        aminoAcids.add(AminoAcid.R);
        aminoAcids.add(AminoAcid.S);
        aminoAcids.add(AminoAcid.T);
        aminoAcids.add(AminoAcid.V);
        aminoAcids.add(AminoAcid.W);
        aminoAcids.add(AminoAcid.Y);
    }
}
