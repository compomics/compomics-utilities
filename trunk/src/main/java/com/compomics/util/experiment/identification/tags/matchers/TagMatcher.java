package com.compomics.util.experiment.identification.tags.matchers;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.MutationMatrix;
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
import java.util.HashSet;

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
     */
    private HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>>> nTermCache = new HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>>>();
    /**
     * The sequence segments cache for C-term sequencing.
     */
    private HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>>> cTermCache = new HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>>>();
    /**
     * The sequence matching preferences.
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences;
    /**
     * A boolean indicating whether fixed PTMs should be reported in the Peptide
     * object.
     */
    private boolean reportFixedPtms = false;

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
    public HashMap<Integer, ArrayList<Peptide>> getPeptideMatches(Tag tag, String accession, String sequence, int tagIndex,
            int componentIndex, double massTolerance) {

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

        int seedMutations;
        if (componentAtIndex instanceof AminoAcidPattern) {
            AminoAcidPattern tagPattern = (AminoAcidPattern) componentAtIndex;
            seedMutations = tagPattern.nMutations(seedSequence, sequenceMatchingPreferences);
        } else if (componentAtIndex instanceof AminoAcidSequence) {
            AminoAcidSequence tagSequence = (AminoAcidSequence) componentAtIndex;
            seedMutations = tagSequence.nMutations(seedSequence, sequenceMatchingPreferences);
        } else {
            throw new UnsupportedOperationException("Tag mapping not supported for tag component " + componentAtIndex.getClass() + ".");
        }

        if (sequenceMatchingPreferences.getMaxMutationsPerPeptide() != null && sequenceMatchingPreferences.getMaxMutationsPerPeptide() < seedMutations) {
            return new HashMap<Integer, ArrayList<Peptide>>(0);
        }

        // Check tag components to the N-term
        ArrayList<SequenceSegment> nTermPossibleSequences = new ArrayList<SequenceSegment>(1);
        nTermPossibleSequences.add(new SequenceSegment(tagIndex));

        for (int i = componentIndex - 1; i >= 0; i--) {

            TagComponent tagComponent = content.get(i);
            nTermPossibleSequences = mapTagComponent(accession, sequence, tagComponent, nTermPossibleSequences, massTolerance, useCache && i == componentIndex - 1, true);

            if (nTermPossibleSequences.isEmpty()) {
                return new HashMap<Integer, ArrayList<Peptide>>(0);
            }
        }

        // Check tag components to the C-term
        ArrayList<SequenceSegment> cTermPossibleSequences = new ArrayList<SequenceSegment>(1);
        cTermPossibleSequences.add(new SequenceSegment(tagIndex + componentAtIndexLength - 1));

        for (int i = componentIndex + 1; i < content.size(); i++) {

            TagComponent tagComponent = content.get(i);
            cTermPossibleSequences = mapTagComponent(accession, sequence, tagComponent, cTermPossibleSequences, massTolerance, useCache && i == componentIndex + 1, false);

            if (cTermPossibleSequences.isEmpty()) {
                return new HashMap<Integer, ArrayList<Peptide>>(0);
            }

        }

        // create all possible peptide sequences by adding all possible N and C term to the seed sequence
        HashMap<Integer, ArrayList<Peptide>> result = buildPeptides(nTermPossibleSequences, seedSequence, cTermPossibleSequences, modificationsAtIndex, seedMutations);

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
     * @param mutationsAtIndex the seeds mutations
     *
     * @return the possible peptides in a map: index on protein -> list of
     * peptides
     */
    public HashMap<Integer, ArrayList<Peptide>> buildPeptides(ArrayList<SequenceSegment> nTermPossibleSequences, String seedSequence, ArrayList<SequenceSegment> cTermPossibleSequences, HashMap<Integer, ArrayList<ModificationMatch>> modificationsAtIndex, int mutationsAtIndex) {

        HashMap<Integer, ArrayList<Peptide>> result = new HashMap<Integer, ArrayList<Peptide>>(nTermPossibleSequences.size() * cTermPossibleSequences.size());

        if (sequenceMatchingPreferences.getMaxMutationsPerPeptide() == null || sequenceMatchingPreferences.getMaxMutationsPerPeptide() > mutationsAtIndex) {

            for (SequenceSegment nTermSegment : nTermPossibleSequences) {

                StringBuilder nTermSequence = new StringBuilder(nTermSegment.length() + seedSequence.length());
                nTermSequence.append(nTermSegment.getSequence());
                nTermSequence.append(seedSequence);

                if (sequenceMatchingPreferences.getMaxMutationsPerPeptide() == null || sequenceMatchingPreferences.getMaxMutationsPerPeptide() > mutationsAtIndex + nTermSegment.getnMutations()) {

                    for (SequenceSegment cTermSegment : cTermPossibleSequences) {

                        if (sequenceMatchingPreferences.getMaxMutationsPerPeptide() == null || sequenceMatchingPreferences.getMaxMutationsPerPeptide() > mutationsAtIndex + nTermSegment.getnMutations() + cTermSegment.getnMutations()) {

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
                }
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
    private ArrayList<SequenceSegment> mapTagComponent(String accession, String sequence, TagComponent tagComponent,
            ArrayList<SequenceSegment> terminalPreviousSequences, double massTolerance, boolean useCache, boolean nTerminus) {

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
                ArrayList<SequenceSegment> possibleSequences = null;
                ArrayList<SequenceSegment> validSequences = new ArrayList<SequenceSegment>(1);

                HashMap<Integer, ArrayList<SequenceSegment>> indexCache = getIndexCache(accession, currentIndex, nTerminus);

                if (nTerminus) {
                    aaIndex--;
                } else {
                    aaIndex++;
                }

                while (aaIndex >= 0 && aaIndex < sequence.length()) {

                    char sequenceAa = sequence.charAt(aaIndex);
                    AminoAcid sequenceAminoAcid = AminoAcid.getAminoAcid(sequenceAa);

                    if (useCache) {
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

                    if (validateSegments(possibleSequences, validSequences, massGap, massTolerance, sequenceAa, reportFixedPtms, nTerminus)) {
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
     * Returns the index cache.
     *
     * @param accession the accession of the protein
     * @param currentIndex the index on the protein sequence
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return the cache index to use
     */
    public HashMap<Integer, ArrayList<SequenceSegment>> getIndexCache(String accession, int currentIndex, boolean nTerminus) {
        HashMap<Integer, ArrayList<SequenceSegment>> indexCache = null;
        if (useCache) {
            HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> proteinCache;
            if (nTerminus) {
                proteinCache = nTermCache.get(accession);
            } else {
                proteinCache = cTermCache.get(accession);
            }
            if (proteinCache == null) {
                proteinCache = addProteinCache(accession, nTerminus);
            }
            indexCache = proteinCache.get(currentIndex);
            if (indexCache == null) {
                indexCache = addIndexCache(proteinCache, currentIndex);
            }
        }
        return indexCache;
    }

    /**
     * Adds a cache for the given protein and returns it.
     *
     * @param accession the accession of the protein
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return a cache for the given protein
     */
    private synchronized HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> addProteinCache(String accession, boolean nTerminus) {
        HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> proteinCache;
        if (nTerminus) {
            proteinCache = nTermCache.get(accession);
        } else {
            proteinCache = cTermCache.get(accession);
        }
        if (proteinCache == null) {
            proteinCache = new HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>>(1);
            if (nTerminus) {
                nTermCache.put(accession, proteinCache);
            } else {
                cTermCache.put(accession, proteinCache);
            }
        }
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
    private synchronized HashMap<Integer, ArrayList<SequenceSegment>> addIndexCache(HashMap<Integer, HashMap<Integer, ArrayList<SequenceSegment>>> proteinCache, int currentIndex) {
        HashMap<Integer, ArrayList<SequenceSegment>> indexCache = proteinCache.get(currentIndex);
        if (indexCache == null) {
            indexCache = new HashMap<Integer, ArrayList<SequenceSegment>>(1);
            proteinCache.put(currentIndex, indexCache);
        }
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
    public synchronized ArrayList<SequenceSegment> addSequenceSegmentsToCacheSynchronized(HashMap<Integer, ArrayList<SequenceSegment>> indexCache,
            String sequence, AminoAcid aminoAcid, int currentIndex, int aaIndex, boolean nTerminus) {
        return addSequenceSegmentsToCache(indexCache, sequence, aminoAcid, currentIndex, aaIndex, nTerminus);
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
    public ArrayList<SequenceSegment> addSequenceSegmentsToCache(HashMap<Integer, ArrayList<SequenceSegment>> indexCache,
            String sequence, AminoAcid aminoAcid, int currentIndex, int aaIndex, boolean nTerminus) {
        // check whether another thread already did the job
        ArrayList<SequenceSegment> result = indexCache.get(aaIndex);
        if (result == null) {
            ArrayList<SequenceSegment> previousSequences;
            if (nTerminus) {
                previousSequences = indexCache.get(aaIndex + 1);
            } else {
                previousSequences = indexCache.get(aaIndex - 1);
            }
            result = getCombinationsForAminoAcid(sequence, previousSequences, aminoAcid, currentIndex, aaIndex, nTerminus);
            indexCache.put(aaIndex, result);
        }
        return result;
    }

    /**
     * Adds the possible new sequence segments generated when appending the
     * given amino acid
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
    public ArrayList<SequenceSegment> getCombinationsForAminoAcid(String sequence, ArrayList<SequenceSegment> possibleSequences, AminoAcid aminoAcid, int currentIndex, int aaIndex, boolean nTerminus) {
        ArrayList<SequenceSegment> result = getCombinationsForAminoAcid(sequence, possibleSequences, aminoAcid, false, currentIndex, aaIndex, nTerminus);
        if (sequenceMatchingPreferences.getMaxMutationsPerPeptide() == null || sequenceMatchingPreferences.getMaxMutationsPerPeptide() > 0) {
            MutationMatrix mutationMatrix = sequenceMatchingPreferences.getMutationMatrix();
            if (mutationMatrix != null) {
                HashSet<Character> mutatedAas = mutationMatrix.getMutatedAminoAcids(aminoAcid.getSingleLetterCodeAsChar());
                if (mutatedAas != null) {
                    for (char aa : mutatedAas) {
                        AminoAcid mutatedAminoAcid = AminoAcid.getAminoAcid(aa);
                        ArrayList<SequenceSegment> sequenceSegmentsForMutatedAa = getCombinationsForAminoAcid(sequence, possibleSequences, mutatedAminoAcid, true, currentIndex, aaIndex, nTerminus);
                        result.addAll(sequenceSegmentsForMutatedAa);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Adds the possible new sequence segments generated when appending the
     * given amino acid.
     *
     * @param sequence the protein sequence
     * @param possibleSequences the possible previous sequences
     * @param aminoAcid the amino acid object
     * @param mutated indicates whether the given amino acid is mutated
     * @param currentIndex the current indexing level on the protein sequence
     * @param aaIndex the amino acid index
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return the new possible sequences
     */
    public ArrayList<SequenceSegment> getCombinationsForAminoAcid(String sequence, ArrayList<SequenceSegment> possibleSequences,
            AminoAcid aminoAcid, boolean mutated, int currentIndex, int aaIndex, boolean nTerminus) {

        char aa = aminoAcid.getSingleLetterCodeAsChar();
        ArrayList<String> fixedModificationsAtAa = null;
        Double fixedMass = null;

        if (reportFixedPtms) {
            fixedModificationsAtAa = fixedAaModifications.get(aa);
        } else {
            fixedMass = fixedAaModificationsMasses.get(aa);
        }

        ArrayList<String> variableModificationsAtAa = variableAaModifications.get(aa);

        if (possibleSequences == null) {

            possibleSequences = new ArrayList<SequenceSegment>(1);
            SequenceSegment sequenceSegment = new SequenceSegment(currentIndex);

            if (!mutated || sequenceMatchingPreferences.getMaxMutationsPerPeptide() == null || sequenceMatchingPreferences.getMaxMutationsPerPeptide() > sequenceSegment.getnMutations()) {

                if (nTerminus) {
                    sequenceSegment.appendNTerminus(aminoAcid);
                } else {
                    sequenceSegment.appendCTerminus(aminoAcid);
                }
                if (mutated) {
                    sequenceSegment.increaseMutationCount();
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
            }

            return possibleSequences;
        } else {

            ArrayList<SequenceSegment> newPossibleSequences = new ArrayList<SequenceSegment>(possibleSequences.size());

            for (SequenceSegment sequenceSegment : possibleSequences) {

                if (!mutated || sequenceMatchingPreferences.getMaxMutationsPerPeptide() == null || sequenceMatchingPreferences.getMaxMutationsPerPeptide() > sequenceSegment.getnMutations()) {

                    SequenceSegment newSegment = new SequenceSegment(sequenceSegment);
                    if (nTerminus) {
                        newSegment.appendNTerminus(aminoAcid);
                    } else {
                        newSegment.appendCTerminus(aminoAcid);
                    }
                    if (mutated) {
                        newSegment.increaseMutationCount();
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
     * @param aa the amino acid at terminus
     * @param reportFixedPtms if yes fixed PTMs will be reported as modification
     * matches
     * @param nTerminus indicates whether the sequencing goes toward the N
     * (true) or the C (false) terminus
     *
     * @return if true no more sequence segment can be mapped
     */
    public boolean validateSegments(ArrayList<SequenceSegment> possibleSequences, ArrayList<SequenceSegment> validSequences, 
            double massGap, double massTolerance, char aa, boolean reportFixedPtms, boolean nTerminus) {

        boolean allInspected = true;

        for (SequenceSegment sequenceSegment : possibleSequences) {

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
            if (sequenceSegment.getMass() + terminalModificationMargin <= massGap + massTolerance) {
                if (Math.abs(sequenceSegment.getMass() - massGap) <= massTolerance) {
                    validSequences.add(sequenceSegment);
                } else if (nTerminus) {
                    boolean found = false;
                    if (variableNTermPeptideModifications != null) {
                        for (String modificationName : variableNTermPeptideModifications) {
                            SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                            addVariableModification(modificationName, modifiedSegment, nTerminus);
                            if (Math.abs(modifiedSegment.getMass() - massGap) <= massTolerance) {
                                validSequences.add(modifiedSegment);
                                found = true;
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
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!found) {
                        allInspected = false;
                    }
                } else {
                    boolean found = false;
                    if (variableCTermPeptideModifications != null) {
                        for (String modificationName : variableCTermPeptideModifications) {
                            SequenceSegment modifiedSegment = new SequenceSegment(sequenceSegment);
                            addVariableModification(modificationName, modifiedSegment, nTerminus);
                            if (Math.abs(modifiedSegment.getMass() - massGap) <= massTolerance) {
                                validSequences.add(modifiedSegment);
                                found = true;
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
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!found) {
                        allInspected = false;
                    }
                }
            }
        }
        return allInspected;
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

    /**
     * Sets whether fixed PTMs should be reported in the Peptide object.
     *
     * @param reportFixedPtms if true fixed PTMs will be reported as
     * modification matches in the Peptide object
     */
    public void setReportFixedPtms(boolean reportFixedPtms) {
        this.reportFixedPtms = reportFixedPtms;
    }
}
