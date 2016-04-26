package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory.ProteinIterator;
//import com.compomics.util.waiting.Duration;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The FM index.
 *
 * @author Dominik Kopczynski
 * @author Marc Vaudel
 */
public class FMIndex implements PeptideMapper {

    /**
     * Sampled suffix array.
     */
    private int[] suffixArray = null;
    /**
     * Wavelet tree for storing the burrows wheeler transform.
     */
    private WaveletTree occurrenceTable = null;
    /**
     * Less table for doing an update step according to the LF step.
     */
    private int[] lessTable = null;
    /**
     * Length of the indexed string (all concatenated protein sequences).
     */
    private int indexStringLength = 0;
    /**
     * Every 2^samplingShift suffix array entry will be sampled.
     */
    private final int samplingShift = 3;
    /**
     * Mask of fast modulo operations.
     */
    private final int samplingMask = (1 << samplingShift) - 1;
    /**
     * Bit shifting for fast multiplying / dividing operations.
     */
    private final int sampling = 1 << samplingShift;
    /**
     * Storing the starting positions of the protein sequences.
     */
    ArrayList<Integer> boundaries = null;
    /**
     * List of all accession IDs in the FASTA file.
     */
    ArrayList<String> accessions = null;

    /**
     * Returns the position of a value in the array or if not found the position
     * of the closest smaller value.
     *
     * @param array the array
     * @param key the key
     * @return he position of a value in the array or if not found the position
     * of the closest smaller value
     */
    private static int binarySearch(ArrayList<Integer> array, int key) {
        int low = 0;
        int mid = 0;
        int high = array.size() - 1;
        while (low <= high) {
            mid = (low + high) >> 1;
            if (array.get(mid) == key) {
                break;
            } else if (array.get(mid) <= key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        if (mid > 0 && key < array.get(mid)) {
            mid -= 1;
        }
        return mid;
    }

    /**
     * Constructor.
     * @param waitingHandler
     * @param displayProgress 
     */
    public FMIndex(WaitingHandler waitingHandler, boolean displayProgress) {
        SequenceFactory sf = SequenceFactory.getInstance(100000);

        if (waitingHandler != null && displayProgress && !waitingHandler.isRunCanceled()) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(6);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        StringBuilder TT = new StringBuilder();
        boundaries = new ArrayList<Integer>();
        accessions = new ArrayList<String>();
        boundaries.add(0);
        indexStringLength = 0;
        int numProteins = 0;
        try {
            ProteinIterator pi = sf.getProteinIterator(false);
            while (pi.hasNext()) {
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    return;
                }
                Protein currentProtein = pi.getNextProtein();
                int proteinLen = currentProtein.getLength();
                indexStringLength += proteinLen;
                ++numProteins;
                boundaries.add(indexStringLength + numProteins);
                accessions.add(currentProtein.getAccession());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        indexStringLength += Math.max(0, numProteins - 1); // delimiters between protein sequences
        indexStringLength += 1; // sentinal

        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        byte[] T = new byte[indexStringLength];
        T[indexStringLength - 1] = '$'; // adding the sentinal

        int tmpN = 0;
        try {
            ProteinIterator pi = sf.getProteinIterator(false);

            while (pi.hasNext()) {
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    return;
                }
                Protein currentProtein = pi.getNextProtein();
                int proteinLen = currentProtein.getLength();
                if (tmpN > 0) {
                    T[tmpN++] = '/'; // adding the delimiters
                }
                System.arraycopy(currentProtein.getSequence().getBytes(), 0, T, tmpN, proteinLen);
                tmpN += proteinLen;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        // create the suffix array using at most 128 characters
        suffixArray = SuffixArraySorter.buildSuffixArray(T, 128);
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        // create Burrows-Wheeler-Transform
        byte[] bwt = new byte[indexStringLength];
        for (int i = 0; i < indexStringLength; ++i) {
            bwt[i] = (suffixArray[i] != 0) ? T[suffixArray[i] - 1] : T[indexStringLength - 1];
        }
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        // sampling suffix array
        int[] sampledSuffixArray = new int[((indexStringLength + 1) >> samplingShift) + 1];
        int sampledIndex = 0;
        for (int i = 0; i < indexStringLength; i += sampling) {
            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                return;
            }
            sampledSuffixArray[sampledIndex++] = suffixArray[i];
        }
        suffixArray = sampledSuffixArray;
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        char[] sortedAas = new char[AminoAcid.getAminoAcids().length + 2];
        System.arraycopy(AminoAcid.getAminoAcids(), 0, sortedAas, 0, AminoAcid.getAminoAcids().length);
        sortedAas[AminoAcid.getAminoAcids().length] = '$';
        sortedAas[AminoAcid.getAminoAcids().length + 1] = '/';
        Arrays.sort(sortedAas);

        long[] alphabet = new long[]{0, 0};
        for (int i = 0; i < sortedAas.length; ++i) {
            int shift = sortedAas[i] - (sortedAas[i] & 64);
            alphabet[(int) (sortedAas[i] >> 6)] |= 1L << shift;
        }

        occurrenceTable = new WaveletTree(bwt, alphabet, waitingHandler);
        lessTable = occurrenceTable.createLessTable();
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }
    }

    /**
     * Returns a list of all possible amino acids per position in the peptide
     * according to the sequence matching preferences.
     *
     * @param peptide the peptide
     * @param seqMatchPref the sequence matching preferences
     * @param numPositions the number of positions
     * @return a list of all possible amino acids per position in the peptide
     */
    public ArrayList<String> createPeptideCombinations(String peptide, SequenceMatchingPreferences seqMatchPref, int[] numPositions) {
        ArrayList<String> combinations = new ArrayList<String>();
        int numCombination = 1; // @TODO: is not used. can it be removed?

        SequenceMatchingPreferences.MatchingType sequenceMatchingType = seqMatchPref.getSequenceMatchingType();
        if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.string) {
            for (int i = 0; i < peptide.length(); ++i) {
                combinations.add(peptide.substring(i, i + 1));
            }
        } else {
            double maxX = seqMatchPref.getLimitX();
            double countX = 0;
            for (int i = 0; i < peptide.length(); ++i) {
                if (peptide.charAt(i) == 'X') {
                    ++countX;
                }
            }
            if (countX / (double) (peptide.length()) < maxX) {
                if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.aminoAcid || sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids) {
                    boolean indistinghuishable = sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids;

                    for (int i = 0; i < peptide.length(); ++i) {
                        if (AminoAcid.getAminoAcid(peptide.charAt(i)).iscombination()) {
                            String chars = peptide.substring(i, i + 1);
                            char[] aaCombinations = AminoAcid.getAminoAcid(peptide.charAt(i)).getSubAminoAcids();
                            for (int j = 0; j < aaCombinations.length; ++j) {
                                chars += aaCombinations[j];
                            }
                            combinations.add(chars);
                            numCombination *= chars.length();
                            numPositions[0] += 1;
                        } else if (indistinghuishable && (peptide.charAt(i) == 'I' || peptide.charAt(i) == 'L')) {
                            String chars = peptide.substring(i, i + 1);
                            char[] aaCombinations = AminoAcid.getAminoAcid(peptide.charAt(i)).getCombinations();
                            for (int j = 0; j < aaCombinations.length; ++j) {
                                chars += aaCombinations[j];
                            }
                            switch (peptide.charAt(i)) {
                                case 'I':
                                    chars += "L";
                                    break;
                                case 'L':
                                    chars += "I";
                                    break;
                            }

                            combinations.add(chars);
                            numCombination *= chars.length();
                            numPositions[0] += 1;
                        } else {
                            combinations.add(peptide.substring(i, i + 1));
                        }
                    }
                }
            } else {
                numPositions[1] = 0;
            }
        }
        return combinations;
    }

