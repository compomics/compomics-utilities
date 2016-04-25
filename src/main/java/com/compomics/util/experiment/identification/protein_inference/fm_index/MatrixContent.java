package com.compomics.util.experiment.identification.protein_inference.fm_index;

/**
 *
 * Element for the matrix necessary in pattern search of the FMIndex.
 * 
 * @author Dominik Kopczynski
 */
public class MatrixContent {
    /**
     * Left Index.
     */
    public int L;
    
    /**
     * Right Index.
     */
    public int R;
    
    /**
     * Character which was chosen.
     */
    public char character;
    
    /**
     * Indicating if the traceback should go a level up.
     */
    public int traceback;
    
    /**
     * Index of the originating entry of a particular cell with the pattern searching matrix.
     */
    public long lastIndex;
    
    public MatrixContent(int _L, int _R, char _character, int _traceback, long _lastIndex){
        L = _L;
        R = _R;
        character = _character;
        traceback = _traceback;
        lastIndex = _lastIndex;
    }
}
