package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.ions.*;
import com.compomics.util.experiment.identification.spectrum_annotation.IonMatchKeysCache;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.pride.CvTerm;

/**
 * This class represents the assignment of a peak to a theoretical ion.
 *
 * @author Marc Vaudel
 */
public class IonMatch extends ExperimentObject {

    /**
     * The version UID for serialization/deserialization compatibility.
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
     * The inferred charge of the ion.
     */
    public Integer charge;
    /**
     * The sign of the charge.
     */
    public Integer chargeSign = Charge.PLUS;
    
    /**
     * Constructor for an ion peak.
     *
     * @param aPeak the matched peak
     * @param anIon the corresponding type of ion
     * @param charge the inferred charge of the ion
     */
    public IonMatch(Peak aPeak, Ion anIon, Integer charge) {
        peak = aPeak;
        ion = anIon;
        this.charge = charge;
    }

    /**
     * Get the absolute matching error in Da.
     *
     * @return the absolute matching error
     */
    public double getAbsoluteError() {
        double theoreticMz = ion.getTheoreticMz(charge);
        return peak.mz - theoreticMz;
    }

    /**
     * Get the absolute matching error in Da after isotope removal.
     *
     * @param minIsotope the minimal isotope
     * @param maxIsotope the maximal isotope
     *
     * @return the absolute matching error
     */
    public double getAbsoluteError(int minIsotope, int maxIsotope) {
        double theoreticMz = ion.getTheoreticMz(charge);
        double measuredMz = peak.mz;
        measuredMz -= getIsotopeNumber(minIsotope, maxIsotope) * Atom.C.getDifferenceToMonoisotopic(1) / charge;
        return measuredMz - theoreticMz;
    }

    /**
     * Get the relative m/z matching error in ppm.
     *
     * @return the relative matching error
     */
    public double getRelativeError() {
        double theoreticMz = ion.getTheoreticMz(charge);
        double measuredMz = peak.mz;
        return ((measuredMz - theoreticMz) * 1000000) / theoreticMz;
    }

    /**
     * Get the relative m/z matching error in ppm after isotope removal.
     *
     * @param minIsotope the minimal isotope
     * @param maxIsotope the maximal isotope
     *
     * @return the relative matching error
     */
    public double getRelativeError(int minIsotope, int maxIsotope) {
        double theoreticMz = ion.getTheoreticMz(charge);
        double measuredMz = peak.mz;
        measuredMz -= getIsotopeNumber(minIsotope, maxIsotope) * Atom.C.getDifferenceToMonoisotopic(1) / charge;
        return ((measuredMz - theoreticMz) * 1000000) / theoreticMz;
    }

    /**
     * Returns the distance in number of neutrons between the experimental mass
     * and theoretic mass, image of the isotope number: 1 typically indicates
     * C13 isotope.
     *
     * @param minIsotope the minimal isotope
     * @param maxIsotope the maximal isotope
     *
     * @return the distance in number of neutrons between the experimental mass
     * and theoretic mass
     */
    public int getIsotopeNumber(int minIsotope, int maxIsotope) {
        double experimentalMass = peak.mz * charge - charge * ElementaryIon.proton.getTheoreticMass();
        double result = (experimentalMass - ion.getTheoreticMass()) / Atom.C.getDifferenceToMonoisotopic(1);
        return Math.min(Math.max((int) Math.round(result), minIsotope), maxIsotope);
    }

    /**
     * Returns the error.
     *
     * @param isPpm a boolean indicating whether the error should be retrieved
     * in ppm (true) or in Dalton (false)
     * @param minIsotope the minimal isotope
     * @param maxIsotope the maximal isotope
     *
     * @return the match m/z error
     */
    public double getError(boolean isPpm, int minIsotope, int maxIsotope) {
        if (isPpm) {
            return getRelativeError(minIsotope, maxIsotope);
        } else {
            return getAbsoluteError(minIsotope, maxIsotope);
        }
    }

