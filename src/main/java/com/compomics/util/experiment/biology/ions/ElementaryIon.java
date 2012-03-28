package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;

/**
 * This class represents an elementary ion
 *
 * @author marc
 */
public class ElementaryIon extends Ion {

    /**
     * subtype int for a proton
     */
    public static final int PROTON = 0;
    /**
     * A proton
     */
    public static final ElementaryIon proton = new ElementaryIon("Proton", 1.007276466812, PROTON);
    /**
     * The name of the ion
     */
    private String name;
    /**
     * The subtype identifier
     */
    private int subType;

    /**
     * Constructor
     *
     * @param name the name of the ion
     * @param theoreticMass the theoretic mass of the ion
     * @param subType the subtype index
     */
    public ElementaryIon(String name, double theoreticMass, int subType) {
        this.name = name;
        this.theoreticMass = theoreticMass;
        type = Ion.IonType.ELEMENTARY_ION;
        this.subType = subType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CvTerm getPrideCvTerm() {
        return null;
    }

    @Override
    public int getSubType() {
        return subType;
    }

    @Override
    public String getSubTypeAsString() {
        switch (subType) {
            case PROTON:
                return "Proton";
            default:
                throw new UnsupportedOperationException("No name for subtype: " + subType + " of " + getTypeAsString() + ".");
        }
    }

    /**
     * Returns an arraylist of possible subtypes
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        ArrayList<Integer> possibleTypes = new ArrayList<Integer>();
        possibleTypes.add(PROTON);
        return possibleTypes;
    }

    @Override
    public ArrayList<NeutralLoss> getNeutralLosses() {
        switch (subType) {
            case PROTON:
                return new ArrayList<NeutralLoss>(); // If you see a neutral loss of a proton, call Gell-Mann and Zweig
            default:
                return new ArrayList<NeutralLoss>();
        }
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        return anotherIon.getType() == IonType.ELEMENTARY_ION
                && anotherIon.getSubType() == subType
                && anotherIon.getTheoreticMass() == theoreticMass
                && anotherIon.getNeutralLossesAsString().equals(getNeutralLossesAsString());
    }
}
