package com.compomics.util.experiment.biology.ions.impl;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;

/**
 * This class represents an elementary ion.
 *
 * @author Marc Vaudel
 */
public class ElementaryIon extends Ion {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -1578136397635015592L;
    /**
     * Subtype int for a proton.
     */
    public static final int PROTON = 0;
    /**
     * A proton.
     */
    public static final ElementaryIon proton = new ElementaryIon("Proton", 1.007276466812, PROTON);
    /**
     * The name of the ion.
     */
    private String name;
    /**
     * The subtype identifier.
     */
    private int subType;
    /**
     * Cache for the multiples of the proton mass.
     */
    public static double[] protonMassMultiples = {0.0, 
        proton.getTheoreticMass(),
        2* proton.getTheoreticMass(),
        3* proton.getTheoreticMass(),
        4* proton.getTheoreticMass(),
        5* proton.getTheoreticMass(),
        6* proton.getTheoreticMass(),
        7* proton.getTheoreticMass(),
        8* proton.getTheoreticMass(),
        9* proton.getTheoreticMass(),
        10* proton.getTheoreticMass()};
    
    /**
     * Returns the mass of the proton multiplied by i. if i is smaller or equal to ten a value in cache is used. It is calculated otherwise. Throws an exception for negative i without sanity check.
     * 
     * @param i i
     * 
     * @return the mass of the proton multiplied by i
     */
    public static double getProtonMassMultiple(int i) {
        return i <= 10 ? protonMassMultiples[i] : i * ElementaryIon.proton.getTheoreticMass();
    }
    
    /**
     * Constructor.
     *
     * @param name the name of the ion
     * @param theoreticMass the theoretic mass of the ion
     * @param subType the subtype index
     */
    public ElementaryIon(String name, double theoreticMass, int subType) {
        this.name = name;
        this.theoreticMass1 = theoreticMass;
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
    public CvTerm getPsiMsCvTerm() {
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
     * Returns the possible subtypes.
     *
     * @return the possible subtypes
     */
    public static int[] getPossibleSubtypes() {
        
        return new int[]{PROTON};
        
    }

    @Override
    public NeutralLoss[] getNeutralLosses() {
        return null;
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        return anotherIon.getType() == IonType.ELEMENTARY_ION
                && anotherIon.getSubType() == subType
                && anotherIon.getTheoreticMass() == theoreticMass1
                && anotherIon.getNeutralLossesAsString().equals(getNeutralLossesAsString());
    }
}
