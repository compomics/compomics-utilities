package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The Chlorine atom.
 *
 * @author Harald Barsnes
 */
public class Chlorine extends Atom {

    /**
     * Constructor.
     */
    public Chlorine() {
        monoisotopicMass = 34.96885269;
        isotopeMap = new HashMap<>(3);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 35.96830682);
        isotopeMap.put(2, 36.96590258);
        representativeComposition = new HashMap<>(2);
        representativeComposition.put(0, 0.76);
        representativeComposition.put(2, 0.24);
        this.name = "Chlorine";
        this.letter = "Cl";
    }
}