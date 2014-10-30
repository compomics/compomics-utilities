package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Segment of sequence indexed on a protein sequence
 *
 * @author Marc
 */
public class SequenceSegment {

    /**
     * The amino acid sequence of this fragment
     */
    private AminoAcidSequence aminoAcidSequence;

    /**
     * the index on the protein
     */
    private int indexOnProtein;

    /**
     * the mass of the segment
     */
    private double mass;

    /**
     * Constructor.
     * 
     * @param indexOnProtein index on the protein sequence
     * @param aminoAcidSequence the amino acid sequence of this fragment
     */
    public SequenceSegment(int indexOnProtein, AminoAcidSequence aminoAcidSequence) {
        if (aminoAcidSequence == null) {
            this.aminoAcidSequence = new AminoAcidSequence();
            mass = 0.0;
        } else {
            this.aminoAcidSequence = aminoAcidSequence;
            mass = aminoAcidSequence.getMass();
        }
        this.indexOnProtein = indexOnProtein;
    }

    /**
     * Constructor.
     * 
     * @param indexOnProtein index on the protein sequence
     */
    public SequenceSegment(int indexOnProtein) {
        this(indexOnProtein, null);
    }

    /**
     * Constructor.
     * 
     * @param sequenceSegment another sequence segment
     * @param indexOnProtein the index on the protein
     */
    public SequenceSegment(SequenceSegment sequenceSegment, Integer indexOnProtein) {
        aminoAcidSequence = new AminoAcidSequence(sequenceSegment.getAminoAcidSequence());
        if (indexOnProtein == null) {
            this.indexOnProtein = sequenceSegment.getIndexOnProtein();
        } else {
            this.indexOnProtein = indexOnProtein;
        }
        mass = sequenceSegment.getMass();
    }

    /**
     * Constructor.
     * 
     * @param sequenceSegment another sequence segment
     */
    public SequenceSegment(SequenceSegment sequenceSegment) {
        this(sequenceSegment, null);
    }

    /**
     * Appends a sequence segment to the N-terminus.
     * 
     * @param sequenceSegment a sequence segment
     */
    public void appendNTerminus(SequenceSegment sequenceSegment) {
        aminoAcidSequence.appendNTerm(sequenceSegment.getAminoAcidSequence());
        mass += sequenceSegment.getMass();
    }

    /**
     * Appends a sequence segment to the C-terminus.
     * 
     * @param sequenceSegment a sequence segment
     */
    public void appendCTerminus(SequenceSegment sequenceSegment) {
        aminoAcidSequence.appendCTerm(sequenceSegment.getAminoAcidSequence());
        mass += sequenceSegment.getMass();
    }

    /**
     * Appends an amino acid to the N-terminus.
     * 
     * @param aminoAcid an amino acid
     */
    public void appendNTerminus(AminoAcid aminoAcid) {
        aminoAcidSequence.appendNTerm(aminoAcid.singleLetterCode);
        mass += aminoAcid.monoisotopicMass;
    }

    /**
     * Appends an amino acid to the C-terminus.
     * 
     * @param aminoAcid an amino acid
     */
    public void appendCTerminus(AminoAcid aminoAcid) {
        aminoAcidSequence.appendCTerm(aminoAcid.singleLetterCode);
        mass += aminoAcid.monoisotopicMass;
    }

    /**
     * Adds a modification to the N-terminus.
     * 
     * @param modificationMatch a mosidication match
     */
    public void addModificationNTerminus(ModificationMatch modificationMatch) {
        PTM ptm = PTMFactory.getInstance().getPTM(modificationMatch.getTheoreticPtm());
        addModificationNTerminus(modificationMatch, ptm.getMass());
    }

    /**
     * Adds a modification to the N-terminus.
     * 
     * @param modificationMatch a mosidication match
     * @param modificationMass the mass of the modification
     */
    public void addModificationNTerminus(ModificationMatch modificationMatch, double modificationMass) {
        aminoAcidSequence.addModificationMatch(1, modificationMatch);
        mass += modificationMass;
    }

    /**
     * Adds a modification to the C-terminus.
     * 
     * @param modificationMatch a mosidication match
     */
    public void addModificationCTerminus(ModificationMatch modificationMatch) {
        PTM ptm = PTMFactory.getInstance().getPTM(modificationMatch.getTheoreticPtm());
        addModificationCTerminus(modificationMatch, ptm.getMass());
    }

    /**
     * Adds a modification to the C-terminus.
     * 
     * @param modificationMatch a mosidication match
     * @param modificationMass the mass of the modification
     */
    public void addModificationCTerminus(ModificationMatch modificationMatch, double modificationMass) {
        aminoAcidSequence.addModificationMatch(aminoAcidSequence.length(), modificationMatch);
        mass += modificationMass;
    }

    /**
     * Adds a mass to the mass of the segment.
     * 
     * @param mass the mass to add
     */
    public void addMass(Double mass) {
        this.mass += mass;
    }

    /**
     * Returns the amino acid sequence of the segment.
     * 
     * @return the amino acid sequence of the segment
     */
    public AminoAcidSequence getAminoAcidSequence() {
        return aminoAcidSequence;
    }

    /**
     * Returns the index on the protein as set.
     * 
     * @return the index on the protein as set
     */
    public int getIndexOnProtein() {
        return indexOnProtein;
    }

    /**
     * Sets the index on the protein.
     * 
     * @param indexOnProtein the index on the protein
     */
    public void setIndexOnProtein(int indexOnProtein) {
        this.indexOnProtein = indexOnProtein;
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
     * Returns the length of the segment.
     * 
     * @return the length of the segment
     */
    public int length() {
        return aminoAcidSequence.length();
    }
    
    /**
     * Returns the sequence of the segment.
     * 
     * @return the sequence of the segment
     */
    public String getSequence() {
        return aminoAcidSequence.asSequence();
    }
    
    /**
     * Returns the modifications on this segment.
     * 
     * @return the modifications on this segment
     */
    public HashMap<Integer, ArrayList<ModificationMatch>> getModificationMatches() {
        return aminoAcidSequence.getModificationMatches();
    }
}
