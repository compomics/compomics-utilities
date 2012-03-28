package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;

import java.util.HashMap;

/**
 * This class will models a glycon fragment.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 1:36:49 PM
 */
public class Glycon extends Ion {

    /**
     * Final index for underivated masses
     */
    public final static int UNDERIVATED_MASS = 0;
    /**
     * Final index for permethylated masses
     */
    public final static int PERMETHYLATED_MASS = 1;
    /**
     * Final index for deuteromethylated masses
     */
    public final static int DEUTEROMETHYLATED_MASS = 2;
    /**
     * Final index for peracetylated masses
     */
    public final static int PERACETYLATED_MASS = 3;
    /**
     * Final index for deuteroacetylated masses
     */
    public final static int DEUTEROACETYLATED_MASS = 4;
    /**
     * Masses of this glycon.
     */
    private HashMap<Integer, Double> theoreticMasses = new HashMap<Integer, Double>();


    /**
     * Constructor for a glycon
     *
     * @param aName         The name of the glycon
     * @param aShortName    A shortened name for the glycon
     */
    public Glycon(String name, String longName) {
        type = IonType.GLYCON;
        this.name = name;
        this.longName = longName;
    }
    /**
     * The glycon short name which can be displayed on a spectrum
     */
    private String name;
    /**
     * The glycon full name
     */
    private String longName;

    /**
     * Add a mass for this glycon
     *
     * @param massType  mass type indexed according to the static field
     * @param value     Value of the mass
     */
    public void addMass(int massType, double value) {
        theoreticMasses.put(massType, value);
    }

    /**
     * Get the glycon mass
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSubType() {
        //@TODO: implement all glycon types
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSubTypeAsString() {
        //@TODO: implement all glycon types
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns an arraylist of possible subtypes
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<NeutralLoss> getNeutralLosses() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
