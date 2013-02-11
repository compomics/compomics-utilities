package com.compomics.util.protein;
import org.apache.log4j.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import java.util.*;

/**
 * This class implements the functionality of an Enzyme by
 * simulating digestion based on a regular expression.
 * 
 * @author Florian Reisinger
 * @since 2.9
 */
public class RegExEnzyme extends Enzyme {

    // Class specific log4j logger for RegExEnzyme instances.
    Logger logger = Logger.getLogger(RegExEnzyme.class);

    private Pattern iCleavagePattern = null;

    /**
     * Create a new RegExEnzyme.
     *
     * @param aTitle
     * @param aCleavage
     * @param aRestrict
     * @param aPosition
     */
    public RegExEnzyme(String aTitle, String aCleavage, String aRestrict, String aPosition) {
        this(aTitle, aCleavage, aRestrict, aPosition, 1);
    }

    /**
     * Create a new RegExEnzyme.
     *
     * @param aTitle
     * @param aCleavage
     * @param aRestrict
     * @param aPosition
     * @param aMiscleavages
     */
    public RegExEnzyme(String aTitle, String aCleavage, String aRestrict, String aPosition, int aMiscleavages) {
        // since the cleavage/restriction pattern for this class are expected to be a
        // regular expression, we don't want to use the default implementation
        super(aTitle, "", aRestrict, aPosition, aMiscleavages);
        // now interpret the cleavage/restriction strings as regular expressions
        this.setCleavage(aCleavage);
        this.setRestrict(aRestrict);
    }

    /**
     * This method can be used to set the cleavage pattern for this RegExEnzyme.
     * The pattern is used by the enzyme to find the cleavage sits, where the last
     * residue matched by the pattern defines the cleavage site.
     * (e.g. in case of C-terminal cleavage, the enzyme cuts right after the match
     * of the pattern, and in case of N-terminal cleavage, the enzyme cuts just
     * before the last residue matching the pattern)  
     *
     * @param aCleavage a String representing the pattern for the cleavage site.
     *
     * @throws PatternSyntaxException if the provided string could not be compiled as regular expression.
     */
    public void setCleavage(String aCleavage) {
        // compile a cleavage Pattern from the specified String
        iCleavagePattern = Pattern.compile(aCleavage, Pattern.CASE_INSENSITIVE);
    }

    public void setCleavage(char[] aCleavage) {
        this.setCleavage( new String(aCleavage) );
    }

    public char[] getCleavage() {
        return iCleavagePattern.pattern().toCharArray();
    }

    public String toString() {
        return this.toString("");
    }

    public String toString(String aPrepend) {
        StringBuffer result = new StringBuffer("\n" + aPrepend + "Hi, I'm the RegExEnzyme '" + this.getTitle() + "'.\n");

        result.append(aPrepend).append("I cleave at the match to the regular expression: ");
        result.append( iCleavagePattern.pattern()).append("\n");
        if(this.getRestrict() != null && this.getRestrict().length > 0) {
            result.append(aPrepend).append("My activity is restricted by these residus: '");
            result.append(new String(this.getRestrict())).append("'.\n");
        } else {
            result.append(aPrepend).append("There are no residus that restrict my activity.\n");
        }
        result.append(aPrepend).append("My position is '");
        result.append((this.getPosition() == Enzyme.CTERM) ? "C-terminal" : "N-terminal").append("'.\n");
        result.append(aPrepend).append("I currently allow ");
        result.append((this.getMiscleavages() == 0) ? "no" : "up to " + this.getMiscleavages());
        result.append(" missed cleavage").append((this.getMiscleavages() == 1) ? "" : "s").append(".\n");

        return result.toString();
    }

    public Object clone() {
        RegExEnzyme ree = (RegExEnzyme)super.clone();
        if(ree != null) {
            // not be necessary to clone the pattern, because Pattern are immutable
            ree.iCleavagePattern = this.iCleavagePattern;
        }
        return ree;
    }

