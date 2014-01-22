package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.ElementaryElement;
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
     * @param subtractIsotope indicates whether the isotope number shall be
     * subtracted
     * @return the absolute matching error
     */
    public double getAbsoluteError(boolean subtractIsotope) {
        double theoreticMass = ion.getTheoreticMass();
        if (subtractIsotope) {
            theoreticMass -= getIsotopeNumber() * ElementaryElement.neutron.getMass();
        }
        return peak.mz - ((theoreticMass + charge.value * ElementaryIon.proton.getTheoreticMass()) / charge.value);
    }

    /**
     * Get the absolute matching error in Da without isotope removal.
     *
     * @return the absolute matching error
     */
    public double getAbsoluteError() {
        return getAbsoluteError(false);
    }

    /**
     * Get the relative m/z matching error in ppm.
     *
     * @param subtractIsotope indicates whether the isotope number shall be
     * subtracted
     * @return the relative matching error
     */
    public double getRelativeError(boolean subtractIsotope) {
        if (charge != null && charge.value != 0) {
            double theoreticMz = (ion.getTheoreticMass() + charge.value * ElementaryIon.proton.getTheoreticMass()) / charge.value;
            double measuredMz = peak.mz;
            if (subtractIsotope) {
                measuredMz -= getIsotopeNumber() * ElementaryElement.neutron.getMass() / charge.value;
            }
            return ((measuredMz - theoreticMz) / theoreticMz) * 1000000;
        } else {
            return Double.MAX_VALUE;
        }
    }

    /**
     * Get the relative m/z matching error in ppm without isotope removal.
     *
     * @return the relative matching error
     */
    public double getRelativeError() {
        return getRelativeError(false);
    }

    /**
     * Returns the distance in number of neutrons between the experimental mass
     * and theoretic mass, image of the isotope number: 1 typically indicates
     * C13 isotope.
     *
     * @return the distance in number of neutrons between the experimental mass
     * and theoretic mass
     */
    public int getIsotopeNumber() {
        double experimentalMass = peak.mz * charge.value - charge.value * ElementaryIon.proton.getTheoreticMass();
        double result = (experimentalMass - ion.getTheoreticMass()) / ElementaryElement.neutron.getMass();
        return (int) Math.round(result);
    }

    /**
     * Returns the error.
     *
     * @param isPpm a boolean indicating whether the error should be retrieved
     * in ppm (true) or in Dalton (false)
     * @param subtractIsotope indicates whether the isotope number shall be
     * subtracted
     * @return the match m/z error
     */
    public double getError(boolean isPpm, boolean subtractIsotope) {
        if (isPpm) {
            return getRelativeError(subtractIsotope);
        } else {
            return getAbsoluteError(subtractIsotope);
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
            case TAG_FRAGMENT_ION:
                TagFragmentIon tagFragmentIon = (TagFragmentIon) ion;

                if (html) {
                    result += "<html>";
                }
                // add type
                result += ion.getSubTypeAsString();

                if (tagFragmentIon.getSubNumber() > 0) {
                    // add fragment ion number
                    if (html) {
                        result += "<sub>" + tagFragmentIon.getSubNumber() + "</sub>";
                    } else {
                        result += tagFragmentIon.getSubNumber();
                    }
                } else {
                    // add gap
                    if (tagFragmentIon.getMassGap() > 0 && tagFragmentIon.getSubNumber() == 0) {
                        if (html) {
                            result += "<sub>(" + tagFragmentIon.getMassGap() + ")</sub>";
                        } else {
                            result += "(" + tagFragmentIon.getMassGap() + ")";
                        }
                    }
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
        return new CvTerm("PRIDE", "PRIDE:0000190", "product ion mass error", getAbsoluteError(true) + "");
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