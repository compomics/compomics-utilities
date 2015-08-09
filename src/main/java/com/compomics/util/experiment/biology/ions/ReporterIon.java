package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * This class models a reporter ion and is its own factory.
 *
 * @author Marc Vaudel
 */
public class ReporterIon extends Ion {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 1109011048958734120L;
    /**
     * Map of the implemented reporter ions. Name &gt; reporter ion
     */
    private static HashMap<String, ReporterIon> implementedIons = new HashMap<String, ReporterIon>();
    /**
     * Standard reporter ion iTRAQ 4Plex 114.
     */
    public final static ReporterIon iTRAQ4Plex_114 = new ReporterIon("iTRAQ4Plex_114", getComposition("iTRAQ4Plex_114"));
    /**
     * Standard reporter ion iTRAQ 4Plex 115.
     */
    public final static ReporterIon iTRAQ4Plex_115 = new ReporterIon("iTRAQ4Plex_115", getComposition("iTRAQ4Plex_115"));
    /**
     * Standard reporter ion iTRAQ 4Plex 116.
     */
    public final static ReporterIon iTRAQ4Plex_116 = new ReporterIon("iTRAQ4Plex_116", getComposition("iTRAQ4Plex_116"));
    /**
     * Standard reporter ion iTRAQ 4Plex 117.
     */
    public final static ReporterIon iTRAQ4Plex_117 = new ReporterIon("iTRAQ4Plex_117", getComposition("iTRAQ4Plex_117"));
    /**
     * Standard reporter ion iTRAQ 8Plex 113.
     */
    public final static ReporterIon iTRAQ8Plex_113 = new ReporterIon("iTRAQ8Plex_113", getComposition("iTRAQ8Plex_113"));
    /**
     * Standard reporter ion iTRAQ 8Plex 114.
     */
    public final static ReporterIon iTRAQ8Plex_114 = new ReporterIon("iTRAQ8Plex_114", getComposition("iTRAQ8Plex_114"));
    /**
     * Standard reporter ion iTRAQ 8Plex 115.
     */
    public final static ReporterIon iTRAQ8Plex_115 = new ReporterIon("iTRAQ8Plex_115", getComposition("iTRAQ8Plex_115"));
    /**
     * Standard reporter ion iTRAQ 8Plex 116.
     */
    public final static ReporterIon iTRAQ8Plex_116 = new ReporterIon("iTRAQ8Plex_116", getComposition("iTRAQ8Plex_116"));
    /**
     * Standard reporter ion iTRAQ 8Plex 117.
     */
    public final static ReporterIon iTRAQ8Plex_117 = new ReporterIon("iTRAQ8Plex_117", getComposition("iTRAQ8Plex_117"));
    /**
     * Standard reporter ion iTRAQ 8Plex 118.
     */
    public final static ReporterIon iTRAQ8Plex_118 = new ReporterIon("iTRAQ8Plex_118", getComposition("iTRAQ8Plex_118"));
    /**
     * Standard reporter ion iTRAQ 8Plex 119.
     */
    public final static ReporterIon iTRAQ8Plex_119 = new ReporterIon("iTRAQ8Plex_119", getComposition("iTRAQ8Plex_119"));
    /**
     * Standard reporter ion iTRAQ 8Plex 121.
     */
    public final static ReporterIon iTRAQ8Plex_121 = new ReporterIon("iTRAQ8Plex_121", getComposition("iTRAQ8Plex_121"));
    /**
     * Standard reporter ion iTRAQ (reporter + balancer).
     */
    public final static ReporterIon iTRAQ_145 = new ReporterIon("iTRAQ145", 145.1); // @TODO: add the actual composition
    /**
     * Standard reporter ion iTRAQ (reporter + balancer).
     */
    public final static ReporterIon iTRAQ_305 = new ReporterIon("iTRAQ305", 305.2); // @TODO: add the actual composition
    /**
     * Standard reporter ion TMT 126.
     */
    public final static ReporterIon TMT_126 = new ReporterIon("TMT_126", getComposition("TMT_126"));
    /**
     * Standard reporter ion TMT 127N.
     */
    public final static ReporterIon TMT_127N = new ReporterIon("TMT_127N", getComposition("TMT_127N"));
    /**
     * Standard reporter ion TMT 127C.
     */
    public final static ReporterIon TMT_127C = new ReporterIon("TMT_127C", getComposition("TMT_127C"));
    /**
     * Standard reporter ion TMT 128N.
     */
    public final static ReporterIon TMT_128N = new ReporterIon("TMT_128N", getComposition("TMT_128N"));
    /**
     * Standard reporter ion TMT 128C.
     */
    public final static ReporterIon TMT_128C = new ReporterIon("TMT_128C", getComposition("TMT_128C"));
    /**
     * Standard reporter ion TMT 129N.
     */
    public final static ReporterIon TMT_129N = new ReporterIon("TMT_129N", getComposition("TMT_129N"));
    /**
     * Standard reporter ion TMT 129C.
     */
    public final static ReporterIon TMT_129C = new ReporterIon("TMT_129C", getComposition("TMT_129C"));
    /**
     * Standard reporter ion TMT 130N.
     */
    public final static ReporterIon TMT_130N = new ReporterIon("TMT_130N", getComposition("TMT_130N"));
    /**
     * Standard reporter ion TMT 130C.
     */
    public final static ReporterIon TMT_130C = new ReporterIon("TMT_130C", getComposition("TMT_130C"));
    /**
     * Standard reporter ion TMT 131.
     */
    public final static ReporterIon TMT_131 = new ReporterIon("TMT_131", getComposition("TMT_131"));
    /**
     * Standard reporter ion TMT 126 with ETD fragmentation.
     */
    public final static ReporterIon TMT_126_ETD = new ReporterIon("TMT_126_ETD", getComposition("TMT_126_ETD"));
    /**
     * Standard reporter ion TMT 127N with ETD fragmentation.
     */
    public final static ReporterIon TMT_127N_ETD = new ReporterIon("TMT_127N_ETD", getComposition("TMT_127N_ETD"));
    /**
     * Standard reporter ion TMT 127C with ETD fragmentation.
     */
    public final static ReporterIon TMT_127C_ETD = new ReporterIon("TMT_127C_ETD", getComposition("TMT_127C_ETD"));
    /**
     * Standard reporter ion TMT 128N with ETD fragmentation.
     */
    public final static ReporterIon TMT_128N_ETD = new ReporterIon("TMT_128N_ETD", getComposition("TMT_128N_ETD"));
    /**
     * Standard reporter ion TMT 128C with ETD fragmentation.
     */
    public final static ReporterIon TMT_128C_ETD = new ReporterIon("TMT_128C_ETD", getComposition("TMT_128C_ETD"));
    /**
     * Standard reporter ion TMT 129N with ETD fragmentation.
     */
    public final static ReporterIon TMT_129N_ETD = new ReporterIon("TMT_129N_ETD", getComposition("TMT_129N_ETD"));
    /**
     * Standard reporter ion TMT 129C with ETD fragmentation.
     */
    public final static ReporterIon TMT_129C_ETD = new ReporterIon("TMT_129C_ETD", getComposition("TMT_129C_ETD"));
    /**
     * Standard reporter ion TMT 130N with ETD fragmentation.
     */
    public final static ReporterIon TMT_130N_ETD = new ReporterIon("TMT_130N_ETD", getComposition("TMT_130N_ETD"));
    /**
     * Standard reporter ion TMT 130C with ETD fragmentation.
     */
    public final static ReporterIon TMT_130C_ETD = new ReporterIon("TMT_130C_ETD", getComposition("TMT_130C_ETD"));
    /**
     * Standard reporter ion TMT 131 with ETD fragmentation.
     */
    public final static ReporterIon TMT_131_ETD = new ReporterIon("TMT_131_ETD", getComposition("TMT_131_ETD"));
    /**
     * Standard reporter ion TMT (reporter + balancer).
     */
    public final static ReporterIon TMT_230 = new ReporterIon("TMT230", 230.2); // @TODO: add the actual composition
    /**
     * Standard reporter ion TMT (reporter + balancer).
     */
    public final static ReporterIon TMT_226 = new ReporterIon("TMT226", 226.2); // @TODO: add the actual composition
    /**
     * Standard reporter ion for lysine acetylation (PMID: 18338905).
     */
    public final static ReporterIon ACE_K_126 = new ReporterIon("aceK126", new AtomChain("C7H11ON"));
    /**
     * Standard reporter ion for lysine acetylation (PMID: 18338905).
     */
    public final static ReporterIon ACE_K_143 = new ReporterIon("aceK143", new AtomChain("C7H14ON2"));
    /**
     * Standard reporter ion for phosphorylation of tyrosine (PMID: 11473401).
     */
    public final static ReporterIon PHOSPHO_Y = new ReporterIon("pY", new AtomChain("C8H10PO4"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQ = new ReporterIon("QQ", new AtomChain("C10H16N4O4"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQ_H2O = new ReporterIon("QQ-H2O", new AtomChain("C10H14N4O3"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQT = new ReporterIon("QQT", new AtomChain("C14H24N5O6"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQT_H2O = new ReporterIon("QQT-H2O", new AtomChain("C14H22N5O5"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQTG = new ReporterIon("QQTG", new AtomChain("C16H26N6O7"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQTG_H2O = new ReporterIon("QQTG-H2O", new AtomChain("C16H24N6O6"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQTGG = new ReporterIon("QQTGG", new AtomChain("C18H29N7O8"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQTGG_H2O = new ReporterIon("QQTGG-H2O", new AtomChain("C18H27N7O7"));
    /**
     * Ion name for user defined ions.
     */
    private String name;

    /**
     * Constructor for a user-defined reporter ion. The reporter ion is saved in
     * a static map by default and can be retrieved using the static methods.
     * See getReporterIon(String name).
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
     * @param save if true the reporter ion will be saved in the static map for
     * later reuse
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
     * Constructor for a user-defined reporter ion. The reporter ion is saved in
     * a static map by default and can be retrieved using the static methods.
     * See getReporterIon(String name).
     *
     * @param name name of the reporter ion. Should be unique to the ion
     * @param atomChain the atomic composition of this ion
     */
    public ReporterIon(String name, AtomChain atomChain) {
        this(name, atomChain, true);
    }

    /**
     * Constructor for a user-defined reporter ion.
     *
     * @param name name of the reporter ion. Should be unique to the ion.
     * @param atomChain the atomic composition of this ion
     * @param save if true the reporter ion will be saved in the static map for
     * later reuse
     */
    public ReporterIon(String name, AtomChain atomChain, boolean save) {
        type = IonType.REPORTER_ION;
        this.name = name;
        this.atomChain = atomChain;
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
        return theoreticMass.doubleValue() == anotherReporterIon.getTheoreticMass(); // @TODO: compare against the accuracy!
    }

    /**
     * Returns the index of a reporter ion. (i.e. its rounded m/z: 114 for iTRAQ
     * 114).
     *
     * @return the index of a reporter ion.
     */
    public int getIndex() {
        return getTheoreticMass().intValue();
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

    /**
     * Convenience method returning the mass + the mass of a proton.
     *
     * @return the mass + the mass of a proton
     */
    public Double getProtonatedMass() {
        return getTheoreticMass() + ElementaryIon.proton.getTheoreticMass();
    }

    /**
     * Returns the atomic composition of the reporter ion of the given name.
     *
     * @param reporterIonName the name of the reporter ion of interest
     *
     * @return the atomic composition of the reporter ion of the given name
     */
    private static AtomChain getComposition(String reporterIonName) {
        if (reporterIonName.equals("iTRAQ4Plex_114")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 1);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 2);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ4Plex_115")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 4);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 2);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ4Plex_116")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 4);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ4Plex_117")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 3);
            atomChain.append(new AtomImpl(Atom.C, 1), 3);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ4Plex_118")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 4);
            atomChain.append(new AtomImpl(Atom.C, 1), 3);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ8Plex_113")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 6);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 2);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ8Plex_114")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 1);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 2);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ8Plex_115")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 4);
            atomChain.append(new AtomImpl(Atom.C, 1), 1);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ8Plex_116")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 4);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ8Plex_117")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 3);
            atomChain.append(new AtomImpl(Atom.C, 1), 3);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ8Plex_118")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 3);
            atomChain.append(new AtomImpl(Atom.C, 1), 3);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 1), 2);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ8Plex_119")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 2);
            atomChain.append(new AtomImpl(Atom.C, 1), 4);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 1), 2);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ8Plex_121")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 1), 6);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 1), 2);
            return atomChain;
        } else if (reporterIonName.equals("iTRAQ145")) {
            //@TODO!
        } else if (reporterIonName.equals("iTRAQ305")) {
            //@TODO!
        } else if (reporterIonName.equals("TMT_126")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 8);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_127N")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 8);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_127C")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 7);
            atomChain.append(new AtomImpl(Atom.C, 1), 1);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_128N")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 7);
            atomChain.append(new AtomImpl(Atom.C, 1), 1);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_128C")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 6);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_129N")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 6);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_129C")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 3);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_130N")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 3);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_130C")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 4);
            atomChain.append(new AtomImpl(Atom.C, 1), 4);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_131")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 4);
            atomChain.append(new AtomImpl(Atom.C, 1), 4);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_126_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 7);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_127N_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 7);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_127C_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 7);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_128N_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 7);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_128C_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_129N_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_129C_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_130N_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 2);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_130C_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 3);
            atomChain.append(new AtomImpl(Atom.C, 1), 4);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            return atomChain;
        } else if (reporterIonName.equals("TMT_131_ETD")) {
            AtomChain atomChain = new AtomChain();
            atomChain.append(new AtomImpl(Atom.C, 0), 3);
            atomChain.append(new AtomImpl(Atom.C, 1), 4);
            atomChain.append(new AtomImpl(Atom.H, 0), 15);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
            return atomChain;
        }
        throw new UnsupportedOperationException("Atomic composition not implemented for reporter ion " + reporterIonName + ".");
    }
}
