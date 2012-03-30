/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author marc
 */
public class PrecursorIon extends Ion {
    
    /**
     * Serial number for backward compatibility
     */
    static final long serialVersionUID = -2630586959372309153L;
        /**
         * For now only one type of precursor implemented
         */
         public static final int PRECURSOR = 0;
    /**
     * the neutral losses found on the ion
     */
    private ArrayList<NeutralLoss> neutralLosses = new ArrayList<NeutralLoss>();

    /**
     * Constructor
     * @param theoreticMass the theoretic mass
     * @param neutralLosses the neutral losses
     */
    public PrecursorIon(double theoreticMass, ArrayList<NeutralLoss> neutralLosses) {
        if (neutralLosses == null) {
            neutralLosses = new ArrayList<NeutralLoss>();
        }
        type = IonType.PRECURSOR_ION;
        this.neutralLosses.addAll(neutralLosses);
        this.theoreticMass = theoreticMass;
    }

    /**
     * Constructor for a generic ion
     * @param neutralLosses the neutral losses
     */
    public PrecursorIon(ArrayList<NeutralLoss> neutralLosses) {
        if (neutralLosses == null) {
            neutralLosses = new ArrayList<NeutralLoss>();
        }
        type = IonType.PRECURSOR_ION;
        this.neutralLosses.addAll(neutralLosses);
    }

    /**
     * Constructor for a generic ion without neutral losses
     */
    public PrecursorIon() {
        type = IonType.PRECURSOR_ION;
    }
    
    /**
     * Constructor
     * @param peptide the theoretic peptide
     */
    public PrecursorIon(Peptide peptide) {
        type = IonType.PRECURSOR_ION;
        this.theoreticMass = peptide.getMass();
    }
    
    @Override
    public String getName() {
        return getSubTypeAsString() + getNeutralLossesAsString();
    }

    @Override
    public CvTerm getPrideCvTerm() {
        if (neutralLosses.isEmpty()) {
            return new CvTerm("PRIDE", "PRIDE:0000263", "precursor ion", null);
        } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.H2O)) {
            return new CvTerm("PRIDE", "PRIDE:0000262", "precursor ion -H2O", null);
        } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.NH3)) {
            return new CvTerm("PRIDE", "PRIDE:0000261", "precursor ion -NH3", null);
        } else {
            return null;
        }
    }

    @Override
    public int getSubType() {
        return PRECURSOR;
    }

    @Override
    public String getSubTypeAsString() {
        return "Prec";
    }

    /**
     * Returns an arraylist of possible subtypes
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        ArrayList<Integer> possibleTypes = new ArrayList<Integer>();
        possibleTypes.add(PRECURSOR);
        return possibleTypes;
    }

    @Override
    public ArrayList<NeutralLoss> getNeutralLosses() {
        return neutralLosses;
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        return anotherIon.getType() == IonType.PRECURSOR_ION
                && anotherIon.getNeutralLossesAsString().equals(getNeutralLossesAsString());
    }
}
