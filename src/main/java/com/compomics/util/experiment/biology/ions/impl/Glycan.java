package com.compomics.util.experiment.biology.ions.impl;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.pride.CvTerm;

import java.util.HashMap;

/**
 * This class represents a glycan.
 *
 * @author Marc Vaudel
 */
public class Glycan extends Ion {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2278483206647518565L;
    /**
     * Final index for underivated masses.
     */
    public final static int UNDERIVATED_MASS = 0;
    /**
     * Final index for permethylated masses.
     */
    public final static int PERMETHYLATED_MASS = 1;
    /**
     * Final index for deuteromethylated masses.
     */
    public final static int DEUTEROMETHYLATED_MASS = 2;
    /**
     * Final index for peracetylated masses.
     */
    public final static int PERACETYLATED_MASS = 3;
    /**
     * Final index for deuteroacetylated masses.
     */
    public final static int DEUTEROACETYLATED_MASS = 4;
    /**
     * Masses of this glycan.
     */
    private final HashMap<Integer, Double> theoreticMasses = new HashMap<>();

    /**
     * Constructor for a glycan.
     *
     * @param name The name of the glycan
     * @param longName A shortened name for the glycan
     */
    public Glycan(String name, String longName) {
        type = IonType.GLYCAN;
        this.name = name;
        this.longName = longName;
    }
    /**
     * The glycan short name which can be displayed on a spectrum.
     */
    private final String name;
    /**
     * The glycan full name.
     */
    private final String longName;

    /**
     * Add a mass for this glycan.
     *
     * @param massType mass type indexed according to the static field
     * @param value Value of the mass
     */
    public void addMass(int massType, double value) {
        theoreticMasses.put(massType, value);
    }

    /**
     * Get the glycan mass.
     *
     * @param aType Type of mass requested indexed according to static int.
     * @return the requested mass
     */
    public double getMass(int aType) {
        return theoreticMasses.get(aType);
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
        //@TODO: implement all glycan types
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSubTypeAsString() {
        //@TODO: implement all glycan types
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the possible subtypes.
     *
     * @return the possible subtypes
     */
    public static int[] getPossibleSubtypes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NeutralLoss[] getNeutralLosses() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
