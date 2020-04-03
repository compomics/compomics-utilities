package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The magnesium atom.
 *
 * @author Harald Barsnes
 */
public class Magnesium extends Atom {

    /**
     * Constructor.
     */
    public Magnesium() {

        monoisotopicMass = 23.985041697;

        isotopeMap = new HashMap<Integer, Double>(3);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 24.98583696);
        isotopeMap.put(2, 25.98259297);

        representativeComposition = new HashMap<Integer, Double>(3);
        representativeComposition.put(0, 0.7899);
        representativeComposition.put(1, 0.1000);
        representativeComposition.put(2, 0.1101);

        this.name = "Magnesium";
        this.letter = "Mg";
    }
}
