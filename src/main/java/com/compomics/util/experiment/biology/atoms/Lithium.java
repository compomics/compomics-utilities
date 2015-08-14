package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * The Lithium atom.
 *
 * @author Marc Vaudel
 */
public class Lithium extends Atom {

    /**
     * Constructor.
     */
    public Lithium() {
        monoisotopicMass = 3.030775;
        isotopeMap = new HashMap<Integer, Double>(10);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 4.02719);
        isotopeMap.put(2, 5.01254);
        isotopeMap.put(3, 6.015122795);
        isotopeMap.put(4, 7.01600455);
        isotopeMap.put(5, 8.02248736);
        isotopeMap.put(6, 9.0267895);
        isotopeMap.put(7, 10.035481);
        isotopeMap.put(8, 11.043798);
        isotopeMap.put(9, 12.05378);
        representativeComposition = new HashMap<Integer, Double>(1);
        representativeComposition.put(0, 1.0);
        this.name = "Lithium";
        this.letter = "Li";
    }
}