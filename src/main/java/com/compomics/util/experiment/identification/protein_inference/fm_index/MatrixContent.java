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
    public int traceback;
    /**
     * Index of the originating entry of a particular cell with the pattern
     * searching matrix.
     */
    public long lastIndex;

    /**
     * Constructor.
     * 
     * @param left the left index
     * @param right the right index
     * @param character the character
     * @param traceback the traceback
     * @param lastIndex the last index
     */
    public MatrixContent(int left, int right, char character, int traceback, long lastIndex) {
        this.left = left;
        this.right = right;
        this.character = character;
        this.traceback = traceback;
        this.lastIndex = lastIndex;
    }
}
