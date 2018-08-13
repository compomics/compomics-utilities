package com.compomics.util.experiment.identification.amino_acid_tags;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;

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
     * The value as sequence.
     */
    private String sequence = null;

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
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.value = value;
        sequence = null;
    }

    @Override
    public String asSequence() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        
        if (sequence == null) {
        
            String valueAsString = Double.toString(value);
            StringBuilder stringBuilder = new StringBuilder(valueAsString.length() + 2);
            stringBuilder.append('<').append(valueAsString).append('>');
            sequence = stringBuilder.toString();
            
        }
        
        return sequence;
    
    }

    @Override
    public double getMass() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return value;
    }

    @Override
    public boolean isSameAs(TagComponent anotherCompontent, SequenceMatchingParameters sequenceMatchingPreferences) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return (anotherCompontent instanceof MassGap) && anotherCompontent.getMass() == value;
    }

    @Override
    public boolean isSameSequenceAndModificationStatusAs(TagComponent anotherCompontent, SequenceMatchingParameters sequenceMatchingPreferences) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return isSameAs(anotherCompontent, sequenceMatchingPreferences);
    }
}
