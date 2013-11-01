package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.identification.matches.ProteinMatch;
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
public class AminoAcidPattern implements Serializable {

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
     * Swap two rows in the pattern.
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
     * Sets the amino acids targeted at a given index.
     *
     * @param index the index in the pattern
     * @param targets the amino acids targeted
     */
    public void setTargeted(int index, ArrayList<AminoAcid> targets) {
        aaTargeted.put(index, targets);
    }

    /**
     * Returns the targeted amino acids at a given index in the pattern.
     *
     * @param index the index in the pattern
     * @return the targeted amino acids
     */
    public ArrayList<AminoAcid> getTargetedAA(int index) {
        return aaTargeted.get(index);
    }

    /**
     * Returns the excluded amino acids at a given index in the pattern.
     *
     * @param index the index in the pattern
     * @return the excluded amino acids
     */
    public ArrayList<AminoAcid> getExcludedAA(int index) {
        return aaExcluded.get(index);
    }

    /**
     * Sets the amino acids excluded at a given index. There shall be no
     * excluded amino acid at the targeted index.
     *
     * @param index the index in the pattern
     * @param exclusions the amino acids excluded
     */
    public void setExcluded(int index, ArrayList<AminoAcid> exclusions) {
        aaExcluded.put(index, exclusions);
    }

    /**
     * Removes an amino acid index from the pattern.
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
        int length = length();

        for (int i = 0; i < length; i++) {

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
     * found.
     *
     * @param aminoAcidSequence the amino-acid sequence to look into
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(String aminoAcidSequence, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        return firstIndex(aminoAcidSequence, matchingType, massTolerance, 0);
    }

    /**
     * Returns the first index where the amino acid pattern is found. -1 if not
     * found.
     *
     * @param aminoAcidSequence the amino-acid sequence to look into
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * @param startIndex the start index where to start looking for
     *
     * @return the first index where the amino acid pattern is found
     */
    public int firstIndex(String aminoAcidSequence, ProteinMatch.MatchingType matchingType, Double massTolerance, int startIndex) {

        int patternLength = length();
        int lastIndex = aminoAcidSequence.length() - patternLength;

        for (int i = startIndex; i <= lastIndex; i++) {

            boolean match = true;

            for (int j = 0; j < patternLength; j++) {

                char aa = aminoAcidSequence.charAt(i + j);
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
        return matches(aminoAcidSequence.substring(0, length()), matchingType, massTolerance);
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
        return matches(aminoAcidSequence.substring(aminoAcidSequence.length() - length()), matchingType, massTolerance);
    }

    /**
     * Indicates whether another AminoAcidPattern targets the same pattern.
     *
     * @param anotherPattern the other AminoAcidPattern
     *
     * @return true if the other AminoAcidPattern targets the same pattern
     */
    public boolean isSameAs(AminoAcidPattern anotherPattern) {
        return anotherPattern.getAsStringPattern().pattern().equalsIgnoreCase(getAsStringPattern().pattern());
    }

    /**
     * Returns the length of the pattern in amino acids.
     *
     * @return the length of the pattern in amino acids
     */
    public int length() {

        if (aaTargeted.isEmpty() && aaExcluded.isEmpty()) {
            return 1;
        }

        if (aaTargeted.isEmpty()) {
            return Collections.max(aaExcluded.keySet()) + 1;
        }

        if (aaExcluded.isEmpty()) {
            return Collections.max(aaTargeted.keySet()) + 1;
        }

        return Math.max(Collections.max(aaTargeted.keySet()), Collections.max(aaExcluded.keySet())) + 1;
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

            if (otherPattern.getExcludedAA(i) != null && !otherPattern.getExcludedAA(i).isEmpty()) {
                if (aaExcluded.get(i) == null) {
                    aaExcluded.put(i, new ArrayList<AminoAcid>());
                }
                for (AminoAcid aa : otherPattern.getExcludedAA(i)) {
                    if (!aaExcluded.get(i).contains(aa)) {
                        aaExcluded.get(i).add(aa);
                    }
                }
            }

            if (otherPattern.getTargetedAA(i) != null && !otherPattern.getTargetedAA(i).isEmpty()) {
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
}
