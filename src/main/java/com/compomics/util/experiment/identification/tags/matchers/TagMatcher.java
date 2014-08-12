package com.compomics.util.experiment.identification.tags.matchers;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.experiment.identification.tags.tagcomponents.MassGap;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class matches tags to peptides.
 *
 * @author Marc Vaudel
 */
public class TagMatcher {

    /**
     * Returns the possible peptides which can be created on this sequence
     * indexed by their start index. Null if not found. Note: PTMs must be in
     * the PTM factory. PTMs are considered at a target amino acid only, longer
     * patterns are not taken into account.
     *
     * @param tag the tag to look for
     * @param sequence the sequence where to look for the tag
     * @param tagIndex the index where the tag is located
     * @param componentIndex the index of the component of the tag indexed by
     * tagIndex in the content list
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param massTolerance the ms2 tolerance
     * @param fixedModifications the fixed modifications to consider
     * @param variableModifications the variable modifications to consider
     * @param reportFixedPtms a boolean indicating whether fixed PTMs should be
     * reported in the Peptide object
     *
     * @return the possible peptides which can be created on this sequence
     * indexed by their start index
     */
    public static HashMap<Integer, ArrayList<Peptide>> getPeptideMatches(Tag tag, String sequence, int tagIndex,
            int componentIndex, SequenceMatchingPreferences sequenceMatchingPreferences, double massTolerance,
            ArrayList<String> fixedModifications, ArrayList<String> variableModifications, boolean reportFixedPtms) {

        ArrayList<TagComponent> content = tag.getContent();

        HashMap<Integer, ArrayList<Peptide>> result = new HashMap<Integer, ArrayList<Peptide>>();
        double minMod = 0;
        for (String modificationName : variableModifications) {
            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
            if (ptm.getMass() < minMod) {
                minMod = ptm.getMass();
            }
        }
        int sequenceLastIndex = sequence.length() - 1;

        // Check tag components to the N-term
        ArrayList<AminoAcidSequence> nTermPossibleSequences = new ArrayList<AminoAcidSequence>();
        ArrayList<Integer> nTermPossibleIndexes = new ArrayList<Integer>();
        nTermPossibleSequences.add(new AminoAcidSequence());
        nTermPossibleIndexes.add(tagIndex);

        for (int i = componentIndex - 1; i >= 0; i--) {

            TagComponent tagComponent = content.get(i);
            ArrayList<AminoAcidSequence> newSequences = new ArrayList<AminoAcidSequence>();
            ArrayList<Integer> newIndexes = new ArrayList<Integer>();
            Iterator<Integer> nTermPossibleIndexesIterator = nTermPossibleIndexes.iterator();

            if (tagComponent instanceof AminoAcidPattern) {

                for (AminoAcidSequence nTermSequence : nTermPossibleSequences) {

                    int aaIndex = nTermPossibleIndexesIterator.next();
                    AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                    int startIndex = aaIndex - aminoAcidPattern.length();

                    if (startIndex >= 0) {

                        String subSequence = sequence.substring(startIndex, aaIndex);

                        if (aminoAcidPattern.matches(subSequence, sequenceMatchingPreferences)) {

                            AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidPattern.getModificationMatches());
                            boolean goodTerminalPTms = true;

                            if (i == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODN && startIndex == 0) {
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(1)) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    } else if (ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODNAA && startIndex == 0) {
                                        if (ptm.getPattern().firstIndex(subSequence, sequenceMatchingPreferences) != 0) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(1)) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (goodTerminalPTms) {
                                newSequence.append(nTermSequence);
                                newIndexes.add(startIndex);
                                newSequences.add(newSequence);
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof AminoAcidSequence) {

                for (AminoAcidSequence nTermSequence : nTermPossibleSequences) {

                    int aaIndex = nTermPossibleIndexesIterator.next();
                    AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                    int startIndex = aaIndex - aminoAcidSequence.length();

                    if (startIndex >= 0) {

                        String subSequence = sequence.substring(startIndex, aaIndex);

                        if (aminoAcidSequence.matches(subSequence, sequenceMatchingPreferences)) {

                            AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidSequence.getModificationMatches());
                            boolean goodTerminalPTms = true;

                            if (i == 0) {

                                for (String modificationName : fixedModifications) {

                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);

                                    if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODN && startIndex == 0) {
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(1)) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    } else if (ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODNAA && startIndex == 0) {
                                        if (ptm.getPattern().firstIndex(subSequence, sequenceMatchingPreferences) != 0) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(1)) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (goodTerminalPTms) {
                                newSequence.append(nTermSequence);
                                newIndexes.add(startIndex);
                                newSequences.add(newSequence);
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof MassGap) {

                double massGap = tagComponent.getMass();

                for (AminoAcidSequence nTermSequence : nTermPossibleSequences) {

                    int aaIndex = nTermPossibleIndexesIterator.next();
                    int currentIndex = aaIndex;
                    ArrayList<Double> possiblePatternsMasses = new ArrayList<Double>();
                    ArrayList<AminoAcidSequence> possibleSequences = new ArrayList<AminoAcidSequence>();
                    ArrayList<AminoAcidSequence> validSequences = new ArrayList<AminoAcidSequence>();

                    while (--aaIndex >= 0) {

                        char aa = sequence.charAt(aaIndex);
                        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                        double fixedMass = 0;
                        ArrayList<ModificationMatch> fixedModificationMatches = new ArrayList<ModificationMatch>();

                        for (String modificationName : fixedModifications) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                            AminoAcidPattern ptmPattern = ptm.getPattern();
                            if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                fixedMass += ptm.getMass();
                                fixedModificationMatches.add(new ModificationMatch(modificationName, false, 1));
                            }
                        }

                        if (possiblePatternsMasses.isEmpty()) {

                            AminoAcidSequence newPattern = new AminoAcidSequence(aminoAcid.singleLetterCode);

                            if (reportFixedPtms) {
                                for (ModificationMatch modificationMatch : fixedModificationMatches) {
                                    newPattern.addModificationMatch(1, modificationMatch);
                                }
                            }

                            double noModMass = aminoAcid.monoisotopicMass + fixedMass;
                            possibleSequences.add(newPattern);
                            possiblePatternsMasses.add(noModMass);

                            for (String modificationName : variableModifications) {

                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                AminoAcidPattern ptmPattern = ptm.getPattern();

                                if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                    newPattern = new AminoAcidSequence(aminoAcid.singleLetterCode);
                                    newPattern.addModificationMatch(1, new ModificationMatch(modificationName, true, 1));
                                    double newMass = noModMass + ptm.getMass();
                                    possibleSequences.add(newPattern);
                                    possiblePatternsMasses.add(newMass);
                                }
                            }
                        } else {
                            ArrayList<Double> newPossibleSequencesMasses = new ArrayList<Double>();
                            ArrayList<AminoAcidSequence> newPossibleSequences = new ArrayList<AminoAcidSequence>();
                            Iterator<AminoAcidSequence> newPossibleSequencesMassesIterator = possibleSequences.iterator();

                            for (double mass : possiblePatternsMasses) {
                                AminoAcidSequence aminoAcidSequence = newPossibleSequencesMassesIterator.next();
                                AminoAcidSequence newSequence = new AminoAcidSequence(aminoAcid.singleLetterCode);
                                double noModMass = aminoAcid.monoisotopicMass + fixedMass + mass;
                                newSequence.append(aminoAcidSequence);

                                if (reportFixedPtms) {
                                    for (ModificationMatch modificationMatch : fixedModificationMatches) {
                                        newSequence.addModificationMatch(1, modificationMatch);
                                    }
                                }

                                newPossibleSequences.add(newSequence);
                                newPossibleSequencesMasses.add(noModMass);

                                for (String modificationName : variableModifications) {

                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    AminoAcidPattern ptmPattern = ptm.getPattern();

                                    if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                        newSequence = new AminoAcidSequence(aminoAcid.singleLetterCode);
                                        newSequence.append(aminoAcidSequence);
                                        newSequence.addModificationMatch(1, new ModificationMatch(modificationName, true, 1));
                                        double newMass = noModMass + ptm.getMass();
                                        newPossibleSequences.add(newSequence);
                                        newPossibleSequencesMasses.add(newMass);
                                    }
                                }
                            }

                            possibleSequences.clear();
                            possiblePatternsMasses.clear();
                            possibleSequences = newPossibleSequences;
                            possiblePatternsMasses = newPossibleSequencesMasses;
                        }

                        ArrayList<ModificationMatch> nTermModifications = new ArrayList<ModificationMatch>();
                        double fixedNTermModifications = 0;

                        for (String modificationName : fixedModifications) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                            if (ptm.getType() == PTM.MODNP) {
                                fixedNTermModifications = ptm.getMass();
                                nTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                break;
                            }
                        }

                        if (aaIndex == 0) {
                            for (String modificationName : fixedModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                if (ptm.getType() == PTM.MODN) {
                                    fixedNTermModifications = ptm.getMass();
                                    nTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                    break;
                                }
                            }
                        }

                        Iterator<AminoAcidSequence> possibleSequencesIterator = possibleSequences.iterator();
                        Iterator<Double> possiblePatternsMassesIterator = possiblePatternsMasses.iterator();

                        while (possibleSequencesIterator.hasNext()) {

                            AminoAcidSequence aminoAcidSequence = possibleSequencesIterator.next();
                            double mass = possiblePatternsMassesIterator.next();

                            for (String modificationName : fixedModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                AminoAcidPattern ptmPattern = ptm.getPattern();
                                if (ptm.getType() == PTM.MODNPAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(0), ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                    fixedNTermModifications = ptm.getMass();
                                    nTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                    break;
                                }
                            }
                            if (aaIndex == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    AminoAcidPattern ptmPattern = ptm.getPattern();
                                    if (ptm.getType() == PTM.MODNAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(0), ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                        fixedNTermModifications = ptm.getMass();
                                        nTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                        break;
                                    }
                                }
                            }
                            if (mass + fixedNTermModifications + minMod > massGap + massTolerance) {
                                possibleSequencesIterator.remove();
                                possiblePatternsMassesIterator.remove();
                            } else {
                                if (Math.abs(mass + fixedNTermModifications - massGap) <= massTolerance) {
                                    if (reportFixedPtms) {
                                        for (ModificationMatch modificationMatch : nTermModifications) {
                                            aminoAcidSequence.addModificationMatch(1, modificationMatch);
                                        }
                                    }
                                    validSequences.add(aminoAcidSequence);
                                    possibleSequencesIterator.remove();
                                    possiblePatternsMassesIterator.remove();
                                } else {
                                    boolean found = false;
                                    if (aaIndex == 0) {
                                        for (String modificationName : variableModifications) {
                                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                            AminoAcidPattern ptmPattern = ptm.getPattern();
                                            if (ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(0), ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                                if (Math.abs(mass + fixedNTermModifications + ptm.getMass() - massGap) <= massTolerance) {
                                                    aminoAcidSequence.addModificationMatch(1, new ModificationMatch(modificationName, true, 1));
                                                    if (reportFixedPtms) {
                                                        for (ModificationMatch modificationMatch : nTermModifications) {
                                                            aminoAcidSequence.addModificationMatch(1, modificationMatch);
                                                        }
                                                    }
                                                    validSequences.add(aminoAcidSequence);
                                                    possibleSequencesIterator.remove();
                                                    possiblePatternsMassesIterator.remove();
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (!found) {
                                        for (String modificationName : variableModifications) {
                                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                            AminoAcidPattern ptmPattern = ptm.getPattern();
                                            if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(0), ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                                if (Math.abs(mass + fixedNTermModifications + ptm.getMass() - massGap) <= massTolerance) {
                                                    aminoAcidSequence.addModificationMatch(1, new ModificationMatch(modificationName, true, 1));
                                                    if (reportFixedPtms) {
                                                        for (ModificationMatch modificationMatch : nTermModifications) {
                                                            aminoAcidSequence.addModificationMatch(1, modificationMatch);
                                                        }
                                                    }
                                                    validSequences.add(aminoAcidSequence);
                                                    possibleSequencesIterator.remove();
                                                    possiblePatternsMassesIterator.remove();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (possibleSequences.isEmpty()) {
                            break;
                        }
                    }
                    for (AminoAcidSequence aminoAcidSequence : validSequences) {
                        aminoAcidSequence.append(nTermSequence);
                        int newIndex = currentIndex - aminoAcidSequence.length();
                        newIndexes.add(newIndex);
                        newSequences.add(aminoAcidSequence);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tag component " + tagComponent.getClass() + " not implemented for sequence matching.");
            }
            if (newIndexes.isEmpty()) {
                return result;
            } else {
                nTermPossibleIndexes.clear();
                nTermPossibleSequences.clear();
                nTermPossibleIndexes = newIndexes;
                nTermPossibleSequences = newSequences;
            }
        }

        TagComponent componentAtIndex = content.get(componentIndex);
        int componentAtIndexLength;
        int endTagIndex = tagIndex - 1;
        HashMap<Integer, ArrayList<ModificationMatch>> modificationsAtIndex = null;

        if (componentAtIndex instanceof AminoAcidPattern) {

            AminoAcidPattern tagPattern = (AminoAcidPattern) componentAtIndex;
            componentAtIndexLength = tagPattern.length();
            modificationsAtIndex = tagPattern.getModificationMatches();
            endTagIndex += componentAtIndexLength;

            // if the component is at the N-term check its n-term modifications
            if (componentIndex == 0) {
                boolean goodTerminalPTms = true;
                for (String modificationName : fixedModifications) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                    if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODN && tagIndex == 0) {
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagPattern.getModificationsAt(1)) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    } else if (ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODNAA && tagIndex == 0) {
                        if (ptm.getPattern().firstIndex(tagPattern, sequenceMatchingPreferences) != 0) {
                            goodTerminalPTms = false;
                            break;
                        }
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagPattern.getModificationsAt(1)) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    }
                }
                if (!goodTerminalPTms) {
                    return result;
                }
            }

            // if the component is at the C-term check its c-term modifications
            if (componentIndex == content.size() - 1) {
                boolean goodTerminalPTms = true;
                for (String modificationName : fixedModifications) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                    if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODC && endTagIndex == sequenceLastIndex) {
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagPattern.getModificationsAt(tagPattern.length())) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    } else if (ptm.getType() == PTM.MODCPAA || ptm.getType() == PTM.MODCAA && endTagIndex == sequenceLastIndex) {
                        if (ptm.getPattern().firstIndex(tagPattern, sequenceMatchingPreferences) != 0) {
                            goodTerminalPTms = false;
                            break;
                        }
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagPattern.getModificationsAt(tagPattern.length())) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    }
                }
                if (!goodTerminalPTms) {
                    return result;
                }
            }
        } else if (componentAtIndex instanceof AminoAcidSequence) {

            AminoAcidSequence tagSequence = (AminoAcidSequence) componentAtIndex;
            componentAtIndexLength = tagSequence.length();
            modificationsAtIndex = tagSequence.getModificationMatches();
            endTagIndex += componentAtIndexLength;

            // if the component is at the N-term check its n-term modifications
            if (componentIndex == 0) {
                boolean goodTerminalPTms = true;
                for (String modificationName : fixedModifications) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                    if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODN && tagIndex == 0) {
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagSequence.getModificationsAt(1)) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    } else if (ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODNAA && tagIndex == 0) {
                        if (ptm.getPattern().firstIndex(tagSequence, sequenceMatchingPreferences) != 0) {
                            goodTerminalPTms = false;
                            break;
                        }
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagSequence.getModificationsAt(1)) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    }
                }
                if (!goodTerminalPTms) {
                    return result;
                }
            }

