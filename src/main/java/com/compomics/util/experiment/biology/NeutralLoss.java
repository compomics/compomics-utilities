package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class represents a neutral loss.
 *
 * @author Marc Vaudel
 */
public class NeutralLoss extends ExperimentObject {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 5540846193082177391L;
    /**
     * H2O loss.
     */
    public static final NeutralLoss H2O = new NeutralLoss("H2O", 2 * Atom.H.mass + Atom.O.mass, false);
    /**
     * NH3 loss.
     */
    public static final NeutralLoss NH3 = new NeutralLoss("NH3", Atom.N.mass + 3 * Atom.H.mass, false);
    /**
     * H3PO4 loss.
     */
    public static final NeutralLoss H3PO4 = new NeutralLoss("H3PO4", 3 * Atom.H.mass + Atom.P.mass + 4 * Atom.O.mass, false);
    /**
     * H3PO3 loss.
     */
    public static final NeutralLoss HPO3 = new NeutralLoss("HPO3", Atom.H.mass + Atom.P.mass + 3 * Atom.O.mass, false);
    /**
     * CH4OS loss.
     */
    public static final NeutralLoss CH4OS = new NeutralLoss("CH4OS", Atom.C.mass + 4 * Atom.H.mass + Atom.O.mass + Atom.S.mass, false);
    /**
     * The mass lost.
     */
    public double mass;
    /**
     * The name of the neutral loss.
     */
    public String name;
    /**
     * Boolean indicating whether the neutral loss will always be accounted for.
     */
    private Boolean fixed = false;

    /**
     * Method indicating whether another neutral loss is the same as the one
     * considered.
     *
     * @param anotherNeutralLoss another neutral loss
     * @return boolean indicating whether the other neutral loss is the same as
     * the one considered
     */
    public boolean isSameAs(NeutralLoss anotherNeutralLoss) {
        return anotherNeutralLoss.name.equals(name)
                || Math.abs(anotherNeutralLoss.mass - mass) < 0.001;
    }

    /**
     * Constructor for a user defined neutral loss.
     *
     * @param name name of the neutral loss
     * @param mass mass of the neutral loss
     * @param fixed is the neutral loss fixed or not
     */
    public NeutralLoss(String name, double mass, boolean fixed) {
        this.name = name;
        this.mass = mass;
        this.fixed = fixed;
    }

    /**
     * Returns a boolean indicating whether the neutral loss is fixed or not.
     *
     * @return a boolean indicating whether the neutral loss is fixed or not
     */
    public boolean isFixed() {
        if (fixed == null) {
            fixed = false;
        }
        return fixed;
    }

    /**
     * Sets whether the loss is fixed or not.
     *
     * @param fixed a boolean indicating whether the loss is fixed or not
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
}