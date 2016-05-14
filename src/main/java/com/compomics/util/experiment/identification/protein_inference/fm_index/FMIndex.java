package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory.ProteinIterator;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import com.sun.prism.impl.PrismSettings;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
    private int[] suffixArrayPrimary = null;
    //private int[] suffixArrayReversed = null;

    /**
     * Wavelet tree for storing the burrows wheeler transform.
     */
    public WaveletTree occurrenceTablePrimary = null;
    public WaveletTree occurrenceTableReversed = null;
    /**
     * Less table for doing an update step according to the LF step.
     */

    public int[] lessTablePrimary = null;
    public int[] lessTableReversed = null;

    /**
     * Length of the indexed string (all concatenated protein sequences).
     */
    public int indexStringLength = 0;
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
    private int[] boundaries = null;
    /**
     * List of all accession IDs in the FASTA file.
     */
    private String[] accessions = null;

    /**
     * List of all amino acid masses
     */
    private double[] aaMasses = null;

    /**
     * List of all amino acid masses
     */
    private String[] modifictationLabels = null;
    
    /**
     * 
     */
    private boolean withVariableModifications = false;

    /**
     * Returns the position of a value in the array or if not found the position
     * of the closest smaller value.
     *
     * @param array the array
     * @param key the key
     * @return he position of a value in the array or if not found the position
     * of the closest smaller value
     */
    private static int binarySearch(int[] array, int key) {
        int low = 0;
        int mid = 0;
        int high = array.length - 1;
        while (low <= high) {
            mid = (low + high) >> 1;
            if (array[mid] <= key) low = mid + 1;
            else high = mid - 1;
        }
        if (mid > 0 && key < array[mid]) {
            mid -= 1;
        }
        return mid;
    }

    /**
     * Constructor. If ptmSettings are provided the index will contain modification information, ignored if null.
     *
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param ptmSettings contains modification parameters for identification
     */
    public FMIndex(WaitingHandler waitingHandler, boolean displayProgress, PtmSettings ptmSettings) {
        
        
        if (ptmSettings != null){
            // create masses table and modifications
            int[] modificationCounts = new int[128];
            for (int i = 0; i < modificationCounts.length; ++i) modificationCounts[i] = 0;
            ArrayList<String> variableModifications = ptmSettings.getVariableModifications();
            ArrayList<String> fixedModifications = ptmSettings.getFixedModifications();
            PTMFactory ptmFactory = PTMFactory.getInstance();


            int highestAAmodificationNum = 0;

            // check which amino acids have variable modificatitions
            for (String modification : variableModifications){
                PTM ptm = ptmFactory.getPTM(modification);
                if (ptm.getPattern().length() > 1) throw new UnsupportedOperationException();
                ArrayList<Character> targets = ptm.getPattern().getAminoAcidsAtTarget();
                modificationCounts[targets.get(0)]++;
                highestAAmodificationNum = Math.max(highestAAmodificationNum, modificationCounts[targets.get(0)]);
                withVariableModifications = true;
            }

            // create masses for all amino acids including modifications
            aaMasses = new double[128 * (1 + highestAAmodificationNum)];
            modifictationLabels = new String[128 * (1 + highestAAmodificationNum)];
            for(int i = 0; i < aaMasses.length; ++i) aaMasses[i] = -1;
            for(int i = 0; i < aaMasses.length; ++i) modifictationLabels[i] = null;
            char[] aminoAcids = AminoAcid.getAminoAcids();
            for (int i = 0; i < aminoAcids.length; ++i) {
                aaMasses[aminoAcids[i]] = AminoAcid.getAminoAcid(aminoAcids[i]).getMonoisotopicMass();
            }

            // change masses for fixed modifications
            for (String modification : fixedModifications){
                PTM ptm = ptmFactory.getPTM(modification);
                if (ptm.getPattern().length() > 1) throw new UnsupportedOperationException();
                ArrayList<Character> targets = ptm.getPattern().getAminoAcidsAtTarget();
                if (modificationCounts[targets.get(0)] != 0){
                     throw new UnsupportedOperationException("Assignment of fixed and variable modification to the same amino acid");
                }
                aaMasses[targets.get(0)] += ptm.getMass();
                modifictationLabels[targets.get(0)] = modification;
            }
            
            // add masses for variable modifications
            for (int i = 0; i < modificationCounts.length; ++i) modificationCounts[i] = 1;
            for (String modification : variableModifications){
                PTM ptm = ptmFactory.getPTM(modification);
                ArrayList<Character> targets = ptm.getPattern().getAminoAcidsAtTarget();
                aaMasses[128 * modificationCounts[targets.get(0)] + targets.get(0)] = aaMasses[targets.get(0)] + ptm.getMass();
                modifictationLabels[128 * modificationCounts[targets.get(0)] + targets.get(0)] = modification;
                modificationCounts[targets.get(0)]++;
            }
            
        }
        else {
            // create masses for all amino acids
            aaMasses = new double[128];
            for(int i = 0; i < aaMasses.length; ++i) aaMasses[i] = -1;
            char[] aminoAcids = AminoAcid.getAminoAcids();
            for (int i = 0; i < aminoAcids.length; ++i) {
                aaMasses[aminoAcids[i]] = AminoAcid.getAminoAcid(aminoAcids[i]).getMonoisotopicMass();
            }
        }
        
        
        
        
        SequenceFactory sf = SequenceFactory.getInstance(100000);
        boolean deNovo = true; // TODO: change it for de novo
        int maxProgressBar = 6 + ((deNovo) ? 4 : 0);

        if (waitingHandler != null && displayProgress && !waitingHandler.isRunCanceled()) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(maxProgressBar
            );
            waitingHandler.setSecondaryProgressCounter(0);
        }

        
        
        // reading all proteins in a first pass to get information about number and total length
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

        
        boundaries = new int[numProteins + 1];
        accessions = new String[numProteins];
        boundaries[0] = 0;
        
        
        
        // reading proteins in a second pass to store their amino acid sequences and their accession numbers
        int tmpN = 0;
        int tmpNumProtein = 0;
        try {
            ProteinIterator pi = sf.getProteinIterator(false);

            while (pi.hasNext()) {
                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    return;
                }
                Protein currentProtein = pi.getNextProtein();
                int proteinLen = currentProtein.getLength();
                if (tmpN > 0) T[tmpN++] = '/'; // adding the delimiters
                System.arraycopy(currentProtein.getSequence().getBytes(), 0, T, tmpN, proteinLen);
                tmpN += proteinLen;
                accessions[tmpNumProtein++] = currentProtein.getAccession();
                boundaries[tmpNumProtein] = tmpN + 1;
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        
        // create the suffix array using at most 128 characters
        suffixArrayPrimary = SuffixArraySorter.buildSuffixArray(T, 128);
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }
        

        // Prepare alphabet
        char[] sortedAas = new char[AminoAcid.getAminoAcids().length + 2];
        System.arraycopy(AminoAcid.getAminoAcids(), 0, sortedAas, 0, AminoAcid.getAminoAcids().length);
        sortedAas[AminoAcid.getAminoAcids().length] = '$';
        sortedAas[AminoAcid.getAminoAcids().length + 1] = '/';
        Arrays.sort(sortedAas);
        long[] alphabet = new long[]{0, 0};
        for (int i = 0; i < sortedAas.length; ++i) {
            alphabet[sortedAas[i] >> 6] |= 1L << (sortedAas[i] & 63);
        }

        
        // create Burrows-Wheeler-Transform
        byte[] bwt = new byte[indexStringLength];
        for (int i = 0; i < indexStringLength; ++i) {
            bwt[i] = (suffixArrayPrimary[i] != 0) ? T[suffixArrayPrimary[i] - 1] : T[indexStringLength - 1];
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
            sampledSuffixArray[sampledIndex++] = suffixArrayPrimary[i];
        }
        suffixArrayPrimary = sampledSuffixArray;
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }
        
        
        // creating the occurrence table and less table for backward search over forward text
        occurrenceTablePrimary = new WaveletTree(bwt, alphabet, waitingHandler, true);
        lessTablePrimary = occurrenceTablePrimary.createLessTable();
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        
        bwt = null;
        if (deNovo) {
            // create inversed text for inversed index
            byte[] TReversed = new byte[indexStringLength];
            for (int i = 0; i < indexStringLength - 1; ++i) {
                TReversed[indexStringLength - 2 - i] = T[i];
            }
            TReversed[indexStringLength - 1] = '$';
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            // create the inversed suffix array using at most 128 characters
            int[] suffixArrayReversed = SuffixArraySorter.buildSuffixArray(TReversed, 128);
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            // create inversed Burrows-Wheeler-Transform
            bwt = new byte[indexStringLength];
            for (int i = 0; i < indexStringLength; ++i) {
                bwt[i] = (suffixArrayReversed[i] != 0) ? TReversed[suffixArrayReversed[i] - 1] : TReversed[indexStringLength - 1];
            }
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            // create inversed less and occurrence table
            occurrenceTableReversed = new WaveletTree(bwt, alphabet, waitingHandler, true);
            lessTableReversed = occurrenceTableReversed.createLessTable();
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            TReversed = null;
        }

        T = null;
        bwt = null;

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
    private ArrayList<String> createPeptideCombinations(String peptide, SequenceMatchingPreferences seqMatchPref, int[] numPositions) {
        ArrayList<String> combinations = new ArrayList<String>();

        SequenceMatchingPreferences.MatchingType sequenceMatchingType = seqMatchPref.getSequenceMatchingType();
        if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.string) {
            for (int i = 0; i < peptide.length(); ++i) {
                combinations.add(peptide.substring(i, i + 1));
            }
        } else {
            double maxX = (seqMatchPref.getLimitX() != null) ? seqMatchPref.getLimitX() : 1;
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
                        //if (AminoAcid.getAminoAcid(peptide.charAt(i)).iscombination()) {
                        String chars = peptide.substring(i, i + 1);
                        char[] aaCombinations = AminoAcid.getAminoAcid(peptide.charAt(i)).getCombinations();
                        for (int j = 0; j < aaCombinations.length; ++j) {
                            chars += aaCombinations[j];
                        }
                        
                        if (indistinghuishable && (peptide.charAt(i) == 'I' || peptide.charAt(i) == 'L')) {
                            switch (peptide.charAt(i)) {
                                case 'I':
                                    chars += "L";
                                    break;
                                case 'L':
                                    chars += "I";
                                    break;
                            }

                        }
                        combinations.add(chars);
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
     * @param tagComponents
     * @param seqMatchPref
     * @param numPositions
     * @return
     */
    private TagElement[] createPeptideCombinations(TagElement[] tagComponents, SequenceMatchingPreferences seqMatchPref) {

        int numElements = 0;
        for (int i = 0; i < tagComponents.length; ++i) {
            if (tagComponents[i].isMass) {
                ++numElements;
            } else {
                numElements += tagComponents[i].sequence.length();
            }
        }

        TagElement[] combinations = new TagElement[numElements];

        int combinationPosition = 0;
        SequenceMatchingPreferences.MatchingType sequenceMatchingType = seqMatchPref.getSequenceMatchingType();
        if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.string) {
            for (TagElement tagElement : tagComponents) {
                if (tagElement.isMass) {
                    combinations[combinationPosition++] = new TagElement(true, "", tagElement.mass, 0);
                } else {
                    for (int j = 0; j < tagElement.sequence.length(); ++j) {
                        combinations[combinationPosition++] = new TagElement(false, tagElement.sequence.substring(j, j + 1), tagElement.mass, tagElement.xNumLimit);
                    }
                }
            }
        } else {
            if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.aminoAcid || sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids) {
                boolean indistinghuishable = sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids;

                for (TagElement tagElement : tagComponents) {
                    if (!tagElement.isMass) {
                        String subSequence = tagElement.sequence;
                        for (char amino : subSequence.toCharArray()) {
                            String chars = String.valueOf(amino);
                            char[] aaCombinations = AminoAcid.getAminoAcid(amino).getCombinations();
                            for (int j = 0; j < aaCombinations.length; ++j) {
                                chars += aaCombinations[j];
                            }
                            if (indistinghuishable && (amino == 'I' || amino == 'L')) {
                                switch (amino) {
                                    case 'I':
                                        chars += "L";
                                        break;
                                    case 'L':
                                        chars += "I";
                                        break;
                                }

                            }
                            combinations[combinationPosition++] = new TagElement(false, chars, tagElement.mass, tagElement.xNumLimit);
                        }
                    } else {
                        combinations[combinationPosition++] = new TagElement(true, "", tagElement.mass, tagElement.xNumLimit);
                    }
                }
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
            int aa = occurrenceTablePrimary.getCharacter(index);
            index = lessTablePrimary[aa] + occurrenceTablePrimary.getRank(index - 1, aa);
            ++numIterations;
        }
        int pos = suffixArrayPrimary[index >> samplingShift] + numIterations;
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
        int maxX = (int)(((seqMatchPref.getLimitX() != null) ? seqMatchPref.getLimitX() : 1) * lenPeptide);

        if (numPositions[1] > 0) {

            ArrayList<MatrixContent>[] backwardList = (ArrayList<MatrixContent>[]) new ArrayList[lenPeptide + 1];

            for (int i = 0; i <= lenPeptide; ++i) {
                backwardList[i] = new ArrayList<MatrixContent>(10);
            }

            backwardList[0].add(new MatrixContent(0, indexStringLength - 1, '\0', null, 0)); // L, R, char, previous content
            for (int j = 0; j < lenPeptide; ++j) {
                String combinationSequence = combinations.get(j);
                ArrayList<MatrixContent> cell = backwardList[j];
                for (MatrixContent content : cell) {
                    int leftIndexOld = content.left;
                    int rightIndexOld = content.right;
                    int numX = content.numX;

                    for (char amino : combinationSequence.toCharArray()) {

                        int intAminoAcid = (int) amino;
                        int leftIndex = lessTablePrimary[intAminoAcid] + occurrenceTablePrimary.getRank(leftIndexOld - 1, intAminoAcid);
                        int rightIndex = lessTablePrimary[intAminoAcid] + occurrenceTablePrimary.getRank(rightIndexOld, intAminoAcid) - 1;
                        
                        
                        if (leftIndex <= rightIndex) {
                            int newNumX = numX + ((amino == 'X') ? 1 : 0);
                            if (newNumX > maxX) continue;
                            backwardList[j + 1].add(new MatrixContent(leftIndex, rightIndex, (char) intAminoAcid, content, newNumX));
                        }
                    }
                }
            }

            // Traceback
            for (MatrixContent content : backwardList[lenPeptide]) {
                MatrixContent currentContent = content;
                String currentPeptide = "";

                while (currentContent.previousContent != null) {
                    currentPeptide += currentContent.character;
                    currentContent = currentContent.previousContent;
                }

                int leftIndex = content.left;
                int rightIndex = content.right;

                HashMap<String, ArrayList<Integer>> matches = new HashMap<String, ArrayList<Integer>>();

                for (int j = leftIndex; j <= rightIndex; ++j) {
                    int pos = getTextPosition(j);
                    int index = binarySearch(boundaries, pos);
                    String accession = accessions[index];

                    if (!matches.containsKey(accession)) {
                        matches.put(accession, new ArrayList<Integer>());
                    }
                    matches.get(accession).add(pos - boundaries[index]);
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
    
    
    private void addModifications(ArrayList<Integer[]> setCharacter){
        int maxNum = setCharacter.size();
        for (int i = 0; i < maxNum; ++i){
            int pos = 128 + setCharacter.get(i)[0];
            while (pos < aaMasses.length && aaMasses[pos] != -1){
                setCharacter.add(new Integer[]{setCharacter.get(i)[0], setCharacter.get(i)[1], setCharacter.get(i)[2], pos});
                pos += 128;
            }
        }
    }
    
    
    private void mappingSequenceAndMasses (TagElement[] combinations, LinkedList<MatrixContent> matrix, ArrayList<MatrixContent> matrixFinished, int[] less, WaveletTree occurrence, double massTolerance){
        final int lenCombinations = combinations.length;
        while (!matrix.isEmpty()) {
            MatrixContent cell = matrix.removeFirst();
            final int pepLen = cell.length;
            final int leftIndexOld = cell.left;
            final int rightIndexOld = cell.right;

            if (combinations[pepLen].isMass) {
                final Double combinationMass = combinations[pepLen].mass;
                final double oldMass = cell.mass;

                ArrayList<Integer[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                if (withVariableModifications) addModifications(setCharacter);
                for (Integer[] borders : setCharacter) {
                    int aminoAcid = borders[0];

                    if (aminoAcid == '$' || aminoAcid == '/') continue;
                    double newMass = oldMass + aaMasses[borders[3]];
                    if (newMass - massTolerance > combinationMass) continue;

                    int lessValue = less[aminoAcid];
                    int leftIndex = lessValue + borders[1];
                    int rightIndex = lessValue + borders[2] - 1;

                    if (Math.abs(combinationMass - newMass) < massTolerance) {
                        List insertList = (pepLen + 1 < lenCombinations) ? matrix : matrixFinished;
                        insertList.add(new MatrixContent(leftIndex, rightIndex, (char) aminoAcid, cell, 0, null, pepLen + 1, 0, borders[3], null));
                    } else {
                        matrix.add(new MatrixContent(leftIndex, rightIndex, (char) aminoAcid, cell, newMass, null, pepLen, 0, borders[3], null));
                    }
                }
            } else {
                final String combinationSequence = combinations[pepLen].sequence;
                final int xNumLimit = combinations[pepLen].xNumLimit;
                int numX = cell.numX;

                for (char aminoAcid : combinationSequence.toCharArray()) {
                    final int leftIndex = less[aminoAcid] + occurrence.getRank(leftIndexOld - 1, aminoAcid);
                    final int rightIndex = less[aminoAcid] + occurrence.getRank(rightIndexOld, aminoAcid) - 1;

                    if (leftIndex <= rightIndex) {
                        int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);
                        if (newNumX > xNumLimit) continue;
                        List insertList = (pepLen + 1 < lenCombinations) ? matrix : matrix;
                        insertList.add(new MatrixContent(leftIndex, rightIndex, (char) aminoAcid, cell, 0, null, pepLen + 1, newNumX, -1, null));
                    }
                }
            }
        }
    }
    
    
    
    
    

    @Override
    public HashMap<Peptide, HashMap<String, ArrayList<Integer>>> getProteinMapping(Tag tag, TagMatcher tagMatcher, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> allMatches = new HashMap<Peptide, HashMap<String, ArrayList<Integer>>>();
        double xLimit = ((sequenceMatchingPreferences.getLimitX() != null) ? sequenceMatchingPreferences.getLimitX() : 1);

        // copying tags into own data structure
        int maxSequencePosition = -1;
        TagElement[] tagElements = new TagElement[tag.getContent().size()];
        for (int i = 0; i < tag.getContent().size(); ++i) {
            if (tag.getContent().get(i) instanceof MassGap) {
                tagElements[i] = new TagElement(true, "", tag.getContent().get(i).getMass(), 0);
            } else if (tag.getContent().get(i) instanceof AminoAcidSequence) {
                tagElements[i] = new TagElement(false, tag.getContent().get(i).asSequence(), 0., (int)(xLimit * tag.getContent().get(i).asSequence().length()));
                if (maxSequencePosition == -1 || tagElements[i].sequence.length() < tagElements[i].sequence.length()) {
                    maxSequencePosition = i;
                }
            } else {
                throw new UnsupportedOperationException("Unsupported tag in tag mapping for FM-Index.");
            }
        }

        final boolean turned = (tagElements.length == 3
                && tagElements[0].isMass
                && !tagElements[1].isMass
                && tagElements[2].isMass
                && tagElements[0].mass < tagElements[2].mass);

        TagElement[] refTagContent = null;
        int[] lessPrimary = null;
        int[] lessReversed = null;
        WaveletTree occurrencePrimary = null;
        WaveletTree occurrenceReversed = null;
        WaveletTree occurrencePrimaryNext = null;
        WaveletTree occurrenceReversedNext = null;

        
        // turning complete tag content if tag set starts with a smaller mass than it ends
        if (turned) {
            refTagContent = new TagElement[tagElements.length];
            for (int i = tagElements.length - 1, j = 0; i >= 0; --i, ++j) {
                String sequenceReversed = (new StringBuilder(tagElements[i].sequence).reverse()).toString();
                refTagContent[j] = new TagElement(tagElements[i].isMass, sequenceReversed, tagElements[i].mass, tagElements[i].xNumLimit);
            }

            lessReversed = lessTablePrimary;
            lessPrimary = lessTableReversed;
            occurrenceReversed = occurrenceTablePrimary;
            occurrencePrimary = occurrenceTableReversed;
        } else {
            refTagContent = tagElements;
            lessPrimary = lessTablePrimary;
            lessReversed = lessTableReversed;
            occurrencePrimary = occurrenceTablePrimary;
            occurrenceReversed = occurrenceTableReversed;
        }

        
        ArrayList<MatrixContent> cached = isCached(refTagContent);
        if (cached != null && cached.isEmpty()) {
            return allMatches;
        }
        

        TagElement[] tagComponents = new TagElement[maxSequencePosition];
        for (int i = maxSequencePosition - 1, j = 0; i >= 0; --i, ++j) {
            String sequenceReversed = (new StringBuilder(refTagContent[i].sequence).reverse()).toString();
            tagComponents[j] = new TagElement(refTagContent[i].isMass, sequenceReversed, refTagContent[i].mass, refTagContent[i].xNumLimit);
        }

        TagElement[] tagComponentsReverse = new TagElement[tagElements.length - maxSequencePosition];
        for (int i = maxSequencePosition, j = 0; i < refTagContent.length; ++i, ++j) {
            tagComponentsReverse[j] = refTagContent[i];
        }
        

        TagElement[] combinations = createPeptideCombinations(tagComponents, sequenceMatchingPreferences);
        TagElement[] combinationsReversed = createPeptideCombinations(tagComponentsReverse, sequenceMatchingPreferences);
        int lenCombinations = combinations.length;


        LinkedList<MatrixContent> matrixReversed = new LinkedList<MatrixContent>();
        ArrayList<MatrixContent> matrixReversedFinished = new ArrayList<MatrixContent>();
        LinkedList<MatrixContent> matrix = new LinkedList<MatrixContent>();
        ArrayList<MatrixContent> matrixFinished = new ArrayList<MatrixContent>();
        ArrayList<MatrixContent> cachePrimary = new ArrayList<MatrixContent>();
        
        if (cached != null){
            for (MatrixContent matrixContent : cached){
                matrix.add(matrixContent);
            }
        }
        else {
            // left index, right index, current character, previous matrix content, mass, peptideSequence, peptide length, number of X
            matrixReversed.add(new MatrixContent(0, indexStringLength - 1, '\0', null, 0, null, 0, 0, -1, null));
        }
        
        if (cached == null) {
            // Map Reverse
            mappingSequenceAndMasses(combinationsReversed, matrixReversed, matrixReversedFinished, lessReversed, occurrenceReversed, massTolerance);
            
            // Traceback Reverse
            for (MatrixContent content : matrixReversedFinished) {
                MatrixContent currentContent = content;
                String currentPeptide = "";

                int leftIndexFront = 0;
                int rightIndexFront = indexStringLength - 1;
                ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();

                int currentModPosition = 1;
                while (currentContent.previousContent != null) {
                    int currentChar = currentContent.character;
                    currentPeptide += currentContent.character;
                    leftIndexFront = lessPrimary[currentChar] + occurrencePrimary.getRank(leftIndexFront - 1, currentChar);
                    rightIndexFront = lessPrimary[currentChar] + occurrencePrimary.getRank(rightIndexFront, currentChar) - 1;
                    
                    if (currentContent.modificationNum > 0 && modifictationLabels != null && modifictationLabels[currentContent.modificationNum] != null){
                        modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationNum], (currentContent.modificationNum >= 128), currentModPosition));
                    }
                    ++currentModPosition;
                    currentContent = currentContent.previousContent;
                }
                for (ModificationMatch modificationMatch : modifications){
                    modificationMatch.setModificationSite(currentPeptide.length() - modificationMatch.getModificationSite() + 1);
                }
                cachePrimary.add(new MatrixContent(leftIndexFront, rightIndexFront, '\0', null, 0, (new StringBuilder(currentPeptide).reverse()).toString(), 0, 0, -1, modifications));
            }
            
            List insertMatrix = (lenCombinations > 0) ? matrix : matrixFinished;
            for (MatrixContent matrixContent : cachePrimary) insertMatrix.add(matrixContent);
            
            cacheIt(refTagContent, cachePrimary);
        }

        // Map Front
        mappingSequenceAndMasses(combinations, matrix, matrixFinished, lessPrimary, occurrencePrimary, massTolerance);

        // Traceback Front
        for (MatrixContent content : matrixFinished) {
            MatrixContent currentContent = content;
            String currentPeptide = "";
            ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();

            int currentModPosition = 1;
            while (currentContent.previousContent != null) {
                currentPeptide += currentContent.character;
                    
                if (currentContent.modificationNum >= 0 && modifictationLabels != null && modifictationLabels[currentContent.modificationNum] != null){
                    modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationNum], (currentContent.modificationNum >= 128), currentModPosition));
                }
                
                ++currentModPosition;
                currentContent = currentContent.previousContent;
            }

            int leftIndex = content.left;
            int rightIndex = content.right;
            for (ModificationMatch modificationMatch : currentContent.modifications){
                modifications.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), currentPeptide.length() + modificationMatch.getModificationSite()));
            }
            String peptide = currentPeptide + currentContent.peptideSequence;

            if (turned) {
                leftIndex = 0;
                rightIndex = indexStringLength - 1;
                for (char aminoAcid : peptide.toCharArray()) {
                    leftIndex = lessReversed[aminoAcid] + occurrenceReversed.getRank(leftIndex - 1, aminoAcid);
                    rightIndex = lessReversed[aminoAcid] + occurrenceReversed.getRank(rightIndex, aminoAcid) - 1;
                }
                for (ModificationMatch modificationMatch : modifications){
                    modificationMatch.setModificationSite(peptide.length() - modificationMatch.getModificationSite() + 1);
                }
                peptide = (new StringBuilder(peptide).reverse()).toString();
            }
            
            HashMap<String, ArrayList<Integer>> matches = new HashMap<String, ArrayList<Integer>>();
            for (int j = leftIndex; j <= rightIndex; ++j) {
                int pos = getTextPosition(j);
                int index = binarySearch(boundaries, pos);
                String accession = accessions[index];

                if (!matches.containsKey(accession)) matches.put(accession, new ArrayList<Integer>());
                matches.get(accession).add(pos - boundaries[index]);
            }
            allMatches.put(new Peptide(peptide, modifications), matches);
                

        }
                
        return allMatches;
    }

    /**
     * Simplyfied class for tag elements
     */
    private class TagElement {

        boolean isMass;
        String sequence;
        double mass;
        int xNumLimit;

        /**
         * Constructor 
         * @param isMass
         * @param sequence
         * @param mass
         * @param xNumLimit 
         */
        TagElement(boolean isMass, String sequence, double mass, int xNumLimit) {
            this.isMass = isMass;
            this.sequence = sequence;
            this.mass = mass;
            this.xNumLimit = xNumLimit;
        }
    }

    /**
     * Class for caching intermediate tag to proteome mapping results
     */
    private class CacheElement {

        Double massFirst;
        String sequence;
        Double massSecond;
        ArrayList<MatrixContent> cachedPrimary;

        /**
         * Constructor
         * @param massFirst
         * @param sequence
         * @param massSecond
         * @param cachedPrimary 
         */
        public CacheElement(Double massFirst, String sequence, Double massSecond, ArrayList<MatrixContent> cachedPrimary) {
            this.sequence = sequence;
            this.massFirst = massFirst;
            this.massSecond = massSecond;
            this.cachedPrimary = cachedPrimary;
        }
    }

    /**
     * List of cached intermediate tag to proteome mapping results
     */
    private final LinkedList<CacheElement> cache = new LinkedList<CacheElement>();

    /**
     * Adding intermediate tag to proteome mapping results into the cache
     * @param tagComponents
     * @return 
     */
    private synchronized ArrayList<MatrixContent> isCached(TagElement[] tagComponents) {
        if (tagComponents.length != 3 || !tagComponents[0].isMass || tagComponents[1].isMass || !tagComponents[2].isMass) {
            return null;
        }
        ArrayList<MatrixContent> cached = null;
        
        ListIterator<CacheElement> listIterator = cache.listIterator();
        while (listIterator.hasNext()) {
            CacheElement cacheElement = listIterator.next();
            if (cacheElement.sequence.compareTo(tagComponents[1].sequence) == 0) {
                if (Math.abs(cacheElement.massSecond - tagComponents[2].mass) < 1e-5) {
                    cached = new ArrayList<MatrixContent>();
                    for (MatrixContent matrixContent : cacheElement.cachedPrimary) {
                        cached.add(new MatrixContent(matrixContent));
                    }
                    break;
                }
            }
        }
        return cached;
    }

    /**
     * caching intermediate results of previous tag to proteome matches
     * @param tagComponents
     * @param cachedPrimary 
     */
    private synchronized void cacheIt(TagElement[] tagComponents, ArrayList<MatrixContent> cachedPrimary) {
        if (tagComponents.length != 3 || !tagComponents[0].isMass || tagComponents[1].isMass || !tagComponents[2].isMass) {
            return;
        }

        ArrayList<MatrixContent> cacheContentPrimary = new ArrayList<MatrixContent>();
        for (MatrixContent matrixContent : cachedPrimary) {
            cacheContentPrimary.add(new MatrixContent(matrixContent));
        }
        CacheElement cacheElement = new CacheElement(tagComponents[0].mass, tagComponents[1].sequence, tagComponents[2].mass, cacheContentPrimary);
        cache.addFirst(cacheElement);
        if (cache.size() > 50) {
            cache.removeLast();
        }
    }
}
