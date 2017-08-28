package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;

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
        subAminoAcidsWithoutCombination = new char[]{'I', 'L'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'X'};
        standardGeneticCode = getStandardGeneticCodeForCombination();
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
