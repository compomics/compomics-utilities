package com.compomics.util.experiment.identification.protein_inference.fm_index;

import java.util.ArrayList;

/**
 * The cache element.
 *
 * @author Dominik Kopczynski
 */
public class CacheElement {

    Double massFirst;
    String sequence;
    Double massSecond;
    ArrayList<MatrixContent> cachedPrimary;

    /**
     * Constructor.
     *
     * @param massFirst the first mass
     * @param sequence the sequence
     * @param massSecond the second mass
     * @param cachedPrimary cached primary
     */
    public CacheElement(Double massFirst, String sequence, Double massSecond, ArrayList<MatrixContent> cachedPrimary) {
        this.sequence = sequence;
        this.massFirst = massFirst;
        this.massSecond = massSecond;
        this.cachedPrimary = cachedPrimary;
    }
}
