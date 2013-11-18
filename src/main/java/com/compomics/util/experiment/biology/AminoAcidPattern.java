package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.preferences.ModificationProfile;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * An amino acid pattern is a sequence of amino-acids. For example for trypsin:
 * Target R or K not followed by P. the Indexing starts with 0.
 *
 * @author Marc Vaudel
 */
public class AminoAcidPattern implements Serializable, TagComponent {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2823716418631089876L;
    /**
     * The index of the amino acid of interest if there is one. Can be a
     * modification site or a cleavage site. For trypsin: 0.
     */
    private Integer target;
    /**
     * The length of the pattern, -1 if not set.
     */
    private int length = -1;
    /**
     * The list of targeted amino acids at a given index. For trypsin: 0 -> {R,
     * K} 1 -> {}
     */
    private HashMap<Integer, ArrayList<AminoAcid>> aaTargeted = new HashMap<Integer, ArrayList<AminoAcid>>();
    /**
     * The list of excluded amino acids at a given index For trypsin: 0 -> {} 1
     * -> {P}
     */
    private HashMap<Integer, ArrayList<AminoAcid>> aaExcluded = new HashMap<Integer, ArrayList<AminoAcid>>();
    /**
     * The modifications carried by the amino acid sequence at target amino
     * acids.
     */
    private HashMap<Integer, ArrayList<ModificationMatch>> targetModifications = new HashMap<Integer, ArrayList<ModificationMatch>>(); // @TODO: do we need modifications on excluded amino acids?

    /**
     * Creates an empty pattern.
     */
    public AminoAcidPattern() {
        target = 0;
        aaTargeted.put(0, new ArrayList<AminoAcid>());
        aaExcluded.put(0, new ArrayList<AminoAcid>());
    }

    /**
     * Constructor taking a sequence of targeted amino acids as input.
     *
     * @param sequence a sequence of targeted amino acids
     */
    public AminoAcidPattern(String sequence) {

        target = 0;
        aaExcluded.put(0, new ArrayList<AminoAcid>());

        for (int i = 0; i < sequence.length(); i++) {
            char letter = sequence.charAt(i);
            AminoAcid aa = AminoAcid.getAminoAcid(letter);
            ArrayList<AminoAcid> list = new ArrayList<AminoAcid>();
            list.add(aa);
            aaTargeted.put(i, list);
        }
    }

    /**
     * Creates a pattern from another pattern.
     *
     * @param aminoAcidPattern the other pattern
     */
    public AminoAcidPattern(AminoAcidPattern aminoAcidPattern) {

        target = aminoAcidPattern.getTarget();

        for (int index = 0; index < aminoAcidPattern.length(); index++) {
            aaTargeted.put(index, aminoAcidPattern.getTargetedAA(index));
            aaExcluded.put(index, aminoAcidPattern.getExcludedAA(index));
        }
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

        target = 0;
        Collections.sort(targetTesidues);
        ArrayList<AminoAcid> aminoAcids = new ArrayList<AminoAcid>();

        for (String letter : targetTesidues) {
            AminoAcid aa = AminoAcid.getAminoAcid(letter);
            if (aa != null) {
                aminoAcids.add(aa);
            } else {
                throw new IllegalArgumentException("Amino acid not recognized " + letter + ".");
            }
        }

        aaTargeted.put(0, aminoAcids);
    }

    /**
     * Swap two rows in the pattern. The first amino acid is 0.
     *
     * @param fromRow
     * @param toRow
     * @throws IllegalArgumentException
     */
    public void swapRows(int fromRow, int toRow) throws IllegalArgumentException {

        if (aaTargeted.size() < fromRow || aaExcluded.size() < fromRow || fromRow < 0 || toRow < 0) {
            throw new IllegalArgumentException("Illegal row index: " + fromRow);
        }
        if (aaTargeted.size() < toRow || aaExcluded.size() < fromRow || toRow < 0 || fromRow < 0) {
            throw new IllegalArgumentException("Illegal row index: " + toRow);
        }

        ArrayList<AminoAcid> toRowDataTarget = aaTargeted.get(toRow);
        ArrayList<AminoAcid> toRowDataExcluded = aaExcluded.get(toRow);

        aaTargeted.put(toRow, aaTargeted.get(fromRow));
        aaExcluded.put(toRow, aaExcluded.get(fromRow));

        aaTargeted.put(fromRow, toRowDataTarget);
        aaExcluded.put(fromRow, toRowDataExcluded);

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

        if (aaTargeted.containsKey(target)) {
            return aaTargeted.get(target);
        }

        return new ArrayList<AminoAcid>();
    }

