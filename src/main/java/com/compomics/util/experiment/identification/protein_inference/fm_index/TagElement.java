package com.compomics.util.experiment.identification.protein_inference.fm_index;

/**
 * The tag element.
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
