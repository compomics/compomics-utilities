package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * The tag element.
 *
 * @author Dominik Kopczynski
 */
public class TagElement extends ExperimentObject  {
 
    boolean isMass;
    String sequence;
    double mass;
    int xNumLimit;
    Integer[] modifications;

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
        this.modifications = null;
    }

    /**
     * Constructor.
     *
     * @param isMass
     * @param sequence
     * @param mass
     * @param xNumLimit
     * @param modifications
     */
    TagElement(boolean isMass, String sequence, double mass, int xNumLimit, Integer[] modifications) {
        this.isMass = isMass;
        this.sequence = sequence;
        this.mass = mass;
        this.xNumLimit = xNumLimit;
        this.modifications = modifications;
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
        this.modifications = null;
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
            if (modifications != null){
                for (int i = 0; i < modifications.length; ++i){
                    int mod = modifications[i];
                    if (mod != -1) output += ", mod(" + i + "/" + mod + ")";
                }
            }
        }
        return output;
    }
}
