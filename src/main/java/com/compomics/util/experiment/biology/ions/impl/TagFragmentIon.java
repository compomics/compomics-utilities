package com.compomics.util.experiment.biology.ions.impl;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;

/**
 * A fragment ion obtained from a tag.
 *
 * @author Marc Vaudel
 */
public class TagFragmentIon extends Ion {

    /**
     * Identifier for an a ion.
     */
    public static final int A_ION = PeptideFragmentIon.A_ION;
    /**
     * Identifier for a b ion.
     */
    public static final int B_ION = PeptideFragmentIon.B_ION;
    /**
     * Identifier for a c ion.
     */
    public static final int C_ION = PeptideFragmentIon.C_ION;
    /**
     * Identifier for an x ion.
     */
    public static final int X_ION = PeptideFragmentIon.X_ION;
    /**
     * Identifier for a y ion.
     */
    public static final int Y_ION = PeptideFragmentIon.Y_ION;
    /**
     * Identifier for a z ion.
     */
    public static final int Z_ION = PeptideFragmentIon.Z_ION;
    /**
     * The neutral losses found on the ion.
     */
    private NeutralLoss[] neutralLosses = null;
    /**
     * Position of the ion in the tag of amino acids considering gaps as an
     * amino acid. 0 based.
     */
    private int number = -1;
    /**
     * Position of the ion in the current sequence of amino acids. 0 based.
     */
    private int subNumber = -1;
    /**
     * The type of fragment.
     */
    private int subType;
    /**
     * Mass gap before this ion.
     */
    private double massGap = 0;
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
     * @param subNumber the number of the fragment ion in the current amino acid
     * sequence
     * @param mass the mass of the fragment ion
     * @param neutralLosses the neutral losses of the ion
     * @param massGap the mass gap before this tag fragment ions
     */
    public TagFragmentIon(int fragmentType, int number, int subNumber, double mass, NeutralLoss[] neutralLosses, double massGap) {
        this.neutralLosses = neutralLosses;
        this.subType = fragmentType;
        type = Ion.IonType.TAG_FRAGMENT_ION;
        this.theoreticMass1 = mass;
        this.number = number;
        this.subNumber = subNumber;
        this.massGap = massGap;
    }

    /**
     * Constructor for a generic ion.
     *
     * @param fragmentType the type of peptide fragment ion as indexed by the
     * static fields
     * @param neutralLosses the neutral losses of the ion
     */
    public TagFragmentIon(int fragmentType, NeutralLoss[] neutralLosses) {
        this.neutralLosses = neutralLosses;
        this.subType = fragmentType;
        type = Ion.IonType.PEPTIDE_FRAGMENT_ION;
    }

    /**
     * Constructor for a generic ion without neutral losses.
     *
     * @param fragmentType the type of peptide fragment ion as indexed by the
     * static fields
     */
    public TagFragmentIon(int fragmentType) {
        this.subType = fragmentType;
        type = Ion.IonType.PEPTIDE_FRAGMENT_ION;
    }

    /**
     * Returns the number of the fragment in the tag. 0 based.
     *
     * @return the number of the fragment in the tag
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns the number of the fragment in the current amino acid sequence. 0
     * based.
     *
     * @return the number of the fragment in the current amino acid sequence
     */
    public int getSubNumber() {
        return subNumber;
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

    /**
     * Returns the name with number and mass gap. For example 110.0...b5-H2O.
     *
     * @return the name with number
     */
    public String getNameWithGapAndNumber() {
        if (massGap == 0 || subNumber > 1) {
            return getNameWithNumber();
        } else {
            return massGap + "-" + getSubTypeAsString() + getNumber() + getNeutralLossesAsString();
        }
    }

    @Override
    public CvTerm getPrideCvTerm() {

        if (cvTerm != null) {
            return cvTerm;
        }

        switch (subType) {
            case A_ION:
                if (neutralLosses == null || neutralLosses.length == 0) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001229", "frag: a ion", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001234", "frag: a ion - H2O", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001235", "frag: a ion - NH3", null);
                }
                break;
            case B_ION:
                if (neutralLosses == null || neutralLosses.length == 0) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001224", "frag: b ion", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001222", "frag: b ion - H2O", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001232", "frag: b ion - NH3", null);
                }
                break;
            case C_ION:
                if (neutralLosses == null || neutralLosses.length == 0) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001231", "frag: c ion", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001515", "frag: c ion - H2O", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001516", "frag: c ion - NH3", null);
                }
                break;
            case X_ION:
                if (neutralLosses == null || neutralLosses.length == 0) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001228", "frag: x ion", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001519", "frag: x ion - H2O", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001520", "frag: x ion - NH3", null);
                }
                break;
            case Y_ION:
                if (neutralLosses == null || neutralLosses.length == 0) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001220", "frag: y ion", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001223", "frag: y ion - H2O", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001233", "frag: y ion - NH3", null);
                }
                break;
            case Z_ION:
                if (neutralLosses == null || neutralLosses.length == 0) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001230", "frag: z ion", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001517", "frag: z ion - H2O", null);
                } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
                    cvTerm = new CvTerm("PSI-MS", "MS:1001518", "frag: z ion - NH3", null);
                }
                break;
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
        return anotherIon.getType() == Ion.IonType.TAG_FRAGMENT_ION
                && anotherIon.getSubType() == subType
                && ((PeptideFragmentIon) anotherIon).getNumber() == number
                && anotherIon.getNeutralLossesAsString().equals(getNeutralLossesAsString());
    }

    /**
     * Returns the mass gap comprised in this ion.
     *
     * @return the mass gap
     */
    public double getMassGap() {
        return massGap;
    }
}
