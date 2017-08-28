/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-sep-2003
 * Time: 20:51:50
 */
package com.compomics.util.protein;
import com.compomics.util.experiment.io.biology.protein.Header;
import org.apache.log4j.Logger;

import java.util.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class implements an enzyme with a dual specificity; the N-terminus of a
 * resultant peptide will have certain residue, the C-terminus will have another,
 * eg. for a C-terminal cutter with N-terminal specificty for 'D' and C-terminal specificity
 * for 'R': (D)XXXXXR; for an N-terminal cutter with N-terminal specificty for 'W' and C-terminal specificity
 * for 'K': WXXXXX(K).
 *
 * @author Lennart Martens
 */
public class DualEnzyme extends Enzyme {

    // Class specific log4j logger for DualEnzyme instances.
    static Logger logger = Logger.getLogger(DualEnzyme.class);

    /**
     * The HashMap with the cleavables for the N-terminal side of the
     * resultant peptide.
     */
    private HashMap iNtermCleavables = null;

    /**
     * The HashMap with the cleavables for the C-terminal side of the
     * resultant peptide.
     */
    private HashMap iCtermCleavables = null;

    /**
     * The code for N-terminal position in the resultant peptide.
     */
    public static final int NTERMINAL = 0;

    /**
     * The code for C-terminal position in the resultant peptide.
     */
    public static final int CTERMINAL = 1;

    /**
     * This constructor allows you to specify all the information for this
     * enzyme plus the number of missed cleavages that this instance will allow.
     * Title and restrict can be 'null'.
     *
     * @param   aTitle  String with the title (or name) for this enzyme.
     * @param   aNtermCleavage  String composed of the residues after which cleavage
     *                          will occur at the N-terminus of the resultant peptide
     *                          (this String will be uppercased).
     * @param   aCtermCleavage  String composed of the residues after which cleavage
     *                          will occur at the C-terminus of the resultant peptide
     *                          (this String will be uppercased).
     * @param   aRestrict   String composed of the residues which inhibit cleavage
     *                      if present behind any of the cleavable residues (this String will be uppercased).
     * @param   aPosition   String which should correspond to "Cterm" or "Nterm"
     *                      for each position respectively.
     * @param   aMiscleavages   int with the number of supported missed cleavages.
     */
    public DualEnzyme(String aTitle, String aNtermCleavage, String aCtermCleavage, String aRestrict, String aPosition, int aMiscleavages) {
        super(aTitle, "", aRestrict, aPosition, aMiscleavages);
        this.setCleavage(aNtermCleavage, DualEnzyme.NTERMINAL);
        this.setCleavage(aCtermCleavage, DualEnzyme.CTERMINAL);
    }

    /**
     * This constructor allows you to specify all the information for this
     * enzyme. Title and restrict can be 'null'.
     *
     * @param   aTitle  String with the title (or name) for this enzyme.
     * @param   aNtermCleavage  String composed of the residues after which cleavage
     *                          will occur at the N-terminus of the resultant peptide
     *                          (this String will be uppercased).
     * @param   aCtermCleavage  String composed of the residues after which cleavage
     *                          will occur at the C-terminus of the resultant peptide
     *                          (this String will be uppercased).
     * @param   aRestrict   String composed of the residues which inhibit cleavage
     *                      if present behind any of the cleavable residues (this String will be uppercased).
     * @param   aPosition   String which should correspond to "Cterm" or "Nterm"
     *                      for each position respectively.
     */
    public DualEnzyme(String aTitle, String aNtermCleavage, String aCtermCleavage, String aRestrict, String aPosition) {
        super(aTitle, "", aRestrict, aPosition);
        this.setCleavage(aNtermCleavage, DualEnzyme.NTERMINAL);
        this.setCleavage(aCtermCleavage, DualEnzyme.CTERMINAL);
    }

    /**
     * This method allows the caller to specify the cleavable residues.
     *
     * @param   aCleavage   char[] with the cleavable residues
     *                      (in <b>UPPER CASE</b>!).
     */
    public void setCleavage(char[] aCleavage) {
        super.setCleavage(aCleavage);
        if(aCleavage != null) {
            iNtermCleavables = new HashMap(aCleavage.length);
            iCtermCleavables = new HashMap(aCleavage.length);
            for(int i = 0; i < aCleavage.length; i++) {
                iNtermCleavables.put(Character.valueOf(aCleavage[i]), "1");
                iCtermCleavables.put(Character.valueOf(aCleavage[i]), "1");
            }
        } else {
            iNtermCleavables = new HashMap();
            iCtermCleavables = new HashMap();
        }
    }

