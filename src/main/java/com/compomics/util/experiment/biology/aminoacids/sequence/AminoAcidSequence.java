package com.compomics.util.experiment.biology.aminoacids.sequence;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.Util;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.awt.Color;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class represents a series of amino acids with associated modifications.
 *
 * @author Marc Vaudel
 */
public class AminoAcidSequence extends ExperimentObject implements TagComponent {

    /**
     * The sequence as string.
     */
    private String sequence;
    /**
     * The sequence as string builder.
     */
    private StringBuilder sequenceStringBuilder = null;
    /**
     * The modifications carried by the amino acid sequence at target amino
     * acids. 1 is the first amino acid.
     */
    private ModificationMatch[] modifications = null;
    /**
     * Convenience array for no modifications
     */
    private static final ModificationMatch[] noMod = new ModificationMatch[0];

    /**
     * Creates a blank sequence. All maps are null.
     */
    public AminoAcidSequence() {
    }

    /**
     * Constructor taking a sequence of amino acids as input.
     *
     * @param sequence a sequence of amino acids
     */
    public AminoAcidSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Constructor taking a sequence of amino acids as input.
     *
     * @param sequence a sequence of amino acids
     * @param modifications the modifications of this sequence in a map
     */
    public AminoAcidSequence(String sequence, ModificationMatch[] modifications) {
        this.sequence = sequence;
        this.modifications = modifications;
    }

    /**
     * Creates a sequence from another sequence.
     *
     * @param sequence the other sequence
     */
    public AminoAcidSequence(AminoAcidSequence sequence) {

        this.sequence = sequence.getSequence();
        ModificationMatch[] modificationMatches = sequence.getModificationMatches();

        if (modificationMatches != null) {

            modifications = Arrays.stream(modificationMatches)
                    .map(ModificationMatch::clone)
                    .toArray(ModificationMatch[]::new);

        }
    }

    /**
     * Returns the sequence as String.
     *
     * @return the sequence as String
     */
    public String getSequence() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(false);

