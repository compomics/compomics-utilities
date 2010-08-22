/*
 * Copyright (C) Lennart Martens
 *
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.protein;
import com.compomics.util.general.IsotopicDistribution;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.util.*;
import java.math.BigDecimal;

import com.compomics.util.interfaces.Sequence;
import com.compomics.util.interfaces.Modification;
import com.compomics.util.general.MassCalc;
import com.compomics.util.general.UnknownElementMassException;


/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class represents a sequence for a protein or peptide. <br />
 *
 * @see com.compomics.util.interfaces.Sequence
 * @author	Lennart Martens
 */
public class AASequenceImpl implements Sequence {

    // Class specific log4j logger for AASequenceImpl instances.
    Logger logger = Logger.getLogger(AASequenceImpl.class);

    /**
     * The Kyte & Doolittle score for AA residus.
     */
    private static Properties iKyte_Doolittle = null;

    /**
     * This variable holds the GRAVY (Kyte & Doolittle) score
     * for the peptide. <br />
     * It uses lazy caching!
     */
    private double iGravy = 0.0;

    /**
     * This boolean aids in the caching of the GRAVY value.
     */
    private boolean iGravyCached = false;

    /**
     * The Meek HPLC retention score for AA residus.
     */
    private static Properties iMeekList = null;

    /**
     * This variable holds the HPLC retention time (Meek) score
     * for the peptide. <br />
     * It uses lazy caching!
     */
    private double iMeek = 0.0;

    /**
     * This boolean aids in the caching of the Meek value.
     */
    private boolean iMeekCached = false;

    /**
     * This varible holds the String that represents the
     * sequence.
     */
    private String iSequence = null;

    /**
     * This Vector holds all the modifications currently on the
     * sequence.
     */
    private Vector iModifications = null;

    /**
     * This variable holds the mass for the sequence. <br />
     * It uses lazy caching!
     */
    private double iMass = -1;

    /**
     * Constant for internal use.
     */
    private static final int GRAVY = 0;

    /**
     * Constant for internal use.
     */
    private static final int MEEK = 1;

    /**
     * Constructor that allows the initialization of the sequence
     * with a String variable representing that sequence.
     *
     * @param	aSequence	String with the sequence.
     */
    public AASequenceImpl(String aSequence) {
        this(aSequence, null);
    }

    /**
     * Constructor that allows the initialization of the sequence
     * with a String variable representing that sequence and a
     * Vector of Modification instances.
     *
     * @param	aSequence	String with the sequence.
     * @param	aMods	Vector with Modification instances.
     */
    public AASequenceImpl(String aSequence, Vector aMods) {
        this.setSequence(aSequence);
        this.setModifications(aMods);
    }

    /**
     * Default constructir is private. Used in static creation methods.
     */
    private AASequenceImpl() {};

    /**
     * Simple setter for the sequence. It also clears the mass cache.
     *
     * @param	aSequence	String with the sequence to be set.
     */
    public void setSequence(String aSequence) {
        // Nullpointer check.
        if (aSequence == null) {
            throw new NullPointerException("Sequence cannot be 'null'!\n");
        } else if (aSequence.trim().equals("")) {
            throw new IllegalArgumentException("Sequence cannot be empty String!\n");
        }
        this.iSequence = aSequence.trim();
        this.iMass = -1.0;
        this.iGravyCached = false;
        this.iMeekCached = false;
    }

    /**
     * Simple getter for the sequence.
     *
     * @return	String	the sequence.
     */
    public String getSequence() {
        return this.iSequence;
    }

    /**
     * Simple setter for the modifications.
     *
     * @param	aMods	Vector with the modifications.
     */
    public void setModifications(Vector aMods) {
        this.iModifications = aMods;
    }

    /**
     * Simple getter for the modifications.
     *
     * @return	Vector	with the modifications.
     */
    public Vector getModifications() {
        return this.iModifications;
    }

