package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory.ProteinIterator;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.Duration;
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
    private int[] suffixArrayReversed = null;

    /**
     * Wavelet tree for storing the burrows wheeler transform.
     */
    private WaveletTree occurrenceTable = null;
    private WaveletTree occurrenceTableReversed = null;
    /**
     * Less table for doing an update step according to the LF step.
     */

    private int[] lessTable = null;
    private int[] lessTableReversed = null;

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
     *
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     */
    public FMIndex(WaitingHandler waitingHandler, boolean displayProgress) {
        SequenceFactory sf = SequenceFactory.getInstance(100000);
        boolean deNovo = false; // TODO: change it for de novo
        int maxProgressBar = 6 + ((deNovo) ? 4 : 0);

        if (waitingHandler != null && displayProgress && !waitingHandler.isRunCanceled()) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(maxProgressBar
            );
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

        bwt = null;
        if (deNovo) {
            byte[] T_ref = new byte[indexStringLength];
            for (int i = 0; i < indexStringLength - 1; ++i) {
                T_ref[indexStringLength - 2 - i] = T[i];
            }
            T_ref[indexStringLength - 1] = '$';

            // create the suffix array using at most 128 characters
            suffixArrayReversed = SuffixArraySorter.buildSuffixArray(T_ref, 128);
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            // create Burrows-Wheeler-Transform
            bwt = new byte[indexStringLength];
            for (int i = 0; i < indexStringLength; ++i) {
                bwt[i] = (suffixArrayReversed[i] != 0) ? T_ref[suffixArrayReversed[i] - 1] : T_ref[indexStringLength - 1];
            }
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            // sampling suffix array
            int[] sampledSuffixArrayReversed = new int[((indexStringLength + 1) >> samplingShift) + 1];
            int sampledIndexReversed = 0;
            for (int i = 0; i < indexStringLength; i += sampling) {
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    return;
                }
                sampledSuffixArrayReversed[sampledIndexReversed++] = suffixArrayReversed[i];
            }
            suffixArrayReversed = sampledSuffixArrayReversed;
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            
            

            occurrenceTableReversed = new WaveletTree(bwt, alphabet, waitingHandler);
            lessTableReversed = occurrenceTableReversed.createLessTable();
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }
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
     * Returns a list of all possible amino acids per position in the peptide
     * according to the sequence matching preferences.
     *
     * @param peptide the peptide
     * @param seqMatchPref the sequence matching preferences
     * @param numPositions the number of positions
     * @return a list of all possible amino acids per position in the peptide
     */
    public ArrayList<TagComponent> createPeptideCombinations(ArrayList<TagComponent> tagComponents, SequenceMatchingPreferences seqMatchPref, int[] numPositions) {
        ArrayList<TagComponent> combinations = new ArrayList<TagComponent>();

        SequenceMatchingPreferences.MatchingType sequenceMatchingType = seqMatchPref.getSequenceMatchingType();
        if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.string) {
            for (int i = 0; i < tagComponents.size(); ++i) {
                TagComponent tagComponent = tagComponents.get(i);
                if (tagComponent instanceof AminoAcidSequence) {
                    String subSequence = ((AminoAcidSequence) tagComponent).asSequence();
                    
                    for (int j = 0; j < subSequence.length(); ++j) {
                        combinations.add(new AminoAcidSequence(subSequence.substring(j, j + 1)));
                    }
                } else if (tagComponent instanceof MassGap) {
                    combinations.add(new MassGap(tagComponent.getMass()));
                } else {
                    throw new UnsupportedOperationException("FM-Index createPeptideCombinations not implemeted for tag component.");
                }
            }
        } else //double maxX = seqMatchPref.getLimitX();
        //double countX = 0;
        if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.aminoAcid || sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids) {
            boolean indistinghuishable = sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids;

            for (TagComponent tagComponent : tagComponents) {
                if (tagComponent instanceof AminoAcidSequence) {
                    String subSequence = tagComponent.asSequence();
                    for (char amino : subSequence.toCharArray()) {
                        //countX += (amino == 'X') ? 1 : 0;
                        if (AminoAcid.getAminoAcid(amino).iscombination()) {
                            String chars = String.valueOf(amino);
                            char[] aaCombinations = AminoAcid.getAminoAcid(amino).getSubAminoAcids();
                            for (int j = 0; j < aaCombinations.length; ++j) {
                                chars += aaCombinations[j];
                            }
                            combinations.add(new AminoAcidSequence(chars));
                            numPositions[0] += 1;
                        } else if (indistinghuishable && (amino == 'I' || amino == 'L')) {
                            String chars = String.valueOf(amino);
                            char[] aaCombinations = AminoAcid.getAminoAcid(amino).getCombinations();
                            for (int j = 0; j < aaCombinations.length; ++j) {
                                chars += aaCombinations[j];
                            }
                            switch (amino) {
                                case 'I':
                                    chars += "L";
                                    break;
                                case 'L':
                                    chars += "I";
                                    break;
                            }

                            combinations.add(new AminoAcidSequence(chars));
                            numPositions[0] += 1;
                        } else {
                            combinations.add(new AminoAcidSequence(String.valueOf(amino)));
                        }
                    }
                } else if (tagComponent instanceof MassGap) {
                    combinations.add(new MassGap(tagComponent.getMass()));
                } else {
                    throw new UnsupportedOperationException("FM-Index createPeptideCombinations not implemeted for tag component.");
                }
            }
        } /*if (countX / (double) (combinations.size()) > maxX) {
                numPositions[1] = 0;
            }*/
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
     * registered proteins in the experiment. This method is implementing the
     * backward search.
     *
     * @param peptide the peptide
     * @param seqMatchPref the sequence matching preferences
     * @return the protein mapping
     */
    @Override
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptide, SequenceMatchingPreferences seqMatchPref) {

        HashMap<String, HashMap<String, ArrayList<Integer>>> allMatches = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int lenPeptide = peptide.length();
        int[] numPositions = new int[]{0, 1};
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref, numPositions);

        if (numPositions[1] > 0) {

            ArrayList<HashMap<Long, MatrixContent>> backwardList = new ArrayList<HashMap<Long, MatrixContent>>();

            for (int i = 0; i <= lenPeptide; ++i) {
                backwardList.add(new HashMap<Long, MatrixContent>());
            }

            backwardList.get(0).put(0L, new MatrixContent(0, indexStringLength - 1, '\0', 0)); // L, R, char, last_index
            int layerStartingPos = 0;

            for (int j = layerStartingPos; j < lenPeptide; ++j) {

                HashMap<Long, MatrixContent> cell = backwardList.get(j);

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
                            Long newKey = new Long((((long) leftIndex) << 32) + (long) rightIndex);
                            backwardList.get(j + 1).put(newKey, new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, key));
                        }
                    }
                }
            }

            // Traceback
            for (Long key : backwardList.get(lenPeptide).keySet()) {
                Long currentKey = key;
                String currentPeptide = "";

                for (int j = lenPeptide; j > 0; --j) {
                    MatrixContent content = backwardList.get(j).get(currentKey);
                    currentKey = content.lastIndex;
                    currentPeptide += content.character;
                }

                MatrixContent contentLR = backwardList.get(lenPeptide).get(key);

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

        Duration d = new Duration();
        d.start();
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> allMatches = new HashMap<Peptide, HashMap<String, ArrayList<Integer>>>();
        
        
        int maxSequencePosition = -1;
        for (int i = tag.getContent().size() - 1; i >= 0; --i) {
            TagComponent tagComponent = tag.getContent().get(i);
            if (tagComponent instanceof AminoAcidSequence) {
                if (maxSequencePosition == -1 || tag.getContent().get(maxSequencePosition).asSequence().length() < tag.getContent().get(i).asSequence().length()){
                    maxSequencePosition = i;
                }
            }
        }
        
        System.out.println(maxSequencePosition);

        ArrayList<TagComponent> tagComponents = new ArrayList<TagComponent>();
        for (int i = maxSequencePosition - 1; i >= 0; --i) {
            TagComponent tagComponent = tag.getContent().get(i);
            if (tagComponent instanceof MassGap) {
                tagComponents.add(new MassGap(tagComponent.getMass()));
            } else if (tagComponent instanceof AminoAcidSequence) {
                tagComponents.add(new AminoAcidSequence((new StringBuilder(tagComponent.asSequence()).reverse()).toString()));
            } else {
                throw new UnsupportedOperationException("Tag constructor not implemeted for tag component " + tagComponent.getClass() + ".");
            }
        }
        
        
        
        
        ArrayList<TagComponent> tagComponentsReverse = new ArrayList<TagComponent>();
        for (int i = maxSequencePosition; i < tag.getContent().size(); ++i) {
            tagComponentsReverse.add(tag.getContent().get(i));
        }

        int[] numPositions = new int[]{0, 1};
        ArrayList<TagComponent> combinations = createPeptideCombinations(tagComponents, sequenceMatchingPreferences, numPositions);
        ArrayList<TagComponent> combinationsReversed = createPeptideCombinations(tagComponentsReverse, sequenceMatchingPreferences, numPositions);
        int lenCombinations = combinations.size();
        int lenCombinationsReversed = combinationsReversed.size();

        String ctoString = "";
        for (TagComponent c : tag.getContent()) {
            ctoString += c.asSequence() + " ";
        }
        
        

        if (numPositions[1] == 0) {
            return allMatches;
        }

        int aminoInsertionsReversed = 0;
        double minMass = AminoAcid.getAminoAcid('G').getMonoisotopicMass();
        for (int j = 0; j < tag.getContent().size(); ++j) {
            if (tag.getContent().get(j) instanceof MassGap) {
                aminoInsertionsReversed += Math.ceil(tag.getContent().get(j).getMass() / minMass);
            }
        }

        int aminoInsertions = 0;
        for (int j = 0; j < tagComponents.size(); ++j) {
            if (tagComponents.get(j) instanceof MassGap) {
                aminoInsertions += Math.ceil(tagComponents.get(j).getMass() / minMass);
            }
        }

        ArrayList<ArrayList<HashMap<Long, MatrixContent>>> matrixReversed = new ArrayList<ArrayList<HashMap<Long, MatrixContent>>>();
        ArrayList<ArrayList<HashMap<Long, MatrixContent>>> matrix = new ArrayList<ArrayList<HashMap<Long, MatrixContent>>>();

        for (int i = 0; i <= aminoInsertionsReversed; ++i) {
            matrix.add(new ArrayList<HashMap<Long, MatrixContent>>());
            matrixReversed.add(new ArrayList<HashMap<Long, MatrixContent>>());
            for (int j = 0; j <= lenCombinationsReversed; ++j) {
                matrix.get(i).add(new HashMap<Long, MatrixContent>());
                matrixReversed.get(i).add(new HashMap<Long, MatrixContent>());
            }
        }
        matrixReversed.get(0).get(0).put(0L, new MatrixContent(0, indexStringLength - 1, '\0', 0, 0, 0, 0, null)); // L, R, char, tracebackRow, tracebackCol, last_index, mass, peptideSequence

        boolean nextRow = true;
        for (int i = 0; i <= aminoInsertionsReversed && nextRow; ++i) {
            nextRow = false;
            ArrayList<HashMap<Long, MatrixContent>> row = matrixReversed.get(i);
            for (int j = 0; j < lenCombinationsReversed; ++j) {
                HashMap<Long, MatrixContent> cell = row.get(j);
                for (Long key : cell.keySet()) {

                    MatrixContent content = cell.get(key);
                    int leftIndexOld = content.left;
                    int rightIndexOld = content.right;
                    
                    if (combinationsReversed.get(j) instanceof AminoAcidSequence) {
                        for (int l = 0; l < combinationsReversed.get(j).asSequence().length(); ++l) {
                            int intAminoAcid = (int) combinationsReversed.get(j).asSequence().charAt(l);
                            int leftIndex = lessTableReversed[intAminoAcid] + occurrenceTableReversed.getRank(leftIndexOld - 1, intAminoAcid);
                            int rightIndex = lessTableReversed[intAminoAcid] + occurrenceTableReversed.getRank(rightIndexOld, intAminoAcid) - 1;

                            if (leftIndex <= rightIndex) {
                                Long newKey = new Long((((long) leftIndex) << 32) + (long) rightIndex);
                                row.get(j + 1).put(newKey, new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, 0, -1, key, 0, null));
                            }
                        }
                    } else {
                        double oldMass = content.mass;
                        //for (char amino : new char[]{'L', 'K', 'T', 'G'}) {
                        for (char amino : AminoAcid.getAminoAcids()) {  // TODO: use sorted list of amino acids and break
                            double newMass = oldMass + AminoAcid.getAminoAcid(amino).getMonoisotopicMass();
                            if (newMass - massTolerance > combinationsReversed.get(j).getMass()) {
                                continue;
                            }

                            int intAminoAcid = (int) amino;
                            int leftIndex = lessTableReversed[intAminoAcid] + occurrenceTableReversed.getRank(leftIndexOld - 1, intAminoAcid);
                            int rightIndex = lessTableReversed[intAminoAcid] + occurrenceTableReversed.getRank(rightIndexOld, intAminoAcid) - 1;

                            if (leftIndex <= rightIndex) {
                                Long newKey = new Long((((long) leftIndex) << 32) + (long) rightIndex);
                                if (Math.abs(combinationsReversed.get(j).getMass() - newMass) < massTolerance) {
                                    row.get(j + 1).put(newKey, new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, 0, -1, key, 0, null));
                                } else if (i < aminoInsertionsReversed) {
                                    matrixReversed.get(i + 1).get(j).put(newKey, new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, -1, 0, key, newMass, null));
                                    nextRow = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Traceback Reverse
        for (int i = 0; i <= aminoInsertionsReversed; ++i) {

            for (Long key : matrixReversed.get(i).get(lenCombinationsReversed).keySet()) {
                int currentI = i;
                int currentJ = lenCombinationsReversed;
                Long currentKey = key;
                String currentPeptide = "";

                int leftIndexFront = 0;
                int rightIndexFront = indexStringLength - 1;
                
                while (true) {
                    MatrixContent content = matrixReversed.get(currentI).get(currentJ).get(currentKey);
                    if (content.tracebackRow == 0 && content.tracebackCol == 0) {
                        break;
                    }
                    currentI += content.tracebackRow;
                    currentJ += content.tracebackCol;
                    currentPeptide += content.character;
                    currentKey = content.lastIndex;
                    leftIndexFront = lessTable[content.character] + occurrenceTable.getRank(leftIndexFront - 1, content.character);
                    rightIndexFront = lessTable[content.character] + occurrenceTable.getRank(rightIndexFront, content.character) - 1;
                }
                matrix.get(0).get(0).put((long)i, new MatrixContent(leftIndexFront, rightIndexFront, '\0', 0, 0, 0, 0, (new StringBuilder(currentPeptide).reverse()).toString()));
            }
        }
        
        
        
        
        // Map Front
        nextRow = true;
        for (int i = 0; i <= aminoInsertions && nextRow; ++i) {
            nextRow = false;
            ArrayList<HashMap<Long, MatrixContent>> row = matrix.get(i);
            for (int j = 0; j < lenCombinations; ++j) {
                HashMap<Long, MatrixContent> cell = row.get(j);

                for (Long key : cell.keySet()) {
                    MatrixContent content = cell.get(key);
                    int leftIndexOld = content.left;
                    int rightIndexOld = content.right;

                    if (combinations.get(j) instanceof AminoAcidSequence) {
                        for (int l = 0; l < combinations.get(j).asSequence().length(); ++l) {

                            int intAminoAcid = (int) combinations.get(j).asSequence().charAt(l);
                            int leftIndex = lessTable[intAminoAcid] + occurrenceTable.getRank(leftIndexOld - 1, intAminoAcid);
                            int rightIndex = lessTable[intAminoAcid] + occurrenceTable.getRank(rightIndexOld, intAminoAcid) - 1;

                            if (leftIndex <= rightIndex) {
                                Long newKey = new Long((((long) leftIndex) << 32) + (long) rightIndex);
                                row.get(j + 1).put(newKey, new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, 0, -1, key, 0, null));
                            }
                        }
                    } else {
                        double oldMass = content.mass;
                        for (char amino : AminoAcid.getAminoAcids()) {  // TODO: use sorted list of amino acids and break
                            double newMass = oldMass + AminoAcid.getAminoAcid(amino).getMonoisotopicMass();
                            if (newMass - massTolerance > combinations.get(j).getMass()) {
                                continue;
                            }

                            int intAminoAcid = (int) amino;
                            int leftIndex = lessTable[intAminoAcid] + occurrenceTable.getRank(leftIndexOld - 1, intAminoAcid);
                            int rightIndex = lessTable[intAminoAcid] + occurrenceTable.getRank(rightIndexOld, intAminoAcid) - 1;
                            
                            if (leftIndex <= rightIndex) {
                                Long newKey = new Long((((long) leftIndex) << 32) + (long) rightIndex);
                                if (Math.abs(combinations.get(j).getMass() - newMass) < massTolerance) {
                                    row.get(j + 1).put(newKey, new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, 0, -1, key, 0, null));
                                } else if (i < aminoInsertions) {
                                    matrix.get(i + 1).get(j).put(newKey, new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, -1, 0, key, newMass, null));
                                    nextRow = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        
        String pep = "";
        // Traceback Front
        for (int i = 0; i <= aminoInsertions; ++i) {

            for (Long key : matrix.get(i).get(lenCombinations).keySet()) {

                int currentI = i;
                int currentJ = lenCombinations;
                Long currentKey = key;
                String currentPeptide = "";

                while (true) {
                    MatrixContent content = matrix.get(currentI).get(currentJ).get(currentKey);
                    if (content.tracebackRow == 0 && content.tracebackCol == 0) {
                        break;
                    }
                    currentI += content.tracebackRow;
                    currentJ += content.tracebackCol;
                    currentKey = content.lastIndex;
                    currentPeptide += content.character;
                }

                MatrixContent contentLR = matrix.get(i).get(lenCombinations).get(key);

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
                pep = currentPeptide + matrix.get(currentI).get(currentJ).get(currentKey).peptideSequence;
                allMatches.put(new Peptide(currentPeptide + matrix.get(currentI).get(currentJ).get(currentKey).peptideSequence, null), matches);
            }
        }
        d.end();
        System.out.println("Duration: " + d.toString() + " " + ctoString + " " + pep);
        return allMatches;
    }
}
