package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.Util;
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
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideVariantMatches;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.utils.ProteinUtils;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.experiment.io.biology.protein.Header;
import com.compomics.util.experiment.io.biology.protein.ProteinDatabase;
import com.compomics.util.experiment.io.biology.protein.ProteinDetailsProvider;
import com.compomics.util.experiment.io.biology.protein.ProteinIterator;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;
import com.compomics.util.parameters.identification.advanced.PeptideVariantsParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import org.jsuffixarrays.*;
import com.compomics.util.experiment.identification.protein_inference.FastaMapper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.Collectors;

/**
 * The FM index.
 *
 * @author Dominik Kopczynski
 * @author Marc Vaudel
 */
public class FMIndex implements FastaMapper, SequenceProvider, ProteinDetailsProvider {

    /**
     * Semaphore for caching.
     */
    static Object cacheMutex = new Object();
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
    private ArrayList<int[]> suffixArraysPrimary = new ArrayList<>();
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
    private ArrayList<int[]> boundaries = new ArrayList<>();
    /**
     * List of all accession IDs in the FASTA file.
     */
    private ArrayList<String[]> accessions = new ArrayList<>();
    /**
     * The decoy accessions contained in this index.
     */
    private HashSet<String> decoyAccessions = new HashSet<>();
    /**
     * The accessions ending positions in the index, important for getSequences
     * function.
     */
    private HashMap<String, AccessionMetaData> accessionMetaData = new HashMap<>();
    /**
     * List of all amino acid masses.
     */
    private double[] aaMasses = null;
    /**
     * List of all indexes for valid amino acid masses.
     */
    private int[] aaMassIndexes = null;
    /**
     * List of all indexes for valid amino acid masses.
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
     * Characters that can be substituted by B.
     */
    private final int[] BSubstitutions = new int[]{'D', 'N'};
    /**
     * Characters that can be substituted by J.
     */
    private final int[] JSubstitutions = new int[]{'I', 'L'};
    /**
     * Characters that can be substituted by Z.
     */
    private final int[] ZSubstitutions = new int[]{'E', 'Q'};

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

    private boolean hasCTermDirectionModification = false;
    private boolean hasNTermDirectionModification = false;

    private boolean hasModificationatTerminus = false;

    private boolean hasFixedModification_CatTerminus = false;
    private boolean hasFixedModification_NatTerminus = false;

    private double negativeModificationMass = 0;
    
    
    
    private final ArrayList<Rank> variantBitsPrimary = new ArrayList<>();
    private final ArrayList<Rank> variantBitsReversed = new ArrayList<>();
    
    private final ArrayList<HashSet<int[]>[]> variantsPrimary = new ArrayList<>();
    private final ArrayList<HashSet<int[]>[]> variantsReversed = new ArrayList<>();
    
    HashMap<String, ArrayList<SNPElement>> SNPs = new HashMap<>();

    /**
     * Either search with one maximal number of variants or use an upper limit
     * for every variant (insertion / deletion / substitution)
     */
    int variantMatchingType = 0;
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
    double lookupMultiplier = 1000;
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
    private static final double LOOKUP_MAX_MASS = 800;
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
     * List for patterns of complex modifications.
     */
    ArrayList<Long[]> modificationPatterns = new ArrayList<>(2);
    /**
     * Mapping dictionary mod name -&gt; (modificationPattern index, relative
     * starting position, pattern length).
     */
    HashMap<String, int[]> modificationPatternNames = new HashMap<>(2);
    /**
     * Longest modification pattern.
     */
    int longestModificationpattern = 0;
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
     * Arraylist for sorted masses to index mappings.
     */
    ArrayList<MassIndexMap> massIndexMaps = null;

    /**
     * Empty default constructor.
     */
    public FMIndex() {
    }

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
     * Adds a modification pattern for bitwise pattern search.
     *
     * @param modification modification object
     */
    public void addModificationPattern(Modification modification) {

        AminoAcidPattern aap = modification.getPattern();
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
        keySet.stream().forEach((key) -> {
            aaTargered.get(key).stream().forEach((c) -> {
                masks[c] |= 1L << (key - startPos);
            });
        });

        modificationPatternNames.put(modification.getName(), new int[]{modificationPatterns.size(), startPos, patternLength});
        modificationPatterns.add(masks);
        longestModificationpattern = Math.max(longestModificationpattern, patternLength);

    }

