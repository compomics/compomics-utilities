package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * The sodium atom.
 *
 * @author Marc Vaudel
 */
public class Sodium extends Atom {

    /**
     * Constructor.
     */
    public Sodium() {
        monoisotopicMass = 22.9897692809;
        isotopeMap = new HashMap<Integer, Double>(20);
        isotopeMap.put(-5, 18.02597);
        isotopeMap.put(-4, 19.013877);
        isotopeMap.put(-3, 20.007351);
        isotopeMap.put(-2, 20.9976552);
        isotopeMap.put(-1, 21.9944364);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 23.99096278);
        isotopeMap.put(2, 24.9899540);
        isotopeMap.put(3, 25.992633);
        isotopeMap.put(4, 26.994077);
        isotopeMap.put(5, 27.998938);
        isotopeMap.put(6, 29.002861);
        isotopeMap.put(7, 30.008976);
        isotopeMap.put(8, 31.01359);
        isotopeMap.put(9, 32.02047);
        isotopeMap.put(10, 33.02672);
        isotopeMap.put(11, 34.03517);
        isotopeMap.put(12, 35.04249);
        isotopeMap.put(13, 36.05148);
        isotopeMap.put(14, 37.05934);
        representativeComposition = new HashMap<Integer, Double>(1);
        representativeComposition.put(0, 1.0);
        this.name = "Sodium";
        this.letter = "Na";
    }
}
