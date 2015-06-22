/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.protein_sequences_manager.gui.taxonomy;

/**
 *
 * @author Kenneth
 */
public enum QueryType {

    FASTA("uniprot"), TAXONOMY("taxonomy");
    private final String location;

    private QueryType(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }
}
