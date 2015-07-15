package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * The nitrogen atom.
 *
 * @author Marc Vaudel
 */
public class Nitrogen extends Atom {

    /**
     * Constructor.
     */
    public Nitrogen() {
        monoisotopicMass = 14.0030740048;
        isotopeMap = new HashMap<Integer, Double>();
        isotopeMap.put(-4, 10.04165);
        isotopeMap.put(-3, 11.02609);
        isotopeMap.put(-2, 12.0186132);
        isotopeMap.put(-1, 13.00573861);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 15.0001088982);
        isotopeMap.put(2, 16.0061017);
        isotopeMap.put(3, 17.008450);
        isotopeMap.put(4, 18.014079);
        isotopeMap.put(5, 19.017029);
        isotopeMap.put(6, 20.02337);
        isotopeMap.put(7, 21.02711);
        isotopeMap.put(8, 22.03439);
        isotopeMap.put(9, 23.04122);
        isotopeMap.put(10, 24.05104);
        isotopeMap.put(11, 25.06066);
        representativeComposition = new HashMap<Integer, Double>();
        representativeComposition.put(0, 0.99636);
        representativeComposition.put(1, 0.00364);
        this.name = "Nitrogen";
        this.letter = "N";
    }
}
