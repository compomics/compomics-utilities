package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * A phosphorus atom.
 *
 * @author Marc Vaudel
 */
public class Phosphorus extends Atom {

    /**
     * Constructor.
     */
    public Phosphorus() {
        monoisotopicMass = 30.97376163;
        isotopeMap = new HashMap<Integer, Double>();
        isotopeMap.put(-7, 24.03435);
        isotopeMap.put(-6, 25.02026);
        isotopeMap.put(-5, 26.01178);
        isotopeMap.put(-4, 26.999230);
        isotopeMap.put(-3, 27.992315);
        isotopeMap.put(-2, 28.9818006);
        isotopeMap.put(-1, 29.9783138);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 31.97390727);
        isotopeMap.put(2, 32.9717255);
        isotopeMap.put(3, 33.973636);
        isotopeMap.put(4, 34.9733141);
        isotopeMap.put(5, 35.978260);
        isotopeMap.put(6, 36.97961);
        isotopeMap.put(7, 37.98416);
        isotopeMap.put(8, 38.98618);
        isotopeMap.put(9, 39.99130);
        isotopeMap.put(10, 40.99434);
        isotopeMap.put(11, 42.00101);
        isotopeMap.put(12, 43.00619);
        isotopeMap.put(13, 44.01299);
        isotopeMap.put(14, 45.01922);
        isotopeMap.put(15, 46.02738);
        representativeComposition = new HashMap<Integer, Double>();
        representativeComposition.put(0, 1.0);
        this.name = "Phosphorus";
        this.letter = "P";
    }
}
