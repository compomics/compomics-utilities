package com.compomics.util.experiment.identification.utils;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideVariantMatches;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class groups functions that can be used to work with peptides.
 *
 * @author Marc Vaudel
 */
public class PeptideUtils {

    /**
     * Returns a boolean indicating whether the peptide matches a decoy
     * sequence.
     *
     * @param peptide the peptide
     * @param sequenceProvider a sequence provider.
     *
     * @return a boolean indicating whether the peptide matches a decoy sequence
     */
    public static boolean isDecoy(Peptide peptide, SequenceProvider sequenceProvider) {

        return peptide.getProteinMapping().navigableKeySet().stream()
                .anyMatch(accession -> sequenceProvider.getDecoyAccessions().contains(accession));

    }

    /**
     * Returns the amino acids before the given peptide as a string in a map
     * based on the peptide protein mapping.
     *
     * @param peptide the peptide
     * @param accession the accession of the protein
     * @param index the position of the peptide on the protein sequence
     * @param nAa the number of amino acids to include
     * @param sequenceProvider the sequence provider
     *
     * @return the amino acids before the given peptide as a string in a map
     * based on the peptide protein mapping
     */
    public static String getAaBefore(Peptide peptide, String accession, int index, int nAa, SequenceProvider sequenceProvider) {

        return sequenceProvider.getSubsequence(accession, index - nAa - 1, index - 1);

    }

