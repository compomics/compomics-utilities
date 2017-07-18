package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

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
     * The sequence as amino acid pattern.
     */
    private AminoAcidPattern aminoAcidPattern = null;
    /**
     * The modifications carried by the amino acid sequence at target amino
     * acids. 1 is the first amino acid.
     */
    private HashMap<Integer, ArrayList<ModificationMatch>> modifications = null;

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
    public AminoAcidSequence(String sequence, HashMap<Integer, ArrayList<ModificationMatch>> modifications) {
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
        HashMap<Integer, ArrayList<ModificationMatch>> modificationMatches = sequence.getModificationMatches();
        if (modificationMatches != null) {
            modifications = new HashMap<Integer, ArrayList<ModificationMatch>>(modificationMatches.size());
            for (int site : modificationMatches.keySet()) {
                ArrayList<ModificationMatch> oldModifications = modificationMatches.get(site);
                ArrayList<ModificationMatch> newModifications = new ArrayList<ModificationMatch>(oldModifications.size());
                for (ModificationMatch modificationMatch : oldModifications) {
                    newModifications.add(modificationMatch.clone());
                }
                modifications.put(site, newModifications);
            }
        }
    }

    /**
     * Returns the sequence as String.
     *
     * @return the sequence as String
     */
    public String getSequence() {
        setSequenceStringBuilder(false);
        if (sequence != null) {
            return sequence;
        } else {
            return "";
        }
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
        return AminoAcid.getAminoAcid(charAt(aa));
    }

    /**
     * Sets the sequence.
     *
     * @param aminoAcidSequence the sequence
     */
    public void setSequence(String aminoAcidSequence) {
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
        setSequenceStringBuilder(true);
        sequenceStringBuilder.setCharAt(index, aa);
    }

    /**
     * Returns this amino acid sequence as amino acid pattern.
     *
     * @return this amino acid sequence as amino acid pattern
     */
    public AminoAcidPattern getAsAminoAcidPattern() {
        setSequenceStringBuilder(false);
        if (aminoAcidPattern == null) {
            aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString(sequence);
            if (modifications != null) {
                for (Integer location : modifications.keySet()) {
                    for (ModificationMatch modMatch : modifications.get(location)) {
                        aminoAcidPattern.addModificationMatch(location, modMatch);
                    }
                }
            }
        }
        return aminoAcidPattern;
    }

    /**
     * Loads the sequence in the string builder.
     */
    private void setSequenceStringBuilder(boolean stringbuilder) {
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
        sequenceStringBuilder = null;
        aminoAcidPattern = null;
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
    public boolean matchesIn(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
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
    public boolean matchesIn(AminoAcidSequence aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
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
    public boolean matches(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
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
    public boolean matches(AminoAcidSequence aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
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
    public int firstIndex(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return getAsAminoAcidPattern().firstIndex(aminoAcidSequence, sequenceMatchingPreferences, 0);
    }

    /**
     * Indicates whether another AminoAcidPattern targets the same sequence
     * without accounting for PTM localization. Modifications are considered
     * equal when of same mass. Modifications should be loaded in the PTM
     * factory.
     *
     * @param anotherPattern the other AminoAcidPattern
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return true if the other AminoAcidPattern targets the same sequence
     */
    public boolean isSameSequenceAndModificationStatusAs(AminoAcidPattern anotherPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {

        if (!anotherPattern.matches(anotherPattern, sequenceMatchingPreferences)) {
            return false;
        }

        PTMFactory ptmFactory = PTMFactory.getInstance();
        HashMap<Double, Integer> masses1 = new HashMap<Double, Integer>();
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> tempModifications = getModificationsAt(i);
            for (ModificationMatch modMatch : tempModifications) {
                PTM ptm = ptmFactory.getPTM(modMatch.getTheoreticPtm());
                double mass = ptm.getMass();
                Integer occurrence = masses1.get(mass);
                if (occurrence == null) {
                    masses1.put(mass, 1);
                } else {
                    masses1.put(mass, occurrence + 1);
                }
            }
        }

        HashMap<Double, Integer> masses2 = new HashMap<Double, Integer>();
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> tempModifications = anotherPattern.getModificationsAt(i);
            for (ModificationMatch modMatch : tempModifications) {
                PTM ptm = ptmFactory.getPTM(modMatch.getTheoreticPtm());
                double mass = ptm.getMass();
                Integer occurrence = masses2.get(mass);
                if (occurrence == null) {
                    masses2.put(mass, 1);
                } else {
                    masses2.put(mass, occurrence + 1);
                }
            }
        }

        if (masses1.size() != masses2.size()) {
            return false;
        }
        for (Double mass : masses1.keySet()) {
            Integer occurrence1 = masses1.get(mass);
            Integer occurrence2 = masses2.get(mass);
            if (occurrence2 == null || occurrence2.intValue() != occurrence1) {
                return false;
            }
        }
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> mods1 = getModificationsAt(i);
            ArrayList<ModificationMatch> mods2 = anotherPattern.getModificationsAt(i);
            if (mods1.size() != mods2.size()) {
                return false;
            }
            for (int j = 0; j < mods1.size(); j++) {
                ModificationMatch modificationMatch1 = mods1.get(j);
                ModificationMatch modificationMatch2 = mods2.get(j);
                if (!modificationMatch1.equals(modificationMatch2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the length of the sequence in amino acids.
     *
     * @return the length of the sequence in amino acids
     */
    public int length() {
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
        setSequenceStringBuilder(true);
        int previousLength = length();
        sequenceStringBuilder.append(otherSequence.getSequence());
        HashMap<Integer, ArrayList<ModificationMatch>> modificationMatches = otherSequence.getModificationMatches();
        if (modificationMatches != null) {
            for (int otherSite : modificationMatches.keySet()) {
                int newSite = otherSite + previousLength;
                for (ModificationMatch oldModificationMatch : modificationMatches.get(otherSite)) {
                    ModificationMatch newModificationMatch = oldModificationMatch.clone();
                    oldModificationMatch.setModificationSite(newSite);
                    addModificationMatch(newSite, newModificationMatch);
                }
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
        setSequenceStringBuilder(true);
        sequenceStringBuilder.insert(0, otherSequence.getSequence());
        int otherSequenceLength = otherSequence.length();
        HashMap<Integer, ArrayList<ModificationMatch>> otherModificationMatches = otherSequence.getModificationMatches();
        if (otherModificationMatches != null || modifications != null) {
            int otherSize = 0;
            if (otherModificationMatches != null) {
                otherSize = otherModificationMatches.size();
            }
            int newSize = 0;
            if (modifications != null) {
                newSize = modifications.size();
            }
            HashMap<Integer, ArrayList<ModificationMatch>> newModificationMatches = new HashMap<Integer, ArrayList<ModificationMatch>>(otherSize + newSize);
            if (otherModificationMatches != null) {
                for (int site : otherModificationMatches.keySet()) {
                    ArrayList<ModificationMatch> modMatches = otherModificationMatches.get(site);
                    ArrayList<ModificationMatch> newModMatches = new ArrayList<ModificationMatch>(modMatches.size());
                    for (ModificationMatch modificationMatch : modMatches) {
                        newModMatches.add(modificationMatch.clone());
                    }
                    newModificationMatches.put(site, newModMatches);
                }
            }
            if (modifications != null) {
                for (int site : modifications.keySet()) {
                    int newSite = site + otherSequenceLength;
                    ArrayList<ModificationMatch> modMatches = modifications.get(site);
                    ArrayList<ModificationMatch> newModMatches = new ArrayList<ModificationMatch>(modMatches.size());
                    for (ModificationMatch oldModificationMatch : modifications.get(site)) {
                        ModificationMatch newModificationMatch = oldModificationMatch.clone();
                        oldModificationMatch.setModificationSite(newSite);
                        newModMatches.add(newModificationMatch);
                    }
                    newModificationMatches.put(site, newModMatches);
                }
            }
            modifications = newModificationMatches;
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
        insert(0, otherSequence);
    }

    /**
     * Getter for the modifications carried by this sequence in a map: aa number
     * &gt; modification matches. 1 is the first amino acid.
     *
     * @return the modifications matches as found by the search engine
     */
    public HashMap<Integer, ArrayList<ModificationMatch>> getModificationMatches() {
        return modifications;
    }

    /**
     * Returns a list of the indexes of the amino acids carrying a modification.
     * 1 is the first amino acid.
     *
     * @return a list of the indexes of the amino acids carrying a modification
     */
    public ArrayList<Integer> getModificationIndexes() {
        if (modifications == null) {
            return new ArrayList<Integer>();
        }
        return new ArrayList<Integer>(modifications.keySet());
    }

    /**
     * Returns the modifications found at a given localization.
     *
     * @param localization the localization as amino acid number. 1 is the first
     * amino acid.
     *
     * @return the modifications found at a given localization as a list.
     */
    public ArrayList<ModificationMatch> getModificationsAt(int localization) {
        if (modifications != null) {
            ArrayList<ModificationMatch> result = modifications.get(localization);
            if (result != null) {
                return result;
            }
        }
        return new ArrayList<ModificationMatch>();
    }

    /**
     * Removes a modification match in the given sequence.
     *
     * @param localisation the localization of the modification
     * @param modificationMatch the modification match to remove
     */
    public void removeModificationMatch(int localisation, ModificationMatch modificationMatch) {
        ArrayList<ModificationMatch> modificationMatches = modifications.get(localisation);
        if (modificationMatches != null) {
            modificationMatches.remove(modificationMatch);
            if (modificationMatches.isEmpty()) {
                modifications.remove(localisation);
            }
        }
    }

    /**
     * Clears the list of imported modification matches.
     */
    public void clearModificationMatches() {
        if (modifications != null) {
            modifications.clear();
        }
    }

    /**
     * Adds a modification to one of the amino acid sequence.
     *
     * @param localization the index of the amino acid retained as target of the
     * modification. 1 is the first amino acid.
     * @param modificationMatch the modification match
     */
    public void addModificationMatch(int localization, ModificationMatch modificationMatch) {
        int index = localization - 1;
        if (index < 0) {
            throw new IllegalArgumentException("Wrong modification target index " + localization + ", 1 is the first amino acid for PTM localization.");
        }
        if (modifications == null) {
            modifications = new HashMap<Integer, ArrayList<ModificationMatch>>();
        }
        ArrayList<ModificationMatch> modificationMatches = modifications.get(localization);
        if (modificationMatches == null) {
            modificationMatches = new ArrayList<ModificationMatch>();
            modifications.put(localization, modificationMatches);
        }
        modificationMatches.add(modificationMatch);
    }

    /**
     * Adds a list of modifications to one of the amino acid sequence.
     *
     * @param localization the index of the amino acid retained as target of the
     * modification. 1 is the first amino acid.
     * @param modificationMatches the modification matches
     */
    public void addModificationMatches(int localization, ArrayList<ModificationMatch> modificationMatches) {
        int index = localization - 1;
        if (index < 0) {
            throw new IllegalArgumentException("Wrong modification target index " + localization + ", 1 is the first amino acid for PTM localization.");
        }
        if (modifications == null) {
            modifications = new HashMap<Integer, ArrayList<ModificationMatch>>();
        }
        ArrayList<ModificationMatch> modificationMatchesAtIndex = modifications.get(localization);
        if (modificationMatchesAtIndex == null) {
            modificationMatchesAtIndex = new ArrayList<ModificationMatch>();
            modifications.put(localization, modificationMatchesAtIndex);
        }
        modificationMatches.addAll(modificationMatches);
    }

    /**
     * Changes the localization of a modification match.
     *
     * @param modificationMatch the modification match of interest
     * @param oldLocalization the old localization
     * @param newLocalization the new localization
     */
    public void changeModificationSite(ModificationMatch modificationMatch, int oldLocalization, int newLocalization) {
        int oldIndex = oldLocalization - 1;
        if (oldIndex < 0) {
            throw new IllegalArgumentException("Wrong modification old target index " + oldLocalization + ", 1 is the first amino acid for PTM localization.");
        }
        if (modifications == null || !modifications.containsKey(oldIndex) || !modifications.get(oldIndex).contains(modificationMatch)) {
            throw new IllegalArgumentException("Modification match " + modificationMatch + " not found at index " + oldLocalization + ".");
        }
        modifications.get(oldIndex).remove(modificationMatch);
        addModificationMatch(newLocalization, modificationMatch);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * this method will work only if the PTM found in the peptide are in the
     * PTMFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them. Note: this
     * does not include HTML start end tags or terminal annotation.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @param excludeAllFixedPtms if true, all fixed PTMs are excluded
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(PtmSettings modificationProfile, boolean useHtmlColorCoding, boolean useShortName, boolean excludeAllFixedPtms) {

        HashMap<Integer, ArrayList<String>> confidentModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> representativeModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> secondaryModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> fixedModificationSites = new HashMap<Integer, ArrayList<String>>();

        if (modifications != null) {
            for (int modSite : modifications.keySet()) {
                for (ModificationMatch modificationMatch : modifications.get(modSite)) {
                    String modName = modificationMatch.getTheoreticPtm();
                    if (modificationMatch.getVariable()) {
                        if (modificationMatch.getConfident()) {
                            if (!confidentModificationSites.containsKey(modSite)) {
                                confidentModificationSites.put(modSite, new ArrayList<String>());
                            }
                            confidentModificationSites.get(modSite).add(modName);
                        } else {
                            if (!representativeModificationSites.containsKey(modSite)) {
                                representativeModificationSites.put(modSite, new ArrayList<String>());
                            }
                            representativeModificationSites.get(modSite).add(modName);
                        }
                    } else if (!excludeAllFixedPtms) {
                        if (!fixedModificationSites.containsKey(modSite)) {
                            fixedModificationSites.put(modSite, new ArrayList<String>());
                        }
                        fixedModificationSites.get(modSite).add(modName);
                    }
                }
            }
        }
        setSequenceStringBuilder(false);
        return getTaggedModifiedSequence(modificationProfile, sequence, confidentModificationSites, representativeModificationSites, secondaryModificationSites,
                fixedModificationSites, useHtmlColorCoding, useShortName);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * This method will work only if the PTM found in the peptide are in the
     * PTMFactory.
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
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(PtmSettings modificationProfile, String sequence,
            HashMap<Integer, ArrayList<String>> confidentModificationSites,
            HashMap<Integer, ArrayList<String>> representativeAmbiguousModificationSites,
            HashMap<Integer, ArrayList<String>> secondaryAmbiguousModificationSites,
            HashMap<Integer, ArrayList<String>> fixedModificationSites, boolean useHtmlColorCoding,
            boolean useShortName) {

        if (confidentModificationSites == null) {
            confidentModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (representativeAmbiguousModificationSites == null) {
            representativeAmbiguousModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (secondaryAmbiguousModificationSites == null) {
            secondaryAmbiguousModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (fixedModificationSites == null) {
            fixedModificationSites = new HashMap<Integer, ArrayList<String>>();
        }

        StringBuilder modifiedSequence = new StringBuilder(sequence.length());

        for (int aa = 1; aa <= sequence.length(); aa++) {

            int aaIndex = aa - 1;
            char aminoAcid = sequence.charAt(aaIndex);

            if (confidentModificationSites.containsKey(aa) && !confidentModificationSites.get(aa).isEmpty()) {
                addTaggedResidue(modifiedSequence, aa, aminoAcid, 1, modificationProfile, confidentModificationSites, useHtmlColorCoding, useShortName);
            } else if (representativeAmbiguousModificationSites.containsKey(aa) && !representativeAmbiguousModificationSites.get(aa).isEmpty()) {
                addTaggedResidue(modifiedSequence, aa, aminoAcid, 2, modificationProfile, representativeAmbiguousModificationSites, useHtmlColorCoding, useShortName);
            } else if (secondaryAmbiguousModificationSites.containsKey(aa) && !secondaryAmbiguousModificationSites.get(aa).isEmpty()) {
                addTaggedResidue(modifiedSequence, aa, aminoAcid, 3, modificationProfile, secondaryAmbiguousModificationSites, useHtmlColorCoding, useShortName);
            } else if (fixedModificationSites.containsKey(aa) && !fixedModificationSites.get(aa).isEmpty()) {
                addTaggedResidue(modifiedSequence, aa, aminoAcid, 1, modificationProfile, fixedModificationSites, useHtmlColorCoding, useShortName);
            } else {
                modifiedSequence.append(aminoAcid);
            }
        }

        return modifiedSequence.toString();
    }

    /**
     * Helper method for annotating the modified sequence as an tagged string
     * with potential modification sites.
     *
     * @param modifiedSequence the modified sequence to add the new annotations
     * to
     * @param aaIndex the current sequence index
     * @param aminoAcid the current amino acid
     * @param localizationConfidenceLevel the localization confidence level
     * @param modificationProfile the modification profile of the search
     * @param modificationSites the current modification sites
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the tagged modified sequence as a string
     */
    private static void addTaggedResidue(StringBuilder modifiedSequence, int aaIndex, char aminoAcid, int localizationConfidenceLevel, PtmSettings modificationProfile,
            HashMap<Integer, ArrayList<String>> modificationSites, boolean useHtmlColorCoding, boolean useShortName) {

        PTMFactory ptmFactory = PTMFactory.getInstance();

        if (modificationSites.get(aaIndex).size() == 1) {
            modifiedSequence.append(getTaggedResidue(aminoAcid, modificationSites.get(aaIndex).get(0), modificationProfile, localizationConfidenceLevel, useHtmlColorCoding, useShortName));
        } else {
            boolean modificationAdded = false;
            for (String ptmName : modificationSites.get(aaIndex)) {
                PTM ptm = ptmFactory.getPTM(ptmName);
                if (ptm.getType() == PTM.MODAA && !modificationAdded) { // there should only be one...
                    modifiedSequence.append(getTaggedResidue(aminoAcid, ptmName, modificationProfile, localizationConfidenceLevel, useHtmlColorCoding, useShortName));
                    modificationAdded = true;
                }
            }

            if (!modificationAdded) {
                modifiedSequence.append(aminoAcid);
            }
        }
    }

    /**
     * Returns the single residue as a tagged string (HTML color or PTM tag).
     * Modified sites are color coded according to three levels: 1- black
     * foreground, colored background 2- colored foreground, white background 3-
     * colored foreground
     *
     * @param residue the residue to tag
     * @param ptmName the name of the PTM
     * @param modificationProfile the modification profile
     * @param localizationConfidenceLevel the localization confidence level
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the single residue as a tagged string
     */
    public static String getTaggedResidue(char residue, String ptmName, PtmSettings modificationProfile, int localizationConfidenceLevel, boolean useHtmlColorCoding, boolean useShortName) {

        StringBuilder taggedResidue = new StringBuilder();
        PTMFactory ptmFactory = PTMFactory.getInstance();
        PTM ptm = ptmFactory.getPTM(ptmName);
        if (ptm.getType() == PTM.MODAA) {
            if (!useHtmlColorCoding) {
                if (localizationConfidenceLevel == 1 || localizationConfidenceLevel == 2) {
                    if (useShortName) {
                        taggedResidue.append(residue).append("<").append(ptm.getShortName()).append(">");
                    } else {
                        taggedResidue.append(residue).append("<").append(ptmName).append(">");
                    }
                } else if (localizationConfidenceLevel == 3) {
                    taggedResidue.append(residue);
                }
            } else {
                Color ptmColor = modificationProfile.getColor(ptmName);
                switch (localizationConfidenceLevel) {
                    case 1:
                        taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(Color.WHITE)).append(";background:#").append(Util.color2Hex(ptmColor)).append("\">").append(residue).append("</span>");
                        break;
                    case 2:
                        taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(ptmColor)).append(";background:#").append(Util.color2Hex(Color.WHITE)).append("\">").append(residue).append("</span>");
                        break;
                    case 3:
                        // taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(ptmColor)).append("\">").append(residue).append("</span>");
                        // taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(Color.BLACK)).append(";background:#").append(Util.color2Hex(Color.WHITE)).append("\">").append(residue).append("</span>");
                        taggedResidue.append(residue);
                        break;
                    default:
                        throw new IllegalArgumentException("No formatting implemented for localization confidence level " + localizationConfidenceLevel + ".");
                }
            }
        } else {
            taggedResidue.append(residue);
        }

        return taggedResidue.toString();
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
    public boolean isSameAs(AminoAcidSequence anotherSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {

        if (!matches(anotherSequence, sequenceMatchingPreferences)) {
            return false;
        }

        PTMFactory ptmFactory = PTMFactory.getInstance();
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> mods1 = getModificationsAt(i);
            ArrayList<ModificationMatch> mods2 = anotherSequence.getModificationsAt(i);
            if (mods1.size() != mods2.size()) {
                return false;
            }
            for (ModificationMatch modificationMatch1 : mods1) {
                PTM ptm1 = ptmFactory.getPTM(modificationMatch1.getTheoreticPtm());
                boolean found = false;
                for (ModificationMatch modificationMatch2 : mods2) {
                    PTM ptm2 = ptmFactory.getPTM(modificationMatch2.getTheoreticPtm());
                    if (ptm1.getMass() == ptm2.getMass()) { // @TODO: compare against the accuracy
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
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
    public boolean isSameSequenceAndModificationStatusAs(AminoAcidSequence anotherSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {

        if (!matches(anotherSequence, sequenceMatchingPreferences)) {
            return false;
        }

        PTMFactory ptmFactory = PTMFactory.getInstance();
        HashMap<Double, Integer> masses1 = new HashMap<Double, Integer>();
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> tempModifications = getModificationsAt(i);
            for (ModificationMatch modMatch : tempModifications) {
                PTM ptm = ptmFactory.getPTM(modMatch.getTheoreticPtm());
                double mass = ptm.getMass();
                Integer occurrence = masses1.get(mass);
                if (occurrence == null) {
                    masses1.put(mass, 1);
                } else {
                    masses1.put(mass, occurrence + 1);
                }
            }
        }

        HashMap<Double, Integer> masses2 = new HashMap<Double, Integer>();
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> tempModifications = anotherSequence.getModificationsAt(i);
            for (ModificationMatch modMatch : tempModifications) {
                PTM ptm = ptmFactory.getPTM(modMatch.getTheoreticPtm());
                double mass = ptm.getMass();
                Integer occurrence = masses2.get(mass);
                if (occurrence == null) {
                    masses2.put(mass, 1);
                } else {
                    masses2.put(mass, occurrence + 1);
                }
            }
        }

        if (masses1.size() != masses2.size()) {
            return false;
        }
        for (Double mass : masses1.keySet()) {
            Integer occurrence1 = masses1.get(mass);
            Integer occurrence2 = masses2.get(mass);
            if (occurrence2 == null || occurrence2.intValue() != occurrence1) {
                return false;
            }
        }
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> mods1 = getModificationsAt(i);
            ArrayList<ModificationMatch> mods2 = anotherSequence.getModificationsAt(i);
            if (mods1.size() != mods2.size()) {
                return false;
            }
            for (int j = 0; j < mods1.size(); j++) {
                ModificationMatch modificationMatch1 = mods1.get(j);
                ModificationMatch modificationMatch2 = mods2.get(j);
                if (!modificationMatch1.equals(modificationMatch2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns an amino acid sequence which is a reversed version of the current
     * pattern.
     *
     * @return an amino acid sequence which is a reversed version of the current
     * pattern
     */
    public AminoAcidSequence reverse() {
        setSequenceStringBuilder(false);
        AminoAcidSequence newSequence = new AminoAcidSequence((new StringBuilder(sequence)).reverse().toString());
        if (modifications != null) {
            for (int i : modifications.keySet()) {
                int reversed = length() - i + 1;
                for (ModificationMatch modificationMatch : modifications.get(i)) {
                    ModificationMatch newMatch = new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.getVariable(), reversed);
                    if (modificationMatch.getConfident()) {
                        newMatch.setConfident(true);
                    }
                    if (modificationMatch.getInferred()) {
                        newMatch.setInferred(true);
                    }
                    newSequence.addModificationMatch(reversed, newMatch);
                }
            }
        }
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
        return hasCombination(sequence.toCharArray());
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
     * Returns the minimal mass that an amino acid sequence can have taking into account ambiguous amino acids.
     * 
     * @param sequence a sequence of amino acids represented by their single letter code
     * 
     * @return the minimal mass the sequence can have
     */
    public static Double getMinMass(char[] sequence) {
        Double minMass = 0.0;
        for (char aa : sequence) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            if (aminoAcid.iscombination()) {
                char[] subAa = aminoAcid.getSubAminoAcids(false);
                aminoAcid = AminoAcid.getAminoAcid(subAa[0]);
                Double minMassTemp = aminoAcid.getMonoisotopicMass();
                for (int i = 1 ; i < subAa.length ; i++) {
                    aminoAcid = AminoAcid.getAminoAcid(subAa[i]);
                    Double massTemp = aminoAcid.getMonoisotopicMass();
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
        ArrayList<StringBuilder> newCombination, combination = new ArrayList<StringBuilder>();
        for (int i = 0; i < sequence.length(); i++) {
            newCombination = new ArrayList<StringBuilder>();
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
    
    /**
     * Returns a boolean indicating whether the given sequence contains ambiguous amino acids like X.
     * 
     * @param sequence the amino acid sequence to inspect
     * 
     * @return a boolean indicating whether the given sequence contains ambiguous amino acids
     */
    public static boolean containsAmbiguousAminoAcid(String sequence) {
        char[] sequenceAsCharArray = sequence.toCharArray();
        for (char aa : sequenceAsCharArray) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            if (aminoAcid.iscombination()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        setSequenceStringBuilder(false);
        return sequence;
    }

    @Override
    public String asSequence() {
        setSequenceStringBuilder(false);
        return sequence;
    }

    @Override
    public double getMass() {
        setSequenceStringBuilder(false);
        double mass = 0;
        for (int i = 0; i < length(); i++) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(sequence.charAt(i));
            mass += aminoAcid.getMonoisotopicMass();
            if (modifications != null) {
                ArrayList<ModificationMatch> modificationAtIndex = modifications.get(i + 1);
                if (modificationAtIndex != null) {
                    for (ModificationMatch modificationMatch : modificationAtIndex) {
                        PTM ptm = PTMFactory.getInstance().getPTM(modificationMatch.getTheoreticPtm());
                        mass += ptm.getMass();
                    }
                }
            }
        }
        return mass;
    }

    @Override
    public boolean isSameAs(TagComponent anotherCompontent, SequenceMatchingPreferences sequenceMatchingPreferences) {
        if (!(anotherCompontent instanceof AminoAcidSequence)) {
            return false;
        } else {
            AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) anotherCompontent;
            return isSameAs(aminoAcidSequence, sequenceMatchingPreferences);
        }
    }

    @Override
    public boolean isSameSequenceAndModificationStatusAs(TagComponent anotherCompontent, SequenceMatchingPreferences sequenceMatchingPreferences) {
        if (!(anotherCompontent instanceof AminoAcidSequence)) {
            return false;
        } else {
            AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) anotherCompontent;
            return isSameSequenceAndModificationStatusAs(aminoAcidSequence, sequenceMatchingPreferences);
        }
    }
}
