/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 8-okt-02
 * Time: 18:38:53
 */
package com.compomics.util.protein;

import java.util.ArrayList;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2008/11/18 11:39:11 $
 */
/**
 * This class implements the functionality for an Enzyme.
 *
 * @author Lennart Martens
 * @author Harald Barsnes
 * @author MArc Vaudel
 */
public class Enzyme implements Cloneable {

    // Class specific log4j logger for Enzyme instances.
    Logger logger = Logger.getLogger(Enzyme.class);
    public static final int CTERM = 0; // @TODO: should be replaced by Emnum
    public static final int NTERM = 1; // @TODO: should be replaced by Emnum
    public static final int FULLY_ENZYMATIC = 1; // @TODO: should be replaced by Emnum
    public static final int N_TERM_ENZYMATIC = 2; // @TODO: should be replaced by Emnum
    public static final int C_TERM_ENZYMATIC = 3; // @TODO: should be replaced by Emnum
    public static final int ENTIRELY_NOT_ENZYMATIC = 4; // @TODO: should be replaced by Emnum
    /**
     * This String holds the title (or name) for the enzyme.
     */
    protected String iTitle = null;
    /**
     * This char[] holds the residues after which cleavage will occur.
     */
    protected char[] iCleavage = null;
    /**
     * Lookup cache for the cleavable residues.
     */
    protected HashMap iCleavables = null;
    /**
     * This char[] holds the residues that will restrict cleavage when present
     * after a cleavable residue.
     */
    protected char[] iRestrict = null;
    /**
     * Lookup cache for the restricting residues.
     */
    protected HashMap iRestrictors = null;
    /**
     * This integer holds the position marker for the cleavage direction for
     * this Enzyme. This variable can be matched against the constants defined
     * on this class.
     */
    protected int iPosition = -1;
    /**
     * This variable holds the number of supported missed cleavages.
     */
    protected int iMiscleavages = 0;

    /**
     * This constructor requires that you specify all the information for this
     * enzyme. Title and restrict can be 'null', and the number of miscleavages
     * is defaulted to 1.
     *
     * @param aTitle String with the title (or name) for this enzyme.
     * @param aCleavage String composed of the residues after which cleavage
     * will occur.
     * @param aRestrict String composed of the residues which inhibit cleavage
     * if present behind of cleavable residues.
     * @param aPosition String which should correspond to "Cterm" or "Nterm" for
     * each position respectively.
     */
    public Enzyme(String aTitle, String aCleavage, String aRestrict, String aPosition) {
        this(aTitle, aCleavage, aRestrict, aPosition, 1);
    }

    /**
     * This constructor allows you to specify all the information for this
     * enzyme plus the number of missed cleavages that this instance will allow.
     * Title and restrict can be 'null'.
     *
     * @param aTitle String with the title (or name) for this enzyme.
     * @param aCleavage String composed of the residues after which cleavage
     * will occur (this String will be uppercased).
     * @param aRestrict String composed of the residues which inhibit cleavage if
     * present behind of cleavable residues (this String will be uppercased).
     * @param aPosition String which should correspond to "Cterm" or "Nterm" for
     * each position respectively.
     * @param aMiscleavages integer with the number of supported missed cleavages.
     */
    public Enzyme(String aTitle, String aCleavage, String aRestrict, String aPosition, int aMiscleavages) {
        iTitle = aTitle;
        this.setCleavage(aCleavage);
        this.setRestrict(aRestrict);
        aPosition = aPosition.trim();
        if (aPosition.equalsIgnoreCase("Cterm")) {
            iPosition = CTERM;
        } else if (aPosition.equalsIgnoreCase("Nterm")) {
            iPosition = NTERM;
        } else {
            throw new IllegalArgumentException("I only understand the positions 'Nterm' or 'Cterm'! You passed: '" + aPosition + "'.");
        }
        iMiscleavages = aMiscleavages;
    }

