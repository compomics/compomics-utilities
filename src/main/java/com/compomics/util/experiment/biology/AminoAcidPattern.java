package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences.MatchingType;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * An amino acid pattern is a sequence of amino acids. For example for trypsin:
 * Target R or K not followed by P. IMPORTANT: the index for the target residue
 * is by default 0
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynsk
 */
public class AminoAcidPattern extends ExperimentObject implements TagComponent {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2823716418631089876L;
    /**
     * Cache for the amino acids at target.
     */
    private HashSet<Character> aaAtTarget = null;
    /**
     * The length of the pattern, -1 if not set.
     */
    private int length = -1;
    /**
     * The list of targeted amino acids at a given index represented by their
     * single letter code. For trypsin: 0 &gt; {R, K} 1 &gt; {all but P}
     */
    private HashMap<Integer, ArrayList<Character>> residueTargeted = null;
    /**
     * The modifications carried by the amino acid sequence at target amino
     * acids.
     */
    private HashMap<Integer, ArrayList<ModificationMatch>> targetModifications = null;

    /**
     * Creates a blank pattern. All maps are null.
     */
    public AminoAcidPattern() {
        length = 0;
    }

    /**
     * Creates an amino acid pattern based on the given amino acid sequence.
     * Warning, the modification mapping is the same, modifying it here modifies
     * the original sequence.
     *
     * @param aminoAcidSequence the original amino acid sequence
     */
    public AminoAcidPattern(AminoAcidSequence aminoAcidSequence) {
        String sequence = aminoAcidSequence.getSequence();
        residueTargeted = new HashMap<Integer, ArrayList<Character>>(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            char letter = sequence.charAt(i);
            ArrayList<Character> list = new ArrayList<Character>(1);
            list.add(letter);
            residueTargeted.put(i, list);
        }
        length = sequence.length();
        targetModifications = aminoAcidSequence.getModificationMatches();
    }

    /**
     * Creates a pattern from another pattern.
     *
     * @param aminoAcidPattern the other pattern
     */
    public AminoAcidPattern(AminoAcidPattern aminoAcidPattern) {
        HashMap<Integer, ArrayList<Character>> otherTargets = aminoAcidPattern.getAaTargeted();
        if (otherTargets != null) {
            residueTargeted = new HashMap<Integer, ArrayList<Character>>(otherTargets.size());
            for (int index : otherTargets.keySet()) {
                residueTargeted.put(index, (ArrayList<Character>) otherTargets.get(index).clone());
            }
        }
        HashMap<Integer, ArrayList<ModificationMatch>> modificationMatches = aminoAcidPattern.getModificationMatches();
        if (modificationMatches != null) {
            targetModifications = new HashMap<Integer, ArrayList<ModificationMatch>>(modificationMatches.size());
            for (int index : modificationMatches.keySet()) {
                targetModifications.put(index, (ArrayList<ModificationMatch>) modificationMatches.get(index).clone());
            }
        }
    }

    /**
     * Parses the amino acid pattern from the given string as created by the
     * toString() method.
     *
     * @param aminoAcidPatternAsString the amino acid pattern as created by the
     * toString() method
     *
     * @return the amino acid pattern
     */
    public static AminoAcidPattern getAminoAcidPatternFromString(String aminoAcidPatternAsString) {
        return getAminoAcidPatternFromString(aminoAcidPatternAsString, 0);
    }

    /**
     * Parses the amino acid pattern from the given string as created by the
     * toString() method.
     *
     * @param aminoAcidPatternAsString the amino acid pattern as created by the
     * toString() method
     * @param startIndex the start index of the pattern
     *
     * @return the amino acid pattern
     */
    public static AminoAcidPattern getAminoAcidPatternFromString(String aminoAcidPatternAsString, int startIndex) {

        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern();

        // check if pattern contains brackets
        int cntOpenBrackets = 0, lastIndex = 0;
        while ((lastIndex = aminoAcidPatternAsString.indexOf("[", lastIndex)) != -1) {
            lastIndex += "[".length();
            cntOpenBrackets++;
        }
        int cntCloseBrackets = 0;
        lastIndex = 0;
        while ((lastIndex = aminoAcidPatternAsString.indexOf("]", lastIndex)) != -1) {
            lastIndex += "]".length();
            cntCloseBrackets++;
        }

        if (cntOpenBrackets != cntCloseBrackets) {
            throw new IllegalArgumentException("Number of opening and closing brackets unequal");
        }

        if (cntOpenBrackets == 0) {
            for (int i = 0; i < aminoAcidPatternAsString.length(); ++i) {
                ArrayList<Character> aminoAcids = new ArrayList<Character>();
                AminoAcid.getAminoAcid(aminoAcidPatternAsString.charAt(i));
                aminoAcids.add(aminoAcidPatternAsString.charAt(i));
                aminoAcidPattern.addPTMSite(i, aminoAcids);
            }
        } else {
            int pos = 0, siteIndex = -startIndex;
            while (pos < aminoAcidPatternAsString.length()) {
                if (aminoAcidPatternAsString.charAt(pos) == '[') {
                    int end = aminoAcidPatternAsString.indexOf("]", pos + 1);
                    ArrayList<Character> aminoAcids = new ArrayList<Character>();
                    for (int i = pos + 1; i < end; ++i) {
                        AminoAcid.getAminoAcid(aminoAcidPatternAsString.charAt(i));
                        aminoAcids.add(aminoAcidPatternAsString.charAt(i));
                    }
                    aminoAcidPattern.addPTMSite(siteIndex++, aminoAcids);
                    pos = end + 1;
                } else {
                    ArrayList<Character> aminoAcids = new ArrayList<Character>();
                    AminoAcid.getAminoAcid(aminoAcidPatternAsString.charAt(pos));
                    aminoAcids.add(aminoAcidPatternAsString.charAt(pos));
                    aminoAcidPattern.addPTMSite(siteIndex++, aminoAcids);
                    ++pos;
                }
            }

        }

        return aminoAcidPattern;
    }

    /**
     * Returns the map of targeted amino acids. Null if not set.
     *
     * @return the map of targeted amino acids
     */
    public HashMap<Integer, ArrayList<Character>> getAaTargeted() {
        return residueTargeted;
    }

