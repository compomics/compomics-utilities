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
     * Left opposite Index.
     */
    public int leftOpposite;
    
    /**
     * Right opposite Index.
     */
    public int rightOpposite;
    
    /**
     * Character which was chosen.
     */
    public char character;
    
    
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
     * Constructor.
     * 
     * @param left
     * @param right
     * @param character
     * @param previousContent
     * @param mass
     * @param peptideSequence 
     */
    public MatrixContent(int left, int right, int leftOpposite, int rightOpposite, char character, MatrixContent previousContent, double mass, String peptideSequence) {
        this.left = left;
        this.right = right;
        this.leftOpposite = leftOpposite;
        this.rightOpposite = rightOpposite;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = mass;
        this.peptideSequence = peptideSequence;
    }
    
    /**
     * Copy constructor
     * @param foreign 
     */
    public MatrixContent(MatrixContent foreign){
        this.left = foreign.left;
        this.right = foreign.right;
        this.leftOpposite = foreign.leftOpposite;
        this.rightOpposite = foreign.rightOpposite;
        this.character = foreign.character;
        this.previousContent = foreign.previousContent;
        this.mass = foreign.mass;
        this.peptideSequence = foreign.peptideSequence;
    }
    
    /**
     * Constructor.
     *
     * @param left the left index
     * @param right the right index
     * @param character the character
     * @param previousContent the last index
     */
    public MatrixContent(int left, int right, char character, MatrixContent previousContent) {
        this.left = left;
        this.right = right;
        this.leftOpposite = -1;
        this.rightOpposite = -1;
        this.character = character;
        this.previousContent = previousContent;
        this.mass = -1;
        this.peptideSequence = null;
    }
}
