package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class models a reporter ion and is its own factory. Note: By convention
 * the mass includes a proton here.
 *
 * @author Marc Vaudel
 */
public class ReporterIon extends Ion {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 1109011048958734120L;
    /**
     * Map of the implemented reporter ions.
     */
    private static HashMap<Integer, String> reporterIonTypes = new HashMap<Integer, String>();
    /**
     * Standard reporter ion iTRAQ 113.
     */
    public final static ReporterIon iTRAQ113 = new ReporterIon("iTRAQ113", 113.1075);
    /**
     * Standard reporter ion iTRAQ 114.
     */
    public final static ReporterIon iTRAQ114 = new ReporterIon("iTRAQ114", 114.111);
    /**
     * Standard reporter ion iTRAQ 115.
     */
    public final static ReporterIon iTRAQ115 = new ReporterIon("iTRAQ115", 115.1079);
    /**
     * Standard reporter ion iTRAQ 116.
     */
    public final static ReporterIon iTRAQ116 = new ReporterIon("iTRAQ116", 116.1113);
    /**
     * Standard reporter ion iTRAQ 117.
     */
    public final static ReporterIon iTRAQ117 = new ReporterIon("iTRAQ117", 117.11465);
    /**
     * Standard reporter ion iTRAQ 118.
     */
    public final static ReporterIon iTRAQ118 = new ReporterIon("iTRAQ118", 118.1117);
    /**
     * Standard reporter ion iTRAQ 119.
     */
    public final static ReporterIon iTRAQ119 = new ReporterIon("iTRAQ119", 119.115);
    /**
     * Standard reporter ion iTRAQ 121.
     */
    public final static ReporterIon iTRAQ121 = new ReporterIon("iTRAQ121", 121.1217);
    /**
     * Standard reporter ion iTRAQ (reporter + balancer).
     */
    public final static ReporterIon iTRAQ_145 = new ReporterIon("iTRAQ145", 145.1); // @TODO: check the mass!!
    /**
     * Standard reporter ion iTRAQ (reporter + balancer).
     */
    public final static ReporterIon iTRAQ_305 = new ReporterIon("iTRAQ305", 305.2); // @TODO: check the mass!!
    /**
     * Standard reporter ion TMT0.
     */
    public final static ReporterIon TMT0 = new ReporterIon("TMT126", 126.127491);
    /**
     * Standard reporter ion TMT1.
     */
    public final static ReporterIon TMT1 = new ReporterIon("TMT127", 127.1308594);
    /**
     * Standard reporter ion TMT2.
     */
    public final static ReporterIon TMT2 = new ReporterIon("TMT128", 128.1341553);
    /**
     * Standard reporter ion TMT3.
     */
    public final static ReporterIon TMT3 = new ReporterIon("TMT129", 129.1375046);
    /**
     * Standard reporter ion TMT4.
     */
    public final static ReporterIon TMT4 = new ReporterIon("TMT130", 130.1408768);
    /**
     * Standard reporter ion TMT5.
     */
    public final static ReporterIon TMT5 = new ReporterIon("TMT131", 131.1444851);
    /**
     * Standard reporter ion TMT (reporter + balancer).
     */
    public final static ReporterIon TMT_230 = new ReporterIon("TMT230", 230.2); // @TODO: check the mass!!
    /**
     * Standard reporter ion TMT (reporter + balancer).
     */
    public final static ReporterIon TMT_226 = new ReporterIon("TMT226", 226.2); // @TODO: check the mass!!
    /**
     * Standard reporter ion for lysine acetylation (PMID: 18338905).
     */
    public final static ReporterIon ACE_K_126 = new ReporterIon("aceK126", 126);
    /**
     * Standard reporter ion for lysine acetylation (PMID: 18338905).
     */
    public final static ReporterIon ACE_K_143 = new ReporterIon("aceK143", 143);
    /**
     * Standard reporter ion for phosphorylation of tyrosine (PMID: 11473401).
     */
    public final static ReporterIon PHOSPHO_Y = new ReporterIon("pY216", 216);
    /**
     * Ion name for user defined ions.
     */
    private String name;
    /**
     * The ion subtype.
     */
    private int subtype;

    /**
     * Constructor for a user-defined reporter ion.
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
     * Constructor for a user-defined reporter ion.
     *
     * @param subType the reporter ion type
     */
    public ReporterIon(int subType) {
        type = IonType.REPORTER_ION;
        this.name = reporterIonTypes.get(subType);
        this.subtype = subType;
    }

    /**
     * This method returns the name of the reporter ion.
     *
     * @return name of the reporter ion
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the ion name.
     *
     * @param name the new ion name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method to set the mass of the reporter ion.
     *
     * @param referenceMass the mass where the reporter ions should be found
     */
    public void setMass(double referenceMass) {
        this.theoreticMass = referenceMass;
    }

    @Override
    public CvTerm getPrideCvTerm() {
        
        // @TODO: implement when the required cv terms are added
        
        return null;
    }

    /**
     * Compares the current reporter ion with another one based on their masses.
     *
     * @param anotherReporterIon the other reporter ion
     * @return a boolean indicating whether masses are equal
     */
    public boolean isSameAs(ReporterIon anotherReporterIon) {
        return subtype == anotherReporterIon.getSubType();
        //return theoreticMass == anotherReporterIon.getTheoreticMass(); // @TODO: never a good idea to compare float values like this!
    }

    /**
     * Returns the index of a reporter ion. (i.e. its rounded m/z: 114 for iTRAQ
     * 114).
     *
     * @return the index of a reporter ion.
     */
    public int getIndex() {
        return (int) getTheoreticMass();
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
     * Returns an arraylist of possible subtypes.
     *
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

    @Override
    public double getTheoreticMass() {
        return theoreticMass - ElementaryIon.proton.getTheoreticMass();
    }
}
