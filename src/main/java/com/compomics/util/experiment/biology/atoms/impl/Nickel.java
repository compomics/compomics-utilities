package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The Nickel atom.
 *
 * @author Harald Barsnes
 */
public class Nickel extends Atom {

    /**
     * Constructor.
     */
    public Nickel() {
        monoisotopicMass = 57.9353429;
        isotopeMap = new HashMap<>(7);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 58.9343467);
        isotopeMap.put(2, 59.9307864);
        isotopeMap.put(3, 60.9310560);
        isotopeMap.put(4, 61.9283451);
        isotopeMap.put(5, 62.9296694);
        isotopeMap.put(6, 63.9279660);
        representativeComposition = new HashMap<>(5);
        representativeComposition.put(0, 0.68);
        representativeComposition.put(2, 0.26);
        representativeComposition.put(3, 0.01);
        representativeComposition.put(4, 0.036);
        representativeComposition.put(6, 0.009);
        this.name = "Nickel";
        this.letter = "Ni";
    }
}