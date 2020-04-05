package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The zinc atom.
 *
 * @author Harald Barsnes
 */
public class Zinc extends Atom {

    /**
     * Constructor.
     */
    public Zinc() {

        monoisotopicMass = 63.9291422;

        isotopeMap = new HashMap<Integer, Double>(7);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 64.9292410);
        isotopeMap.put(2, 65.9260334);
        isotopeMap.put(3, 66.9271273);
        isotopeMap.put(4, 67.9248442);
        isotopeMap.put(5, 68.9265503);
        isotopeMap.put(6, 69.9253193);

        representativeComposition = new HashMap<Integer, Double>(5);
        representativeComposition.put(0, 0.4917);
        representativeComposition.put(2, 0.2773);
        representativeComposition.put(3, 0.0404);
        representativeComposition.put(4, 0.1845);
        representativeComposition.put(6, 0.0061);

        this.name = "Zinc";
        this.letter = "Zn";
    }
}
