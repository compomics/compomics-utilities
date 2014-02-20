package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import static com.compomics.util.experiment.biology.ions.PrecursorIon.PRECURSOR;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

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
     * Map of the implemented reporter ions. Name -> reporter ion
     */
    private static HashMap<String, ReporterIon> implementedIons = new HashMap<String, ReporterIon>();
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
     * Standard reporter ion for an old TMT 126.
     */
    public final static ReporterIon TMT126_old = new ReporterIon("TMT126", 126.127491);
    /**
     * Standard reporter ion for an old TMT 127.
     */
    public final static ReporterIon TMT127_old = new ReporterIon("TMT127", 127.1308594);
    /**
     * Standard reporter ion for an old TMT 128.
     */
    public final static ReporterIon TMT128_old = new ReporterIon("TMT128", 128.1341553);
    /**
     * Standard reporter ion for an old TMT 129.
     */
    public final static ReporterIon TMT129_old = new ReporterIon("TMT129", 129.1375046);
    /**
     * Standard reporter ion for an old TMT 130.
     */
    public final static ReporterIon TMT130_old = new ReporterIon("TMT130", 130.1408768);
    /**
     * Standard reporter ion for an old TMT 131.
     */
    public final static ReporterIon TMT131_old = new ReporterIon("TMT131", 131.1444851);
    /**
     * Standard reporter ion TMT 126 obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT126_hcd = new ReporterIon("TMT126", 126.127725);
    /**
     * Standard reporter ion TMT 126 obtained by etd fragmentation.
     */
    public final static ReporterIon TMT126_etd = new ReporterIon("TMT114", 114.127725);
    /**
     * Standard reporter ion TMT 127 obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT127_hcd = new ReporterIon("TMT127", 127.124760);
    /**
     * Standard reporter ion TMT 127 obtained by etd fragmentation.
     */
    public final static ReporterIon TMT127_etd = new ReporterIon("TMT115", 115.124760);
    /**
     * Standard reporter ion TMT 127N obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT127N_hcd = new ReporterIon("TMT127N", 127.124760);
    /**
     * Standard reporter ion TMT 127C obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT127C_hcd = new ReporterIon("TMT127C", 127.131079);
    /**
     * Standard reporter ion TMT 128 obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT128_hcd = new ReporterIon("TMT128", 128.134433);
    /**
     * Standard reporter ion TMT 128 obtained by etd fragmentation.
     */
    public final static ReporterIon TMT128_etd = new ReporterIon("TMT116", 116.134433);
    /**
     * Standard reporter ion TMT 128N obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT128N_hcd = new ReporterIon("TMT128N", 128.128114);
    /**
     * Standard reporter ion TMT 128C obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT128C_hcd = new ReporterIon("TMT128C", 128.134433);
    /**
     * Standard reporter ion TMT 129 obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT129_hcd = new ReporterIon("TMT129", 129.131468);
    /**
     * Standard reporter ion TMT 129 obtained by etd fragmentation.
     */
    public final static ReporterIon TMT129_etd = new ReporterIon("TMT117", 117.131468);
    /**
     * Standard reporter ion TMT 129N obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT129N_hcd = new ReporterIon("TMT129N", 129.131468);
    /**
     * Standard reporter ion TMT 129C obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT129C_hcd = new ReporterIon("TMT129C", 129.131468);
    /**
     * Standard reporter ion TMT 130 obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT130_hcd = new ReporterIon("TMT130", 130.141141);
    /**
     * Standard reporter ion TMT 130 obtained by etd fragmentation.
     */
    public final static ReporterIon TMT130_etd = new ReporterIon("TMT118", 118.141141);
    /**
     * Standard reporter ion TMT 130N obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT130N_hcd = new ReporterIon("TMT130N", 130.131141);
    /**
     * Standard reporter ion TMT 130C obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT130C_hcd = new ReporterIon("TMT130C", 130.141141);
    /**
     * Standard reporter ion TMT 131 obtained by hcd fragmentation.
     */
    public final static ReporterIon TMT131_hcd = new ReporterIon("TMT131", 131.138176);
    /**
     * Standard reporter ion TMT 131 obtained by etd fragmentation.
     */
    public final static ReporterIon TMT131_etd = new ReporterIon("TMT119", 119.138176);
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
     * Constructor for a user-defined reporter ion. The reporter ion is saved in a static map by default and can be retrieved using the static methods. See getReporterIon(String name).
     *
     * @param name name of the reporter ion. Should be unique to the ion.
     * @param mass theoretic mass of the reporter ion
     */
    public ReporterIon(String name, double mass) {
        this(name, mass, true);
    }

    /**
     * Constructor for a user-defined reporter ion.
     *
     * @param name name of the reporter ion. Should be unique to the ion.
     * @param mass theoretic mass of the reporter ion
     * @param save if true the reporter ion will be saved in the static map for later reuse
     */
    public ReporterIon(String name, double mass, boolean save) {
        type = IonType.REPORTER_ION;
        this.name = name;
        this.theoreticMass = mass;
        if (save) {
            implementedIons.put(name, this);
        }
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
        return theoreticMass == anotherReporterIon.getTheoreticMass();
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
        ArrayList<String> ionList = new ArrayList<String>(getImplementedIons());
        Collections.sort(ionList);
        return ionList.indexOf(name);
    }

    @Override
    public String getSubTypeAsString() {
        return getName();
    }

    /**
     * Returns the reporter ion indexed by the given index.
     *
     * @param subType the index of interest
     *
     * @return the corresponding reporter ion
     */
    public static ReporterIon getReporterIon(int subType) {
        ArrayList<String> ionList = new ArrayList<String>(getImplementedIons());
        Collections.sort(ionList);
        String name = ionList.get(subType);
        return getReporterIon(name);
    }

    /**
     * Returns the reporter ion corresponding to the given name.
     *
     * @param name the name of the reporter ion
     *
     * @return the corresponding reporter ion
     */
    public static ReporterIon getReporterIon(String name) {
        return implementedIons.get(name);
    }

    /**
     * Returns an arraylist of possible subtypes.
     *
     * @return an arraylist of possible subtypes
     */
    public static Set<String> getImplementedIons() {
        return implementedIons.keySet();
    }

    /**
     * Returns an arraylist of possible subtypes.
     *
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        ArrayList<Integer> possibleTypes = new ArrayList<Integer>(implementedIons.size());
        for (int i = 0; i < implementedIons.size(); i++) {
            possibleTypes.add(i);
        }
        return possibleTypes;
    }

    @Override
    public ArrayList<NeutralLoss> getNeutralLosses() {
        return new ArrayList<NeutralLoss>();
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        if (anotherIon instanceof ReporterIon) {
            ReporterIon otherIon = (ReporterIon) anotherIon;
            return isSameAs(otherIon);
        }
        return false;
    }

    @Override
    public double getTheoreticMass() {
        return theoreticMass - ElementaryIon.proton.getTheoreticMass();
    }
}
