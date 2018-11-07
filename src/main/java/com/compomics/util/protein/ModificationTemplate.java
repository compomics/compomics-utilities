/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 1-jul-2004
 * Time: 15:03:58
 */
package com.compomics.util.protein;
import org.apache.log4j.Logger;


import java.util.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class holds a template for a Modification, meaning that it contains all shared characteristics of
 * a Modification, but not the instance-specific one. As such, instances can be treated as singletons and are
 * obviously immutable by nature.
 *
 * @author Lennart Martens
 */
public class ModificationTemplate {

    /**
     * Empty default constructor
     */
    public ModificationTemplate() {
    }

    // Class specific log4j logger for ModificationTemplate instances.
    Logger logger = Logger.getLogger(ModificationTemplate.class);

    /**
     * This HashMap will contain the following 'key-value' mappings: (key &gt; value) <br>
     * (residue &gt; double[]{MONOISOTOPIC_DELTA, AVERAGE_DELTA})
     */
    protected HashMap iMassDeltas = null;

    /**
     * The code for this modification (eg., Mox).
     */
    protected String iCode = null;

    /**
     * The title for this modification (eg., Oxidation Met).
     */
    protected String iTitle = null;

    /**
     * Boolean to indicate whether this modification is an artifact.
     */
    protected boolean iArtifact = false;

    /**
     * This constructor allows initialization of all the properties for the modification template.
     *
     * @param aTitle    String with the title for the modification (eg., Oxidation Met).
     * @param aCode String with the code for the modification (eg., Mox). The code can be used when
     *              annotating a sequence String (eg., NH2-MGTEFSM&lt;Mox&gt;R-COOH).
     * @param aMassDeltas   HashMap with the following 'key-value' mappings: (key &gt; value) <br>
     *                      (residue &gt; double[]{MONOISOTOPIC_DELTA, AVERAGE_DELTA} <br>
     *                      Note that the residues for the N-terminus and C-terminus are represented
     *                      by the NTERMINUS and CTERMINUS constants, respectively.
     */
    public ModificationTemplate(String aTitle, String aCode, HashMap aMassDeltas) {
        this(aTitle, aCode, aMassDeltas, false);
    }

    /**
     * This constructor allows initialization of all the properties for the modification template.
     *
     * @param aTitle    String with the title for the modification (eg., Oxidation Met).
     * @param aCode String with the code for the modification (eg., Mox). The code can be used when
     *              annotating a sequence String (eg., NH2-MGTEFSM&lt;Mox&gt;R-COOH).
     * @param aMassDeltas   HashMap with the following 'key-value' mappings: (key &gt; value) <br>
     *                      (residue &gt; double[]{MONOISOTOPIC_DELTA, AVERAGE_DELTA} <br>
     *                      Note that the residues for the N-terminus and C-terminus are represented
     *                      by the NTERMINUS and CTERMINUS constants, respectively.
     * @param aArtifact boolean to indicate whether this class is an artifact.
     */
    public ModificationTemplate(String aTitle, String aCode, HashMap aMassDeltas, boolean aArtifact) {
        this.iTitle = aTitle;
        this.iCode = aCode;
        // Check the structure of the HashMap.
        // If it doesn't conform to 'String, double[] (length 2; more is permitted however)' key/value pairs,
        // throw an appropriate IllegalArumentException.
        Iterator it = aMassDeltas.keySet().iterator();
        while(it.hasNext()) {
            // By default, assume it doesn't pass; let it prove itself.
            boolean pass = false;
            Object key = it.next();
            // Check the key to see whether it is a String.
            if(key instanceof String) {
                // Okay, key is correct, now the value.
                Object value = aMassDeltas.get(key);
                if(value instanceof double[]) {
                    if(((double[])value).length >= 2) {
                        // This element in the HashMap passes the test.
                        pass = true;
                    }
                }
            }
            // If the element didn't pass the test, we reject the entire HashMap.
            if(!pass) {
                throw new IllegalArgumentException("Your HashMap did not conform to the required (String, double[] (length 2)) conformation!");
            }
        }
        this.iMassDeltas = aMassDeltas;
        this.iArtifact = aArtifact;
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
        return this.getMassDelta(aResidue, com.compomics.util.interfaces.Modification.AVERAGE);
    }