    /**
     * This method calculates the mass over charge ratio for a given charge
     * for the current sequence. Returns -1, if the mass could not be calculated.
     *
     * @param   charge the charge to use for the m/z ratio calculation
     * @return	double with the m/z ratio for this sequence with the given charge,
     *          -1 if the mass could not be calculated.
     */
    public double getMz(int charge) {

        double tempMz = -1;

        try {
            // calculate the m/z ratio
            tempMz = (getMass() + (((double) charge) * new MassCalc().calculateMass("H"))) / charge;
        } catch (UnknownElementMassException ume) {
            logger.error(ume.getMessage(), ume);
        }

        return tempMz;
    }

    /**
     * This method calculates the mass for the current sequence. <br />
     * Mass cached lazily, so after the first calculation it comes from
     * memory.
     *
     * @return	double	with the mass for this sequence.
     */
    public double getMass() {
        if (iMass < 0.0) {
            // We need to calculate the mass since it is not cached.
            try {
                MassCalc temp = new MassCalc(MassCalc.MONOAA);
                this.iMass = temp.calculateMass(iSequence);
                // Okay, we now have the mass as is.
                // Next up: apply all changes to the mass as indicated in
                // the Modifications we have (if any).
                if (iModifications != null) {
                    int liSize = iModifications.size();
                    for (int i = 0; i < liSize; i++) {
                        ModificationImplementation m = (ModificationImplementation) iModifications.get(i);
                        int location = m.getLocation();
                        double delta = 0.0;
                        if (location == 0) {
                            delta = m.getMonoisotopicMassDelta(ModificationImplementation.NTERMINUS);
                        } else if (location > iSequence.length()) {
                            delta = m.getMonoisotopicMassDelta(ModificationImplementation.CTERMINUS);
                        } else {
                            String loc = iSequence.substring(location - 1, location);
                            delta = m.getMonoisotopicMassDelta(loc) - (temp.calculateMass(loc) - 18.010565);
                        }
                        this.iMass += delta;
                    }
                }
            } catch (UnknownElementMassException ume) {
                logger.error(ume.getMessage(), ume);
            }
        }
        return this.iMass;
    }

    /**
     * This method allows the construction of an AASequenceImpl object, complete with modifications
     * from an annotated sequence String (eg., something like: 'NH2-YS<P>FVATER-COOH' or 'Ace-MATHM<Mox>PIR-COOH').
     *
     * @param aAnnotatedSequence    String with the annotated sequence (eg., something like:
     *                              'NH2-YS<P>FVATER-COOH' or 'Ace-MATHM<Mox>PIR-COOH')
     * @return  AASequenceImpl with the sequence and annotated modifications.
     */
    public static AASequenceImpl parsePeptideFromAnnotatedSequence(String aAnnotatedSequence) {
        AASequenceImpl p = new AASequenceImpl();
        p.parseSequenceAndModificationsFromString(aAnnotatedSequence);

        // C'est fini!
        return p;
    }

