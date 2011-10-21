package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Glu or Gln: Glx (Mascot)
 *
 * @author Harald Barsnes
 */
public class Z extends AminoAcid {

    public Z() {
        singleLetterCode = "Z";
        threeLetterCode = "Glx";
        name = "Z_Mascot";
        averageMass = 128.6216;
        monoisotopicMass = 128.5505855;
    }
}