    /**
     * Method to get the text position using the sampled suffix array.
     *
     * @param index the position
     * @return the text position
     */
    private int getTextPosition(int index) {
        int numIterations = 0;
        while (((index & samplingMask) != 0) && (index != 0)) {
            int aa = occurrenceTable.getCharacter(index);
            index = lessTable[aa] + occurrenceTable.getRank(index - 1, aa);
            ++numIterations;
        }
        int pos = suffixArray[index >> samplingShift] + numIterations;
        return (pos < indexStringLength) ? pos : pos - indexStringLength;
    }

    /**
     * Main method for mapping a peptide with all variants against all
     * registered proteins in the experiment.
     *
     * @param peptide the peptide
     * @param seqMatchPref the sequence matching preferences
     * @return the protein mapping
     */
    @Override
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptide, SequenceMatchingPreferences seqMatchPref) {

        HashMap<String, HashMap<String, ArrayList<Integer>>> allMatches = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int p = peptide.length();
        int[] numPositions = new int[]{0, 1};
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref, numPositions);
        int k = numPositions[0];

        if (numPositions[1] > 0) {

            ArrayList<ArrayList<HashMap<Long, MatrixContent>>> matrix = new ArrayList<ArrayList<HashMap<Long, MatrixContent>>>();

            for (int i = 0; i <= k; ++i) {
                matrix.add(new ArrayList<HashMap<Long, MatrixContent>>());
                for (int j = 0; j <= p; ++j) {
                    matrix.get(i).add(new HashMap<Long, MatrixContent>());
                }
            }

            matrix.get(0).get(0).put(0L, new MatrixContent(0, indexStringLength - 1, '\0', -1, 0)); // L, R, char, traceback, last_index
            int layerStartingPos = 0;

            for (int i = 0; i <= k; ++i) {

                ArrayList<HashMap<Long, MatrixContent>> row = matrix.get(i);
                int tmpLayerStartingPos = p;

                for (int j = layerStartingPos; j < p; ++j) {

                    HashMap<Long, MatrixContent> cell = row.get(j);

                    for (Long key : cell.keySet()) {

                        boolean first = true;
                        MatrixContent content = cell.get(key);
                        int leftIndexOld = content.left;
                        int rightIndexOld = content.right;

                        for (int l = 0; l < combinations.get(j).length(); ++l) {

                            int intAminoAcid = (int) combinations.get(j).charAt(l);
                            int leftIndex = lessTable[intAminoAcid] + ((0 < leftIndexOld) ? occurrenceTable.getRank(leftIndexOld - 1, intAminoAcid) : 0);
                            int rightIndex = lessTable[intAminoAcid] + occurrenceTable.getRank(rightIndexOld, intAminoAcid) - 1;

                            if (leftIndex <= rightIndex) {
                                Long newKey = new Long(leftIndex * indexStringLength + rightIndex);
                                if (first) {
                                    row.get(j + 1).put(newKey, new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, 0, key));
                                } else if (i < k) {
                                    matrix.get(i + 1).get(j + 1).put(newKey, new MatrixContent(leftIndex, rightIndex, (char)intAminoAcid, 1, key));
                                    tmpLayerStartingPos = Math.min(tmpLayerStartingPos, j + 1);
                                }
                            }

                            first = false;
                        }
                    }
                }

                layerStartingPos = tmpLayerStartingPos;
            }

            // Traceback
            for (int i = 0; i <= k; ++i) {

                for (Long key : matrix.get(i).get(p).keySet()) {

                    int currentI = i;
                    int currentJ = p;
                    Long currentKey = key;
                    String currentPeptide = "";

                    while (true) {
                        MatrixContent content = matrix.get(currentI).get(currentJ).get(currentKey);
                        if (content.traceback == -1) {
                            break;
                        }
                        currentI -= content.traceback;
                        currentJ -= 1;
                        currentKey = content.lastIndex;
                        currentPeptide += content.character;
                    }

                    MatrixContent contentLR = matrix.get(i).get(p).get(key);

                    int leftIndex = contentLR.left;
                    int rightIndex = contentLR.right;

                    HashMap<String, ArrayList<Integer>> matches = new HashMap<String, ArrayList<Integer>>();

                    for (int j = leftIndex; j <= rightIndex; ++j) {
                        int pos = getTextPosition(j);
                        int index = binarySearch(boundaries, pos);
                        String accession = accessions.get(index);

                        if (!matches.containsKey(accession)) {
                            matches.put(accession, new ArrayList<Integer>());
                        }
                        matches.get(accession).add(pos - boundaries.get(index));
                    }

                    allMatches.put(currentPeptide, matches);
                }
            }
        }

        return allMatches;
    }

    @Override
    public void emptyCache() {
        // No cache here
    }

    @Override
    public void close() throws IOException, SQLException {
        // No open connection here
    }

    @Override
    public HashMap<Peptide, HashMap<String, ArrayList<Integer>>> getProteinMapping(Tag tag, TagMatcher tagMatcher, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
