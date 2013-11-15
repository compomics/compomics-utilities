package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.*;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class models an ion.
 *
 * @author Marc Vaudel
 */
public abstract class Ion extends ExperimentObject {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -1505719074403886934L;

    /**
     * An enumerator of the supported ion types.
     */
    public enum IonType {

        /**
         * Identifier for a peptide fragment ion.
         */
        PEPTIDE_FRAGMENT_ION,
        /**
         * A tag fragment ion
         */
        TAG_FRAGMENT_ION,
        /**
         * Identifier for an MH ion. The number of H is not represented here.
         */
        PRECURSOR_ION,
        /**
         * Identifier for an immonium ion.
         */
        IMMONIUM_ION,
        /**
         * Identifier for a reporter ion.
         */
        REPORTER_ION,
        /**
         * Identifier for a glycon
         */
        GLYCON,
        /**
         * Identifier for an elementary ion
         */
        ELEMENTARY_ION,
        /**
         * This int is the identifier for an unknown ion.
         */
        UNKNOWN;
    }
    /**
     * Type of ion.
     */
    protected IonType type = IonType.UNKNOWN;
    /*
     * Ion attribute - the theoretic mass
     */
    protected double theoreticMass;

    /**
     * Returns the name of the ion. The name should be short enough to be
     * displayed on a spectrum.
     *
     * @return the name of the ion
     */
    public abstract String getName();

    /**
     * Returns the pride cv term adapted to the fragment ion. null if none
     * corresponding.
     *
     * @return the pride cv term adapted to the fragment ion. null if none
     * corresponding
     */
    public abstract CvTerm getPrideCvTerm();

    /**
     * Returns the ion subtype.
     *
     * @return the ion subtype as integer
     */
    public abstract int getSubType();

    /**
     * Returns the subtype as string.
     *
     * @return the subtype as string
     */
    public abstract String getSubTypeAsString();

    /**
     * Returns an arraylist of possible subtypes.
     *
     * @param ionType an arraylist of possible subtypes
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes(IonType ionType) {
        switch (ionType) {
            case ELEMENTARY_ION:
                return ElementaryIon.getPossibleSubtypes();
            case GLYCON:
                return Glycon.getPossibleSubtypes();
            case IMMONIUM_ION:
                return ImmoniumIon.getPossibleSubtypes();
            case PEPTIDE_FRAGMENT_ION:
                return PeptideFragmentIon.getPossibleSubtypes();
            case TAG_FRAGMENT_ION:
                return TagFragmentIon.getPossibleSubtypes();
            case PRECURSOR_ION:
                return PrecursorIon.getPossibleSubtypes();
            case REPORTER_ION:
                return ReporterIon.getPossibleSubtypes();
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * Returns the possible neutral losses of this ion type. An empty list if
     * none.
     *
     * @return the possible neutral losses of this ion type
     */
    public abstract ArrayList<NeutralLoss> getNeutralLosses();

    /**
     * Returns a boolean indicating whether the ion is the same as another ion.
     *
     * @param anotherIon the other ion
     * @return a boolean indicating whether the ion is the same as another ion
     */
    public abstract boolean isSameAs(Ion anotherIon);

    /**
     * Returns the neutral loss (if any), the empty string if no loss.
     *
     * @return the neutral loss
     */
    public String getNeutralLossesAsString() {
        return getNeutralLossesAsString(getNeutralLosses());
    }

    /**
     * Returns the neutral loss (if any), the empty string if no loss.
     *
     * @param neutralLosses the neutral loss (if any)
     * @return the neutral loss
     */
    public static String getNeutralLossesAsString(ArrayList<NeutralLoss> neutralLosses) {
        ArrayList<String> names = new ArrayList<String>();
        for (NeutralLoss neutralLoss : neutralLosses) {
            names.add(neutralLoss.name);
        }
        Collections.sort(names);
        StringBuffer result = new StringBuffer();
        for (String name : names) {
            result.append("-").append(name);
        }
        return result.toString();
    }

    /**
     * Returns the theoretic mass.
     *
     * @return the theoretic mass
     */
    public double getTheoreticMass() {
        return theoreticMass;
    }

    /**
     * Returns the theoretic m/z of an ion at a given charge state.
     *
     * @param chargeValue the value of the carried charge
     * @return the theoretic m/z.
     */
    public double getTheoreticMz(int chargeValue) {
        return (getTheoreticMass() + chargeValue * ElementaryIon.proton.theoreticMass) / chargeValue;
    }

