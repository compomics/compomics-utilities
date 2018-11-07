/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 1-jul-2004
 * Time: 15:05:12
 */
package com.compomics.util.interfaces;

import java.util.Collection;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This interface describes the behaviour for a modification on a peptide or aminoacid sequence.
 *
 * @author Lennart Martens
 */
public interface Modification extends Comparable {
    /**
     * Constant to indicate the position of the monoisotopic mass in the mass array.
     */
    public int MONOISOTOPIC = 0;
    /**
     * Constant to indicate the position of the average mass in the mass array.
     */
    public int AVERAGE = 1;
    /**
     * The residue code for the N-terminus.
     */
    public String NTERMINUS = "0";
    /**
     * The residue code for the C-terminus.
     */
    public String CTERMINUS = "1";

    /**
     * This method returns the location of the modification in the sequence.
     * Note that the N-terminus is 0, and the C-terminus is (sequence_length)+1.
     *
     * @return  int with the location for the modification.
     */
    public abstract int getLocation();

    /**
     * This method indicates whether this modification  is considered an artifact.
     *
     * @return  boolean that indicates whether this modification is an artifact.
     */
    public abstract boolean isArtifact();

    /**
     * This method returns the short code for the modification, eg. 'Mox'.
     *
     * @return  String  with the short code for the modification.
     *                  Can be used to annotate a sequence.
     */
    public abstract String getCode();

    /**
     * This method returns the title of the modification,
     * eg. 'Oxidation Met'.
     *
     * @return  String with the title for the modification.
     */
    public abstract String getTitle();

    /**
     * This method returns a double with the monoisotopic mass difference
     * conferred on the sequence by this modification for the specified residue.
     * This mass delta can be negative!
     *
     * @param   aResidue String with the residue for which the mass delta needs to be calculated.
     * @return  double with the monoisotopic mass difference.
     */
    public abstract double getMonoisotopicMassDelta(String aResidue);

    /**
     * This method returns a double with the average mass difference
     * conferred on the sequence by this modification for the specified residue.
     * This mass delta can be negative!
     *
     * @param   aResidue String with the residue for which the mass delta needs to be calculated.
     * @return  double with the average mass difference.
     */
    public abstract double getAverageMassDelta(String aResidue);

    /**
     * This method allows the setting of the location for this modification.
     * The specified integer should be calculated from the start of the parent sequence,
     * where the N-terminus is 0, and the C-terminus is (sequence_length)+1.
     *
     * @param aLocation int with the location for this modification within the parent sequence.
     */
    public abstract void setLocation(int aLocation);

    /**
     * This method reports on all the residues that can be modified by this Modification.
     *
     * @return  Collection with the residues that can be modified by this modification.
     */
    public abstract Collection getResidues();

    /**
     * This method returns a String representation of the Modification.
     *
     * @return  String with a String Representation of the Modification.
     */
    public abstract String toString();
}
