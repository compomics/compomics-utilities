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
    private NeutralLoss[] neutralLosses = null;
    /**
     * Position of the ion in the peptide for peptide ions.
     */
    private int number = -1;
    /**
     * The type of fragment.
     */
    private int subType;
    /**
     * The CV term of the reporter ion, null if not set.
     */
    private CvTerm cvTerm = null;
    /**
     * The PSI MS CV term of the reporter ion, null if not set.
     */
    private CvTerm psiCvTerm = null;

    /**
     * Constructor.
     *
     * @param fragmentType the type of peptide fragment ion as indexed by the
     * static fields
     * @param number the number of the fragment ion
     * @param mass the mass of the fragment ion
     * @param neutralLosses the neutral losses of the ion
     */
    public PeptideFragmentIon(int fragmentType, int number, double mass, NeutralLoss[] neutralLosses) {
        if (neutralLosses != null) {
            this.neutralLosses = neutralLosses;
        }
        this.subType = fragmentType;
        type = IonType.PEPTIDE_FRAGMENT_ION;
        this.theoreticMass1 = mass;
        this.number = number;
    }

    /**
     * Constructor for a generic ion.
     *
     * @param fragmentType the type of peptide fragment ion as indexed by the
     * static fields
     * @param neutralLosses the neutral losses of the ion
     */
    public PeptideFragmentIon(int fragmentType, NeutralLoss[] neutralLosses) {
        if (neutralLosses != null) {
            this.neutralLosses = neutralLosses;
        }
        this.subType = fragmentType;
        type = IonType.PEPTIDE_FRAGMENT_ION;
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
    public NeutralLoss[] getNeutralLosses() {
        return neutralLosses;
    }

    @Override
    public String getName() {
        return getSubTypeAsString() + getNeutralLossesAsString();
    }

    /**
     * Returns the name with number. For example b5-H2O.
     *
     * @return the name with number
     */
    public String getNameWithNumber() {
        return getSubTypeAsString() + getNumber() + getNeutralLossesAsString();
    }

    @Override
    public CvTerm getPrideCvTerm() {

        if (cvTerm == null) {

            switch (subType) {
                case A_ION:
                    if (neutralLosses == null || neutralLosses.length == 0) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001229", "frag: a ion", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001234", "frag: a ion - H2O", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001235", "frag: a ion - NH3", "" + getNumber());
                    }
                    break;
                case B_ION:
                    if (neutralLosses == null || neutralLosses.length == 0) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001224", "frag: b ion", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001222", "frag: b ion - H2O", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001232", "frag: b ion - NH3", "" + getNumber());
                    }
                    break;
                case C_ION:
                    if (neutralLosses == null || neutralLosses.length == 0) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001231", "frag: c ion", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001515", "frag: c ion - H2O", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001516", "frag: c ion - NH3", "" + getNumber());
                    }
                    break;
                case X_ION:
                    if (neutralLosses == null || neutralLosses.length == 0) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001228", "frag: x ion", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001519", "frag: x ion - H2O", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001520", "frag: x ion - NH3", "" + getNumber());
                    }
                    break;
                case Y_ION:
                    if (neutralLosses == null || neutralLosses.length == 0) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001220", "frag: y ion", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001223", "frag: y ion - H2O", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001233", "frag: y ion - NH3", "" + getNumber());
                    }
                    break;
                case Z_ION:
                    if (neutralLosses == null || neutralLosses.length == 0) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001230", "frag: z ion", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001517", "frag: z ion - H2O", "" + getNumber());
                    } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                        cvTerm = new CvTerm("PSI-MS", "MS:1001518", "frag: z ion - NH3", "" + getNumber());
                    }
                    break;
            }
        }

        return cvTerm;
    }

    @Override
    public CvTerm getPsiMsCvTerm() {

        if (psiCvTerm != null) {
            return psiCvTerm;
        }

        switch (subType) {
            case A_ION:
                psiCvTerm = new CvTerm("PSI-MS", "MS:1001229", "frag: a ion", null);
                break;
            case B_ION:
                psiCvTerm = new CvTerm("PSI-MS", "MS:1001224", "frag: b ion", null);
                break;
            case C_ION:
                psiCvTerm = new CvTerm("PSI-MS", "MS:1001231", "frag: c ion", null);
                break;
            case X_ION:
                psiCvTerm = new CvTerm("PSI-MS", "MS:1001228", "frag: x ion", null);
                break;
            case Y_ION:
                psiCvTerm = new CvTerm("PSI-MS", "MS:1001220", "frag: y ion", null);
                break;
            case Z_ION:
                psiCvTerm = new CvTerm("PSI-MS", "MS:1001230", "frag: z ion", null);
                break;
        }

        return psiCvTerm;
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
     * Returns the type of fragment ion as a letter. e.g. 'a' for an a-ion.
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
     * Returns the ion index corresponding to the given symbol in the drop down
     * menu.
     *
     * @param ionSymbol the ion symbol
     *
     * @return the ion index corresponding to the given symbol in the drop down
     * menu
     */
    public static Integer getIonType(String ionSymbol) {
        if (ionSymbol.equals("a")) {
            return PeptideFragmentIon.A_ION;
        } else if (ionSymbol.equals("b")) {
            return PeptideFragmentIon.B_ION;
        } else if (ionSymbol.equals("c")) {
            return PeptideFragmentIon.C_ION;
        } else if (ionSymbol.equals("x")) {
            return PeptideFragmentIon.X_ION;
        } else if (ionSymbol.equals("y")) {
            return PeptideFragmentIon.Y_ION;
        } else if (ionSymbol.equals("z")) {
            return PeptideFragmentIon.Z_ION;
        }
        throw new UnsupportedOperationException("Ion of type " + ionSymbol + " not supported.");
    }

    /**
     * Returns an arraylist of possible subtypes.
     *
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        ArrayList<Integer> possibleTypes = new ArrayList<>();
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
