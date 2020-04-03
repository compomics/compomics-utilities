package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The potassium atom.
 *
 * @author Harald Barsnes
 */
public class Potassium extends Atom {

    /**
     * Constructor.
     */
    public Potassium() {

        monoisotopicMass = 38.963706487;

        isotopeMap = new HashMap<Integer, Double>(3);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 56.9353928);
        isotopeMap.put(2, 57.9332744);

        representativeComposition = new HashMap<Integer, Double>(3);
        representativeComposition.put(0, 0.932581);
        representativeComposition.put(1, 0.000117);
        representativeComposition.put(2, 0.067302);

        this.name = "Potassium";
        this.letter = "K";
    }
}
