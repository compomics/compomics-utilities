package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;

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
    
    /**
     * Sets the mass of the gap.
     * 
     * @param value the mass of the gap
     */
    public void setMass(double value) {
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
    public boolean isSameAs(TagComponent anotherCompontent, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return (anotherCompontent instanceof MassGap) && anotherCompontent.getMass() == value;
    }

    @Override
    public boolean isSameSequenceAndModificationStatusAs(TagComponent anotherCompontent, SequenceMatchingPreferences sequenceMatchingPreferences) {
        return isSameAs(anotherCompontent, sequenceMatchingPreferences);
    }
}
