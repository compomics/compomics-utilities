package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.variants.amino_acids.*;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.identification.amino_acid_tags.MassGap;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import com.compomics.util.experiment.biology.variants.Variant;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideVariantMatches;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.io.biology.protein.ProteinIterator;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;
import com.compomics.util.parameters.identification.PeptideVariantsParameters;
import com.compomics.util.parameters.identification.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import org.jsuffixarrays.*;
import java.util.concurrent.Semaphore;

/**
 * The FM index.
 *
 * @author Dominik Kopczynski
 * @author Marc Vaudel
 */
public class FMIndex implements PeptideMapper, SequenceProvider {

    /**
     * Semaphore for caching.
     */
    static Semaphore cacheMutex = new Semaphore(1);
    /**
     * Number of chunks of complete index.
     */
    private int indexParts = 0;
    /**
     * Byte size of index chuck.
     */
    private final int indexChunkSize = 100 * 1024 * 1024;
    /**
     * Sampled suffix array.
     */
    private final ArrayList<int[]> suffixArraysPrimary = new ArrayList<>();
    /**
     * Wavelet tree for storing the burrows wheeler transform.
     */
    public ArrayList<WaveletTree> occurrenceTablesPrimary = new ArrayList<>();
    /**
     * Wavelet tree for storing the burrows wheeler transform reversed.
     */
    public ArrayList<WaveletTree> occurrenceTablesReversed = new ArrayList<>();
    /**
     * Less table for doing an update step according to the LF step.
     */
    public ArrayList<int[]> lessTablesPrimary = new ArrayList<>();
    /**
     * Less table for doing an update step according to the LF step reversed.
     */
    public ArrayList<int[]> lessTablesReversed = new ArrayList<>();
    /**
     * Length of the indexed string (all concatenated protein sequences).
     */
    public ArrayList<Integer> indexStringLengths = new ArrayList<>();
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
    private final ArrayList<int[]> boundaries = new ArrayList<>();
    /**
     * List of all accession IDs in the FASTA file.
     */
    private final ArrayList<String[]> accessions = new ArrayList<>();
    /**
     * List of all amino acid masses.
     */
    private double[] aaMasses = null;
    /**
     * List of all indexes for valid amino acid masses
     */
    private int[] aaMassIndexes = null;
    /**
     * List of all indexes for valid amino acid masses
     */
    private int numMasses = 0;
    /**
     * List of all amino acid masses.
     */
    private String[] modifictationLabels = null;
    /**
     * List of all amino acid masses.
     */
    private boolean[] modificationFlags = null;
    /**
     * If true, variable modifications are included.
     */
    private boolean withVariableModifications = false;
    /**
     * Characters that can be substituted by B
     */
    private int[] BSubstitutions = new int[]{'D', 'N'};
    /**
     * Characters that can be substituted by J
     */
    private int[] JSubstitutions = new int[]{'I', 'L'};
    /**
     * Characters that can be substituted by Z
     */
    private int[] ZSubstitutions = new int[]{'E', 'Q'};

    private ArrayList<String> fmodc = null; // @TODO: add JavaDoc
    private ArrayList<Double> fmodcMass = null;

    private ArrayList<String>[] fmodcaa = null;
    private ArrayList<Double>[] fmodcaaMass = null;

    private ArrayList<String> fmodn = null;
    private ArrayList<Double> fmodnMass = null;

    private ArrayList<String>[] fmodnaa = null;
    private ArrayList<Double>[] fmodnaaMass = null;

    private ArrayList<String> fmodcp = null;
    private ArrayList<Double> fmodcpMass = null;

    private ArrayList<String>[] fmodcpaa = null;
    private ArrayList<Double>[] fmodcpaaMass = null;

    private ArrayList<String> fmodnp = null;
    private ArrayList<Double> fmodnpMass = null;

    private ArrayList<String>[] fmodnpaa = null;
    private ArrayList<Double>[] fmodnpaaMass = null;

    private ArrayList<String> vmodc = null;
    private ArrayList<Double> vmodcMass = null;

    private ArrayList<String>[] vmodcaa = null;
    private ArrayList<Double>[] vmodcaaMass = null;

    private ArrayList<String> vmodn = null;
    private ArrayList<Double> vmodnMass = null;

    private ArrayList<String>[] vmodnaa = null;
    private ArrayList<Double>[] vmodnaaMass = null;

    private ArrayList<String> vmodcp = null;
    private ArrayList<Double> vmodcpMass = null;

    private ArrayList<String>[] vmodcpaa = null;
    private ArrayList<Double>[] vmodcpaaMass = null;

    private ArrayList<String> vmodnp = null;
    private ArrayList<Double> vmodnpMass = null;

    private ArrayList<String>[] vmodnpaa = null;
    private ArrayList<Double>[] vmodnpaaMass = null;

    private boolean hasCTermDirectionPTM = false;
    private boolean hasNTermDirectionPTM = false;

    private boolean hasPTMatTerminus = false;

    private boolean hasFixedPTM_CatTerminus = false;
    private boolean hasFixedPTM_NatTerminus = false;

    double negativePTMMass = 0;

