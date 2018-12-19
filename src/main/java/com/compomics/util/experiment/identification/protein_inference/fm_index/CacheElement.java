/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

import java.util.ArrayList;

/**
 *
 * @author dominik.kopczynski
 */
public class CacheElement {
    Double massFirst;
    String sequence;
    Double massSecond;
    ArrayList<MatrixContent> cachedPrimary;

    /**
     * Constructor
     *
     * @param massFirst
     * @param sequence
     * @param massSecond
     * @param cachedPrimary
     */
    public CacheElement(Double massFirst, String sequence, Double massSecond, ArrayList<MatrixContent> cachedPrimary) {
        this.sequence = sequence;
        this.massFirst = massFirst;
        this.massSecond = massSecond;
        this.cachedPrimary = cachedPrimary;
    }
}
