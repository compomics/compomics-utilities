package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_generation.Ms2pipFeature;

/**
 * Feature based on the relationship between two amino acid features.
 *
 * @author Marc Vaudel
 */
public class AminoAcidRelationship implements Ms2pipFeature {
    
    /**
     * The different relationships implemented.
     */
    public enum Relationship {
        sum, subtraction, multiplication;
    }
    
    /**
     * The relationship used.
     */
    private Relationship relationship;
    /**
     * The first amino acid feature.
     */
    private AminoAcidFeature aminoAcidFeature1;
    /**
     * The second amino acid feature.
     */
    private AminoAcidFeature aminoAcidFeature2;
    
    /**
     * Constructor.
     * 
     * @param relationship the relationship between the amino acid feature.
     * 
     * @param aminoAcidFeature1 the first amino acid feature
     * @param aminoAcidFeature2 the second amino acid feature
     */
    public AminoAcidRelationship(Relationship relationship, AminoAcidFeature aminoAcidFeature1, AminoAcidFeature aminoAcidFeature2) {
        this.relationship = relationship;
        this.aminoAcidFeature1 = aminoAcidFeature1;
        this.aminoAcidFeature2 = aminoAcidFeature2;
    }

    /**
     * Returns the relationship between the features.
     * 
     * @return the relationship between the features
     */
    public Relationship getRelationship() {
        return relationship;
    }

    /**
     * Sets the relationship between the features.
     * 
     * @param relationship the relationship between the features
     */
    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    /**
     * Returns the first amino acid feature.
     * 
     * @return the first amino acid feature
     */
    public AminoAcidFeature getAminoAcidFeature1() {
        return aminoAcidFeature1;
    }

    /**
     * Sets the first amino acid feature.
     * 
     * @param aminoAcidFeature1 the first amino acid feature
     */
    public void setAminoAcidFeature1(AminoAcidFeature aminoAcidFeature1) {
        this.aminoAcidFeature1 = aminoAcidFeature1;
    }

    /**
     * Returns the second amino acid feature.
     * 
     * @return the second amino acid feature
     */
    public AminoAcidFeature getAminoAcidFeature2() {
        return aminoAcidFeature2;
    }

    /**
     * Sets the second amino acid feature.
     * 
     * @param aminoAcidFeature2 the second amino acid feature
     */
    public void setAminoAcidFeature2(AminoAcidFeature aminoAcidFeature2) {
        this.aminoAcidFeature2 = aminoAcidFeature2;
    }
    
    @Override
    public String getCategory() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return relationship.name() + " between " + aminoAcidFeature1.getDescription() + " and " + aminoAcidFeature2.getDescription();
    }
}
