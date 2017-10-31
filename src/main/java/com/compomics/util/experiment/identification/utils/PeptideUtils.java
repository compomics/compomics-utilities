package com.compomics.util.experiment.identification.utils;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
                                .mapToObj(index -> sequenceProvider.getSubsequence(entry.getKey(), index - nAa - 1, index - 1))
                                .toArray(String[]::new),
                        (a, b) -> {
                            throw new IllegalArgumentException("Duplicate key.");
                        },
                        TreeMap::new));

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
                                .mapToObj(index -> sequenceProvider.getSubsequence(entry.getKey(), index + peptide.getSequence().length(), index + peptide.getSequence().length() + nAa))
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
     * @param variable if true, only variable Modifications are shown, false
     * return only the fixed Modifications
     *
     * @return the peptide modifications as a string
     */
    public static String getPeptideModificationsAsString(Peptide peptide, boolean variable) {

        TreeMap<String, HashSet<Integer>> modMap = Arrays.stream(peptide.getModificationMatches())
                .filter(modificationMatch -> modificationMatch.getVariable() == variable)
                .collect(Collectors.groupingBy(ModificationMatch::getModification,
                        TreeMap::new,
                        Collectors.mapping(ModificationMatch::getModificationSite,
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
     * the peptide are in the ModificationFactory.
     *
     * @param modificationProfile the modification profile of the search
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param peptide the peptide to annotate
     * @param confidentModificationSites the confidently localized variable
     * modification sites in a map: aa number &gt; list of modifications (1 is
     * the first AA) (can be null)
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
    public static String getTaggedModifiedSequence(Peptide peptide, ModificationParameters modificationProfile, HashMap<Integer, ArrayList<String>> confidentModificationSites, HashMap<Integer, ArrayList<String>> representativeAmbiguousModificationSites, HashMap<Integer, ArrayList<String>> secondaryAmbiguousModificationSites, HashMap<Integer, ArrayList<String>> fixedModificationSites, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {
        if (confidentModificationSites == null) {
            confidentModificationSites = new HashMap<>(0);
        }
        if (representativeAmbiguousModificationSites == null) {
            representativeAmbiguousModificationSites = new HashMap<>(0);
        }
        if (secondaryAmbiguousModificationSites == null) {
            secondaryAmbiguousModificationSites = new HashMap<>(0);
        }
        if (fixedModificationSites == null) {
            fixedModificationSites = new HashMap<>(0);
        }
        String modifiedSequence = "";
        if (useHtmlColorCoding && includeHtmlStartEndTags) {
            modifiedSequence += "<html>";
        }
        modifiedSequence += peptide.getNTerminal() + "-";
        modifiedSequence += AminoAcidSequence.getTaggedModifiedSequence(modificationProfile, peptide.getSequence(), confidentModificationSites, representativeAmbiguousModificationSites, secondaryAmbiguousModificationSites, fixedModificationSites, useHtmlColorCoding, useShortName);
        modifiedSequence += "-" + peptide.getCTerminal();
        if (useHtmlColorCoding && includeHtmlStartEndTags) {
            modifiedSequence += "</html>";
        }
        return modifiedSequence;
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
     * Returns a boolean indicating whether the peptide is enzymatic using one of the given enzymes.
     * 
     * @param peptide the peptide
     * @param proteinAccession the accession of the protein
     * @param proteinSequence the sequence of the protein
     * @param enzymes the enzymes used for digestion
     * 
     * @return a boolean indicating whether the peptide is enzymatic using one of the given enzymes
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
     * Returns a boolean indicating whether the peptide is enzymatic in at least one protein using one of the given enzymes.
     * 
     * @param peptide the peptide
     * @param sequenceProvider the sequence provider
     * @param enzymes the enzymes used for digestion
     * 
     * @return a boolean indicating whether the peptide is enzymatic using one of the given enzymes
     */
    public static boolean isEnzymatic(Peptide peptide, SequenceProvider sequenceProvider, ArrayList<Enzyme> enzymes) {
        
        return peptide.getProteinMapping().entrySet().stream()
                .anyMatch(entry -> isEnzymatic(
                        peptide, 
                        entry.getKey(), 
                        sequenceProvider.getSequence(entry.getKey()), 
                        enzymes));
        
    }
}
