package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * An oxygen atom.
 *
 * @author Marc Vaudel
 */
public class Oxygen extends Atom {

    /**
     * Constructor.
     */
    public Oxygen() {
        isotopeMap = new HashMap<Integer, Double>();
        isotopeMap.put(-4, 12.034405);
        isotopeMap.put(-3, 13.024812);
        isotopeMap.put(-2, 14.00859625);
        isotopeMap.put(-1, 15.0030656);
        isotopeMap.put(0, 15.99491461956);
        isotopeMap.put(1, 16.99913170);
        isotopeMap.put(2, 17.9991610);
        isotopeMap.put(3, 19.003580);
        isotopeMap.put(4, 20.0040767);
        isotopeMap.put(5, 21.008656);
        isotopeMap.put(6, 22.00997);
        isotopeMap.put(7, 23.01569);
        isotopeMap.put(8, 24.02047);
        representativeComposition = new HashMap<Integer, Double>();
        representativeComposition.put(0, 0.99757);
        representativeComposition.put(1, 0.00038);
        representativeComposition.put(2, 0.00205);
        this.name = "Oxygen";
        this.letter = "O";
    }
}
