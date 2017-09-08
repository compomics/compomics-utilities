package com.compomics.util.experiment.biology.ions.impl;

import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * This class models a reporter ion and is its own factory.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
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
     * List of sorted implemented ions.
     */
    private static ArrayList<String> sortedImplementedIonsNames = null;
    /**
     * The possible subtypes as list of indexes.
     */
    private static ArrayList<Integer> possibleSubtypes = null;
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
    public final static ReporterIon iTRAQ_145 = new ReporterIon("iTRAQ145", 144.1); // @TODO: add the actual composition
    /**
     * Standard reporter ion iTRAQ (reporter + balancer).
     */
    public final static ReporterIon iTRAQ_305 = new ReporterIon("iTRAQ305", 304.2); // @TODO: add the actual composition
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
    public final static ReporterIon TMT_230 = new ReporterIon("TMT230", 229.2); // @TODO: add the actual composition
    /**
     * Standard reporter ion TMT (reporter + balancer).
     */
    public final static ReporterIon TMT_226 = new ReporterIon("TMT226", 225.2); // @TODO: add the actual composition
    /**
     * Standard reporter ion for lysine acetylation (PMID: 18338905).
     */
    public final static ReporterIon ACE_K_126 = new ReporterIon("aceK126", AtomChain.getAtomChain("C(7)H(11)ON"));
    /**
     * Standard reporter ion for lysine acetylation (PMID: 18338905).
     */
    public final static ReporterIon ACE_K_143 = new ReporterIon("aceK143", AtomChain.getAtomChain("C(7)H(14)ON(2)"));
    /**
     * Standard reporter ion for phosphorylation of tyrosine (PMID: 11473401).
     */
    public final static ReporterIon PHOSPHO_Y = new ReporterIon("pY", AtomChain.getAtomChain("C(8)H(10)NPO(4)"));
    /**
     * Standard reporter ion for formylation of K (PMID: 24895383).
     */
    public final static ReporterIon FORMYL_K = new ReporterIon("fK112", AtomChain.getAtomChain("C(6)H(9)NO"));
    /**
     * Standard reporter ion for methylation of R.
     */
    public final static ReporterIon METHYL_R_87 = new ReporterIon("metR87", AtomChain.getAtomChain("C(4)H(10)N(2)"));
    /**
     * Standard reporter ion for methylation of R (PMID: 16335983).
     */
    public final static ReporterIon METHYL_R_112 = new ReporterIon("metR112", AtomChain.getAtomChain("C(5)H(9)N(3)"));
    /**
     * Standard reporter ion for methylation of R (PMID: 16335983).
     */
    public final static ReporterIon METHYL_R_115 = new ReporterIon("metR115", AtomChain.getAtomChain("C(5)H(10)N(2)O"));
    /**
     * Standard reporter ion for methylation of R (PMID: 16335983).
     */
    public final static ReporterIon METHYL_R_143 = new ReporterIon("metR143", AtomChain.getAtomChain("C(6)H(14)N(4)"));
    /**
     * Standard reporter ion for methylation of R (PMID: 16335983).
     */
    public final static ReporterIon METHYL_R_70 = new ReporterIon("metR70", AtomChain.getAtomChain("C(4)H(7)N"));
    /**
     * Standard reporter ion for di-methylation of R (PMID: 16335983).
     */
    public final static ReporterIon DI_METHYL_R_112 = new ReporterIon("dimetR112", AtomChain.getAtomChain("C(5)H(9)N(3)"));
    /**
     * Standard reporter ion for di-methylation of R (PMID: 16335983).
     */
    public final static ReporterIon DI_METHYL_R_115 = new ReporterIon("dimetR115", AtomChain.getAtomChain("C(5)H(10)N(2)O"));
    /**
     * Standard reporter ion for di-methylation of R (PMID: 16335983).
     */
    public final static ReporterIon DI_METHYL_R_157 = new ReporterIon("metR157", AtomChain.getAtomChain("C(7)H(16)N(4)"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQ = new ReporterIon("QQ", AtomChain.getAtomChain("C(10)H(16)N(4)O(4)"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQ_H2O = new ReporterIon("QQ-H2O", AtomChain.getAtomChain("C(10)H(14)N(4)O(3)"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQT = new ReporterIon("QQT", AtomChain.getAtomChain("C(14)H(24)N(5)O(6)"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQT_H2O = new ReporterIon("QQT-H2O", AtomChain.getAtomChain("C(14)H(22)N(5)O(5)"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQTG = new ReporterIon("QQTG", AtomChain.getAtomChain("C(16)H(26)N(6)O(7)"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQTG_H2O = new ReporterIon("QQTG-H2O", AtomChain.getAtomChain("C(16)H(24)N(6)O(6)"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQTGG = new ReporterIon("QQTGG", AtomChain.getAtomChain("C(18)H(29)N(7)O(8)"));
    /**
     * Standard reporter ion for SUMO-2/3 Q87R.
     */
    public final static ReporterIon QQTGG_H2O = new ReporterIon("QQTGG-H2O", AtomChain.getAtomChain("C(18)H(27)N(7)O(7)"));
    /**
     * Ion name for user defined ions.
     */
    private String name;
    /**
     * The CV term of the reporter ion, null if not set.
     */
    private CvTerm cvTerm = null;

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
        this.theoreticMass1 = mass;
        if (save) {
            implementedIons.put(name, this);
            sortedImplementedIonsNames = null;
            possibleSubtypes = null;
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
            addReporterIon(this);
        }
    }

    /**
     * Adds a reporter ion to the class static map. Reporter ions with the same
     * name will be overwritten.
     *
     * @param reporterIon the reporter ion to add
     */
    public static void addReporterIon(ReporterIon reporterIon) {
        if (implementedIons == null) {
            implementedIons = new HashMap<>();
        }
        implementedIons.put(reporterIon.name, reporterIon);
        sortedImplementedIonsNames = null;
        possibleSubtypes = null;
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
        this.cvTerm = null;
    }

    /**
     * Method to set the mass of the reporter ion.
     *
     * @param referenceMass the mass where the reporter ions should be found
     */
    public void setMass(double referenceMass) {
        this.theoreticMass1 = referenceMass;
    }

    @Override
    public CvTerm getPrideCvTerm() {

        if (cvTerm != null) {
            return cvTerm;
        }

        if (name.contains("TMT")) {

            if (name.equalsIgnoreCase("TMT_126")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "126");
            } else if (name.equalsIgnoreCase("TMT_127N")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "127N");
            } else if (name.equalsIgnoreCase("TMT_127C")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "127C");
            } else if (name.equalsIgnoreCase("TMT_128N")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "128N");
            } else if (name.equalsIgnoreCase("TMT_128C")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "128C");
            } else if (name.equalsIgnoreCase("TMT_129N")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "129N");
            } else if (name.equalsIgnoreCase("TMT_129C")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "129C");
            } else if (name.equalsIgnoreCase("TMT_130N")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "130N");
            } else if (name.equalsIgnoreCase("TMT_130C")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "130C");
            } else if (name.equalsIgnoreCase("TMT_131")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002670", "frag: TMT reporter ion", "131");
            } else if (name.equalsIgnoreCase("TMT_126_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "126");
            } else if (name.equalsIgnoreCase("TMT_127N_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "127N");
            } else if (name.equalsIgnoreCase("TMT_127C_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "127C");
            } else if (name.equalsIgnoreCase("TMT_128N_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "128N");
            } else if (name.equalsIgnoreCase("TMT_128C_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "128C");
            } else if (name.equalsIgnoreCase("TMT_129N_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "129N");
            } else if (name.equalsIgnoreCase("TMT_129C_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "129C");
            } else if (name.equalsIgnoreCase("TMT_130N_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "130N");
            } else if (name.equalsIgnoreCase("TMT_130C_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "130C");
            } else if (name.equalsIgnoreCase("TMT_131_ETD")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002671", "frag: TMT ETD reporter ion", "131");
            }

        } else if (name.contains("iTRAQ")) {
            if (name.equalsIgnoreCase("iTRAQ4Plex_114")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002668", "frag: iTRAQ 4plex reporter ion", "114");
            } else if (name.equalsIgnoreCase("iTRAQ4Plex_115")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002668", "frag: iTRAQ 4plex reporter ion", "115");
            } else if (name.equalsIgnoreCase("iTRAQ4Plex_116")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002668", "frag: iTRAQ 4plex reporter ion", "116");
            } else if (name.equalsIgnoreCase("iTRAQ4Plex_117")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002668", "frag: iTRAQ 4plex reporter ion", "117");
            } else if (name.equalsIgnoreCase("iTRAQ8Plex_113")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002669", "frag: iTRAQ 8plex reporter ion", "113");
            } else if (name.equalsIgnoreCase("iTRAQ8Plex_114")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002669", "frag: iTRAQ 8plex reporter ion", "114");
            } else if (name.equalsIgnoreCase("iTRAQ8Plex_115")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002669", "frag: iTRAQ 8plex reporter ion", "115");
            } else if (name.equalsIgnoreCase("iTRAQ8Plex_116")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002669", "frag: iTRAQ 8plex reporter ion", "116");
            } else if (name.equalsIgnoreCase("iTRAQ8Plex_117")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002669", "frag: iTRAQ 8plex reporter ion", "117");
            } else if (name.equalsIgnoreCase("iTRAQ8Plex_118")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002669", "frag: iTRAQ 8plex reporter ion", "118");
            } else if (name.equalsIgnoreCase("iTRAQ8Plex_119")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002669", "frag: iTRAQ 8plex reporter ion", "119");
            } else if (name.equalsIgnoreCase("iTRAQ8Plex_121")) {
                cvTerm = new CvTerm("PSI-MS", "MS:1002669", "frag: iTRAQ 8plex reporter ion", "121");
            }
        }

        return cvTerm;
    }

    @Override
    public CvTerm getPsiMsCvTerm() {
        return getPrideCvTerm();
    }

    /**
     * Compares the current reporter ion with another one based on their masses.
     *
     * @param anotherReporterIon the other reporter ion
     * @return a boolean indicating whether masses are equal
     */
    public boolean isSameAs(ReporterIon anotherReporterIon) {
        if (atomChain != null && anotherReporterIon.getAtomicComposition() != null) {
            return atomChain.isSameCompositionAs(anotherReporterIon.getAtomicComposition());
        }
        return false;
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
        ArrayList<String> ionList = getSortedImplementedIons();
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
        ArrayList<String> ionList = getSortedImplementedIons();
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
     * Returns a set of possible subtypes.
     *
     * @return a set of possible subtypes
     */
    public static Set<String> getImplementedIons() {
        return implementedIons.keySet();
    }

    /**
     * Returns an ordered list of possible subtypes.
     *
     * @return an ordered list of possible subtypes
     */
    public static ArrayList<String> getSortedImplementedIons() {
        if (sortedImplementedIonsNames == null) {
            ArrayList<String> tempList = new ArrayList<>(getImplementedIons());
            Collections.sort(tempList);
            sortedImplementedIonsNames = tempList;
        }
        return sortedImplementedIonsNames;
    }

    /**
     * Returns an arraylist of possible subtypes.
     *
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        if (possibleSubtypes == null) {
            ArrayList<Integer> tempList = new ArrayList<>(implementedIons.size());
            for (int i = 0; i < implementedIons.size(); i++) {
                tempList.add(i);
            }
            possibleSubtypes = tempList;
        }
        return possibleSubtypes;
    }

    @Override
    public NeutralLoss[] getNeutralLosses() {
        return null;
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
    public ReporterIon clone() {
        return new ReporterIon(name, atomChain.clone(), false);
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
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
            atomChain.append(new AtomImpl(Atom.C, 1), 1);
            atomChain.append(new AtomImpl(Atom.H, 0), 12);
            atomChain.append(new AtomImpl(Atom.N, 0), 1);
            atomChain.append(new AtomImpl(Atom.N, 1), 1);
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
            atomChain.append(new AtomImpl(Atom.C, 0), 5);
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
