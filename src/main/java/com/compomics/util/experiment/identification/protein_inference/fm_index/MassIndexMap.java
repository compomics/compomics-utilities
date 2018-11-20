/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 *
 * @author dominik.kopczynski
 */

public class MassIndexMap extends ExperimentObject {

    public double mass;
    public int[] indexes;

    /**
     * Empty default constructor
     */
    public MassIndexMap() {}

    public MassIndexMap(double mass, int[] indexes) {
        this.mass = mass;
        this.indexes = indexes;
    }
}
