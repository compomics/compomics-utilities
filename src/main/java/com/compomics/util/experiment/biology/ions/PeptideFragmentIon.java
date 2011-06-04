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
         * This int is the identifier for an alanine immonium ion.
         */
        IMMONIUM_A,
        /**
         * This int is the identifier for an arginine immonium ion.
         */
        IMMONIUM_R,
        /**
         * This int is the identifier for an asparagine immonium ion.
         */
        IMMONIUM_N,
        /**
         * This int is the identifier for an aspartic acid immonium ion.
         */
        IMMONIUM_D,
        /**
         * This int is the identifier for a cysteine immonium ion.
         */
        IMMONIUM_C,
        /**
         * This int is the identifier for a glutamic acid immonium ion.
         */
        IMMONIUM_E,
        /**
         * This int is the identifier for a glutamine acid immonium ion.
         */
        IMMONIUM_Q,
        /**
         * This int is the identifier for a glycine acid immonium ion.
         */
        IMMONIUM_G,
        /**
         * This int is the identifier for an histidine acid immonium ion.
         */
        IMMONIUM_H,
        /**
         * This int is the identifier for an isoleucine acid immonium ion.
         */
        IMMONIUM_I,
        /**
         * This int is the identifier for a leucine acid immonium ion.
         */
        IMMONIUM_L,
        /**
         * This int is the identifier for a lysine acid immonium ion.
         */
        IMMONIUM_K,
        /**
         * This int is the identifier for a methionine acid immonium ion.
         */
        IMMONIUM_M,
        /**
         * This int is the identifier for a phenylalanine acid immonium ion.
         */
        IMMONIUM_F,
        /**
         * This int is the identifier for a proline acid immonium ion.
         */
        IMMONIUM_P,
        /**
         * This int is the identifier for a serine acid immonium ion.
         */
        IMMONIUM_S,
        /**
         * This int is the identifier for a threonine acid immonium ion.
         */
        IMMONIUM_T,
        /**
         * This int is the identifier for a tryptophan acid immonium ion.
         */
        IMMONIUM_W,
        /**
         * This int is the identifier for a tyrosine acid immonium ion.
         */
        IMMONIUM_Y,
        /**
         * This int is the identifier for a valine acid immonium ion.
         */
        IMMONIUM_V,
        /**
         * This int is the identifier for a precursor ion loss. The nature of the loss is not coded yet.
         */
        PRECURSOR_LOSS,
        /**
         * This int is the identifier for an unknown ion.
         */
        UNKNOWN;
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
        } else if (type == PeptideFragmentIonType.IMMONIUM_A) {
            return "iA";
        } else if (type == PeptideFragmentIonType.IMMONIUM_C) {
            return "iC";
        } else if (type == PeptideFragmentIonType.IMMONIUM_D) {
            return "iD";
        } else if (type == PeptideFragmentIonType.IMMONIUM_E) {
            return "iE";
        } else if (type == PeptideFragmentIonType.IMMONIUM_F) {
            return "iF";
        } else if (type == PeptideFragmentIonType.IMMONIUM_G) {
            return "iG";
        } else if (type == PeptideFragmentIonType.IMMONIUM_H) {
            return "iH";
        } else if (type == PeptideFragmentIonType.IMMONIUM_I) {
            return "iI";
        } else if (type == PeptideFragmentIonType.IMMONIUM_K) {
            return "iK";
        } else if (type == PeptideFragmentIonType.IMMONIUM_L) {
            return "iL";
        } else if (type == PeptideFragmentIonType.IMMONIUM_M) {
            return "iM";
        } else if (type == PeptideFragmentIonType.IMMONIUM_N) {
            return "iN";
        } else if (type == PeptideFragmentIonType.IMMONIUM_P) {
            return "iP";
        } else if (type == PeptideFragmentIonType.IMMONIUM_Q) {
            return "iQ";
        } else if (type == PeptideFragmentIonType.IMMONIUM_R) {
            return "iR";
        } else if (type == PeptideFragmentIonType.IMMONIUM_S) {
            return "iS";
        } else if (type == PeptideFragmentIonType.IMMONIUM_T) {
            return "iT";
        } else if (type == PeptideFragmentIonType.IMMONIUM_V) {
            return "iV";
        } else if (type == PeptideFragmentIonType.IMMONIUM_W) {
            return "iW";
        } else if (type == PeptideFragmentIonType.IMMONIUM_Y) {
            return "iY";
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

    /**
     * Converts a given amino acid residue to the given immonium ion.
     * 
     * @param aminoAcidResidue  the amino acid residue
     * @return                  the corresponding immonium ion
     */
    public static PeptideFragmentIonType getImmoniumIon(String aminoAcidResidue) {

        if (aminoAcidResidue.equalsIgnoreCase("A")) {
            return PeptideFragmentIonType.IMMONIUM_A;
        } else if (aminoAcidResidue.equalsIgnoreCase("C")) {
            return PeptideFragmentIonType.IMMONIUM_C;
        } else if (aminoAcidResidue.equalsIgnoreCase("D")) {
            return PeptideFragmentIonType.IMMONIUM_D;
        } else if (aminoAcidResidue.equalsIgnoreCase("E")) {
            return PeptideFragmentIonType.IMMONIUM_E;
        } else if (aminoAcidResidue.equalsIgnoreCase("F")) {
            return PeptideFragmentIonType.IMMONIUM_F;
        } else if (aminoAcidResidue.equalsIgnoreCase("G")) {
            return PeptideFragmentIonType.IMMONIUM_G;
        } else if (aminoAcidResidue.equalsIgnoreCase("H")) {
            return PeptideFragmentIonType.IMMONIUM_H;
        } else if (aminoAcidResidue.equalsIgnoreCase("I")) {
            return PeptideFragmentIonType.IMMONIUM_I;
        } else if (aminoAcidResidue.equalsIgnoreCase("K")) {
            return PeptideFragmentIonType.IMMONIUM_K;
        } else if (aminoAcidResidue.equalsIgnoreCase("L")) {
            return PeptideFragmentIonType.IMMONIUM_L;
        } else if (aminoAcidResidue.equalsIgnoreCase("M")) {
            return PeptideFragmentIonType.IMMONIUM_M;
        } else if (aminoAcidResidue.equalsIgnoreCase("N")) {
            return PeptideFragmentIonType.IMMONIUM_N;
        } else if (aminoAcidResidue.equalsIgnoreCase("P")) {
            return PeptideFragmentIonType.IMMONIUM_P;
        } else if (aminoAcidResidue.equalsIgnoreCase("Q")) {
            return PeptideFragmentIonType.IMMONIUM_Q;
        } else if (aminoAcidResidue.equalsIgnoreCase("R")) {
            return PeptideFragmentIonType.IMMONIUM_R;
        } else if (aminoAcidResidue.equalsIgnoreCase("S")) {
            return PeptideFragmentIonType.IMMONIUM_S;
        } else if (aminoAcidResidue.equalsIgnoreCase("T")) {
            return PeptideFragmentIonType.IMMONIUM_T;
        } else if (aminoAcidResidue.equalsIgnoreCase("V")) {
            return PeptideFragmentIonType.IMMONIUM_V;
        } else if (aminoAcidResidue.equalsIgnoreCase("W")) {
            return PeptideFragmentIonType.IMMONIUM_W;
        } else if (aminoAcidResidue.equalsIgnoreCase("Y")) {
            return PeptideFragmentIonType.IMMONIUM_Y;
        }

        return PeptideFragmentIonType.UNKNOWN;
    }
}