    /**
     * Convenience constructor giving a list of targeted residues as input. For
     * instance (S, T, Y)
     *
     * @param targetResidues a list of targeted residues
     * @throws IllegalArgumentException exception thrown whenever a letter is
     * not recognized as amino acid
     */
    public AminoAcidPattern(ArrayList<String> targetResidues) throws IllegalArgumentException {
        ArrayList<Character> aminoAcids = new ArrayList<Character>(targetResidues.size());
        for (String letter : targetResidues) {
            aminoAcids.add(letter.charAt(0));
        }
        residueTargeted = new HashMap<Integer, ArrayList<Character>>(1);
        residueTargeted.put(0, aminoAcids);
        length = 1;
    }

    /**
     * Swap two rows in the pattern. The first amino acid is 0.
     *
     * @param fromRow from row
     * @param toRow to row
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     */
    public void swapRows(int fromRow, int toRow) throws IllegalArgumentException {

        if (residueTargeted == null) {
            residueTargeted = new HashMap<Integer, ArrayList<Character>>(1);
        }

        if (residueTargeted.size() < fromRow || fromRow < 0 || toRow < 0) {
            throw new IllegalArgumentException("Illegal row index: " + fromRow);
        }
        if (residueTargeted.size() < toRow || toRow < 0 || fromRow < 0) {
            throw new IllegalArgumentException("Illegal row index: " + toRow);
        }

        ArrayList<Character> toRowDataTarget = residueTargeted.get(toRow);
        residueTargeted.put(toRow, residueTargeted.get(fromRow));
        residueTargeted.put(fromRow, toRowDataTarget);

        // TODO: if an error should occur, an index shifting should be added here
        aaAtTarget = null;
    }

    /**
     * Returns the index of the amino acid of interest in the pattern. Null if
     * none.
     *
     * @return the index of the amino acid of interest in the pattern.
     */
    public Integer getTarget() {
        return 0;
    }

    /**
     * Sets the index of the amino acid of interest in the pattern.
     *
     * @param target the index of the amino acid of interest in the pattern.
     */
    public void setTarget(Integer target) {
        
        if (residueTargeted == null) {
            residueTargeted = new HashMap<Integer, ArrayList<Character>>(1);
        }
        
        if (residueTargeted.size() > 0 && !residueTargeted.containsKey(target)) {
            throw new IllegalArgumentException("Target number exceeds residue site for index shifting.");
        }

        HashMap<Integer, ArrayList<Character>> residueTargetedTmp = new HashMap<Integer, ArrayList<Character>>();
        for (HashMap.Entry<Integer, ArrayList<Character>> entry : residueTargeted.entrySet()) {
            residueTargetedTmp.put(entry.getKey() - target, entry.getValue());
        }
        residueTargeted = residueTargetedTmp;
        aaAtTarget = null;
    }

    /**
     * Returns the targeted amino acids at position "target". An empty list if
     * none.
     *
     * @return the targeted amino acids at position "target"
     */
    public ArrayList<Character> getAminoAcidsAtTarget() {
        return getTargetedAA(0);
    }

    /**
     * Returns a set containing the amino acids at target.
     *
     * @return a set containing the amino acids at target
     */
    public HashSet<Character> getAminoAcidsAtTargetSet() {
        if (aaAtTarget == null) {
            ArrayList<Character> aaAtTargetList = getAminoAcidsAtTarget();
            aaAtTarget = new HashSet<Character>(aaAtTargetList);
        }
        return aaAtTarget;
    }

    /**
     * Sets the amino acids targeted at a given index. The first amino acid is
     * 0. Previous value will be silently overwritten.
     *
     * @param index the index in the pattern
     * @param targets the amino acids targeted
     */
    public void setTargeted(int index, ArrayList<Character> targets) {
        if (residueTargeted == null) {
            residueTargeted = new HashMap<Integer, ArrayList<Character>>(1);
        }
        residueTargeted.put(index, targets);
        if (index + 1 > length) {
            length = index + 1;
        }
        aaAtTarget = null;
    }

    /**
     * Excludes the given amino acids from the targeted amino acids at the given
     * index.
     *
     * @param index the index of the excluded amino acid
     * @param exceptions the amino acids to exclude
     */
    public void setExcluded(int index, ArrayList<Character> exceptions) {
        if (residueTargeted == null) {
            residueTargeted = new HashMap<Integer, ArrayList<Character>>(1);
        }
        if (exceptions == null || exceptions.isEmpty()) {
            residueTargeted.put(index, new ArrayList<Character>());
        } else {
            ArrayList<Character> notExcluded = new ArrayList<Character>();
            ArrayList<Character> targeted = residueTargeted.get(index);
            if (targeted == null || targeted.isEmpty()) {
                for (char aa : AminoAcid.getUniqueAminoAcids()) {
                    if (!exceptions.contains(aa)) {
                        notExcluded.add(aa);
                    }
                }
            } else {
                for (Character aminoAcid : targeted) {
                    if (!exceptions.contains(aminoAcid)) {
                        notExcluded.add(aminoAcid);
                    }
                }
            }
            residueTargeted.put(index, notExcluded);
        }
        if (index + 1 > length) {
            length = index + 1;
        }
        aaAtTarget = null;
    }

    /**
     * Returns the targeted amino acids at a given index in the pattern. The
     * first amino acid is 0.
     *
     * @param index the index in the pattern
     * @return the targeted amino acids
     */
    public ArrayList<Character> getTargetedAA(int index) {
        if (residueTargeted != null) {
            ArrayList<Character> result = residueTargeted.get(index);
            if (result != null) {
                return result;
            }
        }
        return new ArrayList<Character>(0);
    }

    /**
     * Returns the number of targeted amino acids at the given index. The first
     * amino acid is 0.
     *
     * @param index the index of interest
     *
     * @return the number of excluded amino acids
     */
    public int getNTargetedAA(int index) {
        if (residueTargeted == null) {
            return 0;
        }
        ArrayList<Character> aas = getTargetedAA(index);
        return aas.size();
    }