    /**
     * Creates a new Enzyme from a com.compomics.util.experiment.biology.Enzyme
     * enzyme and the maximum number of missed cleavages.
     *
     * @param enzyme The com.compomics.util.experiment.biology.Enzyme enzyme
     * @param maxMissedCleavages The maximum number of missed cleavages
     */
    public Enzyme(com.compomics.util.experiment.biology.enzymes.Enzyme enzyme, int maxMissedCleavages) {

        String position = "", cleavage = "", restrict = "";

        if (enzyme.getAminoAcidBefore().size() > 0) {
            position = "Cterm";

            HashSet<Character> temp = enzyme.getAminoAcidBefore();

            for (Character aa : temp) {
                cleavage += aa;
            }

            temp = enzyme.getRestrictionAfter();

            for (Character aa : temp) {
                restrict += aa;
            }
        } else {
            position = "Nterm";

            HashSet<Character> temp = enzyme.getAminoAcidAfter();

            for (Character aa : temp) {
                cleavage += aa;
            }

            temp = enzyme.getRestrictionAfter();

            for (Character aa : temp) {
                restrict += aa;
            }
        }

        iTitle = enzyme.getName();
        this.setCleavage(cleavage);
        this.setRestrict(restrict);
        position = position.trim();

        if (position.equalsIgnoreCase("Cterm")) {
            iPosition = CTERM;
        } else if (position.equalsIgnoreCase("Nterm")) {
            iPosition = NTERM;
        } else {
            throw new IllegalArgumentException("I only understand the positions 'Nterm' or 'Cterm'! You passed: '" + position + "'.");
        }

        iMiscleavages = maxMissedCleavages;
    }

    /**
     * Simple getter for the title (name) of the Enzyme.
     *
     * @return String with the title (name).
     */
    public String getTitle() {
        return iTitle;
    }

    /**
     * This method allows the caller to change the title (name) of the Enzyme.
     *
     * @param aTitle String with the title (name) for the Enzyme.
     */
    public void setTitle(String aTitle) {
        iTitle = aTitle;
    }

    /**
     * Simple getter for the cleavagable residues of the Enzyme.
     *
     * @return char[] with the cleavable residues.
     */
    public char[] getCleavage() {
        return iCleavage;
    }

    /**
     * This method allows the caller to specify the cleavable residues.
     *
     * @param aCleavage char[] with the cleavable residues (in <b>UPPER
     * CASE</b>!).
     */
    public void setCleavage(char[] aCleavage) {
        iCleavage = aCleavage;
        if (iCleavage != null) {
            iCleavables = new HashMap(this.iCleavage.length);
            for (int i = 0; i < iCleavage.length; i++) {
                iCleavables.put(Character.valueOf(iCleavage[i]), "1");
            }
        } else {
            iCleavables = new HashMap();
        }
    }

    /**
     * This method allows the caller to specify the cleavable residues. They
     * will be read from the String as a continuous summation of characters
     * (i.e: 'RKGH').
     *
     * @param aCleavage String with the continuous characters corresponding to
     * the cleavable residues. Note that the String is uppercased.
     */
    public void setCleavage(String aCleavage) {
        char[] temp = null;
        if (aCleavage != null) {
            temp = aCleavage.toUpperCase().toCharArray();
        }
        this.setCleavage(temp);
    }

    /**
     * Simple getter for the restricting residues of the Enzyme.
     *
     * @return char[] with the restricting residues.
     */
    public char[] getRestrict() {
        return iRestrict;
    }

    /**
     * This method allows the caller to specify the residues that restrict
     * cleavage.
     *
     * @param aRestrict char[] with the residues (in <b>UPPER CASE</b>!) which
     * restrict cleavage.
     */
    public void setRestrict(char[] aRestrict) {
        iRestrict = aRestrict;
        if (iRestrict != null) {
            iRestrictors = new HashMap(this.iRestrict.length);
            for (int i = 0; i < iRestrict.length; i++) {
                iRestrictors.put(Character.valueOf(iRestrict[i]), "1");
            }
        } else {
            iRestrictors = new HashMap();
        }
    }

    /**
     * This method allows the caller to specify the residues which restrict
     * cleavage. They will be read from the String as a continuous summation of
     * characters (i.e: 'PGHK').
     *
     * @param aRestrict String with the continuous characters corresponding to
     * the restricting residues. Note that the String is uppercased.
     */
    public void setRestrict(String aRestrict) {
        char[] temp = null;
        if (aRestrict != null) {
            temp = aRestrict.toUpperCase().toCharArray();
        }
        this.setRestrict(temp);
    }

