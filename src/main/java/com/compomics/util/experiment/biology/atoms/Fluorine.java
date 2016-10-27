package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * Fluorine
 *
 * @author Marc Vaudel
 */
public class Fluorine extends Atom {

    /**
     * Constructor
     */
    public Fluorine() {
        monoisotopicMass = 18.99840322;
        isotopeMap = new HashMap<Integer, Double>(18);
        isotopeMap.put(-5, 14.03506);
        isotopeMap.put(-4, 15.01801);
        isotopeMap.put(-3, 16.011466);
        isotopeMap.put(-2, 17.00209524);
        isotopeMap.put(-1, 18.0009380);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 19.99998132);
        isotopeMap.put(2, 20.9999490);
        isotopeMap.put(3, 22.002999);
        isotopeMap.put(4, 23.00357);
        isotopeMap.put(5, 24.00812);
        isotopeMap.put(6, 25.01210);
        isotopeMap.put(7, 26.01962);
        isotopeMap.put(8, 27.02676);
        isotopeMap.put(9, 28.03567);
        isotopeMap.put(10, 29.04326);
        isotopeMap.put(10, 30.05250);
        isotopeMap.put(10, 31.06043);
        representativeComposition = new HashMap<Integer, Double>(2);
        representativeComposition.put(0, 1.0);
        
        this.name = "Fluorine";
        this.letter = "F";
    }
}
