package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The Cobalt atom.
 *
 * @author Harald Barsnes
 */
public class Cobalt extends Atom {

    /**
     * Constructor.
     */
    public Cobalt() {
        monoisotopicMass = 58.9331950;
        isotopeMap = new HashMap<>(1);
        isotopeMap.put(0, monoisotopicMass);
        representativeComposition = new HashMap<>(1);
        representativeComposition.put(0, 1.0);
        this.name = "Cobalt";
        this.letter = "Co";
    }
}