    /**
     * Simple getter for the cleavage position of the Enzyme.
     *
     * @return int with the coded cleavage position (to be compared with the
     * constants on this class).
     */
    public int getPosition() {
        return iPosition;
    }

    /**
     * This method allows the caller to set the cleavage position for the
     * Enzyme. Please use the constants defined on this class as parameters.
     *
     * @param aPosition int with the coded position, according to the constants
     * on this class.
     */
    public void setPosition(int aPosition) {
        iPosition = aPosition;
    }

    /**
     * Simple getter for the number of allowed missed cleavages for the Enzyme.
     *
     * @return int with the number of allowed missed cleavages.
     */
    public int getMiscleavages() {
        return iMiscleavages;
    }

    /**
     * This method allows the caller to specify the number of allowed missed
     * cleavages for this enzyme.
     *
     * @param aMiscleavages int with the number of allowed missed cleavages.
     */
    public void setMiscleavages(int aMiscleavages) {
        iMiscleavages = aMiscleavages;
    }

    /**
     * This method generates a String representation of the Enzyme, which is
     * useful for displaying as useful information for the user or during
     * testing/debugging.
     *
     * @return String with a textual description of this Enzyme.
     */
    public String toString() {
        return this.toString("");
    }

    /**
     * This method generates a String representation of the Enzyme, which is
     * useful for displaying as useful information for the user or during
     * testing/debugging. It takes a parameter String that is prepended to each
     * line.
     *
     * @param aPrepend String to prepend to each outputted line.
     * @return String with a textual description of this Enzyme.
     */
    public String toString(String aPrepend) {
        StringBuffer result = new StringBuffer("\n" + aPrepend + "Hi, I'm the Enzyme '" + this.iTitle + "'.\n");

        result.append(aPrepend + "I cleave at the sight of: '" + new String(this.iCleavage) + "'.\n");
        if (this.iRestrict != null) {
            result.append(aPrepend + "My activity is restricted by these residus: '" + new String(this.iRestrict) + "'.\n");
        } else {
            result.append(aPrepend + "There are no residus that restrict my activity.\n");
        }
        result.append(aPrepend + "My position is '" + ((this.iPosition == Enzyme.CTERM) ? "C-terminal" : "N-terminal") + "'.\n");
        result.append(aPrepend + "I currently allow " + ((this.iMiscleavages == 0) ? "no" : "up to " + this.iMiscleavages) + " missed cleavage" + ((this.iMiscleavages == 1) ? "" : "s") + ".\n");

        return result.toString();
    }

    /**
     * This method is the focus of the Enzyme instance. It can perform an
     * <i>in-silico</i> digest of a Protein sequence according to the
     * specifications detailed in the construction or via the setters. Using
     * this methods returns all possible peptides, regardless of length. To only
     * return peptides within certain lengths use the other cleave method.
     *
     * @param aProtein Protein instance to cleave.
     * @return Protein[] with the resultant peptides.
     */
    public Protein[] cleave(Protein aProtein) {
        return cleave(aProtein, 0, Integer.MAX_VALUE);
    }

