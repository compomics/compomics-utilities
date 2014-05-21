package com.compomics.util.experiment.identification;

import java.util.HashMap;

/**
 * The advocate of a hit can be a search engine, a sequencing algorithm, a rescoring algorithm, etc.
 *
 * @author Marc Vaudel
 */
public class Advocate {

    /**
     * The different types of advocates
     */
    public enum AdvocateType {
        search_engine, sequencing_algorithm, spectral_library, rescoring_algorithm, multiple_algorithm_software, unknown;
    }
    /**
     * The Mascot search engine.
     */
    public static final Advocate mascot = new Advocate(0, "Mascot", AdvocateType.search_engine);
    /**
     * The OMSSA search engine.
     */
    public static final Advocate omssa = new Advocate(1, "OMSSA", AdvocateType.search_engine);
    /**
     * The X!Tandem search engine.
     */
    public static final Advocate xtandem = new Advocate(2, "X!Tandem", AdvocateType.search_engine);
    /**
     * The pepnovo+ de novo sequencing algorithm.
     */
    public static final Advocate pepnovo = new Advocate(3, "PepNovo+", AdvocateType.sequencing_algorithm);
    /**
     * The Andromeda search engine.
     */
    public static final Advocate andromeda = new Advocate(4, "Andromeda", AdvocateType.search_engine);
    /**
     * The MS Amanda search engine.
     */
    public static final Advocate msAmanda = new Advocate(5, "MS Amanda", AdvocateType.search_engine);
    /**
     * The PeptideShaker multiple algorithm software.
     */
    public static final Advocate peptideShaker = new Advocate(6, "PeptideShaker", AdvocateType.multiple_algorithm_software);
    /**
     * The MS-GF+ search engine.
     */
    public static final Advocate msgf = new Advocate(7, "MS-GF+", AdvocateType.search_engine);
    /**
     * The DirecTag sequencing algorithm.
     */
    public static final Advocate direcTag = new Advocate(8, "DirecTag", AdvocateType.sequencing_algorithm);
    /**
     * Map of user defined advocates indexed by index
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
     * Constructor.
     *
     * @param index the index of the advocate
     * @param name the name of the advocate, should be identical to the one present in the result file
     * @param type the type of advocate
     */
    private Advocate(int index, String name, AdvocateType type) {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    /**
     * Constructor for an advocate of unknown type.
     *
     * @param index the index of the advocate
     * @param name the name of the advocate, should be identical to the one present in the result file
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
     * Returns the implemented advocates in an array.
     * 
     * @return the implemented advocates in an array
     */
    public static Advocate[] values() {
        Advocate[] result = new Advocate[9 + userAdvocates.size()];
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
        Advocate userAdvocate = userAdvocates.get(advocateName);
        if (userAdvocate != null) {
            return userAdvocate;
        }
        for (Advocate advocate : values()) {
            if (advocate.getName().equals(advocateName)) {
                return advocate;
            }
        }
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
     * Returns the pubmed id of the reference of the advocate of interest.
     *
     * @return the pubmed id of the reference of the advocate of interest
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
        } else {
            return null;
        }
    }
}
