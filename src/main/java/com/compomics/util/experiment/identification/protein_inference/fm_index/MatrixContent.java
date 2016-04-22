/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

/**
 *
 * @author dominik.kopczynski
 */
public class MatrixContent {
    public int L;
    public int R;
    public char character;
    public int traceback;
    public long lastIndex;
    
    public MatrixContent(int _L, int _R, char _character, int _traceback, long _lastIndex){
        L = _L;
        R = _R;
        character = _character;
        traceback = _traceback;
        lastIndex = _lastIndex;
    }
}
