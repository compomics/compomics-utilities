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

    // Attributes

    public final static int UNDERIVATED_MASS = 0;
    public final static int PERMETHYLATED_MASS = 1;
    public final static int DEUTEROMETHYLATED_MASS = 2;
    public final static int PERACETYLATED_MASS = 3;
    public final static int DEUTEROACETYLATED_MASS = 4;

    private HashMap<Integer, Double> theoreticMasses = new HashMap<Integer, Double>();
    private String name;
    private String shortName;


    // Constructor


    public Glycon(String aName, String aShortName) {
        this.name = aName;
        this.shortName = aShortName;
        this.familyType = Ion.GLYCON_FRAGMENT;
    }

    public void addMass(int massType, double value) {
        theoreticMasses.put(massType, value);
    }

    public double getMass(int aType) {
        return theoreticMasses.get(aType);
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

}
