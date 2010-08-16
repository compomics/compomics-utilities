package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.massspectrometry.Charge;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:58:02 AM
 * This class modelizes a peptide fragment ion.
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
     * This int is the identifier for a precursor ion. The nature of the loss is not coded yet.
     */
    public final static int PRECURSOR_LOSS = 16;


    // Attributes

    private int type;
    private int number;
    private Charge charge;


    // Constructors

    public PeptideFragmentIon(int type, double mz) {
        this.type = type;
        this.theoreticMass = mz;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }

    public PeptideFragmentIon(int type, int number, Charge charge) {
        this.type = type;
        this.number = number;
        this.charge = charge;
        this.familyType = Ion.PEPTIDE_FRAGMENT;
    }

    // Methods

    public int getType() {
        return type;
    }
}