    /**
     * Either search with one maximal number of variants or use an upper limit
     * for every variant (insertion / deletion / substitution)
     */
    boolean genericVariantMatching = true;
    /**
     * Number of allowed variant operations.
     */
    int maxNumberVariants = 0;
    /**
     * Number of allowed insertion operations.
     */
    int maxNumberInsertions = 0;
    /**
     * Number of allowed deletion operations.
     */
    int maxNumberDeletions = 0;
    /**
     * Number of allowed substitution operations.
     */
    int maxNumberSubstitutions = 0;
    /**
     * Allowed substitutions.
     */
    boolean[][] substitutionMatrix = null;
    /**
     * Precision for the masses in lookup table.
     */
    double lookupMultiplier = 10000;
    /**
     * The mass accuracy type.
     */
    SearchParameters.MassAccuracyType massAccuracyType = SearchParameters.MassAccuracyType.DA;
    /**
     * Lookup tolerance mass.
     */
    double massTolerance = 0.02;
    /**
     * Maximum mass for lookup table [Da].
     */
    double lookupMaxMass = 800;
    /**
     * Mass lookup table.
     */
    long[] lookupMasses = null;
    /**
     * maximum supported number of Xs per tag.
     */
    int maxXPerTag = 4;
    /**
     * Lookup table for up to maxXPerTag Xs per mass tag.
     */
    long[][] Xlookup = null;
    /**
     * List for patterns of complex PTMs.
     */
    ArrayList<Long[]> PTMPatterns = new ArrayList<>();
    /**
     * Mapping dictionary PTM name -> (PTMPattern index, relative starting
     * position, pattern length).
     */
    HashMap<String, Integer[]> PTMPatternNames = new HashMap<>();
    /**
     * Longest PTM pattern.
     */
    int longestPTMpattern = 0;
    /**
     * All permutations.
     */
    int[][][] allPermutations = {
        {{-1}},
        {{0}},
        {{0, 1}, {1, 0}},
        {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}},
        {{0, 1, 2, 3}, {0, 1, 3, 2}, {0, 2, 1, 3}, {0, 2, 3, 1}, {0, 3, 1, 2},
        {0, 3, 2, 1}, {1, 0, 2, 3}, {1, 0, 3, 2}, {1, 2, 0, 3}, {1, 2, 3, 0},
        {1, 3, 0, 2}, {1, 3, 2, 0}, {2, 0, 1, 3}, {2, 0, 3, 1}, {2, 1, 0, 3},
        {2, 1, 3, 0}, {2, 3, 0, 1}, {2, 3, 1, 0}, {3, 0, 1, 2}, {3, 0, 2, 1},
        {3, 1, 0, 2}, {3, 1, 2, 0}, {3, 2, 0, 1}, {3, 2, 1, 0}}};

    /**
     * struct for building own data structure for mass to index mapping.
     */
    public class MassIndexMap {

        public double mass;
        public int[] indexes;

        public MassIndexMap(double mass, int[] indexes) {
            this.mass = mass;
            this.indexes = indexes;
        }
    }

    /**
     * Arraylist for sorted masses to index mappings.
     */
    ArrayList<MassIndexMap> massIndexMaps = null;

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
            if (array[mid] <= key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        if (mid > 0 && key < array[mid]) {
            mid -= 1;
        }
        return mid;
    }

    /**
     * Compute the mass value.
     *
     * @param currentMass the current mass
     * @param refMass the reference mass
     * @return the mass value
     */
    public double computeMassValue(double currentMass, double refMass) {
        if (massAccuracyType == SearchParameters.MassAccuracyType.DA) {
            return currentMass;
        }
        return Math.abs(currentMass - refMass) / refMass * 1e6;
    }

    /**
     * Compute the inverse mass value.
     *
     * @param currentMass the current mass
     * @param refMass the reference mass
     * @return the inverse mass value
     */
    public double computeInverseMassValue(double currentMass, double refMass) {
        if (massAccuracyType == SearchParameters.MassAccuracyType.DA) {
            return currentMass;
        }
        return currentMass / 1e6 * refMass;
    }

    /**
     * Adds a PTM pattern for bitwise pattern search.
     *
     * @param ptm PTM object
     */
    public void addPTMPattern(Modification ptm) {
        AminoAcidPattern aap = ptm.getPattern();
        HashMap<Integer, ArrayList<Character>> aaTargered = aap.getAaTargeted();
        Set<Integer> keySet = aaTargered.keySet();
        int startPos = Collections.min(keySet);
        int endPos = Collections.max(keySet);
        int patternLength = endPos - startPos + 1;
        if (patternLength > 62) {
            throw new UnsupportedOperationException("Pattern contains more than 64 sites, not supported");
        }
        long preMask = (1 << patternLength) - 1;
        for (int key : keySet) {
            preMask &= ~(1L << (key - startPos));
        }

        Long[] masks = new Long[128];
        for (int i = 0; i < 128; ++i) {
            masks[i] = preMask;
        }
        for (int key : keySet) {
            for (char c : aaTargered.get(key)) {
                masks[c] |= 1L << (key - startPos);
            }
        }

        PTMPatternNames.put(ptm.getName(), new Integer[]{PTMPatterns.size(), startPos, patternLength});
        PTMPatterns.add(masks);
        longestPTMpattern = Math.max(longestPTMpattern, patternLength);
    }

    /**
     * Checking if peptide-protein should be discarded due to pattern PTM
     * conflict.
     *
     * @param peptideProteinMapping the peptide protein mapping
     * @return either yes or no
     */
    public boolean checkPTMPattern(PeptideProteinMapping peptideProteinMapping) {
        if (PTMPatterns.isEmpty()) {
            return true;
        }

        // prepare text for pattern search
        String searchText = peptideProteinMapping.getPeptideSequence();
        // TODO: extend searchText for length of longest PTM pattern in both directions

        for (ModificationMatch modificationMatch : peptideProteinMapping.getModificationMatches()) {
            if (PTMPatternNames.containsKey(modificationMatch.getModification())) {
                Integer[] PTMPatternData = PTMPatternNames.get(modificationMatch.getModification()); //(PTMPattern index, starting position, pattern length)
                Long[] masks = PTMPatterns.get(PTMPatternData[0]);
                int textPos = modificationMatch.getModificationSite() - 1;
                if (textPos + PTMPatternData[1] < 0) {
                    return false; // TODO: handle this
                }
                if (textPos + PTMPatternData[1] + PTMPatternData[2] > searchText.length()) {
                    return false; // TODO: handle this
                }
                // use shift-and algorithm for pattern search
                long pattern = 1L;
                for (int i = textPos + PTMPatternData[1], j = 0; j < PTMPatternData[2] && pattern != 0; ++i, ++j) {
                    pattern = (pattern & masks[searchText.charAt(i)]) << 1;
                }
                return ((1L << PTMPatternData[2]) & pattern) > 0L;
            }

        }

        return true;
    }

    /**
     * Compute mapping ranges.
     *
     * @param mass the mass
     * @return the mapping ranges
     */
    public int[] computeMappingRanges(double mass) {
        int[] ranges = new int[]{0, -1};

        int low = 0;
        int mid = 0;
        int high = massIndexMaps.size() - 1;
        while (low <= high) {
            mid = (low + high) >> 1;
            if (massIndexMaps.get(mid).mass <= mass - computeInverseMassValue(massTolerance, massIndexMaps.get(mid).mass)) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        ranges[0] = Math.max(mid, 0);
        while (ranges[0] < massIndexMaps.size() - 1 && massIndexMaps.get(ranges[0]).mass < mass - computeInverseMassValue(massTolerance, massIndexMaps.get(ranges[0]).mass)) {
            ++ranges[0];
        }
        if (massAccuracyType == SearchParameters.MassAccuracyType.DA && Math.abs(massIndexMaps.get(ranges[0]).mass - mass) > massTolerance) {
            return ranges;
        }
        if (massAccuracyType == SearchParameters.MassAccuracyType.PPM && computeMassValue(mass, massIndexMaps.get(ranges[0]).mass) > massTolerance) {
            return ranges;
        }

        low = ranges[0];
        high = massIndexMaps.size() - 1;
        while (low <= high) {
            mid = (low + high) >> 1;
            if (massIndexMaps.get(mid).mass < mass + computeInverseMassValue(massTolerance, massIndexMaps.get(mid).mass)) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        ranges[1] = Math.min(mid, massIndexMaps.size());
        while (0 < ranges[1] && massIndexMaps.get(ranges[1]).mass > mass + computeInverseMassValue(massTolerance, massIndexMaps.get(ranges[1]).mass)) {
            --ranges[1];
        }

        return ranges;
    }

    /**
     * Computes the number of allocated bytes.
     *
     * @return allocated bytes
     */
    public long getAllocatedBytes() {
        long bytes = 0;
        for (int indexPart = 0; indexPart < indexParts; ++indexPart) {
            bytes += occurrenceTablesPrimary.get(indexPart).getAllocatedBytes() + occurrenceTablesReversed.get(indexPart).getAllocatedBytes() + suffixArraysPrimary.get(indexPart).length * 4;
        }
        return bytes;
    }

    /**
     * Constructor. If PTM settings are provided the index will contain
     * modification information, ignored if null.
     *
     * @param fastaFile the fasta file to index
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param peptideVariantsPreferences contains all parameters for variants
     * @param searchParameters the search parameters
     *
     * @throws IOException exception thrown if an error occurs while iterating
     * the fasta file.
     */
    public FMIndex(File fastaFile, WaitingHandler waitingHandler, boolean displayProgress, PeptideVariantsParameters peptideVariantsPreferences, SearchParameters searchParameters) throws IOException {
        massTolerance = searchParameters.getFragmentIonAccuracy();
        massAccuracyType = searchParameters.getFragmentAccuracyType();
        init(fastaFile, waitingHandler, displayProgress, searchParameters.getPtmSettings(), peptideVariantsPreferences);
    }

    /**
     * Constructor. If PTM settings are provided the index will contain
     * modification information, ignored if null.
     *
     * @param fastaFile the fasta file to index
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param ptmSettings contains modification parameters for identification
     * @param peptideVariantsPreferences contains all parameters for variants
     *
     * @throws IOException exception thrown if an error occurs while iterating
     * the fasta file.
     */
    public FMIndex(File fastaFile, WaitingHandler waitingHandler, boolean displayProgress, PtmSettings ptmSettings, PeptideVariantsParameters peptideVariantsPreferences) throws IOException {
        init(fastaFile, waitingHandler, displayProgress, ptmSettings, peptideVariantsPreferences);
    }

    /**
     * Init function only called by the constructors. If PTM settings are
     * provided the index will contain modification information, ignored if
     * null.
     *
     * @param fastaFile the fasta file to index
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param ptmSettings contains modification parameters for identification
     * @param peptideVariantsPreferences contains all parameters for variants
     * @param massTolerance the mass tolerance
     *
     * @throws IOException exception thrown if an error occurs while iterating
     * the fasta file.
     */
    private void init(File fastaFile, WaitingHandler waitingHandler, boolean displayProgress, PtmSettings ptmSettings, PeptideVariantsParameters peptideVariantsPreferences) throws IOException {

        // load all variant preferences
        maxNumberVariants = peptideVariantsPreferences.getnVariants();
        genericVariantMatching = !peptideVariantsPreferences.getUseSpecificCount();
        maxNumberInsertions = peptideVariantsPreferences.getnAaInsertions();
        maxNumberDeletions = peptideVariantsPreferences.getnAaDeletions();
        maxNumberSubstitutions = peptideVariantsPreferences.getnAaSubstitutions();

        TreeSet<Character> aaGroups = new TreeSet<>();
        aaGroups.add('B');
        aaGroups.add('J');
        aaGroups.add('X');
        aaGroups.add('Z');

        substitutionMatrix = new boolean[128][128];
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                substitutionMatrix[i][j] = false;
            }
        }
        AaSubstitutionMatrix aaSubstitutionMatrix = peptideVariantsPreferences.getAaSubstitutionMatrix();
        for (int aa = 'A'; aa <= 'Z'; ++aa) {
            if (!aaSubstitutionMatrix.getOriginalAminoAcids().contains((char) aa)) {
                continue;
            }
            HashSet<Character> substitutions = aaSubstitutionMatrix.getSubstitutionAminoAcids((char) aa);
            if (substitutions.isEmpty()) {
                continue;
            }
            Iterator<Character> substitutionAAIterator = substitutions.iterator();
            while (substitutionAAIterator.hasNext()) {
                int subAA = substitutionAAIterator.next();
                substitutionMatrix[aa][subAA] = true;
            }
        }

        // load all ptm preferences
        if (ptmSettings != null) {
            // create masses table and modifications
            int[] modificationCounts = new int[128];
            for (int i = 0; i < modificationCounts.length; ++i) {
                modificationCounts[i] = 0;
            }
            ArrayList<String> variableModifications = ptmSettings.getVariableModifications();
            ArrayList<String> fixedModifications = ptmSettings.getFixedModifications();
            ModificationFactory ptmFactory = ModificationFactory.getInstance();

            int hasVariableModification = 0;

            // check which amino acids have variable modificatitions
            for (String modification : variableModifications) {
                Modification ptm = ptmFactory.getModification(modification);
                ArrayList<Character> targets;
                switch (ptm.getModificationType()) {
                    case modaa:
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            modificationCounts[c]++;
                            hasVariableModification = Math.max(hasVariableModification, modificationCounts[c]);
                        }
                        withVariableModifications = true;
                        break;

                    case modc_protein:
                        if (vmodc == null) {
                            vmodc = new ArrayList<>();
                            vmodcMass = new ArrayList<>();
                            hasCTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                        }
                        vmodc.add(modification);
                        vmodcMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modcaa_protein:
                        if (vmodcaa == null) {
                            vmodcaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodcaa[i] = new ArrayList<>();
                            }
                            vmodcaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodcaaMass[i] = new ArrayList<>();
                            }
                            hasCTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            vmodcaa[c].add(modification);
                            vmodcaaMass[c].add(ptm.getMass());
                        }
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modc_peptide:
                        if (vmodcp == null) {
                            vmodcp = new ArrayList<>();
                            vmodcpMass = new ArrayList<>();
                            hasCTermDirectionPTM = true;
                        }
                        vmodcp.add(modification);
                        vmodcpMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modcaa_peptide:
                        if (vmodcpaa == null) {
                            vmodcpaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodcpaa[i] = new ArrayList<>();
                            }
                            vmodcpaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodcpaaMass[i] = new ArrayList<>();
                            }
                            hasCTermDirectionPTM = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            vmodcpaa[c].add(modification);
                            vmodcpaaMass[c].add(ptm.getMass());
                        }
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modn_protein:
                        if (vmodn == null) {
                            vmodn = new ArrayList<>();
                            vmodnMass = new ArrayList<>();
                            hasNTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                        }
                        vmodn.add(modification);
                        vmodnMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modnaa_protein:
                        if (vmodnaa == null) {
                            vmodnaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodnaa[i] = new ArrayList<>();
                            }
                            vmodnaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodnaaMass[i] = new ArrayList<>();
                            }
                            hasNTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            vmodnaa[c].add(modification);
                            vmodnaaMass[c].add(ptm.getMass());
                        }
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modn_peptide:
                        if (vmodnp == null) {
                            vmodnp = new ArrayList<>();
                            vmodnpMass = new ArrayList<>();
                            hasNTermDirectionPTM = true;
                        }
                        vmodnp.add(modification);
                        vmodnpMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modnaa_peptide:
                        if (vmodnpaa == null) {
                            vmodnpaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodnpaa[i] = new ArrayList<>();
                            }
                            vmodnpaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodnpaaMass[i] = new ArrayList<>();
                            }
                            hasNTermDirectionPTM = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            vmodnpaa[c].add(modification);
                            vmodnpaaMass[c].add(ptm.getMass());
                        }
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;
                }
            }

            // create masses for all amino acids including modifications
            aaMasses = new double[128 * (1 + hasVariableModification)];
            modifictationLabels = new String[128 * (1 + hasVariableModification)];
            modificationFlags = new boolean[128 * (1 + hasVariableModification)];
            for (int i = 0; i < aaMasses.length; ++i) {
                aaMasses[i] = -1;
                modifictationLabels[i] = null;
                modificationFlags[i] = false;
            }
            char[] aminoAcids = AminoAcid.getAminoAcids();
            for (int i = 0; i < aminoAcids.length; ++i) {
                if (!aaGroups.contains(aminoAcids[i])) {
                    aaMasses[aminoAcids[i]] = AminoAcid.getAminoAcid(aminoAcids[i]).getMonoisotopicMass();
                }
            }

            // change masses for fixed modifications
            for (String modification : fixedModifications) {
                Modification ptm = ptmFactory.getModification(modification);
                ArrayList<Character> targets;
                switch (ptm.getModificationType()) {
                    case modaa:
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            aaMasses[c] += ptm.getMass();
                            negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                            modifictationLabels[c] = modification;
                            modificationFlags[c] = true;
                        }
                        break;

                    case modc_protein:
                        if (fmodc == null) {
                            fmodc = new ArrayList<>();
                            fmodcMass = new ArrayList<>();
                            hasCTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                            hasFixedPTM_CatTerminus = true;
                        }
                        fmodc.add(modification);
                        fmodcMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modcaa_protein:
                        if (fmodcaa == null) {
                            fmodcaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodcaa[i] = new ArrayList<>();
                            }
                            fmodcaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodcaaMass[i] = new ArrayList<>();
                            }
                            hasCTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                            hasFixedPTM_CatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            fmodcaa[c].add(modification);
                            fmodcaaMass[c].add(ptm.getMass());
                        }
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modc_peptide:
                        if (fmodcp == null) {
                            fmodcp = new ArrayList<>();
                            fmodcpMass = new ArrayList<>();
                            hasCTermDirectionPTM = true;
                        }
                        fmodcp.add(modification);
                        fmodcpMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modcaa_peptide:
                        if (fmodcpaa == null) {
                            fmodcpaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodcpaa[i] = new ArrayList<>();
                            }
                            fmodcpaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodcpaaMass[i] = new ArrayList<>();
                            }
                            hasCTermDirectionPTM = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            fmodcpaa[c].add(modification);
                            fmodcpaaMass[c].add(ptm.getMass());
                        }
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modn_protein:
                        if (fmodn == null) {
                            fmodn = new ArrayList<>();
                            fmodnMass = new ArrayList<>();
                            hasNTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                            hasFixedPTM_NatTerminus = true;
                        }
                        fmodn.add(modification);
                        fmodnMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modnaa_protein:
                        if (fmodnaa == null) {
                            fmodnaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodnaa[i] = new ArrayList<>();
                            }
                            fmodnaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodnaaMass[i] = new ArrayList<>();
                            }
                            hasNTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                            hasFixedPTM_NatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            fmodnaa[c].add(modification);
                            fmodnaaMass[c].add(ptm.getMass());
                        }
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modn_peptide:
                        if (fmodnp == null) {
                            fmodnp = new ArrayList<>();
                            fmodnpMass = new ArrayList<>();
                            hasNTermDirectionPTM = true;
                        }
                        fmodnp.add(modification);
                        fmodnpMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case modnaa_peptide:
                        if (fmodnpaa == null) {
                            fmodnpaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodnpaa[i] = new ArrayList<>();
                            }
                            fmodnpaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodnpaaMass[i] = new ArrayList<>();
                            }
                            hasNTermDirectionPTM = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addPTMPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            fmodnpaa[c].add(modification);
                            fmodnpaaMass[c].add(ptm.getMass());
                        }
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;
                }

            }

            // add masses for variable modifications
            for (int i = 0; i < modificationCounts.length; ++i) {
                modificationCounts[i] = 0;
            }
            for (String modification : variableModifications) {
                Modification ptm = ptmFactory.getModification(modification);
                if (ptm.getModificationType() == ModificationType.modaa) {
                    ArrayList<Character> targets = ptm.getPattern().getAminoAcidsAtTarget();
                    for (Character c : targets) {
                        aaMasses[128 * (1 + modificationCounts[c]) + c] = aaMasses[c] + ptm.getMass();
                        modifictationLabels[128 * (1 + modificationCounts[c]) + c] = modification;
                        modificationFlags[128 * (1 + modificationCounts[c]) + c] = true;
                        modificationCounts[c]++;
                    }
                }
            }

        } else {
            // create masses for all amino acids
            aaMasses = new double[128];
            for (int i = 0; i < aaMasses.length; ++i) {
                aaMasses[i] = -1;
            }
            char[] aminoAcids = AminoAcid.getAminoAcids();
            for (int i = 0; i < aminoAcids.length; ++i) {
                if (!aaGroups.contains(aminoAcids[i])) {
                    aaMasses[aminoAcids[i]] = AminoAcid.getAminoAcid(aminoAcids[i]).getMonoisotopicMass();
                }
            }
        }

        ArrayList<Integer> aaMassVector = new ArrayList<>();
        for (int i = 0; i < aaMasses.length; ++i) {
            if (aaMasses[i] > 0) {
                aaMassVector.add(i);
            }
        }

        aaMassIndexes = new int[aaMassVector.size()];
        for (int i = 0; i < aaMassVector.size(); ++i) {
            aaMassIndexes[i] = aaMassVector.get(i);
        }
        numMasses = aaMassVector.size() + 1; // +1 because of X

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

        // reading all proteins in a first pass to get information about number and total length
        ArrayList<Integer> tmpLengths = new ArrayList<>();
        ArrayList<Integer> tmpProteins = new ArrayList<>();
        long ticker = indexChunkSize;

        int indexStringLength = 1;
        int numProteins = 0;
        ProteinIterator pi = new FastaIterator(fastaFile);
        Protein protein;
        while ((protein = pi.getNextProtein()) != null) {
            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                return;
            }
            int proteinLen = protein.getLength();
            indexStringLength += proteinLen;
            ++numProteins;
            if (indexStringLength > ticker) {
                tmpLengths.add(indexStringLength);
                tmpProteins.add(numProteins);
                indexStringLength = 1;
                numProteins = 0;
            }
        }
        tmpLengths.add(indexStringLength);
        tmpProteins.add(numProteins);

        int maxProgressBar = 10 * tmpLengths.size();

        if (waitingHandler != null && displayProgress && !waitingHandler.isRunCanceled()) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(maxProgressBar);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        pi = pi = new FastaIterator(fastaFile);
        for (int i = 0; i < tmpLengths.size(); ++i) {
            addDataToIndex(pi, tmpLengths.get(i), tmpProteins.get(i), alphabet, waitingHandler, displayProgress);
        }

        int lookupLength = ((int) ((lookupMaxMass + computeInverseMassValue(massTolerance, lookupMaxMass)) * lookupMultiplier));
        lookupMasses = new long[(lookupLength >>> 6) + 3];
        for (int i = 0; i < lookupMasses.length; ++i) {
            lookupMasses[i] = 0L;
        }

        Xlookup = new long[maxXPerTag + 1][];
        for (int i = 1; i <= maxXPerTag; ++i) {
            Xlookup[i] = new long[(lookupLength >>> 6) + 3];
            for (int j = 0; j < Xlookup[i].length; ++j) {
                Xlookup[i][j] = 0L;
            }
        }
        massIndexMaps = new ArrayList<>(1000000);

        recursiveMassFilling(0., 0, 0, null);

        Collections.sort(massIndexMaps, new Comparator<MassIndexMap>() {
            public int compare(MassIndexMap m1, MassIndexMap m2) {
                return (int) ((m1.mass - m2.mass) * 1000000.);
            }
        });
        cache = (HashMap<String, CacheElement>[]) new HashMap[indexParts];
        for (int indexPart = 0; indexPart < indexParts; ++indexPart) {
            cache[indexPart] = new HashMap<>();
        }
    }

    /**
     * Add data to index
     *
     * @param pi the protein iterator
     * @param indexStringLength the index string length
     * @param numProteins the number of proteins
     * @param alphabet the alphabet
     * @param waitingHandler the waiting handler
     * @param displayProgress if progress is to be displayed
     *
     * @throws IOException exception thrown if an error occurs while iterating
     * the fasta file.
     */
    void addDataToIndex(ProteinIterator pi, int indexStringLength, int numProteins, long[] alphabet, WaitingHandler waitingHandler, boolean displayProgress) throws IOException {
        indexParts += 1;
        indexStringLength += numProteins + 1; // delimiters between protein sequences + sentinal
        indexStringLengths.add(indexStringLength);

        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }
        byte[] T = new byte[indexStringLength];
        T[0] = '/';                     // adding delimiter at beginning
        T[indexStringLength - 2] = '/'; // adding delimiter at ending
        T[indexStringLength - 1] = '$'; // adding the sentinal

        int[] bndaries = new int[numProteins + 1];
        boundaries.add(bndaries);
        String[] accssions = new String[numProteins];
        accessions.add(accssions);
        boundaries.get(0)[0] = 1;

        // reading proteins in a second pass to store their amino acid sequences and their accession numbers
        int tmpN = 0;
        int tmpNumProtein = 0;

        for (int i = 0; i < numProteins; ++i) {
            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                return;
            }
            Protein currentProtein = pi.getNextProtein();
            if (currentProtein == null) {
                throw new IllegalArgumentException("More sequences from database requested than contained.");
            }
            int proteinLen = currentProtein.getLength();
            T[tmpN++] = '/'; // adding the delimiters
            System.arraycopy(currentProtein.getSequence().toUpperCase().getBytes(), 0, T, tmpN, proteinLen);
            tmpN += proteinLen;
            accssions[tmpNumProtein++] = currentProtein.getAccession();
            bndaries[tmpNumProtein] = tmpN + 1;

        }

        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        int[] T_int = new int[indexStringLength];
        for (int i = 0; i < indexStringLength; ++i) {
            T_int[i] = T[i];
        }
        int[] suffixArrayPrimary = (new DivSufSort()).buildSuffixArray(T_int, 0, indexStringLength);

        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }
        T_int = null;

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
        suffixArraysPrimary.add(sampledSuffixArray);
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        // creating the occurrence table and less table for backward search over forward text
        WaveletTree occurrenceTablePrimary = new WaveletTree(bwt, alphabet, waitingHandler, numMasses, hasPTMatTerminus);
        int[] lessTablePrimary = occurrenceTablePrimary.createLessTable();
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        bwt = null;
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
        T_int = new int[indexStringLength];
        for (int i = 0; i < indexStringLength; ++i) {
            T_int[i] = TReversed[i];
        }
        int[] suffixArrayReversed = (new DivSufSort()).buildSuffixArray(T_int, 0, indexStringLength);
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
        WaveletTree occurrenceTableReversed = new WaveletTree(bwt, alphabet, waitingHandler, numMasses, hasPTMatTerminus);
        int[] lessTableReversed = occurrenceTableReversed.createLessTable();
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }

        occurrenceTablesPrimary.add(occurrenceTablePrimary);
        occurrenceTablesReversed.add(occurrenceTableReversed);
        lessTablesPrimary.add(lessTablePrimary);
        lessTablesReversed.add(lessTableReversed);
    }

    /**
     * Recursive function to compute all possible mass combinations up to a
     * given maximum limit
     *
     * @param mass current mass
     * @param pos current index of amino acid mass array
     */
    void recursiveMassFilling(double mass, int pos, int loop, int[] massIndexes) {
        if (mass >= lookupMaxMass) {
            return;
        }
        double transformedMass = computeInverseMassValue(massTolerance, mass);
        if (mass > transformedMass) {
            int startMass = (int) ((mass - transformedMass) * lookupMultiplier);
            int endMass = (int) ((mass + transformedMass) * lookupMultiplier + 1);

            lookupMasses[startMass >>> 6] |= (~(0L)) << (startMass & 63);
            for (int p = (startMass >>> 6) + 1; p < (endMass >>> 6); ++p) {
                lookupMasses[p] = ~0L;
            }
            lookupMasses[endMass >>> 6] |= (~(0L)) >>> (64 - (endMass & 63));

            if (loop <= maxXPerTag) {
                Xlookup[loop][startMass >>> 6] |= (~(0L)) << (startMass & 63);
                for (int p = (startMass >>> 6) + 1; p < (endMass >>> 6); ++p) {
                    Xlookup[loop][p] = ~0L;
                }
                Xlookup[loop][endMass >>> 6] |= (~(0L)) >>> (64 - (endMass & 63));

                massIndexMaps.add(new MassIndexMap(mass, massIndexes));
            }
        }

        for (int i = pos; i < aaMassIndexes.length; ++i) {
            int[] massIndexesNew = new int[massIndexes != null ? massIndexes.length + 1 : 1];
            if (massIndexes != null) {
                for (int j = 0; j < massIndexes.length; ++j) {
                    massIndexesNew[j] = massIndexes[j];
                }
            }
            massIndexesNew[massIndexesNew.length - 1] = aaMassIndexes[i];
            recursiveMassFilling(mass + aaMasses[aaMassIndexes[i]], i, loop + 1, massIndexesNew);
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
    private ArrayList<String> createPeptideCombinations(String peptide, SequenceMatchingParameters seqMatchPref) {
        ArrayList<String> combinations = new ArrayList<>();

        SequenceMatchingParameters.MatchingType sequenceMatchingType = seqMatchPref.getSequenceMatchingType();
        if (sequenceMatchingType == SequenceMatchingParameters.MatchingType.string) {
            for (int i = 0; i < peptide.length(); ++i) {
                combinations.add(peptide.substring(i, i + 1));
            }
        } else if (sequenceMatchingType == SequenceMatchingParameters.MatchingType.aminoAcid || sequenceMatchingType == SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids) {
            boolean indistinghuishable = sequenceMatchingType == SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids;

            for (int i = 0; i < peptide.length(); ++i) {
                String chars = peptide.substring(i, i + 1);
                char[] aaCombinations = AminoAcid.getAminoAcid(peptide.charAt(i)).getCombinations();
                for (int j = 0; j < aaCombinations.length; ++j) {
                    chars += aaCombinations[j];
                }
                if (peptide.charAt(i) == 'B' || peptide.charAt(i) == 'J' || peptide.charAt(i) == 'Z') {
                    aaCombinations = AminoAcid.getAminoAcid(peptide.charAt(i)).getSubAminoAcids(false);
                    for (int j = 0; j < aaCombinations.length; ++j) {
                        chars += aaCombinations[j];
                    }
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
        return combinations;
    }

    /**
     * Returns a list of all possible amino acids per position in the peptide
     * according to the sequence matching preferences.
     *
     * @param tagComponents the tag components
     * @param seqMatchPref the sequence matching preferences
     * @return a list of all possible amino acids per position in the peptide
     */
    private TagElement[] createPeptideCombinations(TagElement[] tagComponents, SequenceMatchingParameters seqMatchPref) {

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
        SequenceMatchingParameters.MatchingType sequenceMatchingType = seqMatchPref.getSequenceMatchingType();
        if (sequenceMatchingType == SequenceMatchingParameters.MatchingType.string) {
            for (TagElement tagElement : tagComponents) {
                if (tagElement.isMass) {
                    combinations[combinationPosition++] = new TagElement(true, "", tagElement.mass, tagElement.xNumLimit);
                } else {
                    for (int j = 0; j < tagElement.sequence.length(); ++j) {
                        combinations[combinationPosition++] = new TagElement(false, tagElement.sequence.substring(j, j + 1), tagElement.mass, tagElement.xNumLimit);
                    }
                }
            }
        } else if (sequenceMatchingType == SequenceMatchingParameters.MatchingType.aminoAcid || sequenceMatchingType == SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids) {
            boolean indistinghuishable = sequenceMatchingType == SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids;

            for (TagElement tagElement : tagComponents) {
                if (!tagElement.isMass) {
                    String subSequence = tagElement.sequence;
                    for (int s = 0; s < subSequence.length(); ++s) {
                        char amino = subSequence.charAt(s);
                        String chars = String.valueOf(amino);
                        char[] aaCombinations = AminoAcid.getAminoAcid(amino).getCombinations();
                        for (int j = 0; j < aaCombinations.length; ++j) {
                            chars += aaCombinations[j];
                        }
                        if (amino == 'B' || amino == 'J' || amino == 'Z') {
                            aaCombinations = AminoAcid.getAminoAcid(amino).getSubAminoAcids(false);
                            for (int j = 0; j < aaCombinations.length; ++j) {
                                chars += aaCombinations[j];
                            }
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
        return combinations;
    }

    /**
     * Method to get the text position using the sampled suffix array.
     *
     * @param index the position
     * @return the text position
     */
    private int getTextPosition(int index, int indexPart) {
        int[] suffixArrayPrimary = suffixArraysPrimary.get(indexPart);
        int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
        WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
        int indexStringLength = indexStringLengths.get(indexPart);
        int numIterations = 0;
        while (((index & samplingMask) != 0) && (index != 0)) {
            int[] aminoInfo = occurrenceTablePrimary.getCharacterInfo(index);
            index = lessTablePrimary[aminoInfo[0]] + aminoInfo[1];

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
    public ArrayList<PeptideProteinMapping> getProteinMapping(String peptide, SequenceMatchingParameters seqMatchPref) {
        ArrayList<PeptideProteinMapping> peptideProteinMapping = new ArrayList<>();
        if (maxNumberVariants > 0 || maxNumberDeletions > 0 || maxNumberInsertions > 0 || maxNumberSubstitutions > 0) {
            if (genericVariantMatching) {
                for (int i = 0; i < indexParts; ++i) {
                    peptideProteinMapping.addAll(getProteinMappingWithVariantsGeneric(peptide, seqMatchPref, i));
                }
                return peptideProteinMapping;
            } else {
                for (int i = 0; i < indexParts; ++i) {
                    peptideProteinMapping.addAll(getProteinMappingWithVariantsSpecific(peptide, seqMatchPref, i));
                }
                return peptideProteinMapping;
            }
        } else {
            for (int i = 0; i < indexParts; ++i) {
                peptideProteinMapping.addAll(getProteinMappingWithoutVariants(peptide, seqMatchPref, i));
            }
            return peptideProteinMapping;
        }
    }

    /**
     * Exact mapping peptides against the proteome.
     *
     * @param peptide the peptide
     * @param seqMatchPref the sequence matching preferences
     * @param indexPart the index part
     * @return the mapping
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithoutVariants(String peptide, SequenceMatchingParameters seqMatchPref, int indexPart) {
        int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
        WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>();

        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int lenPeptide = peptide.length();
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref);
        int maxX = (int) (((seqMatchPref.getLimitX() != null) ? seqMatchPref.getLimitX() : 1) * lenPeptide);

        ArrayList<MatrixContent>[] backwardList = (ArrayList<MatrixContent>[]) new ArrayList[lenPeptide + 1];

        int countX = 0;
        for (int i = 0; i <= lenPeptide; ++i) {
            backwardList[i] = new ArrayList<>(10);
            if (i < lenPeptide && pep_rev.charAt(i) == 'X') {
                ++countX;
            }
        }

        if (countX <= maxX) {
            backwardList[0].add(new MatrixContent(indexStringLengths.get(indexPart) - 1)); // L, R, char, previous content, num of X
            for (int j = 0; j < lenPeptide; ++j) {
                String combinationSequence = combinations.get(j);
                ArrayList<MatrixContent> cell = backwardList[j];
                for (MatrixContent content : cell) {
                    int leftIndexOld = content.left;
                    int rightIndexOld = content.right;
                    int numX = content.numX;

                    for (int c = 0; c < combinationSequence.length(); ++c) {
                        int aminoAcid = combinationSequence.charAt(c);

                        int lessValue = lessTablePrimary[aminoAcid];
                        int[] range = occurrenceTablePrimary.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcid);
                        final int leftIndex = lessValue + range[0];
                        final int rightIndex = lessValue + range[1] - 1;

                        if (leftIndex <= rightIndex) {
                            int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);
                            if (newNumX > maxX) {
                                continue;
                            }
                            backwardList[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newNumX));
                        }
                    }
                }
            }

            // traceback
            for (MatrixContent content : backwardList[lenPeptide]) {
                MatrixContent currentContent = content;
                String currentPeptide = "";

                while (currentContent.previousContent != null) {
                    currentPeptide += (char) currentContent.character;
                    currentContent = currentContent.previousContent;
                }

                int leftIndex = content.left;
                int rightIndex = content.right;

                for (int j = leftIndex; j <= rightIndex; ++j) {
                    int pos = getTextPosition(j, indexPart);
                    int index = binarySearch(boundaries.get(indexPart), pos);
                    String accession = accessions.get(indexPart)[index];

                    PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, currentPeptide, pos - boundaries.get(indexPart)[index]);
                    allMatches.add(peptideProteinMapping);
                }
            }
        }

        /*
        for (PeptideProteinMapping ppm : allMatches){
            System.out.println(ppm.getPeptideSequence() + " " + ppm.getProteinAccession() + " " + ppm.getIndex());
        }*/
        return allMatches;
    }

    /**
     * Variant tolerant mapping peptides against the proteome.
     *
     * @param peptide the peptide
     * @param seqMatchPref the sequence match preferences
     * @param indexPart the index part
     * @return the mapping
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithVariantsGeneric(String peptide, SequenceMatchingParameters seqMatchPref, int indexPart) {
        int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
        WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>();
        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int lenPeptide = peptide.length();
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref);
        int xNumLimit = (int) (((seqMatchPref.getLimitX() != null) ? seqMatchPref.getLimitX() : 1) * lenPeptide);

        ArrayList<MatrixContent>[][] backwardMatrix = (ArrayList<MatrixContent>[][]) new ArrayList[maxNumberVariants + 1][lenPeptide + 1];

        for (int k = 0; k <= maxNumberVariants; ++k) {
            for (int j = 0; j <= lenPeptide; ++j) {
                backwardMatrix[k][j] = new ArrayList<>(10);
            }
        }
        int countX = 0;
        for (int j = 0; j <= lenPeptide; ++j) {
            if (j < lenPeptide && pep_rev.charAt(j) == 'X') {
                ++countX;
            }
        }

        if (countX <= xNumLimit) {
            backwardMatrix[0][0].add(new MatrixContent(indexStringLengths.get(indexPart) - 1));

            for (int k = 0; k <= maxNumberVariants; ++k) {
                ArrayList<MatrixContent>[] backwardList = backwardMatrix[k];
                for (int j = 0; j < lenPeptide; ++j) {
                    String combinationSequence = combinations.get(j);
                    ArrayList<MatrixContent> cell = backwardList[j];

                    for (int i = 0; i < cell.size(); ++i) {
                        MatrixContent content = cell.get(i);
                        int leftIndexOld = content.left;
                        int rightIndexOld = content.right;
                        int numX = content.numX;
                        int numVariants = content.numVariants;
                        int length = content.length;

                        for (int c = 0; c < combinationSequence.length(); ++c) {
                            int aminoAcid = combinationSequence.charAt(c);

                            int lessValue = lessTablePrimary[aminoAcid];
                            int[] range = occurrenceTablePrimary.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcid);
                            final int leftIndex = lessValue + range[0];
                            final int rightIndex = lessValue + range[1] - 1;
                            int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);

                            if (leftIndex <= rightIndex) {
                                if (newNumX > xNumLimit) {
                                    continue;
                                }

                                // match
                                backwardList[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newNumX, length + 1, numVariants, '-'));
                            }

                            if (numVariants < maxNumberVariants && c == 0) {
                                // insertion
                                if (numVariants < maxNumberVariants) {
                                    backwardMatrix[k + 1][j + 1].add(new MatrixContent(leftIndexOld, rightIndexOld, aminoAcid, content, newNumX, length + 1, numVariants + 1, '*'));
                                }

                                // deletion and substitution
                                int[][] setCharacter = occurrenceTablePrimary.rangeQuery(leftIndexOld - 1, rightIndexOld);
                                for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                                    int[] borders = setCharacter[b];
                                    final int errorAminoAcid = borders[0];
                                    final int errorNewNumX = newNumX + ((errorAminoAcid == 'X') ? 1 : 0);
                                    final int errorLessValue = lessTablePrimary[errorAminoAcid];
                                    final int errorLeftIndex = errorLessValue + borders[1];
                                    final int errorRightIndex = errorLessValue + borders[2] - 1;

                                    if (errorNewNumX <= xNumLimit) {
                                        // deletion
                                        backwardMatrix[k + 1][j].add(new MatrixContent(errorLeftIndex, errorRightIndex, '*', content, errorNewNumX, length, numVariants + 1, Character.toChars(errorAminoAcid + 32)[0]));

                                        // substitution
                                        if (aminoAcid != errorAminoAcid) {
                                            backwardMatrix[k + 1][j + 1].add(new MatrixContent(errorLeftIndex, errorRightIndex, aminoAcid, content, errorNewNumX, length + 1, numVariants + 1, (char) errorAminoAcid));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // traceback
            for (ArrayList<MatrixContent>[] backwardList : backwardMatrix) {
                for (MatrixContent content : backwardList[lenPeptide]) {
                    MatrixContent currentContent = content;
                    String currentPeptide = "";
                    String allVariants = "";

                    while (currentContent.previousContent != null) {
                        //if (currentContent.character != '*')
                        currentPeptide += (char) currentContent.character;
                        allVariants += currentContent.variant;
                        currentContent = currentContent.previousContent;
                    }

                    int leftIndex = content.left;
                    int rightIndex = content.right;
                    String cleanPeptide = currentPeptide.replace("*", "");

                    for (int j = leftIndex; j <= rightIndex; ++j) {
                        int pos = getTextPosition(j, indexPart);
                        int index = binarySearch(boundaries.get(indexPart), pos);
                        String accession = accessions.get(indexPart)[index];

                        int startPosition = pos - boundaries.get(indexPart)[index];
                        boolean newPeptide = true;

                        for (PeptideProteinMapping ppm : allMatches) {
                            if (ppm.getProteinAccession().equals(accession) && ppm.getPeptideSequence().equals(cleanPeptide) && Math.abs(ppm.getIndex() - startPosition) <= maxNumberVariants) {
                                newPeptide = false;
                                break;
                            }
                        }

                        if (newPeptide) {

                            HashMap<Integer, Variant> variants = new HashMap<>(0);
                            int lengthDifference = 0;

                            // adding variants and adjusting modification sites
                            for (int l = 0, length = 0; l < allVariants.length(); ++l) {
                                int edit = allVariants.charAt(l);
                                ++length;
                                if (edit != '-') {
                                    if (edit == '*') { // insertion
                                        Variant variant = new Insertion(peptide.charAt(length - 1));
                                        variants.put(length, variant);
                                        lengthDifference--;
                                    } else if ('A' <= edit && edit <= 'Z') { // substitution
                                        Variant variant = new Substitution((char) edit, peptide.charAt(length - 1));
                                        variants.put(length, variant);
                                    } else if ('a' <= edit && edit <= 'z') { // deletion
                                        Variant variant = new Deletion((char) (edit - 32));
                                        variants.put(length, variant);
                                        lengthDifference++;
                                        --length;
                                    }
                                }
                            }

                            PeptideVariantMatches peptideVariantMatches = variants.isEmpty() ? null : new PeptideVariantMatches(variants, lengthDifference);

                            PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, cleanPeptide, startPosition, null, peptideVariantMatches);
                            allMatches.add(peptideProteinMapping);
                        }
                    }
                }
            }
        }

        /*
        for (PeptideProteinMapping ppm : allMatches){
            System.out.println(ppm.getPeptideSequence() + " " + ppm.getProteinAccession() + " " + ppm.getIndex() + " " + ppm.getVariantMatches().size() + "e");
        }
         */
        return allMatches;
    }

    /**
     * Variant tolerant mapping peptides against the proteome.
     *
     * @param peptide the peptide
     * @param seqMatchPref the sequence matching preferences
     * @param indexPart the index part
     * @return the mapping
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithVariantsSpecific(String peptide, SequenceMatchingParameters seqMatchPref, int indexPart) {
        int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
        WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>();

        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int lenPeptide = peptide.length();
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref);
        int xNumLimit = (int) (((seqMatchPref.getLimitX() != null) ? seqMatchPref.getLimitX() : 1) * lenPeptide);

        int numErrors = maxNumberDeletions + maxNumberInsertions + maxNumberSubstitutions;
        LinkedList<MatrixContent>[][] backwardMatrix = (LinkedList<MatrixContent>[][]) new LinkedList[numErrors + 1][lenPeptide + 1];

        for (int k = 0; k <= numErrors; ++k) {
            for (int j = 0; j <= lenPeptide; ++j) {
                backwardMatrix[k][j] = new LinkedList<>();
            }
        }
        int countX = 0;
        for (int j = 0; j <= lenPeptide; ++j) {
            if (j < lenPeptide && pep_rev.charAt(j) == 'X') {
                ++countX;
            }
        }

        if (countX <= xNumLimit) {
            backwardMatrix[0][0].add(new MatrixContent(indexStringLengths.get(indexPart) - 1));

            for (int k = 0; k <= numErrors; ++k) {
                LinkedList<MatrixContent>[] backwardList = backwardMatrix[k];
                for (int j = 0; j < lenPeptide; ++j) {
                    String combinationSequence = combinations.get(j);
                    LinkedList<MatrixContent> cell = backwardList[j];

                    while (!cell.isEmpty()) {
                        MatrixContent content = cell.pop();
                        int leftIndexOld = content.left;
                        int rightIndexOld = content.right;
                        int numX = content.numX;
                        int length = content.length;
                        int numDeletions = content.numSpecificVariants[0];
                        int numInsertions = content.numSpecificVariants[1];
                        int numSubstitutions = content.numSpecificVariants[2];

                        for (int c = 0; c < combinationSequence.length(); ++c) {
                            int aminoAcid = combinationSequence.charAt(c);

                            int lessValue = lessTablePrimary[aminoAcid];
                            int[] range = occurrenceTablePrimary.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcid);
                            final int leftIndex = lessValue + range[0];
                            final int rightIndex = lessValue + range[1] - 1;
                            int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);

                            if (leftIndex <= rightIndex) {
                                if (newNumX > xNumLimit) {
                                    continue;
                                }

                                // match
                                backwardList[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newNumX, length + 1, new int[]{numDeletions, numInsertions, numSubstitutions}, '-'));
                            }

                            if (c == 0) {
                                // insertion
                                if (numInsertions < maxNumberInsertions) {
                                    backwardMatrix[k + 1][j + 1].add(new MatrixContent(leftIndexOld, rightIndexOld, aminoAcid, content, newNumX, length + 1, new int[]{numDeletions, numInsertions + 1, numSubstitutions}, '*'));
                                }

                                // deletion and substitution
                                int[][] setCharacter = occurrenceTablePrimary.rangeQuery(leftIndexOld - 1, rightIndexOld);
                                for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                                    int[] borders = setCharacter[b];
                                    final int errorAminoAcid = borders[0];
                                    final int errorNewNumX = newNumX + ((errorAminoAcid == 'X') ? 1 : 0);
                                    final int errorLessValue = lessTablePrimary[errorAminoAcid];
                                    final int errorLeftIndex = errorLessValue + borders[1];
                                    final int errorRightIndex = errorLessValue + borders[2] - 1;

                                    if (errorNewNumX <= xNumLimit) {

                                        // deletion
                                        if (numDeletions < maxNumberDeletions) {
                                            backwardMatrix[k + 1][j].add(new MatrixContent(errorLeftIndex, errorRightIndex, '*', content, errorNewNumX, length, new int[]{numDeletions + 1, numInsertions, numSubstitutions}, Character.toChars(errorAminoAcid + 32)[0]));
                                        }

                                        // substitution
                                        if (aminoAcid != errorAminoAcid && numSubstitutions < maxNumberSubstitutions && substitutionMatrix[errorAminoAcid][aminoAcid]) {
                                            backwardMatrix[k + 1][j + 1].add(new MatrixContent(errorLeftIndex, errorRightIndex, aminoAcid, content, errorNewNumX, length + 1, new int[]{numDeletions, numInsertions, numSubstitutions + 1}, (char) errorAminoAcid));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // traceback
            for (LinkedList<MatrixContent>[] backwardList : backwardMatrix) {
                for (MatrixContent content : backwardList[lenPeptide]) {
                    MatrixContent currentContent = content;
                    String currentPeptide = "";
                    String allVariants = "";

                    while (currentContent.previousContent != null) {
                        //if (currentContent.character != '*')
                        currentPeptide += (char) currentContent.character;
                        allVariants += currentContent.variant;
                        currentContent = currentContent.previousContent;
                    }

                    int leftIndex = content.left;
                    int rightIndex = content.right;
                    String cleanPeptide = currentPeptide.replace("*", "");

                    for (int j = leftIndex; j <= rightIndex; ++j) {
                        int pos = getTextPosition(j, indexPart);
                        int index = binarySearch(boundaries.get(indexPart), pos);
                        String accession = accessions.get(indexPart)[index];

                        int startPosition = pos - boundaries.get(indexPart)[index];
                        boolean newPeptide = true;

                        for (PeptideProteinMapping ppm : allMatches) {
                            if (ppm.getProteinAccession().equals(accession) && ppm.getPeptideSequence().equals(cleanPeptide) && Math.abs(ppm.getIndex() - startPosition) <= numErrors) {
                                newPeptide = false;
                                break;
                            }
                        }

                        if (newPeptide) {

                            HashMap<Integer, Variant> variants = new HashMap<>(0);
                            int lengthDifference = 0;

                            // adding variants and adjusting modification sites
                            for (int l = 0, length = 0; l < allVariants.length(); ++l) {
                                int edit = allVariants.charAt(l);
                                ++length;
                                if (edit != '-') {
                                    if (edit == '*') { // insertion
                                        Variant variant = new Insertion(peptide.charAt(length - 1));
                                        variants.put(length, variant);
                                        lengthDifference--;
                                    } else if ('A' <= edit && edit <= 'Z') { // substitution
                                        Variant variant = new Substitution((char) edit, peptide.charAt(length - 1));
                                        variants.put(length, variant);
                                    } else if ('a' <= edit && edit <= 'z') { // deletion
                                        Variant variant = new Deletion((char) (edit - 32));
                                        variants.put(length, variant);
                                        lengthDifference++;
                                        --length;
                                    }
                                }
                            }

                            PeptideVariantMatches peptideVariantMatches = variants.isEmpty() ? null : new PeptideVariantMatches(variants, lengthDifference);

                            PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, cleanPeptide, startPosition, null, peptideVariantMatches);
                            allMatches.add(peptideProteinMapping);
                        }
                    }
                }
            }
        }

        /*
        for (PeptideProteinMapping ppm : allMatches){
            System.out.println(ppm.getPeptideSequence() + " " + ppm.getProteinAccession() + " " + ppm.getIndex() + " " + ppm.getVariantMatches().size() + "e");
        }
         */
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

    /**
     * Adding modifications for backward search suggestions.
     *
     * @param setCharacter the set characters
     */
    private void addModifications(int[][] setCharacter) {
        int maxNum = setCharacter[numMasses][0];
        for (int i = 0; i < maxNum; ++i) {
            int pos = 128 + setCharacter[i][0];
            while (pos < aaMasses.length && aaMasses[pos] != -1) {
                setCharacter[setCharacter[numMasses][0]++] = new int[]{setCharacter[i][0], setCharacter[i][1], setCharacter[i][2], pos, setCharacter[i][4]};
                pos += 128;
            }
        }
    }

    /**
     * Exchange B, J, Z amino acid queries into their corresponding amino acids
     *
     * @param setCharacter
     */
    private void addAmbiguous(int[][] setCharacter) {

        long[] foundChars = new long[]{0L, 0L};
        int maxNum = setCharacter[numMasses][0];
        int[] BValues = null;
        int[] JValues = null;
        int[] ZValues = null;
        for (int i = 0; i < maxNum; ++i) {
            if (setCharacter[i][0] == 'B' || setCharacter[i][0] == 'J' || setCharacter[i][0] == 'Z') {
                switch (setCharacter[i][0]) {
                    case 'B':
                        BValues = setCharacter[i];
                        break;
                    case 'J':
                        JValues = setCharacter[i];
                        break;
                    case 'Z':
                        ZValues = setCharacter[i];
                        break;
                }
                if (i != maxNum - 1) {
                    setCharacter[i] = setCharacter[maxNum - 1];
                    --i;
                }
                --setCharacter[numMasses][0];
                --maxNum;
            } else {
                int c = setCharacter[i][0];
                foundChars[c >>> 6] |= (1L) << (c & 63);
            }
        }

        // adding all amino acids possible by B substitution, if not yet present in list
        if (BValues != null) {
            for (int i = 0; i < BSubstitutions.length; ++i) {
                int c = BSubstitutions[i];
                if (((foundChars[c >>> 6] >>> (c & 63)) & 1L) == 0) {
                    setCharacter[setCharacter[numMasses][0]++] = new int[]{c, BValues[1], BValues[2], c, 'B'};
                }
            }
        }

        // adding all amino acids possible by J substitution, if not yet present in list
        if (JValues != null) {
            for (int i = 0; i < JSubstitutions.length; ++i) {
                int c = JSubstitutions[i];
                if (((foundChars[c >>> 6] >>> (c & 63)) & 1L) == 0) {
                    setCharacter[setCharacter[numMasses][0]++] = new int[]{c, JValues[1], JValues[2], c, 'J'};
                }
            }
        }

        // adding all amino acids possible by Z substitution, if not yet present in list
        if (ZValues != null) {
            for (int i = 0; i < ZSubstitutions.length; ++i) {
                int c = ZSubstitutions[i];
                if (((foundChars[c >>> 6] >>> (c & 63)) & 1L) == 0) {
                    setCharacter[setCharacter[numMasses][0]++] = new int[]{c, ZValues[1], ZValues[2], c, 'Z'};
                }
            }
        }
        //System.out.println("out");
    }

    private boolean massNotValid(double mass) {
        int intMass = (int) (mass * lookupMultiplier);
        return (mass > massTolerance && mass < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0));
    }

    /**
     * Mapping the tag elements to the reference text having mass tolerance in
     * Dalton.
     *
     * @param combinations the combinations
     * @param matrix the matrix
     * @param matrixFinished the finished matrix
     * @param less the less array
     * @param occurrence the wavelet tree
     */
    private void mappingSequenceAndMassesDA(TagElement[] combinations, LinkedList<MatrixContent>[] matrix, int[] less, WaveletTree occurrence) {

        for (int j = 0; j < combinations.length; ++j) {
            LinkedList<MatrixContent> content = matrix[j];
            TagElement combination = combinations[j];

            while (!content.isEmpty()) {
                MatrixContent cell = content.removeFirst();
                final int length = cell.length;
                final int leftIndexOld = cell.left;
                final int rightIndexOld = cell.right;
                final int numX = cell.numX;

                if (combination.isMass) {
                    final double combinationMass = combination.mass;
                    final double oldMass = cell.mass;
                    int[][] setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                    addAmbiguous(setCharacter);
                    if (withVariableModifications) {
                        addModifications(setCharacter);
                    }

                    for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                        int[] borders = setCharacter[b];
                        final int aminoAcid = borders[0];
                        if (aminoAcid == '/') {
                            continue;
                        }
                        int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);
                        if (newNumX > combination.xNumLimit) {
                            continue;
                        }
                        final double newMass = oldMass + (aminoAcid != 'X' ? aaMasses[borders[3]] : 0);

                        // check if not exceeding tag mass
                        if (newMass - massTolerance <= combinationMass) {
                            final int aminoAcidSearch = (borders[4] == -1) ? aminoAcid : borders[4];
                            final int lessValue = less[aminoAcidSearch];
                            final int leftIndex = lessValue + borders[1];
                            final int rightIndex = lessValue + borders[2] - 1;
                            final double massDiff = Math.abs(combinationMass - newMass);
                            //System.out.println(j + " " + length + " " + (char)borders[0] + " " +  leftIndex + " " + rightIndex + " " + offset + " " + newNumX + " " + massDiff + " / " + combination.xNumLimit);

                            // make a lookup when mass difference is below 800Da if it is still possible to reach by a AA combination
                            if (massNotValid(massDiff)) {
                                continue;
                            }
                            boolean withinMass = withinMassTolerance(massDiff, newNumX);
                            int offset = ((massDiff <= massTolerance) ? 1 : 0) | (withinMass ? 1 : 0);

                            if (offset > 0) {
                                newNumX = 0;
                            }
                            matrix[j + offset].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, length + 1, newNumX, borders[3], borders[4], j));
                            if (withinMass) {
                                matrix[j + offset].getLast().XMassDiff = massDiff;
                            }
                        }
                    }
                } else {
                    final String combinationSequence = combination.sequence;
                    final int xNumLimit = combination.xNumLimit;
                    final int aminoAcid = combinationSequence.charAt(0);
                    for (int i = 0; i < combinationSequence.length(); ++i) {
                        final int aminoAcidSearch = combinationSequence.charAt(i);
                        final int lessValue = less[aminoAcidSearch];
                        final int[] range = occurrence.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcidSearch);
                        final int leftIndex = lessValue + range[0];
                        final int rightIndex = lessValue + range[1] - 1;
                        int newNumX = numX + ((aminoAcidSearch == 'X') ? 1 : 0);
                        if (leftIndex <= rightIndex && newNumX <= xNumLimit) {
                            if (j < combinations.length - 1 && combinations[j].isMass != combinations[j + 1].isMass) {
                                newNumX = 0;
                            }
                            matrix[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, length + 1, newNumX, -1, aminoAcidSearch, j));
                        }
                    }
                }
            }
        }
    }

    /**
     * Mapping the tag elements to the reference text having mass tolerance in
     * ppm.
     *
     * @param combinations the combinations
     * @param matrix the matrix
     * @param matrixFinished the finished matrix
     * @param less the less array
     * @param occurrence the wavelet tree
     */
    private void mappingSequenceAndMassesPPM(TagElement[] combinations, LinkedList<MatrixContent>[] matrix, int[] less, WaveletTree occurrence) {

        for (int j = 0; j < combinations.length; ++j) {
            LinkedList<MatrixContent> content = matrix[j];
            TagElement combination = combinations[j];

            while (!content.isEmpty()) {
                MatrixContent cell = content.removeFirst();
                final int length = cell.length;
                final int leftIndexOld = cell.left;
                final int rightIndexOld = cell.right;
                final int numX = cell.numX;

                if (combination.isMass) {
                    final double combinationMass = combination.mass;
                    final double oldMass = cell.mass;
                    int[][] setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                    addAmbiguous(setCharacter);
                    if (withVariableModifications) {
                        addModifications(setCharacter);
                    }

                    for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                        int[] borders = setCharacter[b];
                        final int aminoAcid = borders[0];
                        if (aminoAcid == '/') {
                            continue;
                        }
                        int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);
                        if (newNumX > combination.xNumLimit) {
                            continue;
                        }
                        final double newMass = oldMass + (aminoAcid != 'X' ? aaMasses[borders[3]] : 0);

                        //System.out.println(j + " " + length + " " + (char)borders[0] + " " + newMass + " " + computeInverseMassValue(massTolerance, combinationMass) + " " + combinationMass);
                        // check if not exceeding tag mass
                        if (newMass - computeInverseMassValue(massTolerance, combinationMass) <= combinationMass) {
                            final int aminoAcidSearch = (borders[4] == -1) ? aminoAcid : borders[4];
                            final int lessValue = less[aminoAcidSearch];
                            final int leftIndex = lessValue + borders[1];
                            final int rightIndex = lessValue + borders[2] - 1;
                            final double massDiff = Math.abs(combinationMass - newMass);

                            // make a lookup when mass difference is below 800Da if it is still possible to reach by a AA combination
                            if (massNotValid(massDiff)) {
                                continue;
                            }
                            boolean withinMass = withinMassTolerance(massDiff, newNumX);
                            int offset = ((computeMassValue(newMass, combinationMass) <= massTolerance) ? 1 : 0) | (withinMass ? 1 : 0);
                            //System.out.println(j + " " + length + " " + (char)borders[0] + " " +  leftIndex + " " + rightIndex + " " + offset + " " + newNumX + " " + massDiff + " / " + combination.xNumLimit);

                            if (offset > 0) {
                                newNumX = 0;
                            }
                            matrix[j + offset].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, length + 1, newNumX, borders[3], borders[4], j));
                            if (withinMass) {
                                matrix[j + offset].getLast().XMassDiff = massDiff;
                            }
                        }
                    }
                } else {
                    final String combinationSequence = combination.sequence;
                    final int xNumLimit = combination.xNumLimit;
                    final int aminoAcid = combinationSequence.charAt(0);
                    for (int i = 0; i < combinationSequence.length(); ++i) {
                        final int aminoAcidSearch = combinationSequence.charAt(i);
                        final int lessValue = less[aminoAcidSearch];
                        final int[] range = occurrence.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcidSearch);
                        final int leftIndex = lessValue + range[0];
                        final int rightIndex = lessValue + range[1] - 1;
                        int newNumX = numX + ((aminoAcidSearch == 'X') ? 1 : 0);
                        if (leftIndex <= rightIndex && newNumX <= xNumLimit) {
                            if (j < combinations.length - 1 && combinations[j].isMass != combinations[j + 1].isMass) {
                                newNumX = 0;
                            }
                            matrix[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, length + 1, newNumX, -1, aminoAcidSearch, j));
                        }
                    }
                }
            }
        }
    }

    /**
     * Variant tolerant mapping the tag elements to the reference text with a
     * generic upper limit of variants.
     *
     * @param combinations the combinations
     * @param matrix the matrix
     * @param matrixFinished the finished matrix
     * @param less the less array
     * @param occurrence the wavelet tree
     * @param massTolerance the mass tolerance
     * @param numberEdits number of allowed edit operations
     */
    private void mappingSequenceAndMassesWithVariantsGeneric(TagElement[] combinations, LinkedList<MatrixContent>[][] matrix, int[] less, WaveletTree occurrence) {
        final int lenCombinations = combinations.length;

        for (int k = 0; k <= maxNumberVariants; ++k) {
            LinkedList<MatrixContent>[] row = matrix[k];

            for (int j = 0; j < lenCombinations; ++j) {
                TagElement combination = combinations[j];
                LinkedList<MatrixContent> cell = row[j];

                while (!cell.isEmpty()) {
                    MatrixContent content = cell.removeFirst();
                    final int leftIndexOld = content.left;
                    final int length = content.length;
                    final int rightIndexOld = content.right;
                    final int numVariants = content.numVariants;
                    final int numX = content.numX;

                    if (combination.isMass) {
                        final double combinationMass = combination.mass;
                        final double oldMass = content.mass;

                        int[][] setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                        if (withVariableModifications) {
                            addModifications(setCharacter);
                        }

                        for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                            int[] borders = setCharacter[b];
                            final int aminoAcid = borders[0];
                            if (aminoAcid == '/') {
                                continue;
                            }
                            final double newMass = oldMass + aaMasses[borders[3]];
                            final int lessValue = less[aminoAcid];
                            final int leftIndex = lessValue + borders[1];
                            final int rightIndex = lessValue + borders[2] - 1;
                            int offset = (Math.abs(combinationMass - newMass) <= massTolerance) ? 1 : 0;

                            if (newMass - massTolerance <= combinationMass) {
                                boolean add = true;
                                double massDiff = combinationMass - newMass;
                                int intMass = (int) (massDiff * lookupMultiplier);
                                if (massDiff > massTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                    add = false;
                                }
                                if (add) {
                                    row[j + offset].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newMass, length + 1, numX, borders[3], numVariants, '-', null));
                                }
                            }
                            // variants
                            if (numVariants < maxNumberVariants) {

                                // deletion
                                matrix[k + 1][j].add(new MatrixContent(leftIndex, rightIndex, '*', content, oldMass, length, numX, -1, numVariants + 1, Character.toChars(aminoAcid + 32)[0], null));

                                // substitution
                                for (int index : aaMassIndexes) {
                                    double aminoMass = oldMass + aaMasses[index];
                                    int offsetSub = (Math.abs(combinationMass - aminoMass) <= massTolerance) ? 1 : 0;
                                    int amino = index & 127;

                                    if (amino != aminoAcid && aminoMass < combinationMass + massTolerance) {
                                        boolean add = true;
                                        double massDiff = combinationMass - aminoMass;
                                        int intMass = (int) (massDiff * lookupMultiplier);
                                        if (massDiff > massTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                            add = false;
                                        }
                                        if (add) {
                                            matrix[k + 1][j + offsetSub].add(new MatrixContent(leftIndex, rightIndex, amino, content, aminoMass, length + 1, numX, index, numVariants + 1, (char) aminoAcid, null));
                                        }
                                    }
                                }
                            }
                        }

                        // insertion
                        if (numVariants < maxNumberVariants) {
                            for (int index : aaMassIndexes) {
                                double aminoMass = oldMass + aaMasses[index];
                                int offsetDel = (Math.abs(combinationMass - aminoMass) <= massTolerance) ? 1 : 0;
                                int amino = index & 127;

                                if (aminoMass < combinationMass + massTolerance) {
                                    boolean add = true;
                                    double massDiff = combinationMass - aminoMass;
                                    int intMass = (int) (massDiff * lookupMultiplier);
                                    if (massDiff > massTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                        add = false;
                                    }
                                    if (add) {
                                        matrix[k + 1][j + offsetDel].add(new MatrixContent(leftIndexOld, rightIndexOld, amino, content, aminoMass, length + 1, numX, index, numVariants + 1, '*', null));
                                    }
                                }
                            }
                        }

                    } else { // sequence mapping 
                        final String combinationSequence = combination.sequence;
                        final int xNumLimit = combination.xNumLimit;

                        for (int c = 0; c < combinationSequence.length(); ++c) {
                            final int aminoAcid = combinationSequence.charAt(c);
                            final int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);
                            if (newNumX > xNumLimit) {
                                continue;
                            }

                            final int lessValue = less[aminoAcid];
                            final int[] range = occurrence.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcid);
                            final int leftIndex = lessValue + range[0];
                            final int rightIndex = lessValue + range[1] - 1;

                            // match
                            if (leftIndex <= rightIndex) {
                                row[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newNumX, length + 1, numVariants, '-'));
                            }

                            // variants
                            if (numVariants < maxNumberVariants && c == 0) {
                                // insertion
                                if (numVariants < maxNumberVariants) {
                                    matrix[k + 1][j + 1].add(new MatrixContent(leftIndexOld, rightIndexOld, aminoAcid, content, newNumX, length + 1, numVariants + 1, '*'));
                                }

                                // deletion and substitution
                                int[][] setCharacterSeq = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                                for (int b = 0; b < setCharacterSeq[numMasses][0]; ++b) {
                                    int[] borders = setCharacterSeq[b];
                                    final int errorAminoAcid = borders[0];
                                    final int errorNewNumX = newNumX + ((errorAminoAcid != 'X') ? 0 : 1);
                                    if (errorNewNumX > xNumLimit) {
                                        continue;
                                    }

                                    final int errorLessValue = less[errorAminoAcid];
                                    final int errorLeftIndex = errorLessValue + borders[1];
                                    final int errorRightIndex = errorLessValue + borders[2] - 1;

                                    // deletion
                                    matrix[k + 1][j].add(new MatrixContent(errorLeftIndex, errorRightIndex, '*', content, errorNewNumX, length, numVariants + 1, Character.toChars(errorAminoAcid + 32)[0]));

                                    // substitution
                                    if (aminoAcid != errorAminoAcid) {
                                        matrix[k + 1][j + 1].add(new MatrixContent(errorLeftIndex, errorRightIndex, aminoAcid, content, errorNewNumX, length + 1, numVariants + 1, (char) errorAminoAcid));
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * Variant tolerant mapping the tag elements to the reference text with a
     * generic upper limit of variants.
     *
     * @param combinations the combinations
     * @param matrix the matrix
     * @param matrixFinished the finished matrix
     * @param less the less array
     * @param occurrence the wavelet tree
     * @param numberEdits number of allowed edit operations
     */
    private void mappingSequenceAndMassesWithVariantsSpecific(TagElement[] combinations, LinkedList<MatrixContent>[][] matrix, int[] less, WaveletTree occurrence) {
        final int lenCombinations = combinations.length;
        int maxNumberSpecificVariants = maxNumberDeletions + maxNumberInsertions + maxNumberSubstitutions;

        for (int k = 0; k <= maxNumberSpecificVariants; ++k) {
            LinkedList<MatrixContent>[] row = matrix[k];

            for (int j = 0; j < lenCombinations; ++j) {
                TagElement combination = combinations[j];
                LinkedList<MatrixContent> cell = row[j];
                while (!cell.isEmpty()) {
                    MatrixContent content = cell.removeFirst();
                    final int leftIndexOld = content.left;
                    final int rightIndexOld = content.right;
                    final int length = content.length;
                    final int numDeletions = content.numSpecificVariants[0];
                    final int numInsertions = content.numSpecificVariants[1];
                    final int numSubstitutions = content.numSpecificVariants[2];
                    final int numX = content.numX;

                    if (combination.isMass) {
                        final double combinationMass = combination.mass;
                        final double oldMass = content.mass;

                        int[][] setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                        if (withVariableModifications) {
                            addModifications(setCharacter);
                        }

                        for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                            int[] borders = setCharacter[b];
                            final int aminoAcid = borders[0];
                            if (aminoAcid == '/') {
                                continue;
                            }
                            final double newMass = oldMass + aaMasses[borders[3]];
                            final int lessValue = less[aminoAcid];
                            final int leftIndex = lessValue + borders[1];
                            final int rightIndex = lessValue + borders[2] - 1;
                            int offset = (Math.abs(combinationMass - newMass) <= massTolerance) ? 1 : 0;

                            if (newMass - massTolerance <= combinationMass) {
                                boolean add = true;
                                double massDiff = combinationMass - newMass;
                                int intMass = (int) (massDiff * lookupMultiplier);
                                if (massDiff > massTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                    add = false;
                                }
                                if (add) {
                                    row[j + offset].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newMass, length + 1, numX, borders[3], new int[]{numDeletions, numInsertions, numSubstitutions}, '-', null));
                                }
                            }
                            // variants
                            if (numDeletions < maxNumberDeletions) {
                                // deletion
                                matrix[k + 1][j].add(new MatrixContent(leftIndex, rightIndex, '*', content, oldMass, length, numX, -1, new int[]{numDeletions + 1, numInsertions, numSubstitutions}, Character.toChars(aminoAcid + 32)[0], null));
                            }
                            // substitution
                            if (numSubstitutions < maxNumberSubstitutions) {
                                for (int index : aaMassIndexes) {
                                    int amino = index & 127;

                                    // check allowed substitutions
                                    if (!substitutionMatrix[amino][aminoAcid]) {
                                        continue;
                                    }

                                    double aminoMass = oldMass + aaMasses[index];
                                    int offsetSub = (Math.abs(combinationMass - aminoMass) <= massTolerance) ? 1 : 0;

                                    if (amino != aminoAcid && aminoMass < combinationMass + massTolerance) {
                                        boolean add = true;
                                        double massDiff = combinationMass - aminoMass;
                                        int intMass = (int) (massDiff * lookupMultiplier);
                                        if (massDiff > massTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                            add = false;
                                        }
                                        if (add) {
                                            matrix[k + 1][j + offsetSub].add(new MatrixContent(leftIndex, rightIndex, amino, content, aminoMass, length + 1, numX, index, new int[]{numDeletions, numInsertions, numSubstitutions + 1}, (char) aminoAcid, null));
                                        }
                                    }
                                }
                            }
                        }

                        // insertion
                        if (numInsertions < maxNumberInsertions) {
                            for (int index : aaMassIndexes) {
                                double aminoMass = oldMass + aaMasses[index];
                                int offsetDel = (Math.abs(combinationMass - aminoMass) <= massTolerance) ? 1 : 0;
                                int amino = index & 127;

                                if (aminoMass < combinationMass + massTolerance) {
                                    boolean add = true;
                                    double massDiff = combinationMass - aminoMass;
                                    int intMass = (int) (massDiff * lookupMultiplier);
                                    if (massDiff > massTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                        add = false;
                                    }
                                    if (add) {
                                        matrix[k + 1][j + offsetDel].add(new MatrixContent(leftIndexOld, rightIndexOld, amino, content, aminoMass, length + 1, numX, index, new int[]{numDeletions, numInsertions + 1, numSubstitutions}, '*', null));
                                    }
                                }
                            }
                        }

                    } else { // sequence mapping 
                        final String combinationSequence = combination.sequence;
                        final int xNumLimit = combination.xNumLimit;

                        for (int c = 0; c < combinationSequence.length(); ++c) {
                            final int aminoAcid = combinationSequence.charAt(c);
                            final int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);
                            if (newNumX > xNumLimit) {
                                continue;
                            }

                            final int lessValue = less[aminoAcid];
                            final int[] range = occurrence.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcid);
                            final int leftIndex = lessValue + range[0];
                            final int rightIndex = lessValue + range[1] - 1;

                            // match
                            if (leftIndex <= rightIndex) {
                                row[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newNumX, length + 1, new int[]{numDeletions, numInsertions, numSubstitutions}, '-'));
                            }

                            // variants
                            if (c == 0) {
                                // insertion
                                if (numInsertions < maxNumberInsertions) {
                                    matrix[k + 1][j + 1].add(new MatrixContent(leftIndexOld, rightIndexOld, aminoAcid, content, newNumX, length + 1, new int[]{numDeletions, numInsertions + 1, numSubstitutions}, '*'));
                                }

                                // deletion and substitution
                                if (numDeletions < maxNumberDeletions || numSubstitutions < maxNumberSubstitutions) {
                                    int[][] setCharacterSeq = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                                    for (int b = 0; b < setCharacterSeq[numMasses][0]; ++b) {
                                        int[] borders = setCharacterSeq[b];
                                        final int errorAminoAcid = borders[0];
                                        final int errorNewNumX = newNumX + ((errorAminoAcid != 'X') ? 0 : 1);
                                        if (errorNewNumX > xNumLimit) {
                                            continue;
                                        }

                                        final int errorLessValue = less[errorAminoAcid];
                                        final int errorLeftIndex = errorLessValue + borders[1];
                                        final int errorRightIndex = errorLessValue + borders[2] - 1;

                                        // deletion
                                        if (numDeletions < maxNumberDeletions) {
                                            matrix[k + 1][j].add(new MatrixContent(errorLeftIndex, errorRightIndex, '*', content, errorNewNumX, length, new int[]{numDeletions + 1, numInsertions, numSubstitutions}, Character.toChars(errorAminoAcid + 32)[0]));
                                        }

                                        // substitution
                                        if (numSubstitutions < maxNumberSubstitutions && aminoAcid != errorAminoAcid && substitutionMatrix[errorAminoAcid][aminoAcid]) {
                                            matrix[k + 1][j + 1].add(new MatrixContent(errorLeftIndex, errorRightIndex, aminoAcid, content, errorNewNumX, length + 1, new int[]{numDeletions, numInsertions, numSubstitutions + 1}, (char) errorAminoAcid));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Computing the mass of a peptide.
     *
     * @param peptide the peptide
     * @return the peptide mass
     */
    public double pepMass(String peptide) {
        double mass = 0;
        for (int i = 0; i < peptide.length(); ++i) {
            mass += aaMasses[peptide.charAt(i)];
        }
        return mass;
    }

    /**
     * Lookup, if mass can be described a combination of numX different amino
     * acids
     *
     * @param mass to be described
     * @param numX number of Xs
     * @return decision
     */
    public boolean withinMassTolerance(double mass, int numX) {
        if (mass + computeInverseMassValue(massTolerance, mass) < negativePTMMass) {
            return false;
        }
        int intMass = (int) (mass * lookupMultiplier);
        return (0 < numX && numX <= maxXPerTag && mass < lookupMaxMass && (((Xlookup[numX][intMass >>> 6] >>> (intMass & 63)) & 1L) == 1));
    }

    /**
     * Mapping the tag elements to the reference text having mass tolerance in
     * ppm.
     *
     * @param combinations the combinations
     * @param matrix the matrix
     * @param matrixFinished the finished matrix
     * @param less the less values
     * @param occurrence the occurrence
     * @param massTolerance the mass tolerance
     * @param CTermDirection the c term direction
     */
    private void mappingSequenceAndMassesPPM(TagElement[] combinations, LinkedList<MatrixContent>[] matrix, int[] less, WaveletTree occurrence, boolean CTermDirection) {
        final int lenCombinations = combinations.length;
        for (int k = 0; k < lenCombinations; ++k) {
            TagElement combination = combinations[k];
            LinkedList<MatrixContent> content = matrix[k];

            while (!content.isEmpty()) {
                MatrixContent cell = content.removeFirst();
                final int length = cell.length;
                final int leftIndexOld = cell.left;
                final int rightIndexOld = cell.right;

                if (combination.isMass) {
                    final double combinationMass = combination.mass;
                    final double oldMass = cell.mass;

                    int[][] setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                    addAmbiguous(setCharacter);
                    if (withVariableModifications) {
                        addModifications(setCharacter);
                    }

                    if (k == lenCombinations - 1) {
                        for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                            int[] borders = setCharacter[b];
                            final int aminoAcid = borders[0];
                            int newNumX = cell.numX + ((aminoAcid == 'X') ? 1 : 0);
                            if (newNumX > combination.xNumLimit) {
                                continue;
                            }

                            if (aminoAcid != '/') {
                                final int aminoAcidSearch = (borders[4] == -1) ? aminoAcid : borders[4];
                                final double newMass = oldMass + (aminoAcid != 'X' ? aaMasses[borders[3]] : 0);
                                double massDiff = combinationMass - newMass;
                                int lastAcid = aminoAcid;
                                final int lessValue = less[aminoAcidSearch];
                                final int leftIndex = lessValue + borders[1];
                                final int rightIndex = lessValue + borders[2] - 1;
                                //System.out.println(k + " " + length + " " + (char)borders[0] + " " +  leftIndex + " " + rightIndex + " " + newNumX + " " + massDiff + " / " + combination.xNumLimit);
                                MatrixContent newCell = new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, length + 1, newNumX, borders[3], borders[4], k);

                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;

                                // ptm at terminus handling
                                ArrayList<String> fmodp = fmodcp;
                                ArrayList<Double> fmodpMass = fmodcpMass;
                                ArrayList<String>[] fmodpaa = fmodcpaa;
                                ArrayList<Double>[] fmodpaaMass = fmodcpaaMass;
                                ArrayList<String> vmodp = vmodcp;
                                ArrayList<Double> vmodpMass = vmodcpMass;
                                ArrayList<String>[] vmodpaa = vmodcpaa;
                                ArrayList<Double>[] vmodpaaMass = vmodcpaaMass;
                                boolean hasFixedPTM_atTerminus = hasFixedPTM_CatTerminus;

                                if (!CTermDirection) {
                                    fmodp = fmodnp;
                                    fmodpMass = fmodnpMass;
                                    fmodpaa = fmodnpaa;
                                    fmodpaaMass = fmodnpaaMass;
                                    vmodp = vmodnp;
                                    vmodpMass = vmodnpMass;
                                    vmodpaa = vmodnpaa;
                                    vmodpaaMass = vmodnpaaMass;
                                    hasFixedPTM_atTerminus = hasFixedPTM_NatTerminus;
                                }

                                boolean hasFixed = false;
                                boolean withinMass = false;
                                double XmassDiff = -1 - massTolerance;

                                // fixed aa defined peptide terminal modification
                                if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                    hasFixed = true;
                                    for (int i = 0; i < fmodpaaMass[lastAcid].size(); ++i) {
                                        double massDiffDiff = massDiff - fmodpaaMass[lastAcid].get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (computeMassValue(fmodpaaMass[lastAcid].get(i) + newMass, combinationMass) < massTolerance || wmt) {
                                            withinMass |= wmt;
                                            XmassDiff = massDiffDiffAbs;
                                            modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                        }

                                        if (XmassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(fmodpaaMass[lastAcid].get(i) + newMass + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length + 1);
                                                }
                                            }
                                        }
                                        // variable undefined peptide terminal modifictation
                                        if (XmassDiff < -massTolerance && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(fmodpaaMass[lastAcid].get(i) + vmodpMass.get(j) + newMass, combinationMass) < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), false, length + 1);
                                                }
                                            }
                                        }
                                    }
                                }

                                // fixed undefined peptide terminal modifictation
                                if (fmodp != null && XmassDiff < -massTolerance) {
                                    hasFixed = true;
                                    for (int i = 0; i < fmodp.size(); ++i) {
                                        double massDiffDiff = massDiff - fmodpMass.get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (computeMassValue(fmodpMass.get(i) + newMass, combinationMass) < massTolerance || wmt) {
                                            withinMass |= wmt;
                                            XmassDiff = massDiffDiffAbs;
                                            modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                        }

                                        if (XmassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(fmodpMass.get(i) + newMass + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length + 1);
                                                }
                                            }
                                        }
                                        // variable undefined peptide terminal modifictation
                                        if (XmassDiff < -massTolerance && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(fmodpMass.get(i) + newMass + vmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), false, length + 1);
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!hasFixedPTM_atTerminus && !hasFixed && XmassDiff < -massTolerance) {
                                    // without any peptide terminal modification
                                    double massDiffF = Math.abs(massDiff);
                                    boolean wmt = withinMassTolerance(massDiffF, newNumX);
                                    if (computeMassValue(newMass, combinationMass) < massTolerance || wmt) {
                                        withinMass |= wmt;
                                        XmassDiff = massDiffF;
                                    }

                                    // variable aa defined peptide terminal modification
                                    if (XmassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                        for (int i = 0; i < vmodpaaMass[lastAcid].size(); ++i) {
                                            double massDiffV = Math.abs(massDiff - vmodpaaMass[lastAcid].get(i));
                                            boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                            if (computeMassValue(vmodpaaMass[lastAcid].get(i) + newMass, combinationMass) < massTolerance || wmtV) {
                                                withinMass |= wmtV;
                                                XmassDiff = massDiffV;
                                                modificationMatchEnd = new ModificationMatch(vmodpaa[lastAcid].get(i), true, length + 1);
                                            }
                                        }
                                    }

                                    // variable undefined peptide terminal modifictation
                                    if (XmassDiff < -massTolerance && vmodp != null) {
                                        for (int i = 0; i < vmodp.size(); ++i) {
                                            double massDiffV = Math.abs(massDiff - vmodpMass.get(i));
                                            boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                            if (computeMassValue(vmodpMass.get(i) + newMass, combinationMass) < massTolerance || wmtV) {
                                                withinMass |= wmtV;
                                                XmassDiff = massDiffV;
                                                modificationMatchEnd = new ModificationMatch(vmodp.get(i), false, length + 1);
                                            }
                                        }
                                    }
                                }

                                if (XmassDiff < -massTolerance) {
                                    if (newMass - computeInverseMassValue(massTolerance, combinationMass) + negativePTMMass <= combinationMass) {
                                        content.add(newCell);
                                    }
                                } else if (modificationMatchEnd != null) {
                                    if (newNumX > 0 && !withinMass) {
                                        continue;
                                    }
                                    MatrixContent newEndCell = new MatrixContent(leftIndex, rightIndex, '\0', newCell, 0, null, null, length + 1, 0, k, modificationMatchEnd, null, -1);
                                    if (modificationMatchEndEnd == null) {
                                        matrix[k + 1].add(newEndCell);
                                    } else {
                                        MatrixContent newEndEndCell = new MatrixContent(leftIndex, rightIndex, '\0', newEndCell, 0, null, null, length + 1, 0, k, modificationMatchEndEnd, null, -1);
                                        matrix[k + 1].add(newEndEndCell);
                                    }
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = XmassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                } else {
                                    if (newNumX > 0 && !withinMass) {
                                        continue;
                                    }
                                    matrix[k + 1].add(newCell);
                                    matrix[k + 1].getLast().numX = 0;
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = XmassDiff;
                                    }
                                }

                            } else if (length > 1) {
                                int lastAcid = cell.character;
                                double massDiff = combinationMass - oldMass;
                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;
                                boolean withinMass = false;
                                double XmassDiff = -1;
                                //System.out.println(k + " " + length + " " + (char)lastAcid + " " + massDiff + " / " + combination.xNumLimit);

                                // ptm at terminus handling
                                ArrayList<String> fmod = fmodc;
                                ArrayList<Double> fmodMass = fmodcMass;
                                ArrayList<String>[] fmodaa = fmodcaa;
                                ArrayList<Double>[] fmodaaMass = fmodcaaMass;
                                ArrayList<String> vmod = vmodc;
                                ArrayList<Double> vmodMass = vmodcMass;
                                ArrayList<String>[] vmodaa = vmodcaa;
                                ArrayList<Double>[] vmodaaMass = vmodcaaMass;
                                ArrayList<String> fmodp = fmodcp;
                                ArrayList<Double> fmodpMass = fmodcpMass;
                                ArrayList<String>[] fmodpaa = fmodcpaa;
                                ArrayList<Double>[] fmodpaaMass = fmodcpaaMass;
                                ArrayList<String> vmodp = vmodcp;
                                ArrayList<Double> vmodpMass = vmodcpMass;
                                ArrayList<String>[] vmodpaa = vmodcpaa;
                                ArrayList<Double>[] vmodpaaMass = vmodcpaaMass;

                                if (!CTermDirection) {
                                    fmod = fmodn;
                                    fmodMass = fmodnMass;
                                    fmodaa = fmodnaa;
                                    fmodaaMass = fmodnaaMass;
                                    vmod = vmodn;
                                    vmodMass = vmodnMass;
                                    vmodaa = vmodnaa;
                                    vmodaaMass = vmodnaaMass;
                                    fmodp = fmodnp;
                                    fmodpMass = fmodnpMass;
                                    fmodpaa = fmodnpaa;
                                    fmodpaaMass = fmodnpaaMass;
                                    vmodp = vmodnp;
                                    vmodpMass = vmodnpMass;
                                    vmodpaa = vmodnpaa;
                                    vmodpaaMass = vmodnpaaMass;
                                }

                                // fixed aa defined protein terminal modification
                                if (fmodaa != null && lastAcid > 0 && fmodaaMass[lastAcid].size() > 0) {
                                    for (int i = 0; i < fmodaaMass[lastAcid].size(); ++i) {
                                        double massDiffDiff = massDiff - fmodaaMass[lastAcid].get(i);
                                        double mDD = oldMass + fmodaaMass[lastAcid].get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (computeMassValue(mDD, combinationMass) < massTolerance || wmt) {
                                            withinMass |= wmt;
                                            XmassDiff = massDiffDiffAbs;
                                            modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(mDD + vmodaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), true, length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(mDD + vmodMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), false, length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(mDD + fmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(mDD + fmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (computeMassValue(mDD + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (computeMassValue(mDD + vmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, length);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // fixed undefined protein terminal modifictation
                                if (fmod != null && modificationMatchEnd == null) {

                                    for (int i = 0; i < fmod.size(); ++i) {
                                        double massDiffDiff = massDiff - fmodMass.get(i);
                                        double mDD = oldMass + fmodMass.get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (computeMassValue(mDD, combinationMass) < massTolerance || wmt) {
                                            withinMass |= wmt;
                                            XmassDiff = massDiffDiffAbs;
                                            modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                double massDiffV = Math.abs(massDiff - vmodaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                                if (computeMassValue(vmodaaMass[lastAcid].get(j) + oldMass, combinationMass) < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), true, length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                double massDiffV = Math.abs(massDiff - vmodMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                                if (computeMassValue(vmodMass.get(j) + oldMass, combinationMass) < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), false, length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(mDD + fmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (computeMassValue(mDD + fmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (computeMassValue(mDD + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (computeMassValue(mDD + vmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, length);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (modificationMatchEnd == null) {
                                    // variable aa defined protein terminal modification
                                    if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                        for (int i = 0; i < vmodaaMass[lastAcid].size(); ++i) {
                                            double massDiffDiff = massDiff - vmodaaMass[lastAcid].get(i);
                                            double mDD = oldMass + vmodaaMass[lastAcid].get(i);
                                            double massDiffDiffAbs = Math.abs(massDiffDiff);
                                            boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                            if (computeMassValue(mDD, combinationMass) < massTolerance || wmt) {
                                                withinMass |= wmt;
                                                XmassDiff = massDiffDiffAbs;
                                                modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (computeMassValue(mDD + fmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (computeMassValue(mDD + fmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (computeMassValue(mDD + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                            withinMass |= wmtV;
                                                            XmassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (computeMassValue(mDD + vmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                            withinMass |= wmtV;
                                                            XmassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, length);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // variable undefined protein terminal modifictation
                                    if (vmod != null && modificationMatchEnd == null) {
                                        for (int i = 0; i < vmod.size(); ++i) {
                                            double massDiffDiff = massDiff - vmodMass.get(i);
                                            double mDD = oldMass + vmodMass.get(i);
                                            double massDiffDiffAbs = Math.abs(massDiffDiff);
                                            boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                            if (computeMassValue(mDD, combinationMass) < massTolerance || wmt) {
                                                withinMass |= wmt;
                                                XmassDiff = massDiffDiffAbs;
                                                modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (computeMassValue(mDD + fmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (computeMassValue(mDD + fmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (computeMassValue(mDD + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance || wmtV) {
                                                            withinMass |= wmtV;
                                                            XmassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (computeMassValue(mDD + vmodpMass.get(j), combinationMass) < massTolerance || wmtV) {
                                                            withinMass |= wmtV;
                                                            XmassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, length);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (modificationMatchEnd != null) {
                                    MatrixContent newEndCell = new MatrixContent(leftIndexOld, rightIndexOld, '\0', cell, 0, null, null, length, 0, k, modificationMatchEnd, null, -1);
                                    if (modificationMatchEndEnd == null) {
                                        matrix[k + 1].add(newEndCell);
                                    } else {
                                        MatrixContent newEndEndCell = new MatrixContent(leftIndexOld, rightIndexOld, '\0', newEndCell, 0, null, null, length, 0, k, modificationMatchEndEnd, null, -1);
                                        matrix[k + 1].add(newEndEndCell);
                                    }
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = XmassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                }
                            }
                        }
                    } else {
                        for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                            int[] borders = setCharacter[b];
                            final int aminoAcid = borders[0];
                            if (aminoAcid == '/') {
                                continue;
                            }
                            final double newMass = oldMass + (aminoAcid != 'X' ? aaMasses[borders[3]] : 0);
                            // check if not exceeding tag mass
                            if (newMass - massTolerance <= combinationMass) {
                                final int aminoAcidSearch = (borders[4] == -1) ? aminoAcid : borders[4];
                                final int lessValue = less[aminoAcidSearch];
                                final int leftIndex = lessValue + borders[1];
                                final int rightIndex = lessValue + borders[2] - 1;
                                int newNumX = cell.numX + ((aminoAcid == 'X') ? 1 : 0);
                                if (newNumX > combination.xNumLimit) {
                                    continue;
                                }
                                double massDiff = Math.abs(combinationMass - newMass);

                                // make a lookup when mass difference is below 800Da if it is still possible to reach by a AA combination
                                int intMass = (int) (massDiff * lookupMultiplier);
                                if (massDiff > massTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                    continue;
                                }
                                boolean withinMass = withinMassTolerance(massDiff, newNumX);
                                int offset = ((computeMassValue(newMass, combinationMass) <= massTolerance) ? 1 : 0) | (withinMass ? 1 : 0);

                                if (offset > 0) {
                                    newNumX = 0;
                                }
                                matrix[k + offset].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, length + 1, newNumX, borders[3], borders[4], k));
                                if (withinMass) {
                                    matrix[k + offset].getLast().XMassDiff = massDiff;
                                }
                            }
                        }
                    }

                } else {
                    final String combinationSequence = combination.sequence;
                    final int xNumLimit = combination.xNumLimit;
                    final int aminoAcid = combinationSequence.charAt(0);
                    for (int i = 0; i < combinationSequence.length(); ++i) {
                        final int aminoAcidSearch = combinationSequence.charAt(i);
                        final int lessValue = less[aminoAcidSearch];
                        final int[] range = occurrence.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcidSearch);
                        final int leftIndex = lessValue + range[0];
                        final int rightIndex = lessValue + range[1] - 1;
                        int newNumX = cell.numX + ((aminoAcidSearch == 'X') ? 1 : 0);
                        if (leftIndex <= rightIndex && newNumX <= xNumLimit) {
                            if (k < combinations.length - 1 && combinations[k].isMass != combinations[k + 1].isMass) {
                                newNumX = 0;
                            }
                            matrix[k + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, length + 1, newNumX, -1, aminoAcidSearch, k));
                        }
                    }
                }
            }
        }
    }

    /**
     * Mapping the tag elements to the reference text having mass tolerance in
     * Dalton.
     *
     * @param combinations the combinations
     * @param matrix the matrix
     * @param less the less values
     * @param occurrence the occurrence
     * @param CTermDirection the c term direction
     */
    private void mappingSequenceAndMassesDA(TagElement[] combinations, LinkedList<MatrixContent>[] matrix, int[] less, WaveletTree occurrence, boolean CTermDirection) {
        final int lenCombinations = combinations.length;
        for (int k = 0; k < lenCombinations; ++k) {
            TagElement combination = combinations[k];
            LinkedList<MatrixContent> content = matrix[k];

            while (!content.isEmpty()) {
                MatrixContent cell = content.removeFirst();
                final int length = cell.length;
                final int leftIndexOld = cell.left;
                final int rightIndexOld = cell.right;

                if (combination.isMass) {
                    final double combinationMass = combination.mass;
                    final double oldMass = cell.mass;

                    int[][] setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                    addAmbiguous(setCharacter);
                    if (withVariableModifications) {
                        addModifications(setCharacter);
                    }

                    if (k == lenCombinations - 1) {
                        for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                            int[] borders = setCharacter[b];
                            final int aminoAcid = borders[0];
                            int newNumX = cell.numX + ((aminoAcid == 'X') ? 1 : 0);
                            if (newNumX > combination.xNumLimit) {
                                continue;
                            }

                            if (aminoAcid != '/') {
                                final int aminoAcidSearch = (borders[4] == -1) ? aminoAcid : borders[4];
                                final double newMass = oldMass + (aminoAcid != 'X' ? aaMasses[borders[3]] : 0);
                                double massDiff = combinationMass - newMass;
                                int lastAcid = aminoAcid;
                                final int lessValue = less[aminoAcidSearch];
                                final int leftIndex = lessValue + borders[1];
                                final int rightIndex = lessValue + borders[2] - 1;
                                //System.out.println(k + " " + length + " " + (char)borders[0] + " " +  leftIndex + " " + rightIndex + " " + newNumX + " " + massDiff + " / " + combination.xNumLimit);
                                MatrixContent newCell = new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, length + 1, newNumX, borders[3], borders[4], k);

                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;

                                // ptm at terminus handling
                                ArrayList<String> fmodp = fmodcp;
                                ArrayList<Double> fmodpMass = fmodcpMass;
                                ArrayList<String>[] fmodpaa = fmodcpaa;
                                ArrayList<Double>[] fmodpaaMass = fmodcpaaMass;
                                ArrayList<String> vmodp = vmodcp;
                                ArrayList<Double> vmodpMass = vmodcpMass;
                                ArrayList<String>[] vmodpaa = vmodcpaa;
                                ArrayList<Double>[] vmodpaaMass = vmodcpaaMass;
                                boolean hasFixedPTM_atTerminus = hasFixedPTM_CatTerminus;

                                if (!CTermDirection) {
                                    fmodp = fmodnp;
                                    fmodpMass = fmodnpMass;
                                    fmodpaa = fmodnpaa;
                                    fmodpaaMass = fmodnpaaMass;
                                    vmodp = vmodnp;
                                    vmodpMass = vmodnpMass;
                                    vmodpaa = vmodnpaa;
                                    vmodpaaMass = vmodnpaaMass;
                                    hasFixedPTM_atTerminus = hasFixedPTM_NatTerminus;
                                }

                                boolean hasFixed = false;
                                boolean withinMass = false;
                                double XmassDiff = -1 - massTolerance;

                                // fixed aa defined peptide terminal modification
                                if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                    hasFixed = true;
                                    for (int i = 0; i < fmodpaaMass[lastAcid].size(); ++i) {
                                        double massDiffDiff = Math.abs(massDiff - fmodpaaMass[lastAcid].get(i));
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (massDiffDiffAbs < massTolerance || wmt) {
                                            withinMass |= wmt;
                                            XmassDiff = massDiffDiffAbs;
                                            modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                        }

                                        if (XmassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length + 1);
                                                }
                                            }
                                        }
                                        // variable undefined peptide terminal modifictation
                                        if (XmassDiff < -massTolerance && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), false, length + 1);
                                                }
                                            }
                                        }

                                    }
                                }

                                // fixed undefined peptide terminal modifictation
                                if (fmodp != null && XmassDiff < -massTolerance) {
                                    hasFixed = true;
                                    for (int i = 0; i < fmodp.size(); ++i) {
                                        double massDiffDiff = massDiff - fmodpMass.get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (massDiffDiffAbs < massTolerance || wmt) {
                                            withinMass |= wmt;
                                            XmassDiff = massDiffDiffAbs;
                                            modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                        }

                                        if (XmassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length + 1);
                                                }
                                            }
                                        }
                                        // variable undefined peptide terminal modifictation
                                        if (XmassDiff < -massTolerance && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), false, length + 1);
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!hasFixedPTM_atTerminus && !hasFixed && XmassDiff < -massTolerance) {
                                    // without any peptide terminal modification
                                    double massDiffF = Math.abs(massDiff);
                                    boolean wmt = withinMassTolerance(massDiffF, newNumX);
                                    if (massDiffF < massTolerance || wmt) {
                                        withinMass |= wmt;
                                        XmassDiff = massDiffF;
                                    }

                                    // variable aa defined peptide terminal modification
                                    if (XmassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                        for (int i = 0; i < vmodpaaMass[lastAcid].size(); ++i) {
                                            double massDiffV = Math.abs(massDiff - vmodpaaMass[lastAcid].get(i));
                                            boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                            if (massDiffV < massTolerance || wmtV) {
                                                withinMass |= wmtV;
                                                XmassDiff = massDiffV;
                                                modificationMatchEnd = new ModificationMatch(vmodpaa[lastAcid].get(i), true, length + 1);
                                            }
                                        }
                                    }

                                    // variable undefined peptide terminal modifictation
                                    if (XmassDiff < -massTolerance && vmodp != null) {
                                        for (int i = 0; i < vmodp.size(); ++i) {
                                            double massDiffV = Math.abs(massDiff - vmodpMass.get(i));
                                            boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                            if (massDiffV < massTolerance || wmtV) {
                                                withinMass |= wmtV;
                                                XmassDiff = massDiffV;
                                                modificationMatchEnd = new ModificationMatch(vmodp.get(i), false, length + 1);
                                            }
                                        }
                                    }
                                }

                                if (XmassDiff < -massTolerance) {
                                    if (newMass - massTolerance + negativePTMMass <= combinationMass) {
                                        content.add(newCell);
                                    }
                                } else if (modificationMatchEnd != null) {
                                    if (newNumX > 0 && !withinMass) {
                                        continue;
                                    }
                                    MatrixContent newEndCell = new MatrixContent(leftIndex, rightIndex, '\0', newCell, 0, null, null, length + 1, 0, k, modificationMatchEnd, null, -1);
                                    if (modificationMatchEndEnd == null) {
                                        matrix[k + 1].add(newEndCell);
                                    } else {
                                        MatrixContent newEndEndCell = new MatrixContent(leftIndex, rightIndex, '\0', newEndCell, 0, null, null, length + 1, 0, k, modificationMatchEndEnd, null, -1);
                                        matrix[k + 1].add(newEndEndCell);
                                    }
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = XmassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                } else {
                                    if (newNumX > 0 && !withinMass) {
                                        continue;
                                    }
                                    matrix[k + 1].add(newCell);
                                    matrix[k + 1].getLast().numX = 0;
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = XmassDiff;
                                    }
                                }

                            } else if (length > 1) {
                                int lastAcid = cell.character;
                                double massDiff = combinationMass - oldMass;
                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;
                                boolean withinMass = false;
                                double XmassDiff = -1;

                                // ptm at terminus handling
                                ArrayList<String> fmod = fmodc;
                                ArrayList<Double> fmodMass = fmodcMass;
                                ArrayList<String>[] fmodaa = fmodcaa;
                                ArrayList<Double>[] fmodaaMass = fmodcaaMass;
                                ArrayList<String> vmod = vmodc;
                                ArrayList<Double> vmodMass = vmodcMass;
                                ArrayList<String>[] vmodaa = vmodcaa;
                                ArrayList<Double>[] vmodaaMass = vmodcaaMass;
                                ArrayList<String> fmodp = fmodcp;
                                ArrayList<Double> fmodpMass = fmodcpMass;
                                ArrayList<String>[] fmodpaa = fmodcpaa;
                                ArrayList<Double>[] fmodpaaMass = fmodcpaaMass;
                                ArrayList<String> vmodp = vmodcp;
                                ArrayList<Double> vmodpMass = vmodcpMass;
                                ArrayList<String>[] vmodpaa = vmodcpaa;
                                ArrayList<Double>[] vmodpaaMass = vmodcpaaMass;

                                if (!CTermDirection) {
                                    fmod = fmodn;
                                    fmodMass = fmodnMass;
                                    fmodaa = fmodnaa;
                                    fmodaaMass = fmodnaaMass;
                                    vmod = vmodn;
                                    vmodMass = vmodnMass;
                                    vmodaa = vmodnaa;
                                    vmodaaMass = vmodnaaMass;
                                    fmodp = fmodnp;
                                    fmodpMass = fmodnpMass;
                                    fmodpaa = fmodnpaa;
                                    fmodpaaMass = fmodnpaaMass;
                                    vmodp = vmodnp;
                                    vmodpMass = vmodnpMass;
                                    vmodpaa = vmodnpaa;
                                    vmodpaaMass = vmodnpaaMass;
                                }

                                // fixed aa defined protein terminal modification
                                if (fmodaa != null && lastAcid > 0 && fmodaaMass[lastAcid].size() > 0) {
                                    for (int i = 0; i < fmodaaMass[lastAcid].size(); ++i) {
                                        double massDiffDiff = massDiff - fmodaaMass[lastAcid].get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (massDiffDiffAbs < massTolerance || wmt) {
                                            withinMass |= wmt;
                                            XmassDiff = massDiffDiffAbs;
                                            modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), true, length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), false, length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (massDiffDiffV < massTolerance || wmtV) {
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (massDiffDiffV < massTolerance || wmtV) {
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, length);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // fixed undefined protein terminal modifictation
                                if (fmod != null && modificationMatchEnd == null) {

                                    for (int i = 0; i < fmod.size(); ++i) {
                                        double massDiffDiff = massDiff - fmodMass.get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (massDiffDiffAbs < massTolerance || wmt) {
                                            withinMass |= wmt;
                                            XmassDiff = massDiffDiffAbs;
                                            modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                double massDiffV = Math.abs(massDiff - vmodaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                                if (massDiffV < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), true, length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                double massDiffV = Math.abs(massDiff - vmodMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                                if (massDiffV < massTolerance || wmtV) {
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), false, length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (massDiffDiffV < massTolerance || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    XmassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (massDiffDiffV < massTolerance || wmtV) {
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (massDiffDiffV < massTolerance || wmtV) {
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, length);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (modificationMatchEnd == null) {
                                    // variable aa defined protein terminal modification
                                    if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                        for (int i = 0; i < vmodaaMass[lastAcid].size(); ++i) {
                                            double massDiffDiff = massDiff - vmodaaMass[lastAcid].get(i);
                                            double massDiffDiffAbs = Math.abs(massDiffDiff);
                                            boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                            if (massDiffDiffAbs < massTolerance || wmt) {
                                                withinMass |= wmt;
                                                XmassDiff = massDiffDiffAbs;
                                                modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (massDiffDiffV < massTolerance || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (massDiffDiffV < massTolerance || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (massDiffDiffV < massTolerance || wmtV) {
                                                            withinMass |= wmtV;
                                                            XmassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (massDiffDiffV < massTolerance || wmtV) {
                                                            withinMass |= wmtV;
                                                            XmassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, length);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // variable undefined protein terminal modifictation
                                    if (vmod != null && modificationMatchEnd == null) {
                                        for (int i = 0; i < vmod.size(); ++i) {
                                            double massDiffDiff = massDiff - vmodMass.get(i);
                                            double massDiffDiffAbs = Math.abs(massDiffDiff);
                                            boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                            if (massDiffDiffAbs < massTolerance || wmt) {
                                                withinMass |= wmt;
                                                XmassDiff = massDiffDiffAbs;
                                                modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (massDiffDiffV < massTolerance || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (massDiffDiffV < massTolerance || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        XmassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (massDiffDiffV < massTolerance || wmtV) {
                                                            withinMass |= wmtV;
                                                            XmassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (massDiffDiffV < massTolerance || wmtV) {
                                                            withinMass |= wmtV;
                                                            XmassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, length);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (modificationMatchEnd != null) {
                                    MatrixContent newEndCell = new MatrixContent(leftIndexOld, rightIndexOld, '\0', cell, 0, null, null, length, 0, k, modificationMatchEnd, null, -1);
                                    if (modificationMatchEndEnd == null) {
                                        matrix[k + 1].add(newEndCell);
                                    } else {
                                        MatrixContent newEndEndCell = new MatrixContent(leftIndexOld, rightIndexOld, '\0', newEndCell, 0, null, null, length, 0, k, modificationMatchEndEnd, null, -1);
                                        matrix[k + 1].add(newEndEndCell);
                                    }
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = XmassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                }
                            }
                        }
                    } else {
                        for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                            int[] borders = setCharacter[b];
                            final int aminoAcid = borders[0];
                            if (aminoAcid == '/') {
                                continue;
                            }
                            final double newMass = oldMass + (aminoAcid != 'X' ? aaMasses[borders[3]] : 0);
                            // check if not exceeding tag mass
                            if (newMass - massTolerance <= combinationMass) {
                                final int aminoAcidSearch = (borders[4] == -1) ? aminoAcid : borders[4];
                                final int lessValue = less[aminoAcidSearch];
                                final int leftIndex = lessValue + borders[1];
                                final int rightIndex = lessValue + borders[2] - 1;
                                int newNumX = cell.numX + ((aminoAcid == 'X') ? 1 : 0);
                                if (newNumX > combination.xNumLimit) {
                                    continue;
                                }
                                double massDiff = Math.abs(combinationMass - newMass);

                                // make a lookup when mass difference is below 800Da if it is still possible to reach by a AA combination
                                int intMass = (int) (massDiff * lookupMultiplier);
                                if (massDiff > massTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                    continue;
                                }
                                boolean withinMass = withinMassTolerance(massDiff, newNumX);
                                int offset = ((massDiff <= massTolerance) ? 1 : 0) | (withinMass ? 1 : 0);

                                if (offset > 0) {
                                    newNumX = 0;
                                }
                                matrix[k + offset].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, length + 1, newNumX, borders[3], borders[4], k));
                                if (withinMass) {
                                    matrix[k + offset].getLast().XMassDiff = massDiff;
                                }
                            }
                        }
                    }

                } else {
                    final String combinationSequence = combination.sequence;
                    final int xNumLimit = combination.xNumLimit;
                    final int aminoAcid = combinationSequence.charAt(0);
                    for (int i = 0; i < combinationSequence.length(); ++i) {
                        final int aminoAcidSearch = combinationSequence.charAt(i);
                        final int lessValue = less[aminoAcidSearch];
                        final int[] range = occurrence.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcidSearch);
                        final int leftIndex = lessValue + range[0];
                        final int rightIndex = lessValue + range[1] - 1;
                        int newNumX = cell.numX + ((aminoAcidSearch == 'X') ? 1 : 0);
                        if (leftIndex <= rightIndex && newNumX <= xNumLimit) {
                            if (k < combinations.length - 1 && combinations[k].isMass != combinations[k + 1].isMass) {
                                newNumX = 0;
                            }
                            matrix[k + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, length + 1, newNumX, -1, aminoAcidSearch, k));
                        }
                    }
                }
            }
        }
    }

    /**
     * Mapping tags against the proteome.
     *
     * @param tag information about the identified peptide
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param massTolerance the mass tolerance
     * @return the protein mapping
     *
     * @throws IOException thrown if an IOException occurs
     * @throws InterruptedException thrown if an InterruptedException occurs
     * @throws ClassNotFoundException thrown if a ClassNotFoundException
     * @throws SQLException thrown if an SQLException occurs
     */
    @Override
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, SequenceMatchingParameters sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        return getProteinMapping(tag, sequenceMatchingPreferences);
    }

    /**
     * Mapping tags against the proteome.
     *
     * @param tag information about the identified peptide
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @return the protein mapping
     *
     * @throws IOException thrown if an IOException occurs
     * @throws InterruptedException thrown if an InterruptedException occurs
     * @throws ClassNotFoundException thrown if a ClassNotFoundException
     * @throws SQLException thrown if an SQLException occurs
     */
    @Override
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, SequenceMatchingParameters sequenceMatchingPreferences) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>();
        if (maxNumberVariants > 0 || maxNumberDeletions > 0 || maxNumberInsertions > 0 || maxNumberSubstitutions > 0) {
            for (int i = 0; i < indexParts; ++i) {
                allMatches.addAll(getProteinMappingWithVariants(tag, sequenceMatchingPreferences, i));
            }
            return allMatches;
        } else {
            for (int i = 0; i < indexParts; ++i) {
                allMatches.addAll(getProteinMappingWithoutVariants(tag, sequenceMatchingPreferences, i));
            }
            return allMatches;
        }
    }

    /**
     * Mapping tags against proteome without variants.
     *
     * @param tag the tag
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param indexPart the index part
     * @return the protein mapping
     *
     * @throws IOException thrown if an IOException occurs
     * @throws InterruptedException thrown if an InterruptedException occurs
     * @throws ClassNotFoundException thrown if a ClassNotFoundException
     * @throws SQLException thrown if an SQLException occurs
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithoutVariants(Tag tag, SequenceMatchingParameters sequenceMatchingPreferences, int indexPart) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
        WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
        int[] lessTableReversed = lessTablesReversed.get(indexPart);
        WaveletTree occurrenceTableReversed = occurrenceTablesReversed.get(indexPart);
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>();
        double xLimit = ((sequenceMatchingPreferences.getLimitX() != null) ? sequenceMatchingPreferences.getLimitX() : 1);

        // copying tags into own data structure
        int maxSequencePosition = -1;
        TagElement[] tagElements = new TagElement[tag.getContent().size()];
        for (int i = 0; i < tag.getContent().size(); ++i) {
            if (tag.getContent().get(i) instanceof MassGap) {
                tagElements[i] = new TagElement(true, "", tag.getContent().get(i).getMass(), (int) Math.round(tag.getContent().get(i).getMass() / 120. * xLimit)); // 120 is the average mass of all amino acids
            } else if (tag.getContent().get(i) instanceof AminoAcidSequence) {
                tagElements[i] = new TagElement(false, tag.getContent().get(i).asSequence(), 0., (int) (tag.getContent().get(i).asSequence().length() * xLimit));
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
        boolean hasCTermDirection = hasCTermDirectionPTM;
        boolean hasNTermDirection = hasNTermDirectionPTM;
        boolean towardsC = true;

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
            hasCTermDirection = hasNTermDirectionPTM;
            hasNTermDirection = hasCTermDirectionPTM;
            towardsC = false;
        } else {
            refTagContent = tagElements;
            lessPrimary = lessTablePrimary;
            lessReversed = lessTableReversed;
            occurrencePrimary = occurrenceTablePrimary;
            occurrenceReversed = occurrenceTableReversed;
        }

        ArrayList<MatrixContent> cached = isCached(refTagContent, indexPart);
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

        LinkedList<MatrixContent>[] matrixReversed = (LinkedList<MatrixContent>[]) new LinkedList[combinationsReversed.length + 1];
        LinkedList<MatrixContent>[] matrix = (LinkedList<MatrixContent>[]) new LinkedList[combinations.length + 1];
        ArrayList<MatrixContent> cachePrimary = new ArrayList<>();

        for (int i = 0; i <= combinationsReversed.length; ++i) {
            matrixReversed[i] = new LinkedList<>();
        }
        for (int i = 0; i <= combinations.length; ++i) {
            matrix[i] = new LinkedList<>();
        }

        if (cached != null) {
            for (MatrixContent matrixContent : cached) {
                matrix[0].add(matrixContent);
            }
        } else {
            matrixReversed[0].add(new MatrixContent(indexStringLengths.get(indexPart) - 1));
        }
        if (cached == null) {
            // Map Reverse
            if (!hasCTermDirection) {
                if (massAccuracyType == SearchParameters.MassAccuracyType.DA) {
                    mappingSequenceAndMassesDA(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed);
                } else {
                    mappingSequenceAndMassesPPM(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed);
                }
            } else {
                if (massAccuracyType == SearchParameters.MassAccuracyType.DA) {
                    mappingSequenceAndMassesDA(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed, towardsC);
                } else {
                    mappingSequenceAndMassesPPM(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed, towardsC);
                }
            }

            //System.out.println("found: " + matrixReversed[combinationsReversed.length].size());
            // Traceback Reverse
            for (MatrixContent content : matrixReversed[combinationsReversed.length]) {
                MatrixContent currentContent = content;
                String currentPeptide = "";
                String currentPeptideSearch = "";

                int leftIndexFront = 0;
                int rightIndexFront = indexStringLengths.get(indexPart) - 1;
                ArrayList<ModificationMatch> modifications = new ArrayList<>();
                ArrayList<int[]> Xcomponents = new ArrayList<>();
                HashMap<Integer, Double> XmassDiffs = new HashMap<>();

                while (currentContent.previousContent != null) {
                    final int aminoAcid = currentContent.character;
                    if (aminoAcid > 0) {
                        currentPeptide += (char) currentContent.character;
                        int c = currentContent.ambiguousChar == -1 ? aminoAcid : currentContent.ambiguousChar;
                        if (currentContent.character == 'X') {
                            Xcomponents.add(new int[]{0, currentContent.tagComponent, currentContent.length});
                            Xcomponents.get(Xcomponents.size() - 1)[2] = currentContent.length;
                        }
                        //System.out.println((char) currentContent.character);
                        currentPeptideSearch += (char) c;
                        final int lessValue = lessPrimary[c];
                        final int[] range = occurrencePrimary.singleRangeQuery(leftIndexFront - 1, rightIndexFront, c);
                        leftIndexFront = lessValue + range[0];
                        rightIndexFront = lessValue + range[1] - 1;
                    }
                    if (currentContent.XMassDiff > -1) {
                        XmassDiffs.put(currentContent.tagComponent, currentContent.XMassDiff);
                    }
                    if (currentContent.modification != null || currentContent.modificationPos >= 0) {
                        if (currentContent.modificationPos >= 0) {
                            if (modificationFlags[currentContent.modificationPos]) {
                                modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.modificationPos >= 128, currentContent.length));
                            }
                        } else {
                            modifications.add(currentContent.modification);
                        }
                    }
                    currentContent = currentContent.previousContent;
                }
                String reversePeptide = (new StringBuilder(currentPeptide).reverse()).toString();
                String reversePeptideSearch = (new StringBuilder(currentPeptideSearch).reverse()).toString();
                MatrixContent cell = new MatrixContent(leftIndexFront, rightIndexFront, reversePeptide.charAt(0), null, 0, reversePeptide, reversePeptideSearch, content.length, 0, 0, null, modifications, -1);
                cell.allXcomponents = Xcomponents;
                cell.allXMassDiffs = XmassDiffs;
                cachePrimary.add(cell);
            }

            for (MatrixContent matrixContent : cachePrimary) {
                matrix[0].add(matrixContent);
            }
            cacheIt(refTagContent, cachePrimary, indexPart);
        }

        if (!matrix[0].isEmpty()) {
            // Map towards NTerm
            if (!hasNTermDirection) {
                if (massAccuracyType == SearchParameters.MassAccuracyType.DA) {
                    mappingSequenceAndMassesDA(combinations, matrix, lessPrimary, occurrencePrimary);
                } else {
                    mappingSequenceAndMassesPPM(combinations, matrix, lessPrimary, occurrencePrimary);
                }
            } else {
                if (massAccuracyType == SearchParameters.MassAccuracyType.DA) {
                    mappingSequenceAndMassesDA(combinations, matrix, lessPrimary, occurrencePrimary, !towardsC);
                } else {
                    mappingSequenceAndMassesPPM(combinations, matrix, lessPrimary, occurrencePrimary, !towardsC);
                }
            }
        }
        // Traceback from NTerm
        for (MatrixContent content : matrix[combinations.length]) {
            MatrixContent currentContent = content;
            String currentPeptide = "";
            String currentPeptideSearch = "";
            ArrayList<ModificationMatch> modifications = new ArrayList<>();
            ArrayList<int[]> Xcomponents = new ArrayList<>();
            HashMap<Integer, Double> XmassDiffs = new HashMap<>();

            while (currentContent.previousContent != null) {
                if (currentContent.character != '\0') {
                    currentPeptide += (char) currentContent.character;
                    //System.out.println((char) currentContent.character);
                    currentPeptideSearch += (char) (currentContent.ambiguousChar == -1 ? currentContent.character : currentContent.ambiguousChar);
                    if (currentContent.character == 'X') {
                        Xcomponents.add(new int[]{1, currentContent.tagComponent, content.length - currentContent.length + 1});
                    }
                }
                if (currentContent.XMassDiff > -1) {
                    XmassDiffs.put(currentContent.tagComponent + 1024, currentContent.XMassDiff);
                }

                if (currentContent.modification != null || currentContent.modificationPos >= 0) {
                    if (currentContent.modificationPos >= 0) {
                        if (modificationFlags[currentContent.modificationPos]) {
                            modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.modificationPos >= 128, content.length - currentContent.length + 1));
                        }
                    } else {
                        modifications.add(new ModificationMatch(currentContent.modification.getModification(), currentContent.modification.getVariable(), content.length - currentContent.modification.getModificationSite() + 1));
                    }
                }

                currentContent = currentContent.previousContent;
            }
            //System.out.println();

            int leftIndex = content.left;
            int rightIndex = content.right;

            for (int[] Xcomponent : currentContent.allXcomponents) {
                Xcomponents.add(new int[]{Xcomponent[0], Xcomponent[1], Xcomponent[2] + content.length - currentContent.length});
            }

            for (Integer key : currentContent.allXMassDiffs.keySet()) {
                XmassDiffs.put(key, currentContent.allXMassDiffs.get(key));
            }

            for (ModificationMatch modificationMatch : currentContent.modifications) {
                modifications.add(new ModificationMatch(modificationMatch.getModification(), modificationMatch.getVariable(), modificationMatch.getModificationSite() + content.length - currentContent.length));
            }

            String peptide = currentPeptide + currentContent.peptideSequence;
            String peptideSearch = currentPeptideSearch + currentContent.peptideSequenceSearch;

            if (turned) {
                leftIndex = 0;
                rightIndex = indexStringLengths.get(indexPart) - 1;

                for (int p = 0; p < peptideSearch.length(); ++p) {
                    int aminoAcid = peptideSearch.charAt(p);
                    final int lessValue = lessReversed[aminoAcid];
                    final int[] range = occurrenceReversed.singleRangeQuery(leftIndex - 1, rightIndex, aminoAcid);
                    leftIndex = lessValue + range[0];
                    rightIndex = lessValue + range[1] - 1;
                }

                for (ModificationMatch modificationMatch : modifications) {
                    modificationMatch.setModificationSite(peptide.length() - modificationMatch.getModificationSite() + 1);
                }

                for (int[] Xcomponent : Xcomponents) {
                    Xcomponent[2] = peptide.length() - Xcomponent[2] + 1;
                }

                peptide = (new StringBuilder(peptide).reverse()).toString();
            }

            if (Xcomponents.isEmpty()) {
                for (int j = leftIndex; j <= rightIndex; ++j) {
                    int pos = getTextPosition(j, indexPart);
                    int index = binarySearch(boundaries.get(indexPart), pos);
                    String accession = accessions.get(indexPart)[index];
                    PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, peptide, pos - boundaries.get(indexPart)[index] + 1, modifications);
                    if (checkPTMPattern(peptideProteinMapping)) {
                        allMatches.add(peptideProteinMapping);
                    }
                }
            } else {
                // substituting all Xs if present into their corresponding amino acids
                ArrayList< ArrayList<Integer>> Xsets = new ArrayList<>();
                ArrayList< int[]> Xorigins = new ArrayList<>();

                int nTag = -1, nComponent = -1;

                for (int i = 0; i < Xcomponents.size(); ++i) {
                    if (nTag != Xcomponents.get(i)[0] || nComponent != Xcomponents.get(i)[1]) {
                        Xorigins.add(new int[]{Xcomponents.get(i)[0], Xcomponents.get(i)[1]});
                        Xsets.add(new ArrayList<>());
                        nTag = Xcomponents.get(i)[0];
                        nComponent = Xcomponents.get(i)[1];
                    }
                    Xsets.get(Xsets.size() - 1).add(Xcomponents.get(i)[2]);
                }
                LinkedList<String> substitutedPeptides = new LinkedList<>();
                LinkedList<String> substitutedPeptidesTmp = new LinkedList<>();
                substitutedPeptides.add(peptide);
                LinkedList< ArrayList<ModificationMatch>> substitutedModifications = new LinkedList<>();
                LinkedList< ArrayList<ModificationMatch>> substitutedModificationsTmp = new LinkedList<>();
                substitutedModifications.add(modifications);

                for (int k = 0; k < Xsets.size(); ++k) {
                    ArrayList<Integer> Xpositions = Xsets.get(k);
                    int len = Xpositions.size();
                    HashMap<String, int[][]> uniqueSubstitutions = new HashMap<>();

                    // find amino acids that correspond to the number of Xs and mass gap caused by all Xs
                    int[] Xranges = computeMappingRanges(XmassDiffs.get(Xorigins.get(k)[0] * 1024 + Xorigins.get(k)[1]));
                    ArrayList< int[]> possibleAAs = new ArrayList<>();
                    for (int i = Xranges[0]; i <= Xranges[1]; ++i) {
                        if (massIndexMaps.get(i).indexes.length == len) {
                            possibleAAs.add(massIndexMaps.get(i).indexes);
                        }
                    }

                    int[][] permutations = allPermutations[len];
                    for (int j = 0; j < possibleAAs.size(); ++j) {
                        for (int i = 0; i < permutations.length; ++i) {
                            String key = "";
                            for (int index : permutations[i]) {
                                key += "-" + possibleAAs.get(j)[index];
                            }
                            int[][] zipped = {permutations[i], possibleAAs.get(j)};
                            uniqueSubstitutions.put(key, zipped);
                        }
                    }

                    while (!substitutedPeptides.isEmpty()) {
                        String pep = substitutedPeptides.removeLast();
                        ArrayList<ModificationMatch> currentModifications = substitutedModifications.removeLast();
                        char[] pepChars = pep.toCharArray();
                        for (int[][] uniquePermutations : uniqueSubstitutions.values()) {

                            ArrayList<ModificationMatch> newModifications = new ArrayList<>();
                            for (ModificationMatch m : currentModifications) {
                                newModifications.add(new ModificationMatch(m.getModification(), m.getVariable(), m.getModificationSite()));
                            }

                            for (int i = 0; i < uniquePermutations[0].length; ++i) {
                                int pos = uniquePermutations[1][uniquePermutations[0][i]];
                                pepChars[Xpositions.get(i) - 1] = (char) (pos & 127);
                                if (modificationFlags[pos]) {
                                    newModifications.add(new ModificationMatch(modifictationLabels[pos], pos >= 128, Xpositions.get(i)));
                                }
                            }
                            substitutedPeptidesTmp.add(String.valueOf(pepChars));
                            substitutedModificationsTmp.add(newModifications);
                        }
                    }
                    while (!substitutedPeptidesTmp.isEmpty()) {
                        substitutedPeptides.add(substitutedPeptidesTmp.removeLast());
                        substitutedModifications.add(substitutedModificationsTmp.removeLast());
                    }
                }

                for (int i = 0; i < substitutedPeptides.size(); ++i) {
                    for (int j = leftIndex; j <= rightIndex; ++j) {
                        int pos = getTextPosition(j, indexPart);
                        int index = binarySearch(boundaries.get(indexPart), pos);
                        String accession = accessions.get(indexPart)[index];

                        // pos - boundaries.get(indexPart)[index] +1 because of start counting from one
                        PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, substitutedPeptides.get(i), pos - boundaries.get(indexPart)[index] + 1, substitutedModifications.get(i));
                        if (checkPTMPattern(peptideProteinMapping)) {
                            allMatches.add(peptideProteinMapping);
                        }
                    }
                }
            }
        }/*
        if (tag.getContent().size() == 3){
            ArrayList<TagComponent> tc = tag.getContent();
            for (PeptideProteinMapping ppm : allMatches){
                System.out.println(tc.get(0).getMass() + " " + tc.get(1).asSequence() + " " + tc.get(2).getMass() + " " + ppm.getPeptideSequence() + " " + ppm.getProteinAccession() + " " + ppm.getIndex());
            }
        }*/
        return allMatches;
    }

    /**
     * Mapping tags against proteome with variants.
     *
     * @param tag the tag
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param indexPart the index part
     * @return the protein mapping
     *
     * @throws IOException thrown if an IOException occurs
     * @throws InterruptedException thrown if an InterruptedException occurs
     * @throws ClassNotFoundException thrown if a ClassNotFoundException
     * @throws SQLException thrown if an SQLException occurs
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithVariants(Tag tag, SequenceMatchingParameters sequenceMatchingPreferences, int indexPart) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
        WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
        int[] lessTableReversed = lessTablesReversed.get(indexPart);
        WaveletTree occurrenceTableReversed = occurrenceTablesReversed.get(indexPart);
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>();

        double xLimit = ((sequenceMatchingPreferences.getLimitX() != null) ? sequenceMatchingPreferences.getLimitX() : 1);

        // copying tags into own data structure
        int maxSequencePosition = -1;
        TagElement[] tagElements = new TagElement[tag.getContent().size()];
        for (int i = 0; i < tag.getContent().size(); ++i) {
            if (tag.getContent().get(i) instanceof MassGap) {
                tagElements[i] = new TagElement(true, "", tag.getContent().get(i).getMass(), 0);
            } else if (tag.getContent().get(i) instanceof AminoAcidSequence) {
                tagElements[i] = new TagElement(false, tag.getContent().get(i).asSequence(), 0., (int) (xLimit * tag.getContent().get(i).asSequence().length()));
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
        //boolean hasCTermDirection = hasCTermDirectionPTM;
        //boolean hasNTermDirection = hasNTermDirectionPTM;
        //boolean towardsC = true;

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
            //hasCTermDirection = hasNTermDirectionPTM;
            //hasNTermDirection = hasCTermDirectionPTM;
            //towardsC = false;
        } else {
            refTagContent = tagElements;
            lessPrimary = lessTablePrimary;
            lessReversed = lessTableReversed;
            occurrencePrimary = occurrenceTablePrimary;
            occurrenceReversed = occurrenceTableReversed;
        }

        ArrayList<MatrixContent> cached = isCached(refTagContent, indexPart);
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

        int numErrors = 1 + ((genericVariantMatching) ? maxNumberVariants : maxNumberDeletions + maxNumberInsertions + maxNumberSubstitutions);

        LinkedList<MatrixContent>[][] matrixReversed = (LinkedList<MatrixContent>[][]) new LinkedList[numErrors][combinationsReversed.length + 1];
        LinkedList<MatrixContent>[][] matrix = (LinkedList<MatrixContent>[][]) new LinkedList[numErrors][combinations.length + 1];
        ArrayList<MatrixContent> cachePrimary = new ArrayList<>();

        // initializing both matrices
        for (int k = 0; k < numErrors; ++k) {
            for (int j = 0; j <= combinationsReversed.length; ++j) {
                matrixReversed[k][j] = new LinkedList<>();
            }
            for (int j = 0; j <= combinations.length; ++j) {
                matrix[k][j] = new LinkedList<>();
            }
        }

        // filling the matrices
        if (cached != null) {
            for (MatrixContent matrixContent : cached) {
                int error = genericVariantMatching ? matrixContent.numVariants : matrixContent.numSpecificVariants[0] + matrixContent.numSpecificVariants[1] + matrixContent.numSpecificVariants[2];
                matrix[error][0].add(matrixContent);
            }
        } else {
            matrixReversed[0][0].add(new MatrixContent(indexStringLengths.get(indexPart) - 1));
        }

        if (cached == null) {
            // Map Reverse
            if (genericVariantMatching) {
                mappingSequenceAndMassesWithVariantsGeneric(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed);
            } else {
                mappingSequenceAndMassesWithVariantsSpecific(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed);
            }

            // Traceback Reverse
            for (int k = 0; k < numErrors; ++k) {
                for (MatrixContent content : matrixReversed[k][combinationsReversed.length]) {
                    MatrixContent currentContent = content;
                    String currentPeptide = "";
                    int leftIndexFront = 0;
                    int rightIndexFront = indexStringLengths.get(indexPart) - 1;
                    ArrayList<ModificationMatch> modifications = new ArrayList<>();
                    String allVariants = "";

                    while (currentContent.previousContent != null) {
                        int aminoAcidPep = currentContent.character;
                        int aminoAcidProt = currentContent.character;
                        int edit = currentContent.variant;
                        boolean update = true;

                        if (edit != '-') {
                            if (edit == '*') {
                                update = false; // insertion
                            } else if ('A' <= edit && edit <= 'Z') {
                                aminoAcidProt = edit; // substitution
                            } else if ('a' <= edit && edit <= 'z') {
                                aminoAcidProt = edit - 32; // deletion
                            }
                        }

                        if (aminoAcidPep > 0) {
                            currentPeptide += (char) aminoAcidPep;
                            allVariants += (char) edit;
                            if (update) {
                                final int lessValue = lessPrimary[aminoAcidProt];
                                final int[] range = occurrencePrimary.singleRangeQuery(leftIndexFront - 1, rightIndexFront, aminoAcidProt);
                                leftIndexFront = lessValue + range[0];
                                rightIndexFront = lessValue + range[1] - 1;
                            }
                        }

                        if (currentContent.modification != null || currentContent.modificationPos >= 0) {
                            if (currentContent.modificationPos >= 0) {
                                if (modificationFlags[currentContent.modificationPos]) {
                                    modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.modificationPos >= 128, currentContent.length));
                                }
                            } else {
                                modifications.add(currentContent.modification);
                            }
                        }
                        currentContent = currentContent.previousContent;
                    }

                    String reversePeptide = (new StringBuilder(currentPeptide).reverse()).toString();
                    allVariants = (new StringBuilder(allVariants).reverse()).toString();
                    if (genericVariantMatching) {
                        cachePrimary.add(new MatrixContent(leftIndexFront, rightIndexFront, reversePeptide.charAt(0), null, 0, reversePeptide, content.length, 0, null, modifications, -1, k, '\0', allVariants));
                    } else {
                        cachePrimary.add(new MatrixContent(leftIndexFront, rightIndexFront, reversePeptide.charAt(0), null, 0, reversePeptide, content.length, 0, null, modifications, -1, new int[]{content.numSpecificVariants[0], content.numSpecificVariants[1], content.numSpecificVariants[2]}, '\0', allVariants));
                    }
                }
            }

            for (MatrixContent matrixContent : cachePrimary) {
                int error = genericVariantMatching ? matrixContent.numVariants : matrixContent.numSpecificVariants[0] + matrixContent.numSpecificVariants[1] + matrixContent.numSpecificVariants[2];
                matrix[error][0].add(matrixContent);
            }
            cacheIt(refTagContent, cachePrimary, indexPart);

        }

        if (genericVariantMatching) {
            mappingSequenceAndMassesWithVariantsGeneric(combinations, matrix, lessPrimary, occurrencePrimary);
        } else {
            mappingSequenceAndMassesWithVariantsSpecific(combinations, matrix, lessPrimary, occurrencePrimary);
        }

        // Traceback from NTerm
        for (int k = 0; k < numErrors; ++k) {
            for (MatrixContent content : matrix[k][combinations.length]) {
                MatrixContent currentContent = content;
                String currentPeptide = "";
                ArrayList<ModificationMatch> modifications = new ArrayList<>();
                String allVariants = "";

                while (currentContent.previousContent != null) {
                    int aminoAcid = currentContent.character;
                    if (aminoAcid > 0) {
                        currentPeptide += (char) aminoAcid;
                        allVariants += (char) currentContent.variant;
                    }

                    if (currentContent.modification != null || currentContent.modificationPos >= 0) {
                        if (currentContent.modificationPos >= 0) {
                            if (modificationFlags[currentContent.modificationPos]) {
                                modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.modificationPos >= 128, content.length - currentContent.length + 1));
                            }
                        } else {
                            modifications.add(new ModificationMatch(currentContent.modification.getModification(), currentContent.modification.getVariable(), currentContent.modification.getModificationSite() + content.length - currentContent.length + 1));
                        }
                    }

                    currentContent = currentContent.previousContent;
                }

                int leftIndex = content.left;
                int rightIndex = content.right;

                for (ModificationMatch modificationMatch : currentContent.modifications) {
                    modifications.add(new ModificationMatch(modificationMatch.getModification(), modificationMatch.getVariable(), modificationMatch.getModificationSite() + content.length - currentContent.length));
                }

                String peptide = currentPeptide + currentContent.peptideSequence;
                allVariants += currentContent.allVariants;

                HashMap<Integer, Variant> variants = new HashMap<>(0);
                int lengthDifference = 0;

                if (turned) {
                    leftIndex = 0;
                    rightIndex = indexStringLengths.get(indexPart) - 1;

                    for (int p = 0; p < peptide.length(); ++p) {
                        boolean update = true;
                        int aminoAcid = peptide.charAt(p);
                        int edit = allVariants.charAt(p);
                        if (edit != '-') {
                            if (edit == '*') {
                                update = false; // insertion
                            } else if ('A' <= edit && edit <= 'Z') {
                                aminoAcid = edit; // substitution
                            } else if ('a' <= edit && edit <= 'z') {
                                aminoAcid = edit - 32; // deletion
                            }
                        }

                        if (update) {
                            final int lessValue = lessReversed[aminoAcid];
                            final int[] range = occurrenceReversed.singleRangeQuery(leftIndex - 1, rightIndex, aminoAcid);
                            leftIndex = lessValue + range[0];
                            rightIndex = lessValue + range[1] - 1;
                        }
                    }

                    for (ModificationMatch modificationMatch : modifications) {
                        modificationMatch.setModificationSite(peptide.length() - modificationMatch.getModificationSite() + 1);
                    }

                    allVariants = (new StringBuilder(allVariants).reverse()).toString();
                    peptide = (new StringBuilder(peptide).reverse()).toString();
                }

                // adding variants and adjusting modification sites
                for (int i = 0, length = 0; i < allVariants.length(); ++i) {
                    int edit = allVariants.charAt(i);
                    ++length;
                    if (edit != '-') {
                        if (edit == '*') { // insertion
                            Variant variant = new Insertion(peptide.charAt(length - 1));
                            variants.put(length, variant);
                            lengthDifference--;
                        } else if ('A' <= edit && edit <= 'Z') { // substitution
                            Variant variant = new Substitution((char) edit, peptide.charAt(length - 1));
                            variants.put(length, variant);
                        } else if ('a' <= edit && edit <= 'z') { // deletion
                            Variant variant = new Deletion((char) (edit - 32));
                            variants.put(length, variant);
                            lengthDifference++;
                            --length;
                        }
                    }
                }

                String cleanPeptide = peptide.replace("*", "");
                for (int j = leftIndex; j <= rightIndex; ++j) {
                    int pos = getTextPosition(j, indexPart);
                    int index = binarySearch(boundaries.get(indexPart), pos);
                    String accession = accessions.get(indexPart)[index];

                    int startPosition = pos - boundaries.get(indexPart)[index];
                    boolean newPeptide = true;

                    for (PeptideProteinMapping ppm : allMatches) {
                        if (ppm.getProteinAccession().equals(accession) && ppm.getPeptideSequence().equals(cleanPeptide) && Math.abs(ppm.getIndex() - startPosition) <= numErrors) {
                            newPeptide = false;
                            break;
                        }
                    }

                    if (newPeptide) {

                        PeptideVariantMatches peptideVariantMatches = variants.isEmpty() ? null : new PeptideVariantMatches(variants, lengthDifference);

                        // startPosition +1 because of start counting from one
                        PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, cleanPeptide, startPosition + 1, modifications, peptideVariantMatches);
                        if (checkPTMPattern(peptideProteinMapping)) {
                            allMatches.add(peptideProteinMapping);
                        }
                    }

                }
            }
        }

        /*
        if (tag.getContent().size() == 3){
            ArrayList<TagComponent> tc = tag.getContent();

            double tagmass = tc.get(0).getMass() + pepMass(tc.get(1).asSequence()) + tc.get(2).getMass();

            for (PeptideProteinMapping ppm : allMatches){
                System.out.println(tc.get(0).getMass() + " " + tc.get(1).asSequence() + " " + tc.get(2).getMass() + " " + ppm.getPeptideSequence() + " " + ppm.getProteinAccession() + " " + ppm.getIndex() + " | " + tagmass + " " + pepMass(ppm.getPeptideSequence()));
            }
            if (allMatches.isEmpty()){
            }
        }
         */
        return allMatches;
    }

    /**
     * Simplified class for tag elements.
     */
    private class TagElement {

        boolean isMass;
        String sequence;
        double mass;
        int xNumLimit;

        /**
         * Constructor.
         *
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

        /**
         * Constructor.
         *
         * @param isMass
         * @param sequence
         * @param mass
         */
        TagElement(boolean isMass, String sequence, double mass) {
            this.isMass = isMass;
            this.sequence = sequence;
            this.mass = mass;
            this.xNumLimit = 0;
        }

        /**
         * Creating String output.
         *
         * @return String output
         */
        @Override
        public String toString() {
            String output;
            if (isMass) {
                output = String.format("%.5f", mass);
            } else {
                output = sequence;
            }
            return output;
        }
    }

    /**
     * Class for caching intermediate tag to proteome mapping results.
     */
    private class CacheElement {

        Double massFirst;
        String sequence;
        Double massSecond;
        ArrayList<MatrixContent> cachedPrimary;

        /**
         * Constructor
         *
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
     * List of cached intermediate tag to proteome mapping results.
     */
    private HashMap<String, CacheElement>[] cache = null;

    /**
     * Adding intermediate tag to proteome mapping results into the cache.
     *
     * @param tagComponents the tag components
     * @param indexPart the part of the index
     *
     * @return a list of cached elements
     */
    private ArrayList<MatrixContent> isCached(TagElement[] tagComponents, int indexPart) {
        if (tagComponents.length != 3 || !tagComponents[0].isMass || tagComponents[1].isMass || !tagComponents[2].isMass) {
            return null;
        }
        ArrayList<MatrixContent> cached = null;

        try {
            cacheMutex.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String key = tagComponents[1].sequence + String.format("%.5f", tagComponents[2].mass);
        CacheElement cacheElement = cache[indexPart].get(key);
        if (cacheElement != null) {
            cached = cacheElement.cachedPrimary;
        }
        cacheMutex.release();
        return cached;
    }

    /**
     * Caching intermediate results of previous tag to proteome matches.
     *
     * @param tagComponents
     * @param cachedPrimary
     */
    private void cacheIt(TagElement[] tagComponents, ArrayList<MatrixContent> cachedPrimary, int indexPart) {
        if (tagComponents.length != 3 || !tagComponents[0].isMass || tagComponents[1].isMass || !tagComponents[2].isMass) {
            return;
        }
        try {
            cacheMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<MatrixContent> cacheContentPrimary = new ArrayList<>();
        for (MatrixContent matrixContent : cachedPrimary) {
            cacheContentPrimary.add(new MatrixContent(matrixContent));
        }

        String key = tagComponents[1].sequence + String.format("%.5f", tagComponents[2].mass);
        CacheElement cacheElement = new CacheElement(tagComponents[0].mass, tagComponents[1].sequence, tagComponents[2].mass, cacheContentPrimary);
        if (!cache[indexPart].containsKey(key)) {
            cache[indexPart].put(key, cacheElement);
        }
        cacheMutex.release();
    }
    
    
    @Override
    public String getSequence(String proteinAccession) {
        
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
