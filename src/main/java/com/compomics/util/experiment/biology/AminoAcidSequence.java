package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.ModificationProfile;
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
            for (int index : modificationMatches.keySet()) {
                modifications.put(index, (ArrayList<ModificationMatch>) modificationMatches.get(index).clone());
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
            aminoAcidPattern = new AminoAcidPattern(sequence);
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
                sequenceStringBuilder = new StringBuilder();
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
     * Indicates whether the sequence matches the given amino acid sequence in size and according to the given matching preferences.
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
     * Indicates whether the sequence matches the given amino acid sequence in size and according to the given matching preferences.
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
            if (occurrence2 == null || occurrence2 != occurrence1) {
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
    public void append(AminoAcidSequence otherSequence) {
        setSequenceStringBuilder(true);
        int previousLength = length();
        sequenceStringBuilder.append(otherSequence.getSequence());
        HashMap<Integer, ArrayList<ModificationMatch>> modificationMatches = otherSequence.getModificationMatches();
        if (modificationMatches != null) {
            for (int i : modificationMatches.keySet()) {
                int newIndex = i + previousLength;
                for (ModificationMatch oldModificationMatch : modificationMatches.get(i)) {
                    ModificationMatch newModificationMatch = new ModificationMatch(oldModificationMatch.getTheoreticPtm(), oldModificationMatch.isVariable(), newIndex);
                    addModificationMatch(newIndex, newModificationMatch);
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
    public void append(String otherSequence) {
        setSequenceStringBuilder(true);
        sequenceStringBuilder.append(otherSequence);
    }

    /**
     * Getter for the modifications carried by this sequence in a map: aa number
     * -> modification matches. 1 is the first amino acid.
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
 this method will work only if the PTM found in the peptide are in the
 PTMFactory. /!\ This method uses the modifications as set in the
 modification matches of this peptide and displays all of them. Note: this
 does not include HTML start end tags or terminal annotation.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @param excludeAllFixedPtms if true, all fixed PTMs are excluded
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(ModificationProfile modificationProfile, boolean useHtmlColorCoding, boolean useShortName, boolean excludeAllFixedPtms) {
        HashMap<Integer, ArrayList<String>> mainModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> secondaryModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> fixedModificationSites = new HashMap<Integer, ArrayList<String>>();

        if (modifications != null) {
            for (int modSite : modifications.keySet()) {
                for (ModificationMatch modificationMatch : modifications.get(modSite)) {
                    String modName = modificationMatch.getTheoreticPtm();
                    if (modificationMatch.isVariable()) {
                        if (modificationMatch.isConfident()) {
                            if (!mainModificationSites.containsKey(modSite)) {
                                mainModificationSites.put(modSite, new ArrayList<String>());
                            }
                            mainModificationSites.get(modSite).add(modName);
                        } else {
                            if (!secondaryModificationSites.containsKey(modSite)) {
                                secondaryModificationSites.put(modSite, new ArrayList<String>());
                            }
                            secondaryModificationSites.get(modSite).add(modName);
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
        return getTaggedModifiedSequence(modificationProfile, sequence, mainModificationSites, secondaryModificationSites,
                fixedModificationSites, useHtmlColorCoding, useShortName);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
 This method will work only if the PTM found in the peptide are in the
 PTMFactory. /!\ This method uses the modifications as set in the
 modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param sequence the amino acid sequence to annotate
     * @param mainModificationSites the main variable modification sites in a
     * map: aa number -> list of modifications (1 is the first AA) (can be null)
     * @param secondaryModificationSites the secondary variable modification
     * sites in a map: aa number -> list of modifications (1 is the first AA)
     * (can be null)
     * @param fixedModificationSites the fixed modification sites in a map: aa
     * number -> list of modifications (1 is the first AA) (can be null)
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(ModificationProfile modificationProfile, String sequence,
            HashMap<Integer, ArrayList<String>> mainModificationSites, HashMap<Integer, ArrayList<String>> secondaryModificationSites,
            HashMap<Integer, ArrayList<String>> fixedModificationSites, boolean useHtmlColorCoding,
            boolean useShortName) {

        if (mainModificationSites == null) {
            mainModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (secondaryModificationSites == null) {
            secondaryModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (fixedModificationSites == null) {
            fixedModificationSites = new HashMap<Integer, ArrayList<String>>();
        }

        StringBuilder modifiedSequence = new StringBuilder(sequence.length());

        for (int aa = 1; aa <= sequence.length(); aa++) {

            int aaIndex = aa - 1;
            char aminoAcid = sequence.charAt(aaIndex);

            if (mainModificationSites.containsKey(aa) && !mainModificationSites.get(aa).isEmpty()) {
                for (String ptmName : mainModificationSites.get(aa)) { //There should be only one
                    modifiedSequence.append(getTaggedResidue(aminoAcid, ptmName, modificationProfile, true, useHtmlColorCoding, useShortName));
                }
            } else if (secondaryModificationSites.containsKey(aa) && !secondaryModificationSites.get(aa).isEmpty()) {
                for (String ptmName : secondaryModificationSites.get(aa)) { //There should be only one
                    modifiedSequence.append(getTaggedResidue(aminoAcid, ptmName, modificationProfile, false, useHtmlColorCoding, useShortName));
                }
            } else if (fixedModificationSites.containsKey(aa) && !fixedModificationSites.get(aa).isEmpty()) {
                for (String ptmName : fixedModificationSites.get(aa)) { //There should be only one
                    modifiedSequence.append(getTaggedResidue(aminoAcid, ptmName, modificationProfile, true, useHtmlColorCoding, useShortName));
                }
            } else {
                modifiedSequence.append(aminoAcid);
            }
        }

        return modifiedSequence.toString();
    }

    /**
     * Returns the single residue as a tagged string (HTML color or PTM tag).
     *
     * @param residue the residue to tag
     * @param ptmName the name of the PTM
     * @param modificationProfile the modification profile
     * @param mainPtm if true, white font is used on colored background, if
     * false colored font on white background
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the single residue as a tagged string
     */
    private static String getTaggedResidue(char residue, String ptmName, ModificationProfile modificationProfile, boolean mainPtm, boolean useHtmlColorCoding, boolean useShortName) {

        StringBuilder taggedResidue = new StringBuilder();
        PTMFactory ptmFactory = PTMFactory.getInstance();
        PTM ptm = ptmFactory.getPTM(ptmName);
        if (ptm.getType() == PTM.MODAA) {
            if (!useHtmlColorCoding) {
                if (useShortName) {
                    taggedResidue.append(residue).append("<").append(ptmFactory.getShortName(ptmName)).append(">");
                } else {
                    taggedResidue.append(residue).append("<").append(ptmName).append(">");
                }
            } else {
                Color ptmColor = modificationProfile.getColor(ptmName);
                if (mainPtm) {
                    taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(Color.WHITE)).append(";background:#").append(Util.color2Hex(ptmColor)).append("\">").append(residue).append("</span>");
                } else {
                    taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(ptmColor)).append(";background:#").append(Util.color2Hex(Color.WHITE)).append("\">").append(residue).append("</span>");
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
                    if (ptm1.getMass() == ptm2.getMass()) {
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
            if (occurrence2 == null || occurrence2 != occurrence1) {
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
                    ModificationMatch newMatch = new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), reversed);
                    if (modificationMatch.isConfident()) {
                        newMatch.setConfident(true);
                    }
                    if (modificationMatch.isInferred()) {
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
        for (int i = 0; i < sequence.length(); i++) {
            char aa = sequence.charAt(i);
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            if (aminoAcid.iscombination()) {
                return true;
            }
        }
        return false;
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
    public Double getMass() {
        setSequenceStringBuilder(false);
        double mass = 0;
        for (int i = 0; i < length(); i++) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(sequence.charAt(i));
            mass += aminoAcid.monoisotopicMass;
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
