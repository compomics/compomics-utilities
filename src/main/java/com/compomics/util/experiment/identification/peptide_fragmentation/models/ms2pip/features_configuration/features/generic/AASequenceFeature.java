package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.features.generic;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.Ms2pipFeature;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration.SingleAAPropertyFeature;

/**
 * Feature based on the amino acid properties of a sequence.
 *
 * @author Marc Vaudel
 */
public abstract class AASequenceFeature implements Ms2pipFeature, SingleAAPropertyFeature {
    
    /**
     * The different functions implemented to compare the amino acid features.
     */
    public enum Function {
        minimum, maximum, mean, sum;
    }
    
    /**
     * The function used to compare the amino acid properties.
     */
    protected Function function;
    /**
     * The property of the amino acids to consider.
     */
    protected AminoAcid.Property aminoAcidProperty;

    /**
     * Returns the function used to compare the amino acid properties.
     * 
     * @return the function used to compare the amino acid properties
     */
    public Function getFunction() {
        return function;
    }

    /**
     * Sets the function used to compare the amino acid properties.
     * 
     * @param function the function used to compare the amino acid properties
     */
    public void setFunction(Function function) {
        this.function = function;
    }

    @Override
    public AminoAcid.Property getAminoAcidProperty() {
        return aminoAcidProperty;
    }

    @Override
    public void setAminoAcidProperty(AminoAcid.Property aminoAcidProperty) {
        this.aminoAcidProperty = aminoAcidProperty;
    }
}
