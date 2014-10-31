package com.compomics.util.experiment.identification.tags.matchers;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.SequenceSegment;
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
     * Mass of the fixed peptide N-term modifications.
     */
    private double fixedNTermPeptideModificationsMass = 0;
    /**
     * List of the fixed peptide N-term modifications.
     */
    private ArrayList<String> fixedNTermPeptideModification = null;
    /**
     * mass of the fixed peptide C-term modifications.
     */
    private double fixedCTermPeptideModificationsMass = 0;
    /**
     * List of the fixed peptide C-term modifications.
     */
    private ArrayList<String> fixedCTermPeptideModification = null;
    /**
     * Mass of the fixed protein N-term modifications.
     */
    private double fixedNTermProteinModificationsMass = 0;
    /**
     * List of the fixed protein N-term modifications.
     */
    private ArrayList<String> fixedNTermProteinModification = null;
    /**
     * Mass of the fixed protein C-term modifications.
     */
    private double fixedCTermProteinModificationsMass = 0;
    /**
     * List of the fixed protein C-term modifications.
     */
    private ArrayList<String> fixedCTermProteinModification = null;
    /**
     * Map of the fixed modifications at specific amino acids: targeted amino
     * acid -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> fixedAaModifications = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the fixed modifications at specific amino acids on peptide
     * N-terminus: targeted amino acid -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> fixedAaModificationsPeptideNterm = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the fixed modifications at specific amino acids on protein
     * N-terminus: targeted amino acid -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> fixedAaModificationsProteinNterm = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the fixed modifications at specific amino acids on peptide
     * C-terminus: targeted amino acid -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> fixedAaModificationsPeptideCterm = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the fixed modifications at specific amino acids on protein
     * C-terminus: targeted amino acid -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> fixedAaModificationsProteinCterm = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids:
     * targeted amino acid -> list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsMasses = new HashMap<Character, Double>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids on
     * peptide N-terminus: targeted amino acid -> list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsPeptideNtermMasses = new HashMap<Character, Double>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids on
     * protein N-terminus: targeted amino acid -> list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsProteinNtermMasses = new HashMap<Character, Double>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids on
     * peptide C-terminus: targeted amino acid -> list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsPeptideCtermMasses = new HashMap<Character, Double>(1);
    /**
     * Map of the masses of the fixed modifications at specific amino acids on
     * protein C-terminus: targeted amino acid -> list of modifications.
     */
    private HashMap<Character, Double> fixedAaModificationsProteinCtermMasses = new HashMap<Character, Double>(1);
    /**
     * List of variable N-terminal peptide modifications.
     */
    private ArrayList<String> variableNTermPeptideModifications = null;
    /**
     * List of variable C-terminal peptide modifications.
     */
    private ArrayList<String> variableCTermPeptideModifications = null;
    /**
     * List of variable N-terminal protein modifications.
     */
    private ArrayList<String> variableNTermProteinModifications = null;
    /**
     * List of variable C-terminal protein modifications.
     */
    private ArrayList<String> variableCTermProteinModifications = null;
    /**
     * Map of the variable modifications at specific amino acid: possible target
     * -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> variableAaModifications = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the variable modifications at specific amino acid on peptide
     * N-terminus: possible target -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> variableAaModificationsAtPeptideNterm = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the variable modifications at specific amino acid on protein
     * N-terminus: possible target -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> variableAaModificationsAtProteinNterm = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the variable modifications at specific amino acid on peptide
     * C-terminus: possible target -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> variableAaModificationsAtPeptideCterm = new HashMap<Character, ArrayList<String>>(1);
    /**
     * Map of the variable modifications at specific amino acid on protein
     * C-terminus: possible target -> list of modifications.
     */
    private HashMap<Character, ArrayList<String>> variableAaModificationsAtProteinCterm = new HashMap<Character, ArrayList<String>>(1);
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
     * Constructor.
     *
     * @param fixedModifications list of fixed modifications
     * @param variableModifications list of variable modifications
     */
    public TagMatcher(ArrayList<String> fixedModifications, ArrayList<String> variableModifications) {
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
                if (fixedNTermProteinModification == null) {
                    fixedNTermProteinModification = new ArrayList<String>(1);
                }
                fixedNTermProteinModification.add(modificationName);
            } else if (ptm.getType() == PTM.MODC) {
                fixedCTermProteinModificationsMass += ptm.getMass();
                if (fixedCTermProteinModification == null) {
                    fixedCTermProteinModification = new ArrayList<String>(1);
                }
                fixedCTermProteinModification.add(modificationName);
            } else if (ptm.getType() == PTM.MODNP) {
                fixedNTermPeptideModificationsMass += ptm.getMass();
                if (fixedNTermPeptideModification == null) {
                    fixedNTermPeptideModification = new ArrayList<String>(1);
                }
                fixedNTermPeptideModification.add(modificationName);
            } else if (ptm.getType() == PTM.MODCP) {
                fixedCTermPeptideModificationsMass += ptm.getMass();
                if (fixedCTermPeptideModification == null) {
                    fixedCTermPeptideModification = new ArrayList<String>(1);
                }
                fixedCTermPeptideModification.add(modificationName);
            } else if (ptm.getType() == PTM.MODAA) {
                AminoAcidPattern ptmPattern = ptm.getPattern();
                if (ptmPattern.length() > 1) {
                    throw new UnsupportedOperationException("Fixed modifications on patterns is not supported, try variable.");
                }
                for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                    ArrayList<String> ptmNames = fixedAaModifications.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        fixedAaModifications.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
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
                    ArrayList<String> ptmNames = fixedAaModificationsProteinNterm.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        fixedAaModificationsProteinNterm.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
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
                    ArrayList<String> ptmNames = fixedAaModificationsPeptideNterm.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        fixedAaModificationsPeptideNterm.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
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
                    ArrayList<String> ptmNames = fixedAaModificationsProteinCterm.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        fixedAaModificationsProteinCterm.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
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
                    ArrayList<String> ptmNames = fixedAaModificationsPeptideCterm.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        fixedAaModificationsPeptideCterm.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
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
            if (!ptm.isCTerm() && ptm.getMass() < minNtermMod) {
                minNtermMod = ptm.getMass();
            } else if (!ptm.isNTerm() && ptm.getMass() < minCtermMod) {
                minCtermMod = ptm.getMass();
            }
            if (ptm.getType() == PTM.MODNP) {
                if (variableNTermPeptideModifications == null) {
                    variableNTermPeptideModifications = new ArrayList<String>(1);
                }
                variableNTermPeptideModifications.add(modificationName);
            } else if (ptm.getType() == PTM.MODCP) {
                if (variableCTermPeptideModifications == null) {
                    variableCTermPeptideModifications = new ArrayList<String>(1);
                }
                variableCTermPeptideModifications.add(modificationName);
            } else if (ptm.getType() == PTM.MODN) {
                if (variableNTermProteinModifications == null) {
                    variableNTermProteinModifications = new ArrayList<String>(1);
                }
                variableNTermProteinModifications.add(modificationName);
            } else if (ptm.getType() == PTM.MODC) {
                if (variableCTermProteinModifications == null) {
                    variableCTermProteinModifications = new ArrayList<String>(1);
                }
                variableCTermProteinModifications.add(modificationName);
            } else if (ptm.getType() == PTM.MODAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    ArrayList<String> ptmNames = variableAaModifications.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        variableAaModifications.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
                }
            } else if (ptm.getType() == PTM.MODNAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    ArrayList<String> ptmNames = variableAaModificationsAtProteinNterm.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        variableAaModificationsAtProteinNterm.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
                }
            } else if (ptm.getType() == PTM.MODNPAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    ArrayList<String> ptmNames = variableAaModificationsAtPeptideNterm.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        variableAaModificationsAtPeptideNterm.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
                }
            } else if (ptm.getType() == PTM.MODCAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    ArrayList<String> ptmNames = variableAaModificationsAtProteinNterm.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        variableAaModificationsAtProteinNterm.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
                }
            } else if (ptm.getType() == PTM.MODCPAA) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                    ArrayList<String> ptmNames = variableAaModificationsAtPeptideNterm.get(aa);
                    if (ptmNames == null) {
                        ptmNames = new ArrayList<String>(1);
                        variableAaModificationsAtPeptideNterm.put(aa, ptmNames);
                    }
                    ptmNames.add(modificationName);
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
     * @param sequence the sequence where to look for the tag
     * @param tagIndex the index where the tag is located
     * @param componentIndex the index of the component of the tag indexed by
     * tagIndex in the content list
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param massTolerance the ms2 tolerance
     * @param reportFixedPtms a boolean indicating whether fixed PTMs should be
     * reported in the Peptide object
     *
     * @return the possible peptides which can be created on this sequence
     * indexed by their start index
     */
    public HashMap<Integer, ArrayList<Peptide>> getPeptideMatches(Tag tag, String sequence, int tagIndex,
            int componentIndex, SequenceMatchingPreferences sequenceMatchingPreferences, double massTolerance,
            boolean reportFixedPtms) {

        ArrayList<TagComponent> content = tag.getContent();

        // Check tag components to the N-term
        ArrayList<SequenceSegment> nTermPossibleSequences = new ArrayList<SequenceSegment>(1);
        nTermPossibleSequences.add(new SequenceSegment(tagIndex));

        for (int i = componentIndex - 1; i >= 0; i--) {

            TagComponent tagComponent = content.get(i);

            nTermPossibleSequences = mapTagComponent(sequence, tagComponent, componentIndex, nTermPossibleSequences, sequenceMatchingPreferences, reportFixedPtms, massTolerance, true);

            if (nTermPossibleSequences.isEmpty()) {
                return new HashMap<Integer, ArrayList<Peptide>>(0);
            }
        }

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

        // Check tag components to the C-term
        ArrayList<SequenceSegment> cTermPossibleSequences = new ArrayList<SequenceSegment>(1);
        cTermPossibleSequences.add(new SequenceSegment(tagIndex + componentAtIndexLength - 1));

        for (int i = componentIndex + 1; i < content.size(); i++) {

            TagComponent tagComponent = content.get(i);

            cTermPossibleSequences = mapTagComponent(sequence, tagComponent, componentIndex, cTermPossibleSequences, sequenceMatchingPreferences, reportFixedPtms, massTolerance, false);

            if (cTermPossibleSequences.isEmpty()) {
                return new HashMap<Integer, ArrayList<Peptide>>(0);
            }

        }

        // create all possible peptide sequences by adding all possible N and C term to the seed sequence
        String seedSequence = sequence.substring(tagIndex, tagIndex + componentAtIndexLength);
        HashMap<Integer, ArrayList<Peptide>> result = buildPeptides(nTermPossibleSequences, seedSequence, cTermPossibleSequences, modificationsAtIndex);

        return result;
    }

    /**
     * Builds the possible peptides based on the given terminal segments and the
     * seed sequence.
     *
     * @param nTermPossibleSequences the N-terminal possible segments
     * @param seedSequence the seed sequence
     * @param cTermPossibleSequences the C-terminal possible segments
     * @param modificationsAtIndex the seed modifications
     *
     * @return the possible peptides in a map: index on protein -> list of
     * peptides
     */
    public HashMap<Integer, ArrayList<Peptide>> buildPeptides(ArrayList<SequenceSegment> nTermPossibleSequences, String seedSequence, ArrayList<SequenceSegment> cTermPossibleSequences, HashMap<Integer, ArrayList<ModificationMatch>> modificationsAtIndex) {

        HashMap<Integer, ArrayList<Peptide>> result = new HashMap<Integer, ArrayList<Peptide>>(nTermPossibleSequences.size() * cTermPossibleSequences.size());

        for (SequenceSegment nTermSegment : nTermPossibleSequences) {

            StringBuilder nTermSequence = new StringBuilder(nTermSegment.length() + seedSequence.length());
            nTermSequence.append(nTermSegment.getSequence());
            nTermSequence.append(seedSequence);

            for (SequenceSegment cTermSegment : cTermPossibleSequences) {

                StringBuilder peptideSequence = new StringBuilder(nTermSegment.length() + seedSequence.length() + cTermSegment.length());
                peptideSequence.append(nTermSequence);
                ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>(1);
                HashMap<Integer, ArrayList<ModificationMatch>> nTermModifications = nTermSegment.getModificationMatches();

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
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), nTermSegment.length() + i));
                        }
                    }
                }

                peptideSequence.append(cTermSegment.getSequence());
                HashMap<Integer, ArrayList<ModificationMatch>> cTermModifications = cTermSegment.getModificationMatches();

                if (cTermModifications != null) {
                    for (int i : cTermModifications.keySet()) {
                        for (ModificationMatch modificationMatch : cTermModifications.get(i)) {
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), nTermSegment.length() + seedSequence.length() + i));
                        }
                    }
                }

                Peptide peptide = new Peptide(peptideSequence.toString(), modificationMatches);
                int nTermIndex = nTermSegment.getIndexOnProtein();
                ArrayList<Peptide> peptides = result.get(nTermIndex);

                if (peptides == null) {
                    peptides = new ArrayList<Peptide>(1);
                    result.put(nTermIndex, peptides);
                }
                peptides.add(peptide);
            }
        }
        return result;
    }

    /**
     * Maps a tag component on the protein sequence and returns the
     * corresponding possible sequence segments.
     *
     * @param sequence the protein sequence
     * @param tagComponent the tag component to map
     * @param componentIndex the index of the component on the protein sequence,
     * 0 is the first amino acid
     * @param terminalPreviousSequences the possible previous terminal sequences
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param reportFixedPtms if true the fixed PTMs will be reported as
     * ModificationMatch
     * @param massTolerance the ms2 mass tolerance to use
     * @param nTerminus if true the sequencing will go toward the N-terminus, to
     * the C-terminus otherwise
     *
     * @return the possible sequence fragment of this tag component appended to
     * the given previous segments
     */
    public ArrayList<SequenceSegment> mapTagComponent(String sequence, TagComponent tagComponent, int componentIndex, ArrayList<SequenceSegment> terminalPreviousSequences, SequenceMatchingPreferences sequenceMatchingPreferences, boolean reportFixedPtms, double massTolerance, boolean nTerminus) {

        ArrayList<SequenceSegment> newSequences = new ArrayList<SequenceSegment>(1);

        if (tagComponent instanceof AminoAcidPattern) {

            for (SequenceSegment terminalSequence : terminalPreviousSequences) {

                int aaIndex = terminalSequence.getIndexOnProtein();
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;

                String subSequence = null;
                int nextIndex = -1;

                if (nTerminus) {

                    int startIndex = aaIndex - aminoAcidPattern.length();

                    if (startIndex >= 0) {
                        subSequence = sequence.substring(startIndex, aaIndex);
                        nextIndex = startIndex;
                    }
                } else {

                    int endIndex = aaIndex + aminoAcidPattern.length();

                    if (endIndex <= sequence.length() - 1) {
                        subSequence = sequence.substring(aaIndex, endIndex);
                        nextIndex = endIndex;
                    }
                }
                if (subSequence != null && aminoAcidPattern.matches(subSequence, sequenceMatchingPreferences)) {

                    AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidPattern.getModificationMatches());
                    SequenceSegment sequenceSegment = new SequenceSegment(nextIndex, newSequence);
                    if (nTerminus) {
                        sequenceSegment.appendCTerminus(terminalSequence);
                    } else {
                        sequenceSegment.appendNTerminus(terminalSequence);
                    }
                    newSequences.add(sequenceSegment);

                }
            }
        } else if (tagComponent instanceof AminoAcidSequence) {

            for (SequenceSegment terminalSequence : terminalPreviousSequences) {

                int aaIndex = terminalSequence.getIndexOnProtein();
                AminoAcidSequence aminoAcidPattern = (AminoAcidSequence) tagComponent;

                String subSequence = null;
                int nextIndex = -1;

                if (nTerminus) {

                    int startIndex = aaIndex - aminoAcidPattern.length();

                    if (startIndex >= 0) {
                        subSequence = sequence.substring(startIndex, aaIndex);
                        nextIndex = startIndex;
                    }
                } else {

                    int endIndex = aaIndex + aminoAcidPattern.length();

                    if (endIndex <= sequence.length() - 1) {
                        subSequence = sequence.substring(aaIndex, endIndex);
                        nextIndex = endIndex;
                    }
                }
                if (subSequence != null && aminoAcidPattern.matches(subSequence, sequenceMatchingPreferences)) {

                    AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidPattern.getModificationMatches());
                    SequenceSegment sequenceSegment = new SequenceSegment(nextIndex, newSequence);
                    if (nTerminus) {
                        sequenceSegment.appendCTerminus(terminalSequence);
                    } else {
                        sequenceSegment.appendNTerminus(terminalSequence);
                    }
                    newSequences.add(sequenceSegment);

                }
            }
        } else if (tagComponent instanceof MassGap) {

            double massGap = tagComponent.getMass();

            for (SequenceSegment terminalSequence : terminalPreviousSequences) {

                int aaIndex = terminalSequence.getIndexOnProtein();
                int currentIndex = aaIndex;
                ArrayList<SequenceSegment> possibleSequences = new ArrayList<SequenceSegment>(1);
                ArrayList<SequenceSegment> validSequences = new ArrayList<SequenceSegment>(1);

                if (nTerminus) {
                    aaIndex--;
                } else {
                    aaIndex++;
                }
                while (aaIndex >= 0 && aaIndex < sequence.length()) {

                    char aa = sequence.charAt(aaIndex);
                    AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                    ArrayList<String> fixedModificationsAtAa = null;
                    Double fixedMass = null;
                    if (reportFixedPtms) {
                        fixedModificationsAtAa = fixedAaModifications.get(aa);
                    } else {
                        fixedMass = fixedAaModificationsMasses.get(aa);
                    }
                    ArrayList<String> variableModificationsAtAa = variableAaModifications.get(aa);

                    if (possibleSequences.isEmpty()) {

                        SequenceSegment sequenceSegment = new SequenceSegment(currentIndex);
                        if (nTerminus) {
                            sequenceSegment.appendNTerminus(aminoAcid);
                        } else {
                            sequenceSegment.appendCTerminus(aminoAcid);
                        }

                        addFixedModifications(sequenceSegment, fixedModificationsAtAa, fixedMass, reportFixedPtms, nTerminus);
                        possibleSequences.add(sequenceSegment);

                        addVariableModifications(variableModificationsAtAa, sequenceSegment, possibleSequences, nTerminus);

                        if (nTerminus && aaIndex == 0) {
                            addFixedModifications(sequenceSegment, fixedNTermProteinModification, fixedNTermProteinModificationsMass, reportFixedPtms, nTerminus);
                            ArrayList<String> fixedModificationsAtProteinNtermAa = null;
                            if (reportFixedPtms) {
                                fixedModificationsAtProteinNtermAa = fixedAaModificationsProteinNterm.get(aa);
                            }
                            addFixedModifications(sequenceSegment, fixedModificationsAtProteinNtermAa, fixedNTermProteinModificationsMass, reportFixedPtms, reportFixedPtms);

                            addVariableModifications(variableNTermProteinModifications, sequenceSegment, possibleSequences, nTerminus);
                            ArrayList<String> variableModificationsAtProteinNtermAa = variableAaModificationsAtProteinNterm.get(aa);
                            addVariableModifications(variableModificationsAtProteinNtermAa, sequenceSegment, possibleSequences, reportFixedPtms);
                        } else if (!nTerminus && aaIndex == sequence.length() - 1) {
                            addFixedModifications(sequenceSegment, fixedCTermProteinModification, fixedCTermProteinModificationsMass, reportFixedPtms, nTerminus);
                            ArrayList<String> fixedModificationsAtProteinCtermAa = null;
                            if (reportFixedPtms) {
                                fixedModificationsAtProteinCtermAa = fixedAaModificationsProteinCterm.get(aa);
                            }
                            addFixedModifications(sequenceSegment, fixedModificationsAtProteinCtermAa, fixedCTermProteinModificationsMass, reportFixedPtms, reportFixedPtms);

                            addVariableModifications(variableCTermProteinModifications, sequenceSegment, possibleSequences, nTerminus);
                            ArrayList<String> variableModificationsAtProteinCtermAa = variableAaModificationsAtProteinCterm.get(aa);
                            addVariableModifications(variableModificationsAtProteinCtermAa, sequenceSegment, possibleSequences, reportFixedPtms);
                        }

                    } else {

                        ArrayList<SequenceSegment> newPossibleSequences = new ArrayList<SequenceSegment>(possibleSequences.size());

                        for (SequenceSegment sequenceSegment : possibleSequences) {

                            SequenceSegment newSegment = new SequenceSegment(sequenceSegment);
                            if (nTerminus) {
                                newSegment.appendNTerminus(aminoAcid);
                            } else {
                                newSegment.appendCTerminus(aminoAcid);
                            }

                            addFixedModifications(newSegment, fixedModificationsAtAa, fixedMass, reportFixedPtms, nTerminus);
                            newPossibleSequences.add(newSegment);

                            addVariableModifications(variableModificationsAtAa, newSegment, newPossibleSequences, nTerminus);

                            if (nTerminus && aaIndex == 0) {
                                addFixedModifications(newSegment, fixedNTermProteinModification, fixedNTermProteinModificationsMass, reportFixedPtms, nTerminus);
                                ArrayList<String> fixedModificationsAtProteinNtermAa = null;
                                if (reportFixedPtms) {
                                    fixedModificationsAtProteinNtermAa = fixedAaModificationsProteinNterm.get(aa);
                                }
                                addFixedModifications(newSegment, fixedModificationsAtProteinNtermAa, fixedAaModificationsProteinNtermMasses.get(aa), reportFixedPtms, reportFixedPtms);

                                addVariableModifications(variableNTermProteinModifications, newSegment, newPossibleSequences, nTerminus);
                                ArrayList<String> variableModificationsAtProteinNtermAa = variableAaModificationsAtProteinNterm.get(aa);
                                addVariableModifications(variableModificationsAtProteinNtermAa, newSegment, newPossibleSequences, reportFixedPtms);
                            } else if (!nTerminus && aaIndex == sequence.length() - 1) {
                                addFixedModifications(newSegment, fixedCTermProteinModification, fixedCTermProteinModificationsMass, reportFixedPtms, nTerminus);
                                ArrayList<String> fixedModificationsAtProteinCtermAa = null;
                                if (reportFixedPtms) {
                                    fixedModificationsAtProteinCtermAa = fixedAaModificationsProteinCterm.get(aa);
                                }
                                addFixedModifications(newSegment, fixedModificationsAtProteinCtermAa, fixedAaModificationsProteinCtermMasses.get(aa), reportFixedPtms, reportFixedPtms);

                                addVariableModifications(variableCTermProteinModifications, newSegment, newPossibleSequences, nTerminus);
                                ArrayList<String> variableModificationsAtProteinCtermAa = variableAaModificationsAtProteinCterm.get(aa);
                                addVariableModifications(variableModificationsAtProteinCtermAa, newSegment, newPossibleSequences, reportFixedPtms);
                            }
                        }

                        possibleSequences = newPossibleSequences;
                    }

                    Iterator<SequenceSegment> possibleSegmentsIterator = possibleSequences.iterator();
                    while (possibleSegmentsIterator.hasNext()) {

                        SequenceSegment sequenceSegment = possibleSegmentsIterator.next();

                        if (nTerminus) {
                            if (fixedNTermPeptideModification != null || fixedAaModificationsPeptideNterm.containsKey(aa)) {
                                SequenceSegment candidateSegment = new SequenceSegment(sequenceSegment);
                                addFixedModifications(candidateSegment, fixedNTermPeptideModification, fixedNTermPeptideModificationsMass, reportFixedPtms, nTerminus);
                                ArrayList<String> fixedModificationsAtPeptideNtermAa = null;
                                if (reportFixedPtms) {
                                    fixedModificationsAtPeptideNtermAa = fixedAaModificationsPeptideNterm.get(aa);
                                }
                                addFixedModifications(candidateSegment, fixedModificationsAtPeptideNtermAa, fixedAaModificationsPeptideNtermMasses.get(aa), reportFixedPtms, nTerminus);
                                sequenceSegment = candidateSegment;
                            }
                        } else {
                            if (fixedCTermPeptideModification != null || fixedAaModificationsPeptideCterm.containsKey(aa)) {
                                SequenceSegment candidateSegment = new SequenceSegment(sequenceSegment);
                                addFixedModifications(candidateSegment, fixedCTermPeptideModification, fixedCTermPeptideModificationsMass, reportFixedPtms, nTerminus);
                                ArrayList<String> fixedModificationsAtPeptideCtermAa = null;
                                if (reportFixedPtms) {
                                    fixedModificationsAtPeptideCtermAa = fixedAaModificationsPeptideCterm.get(aa);
                                }
                                addFixedModifications(candidateSegment, fixedModificationsAtPeptideCtermAa, fixedAaModificationsPeptideCtermMasses.get(aa), reportFixedPtms, nTerminus);
                            }
                        }

                        double terminalModificationMargin;
                        if (nTerminus) {
                            terminalModificationMargin = minNtermMod;
                        } else {
                            terminalModificationMargin = minCtermMod;
                        }
                        if (sequenceSegment.getMass() + terminalModificationMargin > massGap + massTolerance) {
                            possibleSegmentsIterator.remove();
                        } else {
                            if (Math.abs(sequenceSegment.getMass() - massGap) <= massTolerance) {
                                validSequences.add(sequenceSegment);
                                possibleSegmentsIterator.remove();
                            } else if (nTerminus) {
                                boolean found = false;
                                if (variableNTermPeptideModifications != null) {
                                    for (String modificationName : variableNTermPeptideModifications) {
                                        SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                                        addVariableModification(modificationName, modifiedSegment, nTerminus);
                                        if (Math.abs(modifiedSegment.getMass() - massGap) <= massTolerance) {
                                            validSequences.add(modifiedSegment);
                                            possibleSegmentsIterator.remove();
                                            break;
                                        }
                                    }
                                }
                                if (!found) {
                                    ArrayList<String> variableNTermPeptideModificationsAtAa = variableAaModificationsAtPeptideNterm.get(aa);
                                    if (variableNTermPeptideModificationsAtAa != null) {
                                        for (String modificationName : variableNTermPeptideModificationsAtAa) {
                                            SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                                            addVariableModification(modificationName, modifiedSegment, nTerminus);
                                            if (Math.abs(modifiedSegment.getMass() - massGap) <= massTolerance) {
                                                validSequences.add(modifiedSegment);
                                                possibleSegmentsIterator.remove();
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                boolean found = false;
                                if (variableCTermPeptideModifications != null) {
                                    for (String modificationName : variableCTermPeptideModifications) {
                                        SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                                        addVariableModification(modificationName, modifiedSegment, nTerminus);
                                        if (Math.abs(modifiedSegment.getMass() - massGap) <= massTolerance) {
                                            validSequences.add(modifiedSegment);
                                            possibleSegmentsIterator.remove();
                                            break;
                                        }
                                    }
                                }
                                if (!found) {
                                    ArrayList<String> variableCTermPeptideModificationsAtAa = variableAaModificationsAtPeptideCterm.get(aa);
                                    if (variableCTermPeptideModificationsAtAa != null) {
                                        for (String modificationName : variableCTermPeptideModificationsAtAa) {
                                            SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                                            addVariableModification(modificationName, modifiedSegment, nTerminus);
                                            if (Math.abs(modifiedSegment.getMass() - massGap) <= massTolerance) {
                                                validSequences.add(modifiedSegment);
                                                possibleSegmentsIterator.remove();
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
                    if (nTerminus) {
                        aaIndex--;
                    } else {
                        aaIndex++;
                    }
                }
                for (SequenceSegment validSegment : validSequences) {
                    int newIndex;
                    if (nTerminus) {
                        newIndex = currentIndex - validSegment.length();
                    } else {
                        newIndex = currentIndex + validSegment.length();
                    }
                    SequenceSegment sequenceSegment = new SequenceSegment(validSegment, newIndex);
                    if (nTerminus) {
                        sequenceSegment.appendCTerminus(terminalSequence);
                    } else {
                        sequenceSegment.appendNTerminus(terminalSequence);
                    }
                    newSequences.add(sequenceSegment);
                }
            }
        } else {
            throw new IllegalArgumentException("Tag component " + tagComponent.getClass() + " not implemented for sequence matching.");
        }
        return newSequences;
    }

    /**
     * Adds fixed modifications to a sequence segment.
     *
     * @param sequenceSegment the sequence segment of interest
     * @param modificationNames the fixed modifications to add
     * @param fixedMass the total mass to add to the sequence
     * @param reportFixedModifications if true modification matches will be
     * added to the sequence segment using modificationNames, the fixedMass will
     * be used otherwise
     * @param nTerminus if true modification will be added to the N-terminus, to
     * the C-terminus otherwise
     */
    public void addFixedModifications(SequenceSegment sequenceSegment, ArrayList<String> modificationNames, Double fixedMass, boolean reportFixedModifications, boolean nTerminus) {
        if (modificationNames != null || fixedMass != null) {
            if (reportFixedModifications) {
                for (String modification : modificationNames) {
                    addFixedModification(sequenceSegment, modification, fixedMass, reportFixedModifications, nTerminus);
                }
            } else {
                sequenceSegment.addMass(fixedMass);
            }
        }
    }

    /**
     * Adds a fixed modification to a sequence segment.
     *
     * @param sequenceSegment the sequence segment of interest
     * @param modificationName the fixed modification to add
     * @param fixedMass the mass to add to the sequence
     * @param reportFixedModifications if true modification match will be added
     * to the sequence segment using modificationName, the fixedMass will be
     * used otherwise
     * @param nTerminus if true modification will be added to the N-terminus, to
     * the C-terminus otherwise
     */
    public void addFixedModification(SequenceSegment sequenceSegment, String modificationName, Double fixedMass, boolean reportFixedModifications, boolean nTerminus) {
        if (modificationName != null || fixedMass != null) {
            if (reportFixedModifications) {
                if (nTerminus) {
                    sequenceSegment.addModificationNTerminus(new ModificationMatch(modificationName, false, 1));
                } else {
                    sequenceSegment.addModificationCTerminus(new ModificationMatch(modificationName, false, sequenceSegment.length()));
                }
            } else {
                sequenceSegment.addMass(fixedMass);
            }
        }
    }

    /**
     * Adds the potential sequence segments obtained after adding the given
     * variable modifications on a segment terminus to the given list of
     * possible segments.
     *
     * @param variableModifications the variable modifications to add
     * @param sequenceSegment the sequence segment of interest
     * @param possibleSegments the possible segment where to add the modified
     * segments
     * @param nTerminus if true modification will be added to the N-terminus, to
     * the C-terminus otherwise
     */
    public void addVariableModifications(ArrayList<String> variableModifications, SequenceSegment sequenceSegment, ArrayList<SequenceSegment> possibleSegments, boolean nTerminus) {
        if (variableModifications != null) {
            for (String modificationName : variableModifications) {
                addVariableModification(modificationName, sequenceSegment, possibleSegments, nTerminus);
            }
        }
    }

    /**
     * Adds the potential modification to the given sequence segment.
     *
     * @param variableModification the variable modifications to add
     * @param sequenceSegment the sequence segment of interest
     * @param nTerminus if true modification will be added to the N-terminus, to
     * the C-terminus otherwise
     */
    public void addVariableModification(String variableModification, SequenceSegment sequenceSegment, boolean nTerminus) {
        if (nTerminus) {
            sequenceSegment.addModificationNTerminus(new ModificationMatch(variableModification, true, 1));
        } else {
            sequenceSegment.addModificationCTerminus(new ModificationMatch(variableModification, true, sequenceSegment.length()));
        }
    }

    /**
     * Adds the potential sequence segments obtained after adding the given
     * variable modification on a segment terminus to the given list of possible
     * segments.
     *
     * @param variableModification the variable modifications to add
     * @param sequenceSegment the sequence segment of interest
     * @param possibleSegments the possible segments where to add the modified
     * segments
     * @param nTerminus if true modification will be added to the N-terminus, to
     * the C-terminus otherwise
     */
    public void addVariableModification(String variableModification, SequenceSegment sequenceSegment, ArrayList<SequenceSegment> possibleSegments, boolean nTerminus) {
        SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
        addVariableModification(variableModification, modifiedSegment, nTerminus);
        possibleSegments.add(modifiedSegment);
    }
}