    /**
     * Returns the error.
     *
     * @param isPpm a boolean indicating whether the error should be retrieved
     * in ppm (true) or in Dalton (false)
     *
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
        return getPeakAnnotation(false, ion, new Charge(chargeSign, charge));
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
     * Returns the key for the ion match uniquely representing a peak
     * annotation.
     *
     * @param ion the ion matched
     * @param charge the charge
     *
     * @return the key for the ion match
     */
    public static String getMatchKey(Ion ion, int charge) {
        return getMatchKey(ion, charge, null);
    }

    /**
     * Returns the key for the ion match uniquely representing a peak
     * annotation. If a cache is given it will be used to store keys, ignored if
     * null.
     *
     * @param ion the ion matched
     * @param charge the charge
     * @param ionMatchKeysCache a cache for the ion match keys
     *
     * @return the key for the ion match
     */
    public static String getMatchKey(Ion ion, int charge, IonMatchKeysCache ionMatchKeysCache) {
        if (ionMatchKeysCache != null) {
            return ionMatchKeysCache.getMatchKey(ion, charge);
        }
        Ion.IonType ionType = ion.getType();
        int ionTypeIndex = ionType.index;
        int ionSubType = ion.getSubType();
        int fragmentIonNumber;
        if (ionType == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
            PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
            fragmentIonNumber = fragmentIon.getNumber();
        } else if (ionType == Ion.IonType.TAG_FRAGMENT_ION) {
            TagFragmentIon tagFragmentIon = ((TagFragmentIon) ion);
            fragmentIonNumber = tagFragmentIon.getNumber();
        } else {
            fragmentIonNumber = 0;
        }
        String neutralLossesAsString = ion.getNeutralLossesAsString();
        String key = getMatchKey(ionTypeIndex, ionSubType, fragmentIonNumber, neutralLossesAsString, charge);
        return key;
    }

    /**
     * Returns the key based on the different attributes of a match.
     *
     * @param ionTypeIndex the index of the ion type
     * @param ionSubType the index of the ion subtype
     * @param fragmentIonNumber the number of the ion, 0 if none
     * @param neutralLossesAsString the neutral losses as a string
     * @param charge the charge
     *
     * @return the key for the ion match
     */
    public static String getMatchKey(int ionTypeIndex, int ionSubType, int fragmentIonNumber, String neutralLossesAsString, int charge) {
        StringBuilder stringBuilder = new StringBuilder(8);
        stringBuilder.append(ionTypeIndex).append("_").append(ionSubType).append("_").append(fragmentIonNumber).append("_").append(neutralLossesAsString).append("_").append(charge);
        return stringBuilder.toString();
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

        StringBuilder result = new StringBuilder();

        switch (ion.getType()) {
            case PEPTIDE_FRAGMENT_ION:
                if (html) {
                    result.append("<html>");
                }
                result.append(ion.getSubTypeAsString());

                // add fragment ion number
                PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
                if (html) {
                    result.append("<sub>").append(fragmentIon.getNumber()).append("</sub>");
                } else {
                    result.append(fragmentIon.getNumber());
                }

                // add charge
                result.append(charge.getChargeAsFormattedString());

                // add any neutral losses
                if (html) {
                    String neutralLoss = ion.getNeutralLossesAsString();

                    for (int i = 0; i < neutralLoss.length(); i++) {
                        if (Character.isDigit(neutralLoss.charAt(i))) {
                            result.append("<sub>").append(neutralLoss.charAt(i)).append("</sub>");
                        } else {
                            result.append(neutralLoss.charAt(i));
                        }
                    }
                } else {
                    result.append(ion.getNeutralLossesAsString());
                }
                if (html) {
                    result.append("</html>");
                }
                return result.toString();
            case TAG_FRAGMENT_ION:
                TagFragmentIon tagFragmentIon = (TagFragmentIon) ion;

                if (html) {
                    result.append("<html>");
                }
                // add type
                result.append(ion.getSubTypeAsString());

                // add fragment ion number
                if (html) {
                    result.append("<sub>").append(tagFragmentIon.getSubNumber()).append("</sub>");
                } else {
                    result.append(tagFragmentIon.getSubNumber());
                }

                // add charge
                result.append(charge.getChargeAsFormattedString());

                // add any neutral losses
                if (html) {
                    String neutralLoss = ion.getNeutralLossesAsString();

                    for (int i = 0; i < neutralLoss.length(); i++) {
                        if (Character.isDigit(neutralLoss.charAt(i))) {
                            result.append("<sub>").append(neutralLoss.charAt(i)).append("</sub>");
                        } else {
                            result.append(neutralLoss.charAt(i));
                        }
                    }
                } else {
                    result.append(ion.getNeutralLossesAsString());
                }

                if (html) {
                    result.append("</html>");
                }
                return result.toString();
            case PRECURSOR_ION:
                if (html) {
                    result.append("<html>");
                }
                result.append(ion.getSubTypeAsString()).append("-");

                // add charge
                result.append(charge.getChargeAsFormattedString());

                // add any neutral losses
                String neutralLoss = ion.getNeutralLossesAsString();
                if (html) {
                    for (int i = 0; i < neutralLoss.length(); i++) {
                        if (Character.isDigit(neutralLoss.charAt(i))) {
                            result.append("<sub>").append(neutralLoss.charAt(i)).append("</sub>");
                        } else {
                            result.append(neutralLoss.charAt(i));
                        }
                    }
                } else {
                    result.append(neutralLoss);
                }
                if (html) {
                    result.append("</html>");
                }
                return result.toString();
            default:
                if (html) {
                    result.append("<html>");
                }
                result.append(ion.getName());
                if (html) {
                    result.append("</html>");
                }
                return result.toString();
        }
    }

