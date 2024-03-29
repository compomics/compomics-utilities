/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 1-jul-2004
 * Time: 15:06:24
 */
package com.compomics.util.protein;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collection;
import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements a specific Modification.
 *
 * @author Lennart Martens
 */
public class ModificationImplementation implements com.compomics.util.interfaces.Modification, Cloneable {

    /**
     * Empty default constructor
     */
    public ModificationImplementation() {
    }

    // Class specific log4j logger for ModificationImplementation instances.
    Logger logger = LogManager.getLogger(ModificationImplementation.class);

    /**
     * The ModificationTemplate which holds all the shared information for a modification.
     * It is to be considered a singleton and it is immutable.
     */
    protected ModificationTemplate iTemplate = null;

    /**
     * The location for the modification in the parent sequence.
     */
    protected int iLocation = -1;

    /**
     * This constructor allows initialization of all the properties for the modification.
     *
     * @param aTemplate ModificationTemplate with the shared information for this modification
     * @param aLocation int with the location of this modification in the parent sequence.
     */
    public ModificationImplementation(ModificationTemplate aTemplate, int aLocation) {
        this.iTemplate = aTemplate;
        this.iLocation = aLocation;
    }

    /**
     * This constructor allows initialization of all the properties for the modification.
     *
     * @param aTitle    String with the title for the modification (eg., Oxidation Met).
     * @param aCode String with the code for the modification (eg., Mox). The code can be used when
     *              annotating a sequence String (eg., NH2-MGTEFSM&lt;Mox&gt;R-COOH).
     * @param aMassDeltas   HashMap with the following 'key-value' mappings: (key &gt; value) <br>
     *                      (residue &gt; double[]{MONOISOTOPIC_DELTA, AVERAGE_DELTA} <br>
     *                      Note that the residues for the N-terminus and C-terminus are represented
     *                      by the NTERMINUS and CTERMINUS constants, respectively.
     * @param aLocation int with the location for this modification.
     */
    public ModificationImplementation(String aTitle, String aCode, HashMap aMassDeltas, int aLocation) {
        this(aTitle, aCode, aMassDeltas, false, aLocation);
    }

    /**
     * This constructor allows initialization of all the properties for the modification.
     *
     * @param aTitle    String with the title for the modification (eg., Oxidation Met).
     * @param aCode String with the code for the modification (eg., Mox). The code can be used when
     *              annotating a sequence String (eg., NH2-MGTEFSM&lt;Mox&gt;R-COOH).
     * @param aMassDeltas   HashMap with the following 'key-value' mappings: (key &gt; value) <br>
     *                      (residue &gt; double[]{MONOISOTOPIC_DELTA, AVERAGE_DELTA} <br>
     *                      Note that the residues for the N-terminus and C-terminus are represented
     *                      by the NTERMINUS and CTERMINUS constants, respectively.
     * @param aArtifact boolean to indicate whether this modification is an artifact.
     * @param aLocation int with the location for this modification.
     */
    public ModificationImplementation(String aTitle, String aCode, HashMap aMassDeltas, boolean aArtifact, int aLocation) {
        this(new ModificationTemplate(aTitle, aCode, aMassDeltas, aArtifact), aLocation);
    }

    /**
     * This method returns a double with the average mass difference
     * conferred on the sequence by this modification for the specified residue.
     * This mass delta can be negative! When a residue was specified that is not affected by this
     * modification, '0.0' is returned.
     *
     * @param   aResidue String with the residue for which the mass delta needs to be calculated.
     * @return  double with the average mass difference.
     */
    public double getAverageMassDelta(String aResidue) {
        return this.getMassDelta(aResidue, AVERAGE);
    }

    /**
     * This method returns the short code for the modification, eg. 'Mox'.
     *
     * @return  String  with the short code for the modification.
     *                  Can be used to annotate a sequence.
     */
    public String getCode() {
        return this.iTemplate.getCode();
    }

    /**
     * This method returns the title of the modification,
     * eg. 'Oxidation Met'.
     *
     * @return  String with the title for the modification.
     */
    public String getTitle() {
        return this.iTemplate.getTitle();
    }

    /**
     * This method returns the location of the modification in the sequence.
     * Note that the N-terminus is 0, and the C-terminus is (sequence_length)+1.
     *
     * @return  int with the location for the modification.
     */
    public int getLocation() {
        return this.iLocation;
    }

