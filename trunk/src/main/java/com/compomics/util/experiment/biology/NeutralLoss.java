package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.neutrallosses.CH4OS;
import com.compomics.util.experiment.biology.neutrallosses.H2O;
import com.compomics.util.experiment.biology.neutrallosses.HPO3;
import com.compomics.util.experiment.biology.neutrallosses.H3PO4;
import com.compomics.util.experiment.biology.neutrallosses.NH3;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class represents a neutral loss.
 *
 * @author marc
 */
public abstract class NeutralLoss extends ExperimentObject {

    /**
     * H2O loss
     */
    public static final NeutralLoss H2O = new H2O();
    /**
     * NH3 loss
     */
    public static final NeutralLoss NH3 = new NH3();
    /**
     * H3PO4 loss
     */
    public static final NeutralLoss H3PO4 = new H3PO4();
    /**
     * H3PO3 loss
     */
    public static final NeutralLoss HPO3 = new HPO3();
    /**
     * CH4OS loss
     */
    public static final NeutralLoss CH4OS = new CH4OS();
    /**
     * The mass lost
     */
    public double mass;
    /**
     * The name of the neutral loss
     */
    public String name;

    /**
     * Method indicating whether another neutral loss is the same as the one considered
     * @param anotherNeutralLoss    another neutral loss
     * @return boolean indicating whether the other neutral loss is the same as the one considered
     */
    public boolean isSameAs(NeutralLoss anotherNeutralLoss) {
        return anotherNeutralLoss.name.equals(name);
    }
}