    /**
     * Returns the amino acids before the given peptide as a string in a map
     * based on the peptide protein mapping.
     *
     * @param peptide the peptide
     * @param nAa the number of amino acids to include
     * @param sequenceProvider the sequence provider
     *
     * @return the amino acids before the given peptide as a string in a map
     * based on the peptide protein mapping
     */
    public static TreeMap<String, String[]> getAaBefore(Peptide peptide, int nAa, SequenceProvider sequenceProvider) {

        return peptide.getProteinMapping().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> Arrays.stream(entry.getValue())
                                .mapToObj(index -> getAaAfter(peptide, entry.getKey(), index, nAa, sequenceProvider))
                                .toArray(String[]::new),
                        (a, b) -> {
                            throw new IllegalArgumentException("Duplicate key.");
                        },
                        TreeMap::new));

    }

    /**
     * Returns the amino acids before the given peptide as a string.
     *
     * @param peptide the peptide
     * @param accession the accession of the protein
     * @param index the position of the peptide on the protein sequence
     * @param nAa the number of amino acids to include
     * @param sequenceProvider the sequence provider
     *
     * @return the amino acids before the given peptide as a string in a map
     * based on the peptide protein mapping
     */
    public static String getAaAfter(Peptide peptide, String accession, int index, int nAa, SequenceProvider sequenceProvider) {

        return sequenceProvider.getSubsequence(accession, index + peptide.getSequence().length(), index + peptide.getSequence().length() + nAa);

    }

    /**
     * Returns the amino acids before the given peptide as a string in a map
     * based on the peptide protein mapping.
     *
     * @param peptide the peptide
     * @param nAa the number of amino acids to include
     * @param sequenceProvider the sequence provider
     *
     * @return the amino acids before the given peptide as a string in a map
     * based on the peptide protein mapping
     */
    public static TreeMap<String, String[]> getAaAfter(Peptide peptide, int nAa, SequenceProvider sequenceProvider) {

        return peptide.getProteinMapping().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> Arrays.stream(entry.getValue())
                                .mapToObj(index -> getAaAfter(peptide, entry.getKey(), index, nAa, sequenceProvider))
                                .toArray(String[]::new),
                        (a, b) -> {
                            throw new IllegalArgumentException("Duplicate key.");
                        },
                        TreeMap::new));

    }

    /**
     * Returns the peptide modifications as a string.
     *
     * @param peptide the peptide
     *
     * @return the peptide modifications as a string
     */
    public static String getVariableModificationsAsString(Peptide peptide) {

        TreeMap<String, HashSet<Integer>> modMap = Arrays.stream(peptide.getVariableModifications())
                .collect(Collectors.groupingBy(ModificationMatch::getModification,
                        TreeMap::new,
                        Collectors.mapping(ModificationMatch::getSite,
                                Collectors.toCollection(HashSet::new))));

        return modMap.entrySet().stream()
                .map(entry -> getModificationString(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(";"));
    }

    /**
     * Returns the peptide modifications as a string.
     *
     * @param peptide the peptide
     *
     * @return the peptide modifications as a string
     * @param modificationParameters the modification parameters
     * @param sequenceProvider a provider for the protein sequences
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences for modification to peptide mapping
     */
    public static String getFixedModificationsAsString(Peptide peptide, ModificationParameters modificationParameters, SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationSequenceMatchingParameters) {

        String[] fixedModifications = peptide.getFixedModifications(modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);

        TreeMap<String, HashSet<Integer>> modMap = IntStream.range(0, fixedModifications.length)
                .mapToObj(i -> fixedModifications[i])
                .filter(modName -> modName != null)
                .collect(Collectors.groupingBy(Function.identity(),
                        TreeMap::new,
                        Collectors.mapping(i -> new Integer(i),
                                Collectors.toCollection(HashSet::new))));

        return modMap.entrySet().stream()
                .map(entry -> getModificationString(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(";"));
    }

    /**
     * Returns the modification and sites as string in the form
     * modName(site1,site2).
     *
     * @param modificationName the name of the modification
     * @param sites the modification sites
     *
     * @return the modification and sites as string
     */
    private static String getModificationString(String modificationName, HashSet<Integer> sites) {

        String sitesString = sites.stream()
                .sorted()
                .map(site -> site.toString())
                .collect(Collectors.joining(","));

        StringBuilder sb = new StringBuilder(modificationName.length() + sitesString.length() + 2);

        sb.append(modificationName).append("(").append(sitesString).append(")");

        return sb.toString();
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with Modification tags, e.g,
     * &lt;mox&gt;. /!\ This method will work only if the Modification found in
     * the peptide are in the ModificationFactory. Modifications should be
     * provided indexed by site as follows: N-term modifications are at index 0,
     * C-term at sequence length + 1, and amino acid at 1-based index on the
     * sequence.
     *
     * @param modificationParameters the modification profile of the search
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param peptide the peptide to annotate
     * @param confidentModificationSites the confidently localized variable
     * modification sites indexed by site.
     * @param representativeAmbiguousModificationSites the representative site
     * of the ambiguously localized variable modifications in a map: aa number
     * &gt; list of modifications (1 is the first AA) (can be null)
     * @param secondaryAmbiguousModificationSites the secondary sites of the
     * ambiguously localized variable modifications in a map: aa number &gt;
     * list of modifications (1 is the first AA) (can be null)
     * @param fixedModificationSites the fixed modification sites in a map: aa
     * number &gt; list of modifications (1 is the first AA) (can be null)
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * Modification tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     *
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(Peptide peptide, ModificationParameters modificationParameters,
            String[] confidentModificationSites, String[] representativeAmbiguousModificationSites, String[] secondaryAmbiguousModificationSites, String[] fixedModificationSites,
            boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {

        String peptideSequence = peptide.getSequence();

        if (confidentModificationSites == null) {

            confidentModificationSites = new String[peptideSequence.length() + 2];

        }

        if (representativeAmbiguousModificationSites == null) {

            representativeAmbiguousModificationSites = new String[peptideSequence.length() + 2];

        }

        if (secondaryAmbiguousModificationSites == null) {

            secondaryAmbiguousModificationSites = new String[peptideSequence.length() + 2];

        }

        if (fixedModificationSites == null) {

            fixedModificationSites = new String[peptideSequence.length() + 2];

        }

        StringBuilder modifiedSequence = new StringBuilder(peptideSequence.length());

        if (useHtmlColorCoding && includeHtmlStartEndTags) {

            modifiedSequence.append("<html>");

        }

        String nTermAsString = getNtermAsString(confidentModificationSites, representativeAmbiguousModificationSites, secondaryAmbiguousModificationSites, fixedModificationSites);
        String cTermAsString = getCtermAsString(peptideSequence.length(), confidentModificationSites, representativeAmbiguousModificationSites, secondaryAmbiguousModificationSites, fixedModificationSites);

        modifiedSequence.append(nTermAsString).append('-');
        modifiedSequence.append(ModificationUtils.getTaggedModifiedSequence(modificationParameters, peptideSequence, 
                confidentModificationSites, representativeAmbiguousModificationSites, secondaryAmbiguousModificationSites, 
                fixedModificationSites, useHtmlColorCoding, useShortName));
        modifiedSequence.append('-').append(cTermAsString);

        if (useHtmlColorCoding && includeHtmlStartEndTags) {

            modifiedSequence.append("</html>");

        }

        return modifiedSequence.toString();

    }

    /**
     * Returns the N-terminal annotation as string.
     *
     * @param modificationArrays modifications to annotate in arrays
     * corresponding to the peptide sequence with N-terminus at index 0
     *
     * @return the N-terminal annotation as string
     */
    public static String getNtermAsString(String[]... modificationArrays) {

        for (String[] modificationArray : modificationArrays) {

            String modName = modificationArray[0];

            if (modName != null) {

                return modName.replaceAll(" ", ".");

            }

        }

        return "NH2";

    }

    /**
     * Returns the C-terminal annotation as string.
     *
     * @param length the length of the peptide
     * @param modificationArrays modifications to annotate in arrays
     * corresponding to the peptide sequence with C-terminus at index length + 2
     *
     * @return the C-terminal annotation as string
     */
    public static String getCtermAsString(int length, String[]... modificationArrays) {

        for (String[] modificationArray : modificationArrays) {

            String modName = modificationArray[length + 1];

            if (modName != null) {

                return modName.replaceAll(" ", ".");

            }

        }

        return "COOH";

    }

    /**
     * Returns the number of enzymatic termini for the given enzyme on this
     * protein at the given location.
     *
     * @param peptideStart the 0 based index of the peptide start on the protein
     * @param peptideEnd the 0 based index of the peptide end on the protein
     * @param proteinSequence the protein sequence
     * @param enzyme the enzyme to use
     *
     * @return true of the peptide is non-enzymatic
     */
    public static int getNEnzymaticTermini(int peptideStart, int peptideEnd, String proteinSequence, Enzyme enzyme) {

        int nEnzymatic = 0;

        if (peptideStart == 0) {

            nEnzymatic++;

        } else {

            char aaBefore = proteinSequence.charAt(peptideStart - 1);
            char aaAfter = proteinSequence.charAt(peptideStart);

            if (enzyme.isCleavageSite(aaBefore, aaAfter)) {

                nEnzymatic++;

            }

        }

        if (peptideEnd == proteinSequence.length() - 1) {

            nEnzymatic++;

        } else {

            char aaBefore = proteinSequence.charAt(peptideEnd);
            char aaAfter = proteinSequence.charAt(peptideEnd + 1);

            if (enzyme.isCleavageSite(aaBefore, aaAfter)) {

                nEnzymatic++;

            }
        }

        return nEnzymatic;
    }

    /**
     * Returns a boolean indicating whether the peptide is enzymatic using one
     * of the given enzymes.
     *
     * @param peptide the peptide
     * @param proteinAccession the accession of the protein
     * @param proteinSequence the sequence of the protein
     * @param enzymes the enzymes used for digestion
     *
     * @return a boolean indicating whether the peptide is enzymatic using one
     * of the given enzymes
     */
    public static boolean isEnzymatic(Peptide peptide, String proteinAccession, String proteinSequence, ArrayList<Enzyme> enzymes) {

        int[] startIndexes = peptide.getProteinMapping().get(proteinAccession);

        if (startIndexes == null) {
            return false;
        }

        return enzymes.stream()
                .anyMatch(enzyme -> Arrays.stream(startIndexes)
                .anyMatch(startIndex -> getNEnzymaticTermini(
                startIndex,
                peptide.getPeptideEnd(proteinAccession, startIndex),
                proteinSequence,
                enzyme) == 2));
    }

    /**
     * Returns a boolean indicating whether the peptide is enzymatic in at least
     * one protein using one of the given enzymes.
     *
     * @param peptide the peptide
     * @param sequenceProvider the sequence provider
     * @param enzymes the enzymes used for digestion
     *
     * @return a boolean indicating whether the peptide is enzymatic using one
     * of the given enzymes
     */
    public static boolean isEnzymatic(Peptide peptide, SequenceProvider sequenceProvider, ArrayList<Enzyme> enzymes) {

        return peptide.getProteinMapping().entrySet().stream()
                .anyMatch(entry -> isEnzymatic(
                peptide,
                entry.getKey(),
                sequenceProvider.getSequence(entry.getKey()),
                enzymes));

    }

    /**
     * Returns a boolean indicating whether the peptide needs variants to be
     * mapped to the given protein.
     *
     * @param peptide the peptide
     * @param accession the accession of the protein
     *
     * @return a boolean indicating whether the peptide needs variants to be
     * mapped to the given protein
     */
    public static boolean isVariant(Peptide peptide, String accession) {

        int[] indexesOnProtein = peptide.getProteinMapping().get(accession);
        HashMap<Integer, PeptideVariantMatches> variantOnProtein = peptide.getVariantMatches().get(accession);

        return indexesOnProtein.length == variantOnProtein.size();

    }
}
