package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * Carbon.
 *
 * @author Marc Vaudel
 */
public class Carbon extends Atom {

    /**
     * Constructor
     */
    public Carbon() {
        monoisotopicMass = 12.0;
        isotopeMap = new HashMap<>(15);
        isotopeMap.put(-4, 8.037675);
        isotopeMap.put(-3, 9.0310367);
        isotopeMap.put(-2, 10.0168532);
        isotopeMap.put(-1, 11.0114336);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 13.0033548378);
        isotopeMap.put(2, 14.003241989);
        isotopeMap.put(3, 15.0105993);
        isotopeMap.put(4, 16.014701);
        isotopeMap.put(5, 17.022586);
        isotopeMap.put(6, 18.02676);
        isotopeMap.put(7, 19.03481);
        isotopeMap.put(8, 20.04032);
        isotopeMap.put(9, 21.04934);
        isotopeMap.put(10, 22.05720);
        representativeComposition = new HashMap<>(2);
        representativeComposition.put(0, 0.9893);
        representativeComposition.put(1, 0.0107);
        
        this.name = "Carbon";
        this.letter = "C";
    }
}