    /**
     * Sets a new theoretic mass.
     *
     * @param theoreticMass a new theoretic mass
     */
    public void setTheoreticMass(double theoreticMass) {
        this.theoreticMass = theoreticMass;
    }

    /**
     * Returns the ion type.
     *
     * @return the ion type
     */
    public IonType getType() {
        return type;
    }

    /**
     * Returns the implemented ion types.
     *
     * @return the implemented ion types
     */
    public static ArrayList<IonType> getImplementedIonTypes() {
        ArrayList<IonType> result = new ArrayList<IonType>();
        result.add(IonType.ELEMENTARY_ION);
        result.add(IonType.GLYCON);
        result.add(IonType.IMMONIUM_ION);
        result.add(IonType.PEPTIDE_FRAGMENT_ION);
        result.add(IonType.TAG_FRAGMENT_ION);
        result.add(IonType.PRECURSOR_ION);
        result.add(IonType.REPORTER_ION);
        return result;
    }

    /**
     * Returns the type of ion as string.
     *
     * @return the type of ion as string
     */
    public String getTypeAsString() {
        return getTypeAsString(type);
    }

    /**
     * Returns the type of ion as string.
     *
     * @param type the type of ion as string
     * @return the type of ion as string
     */
    public static String getTypeAsString(IonType type) {
        switch (type) {
            case PEPTIDE_FRAGMENT_ION:
                return "Peptide fragment ion";
            case TAG_FRAGMENT_ION:
                return "Tag fragment ion";
            case PRECURSOR_ION:
                return "Precursor ion";
            case IMMONIUM_ION:
                return "Immonium ion";
            case REPORTER_ION:
                return "Reporter ion";
            case GLYCON:
                return "Glycon";
            case ELEMENTARY_ION:
                return "Elementary ion";
            case UNKNOWN:
                return "Unknown ion type";
            default:
                throw new UnsupportedOperationException("No name for ion type " + type + ".");
        }
    }

    /**
     * Convenience method returning a generic ion based on the given ion type.
     *
     * @param ionType the ion type
     * @param subType the ion subtype
     * @param neutralLosses the neutral losses. An empty or null list if none.
     * @return a generic ion
     */
    public static Ion getGenericIon(IonType ionType, int subType, ArrayList<NeutralLoss> neutralLosses) {
        if (neutralLosses == null) {
            neutralLosses = new ArrayList<NeutralLoss>();
        }
        switch (ionType) {
            case ELEMENTARY_ION:
                return new ElementaryIon("new ElementaryIon", 0.0, subType);
            case GLYCON:
                return new Glycon("new Glycon", "new Glycon");
            case IMMONIUM_ION:
                return new ImmoniumIon(subType);
            case PEPTIDE_FRAGMENT_ION:
                return new PeptideFragmentIon(subType, neutralLosses);
            case TAG_FRAGMENT_ION:
                return new TagFragmentIon(subType, neutralLosses);
            case PRECURSOR_ION:
                return new PrecursorIon(neutralLosses);
            case REPORTER_ION:
                return new ReporterIon(subType);
            default:
                throw new UnsupportedOperationException("No generic constructor for " + getTypeAsString(ionType) + ".");
        }
    }

    /**
     * Convenience method returning a generic ion based on the given ion type
     * without neutral losses.
     *
     * @param ionType the ion type
     * @param subType the ion subtype
     * @return a generic ion
     */
    public static Ion getGenericIon(IonType ionType, int subType) {
        switch (ionType) {
            case ELEMENTARY_ION:
                return new ElementaryIon("new ElementaryIon", 0.0, subType);
            case GLYCON:
                return new Glycon("new Glycon", "new Glycon");
            case IMMONIUM_ION:
                return new ImmoniumIon(subType);
            case PEPTIDE_FRAGMENT_ION:
                return new PeptideFragmentIon(subType);
            case TAG_FRAGMENT_ION:
                return new TagFragmentIon(subType);
            case PRECURSOR_ION:
                return new PrecursorIon();
            case REPORTER_ION:
                return new ReporterIon(subType);
            default:
                throw new UnsupportedOperationException("No generic constructor for " + getTypeAsString(ionType) + ".");
        }
    }
}