    /**
     * This method allows the caller to specify the cleavable residues.
     * They will be read from the String as a continuous summation of
     * characters (i.e: 'RKGH').
     *
     * @param   aCleavage   String with the continuous characters
     *                      corresponding to the cleavable residues.
     *                      Note that the String is uppercased.
     */
    public void setCleavage(String aCleavage) {
        char[] temp = null;
        if(aCleavage != null) {
            temp = aCleavage.toUpperCase().toCharArray();
        }
        this.setCleavage(temp);
    }

    /**
     * This method allows the caller to specify the cleavable residues.
     *
     * @param   aCleavage   char[] with the cleavable residues
     *                      (in <b>UPPER CASE</b>!).
     * @param   aTerminus   int with the code for the terminal position in
     *                      the resultant peptide. Can be 'NTERMINAL' or 'CTERMINAL'.
     */
    public void setCleavage(char[] aCleavage, int aTerminus) {
        HashMap cleavables = null;
        if(aCleavage != null) {
            cleavables = new HashMap(aCleavage.length);
            for(int i = 0; i < aCleavage.length; i++) {
                cleavables.put(Character.valueOf(aCleavage[i]), "1");
            }
        } else {
            cleavables = new HashMap();
        }
        switch(aTerminus) {
            case NTERMINAL:
                iNtermCleavables = cleavables;
                break;
            case CTERMINAL:
                iCtermCleavables = cleavables;
                break;
            default:
                throw new IllegalArgumentException("You specified " + aTerminus
                        + " as the terminus code, while it should be "
                        + DualEnzyme.NTERMINAL + " (NTERMINAL) or "
                        + DualEnzyme.CTERMINAL + " (CTERMINAL)!");
        }
    }

    /**
     * This method allows the caller to specify the cleavable residus.
     * They will be read from the String as a continuous summation of
     * characters (i.e: 'RKGH').
     *
     * @param   aCleavage   String with the continuous characters
     *                      corresponding to the cleavable residues.
     *                      Note that the String is uppercased.
     * @param   aTerminus   int with the code for the terminal position in
     *                      the resultant peptide. Can be 'NTERMINAL' or 'CTERMINAL'.
     */
    public void setCleavage(String aCleavage, int aTerminus) {
        char[] temp = null;
        if(aCleavage != null) {
            temp = aCleavage.toUpperCase().toCharArray();
        }
        this.setCleavage(temp, aTerminus);
    }

    /**
     * This method returns the residues that are used for cleavage at the respective
     * locations.
     *
     * @param aTerminus int with the code for the terminal position in
 *                      the resultant peptide. Can be 'NTERMINAL' or 'CTERMINAL'.
     * @return  char[]  with the cleavable residues for the specified terminus of the resultant peptide.
     */
    public char[] getCleavage(int aTerminus) {

        Set keys = null;
        switch(aTerminus) {
            case NTERMINAL:
                keys = iNtermCleavables.keySet();
                break;
            case CTERMINAL:
                keys = iCtermCleavables.keySet();
                break;
            default:
                throw new IllegalArgumentException("You specified " + aTerminus
                        + " as the terminus code, while it should be "
                        + DualEnzyme.NTERMINAL + " (NTERMINAL) or "
                        + DualEnzyme.CTERMINAL + " (CTERMINAL)!");
        }
        
        ArrayList lList = new ArrayList(keys);
        Collections.sort(lList);

        char[] result = new char[lList.size()];
        for (int i = 0; i < lList.size(); i++) {
            char c = ((Character) lList.get(i)).charValue();
            result[i] = c;
        }

        return result;
    }

    /**
     * Simple getter for the cleavable residues of the Enzyme.
     * For a DualEnzyme, it returns [nterms]X[cterms]
     *
     * @return  char[]  with the cleavable residues, structured as [nterms]X[cterms].
     */
    public char[] getCleavage() {
        Set nTermKeys = iNtermCleavables.keySet();
        Set cTermKeys = iCtermCleavables.keySet();
        char[] result = new char[nTermKeys.size() + cTermKeys.size() + 1];
        int counter = 0;
        Iterator iter = nTermKeys.iterator();
        while(iter.hasNext()) {
            Character lCharacter = (Character)iter.next();
            result[counter] = lCharacter.charValue();
            counter++;
        }
        // Put an 'X' in between.
        result[counter] = 'X';
        counter++;

        iter = cTermKeys.iterator();
        while(iter.hasNext()) {
            Character lCharacter = (Character)iter.next();
            result[counter] = lCharacter.charValue();
        }

        return result;
    }

