package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;

/**
 * This class models a peptide fragment ion.
 *
 * @author Marc Vaudel
 */
public class PeptideFragmentIon extends Ion {

    /**
     * Serial number for backward compatibility
     */
    static final long serialVersionUID = 8283809283803740651L;
    /**
     * Identifier for an a ion.
     */
    public static final int A_ION = 0;
    /**
     * Identifier for a b ion.
     */
    public static final int B_ION = 1;
    /**
     * Identifier for a c ion.
     */
    public static final int C_ION = 2;
    /**
     * Identifier for an x ion.
     */
    public static final int X_ION = 3;
    /**
     * Identifier for a y ion.
     */
    public static final int Y_ION = 4;
    /**
     * Identifier for a z ion.
     */
    public static final int Z_ION = 5;
    /**
     * The neutral losses found on the ion.
     */
    private ArrayList<NeutralLoss> neutralLosses = new ArrayList<NeutralLoss>();
    /**
     * Position of the ion in the peptide for peptide ions.
     */
    private int number = -1;
    /**
     * The type of fragment.
     */
    private int subType;

    /**
     * Constructor.
     *
     * @param fragmentType the type of peptide fragment ion as indexed by the
     * static fields
     * @param number the number of the fragment ion
     * @param mass the mass of the fragment ion
     * @param neutralLosses the neutral losses of the ion
     */
    public PeptideFragmentIon(int fragmentType, int number, double mass, ArrayList<NeutralLoss> neutralLosses) {
        if (neutralLosses == null) {
            neutralLosses = new ArrayList<NeutralLoss>();
        }
        this.subType = fragmentType;
        type = IonType.PEPTIDE_FRAGMENT_ION;
        this.theoreticMass = mass;
        this.neutralLosses.addAll(neutralLosses);
        this.number = number;
    }

    /**
     * Constructor for a generic ion.
     *
     * @param fragmentType the type of peptide fragment ion as indexed by the
     * static fields
     * @param neutralLosses the neutral losses of the ion
     */
    public PeptideFragmentIon(int fragmentType, ArrayList<NeutralLoss> neutralLosses) {
        if (neutralLosses == null) {
            neutralLosses = new ArrayList<NeutralLoss>();
        }
        this.subType = fragmentType;
        type = IonType.PEPTIDE_FRAGMENT_ION;
        this.neutralLosses.addAll(neutralLosses);
    }

    /**
     * Constructor for a generic ion without neutral losses.
     *
     * @param fragmentType the type of peptide fragment ion as indexed by the
     * static fields
     */
    public PeptideFragmentIon(int fragmentType) {
        this.subType = fragmentType;
        type = IonType.PEPTIDE_FRAGMENT_ION;
    }

    /**
     * Returns the number of the fragment in the sequence.
     *
     * @return the number of the fragment in the sequence
     */
    public int getNumber() {
        return number;
    }

    @Override
    public ArrayList<NeutralLoss> getNeutralLosses() {
        return neutralLosses;
    }

    @Override
    public String getName() {
        return getSubTypeAsString() + getNeutralLossesAsString();
    }

    @Override
    public CvTerm getPrideCvTerm() {
        switch (subType) {
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
            default:
                return null;
        }
    }

    @Override
    public int getSubType() {
        return subType;
    }

    @Override
    public String getSubTypeAsString() {
        try {
            return getSubTypeAsString(subType);
        } catch (UnsupportedOperationException e) {
            throw new UnsupportedOperationException("No name for subtype: " + subType + " of " + getTypeAsString() + ".");
        }
    }

    /**
     * Returns the type of fragment ion as a letter.
     *
     * @param subType the subtype
     * @return the type of fragment ion as a letter
     */
    public static String getSubTypeAsString(int subType) {
        switch (subType) {
            case A_ION:
                return "a";
            case B_ION:
                return "b";
            case C_ION:
                return "c";
            case X_ION:
                return "x";
            case Y_ION:
                return "y";
            case Z_ION:
                return "z";
            default:
                throw new UnsupportedOperationException("No name for subtype: " + subType + ".");
        }
    }

    /**
     * Returns an arraylist of possible subtypes.
     *
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        ArrayList<Integer> possibleTypes = new ArrayList<Integer>();
        possibleTypes.add(A_ION);
        possibleTypes.add(B_ION);
        possibleTypes.add(C_ION);
        possibleTypes.add(X_ION);
        possibleTypes.add(Y_ION);
        possibleTypes.add(Z_ION);
        return possibleTypes;
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        return anotherIon.getType() == IonType.PEPTIDE_FRAGMENT_ION
                && anotherIon.getSubType() == subType
                && ((PeptideFragmentIon) anotherIon).getNumber() == number
                && anotherIon.getNeutralLossesAsString().equals(getNeutralLossesAsString());
    }
}
