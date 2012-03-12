package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class models a peptide fragment ion.
 *
 * Created by IntelliJ IDEA. User: Marc Date: Jun 18, 2010 Time: 8:58:02 AM
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
         * This int is the identifier for a b ion.
         */
        B_ION,
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
        Y_ION,
        /**
         * This int is the identifier for a z ion.
         */
        Z_ION,
        /**
         * This int is the identifier for an MH ion. The number of H is not
         * represented here.
         */
        PRECURSOR_ION,
        /**
         * This int is the identifier for an immonium ion.
         */
        IMMONIUM,
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
     * the neutral losses found on the ion
     */
    private ArrayList<NeutralLoss> neutralLosses = new ArrayList<NeutralLoss>();
    /**
     * position of the ion in the peptide for peptide ions (for a, b, c, x, y
     * and z ions)
     */
    private int number = -1;
    /**
     * Amino-acid generating the ion (for immonium ions)
     */
    private String residue;

    /**
     * Construction for a peptide fragment.
     *
     * @param type the type of ion according to static fields
     * @param mass the ion mass
     */
    public PeptideFragmentIon(PeptideFragmentIonType type, double mass) {
        this.type = type;
        this.theoreticMass = mass;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }

    /**
     * Construction for a peptide fragment with neutral loss.
     *
     * @param type the type of ion according to static fields
     * @param mass the ion mass (accounting for neutral losses)
     * @param number the ion number
     * @param neutralLosses List of neutral losses detected
     */
    public PeptideFragmentIon(PeptideFragmentIonType type, int number, double mass, ArrayList<NeutralLoss> neutralLosses) {
        this.type = type;
        this.theoreticMass = mass;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
        this.neutralLosses.addAll(neutralLosses);
        this.number = number;
    }

    /**
     * Construction for a peptide fragment.
     *
     * @param type the type of ion according to static fields
     * @param number the ion number
     * @param mass the ion mass
     */
    public PeptideFragmentIon(PeptideFragmentIonType type, int number, double mass) {
        this.type = type;
        this.number = number;
        this.theoreticMass = mass;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }

    /**
     * Construction for a peptide fragment.
     *
     * @param type the type of ion according to static fields
     * @param residue the ion number
     * @param mass the ion mass
     */
    public PeptideFragmentIon(PeptideFragmentIonType type, String residue, double mass) {
        this.type = type;
        this.residue = residue;
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
     *
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
        if (type == PeptideFragmentIonType.B_ION) {
            return "b";
        } else if (type == PeptideFragmentIonType.Y_ION) {
            return "y";
        } else if (type == PeptideFragmentIonType.A_ION) {
            return "a";
        } else if (type == PeptideFragmentIonType.C_ION) {
            return "c";
        } else if (type == PeptideFragmentIonType.X_ION) {
            return "x";
        } else if (type == PeptideFragmentIonType.Z_ION) {
            return "z";
        } else if (type == PeptideFragmentIonType.PRECURSOR_ION) {
            return "Prec";
        } else if (type == PeptideFragmentIonType.IMMONIUM) {
            return "i" + residue;
        }
        return "?";
    }

    /**
     * Returns the neutral loss (if any), the empty string if no loss.
     *
     * @return the neutral loss
     */
    public String getNeutralLoss() {
        ArrayList<String> names = new ArrayList<String>();
        for (NeutralLoss neutralLoss : neutralLosses) {
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
     * Returns the neutral losses for this fragment ion
     *
     * @return a list of neutral losses
     */
    public ArrayList<NeutralLoss> getNeutralLosses() {
        return neutralLosses;
    }

    /**
     * Returns the pride cv term adapted to the fragment ion. null if none
     * corresponding
     *
     * @return the pride cv term adapted to the fragment ion. null if none
     * corresponding
     */
    public CvTerm getPrideCvTerm() {
        switch (type) {
            case A_ION:
                if (neutralLosses.isEmpty()) {
                    return new CvTerm("PRIDE", "PRIDE:0000233", "a ion", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.H2O)) {
                    return new CvTerm("PRIDE", "PRIDE:0000234", "a ion -H2O", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.NH3)) {
                    return new CvTerm("PRIDE", "PRIDE:0000235", "a ion -NH3", null);
                } else {
                    return null;
                }
            case B_ION:
                if (neutralLosses.isEmpty()) {
                    return new CvTerm("PRIDE", "PRIDE:0000194", "b ion", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.H2O)) {
                    return new CvTerm("PRIDE", "PRIDE:0000196", "b ion -H2O", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.NH3)) {
                    return new CvTerm("PRIDE", "PRIDE:0000195", "b ion -NH3", null);
                } else {
                    return null;
                }
            case C_ION:
                if (neutralLosses.isEmpty()) {
                    return new CvTerm("PRIDE", "PRIDE:0000236", "c ion", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.H2O)) {
                    return new CvTerm("PRIDE", "PRIDE:0000237", "c ion -H2O", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.NH3)) {
                    return new CvTerm("PRIDE", "PRIDE:0000238", "c ion -NH3", null);
                } else {
                    return null;
                }
            case X_ION:
                if (neutralLosses.isEmpty()) {
                    return new CvTerm("PRIDE", "PRIDE:0000227", "x ion", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.H2O)) {
                    return new CvTerm("PRIDE", "PRIDE:0000228", "x ion -H2O", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.NH3)) {
                    return new CvTerm("PRIDE", "PRIDE:0000229", "x ion -NH3", null);
                } else {
                    return null;
                }
            case Y_ION:
                if (neutralLosses.isEmpty()) {
                    return new CvTerm("PRIDE", "PRIDE:0000193", "y ion", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.H2O)) {
                    return new CvTerm("PRIDE", "PRIDE:0000197", "y ion -H2O", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.NH3)) {
                    return new CvTerm("PRIDE", "PRIDE:0000198", "y ion -NH3", null);
                } else {
                    return null;
                }
            case Z_ION:
                if (neutralLosses.isEmpty()) {
                    return new CvTerm("PRIDE", "PRIDE:0000230", "z ion", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.H2O)) {
                    return new CvTerm("PRIDE", "PRIDE:0000231", "z ion -H2O", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.NH3)) {
                    return new CvTerm("PRIDE", "PRIDE:0000232", "z ion -NH3", null);
                } else {
                    return null;
                }
            case IMMONIUM:
                char aa = residue.charAt(0);
                switch (aa) {

                    case 'A':
                        return new CvTerm("PRIDE", "PRIDE:0000240", "immonium A", null);
                    case 'C':
                        return new CvTerm("PRIDE", "PRIDE:0000241", "immonium C", null);
                    case 'D':
                        return new CvTerm("PRIDE", "PRIDE:0000242", "immonium D", null);
                    case 'E':
                        return new CvTerm("PRIDE", "PRIDE:0000243", "immonium E", null);
                    case 'F':
                        return new CvTerm("PRIDE", "PRIDE:0000244", "immonium F", null);
                    case 'G':
                        return new CvTerm("PRIDE", "PRIDE:0000245", "immonium G", null);
                    case 'H':
                        return new CvTerm("PRIDE", "PRIDE:0000246", "immonium H", null);
                    case 'I':
                        return new CvTerm("PRIDE", "PRIDE:0000247", "immonium I", null);
                    case 'K':
                        return new CvTerm("PRIDE", "PRIDE:0000248", "immonium K", null);
                    case 'L':
                        return new CvTerm("PRIDE", "PRIDE:0000249", "immonium L", null);
                    case 'M':
                        return new CvTerm("PRIDE", "PRIDE:0000250", "immonium M", null);
                    case 'N':
                        return new CvTerm("PRIDE", "PRIDE:0000251", "immonium N", null);
                    case 'P':
                        return new CvTerm("PRIDE", "PRIDE:0000252", "immonium P", null);
                    case 'Q':
                        return new CvTerm("PRIDE", "PRIDE:0000253", "immonium Q", null);
                    case 'R':
                        return new CvTerm("PRIDE", "PRIDE:0000254", "immonium R", null);
                    case 'S':
                        return new CvTerm("PRIDE", "PRIDE:0000255", "immonium S", null);
                    case 'T':
                        return new CvTerm("PRIDE", "PRIDE:0000256", "immonium T", null);
                    case 'V':
                        return new CvTerm("PRIDE", "PRIDE:0000257", "immonium V", null);
                    case 'W':
                        return new CvTerm("PRIDE", "PRIDE:0000258", "immonium W", null);
                    case 'Y':
                        return new CvTerm("PRIDE", "PRIDE:0000259", "immonium Y", null);
                    default:
                        return null;
                }
            case PRECURSOR_ION:
                if (neutralLosses.isEmpty()) {
                    return new CvTerm("PRIDE", "PRIDE:0000263", "precursor ion", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.H2O)) {
                    return new CvTerm("PRIDE", "PRIDE:0000262", "precursor ion -H2O", null);
                } else if (neutralLosses.size() == 1 && neutralLosses.get(0).isSameAs(NeutralLoss.NH3)) {
                    return new CvTerm("PRIDE", "PRIDE:0000261", "precursor ion -NH3", null);
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return getIonType() + getNeutralLoss();
    }
}
