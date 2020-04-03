package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The copper atom.
 *
 * @author Harald Barsnes
 */
public class Copper extends Atom {

    /**
     * Constructor.
     */
    public Copper() {

        monoisotopicMass = 62.9295975;

        isotopeMap = new HashMap<Integer, Double>(3);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 63.9297642);
        isotopeMap.put(2, 64.9277895);

        representativeComposition = new HashMap<Integer, Double>(2);
        representativeComposition.put(0, 0.6915);
        representativeComposition.put(2, 0.3085);

        this.name = "Copper";
        this.letter = "Cu";
    }
}
