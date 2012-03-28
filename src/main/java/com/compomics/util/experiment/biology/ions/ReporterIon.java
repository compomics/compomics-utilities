package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class models a reporter ion and is its own factory
 *
 * User: Marc Date: Sep 1, 2010 Time: 1:44:59 PM
 */
public class ReporterIon extends Ion {

    /**
     * Standard reporter ion iTRAQ 113
     */
    public final static ReporterIon iTRAQ113 = new ReporterIon("iTRAQ113", 113);
    /**
     * Standard reporter ion iTRAQ 114
     */
    public final static ReporterIon iTRAQ114 = new ReporterIon("iTRAQ114", 114.1112);
    /**
     * Standard reporter ion iTRAQ 115
     */
    public final static ReporterIon iTRAQ115 = new ReporterIon("iTRAQ115", 115.1083);
    /**
     * Standard reporter ion iTRAQ 116
     */
    public final static ReporterIon iTRAQ116 = new ReporterIon("iTRAQ116", 116.1116);
    /**
     * Standard reporter ion iTRAQ 117
     */
    public final static ReporterIon iTRAQ117 = new ReporterIon("iTRAQ117", 117.1150);
    /**
     * Standard reporter ion iTRAQ 118
     */
    public final static ReporterIon iTRAQ118 = new ReporterIon("iTRAQ118", 118);
    /**
     * Standard reporter ion iTRAQ 119
     */
    public final static ReporterIon iTRAQ119 = new ReporterIon("iTRAQ119", 119);
    /**
     * Standard reporter ion iTRAQ 121
     */
    public final static ReporterIon iTRAQ121 = new ReporterIon("iTRAQ121", 121);
    /**
     * Standard reporter ion TMT0
     */
    public final static ReporterIon TMT0 = new ReporterIon("TMT0", 126);
    /**
     * Standard reporter ion TMT1
     */
    public final static ReporterIon TMT1 = new ReporterIon("TMT1", 127);
    /**
     * Standard reporter ion TMT2
     */
    public final static ReporterIon TMT2 = new ReporterIon("TMT2", 128);
    /**
     * Standard reporter ion TMT3
     */
    public final static ReporterIon TMT3 = new ReporterIon("TMT3", 129);
    /**
     * Standard reporter ion TMT4
     */
    public final static ReporterIon TMT4 = new ReporterIon("TMT4", 130);
    /**
     * Standard reporter ion TMT5
     */
    public final static ReporterIon TMT5 = new ReporterIon("TMT5", 131);
    /**
     * ion name for user defined ions
     */
    private String name;
    /**
     * The ion subtype
     */
    private int subtype;
    /**
     * Map of the implemented reporter ions
     */
    private static HashMap<Integer, String> reporterIonTypes = new HashMap<Integer, String>();

    /**
     * Constructor for a user-defined reporter ion
     *
     * @param name name of the reporter ion. Should be unique to the ion.
     * @param mass theoretic mass of the reporter ion
     */
    public ReporterIon(String name, double mass) {
        type = IonType.REPORTER_ION;
        this.name = name;
        this.theoreticMass = mass;
        boolean found = false;
        for (int possibleType : reporterIonTypes.keySet()) {
            if (reporterIonTypes.get(possibleType).equals(name)) {
                this.subtype = possibleType;
                found = true;
                break;
            }
        }
        if (!found) {
            subtype = reporterIonTypes.size();
            reporterIonTypes.put(subtype, name);
        }
    }

    /**
     * This method returns the name of the reporter ion
     *
     * @return name of the reporter ion
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the ion name
     *
     * @param name the new ion name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method to set the mass of the reporter ion
     *
     * @param referenceMass the mass where the reporter ions should be found
     */
    public void setMass(double referenceMass) {
        this.theoreticMass = referenceMass;
    }

    @Override
    public CvTerm getPrideCvTerm() {
        return null;
    }

    /**
     * Compares the current reporter ion with another one based on their masses
     *
     * @param anotherReporterIon the other reporter ion
     * @return a boolean indicating whether masses are equal
     */
    public boolean isSameAs(ReporterIon anotherReporterIon) {
        return theoreticMass == anotherReporterIon.getTheoreticMass();
    }

    @Override
    public int getSubType() {
        return subtype;
    }

    @Override
    public String getSubTypeAsString() {
        return reporterIonTypes.get(subtype);
    }

    /**
     * Returns an arraylist of possible subtypes
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        ArrayList<Integer> possibleTypes = new ArrayList<Integer>(reporterIonTypes.keySet());
        Collections.sort(possibleTypes);
        return possibleTypes;
    }

    @Override
    public ArrayList<NeutralLoss> getNeutralLosses() {
        return new ArrayList<NeutralLoss>();
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        return anotherIon.getType() == IonType.REPORTER_ION
                && anotherIon.getSubType() == subtype;
    }
}
