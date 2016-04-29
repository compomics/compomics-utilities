package com.compomics.util.experiment.identification.protein_inference.fm_index;

/**
 * Element for the matrix necessary in pattern search of the FMIndex.
 *
 * @author Dominik Kopczynski
 */
public class MatrixContent {

    /**
     * Left Index.
     */
    public int left;
    
    /**
     * Right Index.
     */
    public int right;
    
    /**
     * Character which was chosen.
     */
    public char character;
    
    /**
     * Indicating if the traceback should go a level up.
     */
    public int tracebackRow;
    
    /**
     * Indicating if the traceback should go an amino acid back.
     */
    public int tracebackCol;
    
    /**
     * Index of the originating entry of a particular cell with the pattern
     * searching matrix.
     */
    public long lastIndex;
    
    /**
     * Current mass.
     */
    public double mass;
    
    /**
     * Current peptide sequence.
     */
    public String peptideSequence;
    
    /**
     * Constructor.
     * 
     * @param left
     * @param right
     * @param character
     * @param tracebackRow
     * @param tracebackCol
     * @param lastIndex
     * @param mass
     * @param peptideSequence 
     */
    public MatrixContent(int left, int right, char character, int tracebackRow, int tracebackCol, long lastIndex, double mass, String peptideSequence) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.tracebackRow = tracebackRow;
        this.tracebackCol = tracebackCol;
        this.lastIndex = lastIndex;
        this.mass = mass;
        this.peptideSequence = peptideSequence;
    }
    
    /**
     * Constructor.
     *
     * @param left the left index
     * @param right the right index
     * @param character the character
     * @param lastIndex the last index
     */
    public MatrixContent(int left, int right, char character, long lastIndex) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.tracebackRow = 0;
        this.tracebackCol = 0;
        this.lastIndex = lastIndex;
        this.mass = 0;
        this.peptideSequence = null;
    }
}
