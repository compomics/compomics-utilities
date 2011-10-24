package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * SeC (U) (Mascot)
 *
 * @author Harald Barsnes
 */
public class Selenocysteine extends AminoAcid {

    public Selenocysteine() {
        singleLetterCode = "U";
        threeLetterCode = "SeC";
        name = "U_Mascot";
        averageMass = 150.0379;
        monoisotopicMass = 150.95363;
    }
}