    public int isEnzymaticProduct(String aParentSequence, String aSubSequence) {
        int start = aParentSequence.indexOf(aSubSequence);
        if ( start < 0 ) {
            throw new IllegalArgumentException("Subsequence is not a subsequence of the parent!");
        }
        // now we know that the specified sequence is indeed a subsequence of the parent
        // we have to find out whether it is a enzymatic product, e.g. if the sequence
        // before/after is cleavage site without restriction

        int end = start + aSubSequence.length();
        // call isEnzymaticProduct method with corrected start index (first position = 1 instead of 0)
        return this.isEnzymaticProduct(aParentSequence, start+1, end);
    }

    public int isEnzymaticProduct(String aParentSequence, int aStart, int aEnd) {
        int result; // place holder that is going to hold the result value

        // Check validity of parameters.
        if( (aStart-1 < 0) ||  (aEnd-1 < 0) ) {
            throw new IllegalArgumentException("Subsequence is not a subsequence of the parent!");
        }
        if(aEnd-1 > aParentSequence.length()-1) {
            throw new IllegalArgumentException("Subsequence end index out of parent length range (" + aEnd + ">" + (aParentSequence.length()-1) + ")!");
        }
        if(aStart > aEnd) {
            throw new IllegalArgumentException("Subsequence could not be retreived since start index is greater than end index (" + aStart + ">=" + aEnd + ")!");
        }

        // create a matcher that will find cleavage sites
        Matcher matcher = iCleavagePattern.matcher(aParentSequence);

        // get all cleavage pattern endpoints (positions where the enzyme cleaves)
        // distinguish between C-terminal (pattern end) and N-terminal (pattern end - 1) cleavage
        // also take into account the restriction sites (when we do NOT cleave although it is a cleavage site)
        int end = 0;
        List ends = new ArrayList();
        // find the pattern matching sites
        while (matcher.find() && end <= aEnd) {
            int tmp = matcher.end(); // end of pattern match (site of cleaving)
            // check we are not at the end of the sequence and that we are not at a restricted cleavage site
            if ( tmp < aParentSequence.length() && iRestrictors.containsKey(Character.valueOf(aParentSequence.charAt(tmp)) ) ) {
                // resticted, do not cleave here
            } else { // remember the cleavage position
                // we are at a valid cleavage site, now we have to distinguish between N-terminal and C-terminal cleavage
                if (iPosition == Enzyme.CTERM) {
                    end = tmp;
                } else if (iPosition == Enzyme.NTERM) {
                    end = tmp - 1;
                } else {
                    throw new IllegalStateException("Cleavage position is not specified correctly! Can not determine if the proviced peptide is a valid enzymatic product!");
                }
                ends.add(Integer.valueOf(end));
            }
        }

        // check N-terminal side of peptide: the start of the peptide -1 = the position of a cleavage site
        // check C-terminal side of peptide: the end of the peptide = the position of a cleavage site
        // if both are true we have a fully enzymatic peptide
        if ( ends.contains(Integer.valueOf(aStart-1)) || (aStart == 1) ) {
            // if the start -1 of the peptide is the end of a cleavage site or it is the start of the entire sequence,
            // then we have already a N-terminal peptide. If it also ends at a cleavage site, then it is fully enzymatic
            if ( ends.contains(Integer.valueOf(aEnd)) ) {
                result = Enzyme.FULLY_ENZYMATIC;
            } else {
                result = Enzyme.N_TERM_ENZYMATIC;
            }
        } else if ( ends.contains(Integer.valueOf(aEnd)) || (aEnd == aParentSequence.length()) ) {
            // if the end of the peptide is the end of a cleavage site or the peptide ends at the end of the entire
            //  sequence and since we know it did not start at a cleavage site, it can only be a C-terminal peptide
            result = Enzyme.C_TERM_ENZYMATIC;
        } else {
            result = Enzyme.ENTIRELY_NOT_ENZYMATIC;
        }

        return result;
    }