    /**
     * Returns the annotation to use for the given ion match as a String.
     *
     * @param html if true, returns the annotation as HTML with subscripts tags
     * @return the annotation to use for the given ion match
     */
    public String getPeakAnnotation(boolean html) {
        return getPeakAnnotation(html, ion, new Charge(chargeSign, charge));
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
     * @param minIsotope the minimal isotope
     * @param maxIsotope the maximal isotope
     *
     * @return the pride CV term for the ion match error
     */
    public CvTerm getIonMassErrorPrideCvTerm(int minIsotope, int maxIsotope) {
        return new CvTerm("PRIDE", "PRIDE:0000190", "product ion mass error", getAbsoluteError(minIsotope, maxIsotope) + "");
    }

    /**
     * Returns the pride CV term for the ion match charge.
     *
     * @return the pride CV term for the ion match charge
     */
    public CvTerm getChargePrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000204", "product ion charge", charge + "");
    }

    /**
     * Enum of the supported error types.
     */
    public enum MzErrorType {

        Absolute("Absolute", "Absolute error", "m/z"),
        RelativePpm("Relative (ppm)", "Relative error in ppm", "ppm"),
        Statistical("Statistical", "Probability to reach this error according to the error distribution", "%p");
        /**
         * The name of the error type.
         */
        public final String name;
        /**
         * The description of the error type.
         */
        public final String description;
        /**
         * The unit to use
         */
        public final String unit;

        /**
         * Constructor.
         *
         * @param name the name of the error type
         * @param description the description of the error type
         * @param unit the unit to use
         */
        private MzErrorType(String name, String description, String unit) {
            this.name = name;
            this.description = description;
            this.unit = unit;
        }

        /**
         * Returns the error type corresponding to the given index. Error types
         * are indexed according to the values() method. Null if not found.
         *
         * @param index the index of the error type in the values() method
         *
         * @return the corresponding error type
         */
        public static MzErrorType getMzErrorType(int index) {
            MzErrorType[] values = MzErrorType.values();
            if (index >= 0 && index < values.length) {
                return values[index];
            }
            return null;
        }
    }
}
