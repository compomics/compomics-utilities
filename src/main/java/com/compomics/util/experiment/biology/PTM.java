package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;

/**
 * This class models a post-translational modification.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 8:02:00 PM
 */
public class PTM extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -545472596243822505L;
    /**
     * modification at particular amino acids
     */
    public static final int MODAA = 0;
    /**
     * modification at the N terminus of a protein
     */
    public static final int MODN = 1;
    /**
     * modification at the N terminus of a protein at particular amino acids
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
    private ArrayList<String> residuesArray = new ArrayList<String>();
    /**
     * Name of the modification
     */
    private String name;
    /**
     * Short name of the modification
     */
    private String shortName;
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
     *
     * @param type              Type of modification according to static attributes
     * @param name              Name of the modification
     * @param mass              Mass difference produced by the modification
     * @param residuesArray     Residue array affected by this modification
     */
    public PTM(int type, String name, double mass, ArrayList<String> residuesArray) {
        this.type = type;
        this.name = name;
        this.mass = mass;
        this.residuesArray.addAll(residuesArray);
    }
    
    /**
     * Constructor for a reference modification
     *
     * @param type              Type of modification according to static attributes
     * @param name              Name of the modification
     * @param shortName         Short name of the modification
     * @param mass              Mass difference produced by the modification
     * @param residuesArray     Residue array affected by this modification
     */
    public PTM(int type, String name, String shortName, double mass, ArrayList<String> residuesArray) {
        this.type = type;
        this.name = name;
        this.shortName = shortName;
        this.mass = mass;
        this.residuesArray.addAll(residuesArray);
    }

    /**
     * getter for the modification type
     *
     * @return the modification type
     */
    public int getType() {
        return type;
    }

    /**
     * getter for the modification name
     *
     * @return the modification name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the ptm name
     * @param name  the ptm name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * getter for the short modification name
     *
     * @return the short modification name
     */
    public String getShortName() {
        return shortName;
    }
    
    /**
     * Sets the short ptm name
     * @param shortName  the ptm name
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * getter for the mass difference induced by this modification
     *
     * @return the mass difference induced by the modification
     */
    public double getMass() {
        return mass;
    }

    /**
     * getter for the residues affected by this modification
     * 
     * @return an array containing potentially modified residues
     */
    public ArrayList<String> getResidues() {
        return residuesArray;
    }
    
    /**
     * Compares two PTMs
     * @param anotherPTM another PTM
     * @return true if the given PTM is the same as the current ptm
     */
    public boolean isSameAs(PTM anotherPTM) {
        if (type != anotherPTM.getType()
                || mass != anotherPTM.getMass()
                || residuesArray.size() != anotherPTM.getResidues().size()) {
            return false;
        }
        for (String aa : anotherPTM.getResidues()) {
            if (!residuesArray.contains(aa)) {
                return false;
            }
        }
        return true;
    }
}
