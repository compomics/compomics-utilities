package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.ions.*;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.pride.CvTerm;

/**
 * This class will model the assignment of a peak to a theoretical ion.
 *
 * @author Marc Vaudel
 */
public class IonMatch extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 5753142782728884464L;
    /**
     * The matched peak.
     */
    public Peak peak;
    /**
     * The matching ion.
     */
    public Ion ion;
    /**
     * The supposed charge of the ion.
     */
    public Charge charge;

    /**
     * Constructor for an ion peak.
     *
     * @param aPeak the matched peak
     * @param anIon the corresponding type of ion
     * @param aCharge the charge of the ion
     */
    public IonMatch(Peak aPeak, Ion anIon, Charge aCharge) {
        peak = aPeak;
        ion = anIon;
        charge = aCharge;
    }

    /**
     * Get the matching error.
     *
     * @deprecated replaced by getAbsoluteError() and getRelativeError()
     * @return the matching error
     */
    public double getError() {
        return peak.mz - ((ion.getTheoreticMass() + charge.value * Atom.H.mass) / charge.value);
    }

    /**
     * Get the absolute matching error in Da.
     *
     * @return the absolute matching error
     */
    public double getAbsoluteError() {
        return peak.mz - ((ion.getTheoreticMass() + charge.value * ElementaryIon.proton.getTheoreticMass()) / charge.value);
    }

    /**
     * Get the relative m/z matching error in ppm.
     *
     * @return the relative matching error
     */
    public double getRelativeError() {
        double theoreticMz = (ion.getTheoreticMass() + charge.value * ElementaryIon.proton.getTheoreticMass()) / charge.value;
        return ((peak.mz - theoreticMz)
                / theoreticMz) * 1000000;
    }

    /**
     * Returns the error.
     *
     * @param isPpm a boolean indicating whether the error should be retrieved
     * in ppm (true) or in Dalton (false)
     * @return the match m/z error
     */
    public double getError(boolean isPpm) {
        if (isPpm) {
            return getRelativeError();
        } else {
            return getAbsoluteError();
        }
    }

    /**
     * Returns the annotation to use for the ion match as a String.
     *
     * @return the annotation to use for the given ion match
     */
    public String getPeakAnnotation() {
        return getPeakAnnotation(false, ion, charge);
    }

    /**
     * Returns the annotation to use for a given ion and charge as a String.
     *
     * @param ion the given ion
     * @param charge the given charge
     * @return the annotation to use for the given ion match
     */
    public static String getPeakAnnotation(Ion ion, Charge charge) {
        return getPeakAnnotation(false, ion, charge);
    }

    /**
     * Returns the annotation to use for a given ion and charge as a String.
     *
     * @param html if true, returns the annotation as HTML with subscripts tags
     * @param ion the given ion
     * @param charge the given charge
     * @return the annotation to use for the given ion match
     */
    public static String getPeakAnnotation(boolean html, Ion ion, Charge charge) {
        
        String result = "";
        
        switch (ion.getType()) {
            case PEPTIDE_FRAGMENT_ION:
                if (html) {
                    result += "<html>";
                }
                result += ion.getSubTypeAsString();

                // add fragment ion number
                PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
                if (html) {
                    result += "<sub>" + fragmentIon.getNumber() + "</sub>";
                } else {
                    result += fragmentIon.getNumber();
                }

                // add charge
                result += charge.getChargeAsFormattedString();

                // add any neutral losses
                if (html) {
                    String neutralLoss = ion.getNeutralLossesAsString();

                    for (int i = 0; i < neutralLoss.length(); i++) {
                        if (Character.isDigit(neutralLoss.charAt(i))) {
                            result += "<sub>" + neutralLoss.charAt(i) + "</sub>";
                        } else {
                            result += neutralLoss.charAt(i);
                        }
                    }
                } else {
                    result += ion.getNeutralLossesAsString();
                }
                if (html) {
                    result += "</html>";
                }
                return result;
            case PRECURSOR_ION:
                if (html) {
                    result += "<html>";
                }
                result += ion.getSubTypeAsString() + "-";

                // add charge
                result += charge.getChargeAsFormattedString();

                // add any neutral losses
                String neutralLoss = ion.getNeutralLossesAsString();
                if (html) {
                    for (int i = 0; i < neutralLoss.length(); i++) {
                        if (Character.isDigit(neutralLoss.charAt(i))) {
                            result += "<sub>" + neutralLoss.charAt(i) + "</sub>";
                        } else {
                            result += neutralLoss.charAt(i);
                        }
                    }
                } else {
                    result += neutralLoss;
                }
                if (html) {
                    result += "</html>";
                }
                return result;
            default:
                if (html) {
                    result += "<html>";
                }
                result += ion.getName();
                if (html) {
                    result += "</html>";
                }
                return result;
        }
    }

    /**
     * Returns the annotation to use for the given ion match as a String.
     *
     * @param html if true, returns the annotation as HTML with subscripts tags
     * @return the annotation to use for the given ion match
     */
    public String getPeakAnnotation(boolean html) {
        return getPeakAnnotation(html, ion, charge);
    }

    /**
     * Returns the pride CV term for the ion match m/z.
     *
     * @return the pride CV term for the ion match m/z
     */
    public CvTerm getMZPrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000188", "product ion m/z", peak.mz + "");
    }

    /**
     * Returns the pride CV term for the ion match intensity.
     *
     * @return the pride CV term for the ion match intensity
     */
    public CvTerm getIntensityPrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000189", "product ion intensity", peak.intensity + "");
    }

    /**
     * Returns the pride CV term for the ion match error.
     *
     * @return the pride CV term for the ion match error
     */
    public CvTerm getIonMassErrorPrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000190", "product ion mass error", getAbsoluteError() + "");
    }

    /**
     * Returns the pride CV term for the ion match charge.
     *
     * @return the pride CV term for the ion match charge
     */
    public CvTerm getChargePrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000204", "product ion charge", charge.value + "");
    }
}
