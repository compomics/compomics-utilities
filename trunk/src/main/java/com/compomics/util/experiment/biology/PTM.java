package com.compomics.util.experiment.biology;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 8:02:00 PM
 * This class modelizes a post-translational modification.
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

    /**
     * the modification type according to static field
     */
    private int type;
    /**
     * the residues affected by this modification. '[' denotes N-term and ']' C-term
     */
    private String[] residuesArray;
    /**
     * Name of the modification
     */
    private String name;
    /**
     * Mass difference produced by this modification
     */
    private double mass;

    /**
     * Constructor for the modification
      */
    public PTM() {

    }

    /**
     * Constructor for a reference modification
     * @param type              Type of modification according to static attributes
     * @param name              Name of the modification
     * @param mass              Mass difference produced by the modification
     * @param residuesArray     Residue array affected by this modification
     */
    public PTM(int type, String name, double mass, String[] residuesArray) {
        this.type = type;
        this.name = name;
        this.mass = mass;
        this.residuesArray = residuesArray;
    }


    /**
     * getter for the modification type
     * @return the modification type
     */
    public int getType() {
        return type;
    }

    /**
     * getter for the modification name
     * @return the modification name
     */
    public String getName() {
        return name;
    }

    /**
     * getter for the mass difference induced by this modification
     * @return the mass difference induced by the modification
     */
    public double getMass() {
        return mass;
    }

    /**
     * getter for the residues affected by this modification
     * @return an array containing potentially modified residues
     */
    public String[] getResiduesArray() {
        return residuesArray;
    }
}