            // if the component is at the C-term check its c-term modifications
            if (componentIndex == content.size() - 1) {

                boolean goodTerminalPTms = true;

                for (String modificationName : fixedModifications) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                    if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODC && endTagIndex == sequenceLastIndex) {
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagSequence.getModificationsAt(tagSequence.length())) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    } else if (ptm.getType() == PTM.MODCPAA || ptm.getType() == PTM.MODCAA && endTagIndex == sequenceLastIndex) {
                        if (ptm.getPattern().firstIndex(tagSequence, sequenceMatchingPreferences) != 0) {
                            goodTerminalPTms = false;
                            break;
                        }
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagSequence.getModificationsAt(tagSequence.length())) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    }
                }
                if (!goodTerminalPTms) {
                    return result;
                }
            }
        } else {
            throw new UnsupportedOperationException("Tag mapping not supported for tag component " + componentAtIndex.getClass() + ".");
        }

        // Check tag components to the C-term
        ArrayList<AminoAcidSequence> cTermPossibleSequences = new ArrayList<AminoAcidSequence>();
        ArrayList<Integer> cTermPossibleIndexes = new ArrayList<Integer>();
        cTermPossibleSequences.add(new AminoAcidSequence());
        cTermPossibleIndexes.add(endTagIndex);

        for (int i = componentIndex + 1; i < content.size(); i++) {

            TagComponent tagComponent = content.get(i);
            ArrayList<AminoAcidSequence> newSequences = new ArrayList<AminoAcidSequence>();
            ArrayList<Integer> newIndexes = new ArrayList<Integer>();
            Iterator<Integer> cTermPossibleIndexesIterator = cTermPossibleIndexes.iterator();

            if (tagComponent instanceof AminoAcidPattern) {

                for (AminoAcidSequence cTermSequence : cTermPossibleSequences) {

                    int aaIndex = cTermPossibleIndexesIterator.next();
                    AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                    int endIndex = aaIndex + aminoAcidPattern.length();

                    if (endIndex <= sequenceLastIndex) {

                        String subSequence = sequence.substring(aaIndex, endIndex);

                        if (aminoAcidPattern.matches(subSequence, sequenceMatchingPreferences)) {
                            AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidPattern.getModificationMatches());
                            boolean goodTerminalPTms = true;

                            if (i == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODC && endIndex == sequenceLastIndex) {
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(newSequence.length())) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    } else if (ptm.getType() == PTM.MODCPAA || ptm.getType() == PTM.MODCAA && endIndex == sequenceLastIndex) {
                                        if (ptm.getPattern().firstIndex(subSequence, sequenceMatchingPreferences) != 0) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(newSequence.length())) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (goodTerminalPTms) {
                                cTermSequence.append(newSequence);
                                newIndexes.add(endIndex);
                                newSequences.add(cTermSequence);
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof AminoAcidSequence) {

                for (AminoAcidSequence cTermSequence : cTermPossibleSequences) {

                    int aaIndex = cTermPossibleIndexesIterator.next();
                    AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                    int endIndex = aaIndex + aminoAcidSequence.length();

                    if (endIndex <= sequenceLastIndex) {

                        String subSequence = sequence.substring(aaIndex, endIndex);
                        if (aminoAcidSequence.matches(subSequence, sequenceMatchingPreferences)) {

                            AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidSequence.getModificationMatches());
                            boolean goodTerminalPTms = true;

                            if (i == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODC && endIndex == sequenceLastIndex) {
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(newSequence.length())) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    } else if (ptm.getType() == PTM.MODCPAA || ptm.getType() == PTM.MODCAA && endIndex == sequenceLastIndex) {
                                        if (ptm.getPattern().firstIndex(subSequence, sequenceMatchingPreferences) != 0) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(newSequence.length())) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (goodTerminalPTms) {
                                cTermSequence.append(newSequence);
                                newIndexes.add(endIndex);
                                newSequences.add(cTermSequence);
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof MassGap) {

                double massGap = tagComponent.getMass();
                for (AminoAcidSequence cTermSequence : cTermPossibleSequences) {

                    int aaIndex = cTermPossibleIndexesIterator.next();
                    int currentIndex = aaIndex;
                    ArrayList<Double> possibleSequencesMasses = new ArrayList<Double>();
                    ArrayList<AminoAcidSequence> possibleSequences = new ArrayList<AminoAcidSequence>();
                    ArrayList<AminoAcidSequence> validSequences = new ArrayList<AminoAcidSequence>();

                    while (++aaIndex <= sequenceLastIndex) {

                        char aa = sequence.charAt(aaIndex);
                        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                        double fixedMass = 0;
                        ArrayList<ModificationMatch> fixedModificationMatches = new ArrayList<ModificationMatch>();

                        for (String modificationName : fixedModifications) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                            AminoAcidPattern ptmPattern = ptm.getPattern();
                            if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                fixedMass += ptm.getMass();
                                fixedModificationMatches.add(new ModificationMatch(modificationName, false, 1));
                            }
                        }

                        if (possibleSequencesMasses.isEmpty()) {

                            AminoAcidSequence newSequence = new AminoAcidSequence(aminoAcid.singleLetterCode);
                            int modIndex = newSequence.length();

                            if (reportFixedPtms) {
                                for (ModificationMatch modificationMatch : fixedModificationMatches) {
                                    modificationMatch.setModificationSite(modIndex);
                                    newSequence.addModificationMatch(modIndex, modificationMatch);
                                }
                            }

                            double noModMass = aminoAcid.monoisotopicMass + fixedMass;
                            possibleSequences.add(newSequence);
                            possibleSequencesMasses.add(noModMass);

                            for (String modificationName : variableModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                AminoAcidPattern ptmPattern = ptm.getPattern();
                                if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                    newSequence = new AminoAcidSequence(aminoAcid.singleLetterCode);
                                    newSequence.addModificationMatch(modIndex, new ModificationMatch(modificationName, true, modIndex));
                                    double newMass = noModMass + ptm.getMass();
                                    possibleSequences.add(newSequence);
                                    possibleSequencesMasses.add(newMass);
                                }
                            }
                        } else {

                            ArrayList<Double> newPossiblePatternsMasses = new ArrayList<Double>();
                            ArrayList<AminoAcidSequence> newPossibleSequences = new ArrayList<AminoAcidSequence>();
                            Iterator<AminoAcidSequence> newPossibleSequencesMassesIterator = possibleSequences.iterator();

                            for (double mass : possibleSequencesMasses) {

                                AminoAcidSequence aminoAcidSequence = newPossibleSequencesMassesIterator.next();
                                AminoAcidSequence newSequence = new AminoAcidSequence(aminoAcidSequence);
                                double noModMass = aminoAcid.monoisotopicMass + fixedMass + mass;
                                newSequence.append(aminoAcid.singleLetterCode);
                                int modIndex = newSequence.length();

                                if (reportFixedPtms) {
                                    for (ModificationMatch modificationMatch : fixedModificationMatches) {
                                        modificationMatch.setModificationSite(modIndex);
                                        newSequence.addModificationMatch(modIndex, modificationMatch);
                                    }
                                }

                                newPossibleSequences.add(newSequence);
                                newPossiblePatternsMasses.add(noModMass);

                                for (String modificationName : variableModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    AminoAcidPattern ptmPattern = ptm.getPattern();
                                    if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                        newSequence = new AminoAcidSequence(aminoAcidSequence);
                                        newSequence.append(aminoAcid.singleLetterCode);
                                        newSequence.addModificationMatch(modIndex, new ModificationMatch(modificationName, true, modIndex));
                                        double newMass = noModMass + ptm.getMass();
                                        newPossibleSequences.add(newSequence);
                                        newPossiblePatternsMasses.add(newMass);
                                    }
                                }
                            }

                            possibleSequences.clear();
                            possibleSequencesMasses.clear();
                            possibleSequences = newPossibleSequences;
                            possibleSequencesMasses = newPossiblePatternsMasses;
                        }

                        double fixedCTermModifications = 0;
                        ArrayList<ModificationMatch> cTermModifications = new ArrayList<ModificationMatch>();

                        for (String modificationName : fixedModifications) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                            if (ptm.getType() == PTM.MODCP) {
                                fixedCTermModifications = ptm.getMass();
                                cTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                break;
                            }
                        }

                        if (aaIndex == sequenceLastIndex) {
                            for (String modificationName : fixedModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                if (ptm.getType() == PTM.MODC) {
                                    fixedCTermModifications = ptm.getMass();
                                    cTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                    break;
                                }
                            }
                        }

                        Iterator<AminoAcidSequence> possibleSequencesIterator = possibleSequences.iterator();
                        Iterator<Double> possibleSequencesMassesIterator = possibleSequencesMasses.iterator();

                        while (possibleSequencesIterator.hasNext()) {

                            AminoAcidSequence aminoAcidSequence = possibleSequencesIterator.next();
                            double mass = possibleSequencesMassesIterator.next();
                            int lastAminoAcidIndex = aminoAcidSequence.length() - 1;

                            for (String modificationName : fixedModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                AminoAcidPattern ptmPattern = ptm.getPattern();
                                if (ptm.getType() == PTM.MODCPAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(lastAminoAcidIndex), ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                    fixedCTermModifications = ptm.getMass();
                                    cTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                    break;
                                }
                            }

                            if (aaIndex == lastAminoAcidIndex) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    AminoAcidPattern ptmPattern = ptm.getPattern();
                                    if (ptm.getType() == PTM.MODCAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(lastAminoAcidIndex), ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                        fixedCTermModifications = ptm.getMass();
                                        cTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                        break;
                                    }
                                }
                            }

                            if (mass + fixedCTermModifications + minMod > massGap + massTolerance) {
                                possibleSequencesIterator.remove();
                                possibleSequencesMassesIterator.remove();
                            } else {
                                int modIndex = aminoAcidSequence.length();
                                if (Math.abs(mass + fixedCTermModifications - massGap) <= massTolerance) {
                                    validSequences.add(aminoAcidSequence);
                                    if (reportFixedPtms) {
                                        for (ModificationMatch modificationMatch : cTermModifications) {
                                            modificationMatch.setModificationSite(modIndex);
                                            aminoAcidSequence.addModificationMatch(modIndex, modificationMatch);
                                        }
                                    }
                                    possibleSequencesIterator.remove();
                                    possibleSequencesMassesIterator.remove();
                                } else {
                                    boolean found = false;
                                    if (aaIndex == lastAminoAcidIndex) {
                                        for (String modificationName : variableModifications) {
                                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                            AminoAcidPattern ptmPattern = ptm.getPattern();
                                            if (ptm.getType() == PTM.MODC || ptm.getType() == PTM.MODCAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(lastAminoAcidIndex), ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                                if (Math.abs(mass + fixedCTermModifications + ptm.getMass() - massGap) <= massTolerance) {
                                                    aminoAcidSequence.addModificationMatch(lastAminoAcidIndex, new ModificationMatch(modificationName, true, modIndex));
                                                    validSequences.add(aminoAcidSequence);
                                                    if (reportFixedPtms) {
                                                        for (ModificationMatch modificationMatch : cTermModifications) {
                                                            modificationMatch.setModificationSite(modIndex);
                                                            aminoAcidSequence.addModificationMatch(modIndex, modificationMatch);
                                                        }
                                                    }
                                                    possibleSequencesIterator.remove();
                                                    possibleSequencesMassesIterator.remove();
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (!found) {
                                        for (String modificationName : variableModifications) {
                                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                            AminoAcidPattern ptmPattern = ptm.getPattern();
                                            if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODCPAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(lastAminoAcidIndex), ptmPattern.getTarget(), sequenceMatchingPreferences)) {
                                                if (Math.abs(mass + fixedCTermModifications + ptm.getMass() - massGap) <= massTolerance) {
                                                    aminoAcidSequence.addModificationMatch(lastAminoAcidIndex, new ModificationMatch(modificationName, true, modIndex));
                                                    if (reportFixedPtms) {
                                                        for (ModificationMatch modificationMatch : cTermModifications) {
                                                            modificationMatch.setModificationSite(modIndex);
                                                            aminoAcidSequence.addModificationMatch(modIndex, modificationMatch);
                                                        }
                                                    }
                                                    validSequences.add(aminoAcidSequence);
                                                    possibleSequencesIterator.remove();
                                                    possibleSequencesMassesIterator.remove();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (possibleSequencesMasses.isEmpty()) {
                            break;
                        }
                    }
                    for (AminoAcidSequence validSequence : validSequences) {
                        AminoAcidSequence newSequence = new AminoAcidSequence(cTermSequence);
                        newSequence.append(validSequence);
                        int newIndex = currentIndex + validSequence.length();
                        newIndexes.add(newIndex);
                        newSequences.add(validSequence);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tag component " + tagComponent.getClass() + " not implemented for sequence mating.");
            }
            if (newIndexes.isEmpty()) {
                return result;
            } else {
                cTermPossibleIndexes.clear();
                cTermPossibleSequences.clear();
                cTermPossibleIndexes = newIndexes;
                cTermPossibleSequences = newSequences;
            }
        }

        // create all possible peptide sequences by adding all possible N and C term to the seed sequence
        String seedSequence = sequence.substring(tagIndex, tagIndex + componentAtIndexLength);
        Iterator<Integer> nTermPossibleIndexesIterator = nTermPossibleIndexes.iterator();

        for (AminoAcidSequence nTermPattern : nTermPossibleSequences) {

            int nTermIndex = nTermPossibleIndexesIterator.next();
            StringBuilder nTermSequence = new StringBuilder(nTermPattern.length() + seedSequence.length());
            nTermSequence.append(nTermPattern.getSequence());
            nTermSequence.append(seedSequence);

            for (AminoAcidSequence cTermPattern : cTermPossibleSequences) {

                StringBuilder peptideSequence = new StringBuilder(nTermPattern.length() + seedSequence.length() + cTermPattern.length());
                peptideSequence.append(nTermSequence);
                ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>();
                HashMap<Integer, ArrayList<ModificationMatch>> nTermModifications = nTermPattern.getModificationMatches();

                if (nTermModifications != null) {
                    for (int i : nTermModifications.keySet()) {
                        for (ModificationMatch modificationMatch : nTermModifications.get(i)) {
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), i));
                        }
                    }
                }

                if (modificationsAtIndex != null) {
                    for (int i : modificationsAtIndex.keySet()) {
                        for (ModificationMatch modificationMatch : modificationsAtIndex.get(i)) {
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), nTermPattern.length() + i));
                        }
                    }
                }

                peptideSequence.append(cTermPattern.getSequence());
                HashMap<Integer, ArrayList<ModificationMatch>> cTermModifications = cTermPattern.getModificationMatches();

                if (cTermModifications != null) {
                    for (int i : cTermModifications.keySet()) {
                        for (ModificationMatch modificationMatch : cTermModifications.get(i)) {
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), nTermPattern.length() + componentAtIndexLength + i));
                        }
                    }
                }

                Peptide peptide = new Peptide(peptideSequence.toString(), modificationMatches);
                ArrayList<Peptide> peptides = result.get(nTermIndex);

                if (peptides == null) {
                    peptides = new ArrayList<Peptide>();
                    result.put(nTermIndex, peptides);
                }
                peptides.add(peptide);
            }
        }

        return result;
    }
}
