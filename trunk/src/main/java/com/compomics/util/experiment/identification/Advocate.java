package com.compomics.util.experiment.identification;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The advocate of a hit can be a search engine, a sequencing algorithm, a
 * rescoring algorithm, etc.
 *
 * @author Marc Vaudel
 */
public class Advocate {

    /**
     * The different types of advocates.
     */
    public enum AdvocateType {

        search_engine, sequencing_algorithm, spectral_library, rescoring_algorithm, multiple_algorithm_software, unknown;
    }
    /**
     * The Mascot search engine.
     */
    public static final Advocate mascot = new Advocate(0, "Mascot", AdvocateType.search_engine, new java.awt.Color(255, 153, 255));
    /**
     * The OMSSA search engine.
     */
    public static final Advocate omssa = new Advocate(1, "OMSSA", AdvocateType.search_engine, new java.awt.Color(153, 153, 255));
    /**
     * The X!Tandem search engine.
     */
    public static final Advocate xtandem = new Advocate(2, "X!Tandem", AdvocateType.search_engine, new java.awt.Color(153, 255, 255));
    /**
     * The PepNovo+ de novo sequencing algorithm.
     */
    public static final Advocate pepnovo = new Advocate(3, "PepNovo+", AdvocateType.sequencing_algorithm, new java.awt.Color(224, 130, 20));
    /**
     * The Andromeda search engine.
     */
    public static final Advocate andromeda = new Advocate(4, "Andromeda", AdvocateType.search_engine);
    /**
     * The MS Amanda search engine.
     */
    public static final Advocate msAmanda = new Advocate(5, "MS Amanda", AdvocateType.search_engine, new java.awt.Color(216, 191, 216));
    /**
     * The PeptideShaker multiple algorithm software.
     */
    public static final Advocate peptideShaker = new Advocate(6, "PeptideShaker", AdvocateType.multiple_algorithm_software, new Color(110, 196, 97));
    /**
     * The MS-GF+ search engine.
     */
    public static final Advocate msgf = new Advocate(7, "MS-GF+", AdvocateType.search_engine, new java.awt.Color(205, 92, 92));
    /**
     * The DirecTag sequencing algorithm.
     */
    public static final Advocate direcTag = new Advocate(8, "DirecTag", AdvocateType.sequencing_algorithm, new java.awt.Color(189, 183, 107));
    /**
     * The byonic search engine integrated in the byonic protein metrics
     * interface.
     */
    public static final Advocate byonic = new Advocate(9, "Byonic", AdvocateType.search_engine);
    /**
     * The Comet search engine, free version of Sequest.
     */
    public static final Advocate comet = new Advocate(10, "Comet", AdvocateType.search_engine);
    /**
     * The ProteinLynx search engine, waters instruments.
     */
    public static final Advocate proteinLynx = new Advocate(11, "ProteinLynx", AdvocateType.search_engine);
    /**
     * The MS-Fit search engine, old school peptide mass fingerprinting.
     */
    public static final Advocate msFit = new Advocate(12, "MS-Fit", AdvocateType.search_engine);
    /**
     * The MyriMatch search engine, old school peptide mass fingerprinting.
     */
    public static final Advocate myriMatch = new Advocate(13, "MyriMatch", AdvocateType.search_engine, new Color(241, 226, 204));
    /**
     * The PEAKS sequencing algorithm.
     */
    public static final Advocate peaks = new Advocate(14, "PEAKS", AdvocateType.sequencing_algorithm);
    /**
     * The phenyx search engine.
     */
    public static final Advocate phenyx = new Advocate(15, "Phenyx", AdvocateType.search_engine);
    /**
     * The profound search engine.
     */
    public static final Advocate proFound = new Advocate(16, "ProFound", AdvocateType.search_engine);
    /**
     * The search engine results of protein prospector.
     */
    public static final Advocate proteinProspector = new Advocate(17, "ProteinProspector", AdvocateType.search_engine);
    /**
     * The search engine integrated in protein scape, Bruker instruments.
     */
    public static final Advocate proteinScape = new Advocate(18, "ProteinScape", AdvocateType.search_engine);
    /**
     * The sequest search engine.
     */
    public static final Advocate sequest = new Advocate(19, "SEQUEST", AdvocateType.search_engine);
    /**
     * The SeQuence IDentfication (SQID) search engine.
     */
    public static final Advocate sqid = new Advocate(20, "SQID", AdvocateType.search_engine);
    /**
     * The scaffold multiple search engine data interpretation software.
     */
    public static final Advocate scaffold = new Advocate(21, "Scaffold", AdvocateType.multiple_algorithm_software);
    /**
     * The sonar search engine, integrated in radars.
     */
    public static final Advocate sonar = new Advocate(22, "sonar", AdvocateType.search_engine);
    /**
     * The SpectraST spectral library search engine (TPP).
     */
    public static final Advocate spectraST = new Advocate(23, "SpectraST", AdvocateType.spectral_library);
    /**
     * The Spectrum Mill search engine, Agilent.
     */
    public static final Advocate spectrumMill = new Advocate(24, "SpectrumMill", AdvocateType.search_engine);
    /**
     * The ZCore search engine, etd search engine.
     */
    public static final Advocate zCore = new Advocate(25, "ZCore", AdvocateType.search_engine);
    /**
     * The percolator rescoring algorithm.
     */
    public static final Advocate percolator = new Advocate(26, "Percolator", AdvocateType.rescoring_algorithm);
    /**
     * Advocate type for mzId files where no software is annotated.
     */
    public static final Advocate genericMzId = new Advocate(100, "mzid", AdvocateType.unknown);
    /**
     * Map of user defined advocates indexed by index.
     */
    private static HashMap<Integer, Advocate> userAdvocates = new HashMap<Integer, Advocate>();
    /**
     * The index of the advocate.
     */
    private final int index;
    /**
     * The name of the advocate.
     */
    private final String name;
    /**
     * The type of advocate
     */
    private final AdvocateType type;
    /**
     * The color of the advocate. Defaults to light gray.
     */
    private Color color = Color.lightGray;
    /**
     * The search engine color map.
     */
    private static HashMap<Integer, java.awt.Color> advocateColorMap;
    /**
     * The search engine tool tip map.
     */
    private static HashMap<Integer, String> advocateToolTipMap;

