package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.Proton;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class models an ion.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:57:33 AM
 */
public abstract class Ion extends ExperimentObject {

    /**
     * Ion family type
     * static index for peptide fragments
     */
    public static final int PEPTIDE_FRAGMENT = 0;
    /**
     * Ion family type
     * static index for glycons
     */
    public static final int GLYCON_FRAGMENT = 1;
    /**
     * Ion family type
     * static index for glycons
     */
    public static final int REPORTER_ION = 2;
    /**
     * Ion family type
     * static index for elementary ions
     */
    public static final int ELEMENTARY_ION = 3;

    /**
     * Ion attribute - the theoretic mass
     */
    public double theoreticMass;
    /**
     * Ion attribute - the ion family name
     */
    protected int familyType;

    /**
     * getter for the ion family name
     * @return the ion family name as indexed in static field
     */
    public int getIonFamilyType() {
        return familyType;
    }
    
    /**
     * Convenience method returning a proton
     * @return Convenience method returning a proton
     */
    public static Ion proton() {
        return new Proton();
    }
}
