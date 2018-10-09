package com.compomics.util.experiment.biology.ions;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.pride.CvTerm;
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
     * Map of available neutral losses.
     */
    private final static HashMap<String, NeutralLoss> neutralLosses = new HashMap<>(0);
    /**
     * H2O loss.
     */
    public static final NeutralLoss H2O = new NeutralLoss("H2O", AtomChain.getAtomChain("H(2)O"), false, new char[]{'D', 'E', 'S', 'T'});
    /**
     * NH3 loss.
     */
    public static final NeutralLoss NH3 = new NeutralLoss("NH3", AtomChain.getAtomChain("NH(3)"), false, new char[]{'K', 'N', 'Q', 'R'});
    /**
     * H3PO4 loss.
     */
    public static final NeutralLoss H3PO4 = new NeutralLoss("H3PO4", AtomChain.getAtomChain("H(3)PO(4)"), false);
    /**
     * H3PO3 loss.
     */
    public static final NeutralLoss HPO3 = new NeutralLoss("HPO3", AtomChain.getAtomChain("HPO(3)"), false);
    /**
     * CH4OS loss.
     */
    public static final NeutralLoss CH4OS = new NeutralLoss("CH4OS", AtomChain.getAtomChain("CH(4)OS"), false);
    /**
     * C3H9N loss.
     */
    public static final NeutralLoss C3H9N = new NeutralLoss("C3H9N", AtomChain.getAtomChain("C(3)H(9)N"), false);
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
    private boolean fixed = false;
    /**
     * The PSI MS CV term of the neutral loss, null if not set.
     */
    private CvTerm psiCvTerm = null;
    /**
     * Amino acids that are likely to induce this loss. Null if not a loss originating from amino acids.
     */
    public final char[] aminoAcids;

    /**
     * Constructor for a user defined neutral loss. The neutral loss is added to
     * the factory.
     *
     * @param name name of the neutral loss
     * @param composition the atomic composition of the neutral loss
     * @param fixed is the neutral loss fixed or not
     * @param aminoAcids the amino acids that are likely to induce this loss
     */
    public NeutralLoss(String name, AtomChain composition, boolean fixed, char[] aminoAcids) {
        this(name, composition, fixed, aminoAcids, true);
    }

    /**
     * Constructor for a user defined neutral loss. The neutral loss is added to
     * the factory.
     *
     * @param name name of the neutral loss
     * @param composition the atomic composition of the neutral loss
     * @param fixed is the neutral loss fixed or not
     */
    public NeutralLoss(String name, AtomChain composition, boolean fixed) {
        this(name, composition, fixed, null, true);
    }

    /**
     * Constructor for a user defined neutral loss.
     *
     * @param name name of the neutral loss
     * @param composition the atomic composition of the neutral loss
     * @param fixed is the neutral loss fixed or not
     * @param aminoAcids the amino acids that are likely to induce this loss
     * @param save if true, the neutral loss will be added to the factory
     */
    public NeutralLoss(String name, AtomChain composition, boolean fixed, char[] aminoAcids, boolean save) {
        this.name = name;
        this.composition = composition;
        this.fixed = fixed;
        this.aminoAcids = aminoAcids;
        if (save) {
            addNeutralLoss(this);
        }
    }

    /**
     * Adds a neutral loss to the class static map. Neutral losses with the same
     * name will be overwritten.
     *
     * @param neutralLoss the neutral loss to add
     */
    public static void addNeutralLoss(NeutralLoss neutralLoss) {
        neutralLosses.put(neutralLoss.name, neutralLoss);
    }

    /**
     * Returns the neutral loss associated to the given name in the static map
     * of the class. Null if not found.
     *
     * @param name the name of the neutral loss of interest
     *
     * @return the neutral loss
     */
    public static NeutralLoss getNeutralLoss(String name) {
        return neutralLosses.get(name);
    }

    /**
     * Removes the neutral loss associated to the given name in the static map
     * of the class.
     *
     * @param name the name of the neutral loss to remove
     */
    public static void removeNeutralLoss(String name) {
            neutralLosses.remove(name);
    }

    /**
     * Returns the CV term for the neutral loss. Null if none
     * corresponding.
     *
     * @return the CV term for the neutral loss.
     */
    public CvTerm getPsiMsCvTerm() {
        readDBMode();
        if (psiCvTerm != null) {
            return psiCvTerm;
        }
        psiCvTerm = new CvTerm("PSI-MS", "MS:1000336", "neutral loss", composition.toString());
        return psiCvTerm;
    }
    
    /**
     * Returns a boolean indicating whether the neutral loss is fixed or not.
     *
     * @return a boolean indicating whether the neutral loss is fixed or not
     */
    public boolean isFixed() {
        readDBMode();
        return fixed;
    }

    /**
     * Sets whether the loss is fixed or not.
     *
     * @param fixed a boolean indicating whether the loss is fixed or not
     */
    public void setFixed(boolean fixed) {
        writeDBMode();
        this.fixed = fixed;
    }

    /**
     * The composition of the loss.
     *
     * @return The composition of the loss
     */
    public AtomChain getComposition() {
        readDBMode();
        return composition;
    }

    /**
     * Sets the composition of the neutral loss.
     *
     * @param composition the composition of the neutral loss
     */
    public void setComposition(AtomChain composition) {
        writeDBMode();
        this.composition = composition;
    }

    /**
     * Returns the mass of the neutral loss, from the atomic composition if
     * available, from the mass field otherwise.
     *
     * @return the mass of the neutral loss
     */
    public double getMass() {
        readDBMode();
            return composition.getMass();
    }

    /**
     * Method indicating whether another neutral loss is the same as the one
     * considered. 
     *
     * @param anotherNeutralLoss another neutral loss
     * 
     * @return boolean indicating whether the other neutral loss is the same as
     * the one considered
     */
    public boolean isSameAs(NeutralLoss anotherNeutralLoss) {
        readDBMode();
        return anotherNeutralLoss.name.equals(name);
    }

    @Override
    public NeutralLoss clone() {
        readDBMode();
        return new NeutralLoss(name, composition.clone(), fixed, aminoAcids, false);
    }
}
