package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;
import java.util.ArrayList;

/**
 * Isoleucine or Leucine.
 *
 * @author Harald Barsnes
 */
public class J extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 1963175809911841522L;

    /**
     * Constructor.
     */
    public J() {
        singleLetterCode = "J";
        threeLetterCode = "I/L";
        name = "Isoleucine or Leucine";
        averageMass = 113.15980;
        monoisotopicMass = 113.08407;
    }

    @Override
    public char[] getSubAminoAcids() {
        return new char[]{'I', 'L'};
    }

    @Override
    public char[] getCombinations() {
        return new char[]{'X'};
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