    /**
     * This method is designed to load a sequence and it's set of modifications from a String
     * which holds the sequence, annotated with all the modifications applied to it. <br />
     * Typically, the String parsed should be derived from the 'String getModifiedSequence()'
     * method (see documentation there).
     *
     * @see com.compomics.util.interfaces.Modification
     * @param aStringWithModificiations String with annotated modifications.
     */
    protected void parseSequenceAndModificationsFromString(String aStringWithModificiations) {
        // First isolate the N-terminal and C-terminal part of the sequence.
        // Structure is '[nterm] - [sequence_with_mods] - [cterm]' structure.
        int startSequence = aStringWithModificiations.indexOf("-");
        int endSequence = aStringWithModificiations.lastIndexOf("-");

        // Isolate the three parts.
        String nterm = aStringWithModificiations.substring(0, startSequence).trim();
        String cterm = aStringWithModificiations.substring(endSequence + 1).trim();
        String sequence = aStringWithModificiations.substring(startSequence + 1, endSequence).trim();

        ArrayList modifications = new ArrayList();
        // Parse the N-terminal modification.
        // Oh yeah, we only parse when there is a modification ('NH2' means no modification).
        if (!nterm.equals("NH2")) {
            // See if the factory knows about this one!
            Modification ntermMod = ModificationFactory.getModification(nterm, Modification.NTERMINUS, 0);
            if (ntermMod == null) {
                // Auwch...
                throw new IllegalArgumentException("N-terminal modification code '" + nterm + "' was not recognized for the N-terminus by the ModificationFactory!");
            }
            modifications.add(ntermMod);
        }
        // Parse the C-terminal modification.
        // Oh yeah, we only parse when there is a modification ('COOH' means no modification).
        if (!cterm.equals("COOH")) {
            // In this case we can only set the modification location later, since we do not know the sequence length yet!!
            Modification ctermMod = ModificationFactory.getModification(cterm, Modification.CTERMINUS, -1);
            if (ctermMod == null) {
                throw new IllegalArgumentException("C-terminal modification code '" + cterm + "' was not recognized for the C-terminus by the ModificationFactory!");
            }
            modifications.add(ctermMod);
        }
        // Now cycle the sequence itself. Modifications are flagged by the presence of '<>' around the code.
        int start = -1;
        // Create a StringBuffer to both found and excise the modifications,
        // as well as having a cleaned-up String at the end.
        StringBuffer sequenceRoller = new StringBuffer(sequence);
        while ((start = sequenceRoller.indexOf("<")) >= 0) {
            // Find the end of the modification.
            int end = sequenceRoller.indexOf(">");
            // See if it al makes sense.
            if (end <= start) {
                throw new RuntimeException("Parsing failed miserably! Found a closing '>' (at " + end + ") BEFORE the opening '<' (at " + start + ") while attempting to parse modifications from: '" + sequenceRoller + "' (originally: '" + sequence + "')!");
            }
            // We've got a modification code now, let's also get the residue it applies to (because the
            // ModificationFactory requires it)!
            String modificationCode = sequenceRoller.substring(start + 1, end);
            // Check whether we actually have a previous index (if we don't, flag en error!!).
            if (start == 0) {
                throw new RuntimeException("First modification ('" + modificationCode + "') in the sequence was found at index O!");
            }
            // Okay, there should be something; garb it!
            String residue = sequenceRoller.substring(start - 1, start);
            // The location must not be forgotten! Fortunately, the location is the 'start-1' index (+1 because '0' is the N-terminus!!!)
            // since we've deleted all previous modifications (so the String 'to the left' of the '<xxx>' is pure sequence).
            Modification mod = ModificationFactory.getModification(modificationCode, residue, start);
            // Check whether we identified and obtained a modification.
            if (mod == null) {
                throw new IllegalArgumentException("Modification code '" + modificationCode + "' was not recognized for residue '" + residue + "' by the ModificationFactory!");
            }
            // Add this modification to the list.
            modifications.add(mod);
            // To close the loop, delete this modification from the StringBuffer.
            sequenceRoller.delete(start, end + 1);
        }
        // Now we should have a set of modifications for this class,
        // all that remains is checking and then initializing the sequence proper.
        if (sequenceRoller.indexOf(">") < 0 && sequenceRoller.indexOf("<") < 0) {
            // No more modifications present. That's good.
            this.iSequence = sequenceRoller.toString();
            // Add the mods as well.
            Iterator it = modifications.iterator();
            while (it.hasNext()) {
                // Remember that we had to wait with the location setting of the C-terminal modification (if any)
                // until we knew the sequence length?
                // If we encounter it here, set it; now we can!
                Modification lModification = (Modification) it.next();
                if (lModification.getLocation() == -1) {
                    lModification.setLocation(iSequence.length() + 1);
                }
                this.addModification(lModification);
            }
        } else {
            throw new IllegalArgumentException("Remaining '<' or '>' in the sequence '" + sequenceRoller + "', hinting at unbalanced modification brackets!");
        }
    }

    /**
     * This method reports on the length of the current sequence.
     *
     * @return  int with the length of the sequence.
     */
    public int getLength() {
        return this.iSequence.length();
    }

