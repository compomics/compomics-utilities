package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.NeutralLoss;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the informations relative to the accounting of neutral
 * losses
 *
 * @author Marc Vaudel
 */
public class NeutralLossesMap implements Serializable {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -4690159937753713106L;
    /**
     * map indicating for each neutral loss when they should start being
     * accounted for the forward ions (b ions for instance)
     */
    private HashMap<NeutralLoss, Integer> bBoundaries = new HashMap<NeutralLoss, Integer>();
    /**
     * map indicating for each neutral loss when they should start being
     * accounted for the reverse ions (y ions for instance)
     */
    private HashMap<NeutralLoss, Integer> yBoundaries = new HashMap<NeutralLoss, Integer>();

    /**
     * Constructor
     */
    public NeutralLossesMap() {
    }

    /**
     * Adds a new neutral loss to the map
     *
     * @param neutralLoss the new neutral loss
     * @param bStart the amino acid position where the neutral loss should start
     * being accounted starting from the N-terminus (first is 1)
     * @param yStart the amino acid position where the neutral loss should start
     * being accounted starting from the C-terminus (first is 1)
     */
    public void addNeutralLoss(NeutralLoss neutralLoss, int bStart, int yStart) {
        boolean found = false;
        for (NeutralLoss oldNeutralLoss : bBoundaries.keySet()) {
            if (oldNeutralLoss.isSameAs(neutralLoss) && bStart < bBoundaries.get(oldNeutralLoss)) {
                bBoundaries.put(oldNeutralLoss, bStart);
                found = true;
                break;
            }
        }
        if (!found) {
            bBoundaries.put(neutralLoss, bStart);
        }
        found = false;
        for (NeutralLoss oldNeutralLoss : yBoundaries.keySet()) {
            if (oldNeutralLoss.isSameAs(neutralLoss) && bStart < yBoundaries.get(oldNeutralLoss)) {
                yBoundaries.put(oldNeutralLoss, bStart);
                found = true;
                break;
            }
        }
        if (!found) {
            yBoundaries.put(neutralLoss, bStart);
        }
    }

    /**
     * Clears the mapping
     */
    public void clearNeutralLosses() {
        bBoundaries.clear();
        yBoundaries.clear();
    }

    /**
     * Makes the neutral losses sequence independant
     */
    public void makeSequenceIndependant() {
        for (NeutralLoss neutralLoss : bBoundaries.keySet()) {
            bBoundaries.put(neutralLoss, 1);
            yBoundaries.put(neutralLoss, 1);
        }
    }

    /**
     * Returns a boolean indicating if the mapping is empty
     *
     * @return a boolean indicating if the mapping is empty
     */
    public boolean isEmpty() {
        return bBoundaries.isEmpty();
    }

    /**
     * Returns an arraylist of implemented neutral losses
     *
     * @return an arraylist of implemented neutral losses
     */
    public ArrayList<NeutralLoss> getAccountedNeutralLosses() {
        return new ArrayList<NeutralLoss>(bBoundaries.keySet());
    }

    /**
     * Returns the amino acid where a neutral loss should start being accounted
     * for when predicting b ions (counting from N-terminus, first aa is 1)
     *
     * @param neutralLoss the neutral loss of interest
     * @return the first amino acid where to account for the neutral loss
     */
    public int getBStart(NeutralLoss neutralLoss) {
        return bBoundaries.get(neutralLoss);
    }

    /**
     * Returns the amino acid where a neutral loss should start being accounted
     * for when predicting b ions (counting from N-terminus, first aa is 1)
     *
     * @param neutralLoss the neutral loss of interest
     * @return the first amino acid where to account for the neutral loss
     */
    public int getYStart(NeutralLoss neutralLoss) {
        return yBoundaries.get(neutralLoss);
    }

    /**
     * Returns a boolean indicating whether a loss is implemented in the mapping
     *
     * @param neutralLoss the neutral loss of interest
     * @return a boolean indicating whether a loss is implemented in the mapping
     */
    public boolean containsLoss(NeutralLoss neutralLoss) {
        return bBoundaries.containsKey(neutralLoss);
    }
}
