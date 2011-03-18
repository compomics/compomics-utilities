package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;

/**
 * This class models a peptide fragment ion.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:02 AM
 */
public class PeptideFragmentIon extends Ion {

    /**
     * An enumerator of the supported fragment ion types.
     */
    public enum PeptideFragmentIonType {

        /**
         * This int is the identifier for an a ion.
         */
        A_ION,
        /**
         * This int is the identifier for an a ion with NH3 loss.
         */
        ANH3_ION,
        /**
         * This int is the identifier for an a ion with H2O loss.
         */
        AH2O_ION,
        /**
         * This int is the identifier for a b ion.
         */
        B_ION,
        /**
         * This int is the identifier for a b ion with NH3 loss.
         */
        BNH3_ION,
        /**
         * This int is the identifier for a b ion with H2O loss.
         */
        BH2O_ION,
        /**
         * This int is the identifier for a c ion.
         */
        C_ION,
        /**
         * This int is the identifier for a x ion.
         */
        X_ION,
        /**
         * This int is the identifier for a y ion.
         */
        Y_ION, /**
         * This int is the identifier for a y ion with NH3 loss.
         */
        YNH3_ION,
        /**
         * This int is the identifier for a y ion with H2O loss.
         */
        YH2O_ION,
        /**
         * This int is the identifier for a z ion.
         */
        Z_ION,
        /**
         * This int is the identifier for an MH ion. The number of H is not represented here.
         */
        MH_ION,
        /**
         * This int is the identifier for an MH-NH3 ion.
         */
        MHNH3_ION,
        /**
         * This int is the identifier for an MH-H2O ion.
         */
        MHH2O_ION,
        /**
         * This int is the identifier for an immonium ion. The nature of the immonium ion is not coded yet.
         */
        IMMONIUM,
        /**
         * This int is the identifier for a precursor ion loss. The nature of the loss is not coded yet.
         */
        PRECURSOR_LOSS;
    }
    /**
     * Type of ion
     */
    private PeptideFragmentIonType type;
    /**
     * position of the ion in the peptide for peptide ions
     */
    private int number;

    /**
     * Construction for a peptide fragment.
     *
     * @param type   the type of ion according to static fields
     * @param mass                      the ion mass
     */
    public PeptideFragmentIon(PeptideFragmentIonType type, double mass) {
        this.type = type;
        this.theoreticMass = mass;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }

    /**
     * Construction for a peptide fragment.
     *
     * @param type      the type of ion according to static fields
     * @param number    the ion number
     * @param mass      the ion mass
     */
    public PeptideFragmentIon(PeptideFragmentIonType type, int number, double mass) {
        this.type = type;
        this.number = number;
        this.theoreticMass = mass;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }

    /**
     * Getter for the ion type
     * 
     * @return the ion type
     */
    public PeptideFragmentIonType getType() {
        return type;
    }

    /**
     * Returns the number of the fragment in the sequence
     * @return the number of the fragment in the sequence
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns the ion type (a, b, c, x, y, z) as a string.
     *
     * @return the ion type as a string.
     */
    public String getIonType() {

        if (type == PeptideFragmentIonType.B_ION
                || type == PeptideFragmentIonType.BH2O_ION
                || type == PeptideFragmentIonType.BNH3_ION) {
            return "b";
        } else if (type == PeptideFragmentIonType.Y_ION
                || type == PeptideFragmentIonType.YH2O_ION
                || type == PeptideFragmentIonType.YNH3_ION) {
            return "y";
        } else if (type == PeptideFragmentIonType.A_ION
                || type == PeptideFragmentIonType.AH2O_ION
                || type == PeptideFragmentIonType.ANH3_ION) {
            return "a";
        } else if (type == PeptideFragmentIonType.C_ION) {
            return "c";
        } else if (type == PeptideFragmentIonType.X_ION) {
            return "x";
        } else if (type == PeptideFragmentIonType.Z_ION) {
            return "z";
        } else if (type == PeptideFragmentIonType.MH_ION
                || type == PeptideFragmentIonType.MHNH3_ION
                || type == PeptideFragmentIonType.MHH2O_ION) {
            return "MH";
        } else if (type == PeptideFragmentIonType.IMMONIUM) {
            return "i";
        } else if (type == PeptideFragmentIonType.PRECURSOR_LOSS) {
            return "Prec-loss";
        }

        return "?";
    }

    /**
     * Returns the neutral loss (if any), the empty string if no loss.
     *
     * @return the neutral loss
     */
    public String getNeutralLoss() {

        if (type == PeptideFragmentIonType.BH2O_ION
                || type == PeptideFragmentIonType.YH2O_ION
                || type == PeptideFragmentIonType.AH2O_ION
                || type == PeptideFragmentIonType.MHH2O_ION) {
            return "-H2O";
        } else if (type == PeptideFragmentIonType.BNH3_ION
                || type == PeptideFragmentIonType.YNH3_ION
                || type == PeptideFragmentIonType.ANH3_ION
                || type == PeptideFragmentIonType.MHNH3_ION) {
            return "-NH3";
        }

        return "";
    }
}
