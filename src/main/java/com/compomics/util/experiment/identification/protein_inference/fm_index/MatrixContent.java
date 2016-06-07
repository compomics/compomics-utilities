package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;

/**
 * Element for the matrix necessary in pattern search of the FMIndex.
 *
 * @author Dominik Kopczynski
 */
public class MatrixContent {

    /**
     * Left index.
     */
    public int left;
    /**
     * Right index.
     */
    public int right;
    /**
     * Character which was chosen.
     */
    public int character;
    /**
     * Index of the originating entry of a particular cell with the pattern
     * searching matrix.
     */
    public MatrixContent previousContent;
    /**
     * Current mass.
     */
    public double mass;
    /**
     * Current peptide sequence.
     */
    public String peptideSequence;
    /**
     * Current combination sequence length.
     */
    public int combinationLength;
    /**
     * Current peptide sequence length.
     */
    public int length;
    /**
     * Current number of contained X's.
     */
    public int numX;
    /**
     * Index to the modifications list.
     */
    public ModificationMatch modification;
    /**
     * List of all modifications.
     */
    public ArrayList<ModificationMatch> modifications;
    /**
     * List of all modifications.
     */
    public int modificationPos;

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param mass current mass
     * @param peptideSequence intermediate peptide sequence
     * @param combinationLength current combination length
     * @param length current peptide length
     * @param numX number of current X amino acids
     * @param modification index to modification list
     * @param modifications intermediate list of modifications
     * @param modifictationPos index to modification list for ptm
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, double mass, String peptideSequence,
            int combinationLength, int length, int numX, ModificationMatch modification, ArrayList<ModificationMatch> modifications, int modifictationPos) {

        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = peptideSequence;
        this.combinationLength = combinationLength;
        this.length = length;
        this.numX = numX;
        this.modification = modification;
        this.modifications = modifications;
        this.modificationPos = modifictationPos;
    }

    /**
     * Copy constructor.
     *
     * @param foreign foreign matrix content instance
     */
    public MatrixContent(MatrixContent foreign) {
        this.left = foreign.left;
        this.right = foreign.right;
        this.character = foreign.character;
        this.previousContent = foreign.previousContent;
        this.mass = foreign.mass;
        this.peptideSequence = foreign.peptideSequence;
        this.combinationLength = foreign.combinationLength;
        this.length = foreign.length;
        this.numX = foreign.numX;
        this.modification = foreign.modification;
        this.modifications = foreign.modifications;
        this.modificationPos = foreign.modificationPos;
    }

    /**
     * Constructor.
     *
     * @param left left index boundary
     * @param right right index boundary
     * @param character current character stored
     * @param previousContent previous matrix content
     * @param numX number of current X amino acids
     */
    public MatrixContent(int left, int right, int character, MatrixContent previousContent, int numX) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = -1;
        this.peptideSequence = null;
        this.combinationLength = 0;
        this.length = 0;
        this.numX = numX;
        this.modification = null;
        this.modifications = null;
        this.modificationPos = -1;
    }
}
