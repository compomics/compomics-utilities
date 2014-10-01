package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * An hydrogen atom.
 *
 * @author Marc Vaudel
 */
public class Hydrogen extends Atom {

    /**
     * Constructor.
     */
    public Hydrogen() {
        monoisotopicMass = 1.00782503207;
        isotopeMap = new HashMap<Integer, Double>();
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 2.0141017778);
        isotopeMap.put(2, 3.0160492777);
        isotopeMap.put(3, 4.02781);
        isotopeMap.put(4, 5.03531);
        isotopeMap.put(5, 6.04494);
        isotopeMap.put(6, 7.05275);
        representativeComposition = new HashMap<Integer, Double>();
        representativeComposition.put(0, 0.999885);
        representativeComposition.put(1, 0.000115);
        this.name = "Hydrogen";
        this.letter = "H";
    }
}