    /**
     * Provides a cloned version of this DualEnzyme.
     *
     * @return  Enzyme with a clone for this DualEnzyme.
     */
    public Object clone() {
        DualEnzyme de = (DualEnzyme)super.clone();
        if(de != null) {
            de.iCtermCleavables = this.iCtermCleavables;
            de.iNtermCleavables = this.iNtermCleavables;
        }
        return de;
    }

    /**
     * This method generates a String representation of the DualEnzyme,
     * which is useful for displaying as useful information for the user or
     * during testing/debugging.
     *
     * @return  String  with a textual description of this Enzyme.
     */
    public String toString() {
        return this.toString("");
    }

    /**
     * This method generates a String representation of the DualEnzyme,
     * which is useful for displaying as useful information for the user or
     * during testing/debugging. It takes a parameter String that is prepended to each line.
     *
     * @param   aPrepend    String to prepend to each outputted line.
     * @return  String  with a textual description of this DualEnzyme.
     */
    public String toString(String aPrepend) {
        StringBuffer result = new StringBuffer("\n" + aPrepend + "Hi, I'm the DualEnzyme '" + this.getTitle() + "'.\n");

        result.append(aPrepend + "I cleave at the sight of:\n");
        result.append(aPrepend + "\t- Nterminal: '" + new String(this.getCleavage(DualEnzyme.NTERMINAL)) + "'.\n");
        result.append(aPrepend + "\t- Cterminal: '" + new String(this.getCleavage(DualEnzyme.CTERMINAL)) + "'.\n");
        if(this.getRestrict() != null && this.getRestrict().length > 0) {
            result.append(aPrepend + "My activity is restricted by these residus: '" + new String(this.getRestrict()) + "'.\n");
        } else {
            result.append(aPrepend + "There are no residus that restrict my activity.\n");
        }
        result.append(aPrepend + "My position is '" + ((this.getPosition() == Enzyme.CTERM)?"C-terminal":"N-terminal") + "'.\n");
        result.append(aPrepend + "I currently allow " + ((this.getMiscleavages() == 0)?"no":"up to " + this.getMiscleavages()) + " missed cleavage" + ((this.getMiscleavages() == 1)?"":"s") + ".\n");

        return result.toString();
    }

    /**
     * This cleave method will process sequence XDYRZ solely into
     * YR peptides.
     *
     * @param   aProtein    Protein instance to cleave.
     * @return  Protein[]   with the resultant peptides.
     */
    public Protein[] oldCleave(Protein aProtein) {
        // Final result.
        Protein[] result = null;
        // Intermediate result.
        ArrayList proteins = new ArrayList();
        // Get the header.
        Header header = aProtein.getHeader();
        // Get the sequence as String and as char[].
        String seqString = aProtein.getSequence().getSequence();
        char[] sequence = seqString.toCharArray();
        // Get the start location.
        int start = aProtein.getHeader().getStartLocation()-1;
        // If there was no start location known, set it to be '0'.
        if(start<0) {
            start = 0;
        }
        // Cycle each char in the String.
        for(int i = 0; i < sequence.length; i++) {
            if(this.isCleavable(sequence, i, iNtermCleavables, iRestrictors)) {
                // In getting here, we can be sure that this residue is a starting
                // cleavage site.
                int init = i;
                // Now find the peptides we can construct from here on,
                // with the specified number of missed cleavages.
                int countMC = 0;
                for(int tryout=i;tryout<sequence.length;tryout++) {
                    if(countMC > iMiscleavages) {
                        break;
                    }
                    // Correct cleavage site is detected if:
                    //  - it really is a cleavable residue without a restrictor.
                    //  - it is the C-terminus of the sequence, in which case it is also correct.
                    if(this.isCleavable(sequence, tryout, iCtermCleavables, iRestrictors) || tryout == (sequence.length-1)) {
                        // Add one to the missed cleavage counter.
                        countMC++;
                        // Create a new Protein based on the info we now have.
                        Header h = (Header)header.clone();
                        int tempStart = -1;
                        int tempStop = -1;
                        // We need to recalculate the start and stop indices, depending on existing start and
                        // stop indices and N-terminal or C-terminal cleavage.
                        if(this.iPosition == Enzyme.CTERM) {
                            // Take the part, starting from init+1 up to and including the current
                            // as a new peptide and store it in the interMed Vector.
                            tempStart = init+1;
                            tempStop = tryout+1;
                        } else if(this.iPosition == Enzyme.NTERM){
                            tempStart = init;
                            tempStop = tryout;
                            // If it was the C-terminus, it should be included since it is not enzymatic and
                            // therefore not N-terminally cleaved off!
                            if(tryout == (sequence.length-1)) {
                                tempStop++;
                            }
                        }
                        // Human readable start and stop in the header, hence the '+1'.
                        h.setLocation(start+tempStart+1, start+tempStop);
                        proteins.add(new Protein(h, new AASequenceImpl(seqString.substring(tempStart, tempStop))));
                    }
                }
            }
        }
        // Now transform the ArrayList into an array.
        result = new Protein[proteins.size()];
        proteins.toArray(result);
        // That's it.
        return result;
    }

