package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.utils.ExperimentObject;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class models an ion.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:57:33 AM
 */
public abstract class Ion extends ExperimentObject {

    /**
     * Ion family type
     * static index for peptide fragments
     */
    public static final int PEPTIDE_FRAGMENT = 0;
    /**
     * Ion family type
     * static index for glycons
     */
    public static final int GLYCON_FRAGMENT = 1;
    /**
     * Ion family type
     * static index for glycons
     */
    public static final int REPORTER_ION = 2;

    /**
     * Ion attribute - the theoretic mass
     */
    public double theoreticMass;
    /**
     * Ion attribute - the ion family name
     */
    protected int familyType;

    /**
     * getter for the ion family name
     * @return the ion family name as indexed in static field
     */
    public int getIonFamilyType() {
        return familyType;
    }

    /**
     * @TODO: Add JavaDoc
     *
     * @param spectrum
     * @param ionTolerance
     * @return
     */
    public IonMatch match(MSnSpectrum spectrum, double ionTolerance) {
        HashMap<Double, Peak> peakMap = spectrum.getPeakMap();
        ArrayList<Double> mzArray = new ArrayList(peakMap.keySet());
        Collections.sort(mzArray);
        Peak bestPeak = null;
        double bestMz = 0;
        for (Double mz : mzArray) {
            if (mz > this.theoreticMass - ionTolerance && mz < this.theoreticMass + ionTolerance) {
                if (bestPeak == null) {
                    bestPeak = peakMap.get(mz);
                    bestMz = mz;
                } else if (Math.abs(mz - this.theoreticMass) < Math.abs(bestMz - this.theoreticMass)) {
                    bestPeak = peakMap.get(mz);
                    bestMz = mz;
                }
            } else if (mz > this.theoreticMass + ionTolerance) {
                break;
            }
        }
        return new IonMatch(bestPeak, this);
    }
}
