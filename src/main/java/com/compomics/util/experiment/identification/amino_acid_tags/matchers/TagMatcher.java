package com.compomics.util.experiment.identification.amino_acid_tags.matchers;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.amino_acid_tags.SequenceSegment;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This class matches tags to peptides.
 *
 * @author Marc Vaudel
 */
public class TagMatcher {

    /**
     * Mass of the fixed peptide N-term modifications.
     */
    private double fixedNTermPeptideModificationsMass = 0;
    /**
     * mass of the fixed peptide C-term modifications.
     */
    private double fixedCTermPeptideModificationsMass = 0;
    /**
     * Mass of the fixed protein N-term modifications.
     */
    private double fixedNTermProteinModificationsMass = 0;
    /**
     * Mass of the fixed protein C-term modifications.
     */
    private double fixedCTermProteinModificationsMass = 0;
    /**
     * Map of the masses of the fixed modifications at specific amino acids:
     * targeted amino acid &gt; list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsMasses = new HashMap<>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids on
     * peptide N-terminus: targeted amino acid &gt; list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsPeptideNtermMasses = new HashMap<>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids on
     * protein N-terminus: targeted amino acid &gt; list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsProteinNtermMasses = new HashMap<>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids on
     * peptide C-terminus: targeted amino acid &gt; list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsPeptideCtermMasses = new HashMap<>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids on
     * protein C-terminus: targeted amino acid &gt; list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsProteinCtermMasses = new HashMap<>(1);
    /**
     * Map of variable N-terminal peptide modifications: modification name &gt;
     * mass.
     */
    private HashMap<String, Double> variableNTermPeptideModifications = null;
    /**
     * Map of variable C-terminal peptide modifications: modification name &gt;
     * mass.
     */
    private HashMap<String, Double> variableCTermPeptideModifications = null;
    /**
     * Map of variable N-terminal protein modifications: modification name &gt;
     * mass.
     */
    private HashMap<String, Double> variableNTermProteinModifications = null;
    /**
     * Map of variable C-terminal protein modifications: modification name &gt;
     * mass.
     */
    private HashMap<String, Double> variableCTermProteinModifications = null;
    /**
     * Map of the variable modifications at specific amino acid: possible target
     * &gt; list of modifications.
     */
    private HashMap<Character, HashMap<String, Double>> variableAaModifications = new HashMap<>(1);
    /**
     * Map of the variable modifications at specific amino acid on peptide
     * N-terminus: possible target &gt; list of modifications.
     */
    private HashMap<Character, HashMap<String, Double>> variableAaModificationsAtPeptideNterm = new HashMap<>(1);
    /**
     * Map of the variable modifications at specific amino acid on protein
     * N-terminus: possible target &gt; list of modifications.
     */
    private HashMap<Character, HashMap<String, Double>> variableAaModificationsAtProteinNterm = new HashMap<>(1);
    /**
     * Map of the variable modifications at specific amino acid on peptide
     * C-terminus: possible target &gt; list of modifications.
     */
    private HashMap<Character, HashMap<String, Double>> variableAaModificationsAtPeptideCterm = new HashMap<>(1);
    /**
     * Map of the variable modifications at specific amino acid on protein
     * C-terminus: possible target &gt; list of modifications.
     */
    private HashMap<Character, HashMap<String, Double>> variableAaModificationsAtProteinCterm = new HashMap<>(1);
    /**
     * Smallest variable modification mass to account for when sequencing to the
     * N-terminus.
     */
    private double minNtermMod = 0;
    /**
     * Smallest variable modification mass to account for when sequencing to the
     * C-terminus.
     */
    private double minCtermMod = 0;
    /**
     * Biggest variable modification mass to account for when sequencing to the
     * N-terminus.
     */
    private double maxNtermMod = 0;
    /**
     * Biggest variable modification mass to account for when sequencing to the
     * C-terminus.
     */
    private double maxCtermMod = 0;
    /**
     * If true the possible sequence segments will be stored in caches.
     */
    private boolean useCache = true;
    /**
     * If true the indexing of the sequence will be executed in a synchronized
     * method. Use this in case different threads might attempt to sequence the
     * same sequence at the same index at the same time.
     */
    private boolean synchronizedIndexing = false;
    /**
     * The sequence segments cache for N-term sequencing
     *
     * Protein accession &gt; Starting index on protein &gt; end index on
     * protein &gt; Mass &gt; sequence segment.
     */
    private HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>>> nTermCache = new HashMap<>();
    /**
     * The sequence segments cache for C-term sequencing.
     *
     * Protein accession &gt; Starting index on protein &gt; end index on
     * protein &gt; Mass &gt; sequence segment.
     */
    private HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>>> cTermCache = new HashMap<>();
    /**
     * The sequence matching preferences
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences;

    private BufferedWriter debugbw = null;

    /**
     * Constructor.
     *
     * @param fixedModifications list of fixed modifications
     * @param variableModifications list of variable modifications
     * @param sequenceMatchingPreferences the sequence matching preferences
     */
    public TagMatcher(ArrayList<String> fixedModifications, ArrayList<String> variableModifications, SequenceMatchingPreferences sequenceMatchingPreferences) {
        this.sequenceMatchingPreferences = sequenceMatchingPreferences;
        importModificationMapping(fixedModifications, variableModifications);
    }

