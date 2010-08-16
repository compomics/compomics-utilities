package com.compomics.util.experiment.biology;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:57:33 AM
 * This class modelizes an ion.
 */
public abstract class Ion {

    // Ion family types

    public static final int PEPTIDE_FRAGMENT = 0;
    public static final int GLYCON_FRAGMENT = 1;

    // Attributes
    public double theoreticMass;
    protected int familyType;

    // Constructors
    public Ion() {
    }

    // Methods

    public int getIonFamilyType() {
        return familyType;
    }
}