    /**
     * This method returns the short code for the modification, eg. 'Mox'.
     *
     * @return  String  with the short code for the modification.
     *                  Can be used to annotate a sequence.
     */
    public String getCode() {
        return this.iCode;
    }

    /**
     * This method returns the title of the modification,
     * eg. 'Oxidation Met'.
     *
     * @return  String with the title for the modification.
     */
    public String getTitle() {
        return this.iTitle;
    }

    /**
     * This method reports on all the residues that can be modified by this Modification. <br>
     * The Collection is a keySet of a HashMap.
     *
     * @return  Collection with the residues that can be modified by this modification.
     */
    public Collection getResidues() {
        return this.iMassDeltas.keySet();
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
        return this.getMassDelta(aResidue, com.compomics.util.interfaces.Modification.MONOISOTOPIC);
    }

    /**
     * This method indicates whether this modification  is considered an artifact.
     *
     * @return  boolean that indicates whether this modification is an artifact.
     */
    public boolean isArtifact() {
        return this.iArtifact;
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
     * @see     java.util.Hashtable
     */
    public boolean equals(Object obj) {
        boolean result = true;
        // Class equality (instanceof is too lenient as it will vouch for subclasses as well!)
        if(obj == null || !obj.getClass().equals(this.getClass())) {
            result = false;
        } else if(!((ModificationTemplate)obj).iTitle.equals(this.iTitle)) {
            // Equality is further defined by title.
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
     *     an execution of a Java application, the <tt>hashCode</tt> method
     *     must consistently return the same integer, provided no information
     *     used in <tt>equals</tt> comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the <tt>equals(Object)</tt>
     *     method, then calling the <code>hashCode</code> method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link Object#equals(Object)}
     *     method, then calling the <tt>hashCode</tt> method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hashtables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by
     * class <tt>Object</tt> does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the Java programming language.)
     *
     * @return  a hash code value for this object.
     * @see     Object#equals(Object)
     * @see     java.util.Hashtable
     */
    public int hashCode() {
        return iTitle.hashCode();
    }

    /**
     * This method returns a String representation of the Modification.
      *
     * @return  String with a String Representation of the Modification.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Modification: '" + this.iTitle + "':");
        if(this.iArtifact) {
            sb.append("\n\tHidden");
        }
        sb.append("\n\tCode: " + this.iCode);
        Set keySet = iMassDeltas.keySet();
        String[] residues = new String[keySet.size()];
        keySet.toArray(residues);
        Arrays.sort(residues);
        for(int i = 0; i < residues.length; i++) {
            String lResidue = residues[i];
            double[] deltas = (double[])iMassDeltas.get(lResidue);
            if(lResidue.equals(com.compomics.util.interfaces.Modification.NTERMINUS)) {
                lResidue = "Nterm";
            } else if(lResidue.equals(com.compomics.util.interfaces.Modification.CTERMINUS)) {
                lResidue = "Cterm";
            }
            sb.append("\n\tResidue: " + lResidue + " " + deltas[com.compomics.util.interfaces.Modification.MONOISOTOPIC]
                    + " Da [MONOISOTOPIC] " + deltas[com.compomics.util.interfaces.Modification.AVERAGE] + " Da [AVERAGE]");
        }

        return sb.toString();
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
        ModificationTemplate mi = (ModificationTemplate)o;
        // Compare the locations.
        int result = this.iTitle.compareTo(mi.iTitle);

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
        // Check whether the int index is 0 or 1 (MONOISTOPIC or AVERAGE).
        if(aMonoOrAvg < com.compomics.util.interfaces.Modification.MONOISOTOPIC || aMonoOrAvg > com.compomics.util.interfaces.Modification.AVERAGE) {
            throw new IllegalArgumentException("The index in the double[] you specified (" + aMonoOrAvg + ") is outside the range of MONOISOTOPIC-AVERAGE (0-1)!!");
        }
        double delta = 0.0;
        Object temp = iMassDeltas.get(aResidue);
        // Only fill out the delta if we find something.
        if(temp != null && temp instanceof double[]) {
            double[] deltas = (double[])temp;
            delta = deltas[aMonoOrAvg];
        }

        return delta;
    }
}