        return sequence == null ? "" : sequence;

    }

    /**
     * Returns the amino acid at the given index on the sequence in its single
     * letter code. 0 is the first amino acid.
     *
     * @param aa the index on the sequence
     *
     * @return the amino acid at the given index on the sequence in its single
     * letter code
     */
    public char charAt(int aa) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(false);

        return sequence.charAt(aa);

    }

    /**
     * Returns the amino acid at the given index on the sequence. 0 is the first
     * amino acid.
     *
     * @param aa the index on the sequence
     *
     * @return the amino acid at the given index on the sequence
     */
    public AminoAcid getAminoAcidAt(int aa) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return AminoAcid.getAminoAcid(charAt(aa));

    }

    /**
     * Sets the sequence.
     *
     * @param aminoAcidSequence the sequence
     */
    public void setSequence(String aminoAcidSequence) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        sequenceStringBuilder = null;

        this.sequence = aminoAcidSequence;

    }

    /**
     * replaces the amino acid at the given position by the given amino acid
     * represented by its single letter code. 0 is the first amino acid.
     *
     * @param index the index where the amino acid should be set.
     * @param aa the amino acid to be set
     */
    public void setAaAtIndex(int index, char aa) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(true);
        sequenceStringBuilder.setCharAt(index, aa);

    }

    /**
     * Loads the sequence in the string builder.
     */
    private void setSequenceStringBuilder(boolean stringbuilder) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        if (stringbuilder && sequenceStringBuilder == null) {

            if (sequence != null) {

                sequenceStringBuilder = new StringBuilder(sequence);
                sequence = null;

            } else {

                sequenceStringBuilder = new StringBuilder(1);

            }

        } else if (sequence == null && sequenceStringBuilder != null) {

            sequence = sequenceStringBuilder.toString();

        }
    }

    /**
     * the sequence is kept in different formats internally. Calling this method
     * removes them from the cache.
     */
    public void emptyInternalCaches() {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        sequenceStringBuilder = null;

    }

    /**
     * Indicates whether the sequence is found in the given amino acid sequence.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the sequence is found in the given
     * amino acid sequence
     */
    public boolean matchesIn(String aminoAcidSequence, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return firstIndex(aminoAcidSequence, sequenceMatchingPreferences) >= 0;

    }

    /**
     * Indicates whether the sequence is found in the given amino acid sequence.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the sequence is found in the given
     * amino acid sequence
     */
    public boolean matchesIn(AminoAcidSequence aminoAcidSequence, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return matchesIn(aminoAcidSequence.getSequence(), sequenceMatchingPreferences);

    }

    /**
     * Indicates whether the sequence matches the given amino acid sequence in
     * size and according to the given matching preferences.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the sequence is found in the given
     * amino acid sequence
     */
    public boolean matches(String aminoAcidSequence, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return length() == aminoAcidSequence.length() && firstIndex(aminoAcidSequence, sequenceMatchingPreferences) >= 0;

    }

    /**
     * Indicates whether the sequence matches the given amino acid sequence in
     * size and according to the given matching preferences.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the sequence is found in the given
     * amino acid sequence
     */
    public boolean matches(AminoAcidSequence aminoAcidSequence, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return matches(aminoAcidSequence.getSequence(), sequenceMatchingPreferences);

    }

    /**
     * Returns the first index where the amino acid sequence is found in the
     * given sequence. -1 if not found. 0 is the first amino acid.
     *
     * @param aminoAcidSequence the amino acid sequence to look into
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the first index where the amino acid sequence is found
     */
    public int firstIndex(String aminoAcidSequence, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return AminoAcidPattern.getAminoAcidPatternFromString(sequence).firstIndex(aminoAcidSequence, sequenceMatchingPreferences, 0);

    }

    /**
     * Returns the length of the sequence in amino acids.
     *
     * @return the length of the sequence in amino acids
     */
    public int length() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (sequence != null) {

            return sequence.length();

        } else if (sequenceStringBuilder != null) {

            return sequenceStringBuilder.length();

        } else {

            return 0;

        }
    }

    /**
     * Appends another sequence at the end of this sequence.
     *
     * @param otherSequence the other sequence to append.
     */
    public void appendCTerm(AminoAcidSequence otherSequence) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        setSequenceStringBuilder(true);

        int previousLength = length();
        sequenceStringBuilder.append(otherSequence.getSequence());

        ModificationMatch[] otherModifications = otherSequence.getModificationMatches();

        if (otherModifications.length > 0) {

            otherModifications = Arrays.stream(otherModifications)
                    .map(ModificationMatch::clone)
                    .peek(modificationMatch -> modificationMatch.setSite(modificationMatch.getSite() + previousLength))
                    .toArray(ModificationMatch[]::new);

            if (modifications == null) {

                modifications = otherModifications;

            } else {

                ModificationMatch[] mergedModifications = new ModificationMatch[modifications.length + otherModifications.length];
                System.arraycopy(modifications, 0, mergedModifications, 0, modifications.length);
                System.arraycopy(otherModifications, 0, mergedModifications, modifications.length, otherModifications.length);
                modifications = mergedModifications;

            }
        }
    }

    /**
     * Appends a series of unmodified amino acids to the sequence.
     *
     * @param otherSequence a series of unmodified amino acids represented by
     * their single letter code
     */
    public void appendCTerm(String otherSequence) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(true);
        sequenceStringBuilder.append(otherSequence);

    }

    /**
     * Inserts another sequence in this sequence.
     *
     * @param offset the index where this sequence should be inserted, 0 is the
     * first amino acid.
     * @param otherSequence the other sequence to insert.
     */
    public void insert(int offset, AminoAcidSequence otherSequence) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(true);
        sequenceStringBuilder.insert(0, otherSequence.getSequence());
        int otherSequenceLength = otherSequence.length();
        ModificationMatch[] otherModifications = otherSequence.getModificationMatches();

        if (otherModifications.length > 0) {

            otherModifications = Arrays.stream(otherModifications)
                    .map(ModificationMatch::clone)
                    .peek(modificationMatch -> modificationMatch.setSite(modificationMatch.getSite() + offset))
                    .toArray(ModificationMatch[]::new);

            if (modifications == null) {

                modifications = otherModifications;

            } else {

                for (ModificationMatch modificationMatch : modifications) {

                    int oldSite = modificationMatch.getSite();

                    if (oldSite > offset + 1) {

                        int newSite = oldSite + otherSequenceLength;
                        modificationMatch.setSite(newSite);

                    }

                }

                modifications = Arrays.copyOf(modifications, modifications.length + otherModifications.length);
                System.arraycopy(otherModifications, 0, modifications, modifications.length, otherModifications.length);

            }
        }
    }

    /**
     * Inserts another sequence in this sequence.
     *
     * @param offset the index where this sequence should be inserted, 0 is the
     * first amino acid.
     * @param otherSequence the other sequence to insert.
     */
    public void insert(int offset, String otherSequence) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(true);
        sequenceStringBuilder.insert(offset, otherSequence);

    }

    /**
     * Appends another sequence at the beginning of this sequence keeping the
     * original order.
     *
     * @param otherSequence the other sequence to append.
     */
    public void appendNTerm(AminoAcidSequence otherSequence) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        insert(0, otherSequence);

    }

    /**
     * Appends a series of unmodified amino acids to the beginning sequence
     * keeping the original order.
     *
     * @param otherSequence a series of unmodified amino acids represented by
     * their single letter code
     */
    public void appendNTerm(String otherSequence) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        insert(0, otherSequence);

    }

    /**
     * Getter for the modifications carried by this sequence in a map: aa number
     * &gt; modification matches. 1 is the first amino acid.
     *
     * @return the modifications matches as found by the search engine
     */
    public ModificationMatch[] getModificationMatches() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return modifications;

    }

    /**
     * Adds a modification to one of the amino acid sequence.
     *
     * @param modificationMatch the modification match
     */
    public void addModificationMatch(ModificationMatch modificationMatch) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        modifications = modifications == null ? new ModificationMatch[1] : Arrays.copyOf(modifications, modifications.length + 1);

        modifications[modifications.length - 1] = modificationMatch;

    }

    /**
     * Sets the modifications.
     *
     * @param modifications the modifications
     */
    public void setModifications(ModificationMatch[] modifications) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.modifications = modifications;

    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * This method will work only if the PTM found in the peptide are in the
     * PTMFactory. Modifications should be provided indexed by site as follows:
     * N-term modifications are at index 0, C-term at sequence length + 1, and
     * amino acid at 1-based index on the sequence.
     *
     * @param modificationProfile the modification profile of the search
     * @param sequence the amino acid sequence to annotate
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
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     *
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(ModificationParameters modificationProfile, String sequence,
            String[] confidentModificationSites,
            String[] representativeAmbiguousModificationSites,
            String[] secondaryAmbiguousModificationSites,
            String[] fixedModificationSites, boolean useHtmlColorCoding,
            boolean useShortName) {

        if (confidentModificationSites == null) {
            confidentModificationSites = new String[sequence.length()];
        }

        if (representativeAmbiguousModificationSites == null) {
            representativeAmbiguousModificationSites = new String[sequence.length()];
        }

        if (secondaryAmbiguousModificationSites == null) {
            secondaryAmbiguousModificationSites = new String[sequence.length()];
        }

        if (fixedModificationSites == null) {
            fixedModificationSites = new String[sequence.length()];
        }

        StringBuilder modifiedSequence = new StringBuilder(sequence.length());

        for (int aa = 1; aa <= sequence.length(); aa++) {

            int aaIndex = aa - 1;
            char aminoAcid = sequence.charAt(aaIndex);

            appendTaggedResidue(modifiedSequence, aminoAcid,
                    confidentModificationSites[aa], representativeAmbiguousModificationSites[aa], secondaryAmbiguousModificationSites[aa], fixedModificationSites[aa],
                    modificationProfile, useHtmlColorCoding, useShortName);

        }

        return modifiedSequence.toString();
    }

    /**
     * Returns the single residue as a tagged string (HTML color or PTM tag).
     * Modified sites are color coded according to three levels: 1- black
     * foreground, colored background 2- colored foreground, white background 3-
     * colored foreground
     *
     * @param stringBuilder the string builder
     * @param residue the residue to tag
     * @param confidentModification the confident ptm at site
     * @param representativeAmbiguousModification the representative ptm at site
     * @param secondaryAmbiguousModification the secondary ptm at site
     * @param fixedModification the fixed ptm at site
     * @param modificationProfile the modification profile
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     */
    public static void appendTaggedResidue(StringBuilder stringBuilder, char residue,
            String confidentModification,
            String representativeAmbiguousModification,
            String secondaryAmbiguousModification,
            String fixedModification,
            ModificationParameters modificationProfile, boolean useHtmlColorCoding, boolean useShortName) {

        if (confidentModification != null) {

            appendTaggedResidue(stringBuilder, residue, confidentModification, modificationProfile, 1, useHtmlColorCoding, useShortName);

        } else if (representativeAmbiguousModification != null) {

            appendTaggedResidue(stringBuilder, residue, representativeAmbiguousModification, modificationProfile, 2, useHtmlColorCoding, useShortName);

        } else if (secondaryAmbiguousModification != null) {

            appendTaggedResidue(stringBuilder, residue, secondaryAmbiguousModification, modificationProfile, 3, useHtmlColorCoding, useShortName);

        } else if (fixedModification != null) {

            appendTaggedResidue(stringBuilder, residue, fixedModification, modificationProfile, 1, useHtmlColorCoding, useShortName);

        } else {

            Character.toString(residue);

        }
    }

    /**
     * Appends the single residue as a tagged string (HTML color or PTM tag).
     * Modified sites are color coded according to three levels: 1- black
     * foreground, colored background 2- colored foreground, white background 3-
     * colored foreground
     *
     * @param stringBuilder the string builder
     * @param residue the residue to tag
     * @param modificationName the name of the PTM
     * @param modificationProfile the modification profile
     * @param localizationConfidenceLevel the localization confidence level
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     */
    public static void appendTaggedResidue(StringBuilder stringBuilder, char residue, String modificationName, ModificationParameters modificationProfile, int localizationConfidenceLevel, boolean useHtmlColorCoding, boolean useShortName) {

        ModificationFactory modificationFactory = ModificationFactory.getInstance();
        Modification modification = modificationFactory.getModification(modificationName);

        if (!useHtmlColorCoding) {

            if (localizationConfidenceLevel == 1 || localizationConfidenceLevel == 2) {

                if (useShortName) {

                    stringBuilder.append(residue).append("<").append(modification.getShortName()).append(">");

                } else {

                    stringBuilder.append(residue).append("<").append(modificationName).append(">");

                }

            } else if (localizationConfidenceLevel == 3) {

                stringBuilder.append(residue);

            }

        } else {

            Color modificationColor = modificationProfile.getColor(modificationName);

            switch (localizationConfidenceLevel) {
                case 1:
                    stringBuilder.append("<span style=\"color:#").append(Util.color2Hex(Color.WHITE)).append(";background:#").append(Util.color2Hex(modificationColor)).append("\">").append(residue).append("</span>");
                    break;

                case 2:
                    stringBuilder.append("<span style=\"color:#").append(Util.color2Hex(modificationColor)).append(";background:#").append(Util.color2Hex(Color.WHITE)).append("\">").append(residue).append("</span>");
                    break;

                case 3:
                    // taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(modificationColor)).append("\">").append(residue).append("</span>");
                    // taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(Color.BLACK)).append(";background:#").append(Util.color2Hex(Color.WHITE)).append("\">").append(residue).append("</span>");
                    stringBuilder.append(residue);
                    break;

                default:
                    throw new IllegalArgumentException("No formatting implemented for localization confidence level " + localizationConfidenceLevel + ".");
            }
        }
    }

    /**
     * Indicates whether another sequence has a matching sequence. Modifications
     * are considered equal when of same mass. Modifications should be loaded in
     * the PTM factory.
     *
     * @param anotherSequence the other AminoAcidPattern
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return true if the other AminoAcidPattern targets the same sequence
     */
    public boolean isSameAs(AminoAcidSequence anotherSequence, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (!isSameSequenceAndModificationStatusAs(anotherSequence, sequenceMatchingPreferences)) {

            return false;

        }

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        ModificationMatch[] modificationMatches1 = getModificationMatches();
        ModificationMatch[] modificationMatches2 = anotherSequence.getModificationMatches();

        Map<Double, HashSet<ModificationMatch>> mods1 = Arrays.stream(modificationMatches1).collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.toCollection(HashSet::new)));
        Map<Double, HashSet<ModificationMatch>> mods2 = Arrays.stream(modificationMatches1).collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.toCollection(HashSet::new)));

        for (Entry<Double, HashSet<ModificationMatch>> entry1 : mods1.entrySet()) {

            HashSet<Integer> sites1 = entry1.getValue().stream()
                    .map(ModificationMatch::getSite)
                    .collect(Collectors.toCollection(HashSet::new));
            HashSet<Integer> sites2 = mods2.get(entry1.getKey()).stream()
                    .map(ModificationMatch::getSite)
                    .collect(Collectors.toCollection(HashSet::new));

            if (!Util.sameSets(sites1, sites2)) {

                return false;

            }
        }

        return true;

    }

    /**
     * Indicates whether another sequence targets the same sequence without
     * accounting for PTM localization. Modifications are considered equal when
     * of same mass. Modifications should be loaded in the PTM factory.
     *
     * @param anotherSequence the other sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return true if the other AminoAcidPattern targets the same sequence
     */
    public boolean isSameSequenceAndModificationStatusAs(AminoAcidSequence anotherSequence, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (!matches(anotherSequence, sequenceMatchingPreferences)) {
            return false;
        }

        ModificationMatch[] modificationMatches1 = getModificationMatches();
        ModificationMatch[] modificationMatches2 = anotherSequence.getModificationMatches();

        if (modificationMatches1.length != modificationMatches2.length) {
            return false;
        }

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        Map<Double, Long> masses1 = Arrays.stream(modificationMatches1).collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.counting()));

        Map<Double, Long> masses2 = Arrays.stream(modificationMatches2).collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.counting()));

        if (masses1.size() != masses2.size()) {
            return false;
        }

        return !masses1.entrySet().stream()
                .anyMatch(entry -> masses2.get(entry.getKey()) == null
                || !entry.getValue().equals(masses2.get(entry.getKey())));
    }

    /**
     * Returns an amino acid sequence which is a reversed version of the current
     * pattern.
     *
     * @return an amino acid sequence which is a reversed version of the current
     * pattern
     */
    public AminoAcidSequence reverse() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(false);
        AminoAcidSequence newSequence = new AminoAcidSequence((new StringBuilder(sequence)).reverse().toString());

        ModificationMatch[] newModifications = Arrays.stream(modifications)
                .map(ModificationMatch::clone)
                .peek(modificationMatch -> modificationMatch.setSite(sequence.length() - modificationMatch.getSite()))
                .toArray(ModificationMatch[]::new);

        newSequence.setModifications(modifications);

        return newSequence;

    }

    /**
     * Indicates whether the given sequence contains an amino acid which is in
     * fact a combination of amino acids.
     *
     * @param sequence the sequence of interest
     *
     * @return a boolean indicating whether the given sequence contains an amino
     * acid which is in fact a combination of amino acids
     */
    public static boolean hasCombination(String sequence) {

        return hasCombination(sequence.chars());

    }

    /**
     * Indicates whether the given sequence contains an amino acid which is in
     * fact a combination of amino acids.
     *
     * @param sequence the sequence of interest
     *
     * @return a boolean indicating whether the given sequence contains an amino
     * acid which is in fact a combination of amino acids
     */
    public static boolean hasCombination(IntStream sequence) {

        return sequence.anyMatch(aa -> AminoAcid.getAminoAcid((char) aa).iscombination());

    }

    /**
     * Indicates whether the given sequence contains an amino acid which is in
     * fact a combination of amino acids.
     *
     * @param sequence the sequence of interest
     *
     * @return a boolean indicating whether the given sequence contains an amino
     * acid which is in fact a combination of amino acids
     */
    public static boolean hasCombination(char[] sequence) {

        for (int i = 0; i < sequence.length; i++) {

            char aa = sequence[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);

            if (aminoAcid.iscombination()) {

                return true;

            }
        }

        return false;
    }

    /**
     * Returns the minimal mass that an amino acid sequence can have taking into
     * account ambiguous amino acids.
     *
     * @param sequence a sequence of amino acids represented by their single
     * letter code
     *
     * @return the minimal mass the sequence can have
     */
    public static double getMinMass(char[] sequence) {

        double minMass = 0.0;

        for (char aa : sequence) {

            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);

            if (aminoAcid.iscombination()) {

                char[] subAa = aminoAcid.getSubAminoAcids(false);
                aminoAcid = AminoAcid.getAminoAcid(subAa[0]);
                double minMassTemp = aminoAcid.getMonoisotopicMass();

                for (int i = 1; i < subAa.length; i++) {

                    aminoAcid = AminoAcid.getAminoAcid(subAa[i]);
                    double massTemp = aminoAcid.getMonoisotopicMass();

                    if (massTemp < minMassTemp) {

                        minMassTemp = massTemp;

                    }
                }

                minMass += minMassTemp;

            } else {

                minMass += aminoAcid.getMonoisotopicMass();

            }
        }

        return minMass;

    }

    /**
     * Returns a list of all combinations which can be created from a sequence
     * when expanding ambiguous amino acids like Xs.
     *
     * @param sequence the sequence of interest
     *
     * @return a list of all combinations which can be created from a sequence
     * when expanding ambiguous amino acids like Xs
     */
    public static ArrayList<StringBuilder> getCombinations(String sequence) {

        ArrayList<StringBuilder> newCombination, combination = new ArrayList<>(1);

        for (int i = 0; i < sequence.length(); i++) {

            newCombination = new ArrayList<>(1);
            char aa = sequence.charAt(i);
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);

            for (char newAa : aminoAcid.getSubAminoAcids(false)) {

                if (combination.isEmpty()) {

                    StringBuilder stringBuilder = new StringBuilder(sequence.length());
                    stringBuilder.append(newAa);
                    newCombination.add(stringBuilder);

                } else {

                    for (StringBuilder stringBuilder : combination) {

                        StringBuilder newStringBuilder = new StringBuilder(sequence.length());
                        newStringBuilder.append(stringBuilder);
                        newStringBuilder.append(newAa);
                        newCombination.add(newStringBuilder);

                    }
                }
            }

            combination = newCombination;

        }

        return combination;

    }

    @Override
    public String toString() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(false);

        return sequence;

    }

    @Override
    public String asSequence() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(false);

        return sequence;

    }

    @Override
    public double getMass() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        setSequenceStringBuilder(false);

        double mass = CharBuffer.wrap(sequence.toCharArray()).chars()
                .mapToObj(aa -> AminoAcid.getAminoAcid((char) aa))
                .mapToDouble(AminoAcid::getMonoisotopicMass)
                .sum();

        if (modifications != null) {

            ModificationFactory modificationFactory = ModificationFactory.getInstance();
            mass += Arrays.stream(modifications)
                    .mapToDouble(modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass())
                    .sum();

        }

        return mass;

    }

    @Override
    public boolean isSameAs(TagComponent anotherCompontent, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (!(anotherCompontent instanceof AminoAcidSequence)) {

            return false;

        } else {

            AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) anotherCompontent;
            return isSameAs(aminoAcidSequence, sequenceMatchingPreferences);

        }
    }

    @Override
    public boolean isSameSequenceAndModificationStatusAs(TagComponent anotherCompontent, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (!(anotherCompontent instanceof AminoAcidSequence)) {

            return false;

        } else {

            AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) anotherCompontent;
            return isSameSequenceAndModificationStatusAs(aminoAcidSequence, sequenceMatchingPreferences);

        }
    }
}
