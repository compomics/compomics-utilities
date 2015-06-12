/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.protein_sequences_manager.enums;

/**
 *
 * @author Kenneth
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

    private final int organism;

    private ModelOrganism(int organism) {
        this.organism = organism;
    }

    public int getTaxonomyID() {
        return organism;
    }

}
