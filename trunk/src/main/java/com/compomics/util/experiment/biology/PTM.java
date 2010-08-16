package com.compomics.util.experiment.biology;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 8:02:00 PM
 * This class modelizes a modification.
 */
public class PTM  implements Serializable {

    /**
     * modification at particular amino acids
     */
    public static final int MODAA = 0;
    /**
     * modification at the N terminus of a protein
     */
    public static final int MODN = 1;
    /**
     * modification at the N terminus of a protein
     */
    public static final int MODNAA = 2;
    /**
     * modification at the C terminus of a protein
     */
    public static final int MODC = 3;
    /**
     * modification at the C terminus of a protein at particular amino acids
     */
    public static final int MODCAA = 4;
    /**
     * modification at the N terminus of a peptide
     */
    public static final int MODNP = 5;
    /**
     * modification at the N terminus of a peptide at particular amino acids
     */
    public static final int MODNPAA = 6;
    /**
     * modification at the C terminus of a peptide
     */
    public static final int MODCP = 7;
    /**
     * modification at the C terminus of a peptide at particular amino acids
     */
    public static final int MODCPAA = 8;
    /**
     * the max number of modification types
     */
    public static final int MODMAX = 9;



    // Attributes

    private int type;
    private String[] residuesArray; // coded according to X!Tandem standards
    private String name;
    private double mass;


    // Constructors

    public PTM() {

    }

    // Constructor used to create reference PTM
    public PTM(int type, String name, double mass, String[] residuesArray) {
        this.type = type;
        this.name = name;
        this.mass = mass;
        this.residuesArray = residuesArray;
    }


    // Methods

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getMass() {
        return mass;
    }

    public String[] getResiduesArray() {
        return residuesArray;
    }
}
