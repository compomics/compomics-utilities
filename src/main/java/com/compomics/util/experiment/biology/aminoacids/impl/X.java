package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
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
        name = "X";
        subAminoAcidsWithoutCombination = new char[]{'V'};
        subAminoAcidsWithCombination = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'Y', 'U', 'O', 'V', 'W', 'Z'};
        aminoAcidCombinations = new char[]{'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'Y', 'U', 'O', 'V', 'W'};
        standardGeneticCode = getStandardGeneticCodeForCombination();
    }
    
    @Override
    public double getMonoisotopicMass() {
        return 110.0;
    }

    @Override
    public String[] getStandardGeneticCode() {
        ArrayList<String> uniqueCodes = new ArrayList<>();
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

    @Override
    public double getHydrophobicity() {
        throw new UnsupportedOperationException("Not supported for amino acid combinations.");
    }

    @Override
    public double getHelicity() {
        throw new UnsupportedOperationException("Not supported for amino acid combinations.");
    }

    @Override
    public double getBasicity() {
        throw new UnsupportedOperationException("Not supported for amino acid combinations.");
    }

    @Override
    public double getPI() {
        throw new UnsupportedOperationException("Not supported for amino acid combinations.");
    }

    @Override
    public double getPK1() {
        throw new UnsupportedOperationException("Not supported for amino acid combinations.");
    }

    @Override
    public double getPK2() {
        throw new UnsupportedOperationException("Not supported for amino acid combinations.");
    }

    @Override
    public double getPKa() {
        throw new UnsupportedOperationException("Not supported for amino acid combinations.");
    }

    @Override
    public int getVanDerWaalsVolume() {
        throw new UnsupportedOperationException("Not supported for amino acid combinations.");
    }
}
