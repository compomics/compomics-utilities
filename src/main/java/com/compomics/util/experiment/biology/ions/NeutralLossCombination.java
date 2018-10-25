package com.compomics.util.experiment.biology.ions;

/**
 * This class represents a combination of multiple neutral losses.
 *
 * @author Marc Vaudel
 */
public class NeutralLossCombination {

    /**
     * Empty default constructor
     */
    public NeutralLossCombination() {
        neutralLossCombination = null;
    }

    /**
     * The neutral losses.
     */
    private final NeutralLoss[] neutralLossCombination;
    /**
     * The mass of the combination.
     */
    private double mass;

    /**
     * Constructor.
     *
     * @param neutralLossCombination a combination of neutral losses
     */
    public NeutralLossCombination(NeutralLoss[] neutralLossCombination) {
        this.neutralLossCombination = neutralLossCombination;
        mass = 0;
        for (NeutralLoss neutralLoss : neutralLossCombination) {
            mass += neutralLoss.getMass();
        }
    }

    /**
     * Returns the neutral losses in this combination.
     *
     * @return the neutral losses in this combination
     */
    public NeutralLoss[] getNeutralLossCombination() {
        return neutralLossCombination;
    }

    /**
     * Returns the mass of this combination.
     *
     * @return the mass of this combination
     */
    public double getMass() {
        return mass;
    }
}
