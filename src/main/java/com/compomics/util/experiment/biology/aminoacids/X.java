package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Unknown amino acid (Mascot)
 *
 * @author Harald Barsnes
 */
public class X extends AminoAcid {

    public X() {
        singleLetterCode = "X";
        threeLetterCode = "Xaa";
        name = "Unknown_Mascot";
        averageMass = 110; // @TODO: is this the correct mass to use? 118 is the average...
        monoisotopicMass = 110; // @TODO: is this the correct mass to use? 118 is the average...
    }
}