    /**
     * Sets the amino acids targeted at a given index. The first amino acid is
     * 0.
     *
     * @param index the index in the pattern
     * @param targets the amino acids targeted
     */
    public void setTargeted(int index, ArrayList<AminoAcid> targets) {
        aaTargeted.put(index, targets);
        length = -1;
    }

    /**
     * Returns the targeted amino acids at a given index in the pattern. The
     * first amino acid is 0.
     *
     * @param index the index in the pattern
     * @return the targeted amino acids
     */
    public ArrayList<AminoAcid> getTargetedAA(int index) {
        return aaTargeted.get(index);
    }

    /**
     * Returns the excluded amino acids at a given index in the pattern. The
     * first amino acid is 0.
     *
     * @param index the index in the pattern
     * @return the excluded amino acids
     */
    public ArrayList<AminoAcid> getExcludedAA(int index) {
        return aaExcluded.get(index);
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
        ArrayList<AminoAcid> aas = getTargetedAA(index);
        if (aas == null) {
            return 0;
        }
        return aas.size();
    }

    /**
     * Returns the number of excluded amino acids at the given index. The first
     * amino acid is 0.
     *
     * @param index the index of interest
     *
     * @return the number of excluded amino acids
     */
    public int getNExcludedAA(int index) {
        ArrayList<AminoAcid> aas = getExcludedAA(index);
        if (aas == null) {
            return 0;
        }
        return aas.size();
    }

    /**
     * Sets the amino acids excluded at a given index. There shall be no
     * excluded amino acid at the targeted index. The first amino acid is 0.
     *
     * @param index the index in the pattern
     * @param exclusions the amino acids excluded
     */
    public void setExcluded(int index, ArrayList<AminoAcid> exclusions) {
        aaExcluded.put(index, exclusions);
        length = -1;
    }

    /**
     * Removes an amino acid index from the pattern. The first amino acid is 0.
     *
     * @param index the index of the amino acid to remove
     */
    public void removeAA(int index) {

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

        indexes = new ArrayList<Integer>(aaExcluded.keySet());
        Collections.sort(indexes);

        for (int aa : indexes) {
            if (aa >= index) {
                if (aa > index) {
                    aaExcluded.put(aa - 1, aaExcluded.get(aa));
                }
                aaExcluded.remove(aa);
            }
        }
        length = -1;
    }

    /**
     * Returns the amino acid pattern as case insensitive pattern for String
     * matching using default single letter code of amino acids.
     *
     * @return the amino acid pattern as java string pattern
     */
    public Pattern getAsStringPattern() {
        return getAsStringPattern(ProteinMatch.MatchingType.string, null);
    }

