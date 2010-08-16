package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.Ion;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 1:29:41 PM
 * This class will modelize the assignement of a peak to a theoretic ion.
 */
public class IonMatch {

    // Attributes

    public Peak peak;
    public Ion ion;
    private double error; // in Da


    // Constructors

    public IonMatch(Peak aPeak, Ion anIon) {
        peak = aPeak;
        ion = anIon;
    }


    // Attributes

    public void setError(double error) {
        this.error = error;
    }

    public double getError() {
        return error;
    }


}
