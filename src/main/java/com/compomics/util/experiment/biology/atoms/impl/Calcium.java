package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The calcium atom.
 *
 * @author Harald Barsnes
 */
public class Calcium extends Atom {

    /**
     * Constructor.
     */
    public Calcium() {

        monoisotopicMass = 39.962590866;

        isotopeMap = new HashMap<Integer, Double>(9);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 40.96227792);
        isotopeMap.put(2, 41.95861783);
        isotopeMap.put(3, 42.95876643);
        isotopeMap.put(4, 43.9554815);
        isotopeMap.put(5, 44.9561863);
        isotopeMap.put(6, 45.9536880);
        isotopeMap.put(7, 46.9545414);
        isotopeMap.put(8, 47.95252290);

        representativeComposition = new HashMap<Integer, Double>(6);
        representativeComposition.put(0, 0.96941);
        representativeComposition.put(2, 0.00647);
        representativeComposition.put(3, 0.00135);
        representativeComposition.put(4, 0.02086);
        representativeComposition.put(6, 0.00135);
        representativeComposition.put(8, 0.00004);

        this.name = "Calcium";
        this.letter = "Ca";
    }
}
