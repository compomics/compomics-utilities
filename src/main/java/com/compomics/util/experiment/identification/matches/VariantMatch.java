/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 *
 * @author dominik.kopczynski
 */
public class VariantMatch extends ExperimentObject {
    
    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 7129515983284796207L;
    
    /**
     * Enumeration naming all variants
     */
    static public enum Variant {deletion, insertion, substitution};
    
    /**
     * What type is the variant of?
     */
    private Variant variantType;
    
    /**
     * The location in the sequence, 1 is the first residue.
     */
    private int variantSite;
    
    /**
     * Inserted / substituted amino acid, '*' for a deletion
     */
    private char alternativeAa;
    

    /**
     * Constructor for a variaton match.
     *
     * @param variantSite The location in the sequence
     * @param variantType What type is the variant of?
     */
    public VariantMatch(int variantSite, Variant variantType) {
        this.variantSite = variantSite;
        this.variantType = variantType;
        this.alternativeAa = '*';
    }
    

    /**
     * Constructor for a variaton match.
     *
     * @param variantSite The location in the sequence
     * @param variantType What type is the variant of?
     * @param alternativeAa Inserted / substituted amino acid
     */
    public VariantMatch(int variantSite, Variant variantType, char alternativeAa) {
        this.variantSite = variantSite;
        this.variantType = variantType;
        this.alternativeAa = alternativeAa;
    }
    
    /**
     * Getter for the variant type.
     *
     * @return the variant type
     */
    public Variant getVariantType(){
        return variantType;
    }
    
    /**
     * Sets the variant type.
     *
     * @param variantType the variant type
     */
    public void setVariantType(Variant variantType){
        this.variantType = variantType;
    }
    
    
    /**
     * Getter for the variant site.
     *
     * @return the variant site
     */
    public int getVariantSite(){
        return variantSite;
    }
    
    /**
     * Sets the variant type.
     *
     * @param variantSite the variant site
     */
    public void setVariantSite(int variantSite){
        this.variantSite = variantSite;
    }
    
    
    /**
     * Getter for the alternative amino acid.
     *
     * @return the alternative amino acid
     */
    public char getAlternativeAa(){
        return alternativeAa;
    }
    
    /**
     * Sets the variant type.
     *
     * @param alternativeAa the alternative amino acid
     */
    public void setAlternativeAa(char alternativeAa){
        this.alternativeAa = alternativeAa;
    }
}
