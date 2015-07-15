package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.Atom;
import java.util.HashMap;

/**
 * Selenium.
 * 
 * @author Marc Vaudel
 */
public class Selenium extends Atom {

    /**
     * Constructor.
     */
    public Selenium() {
        monoisotopicMass = 79.9165213;
        isotopeMap = new HashMap<Integer, Double>();
        isotopeMap.put(-15, 64.96466);
        isotopeMap.put(-14, 65.95521);
        isotopeMap.put(-13, 66.95009);
        isotopeMap.put(-12, 67.94180);
        isotopeMap.put(-11, 68.93956);
        isotopeMap.put(-10, 69.93339);
        isotopeMap.put(-9, 70.93224);
        isotopeMap.put(-8, 71.927112);
        isotopeMap.put(-7, 72.926765);
        isotopeMap.put(-6, 73.9224764);
        isotopeMap.put(-5, 74.9225234);
        isotopeMap.put(-4, 75.9192136);
        isotopeMap.put(-3, 76.9199140);
        isotopeMap.put(-2, 77.9173091);
        isotopeMap.put(-1, 78.9184991);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 80.9179925);
        isotopeMap.put(2, 81.9166994);
        isotopeMap.put(3, 82.919118);
        isotopeMap.put(4, 83.918462);
        isotopeMap.put(5, 84.92225);
        isotopeMap.put(6, 85.924272);
        isotopeMap.put(7, 86.92852);
        isotopeMap.put(8, 87.93142);
        isotopeMap.put(9, 88.93645);
        isotopeMap.put(10, 89.93996);
        isotopeMap.put(11, 90.94596);
        isotopeMap.put(12, 91.94992);
        isotopeMap.put(13, 92.95629);
        isotopeMap.put(14, 93.96049);
        representativeComposition = new HashMap<Integer, Double>();
        representativeComposition.put(-6, 0.0089);
        representativeComposition.put(-4, 0.0937);
        representativeComposition.put(-3, 0.0763);
        representativeComposition.put(-2, 0.2377);
        representativeComposition.put(0, 0.4961);
        representativeComposition.put(2, 0.0873);
        
        this.name = "Selenium";
        this.letter = "Se";
    }
}
