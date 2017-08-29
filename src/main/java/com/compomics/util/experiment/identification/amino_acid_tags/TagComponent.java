package com.compomics.util.experiment.identification.amino_acid_tags;

import com.compomics.util.parameters.identification.SequenceMatchingParameters;

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
    public double getMass();

    /**
     * Indicates whether another component is the same as the component of
     * interest.
     *
     * @param anotherCompontent another component
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the other component is the same as
     * the one of interest
     */
    public boolean isSameAs(TagComponent anotherCompontent, SequenceMatchingParameters sequenceMatchingPreferences);

    /**
     * Indicates whether another component is the same as the component of
     * interest.
     *
     * @param anotherCompontent another component
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the other component is the same as
     * the one of interest
     */
    public boolean isSameSequenceAndModificationStatusAs(TagComponent anotherCompontent, SequenceMatchingParameters sequenceMatchingPreferences);
}
