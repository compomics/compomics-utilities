package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.massspectrometry.Charge;

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
     * This int is the identifier for an a ion.
     */
    public final static int A_ION = 0;
    /**
     * This int is the identifier for an a* ion.
     */
    public final static int ANH3_ION = 1;
    /**
     * This int is the identifier for an a° ion.
     */
    public final static int AH2O_ION = 2;
    /**
     * This int is the identifier for a b ion.
     */
    public final static int B_ION = 3;
    /**
     * This int is the identifier for a b* ion.
     */
    public final static int BNH3_ION = 4;
    /**
     * This int is the identifier for a b° ion.
     */
    public final static int BH2O_ION = 5;
    /**
     * This int is the identifier for a c ion.
     */
    public final static int C_ION = 6;
    /**
     * This int is the identifier for a x ion.
     */
    public final static int X_ION = 7;
    /**
     * This int is the identifier for a y ion.
     */
    public final static int Y_ION = 8;
    /**
     * This int is the identifier for a y* ion.
     */
    public final static int YNH3_ION = 9;
    /**
     * This int is the identifier for a y° ion.
     */
    public final static int YH2O_ION = 10;
    /**
     * This int is the identifier for a z ion.
     */
    public final static int Z_ION = 11;
    /**
     * This int is the identifier for an MH ion. The number of H is not represented here.
     */
    public final static int MH_ION = 12;
    /**
     * This int is the identifier for an MH-NH3 ion.
     */
    public final static int MHNH3_ION = 13;
    /**
     * This int is the identifier for an MH-H2O ion.
     */
    public final static int MHH2O_ION = 14;
    /**
     * This int is the identifier for an immonium ion. The nature of the immonium ion is not coded yet.
     */
    public final static int IMMONIUM = 15;
    /**
     * This int is the identifier for a precursor ion loss. The nature of the loss is not coded yet.
     */
    public final static int PRECURSOR_LOSS = 16;
    /**
     * Type of ion
     */
    private int type;
    /**
     * position of the ion in the peptide for peptide ions
     */
    private int number;
    /**
     * Ion charge
     */
    private Charge charge;
    /**
     * Ion intensity.
     */
    private Double intensity;

    /**
     * Construction for a peptide fragment.
     *
     * @param type  the type of ion according to static fields
     * @param mz    the ion m/z
     */
    public PeptideFragmentIon(int type, double mz) {
        this.type = type;
        this.theoreticMass = mz;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }

    /**
     * Construction for a peptide fragment.
     *
     * @param type      the type of ion according to static fields
     * @param number    the ion number
     * @param mz        the ion m/z
     */
    public PeptideFragmentIon(int type, int number, double mz) {
        this.type = type;
        this.number = number;
        this.theoreticMass = mz;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }

    /**
     * Constructor for a theoretic peptide fragment with ion number in the sequence
     *
     * @param type      the type of ion according to static fields
     * @param number    the ion number
     * @param charge    the ion charge
     */
    public PeptideFragmentIon(int type, int number, Charge charge) {
        this.type = type;
        this.number = number;
        this.charge = charge;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }
    
    /**
     * Getter for the charge
     *
     * @return the ion charge
     */
    public Charge getCharge() {
        return charge;
    }

    /**
     * Setter for the charge
     *
     * @param charge
     */
    public void setCharge(Charge charge) {
        this.charge = charge;
    }
    
    /**
     * Getter for the intensity
     *
     * @return the intensity
     */
    public Double getIntensity() {
        return intensity;
    }

    /**
     * Setter for the intensity
     *
     * @param intensity
     */
    public void setIntensity(Double intensity) {
        this.intensity = intensity;
    }

    /**
     * Getter for the ion type
     * 
     * @return the ion type according to the static fields
     */
    public int getType() {
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

        if (type == B_ION || type == BH2O_ION || type == BNH3_ION) {
            return "b";
        } else if (type == Y_ION || type == YH2O_ION || type == YNH3_ION) {
            return "y";
        } else if (type == A_ION || type == AH2O_ION || type == ANH3_ION) {
            return "a";
        } else if (type == C_ION) {
            return "c";
        } else if (type == X_ION) {
            return "x";
        } else if (type == Z_ION) {
            return "z";
        } else if (type == MH_ION || type == MHNH3_ION || type == MHH2O_ION) {
            return "Prec"; //  @TODO: verify that this is correct!!
        } else if (type == IMMONIUM) {
            return "i";
        } else if (type == PRECURSOR_LOSS) {
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

        if (type == BH2O_ION || type == YH2O_ION
                || type == AH2O_ION || type == MHH2O_ION) {
            return "-H20";
        } else if (type == BNH3_ION || type == YNH3_ION
                || type == ANH3_ION || type == MHNH3_ION) {
            return "-NH3";
        }

        return "";
    }
}
