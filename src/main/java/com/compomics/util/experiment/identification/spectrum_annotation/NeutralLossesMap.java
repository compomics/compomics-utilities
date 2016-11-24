package com.compomics.util.experiment.identification.spectrum_annotation;

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
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -4690159937753713106L;
    /**
     * Map indicating for each neutral loss when they should start being
     * accounted for the forward ions (b ions for instance).
     *
     * @deprecated use String indexes instead
     */
    private HashMap<NeutralLoss, Integer> bBoundaries = new HashMap<NeutralLoss, Integer>();
    /**
     * Map indicating for each neutral loss when they should start being
     * accounted for the reverse ions (y ions for instance).
     *
     * @deprecated use String indexes instead
     */
    private HashMap<NeutralLoss, Integer> yBoundaries = new HashMap<NeutralLoss, Integer>();
    /**
     * Map indicating for each neutral loss when they should start being
     * accounted for the forward ions (b ions for instance).
     */
    private HashMap<String, Integer> forwardBoundaries = new HashMap<String, Integer>();
    /**
     * Map indicating for each neutral loss when they should start being
     * accounted for the reverse ions (y ions for instance).
     */
    private HashMap<String, Integer> rewindBoundaries = new HashMap<String, Integer>();
    /**
     * Cache for the accounted neutral losses.
     */
    private ArrayList<String> accountedNeutralLosses = null;

    /**
     * Constructor.
     */
    public NeutralLossesMap() {
    }

    /**
     * Backward compatibility fix for objects of utilities version older than
     * 4.2.16.
     */
    public void backwardCompatibilityFix() {
        if (forwardBoundaries == null) {
            forwardBoundaries = new HashMap<String, Integer>();
            if (bBoundaries != null) {
                for (NeutralLoss neutralLoss : bBoundaries.keySet()) {
                    Integer boundary = bBoundaries.get(neutralLoss);
                    String name = neutralLoss.name;
                    forwardBoundaries.put(name, boundary);
                    NeutralLoss.addNeutralLoss(neutralLoss);
                }
            }
        }
        if (rewindBoundaries == null) {
            rewindBoundaries = new HashMap<String, Integer>();
            if (yBoundaries != null) {
                for (NeutralLoss neutralLoss : yBoundaries.keySet()) {
                    Integer boundary = yBoundaries.get(neutralLoss);
                    String name = neutralLoss.name;
                    rewindBoundaries.put(name, boundary);
                    NeutralLoss.addNeutralLoss(neutralLoss);
                }
            }
        }
    }

    /**
     * Adds a new neutral loss to the map.
     *
     * @param neutralLoss the new neutral loss
     * 
     * @param bStart the amino acid position where the neutral loss should start
     * being accounted starting from the N-terminus (first is 1)
     * @param yStart the amino acid position where the neutral loss should start
     * being accounted starting from the C-terminus (first is 1)
     */
    public void addNeutralLoss(NeutralLoss neutralLoss, Integer bStart, Integer yStart) {
        addNeutralLoss(neutralLoss.name, bStart, yStart);
        accountedNeutralLosses = null;
    }

    /**
     * Adds a new neutral loss to the map.
     *
     * @param neutralLossName the new neutral loss name
     * 
     * @param bStart the amino acid position where the neutral loss should start
     * being accounted starting from the N-terminus (first is 1)
     * @param yStart the amino acid position where the neutral loss should start
     * being accounted starting from the C-terminus (first is 1)
     */
    public void addNeutralLoss(String neutralLossName, Integer bStart, Integer yStart) {
        backwardCompatibilityFix();
        Integer position = forwardBoundaries.get(neutralLossName);
        if (position == null || bStart < position) {
            forwardBoundaries.put(neutralLossName, bStart);
        }
        position = rewindBoundaries.get(neutralLossName);
        if (position == null || yStart < position) {
            rewindBoundaries.put(neutralLossName, yStart);
        }
        accountedNeutralLosses = null;
    }

    /**
     * Clears the mapping.
     */
    public void clearNeutralLosses() {
        backwardCompatibilityFix();
        forwardBoundaries.clear();
        rewindBoundaries.clear();
        accountedNeutralLosses = null;
    }

    /**
     * Makes the neutral losses sequence independent.
     */
    public void makeSequenceIndependant() {
        backwardCompatibilityFix();
        for (String neutralLossName : forwardBoundaries.keySet()) {
            forwardBoundaries.put(neutralLossName, 1);
            rewindBoundaries.put(neutralLossName, 1);
        }
    }

    /**
     * Returns a boolean indicating if the mapping is empty.
     *
     * @return a boolean indicating if the mapping is empty
     */
    public boolean isEmpty() {
        backwardCompatibilityFix();
        return forwardBoundaries.isEmpty();
    }

    /**
     * Returns an arraylist of the names of the implemented neutral losses.
     *
     * @return an arraylist of the names of the implemented neutral losses
     */
    public ArrayList<String> getAccountedNeutralLosses() {
        backwardCompatibilityFix();
        if (accountedNeutralLosses == null) {
            accountedNeutralLosses = new ArrayList<String>(forwardBoundaries.keySet());
        }
        return accountedNeutralLosses;
    }

    /**
     * Returns the amino acid where a neutral loss should start being accounted
     * for when predicting b ions (counting from N-terminus, first aa is 1).
     *
     * @param neutralLossName the name of the neutral loss of interest
     * 
     * @return the first amino acid where to account for the neutral loss
     */
    public Integer getForwardStart(String neutralLossName) {
        backwardCompatibilityFix();
        return forwardBoundaries.get(neutralLossName);
    }

    /**
     * Returns the amino acid where a neutral loss should start being accounted
     * for when predicting b ions (counting from N-terminus, first aa is 1).
     *
     * @param neutralLossName the name of the neutral loss of interest
     * 
     * @return the first amino acid where to account for the neutral loss
     */
    public int getRewindStart(String neutralLossName) {
        backwardCompatibilityFix();
        Integer start = rewindBoundaries.get(neutralLossName);
        if (start == null) {
            return 0;
        }
        return start;
    }

    /**
     * Returns a boolean indicating whether a loss is implemented in the
     * mapping.
     *
     * @param neutralLossName the name of the neutral loss of interest
     * 
     * @return a boolean indicating whether a loss is implemented in the mapping
     */
    public boolean containsLoss(String neutralLossName) {
        backwardCompatibilityFix();
        return forwardBoundaries.containsKey(neutralLossName);
    }

    @Override
    public NeutralLossesMap clone() {
        NeutralLossesMap result = new NeutralLossesMap();
        for (String neutralLossName : getAccountedNeutralLosses()) {
            result.addNeutralLoss(neutralLossName, getForwardStart(neutralLossName), getRewindStart(neutralLossName));
        }
        return result;
    }
}