    /**
     * This method is the focus of the Enzyme instance. It can perform
     * an <i>in-silico</i> digest of a Protein sequence according to the
     * specifications detailed in the construction or via the setters.
     *
     * @param   aProtein    Protein instance to cleave.
     * @return  Protein[]   with the resultant peptides.
     */
    public Protein[] cleave(Protein aProtein) {
        // Final result.
        Protein[] result = null;
        // Intermediate result.
        ArrayList proteins = new ArrayList();
        // Get the header.
        Header header = aProtein.getHeader();
        // Get the sequence as String and as char[].
        String seqString = aProtein.getSequence().getSequence();
        char[] sequence = seqString.toCharArray();
        // Get the start location.
        int start = aProtein.getHeader().getStartLocation()-1;
        // If there was no start location known, set it to be '0'.
        if(start<0) {
            start = 0;
        }
        // Previous C-term cleavage position (without taking miscleavages into account).
        int previousCtermCleavagePosition = 0;
        // Previous N-term cleavage position (without taking miscleavages into account).
        int previousNtermCleavagePosition = 0;
        // Cycle each char in the String.
        for(int i = 0; i < sequence.length; i++) {
            if(this.isCleavable(sequence, i, iNtermCleavables, iRestrictors)) {
                // In getting here, we can be sure that this residue is a starting
                // cleavage site.
                int init = i;
                // Thus, the peptide from the previous C-terminal cleavage residue up
                // to this one (for C-term cleavage, including this one), is also an
                // enzymatic peptide. We'll process it here separately.
                int endLoc = -1;
                if(this.iPosition == Enzyme.CTERM) {
                    endLoc = i+1;
                } else if(this.iPosition == Enzyme.NTERM) {
                    endLoc = i;
                }
                // The starting location of this 'intermediate' peptide depends on the residue
                // order. If the previous C-terminal cleavage is greater than the current N-terminal position
                // (as in FGDHVDGHRTS, for instance), we should take the previous N-terminal position as starting
                // point.
                int startLoc = previousCtermCleavagePosition;
                if(previousCtermCleavagePosition > init) {
                    startLoc = previousNtermCleavagePosition;
                }
                Header intermed = (Header)header.clone();
                // The header locations  are defined to be human-readable (ie., start from '1').
                // Therefore the start location is augmented with 1, the end location (which in Java is not inclusive)
                // does not need to be augmented.
                intermed.setLocation(start + startLoc + 1, start + endLoc);
                proteins.add(new Protein(intermed, new AASequenceImpl(seqString.substring(startLoc, endLoc))));
                // Set the previous N-term cleavage position.
                if(this.iPosition == Enzyme.CTERM) {
                    previousNtermCleavagePosition = init+1;
                } else if(this.iPosition == Enzyme.NTERM) {
                    previousNtermCleavagePosition = init;
                }

                // Now find the peptides we can construct from here on,
                // with the specified number of missed cleavages.
                int countMC = 0;
                for(int tryout=i;tryout<sequence.length;tryout++) {
                    if(countMC > iMiscleavages) {
                        break;
                    }
                    // Correct cleavage site is detected if:
                    //  - it really is a cleavable residue without a restrictor.
                    //  - it is the C-terminus of the sequence, in which case it is also correct.
                    if(this.isCleavable(sequence, tryout, iCtermCleavables, iRestrictors) || tryout == (sequence.length-1)) {
                        // Add one to the missed cleavage counter.
                        countMC++;
                        // Create a new Protein based on the info we now have.
                        Header h = (Header)header.clone();
                        int tempStart = -1;
                        int tempStop = -1;
                        // We need to recalculate the start and stop indices, depending on existing start and
                        // stop indices and N-terminal or C-terminal cleavage.
                        if(this.iPosition == Enzyme.CTERM) {
                            // Take the part, starting from init+1 up to and including the current
                            // as a new peptide and store it in the interMed Vector.
                            tempStart = init+1;
                            tempStop = tryout+1;
                        } else if(this.iPosition == Enzyme.NTERM){
                            tempStart = init;
                            tempStop = tryout;
                            // If it was the C-terminus, it should be included since it is not enzymatic and
                            // therefore not N-terminally cleaved off!
                            if(tryout == (sequence.length-1)) {
                                tempStop++;
                            }
                        }
                        // Extra check; if we have a cleavable at the C-terminus, we'll have an
                        // empty String for a peptide. Which isn't good. Skip this.
                        if(tempStart == tempStop) {
                            continue;
                        }
                        // If this is the first cleavage site (at this point countMC == 1)
                        // then store the current C-terminal cleavage location for use with
                        // the next intermediate peptide.
                        if(countMC == 1) {
                            previousCtermCleavagePosition = tempStop;
                        }

                        // The header locations are defined to be human-readable (ie., start from '1').
                        // Therefore the start location is augmented with 1, the end location (which in Java is not inclusive)
                        // does not need to be augmented.
                        h.setLocation(start+tempStart+1, start+tempStop);
                        proteins.add(new Protein(h, new AASequenceImpl(seqString.substring(tempStart, tempStop))));
                    }
                }
            }
        }
        // Before we finalize the cleaving, we should probably check for the C-terminus of the protein,
        // since this is probably not cleaved up till now.
        if(previousCtermCleavagePosition < sequence.length) {
            Header h = (Header)header.clone();
            h.setLocation(start + previousCtermCleavagePosition + 1, start + sequence.length);
            proteins.add(new Protein(h, new AASequenceImpl(seqString.substring(previousCtermCleavagePosition, sequence.length))));
        }
        // Now transform the ArrayList into an array.
        result = new Protein[proteins.size()];
        proteins.toArray(result);
        // That's it.
        return result;
    }

