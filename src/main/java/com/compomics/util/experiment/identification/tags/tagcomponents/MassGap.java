package com.compomics.util.experiment.identification.tags.tagcomponents;

import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * An undefined mass gap.
 *
 * @author Marc
 */
public class MassGap extends ExperimentObject implements TagComponent {

    /**
     * The value of the mass gap.
     */
    private double value;

    /**
     * Constructor.
     *
     * @param value the value of the mass gap
     */
    public MassGap(double value) {
        this.value = value;
    }

    @Override
    public String asSequence() {
        return "<" + value + ">";
    }

    @Override
    public Double getMass() {
        return value;
    }

    @Override
    public boolean isSameAs(TagComponent anotherCompontent, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        return (anotherCompontent instanceof MassGap) && anotherCompontent.getMass() == value;
    }

    @Override
    public boolean isSameSequenceAndModificationStatusAs(TagComponent anotherCompontent, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        return isSameAs(anotherCompontent, matchingType, massTolerance);
    }
}
