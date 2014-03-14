package com.compomics.util.experiment.identification;

/**
 * The advocate of a hit can be a search engine, a re-scoring algorithm, etc.
 *
 * @author Marc Vaudel
 */
public enum Advocate {

    /**
     * The search engine Mascot.
     */
    Mascot(0, "Mascot"),
    /**
     * The search engine OMSSA.
     */
    OMSSA(1, "OMSSA"),
    /**
     * The search engine X!Tandem.
     */
    XTandem(2, "X!Tandem"),
    /**
     * The de novo algorithm PepNovo+.
     */
    pepnovo(3, "PepNovo+"),
    /**
     * The search engine Andromeda.
     */
    andromeda(4, "Andromeda"),
    /**
     * The search engine MS-Amanda.
     */
    msAmanda(5, "MS Amanda"),
    /**
     * The post processing tool PeptideShaker.
     */
    PeptideShaker(6, "PeptideShaker"),
    /**
     * The search engine MS-GF+.
     */
    MSGF(7, "MS-GF+"),
    /**
     * The de novo algorithm DirecTag.
     */
    DirecTag(8, "DirecTag");
    /**
     * The index of the advocate.
     */
    private final int index;
    /**
     * The name of the advocate.
     */
    private final String name;
    
    /**
     * Constructor.
     *
     * @param index
     * @param name
     */
    private Advocate(int index, String name) {
        this.index = index;
        this.name = name;
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
}
