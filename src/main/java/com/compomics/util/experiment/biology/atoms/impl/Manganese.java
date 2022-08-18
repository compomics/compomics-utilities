package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The Manganese atom.
 *
 * @author Harald Barsnes
 */
public class Manganese extends Atom {

    /**
     * Constructor.
     */
    public Manganese() {
        monoisotopicMass = 54.9380451;
        isotopeMap = new HashMap<>(1);
        isotopeMap.put(0, monoisotopicMass);
        representativeComposition = new HashMap<>(1);
        representativeComposition.put(0, 1.0);
        this.name = "Manganese";
        this.letter = "Mn";
    }
}