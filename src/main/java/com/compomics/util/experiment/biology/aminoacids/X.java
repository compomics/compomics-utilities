package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import java.util.ArrayList;

/**
 * Unknown amino acid (Mascot).
 *
 * @author Harald Barsnes
 */
public class X extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2618109862080991929L;

    /**
     * Constructor.
     */
    public X() {
        singleLetterCode = "X";
        threeLetterCode = "Xaa";
        name = "Unknown_Mascot";
        averageMass = 110; // @TODO: is this the correct mass to use? 118 is the average...
        monoisotopicMass = 110; // @TODO: is this the correct mass to use? 118 is the average...
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'Y', 'U', 'O', 'V', 'W', 'Z'};
    }

    @Override
    public char[] getCombinations() {
        return new char[0];
    }

    @Override
    public String[] getStandardGeneticCode() {
        ArrayList<String> uniqueCodes = new ArrayList<String>();
        for (char aa : getSubAminoAcids()) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            for (String code : aminoAcid.getStandardGeneticCode()) {
                if (!uniqueCodes.contains(code)) {
                    uniqueCodes.add(code);
                }
            }
        }
        return (String[]) uniqueCodes.toArray();
    }

    @Override
    public boolean iscombination() {
        return true;
    }
}