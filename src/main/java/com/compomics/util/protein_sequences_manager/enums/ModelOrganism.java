package com.compomics.util.protein_sequences_manager.enums;

/**
 * ModelOrganism enumerator.
 *
 * @author Kenneth Verheggen
 */
public enum ModelOrganism {

    arabidopsis(3702),
    celegans(6239),
    chicken(9031),
    cow(9913),
    dog(9615),
    drosophila(7227),
    human(9606),
    mouse(1090),
    pig(9823),
    rat(10116),
    yeast(559292),
    zebrafish(7955);

    /**
     * The organism.
     */
    private final int organism;

    /**
     * Constructor.
     * 
     * @param organism the organism
     */
    private ModelOrganism(int organism) {
        this.organism = organism;
    }

    /**
     * Returns the taxonomy.
     * 
     * @return the taxonomy
     */
    public int getTaxonomyID() {
        return organism;
    }
}
