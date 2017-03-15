package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.experiment.biology.AminoAcid;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Iterator going through the possible sequences of a sequence containing
 * combinations of amino acids.
 *
 * @author Marc Vaudel
 */
public class AmbiguousSequenceIterator {

    /**
     * The sequence as char array.
     */
    private char[] sequenceAsCharArray;
    /**
     * The amino acid combinations
     */
    private ArrayList<char[]> aaCombinations;
    /**
     * The amino acid combination iteration indices.
     */
    private int[] iterationIndices;
    /**
     * The indices on the sequence.
     */
    private int[] indicesOnSequence;
    /**
     * The secondary iteration index.
     */
    private int secondaryIndex = 0;

    /**
     * Constructor.
     *
     * @param sequence the sequence to iterate as char array
     * @param expectedNumberOfCombinations the expected number of combinations
     */
    public AmbiguousSequenceIterator(char[] sequence, int expectedNumberOfCombinations) {
        this.sequenceAsCharArray = sequence;
        initialize(expectedNumberOfCombinations);
    }

    /**
     * Constructor.
     *
     * @param sequence the sequence as String
     * @param expectedNumberOfCombinations the expected number of combinations
     */
    public AmbiguousSequenceIterator(String sequence, int expectedNumberOfCombinations) {
        this(sequence.toCharArray(), expectedNumberOfCombinations);
    }

    /**
     * Constructor.
     *
     * @param sequence the sequence as String
     */
    public AmbiguousSequenceIterator(String sequence) {
        this(sequence, 2);
    }

    /**
     * Initializes the iterator.
     *
     * @param expectedNumberOfCombinations the expected number of combinations
     */
    private void initialize(int expectedNumberOfCombinations) {

        // Find amino acid combinations and store them in a map
        int initialSize = Math.min(expectedNumberOfCombinations, 16);
        aaCombinations = new ArrayList<char[]>(initialSize);
        ArrayList<Integer> indicesList = new ArrayList<Integer>(initialSize);
        for (int i = 0; i < sequenceAsCharArray.length; i++) {
            char aa = sequenceAsCharArray[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            if (aminoAcid.iscombination()) {
                char[] possibleAas = aminoAcid.getSubAminoAcids(false);
                aaCombinations.add(possibleAas);
                indicesList.add(i);
            }
        }

        // Set up indices for iteration
        indicesOnSequence = new int[indicesList.size()];
        iterationIndices = new int[indicesList.size()];
        int count = 0;
        for (Integer index : indicesList) {
            indicesOnSequence[count] = index;
            iterationIndices[count] = 0;
            count++;
        }
        iterationIndices[0] = -1;
    }

    /**
     * Returns the next sequence, null if none.
     *
     * @return the next sequence
     */
    public char[] getNextSequence() {

        // Increase the amino acid iteration indices
        if (!increaseIndices()) {
            return null;
        }

        // Create the new sequence
        char[] newSequenceAsCharArray = Arrays.copyOf(sequenceAsCharArray, sequenceAsCharArray.length);
        for (int i = 0; i < indicesOnSequence.length; i++) {
            int index = indicesOnSequence[i];
            char[] possibleAas = aaCombinations.get(i);
            int iterationIndex = iterationIndices[i];
            char aa = possibleAas[iterationIndex];
            newSequenceAsCharArray[index] = aa;
        }

        return newSequenceAsCharArray;
    }

    /**
     * Increases the iteration indices.
     *
     * @return a boolean indicating whether there are new indices
     */
    private boolean increaseIndices() {
        if (secondaryIndex == iterationIndices.length) {
            return false;
        }
        char[] aasAtIndex = aaCombinations.get(secondaryIndex);
        int aaIndex = iterationIndices[secondaryIndex];
        aaIndex++;
        if (aaIndex == aasAtIndex.length) {
            iterationIndices[secondaryIndex] = 0;
            secondaryIndex++;
            return increaseIndices();
        }
        iterationIndices[secondaryIndex] = aaIndex;
        return true;
    }
}
