package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * The helium atom.
 *
 * @author Marc Vaudel
 */
public class Helium extends Atom {

    /**
     * Constructor.
     */
    public Helium() {
        monoisotopicMass = 2.015894;
        isotopeMap = new HashMap<Integer, Double>();
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 3.0160293191);
        isotopeMap.put(2, 4.00260325415);
        isotopeMap.put(3, 5.01222);
        isotopeMap.put(4, 6.0188891);
        isotopeMap.put(5, 7.028021);
        isotopeMap.put(6, 8.033922);
        isotopeMap.put(7, 9.04395);
        isotopeMap.put(8, 10.05240);
        representativeComposition = new HashMap<Integer, Double>();
        representativeComposition.put(0, 0.9999);
        representativeComposition.put(1, 0.0001);
        this.name = "Helium";
        this.letter = "He";
    }
}