    /**
     * This method gets the GRAVY score (Kyte&Doolittle) from
     * the cache, or, if it isn't cached, reconstructs it.
     *
     * @return	double	with the GRAVY coefficient.
     */
    public double getGravy() {
        if (iGravyCached) {
            // Cached. Do nothing.
        } else {
            iGravy = this.calculateScore(GRAVY);
            iGravyCached = true;
        }

        return iGravy;
    }

    /**
     * This method will return an estimated 'net' HPLC retention
     * time for the sequence based on the table by Meek.<br />
     * It does NOT take a t0 value, specific to a setup, into account.
     *
     * @return	double	with the 'net' HPLC retention time as calculated
     *					from Meek's table.
     */
    public double getMeek() {
        if (iMeekCached) {
            // Cached, do nothing.
        } else {
            iMeek = this.calculateScore(MEEK);
            iMeekCached = true;
        }

        return iMeek;
    }

    /**
     * This method will return the sequence with annotated modifications.
     * For this annotation the key of the modifications will be used.
     * If the key is a formula, it will be enclosed in '<>' as well.
     *
     * @return	String	with the annotated sequence (i.e.: containing the
     *					modifications.
     */
    public String getModifiedSequence() {
        String result = null;

        /// First check if any modifications are present.
        if (iModifications == null) {
            result = "NH2-" + this.getSequence() + "-COOH";
        } else {

            StringBuffer tempSeq = new StringBuffer("");

            // Cycle the sequence, check for mods for each location in the
            // sequence. A Vector[] will hold the mods for each position
            // once we're done.
            int liSeqLength = iSequence.length();
            Vector[] mods = new Vector[liSeqLength + 2];
            for (int i = 0; i < liSeqLength + 2; i++) {
                mods[i] = new Vector(2, 2);
            }
            int liSize = iModifications.size();
            for (int i = 0; i < liSize; i++) {
                ModificationImplementation m = (ModificationImplementation) iModifications.elementAt(i);
                int loc = m.getLocation();
                mods[loc].add(m);
            }

            // Now, we've got an array of Vectors, each holding the mods for
            // each location in the sequence (including '0' and 'length+1' for
            // the N-terminus and C-terminus, respectively).
            // We'll cycle the sequence, add each character to the StringBuffer
            // and append to that character all mods (if any).
            // Nterm first.
            if (mods[0].size() > 0) {
                int liTemp = mods[0].size();
                Collections.sort(mods[0]);
                for (int i = 0; i < liTemp; i++) {
                    ModificationImplementation tempMod = (ModificationImplementation) mods[0].get(i);
                    tempSeq.append(tempMod.getCode());
                }
            } else {
                tempSeq.append("NH2");
            }
            tempSeq.append("-");
            // 'Real' sequence.
            for (int i = 0; i < liSeqLength; i++) {
                tempSeq.append(iSequence.charAt(i));
                int liTemp = mods[i + 1].size();
                if (liTemp > 0) {
                    Collections.sort(mods[i + 1]);
                    for (int j = 0; j < liTemp; j++) {
                        ModificationImplementation tempMod = (ModificationImplementation) mods[i + 1].get(j);
                        tempSeq.append("<" + tempMod.getCode() + ">");
                    }
                }
            }
            tempSeq.append("-");
            //C-term last.
            if (mods[liSeqLength + 1].size() > 0) {
                int liTemp = mods[liSeqLength + 1].size();
                Collections.sort(mods[liSeqLength + 1]);
                for (int i = 0; i < liTemp; i++) {
                    ModificationImplementation tempMod = (ModificationImplementation) mods[liSeqLength + 1].get(i);
                    tempSeq.append(tempMod.getCode());
                }
            } else {
                tempSeq.append("COOH");
            }

            result = tempSeq.toString();
        }

        // Voila.
        return result;
    }

