package com.compomics.util.experiment.identification.amino_acid_tags;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Segment of sequence indexed on a protein sequence.
 *
 * @author Marc Vaudel
 */
public class SequenceSegment {

    /**
     * The index of the terminus on the protein.
     */
    private int terminalIndex;
    /**
     * The length of the segment.
     */
    private int length = 0;
    /**
     * Map of mutations: index on the segment - mutated character.
     */
    private HashMap<Integer, Character> mutations = null;
    /**
     * Map of the PTMs: index on the segment - name of the modification.
     */
    private HashMap<Integer, String> modificationMatches = null;
    /**
     * The previous segment in case this one is a follow-up.
     */
    private SequenceSegment previousSegment = null;
    /**
     * Boolean indicating whether the segment is sequencing from the N (true) or
     * the C (false) terminus.
     */
    public final boolean nTerminus;
    /**
     * The index of the mutation.
     */
    private Integer mutationIndex = null;
    /**
     * The mutated amino acid.
     */
    private Character mutation = null;
    /**
     * The modification located at the terminus.
     */
    private String modificationAtTerminus = null;
    /**
     * The mass of the segment.
     */
    private double mass = 0;

    /**
     * Constructor.
     *
     * @param terminalIndex the index of the segment on the protein
     * @param nTerminus indicates whether the segment is indexed to the N (true)
     * or C (false) terminus
     */
    public SequenceSegment(int terminalIndex, boolean nTerminus) {
        this.nTerminus = nTerminus;
        this.terminalIndex = terminalIndex;
    }

    /**
     * Constructor.
     *
     * @param sequenceSegment another sequence segment
     */
    public SequenceSegment(SequenceSegment sequenceSegment) {
        this.nTerminus = sequenceSegment.nTerminus;
        this.terminalIndex = sequenceSegment.getTerminalIndex();
        length = sequenceSegment.length();
        previousSegment = sequenceSegment;
        Integer otherMutationIndex = sequenceSegment.getMutationIndex();
        if (otherMutationIndex != null) {
            mutationIndex = otherMutationIndex;
            mutation = sequenceSegment.getMutation();
        }
        mass = sequenceSegment.getMass();
    }

    /**
     * Returns the length of the segment.
     *
     * @return the length of the segment
     */
    public int length() {
        return length;
    }

    /**
     * Returns a map of mutations on that segment: index on the segment -
     * mutated character.
     *
     * @return a map of mutations on that segment
     */
    public HashMap<Integer, Character> getMutations() {
        if (previousSegment != null) {
            HashMap<Integer, Character> previousMutations = previousSegment.getMutations();
            if (previousMutations != null) {
                if (mutations == null) {
                    if (mutation == null) {
                        return previousMutations;
                    } else {
                        HashMap<Integer, Character> tempMutations = new HashMap<>(previousMutations.size());
                        tempMutations.put(mutationIndex, mutation);
                        return tempMutations;
                    }
                }
                mutations.putAll(previousMutations);
            }
        }
        if (mutation != null) {
            if (mutations == null) {
                HashMap<Integer, Character> tempMutations = new HashMap<>(1);
                tempMutations.put(mutationIndex, mutation);
                return tempMutations;
            }
            mutations.put(mutationIndex, mutation);
        }
        return mutations;
    }

    /**
     * Returns a map of modifications on that segment: index on the segment -
     * modification name.
     *
     * @return a map of modifications on that segment
     */
    public HashMap<Integer, String> getModificationMatches() {
        if (previousSegment != null) {
            HashMap<Integer, String> previousModifications = previousSegment.getModificationMatches();
            if (previousModifications != null) {
                if (modificationMatches == null) {
                    if (modificationAtTerminus == null) {
                        return previousModifications;
                    } else {
                        HashMap<Integer, String> tempModifications = new HashMap<>(previousModifications.size());
                        tempModifications.put(length, modificationAtTerminus);
                        return tempModifications;
                    }
                }
                modificationMatches.putAll(previousModifications);
            }
        }
        if (modificationAtTerminus != null) {
            if (modificationMatches == null) {
                HashMap<Integer, String> tempModifications = new HashMap<>(1);
                tempModifications.put(length, modificationAtTerminus);
                return tempModifications;
            }
            modificationMatches.put(length, modificationAtTerminus);
        }
        return modificationMatches;
    }

    /**
     * Returns the index of the terminal on the protein sequence.
     *
     * @return the index of the terminal on the protein sequence
     */
    public int getTerminalIndex() {
        return terminalIndex;
    }

    /**
     * Returns the starting index of the segment on the protein.
     *
     * @return the starting index of the segment on the protein
     */
    public int getIndexOnProtein() {
        if (nTerminus) {
            return terminalIndex + 1;
        } else {
            return terminalIndex - length;
        }
    }

    /**
     * Returns the mutation index, null if none.
     *
     * @return the mutation index
     */
    public Integer getMutationIndex() {
        return mutationIndex;
    }

    /**
     * Returns the mutated amino acid, null if none.
     *
     * @return the amino acid at the terminus if mutated
     */
    public Character getMutation() {
        return mutation;
    }

    /**
     * Returns the mass of the segment.
     *
     * @return the mass of the segment
     */
    public double getMass() {
        return mass;
    }

    /**
     * Adds a mass to the segment.
     *
     * @param mass the mass to add
     */
    public void addMass(double mass) {
        this.mass += mass;
    }

