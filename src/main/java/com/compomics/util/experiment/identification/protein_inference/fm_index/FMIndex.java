package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory.ProteinIterator;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.variants.amino_acids.*;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.variants.AaSubstitutionMatrix;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.VariantMatch;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import org.jsuffixarrays.*;
import java.util.concurrent.Semaphore;

/**
 * The FM index.
 *
 * @author Dominik Kopczynski
 * @author Marc Vaudel
 */
public class FMIndex implements PeptideMapper {
    /**
     * Semaphore for caching.
     */
    static Semaphore cacheMutex = new Semaphore(1);
    /**
     * Sampled suffix array.
     */
    private int[] suffixArrayPrimary = null; //private int[] suffixArrayReversed = null;
    /**
     * Wavelet tree for storing the burrows wheeler transform.
     */
    public WaveletTree occurrenceTablePrimary = null;
    /**
     * Wavelet tree for storing the burrows wheeler transform reversed.
     */
    public WaveletTree occurrenceTableReversed = null;
    /**
     * Less table for doing an update step according to the LF step.
     */
    public int[] lessTablePrimary = null;
    /**
     * Less table for doing an update step according to the LF step reversed.
     */
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
     * Lookup tolerance mass.
     */
    double lookupTolerance = 0.02;
    /**
     * Maximum mass for lookup table.
     */
    double lookupMaxMass = 800;
    /**
     * Mass lookup table.
     */
    long[] lookupMasses = null;

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

    // @TODO: add javadoc
    public long getAllocatedBytes() {
        return occurrenceTablePrimary.getAllocatedBytes() + occurrenceTableReversed.getAllocatedBytes() + indexStringLength;
    }

