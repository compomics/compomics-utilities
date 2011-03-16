package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class will model the assignment of a peak to a theoretical ion.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 1:29:41 PM
 */
public class IonMatch extends ExperimentObject {

    /**
     * The matched peak
     */
    public Peak peak;
    /**
     * The matching ion
     */
    public Ion ion;
    /**
     * The supposed charge of the ion
     */
    public Charge charge;


    /**
     * Constructor for an ion peak
     *
     * @param aPeak the matched peak
     * @param anIon the corresponding type of ion
     * @param aCharge the charge of the ion
     */
    public IonMatch(Peak aPeak, Ion anIon, Charge aCharge) {
        peak = aPeak;
        ion = anIon;
    }

    /**
     * get the matching error
     *
     * @return the matching error
     */
    public double getError() {
        return (peak.mz-ion.theoreticMass)*charge.value;
    }
}