    /**
     * Returns the amino acid pattern as case insensitive pattern for String
     * matching.
     *
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return the amino acid pattern as java string pattern
     */
    public Pattern getAsStringPattern(ProteinMatch.MatchingType matchingType, Double massTolerance) {

        String regex = "";
        int tempLength = length();

        for (int i = 0; i < tempLength; i++) {

            ArrayList<AminoAcid> tempTarget = aaTargeted.get(i);
            ArrayList<String> toAdd = new ArrayList<String>();

            if (tempTarget == null || tempTarget.isEmpty()) {
                toAdd.addAll(AminoAcid.getAminoAcidsList());
            } else {
                for (AminoAcid aa : tempTarget) {
                    if (!toAdd.contains(aa.singleLetterCode)) {
                        toAdd.add(aa.singleLetterCode);
                    }
                    if (matchingType == ProteinMatch.MatchingType.aminoAcid || matchingType == ProteinMatch.MatchingType.indistiguishibleAminoAcids) {
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
                        if (matchingType == ProteinMatch.MatchingType.indistiguishibleAminoAcids) {
                            for (char tempAa : aa.getIndistinguishibleAminoAcids(massTolerance)) {
                                String value = tempAa + "";
                                if (!toAdd.contains(value)) {
                                    toAdd.add(value);
                                }
                            }
                        }
                    }
                }
            }

            Collections.sort(toAdd);
            ArrayList<String> restrictions = new ArrayList<String>();
            ArrayList<AminoAcid> exclude = aaExcluded.get(i);

            if (exclude != null) {
                for (AminoAcid aa : exclude) {
                    if (!restrictions.contains(aa.singleLetterCode)) {
                        restrictions.add(aa.singleLetterCode);
                    }
                }
            }

            regex += "[";

            for (String aa : toAdd) {
                if (!restrictions.contains(aa)) {
                    regex += aa;
                }
            }

            regex += "]";
        }

        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Returns the indexes where the amino acid pattern was found in the input
     * using default single letter code of amino acids. 1 is the first amino
     * acid.
     *
     * @param input the amino acid input sequence as string
     *
     * @return a list of indexes where the amino acid pattern was found
     */
    public ArrayList<Integer> getIndexes(String input) {
        return getIndexes(input, ProteinMatch.MatchingType.string, Double.NaN);
    }

    /**
     * Returns the indexes where the amino acid pattern was found in the input
     * using default single letter code of amino acids. 1 is the first amino
     * acid.
     *
     * @param input the amino acid input sequence as AminoAcidPattern
     *
     * @return a list of indexes where the amino acid pattern was found
     */
    public ArrayList<Integer> getIndexes(AminoAcidPattern input) {
        return getIndexes(input, ProteinMatch.MatchingType.string, Double.NaN);
    }

    /**
     * Returns the indexes where the amino acid pattern was found in the input.
     * 1 is the first amino acid.
     *
     * @param input the amino acid input sequence as string
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return a list of indexes where the amino acid pattern was found
     */
    public ArrayList<Integer> getIndexes(String input, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int index = 0;
        while ((index = firstIndex(input, matchingType, massTolerance, index)) >= 0) {
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
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return a list of indexes where the amino acid pattern was found
     */
    public ArrayList<Integer> getIndexes(AminoAcidPattern input, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int index = 0;
        while ((index = firstIndex(input, matchingType, massTolerance, index)) >= 0) {
            result.add(index + 1);
            index++;
        }
        return result;
    }

    /**
     * Indicates whether the pattern is found in the given amino-acid sequence
     * using default single letter code of amino acids.
     *
     * @param aminoAcidSequence the amino-acid sequence
     *
     * @return a boolean indicating whether the pattern is found in the given
     * amino-acid sequence
     */
    public boolean matches(String aminoAcidSequence) {
        return matches(aminoAcidSequence, ProteinMatch.MatchingType.string, Double.NaN);
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found. 0 is the first amino acid.
     *
     * @param aminoAcidSequence the amino-acid sequence to look into
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(String aminoAcidSequence, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern(aminoAcidSequence); // @TODO: should not use new here?
        return firstIndex(aminoAcidPattern, matchingType, massTolerance);
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found. 0 is the first amino acid.
     *
     * @param aminoAcidPattern the amino-acid sequence to look into
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(AminoAcidPattern aminoAcidPattern, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        return firstIndex(aminoAcidPattern, matchingType, massTolerance, 0);
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found. 0 is the first amino acid.
     *
     * @param sequence the amino-acid sequence to look into
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * @param startIndex the start index where to start looking for
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(String sequence, ProteinMatch.MatchingType matchingType, Double massTolerance, int startIndex) {
        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern(sequence); // @TODO: should not use new here?
        return firstIndex(aminoAcidPattern, matchingType, massTolerance, startIndex);
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found. 0 is the first amino acid.
     *
     * @param aminoAcidPattern the amino-acid sequence to look into
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * @param startIndex the start index where to start looking for
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(AminoAcidPattern aminoAcidPattern, ProteinMatch.MatchingType matchingType, Double massTolerance, int startIndex) {

        int patternLength = length(); // @TODO: should not use length here?
        int aminoAcidPatternLength = aminoAcidPattern.length();
        int lastIndex = aminoAcidPatternLength - patternLength;

        for (int i = startIndex; i <= lastIndex; i++) {
            boolean match = true;

            for (int j = 0; j < patternLength; j++) {

                for (AminoAcid aminoAcid : aminoAcidPattern.getTargetedAA(i + j)) {

                    char aa = aminoAcid.singleLetterCode.charAt(0);

                    boolean reject = false;
                    ArrayList<AminoAcid> aaList = aaExcluded.get(j);

                    if (aaList != null && !aaList.isEmpty()) {
                        for (AminoAcid vetoAA : aaList) {

                            if (aa == vetoAA.singleLetterCode.charAt(0)) {
                                reject = true;
                                break;
                            }

                            if (matchingType == ProteinMatch.MatchingType.aminoAcid || matchingType == ProteinMatch.MatchingType.indistiguishibleAminoAcids) {

                                for (char tempAA : vetoAA.getSubAminoAcids()) {
                                    if (aa == tempAA) {
                                        reject = true;
                                        break;
                                    }
                                }

                                if (reject) {
                                    break;
                                }

                                for (char tempAA : vetoAA.getCombinations()) {
                                    if (aa == tempAA) {
                                        reject = true;
                                        break;
                                    }
                                }

                                if (matchingType == ProteinMatch.MatchingType.indistiguishibleAminoAcids) {
                                    for (char tempAA : vetoAA.getIndistinguishibleAminoAcids(massTolerance)) {
                                        if (aa == tempAA) {
                                            reject = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (reject) {
                            match = false;
                            break;
                        }
                    }

                    aaList = aaTargeted.get(j);

                    if (aaList != null && !aaList.isEmpty()) {

                        boolean found = false;

                        for (AminoAcid targetedAA : aaList) {
                            if (aa == targetedAA.singleLetterCode.charAt(0)) {
                                found = true;
                                break;
                            } else if (matchingType == ProteinMatch.MatchingType.aminoAcid || matchingType == ProteinMatch.MatchingType.indistiguishibleAminoAcids) {

                                for (char tempAA : targetedAA.getSubAminoAcids()) {
                                    if (aa == tempAA) {
                                        found = true;
                                        break;
                                    }
                                }

                                if (found) {
                                    break;
                                }

                                for (char tempAA : targetedAA.getCombinations()) {
                                    if (aa == tempAA) {
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found && matchingType == ProteinMatch.MatchingType.indistiguishibleAminoAcids) {
                                    for (char tempAA : targetedAA.getIndistinguishibleAminoAcids(massTolerance)) {
                                        if (aa == tempAA) {
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (!found) {
                            match = false;
                            break;
                        }
                    }
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
     * Indicates whether the pattern is found in the given amino-acid sequence.
     *
     * @param aminoAcidSequence the amino-acid sequence
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return a boolean indicating whether the pattern is found in the given
     * amino-acid sequence
     */
    public boolean matches(String aminoAcidSequence, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        return firstIndex(aminoAcidSequence, matchingType, massTolerance) >= 0;
    }

    /**
     * Indicates whether the pattern is found in the given amino-acid sequence.
     *
     * @param aminoAcidPattern the amino-acid sequence
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return a boolean indicating whether the pattern is found in the given
     * amino-acid sequence
     */
    public boolean matches(AminoAcidPattern aminoAcidPattern, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        return firstIndex(aminoAcidPattern, matchingType, massTolerance) >= 0;
    }

    /**
     * Indicates whether the given amino acid sequence starts with the pattern
     * using default single letter code of amino acids.
     *
     * @param aminoAcidSequence the amino acid sequence
     *
     * @return a boolean indicating whether the given amino acid sequence starts
     * with the pattern
     */
    public boolean isStarting(String aminoAcidSequence) {
        return isStarting(aminoAcidSequence, ProteinMatch.MatchingType.string, Double.NaN);
    }

    /**
     * Indicates whether the given amino acid sequence starts with the pattern
     * using default single letter code of amino acids.
     *
     * @param aminoAcidPattern the amino acid sequence
     *
     * @return a boolean indicating whether the given amino acid sequence starts
     * with the pattern
     */
    public boolean isStarting(AminoAcidPattern aminoAcidPattern) {
        return isStarting(aminoAcidPattern, ProteinMatch.MatchingType.string, Double.NaN);
    }

    /**
     * Indicates whether the given amino acid sequence starts with the pattern.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return a boolean indicating whether the given amino acid sequence starts
     * with the pattern
     */
    public boolean isStarting(String aminoAcidSequence, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        int patternLength = length(); // @TODO: should not use length() here?
        return matches(aminoAcidSequence.substring(0, patternLength), matchingType, massTolerance);
    }

    /**
     * Indicates whether the given amino acid sequence starts with the pattern.
     *
     * @param aminoAcidPattern the amino acid sequence
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return a boolean indicating whether the given amino acid sequence starts
     * with the pattern
     */
    public boolean isStarting(AminoAcidPattern aminoAcidPattern, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        int patternLength = length(); // @TODO: should not use length() here?
        return matches(aminoAcidPattern.getSubPattern(0, patternLength), matchingType, massTolerance);
    }

    /**
     * Indicates whether the given amino acid sequence ends with the pattern
     * using default single letter code of amino acids.
     *
     * @param aminoAcidSequence the amino acid sequence
     *
     * @return a boolean indicating whether the given amino acid sequence ends
     * with the pattern
     */
    public boolean isEnding(String aminoAcidSequence) {
        return isEnding(aminoAcidSequence, ProteinMatch.MatchingType.string, Double.NaN);
    }

    /**
     * Indicates whether the given amino acid sequence ends with the pattern
     * using default single letter code of amino acids.
     *
     * @param aminoAcidPattern the amino acid sequence
     *
     * @return a boolean indicating whether the given amino acid sequence ends
     * with the pattern
     */
    public boolean isEnding(AminoAcidPattern aminoAcidPattern) {
        return isEnding(aminoAcidPattern, ProteinMatch.MatchingType.string, Double.NaN);
    }

    /**
     * Indicates whether the given amino acid sequence ends with the pattern.
     *
     * @param aminoAcidPattern the amino acid sequence
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return a boolean indicating whether the given amino acid sequence ends
     * with the pattern
     */
    public boolean isEnding(AminoAcidPattern aminoAcidPattern, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        int patternLength = length();
        return matches(aminoAcidPattern.getSubPattern(aminoAcidPattern.length() - patternLength), matchingType, massTolerance);
    }

    /**
     * Indicates whether the given amino acid sequence ends with the pattern.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return a boolean indicating whether the given amino acid sequence ends
     * with the pattern
     */
    public boolean isEnding(String aminoAcidSequence, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        int patternLength = length();
        return matches(aminoAcidSequence.substring(aminoAcidSequence.length() - patternLength), matchingType, massTolerance);
    }

    /**
     * Indicates whether another AminoAcidPattern targets the same pattern.
     *
     * @param anotherPattern the other AminoAcidPattern
     *
     * @return true if the other AminoAcidPattern targets the same pattern
     */
    public boolean isSameAs(AminoAcidPattern anotherPattern) {
        if (!anotherPattern.getAsStringPattern().pattern().equalsIgnoreCase(getAsStringPattern().pattern())) {
            return false;
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

        if (length == -1) {
            if (aaTargeted.isEmpty() && aaExcluded.isEmpty()) {
                length = 0;
            } else if (aaTargeted.isEmpty()) {
                length = Collections.max(aaExcluded.keySet()) + 1;
            } else if (aaExcluded.isEmpty()) {
                length = Collections.max(aaTargeted.keySet()) + 1;
            } else {
                length = Math.max(Collections.max(aaTargeted.keySet()), Collections.max(aaExcluded.keySet())) + 1;
            }
        }
        return length;
    }

    /**
     * Computes a pattern which can be searched by standard search engines,
     * i.e., a pattern targeting a single amino-acid and not a complex pattern.
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
     * Simple merger for two patterns. To be used carefully.
     *
     * Example: this: target{0->S} exclusion{} otherPattern: target{0->T}
     * exclusion{} result (this): target{0->S|T} exclusion{}
     *
     * Example of misuse: this: target{0->S} exclusion{0->null, 1>P}
     * otherPattern: target{0->T, 1->P} exclusion{} result (this):
     * target{0->S|T, 1->P} exclusion{0->null, 1>P}
     *
     * @param otherPattern another pattern to be merged with this
     */
    public void merge(AminoAcidPattern otherPattern) {

        for (int i = 0; i < otherPattern.length(); i++) {

            if (otherPattern.getNExcludedAA(i) > 0) {
                if (aaExcluded.get(i) == null) {
                    aaExcluded.put(i, new ArrayList<AminoAcid>());
                }
                for (AminoAcid aa : otherPattern.getExcludedAA(i)) {
                    if (!aaExcluded.get(i).contains(aa)) {
                        aaExcluded.get(i).add(aa);
                    }
                }
            }

            if (otherPattern.getNTargetedAA(i) > 0) {
                if (aaTargeted.get(i) == null) {
                    aaTargeted.put(i, new ArrayList<AminoAcid>());
                }
                for (AminoAcid aa : otherPattern.getTargetedAA(i)) {
                    if (!aaTargeted.get(i).contains(aa)) {
                        aaTargeted.get(i).add(aa);
                    }
                }
            }
        }
        for (int i : otherPattern.getModificationMatches().keySet()) {
            addModificationMatches(i, otherPattern.getModificationMatches().get(i));
        }
        length = -1;
    }

    /**
     * Appends another pattern at the end of this pattern.
     *
     * @param otherPattern the other pattern to append.
     */
    public void append(AminoAcidPattern otherPattern) {
        int patternLength = length();
        for (int i = 0; i < otherPattern.length(); i++) {
            int index = patternLength + i;
            aaExcluded.put(index, new ArrayList<AminoAcid>(otherPattern.getExcludedAA(i)));
            aaTargeted.put(index, new ArrayList<AminoAcid>(otherPattern.getTargetedAA(i)));
        }
        for (int i : otherPattern.getModificationMatches().keySet()) {
            addModificationMatches(i + patternLength, otherPattern.getModificationMatches().get(i));
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
        return getAsStringPattern().pattern();
    }

    @Override
    public String asSequence() {
        String result = "";
        for (int i = 0; i < length(); i++) {
            if (getNTargetedAA(i) == 1 && getNExcludedAA(i) == 0) {
                result += getTargetedAA(i).get(0).singleLetterCode;
            } else {
                result += "[";
                if (getNTargetedAA(i) == 0) {
                    result += "X";
                } else {
                    for (AminoAcid aa : getTargetedAA(i)) {
                        result += aa.singleLetterCode;
                    }
                }
                if (getNExcludedAA(i) > 0) {
                    result += "/";
                    for (AminoAcid aa : getExcludedAA(i)) {
                        result += aa.singleLetterCode;
                    }
                }
            }
        }
        return result;
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
        String result = "";
        if (getNTargetedAA(index) == 1 && getNExcludedAA(index) == 0) {
            result += getTargetedAA(index).get(0).singleLetterCode;
        } else {
            result += "[";
            if (getNTargetedAA(index) == 0) {
                result += "X";
            } else {
                for (AminoAcid aa : getTargetedAA(index)) {
                    result += aa.singleLetterCode;
                }
            }
            if (getNExcludedAA(index) > 0) {
                result += "/";
                for (AminoAcid aa : getExcludedAA(index)) {
                    result += aa.singleLetterCode;
                }
            }
        }
        return result;
    }

    /**
     * Getter for the modifications carried by this sequence.
     *
     * @return the modifications matches as found by the search engine
     */
    public HashMap<Integer, ArrayList<ModificationMatch>> getModificationMatches() {
        return targetModifications;
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
        ArrayList<ModificationMatch> result = targetModifications.get(localization);
        if (result == null) {
            return new ArrayList<ModificationMatch>();
        }
        return result;
    }

    /**
     * Clears the list of imported modification matches.
     */
    public void clearModificationMatches() {
        targetModifications.clear();
    }

    /**
     * Adds a modification to one of the amino-acid pattern.
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
        ArrayList<ModificationMatch> modificationMatches = targetModifications.get(localization);
        if (modificationMatches == null) {
            modificationMatches = new ArrayList<ModificationMatch>();
            targetModifications.put(localization, modificationMatches);
        }
        modificationMatches.add(modificationMatch);
    }

    /**
     * Adds a list of modifications to one of the amino-acid pattern.
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
        if (!targetModifications.containsKey(oldIndex) || !targetModifications.get(oldIndex).contains(modificationMatch)) {
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

            if (aminoAcidPattern.getNTargetedAA(patternIndex) > 1 && aminoAcidPattern.getNExcludedAA(patternIndex) > 0) {
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
            if (aminoAcidPattern.getNExcludedAA(patternIndex) > 0) {
                modifiedSequence += "/";
                for (AminoAcid aminoAcid : aminoAcidPattern.getExcludedAA(patternIndex)) {
                    modifiedSequence += aminoAcid.singleLetterCode;
                }
            }
            if (aminoAcidPattern.getNTargetedAA(aa) > 1 && aminoAcidPattern.getNExcludedAA(patternIndex) > 0) {
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
     * amino acids.
     *
     * @return all possible sequences which can be obtained from the targeted
     * amino acids
     */
    public ArrayList<String> getAllPossibleSequences() {
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < length(); i++) {
            if (results.isEmpty()) {
                for (AminoAcid aminoAcid : aaTargeted.get(i)) {
                    results.add(aminoAcid.singleLetterCode);
                }
            } else {
                ArrayList<String> newResult = new ArrayList<String>();
                for (AminoAcid aminoAcid : aaTargeted.get(i)) {
                    for (String sequence : results) {
                        newResult.add(sequence + aminoAcid.singleLetterCode);
                    }
                }
                results = newResult;
            }
        }
        return results;
    }

    @Override
    public Double getMass() {
        double mass = 0;
        for (int i = 0; i < length(); i++) {
            if (getNTargetedAA(i) == 1) {
                mass += getTargetedAA(i).get(0).monoisotopicMass;
            } else {
                throw new IllegalArgumentException("Impossible to estimate the mass of the amino-acid pattern" + asSequence() + ". " + getNTargetedAA(i) + " possibilities at index " + i + ".");
            }
            ArrayList<ModificationMatch> modificationAtIndex = targetModifications.get(i);
            if (modificationAtIndex != null) {
                for (ModificationMatch modificationMatch : modificationAtIndex) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationMatch.getTheoreticPtm());
                    mass += ptm.getMass();
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
     *
     * @return a sub pattern
     */
    public AminoAcidPattern getSubPattern(int startIndex, int endIndex) {
        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern();
        for (int i = startIndex; i <= endIndex; i++) {
            aminoAcidPattern.setTargeted(i - startIndex, getTargetedAA(i));
            aminoAcidPattern.setExcluded(i - startIndex, getExcludedAA(i));
        }
        aminoAcidPattern.setTarget(getTarget() - startIndex);
        for (int i : targetModifications.keySet()) {
            aminoAcidPattern.addModificationMatches(i - startIndex, targetModifications.get(i));
        }
        return aminoAcidPattern;
    }

    /**
     * Returns a sub pattern of the pattern.
     *
     * @param startIndex the start index, inclusive (0 is the first amino acid)
     *
     * @return a sub pattern
     */
    public AminoAcidPattern getSubPattern(int startIndex) {
        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern();
        int patternLength = length();
        for (int i = startIndex; i <= patternLength; i++) {
            aminoAcidPattern.setTargeted(i - startIndex, getTargetedAA(i));
            aminoAcidPattern.setExcluded(i - startIndex, getExcludedAA(i));
        }
        aminoAcidPattern.setTarget(getTarget() - startIndex);
        for (int i : targetModifications.keySet()) {
            aminoAcidPattern.addModificationMatches(i - startIndex, targetModifications.get(i));
        }
        return aminoAcidPattern;
    }

    @Override
    public boolean isSameAs(TagComponent anotherCompontent) {
        if (!(anotherCompontent instanceof AminoAcidPattern)) {
            return false;
        } else {
            AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) anotherCompontent;
            return isSameAs(aminoAcidPattern);
        }
    }
}
