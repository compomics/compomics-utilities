package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.*;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class models an ion.
 *
 * Created by IntelliJ IDEA. User: Marc Date: Jun 18, 2010 Time: 8:57:33 AM
 */
public abstract class Ion extends ExperimentObject {

    /**
     * An enumerator of the supported ion types.
     */
    public enum IonType {

        /**
         * identifier for a peptide fragment ion.
         */
        PEPTIDE_FRAGMENT_ION,
        /**
         * identifier for an MH ion. The number of H is not represented here.
         */
        PRECURSOR_ION,
        /**
         * identifier for an immonium ion.
         */
        IMMONIUM_ION,
        /**
         * identifier for a reporter ion.
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
     * Type of ion
     */
    protected IonType type = IonType.UNKNOWN;
    /**
     * Ion attribute - the theoretic mass
     */
    protected double theoreticMass;

    /**
     * Returns the name of the ion. The name should be short enough to be
     * displayed on a spectrum.
     */
    public abstract String getName();

    /**
     * Returns the pride cv term adapted to the fragment ion. null if none
     * corresponding
     *
     * @return the pride cv term adapted to the fragment ion. null if none
     * corresponding
     */
    public abstract CvTerm getPrideCvTerm();

    /**
     * Returns the ion subtype
     *
     * @return the ion subtype as integer
     */
    public abstract int getSubType();

    /**
     * Returns the subtype as string
     *
     * @return the subtype as string
     */
    public abstract String getSubTypeAsString();

    /**
     * Returns an arraylist of possible subtypes
     *
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
     * Returns a boolean indicating whether the ion is the same as another ion
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
        ArrayList<String> names = new ArrayList<String>();
        for (NeutralLoss neutralLoss : getNeutralLosses()) {
            names.add(neutralLoss.name);
        }
        Collections.sort(names);
        String result = "";
        for (String name : names) {
            result += "-" + name;
        }
        return result;
    }

    /**
     * Returns the theoretic mass
     *
     * @return
     */
    public double getTheoreticMass() {
        return theoreticMass;
    }

    /**
     * Sets a new theoretic mass
     *
     * @param theoreticMass a new theoretic mass
     */
    public void setTheoreticMass(double theoreticMass) {
        this.theoreticMass = theoreticMass;
    }

    /**
     * Returns the ion type
     *
     * @return
     */
    public IonType getType() {
        return type;
    }

    /**
     * Returns the type of ion as string
     */
    public String getTypeAsString() {
        switch (type) {
            case PEPTIDE_FRAGMENT_ION:
                return "Peptide fragment ion";
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
}