    /**
     * This method will return an AASequenceImpl that represents
     * an N-terminal  truncation of the current sequence. <br />
     * Note that the applicable modifications (those within the truncation size)
     * are also represented in the truncated sequence!
     *
     * @param   aTruncationSize int with the amount of N-terminal residus the
     *                          truncated sequence should have.
     * @return  AASEquenceImpl  with the N-terminal truncated sequence (including modifications).
     */
    public AASequenceImpl getNTermTruncatedSequence(int aTruncationSize) {
        AASequenceImpl result = null;

        if (aTruncationSize >= this.getLength()) {
            result = new AASequenceImpl(this.iSequence);
        } else {
            result = new AASequenceImpl(this.iSequence.substring(0, aTruncationSize));

            // See if there are modifications, and if so, handle them.
            if (iModifications != null) {
                int liSize = iModifications.size();
                Vector mods = new Vector(10, 5);
                for (int i = 0; i < liSize; i++) {
                    // If the modification applies to a position that falls
                    // within the new truncated size, take it with us.
                    // Else, leave it be.
                    ModificationImplementation m = (ModificationImplementation) iModifications.get(i);
                    if (m.getLocation() <= aTruncationSize) {
                        mods.add(m);
                    }
                }
                if (mods.size() > 0) {
                    result.setModifications(mods);
                }
            }
        }

        return result;
    }

    /**
     * This method will return an AASequenceImpl that represents
     * a C-terminal truncation of the current sequence. <br />
     * Note that the applicable modifications (those within the truncation size)
     * are also represented in the truncated sequence!
     *
     * @param   aTruncationSize int with the amount of C-terminal residus the
     *                          truncated sequence should have.
     * @return  AASEquenceImpl  with the C-terminal truncated sequence (including modifications).
     */
    public AASequenceImpl getCTermTruncatedSequence(int aTruncationSize) {
        AASequenceImpl result = null;

        if (aTruncationSize >= this.getLength()) {
            result = new AASequenceImpl(this.iSequence);
        } else {
            result = new AASequenceImpl(this.iSequence.substring(this.iSequence.length() - aTruncationSize, this.iSequence.length()));

            // See if there are modifications, and if so, handle them.
            if (iModifications != null) {
                int liSize = iModifications.size();
                Vector mods = new Vector(10, 5);
                for (int i = 0; i < liSize; i++) {
                    // If the modification applies to a position that falls
                    // within the new truncated size, take it with us.
                    // Else, leave it be.
                    ModificationImplementation m = (ModificationImplementation) iModifications.get(i);
                    if (m.getLocation() >= (this.iSequence.length() - aTruncationSize)) {
                        // Recalculate the correct location for the mdoification.
                        Modification m2 = (Modification) m.clone();
                        m2.setLocation(m.getLocation() - (this.iSequence.length() - aTruncationSize));
                        mods.add(m2);
                    }
                }
                if (mods.size() > 0) {
                    result.setModifications(mods);
                }
            }
        }

        return result;
    }

    /**
     * This method will return an AASequenceImpl that represents
     * an internal truncation of the current sequence. <br />
     * Note that the applicable modifications (those within the truncation size)
     * are also represented in the resulting truncated sequence!
     *
     * @param   aStart int with the start (N-terminal) residu for the
     *                          truncation. The first residu is number '1'.
     * @param   aEnd    int with the end residu (C-terminal; NOT included) for the truncation.
     * @return  AASEquenceImpl  with the C-terminal truncated sequence (including modifications).
     */
    public AASequenceImpl getTruncatedSequence(int aStart, int aEnd) {
        AASequenceImpl result = null;

        if (aStart <= 0 && aEnd >= this.getLength()) {
            result = new AASequenceImpl(this.iSequence);
        } else {
            result = new AASequenceImpl(this.iSequence.substring(aStart - 1, aEnd - 1));

            // See if there are modifications, and if so, handle them.
            if (iModifications != null) {
                int liSize = iModifications.size();
                Vector mods = new Vector(10, 5);
                for (int i = 0; i < liSize; i++) {
                    // If the modification applies to a position that falls
                    // within the new truncated size, take it with us.
                    // Else, leave it be.
                    ModificationImplementation m = (ModificationImplementation) iModifications.get(i);
                    if (m.getLocation() >= aStart && m.getLocation() < aEnd) {
                        Modification m2 = (Modification) m.clone();
                        m2.setLocation(m.getLocation() - (aStart - 1));
                        mods.add(m2);
                    } else if (m.getLocation() == 0 && aStart <= 1) {
                        // Add the N-term modification.
                        mods.add(m);
                    } else if (m.getLocation() == this.iSequence.length() + 1 && aEnd >= this.iSequence.length() + 1) {
                        // Add the C-term modification.
                        Modification m2 = (Modification) m.clone();
                        m2.setLocation(m.getLocation() - (aStart - 1));
                        mods.add(m2);
                    }
                }
                if (mods.size() > 0) {
                    result.setModifications(mods);
                }
            }
        }

        return result;
    }

