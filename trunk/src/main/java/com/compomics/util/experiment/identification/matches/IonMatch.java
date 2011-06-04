package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class will model the assignment of a peak to a theoretical ion.
 * 
 * @author Marc Vaudel
 */
public class IonMatch extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 5753142782728884464L;
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
     * Constructor for an ion peak.
     *
     * @param aPeak     the matched peak
     * @param anIon     the corresponding type of ion
     * @param aCharge   the charge of the ion
     */
    public IonMatch(Peak aPeak, Ion anIon, Charge aCharge) {
        peak = aPeak;
        ion = anIon;
        charge = aCharge;
    }

    /**
     * Get the matching error.
     *
     * @return the matching error
     */
    public double getError() {
        return peak.mz - ((ion.theoreticMass + charge.value * Atom.H.mass) / charge.value);
    }

    /**
     * Returns the annotation to use for the given ion match as a String.
     *
     * Format: ion type + [ion number] + [charge] + [neutral loss]
     *
     * @return the annotation to use for the given ion match
     */
    public String getPeakAnnotation() {

        if (ion instanceof PeptideFragmentIon) {
            PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);

            String annotation = fragmentIon.getIonType();

            // add fragment ion number
            if (!fragmentIon.getIonType().equalsIgnoreCase("MH")
                    && !fragmentIon.getIonType().startsWith("i")
                    && !fragmentIon.getIonType().equalsIgnoreCase("Prec-loss")) {
                annotation += fragmentIon.getNumber();
            }

            // add charge and any neutral losses
            annotation += charge.getChargeAsFormattedString() + fragmentIon.getNeutralLoss();

            return annotation;
        } else {
            return null;
        }
    }
}