    /**
     * Checking if peptide-protein should be discarded due to pattern
     * modification conflict.
     *
     * @param peptideProteinMapping the peptide protein mapping
     * @return either yes or no
     */
    public boolean checkModificationPattern(PeptideProteinMapping peptideProteinMapping) {

        if (modificationPatterns.isEmpty()) {

            return true;

        }

        // prepare text for pattern search
        String searchText = peptideProteinMapping.getPeptideSequence();
        // TODO: extend searchText for length of longest modification pattern in both directions

        for (ModificationMatch modificationMatch : peptideProteinMapping.getVariableModifications()) {
            if (modificationPatternNames.containsKey(modificationMatch.getModification())) {
                int[] ptmPatternData = modificationPatternNames.get(modificationMatch.getModification()); //(ptmPattern index, starting position, pattern length)
                Long[] masks = modificationPatterns.get(ptmPatternData[0]);
                int textPos = modificationMatch.getSite() - 1;
                if (textPos + ptmPatternData[1] < 0) {
                    return false; // TODO: handle this
                }
                if (textPos + ptmPatternData[1] + ptmPatternData[2] > searchText.length()) {
                    return false; // TODO: handle this
                }
                // use shift-and algorithm for pattern search
                long pattern = 1L;
                for (int i = textPos + ptmPatternData[1], j = 0; j < ptmPatternData[2] && pattern != 0; ++i, ++j) {
                    pattern = (pattern & masks[searchText.charAt(i)]) << 1;
                }
                return ((1L << ptmPatternData[2]) & pattern) > 0L;
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
            bytes += occurrenceTablesPrimary.get(indexPart).getAllocatedBytes() + occurrenceTablesReversed.get(indexPart).getAllocatedBytes() + suffixArraysPrimary.get(indexPart).length * 4; // four bytes per int, hopefully

            String[] accessionsPart = accessions.get(indexPart);
            for (int j = 0; j < accessionsPart.length; ++j) {
                bytes += accessionsPart[j].length() * 2; // two bytes per character, don't ask me, Java.
            }
            for (String accKey : accessionMetaData.keySet()){
                bytes += accKey.length() * 2;
                bytes += accessionMetaData.get(accKey).getHeaderAsString().length() * 2;
            }
            bytes += suffixArraysPrimary.get(indexPart).length * 4;
        }
        return bytes;
    }
    
    
    

    /**
     * Constructor. If modification settings are provided the index will contain
     * modification information, ignored if null.
     *
     * @param fastaFile the FASTA file to index
     * @param fastaParameters the parameters for the FASTA file parsing
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param peptideVariantsPreferences contains all parameters for variants
     * @param searchParameters the search parameters
     *
     * @throws IOException exception thrown if an error occurs while iterating
     * the FASTA file.
     */
    public FMIndex(File fastaFile, FastaParameters fastaParameters, WaitingHandler waitingHandler, boolean displayProgress, PeptideVariantsParameters peptideVariantsPreferences, SearchParameters searchParameters) throws IOException {
        if (searchParameters != null) {
            massTolerance = searchParameters.getFragmentIonAccuracy();
            massAccuracyType = searchParameters.getFragmentAccuracyType();
            init(fastaFile, fastaParameters, waitingHandler, displayProgress, searchParameters.getModificationParameters(), peptideVariantsPreferences);
        } else {
            init(fastaFile, fastaParameters, waitingHandler, displayProgress, null, peptideVariantsPreferences);
        }
    }

    /**
     * Constructor. If modification settings are provided the index will contain
     * modification information, ignored if null.
     *
     * @param fastaFile the FASTA file to index
     * @param fastaParameters the parameters for the FASTA file parsing
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param modificationSettings contains modification parameters for
     * identification
     * @param peptideVariantsPreferences contains all parameters for variants
     *
     * @throws IOException exception thrown if an error occurs while iterating
     * the FASTA file
     */
    public FMIndex(File fastaFile, FastaParameters fastaParameters, WaitingHandler waitingHandler, boolean displayProgress, ModificationParameters modificationSettings, PeptideVariantsParameters peptideVariantsPreferences) throws IOException {
        init(fastaFile, fastaParameters, waitingHandler, displayProgress, modificationSettings, peptideVariantsPreferences);
    }

    /**
     * Init function only called by the constructors. If modification settings
     * are provided the index will contain modification information, ignored if
     * null.
     *
     * @param fastaFile the FASTA file to index
     * @param fastaParameters the parameters for the FASTA file parsing
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param modificatoinSettings contains modification parameters for
     * identification
     * @param peptideVariantsPreferences contains all parameters for variants
     * @param massTolerance the mass tolerance
     *
     * @throws IOException exception thrown if an error occurs while iterating
     * the fasta file.
     */
    private void init(File fastaFile, FastaParameters fastaParameters, WaitingHandler waitingHandler, boolean displayProgress, ModificationParameters modificatoinSettings, PeptideVariantsParameters peptideVariantsPreferences) throws IOException {

        // load all variant preferences
        maxNumberVariants = peptideVariantsPreferences.getnVariants();
        variantMatchingType = peptideVariantsPreferences.getUseSpecificCount() ? 1 : 0;
        maxNumberInsertions = peptideVariantsPreferences.getnAaInsertions();
        maxNumberDeletions = peptideVariantsPreferences.getnAaDeletions();
        maxNumberSubstitutions = peptideVariantsPreferences.getnAaSubstitutions();
        
        
        // example for adding proteomic SNPs into the index, here for a 
        // mouse proteome
        /*
        variantMatchingType = 2;
        SNPs.put("Q925J9", new ArrayList<>());
        SNPs.get("Q925J9").add(new SNPElement(587, 'V', 'S'));
        
        SNPs.put("Q91YI4", new ArrayList<>());
        SNPs.get("Q91YI4").add(new SNPElement(35, 'A', 'D'));
        
        SNPs.put("Q91VD9", new ArrayList<>());
        SNPs.get("Q91VD9").add(new SNPElement(518, 'G', 'I'));
        
        SNPs.put("Q9WUA5", new ArrayList<>());
        SNPs.get("Q9WUA5").add(new SNPElement(14, 'Y', 'G'));
        
        SNPs.put("Q7TN99", new ArrayList<>());
        SNPs.get("Q7TN99").add(new SNPElement(292, 'E', 'L'));
        
        SNPs.put("Q99LC3", new ArrayList<>());
        SNPs.get("Q99LC3").add(new SNPElement(145, 'C', 'A'));
        */

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
        if (modificatoinSettings != null) {
            // create masses table and modifications
            int[] modificationCounts = new int[128];
            for (int i = 0; i < modificationCounts.length; ++i) {
                modificationCounts[i] = 0;
            }
            ArrayList<String> variableModifications = modificatoinSettings.getVariableModifications();
            ArrayList<String> fixedModifications = modificatoinSettings.getFixedModifications();
            ModificationFactory ptmFactory = ModificationFactory.getInstance();

            int hasVariableModification = 0;

            // check which amino acids have variable modificatitions
            for (String modification : variableModifications) {
                Modification ptm = ptmFactory.getModification(modification);
                ArrayList<Character> targets;
                switch (ptm.getModificationType()) {
                    case modaa:
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
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
                            hasCTermDirectionModification = true;
                            hasModificationatTerminus = true;
                        }
                        vmodc.add(modification);
                        vmodcMass.add(ptm.getMass());
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            hasCTermDirectionModification = true;
                            hasModificationatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            vmodcaa[c].add(modification);
                            vmodcaaMass[c].add(ptm.getMass());
                        }
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
                        break;

                    case modc_peptide:
                        if (vmodcp == null) {
                            vmodcp = new ArrayList<>();
                            vmodcpMass = new ArrayList<>();
                            hasCTermDirectionModification = true;
                        }
                        vmodcp.add(modification);
                        vmodcpMass.add(ptm.getMass());
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            hasCTermDirectionModification = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            vmodcpaa[c].add(modification);
                            vmodcpaaMass[c].add(ptm.getMass());
                        }
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
                        break;

                    case modn_protein:
                        if (vmodn == null) {
                            vmodn = new ArrayList<>();
                            vmodnMass = new ArrayList<>();
                            hasNTermDirectionModification = true;
                            hasModificationatTerminus = true;
                        }
                        vmodn.add(modification);
                        vmodnMass.add(ptm.getMass());
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            hasNTermDirectionModification = true;
                            hasModificationatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            vmodnaa[c].add(modification);
                            vmodnaaMass[c].add(ptm.getMass());
                        }
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
                        break;

                    case modn_peptide:
                        if (vmodnp == null) {
                            vmodnp = new ArrayList<>();
                            vmodnpMass = new ArrayList<>();
                            hasNTermDirectionModification = true;
                        }
                        vmodnp.add(modification);
                        vmodnpMass.add(ptm.getMass());
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            hasNTermDirectionModification = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            vmodnpaa[c].add(modification);
                            vmodnpaaMass[c].add(ptm.getMass());
                        }
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            aaMasses[c] += ptm.getMass();
                            negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
                            // modifictationLabels[c] = modification;
                            // modificationFlags[c] = true;
                        }
                        break;

                    case modc_protein:
                        if (fmodc == null) {
                            fmodc = new ArrayList<>();
                            fmodcMass = new ArrayList<>();
                            hasCTermDirectionModification = true;
                            hasModificationatTerminus = true;
                            hasFixedModification_CatTerminus = true;
                        }
                        fmodc.add(modification);
                        fmodcMass.add(ptm.getMass());
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            hasCTermDirectionModification = true;
                            hasModificationatTerminus = true;
                            hasFixedModification_CatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            fmodcaa[c].add(modification);
                            fmodcaaMass[c].add(ptm.getMass());
                        }
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
                        break;

                    case modc_peptide:
                        if (fmodcp == null) {
                            fmodcp = new ArrayList<>();
                            fmodcpMass = new ArrayList<>();
                            hasCTermDirectionModification = true;
                        }
                        fmodcp.add(modification);
                        fmodcpMass.add(ptm.getMass());
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            hasCTermDirectionModification = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            fmodcpaa[c].add(modification);
                            fmodcpaaMass[c].add(ptm.getMass());
                        }
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
                        break;

                    case modn_protein:
                        if (fmodn == null) {
                            fmodn = new ArrayList<>();
                            fmodnMass = new ArrayList<>();
                            hasNTermDirectionModification = true;
                            hasModificationatTerminus = true;
                            hasFixedModification_NatTerminus = true;
                        }
                        fmodn.add(modification);
                        fmodnMass.add(ptm.getMass());
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            hasNTermDirectionModification = true;
                            hasModificationatTerminus = true;
                            hasFixedModification_NatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            fmodnaa[c].add(modification);
                            fmodnaaMass[c].add(ptm.getMass());
                        }
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
                        break;

                    case modn_peptide:
                        if (fmodnp == null) {
                            fmodnp = new ArrayList<>();
                            fmodnpMass = new ArrayList<>();
                            hasNTermDirectionModification = true;
                        }
                        fmodnp.add(modification);
                        fmodnpMass.add(ptm.getMass());
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
                            hasNTermDirectionModification = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            addModificationPattern(ptm);
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        for (Character c : targets) {
                            fmodnpaa[c].add(modification);
                            fmodnpaaMass[c].add(ptm.getMass());
                        }
                        negativeModificationMass = Math.min(negativeModificationMass, ptm.getMass());
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
        
        // check if fasta file has an FMIndex
        String fastaExtension = getFileExtension(fastaFile);
        File FMFile = new File(fastaFile.getAbsolutePath().replace(fastaExtension, ".fmi"));
        boolean loadFasta = true;
        if (FMFile.exists()){
            
            DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(FMFile.getAbsolutePath()), 1024 * 1024));
            ObjectInputStream ois = null;
            
            try{
                ois = new ObjectInputStream(is);
                String indexVersion = ois.readUTF();
                if (indexVersion.equals(Util.getVersion())){
                    indexParts = ois.readInt();
                    indexStringLengths = (ArrayList<Integer>)ois.readObject();
                    suffixArraysPrimary = (ArrayList<int[]>)ois.readObject();
                    occurrenceTablesPrimary = (ArrayList<WaveletTree>)ois.readObject();
                    occurrenceTablesReversed = (ArrayList<WaveletTree>)ois.readObject();
                    lessTablesPrimary = (ArrayList<int[]>)ois.readObject();
                    lessTablesReversed = (ArrayList<int[]>)ois.readObject();
                    boundaries = (ArrayList<int[]>)ois.readObject();
                    accessions = (ArrayList<String[]>)ois.readObject();
                    decoyAccessions = (HashSet<String>)ois.readObject();
                    accessionMetaData = (HashMap<String, AccessionMetaData>)ois.readObject();
                    
                    for(WaveletTree wt : occurrenceTablesPrimary) wt.setNumMasses(numMasses);
                    for(WaveletTree wt : occurrenceTablesReversed) wt.setNumMasses(numMasses);
                    
                    loadFasta = false;
                }
            }
            catch(Exception e){
            }
            finally {
                if (ois != null) ois.close();
            }
        }
        if (loadFasta) {

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

            int maxProgressBar = 11 * tmpLengths.size();

            if (waitingHandler != null && displayProgress && !waitingHandler.isRunCanceled()) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxSecondaryProgressCounter(maxProgressBar);
                waitingHandler.setSecondaryProgressCounter(0);
            }

            pi = new FastaIterator(fastaFile);
            for (int i = 0; i < tmpLengths.size(); ++i) {
                addDataToIndex(pi, tmpLengths.get(i), tmpProteins.get(i), alphabet, fastaParameters, waitingHandler, displayProgress);
            }
        }

        int lookupLength = ((int) ((LOOKUP_MAX_MASS + computeInverseMassValue(massTolerance, LOOKUP_MAX_MASS)) * lookupMultiplier));
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
        
        
        
        // create SNP variants
        if (variantMatchingType == 2){
            for (int prt = 0; prt < indexParts; ++prt){
            
                // create forward variants
                int[] lessTablePrimary = lessTablesPrimary.get(prt);
                WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(prt);
            
                int indexStringLength = occurrenceTablePrimary.getLength();
                
                long[] variantPrimBits = new long[(indexStringLength >>> 6) + 1];
                long[] variantRevBits = new long[(indexStringLength >>> 6) + 1];
                HashSet<int[]>[] variantsPrimTmp = (HashSet<int[]>[]) new HashSet[indexStringLength];
                HashSet<int[]>[] variantsPrim = null;
                HashSet<int[]>[] variantsRevTmp = (HashSet<int[]>[]) new HashSet[indexStringLength];
                HashSet<int[]>[] variantsRev = null;
                int numVariants = 0;
                
                // reconstruct the suffix array
                int[] SA = new int[indexStringLength];
                SA[0] = indexStringLength - 1;
                int idx = 0;
                int sa = SA[0];
                while (true) {
                    int[] aminoInfo = occurrenceTablePrimary.getCharacterInfo(idx);
                    idx = lessTablePrimary[aminoInfo[0]] + aminoInfo[1];
                    if (idx == 0) break;
                    SA[idx] = --sa;
                }
                lessTablePrimary = null;
                occurrenceTablePrimary = null;
                
                // create inverse suffix array
                int[] inversedSampledSuffixArray = new int[indexStringLength];
                for (int i = 0; i < indexStringLength; ++i) {
                    inversedSampledSuffixArray[SA[i]] = i;
                }
                SA = null;
                for (String accession : accessions.get(prt)) {
                    
                    // adding variants
                    if (variantMatchingType == 2 && SNPs.containsKey(accession)){
                        for (SNPElement SNP : SNPs.get(accession)){
                            int posSNP = inversedSampledSuffixArray[accessionMetaData.get(accession).trueBeginning + SNP.position];
                            variantPrimBits[posSNP >>> 6] |= 1L << (posSNP & 63);
                            if (variantsPrimTmp[posSNP] == null) variantsPrimTmp[posSNP] = new HashSet<>();
                            variantsPrimTmp[posSNP].add(new int[]{SNP.sourceAA, SNP.targetAA});
                            numVariants += 1;
                        }
                    }
                }
                inversedSampledSuffixArray = null;
                variantsPrim = (HashSet<int[]>[]) new HashSet[numVariants];
                for (int i = 0, j = 0; i < indexStringLength; ++i){
                    if (variantsPrimTmp[i] != null){
                        variantsPrim[j++] = variantsPrimTmp[i];
                    }
                }
                variantBitsPrimary.add(new Rank(variantPrimBits, indexStringLength));
                variantsPrimary.add(variantsPrim);
                
                
                
                
                
                // create backward variants
                
                
                int[] lessTableReversed = lessTablesReversed.get(prt);
                WaveletTree occurrenceTableReversed = occurrenceTablesReversed.get(prt);
                idx = 0;
                SA = new int[indexStringLength];
                SA[0] = indexStringLength - 1;
                sa = SA[0];
                while (true) {
                    int[] aminoInfo = occurrenceTableReversed.getCharacterInfo(idx);
                    idx = lessTableReversed[aminoInfo[0]] + aminoInfo[1];
                    if (idx == 0) break;
                    SA[idx] = --sa;
                }
                lessTableReversed = null;
                occurrenceTableReversed = null;
                
                // compute reversed variant positions
                inversedSampledSuffixArray = new int[indexStringLength];
                for (int i = 0; i < indexStringLength; ++i) {
                    inversedSampledSuffixArray[SA[i]] = i;
                }
                for (String accession : accessions.get(prt)) {
                
                    // adding variants
                    if (SNPs.containsKey(accession)){
                        for (SNPElement SNP : SNPs.get(accession)){
                            int posSNP = inversedSampledSuffixArray[indexStringLength - 2 - (accessionMetaData.get(accession).trueBeginning + SNP.position)];
                            variantRevBits[posSNP >>> 6] |= 1L << (posSNP & 63);
                            if (variantsRevTmp[posSNP] == null) variantsRevTmp[posSNP] = new HashSet<>();
                            variantsRevTmp[posSNP].add(new int[]{SNP.sourceAA, SNP.targetAA});
                        }
                    }
                }

                variantsRev = (HashSet<int[]>[]) new HashSet[numVariants];
                for (int i = 0, j = 0; i < indexStringLength; ++i){
                    if (variantsRevTmp[i] != null){
                        variantsRev[j++] = variantsRevTmp[i];
                    }
                }
                
                variantBitsReversed.add(new Rank(variantRevBits, indexStringLength));
                variantsReversed.add(variantsRev);
            }
        }
        
        
        
        if (loadFasta){
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(FMFile.getAbsolutePath())));
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeUTF(Util.getVersion());
            oos.writeInt(indexParts);
            oos.writeObject(indexStringLengths);
            oos.writeObject(suffixArraysPrimary);
            oos.writeObject(occurrenceTablesPrimary);
            oos.writeObject(occurrenceTablesReversed);
            oos.writeObject(lessTablesPrimary);
            oos.writeObject(lessTablesReversed);
            oos.writeObject(boundaries);
            oos.writeObject(accessions);
            oos.writeObject(decoyAccessions);
            oos.writeObject(accessionMetaData);
            oos.close();
        }
    }

    /**
     * Add data to index.
     *
     * @param pi the protein iterator
     * @param indexStringLength the index string length
     * @param numProteins the number of proteins
     * @param alphabet the alphabet
     * @param fastaParameters the parameters for the FASTA file parsing
     * @param waitingHandler the waiting handler
     * @param displayProgress if progress is to be displayed
     *
     * @throws IOException exception thrown if an error occurs while iterating
     * the FASTA file.
     */
    void addDataToIndex(ProteinIterator pi, int indexStringLength, int numProteins, long[] alphabet, FastaParameters fastaParameters, WaitingHandler waitingHandler, boolean displayProgress) throws IOException {

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
        int indexPart = accessions.size();
        accessions.add(accssions);
        boundaries.get(0)[0] = 1;

        // reading proteins in a second pass to store their amino acid sequences and their accession numbers
        int tmpN = 0;
        int tmpNumProtein = 0;
        HashMap<String, Integer> accessionEndings = new HashMap<>();

        for (int i = 0; i < numProteins; ++i) {

            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                return;
            }

            Protein currentProtein = pi.getNextProtein();

            if (currentProtein == null) {
                throw new IllegalArgumentException("More sequences from database requested than contained.");
            }

            String accession = currentProtein.getAccession();
            Header headerObject = ((FastaIterator) pi).getLastHeader();
            String header = (headerObject.getRawHeader().charAt(0) == '>' ? headerObject.getRawHeader().substring(1) : headerObject.getRawHeader());
            AccessionMetaData accMD = new AccessionMetaData(header);
            accessionMetaData.put(accession, accMD);

            if (accession == null || accession.equals("")) {
                accession = header;
            }

            if (fastaParameters != null && ProteinUtils.isDecoy(accession, fastaParameters)) {

                decoyAccessions.add(accession);

            }

            int proteinLen = currentProtein.getLength();

            T[tmpN++] = '/'; // adding the delimiters
            accMD.trueBeginning = tmpN;
            accessionEndings.put(accession, tmpN + proteinLen);
            System.arraycopy(currentProtein.getSequence().toUpperCase().getBytes(), 0, T, tmpN, proteinLen);
            tmpN += proteinLen;
            accssions[tmpNumProtein++] = accession;
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

        // compute end positions of all accessions in the index
        int[] inversedSampledSuffixArray = new int[indexStringLength];
        for (int i = 0; i < indexStringLength; ++i) {
            inversedSampledSuffixArray[suffixArrayPrimary[i]] = i;
        }
        for (String accession : accessionEndings.keySet()) {
            int truePos = accessionEndings.get(accession);
            AccessionMetaData accessionMeta = accessionMetaData.get(accession);
            accessionMeta.index = inversedSampledSuffixArray[truePos];
            accessionMeta.indexPart = indexPart;
        }
        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }
        inversedSampledSuffixArray = null;
        

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
        WaveletTree occurrenceTablePrimary = new WaveletTree(bwt, alphabet, waitingHandler, numMasses, hasModificationatTerminus);
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
        WaveletTree occurrenceTableReversed = new WaveletTree(bwt, alphabet, waitingHandler, numMasses, hasModificationatTerminus);
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
        if (mass >= LOOKUP_MAX_MASS) {
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

    @Override
    public ArrayList<PeptideProteinMapping> getProteinMapping(String peptide, SequenceMatchingParameters sequenceMatchingParameters) {
        ArrayList<PeptideProteinMapping> peptideProteinMapping = new ArrayList<>();
        if (maxNumberVariants > 0 || maxNumberDeletions > 0 || maxNumberInsertions > 0 || maxNumberSubstitutions > 0) {
            if (variantMatchingType == 0) {
                for (int i = 0; i < indexParts; ++i) {
                    peptideProteinMapping.addAll(getProteinMappingWithVariantsGeneric(peptide, sequenceMatchingParameters, i));
                }
                return peptideProteinMapping;
            } else {
                for (int i = 0; i < indexParts; ++i) {
                    peptideProteinMapping.addAll(getProteinMappingWithVariantsSpecific(peptide, sequenceMatchingParameters, i));
                }
                return peptideProteinMapping;
            }
        } else {
            for (int i = 0; i < indexParts; ++i) {
                peptideProteinMapping.addAll(getProteinMappingWithoutVariants(peptide, sequenceMatchingParameters, i));
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
        int maxX = (int) (seqMatchPref.getLimitX() * lenPeptide);

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
        int xNumLimit = (int) (seqMatchPref.getLimitX() * lenPeptide);

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
                                ArrayList<int[]> setCharacter = occurrenceTablePrimary.rangeQuery(leftIndexOld - 1, rightIndexOld);
                                for (int[] borders : setCharacter) {
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
        int xNumLimit = (int) (seqMatchPref.getLimitX() * lenPeptide);

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
                                ArrayList<int[]> setCharacter = occurrenceTablePrimary.rangeQuery(leftIndexOld - 1, rightIndexOld);
                                for (int[] borders : setCharacter) {
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

    /**
     * Adding modifications for backward search suggestions.
     *
     * @param setCharacter the set characters
     */
    private void addModifications(ArrayList<int[]> setCharacter) {

        int maxNum = setCharacter.size();

        for (int i = 0; i < maxNum; ++i) {

            int pos = 128 + setCharacter.get(i)[0];

            while (pos < aaMasses.length && aaMasses[pos] != -1) {

                setCharacter.add(new int[]{setCharacter.get(i)[0], setCharacter.get(i)[1], setCharacter.get(i)[2], pos, setCharacter.get(i)[4]});
                pos += 128;

            }
        }
    }

    /**
     * Exchange B, J, Z amino acid queries into their corresponding amino acids
     *
     * @param setCharacter
     */
    private void addAmbiguous(ArrayList<int[]> setCharacter) {

        long[] foundChars = new long[]{0L, 0L};
        int maxNum = setCharacter.size();
        int[] bValues = null;
        int[] jValues = null;
        int[] zValues = null;
        for (int i = setCharacter.size() - 1; i >= 0; --i) {
            if (setCharacter.get(i)[0] == 'B' || setCharacter.get(i)[0] == 'J' || setCharacter.get(i)[0] == 'Z') {
                switch (setCharacter.get(i)[0]) {
                    case 'B':
                        bValues = setCharacter.get(i);
                        break;
                    case 'J':
                        jValues = setCharacter.get(i);
                        break;
                    case 'Z':
                        zValues = setCharacter.get(i);
                        break;
                }
                setCharacter.remove(i);
            } else {
                int c = setCharacter.get(i)[0];
                foundChars[c >>> 6] |= (1L) << (c & 63);
            }
        }

        // adding all amino acids possible by B substitution, if not yet present in list
        if (bValues != null) {
            for (int i = 0; i < BSubstitutions.length; ++i) {
                int c = BSubstitutions[i];
                if (((foundChars[c >>> 6] >>> (c & 63)) & 1L) == 0) {
                    setCharacter.add(new int[]{c, bValues[1], bValues[2], c, 'B'});
                }
            }
        }

        // adding all amino acids possible by J substitution, if not yet present in list
        if (jValues != null) {
            for (int i = 0; i < JSubstitutions.length; ++i) {
                int c = JSubstitutions[i];
                if (((foundChars[c >>> 6] >>> (c & 63)) & 1L) == 0) {
                    setCharacter.add(new int[]{c, jValues[1], jValues[2], c, 'J'});
                }
            }
        }

        // adding all amino acids possible by Z substitution, if not yet present in list
        if (zValues != null) {
            for (int i = 0; i < ZSubstitutions.length; ++i) {
                int c = ZSubstitutions[i];
                if (((foundChars[c >>> 6] >>> (c & 63)) & 1L) == 0) {
                    setCharacter.add(new int[]{c, zValues[1], zValues[2], c, 'Z'});
                }
            }
        }
    }

    private boolean massNotValid(double mass) {
        int intMass = (int) (mass * lookupMultiplier);
        return (mass > massTolerance && mass < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0));
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
                    ArrayList<int[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                    addAmbiguous(setCharacter);

                    if (withVariableModifications) addModifications(setCharacter);

                    for (int[] borders : setCharacter) {
                        final int aminoAcid = borders[0];
                        if (aminoAcid == '/') {
                            continue;
                        }
                        int newNumX = numX + (aminoAcid == 'X' ? 1 : 0);
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
                    ArrayList<int[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                    addAmbiguous(setCharacter);

                    if (withVariableModifications) addModifications(setCharacter);

                    for (int[] borders : setCharacter) {
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

                        ArrayList<int[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                        if (withVariableModifications) addModifications(setCharacter);

                        for (int[] borders : setCharacter) {
                            final int aminoAcid = borders[0];
                            if (aminoAcid == '/')  continue;
                            final double newMass = oldMass + aaMasses[borders[3]];
                            final int lessValue = less[aminoAcid];
                            final int leftIndex = lessValue + borders[1];
                            final int rightIndex = lessValue + borders[2] - 1;
                            int offset = (Math.abs(combinationMass - newMass) <= massTolerance) ? 1 : 0;

                            if (newMass - massTolerance <= combinationMass) {
                                boolean add = true;
                                double massDiff = combinationMass - newMass;
                                int intMass = (int) (massDiff * lookupMultiplier);
                                if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                        if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                    if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                ArrayList<int[]> setCharacterSeq = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                                for (int[] borders : setCharacterSeq) {
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
     * @param massTolerance the mass tolerance
     * @param numberEdits number of allowed edit operations
     */
    private void mappingSequenceAndMassesWithFixedVariants(TagElement[] combinations, LinkedList<MatrixContent>[][] matrix, int[] less, WaveletTree occurrence, Rank variantPositions, HashSet<int[]>[] variants) {
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

                        ArrayList<int[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                        if (withVariableModifications)  addModifications(setCharacter);

                        for (int[] borders : setCharacter) {
                            final int aminoAcid = borders[0];
                            if (aminoAcid == '/') continue;
                            
                            final double newMass = oldMass + aaMasses[borders[3]];
                            final int lessValue = less[aminoAcid];
                            final int leftIndex = lessValue + borders[1];
                            final int rightIndex = lessValue + borders[2] - 1;
                            int offset = (Math.abs(combinationMass - newMass) <= massTolerance) ? 1 : 0;

                            if (newMass - massTolerance <= combinationMass) {
                                boolean add = true;
                                double massDiff = combinationMass - newMass;
                                int intMass = (int) (massDiff * lookupMultiplier);
                                if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                    add = false;
                                }
                                if (add) {
                                    row[j + offset].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newMass, length + 1, numX, borders[3], numVariants, '-', null));
                                }
                            }
                            
                            
                            // check for SNP variants
                            int numLeftSNPs = variantPositions.getRankOne(leftIndex - 1);
                            int numRightSNPs = variantPositions.getRankOne(rightIndex);
                            
                            if (numLeftSNPs < numRightSNPs){
                                
                                // search for all SNPs positions
                                for (int SNPnum = numLeftSNPs + 1; SNPnum <= numRightSNPs; ++SNPnum){
                                    
                                    int selectSNP = variantPositions.getSelect(SNPnum);
                                    
                                    // go through all SNPs at certain position
                                    for (int[] variant : variants[SNPnum - 1]){
                                        if (variant[0] != aminoAcid) continue;
                                        
                                        ArrayList<int[]> setCharacterSNP = new ArrayList<>(numMasses);
                                        setCharacterSNP.add(new int[]{variant[1], selectSNP, selectSNP, variant[1], -1});
                                        
                                        if (withVariableModifications)  addModifications(setCharacterSNP);
                                        
                                        for (int[] bordersSNP : setCharacterSNP) {
                                        
                                            final int aminoAcidSNP = variant[1];
                                            final double newMassSNP = oldMass + aaMasses[bordersSNP[3]];
                                            final int leftIndexSNP = selectSNP;
                                            final int rightIndexSNP = selectSNP;
                                            if (leftIndexSNP <= rightIndexSNP){
                                                int offsetSNP = (Math.abs(combinationMass - newMassSNP) <= massTolerance) ? 1 : 0;

                                                if (newMassSNP - massTolerance <= combinationMass) {
                                                    boolean add = true;
                                                    double massDiff = combinationMass - newMassSNP;
                                                    int intMass = (int) (massDiff * lookupMultiplier);
                                                    if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                                        add = false;
                                                    }
                                                    if (add) {
                                                        row[j + offsetSNP].add(new MatrixContent(leftIndexSNP, rightIndexSNP, aminoAcidSNP, content, newMassSNP, length + 1, numX, bordersSNP[3], numVariants + 1, (char)aminoAcid, null));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    } else { // sequence mapping 
                        final String combinationSequence = combination.sequence;
                        final int xNumLimit = combination.xNumLimit;

                        for (int c = 0; c < combinationSequence.length(); ++c) {
                            final int aminoAcidCheck = combinationSequence.charAt(c);
                            final int newNumX = numX + ((aminoAcidCheck == 'X') ? 1 : 0);
                            if (newNumX > xNumLimit) {
                                continue;
                            }

                            ArrayList<int[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);

                            for (int[] borders : setCharacter) {
                                final int aminoAcid = borders[0];
                                if (aminoAcid == '/')  continue;
                                
                                final int lessValue = less[aminoAcid];
                                final int leftIndex = lessValue + borders[1];
                                final int rightIndex = lessValue + borders[2] - 1;

                                // match
                                if (aminoAcid == aminoAcidCheck){
                                    if (leftIndex <= rightIndex) {
                                        row[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, content, newNumX, length + 1, numVariants, '-'));
                                    }
                                }

                                // check for SNPs
                                else {
                                    int numLeftSNPs = variantPositions.getRankOne(leftIndex - 1);
                                    int numRightSNPs = variantPositions.getRankOne(rightIndex);
                                    // search for all SNPs positions
                                    if (numLeftSNPs < numRightSNPs){
                                        for (int SNPnum = numLeftSNPs + 1; SNPnum <= numRightSNPs; ++SNPnum){

                                            int selectSNP = variantPositions.getSelect(SNPnum);

                                            // go through all SNPs at certain position
                                            for (int[] variant : variants[SNPnum - 1]){
                                                if (variant[0] != aminoAcid || variant[1] != aminoAcidCheck) continue;

                                                final int leftIndexSNP = selectSNP;
                                                final int rightIndexSNP = selectSNP;
                                                row[j + 1].add(new MatrixContent(leftIndexSNP, rightIndexSNP, aminoAcidCheck, content, numX, length + 1, numVariants + 1, (char)aminoAcid));
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

                        ArrayList<int[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                        if (withVariableModifications) addModifications(setCharacter);

                        for (int[] borders : setCharacter) {
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
                                if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                        if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                    if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                    ArrayList<int[]> setCharacterSeq = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                                    for (int[] borders : setCharacterSeq) {
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
        if (mass + computeInverseMassValue(massTolerance, mass) < negativeModificationMass) {
            return false;
        }
        int intMass = (int) (mass * lookupMultiplier);
        return (0 < numX && numX <= maxXPerTag && mass < LOOKUP_MAX_MASS && (((Xlookup[numX][intMass >>> 6] >>> (intMass & 63)) & 1L) == 1));
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
            MatrixContent lastCell = null;

            while (!content.isEmpty()) {

                MatrixContent cell = content.removeFirst();
                final int length = cell.length;
                final int leftIndexOld = cell.left;
                final int rightIndexOld = cell.right;

                if (combination.isMass) {

                    final double combinationMass = combination.mass;
                    final double oldMass = cell.mass;

                    ArrayList<int[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                    addAmbiguous(setCharacter);
                    if (withVariableModifications) addModifications(setCharacter);

                    if (k == lenCombinations - 1) {
                        for (int[] borders : setCharacter) {
                            final int aminoAcid = borders[0];
                            int newNumX = cell.numX + (aminoAcid == 'X' ? 1 : 0);
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
                                lastCell = newCell;

                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;

                                // ptm at terminus handling
                                ArrayList<String> fmodp;
                                ArrayList<Double> fmodpMass;
                                ArrayList<String>[] fmodpaa;
                                ArrayList<Double>[] fmodpaaMass;
                                ArrayList<String> vmodp;
                                ArrayList<Double> vmodpMass;
                                ArrayList<String>[] vmodpaa;
                                ArrayList<Double>[] vmodpaaMass;
                                boolean hasFixedModification_atTerminus;

                                if (CTermDirection) {

                                    fmodp = fmodcp;
                                    fmodpMass = fmodcpMass;
                                    fmodpaa = fmodcpaa;
                                    fmodpaaMass = fmodcpaaMass;
                                    vmodp = vmodcp;
                                    vmodpMass = vmodcpMass;
                                    vmodpaa = vmodcpaa;
                                    vmodpaaMass = vmodcpaaMass;
                                    hasFixedModification_atTerminus = hasFixedModification_CatTerminus;

                                } else {

                                    fmodp = fmodnp;
                                    fmodpMass = fmodnpMass;
                                    fmodpaa = fmodnpaa;
                                    fmodpaaMass = fmodnpaaMass;
                                    vmodp = vmodnp;
                                    vmodpMass = vmodnpMass;
                                    vmodpaa = vmodnpaa;
                                    vmodpaaMass = vmodnpaaMass;
                                    hasFixedModification_atTerminus = hasFixedModification_NatTerminus;
                                }

                                boolean hasFixed = false;
                                boolean withinMass = false;
                                double xMassDiff = -1 - massTolerance;

                                // fixed aa defined peptide terminal modification
                                if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {

                                    hasFixed = true;

                                    for (int i = 0; i < fmodpaaMass[lastAcid].size(); ++i) {

                                        double massDiffDiff = massDiff - fmodpaaMass[lastAcid].get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (((newNumX == 0) && computeMassValue(fmodpaaMass[lastAcid].get(i) + newMass, combinationMass) < massTolerance) || wmt) {
                                            withinMass |= wmt;
                                            xMassDiff = massDiffDiffAbs;
                                        }

                                        if (xMassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(fmodpaaMass[lastAcid].get(i) + newMass + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length + 1);
                                                }
                                            }
                                        }
                                        // variable undefined peptide terminal modifictation
                                        if (xMassDiff < -massTolerance && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(fmodpaaMass[lastAcid].get(i) + vmodpMass.get(j) + newMass, combinationMass) < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length + 1);
                                                }
                                            }
                                        }
                                    }
                                }

                                // fixed undefined peptide terminal modifictation
                                if (fmodp != null && xMassDiff < -massTolerance) {
                                    hasFixed = true;
                                    for (int i = 0; i < fmodp.size(); ++i) {
                                        double massDiffDiff = massDiff - fmodpMass.get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (((newNumX == 0) && computeMassValue(fmodpMass.get(i) + newMass, combinationMass) < massTolerance) || wmt) {
                                            withinMass |= wmt;
                                            xMassDiff = massDiffDiffAbs;
                                        }

                                        if (xMassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(fmodpMass.get(i) + newMass + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length + 1);
                                                }
                                            }
                                        }
                                        // variable undefined peptide terminal modifictation
                                        if (xMassDiff < -massTolerance && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(fmodpMass.get(i) + newMass + vmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length + 1);
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!hasFixedModification_atTerminus && !hasFixed && xMassDiff < -massTolerance) {
                                    // without any peptide terminal modification
                                    double massDiffF = Math.abs(massDiff);
                                    boolean wmt = withinMassTolerance(massDiffF, newNumX);
                                    if (((newNumX == 0) && computeMassValue(newMass, combinationMass) < massTolerance) || wmt) {
                                        withinMass |= wmt;
                                        xMassDiff = massDiffF;
                                    }

                                    // variable aa defined peptide terminal modification
                                    if (xMassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                        for (int i = 0; i < vmodpaaMass[lastAcid].size(); ++i) {
                                            double massDiffV = Math.abs(massDiff - vmodpaaMass[lastAcid].get(i));
                                            boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                            if (((newNumX == 0) && computeMassValue(vmodpaaMass[lastAcid].get(i) + newMass, combinationMass) < massTolerance) || wmtV) {
                                                withinMass |= wmtV;
                                                xMassDiff = massDiffV;
                                                modificationMatchEnd = new ModificationMatch(vmodpaa[lastAcid].get(i), length + 1);
                                            }
                                        }
                                    }

                                    // variable undefined peptide terminal modifictation
                                    if (xMassDiff < -massTolerance && vmodp != null) {
                                        for (int i = 0; i < vmodp.size(); ++i) {
                                            double massDiffV = Math.abs(massDiff - vmodpMass.get(i));
                                            boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                            if (((newNumX == 0) && computeMassValue(vmodpMass.get(i) + newMass, combinationMass) < massTolerance) || wmtV) {
                                                withinMass |= wmtV;
                                                xMassDiff = massDiffV;
                                                modificationMatchEnd = new ModificationMatch(vmodp.get(i), length + 1);
                                            }
                                        }
                                    }
                                }

                                if (xMassDiff < -massTolerance) {
                                    if (newMass - computeInverseMassValue(massTolerance, combinationMass) + negativeModificationMass <= combinationMass) {
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
                                        matrix[k + 1].getLast().XMassDiff = xMassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                } else {
                                    if (newNumX > 0 && !withinMass) {
                                        continue;
                                    }
                                    matrix[k + 1].add(newCell);
                                    matrix[k + 1].getLast().numX = 0;
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = xMassDiff;
                                    }
                                }

                            } else if (length > 1) {

                                int lastAcid = cell.character;
                                double massDiff = combinationMass - oldMass;
                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;
                                boolean withinMass = false;
                                double xMassDiff = -1;
                                
                                
                                // ptm at terminus handling
                                ArrayList<String> fmod;
                                ArrayList<Double> fmodMass;
                                ArrayList<String>[] fmodaa;
                                ArrayList<Double>[] fmodaaMass;
                                ArrayList<String> vmod;
                                ArrayList<Double> vmodMass;
                                ArrayList<String>[] vmodaa;
                                ArrayList<Double>[] vmodaaMass;
                                ArrayList<String> fmodp;
                                ArrayList<Double> fmodpMass;
                                ArrayList<String>[] fmodpaa;
                                ArrayList<Double>[] fmodpaaMass;
                                ArrayList<String> vmodp;
                                ArrayList<Double> vmodpMass;
                                ArrayList<String>[] vmodpaa;
                                ArrayList<Double>[] vmodpaaMass;

                                if (CTermDirection) {

                                    fmod = fmodc;
                                    fmodMass = fmodcMass;
                                    fmodaa = fmodcaa;
                                    fmodaaMass = fmodcaaMass;
                                    vmod = vmodc;
                                    vmodMass = vmodcMass;
                                    vmodaa = vmodcaa;
                                    vmodaaMass = vmodcaaMass;
                                    fmodp = fmodcp;
                                    fmodpMass = fmodcpMass;
                                    fmodpaa = fmodcpaa;
                                    fmodpaaMass = fmodcpaaMass;
                                    vmodp = vmodcp;
                                    vmodpMass = vmodcpMass;
                                    vmodpaa = vmodcpaa;
                                    vmodpaaMass = vmodcpaaMass;

                                } else {

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

                                boolean matchThroughFixedMod = false;

                                // fixed aa defined protein terminal modification
                                if (fmodaa != null && lastAcid > 0 && fmodaaMass[lastAcid].size() > 0) {
                                    for (int i = 0; i < fmodaaMass[lastAcid].size(); ++i) {
                                        double massDiffDiff = massDiff - fmodaaMass[lastAcid].get(i);
                                        double mDD = oldMass + fmodaaMass[lastAcid].get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (((newNumX == 0) && computeMassValue(mDD, combinationMass) < massTolerance) || wmt) {
                                            withinMass |= wmt;
                                            xMassDiff = massDiffDiffAbs;
                                            matchThroughFixedMod = true;
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(mDD + vmodaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(mDD + vmodMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(mDD + fmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    matchThroughFixedMod = true;
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(mDD + fmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    matchThroughFixedMod = true;
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && computeMassValue(mDD + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && computeMassValue(mDD + vmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length);
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
                                        if (((newNumX == 0) && computeMassValue(mDD, combinationMass) < massTolerance) || wmt) {
                                            withinMass |= wmt;
                                            xMassDiff = massDiffDiffAbs;
                                            matchThroughFixedMod = true;
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                double massDiffV = Math.abs(massDiff - vmodaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(vmodaaMass[lastAcid].get(j) + oldMass, combinationMass) < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                double massDiffV = Math.abs(massDiff - vmodMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(vmodMass.get(j) + oldMass, combinationMass) < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(mDD + fmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    matchThroughFixedMod = true;
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && computeMassValue(mDD + fmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    matchThroughFixedMod = true;
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && computeMassValue(mDD + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && computeMassValue(mDD + vmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length);
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
                                            if (((newNumX == 0) && computeMassValue(mDD, combinationMass) < massTolerance) || wmt) {
                                                withinMass |= wmt;
                                                xMassDiff = massDiffDiffAbs;
                                                modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && computeMassValue(mDD + fmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && computeMassValue(mDD + fmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (((newNumX == 0) && computeMassValue(mDD + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                            withinMass |= wmtV;
                                                            xMassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (((newNumX == 0) && computeMassValue(mDD + vmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                            withinMass |= wmtV;
                                                            xMassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length);
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
                                            if (((newNumX == 0) && computeMassValue(mDD, combinationMass) < massTolerance) || wmt) {
                                                withinMass |= wmt;
                                                xMassDiff = massDiffDiffAbs;
                                                modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && computeMassValue(mDD + fmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && computeMassValue(mDD + fmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (((newNumX == 0) && computeMassValue(mDD + vmodpaaMass[lastAcid].get(j), combinationMass) < massTolerance) || wmtV) {
                                                            withinMass |= wmtV;
                                                            xMassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (((newNumX == 0) && computeMassValue(mDD + vmodpMass.get(j), combinationMass) < massTolerance) || wmtV) {
                                                            withinMass |= wmtV;
                                                            xMassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length);
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
                                        matrix[k + 1].getLast().XMassDiff = xMassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                } else if (matchThroughFixedMod && lastCell != null) {
                                    matrix[k + 1].add(lastCell);
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = xMassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                }
                            }
                        }
                    } else {
                        for (int[] borders : setCharacter) {
                            final int aminoAcid = borders[0];
                            if (aminoAcid == '/')  continue;
                            
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
                                if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
            MatrixContent lastCell = null;

            while (!content.isEmpty()) {

                MatrixContent cell = content.removeFirst();
                final int length = cell.length;
                final int leftIndexOld = cell.left;
                final int rightIndexOld = cell.right;

                if (combination.isMass) {

                    final double combinationMass = combination.mass;
                    final double oldMass = cell.mass;

                    ArrayList<int[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                    addAmbiguous(setCharacter);

                    if (withVariableModifications) addModifications(setCharacter);

                    if (k == lenCombinations - 1) {

                        for (int[] borders : setCharacter) {
                            final int aminoAcid = borders[0];
                            int newNumX = cell.numX + ((aminoAcid == 'X') ? 1 : 0);
                            if (newNumX > combination.xNumLimit) continue;

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
                                lastCell = newCell;

                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;

                                // ptm at terminus handling
                                ArrayList<String> fmodp;
                                ArrayList<Double> fmodpMass;
                                ArrayList<String>[] fmodpaa;
                                ArrayList<Double>[] fmodpaaMass;
                                ArrayList<String> vmodp;
                                ArrayList<Double> vmodpMass;
                                ArrayList<String>[] vmodpaa;
                                ArrayList<Double>[] vmodpaaMass;
                                boolean hasFixedModification_atTerminus;

                                if (CTermDirection) {
                                    fmodp = fmodcp;
                                    fmodpMass = fmodcpMass;
                                    fmodpaa = fmodcpaa;
                                    fmodpaaMass = fmodcpaaMass;
                                    vmodp = vmodcp;
                                    vmodpMass = vmodcpMass;
                                    vmodpaa = vmodcpaa;
                                    vmodpaaMass = vmodcpaaMass;
                                    hasFixedModification_atTerminus = hasFixedModification_CatTerminus;
                                } else {
                                    fmodp = fmodnp;
                                    fmodpMass = fmodnpMass;
                                    fmodpaa = fmodnpaa;
                                    fmodpaaMass = fmodnpaaMass;
                                    vmodp = vmodnp;
                                    vmodpMass = vmodnpMass;
                                    vmodpaa = vmodnpaa;
                                    vmodpaaMass = vmodnpaaMass;
                                    hasFixedModification_atTerminus = hasFixedModification_NatTerminus;
                                }

                                boolean hasFixed = false;
                                boolean withinMass = false;
                                double xMassDiff = -1 - massTolerance;

                                // fixed aa defined peptide terminal modification
                                if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {

                                    hasFixed = true;

                                    for (int i = 0; i < fmodpaaMass[lastAcid].size(); ++i) {

                                        double massDiffDiff = Math.abs(massDiff - fmodpaaMass[lastAcid].get(i));
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (((newNumX == 0) && massDiffDiffAbs < massTolerance) || wmt) {
                                            withinMass |= wmt;
                                            xMassDiff = massDiffDiffAbs;
                                        }

                                        if (xMassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {

                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);

                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length + 1);
                                                }
                                            }
                                        }

                                        // variable undefined peptide terminal modifictation
                                        if (xMassDiff < -massTolerance && vmodp != null) {

                                            for (int j = 0; j < vmodp.size(); ++j) {

                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);

                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {

                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length + 1);
                                                }
                                            }
                                        }

                                    }
                                }

                                // fixed undefined peptide terminal modifictation
                                if (fmodp != null && xMassDiff < -massTolerance) {

                                    hasFixed = true;

                                    for (int i = 0; i < fmodp.size(); ++i) {

                                        double massDiffDiff = massDiff - fmodpMass.get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);

                                        if (((newNumX == 0) && massDiffDiffAbs < massTolerance) || wmt) {
                                            withinMass |= wmt;
                                            xMassDiff = massDiffDiffAbs;
                                        }

                                        if (xMassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length + 1);
                                                }
                                            }
                                        }

                                        // variable undefined peptide terminal modifictation
                                        if (xMassDiff < -massTolerance && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length + 1);
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!hasFixedModification_atTerminus && !hasFixed && xMassDiff < -massTolerance) {

                                    // without any peptide terminal modification
                                    double massDiffF = Math.abs(massDiff);
                                    boolean wmt = withinMassTolerance(massDiffF, newNumX);
                                    if (((newNumX == 0) && massDiffF < massTolerance) || wmt) {
                                        withinMass |= wmt;
                                        xMassDiff = massDiffF;
                                    }

                                    // variable aa defined peptide terminal modification
                                    if (xMassDiff < -massTolerance && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                        for (int i = 0; i < vmodpaaMass[lastAcid].size(); ++i) {
                                            double massDiffV = Math.abs(massDiff - vmodpaaMass[lastAcid].get(i));
                                            boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                            if (((newNumX == 0) && massDiffV < massTolerance) || wmtV) {
                                                withinMass |= wmtV;
                                                xMassDiff = massDiffV;
                                                modificationMatchEnd = new ModificationMatch(vmodpaa[lastAcid].get(i), length + 1);
                                            }
                                        }
                                    }

                                    // variable undefined peptide terminal modifictation
                                    if (xMassDiff < -massTolerance && vmodp != null) {
                                        for (int i = 0; i < vmodp.size(); ++i) {
                                            double massDiffV = Math.abs(massDiff - vmodpMass.get(i));
                                            boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                            if (((newNumX == 0) && massDiffV < massTolerance) || wmtV) {
                                                withinMass |= wmtV;
                                                xMassDiff = massDiffV;
                                                modificationMatchEnd = new ModificationMatch(vmodp.get(i), length + 1);
                                            }
                                        }
                                    }
                                }

                                if (xMassDiff < -massTolerance) {
                                    if (newMass - massTolerance + negativeModificationMass <= combinationMass) {
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
                                        matrix[k + 1].getLast().XMassDiff = xMassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                } else {
                                    if (newNumX > 0 && !withinMass) {
                                        continue;
                                    }
                                    matrix[k + 1].add(newCell);
                                    matrix[k + 1].getLast().numX = 0;
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = xMassDiff;
                                    }
                                }

                            } else if (length > 1) {
                                int lastAcid = cell.character;
                                double massDiff = combinationMass - oldMass;
                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;
                                boolean withinMass = false;
                                double xMassDiff = -1;

                                // ptm at terminus handling
                                ArrayList<String> fmod;
                                ArrayList<Double> fmodMass;
                                ArrayList<String>[] fmodaa;
                                ArrayList<Double>[] fmodaaMass;
                                ArrayList<String> vmod;
                                ArrayList<Double> vmodMass;
                                ArrayList<String>[] vmodaa;
                                ArrayList<Double>[] vmodaaMass;
                                ArrayList<String> fmodp;
                                ArrayList<Double> fmodpMass;
                                ArrayList<String>[] fmodpaa;
                                ArrayList<Double>[] fmodpaaMass;
                                ArrayList<String> vmodp;
                                ArrayList<Double> vmodpMass;
                                ArrayList<String>[] vmodpaa;
                                ArrayList<Double>[] vmodpaaMass;

                                if (CTermDirection) {
                                    fmod = fmodc;
                                    fmodMass = fmodcMass;
                                    fmodaa = fmodcaa;
                                    fmodaaMass = fmodcaaMass;
                                    vmod = vmodc;
                                    vmodMass = vmodcMass;
                                    vmodaa = vmodcaa;
                                    vmodaaMass = vmodcaaMass;
                                    fmodp = fmodcp;
                                    fmodpMass = fmodcpMass;
                                    fmodpaa = fmodcpaa;
                                    fmodpaaMass = fmodcpaaMass;
                                    vmodp = vmodcp;
                                    vmodpMass = vmodcpMass;
                                    vmodpaa = vmodcpaa;
                                    vmodpaaMass = vmodcpaaMass;
                                } else {
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

                                boolean matchThroughFixedMod = false;

                                // fixed aa defined protein terminal modification
                                if (fmodaa != null && lastAcid > 0 && fmodaaMass[lastAcid].size() > 0) {
                                    for (int i = 0; i < fmodaaMass[lastAcid].size(); ++i) {
                                        double massDiffDiff = massDiff - fmodaaMass[lastAcid].get(i);
                                        double massDiffDiffAbs = Math.abs(massDiffDiff);
                                        boolean wmt = withinMassTolerance(massDiffDiffAbs, newNumX);
                                        if (((newNumX == 0) && massDiffDiffAbs < massTolerance) || wmt) {
                                            withinMass |= wmt;
                                            xMassDiff = massDiffDiffAbs;
                                            matchThroughFixedMod = true;
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(j), length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - vmodMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    modificationMatchEnd = new ModificationMatch(vmod.get(j), length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    matchThroughFixedMod = true;
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    matchThroughFixedMod = true;
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodp.get(j), length);
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
                                        if (((newNumX == 0) && massDiffDiffAbs < massTolerance) || wmt) {
                                            withinMass |= wmt;
                                            xMassDiff = massDiffDiffAbs;
                                            matchThroughFixedMod = true;
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                double massDiffV = Math.abs(massDiff - vmodaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffV < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffV;
                                                    modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(j), length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                double massDiffV = Math.abs(massDiff - vmodMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffV < massTolerance) || wmtV) {
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffV;
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    matchThroughFixedMod = true;
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                    hasFixedPep = true;
                                                    withinMass |= wmtV;
                                                    xMassDiff = massDiffDiffV;
                                                    matchThroughFixedMod = true;
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length);
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
                                            if (((newNumX == 0) && massDiffDiffAbs < massTolerance) || wmt) {
                                                withinMass |= wmt;
                                                xMassDiff = massDiffDiffAbs;
                                                modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                            withinMass |= wmtV;
                                                            xMassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                            withinMass |= wmtV;
                                                            xMassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length);
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
                                            if (((newNumX == 0) && massDiffDiffAbs < massTolerance) || wmt) {
                                                withinMass |= wmt;
                                                xMassDiff = massDiffDiffAbs;
                                                modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    double massDiffDiffV = Math.abs(massDiffDiff - fmodpMass.get(j));
                                                    boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                    if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                        hasFixedPep = true;
                                                        withinMass |= wmtV;
                                                        xMassDiff = massDiffDiffV;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                            withinMass |= wmtV;
                                                            xMassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        double massDiffDiffV = Math.abs(massDiffDiff - vmodpMass.get(j));
                                                        boolean wmtV = withinMassTolerance(massDiffDiffV, newNumX);
                                                        if (((newNumX == 0) && massDiffDiffV < massTolerance) || wmtV) {
                                                            withinMass |= wmtV;
                                                            xMassDiff = massDiffDiffV;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), length);
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
                                        matrix[k + 1].getLast().XMassDiff = xMassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                } else if (matchThroughFixedMod && lastCell != null) {
                                    matrix[k + 1].add(lastCell);
                                    if (withinMass) {
                                        matrix[k + 1].getLast().XMassDiff = xMassDiff;
                                    }
                                    matrix[k + 1].getLast().numX = 0;
                                }
                            }
                        }
                    } else {
                        for (int[] borders : setCharacter) {
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
                                if (massDiff > massTolerance && massDiff < LOOKUP_MAX_MASS && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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

    @Override
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, SequenceMatchingParameters sequenceMatchingPreferences) {
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>(1);
        if (maxNumberVariants > 0 || maxNumberDeletions > 0 || maxNumberInsertions > 0 || maxNumberSubstitutions > 0 || variantMatchingType == 2) {
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
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithoutVariants(Tag tag, SequenceMatchingParameters sequenceMatchingPreferences, int indexPart) {

        int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
        WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
        int[] lessTableReversed = lessTablesReversed.get(indexPart);
        WaveletTree occurrenceTableReversed = occurrenceTablesReversed.get(indexPart);
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>(1);
        double xLimit = sequenceMatchingPreferences.getLimitX();

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

        TagElement[] refTagContent;
        int[] lessPrimary;
        int[] lessReversed;
        WaveletTree occurrencePrimary;
        WaveletTree occurrenceReversed;
        boolean hasCTermDirection = hasCTermDirectionModification;
        boolean hasNTermDirection = hasNTermDirectionModification;
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
            hasCTermDirection = hasNTermDirectionModification;
            hasNTermDirection = hasCTermDirectionModification;
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
        ArrayList<MatrixContent> cachePrimary = new ArrayList<>(3);

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
                StringBuilder currentPeptide = new StringBuilder(8);
                StringBuilder currentPeptideSearch = new StringBuilder(0);

                int leftIndexFront = 0;
                int rightIndexFront = indexStringLengths.get(indexPart) - 1;
                ArrayList<ModificationMatch> modifications = new ArrayList<>(1);
                ArrayList<int[]> xComponents = new ArrayList<>(0);
                HashMap<Integer, Double> xMassDiffs = new HashMap<>(0);

                while (currentContent.previousContent != null) {
                    final int aminoAcid = currentContent.character;
                    if (aminoAcid > 0) {
                        currentPeptide.append((char) currentContent.character);
                        int c = currentContent.ambiguousChar == -1 ? aminoAcid : currentContent.ambiguousChar;
                        if (currentContent.character == 'X') {
                            xComponents.add(new int[]{0, currentContent.tagComponent, currentContent.length});
                            xComponents.get(xComponents.size() - 1)[2] = currentContent.length;
                        }
                        //System.out.println((char) currentContent.character);
                        currentPeptideSearch.append((char) c);
                        final int lessValue = lessPrimary[c];
                        final int[] range = occurrencePrimary.singleRangeQuery(leftIndexFront - 1, rightIndexFront, c);
                        leftIndexFront = lessValue + range[0];
                        rightIndexFront = lessValue + range[1] - 1;
                    }
                    if (currentContent.XMassDiff > -1) {
                        xMassDiffs.put(currentContent.tagComponent, currentContent.XMassDiff);
                    }
                    if (currentContent.modification != null || currentContent.modificationPos >= 0) {
                        if (currentContent.modificationPos >= 0) {
                            if (modificationFlags[currentContent.modificationPos]) {
                                modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.length));
                            }
                        } else {
                            modifications.add(currentContent.modification);
                        }
                    }
                    currentContent = currentContent.previousContent;
                }
                String reversePeptide = currentPeptide.reverse().toString();
                String reversePeptideSearch = currentPeptideSearch.reverse().toString();
                MatrixContent cell = new MatrixContent(leftIndexFront, rightIndexFront, reversePeptide.charAt(0), null, 0, reversePeptide, reversePeptideSearch, content.length, 0, 0, null, modifications, -1);
                cell.allXcomponents = xComponents;
                cell.allXMassDiffs = xMassDiffs;
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
            StringBuilder currentPeptide = new StringBuilder(8);
            StringBuilder currentPeptideSearch = new StringBuilder(0);
            ArrayList<ModificationMatch> modifications = new ArrayList<>(1);
            ArrayList<int[]> xComponents = new ArrayList<>(0);
            HashMap<Integer, Double> XmassDiffs = new HashMap<>(0);

            while (currentContent.previousContent != null) {

                if (currentContent.character != '\0') {

                    currentPeptide.append((char) currentContent.character);
                    currentPeptideSearch.append((char) (currentContent.ambiguousChar == -1 ? currentContent.character : currentContent.ambiguousChar));

                    if (currentContent.character == 'X') {

                        xComponents.add(new int[]{1, currentContent.tagComponent, content.length - currentContent.length + 1});

                    }
                }

                if (currentContent.XMassDiff > -1) {

                    XmassDiffs.put(currentContent.tagComponent + 1024, currentContent.XMassDiff);

                }

                if (currentContent.modification != null || currentContent.modificationPos >= 0) {

                    if (currentContent.modificationPos >= 0) {

                        if (modificationFlags[currentContent.modificationPos]) {

                            modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], content.length - currentContent.length + 1));

                        }

                    } else {

                        modifications.add(new ModificationMatch(currentContent.modification.getModification(), content.length - currentContent.modification.getSite() + 1));

                    }
                }

                currentContent = currentContent.previousContent;

            }

            int leftIndex = content.left;
            int rightIndex = content.right;

            for (int[] Xcomponent : currentContent.allXcomponents) {

                xComponents.add(new int[]{Xcomponent[0], Xcomponent[1], Xcomponent[2] + content.length - currentContent.length});

            }

            for (Integer key : currentContent.allXMassDiffs.keySet()) {

                XmassDiffs.put(key, currentContent.allXMassDiffs.get(key));

            }

            for (ModificationMatch modificationMatch : currentContent.modifications) {

                modifications.add(new ModificationMatch(modificationMatch.getModification(), modificationMatch.getSite() + content.length - currentContent.length));

            }

            StringBuilder peptide = new StringBuilder(currentPeptide.append(currentContent.peptideSequence));
            StringBuilder peptideSearch = new StringBuilder(currentPeptideSearch.append(currentContent.peptideSequenceSearch));

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

                    modificationMatch.setSite(peptide.length() - modificationMatch.getSite() + 1);

                }

                for (int[] Xcomponent : xComponents) {

                    Xcomponent[2] = peptide.length() - Xcomponent[2] + 1;

                }

                peptide = peptide.reverse();

            }

            if (xComponents.isEmpty()) {

                for (int j = leftIndex; j <= rightIndex; ++j) {

                    int pos = getTextPosition(j, indexPart);
                    int index = binarySearch(boundaries.get(indexPart), pos);
                    String accession = accessions.get(indexPart)[index];
                    PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, peptide.toString(), pos - boundaries.get(indexPart)[index] + 1, modifications.toArray(new ModificationMatch[modifications.size()]));

                    if (checkModificationPattern(peptideProteinMapping)) {

                        allMatches.add(peptideProteinMapping);

                    }
                }

            } else {

                // substituting all Xs if present into their corresponding amino acids
                ArrayList< ArrayList<Integer>> xSets = new ArrayList<>();
                ArrayList< int[]> xOrigins = new ArrayList<>();

                int nTag = -1, nComponent = -1;

                for (int i = 0; i < xComponents.size(); ++i) {

                    if (nTag != xComponents.get(i)[0] || nComponent != xComponents.get(i)[1]) {

                        xOrigins.add(new int[]{xComponents.get(i)[0], xComponents.get(i)[1]});
                        xSets.add(new ArrayList<>());
                        nTag = xComponents.get(i)[0];
                        nComponent = xComponents.get(i)[1];

                    }

                    xSets.get(xSets.size() - 1).add(xComponents.get(i)[2]);

                }

                LinkedList<String> substitutedPeptides = new LinkedList<>();
                LinkedList<String> substitutedPeptidesTmp = new LinkedList<>();
                substitutedPeptides.add(peptide.toString());
                LinkedList< ArrayList<ModificationMatch>> substitutedModifications = new LinkedList<>();
                LinkedList< ArrayList<ModificationMatch>> substitutedModificationsTmp = new LinkedList<>();
                substitutedModifications.add(modifications);

                for (int k = 0; k < xSets.size(); ++k) {

                    ArrayList<Integer> xPositions = xSets.get(k);
                    int len = xPositions.size();
                    HashMap<String, int[][]> uniqueSubstitutions = new HashMap<>();

                    // find amino acids that correspond to the number of Xs and mass gap caused by all Xs
                    int[] Xranges = computeMappingRanges(XmassDiffs.get(xOrigins.get(k)[0] * 1024 + xOrigins.get(k)[1]));
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

                                newModifications.add(new ModificationMatch(m.getModification(), m.getSite()));

                            }

                            for (int i = 0; i < uniquePermutations[0].length; ++i) {

                                int pos = uniquePermutations[1][uniquePermutations[0][i]];
                                pepChars[xPositions.get(i) - 1] = (char) (pos & 127);

                                if (modificationFlags[pos]) {

                                    newModifications.add(new ModificationMatch(modifictationLabels[pos], xPositions.get(i)));

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
                        ArrayList<ModificationMatch> currenModifications = substitutedModifications.get(i);
                        PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, substitutedPeptides.get(i), pos - boundaries.get(indexPart)[index] + 1, currenModifications.toArray(new ModificationMatch[currenModifications.size()]));

                        if (checkModificationPattern(peptideProteinMapping)) {

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
     *
     * @return the protein mapping
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithVariants(Tag tag, SequenceMatchingParameters sequenceMatchingPreferences, int indexPart) {

        int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
        WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
        int[] lessTableReversed = lessTablesReversed.get(indexPart);
        WaveletTree occurrenceTableReversed = occurrenceTablesReversed.get(indexPart);
        
        Rank variantPositionsListPrimary = null;
        Rank variantPositionsListReversed = null;
        HashSet<int[]>[] variantsListPrimary = null;
        HashSet<int[]>[] variantsListReversed = null;
        
        if (variantMatchingType == 2){
            variantPositionsListPrimary = variantBitsPrimary.get(indexPart);
            variantPositionsListReversed = variantBitsReversed.get(indexPart);
            variantsListPrimary = variantsPrimary.get(indexPart);
            variantsListReversed = variantsReversed.get(indexPart);
        }
        
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<>();

        double xLimit = sequenceMatchingPreferences.getLimitX();

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

        TagElement[] refTagContent;
        int[] lessPrimary;
        int[] lessReversed;
        WaveletTree occurrencePrimary;
        WaveletTree occurrenceReversed;
        Rank variantPositionsPrimary;
        Rank variantPositionsReversed;
        HashSet<int[]>[] variantsPrimary;
        HashSet<int[]>[] variantsReversed;
        //boolean hasCTermDirection = hasCTermDirectionModification;
        //boolean hasNTermDirection = hasNTermDirectionModification;
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
            
            
            variantPositionsReversed = variantPositionsListPrimary;
            variantPositionsPrimary = variantPositionsListReversed;
            variantsReversed = variantsListPrimary;
            variantsPrimary = variantsListReversed;
            //hasCTermDirection = hasNTermDirectionModification;
            //hasNTermDirection = hasCTermDirectionModification;
            //towardsC = false;

        } else {

            refTagContent = tagElements;
            lessPrimary = lessTablePrimary;
            lessReversed = lessTableReversed;
            occurrencePrimary = occurrenceTablePrimary;
            occurrenceReversed = occurrenceTableReversed;
            
            
            variantPositionsPrimary = variantPositionsListPrimary;
            variantPositionsReversed = variantPositionsListReversed;
            variantsPrimary = variantsListPrimary;
            variantsReversed = variantsListReversed;

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

        int numErrors = 1;
        if (variantMatchingType == 0) numErrors += maxNumberVariants;
        else if (variantMatchingType == 1) numErrors += maxNumberDeletions + maxNumberInsertions + maxNumberSubstitutions;

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

                int error = 0;
                if (variantMatchingType == 0) error = matrixContent.numVariants;
                else if(variantMatchingType == 1) error = matrixContent.numSpecificVariants[0] + matrixContent.numSpecificVariants[1] + matrixContent.numSpecificVariants[2];
                matrix[error][0].add(matrixContent);

            }

        } else {

            matrixReversed[0][0].add(new MatrixContent(indexStringLengths.get(indexPart) - 1));

        }

        if (cached == null) {

            // Map Reverse
            if (variantMatchingType == 0) {

                mappingSequenceAndMassesWithVariantsGeneric(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed);

            } else if (variantMatchingType == 1) {

                mappingSequenceAndMassesWithVariantsSpecific(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed);

            } else if (variantMatchingType == 2) {
                
                mappingSequenceAndMassesWithFixedVariants(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed, variantPositionsReversed, variantsReversed);

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

                                    modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.length));

                                }

                            } else {

                                modifications.add(currentContent.modification);

                            }
                        }

                        currentContent = currentContent.previousContent;

                    }

                    String reversePeptide = (new StringBuilder(currentPeptide).reverse()).toString();
                    allVariants = (new StringBuilder(allVariants).reverse()).toString();

                    if (variantMatchingType == 0) {

                        cachePrimary.add(new MatrixContent(leftIndexFront, rightIndexFront, reversePeptide.charAt(0), null, 0, reversePeptide, content.length, 0, null, modifications, -1, k, '\0', allVariants));

                    } else {

                        cachePrimary.add(new MatrixContent(leftIndexFront, rightIndexFront, reversePeptide.charAt(0), null, 0, reversePeptide, content.length, 0, null, modifications, -1, new int[]{content.numSpecificVariants[0], content.numSpecificVariants[1], content.numSpecificVariants[2]}, '\0', allVariants));

                    }
                }
            }

            for (MatrixContent matrixContent : cachePrimary) {

                int error = 0;
                if (variantMatchingType == 0) error = matrixContent.numVariants;
                else if (variantMatchingType == 1) error = matrixContent.numSpecificVariants[0] + matrixContent.numSpecificVariants[1] + matrixContent.numSpecificVariants[2];
                matrix[error][0].add(matrixContent);
            }

            cacheIt(refTagContent, cachePrimary, indexPart);

        }

        if (variantMatchingType == 0) {

            mappingSequenceAndMassesWithVariantsGeneric(combinations, matrix, lessPrimary, occurrencePrimary);

        } else if (variantMatchingType == 1) {

            mappingSequenceAndMassesWithVariantsSpecific(combinations, matrix, lessPrimary, occurrencePrimary);

        } else if (variantMatchingType == 2) {
            mappingSequenceAndMassesWithFixedVariants(combinations, matrix, lessPrimary, occurrencePrimary, variantPositionsPrimary, variantsPrimary);

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

                                modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], content.length - currentContent.length + 1));

                            }
                        } else {

                            modifications.add(new ModificationMatch(currentContent.modification.getModification(), currentContent.modification.getSite() + content.length - currentContent.length + 1));

                        }
                    }

                    currentContent = currentContent.previousContent;

                }

                int leftIndex = content.left;
                int rightIndex = content.right;

                for (ModificationMatch modificationMatch : currentContent.modifications) {

                    modifications.add(new ModificationMatch(modificationMatch.getModification(), modificationMatch.getSite() + content.length - currentContent.length));

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

                        modificationMatch.setSite(peptide.length() - modificationMatch.getSite() + 1);

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
                        PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, cleanPeptide, startPosition + 1, modifications.toArray(new ModificationMatch[modifications.size()]), peptideVariantMatches);

                        if (checkModificationPattern(peptideProteinMapping)) {

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
    
    
    public static String getFileExtension(File file) {
        String extension = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }

        return extension;

    }
    
    
    
    /**
     * Reconstructs the fasta file stored in the index
     * @param file the output fasta file object
     */
    public void reconstructFasta(File file){
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( new FileWriter(file), 1024 * 1024);
            for (String accession : getAccessions()){
                writer.write(">" + getHeader(accession) + "\n");
                writer.write(getSequence(accession) + "\n");
            }

        }
        catch ( IOException e)
        {
        }
        finally
        {
            try
            {
                if ( writer != null)
                writer.close( );
            }
            catch ( IOException e)
            {
            }
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

        synchronized (cacheMutex) {
            String key = tagComponents[1].sequence + String.format("%.5f", tagComponents[2].mass);
            CacheElement cacheElement = cache[indexPart].get(key);
            if (cacheElement != null) {
                cached = cacheElement.cachedPrimary;
            }
        }
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
        synchronized (cacheMutex) {

            ArrayList<MatrixContent> cacheContentPrimary = new ArrayList<>();
            for (MatrixContent matrixContent : cachedPrimary) {
                cacheContentPrimary.add(new MatrixContent(matrixContent));
            }

            String key = tagComponents[1].sequence + String.format("%.5f", tagComponents[2].mass);
            CacheElement cacheElement = new CacheElement(tagComponents[0].mass, tagComponents[1].sequence, tagComponents[2].mass, cacheContentPrimary);
            if (!cache[indexPart].containsKey(key)) {
                cache[indexPart].put(key, cacheElement);
            }
        }
    }

    @Override
    public String getSequence(String proteinAccession) {

        AccessionMetaData accessionMeta = accessionMetaData.get(proteinAccession);

        if (accessionMeta != null) {

            int index = accessionMeta.index;
            int indexPart = accessionMeta.indexPart;
            int[] lessTablePrimary = lessTablesPrimary.get(indexPart);
            WaveletTree occurrenceTablePrimary = occurrenceTablesPrimary.get(indexPart);
            StringBuilder stringBuilder = new StringBuilder();

            while (true) {

                int[] aminoInfo = occurrenceTablePrimary.getCharacterInfo(index);
                index = lessTablePrimary[aminoInfo[0]] + aminoInfo[1];
                if (aminoInfo[0] == '/') {
                    break;
                }
                stringBuilder.append((char) aminoInfo[0]);

            }

            return stringBuilder.reverse().toString();

        }

        throw new UnsupportedOperationException("Protein accession '" + proteinAccession + "' not found in index.");
    }

    @Override
    public String getSubsequence(String accession, int start, int end) {

        String proteinSequence = getSequence(accession);
        return proteinSequence.substring(Math.max(start, 0), Math.min(end, proteinSequence.length()));

    }

    @Override
    public Collection<String> getAccessions() {
        return accessions.stream().flatMap(f -> Arrays.stream(f)).collect(Collectors.toList());
    }

    @Override
    public HashSet<String> getDecoyAccessions() {

        return decoyAccessions;

    }

    @Override
    public String getHeader(String proteinAccession) {

        AccessionMetaData accessionMeta = accessionMetaData.get(proteinAccession);

        return accessionMeta.getHeaderAsString();

    }

    @Override
    public String getDescription(String accession) {

        AccessionMetaData accessionMeta = accessionMetaData.get(accession);
        Header header = accessionMeta.getHeader();
        return header.getDescription();

    }

    @Override
    public String getSimpleDescription(String accession) {

        AccessionMetaData accessionMeta = accessionMetaData.get(accession);
        Header header = accessionMeta.getHeader();
        return header.getSimpleProteinDescription();

    }

    @Override
    public ProteinDatabase getProteinDatabase(String accession) {

        AccessionMetaData accessionMeta = accessionMetaData.get(accession);
        Header header = accessionMeta.getHeader();
        return header.getDatabaseType();

    }

    @Override
    public String getGeneName(String accession) {

        AccessionMetaData accessionMeta = accessionMetaData.get(accession);
        Header header = accessionMeta.getHeader();
        return header.getGeneName();

    }

    @Override
    public String getTaxonomy(String accession) {

        AccessionMetaData accessionMeta = accessionMetaData.get(accession);
        Header header = accessionMeta.getHeader();
        return header.getTaxonomy();

    }

    @Override
    public Integer getProteinEvidence(String accession) {

        AccessionMetaData accessionMeta = accessionMetaData.get(accession);
        Header header = accessionMeta.getHeader();
        return header.getProteinEvidence();

    }
}
