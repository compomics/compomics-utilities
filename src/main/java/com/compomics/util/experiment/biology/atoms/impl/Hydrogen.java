package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The hydrogen atom.
 *
 * @author Marc Vaudel
 */
public class Hydrogen extends Atom {

    /**
     * Constructor.
     */
    public Hydrogen() {
        monoisotopicMass = 1.00782503207;
        isotopeMap = new HashMap<>(2);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 2.0141017778);
        representativeComposition = new HashMap<>(2);
        representativeComposition.put(0, 0.999885);
        representativeComposition.put(1, 0.000115);
        this.name = "Hydrogen";
        this.letter = "H";
    }
}