    /**
     * Constructor.
     *
     * @param index the index of the advocate
     * @param name the name of the advocate, should be identical to the one
     * present in the result file
     * @param type the type of advocate
     * @param color the color of the advocate
     */
    private Advocate(int index, String name, AdvocateType type, Color color) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.color = color;
    }

    /**
     * Constructor. Advocate color will be light gray.
     *
     * @param index the index of the advocate
     * @param name the name of the advocate, should be identical to the one
     * present in the result file
     * @param type the type of advocate
     */
    private Advocate(int index, String name, AdvocateType type) {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    /**
     * Constructor for an advocate of unknown type. Advocate color will be light
     * gray.
     *
     * @param index the index of the advocate
     * @param name the name of the advocate, should be identical to the one
     * present in the result file
     */
    private Advocate(int index, String name) {
        this.index = index;
        this.name = name;
        this.type = AdvocateType.unknown;
    }

    /**
     * Returns the index of the advocate.
     *
     * @return the index of the advocate
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the name of the advocate.
     *
     * @return the name of the advocate
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of advocate.
     *
     * @return the type of advocate
     */
    public AdvocateType getType() {
        return type;
    }

    /**
     * Returns the color of the advocate.
     *
     * @return the color of the advocate
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the implemented advocates in an array.
     *
     * @return the implemented advocates in an array
     */
    public static Advocate[] values() {
        Advocate[] result = new Advocate[28 + userAdvocates.size()];
        int i = 0;
        result[i] = mascot;
        result[++i] = omssa;
        result[++i] = xtandem;
        result[++i] = pepnovo;
        result[++i] = andromeda;
        result[++i] = msAmanda;
        result[++i] = peptideShaker;
        result[++i] = msgf;
        result[++i] = direcTag;
        result[++i] = byonic;
        result[++i] = comet;
        result[++i] = proteinLynx;
        result[++i] = msFit;
        result[++i] = myriMatch;
        result[++i] = peaks;
        result[++i] = phenyx;
        result[++i] = proFound;
        result[++i] = proteinProspector;
        result[++i] = proteinScape;
        result[++i] = sequest;
        result[++i] = sqid;
        result[++i] = scaffold;
        result[++i] = sonar;
        result[++i] = spectraST;
        result[++i] = spectrumMill;
        result[++i] = zCore;
        result[++i] = percolator;
        result[++i] = genericMzId;

        for (Advocate advocate : userAdvocates.values()) {
            result[++i] = advocate;
        }
        return result;
    }

    /**
     * Returns the advocate corresponding to the given index. Null if not found.
     *
     * @param index the index of the advocate
     *
     * @return the advocate of interest
     */
    public static Advocate getAdvocate(int index) {
        for (Advocate advocate : values()) {
            if (advocate.getIndex() == index) {
                return advocate;
            }
        }
        return null;
    }

    /**
     * Returns the advocate with the given name. Null if not recognized.
     *
     * @param advocateName the name of the advocate of interest
     *
     * @return the advocate with the given name
     */
    public static Advocate getAdvocate(String advocateName) {

        // check the default advocates
        for (Advocate advocate : values()) {
            if (advocate.getName().equals(advocateName)) {
                return advocate;
            }
        }

        // check the user advocates
        Iterator<Integer> iterator = userAdvocates.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();

            Advocate advocate = userAdvocates.get(key);

            if (advocate.getName().equals(advocateName)) {
                return advocate;
            }
        }

        // unknown advocate
        return null;
    }

    /**
     * Adds a user advocate and returns it.
     *
     * @param advocateName the name of the advocate
     *
     * @return the new advocate
     */
    public static Advocate addUserAdvocate(String advocateName) {
        int maxIndex = 0;
        for (Advocate advocate : values()) {
            int advocateIndex = advocate.getIndex();
            if (advocateIndex >= maxIndex) {
                maxIndex = advocateIndex + 1;
            }
        }
        Advocate newAdvocate = new Advocate(maxIndex, advocateName);
        userAdvocates.put(maxIndex, newAdvocate);
        return newAdvocate;
    }

    /**
     * Returns the map of user advocates imported.
     *
     * @return the map of user advocates imported
     */
    public static HashMap<Integer, Advocate> getUserAdvocates() {
        return userAdvocates;
    }

    /**
     * Returns the map of user advocates imported.
     *
     * @param userAdvocates the map of user advocates
     */
    public static void setUserAdvocates(HashMap<Integer, Advocate> userAdvocates) {
        Advocate.userAdvocates = userAdvocates;
    }

    /**
     * Returns the PubMed id of the reference of the advocate of interest.
     *
     * @return the PubMed id of the reference of the advocate of interest
     */
    public String getPmid() {
        if (this == mascot) {
            return "10612281";
        } else if (this == omssa) {
            return "15473683";
        } else if (this == xtandem) {
            return "14976030";
        } else if (this == pepnovo) {
            return "15858974";
        } else if (this == andromeda) {
            return "21254760";
        } else if (this == direcTag) {
            return "18630943";
        } else if (this == msAmanda) {
            return "24909410";
        } else if (this == msgf) {
            return "20829449"; // @TODO: update to the real ms-gf+ citation when available
        } else if (this == myriMatch) {
            return "17269722 ";
        } else {
            return null;
        }
    }

    /**
     * Returns the advocate color map. Key is the advocate index and the element
     * the advocate color.
     *
     * @return the advocate color map
     */
    public static HashMap<Integer, java.awt.Color> getAdvocateColorMap() {

        if (advocateColorMap == null) {
            advocateColorMap = new HashMap<Integer, Color>();

            for (Advocate tempAdvoate : values()) {
                advocateColorMap.put(tempAdvoate.getIndex(), tempAdvoate.getColor());
            }
        }

        return advocateColorMap;
    }

    /**
     * Returns the advocate tool tip map. Key is the advocate index and the
     * element the advocate name.
     *
     * @return the advocate tool tip map
     */
    public static HashMap<Integer, String> getAdvocateToolTipMap() {

        if (advocateToolTipMap == null) {
            advocateToolTipMap = new HashMap<Integer, String>();

            for (Advocate tempAdvoate : values()) {
                advocateToolTipMap.put(tempAdvoate.getIndex(), tempAdvoate.getName());
            }
        }

        return advocateToolTipMap;
    }

    /**
     * Returns the advocate based on the identification file name. Null if not
     * found. Note: this method implements a limited number of algorithms and
     * does not support generic files. Use the IdfileReader when possible.
     * Implemented formats: omx -> OMSSA dat -> Mascot xml -> X!Tandem csv ->
     * MS-Amanda
     *
     * @param idFileName the name of the identification file
     *
     * @return the advocate likely to have been used to create the given file
     */
    public static Advocate getAdvocateFromFile(String idFileName) {
        if (idFileName.toLowerCase().endsWith("dat")) {
            return Advocate.mascot;
        } else if (idFileName.toLowerCase().endsWith("omx")) {
            return Advocate.omssa;
        } else if (idFileName.toLowerCase().endsWith("xml")) {
            return Advocate.xtandem;
        } else if (idFileName.toLowerCase().endsWith("mzid")) {
            return Advocate.msgf;
        } else if (idFileName.toLowerCase().endsWith("csv")) {
            return Advocate.msAmanda;
        }
        return null;
    }
}
