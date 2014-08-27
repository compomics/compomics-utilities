package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.ModificationProfile;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences.MatchingType;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * An amino acid pattern is a sequence of amino acids. For example for trypsin:
 * Target R or K not followed by P. the Indexing starts with 0.
 *
 * @author Marc Vaudel
 */
public class AminoAcidPattern extends ExperimentObject implements TagComponent {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2823716418631089876L;
    /**
     * The index of the amino acid of interest if there is one. Can be a
     * modification site or a cleavage site. For trypsin: 0.
     */
    private Integer target = 0;
    /**
     * The length of the pattern, -1 if not set.
     */
    private int length = -1;
    /**
     * The list of targeted amino acids at a given index. For trypsin: 0 -> {R,
     * K} 1 -> {all but P}
     */
    private HashMap<Integer, ArrayList<AminoAcid>> aaTargeted = null;
    /**
     * The list of excluded amino acids at a given index For trypsin: 0 -> {} 1
     * -> {P}
     *
     * @deprecated target all but this one instead
     */
    private HashMap<Integer, ArrayList<AminoAcid>> aaExcluded = null;
    /**
     * The modifications carried by the amino acid sequence at target amino
     * acids.
     */
    private HashMap<Integer, ArrayList<ModificationMatch>> targetModifications = null;

    /**
     * Creates a blank pattern. All maps are null.
     */
    public AminoAcidPattern() {
    }

