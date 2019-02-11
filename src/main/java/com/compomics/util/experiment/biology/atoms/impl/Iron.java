package com.compomics.util.experiment.biology.atoms.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import java.util.HashMap;

/**
 * The iron atom.
 *
 * @author Harald Barsnes
 */
public class Iron extends Atom {

    /**
     * Constructor.
     */
    public Iron() {
        monoisotopicMass = 55.9349363;
        isotopeMap = new HashMap<Integer, Double>(13);
        isotopeMap.put(-11, 45.01458);
        isotopeMap.put(-10, 46.00081);
        isotopeMap.put(-9, 46.99289);
        isotopeMap.put(-8, 47.98050);
        isotopeMap.put(-7, 48.97361);
        isotopeMap.put(-6, 49.96299);
        isotopeMap.put(-5, 50.956820);
        isotopeMap.put(-4, 51.948114);
        isotopeMap.put(-3, 52.9453079);
        isotopeMap.put(-2, 53.9396090);
        isotopeMap.put(-1, 54.9382934);
        isotopeMap.put(0, monoisotopicMass);
        isotopeMap.put(1, 56.9353928);
        isotopeMap.put(2, 57.9332744);
        isotopeMap.put(3, 58.9348755);
        isotopeMap.put(4, 59.934072);
        isotopeMap.put(5, 60.936745);
        isotopeMap.put(6, 61.936767);
        isotopeMap.put(7, 62.94037);
        isotopeMap.put(8, 63.9412);
        isotopeMap.put(9, 64.94538);
        isotopeMap.put(10, 65.94678);
        isotopeMap.put(11, 66.95095);
        isotopeMap.put(12, 67.95370);
        isotopeMap.put(13, 68.95878);
        isotopeMap.put(14, 69.96146);
        isotopeMap.put(15, 70.96672);
        isotopeMap.put(16, 71.96962);
        representativeComposition = new HashMap<Integer, Double>(3);
        representativeComposition.put(-2, 0.05845);
        representativeComposition.put(0, 0.91754);
        representativeComposition.put(1, 0.02119);
        representativeComposition.put(2, 0.00282);
        this.name = "Ion";
        this.letter = "Fe";
    }
}