    /**
     * This method is the focus of the Enzyme instance. It can perform an
     * <i>in-silico</i> digest of a Protein sequence according to the
     * specifications detailed in the construction or via the setters. Only
     * returns peptides between the minimum and maximum peptide lengths.
     *
     * @param aProtein Protein instance to cleave.
     * @param minPeptideLength The minimum peptide length to consider
     * @param maxPeptideLength The maximum peptide length to consider
     * @return Protein[] with the resultant peptides.
     */
    public Protein[] cleave(Protein aProtein, int minPeptideLength, int maxPeptideLength) {

        // We'll need a lot of stuff here.
        //  - a Vector for all the startindices
        //  - a Vector for the stopindices
        //  - a Vector of intermediate results.
        Vector startIndices = new Vector(20, 10);
        Vector endIndices = new Vector(20, 10);
        Vector interMed = new Vector(20, 10);

        // We will also feed the current Protein sequence into a
        // char[] for easy iteration.
        char[] sequence = aProtein.getSequence().getSequence().toCharArray();

        // Check for a header that contains locations.
        int headerStart = 0;

        if (aProtein.getHeader() != null) {
            headerStart = aProtein.getHeader().getStartLocation() - 1;
            if (headerStart < 0) {
                headerStart = 0;
            }
        }

        // Okay, I guess we've set the stage now.
        // Let's start cleaving!
        int walkingIndex = 0;

        for (int i = 0; i < sequence.length; i++) {
            // Transform the current char into the corresponding wrapper.
            Character current = Character.valueOf(sequence[i]);

            // See whether it is a cleavable residu!
            if (iCleavables.get(current) != null) {
                // Okay, this should be cleavable.
                // First of all however, we need to check
                // for the possible presence of a restrictor!
                // (And, of course, first check to see whether there is a
                //  next character at all!)
                if ((i + 1) < sequence.length) {
                    Character next = Character.valueOf(sequence[i + 1]);
                    if (iRestrictors.get(next) != null) {
                        // It is a restrictor!
                        // Just let the loop continue!
                        continue;
                    }
                }

                // Since we've gotten to here, we need to cleave here!
                // So do it!
                // Oh yeah, and mind the position of cleaving!
                String temp = null;
                int start = -1;
                int end = -1;

                if (this.iPosition == Enzyme.CTERM) {
                    // Take the part, starting from walkingIndex up to the current
                    // as a new peptide and store it in the interMed Vector.
                    temp = new String(sequence, walkingIndex, ((i - walkingIndex) + 1));
                    // Start index is human-readable (starting from 1),
                    // hence the '+1'.
                    start = headerStart + walkingIndex + 1;
                    end = headerStart + i + 1;
                    // Start the next peptide after the current one.
                    // An index that so happens to
                    walkingIndex = i + 1;
                } else if (this.iPosition == Enzyme.NTERM) {
                    temp = new String(sequence, walkingIndex, (i - walkingIndex));
                    // Start index is human readable: starting from 1.
                    start = headerStart + walkingIndex + 1;
                    end = headerStart + i;
                    walkingIndex = i;
                }

                // Add each retrieved value to the correct
                // Vector.
                interMed.add(temp);
                startIndices.add(Integer.valueOf(start));
                endIndices.add(Integer.valueOf(end));
            }
        }

        // Add this point, we should check whether we have
        // the entire sequence.
        // We probably don't, because the last cleavable residu will
        // probably not have been the last residu in the sequence.
        // That's why we should append the 'remainder' of our cleavage
        // as well (and the corresponding indices as well, of course).
        if ((walkingIndex < sequence.length) && (!aProtein.isTruncated() || aProtein.getTruncationPosition() == Protein.CTERMTRUNC)) {
            interMed.add(new String(sequence, walkingIndex, (sequence.length - walkingIndex)));
            startIndices.add(Integer.valueOf(headerStart + walkingIndex + 1));
            endIndices.add(Integer.valueOf(headerStart + sequence.length));
        }

        // Allright, now we should have all the individual peptides.
        // Now we should take into account the specified number of miscleavages.

        // Get all the sequences up to now.
        String[] imSequences = (String[]) interMed.toArray(new String[interMed.size()]);

        // Cycle the current sequences.
        for (int j = 0; j < imSequences.length; j++) {

            String temp = imSequences[j];

            // Apply the number of allowed missed cleavages sequentially from
            // this sequence.
            for (int k = 0; k < this.iMiscleavages; k++) {

                // If we fall outside of the range of current sequences
                // (for instance if we try to apply a second allowed missed
                //  cleavage to the penultimate peptide, we fall outside of
                //  the available peptides!)
                // we break the loop.
                if ((j + k + 1) >= imSequences.length) {
                    break;
                }

                // Add our constructed sequence.
                temp += imSequences[j + k + 1];
                interMed.add(temp);
                startIndices.add(startIndices.get(j));
                endIndices.add(endIndices.get(j + k + 1));
            }
        }

        // Cycle all to check for

        // Cycle all again, and do a cleanup if C-terminal truncation has been detected.
        if ((aProtein.isTruncated()) && (aProtein.getTruncationPosition() == Protein.CTERMTRUNC)) {

            // Okay, C-terminal truncation is flagged.
            // This means that all peptides with a startindex equal to the startindex
            // of the parent (or '1' if the parent does not have startindex), are not
            // realistic peptides, but artifacts of our truncation.
            // So they get kicked out.
            int parentStart = aProtein.getHeader().getStartLocation();

            if (parentStart < 0) {
                parentStart = 1;
            }

            for (int i = 0; i < interMed.size(); i++) {
                int start = ((Integer) startIndices.get(i)).intValue();
                if (start == parentStart) {
                    startIndices.remove(i);
                    endIndices.remove(i);
                    interMed.remove(i);
                    i--;
                }
            }
        }

        // We've got all sequences.
        // Let's construct the Protein instances for them and
        // then return them!
        int liSize = interMed.size();
        Vector result = new Vector(liSize);
        Header header = aProtein.getHeader();

        // Create the Proteins and store them.
        for (int i = 0; i < liSize; i++) {

            // If the sequence comes from a translation, it will contain an '_' if a stopcodon is present.
            // Omit all sequences containing these.
            String pepSequence = (String) interMed.get(i);
            if (pepSequence.indexOf("_") < 0) {

                // only include peptides within the min and max peptide lengths
                if (pepSequence.length() >= minPeptideLength && pepSequence.length() <= maxPeptideLength) {

                    Header h = null;

                    if (header != null) {
                        h = (Header) header.clone();
                        h.setLocation(((Integer) startIndices.get(i)).intValue(), ((Integer) endIndices.get(i)).intValue());
                    }

                    result.add(new Protein(h, new AASequenceImpl(pepSequence)));
                }
            }
        }

        Protein[] finalResult = new Protein[result.size()];
        result.toArray(finalResult);

        return finalResult;
    }

