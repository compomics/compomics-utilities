package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * The sulfur atom.
 *
 * @author Marc Vaudel
 */
public class Sulfur extends Atom {

    /**
     * Constructor.
     */
    public Sulfur() {
        monoisotopicMass = 31.97207100;
        isotopeMap = new HashMap<Integer, Double>();
        isotopeMap.put(-6, 26.02788);
        isotopeMap.put(-5, 27.01883);
        isotopeMap.put(-4, 28.00437);
        isotopeMap.put(-3, 28.99661);
        isotopeMap.put(-2, 29.984903);
        isotopeMap.put(-1, 30.9795547);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 32.97145876);
        isotopeMap.put(2, 33.96786690);
        isotopeMap.put(3, 34.96903216);
        isotopeMap.put(4, 35.96708076);
        isotopeMap.put(5, 36.97112557);
        isotopeMap.put(6, 37.971163);
        isotopeMap.put(7, 38.97513);
        isotopeMap.put(8, 39.97545);
        isotopeMap.put(9, 40.97958);
        isotopeMap.put(10, 41.98102);
        isotopeMap.put(11, 42.98715);
        isotopeMap.put(12, 43.99021);
        isotopeMap.put(13, 44.99651);
        isotopeMap.put(14, 47.00859);
        isotopeMap.put(15, 48.01417);
        isotopeMap.put(16, 49.02362);
        representativeComposition = new HashMap<Integer, Double>();
        representativeComposition.put(0, 0.9493);
        representativeComposition.put(1, 0.0076);
        representativeComposition.put(2, 0.0429);
        representativeComposition.put(4, 0.0002);
        this.name = "Sulfur";
        this.letter = "S";
    }
}
