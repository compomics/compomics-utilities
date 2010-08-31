package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 1:36:49 PM
 * This class will modelize a glycon fragment.
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
     * Name of the glycon
     */
    private String name;

    /**
     * Short name of the glycon
     */
    private String shortName;


    /**
     * Constructor for a glycon
     * @param aName         The name of the glycon
     * @param aShortName    A shortened name for the glycon
     */
    public Glycon(String aName, String aShortName) {
        this.name = aName;
        this.shortName = aShortName;
        this.familyType = Ion.GLYCON_FRAGMENT;
    }

    /**
     * Add a mass for this glycon
     * @param massType  mass type indexed according to the static field
     * @param value     Value of the mass
     */
    public void addMass(int massType, double value) {
        theoreticMasses.put(massType, value);
    }

    /**
     * Get the glycon mass
     * @param aType Type of mass requested indexed according to static int.
     * @return the requested mass
     */
    public double getMass(int aType) {
        return theoreticMasses.get(aType);
    }

    /**
     * Getter for the glycons name
     * @return the glycons name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the glycons short name
     * @return the glycon short name
     */
    public String getShortName() {
        return shortName;
    }

}