    /**
     * Removes an amino acid index from the pattern. The first amino acid is 0.
     *
     * @param index the index of the amino acid to remove
     */
    public void removeAA(int index) { // @TODO: delete does not always work...

        if (residueTargeted != null) {
            ArrayList<Integer> indexes = new ArrayList<Integer>(residueTargeted.keySet());
            Collections.sort(indexes);
            for (int aa : indexes) {
                if (aa >= index) {
                    if (aa > index) {
                        residueTargeted.put(aa - 1, residueTargeted.get(aa)); 
                    }
                    residueTargeted.remove(aa);
                }
            }
        }

        if (targetModifications != null) {
            ArrayList<Integer> indexes = new ArrayList<Integer>(targetModifications.keySet());
            Collections.sort(indexes);
            int ptmIndex = index + 1;
            for (int aa : indexes) {
                if (aa >= ptmIndex) {
                    if (aa > ptmIndex) {
                        targetModifications.put(aa - 1, targetModifications.get(aa));
                    }
                }
            }
        }

        aaAtTarget = null;
        length = -1;
    }

    /**
     * Returns the amino acid pattern as case insensitive pattern for String
     * matching.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param includeMutations if true mutated amino acids will be included
     *
     * @return the amino acid pattern as java string pattern
     */
    public Pattern getAsStringPattern(SequenceMatchingPreferences sequenceMatchingPreferences, boolean includeMutations) {

        MatchingType matchingType = sequenceMatchingPreferences.getSequenceMatchingType();

        int tempLength = length();
        StringBuilder regexBuilder = new StringBuilder(tempLength);

        for (int i = 0; i < tempLength; i++) {

            ArrayList<Character> toAdd = new ArrayList<Character>(1);

            if (residueTargeted != null) {
                ArrayList<Character> tempTarget = residueTargeted.get(i);

                if (tempTarget == null || tempTarget.isEmpty()) {
                    toAdd.ensureCapacity(AminoAcid.getUniqueAminoAcids().length);
                    for (Character aa : AminoAcid.getUniqueAminoAcids()) {
                        toAdd.add(aa);
                    }
                } else {
                    for (Character aa : tempTarget) {
                        if (!toAdd.contains(aa)) {
                            toAdd.add(aa);
                        }
                        if (matchingType == MatchingType.aminoAcid || matchingType == MatchingType.indistiguishableAminoAcids) {
                            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                            for (char tempAa : aminoAcid.getSubAminoAcids()) {
                                if (!toAdd.contains(tempAa)) {
                                    toAdd.add(tempAa);
                                }
                            }
                            for (char tempAa : aminoAcid.getCombinations()) {
                                if (!toAdd.contains(tempAa)) {
                                    toAdd.add(tempAa);
                                }
                            }
                            if (matchingType == MatchingType.indistiguishableAminoAcids
                                    && (aminoAcid == AminoAcid.I || aminoAcid == AminoAcid.J || aminoAcid == AminoAcid.L)) {
                                if (!toAdd.contains('I')) {
                                    toAdd.add('I');
                                }
                                if (!toAdd.contains('J')) {
                                    toAdd.add('L');
                                }
                            }
                        }
                    }
                }
            }

            Collections.sort(toAdd);

            regexBuilder.ensureCapacity(toAdd.size() + 2);
            regexBuilder.append("[");

            for (Character aa : toAdd) {
                regexBuilder.append(aa);
            }

            regexBuilder.append("]");
        }

        return Pattern.compile(regexBuilder.toString(), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Returns the pattern in the PROSITE format.
     *
     * @return the pattern in the PROSITE format
     */
    public String getPrositeFormat() {

        StringBuilder result = new StringBuilder();
        int cpt = 0;

        for (int i = 0; i < length(); i++) {

            ArrayList<Character> targetedAas = getTargetedAA(i);

            if (targetedAas.isEmpty()) {
                cpt++;
            } else if (targetedAas.size() > 15) {
                ArrayList<Character> excludedAas = new ArrayList<Character>();
                for (char aa : AminoAcid.getUniqueAminoAcids()) {
                    if (!targetedAas.contains(aa)) {
                        excludedAas.add(aa);
                    }
                }
                if (cpt > 0) {
                    result.append("(").append(cpt).append(")");
                    cpt = 0;
                }
                result.append("{");
                for (Character aa : excludedAas) {
                    result.append(aa);
                }
                result.append("}");
            } else {
                if (cpt > 0) {
                    result.append("(").append(cpt).append(")");
                    cpt = 0;
                }
                if (!targetedAas.isEmpty()) {
                    result.append("[");
                    for (Character aa : targetedAas) {
                        result.append(aa);
                    }
                    result.append("]");
                }
            }

            if (i == 0) {
                result.append("!");
            }
        }

        return result.toString();
    }

    /**
     * Returns the indexes where the amino acid pattern was found in the input.
     * 1 is the first amino acid.
     *
     * @param input the amino acid input sequence as string
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a list of indexes where the amino acid pattern was found
     */
    public ArrayList<Integer> getIndexes(String input, SequenceMatchingPreferences sequenceMatchingPreferences) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int index = 0;
        while ((index = firstIndex(input, sequenceMatchingPreferences, index)) >= 0) {
            result.add(index + 1);
            index++;
        }
        return result;
    }

    /**
     * Returns the indexes where the amino acid pattern was found in the input.
     * 1 is the first amino acid.
     *
     * @param input the amino acid input sequence as AminoAcidPattern
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a list of indexes where the amino acid pattern was found
     */
    public ArrayList<Integer> getIndexes(AminoAcidPattern input, SequenceMatchingPreferences sequenceMatchingPreferences) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int index = 0;
        while ((index = firstIndex(input, sequenceMatchingPreferences, index)) >= 0) {
            result.add(index + 1);
            index++;
        }
        return result;
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found. 0 is the first amino acid.
     *
     * @param aminoAcidSequence the amino acid sequence to look into
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return firstIndex(aminoAcidSequence, sequenceMatchingPreferences, 0);
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found. 0 is the first amino acid.
     *
     * @param aminoAcidSequence the amino acid sequence to look into
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(AminoAcidSequence aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return firstIndex(aminoAcidSequence.getSequence(), sequenceMatchingPreferences, 0);
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found. 0 is the first amino acid.
     *
     * @param aminoAcidPattern the amino acid sequence to look into
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(AminoAcidPattern aminoAcidPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return firstIndex(aminoAcidPattern, sequenceMatchingPreferences, 0);
    }

    /**
     * Indicates whether the pattern contains a subsequence of amino acids.
     *
     * @param aminoAcidSequence the amino acid sequence to look for
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the first index where the amino acid pattern is found
     */
    public boolean contains(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        AminoAcidPattern pattern = getAminoAcidPatternFromString(aminoAcidSequence);
        return pattern.firstIndex(this, sequenceMatchingPreferences) >= 0;
    }

    /**
     * Indicates whether the pattern contains a subsequence of amino acids.
     *
     * @param aminoAcidPattern the amino acid sequence to look for
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the first index where the amino acid pattern is found
     */
    public boolean contains(AminoAcidPattern aminoAcidPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return aminoAcidPattern.firstIndex(this, sequenceMatchingPreferences) >= 0;
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found. 0 is the first amino acid.
     *
     * @param aminoAcidSequence the amino acid sequence to look into
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param startIndex the start index where to start looking for
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences, int startIndex) {

        int patternLength = length();
        int aminoAcidPatternLength = aminoAcidSequence.length();
        int lastIndex = aminoAcidPatternLength - patternLength;

        for (int i = startIndex; i <= lastIndex; i++) {

            boolean match = true;

            for (int j = 0; j < patternLength; j++) {
                char aa = aminoAcidSequence.charAt(i + j);
                if (!isTargeted(aa, j, sequenceMatchingPreferences)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the first index where the amino acid pattern is found in the
     * given pattern. -1 if not found. 0 is the first amino acid.
     *
     * @param aminoAcidPattern the amino acid sequence to look into
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param startIndex the start index where to start looking for
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(AminoAcidPattern aminoAcidPattern, SequenceMatchingPreferences sequenceMatchingPreferences, int startIndex) {

        int patternLength = length();
        int aminoAcidPatternLength = aminoAcidPattern.length();
        int lastIndex = aminoAcidPatternLength - patternLength;

        for (int i = startIndex; i <= lastIndex; i++) {
            boolean match = true;
            for (int j = 0; j < patternLength; j++) {
                ArrayList<Character> aminoAcids = aminoAcidPattern.getTargetedAA(i + j);
                if (!aminoAcids.isEmpty()) {
                    boolean aaMatched = false;
                    for (Character aa : aminoAcids) {
                        if (isTargeted(aa, j, sequenceMatchingPreferences)) {
                            aaMatched = true;
                            break;
                        }
                    }
                    if (!aaMatched) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Indicates whether the given amino acid at the given index of the pattern
     * is targeted without accounting for mutations.
     *
     * @param aa the amino acid as character
     * @param index the index in the pattern
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return true if the given amino acid at the given index of the pattern is
     * targeted
     */
    public boolean isTargeted(Character aa, int index, SequenceMatchingPreferences sequenceMatchingPreferences) {

        if (residueTargeted != null) {

            MatchingType matchingType = sequenceMatchingPreferences.getSequenceMatchingType();
            ArrayList<Character> aaList = residueTargeted.get(index);

            if (aaList != null && !aaList.isEmpty()) {

                for (int i = 0; i < aaList.size(); i++) {
                    Character targetedAA = aaList.get(i);
                    if (aa.equals(targetedAA)) {
                        return true;
                    } else if (matchingType == MatchingType.aminoAcid || matchingType == MatchingType.indistiguishableAminoAcids) {

                        AminoAcid targetedAminoAcid = AminoAcid.getAminoAcid(targetedAA);

                        for (Character tempAA : targetedAminoAcid.getSubAminoAcids()) {
                            if (aa.equals(tempAA)) {
                                return true;
                            }
                        }

                        for (Character tempAA : targetedAminoAcid.getCombinations()) {
                            if (aa.equals(tempAA)) {
                                return true;
                            }
                        }

                        if (matchingType == MatchingType.indistiguishableAminoAcids
                                && (targetedAminoAcid == AminoAcid.I || targetedAminoAcid == AminoAcid.J || targetedAminoAcid == AminoAcid.L)) {
                            if (aa == 'I' || aa == 'J' || aa == 'L') {
                                return true;
                            }
                        }
                    }
                }
            } else if (aaList != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Indicates whether the pattern is found in the given amino acid sequence.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the pattern is found in the given
     * amino acid sequence
     */
    public boolean matchesIn(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return firstIndex(aminoAcidSequence, sequenceMatchingPreferences) >= 0;
    }

    /**
     * Indicates whether the pattern is found in the given amino acid sequence.
     *
     * @param aminoAcidPattern the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the pattern is found in the given
     * amino acid sequence
     */
    public boolean matchesIn(AminoAcidPattern aminoAcidPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return firstIndex(aminoAcidPattern, sequenceMatchingPreferences) >= 0;
    }

    /**
     * Indicates whether the pattern is found in the given amino acid sequence
     * at the given index, where 0 is the first amino acid. Returns false if the
     * entire pattern cannot be mapped to the sequence.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param index the index at which the matching should be done
     *
     * @return a boolean indicating whether the pattern is found in the given
     * amino acid sequence at the given index
     */
    public boolean matchesAt(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences, int index) {
        int startIndex = index;
        int endIndex = length();
        if (startIndex < 0) {
            return false;
        }
        if (endIndex >= aminoAcidSequence.length()) {
            return false;
        }
        String subSequence = aminoAcidSequence.substring(index, index + length());
        return matches(subSequence, sequenceMatchingPreferences);
    }

    /**
     * Indicates whether the pattern matches the given amino acid sequence.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the pattern is found in the given
     * amino acid sequence
     */
    public boolean matches(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return length() == aminoAcidSequence.length() && firstIndex(aminoAcidSequence, sequenceMatchingPreferences) >= 0;
    }

    /**
     * Indicates whether the pattern is found in the given amino acid sequence.
     *
     * @param aminoAcidPattern the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the pattern is found in the given
     * amino acid sequence
     */
    public boolean matches(AminoAcidPattern aminoAcidPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return length() == aminoAcidPattern.length() && firstIndex(aminoAcidPattern, sequenceMatchingPreferences) >= 0;
    }

    /**
     * Indicates whether the given amino acid sequence starts with the pattern.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the given amino acid sequence starts
     * with the pattern
     */
    public boolean isStarting(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        int patternLength = length();
        return matchesIn(aminoAcidSequence.substring(0, patternLength), sequenceMatchingPreferences);
    }

    /**
     * Indicates whether the given amino acid sequence starts with the pattern.
     *
     * @param aminoAcidPattern the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the given amino acid sequence starts
     * with the pattern
     */
    public boolean isStarting(AminoAcidPattern aminoAcidPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {
        int patternLength = length();
        return matchesIn(aminoAcidPattern.getSubPattern(0, patternLength, false), sequenceMatchingPreferences);
    }

    /**
     * Indicates whether the given amino acid sequence ends with the pattern.
     *
     * @param aminoAcidPattern the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the given amino acid sequence ends
     * with the pattern
     */
    public boolean isEnding(AminoAcidPattern aminoAcidPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {
        int patternLength = length();
        return matchesIn(aminoAcidPattern.getSubPattern(aminoAcidPattern.length() - patternLength, false), sequenceMatchingPreferences);
    }

    /**
     * Indicates whether the given amino acid sequence ends with the pattern.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the given amino acid sequence ends
     * with the pattern
     */
    public boolean isEnding(String aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        int patternLength = length();
        return matchesIn(aminoAcidSequence.substring(aminoAcidSequence.length() - patternLength), sequenceMatchingPreferences);
    }

    /**
     * Indicates whether another AminoAcidPattern targets the same pattern.
     * Modifications are considered equal when of same mass. Modifications
     * should be loaded in the PTM factory.
     *
     * @param anotherPattern the other AminoAcidPattern
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return true if the other AminoAcidPattern targets the same pattern
     */
    public boolean isSameAs(AminoAcidPattern anotherPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {

        if (anotherPattern == null) {
            return false;
        }

        if (!matches(anotherPattern, sequenceMatchingPreferences)) {
            return false;
        }

        PTMFactory ptmFactory = PTMFactory.getInstance();
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> mods1 = getModificationsAt(i);
            ArrayList<ModificationMatch> mods2 = anotherPattern.getModificationsAt(i);
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
     * Indicates whether another AminoAcidPattern targets the same pattern
     * without accounting for PTM localization. Modifications are considered
     * equal when of same mass. Modifications should be loaded in the PTM
     * factory.
     *
     * @param anotherPattern the other AminoAcidPattern
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return true if the other AminoAcidPattern targets the same pattern
     */
    public boolean isSameSequenceAndModificationStatusAs(AminoAcidPattern anotherPattern, SequenceMatchingPreferences sequenceMatchingPreferences) {

        if (!matches(anotherPattern, sequenceMatchingPreferences)) {
            return false;
        }

        PTMFactory ptmFactory = PTMFactory.getInstance();
        HashMap<Double, Integer> masses1 = new HashMap<Double, Integer>();
        for (int i = 1; i <= length(); i++) {
            ArrayList<ModificationMatch> modifications = getModificationsAt(i);
            for (ModificationMatch modMatch : modifications) {
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
            ArrayList<ModificationMatch> modifications = anotherPattern.getModificationsAt(i);
            for (ModificationMatch modMatch : modifications) {
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
     * Returns the length of the pattern in amino acids.
     *
     * @return the length of the pattern in amino acids
     */
    public int length() {
        if (residueTargeted == null) {
            return 0;
        }
        return residueTargeted.size();
    }

    /**
     * Computes a pattern which can be searched by standard search engines,
     * i.e., a pattern targeting a single amino acid and not a complex pattern.
     *
     * @return a pattern which can be searched by standard search engines
     */
    public AminoAcidPattern getStandardSearchPattern() {
        AminoAcidPattern result = new AminoAcidPattern();
        result.setTarget(0);
        result.setTargeted(0, getAminoAcidsAtTarget());
        return result;
    }

    /**
     * Returns the trypsin example as amino acid pattern.
     *
     * @return the trypsin example as amino acid pattern
     */
    public static AminoAcidPattern getTrypsinExample() {
        AminoAcidPattern example = new AminoAcidPattern();
        example.setTarget(0);
        ArrayList<Character> target = new ArrayList<Character>();
        target.add(AminoAcid.K.getSingleLetterCodeAsChar());
        target.add(AminoAcid.R.getSingleLetterCodeAsChar());
        example.setTargeted(0, target);
        ArrayList<Character> exclusion = new ArrayList<Character>();
        exclusion.add(AminoAcid.P.getSingleLetterCodeAsChar());
        example.setExcluded(1, exclusion);
        return example;
    }

    /**
     * Simple merger for two patterns.
     *
     * Example: this: target{0&gt;S} otherPattern: target{0&gt;T} result (this):
     * target{0&gt;S|T}
     *
     * @param otherPattern another pattern to be merged with this
     */
    public void merge(AminoAcidPattern otherPattern) {

        HashMap<Integer, ArrayList<Character>> otherInclusionMap = otherPattern.getAaTargeted();

        if (otherInclusionMap != null) {
            for (int i : otherInclusionMap.keySet()) {
                ArrayList<Character> otherAAs = otherPattern.getTargetedAA(i);
                if (residueTargeted == null) {
                    residueTargeted = new HashMap<Integer, ArrayList<Character>>(otherInclusionMap.size());
                }
                ArrayList<Character> targetedAA = residueTargeted.get(i);
                if (targetedAA == null) {
                    residueTargeted.put(i, (ArrayList<Character>) otherAAs.clone());
                } else if (!otherAAs.isEmpty()) {
                    for (Character aa : otherAAs) {
                        if (!targetedAA.contains(aa)) {
                            targetedAA.add(aa);
                        }
                    }
                } else {
                    targetedAA.clear();
                }
                if (i + 1 > length) {
                    length = i + 1;
                }
            }
        }

        HashMap<Integer, ArrayList<ModificationMatch>> modificationMatches = otherPattern.getModificationMatches();
        if (modificationMatches != null) {
            for (int i : modificationMatches.keySet()) {
                addModificationMatches(i, otherPattern.getModificationMatches().get(i));
                if (i + 1 > length) {
                    length = i + 1;
                }
            }
        }
    }

    /**
     * Appends another pattern at the end of this pattern.
     *
     * @param otherPattern the other pattern to append.
     */
    public void append(AminoAcidPattern otherPattern) {
        int patternLength = length();
        HashMap<Integer, ArrayList<Character>> otherTargetedMap = otherPattern.getAaTargeted();
        if (otherTargetedMap != null) {
            if (residueTargeted == null) {
                residueTargeted = new HashMap<Integer, ArrayList<Character>>(otherTargetedMap.size());
            }
            for (int i : otherTargetedMap.keySet()) {
                int index = patternLength + i;
                residueTargeted.put(index, (ArrayList<Character>) otherTargetedMap.get(i).clone());
            }
        }

        HashMap<Integer, ArrayList<ModificationMatch>> modificationMatches = otherPattern.getModificationMatches();
        if (modificationMatches != null) {
            for (int i : modificationMatches.keySet()) {
                if (i == 0) {
                    throw new IllegalArgumentException("Attempting to merge a pattern with an internal terminal modification.");
                }
                int newIndex = i + patternLength;
                for (ModificationMatch oldModificationMatch : modificationMatches.get(i)) {
                    ModificationMatch newModificationMatch = new ModificationMatch(oldModificationMatch.getTheoreticPtm(), oldModificationMatch.isVariable(), newIndex);
                    addModificationMatch(newIndex, newModificationMatch);
                }
            }
        }
        length = patternLength + otherPattern.length();
    }

    /**
     * Convenience method merging two different patterns (see public void
     * merge(AminoAcidPattern otherPattern) for detailed information of the
     * merging procedure).
     *
     * @param pattern1 the first pattern
     * @param pattern2 the second pattern
     * @return a merged version of the two patterns
     */
    public static AminoAcidPattern merge(AminoAcidPattern pattern1, AminoAcidPattern pattern2) {
        AminoAcidPattern result = new AminoAcidPattern(pattern1);
        result.merge(pattern2);
        return result;
    }

    @Override
    public String toString() {
        return asSequence();
    }

    /**
     * Returns the sequence represented by this amino acid pattern in a new
     * string builder.
     *
     * @return the sequence represented by this amino acid pattern in a new
     * string builder
     */
    public StringBuilder asStringBuilder() {
        StringBuilder result = new StringBuilder(length());
        for (int i = 0; i < length(); i++) {
            if (getNTargetedAA(i) == 1) {
                result.append(getTargetedAA(i).get(0));
            } else {
                int nTargetedAas = getNTargetedAA(i);
                switch (nTargetedAas) {
                    case 0:
                        result.append("X");
                        break;
                    case 1:
                        result.append(getTargetedAA(i).get(0));
                        break;
                    default:
                        result.append("[");
                        for (Character aa : getTargetedAA(i)) {
                            result.append(aa);
                        }
                        result.append("]");
                        break;
                }
            }
        }
        return result;
    }

    @Override
    public String asSequence() {
        return asStringBuilder().toString();
    }

    /**
     * Returns the component of the amino acid pattern at the given index. 0 is
     * the first amino acid.
     *
     * @param index the index in the pattern. 0 is the first amino acid
     *
     * @return the component of the amino acid pattern at the given index
     */
    public String asSequence(int index) {
        return asStringBuilder().substring(index, index + 1);
    }

    /**
     * Getter for the modifications carried by this sequence in a map: aa number
     * &gt; modification matches. 1 is the first amino acid.
     *
     * @return the modifications matches as found by the search engine
     */
    public HashMap<Integer, ArrayList<ModificationMatch>> getModificationMatches() {
        return targetModifications;
    }

    /**
     * Returns a list of the indexes of the amino acids carrying a modification.
     * 1 is the first amino acid.
     *
     * @return a list of the indexes of the amino acids carrying a modification
     */
    public ArrayList<Integer> getModificationIndexes() {
        if (targetModifications == null) {
            return new ArrayList<Integer>();
        }
        return new ArrayList<Integer>(targetModifications.keySet());
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
        if (targetModifications != null) {
            ArrayList<ModificationMatch> result = targetModifications.get(localization);
            if (result != null) {
                return result;
            }
        }
        return new ArrayList<ModificationMatch>();
    }

    /**
     * Removes a modification match in the given pattern.
     *
     * @param localisation the localization of the modification
     * @param modificationMatch the modification match to remove
     */
    public void removeModificationMatch(int localisation, ModificationMatch modificationMatch) {
        if (targetModifications != null) {
            ArrayList<ModificationMatch> modificationMatches = targetModifications.get(localisation);
            if (modificationMatches != null) {
                modificationMatches.remove(modificationMatch);
                if (modificationMatches.isEmpty()) {
                    targetModifications.remove(localisation);
                }
            }
        }
    }

    /**
     * Clears the list of imported modification matches.
     */
    public void clearModificationMatches() {
        if (targetModifications != null) {
            targetModifications.clear();
        }
    }

    /**
     * Adds a modification to one of the amino acid pattern.
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
        if (targetModifications == null) {
            targetModifications = new HashMap<Integer, ArrayList<ModificationMatch>>();
        }
        ArrayList<ModificationMatch> modificationMatches = targetModifications.get(localization);
        if (modificationMatches == null) {
            modificationMatches = new ArrayList<ModificationMatch>();
            targetModifications.put(localization, modificationMatches);
        }
        modificationMatches.add(modificationMatch);
    }

    /**
     * Adds a list of modifications to one of the amino acid pattern.
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
        if (targetModifications == null) {
            targetModifications = new HashMap<Integer, ArrayList<ModificationMatch>>();
        }
        ArrayList<ModificationMatch> modificationMatchesAtIndex = targetModifications.get(localization);
        if (modificationMatchesAtIndex == null) {
            modificationMatchesAtIndex = new ArrayList<ModificationMatch>();
            targetModifications.put(localization, modificationMatchesAtIndex);
        }
        modificationMatches.addAll(modificationMatches);
    }

    /**
     * Adds a list of modifications to one of the amino acid pattern.
     *
     * @param localization the index of the amino acid residue site
     * @param PTMSite valid amino acids for this site
     */
    public void addPTMSite(int localization, ArrayList<Character> PTMSite) {
        if (residueTargeted == null) {
            residueTargeted = new HashMap<Integer, ArrayList<Character>>(1);
        }
        residueTargeted.put(localization, PTMSite);
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
        if (targetModifications == null || !targetModifications.containsKey(oldIndex) || !targetModifications.get(oldIndex).contains(modificationMatch)) {
            throw new IllegalArgumentException("Modification match " + modificationMatch + " not found at index " + oldLocalization + ".");
        }
        targetModifications.get(oldIndex).remove(modificationMatch);
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

        HashMap<Integer, ArrayList<String>> mainModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> secondaryModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> fixedModificationSites = new HashMap<Integer, ArrayList<String>>();

        if (targetModifications != null) {
            for (int modSite : targetModifications.keySet()) {
                for (ModificationMatch modificationMatch : targetModifications.get(modSite)) {
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

        return getTaggedModifiedSequence(modificationProfile, this, mainModificationSites, secondaryModificationSites,
                fixedModificationSites, useHtmlColorCoding, useShortName);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * This method will work only if the PTM found in the peptide are in the
     * PTMFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param aminoAcidPattern the amino acid pattern to annotate
     * @param mainModificationSites the main variable modification sites in a
     * map: aa number &gt; list of modifications (1 is the first AA) (can be
     * null)
     * @param secondaryModificationSites the secondary variable modification
     * sites in a map: aa number &gt; list of modifications (1 is the first AA)
     * (can be null)
     * @param fixedModificationSites the fixed modification sites in a map: aa
     * number &gt; list of modifications (1 is the first AA) (can be null)
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(PtmSettings modificationProfile, AminoAcidPattern aminoAcidPattern,
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

        String modifiedSequence = "";

        for (int aa = 1; aa <= aminoAcidPattern.length(); aa++) {

            int patternIndex = aa - 1;

            if (aminoAcidPattern.getNTargetedAA(patternIndex) > 1) {
                modifiedSequence += "[";
            }
            if (aminoAcidPattern.getNTargetedAA(patternIndex) == 0) {
                if (mainModificationSites.containsKey(aa) && !mainModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : mainModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue('X', ptmName, modificationProfile, true, useHtmlColorCoding, useShortName);
                    }
                } else if (secondaryModificationSites.containsKey(aa) && !secondaryModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : secondaryModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue('X', ptmName, modificationProfile, false, useHtmlColorCoding, useShortName);
                    }
                } else if (fixedModificationSites.containsKey(aa) && !fixedModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : fixedModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue('X', ptmName, modificationProfile, true, useHtmlColorCoding, useShortName);
                    }
                } else {
                    modifiedSequence += "X";
                }
            }
            for (Character aminoAcid : aminoAcidPattern.getTargetedAA(patternIndex)) {
                if (mainModificationSites.containsKey(aa) && !mainModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : mainModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue(aminoAcid, ptmName, modificationProfile, true, useHtmlColorCoding, useShortName);
                    }
                } else if (secondaryModificationSites.containsKey(aa) && !secondaryModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : secondaryModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue(aminoAcid, ptmName, modificationProfile, false, useHtmlColorCoding, useShortName);
                    }
                } else if (fixedModificationSites.containsKey(aa) && !fixedModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : fixedModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue(aminoAcid, ptmName, modificationProfile, true, useHtmlColorCoding, useShortName);
                    }
                } else {
                    modifiedSequence += aminoAcid;
                }
            }
            if (aminoAcidPattern.getNTargetedAA(aa) > 1) {
                modifiedSequence += "]";
            }
        }

        return modifiedSequence;
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
    private static String getTaggedResidue(char residue, String ptmName, PtmSettings modificationProfile, boolean mainPtm, boolean useHtmlColorCoding, boolean useShortName) {

        String taggedResidue = "";
        PTMFactory ptmFactory = PTMFactory.getInstance();
        PTM ptm = ptmFactory.getPTM(ptmName);

        if (ptm.getType() == PTM.MODAA) {
            if (!useHtmlColorCoding) {
                if (useShortName) {
                    taggedResidue += residue + "<" + ptm.getShortName() + ">";
                } else {
                    taggedResidue += residue + "<" + ptmName + ">";
                }
            } else {
                Color ptmColor = modificationProfile.getColor(ptmName);
                if (mainPtm) {
                    taggedResidue
                            += "<span style=\"color:#" + Util.color2Hex(Color.WHITE) + ";background:#" + Util.color2Hex(ptmColor) + "\">"
                            + residue
                            + "</span>";
                } else {
                    taggedResidue
                            += "<span style=\"color:#" + Util.color2Hex(ptmColor) + ";background:#" + Util.color2Hex(Color.WHITE) + "\">"
                            + residue
                            + "</span>";
                }
            }
        } else {
            taggedResidue += residue;
        }

        return taggedResidue;
    }

    /**
     * Returns all possible sequences which can be obtained from the targeted
     * amino acids. Missing amino acids will be denoted as 'X'. This does not
     * implement excluded amino acids.
     *
     * @return all possible sequences which can be obtained from the targeted
     * amino acids
     */
    public ArrayList<String> getAllPossibleSequences() {

        ArrayList<StringBuilder> stringBuilders = new ArrayList<StringBuilder>(1);
        int tempLength = length();

        for (int i = 0; i < length(); i++) {
            if (stringBuilders.isEmpty()) {
                if (residueTargeted != null) {
                    ArrayList<Character> aminoAcids = residueTargeted.get(i);
                    if (aminoAcids != null && !aminoAcids.isEmpty()) {
                        for (Character aminoAcid : aminoAcids) {
                            StringBuilder newBuilder = new StringBuilder(tempLength);
                            newBuilder.append(aminoAcid);
                            stringBuilders.add(newBuilder);
                        }
                    } else {
                        StringBuilder newBuilder = new StringBuilder(tempLength);
                        newBuilder.append('X');
                        stringBuilders.add(newBuilder);
                    }
                } else {
                    StringBuilder newBuilder = new StringBuilder(tempLength);
                    newBuilder.append('X');
                    stringBuilders.add(newBuilder);
                }
            } else {
                ArrayList<StringBuilder> newBuilders = new ArrayList<StringBuilder>(1);
                for (StringBuilder stringBuilder : stringBuilders) {
                    if (residueTargeted != null) {
                        ArrayList<Character> aminoAcids = residueTargeted.get(i);
                        if (aminoAcids != null && !aminoAcids.isEmpty()) {
                            for (Character aminoAcid : residueTargeted.get(i)) {
                                StringBuilder newBuilder = new StringBuilder(stringBuilder);
                                newBuilder.append(aminoAcid);
                                newBuilders.add(newBuilder);
                            }
                        } else {
                            StringBuilder newBuilder = new StringBuilder(stringBuilder);
                            newBuilder.append('X');
                            newBuilders.add(newBuilder);
                        }
                    } else {
                        StringBuilder newBuilder = new StringBuilder(stringBuilder);
                        newBuilder.append('X');
                        newBuilders.add(newBuilder);
                    }
                }
                stringBuilders = newBuilders;
            }
        }

        ArrayList<String> results = new ArrayList<String>(stringBuilders.size());
        for (StringBuilder stringBuilder : stringBuilders) {
            results.add(stringBuilder.toString());
        }

        return results;
    }

    @Override
    public Double getMass() {
        double mass = 0;

        for (int i = 0; i < length(); i++) {
            if (residueTargeted != null) {
                ArrayList<Character> aminoAcids = residueTargeted.get(i);
                if (aminoAcids.size() == 1) {
                    Character aa = getTargetedAA(i).get(0);
                    AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                    mass += aminoAcid.getMonoisotopicMass();
                } else {
                    throw new IllegalArgumentException("Impossible to estimate the mass of the amino acid pattern " + asSequence() + ". "
                            + aminoAcids.size() + " amino acids at target position " + i + " as targeted amino acid.");
                }
            } else {
                throw new IllegalArgumentException("Impossible to estimate the mass of the amino acid pattern " + asSequence() + ". null as targeted amino acid map.");
            }
            if (targetModifications != null) {
                ArrayList<ModificationMatch> modificationAtIndex = targetModifications.get(i + 1);
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

    /**
     * Returns a sub pattern of the pattern.
     *
     * @param startIndex the start index, inclusive (0 is the first amino acid)
     * @param endIndex the end index, inclusive
     * @param updateTarget boolean indicating whether the target of the pattern
     * shall be updated. If yes it will be shifted by startIndex, simply copied
     * otherwise.
     *
     * @return a sub pattern
     */
    public AminoAcidPattern getSubPattern(int startIndex, int endIndex, boolean updateTarget) {

        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern();

        if (residueTargeted != null) {
            for (int i : residueTargeted.keySet()) {
                if (i >= startIndex && i <= endIndex) {
                    ArrayList<Character> aminoAcids = (ArrayList<Character>) residueTargeted.get(i).clone();
                    aminoAcidPattern.setTargeted(i - startIndex, aminoAcids);
                }
            }
        }

        if (updateTarget) {
            aminoAcidPattern.setTarget(getTarget() - startIndex);
        } else {
            aminoAcidPattern.setTarget(getTarget());
        }

        if (targetModifications != null) {
            for (int i : targetModifications.keySet()) {
                if (i > startIndex && i <= endIndex + 1) {
                    int index = i - startIndex;
                    ArrayList<ModificationMatch> modificationMatches = targetModifications.get(i);
                    ArrayList<ModificationMatch> newMatches = new ArrayList<ModificationMatch>(modificationMatches.size());
                    for (ModificationMatch modificationMatch : modificationMatches) {
                        newMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), index));
                    }
                    aminoAcidPattern.addModificationMatches(index, newMatches);
                }
            }
        }

        return aminoAcidPattern;
    }

    /**
     * Returns a sub pattern of the pattern.
     *
     * @param startIndex the start index, inclusive (0 is the first amino acid)
     * @param updateTarget boolean indicating whether the target of the pattern
     * shall be updated. If yes it will be shifted by startIndex, simply copied
     * otherwise.
     *
     * @return a sub pattern
     */
    public AminoAcidPattern getSubPattern(int startIndex, boolean updateTarget) {
        return getSubPattern(startIndex, length(), updateTarget);
    }

    /**
     * Returns an amino acid pattern which is a reversed version of the current
     * pattern.
     *
     * @return an amino acid pattern which is a reversed version of the current
     * pattern
     */
    public AminoAcidPattern reverse() {

        AminoAcidPattern newPattern = new AminoAcidPattern();

        if (residueTargeted != null) {
            for (int i : residueTargeted.keySet()) {
                int reversed = length() - i - 1;
                newPattern.setTargeted(reversed, (ArrayList<Character>) residueTargeted.get(i).clone());
            }
        }

        if (targetModifications != null) {
            for (int i : targetModifications.keySet()) {
                int reversed = length() - i + 1;
                for (ModificationMatch modificationMatch : targetModifications.get(i)) {
                    ModificationMatch newMatch = new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), reversed);
                    if (modificationMatch.isConfident()) {
                        newMatch.setConfident(true);
                    }
                    if (modificationMatch.isInferred()) {
                        newMatch.setInferred(true);
                    }
                    newPattern.addModificationMatch(reversed, newMatch);
                }
            }
        }

        /*
        if (target > -1) {
            newPattern.setTarget(length() - target - 1);
        }
         */
        return newPattern;
    }

    @Override
    public boolean isSameAs(TagComponent anotherCompontent, SequenceMatchingPreferences sequenceMatchingPreferences) {
        if (!(anotherCompontent instanceof AminoAcidPattern)) {
            return false;
        } else {
            AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) anotherCompontent;
            return isSameAs(aminoAcidPattern, sequenceMatchingPreferences);
        }
    }

    @Override
    public boolean isSameSequenceAndModificationStatusAs(TagComponent anotherCompontent, SequenceMatchingPreferences sequenceMatchingPreferences) {
        if (!(anotherCompontent instanceof AminoAcidPattern)) {
            return false;
        } else {
            AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) anotherCompontent;
            return isSameSequenceAndModificationStatusAs(aminoAcidPattern, sequenceMatchingPreferences);
        }
    }
}