    /**
     * Appends a sequence segment to the terminus of sequencing.
     *
     * @param sequenceSegment a sequence segment
     */
    public void appendTerminus(SequenceSegment sequenceSegment) {
        HashMap<Integer, Character> otherMutations = sequenceSegment.getMutations();
        if (otherMutations != null) {
            if (mutations == null) {
                mutations = new HashMap<>(otherMutations.size());
            }
            for (int index : otherMutations.keySet()) {
                mutations.put(index + length, otherMutations.get(index));
            }
        }
        HashMap<Integer, String> otherModifications = sequenceSegment.getModificationMatches();
        if (otherModifications != null) {
            if (modificationMatches == null) {
                modificationMatches = new HashMap<>(otherModifications.size());
            }
            for (int index : otherModifications.keySet()) {
                modificationMatches.put(index + length, otherModifications.get(index));
            }
        }
        length += sequenceSegment.length();
        if (nTerminus) {
            terminalIndex -= sequenceSegment.length();
        } else {
            terminalIndex += sequenceSegment.length();
        }
        Integer otherMutationIndex = sequenceSegment.getMutationIndex();
        if (otherMutationIndex != null) {
            if (mutationIndex == null) {
                mutationIndex = otherMutationIndex;
                mutation = sequenceSegment.getMutation();
            } else {
                if (mutations == null) {
                    mutations = new HashMap<>();
                    mutations.put(otherMutationIndex, mutation);
                }
            }
        }
        mass += sequenceSegment.getMass();
    }

    /**
     * Appends an amino acid sequence to the terminus of sequencing.
     *
     * @param aminoAcidSequence an amino acid sequence
     */
    public void appendTerminus(AminoAcidSequence aminoAcidSequence) {
        HashMap<Integer, ArrayList<ModificationMatch>> otherModifications = aminoAcidSequence.getModificationMatches();
        if (otherModifications != null) {
            if (modificationMatches == null) {
                modificationMatches = new HashMap<>(otherModifications.size());
            }
            for (int index : otherModifications.keySet()) {
                ArrayList<ModificationMatch> modificationMatchesList = otherModifications.get(index);
                if (modificationMatches.size() > 1) {
                    throw new IllegalArgumentException("Two PTMs found on the same amino acid when mapping tags. Only one supported.");
                }
                modificationMatches.put(index + length, modificationMatchesList.get(0).getTheoreticPtm());
            }
        }
        length += aminoAcidSequence.length();
        if (nTerminus) {
            terminalIndex -= aminoAcidSequence.length();
        } else {
            terminalIndex += aminoAcidSequence.length();
        }
        mass += aminoAcidSequence.getMass();
    }

    /**
     * Appends an amino acid pattern to the terminus of sequencing.
     *
     * @param aminoAcidPattern an amino acid pattern
     */
    public void appendTerminus(AminoAcidPattern aminoAcidPattern) {
        HashMap<Integer, ArrayList<ModificationMatch>> otherModifications = aminoAcidPattern.getModificationMatches();
        if (otherModifications != null) {
            if (modificationMatches == null) {
                modificationMatches = new HashMap<>(otherModifications.size());
            }
            for (int index : otherModifications.keySet()) {
                ArrayList<ModificationMatch> modificationMatchesList = otherModifications.get(index);
                if (modificationMatches.size() > 1) {
                    throw new IllegalArgumentException("Two PTMs found on the same amino acid when mapping tags. Only one supported.");
                }
                modificationMatches.put(index + length, modificationMatchesList.get(0).getTheoreticPtm());
            }
        }
        length += aminoAcidPattern.length();
        if (nTerminus) {
            terminalIndex -= aminoAcidPattern.length();
        } else {
            terminalIndex += aminoAcidPattern.length();
        }
        mass += aminoAcidPattern.getMass();
    }

    /**
     * Appends an amino acid to the terminus of sequencing.
     *
     * @param aminoAcid an amino acid
     */
    public void appendTerminus(AminoAcid aminoAcid) {
        if (nTerminus) {
            terminalIndex--;
        } else {
            terminalIndex++;
        }
        length++;
        mass += aminoAcid.getMonoisotopicMass();
    }

    /**
     * Adds a modification to the terminus of sequencing.
     *
     * @param modification the modification
     */
    public void addModificationTerminus(String modification) {
        PTM ptm = PTMFactory.getInstance().getPTM(modification);
        addModificationTerminus(modification, ptm.getMass());
    }

    /**
     * Adds a modification to the terminus of sequencing.
     *
     * @param modification the name of the modification
     * @param modificationMass the mass of the modification
     */
    public void addModificationTerminus(String modification, double modificationMass) {
        modificationAtTerminus = modification;
        mass += modificationMass;
    }
    
    /**
     * Adds a mutation to the segment.
     * 
     * @param index the index of the mutated amino acid
     * @param aa the mutated amino acid
     */
    public void addMutation(Integer index, Character aa) {
        if (mutationIndex == null) {
            mutationIndex = index;
            mutation = aa;
        } else {
            if (mutations == null) {
                mutations = new HashMap<>(1);
            }
            mutations.put(index, aa);
        }
    }

    /**
     * Returns the number of mutations.
     *
     * @return the number of mutations
     */
    public int getnMutations() {
        if (mutations == null) {
            if (mutationIndex == null) {
                return 0;
            } else {
                return 1;
            }
        }
        return mutations.size();
    }

    /**
     * Returns the sequence of this segment.
     *
     * @param sequence the protein sequence
     *
     * @return the sequence of this segment
     */
    public String getSegmentSequence(String sequence) {
        if (nTerminus) {
            return sequence.substring(terminalIndex + 1, terminalIndex + 1 + length);
        } else {
            return sequence.substring(terminalIndex - length, terminalIndex);
        }
    }
}
