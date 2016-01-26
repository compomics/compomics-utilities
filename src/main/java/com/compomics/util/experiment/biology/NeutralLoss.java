package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.HashMap;

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
    public static final NeutralLoss H2O = new NeutralLoss("H2O", new AtomChain("H2O", true), false);
    /**
     * NH3 loss.
     */
    public static final NeutralLoss NH3 = new NeutralLoss("NH3", new AtomChain("NH3", true), false);
    /**
     * H3PO4 loss.
     */
    public static final NeutralLoss H3PO4 = new NeutralLoss("H3PO4", new AtomChain("H3PO4", true), false);
    /**
     * H3PO3 loss.
     */
    public static final NeutralLoss HPO3 = new NeutralLoss("HPO3", new AtomChain("HPO3", true), false);
    /**
     * CH4OS loss.
     */
    public static final NeutralLoss CH4OS = new NeutralLoss("CH4OS", new AtomChain("CH4OS", true), false);
    /**
     * C3H9N loss.
     */
    public static final NeutralLoss C3H9N = new NeutralLoss("C3H9N", new AtomChain("C3H9N", true), false);
    /**
     * The mass lost.
     *
     * @deprecated use the composition instead.
     */
    private Double mass;
    /**
     * The composition of the ion.
     */
    private AtomChain composition;
    /**
     * The name of the neutral loss.
     */
    public final String name;
    /**
     * Boolean indicating whether the neutral loss will always be accounted for.
     */
    private Boolean fixed = false;
    /**
     * Map of available neutral losses.
     */
    private static HashMap<String, NeutralLoss> neutralLosses;

    /**
     * Constructor for a user defined neutral loss.
     *
     * @param name name of the neutral loss
     * @param composition the atomic composition of the neutral loss
     * @param fixed is the neutral loss fixed or not
     */
    public NeutralLoss(String name, AtomChain composition, boolean fixed) {
        this.name = name;
        this.composition = composition;
        this.fixed = fixed;
        addNeutralLoss(this);
    }
    
    /**
     * Adds a neutral loss to the class static map. Neutral losses with the same name will be overwritten.
     * 
     * @param neutralLoss the neutral loss to add
     */
    public static void addNeutralLoss(NeutralLoss neutralLoss) {
        if (neutralLosses == null) {
            neutralLosses = new HashMap<String, NeutralLoss>();
        }
        neutralLosses.put(neutralLoss.name, neutralLoss);
    }
    
    /**
     * Returns the neutral loss associated to the given name in the static map of the class. Null if not found.
     * 
     * @param name the name of the neutral loss of interest
     * 
     * @return the neutral loss
     */
    public static NeutralLoss getNeutralLoss(String name) {
        if (neutralLosses == null) {
            return null;
        }
        return neutralLosses.get(name);
    }
    
    /**
     * Removes the neutral loss associated to the given name in the static map of the class.
     * 
     * @param name the name of the neutral loss to remove
     */
    public static void removeNeutralLoss(String name) {
        if (neutralLosses != null) {
            neutralLosses.remove(name);
        }
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

    /**
     * The composition of the loss.
     *
     * @return The composition of the loss
     */
    public AtomChain getComposition() {
        return composition;
    }

    /**
     * Sets the composition of the neutral loss.
     *
     * @param composition the composition of the neutral loss
     */
    public void setComposition(AtomChain composition) {
        this.composition = composition;
    }

    /**
     * Returns the mass of the neutral loss, from the atomic composition if
     * available, from the mass field otherwise.
     *
     * @return the mass of the neutral loss
     */
    public Double getMass() {
        if (composition != null) {
            return composition.getMass();
        }
        return mass;
    }

    /**
     * Method indicating whether another neutral loss is the same as the one
     * considered.
     *
     * @param anotherNeutralLoss another neutral loss
     * @return boolean indicating whether the other neutral loss is the same as
     * the one considered
     */
    public boolean isSameAs(NeutralLoss anotherNeutralLoss) {
        if (anotherNeutralLoss.getComposition() == null || getComposition() == null) { // Backward compatibility
            return anotherNeutralLoss.name.equals(name)
                    && anotherNeutralLoss.mass.equals(mass);
        }
        return anotherNeutralLoss.name.equals(name)
                && anotherNeutralLoss.getComposition().isSameCompositionAs(getComposition());
    }
}