    /**
     * This method returns a deep copy of the current Enzyme.
     *
     * @return Object Enzyme instance that is a deep copy of the current Enzyme.
     */
    public Object clone() {
        Enzyme e = null;
        try {
            e = (Enzyme) super.clone();
            e.iCleavables = this.iCleavables;
            e.iCleavage = this.iCleavage;
            e.iMiscleavages = this.iMiscleavages;
            e.iPosition = this.iPosition;
            e.iRestrict = this.iRestrict;
            e.iRestrictors = this.iRestrictors;
            e.iTitle = this.iTitle;
        } catch (CloneNotSupportedException cnse) {
            logger.error(cnse.getMessage(), cnse);
        }
        return e;
    }

    /**
     * This method reports on the possibility that the presented subsequence is
     * the result of enzymatic activity. Note that using a substring, only the
     * FIRST (starting from the N-terminus) occurrence of the subsequence in the
     * parent String will be considered! If multiple occurrences are possible,
     * use the overloaded method that takes indices. Returning int values can be
     * checked against public static final vars on this class.
     *
     * @param aParentSequence String with the parent sequence
     * @param aSubSequence String with the subsequence
     * @return int with the coded possibility (1 = Full enzymatic product, 2 =
     * N-terminal half enzymatic product, 3 = C-terminal half enzymatic product
     * and 4 = Entirely not an enzymatic product.
     */
    public int isEnzymaticProduct(String aParentSequence, String aSubSequence) {
        int start = aParentSequence.indexOf(aSubSequence);
        int end = start + aSubSequence.length();

        return this.isEnzymaticProduct(aParentSequence, start + 1, end);
    }

