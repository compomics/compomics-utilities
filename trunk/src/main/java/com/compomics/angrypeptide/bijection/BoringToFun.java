/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.angrypeptide.bijection;

import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.angrypeptide.fun.Targets;
import com.compomics.util.experiment.biology.ions.ElementaryIon;

/**
 * this class translate the boring scientific model into a fun game model.
 * yeepee.
 *
 * @author Marc
 */
public class BoringToFun {

    /**
     * The search parameters to be used
     */
    private MatchingParameters searchParameters;

    /**
     * Constructor
     *
     * @param searchParameters the search parameters
     */
    public BoringToFun(MatchingParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    /**
     * Returns the targets corresponding to a given spectrum
     *
     * @param spectrum the spectrum of interest
     * @return a Targets object containing the targets
     */
    public Targets getTargets(MSnSpectrum spectrum, int precursorCharge) {
        Targets targets = new Targets();

        double precursorMass = (precursorCharge) * spectrum.getPrecursor().getMz() - (precursorCharge) * ElementaryIon.proton.getTheoreticMass();
//        for (int fragmentCharge = 1; fragmentCharge < Math.max(2, precursorCharge); fragmentCharge++) {
        int fragmentCharge = 1;
            for (Peak peak : spectrum.getPeakList()) {
                double m = fragmentCharge * peak.mz - fragmentCharge * ElementaryIon.proton.getTheoreticMass();
                if (m < precursorMass && m > 2 * searchParameters.getMinFragmentMass()) {
                    targets.addTarget(m, peak.intensity);
                }
            }
//        }

        return targets;
    }
}
