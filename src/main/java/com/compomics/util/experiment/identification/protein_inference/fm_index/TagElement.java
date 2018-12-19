/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.fm_index;

/**
 *
 *
 * @author Dominik Kopczynski
 */
public class TagElement {
    
    boolean isMass;
    String sequence;
    double mass;
    int xNumLimit;

    /**
     * Constructor.
     *
     * @param isMass
     * @param sequence
     * @param mass
     * @param xNumLimit
     */
    TagElement(boolean isMass, String sequence, double mass, int xNumLimit) {
        this.isMass = isMass;
        this.sequence = sequence;
        this.mass = mass;
        this.xNumLimit = xNumLimit;
    }

    /**
     * Constructor.
     *
     * @param isMass
     * @param sequence
     * @param mass
     */
    TagElement(boolean isMass, String sequence, double mass) {
        this.isMass = isMass;
        this.sequence = sequence;
        this.mass = mass;
        this.xNumLimit = 0;
    }

    /**
     * Creating String output.
     *
     * @return String output
     */
    @Override
    public String toString() {
        String output;
        if (isMass) {
            output = String.format("%.5f", mass);
        } else {
            output = sequence;
        }
        return output;
    }
}
