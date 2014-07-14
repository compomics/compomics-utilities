package com.compomics.util.experiment.identification.tags;

import com.compomics.util.experiment.biology.AminoAcidPattern;

/**
 * Interface for a sequence tag component.
 *
 * @author Marc Vaudel
 */
public interface TagComponent {

    /**
     * Returns the tag component as String like a peptide sequence. Note: this
     * does not include modifications.
     *
     * @return the tag component as String like a peptide sequence
     */
    public String asSequence();

    /**
     * Returns the mass of the tag component.
     *
     * @return the mass of the tag component
     */
    public Double getMass();

    /**
     * Indicates whether another component is the same as the component of
     * interest
     *
     * @param anotherCompontent another component
     * @param matchingType the amino acid matching type
     * @param massTolerance the mass tolerance to use to consider amino acids as indistinguishable
     *
     * @return a boolean indicating whether the other component is the same as
     * the one of interest
     */
    public boolean isSameAs(TagComponent anotherCompontent, AminoAcidPattern.MatchingType matchingType, Double massTolerance);

    /**
     * Indicates whether another component is the same as the component of
     * interest
     *
     * @param anotherCompontent another component
     * @param matchingType the amino acid matching type
     * @param massTolerance the mass tolerance to use to consider amino acids as indistinguishable
     *
     * @return a boolean indicating whether the other component is the same as
     * the one of interest
     */
    public boolean isSameSequenceAndModificationStatusAs(TagComponent anotherCompontent, AminoAcidPattern.MatchingType matchingType, Double massTolerance);
}
