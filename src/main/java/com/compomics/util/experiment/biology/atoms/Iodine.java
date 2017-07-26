package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * Iodine.
 *
 * @author Marc Vaudel
 */
public class Iodine extends Atom {

    /**
     * Constructor.
     */
    public Iodine() {
        monoisotopicMass = 126.904473;
        isotopeMap = new HashMap<>(1);
        isotopeMap.put(0, monoisotopicMass);
        representativeComposition = new HashMap<>(1);
        representativeComposition.put(0, 1.0);

        this.name = "Iodine";
        this.letter = "I";
    }
}
