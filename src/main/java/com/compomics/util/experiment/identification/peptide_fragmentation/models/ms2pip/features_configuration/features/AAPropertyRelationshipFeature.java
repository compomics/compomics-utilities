package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic.AAPropertyFeature;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.MultipleAAPropertyFeature;

/**
 * Feature based on the relationship between two amino acid features.
 *
 * @author Marc Vaudel
 */
public class AAPropertyRelationshipFeature implements Ms2pipFeature, MultipleAAPropertyFeature {
    
    /**
     * The index of this ms2pip feature.
     */
    public static final int index = 8;
    /**
     * The different relationships implemented.
     */
    public enum Relationship {
        addition, subtraction, multiplication;
    }
    
    /**
     * The relationship used.
     */
    private Relationship relationship;
    /**
     * The first amino acid feature.
     */
    private AAPropertyFeature aminoAcidFeature1;
    /**
     * The second amino acid feature.
     */
    private AAPropertyFeature aminoAcidFeature2;
    
    /**
     * Constructor.
     * 
     * @param relationship the relationship between the amino acid feature.
     * 
     * @param aminoAcidFeature1 the first amino acid feature
     * @param aminoAcidFeature2 the second amino acid feature
     */
    public AAPropertyRelationshipFeature(Relationship relationship, AAPropertyFeature aminoAcidFeature1, AAPropertyFeature aminoAcidFeature2) {
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
    public AAPropertyFeature getAminoAcidFeature1() {
        return aminoAcidFeature1;
    }

    /**
     * Sets the first amino acid feature.
     * 
     * @param aminoAcidFeature1 the first amino acid feature
     */
    public void setAminoAcidFeature1(AAPropertyFeature aminoAcidFeature1) {
        this.aminoAcidFeature1 = aminoAcidFeature1;
    }

    /**
     * Returns the second amino acid feature.
     * 
     * @return the second amino acid feature
     */
    public AAPropertyFeature getAminoAcidFeature2() {
        return aminoAcidFeature2;
    }

    /**
     * Sets the second amino acid feature.
     * 
     * @param aminoAcidFeature2 the second amino acid feature
     */
    public void setAminoAcidFeature2(AAPropertyFeature aminoAcidFeature2) {
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

    @Override
    public AminoAcid.Property[] getAminoAcidProperties() {
        AminoAcid.Property[] properties = new AminoAcid.Property[2];
        properties[0] = aminoAcidFeature1.getAminoAcidProperty();
        properties[1] = aminoAcidFeature2.getAminoAcidProperty();
        return properties;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
