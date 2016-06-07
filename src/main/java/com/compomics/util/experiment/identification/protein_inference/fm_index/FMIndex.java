package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory.ProteinIterator;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.jsuffixarrays.*;

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
     * List of all amino acid masses.
     */
    private String[] modifictationLabels = null;
    /**
     * List of all amino acid masses.
     */
    private boolean[] modifictationFlags = null;
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
     * Constructor. If PTM settings are provided the index will contain
     * modification information, ignored if null.
     *
     * @param waitingHandler the waiting handler
     * @param displayProgress if true, the progress is displayed
     * @param ptmSettings contains modification parameters for identification
     */
    public FMIndex(WaitingHandler waitingHandler, boolean displayProgress, PtmSettings ptmSettings) {

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
            modifictationFlags = new boolean[128 * (1 + hasVariableModification)];
            for (int i = 0; i < aaMasses.length; ++i) {
                aaMasses[i] = -1;
                modifictationLabels[i] = null;
                modifictationFlags[i] = false;
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
                        /*if (modificationCounts[targets.get(0)] > 0){
                            throw new UnsupportedOperationException("Simultaneous fixed and variable modification of the same amino acid is not allowed.");
                        }*/
                        aaMasses[targets.get(0)] += ptm.getMass();
                        negativePTMMass = Math.min(negativePTMMass, ptm.getMass());
                        modifictationLabels[targets.get(0)] = modification;
                        modifictationFlags[targets.get(0)] = true;
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
                    modifictationFlags[128 * (1 + modificationCounts[targets.get(0)]) + targets.get(0)] = true;
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
        occurrenceTablePrimary = new WaveletTree(bwt, alphabet, waitingHandler, hasPTMatTerminus);
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
            occurrenceTableReversed = new WaveletTree(bwt, alphabet, waitingHandler, hasPTMatTerminus);
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
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptide, SequenceMatchingPreferences seqMatchPref) {

        HashMap<String, HashMap<String, ArrayList<Integer>>> allMatches = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

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
            backwardList[0].add(new MatrixContent(0, indexStringLength - 1, '\0', null, 0)); // L, R, char, previous content, num of X
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

    /**
     * Adding modifications for backward search suggestions
     *
     * @param setCharacter the set characters
     */
    private void addModifications(ArrayList<Integer[]> setCharacter) {
        int maxNum = setCharacter.size();
        for (int i = 0; i < maxNum; ++i) {
            int pos = 128 + setCharacter.get(i)[0];
            while (pos < aaMasses.length && aaMasses[pos] != -1) {
                setCharacter.add(new Integer[]{setCharacter.get(i)[0], setCharacter.get(i)[1], setCharacter.get(i)[2], pos, 0});
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
    private void mappingSequenceAndMasses(TagElement[] combinations, LinkedList<MatrixContent> matrix, ArrayList<MatrixContent> matrixFinished, int[] less, WaveletTree occurrence, double massTolerance) {
        final int lenCombinations = combinations.length;

        while (!matrix.isEmpty()) {
            MatrixContent cell = matrix.removeFirst();
            final int combinationLength = cell.combinationLength;
            final int pepLen = cell.length;
            final int leftIndexOld = cell.left;
            final int rightIndexOld = cell.right;
            TagElement combination = combinations[combinationLength];

            if (combination.isMass) {
                final double combinationMass = combination.mass;
                final double oldMass = cell.mass;

                ArrayList<Integer[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                if (withVariableModifications) {
                    addModifications(setCharacter);
                }
                for (Integer[] borders : setCharacter) {
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
                            MatrixContent newCell = new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, null, combinationLength + 1, pepLen + 1, 0, null, null, borders[3]);
                            List insertList = (combinationLength + 1 < lenCombinations) ? matrix : matrixFinished;
                            insertList.add(newCell);
                        } else {
                            matrix.add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, null, combinationLength, pepLen + 1, 0, null, null, borders[3]));
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
                        MatrixContent newCell = new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, null, combinationLength + 1, pepLen + 1, newNumX, null, null, -1);
                        List insertList = (combinationLength + 1 < lenCombinations) ? matrix : matrixFinished;
                        insertList.add(newCell);
                    }
                }
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
     * @param frontDirection is the direction forward to the text
     */
    private void mappingSequenceAndMasses(TagElement[] combinations, LinkedList<MatrixContent> matrix, ArrayList<MatrixContent> matrixFinished, int[] less, WaveletTree occurrence, double massTolerance, boolean CTermDirection) {

        final int lenCombinations = combinations.length;
        while (!matrix.isEmpty()) {

            MatrixContent cell = matrix.removeFirst();
            final int combinationLength = cell.combinationLength;
            final int pepLen = cell.length;
            final int leftIndexOld = cell.left;
            final int rightIndexOld = cell.right;
            TagElement combination = combinations[combinationLength];

            if (combination.isMass) {
                final double combinationMass = combination.mass;
                final double oldMass = cell.mass;

                ArrayList<Integer[]> setCharacter = occurrence.rangeQuery(leftIndexOld - 1, rightIndexOld);
                if (withVariableModifications) {
                    addModifications(setCharacter);
                }

                if (combinationLength == lenCombinations - 1) {
                    for (Integer[] borders : setCharacter) {
                        final int aminoAcid = borders[0];

                        if (aminoAcid != '/') {
                            final double newMass = oldMass + aaMasses[borders[3]];
                            double massDiff = combinationMass - newMass;
                            int lastAcid = aminoAcid;
                            final int lessValue = less[aminoAcid];
                            final int leftIndex = lessValue + borders[1];
                            final int rightIndex = lessValue + borders[2] - 1;
                            //ModificationMatch modificationMatch = modifictationFlags[borders[3]] ? new ModificationMatch(modifictationLabels[borders[3]], (borders[3] >= 128), pepLen) : null;
                            MatrixContent newCell = new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, null, combinationLength, pepLen + 1, 0, null, null, borders[3]);

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
                                        modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, pepLen);
                                    }

                                    if (!endOfPeptide && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                        for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                            if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                endOfPeptide = true;
                                                modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, pepLen);
                                                modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, pepLen);
                                            }
                                        }
                                    }
                                    // variable undefined peptide terminal modifictation
                                    if (!endOfPeptide && vmodp != null) {
                                        for (int j = 0; j < vmodp.size(); ++j) {
                                            if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                endOfPeptide = true;
                                                modificationMatchEnd = new ModificationMatch(fmodpaa[lastAcid].get(i), false, pepLen);
                                                modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), false, pepLen);
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
                                        modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, pepLen);
                                    }

                                    if (!endOfPeptide && vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                        for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {
                                            if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                endOfPeptide = true;
                                                modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, pepLen);
                                                modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, pepLen);
                                            }
                                        }
                                    }
                                    // variable undefined peptide terminal modifictation
                                    if (!endOfPeptide && vmodp != null) {
                                        for (int j = 0; j < vmodp.size(); ++j) {
                                            if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                endOfPeptide = true;
                                                modificationMatchEnd = new ModificationMatch(fmodp.get(i), false, pepLen);
                                                modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), false, pepLen);
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
                                            modificationMatchEnd = new ModificationMatch(vmodpaa[lastAcid].get(i), true, pepLen);
                                        }
                                    }
                                }
                                // variable undefined peptide terminal modifictation
                                if (!endOfPeptide && vmodp != null) {
                                    for (int i = 0; i < vmodp.size(); ++i) {
                                        if (Math.abs(massDiff - vmodpMass.get(i)) < massTolerance) {
                                            endOfPeptide = true;
                                            modificationMatchEnd = new ModificationMatch(vmodp.get(i), false, pepLen);
                                        }
                                    }
                                }
                            }

                            if (!endOfPeptide) {
                                if (newMass - massTolerance + negativePTMMass <= combinationMass) {
                                    matrix.add(newCell);
                                }
                            } else if (modificationMatchEnd != null) {
                                MatrixContent newEndCell = new MatrixContent(leftIndex, rightIndex, '\0', newCell, 0, null, combinationLength, pepLen, 0, modificationMatchEnd, null, -1);
                                if (modificationMatchEndEnd == null) {
                                    matrixFinished.add(newEndCell);
                                } else {
                                    MatrixContent newEndEndCell = new MatrixContent(leftIndex, rightIndex, '\0', newEndCell, 0, null, combinationLength, pepLen, 0, modificationMatchEndEnd, null, -1);
                                    matrixFinished.add(newEndEndCell);
                                }
                            } else {
                                matrixFinished.add(newCell);
                            }

                        } else if (pepLen > 1) {

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

                            boolean hasFixed = false;

                            // fixed aa defined protein terminal modification
                            if (fmodaa != null && lastAcid > 0 && fmodaaMass[lastAcid].size() > 0) {
                                hasFixed = true;
                                for (int i = 0; i < fmodaaMass[lastAcid].size(); ++i) {
                                    Double massDiffDiff = massDiff - fmodaaMass[lastAcid].get(i);
                                    if (Math.abs(massDiffDiff) < massTolerance) {
                                        modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, pepLen - 1);
                                    }

                                    // variable aa defined protein terminal modification
                                    if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                        for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                            if (Math.abs(massDiffDiff - vmodaaMass[lastAcid].get(j)) < massTolerance) {
                                                modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, pepLen - 1);
                                                modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), true, pepLen - 1);
                                            }
                                        }
                                    }
                                    // variable undefined protein terminal modifictation
                                    if (vmod != null && modificationMatchEnd == null) {
                                        for (int j = 0; j < vmod.size(); ++j) {
                                            if (Math.abs(massDiffDiff - vmodMass.get(j)) < massTolerance) {
                                                modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, pepLen - 1);
                                                modificationMatchEndEnd = new ModificationMatch(vmod.get(j), false, pepLen - 1);
                                            }
                                        }
                                    }

                                    // second ptm at peptide terminus
                                    boolean hasFixedPep = false;
                                    if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                        for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {

                                            if (Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                hasFixedPep = true;
                                                modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, pepLen - 1);
                                                modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, pepLen - 1);
                                            }
                                        }
                                    }

                                    if (fmodp != null) {
                                        for (int j = 0; j < fmodp.size(); ++j) {
                                            if (Math.abs(massDiffDiff - fmodpMass.get(j)) < massTolerance) {
                                                hasFixedPep = true;
                                                modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), false, pepLen - 1);
                                                modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, pepLen - 1);
                                            }
                                        }
                                    }

                                    if (!hasFixedPep) {
                                        if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), true, pepLen - 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, pepLen - 1);
                                                }
                                            }
                                        }

                                        if (vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(fmodaa[lastAcid].get(i), true, pepLen - 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, pepLen - 1);
                                                }
                                            }
                                        }
                                    }

                                }

                            }

                            // fixed undefined protein terminal modifictation
                            if (fmod != null && modificationMatchEnd == null) {

                                hasFixed = true;
                                for (int i = 0; i < fmod.size(); ++i) {
                                    Double massDiffDiff = massDiff - fmodMass.get(i);
                                    if (Math.abs(massDiffDiff) < massTolerance) {
                                        modificationMatchEnd = new ModificationMatch(fmod.get(i), false, pepLen - 1);
                                    }

                                    // variable aa defined protein terminal modification
                                    if (vmodaa != null && lastAcid > 0 && vmodaaMass[lastAcid].size() > 0) {
                                        for (int j = 0; j < vmodaaMass[lastAcid].size(); ++j) {
                                            if (Math.abs(massDiff - vmodaaMass[lastAcid].get(j)) < massTolerance) {
                                                modificationMatchEnd = new ModificationMatch(fmod.get(i), false, pepLen - 1);
                                                modificationMatchEndEnd = new ModificationMatch(vmodaa[lastAcid].get(j), true, pepLen - 1);
                                            }
                                        }
                                    }
                                    // variable undefined protein terminal modifictation
                                    if (vmod != null && modificationMatchEnd == null) {
                                        for (int j = 0; j < vmod.size(); ++j) {
                                            if (Math.abs(massDiff - vmodMass.get(j)) < massTolerance) {
                                                modificationMatchEnd = new ModificationMatch(fmod.get(i), false, pepLen - 1);
                                                modificationMatchEndEnd = new ModificationMatch(vmod.get(j), false, pepLen - 1);
                                            }
                                        }
                                    }

                                    // second ptm at peptide terminus
                                    boolean hasFixedPep = false;
                                    if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                        for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {

                                            if (Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                hasFixedPep = true;
                                                modificationMatchEnd = new ModificationMatch(fmod.get(i), false, pepLen - 1);
                                                modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, pepLen - 1);
                                            }
                                        }
                                    }

                                    if (fmodp != null) {
                                        for (int j = 0; j < fmodp.size(); ++j) {
                                            if (Math.abs(massDiffDiff - fmodpMass.get(j)) < massTolerance) {
                                                hasFixedPep = true;
                                                modificationMatchEnd = new ModificationMatch(fmod.get(i), false, pepLen - 1);
                                                modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, pepLen - 1);
                                            }
                                        }
                                    }

                                    if (!hasFixedPep) {
                                        if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, pepLen - 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, pepLen - 1);
                                                }
                                            }
                                        }

                                        if (vmodp != null) {
                                            for (int j = 0; j < vmodp.size(); ++j) {
                                                if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(fmod.get(i), false, pepLen - 1);
                                                    modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, pepLen - 1);
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
                                            modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, pepLen - 1);
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {

                                                if (Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, pepLen - 1);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, pepLen - 1);
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                if (Math.abs(massDiffDiff - fmodpMass.get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, pepLen - 1);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, pepLen - 1);
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                    if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, pepLen - 1);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, pepLen - 1);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(vmodaa[lastAcid].get(i), true, pepLen - 1);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, pepLen - 1);
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
                                            modificationMatchEnd = new ModificationMatch(vmod.get(i), false, pepLen - 1);
                                        }

                                        // second ptm at peptide terminus
                                        boolean hasFixedPep = false;
                                        if (fmodpaa != null && lastAcid > 0 && fmodpaaMass[lastAcid].size() > 0) {
                                            for (int j = 0; j < fmodpaaMass[lastAcid].size(); ++j) {
                                                if (Math.abs(massDiffDiff - fmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(vmod.get(i), false, pepLen - 1);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodpaa[lastAcid].get(j), false, pepLen - 1);
                                                }
                                            }
                                        }

                                        if (fmodp != null) {
                                            for (int j = 0; j < fmodp.size(); ++j) {
                                                if (Math.abs(massDiffDiff - fmodpMass.get(j)) < massTolerance) {
                                                    hasFixedPep = true;
                                                    modificationMatchEnd = new ModificationMatch(vmod.get(i), false, pepLen - 1);
                                                    modificationMatchEndEnd = new ModificationMatch(fmodp.get(j), false, pepLen - 1);
                                                }
                                            }
                                        }

                                        if (!hasFixedPep) {
                                            if (vmodpaa != null && lastAcid > 0 && vmodpaaMass[lastAcid].size() > 0) {
                                                for (int j = 0; j < vmodpaaMass[lastAcid].size(); ++j) {

                                                    if (Math.abs(massDiffDiff - vmodpaaMass[lastAcid].get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), false, pepLen - 1);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodpaa[lastAcid].get(j), true, pepLen - 1);
                                                    }
                                                }
                                            }

                                            if (vmodp != null) {
                                                for (int j = 0; j < vmodp.size(); ++j) {
                                                    if (Math.abs(massDiffDiff - vmodpMass.get(j)) < massTolerance) {
                                                        hasFixedPep = true;
                                                        modificationMatchEnd = new ModificationMatch(vmod.get(i), false, pepLen - 1);
                                                        modificationMatchEndEnd = new ModificationMatch(vmodp.get(j), true, pepLen - 1);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (modificationMatchEnd != null) {
                                MatrixContent newEndCell = new MatrixContent(leftIndexOld, rightIndexOld, '\0', cell, 0, null, combinationLength, pepLen, 0, modificationMatchEnd, null, -1);
                                if (modificationMatchEndEnd == null) {
                                    matrixFinished.add(newEndCell);
                                } else {
                                    MatrixContent newEndEndCell = new MatrixContent(leftIndexOld, rightIndexOld, '\0', newEndCell, 0, null, combinationLength, pepLen, 0, modificationMatchEndEnd, null, -1);
                                    matrixFinished.add(newEndEndCell);
                                }
                            }
                        }
                    }
                } else {
                    for (Integer[] borders : setCharacter) {
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
                                MatrixContent newCell = new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, null, combinationLength + 1, pepLen + 1, 0, null, null, borders[3]);
                                List insertList = (combinationLength + 1 < lenCombinations) ? matrix : matrixFinished;
                                insertList.add(newCell);
                            } else {
                                matrix.add(new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, newMass, null, combinationLength, pepLen + 1, 0, null, null, borders[3]));
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
                        MatrixContent newCell = new MatrixContent(leftIndex, rightIndex, aminoAcid, cell, 0, null, combinationLength + 1, pepLen + 1, newNumX, null, null, -1);
                        List insertList = (combinationLength + 1 < lenCombinations) ? matrix : matrixFinished;
                        insertList.add(newCell);
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
        int lenCombinations = combinations.length;

        LinkedList<MatrixContent> matrixReversed = new LinkedList<MatrixContent>();
        ArrayList<MatrixContent> matrixReversedFinished = new ArrayList<MatrixContent>();
        LinkedList<MatrixContent> matrix = new LinkedList<MatrixContent>();
        ArrayList<MatrixContent> matrixFinished = new ArrayList<MatrixContent>();
        ArrayList<MatrixContent> cachePrimary = new ArrayList<MatrixContent>();

        if (cached != null) {
            for (MatrixContent matrixContent : cached) {
                matrix.add(matrixContent);
            }
        } else {
            // left index, right index, current character, previous matrix content, mass, peptideSequence, peptide length, number of X
            matrixReversed.add(new MatrixContent(0, indexStringLength - 1, '\0', null, 0, null, 0, 1, 0, null, null, -1));
        }

        if (cached == null) {
            // Map Reverse
            if (!hasCTermDirection) {
                mappingSequenceAndMasses(combinationsReversed, matrixReversed, matrixReversedFinished, lessReversed, occurrenceReversed, massTolerance);
            } else {
                mappingSequenceAndMasses(combinationsReversed, matrixReversed, matrixReversedFinished, lessReversed, occurrenceReversed, massTolerance, towardsC);
            }

            // Traceback Reverse
            for (MatrixContent content : matrixReversedFinished) {
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
                            if (modifictationFlags[currentContent.modificationPos]) {
                                modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.modificationPos >= 128, currentContent.length - 1));
                            }
                        } else {
                            modifications.add(currentContent.modification);
                        }
                    }
                    currentContent = currentContent.previousContent;
                }
                String reversePeptide = (new StringBuilder(currentPeptide).reverse()).toString();
                cachePrimary.add(new MatrixContent(leftIndexFront, rightIndexFront, reversePeptide.charAt(0), null, 0, reversePeptide, 0, content.length, 0, null, modifications, -1));
            }

            if (lenCombinations > 0) {
                for (MatrixContent matrixContent : cachePrimary) {
                    matrix.add(matrixContent);
                }
            } else {
                for (MatrixContent matrixContent : cachePrimary) {
                    matrixFinished.add(matrixContent);
                }
            }

            cacheIt(refTagContent, cachePrimary);
        }

        if (!matrix.isEmpty()) {
            // Map towards NTerm
            if (!hasNTermDirection) {
                mappingSequenceAndMasses(combinations, matrix, matrixFinished, lessPrimary, occurrencePrimary, massTolerance);
            } else {
                mappingSequenceAndMasses(combinations, matrix, matrixFinished, lessPrimary, occurrencePrimary, massTolerance, !towardsC);
            }
        }
        // Traceback from NTerm
        for (MatrixContent content : matrixFinished) {
            MatrixContent currentContent = content;
            String currentPeptide = "";
            ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();

            while (currentContent.previousContent != null) {
                if (currentContent.character != '\0') {
                    currentPeptide += (char) currentContent.character;
                }

                /*
                if (currentContent.modification != null) {
                    modifications.add(currentContent.modification);
                }*/
                if (currentContent.modification != null || currentContent.modificationPos >= 0) {
                    if (currentContent.modificationPos >= 0) {
                        if (modifictationFlags[currentContent.modificationPos]) {
                            modifications.add(new ModificationMatch(modifictationLabels[currentContent.modificationPos], currentContent.modificationPos >= 128, currentContent.length - 1));
                        }
                    } else {
                        modifications.add(currentContent.modification);
                    }
                }

                currentContent = currentContent.previousContent;
            }
            for (ModificationMatch modificationMatch : modifications) {
                modificationMatch.setModificationSite(currentContent.peptideSequence.length() + currentPeptide.length() - modificationMatch.getModificationSite() + 1);
            }

            int leftIndex = content.left;
            int rightIndex = content.right;

            for (ModificationMatch modificationMatch : currentContent.modifications) {
                modificationMatch.setModificationSite(modificationMatch.getModificationSite() + currentPeptide.length());
                modifications.add(modificationMatch);
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

            allMatches.put(new Peptide(peptide, modifications), matches);
        }

        /*
        if (tag.getContent().size() == 3){
            ArrayList<TagComponent> tc = tag.getContent();
            for (Peptide pep : allMatches.keySet()){
                for (String acc : allMatches.get(pep).keySet()){
                    for (int pos : allMatches.get(pep).get(acc)){
                        System.out.println(tc.get(0).getMass() + " " + tc.get(1).asSequence() + " " + tc.get(2).getMass() + " " + pep.getSequence() + " " + acc + " " + pos);
                    }
                }
            }
        }*/
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
     * Caching intermediate results of previous tag to proteome matches.
     *
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
