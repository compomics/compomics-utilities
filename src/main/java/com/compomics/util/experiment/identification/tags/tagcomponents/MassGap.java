package com.compomics.util.experiment.identification.tags.tagcomponents;

import com.compomics.util.experiment.identification.tags.TagComponent;

/**
 * An undefined mass gap.
 *
 * @author Marc
 */
public class MassGap implements TagComponent {

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
    public boolean isSameAs(TagComponent anotherCompontent) {
        return (anotherCompontent instanceof MassGap) && anotherCompontent.getMass() == value;
    }
}