    /**
     * Imports the modifications in the attribute maps.
     *
     * @param fixedModifications list of fixed modifications
     * @param variableModifications list of variable modifications
     */
    private void importModificationMapping(ArrayList<String> fixedModifications, ArrayList<String> variableModifications) {
        // Sort modifications according to potential targets
        for (String modificationName : fixedModifications) {
            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
            if (ptm.getType() == PTM.MODN) {
                fixedNTermProteinModificationsMass += ptm.getMass();
            } else if (ptm.getType() == PTM.MODC) {
                fixedCTermProteinModificationsMass += ptm.getMass();
            } else if (ptm.getType() == PTM.MODNP) {
                fixedNTermPeptideModificationsMass += ptm.getMass();
            } else if (ptm.getType() == PTM.MODCP) {
                fixedCTermPeptideModificationsMass += ptm.getMass();
            } else if (ptm.getType() == PTM.MODAA) {
                AminoAcidPattern ptmPattern = ptm.getPattern();
                if (ptmPattern.length() > 1) {
                    throw new UnsupportedOperationException("Fixed modifications on patterns is not supported, try variable.");
                }
                for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                    Double fixedMass = fixedAaModificationsMasses.get(aa);
                    if (fixedMass == null) {
                        fixedMass = 0.0;
                    }
                    fixedAaModificationsMasses.put(aa, fixedMass + ptm.getMass());
                }
            } else if (ptm.getType() == PTM.MODNAA) {
                AminoAcidPattern ptmPattern = ptm.getPattern();
                if (ptmPattern.length() > 1) {
                    throw new UnsupportedOperationException("Fixed modifications on patterns is not supported, try variable.");
                }
                for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                    Double fixedMass = fixedAaModificationsProteinNtermMasses.get(aa);
                    if (fixedMass == null) {
                        fixedMass = 0.0;
                    }
                    fixedAaModificationsProteinNtermMasses.put(aa, fixedMass + ptm.getMass());
                }
            } else if (ptm.getType() == PTM.MODNPAA) {
                AminoAcidPattern ptmPattern = ptm.getPattern();
                if (ptmPattern.length() > 1) {
                    throw new UnsupportedOperationException("Fixed modifications on patterns is not supported, try variable.");
                }
                for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                    Double fixedMass = fixedAaModificationsPeptideNtermMasses.get(aa);
                    if (fixedMass == null) {
                        fixedMass = 0.0;
                    }
                    fixedAaModificationsPeptideNtermMasses.put(aa, fixedMass + ptm.getMass());
                }
            } else if (ptm.getType() == PTM.MODCAA) {
                AminoAcidPattern ptmPattern = ptm.getPattern();
                if (ptmPattern.length() > 1) {
                    throw new UnsupportedOperationException("Fixed modifications on patterns is not supported, try variable.");
                }
                for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                    Double fixedMass = fixedAaModificationsProteinCtermMasses.get(aa);
                    if (fixedMass == null) {
                        fixedMass = 0.0;
                    }
                    fixedAaModificationsProteinCtermMasses.put(aa, fixedMass + ptm.getMass());
                }
            } else if (ptm.getType() == PTM.MODCPAA) {
                AminoAcidPattern ptmPattern = ptm.getPattern();
                if (ptmPattern.length() > 1) {
                    throw new UnsupportedOperationException("Fixed modifications on patterns is not supported, try variable.");
                }
                for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                    Double fixedMass = fixedAaModificationsPeptideCtermMasses.get(aa);
                    if (fixedMass == null) {
                        fixedMass = 0.0;
                    }
                    fixedAaModificationsPeptideCtermMasses.put(aa, fixedMass + ptm.getMass());
                }
            }
        }

        for (String modificationName : variableModifications) {
            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
            if (ptm.getType() == PTM.MODNP) {
                if (variableNTermPeptideModifications == null) {
                    variableNTermPeptideModifications = new HashMap<>(1);
                }
                variableNTermPeptideModifications.put(modificationName, ptm.getMass());
                if (ptm.getMass() < minNtermMod) {
                    minNtermMod = ptm.getMass();
                }
                if (ptm.getMass() > maxNtermMod) {
                    maxNtermMod = ptm.getMass();
                }
            } else if (ptm.getType() == PTM.MODCP) {
                if (variableCTermPeptideModifications == null) {
                    variableCTermPeptideModifications = new HashMap<>(1);
                }
                variableCTermPeptideModifications.put(modificationName, ptm.getMass());
                if (ptm.getMass() < minCtermMod) {
                    minCtermMod = ptm.getMass();
                }
                if (ptm.getMass() > maxCtermMod) {
                    maxCtermMod = ptm.getMass();
                }
            } else if (ptm.getType() == PTM.MODN) {
                if (variableNTermProteinModifications == null) {
                    variableNTermProteinModifications = new HashMap<>(1);
                }
                variableNTermProteinModifications.put(modificationName, ptm.getMass());
            } else if (ptm.getType() == PTM.MODC) {
                if (variableCTermProteinModifications == null) {
                    variableCTermProteinModifications = new HashMap<>(1);
                }
                variableCTermProteinModifications.put(modificationName, ptm.getMass());
            } else if (ptm.getType() == PTM.MODAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    HashMap<String, Double> ptmMap = variableAaModifications.get(aa);
                    if (ptmMap == null) {
                        ptmMap = new HashMap<>(1);
                        variableAaModifications.put(aa, ptmMap);
                    }
                    ptmMap.put(modificationName, ptm.getMass());
                }
            } else if (ptm.getType() == PTM.MODNAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    HashMap<String, Double> ptmMap = variableAaModificationsAtProteinNterm.get(aa);
                    if (ptmMap == null) {
                        ptmMap = new HashMap<>(1);
                        variableAaModificationsAtProteinNterm.put(aa, ptmMap);
                    }
                    ptmMap.put(modificationName, ptm.getMass());
                }
            } else if (ptm.getType() == PTM.MODNPAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    HashMap<String, Double> ptmMap = variableAaModificationsAtPeptideNterm.get(aa);
                    if (ptmMap == null) {
                        ptmMap = new HashMap<>(1);
                        variableAaModificationsAtPeptideNterm.put(aa, ptmMap);
                    }
                    ptmMap.put(modificationName, ptm.getMass());
                }
                if (ptm.getMass() < minNtermMod) {
                    minNtermMod = ptm.getMass();
                }
                if (ptm.getMass() > maxNtermMod) {
                    maxNtermMod = ptm.getMass();
                }
            } else if (ptm.getType() == PTM.MODCAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    HashMap<String, Double> ptmMap = variableAaModificationsAtProteinNterm.get(aa);
                    if (ptmMap == null) {
                        ptmMap = new HashMap<>(1);
                        variableAaModificationsAtProteinNterm.put(aa, ptmMap);
                    }
                    ptmMap.put(modificationName, ptm.getMass());
                }
            } else if (ptm.getType() == PTM.MODCPAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    HashMap<String, Double> ptmMap = variableAaModificationsAtPeptideNterm.get(aa);
                    if (ptmMap == null) {
                        ptmMap = new HashMap<>(1);
                        variableAaModificationsAtPeptideNterm.put(aa, ptmMap);
                    }
                    ptmMap.put(modificationName, ptm.getMass());
                }
                if (ptm.getMass() < minCtermMod) {
                    minCtermMod = ptm.getMass();
                }
                if (ptm.getMass() > maxCtermMod) {
                    maxCtermMod = ptm.getMass();
                }
            }
        }
    }

    /**
     * Returns the possible peptides which can be created on this sequence
     * indexed by their start index. Null if not found. Note: PTMs must be in
     * the PTM factory. PTMs are considered at a target amino acid only, longer
     * patterns are not taken into account.
     *
     * @param tag the tag to look for
     * @param accession the accession of the protein
     * @param sequence the sequence where to look for the tag
     * @param tagIndex the index where the tag is located
     * @param componentIndex the index of the component of the tag indexed by
     * tagIndex in the content list
     * @param massTolerance the ms2 tolerance
     *
     * @return the possible peptides which can be created on this sequence
     * indexed by their start index
     */
    public ArrayList<PeptideProteinMapping> getPeptideMatches(Tag tag, String accession, String sequence, Integer tagIndex,
            Integer componentIndex, double massTolerance) {

        ArrayList<TagComponent> content = tag.getContent();

        // Get information about the reference sequence
        TagComponent componentAtIndex = content.get(componentIndex);
        int componentAtIndexLength;
        HashMap<Integer, ArrayList<ModificationMatch>> modificationsAtIndex = null;

        if (componentAtIndex instanceof AminoAcidPattern) {

            AminoAcidPattern tagPattern = (AminoAcidPattern) componentAtIndex;
            componentAtIndexLength = tagPattern.length();
            modificationsAtIndex = tagPattern.getModificationMatches();

        } else if (componentAtIndex instanceof AminoAcidSequence) {

            AminoAcidSequence tagSequence = (AminoAcidSequence) componentAtIndex;
            componentAtIndexLength = tagSequence.length();
            modificationsAtIndex = tagSequence.getModificationMatches();

        } else {
            throw new UnsupportedOperationException("Tag mapping not supported for tag component " + componentAtIndex.getClass() + ".");
        }

        String seedSequence = sequence.substring(tagIndex, tagIndex + componentAtIndexLength);

        // Check tag components to the N-term
        ArrayList<SequenceSegment> nTermPossibleSequences = new ArrayList<>(1);
        nTermPossibleSequences.add(new SequenceSegment(tagIndex, true));

        for (int i = componentIndex - 1; i >= 0; i--) {

            TagComponent tagComponent = content.get(i);

            nTermPossibleSequences = mapTagComponent(accession, sequence, tagComponent, nTermPossibleSequences, massTolerance, useCache && i == componentIndex - 1, true, i == 0);

            if (nTermPossibleSequences.isEmpty()) {
                return new ArrayList<>(0);
            }
        }

        // Check tag components to the C-term
        ArrayList<SequenceSegment> cTermPossibleSequences = new ArrayList<>(1);
        cTermPossibleSequences.add(new SequenceSegment(tagIndex + componentAtIndexLength - 1, false));

        for (int i = componentIndex + 1; i < content.size(); i++) {

            TagComponent tagComponent = content.get(i);

            cTermPossibleSequences = mapTagComponent(accession, sequence, tagComponent, cTermPossibleSequences, massTolerance, useCache && i == componentIndex + 1, false, i == content.size() - 1);

            if (cTermPossibleSequences.isEmpty()) {
                return new ArrayList<>(0);
            }

        }

        // create all possible peptide sequences by adding all possible N and C term to the seed sequence
        ArrayList<PeptideProteinMapping> result = buildPeptides(accession, sequence, nTermPossibleSequences, seedSequence, cTermPossibleSequences, modificationsAtIndex, 0);

        return result;
    }

    /**
     * Builds the possible peptides based on the given terminal segments and the
     * seed sequence.
     *
     * @param sequence the protein sequence
     * @param accession the protein accession
     * @param nTermPossibleSequences the N-terminal possible segments
     * @param seedSequence the seed sequence
     * @param cTermPossibleSequences the C-terminal possible segments
     * @param modificationsAtIndex the seed modifications
     * @param mutationsAtIndex the seeds mutations
     *
     * @return the possible peptides in a map: index on protein &gt; list of
     * peptides
     */
    public ArrayList<PeptideProteinMapping> buildPeptides(String accession, String sequence, ArrayList<SequenceSegment> nTermPossibleSequences, String seedSequence, ArrayList<SequenceSegment> cTermPossibleSequences, HashMap<Integer, ArrayList<ModificationMatch>> modificationsAtIndex, int mutationsAtIndex) {

        ArrayList<PeptideProteinMapping> result = new ArrayList<>(nTermPossibleSequences.size() * cTermPossibleSequences.size());

        for (SequenceSegment nTermSegment : nTermPossibleSequences) {

            StringBuilder nTermSequence = new StringBuilder(nTermSegment.length() + seedSequence.length());
            nTermSequence.append(nTermSegment.getSegmentSequence(sequence));
            nTermSequence.append(seedSequence);

            for (SequenceSegment cTermSegment : cTermPossibleSequences) {

                StringBuilder peptideSequence = new StringBuilder(nTermSegment.length() + seedSequence.length() + cTermSegment.length());
                peptideSequence.append(nTermSequence);
                ArrayList<ModificationMatch> modificationMatches = new ArrayList<>(1);
                HashMap<Integer, String> nTermModifications = nTermSegment.getModificationMatches();

                if (nTermModifications != null) {
                    for (Integer site : nTermModifications.keySet()) {
                        String ptmName = nTermModifications.get(site);
                        int remappedSite = nTermSegment.length() + 1 - site;
                        modificationMatches.add(new ModificationMatch(ptmName, true, remappedSite));
                    }
                }

                if (modificationsAtIndex != null) {
                    for (Integer i : modificationsAtIndex.keySet()) {
                        for (ModificationMatch modificationMatch : modificationsAtIndex.get(i)) {
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.getVariable(), nTermSegment.length() + i));
                        }
                    }
                }

                peptideSequence.append(cTermSegment.getSegmentSequence(sequence));
                HashMap<Integer, String> cTermModifications = cTermSegment.getModificationMatches();

                if (cTermModifications != null) {
                    for (Integer site : cTermModifications.keySet()) {
                        String ptmName = cTermModifications.get(site);
                        int remappedSite = nTermSegment.length() + seedSequence.length() + site;
                        modificationMatches.add(new ModificationMatch(ptmName, true, remappedSite));
                    }
                }

                Integer nTermIndex = nTermSegment.getTerminalIndex() + 1;
                PeptideProteinMapping peptideProteinMapping = new PeptideProteinMapping(accession, peptideSequence.toString(), nTermIndex, modificationMatches);
                result.add(peptideProteinMapping);
            }
        }
        return result;
    }

    /**
     * Maps a tag component on the protein sequence and returns the
     * corresponding possible sequence segments.
     *
     * @param accession the accession of the protein
     * @param sequence the protein sequence
     * @param tagComponent the tag component to map
     * @param terminalPreviousSequences the possible previous terminal sequences
     * @param reportFixedPtms if true the fixed PTMs will be reported as
     * ModificationMatch
     * @param massTolerance the ms2 mass tolerance to use
     * @param nTerminus if true the sequencing will go toward the N-terminus, to
     * the C-terminus otherwise
     *
     * @return the possible sequence fragment of this tag component appended to
     * the given previous segments
     */
    private ArrayList<SequenceSegment> mapTagComponent(String accession, String sequence, TagComponent tagComponent, ArrayList<SequenceSegment> terminalPreviousSequences, double massTolerance, boolean useCache, boolean nTerminus, boolean lastComponent) {

        if (tagComponent instanceof AminoAcidPattern) {

            for (SequenceSegment terminalSequence : terminalPreviousSequences) {
                Integer aaIndex = terminalSequence.getTerminalIndex();
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;

                String subSequence = null;

                if (nTerminus) {

                    Integer startIndex = aaIndex - aminoAcidPattern.length();

                    if (startIndex >= 0) {
                        subSequence = sequence.substring(startIndex, aaIndex);
                    }
                } else {

                    Integer endIndex = aaIndex + aminoAcidPattern.length();

                    if (endIndex <= sequence.length() - 1) {
                        subSequence = sequence.substring(aaIndex, endIndex);
                    }
                }
                if (subSequence != null && aminoAcidPattern.matches(subSequence, sequenceMatchingPreferences)) {

                    terminalSequence.appendTerminus((SequenceSegment) tagComponent);

                }

            }

            return terminalPreviousSequences;

        } else if (tagComponent instanceof AminoAcidSequence) {

            for (SequenceSegment terminalSequence : terminalPreviousSequences) {

                Integer aaIndex = terminalSequence.getTerminalIndex();
                AminoAcidSequence aminoAcidPattern = (AminoAcidSequence) tagComponent;

                String subSequence = null;

                if (nTerminus) {

                    Integer startIndex = aaIndex - aminoAcidPattern.length();

                    if (startIndex >= 0) {
                        subSequence = sequence.substring(startIndex, aaIndex);
                    }
                } else {

                    Integer endIndex = aaIndex + aminoAcidPattern.length();

                    if (endIndex <= sequence.length() - 1) {
                        subSequence = sequence.substring(aaIndex, endIndex);
                    }
                }
                if (subSequence != null && aminoAcidPattern.matches(subSequence, sequenceMatchingPreferences)) {

                    terminalSequence.appendTerminus((SequenceSegment) tagComponent);

                }
            }

            return terminalPreviousSequences;

        } else if (tagComponent instanceof MassGap) {

            double massGap = tagComponent.getMass();

            ArrayList<SequenceSegment> newSequences = new ArrayList<>(1);

            for (int i = 0; i < terminalPreviousSequences.size(); i++) {

                SequenceSegment terminalSequence = terminalPreviousSequences.get(i);
                int aaIndex = terminalSequence.getTerminalIndex();
                Integer currentIndex = aaIndex;
                ArrayList<SequenceSegment> possibleSequences = null;
                ArrayList<SequenceSegment> validSequences = new ArrayList<>(1);

                HashMap<Integer, ArrayList<SequenceSegment>> indexCache = getIndexCache(accession, currentIndex, nTerminus);

                if (nTerminus) {
                    aaIndex--;
                } else {
                    aaIndex++;
                }
                while (aaIndex >= 0 && aaIndex < sequence.length()) {

                    char sequenceAa = sequence.charAt(aaIndex);
                    AminoAcid sequenceAminoAcid = AminoAcid.getAminoAcid(sequenceAa);
                    int segmentLength = Math.abs(aaIndex - currentIndex);
                    if (useCache && segmentLength <= 12) {
                        possibleSequences = indexCache.get(aaIndex);
                        if (possibleSequences == null) {
                            if (synchronizedIndexing) {
                                possibleSequences = addSequenceSegmentsToCacheSynchronized(indexCache, sequence, sequenceAminoAcid, currentIndex, aaIndex, nTerminus);
                            } else {
                                possibleSequences = addSequenceSegmentsToCache(indexCache, sequence, sequenceAminoAcid, currentIndex, aaIndex, nTerminus);
                            }
                        }
                    } else {
                        possibleSequences = getCombinationsForAminoAcid(sequence, possibleSequences, sequenceAminoAcid, currentIndex, aaIndex, nTerminus);
                    }

                    if (validateSegments(possibleSequences, validSequences, massGap, massTolerance, sequence, sequenceAa, nTerminus)) {
                        if (debugbw != null) {
                            try {
                                debugbw.write(segmentLength + "\n");
                                debugbw.flush();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        break;
                    }
                    if (nTerminus) {
                        aaIndex--;
                    } else {
                        aaIndex++;
                    }
                }
                if (!lastComponent) {
                    for (SequenceSegment validSegment : validSequences) {
                        SequenceSegment sequenceSegment = new SequenceSegment(validSegment);
                        sequenceSegment.appendTerminus(terminalSequence);
                        newSequences.add(sequenceSegment);
                    }
                } else {
                    newSequences.addAll(validSequences);
                }
            }

            return newSequences;

        } else {
            throw new IllegalArgumentException("Tag component " + tagComponent.getClass() + " not implemented for sequence matching.");
        }
    }

    /**
     * Returns the index cache for the given segment seed, accession and index.
     *
     * @param accession the accession of the protein
     * @param currentIndex the index on the protein
     * @param nTerminus boolean indicating whether the N or C terminus cache
     * should be used
     *
     * @return the index cache
     */
    public HashMap<Integer, ArrayList<SequenceSegment>> getIndexCache(String accession, Integer currentIndex, boolean nTerminus) {

        HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> proteinCache;
        if (nTerminus) {
            proteinCache = nTermCache.get(accession);
        } else {
            proteinCache = cTermCache.get(accession);
        }
        if (proteinCache == null) {
            if (synchronizedIndexing) {
                proteinCache = addProteinCacheSynchronized(accession, currentIndex, nTerminus);
            } else {
                proteinCache = addProteinCache(accession, currentIndex, nTerminus);
            }
        }
        HashMap<Integer, ArrayList<SequenceSegment>> indexCache = proteinCache.get(currentIndex);
        if (indexCache == null) {
            if (synchronizedIndexing) {
                indexCache = addIndexCacheSynchronized(proteinCache, currentIndex);
            } else {
                indexCache = addIndexCache(proteinCache, currentIndex);
            }
        }
        return indexCache;
    }

    /**
     * Adds a cache for the given segment seed and returns it.
     *
     * @param seed the segment seed
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return a cache for the given protein
     */
    private synchronized HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> addProteinCacheSynchronized(String accession, Integer currentIndex, boolean nTerminus) {
        HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> proteinCache;
        if (nTerminus) {
            proteinCache = nTermCache.get(accession);
        } else {
            proteinCache = cTermCache.get(accession);
        }
        if (proteinCache == null) {
            proteinCache = addProteinCache(accession, currentIndex, nTerminus);
        }
        return proteinCache;
    }

    /**
     * Adds a cache for the given segment seed and returns it.
     *
     * @param seed the segment seed
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return a cache for the given protein
     */
    private HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> addProteinCache(String accession, Integer currentIndex, boolean nTerminus) {
        HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> proteinCache = new HashMap<>();
        if (nTerminus) {
            nTermCache.put(accession, proteinCache);
        } else {
            cTermCache.put(accession, proteinCache);
        }
        HashMap<Integer, ArrayList<SequenceSegment>> indexCache = new HashMap<>(1);
        proteinCache.put(currentIndex, indexCache);
        return proteinCache;
    }

    /**
     * Adds a cache for the given index and returns it.
     *
     * @param proteinCache the protein cache
     * @param currentIndex the index of interest
     *
     * @return a cache for the given index
     */
    private HashMap<Integer, ArrayList<SequenceSegment>> addIndexCacheSynchronized(HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> proteinCache, Integer currentIndex) {
        HashMap<Integer, ArrayList<SequenceSegment>> indexCache = proteinCache.get(currentIndex);
        if (indexCache == null) {
            addIndexCache(proteinCache, currentIndex);
        }
        return indexCache;
    }

    /**
     * Adds a cache for the given index and returns it.
     *
     * @param proteinCache the protein cache
     * @param currentIndex the index of Integererest
     *
     * @return a cache for the given index
     */
    private HashMap<Integer, ArrayList<SequenceSegment>> addIndexCache(HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> proteinCache, Integer currentIndex) {
        HashMap<Integer, ArrayList<SequenceSegment>> indexCache = new HashMap<>(1);
        proteinCache.put(currentIndex, indexCache);
        return indexCache;
    }

    /**
     * Adds the possible new sequence segments generated when appending the
     * given amino acid to the given cache and returns the list of possible
     * segments.
     *
     * @param indexCache the cache for this index on the sequence
     * @param sequence the protein sequence
     * @param aminoAcid the amino acid object
     * @param currentIndex the current indexing level on the protein sequence
     * @param aaIndex the amino acid index
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return the new possible sequences
     */
    public synchronized ArrayList<SequenceSegment> addSequenceSegmentsToCacheSynchronized(HashMap<Integer, ArrayList<SequenceSegment>> indexCache, String sequence, AminoAcid aminoAcid, Integer currentIndex, Integer aaIndex, boolean nTerminus) {
        // check whether another thread already did the job
        ArrayList<SequenceSegment> result = indexCache.get(aaIndex);
        if (result == null) {
            result = addSequenceSegmentsToCache(indexCache, sequence, aminoAcid, currentIndex, aaIndex, nTerminus);
        }
        return result;
    }

    /**
     * Adds the possible new sequence segments generated when appending the
     * given amino acid to the given cache and returns the list of possible
     * segments.
     *
     * @param indexCache the cache for this index on the sequence
     * @param sequence the protein sequence
     * @param aminoAcid the amino acid object
     * @param currentIndex the current indexing level on the protein sequence
     * @param aaIndex the amino acid index
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return the new possible sequences
     */
    public ArrayList<SequenceSegment> addSequenceSegmentsToCache(HashMap<Integer, ArrayList<SequenceSegment>> indexCache, String sequence, AminoAcid aminoAcid, Integer currentIndex, Integer aaIndex, boolean nTerminus) {
        ArrayList<SequenceSegment> previousSequences;
        if (nTerminus) {
            previousSequences = indexCache.get(aaIndex + 1);
        } else {
            previousSequences = indexCache.get(aaIndex - 1);
        }
        ArrayList<SequenceSegment> result = getCombinationsForAminoAcid(sequence, previousSequences, aminoAcid, currentIndex, aaIndex, nTerminus);
        indexCache.put(aaIndex, result);
        return result;
    }

    /**
     * Adds the possible new sequence segments generated when appending the
     * given amino acid.
     *
     * @param sequence the protein sequence
     * @param possibleSequences the possible previous sequences
     * @param aminoAcid the amino acid object
     * @param currentIndex the current indexing level on the protein sequence
     * @param aaIndex the amino acid index
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return the new possible sequences
     */
    public ArrayList<SequenceSegment> getCombinationsForAminoAcid(String sequence, ArrayList<SequenceSegment> possibleSequences, AminoAcid aminoAcid, Integer currentIndex, Integer aaIndex, boolean nTerminus) {

        char aa = aminoAcid.getSingleLetterCodeAsChar();
        Double fixedMass = fixedAaModificationsMasses.get(aa);
        HashMap<String, Double> variableModificationsAtAa = variableAaModifications.get(aa);

        if (possibleSequences == null) {

            possibleSequences = new ArrayList<>(2);
            SequenceSegment sequenceSegment = new SequenceSegment(aaIndex, nTerminus);
            possibleSequences.add(sequenceSegment);
            sequenceSegment.appendTerminus(aminoAcid);

            double modificationMass = 0;
            if (fixedMass != null) {
                modificationMass += fixedMass;
            }
            if (nTerminus && aaIndex == 0) {
                modificationMass += fixedNTermProteinModificationsMass;
                if (!fixedAaModificationsProteinNtermMasses.isEmpty()) {
                    Double aaTerminalMass = fixedAaModificationsProteinNtermMasses.get(aa);
                    if (aaTerminalMass != null) {
                        modificationMass += aaTerminalMass;
                    }
                }
            } else if (!nTerminus && aaIndex == sequence.length() - 1) {
                modificationMass += fixedCTermProteinModificationsMass;
                if (!fixedAaModificationsProteinCtermMasses.isEmpty()) {
                    Double aaTerminalMass = fixedAaModificationsProteinCtermMasses.get(aa);
                    if (aaTerminalMass != null) {
                        modificationMass += aaTerminalMass;
                    }
                }
            }
            sequenceSegment.addMass(modificationMass);

            addVariableModifications(variableModificationsAtAa, sequenceSegment, possibleSequences);
            if (nTerminus && aaIndex == 0) {
                addVariableModifications(variableNTermProteinModifications, sequenceSegment, possibleSequences);
                if (!variableAaModificationsAtProteinNterm.isEmpty()) {
                    HashMap<String, Double> aaTerminalModifications = variableAaModificationsAtProteinNterm.get(aa);
                    if (aaTerminalModifications != null) {
                        addVariableModifications(aaTerminalModifications, sequenceSegment, possibleSequences);
                    }
                }
            } else if (!nTerminus && aaIndex == sequence.length() - 1) {
                addVariableModifications(variableCTermProteinModifications, sequenceSegment, possibleSequences);
                if (!variableAaModificationsAtProteinCterm.isEmpty()) {
                    HashMap<String, Double> aaTerminalModifications = variableAaModificationsAtProteinCterm.get(aa);
                    if (aaTerminalModifications != null) {
                        addVariableModifications(aaTerminalModifications, sequenceSegment, possibleSequences);
                    }
                }
            }

            return possibleSequences;

        } else {

            ArrayList<SequenceSegment> newPossibleSequences = new ArrayList<>(possibleSequences.size());

            for (int i = 0; i < possibleSequences.size(); i++) {
                SequenceSegment sequenceSegment = possibleSequences.get(i);

                SequenceSegment newSegment = new SequenceSegment(sequenceSegment);
                newPossibleSequences.add(newSegment);
                newSegment.appendTerminus(aminoAcid);

                double modificationMass = 0;
                if (fixedMass != null) {
                    modificationMass += fixedMass;
                }
                if (nTerminus && aaIndex == 0) {
                    modificationMass += fixedNTermProteinModificationsMass;
                } else if (!nTerminus && aaIndex == sequence.length() - 1) {
                    modificationMass += fixedCTermProteinModificationsMass;
                }
                newSegment.addMass(modificationMass);

                addVariableModifications(variableModificationsAtAa, newSegment, newPossibleSequences);
                if (nTerminus && aaIndex == 0) {
                    addVariableModifications(variableNTermProteinModifications, newSegment, newPossibleSequences);
                    if (!variableAaModificationsAtProteinNterm.isEmpty()) {
                        HashMap<String, Double> aaTerminalModifications = variableAaModificationsAtProteinNterm.get(aa);
                        if (aaTerminalModifications != null) {
                            addVariableModifications(aaTerminalModifications, newSegment, newPossibleSequences);
                        }
                    }
                } else if (!nTerminus && aaIndex == sequence.length() - 1) {
                    addVariableModifications(variableCTermProteinModifications, newSegment, newPossibleSequences);
                    if (!variableAaModificationsAtProteinCterm.isEmpty()) {
                        HashMap<String, Double> aaTerminalModifications = variableAaModificationsAtProteinCterm.get(aa);
                        if (aaTerminalModifications != null) {
                            addVariableModifications(aaTerminalModifications, newSegment, newPossibleSequences);
                        }
                    }
                }
            }

            return newPossibleSequences;
        }
    }

    /**
     * Removes the segments which cannot match the mass gap from the possible
     * sequences and transfers the valid segments to the valid sequences.
     * Returns a boolean indicating whether the sequence iteration should be
     * terminated.
     *
     * @param possibleSequences the possible sequences
     * @param validSequences the valid sequences
     * @param massGap the mass gap
     * @param massTolerance the mass tolerance to use
     * @param sequence the protein sequence
     * @param sequenceAa the amino acid at terminus on the protein sequence
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return if true no more sequence segment can be mapped
     */
    public boolean validateSegments(ArrayList<SequenceSegment> possibleSequences, ArrayList<SequenceSegment> validSequences, double massGap, double massTolerance, String sequence, char sequenceAa, boolean nTerminus) {

        boolean allInspected = true;

        for (int i = 0; i < possibleSequences.size(); i++) {
            SequenceSegment sequenceSegment = possibleSequences.get(i);
            double sequenceMass = sequenceSegment.getMass();

            if (nTerminus) {
                sequenceMass += fixedNTermPeptideModificationsMass;
                if (!fixedAaModificationsPeptideNtermMasses.isEmpty()) {
                    Double aaTerminalMass = fixedAaModificationsPeptideNtermMasses.get(sequenceAa);
                    if (aaTerminalMass != null) {
                        sequenceMass += aaTerminalMass;
                    }
                }
            } else {
                sequenceMass += fixedCTermPeptideModificationsMass;
                if (!fixedAaModificationsPeptideCtermMasses.isEmpty()) {
                    Double aaTerminalMass = fixedAaModificationsPeptideCtermMasses.get(sequenceAa);
                    if (aaTerminalMass != null) {
                        sequenceMass += aaTerminalMass;
                    }
                }
            }

            double terminalModificationMin;
            if (nTerminus) {
                terminalModificationMin = minNtermMod;
            } else {
                terminalModificationMin = minCtermMod;
            }
            double terminalModificationMax;
            if (nTerminus) {
                terminalModificationMax = maxNtermMod;
            } else {
                terminalModificationMax = maxCtermMod;
            }
            boolean found = false, overGap = true;
            if (sequenceMass + terminalModificationMin <= massGap + massTolerance) {
                overGap = false;
                if (sequenceMass + terminalModificationMax >= massGap - massTolerance) {
                    found = validateSegment(validSequences, sequenceSegment, sequenceMass, massGap, massTolerance, sequenceAa, nTerminus);
                }
            }
            if (!found && !overGap) {
                allInspected = false;
            }
        }
        return allInspected;
    }

    /**
     * Validates a sequence segment.
     *
     * @param validSequences the retained sequences
     * @param sequenceSegment the sequence segment to validate
     * @param sequenceMass the mass of the sequence without mutation
     * @param massGap the mass gap to fill
     * @param massTolerance the mass tolerance to use
     * @param sequenceAa the amino acid at index on the sequence
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return a boolean indicating the segment was validated
     */
    private boolean validateSegment(ArrayList<SequenceSegment> validSequences, SequenceSegment sequenceSegment, double sequenceMass, double massGap, double massTolerance, char sequenceAa, boolean nTerminus) {
        return validateSegment(validSequences, sequenceSegment, sequenceMass, massGap, massTolerance, sequenceAa, 0, 0, null, nTerminus);
    }

    /**
     * Validates a sequence segment.
     *
     * @param validSequences the retained sequences
     * @param sequenceSegment the sequence segment to validate
     * @param sequenceMass the mass of the sequence without mutation
     * @param massGap the mass gap to fill
     * @param massTolerance the mass tolerance to use
     * @param sequenceAa the amino acid at index on the sequence
     * @param mutatedIndex the index of the mutation on the sequence segment
     * @param deltaMutation the mass variation induced by the mutation
     * @param mutated the list of potential products of the mutation
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return a boolean indicating the segment was validated
     */
    private boolean validateSegment(ArrayList<SequenceSegment> validSequences, SequenceSegment sequenceSegment, double sequenceMass, double massGap, double massTolerance, char sequenceAa, int mutatedIndex, double deltaMutation, HashSet<Character> mutated, boolean nTerminus) {

        if (Math.abs(sequenceMass + deltaMutation - massGap) <= massTolerance) {
            if (mutated == null) {
                validSequences.add(sequenceSegment);
            } else {
                for (char aa : mutated) {
                    SequenceSegment mutatedSegment = new SequenceSegment(sequenceSegment);
                    mutatedSegment.addMutation(mutatedIndex, aa);
                    validSequences.add(mutatedSegment);
                }
            }
            return true;
        } else {
            if (nTerminus) {
                if (variableNTermPeptideModifications != null) {
                    for (String modificationName : variableNTermPeptideModifications.keySet()) {
                        double modifiedMass = sequenceMass + variableNTermPeptideModifications.get(modificationName);
                        if (Math.abs(modifiedMass + deltaMutation - massGap) <= massTolerance) {
                            if (mutated == null) {
                                SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                                modifiedSegment.addModificationTerminus(modificationName);
                                validSequences.add(modifiedSegment);
                            } else {
                                for (char aa : mutated) {
                                    SequenceSegment mutatedSegment = new SequenceSegment(sequenceSegment);
                                    mutatedSegment.addMutation(mutatedIndex, aa);
                                    mutatedSegment.addModificationTerminus(modificationName);
                                    validSequences.add(mutatedSegment);
                                }
                            }
                            return true;
                        }
                    }
                }
                if (!variableAaModificationsAtPeptideNterm.isEmpty()) {
                    HashMap<String, Double> variableTermPeptideModificationsAtAa = variableAaModificationsAtPeptideNterm.get(sequenceAa);
                    if (variableTermPeptideModificationsAtAa != null) {
                        for (String modificationName : variableTermPeptideModificationsAtAa.keySet()) {
                            double modifiedMass = sequenceMass + variableTermPeptideModificationsAtAa.get(modificationName);
                            if (Math.abs(modifiedMass + deltaMutation - massGap) <= massTolerance) {
                                if (mutated == null) {
                                    SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                                    modifiedSegment.addModificationTerminus(modificationName);
                                    validSequences.add(modifiedSegment);
                                } else {
                                    for (char aa : mutated) {
                                        SequenceSegment mutatedSegment = new SequenceSegment(sequenceSegment);
                                        mutatedSegment.addMutation(mutatedIndex, aa);
                                        mutatedSegment.addModificationTerminus(modificationName);
                                        validSequences.add(mutatedSegment);
                                    }
                                }
                                return true;
                            }
                        }
                    }
                }
            } else {
                if (variableCTermPeptideModifications != null) {
                    for (String modificationName : variableCTermPeptideModifications.keySet()) {
                        double modifiedMass = sequenceMass + variableCTermPeptideModifications.get(modificationName);
                        if (Math.abs(modifiedMass + deltaMutation - massGap) <= massTolerance) {
                            if (mutated == null) {
                                SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                                modifiedSegment.addModificationTerminus(modificationName);
                                validSequences.add(modifiedSegment);
                            } else {
                                for (char aa : mutated) {
                                    SequenceSegment mutatedSegment = new SequenceSegment(sequenceSegment);
                                    mutatedSegment.addMutation(mutatedIndex, aa);
                                    mutatedSegment.addModificationTerminus(modificationName);
                                    validSequences.add(mutatedSegment);
                                }
                            }
                            return true;
                        }
                    }
                }
                if (!variableAaModificationsAtPeptideCterm.isEmpty()) {
                    HashMap<String, Double> variableTermPeptideModificationsAtAa = variableAaModificationsAtPeptideCterm.get(sequenceAa);
                    if (variableTermPeptideModificationsAtAa != null) {
                        for (String modificationName : variableTermPeptideModificationsAtAa.keySet()) {
                            double modifiedMass = sequenceMass + variableTermPeptideModificationsAtAa.get(modificationName);
                            if (Math.abs(modifiedMass + deltaMutation - massGap) <= massTolerance) {
                                if (mutated == null) {
                                    SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                                    modifiedSegment.addModificationTerminus(modificationName);
                                    validSequences.add(modifiedSegment);
                                } else {
                                    for (char aa : mutated) {
                                        SequenceSegment mutatedSegment = new SequenceSegment(sequenceSegment);
                                        mutatedSegment.addMutation(mutatedIndex, aa);
                                        mutatedSegment.addModificationTerminus(modificationName);
                                        validSequences.add(mutatedSegment);
                                    }
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Adds the potential sequence segments obtained after adding the given
     * variable modifications on a segment terminus to the given list of
     * possible segments.
     *
     * @param variableModifications the variable modifications to add
     * @param noModSegment the sequence segment without modification
     * @param possibleSegments the possible segment where to add the modified
     * segments
     */
    public void addVariableModifications(HashMap<String, Double> variableModifications, SequenceSegment noModSegment, ArrayList<SequenceSegment> possibleSegments) {
        if (variableModifications != null) {
            for (String modificationName : variableModifications.keySet()) {
                SequenceSegment modifiedSegment = new SequenceSegment(noModSegment);
                Double ptmMass = variableModifications.get(modificationName);
                modifiedSegment.addModificationTerminus(modificationName, ptmMass);
                possibleSegments.add(modifiedSegment);
            }
        }
    }

    /**
     * Clears the cache.
     */
    public void clearCache() {
        nTermCache.clear();
        cTermCache.clear();
    }

    /**
     * Sets whether a cache should be used.
     *
     * @param useCache if true a cache will be used
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * Sets whether the indexing of the sequence should be executed in a
     * synchronized method. Use this in case different threads might attempt to
     * sequence the same sequence at the same index at the same time.
     *
     * @param synchronizedIndexing true if the indexing of the sequence should
     * be executed in a synchronized method
     */
    public void setSynchronizedIndexing(boolean synchronizedIndexing) {
        this.synchronizedIndexing = synchronizedIndexing;
    }

}