    /**
     * This method reports on all the residues that can be modified by this Modification.
     *
     * @return  Collection with the residues that can be modified by this modification.
     */
    public Collection getResidues() {
        return this.iTemplate.getResidues();
    }

    /**
     * This method returns a double with the monoisotopic mass difference
     * conferred on the sequence by this modification for the specified residue.
     * This mass delta can be negative! When a residue was specified that is not affected by this
     * modification, '0.0' is returned.
     *
     * @param   aResidue String with the residue for which the mass delta needs to be calculated.
     * @return  double with the monoisotopic mass difference.
     */
    public double getMonoisotopicMassDelta(String aResidue) {
        return this.getMassDelta(aResidue, MONOISOTOPIC);
    }

    /**
     * This method allows the setting of the location for this modification.
     * The specified integer should be calculated from the start of the parent sequence,
     * where the N-terminus is 0, and the C-terminus is (sequence_length)+1.
     *
     * @param aLocation int with the location for this modification within the parent sequence.
     */
    public void setLocation(int aLocation) {
        this.iLocation = aLocation;
    }

    /**
     * This method indicates whether this modification  is considered an artifact.
     *
     * @return  boolean that indicates whether this modification is an artifact.
     */
    public boolean isArtifact() {
        return this.iTemplate.isArtifact();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * For this class comparison is based on:
     * <ul>
     *  <li> class identity </li>
     *  <li> title equality </li>
     *  <li> location equality </li>
     * </ul>
     *
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see     #hashCode()
     */
    public boolean equals(Object obj) {
        boolean result = true;
        // Class equality (instanceof is too lenient as it will vouch for subclasses as well!)
        if(obj == null || !obj.getClass().equals(this.getClass())) {
            result = false;
        } else if(((ModificationImplementation)obj).iLocation == this.iLocation) {
            // Equality is further defined by title.
            result = this.iTemplate.equals(((ModificationImplementation)obj).iTemplate);
        } else {
            // This means that the location was different, and therefore these
            // instances cannot be equal.
            result = false;
        }
        return result;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * <p>
     * The general contract of <code>hashCode</code> is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the <code>hashCode</code> method
     *     must consistently return the same integer, provided no information
     *     used in <code>equals</code> comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the <code>equals(Object)</code>
     *     method, then calling the <code>hashCode</code> method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link Object#equals(Object)}
     *     method, then calling the <code>hashCode</code> method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hashtables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by
     * class <code>Object</code> does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the Java programming language.)
     *
     * @return  a hash code value for this object.
     * @see     Object#equals(Object)
     */
    public int hashCode() {
        return iTemplate.hashCode();
    }

    /**
     * This method returns a String representation of the Modification.
      *
     * @return  String with a String Representation of the Modification.
     */
    public String toString() {
        return this.iTemplate.toString() + "\n\tLocation: : " + this.iLocation;
    }

    /**
     * Override of the clone method. It doesn't do anything except making the method public
     * and catching the 'CloneNotSupportedException'. The method now returns a 'null' when cloning was not
     * possible.
     *
     * @return  Object  with a clone of this class, or 'null' when the CloneNotSupportedException was thrown.
     */
    public Object clone () {
        Object clone = null;
        try {
            clone = super.clone();
        } catch(CloneNotSupportedException cnse) {
            logger.error(cnse.getMessage(), cnse);
        }
        return clone;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * In this implementation, ordering is first done on location, and only if this is
     * identical, the title is compared. Therefore, since equals uses title and location as well,
     * two equals instances will compare to '0'.
     *
     * @param   o the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this Object.
     */
    public int compareTo(Object o) {
        // Cast first.
        ModificationImplementation mi = (ModificationImplementation)o;
        // Compare the locations.
        int result = this.iLocation-mi.iLocation;
        if(result == 0) {
            result = this.iTemplate.compareTo(mi.iTemplate);
        }
        return result;
    }

    /**
     * This method returns the mass delta for the specified residue, measured either
     * monoisotopically or averaged.
     *
     * @param aResidue  String  with the residue for which the modification applies (value for the N-terminus is
     *                          the NTERMINUS constant and for the C-terminus the CTERMINUS constant).
     * @param aMonoOrAvg    int which should be either MONOISOTOPIC or AVERAGE
     * @return  double  with the mass delta (can be negative, of course!) or '0.0' if this modification
     *                  does not apply to the specified residue.
     */
    protected double getMassDelta(String aResidue, int aMonoOrAvg) {
        return this.iTemplate.getMassDelta(aResidue, aMonoOrAvg);
    }
}