    /**
     * This method reports whether a certain residu (or fixed sequence
     * String) is found in the current sequence.
     *
     * @param   aSequence   with the residu (or fixed sequence of residus)
     *                      to find in the current sequence.
     * @return  boolean     that indicates whether the sequence contains
     *                      the indicated residu (or sequence).
     */
    public boolean contains(String aSequence) {
        boolean result = false;

        if (this.iSequence.indexOf(aSequence) >= 0) {
            result = true;
        }

        return result;
    }

    /**
     * This method adds a modification to the list of modifications.
     *
     * @param aModification Modification instance to add to the modifications list.
     */
    public void addModification(Modification aModification) {
        if (this.iModifications == null) {
            iModifications = new Vector(this.iSequence.length() + 2, 2);
        }
        this.iModifications.add(aModification);
        // Undo the cache.
        this.iMass = -1.0;
    }

    /**
     * Calculation consists of adding all the coefficients for the
     * recognized AA, and then dividing the result by the number of
     * additions done (== the number of AA recognized == the number
     * of AA for which we have a score). This is a simple mean, btw.
     *
     * @param	aList	int with the list to use (use only final
     *					member vars of this class!).
     * @return	double	with the score for the sequence.
     */
    private double calculateScore(int aList) {
        double temp = 0.0;
        Properties tempList = new Properties();
        switch (aList) {
            case GRAVY:
                if (iKyte_Doolittle == null) {
                    iKyte_Doolittle = this.loadProps("kyte_doolittle.properties");
                }
                tempList = iKyte_Doolittle;
                break;
            case MEEK:
                if (iMeekList == null) {
                    iMeekList = this.loadProps("meek.properties");
                }
                tempList = iMeekList;
                break;
        }

        // Cycle the sequence, add each known AA and
        // divide by the number of additions.
        int additions = 0;
        for (int i = 0; i < iSequence.length(); i++) {
            String key = new Character(iSequence.charAt(i)).toString();
            double index = 0.0;
            if (tempList.containsKey(key)) {
                index = Double.parseDouble(tempList.getProperty(key));
                additions++;
            }
            temp += index;
        }

        // Division step.
        if (additions == 0) {
            additions++;
        }
        temp /= additions;

        BigDecimal bd = new BigDecimal(temp);
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_EVEN);

        return bd.doubleValue();
    }

    /**
     * This method calculates the molecular formula based on the sequence
     * @return MolecularFormula
     */
    public MolecularFormula getMolecularFormula(){
        MolecularFormula lResult = new MolecularFormula(this);
        return lResult;
    }

    /**
     * This method gives the IsotopicDistribution for the sequence
     * @return IsotopicDistribution
     */
     public IsotopicDistribution getIsotopicDistribution(){
         MolecularFormula lForm = getMolecularFormula();
         IsotopicDistribution lCalc = new IsotopicDistribution(lForm);
         return lCalc;
     }


	/**
     * This method loads a Properties instance from the classpath.
     * It returns an empty instance and displays an error message
     * if the Properties instance was not found.
     *
     * @param	aPropFileName	String with the filename for the
     *							properties file.
     * @return	Properties	with the props from the file, or an empty
     *						instance if the file was not found.
     */
    private Properties loadProps(String aPropFileName) {
        Properties p = new Properties();
        try {
            p.load(this.getClass().getClassLoader().getResourceAsStream(aPropFileName));
        } catch (IOException ioe) {
            logger.error("\nProperties file (" + aPropFileName + ") not found in classpath!");
            logger.error("All resultant values will be computed to 0.0!!\n");
        }
        return p;
    }
}