    /**
     * This method reports on the possibility that the presented subsequence
     * (represented by the start and end location in the parent) is the result
     * of enzymatic activity. Returning int values can be checked against public
     * static final vars on this class.
     *
     * @param aParentSequence String with the parent sequence
     * @param aStart int with the start of the subsequence relative to the
     * parent (first residue is '1').
     * @param aEnd int with the end of the subsequence relative to the parent
     * @return int with the coded possibility (1 = Full enzymatic product, 2 =
     * N-terminal half enzymatic product, 3 = C-terminal half enzymatic product
     * and 4 = Entirely not an enzymatic product.
     */
    public int isEnzymaticProduct(String aParentSequence, int aStart, int aEnd) {
        int result = 0;

        // Correction for human-readable indices.
        aStart--;
        aEnd--;

        // Check validity of parameters.
        if ((aStart < 0) || (aEnd < 0)) {
            throw new IllegalArgumentException("Subsequence is not a subsequence of the parent!");
        }
        if (aEnd > aParentSequence.length() - 1) {
            throw new IllegalArgumentException("Subsequence end index out of parent length range (" + aEnd + ">" + (aParentSequence.length() - 1) + ")!");
        }
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Subsequence could not be retreived since start index is greater than end index (" + aStart + ">=" + aEnd + ")!");
        }
        // The maximum length of the sequence.
        int maxLength = aParentSequence.length();

        if (this.getPosition() == Enzyme.CTERM) {
            // First check N-terminal side.
            if ((aStart - 1) >= 0) {
                Character residue = Character.valueOf(aParentSequence.charAt(aStart - 1));
                Character possRestrict = Character.valueOf(aParentSequence.charAt(aStart));
                if ((this.iCleavables.get(residue) != null) && (this.iRestrictors.get(possRestrict) == null)) {
                    result += 1;
                }
            } else {
                // It is the N-terminus of the parent. This checks out.
                result += 1;
            }

            // Now C-terminal side.
            Character residue = Character.valueOf(aParentSequence.charAt(aEnd));
            if ((this.iCleavables.get(residue) != null) || ((aEnd + 1) == maxLength)) {
                if ((aEnd + 1) < maxLength) {
                    if (this.iRestrictors.get(Character.valueOf(aParentSequence.charAt(aEnd + 1))) == null) {
                        result += 2;
                    }
                } else {
                    // The cleavage site appears to be the C-terminal residue in
                    // the parent sequence. This qualifies.
                    result += 2;
                }
            }
        } else {
            // First check N-terminal side.
            Character residue = Character.valueOf(aParentSequence.charAt(aStart));
            if ((iCleavables.get(residue) != null)) {
                // The site is potentially cleavable.
                // What about restriction residues?
                if (((aStart + 1) < maxLength) && (iRestrictors.get(Character.valueOf(aParentSequence.charAt(aStart + 1))) != null)) {
                    // Do nothing, since it is not a site.
                } else {
                    // It is a true site.
                    result += 1;
                }
            } else if (aStart == 0) {
                // It is the N-terminus. Validate it.
                result += 1;
            }

            // Now C-terminal side.
            if ((aEnd + 1) < maxLength) {
                Character residue2 = Character.valueOf(aParentSequence.charAt(aEnd + 1));
                if (this.iCleavables.get(residue2) != null) {
                    if ((aEnd + 2) < maxLength) {
                        if (this.iRestrictors.get(Character.valueOf(aParentSequence.charAt(aEnd + 2))) == null) {
                            result += 2;
                        }
                    } else {
                        // The cleavage site appears to be the C-terminal residue in
                        // the parent sequence. This qualifies.
                        result += 2;
                    }
                }
            } else if (aEnd == (maxLength - 1)) {
                // The C-terminus of the subsequence is the C-terminus of the parent.
                // This qualifies.
                result += 2;
            }
        }

        // That's it. Let's have a look, shall we?
        switch (result) {
            case 0:
                // Neither of the termini checked out.
                result = Enzyme.ENTIRELY_NOT_ENZYMATIC;
                break;
            case 1:
                // Only N-term checked out.
                result = Enzyme.N_TERM_ENZYMATIC;
                break;
            case 2:
                // Only C-tyerm checked out.
                result = Enzyme.C_TERM_ENZYMATIC;
                break;
            case 3:
                // Both N- and C-term checked out.
                result = Enzyme.FULLY_ENZYMATIC;
                break;
            default:
                throw new RuntimeException("A number larger than 3 has been calculated for the 'enzymaticness' of a peptide.");
        }

        return result;
    }
}