    public Protein[] cleave(Protein aProtein) {
        // // // // // // // // // // // // // // // // // // // //
        // digest the protein sequence into peptides and record the start/end positions
        List peptides = new ArrayList(); // the list that will hold the peptides we generate (Strings)
        List startIndices = new ArrayList(); // keep track of the start positions of the peptides (Integer)
        List endIndices = new ArrayList(); // keep track of the stop positions of the peptides (Integer)

        // !Attention! the order of the next methods is important and shoud not be changed/interrupted
        // without checking the correctness of the cleaving algorythm!
        cleave(aProtein, peptides, startIndices, endIndices);
        handleTruncations(aProtein, peptides, startIndices, endIndices);
        addMisCleaved(peptides, startIndices, endIndices);
        removeStopCodonPeptides(peptides, startIndices, endIndices);

        // // // // // // // // // // // // // // // // // // // //
        // now we have the peptides (taking miscleavages into account), lets create protein objects from them
        Header header = aProtein.getHeader();
        Protein[] result = new Protein[peptides.size()];
        Iterator iterator = peptides.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String peptide = (String) iterator.next();
            Header h = (Header)header.clone();
            h.setLocation(((Integer)startIndices.get(i)).intValue()+1, ((Integer)endIndices.get(i)).intValue());
            result[i] = new Protein( h, new AASequenceImpl(peptide) );
            i++;
        }

