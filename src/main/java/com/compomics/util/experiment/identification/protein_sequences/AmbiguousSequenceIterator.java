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

    private char[] sequenceAsCharArray;

    private ArrayList<char[]> aaCombinations;
    private int[] iterationIndices;
    private int[] indicesOnSequence;
    private int secondaryIndex = 0;

    public AmbiguousSequenceIterator(char[] sequence, int expectedNumberOfCombinations) {
        this.sequenceAsCharArray = sequence;
        initialize(expectedNumberOfCombinations);
    }

    public AmbiguousSequenceIterator(String sequence, int expectedNumberOfCombinations) {
        this(sequence.toCharArray(), expectedNumberOfCombinations);
    }

    public AmbiguousSequenceIterator(String sequence) {
        this(sequence, 2);
    }

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
    }

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