    /**
     * Constructor. If PTM settings are provided the index will contain
     * modification information, ignored if null.
     *
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param ptmSettings contains modification parameters for identification
     * @param peptideVariantsPreferences contains all parameters for variants
     */
    public FMIndex(WaitingHandler waitingHandler, boolean displayProgress, PtmSettings ptmSettings, PeptideVariantsPreferences peptideVariantsPreferences) {
        // load all variant preferences
        maxNumberVariants = peptideVariantsPreferences.getnVariants();
        genericVariantMatching = !peptideVariantsPreferences.getUseSpecificCount();
        maxNumberInsertions = peptideVariantsPreferences.getnAaInsertions();
        maxNumberDeletions = peptideVariantsPreferences.getnAaDeletions();
        maxNumberSubstitutions = peptideVariantsPreferences.getnAaSubstitutions();

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
            PTMFactory ptmFactory = PTMFactory.getInstance();

            int hasVariableModification = 0;

            // check which amino acids have variable modificatitions
            for (String modification : variableModifications) {
                PTM ptm = ptmFactory.getPTM(modification);
                ArrayList<Character> targets;
                switch (ptm.getType()) {
                    case PTM.MODAA:
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        modificationCounts[targets.get(0)]++;
                        hasVariableModification = Math.max(hasVariableModification, modificationCounts[targets.get(0)]);
                        withVariableModifications = true;
                        break;

                    case PTM.MODC:
                        if (vmodc == null) {
                            vmodc = new ArrayList<String>();
                            vmodcMass = new ArrayList<Double>();
                            hasCTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                        }
                        vmodc.add(modification);
                        vmodcMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODCAA:
                        if (vmodcaa == null) {
                            vmodcaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodcaa[i] = new ArrayList<String>();
                            }
                            vmodcaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodcaaMass[i] = new ArrayList<Double>();
                            }
                            hasCTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        vmodcaa[targets.get(0)].add(modification);
                        vmodcaaMass[targets.get(0)].add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODCP:
                        if (vmodcp == null) {
                            vmodcp = new ArrayList<String>();
                            vmodcpMass = new ArrayList<Double>();
                            hasCTermDirectionPTM = true;
                        }
                        vmodcp.add(modification);
                        vmodcpMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODCPAA:
                        if (vmodcpaa == null) {
                            vmodcpaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodcpaa[i] = new ArrayList<String>();
                            }
                            vmodcpaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodcpaaMass[i] = new ArrayList<Double>();
                            }
                            hasCTermDirectionPTM = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        vmodcpaa[targets.get(0)].add(modification);
                        vmodcpaaMass[targets.get(0)].add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODN:
                        if (vmodn == null) {
                            vmodn = new ArrayList<String>();
                            vmodnMass = new ArrayList<Double>();
                            hasNTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                        }
                        vmodn.add(modification);
                        vmodnMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODNAA:
                        if (vmodnaa == null) {
                            vmodnaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodnaa[i] = new ArrayList<String>();
                            }
                            vmodnaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodnaaMass[i] = new ArrayList<Double>();
                            }
                            hasNTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        vmodnaa[targets.get(0)].add(modification);
                        vmodnaaMass[targets.get(0)].add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODNP:
                        if (vmodnp == null) {
                            vmodnp = new ArrayList<String>();
                            vmodnpMass = new ArrayList<Double>();
                            hasNTermDirectionPTM = true;
                        }
                        vmodnp.add(modification);
                        vmodnpMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODNPAA:
                        if (vmodnpaa == null) {
                            vmodnpaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodnpaa[i] = new ArrayList<String>();
                            }
                            vmodnpaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                vmodnpaaMass[i] = new ArrayList<Double>();
                            }
                            hasNTermDirectionPTM = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        vmodnpaa[targets.get(0)].add(modification);
                        vmodnpaaMass[targets.get(0)].add(ptm.getMass());
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
                aaMasses[aminoAcids[i]] = AminoAcid.getAminoAcid(aminoAcids[i]).getMonoisotopicMass();
            }

            // change masses for fixed modifications
            for (String modification : fixedModifications) {
                PTM ptm = ptmFactory.getPTM(modification);
                ArrayList<Character> targets;
                switch (ptm.getType()) {
                    case PTM.MODAA:
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        aaMasses[targets.get(0)] += ptm.getMass();
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        modifictationLabels[targets.get(0)] = modification;
                        modificationFlags[targets.get(0)] = true;
                        break;

                    case PTM.MODC:
                        if (fmodc == null) {
                            fmodc = new ArrayList<String>();
                            fmodcMass = new ArrayList<Double>();
                            hasCTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                            hasFixedPTM_CatTerminus = true;
                        }
                        fmodc.add(modification);
                        fmodcMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODCAA:
                        if (fmodcaa == null) {
                            fmodcaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodcaa[i] = new ArrayList<String>();
                            }
                            fmodcaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodcaaMass[i] = new ArrayList<Double>();
                            }
                            hasCTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                            hasFixedPTM_CatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        fmodcaa[targets.get(0)].add(modification);
                        fmodcaaMass[targets.get(0)].add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODCP:
                        if (fmodcp == null) {
                            fmodcp = new ArrayList<String>();
                            fmodcpMass = new ArrayList<Double>();
                            hasCTermDirectionPTM = true;
                        }
                        fmodcp.add(modification);
                        fmodcpMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODCPAA:
                        if (fmodcpaa == null) {
                            fmodcpaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodcpaa[i] = new ArrayList<String>();
                            }
                            fmodcpaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodcpaaMass[i] = new ArrayList<Double>();
                            }
                            hasCTermDirectionPTM = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        fmodcpaa[targets.get(0)].add(modification);
                        fmodcpaaMass[targets.get(0)].add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODN:
                        if (fmodn == null) {
                            fmodn = new ArrayList<String>();
                            fmodnMass = new ArrayList<Double>();
                            hasNTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                            hasFixedPTM_NatTerminus = true;
                        }
                        fmodn.add(modification);
                        fmodnMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODNAA:
                        if (fmodnaa == null) {
                            fmodnaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodnaa[i] = new ArrayList<String>();
                            }
                            fmodnaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodnaaMass[i] = new ArrayList<Double>();
                            }
                            hasNTermDirectionPTM = true;
                            hasPTMatTerminus = true;
                            hasFixedPTM_NatTerminus = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        fmodnaa[targets.get(0)].add(modification);
                        fmodnaaMass[targets.get(0)].add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODNP:
                        if (fmodnp == null) {
                            fmodnp = new ArrayList<String>();
                            fmodnpMass = new ArrayList<Double>();
                            hasNTermDirectionPTM = true;
                        }
                        fmodnp.add(modification);
                        fmodnpMass.add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;

                    case PTM.MODNPAA:
                        if (fmodnpaa == null) {
                            fmodnpaa = (ArrayList<String>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodnpaa[i] = new ArrayList<String>();
                            }
                            fmodnpaaMass = (ArrayList<Double>[]) new ArrayList[128];
                            for (int i = 0; i < 128; ++i) {
                                fmodnpaaMass[i] = new ArrayList<Double>();
                            }
                            hasNTermDirectionPTM = true;
                        }
                        if (ptm.getPattern().length() > 1) {
                            throw new UnsupportedOperationException();
                        }
                        targets = ptm.getPattern().getAminoAcidsAtTarget();
                        fmodnpaa[targets.get(0)].add(modification);
                        fmodnpaaMass[targets.get(0)].add(ptm.getMass());
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        break;
                }

            }

            // add masses for variable modifications
            for (int i = 0; i < modificationCounts.length; ++i) {
                modificationCounts[i] = 0;
            }
            for (String modification : variableModifications) {
                PTM ptm = ptmFactory.getPTM(modification);
                if (ptm.getType() == PTM.MODAA) {
                    ArrayList<Character> targets = ptm.getPattern().getAminoAcidsAtTarget();
                    aaMasses[128 * (1 + modificationCounts[targets.get(0)]) + targets.get(0)] = aaMasses[targets.get(0)] + ptm.getMass();
                    modifictationLabels[128 * (1 + modificationCounts[targets.get(0)]) + targets.get(0)] = modification;
                    modificationFlags[128 * (1 + modificationCounts[targets.get(0)]) + targets.get(0)] = true;
                    modificationCounts[targets.get(0)]++;
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
                aaMasses[aminoAcids[i]] = AminoAcid.getAminoAcid(aminoAcids[i]).getMonoisotopicMass();
            }
        }

        ArrayList<Integer> aaMassVector = new ArrayList<Integer>();
        for (int i = 0; i < aaMasses.length; ++i) {
            if (aaMasses[i] > 0) {
                aaMassVector.add(i);
            }
        }

        aaMassIndexes = new int[aaMassVector.size()];
        for (int i = 0; i < aaMassVector.size(); ++i) {
            aaMassIndexes[i] = aaMassVector.get(i);
        }
        numMasses = aaMassVector.size();

        SequenceFactory sf = SequenceFactory.getInstance(100000);
        boolean deNovo = true; // @TODO: change it for de novo
        int maxProgressBar = 6 + ((deNovo) ? 4 : 0);

        if (waitingHandler != null && displayProgress && !waitingHandler.isRunCanceled()) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(maxProgressBar
            );
            waitingHandler.setSecondaryProgressCounter(0);
        }

        // reading all proteins in a first pass to get information about number and total length
        indexStringLength = 1;
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
        indexStringLength += 2; // last delimiter + sentinal

        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }
        byte[] T = new byte[indexStringLength];
        T[0] = '/';                     // adding delimiter at beginning
        T[indexStringLength - 2] = '/'; // adding delimiter at ending
        T[indexStringLength - 1] = '$'; // adding the sentinal
        
        System.out.println("Num Proteins: " + numProteins);
        System.out.println("Num AA: " + (indexStringLength - numProteins - 2));

        boundaries = new int[numProteins + 1];
        accessions = new String[numProteins];
        boundaries[0] = 1;

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
                T[tmpN++] = '/'; // adding the delimiters
                System.arraycopy(currentProtein.getSequence().toUpperCase().getBytes(), 0, T, tmpN, proteinLen);
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

        int[] T_int = new int[indexStringLength];
        for (int i = 0; i < indexStringLength; ++i) {
            T_int[i] = T[i];
        }
        suffixArrayPrimary = (new DivSufSort()).buildSuffixArray(T_int, 0, indexStringLength);

        if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            waitingHandler.increaseSecondaryProgressCounter();
        }
        T_int = null;

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
        occurrenceTablePrimary = new WaveletTree(bwt, alphabet, waitingHandler, numMasses, hasPTMatTerminus);
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
            occurrenceTableReversed = new WaveletTree(bwt, alphabet, waitingHandler, numMasses, hasPTMatTerminus);
            lessTableReversed = occurrenceTableReversed.createLessTable();
            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            TReversed = null;
        }

        T = null;
        bwt = null;

        lookupMasses = new long[(((int) ((lookupMaxMass + lookupTolerance) * lookupMultiplier)) >>> 6) + 3];
        for (int i = 0; i < lookupMasses.length; ++i) {
            lookupMasses[i] = 0L;
        }
        recursiveMassFilling(lookupMasses, lookupMultiplier, lookupTolerance, lookupMaxMass, 0., 0);

    }

    /**
     * Recursive function to compute all possible mass combinations up to a
     * given maximum limit
     *
     * @param lookupMasses mass lookup array
     * @param lookupMultiplier precision
     * @param lookupTolerance lookup tolerance
     * @param lookupMaxMass maximal mass for lookup strategy
     * @param mass current mass
     * @param pos current index of amino acid mass array
     */
    void recursiveMassFilling(long[] lookupMasses, double lookupMultiplier, double lookupTolerance, double lookupMaxMass, double mass, int pos) {
        if (mass >= lookupMaxMass) {
            return;
        }
        if (mass > lookupTolerance) {
            int startMass = (int) ((mass - lookupTolerance) * lookupMultiplier);
            int endMass = (int) ((mass + lookupTolerance) * lookupMultiplier + 1);
            lookupMasses[startMass >>> 6] |= (~(0L)) << (startMass & 63);
            for (int p = (startMass >>> 6) + 1; p < (endMass >>> 6); ++p) {
                lookupMasses[p] = ~0L;
            }
            lookupMasses[endMass >>> 6] |= (~(0L)) >>> (64 - (endMass & 63));
        }

        for (int i = pos; i < aaMassIndexes.length; ++i) {
            recursiveMassFilling(lookupMasses, lookupMultiplier, lookupTolerance, lookupMaxMass, mass + aaMasses[aaMassIndexes[i]], i);
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
    private ArrayList<String> createPeptideCombinations(String peptide, SequenceMatchingPreferences seqMatchPref) {
        ArrayList<String> combinations = new ArrayList<String>();

        SequenceMatchingPreferences.MatchingType sequenceMatchingType = seqMatchPref.getSequenceMatchingType();
        if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.string) {
            for (int i = 0; i < peptide.length(); ++i) {
                combinations.add(peptide.substring(i, i + 1));
            }
        } else if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.aminoAcid || sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids) {
            boolean indistinghuishable = sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids;

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
        } else if (sequenceMatchingType == SequenceMatchingPreferences.MatchingType.aminoAcid || sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids) {
            boolean indistinghuishable = sequenceMatchingType == SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids;

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
    private int getTextPosition(int index) {
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
    public ArrayList<PeptideProteinMapping> getProteinMapping(String peptide, SequenceMatchingPreferences seqMatchPref) {

        if (maxNumberVariants > 0 || maxNumberDeletions > 0 || maxNumberInsertions > 0 || maxNumberSubstitutions > 0) {
            if (genericVariantMatching) {
                return getProteinMappingWithVariantsGeneric(peptide, seqMatchPref);
            } else {
                return getProteinMappingWithVariantsSpecific(peptide, seqMatchPref);
            }
        } else {
            return getProteinMappingWithoutVariants(peptide, seqMatchPref);
        }

    }

    /**
     * Exact mapping peptides against the proteome.
     *
     * @param peptide the peptide
     * @param seqMatchPref the sequence matching preferences
     * @return the mapping
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithoutVariants(String peptide, SequenceMatchingPreferences seqMatchPref) {
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<PeptideProteinMapping>();

        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int lenPeptide = peptide.length();
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref);
        int maxX = (int) (((seqMatchPref.getLimitX() != null) ? seqMatchPref.getLimitX() : 1) * lenPeptide);

        ArrayList<MatrixContent>[] backwardList = (ArrayList<MatrixContent>[]) new ArrayList[lenPeptide + 1];

        int countX = 0;
        for (int i = 0; i <= lenPeptide; ++i) {
            backwardList[i] = new ArrayList<MatrixContent>(10);
            if (i < lenPeptide && pep_rev.charAt(i) == 'X') {
                ++countX;
            }
        }

        if (countX <= maxX) {
            backwardList[0].add(new MatrixContent(indexStringLength - 1)); // L, R, char, previous content, num of X
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
                    int pos = getTextPosition(j);
                    int index = binarySearch(boundaries, pos);
                    String accession = accessions[index];

                    PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, currentPeptide, pos - boundaries[index]);
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
     * @return the mapping
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithVariantsGeneric(String peptide, SequenceMatchingPreferences seqMatchPref) {
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<PeptideProteinMapping>();
        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int lenPeptide = peptide.length();
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref);
        int xNumLimit = (int) (((seqMatchPref.getLimitX() != null) ? seqMatchPref.getLimitX() : 1) * lenPeptide);

        ArrayList<MatrixContent>[][] backwardMatrix = (ArrayList<MatrixContent>[][]) new ArrayList[maxNumberVariants + 1][lenPeptide + 1];

        for (int k = 0; k <= maxNumberVariants; ++k) {
            for (int j = 0; j <= lenPeptide; ++j) {
                backwardMatrix[k][j] = new ArrayList<MatrixContent>(10);
            }
        }
        int countX = 0;
        for (int j = 0; j <= lenPeptide; ++j) {
            if (j < lenPeptide && pep_rev.charAt(j) == 'X') {
                ++countX;
            }
        }

        if (countX <= xNumLimit) {
            backwardMatrix[0][0].add(new MatrixContent(indexStringLength - 1));

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
                        int pos = getTextPosition(j);
                        int index = binarySearch(boundaries, pos);
                        String accession = accessions[index];

                        int startPosition = pos - boundaries[index];
                        boolean newPeptide = true;

                        for (PeptideProteinMapping ppm : allMatches) {
                            if (ppm.getProteinAccession().equals(accession) && ppm.getPeptideSequence().equals(cleanPeptide) && Math.abs(ppm.getIndex() - startPosition) <= maxNumberVariants) {
                                newPeptide = false;
                                break;
                            }
                        }

                        if (newPeptide) {

                            ArrayList<VariantMatch> variants = new ArrayList<VariantMatch>();
                            // adding variants and adjusting modification sites
                            for (int l = 0, length = 0; l < allVariants.length(); ++l) {
                                int edit = allVariants.charAt(l);
                                ++length;
                                if (edit != '-') {
                                    if (edit == '*') { // insertion
                                        variants.add(new VariantMatch(new Insertion(peptide.charAt(length - 1)), "-", length));
                                    } else if ('A' <= edit && edit <= 'Z') { // substitution
                                        variants.add(new VariantMatch(new Substitution((char) edit, peptide.charAt(length - 1)), "-", length));
                                    } else if ('a' <= edit && edit <= 'z') { // deletion
                                        variants.add(new VariantMatch(new Deletion((char) (edit - 32)), "-", length));
                                        --length;
                                    }
                                }
                            }

                            PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, cleanPeptide, startPosition, null, variants);
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
     * @return the mapping
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithVariantsSpecific(String peptide, SequenceMatchingPreferences seqMatchPref) {
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<PeptideProteinMapping>();

        String pep_rev = new StringBuilder(peptide).reverse().toString();
        int lenPeptide = peptide.length();
        ArrayList<String> combinations = createPeptideCombinations(pep_rev, seqMatchPref);
        int xNumLimit = (int) (((seqMatchPref.getLimitX() != null) ? seqMatchPref.getLimitX() : 1) * lenPeptide);

        int numErrors = maxNumberDeletions + maxNumberInsertions + maxNumberSubstitutions;
        LinkedList<MatrixContent>[][] backwardMatrix = (LinkedList<MatrixContent>[][]) new LinkedList[numErrors + 1][lenPeptide + 1];

        for (int k = 0; k <= numErrors; ++k) {
            for (int j = 0; j <= lenPeptide; ++j) {
                backwardMatrix[k][j] = new LinkedList<MatrixContent>();
            }
        }
        int countX = 0;
        for (int j = 0; j <= lenPeptide; ++j) {
            if (j < lenPeptide && pep_rev.charAt(j) == 'X') {
                ++countX;
            }
        }

        if (countX <= xNumLimit) {
            backwardMatrix[0][0].add(new MatrixContent(indexStringLength - 1));

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
                        int pos = getTextPosition(j);
                        int index = binarySearch(boundaries, pos);
                        String accession = accessions[index];

                        int startPosition = pos - boundaries[index];
                        boolean newPeptide = true;

                        for (PeptideProteinMapping ppm : allMatches) {
                            if (ppm.getProteinAccession().equals(accession) && ppm.getPeptideSequence().equals(cleanPeptide) && Math.abs(ppm.getIndex() - startPosition) <= numErrors) {
                                newPeptide = false;
                                break;
                            }
                        }

                        if (newPeptide) {

                            ArrayList<VariantMatch> variants = new ArrayList<VariantMatch>();
                            // adding variants and adjusting modification sites
                            for (int l = 0, length = 0; l < allVariants.length(); ++l) {
                                int edit = allVariants.charAt(l);
                                ++length;
                                if (edit != '-') {
                                    if (edit == '*') { // insertion
                                        variants.add(new VariantMatch(new Insertion(peptide.charAt(length - 1)), "-", length));
                                    } else if ('A' <= edit && edit <= 'Z') { // substitution
                                        variants.add(new VariantMatch(new Substitution((char) edit, peptide.charAt(length - 1)), "-", length));
                                    } else if ('a' <= edit && edit <= 'z') { // deletion
                                        variants.add(new VariantMatch(new Deletion((char) (edit - 32)), "-", length));
                                        --length;
                                    }
                                }
                            }

                            PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, cleanPeptide, startPosition, null, variants);
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
                setCharacter[setCharacter[numMasses][0]++] = new int[]{setCharacter[i][0], setCharacter[i][1], setCharacter[i][2], pos};
                pos += 128;
            }
        }
    }

    /**
     * Mapping the tag elements to the reference text.
     *
     * @param combinations the combinations
     * @param matrix the matrix
     * @param matrixFinished the finished matrix
     * @param less the less array
     * @param occurrence the wavelet tree
     * @param massTolerance the mass tolerance
     */
    private void mappingSequenceAndMasses(TagElement[] combinations, LinkedList<MatrixContent>[] matrix, int[] less, WaveletTree occurrence, double massTolerance) {

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
                    if (withVariableModifications) {
                        addModifications(setCharacter);
                    }

                    //for (Integer[] borders : setCharacter) {
                    for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                        int[] borders = setCharacter[b];
                        final int aminoAcid = borders[0];
                        if (aminoAcid == '/') {
                            continue;
                        }
                        final double newMass = oldMass + aaMasses[borders[3]];
                        if (newMass - massTolerance <= combinationMass) {
                            final int lessValue = less[aminoAcid];
                            final int leftIndex = lessValue + borders[1];
                            final int rightIndex = lessValue + borders[2] - 1;

                            int offset = (Math.abs(combinationMass - newMass) <= massTolerance) ? 1 : 0;
                            boolean add = true;
                            double massDiff = combinationMass - newMass;
                            int intMass = (int) (massDiff * lookupMultiplier);
                            if (massDiff > lookupTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
                                add = false;
                            }
                            if (add) {
                                matrix[j + offset].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, length + 1, numX, borders[3]));
                            }
                        }
                    }
                } else {
                    final String combinationSequence = combination.sequence;
                    final int xNumLimit = combination.xNumLimit;

                    for (int i = 0; i < combinationSequence.length(); ++i) {
                        final int aminoAcid = combinationSequence.charAt(i);
                        final int lessValue = less[aminoAcid];
                        final int[] range = occurrence.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcid);
                        final int leftIndex = lessValue + range[0];
                        final int rightIndex = lessValue + range[1] - 1;
                        final int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);
                        if (leftIndex <= rightIndex && newNumX <= xNumLimit) {
                            matrix[j + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, length + 1, newNumX, -1));
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
    private void mappingSequenceAndMassesWithVariantsGeneric(TagElement[] combinations, LinkedList<MatrixContent>[][] matrix, int[] less, WaveletTree occurrence, double massTolerance) {
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
                                if (massDiff > lookupTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                        if (massDiff > lookupTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                    if (massDiff > lookupTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
     * @param massTolerance the mass tolerance
     * @param numberEdits number of allowed edit operations
     */
    private void mappingSequenceAndMassesWithVariantsSpecific(TagElement[] combinations, LinkedList<MatrixContent>[][] matrix, int[] less, WaveletTree occurrence, double massTolerance) {
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
                                if (massDiff > lookupTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                        if (massDiff > lookupTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
                                    if (massDiff > lookupTolerance && massDiff < lookupMaxMass && (((lookupMasses[intMass >>> 6] >>> (intMass & 63)) & 1L) == 0)) {
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
    double pepMass(String peptide) {
        double mass = 0;
        for (int i = 0; i < peptide.length(); ++i) {
            mass += aaMasses[peptide.charAt(i)];
        }
        return mass;
    }

    /**
     * Mapping the tag elements to the reference text.
     *
     * @param combinations
     * @param matrix
     * @param matrixFinished
     * @param less
     * @param occurrence
     * @param massTolerance
     * @param CTermDirection
     */
    private void mappingSequenceAndMasses(TagElement[] combinations, LinkedList<MatrixContent>[] matrix, int[] less, WaveletTree occurrence, double massTolerance, boolean CTermDirection) {

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
                    if (withVariableModifications) {
                        addModifications(setCharacter);
                    }
                    if (k == lenCombinations - 1) {
                        for (int b = 0; b < setCharacter[numMasses][0]; ++b) {
                            int[] borders = setCharacter[b];
                            final int aminoAcid = borders[0];

                            if (aminoAcid != '/') {
                                final double newMass = oldMass + aaMasses[borders[3]];
                                double massDiff = combinationMass - newMass;
                                int lastAcid = aminoAcid;
                                final int lessValue = less[aminoAcid];
                                final int leftIndex = lessValue + borders[1];
                                final int rightIndex = lessValue + borders[2] - 1;
                                MatrixContent newCell = new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, length + 1, cell.numX, borders[3]);

                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;
                                boolean endOfPeptide = false;

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

                                // fixed aa defined peptide terminal modification
                                if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                    hasFixed = true;
                                    for (int i = 0; i < fmodpaaMass[lastAcid].size(); ++i) {
                                        double massDiffDiff = massDiff - fmodpaaMass[lastAcid].get(i);

                                        if (Math.abs(massDiffDiff) < massTolerance) {
                                            endOfPeptide = true;
                                            modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                        }

                                        if (!endOfPeptide && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                    endOfPeptide = true;
                                                    modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length + 1);
                                                }
                                            }
                                        }
                                        // variable undefined peptide terminal modifictation
                                        if (!endOfPeptide && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                    endOfPeptide = true;
                                                    modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), false, length + 1);
                                                }
                                            }
                                        }

                                    }
                                }

                                // fixed undefined peptide terminal modifictation
                                if (fmodp != null && !endOfPeptide) {
                                    hasFixed = true;
                                    for (int i = 0; i < fmodp.size(); ++i) {
                                        double massDiffDiff = massDiff - fmodpMass.get(i);
                                        if (Math.abs(massDiff - fmodpMass.get(i)) < massTolerance) {
                                            endOfPeptide = true;
                                            modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                        }

                                        if (!endOfPeptide && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                                if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                    endOfPeptide = true;
                                                    modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length + 1);
                                                }
                                            }
                                        }
                                        // variable undefined peptide terminal modifictation
                                        if (!endOfPeptide && vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                    endOfPeptide = true;
                                                    modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, length + 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), false, length + 1);
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!hasFixedPTM_atTerminus && !hasFixed && !endOfPeptide) {

                                    // without any peptide terminal modification
                                    if (Math.abs(massDiff) < massTolerance) {
                                        endOfPeptide = true;
                                    }

                                    // variable aa defined peptide terminal modification
                                    if (!endOfPeptide && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                        for (int i = 0; i < vmodpaaMass[lastAcid].size(); ++i) {
                                            if (Math.abs(massDiff - vmodpaaMass[lastAcid].get(i)) < massTolerance) {
                                                endOfPeptide = true;
                                                modificationMatchEnd = new ModificationMatch(vmodpaa[lastAcid].get(i), true, length + 1);
                                            }
                                        }
                                    }
                                    // variable undefined peptide terminal modifictation
                                    if (!endOfPeptide && vmodp != null) {
                                        for (int i = 0; i < vmodp.size(); ++i) {
                                            if (Math.abs(massDiff - vmodpMass.get(i)) < massTolerance) {
                                                endOfPeptide = true;
                                                modificationMatchEnd = new ModificationMatch(vmodp.get(i), false, length + 1);
                                            }
                                        }
                                    }
                                }

                                if (!endOfPeptide) {
                                    if (newMass - massTolerance + negativePTMMass <= combinationMass) {
                                        content.add(newCell);
                                    }
                                } else if (modificationMatchEnd != null) {
                                    MatrixContent newEndCell = new MatrixContent(leftIndex, rightIndex, '\0', newCell, 0, null, length + 1, 0, modificationMatchEnd, null, -1);
                                    if (modificationMatchEndEnd == null) {
                                        matrix[k + 1].add(newEndCell);
                                    } else {
                                        MatrixContent newEndEndCell = new MatrixContent(leftIndex, rightIndex, '\0', newEndCell, 0, null, length + 1, 0, modificationMatchEndEnd, null, -1);
                                        matrix[k + 1].add(newEndEndCell);
                                    }
                                } else {
                                    matrix[k + 1].add(newCell);
                                }

                            } else if (length > 1) {
                                int lastAcid = cell.character;
                                double massDiff = combinationMass - oldMass;
                                ModificationMatch modificationMatchEnd = null;
                                ModificationMatch modificationMatchEndEnd = null;

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
                                        Double massDiffDiff = massDiff - fmodaaMass[lastAcid].get(i);
                                        if (Math.abs(massDiffDiff) < massTolerance) {
                                            modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                if (Math.abs(massDiffDiff - vmodaaMass[lastAcid].get(j)) < massTolerance) {
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), true, length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                if (Math.abs(massDiffDiff - vmodMass.get(j)) < massTolerance) {
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), false, length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {

                                                if (Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                if (Math.abs(massDiffDiff - fmodpMass.get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                    if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                        hasFixedPep = true;
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
                                        Double massDiffDiff = massDiff - fmodMass.get(i);
                                        if (Math.abs(massDiffDiff) < massTolerance) {
                                            modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                        }

                                        // variable aa defined protein terminal modification
                                        if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                                if (Math.abs(massDiff - vmodaaMass[lastAcid].get(j)) < massTolerance) {
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), true, length);
                                                }
                                            }
                                        }
                                        // variable undefined protein terminal modifictation
                                        if (vmod != null && modificationMatchEnd == null) {
                                            for (int j = 0; j < vmod.size(); ++j) {
                                                if (Math.abs(massDiff - vmodMass.get(j)) < massTolerance) {
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(vmod.get(j), false, length);
                                                }
                                            }
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {

                                                if (Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                if (Math.abs(massDiffDiff - fmodpMass.get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                    if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(fmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                        hasFixedPep = true;
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
                                            if (Math.abs(massDiffDiff) < massTolerance) {
                                                modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {

                                                    if (Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    if (Math.abs(massDiffDiff - fmodpMass.get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                        if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                            hasFixedPep = true;
                                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                            hasFixedPep = true;
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
                                            if (Math.abs(massDiffDiff) < massTolerance) {
                                                modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                            }

                                            // second ptm at peptide terminus
                                            boolean hasFixedPep = false;
                                            if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                    if (Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (fmodp != null) {
                                                for (int j = 0; j < fmodp.size(); ++j) {
                                                    if (Math.abs(massDiffDiff - fmodpMass.get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                        modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, length);
                                                    }
                                                }
                                            }

                                            if (!hasFixedPep) {
                                                if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                    for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                        if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                            hasFixedPep = true;
                                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), false, length);
                                                            modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, length);
                                                        }
                                                    }
                                                }

                                                if (vmodp != null) {
                                                    for (int j = 0; j < vmodp.size(); ++j) {
                                                        if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                            hasFixedPep = true;
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
                                    MatrixContent newEndCell = new MatrixContent(leftIndexOld, rightIndexOld, '\0', cell, 0, null, length, 0, modificationMatchEnd, null, -1);
                                    if (modificationMatchEndEnd == null) {
                                        matrix[k + 1].add(newEndCell);
                                    } else {
                                        MatrixContent newEndEndCell = new MatrixContent(leftIndexOld, rightIndexOld, '\0', newEndCell, 0, null, length, 0, modificationMatchEndEnd, null, -1);
                                        matrix[k + 1].add(newEndEndCell);
                                    }
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

                            final double newMass = oldMass + aaMasses[borders[3]];
                            if (newMass - massTolerance <= combinationMass) {
                                final int lessValue = less[aminoAcid];
                                final int leftIndex = lessValue + borders[1];
                                final int rightIndex = lessValue + borders[2] - 1;
                                //ModificationMatch modificationMatch = modifictationFlags[borders[3]] ? new ModificationMatch(modifictationLabels[borders[3]], (borders[3] >= 128), pepLen) : null;
                                if (combinationMass <= newMass + massTolerance) {
                                    matrix[k + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, null, length + 1, 0, null, null, borders[3]));
                                } else {
                                    content.add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, null, length + 1, 0, null, null, borders[3]));
                                }
                            }
                        }
                    }

                } else {
                    final String combinationSequence = combination.sequence;
                    final int xNumLimit = combination.xNumLimit;
                    final int numX = cell.numX;

                    for (int i = 0; i < combinationSequence.length(); ++i) {
                        final int aminoAcid = combinationSequence.charAt(i);
                        final int lessValue = less[aminoAcid];
                        final int[] range = occurrence.singleRangeQuery(leftIndexOld - 1, rightIndexOld, aminoAcid);
                        final int leftIndex = lessValue + range[0];
                        final int rightIndex = lessValue + range[1] - 1;
                        final int newNumX = numX + ((aminoAcid == 'X') ? 1 : 0);
                        if (leftIndex <= rightIndex && newNumX <= xNumLimit) {
                            matrix[k + 1].add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, length + 1, newNumX, -1));
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
     * @param tagMatcher the tag matcher
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param massTolerance the mass tolerance
     * @return the protein mapping
     * @throws IOException thrown if an IOException occurs
     * @throws InterruptedException thrown if an InterruptedException occurs
     * @throws ClassNotFoundException thrown if a ClassNotFoundException
     * @throws SQLException thrown if an SQLException occurs
     */
    @Override
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, TagMatcher tagMatcher, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        if (maxNumberVariants > 0 || maxNumberDeletions > 0 || maxNumberInsertions > 0 || maxNumberSubstitutions > 0) {
            return getProteinMappingWithVariants(tag, tagMatcher, sequenceMatchingPreferences, massTolerance);
        } else {
            return getProteinMappingWithoutVariants(tag, tagMatcher, sequenceMatchingPreferences, massTolerance);
        }
    }

    /**
     * Mapping tags against proteome without variants.
     *
     * @param tag the tag
     * @param tagMatcher the tag matcher
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param massTolerance the mass tolerance
     * @return the protein mapping
     * @throws IOException thrown if an IOException occurs
     * @throws InterruptedException thrown if an InterruptedException occurs
     * @throws ClassNotFoundException thrown if a ClassNotFoundException
     * @throws SQLException thrown if an SQLException occurs
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithoutVariants(Tag tag, TagMatcher tagMatcher, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<PeptideProteinMapping>();
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

        LinkedList<MatrixContent>[] matrixReversed = (LinkedList<MatrixContent>[]) new LinkedList[combinationsReversed.length + 1];
        LinkedList<MatrixContent>[] matrix = (LinkedList<MatrixContent>[]) new LinkedList[combinations.length + 1];
        ArrayList<MatrixContent> cachePrimary = new ArrayList<MatrixContent>();

        for (int i = 0; i <= combinationsReversed.length; ++i) {
            matrixReversed[i] = new LinkedList<MatrixContent>();
        }
        for (int i = 0; i <= combinations.length; ++i) {
            matrix[i] = new LinkedList<MatrixContent>();
        }

        if (cached != null) {
            for (MatrixContent matrixContent : cached) {
                matrix[0].add(matrixContent);
            }
        } else {
            matrixReversed[0].add(new MatrixContent(indexStringLength - 1));
        }

        if (cached == null) {
            // Map Reverse
            if (!hasCTermDirection) {
                mappingSequenceAndMasses(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed, massTolerance);
            } else {
                mappingSequenceAndMasses(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed, massTolerance, towardsC);
            }

            // Traceback Reverse
            for (MatrixContent content : matrixReversed[combinationsReversed.length]) {
                MatrixContent currentContent = content;
                String currentPeptide = "";

                int leftIndexFront = 0;
                int rightIndexFront = indexStringLength - 1;
                ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();

                while (currentContent.previousContent != null) {
                    final int aminoAcid = currentContent.character;
                    if (aminoAcid > 0) {
                        currentPeptide += (char) currentContent.character;
                        final int lessValue = lessPrimary[aminoAcid];
                        final int[] range = occurrencePrimary.singleRangeQuery(leftIndexFront - 1, rightIndexFront, aminoAcid);
                        leftIndexFront = lessValue + range[0];
                        rightIndexFront = lessValue + range[1] - 1;
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
                cachePrimary.add(new MatrixContent(leftIndexFront, rightIndexFront, reversePeptide.charAt(0), null, 0, reversePeptide, content.length, 0, null, modifications, -1));
            }

            for (MatrixContent matrixContent : cachePrimary) {
                matrix[0].add(matrixContent);
            }

            cacheIt(refTagContent, cachePrimary);
        }

        if (!matrix[0].isEmpty()) {
            // Map towards NTerm
            if (!hasNTermDirection) {
                mappingSequenceAndMasses(combinations, matrix, lessPrimary, occurrencePrimary, massTolerance);
            } else {
                mappingSequenceAndMasses(combinations, matrix, lessPrimary, occurrencePrimary, massTolerance, !towardsC);
            }
        }
        // Traceback from NTerm
        for (MatrixContent content : matrix[combinations.length]) {
            MatrixContent currentContent = content;
            String currentPeptide = "";
            ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();

            while (currentContent.previousContent != null) {
                if (currentContent.character != '\0') {
                    currentPeptide += (char) currentContent.character;
                }

                if (currentContent.modification != null || currentContent.modificationPos >= 0) {
                    if (currentContent.modificationPos >= 0) {
                        if (modificationFlags[currentContent.modificationPos]) {
                            modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.modificationPos >= 128, content.length - currentContent.length + 1));
                        }
                    } else {
                        modifications.add(new ModificationMatch(currentContent.modification.getTheoreticPtm(), currentContent.modification.isVariable(), content.length - currentContent.modification.getModificationSite() + 1));
                    }
                }

                currentContent = currentContent.previousContent;
            }

            int leftIndex = content.left;
            int rightIndex = content.right;

            for (ModificationMatch modificationMatch : currentContent.modifications) {
                modifications.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), modificationMatch.getModificationSite() + content.length - currentContent.length));
            }

            String peptide = currentPeptide + currentContent.peptideSequence;

            if (turned) {
                leftIndex = 0;
                rightIndex = indexStringLength - 1;

                for (int p = 0; p < peptide.length(); ++p) {
                    final int aminoAcid = peptide.charAt(p);
                    final int lessValue = lessReversed[aminoAcid];
                    final int[] range = occurrenceReversed.singleRangeQuery(leftIndex - 1, rightIndex, aminoAcid);
                    leftIndex = lessValue + range[0];
                    rightIndex = lessValue + range[1] - 1;

                }

                for (ModificationMatch modificationMatch : modifications) {
                    modificationMatch.setModificationSite(peptide.length() - modificationMatch.getModificationSite() + 1);
                }

                peptide = (new StringBuilder(peptide).reverse()).toString();
            }

            for (int j = leftIndex; j <= rightIndex; ++j) {
                int pos = getTextPosition(j);
                int index = binarySearch(boundaries, pos);
                String accession = accessions[index];

                PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, peptide, pos - boundaries[index], modifications);
                allMatches.add(peptideProteinMapping);
            }
        }

        /*
        if (tag.getContent().size() == 3){
            ArrayList<TagComponent> tc = tag.getContent();
            for (PeptideProteinMapping ppm : allMatches){
                System.out.println(tc.get(0).getMass() + " " + tc.get(1).asSequence() + " " + tc.get(2).getMass() + " " + ppm.getPeptideSequence() + " " + ppm.getProteinAccession() + " " + ppm.getIndex());
            }
        }*/
        return allMatches;
    }

    /**
     * mapping tags against proteome with variants.
     *
     * @param tag the tag
     * @param tagMatcher the tag matcher
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param massTolerance the mass tolerance
     * @return the protein mapping
     * @throws IOException thrown if an IOException occurs
     * @throws InterruptedException thrown if an InterruptedException occurs
     * @throws ClassNotFoundException thrown if a ClassNotFoundException
     * @throws SQLException thrown if an SQLException occurs
     */
    public ArrayList<PeptideProteinMapping> getProteinMappingWithVariants(Tag tag, TagMatcher tagMatcher, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        ArrayList<PeptideProteinMapping> allMatches = new ArrayList<PeptideProteinMapping>();

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

        int numErrors = 1 + ((genericVariantMatching) ? maxNumberVariants : maxNumberDeletions + maxNumberInsertions + maxNumberSubstitutions);

        LinkedList<MatrixContent>[][] matrixReversed = (LinkedList<MatrixContent>[][]) new LinkedList[numErrors][combinationsReversed.length + 1];
        LinkedList<MatrixContent>[][] matrix = (LinkedList<MatrixContent>[][]) new LinkedList[numErrors][combinations.length + 1];
        ArrayList<MatrixContent> cachePrimary = new ArrayList<MatrixContent>();

        // initializing both matrices
        for (int k = 0; k < numErrors; ++k) {
            for (int j = 0; j <= combinationsReversed.length; ++j) {
                matrixReversed[k][j] = new LinkedList<MatrixContent>();
            }
            for (int j = 0; j <= combinations.length; ++j) {
                matrix[k][j] = new LinkedList<MatrixContent>();
            }
        }

        // filling the matrices
        if (cached != null) {
            for (MatrixContent matrixContent : cached) {
                int error = genericVariantMatching ? matrixContent.numVariants : matrixContent.numSpecificVariants[0] + matrixContent.numSpecificVariants[1] + matrixContent.numSpecificVariants[2];
                matrix[error][0].add(matrixContent);
            }
        } else {
            matrixReversed[0][0].add(new MatrixContent(indexStringLength - 1));
        }

        if (cached == null) {
            // Map Reverse
            if (genericVariantMatching) {
                mappingSequenceAndMassesWithVariantsGeneric(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed, massTolerance);
            } else {
                mappingSequenceAndMassesWithVariantsSpecific(combinationsReversed, matrixReversed, lessReversed, occurrenceReversed, massTolerance);
            }

            // Traceback Reverse
            for (int k = 0; k < numErrors; ++k) {
                for (MatrixContent content : matrixReversed[k][combinationsReversed.length]) {
                    MatrixContent currentContent = content;
                    String currentPeptide = "";
                    int leftIndexFront = 0;
                    int rightIndexFront = indexStringLength - 1;
                    ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();
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
            cacheIt(refTagContent, cachePrimary);

        }

        if (genericVariantMatching) {
            mappingSequenceAndMassesWithVariantsGeneric(combinations, matrix, lessPrimary, occurrencePrimary, massTolerance);
        } else {
            mappingSequenceAndMassesWithVariantsSpecific(combinations, matrix, lessPrimary, occurrencePrimary, massTolerance);
        }

        // Traceback from NTerm
        for (int k = 0; k < numErrors; ++k) {
            for (MatrixContent content : matrix[k][combinations.length]) {
                MatrixContent currentContent = content;
                String currentPeptide = "";
                ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();
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
                            modifications.add(new ModificationMatch(currentContent.modification.getTheoreticPtm(), currentContent.modification.isVariable(), currentContent.modification.getModificationSite() + content.length - currentContent.length + 1));
                        }
                    }

                    currentContent = currentContent.previousContent;
                }

                int leftIndex = content.left;
                int rightIndex = content.right;

                for (ModificationMatch modificationMatch : currentContent.modifications) {
                    modifications.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), modificationMatch.getModificationSite() + content.length - currentContent.length));
                }

                String peptide = currentPeptide + currentContent.peptideSequence;
                allVariants += currentContent.allVariants;
                ArrayList<VariantMatch> variants = new ArrayList<VariantMatch>();

                if (turned) {
                    leftIndex = 0;
                    rightIndex = indexStringLength - 1;

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
                            variants.add(new VariantMatch(new Insertion(peptide.charAt(length - 1)), "-", length));
                        } else if ('A' <= edit && edit <= 'Z') { // substitution
                            variants.add(new VariantMatch(new Substitution((char) edit, peptide.charAt(length - 1)), "-", length));
                        } else if ('a' <= edit && edit <= 'z') { // deletion
                            variants.add(new VariantMatch(new Deletion((char) (edit - 32)), "-", length));
                            --length;
                        }
                    }
                }

                String cleanPeptide = peptide.replace("*", "");
                for (int j = leftIndex; j <= rightIndex; ++j) {
                    int pos = getTextPosition(j);
                    int index = binarySearch(boundaries, pos);
                    String accession = accessions[index];

                    int startPosition = pos - boundaries[index];
                    boolean newPeptide = true;

                    for (PeptideProteinMapping ppm : allMatches) {
                        if (ppm.getProteinAccession().equals(accession) && ppm.getPeptideSequence().equals(cleanPeptide) && Math.abs(ppm.getIndex() - startPosition) <= numErrors) {
                            newPeptide = false;
                            break;
                        }
                    }

                    if (newPeptide) {

                        PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, cleanPeptide, startPosition, modifications, variants);
                        allMatches.add(peptideProteinMapping);
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
    private final LinkedList<CacheElement> cache = new LinkedList<CacheElement>();

    /**
     * Adding intermediate tag to proteome mapping results into the cache.
     *
     * @param tagComponents
     * @return
     */
    private ArrayList<MatrixContent> isCached(TagElement[] tagComponents) {
        if (tagComponents.length != 3 || !tagComponents[0].isMass || tagComponents[1].isMass || !tagComponents[2].isMass) {
            return null;
        }
        ArrayList<MatrixContent> cached = null;

        try {
            cacheMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (CacheElement cacheElement : cache) {
            if (cacheElement.sequence.equals(tagComponents[1].sequence) && Math.abs(cacheElement.massSecond - tagComponents[2].mass) < 1e-5) {
                cached = new ArrayList<MatrixContent>(cacheElement.cachedPrimary);
                break;
            }
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
    private void cacheIt(TagElement[] tagComponents, ArrayList<MatrixContent> cachedPrimary) {
        if (tagComponents.length != 3 || !tagComponents[0].isMass || tagComponents[1].isMass || !tagComponents[2].isMass) {
            return;
        }
        try {
            cacheMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
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
        cacheMutex.release();
    }
}
