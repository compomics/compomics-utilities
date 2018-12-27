package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * The mass index map.
 *
 * @author Dominik Kopczynski
 */
public class MassIndexMap extends ExperimentObject {

    public double mass;
    public int[] indexes;

    /**
     * Empty default constructor
     */
    public MassIndexMap() {
    }

    public MassIndexMap(double mass, int[] indexes) {
        this.mass = mass;
        this.indexes = indexes;
    }
}
