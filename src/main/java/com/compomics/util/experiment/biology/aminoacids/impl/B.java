package com.compomics.util.experiment.biology.aminoacids.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import java.util.ArrayList;
import java.util.List;

/**
 * Asn or Asp: Asx (Mascot).
 *
 * @author Harald Barsnes
 */
public class B extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -584166511231722270L;

    /**
     * Constructor.
     */
    public B() {
        singleLetterCode = "B";
        threeLetterCode = "Asx";
        name = "Asparagine or Aspartic Acid";
        averageMass = 114.595;
        monoisotopicMass = 114.534935;
        subAminoAcidsWithoutCombination = new char[]{'N', 'D'};
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