    /**
     * This method reports on the possibility that the presented subsequence
     * (represented by the start and end location in the parent) is the result
     * of enzymatic activity.
     * Returning int values can be checked against public static final vars on this class.
     *
     * @param   aParentSequence String with the parent sequence
     * @param   aStart  int with the start of the subsequence relative to the parent (first residue is '1').
     * @param   aEnd  int with the end of the subsequence relative to the parent
     * @return  int with the coded possibility (1 = Full enzymatic product,
     *              2 = N-terminal half enzymatic product, 3 = C-terminal half
     *              enzymatic product and 4 = Entirely not an enzymatic product.
     */
    public int isEnzymaticProduct(String aParentSequence, int aStart, int aEnd) {
        int result = 0;

        // Correction for human-readable indices.
        aStart--;
        aEnd--;

        // Check validity of parameters.
        if((aStart < 0) || (aEnd <0)) {
            throw new IllegalArgumentException("Subsequence is not a subsequence of the parent!");
        }
        if(aEnd > aParentSequence.length()-1) {
            throw new IllegalArgumentException("Subsequence end index out of parent length range (" + aEnd + ">" + (aParentSequence.length()-1) + ")!");
        }
        if(aStart >= aEnd) {
            throw new IllegalArgumentException("Subsequence could not be retreived since start index is greater than or equal to end index (" + aStart + ">=" + aEnd + ")!");
        }

        // The maximum length of the sequence.
        int maxLength = aParentSequence.length();

        if(this.getPosition() == Enzyme.CTERM) {
            // First check N-terminal side.
            if((aStart-1) >= 0) {
                Character residue = Character.valueOf(aParentSequence.charAt(aStart-1));
                Character possRestrict = Character.valueOf(aParentSequence.charAt(aStart));
                if((this.iNtermCleavables.get(residue) != null) && (this.iRestrictors.get(possRestrict) == null)) {
                    result += 1;
                }
            } else {
                // It is the N-terminus of the parent. This checks out.
                result += 1;
            }

            // Now C-terminal side.
            Character residue = Character.valueOf(aParentSequence.charAt(aEnd));
            if((this.iCtermCleavables.get(residue) != null) || ((aEnd+1) == maxLength)) {
                if((aEnd+1) < maxLength) {
                    if(this.iRestrictors.get(Character.valueOf(aParentSequence.charAt(aEnd+1))) == null)
                        result += 2;
                } else {
                    // The cleavage site appears to be the C-terminal residue in
                    // the parent sequence. This qualifies.
                    result += 2;
                }
            }
        } else {
            // First check N-terminal side.
            Character residue = Character.valueOf(aParentSequence.charAt(aStart));
            if((iNtermCleavables.get(residue) != null)) {
                // The site is potentially cleavable.
                // What about restriction residues?
                if( ((aStart+1) < maxLength) && (iRestrictors.get(Character.valueOf(aParentSequence.charAt(aStart+1))) != null) ) {
                    // Do nothing, since it is not a site.
                } else {
                    // It is a true site.
                    result += 1;
                }
            } else if(aStart == 0) {
                // It is the N-terminus. Validate it.
                result += 1;
            }

            // Now C-terminal side.
            if((aEnd+1) < maxLength) {
                Character residue2 = Character.valueOf(aParentSequence.charAt(aEnd+1));
                if(this.iCtermCleavables.get(residue2) != null) {
                    if((aEnd+2) < maxLength) {
                        if(this.iRestrictors.get(Character.valueOf(aParentSequence.charAt(aEnd+2))) == null)
                            result += 2;
                    } else {
                        // The cleavage site appears to be the C-terminal residue in
                        // the parent sequence. This qualifies.
                        result += 2;
                    }
                }
            } else if(aEnd == (maxLength-1)) {
                // The C-terminus of the subsequence is the C-terminus of the parent.
                // This qualifies.
                result += 2;
            }
        }

        // That's it. Let's have a look, shall we?
        switch(result) {
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

    /**
     * This method checks whether a certain residue is cleavable, based on the sequence it is in,
     * the position the residue is at and a Map of cleavable residues and restrictors.
     *
     * @param aSequence char[] with the sequence.
     * @param aPosition int with the position of the residue in the char[].
     * @param aCleavables   HashMap with the cleavables as Characters as keys.
     * @param aRestrictors  HashMap with the restrictors as Characters as keys.
     * @return  boolean 'true' when the residue is considered a cleavage site,
     *                  'false' otherwise.
     */
    private boolean isCleavable(char[] aSequence, int aPosition, HashMap aCleavables, HashMap aRestrictors) {
        boolean cleavable = false;
        // Check params.
        if(aPosition >= aSequence.length || aPosition < 0) {
            throw new IllegalArgumentException("Your position (" + aPosition + ") was outside of sequence boundaries (0, " + (aSequence.length-1) + ")!");
        }
        // Okay, that checks out.
        // Now we first see whether this position really can be cleaved.
        if(aCleavables.containsKey(Character.valueOf(aSequence[aPosition]))) {
            // Okay, it could possibly be a cleavage site.
            // See if it has a C-terminal residue, and whether it is a restrictor.
            if( (aPosition+1 < aSequence.length) && aRestrictors.containsKey(Character.valueOf(aSequence[aPosition+1])) ) {
                // It is a restrictor!
                cleavable = false;
            } else {
                cleavable = true;
            }
        }
        return cleavable;
    }

    /**
     * Tests the DualEnzyme by digesting a hardcoded protein.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Enzyme dual = new DualEnzyme("TestDualEnzyme", "D", "R", "P", "Cterm", 0);
        Protein[] p = dual.cleave(new Protein(">sw|Q55645 (15-45)|TEST_HUMAN Test Protein for the cleave() method.", "FGHDKLMDTGKRVWRGHF"));

        for(int i = 0; i < p.length; i++) {
            Protein lProtein = p[i];
            logger.info(lProtein.getHeader().getFullHeaderWithAddenda());
            logger.info(lProtein.getSequence().getSequence());
        }
    }
}