        return result;
    }

    /**
     * This method cleans up peptides containing a stop codon "_". The corresponding
     * lists of start and end indices are updated accordingly.
     *
     * @param peptides the list of peptides to scan for "_" containing peptides.
     * @param startIndices the corresponfing list of start indices.
     * @param endIndices the corresponfing list of end indices.
     */
    private void removeStopCodonPeptides(List peptides, List startIndices, List endIndices) {
        Iterator pepIter = peptides.iterator();
        Iterator startIter = startIndices.iterator();
        Iterator endIter = endIndices.iterator();
        while (pepIter.hasNext()) {
            String peptide = (String) pepIter.next();
            // keep iterators in sync
            startIter.next();
            endIter.next();
            // check for stop codon
            if (peptide.indexOf("_") >= 0) {
                // peptide contains a stop codon and has to be removed
                pepIter.remove();
                // also remove the corresponding start and end positions
                startIter.remove();
                endIter.remove();
            }
        }
    }

    /**
     * This method performs a simple digest following the pattern of the enzyme.
     *
     * @param aProtein the protein to cleave.
     * @param peptides the list that is to hold the peptides.
     * @param startIndices the list that is to hold the corresponding start indices for the peptides.
     * @param endIndices the list that is to hold the corresponding start indices for the peptides.
     */
    private void cleave(Protein aProtein, List peptides, List startIndices, List endIndices) {
        String sequence = aProtein.getSequence().getSequence();

        // Check for a header that contains locations and if we have to adjust the positions we calculate while cleaving
        int headerStart = aProtein.getHeader().getStartLocation()-1;
        if(headerStart < 0) {
            headerStart = 0;
        }

        int startPos = 0; // the start position of the current peptide
        int currEnd;  // the end position of the current peptide
        char after; // the amino acid after the cleavage site (where we have to check for restrictions)
        Matcher matcher = iCleavagePattern.matcher(sequence);

        while (matcher.find()) { // get the next match
            currEnd = matcher.end();
            // check if the cleavage site is at the end of the sequence string
            if (currEnd >= sequence.length()) {
                // the cleaveage pattern is right to the end of the sequence
                // we don't need to cleave, since there is nothing left after the cleavage site
            } else {
                // we are not at the end of the sequence, so we record the amino acid after the cleavage site
                after = sequence.charAt(currEnd); // character AFTER cleavage site
                // if the first character after the cleavage site is a restrictor, we do not cleave!
                // only if the next amino acid is not a restricting site, we cleave
                if ( iRestrict == null || (Arrays.binarySearch(iRestrict, after) < 0) ) {
                    // not restricted! we cleave
                    // but first we have to check at which position Cterm or Nterm
                    if ( iPosition == Enzyme.CTERM ) {
                        String pep = sequence.substring(startPos, currEnd);
                        peptides.add( pep );
                        startIndices.add(Integer.valueOf(headerStart + startPos) );
                        endIndices.add( Integer.valueOf(headerStart + startPos + pep.length()) );
                        startPos = currEnd; // update the start position of the peptide only if we have cleaved
                    } else if ( iPosition == Enzyme.NTERM ) {
                        String pep = sequence.substring(startPos, currEnd-1);
                        peptides.add( pep );
                        startIndices.add(Integer.valueOf(headerStart + startPos) );
                        endIndices.add(Integer.valueOf(headerStart + startPos + pep.length()) );
                        startPos = currEnd-1; // update the start position of the peptide only if we have cleaved
                    } else {
                        // should be covered by the constructor (does not allow differnt values form CTERM/NTERM
                        throw new IllegalStateException("Illegal position (neiter c terminal nor n terminal) while trying to cleave!");
                    }

                } else {
                    // restricted, we do NOT cleave
                }
            }
        }
        // now add the rest of the sequence (everything after the last match)
        String lastPep = sequence.substring(startPos, sequence.length());
        peptides.add(lastPep);
        startIndices.add( Integer.valueOf(headerStart + startPos) );
        endIndices.add( Integer.valueOf((headerStart + startPos + lastPep.length())) );
    }

    /**
     * This method checks if the isTruncated flag of the protein is set and if so, it will
     * assume that the first (in case of C-terminal truncation) or last (in case of N-terminal
     * truncation) peptide are artifacts of that truncation and not real peptides. Accordingly
     * the first or last peptide will be removed from the peptide list and start and end positions
     * will be updated.
     *
     * @param aProtein the protein to check (which may has been truncated).
     * @param peptides the list of peptides derived from that protein through normal cleavage.
     * @param startIndices the list of corresponding start indices for the peptides.
     * @param endIndices the list of corresponding end indices for the peptides.
     */
    private void handleTruncations(Protein aProtein, List peptides, List startIndices, List endIndices) {
        // check weather the protein was truncated and remove the according peptide
        // (C-term truncation -> first peptide, N-term truncation -> last peptide)
        if (aProtein.isTruncated()) {
            if (aProtein.getTruncationPosition() == Protein.CTERMTRUNC) {
                // C-terminal truncated protein, we get rid of the FIRST peptide, since
                // it is most likely an artefact of the truncation and not a real peptide
                peptides.remove(0);
                startIndices.remove(0);
                endIndices.remove(0);
            } else if (aProtein.getTruncationPosition() == Protein.NTERMTRUNC ) {
                // N-terminal truncated protein, we get rid of the LAST peptide, since
                // it is most likely an artefact of the truncation and not a real peptide
                int last = peptides.size() - 1;
                peptides.remove(last);
                startIndices.remove(last);
                endIndices.remove(last);
            } else {
                throw new IllegalArgumentException("Truncation position is expected to be " +
                        "either 'Protein.CTERMTRUNC' or 'Protein.NTERMTRUNC'! Protein: " +
                        aProtein.getHeader().getFullHeaderWithAddenda());
            }
        }
    }

    /**
     * Method that computes the peptides if miscleavages were allowed.
     *
     * @param peptides the list of peptides without miscleavages.
     * @param startIndices the list of corresponding start indices for the peptides.
     * @param endIndices the list of corresponding end indices for the peptides.
     */
    private void addMisCleaved(List peptides, List startIndices, List endIndices) {
        // // // // // // // // // // // // // // // // // // // //
        // Allright, now we should have all the individual peptides.
        // Now we should take into account the specified number of miscleavages.

        // Get all the sequences up to now.
        String[] imSequences = (String[]) peptides.toArray(new String[peptides.size()]);

        // Cycle the current sequences.
        for(int j=0;j<imSequences.length;j++) {
            String temp = imSequences[j];
            // Apply the number of allowed missed cleavages sequentially from
            // this sequence.
            for (int k=0;k<this.iMiscleavages;k++) {
                // If we fall outside of the range of current sequences
                // (for instance if we try to apply a second allowed missed
                //  cleavage to the penultimate peptide, we fall outside of
                //  the available peptides!)
                // we break the loop.
                if((j+k+1) >= imSequences.length) {
                    break;
                }

                // Add our constructed sequence.
                temp += imSequences[j+k+1];
                peptides.add(temp);
                startIndices.add(startIndices.get(j));
                endIndices.add(endIndices.get(j+k+1));
            }
        }
    }
}
