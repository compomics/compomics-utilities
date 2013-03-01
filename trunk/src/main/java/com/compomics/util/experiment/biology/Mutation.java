/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.biology;

/**
 * This class represents a mutation of one amino acid to another from the 20-letter amino acid alphabet.
 * 
 * @author Thilo Muth
 */
public class Mutation {
    

    /**
     * The original amino acid.
     */
    private AminoAcid origin;

    /**
     * The mutated aminc acid.
     */
    private AminoAcid target;

    /**
     * The mass shift between original and target amino acid.
     */
    private double massShift;

    /**
     * Constructs a mutation from one amino acid (origin) to another (target).
     */
    public Mutation(AminoAcid origin, AminoAcid target) {
        this.origin = origin;
        this.target = target;
        this.massShift = target.monoisotopicMass - origin.monoisotopicMass;
    }

    /**
     * Returns the mass shift.
      * @return The mass shift.
     */
    public double getMassShift() {
        return massShift;
    }

    /**
     * Get rounded mass shift.
     * @return Rounded mass shift.
     */
    public int getRoundedMassShift() {
        return (int) Math.round(massShift);
    }

    /**
     * Returns the origin amino acid.
     * @return The origin amino acid.
     */
    public AminoAcid getOrigin() {
        return origin;
    }

    /**
     * Returns the target amino acid.
     * @return The target amino acid.
     */
    public AminoAcid getTarget() {
        return target;
    }

    /**
     * Overwritten equals method.
     * @param mutation The mutation to be compared.
     * @return True if both mutations are the same.
     */
    public boolean equals(Mutation mutation) {
        if(this.origin.singleLetterCode.equals(mutation.getOrigin().singleLetterCode) && this.target.singleLetterCode.equals(mutation.getTarget().singleLetterCode)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.origin.threeLetterCode + " => "  + this.target.threeLetterCode;
    }
}