    /**
     * Constructor taking a sequence of targeted amino acids as input.
     *
     * @param sequence a sequence of targeted amino acids
     */
    public AminoAcidPattern(String sequence) {
        aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            char letter = sequence.charAt(i);
            AminoAcid aa = AminoAcid.getAminoAcid(letter);
            ArrayList<AminoAcid> list = new ArrayList<AminoAcid>(1);
            list.add(aa);
            aaTargeted.put(i, list);
        }
        length = sequence.length();
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
        aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            char letter = sequence.charAt(i);
            AminoAcid aa = AminoAcid.getAminoAcid(letter);
            ArrayList<AminoAcid> list = new ArrayList<AminoAcid>(1);
            list.add(aa);
            aaTargeted.put(i, list);
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
        target = aminoAcidPattern.getTarget();
        HashMap<Integer, ArrayList<AminoAcid>> otherTargets = aminoAcidPattern.getAaTargeted();
        if (otherTargets != null) {
            aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>(otherTargets.size());
            for (int index : otherTargets.keySet()) {
                aaTargeted.put(index, (ArrayList<AminoAcid>) otherTargets.get(index).clone());
            }
        }
        HashMap<Integer, ArrayList<AminoAcid>> otherExcluded = aminoAcidPattern.getAaExcluded();
        if (otherExcluded != null) {
            // Backward compatibility
            for (int index : otherExcluded.keySet()) {
                setExcluded(index, otherExcluded.get(index));
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
     * Returns the map of targeted amino acids. Null if not set.
     *
     * @return the map of targeted amino acids
     */
    public HashMap<Integer, ArrayList<AminoAcid>> getAaTargeted() {
        return aaTargeted;
    }

    /**
     * Returns the map of excluded amino acids. Null if not set.
     *
     * @deprecated use targeted amino acids only
     * @return the map of excluded amino acids
     */
    public HashMap<Integer, ArrayList<AminoAcid>> getAaExcluded() {
        return aaExcluded;
    }

    /**
     * Convenience constructor giving a list of targeted residues as input. For
     * instance (S, T, Y)
     *
     * @param targetTesidues a list of targeted residues
     * @throws IllegalArgumentException exception thrown whenever a letter is
     * not recognized as amino acid
     */
    public AminoAcidPattern(ArrayList<String> targetTesidues) throws IllegalArgumentException {
        ArrayList<AminoAcid> aminoAcids = new ArrayList<AminoAcid>(targetTesidues.size());
        for (String letter : targetTesidues) {
            AminoAcid aa = AminoAcid.getAminoAcid(letter);
            if (aa != null) {
                aminoAcids.add(aa);
            } else {
                throw new IllegalArgumentException("Amino acid not recognized " + letter + ".");
            }
        }
        aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>(1);
        aaTargeted.put(0, aminoAcids);
        length = 1;
    }

    /**
     * Swap two rows in the pattern. The first amino acid is 0.
     *
     * @param fromRow
     * @param toRow
     * @throws IllegalArgumentException
     */
    public void swapRows(int fromRow, int toRow) throws IllegalArgumentException {

        if (aaTargeted.size() < fromRow || fromRow < 0 || toRow < 0) {
            throw new IllegalArgumentException("Illegal row index: " + fromRow);
        }
        if (aaTargeted.size() < toRow || toRow < 0 || fromRow < 0) {
            throw new IllegalArgumentException("Illegal row index: " + toRow);
        }

        ArrayList<AminoAcid> toRowDataTarget = aaTargeted.get(toRow);

        aaTargeted.put(toRow, aaTargeted.get(fromRow));

        aaTargeted.put(fromRow, toRowDataTarget);

        if (target == fromRow) {
            target = toRow;
        } else if (target == toRow) {
            target = fromRow;
        }
    }

    /**
     * Returns the index of the amino acid of interest in the pattern. Null if
     * none.
     *
     * @return the index of the amino acid of interest in the pattern.
     */
    public Integer getTarget() {
        return target;
    }

    /**
     * Sets the index of the amino acid of interest in the pattern.
     *
     * @param target the index of the amino acid of interest in the pattern.
     */
    public void setTarget(Integer target) {
        this.target = target;
    }

    /**
     * Returns the targeted amino acids at position "target". An empty list if
     * none.
     *
     * @return the targeted amino acids at position "target"
     */
    public ArrayList<AminoAcid> getAminoAcidsAtTarget() {
        return getTargetedAA(target);
    }

    /**
     * Sets the amino acids targeted at a given index. The first amino acid is
     * 0. Previous value will be silently overwritten.
     *
     * @param index the index in the pattern
     * @param targets the amino acids targeted
     */
    public void setTargeted(int index, ArrayList<AminoAcid> targets) {
        if (aaTargeted == null) {
            aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>(1);
        }
        aaTargeted.put(index, targets);
        if (index + 1 > length) {
            length = index + 1;
        }
    }

    /**
     * Excludes the given amino acids from the targeted amino acids at the given
     * index.
     *
     * @param index the index of the excluded amino acid
     * @param exceptions the amino acids to exclude
     */
    public void setExcluded(int index, ArrayList<AminoAcid> exceptions) {
        if (aaTargeted == null) {
            aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>(1);
        }
        if (exceptions == null || exceptions.isEmpty()) {
            aaTargeted.put(index, new ArrayList<AminoAcid>());
        } else {
            ArrayList<AminoAcid> notExcluded = new ArrayList<AminoAcid>();
            ArrayList<AminoAcid> targeted = aaTargeted.get(index);
            if (targeted == null || targeted.isEmpty()) {
                for (char aa : AminoAcid.getUniqueAminoAcids()) {
                    AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                    if (!exceptions.contains(aminoAcid)) {
                        notExcluded.add(aminoAcid);
                    }
                }
            } else {
                for (AminoAcid aminoAcid : targeted) {
                    if (!exceptions.contains(aminoAcid)) {
                        notExcluded.add(aminoAcid);
                    }
                }
            }
            aaTargeted.put(index, notExcluded);
        }
        if (index + 1 > length) {
            length = index + 1;
        }
    }

    /**
     * Returns the targeted amino acids at a given index in the pattern. The
     * first amino acid is 0.
     *
     * @param index the index in the pattern
     * @return the targeted amino acids
     */
    public ArrayList<AminoAcid> getTargetedAA(int index) {
        if (aaTargeted != null) {
            ArrayList<AminoAcid> result = aaTargeted.get(index);
            if (result != null) {
                return result;
            }
        }
        return new ArrayList<AminoAcid>();
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
        if (aaTargeted == null) {
            return 0;
        }
        ArrayList<AminoAcid> aas = getTargetedAA(index);
        return aas.size();
    }

    /**
     * Removes an amino acid index from the pattern. The first amino acid is 0.
     *
     * @param index the index of the amino acid to remove
     */
    public void removeAA(int index) {

        if (aaTargeted != null) {
            ArrayList<Integer> indexes = new ArrayList<Integer>(aaTargeted.keySet());
            Collections.sort(indexes);
            for (int aa : indexes) {
                if (aa >= index) {
                    if (aa > index) {
                        aaTargeted.put(aa - 1, aaTargeted.get(aa));
                    }
                    aaTargeted.remove(aa);
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

        length = -1;
    }

    /**
     * Returns the amino acid pattern as case insensitive pattern for String
     * matching.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the amino acid pattern as java string pattern
     */
    public Pattern getAsStringPattern(SequenceMatchingPreferences sequenceMatchingPreferences) {

        MatchingType matchingType = sequenceMatchingPreferences.getSequenceMatchingType();

        String regex = "";
        int tempLength = length();

        for (int i = 0; i < tempLength; i++) {

            ArrayList<String> toAdd = new ArrayList<String>();

            if (aaTargeted != null) {
                ArrayList<AminoAcid> tempTarget = aaTargeted.get(i);

                if (tempTarget == null || tempTarget.isEmpty()) {
                    toAdd.addAll(AminoAcid.getAminoAcidsList());
                } else {
                    for (AminoAcid aa : tempTarget) {
                        if (!toAdd.contains(aa.singleLetterCode)) {
                            toAdd.add(aa.singleLetterCode);
                        }
                        if (matchingType == MatchingType.aminoAcid || matchingType == MatchingType.indistiguishableAminoAcids) {
                            for (char tempAa : aa.getSubAminoAcids()) {
                                String value = tempAa + "";
                                if (!toAdd.contains(value)) {
                                    toAdd.add(value);
                                }
                            }
                            for (char tempAa : aa.getCombinations()) {
                                String value = tempAa + "";
                                if (!toAdd.contains(value)) {
                                    toAdd.add(value);
                                }
                            }
                            if (matchingType == MatchingType.indistiguishableAminoAcids) {
                                for (char tempAa : aa.getIndistinguishableAminoAcids(sequenceMatchingPreferences.getMs2MzTolerance())) {
                                    String value = tempAa + "";
                                    if (!toAdd.contains(value)) {
                                        toAdd.add(value);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Collections.sort(toAdd);

            regex += "[";

            for (String aa : toAdd) {
                regex += aa;
            }

            regex += "]";
        }

        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Returns the pattern in the prosite format.
     *
     * @return the pattern in the prosite format
     */
    public String getPrositeFormat() {
        StringBuilder result = new StringBuilder();
        int cpt = 0;
        for (int i = 0; i < length(); i++) {
            ArrayList<AminoAcid> targetedAas = getTargetedAA(i);
            if (targetedAas.isEmpty()) {
                cpt++;
            } else if (targetedAas.size() > 15) {
                ArrayList<AminoAcid> excludedAas = new ArrayList<AminoAcid>();
                for (char aa : AminoAcid.getUniqueAminoAcids()) {
                    AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                    if (!targetedAas.contains(aminoAcid)) {
                        excludedAas.add(aminoAcid);
                    }
                }
                if (cpt > 0) {
                    result.append("(").append(cpt).append(")");
                    cpt = 0;
                }
                result.append("{");
                for (AminoAcid aa : excludedAas) {
                    result.append(aa.singleLetterCode);
                }
                result.append("}");
            } else {
                if (cpt > 0) {
                    result.append("(").append(cpt).append(")");
                    cpt = 0;
                }
                if (!targetedAas.isEmpty()) {
                    result.append("[");
                    for (AminoAcid aa : targetedAas) {
                        result.append(aa.singleLetterCode);
                    }
                    result.append("]");
                }
            }
            if (i == target) {
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
        AminoAcidPattern pattern = new AminoAcidPattern(aminoAcidSequence);
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
                }
                if (!match) {
                    break;
                }
            }
            if (match) {
                return i + target;
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
                ArrayList<AminoAcid> aminoAcids = aminoAcidPattern.getTargetedAA(i + j);
                if (!aminoAcids.isEmpty()) {
                    boolean aaMatched = false;
                    for (AminoAcid aminoAcid : aminoAcids) {
                        char aa = aminoAcid.singleLetterCode.charAt(0);
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
                return i + target;
            }
        }
        return -1;
    }

    /**
     * Indicates whether the given amino acid at the given index of the pattern
     * is targeted.
     *
     * @param aa the amino acid as character
     * @param index the index in the pattern
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return true if the given amino acid at the given index of the pattern is
     * targeted
     */
    public boolean isTargeted(char aa, int index, SequenceMatchingPreferences sequenceMatchingPreferences) {

        if (aaTargeted != null) {

            MatchingType matchingType = sequenceMatchingPreferences.getSequenceMatchingType();

            ArrayList<AminoAcid> aaList = aaTargeted.get(index);

            if (aaList != null && !aaList.isEmpty()) {

                for (AminoAcid targetedAA : aaList) {
                    if (aa == targetedAA.getSingleLetterCodeAsChar()) {
                        return true;
                    } else if (matchingType == MatchingType.aminoAcid || matchingType == MatchingType.indistiguishableAminoAcids) {

                        for (char tempAA : targetedAA.getSubAminoAcids()) {
                            if (aa == tempAA) {
                                return true;
                            }
                        }

                        for (char tempAA : targetedAA.getCombinations()) {
                            if (aa == tempAA) {
                                return true;
                            }
                        }

                        if (matchingType == MatchingType.indistiguishableAminoAcids) {
                            for (char tempAA : targetedAA.getIndistinguishableAminoAcids(sequenceMatchingPreferences.getMs2MzTolerance())) {
                                if (aa == tempAA) {
                                    return true;
                                }
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
        return matches(aminoAcidSequence.substring(0, patternLength), sequenceMatchingPreferences);
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
        return matches(aminoAcidPattern.getSubPattern(0, patternLength, false), sequenceMatchingPreferences);
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
        return matches(aminoAcidPattern.getSubPattern(aminoAcidPattern.length() - patternLength, false), sequenceMatchingPreferences);
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
        return matches(aminoAcidSequence.substring(aminoAcidSequence.length() - patternLength), sequenceMatchingPreferences);
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
     * Returns the length of the pattern in amino acids.
     *
     * @return the length of the pattern in amino acids
     */
    public int length() {
        if (length == -1 || length == 0) { //we need to check the 0 case every time due to backward compatibility issues
            if (aaTargeted == null || aaTargeted.isEmpty()) {
                length = 0;
            } else {
                length = Collections.max(aaTargeted.keySet()) + 1;
            }
        }
        return length;
    }

    /**
     * Computes a pattern which can be searched by standard search engines,
     * i.e., a pattern targeting a single amino acid and not a complex pattern.
     *
     * @return a pattern which can be searched by standard search engines
     */
    public AminoAcidPattern getStandardSearchPattern() {
        AminoAcidPattern result = new AminoAcidPattern();
        result.setTarget(target);
        result.setTargeted(target, getAminoAcidsAtTarget());
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
        ArrayList<AminoAcid> target = new ArrayList<AminoAcid>();
        target.add(AminoAcid.K);
        target.add(AminoAcid.R);
        example.setTargeted(0, target);
        ArrayList<AminoAcid> exclusion = new ArrayList<AminoAcid>();
        exclusion.add(AminoAcid.P);
        example.setExcluded(1, exclusion);
        return example;
    }

    /**
     * Simple merger for two patterns.
     *
     * Example: this: target{0->S} otherPattern: target{0->T} result (this):
     * target{0->S|T}
     *
     * @param otherPattern another pattern to be merged with this
     */
    public void merge(AminoAcidPattern otherPattern) {

        HashMap<Integer, ArrayList<AminoAcid>> otherInclusionMap = otherPattern.getAaTargeted();

        if (otherInclusionMap != null) {
            for (int i : otherInclusionMap.keySet()) {
                ArrayList<AminoAcid> otherAAs = otherPattern.getTargetedAA(i);
                if (aaTargeted == null) {
                    aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>(otherInclusionMap.size());
                }
                ArrayList<AminoAcid> targetedAA = aaTargeted.get(i);
                if (targetedAA == null) {
                    aaTargeted.put(i, (ArrayList<AminoAcid>) otherAAs.clone());
                } else {
                    if (!otherAAs.isEmpty()) {
                        for (AminoAcid aa : otherAAs) {
                            if (!targetedAA.contains(aa)) {
                                targetedAA.add(aa);
                            }
                        }
                    } else {
                        targetedAA.clear();
                    }
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
        HashMap<Integer, ArrayList<AminoAcid>> otherTargetedMap = otherPattern.getAaTargeted();
        if (otherTargetedMap != null) {
            if (aaTargeted == null) {
                aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>(otherTargetedMap.size());
            }
            for (int i : otherTargetedMap.keySet()) {
                int index = patternLength + i;
                aaTargeted.put(index, (ArrayList<AminoAcid>) otherTargetedMap.get(i).clone());
            }
        }

        HashMap<Integer, ArrayList<ModificationMatch>> modificationMatches = otherPattern.getModificationMatches();
        if (modificationMatches != null) {
            for (int i : modificationMatches.keySet()) {
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
                result.append(getTargetedAA(i).get(0).singleLetterCode);
            } else {
                int nTargetedAas = getNTargetedAA(i);
                if (nTargetedAas == 0) {
                    result.append("X");
                } else if (nTargetedAas == 1) {
                    result.append(getTargetedAA(i).get(0).singleLetterCode);
                } else {
                    result.append("[");
                    for (AminoAcid aa : getTargetedAA(i)) {
                        result.append(aa.singleLetterCode);
                    }
                    result.append("]");
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
        return asStringBuilder().toString();
    }

    /**
     * Getter for the modifications carried by this sequence in a map: aa number
     * -> modification matches. 1 is the first amino acid.
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
        ArrayList<ModificationMatch> modificationMatches = targetModifications.get(localisation);
        if (modificationMatches != null) {
            modificationMatches.remove(modificationMatch);
            if (modificationMatches.isEmpty()) {
                targetModifications.remove(localisation);
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
     * does not include html start end tags or terminal annotation.
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
    public static String getTaggedModifiedSequence(ModificationProfile modificationProfile, AminoAcidPattern aminoAcidPattern,
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
                        modifiedSequence += getTaggedResidue("X", ptmName, modificationProfile, true, useHtmlColorCoding, useShortName);
                    }
                } else if (secondaryModificationSites.containsKey(aa) && !secondaryModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : secondaryModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue("X", ptmName, modificationProfile, false, useHtmlColorCoding, useShortName);
                    }
                } else if (fixedModificationSites.containsKey(aa) && !fixedModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : fixedModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue("X", ptmName, modificationProfile, true, useHtmlColorCoding, useShortName);
                    }
                } else {
                    modifiedSequence += "X";
                }
            }
            for (AminoAcid aminoAcid : aminoAcidPattern.getTargetedAA(patternIndex)) {
                if (mainModificationSites.containsKey(aa) && !mainModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : mainModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue(aminoAcid.singleLetterCode, ptmName, modificationProfile, true, useHtmlColorCoding, useShortName);
                    }
                } else if (secondaryModificationSites.containsKey(aa) && !secondaryModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : secondaryModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue(aminoAcid.singleLetterCode, ptmName, modificationProfile, false, useHtmlColorCoding, useShortName);
                    }
                } else if (fixedModificationSites.containsKey(aa) && !fixedModificationSites.get(aa).isEmpty()) {
                    for (String ptmName : fixedModificationSites.get(aa)) { //There should be only one
                        modifiedSequence += getTaggedResidue(aminoAcid.singleLetterCode, ptmName, modificationProfile, true, useHtmlColorCoding, useShortName);
                    }
                } else {
                    modifiedSequence += aminoAcid.singleLetterCode;
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
    private static String getTaggedResidue(String residue, String ptmName, ModificationProfile modificationProfile, boolean mainPtm, boolean useHtmlColorCoding, boolean useShortName) {

        String taggedResidue = "";
        PTMFactory ptmFactory = PTMFactory.getInstance();
        PTM ptm = ptmFactory.getPTM(ptmName);
        if (ptm.getType() == PTM.MODAA) {
            if (!useHtmlColorCoding) {
                if (useShortName) {
                    taggedResidue += residue + "<" + ptmFactory.getShortName(ptmName) + ">";
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
        ArrayList<StringBuilder> stringBuilders = new ArrayList<StringBuilder>();
        for (int i = 0; i < length(); i++) {
            if (stringBuilders.isEmpty()) {
                if (aaTargeted != null) {
                    ArrayList<AminoAcid> aminoAcids = aaTargeted.get(i);
                    if (aminoAcids != null && !aminoAcids.isEmpty()) {
                        for (AminoAcid aminoAcid : aminoAcids) {
                            stringBuilders.add(new StringBuilder(aminoAcid.singleLetterCode));
                        }
                    } else {
                        stringBuilders.add(new StringBuilder("X"));
                    }
                } else {
                    stringBuilders.add(new StringBuilder("X"));
                }
            } else {
                ArrayList<StringBuilder> newBuilders = new ArrayList<StringBuilder>();
                for (StringBuilder stringBuilder : stringBuilders) {
                    StringBuilder newBuilder = new StringBuilder(stringBuilder);
                    if (aaTargeted != null) {
                        ArrayList<AminoAcid> aminoAcids = aaTargeted.get(i);
                        if (aminoAcids != null && !aminoAcids.isEmpty()) {
                            for (AminoAcid aminoAcid : aaTargeted.get(i)) {
                                newBuilder.append(aminoAcid.singleLetterCode);
                                newBuilders.add(newBuilder);
                            }
                        } else {
                            newBuilder.append("X");
                            newBuilders.add(newBuilder);
                        }
                    } else {
                        newBuilder.append("X");
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
            if (aaTargeted != null) {
                ArrayList<AminoAcid> aminoAcids = aaTargeted.get(i);
                if (aminoAcids.size() == 1) {
                    mass += getTargetedAA(i).get(0).monoisotopicMass;
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
        if (aaTargeted != null) {
            for (int i : aaTargeted.keySet()) {
                if (i >= startIndex && i <= endIndex) {
                    ArrayList<AminoAcid> aminoAcids = (ArrayList<AminoAcid>) aaTargeted.get(i).clone();
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

        if (aaTargeted != null) {
            for (int i : aaTargeted.keySet()) {
                int reversed = length() - i - 1;
                newPattern.setTargeted(reversed, (ArrayList<AminoAcid>) aaTargeted.get(i).clone());
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

        if (target > -1) {
            newPattern.setTarget(length() - target - 1);
        }

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
