/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 7-okt-02
 * Time: 10:26:23
 */
package com.compomics.util.protein;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements the behaviour for a Protein instance.
 * A lot of functionality is borrowed from the AASequenceImpl class.
 * The internal representation corresponds most closely to a FASTA
 * entry.
 *
 * @author Lennart Martens
 * @see com.compomics.util.protein.AASequenceImpl
 */
public class Protein {

    /**
     * The sequence is a very important element in a Protein instance.
     */
    private AASequenceImpl iSequence = null;

    /**
     * The header for this Protein.
     */
    private Header iHeader = null;

    /**
     * This flag indicates whether the protein has been truncated at
     * any point. This is important when considering enzymatic cleavage
     * of these proteins, since their C-terminal part no longer
     * makes sense - it has been blunted by the truncation process!
     */
    private boolean iTruncated = false;

    /**
     * This int will indicate at which position a protein has been truncated.
     * This code is only meaningful when the 'isTruncated()' method returns 'true'.
     */
    private int iTruncationPosition = 0;

    /**
     * The code for an N-terminal truncation.
     * These variables can be compared to the result of the
     * 'getTruncationPosition()' method, BUT ONLY when the
     * 'isTruncated()' method returns true.
     */
    public static final int NTERMTRUNC = 1;

    /**
     * The code for an C-terminal truncation.
     * These variables can be compared to the result of the
     * 'getTruncationPosition()' method, BUT ONLY when the
     * 'isTruncated()' method returns true.
     */
    public static final int CTERMTRUNC = 2;


    /**
     * This constructor requires an AASequenceImpl as argument.
     * THis will be the sequence around which the protein will be built.
     *
     * @param   aSequence   AASequenceImpl around which this protein will be built.
     */
    public Protein(AASequenceImpl aSequence) {
        this(null, aSequence);
    }

    /**
     * This constructor allows the passing of a Header, as well as an
     * AASequenceImpl for this Protein.
     *
     * @param   aHeader  Header with the header information for this Protein.
     * @param   aSequence   AASequenceImpl with the sequence for this Protein.
     */
    public Protein(Header aHeader, AASequenceImpl aSequence) {
        this(aHeader, aSequence, false, 0);
    }

    /**
     * This constructor allows the passing of a Header, as well as an
     * AASequenceImpl for this Protein. It also allows for the specification
     * of the 'truncatedness' of the Protein.
     *
     * @param   aHeader  Header with the header information for this Protein.
     * @param   aSequence   AASequenceImpl with the sequence for this Protein.
     * @param   aTruncated  boolean that indicates whether this Protein has been truncated.
     * @param   aTruncationPosition int with the coded position for the truncation (N-Term or C-Term).
     */
    public Protein(Header aHeader, AASequenceImpl aSequence, boolean aTruncated, int aTruncationPosition) {
        this.iSequence = aSequence;
        this.iHeader = aHeader;
        this.iTruncated = aTruncated;
        this.iTruncationPosition = aTruncationPosition;
    }

    /**
     * This constructor allows for the construction of a Protein instance
     * by passing a FASTA entry.
     *
     * @param   aFASTAString String with the FASTA representation of the Protein.
     */
    public Protein(String aFASTAString) {
         this(aFASTAString, false, 0);
    }

    /**
     * This constructor allows for the construction of a Protein instance
     * by passing a FASTA entry and boolean flag for truncation.
     *
     * @param   aFASTAString String with the FASTA representation of the Protein.
     * @param   aTruncated  boolean that indicates whether this Protein has been truncated.
     * @param   aTruncationPosition int with the coded position for the truncation (N-Term or C-Term).
     */
    public Protein(String aFASTAString, boolean aTruncated, int aTruncationPosition) {
        try {
            // Parse the FASTA entry.
            BufferedReader br = new BufferedReader(new StringReader(aFASTAString));

            // First line is the header.
            this.iHeader = Header.parseFromFASTA(br.readLine());

            // Next, read the remaining lines that make up the sequence.
            StringBuffer lSB = new StringBuffer();
            String line = null;
            while((line = br.readLine()) != null) {
                lSB.append(line);
            }

            // Initialize the sequence.
            this.iSequence = new AASequenceImpl(lSB.toString());
            // Initialize truncation.
            this. iTruncated = aTruncated;
            this.iTruncationPosition = aTruncationPosition;
        } catch(IOException ioe) {
            // We certainly do NONT expect an IOException...
            throw new IllegalArgumentException("Unable to process your FASTA String ('" + aFASTAString + "'). IOException: " + ioe.getMessage() + ".");
        }
    }

    /**
     * This constructor allows for the construction of a Protein instance through the
     * passing of a Header String and a Sequence String. This is mainly useful to obtain
     * a Protein instance without a Header.
     *
     * @param   aHeader String with the header (can be 'null').
     * @param   aSequence   String with the sequence.
     */
    public Protein(String aHeader, String aSequence) {
        this(aHeader, aSequence, false, 0);
    }

    /**
     * This constructor allows for the construction of a Protein instance through the
     * passing of a Header String and a Sequence String. This is mainly useful to obtain
     * a Protein instance without a Header.
     *
     * @param   aHeader String with the header (can be 'null').
     * @param   aSequence   String with the sequence.
     * @param   aTruncated  boolean that indicates whether this Protein has been truncated.
     * @param   aTruncationPosition int with the coded position for the truncation (N-Term or C-Term).
     */
    public Protein(String aHeader, String aSequence, boolean aTruncated, int aTruncationPosition) {
        this.iHeader = Header.parseFromFASTA(aHeader);
        this.iSequence = new AASequenceImpl(aSequence);
        this.iTruncated = aTruncated;
        this.iTruncationPosition = aTruncationPosition;
    }

    /**
     * This method reports on the header for the current
     * protein.
     *
     * @return  Header  with the current header for this protein.
     */
    public Header getHeader() {
        return this.iHeader;
    }

    /**
     * This method reports on the sequence for the current
     * protein.
     *
     * @return  AASequenceImpl  with the current sequence for this protein.
     */
    public AASequenceImpl getSequence() {
        return this.iSequence;
    }

    /**
     * This method truncates the sequence for this protein on the N-terminus
     * to the requested size.
     *
     * @param   aSize   int with the size of the resulting N-terminal sequence
     * @return  Protein with an N-terminal truncated sequence.
     */
    public Protein getNTermTruncatedProtein(int aSize) {
        // First get the current sequence.
        AASequenceImpl sequence = this.getSequence();
        // Extract start and end locations.

        // Start is 1 (human readable!), unless a startlocation is specified for
        // the current protein, in which case the current startlocation is kept.
        int start = this.getHeader().getStartLocation();
        if(start < 0) {
            start = 1;
        }
        // The endlocation is (start + truncated length).
        // Where truncation length is the
        sequence = sequence.getNTermTruncatedSequence(aSize);
        int end = start + sequence.getLength() - 1;

        // Get a copy of the header and set the location.
        Header header = (Header)this.getHeader().clone();
        header.setLocation(start, end);

        // See if we should flag the truncatedness.
        boolean flag = false;
        if(this.getLength() > aSize) {
            flag = true;
        }

        // Return the newly constructed Protein.
        return new Protein(header, sequence, flag, Protein.NTERMTRUNC);
    }

    /**
     * This method truncates the sequence for this protein on the C-terminus
     * to the requested size.
     *
     * @param   aSize   int with the size of the resulting C-terminal sequence
     * @return  Protein with an C-terminal truncated sequence.
     */
    public Protein getCTermTruncatedProtein(int aSize) {
        // First get the current sequence.
        AASequenceImpl sequence = this.getSequence();
        // Extract start and end locations.
        // End location is the current endlocation, or, if there isn't any,
        // the length of the sequence.
        int end = this.getHeader().getEndLocation();
        if(end < 0) {
            end = sequence.getLength();
        }
        // Start is (last residu minus truncated size).
        sequence = sequence.getCTermTruncatedSequence(aSize);
        int start = end - sequence.getLength() + 1;
        // get a copy of the header and set the location.
        Header header = (Header)this.getHeader().clone();
        header.setLocation(start, end);

        // See if we should flag the truncatedness.
        boolean flag = false;
        if(this.getLength() > aSize) {
            flag = true;
        }

        // Return the newly constructed Protein.
        return new Protein(header, sequence, flag, Protein.CTERMTRUNC);
    }

    /**
     * This method reports on the length of the sequence for the current protein.
     *
     * @return  long    with the length of the sequence for the current protein.
     */
    public long getLength() {
        return this.getSequence().getLength();
    }

    /**
     * This method returns the protein weight in Da.
     *
     * @return  double with the mass of the Protein in Da.
     */
    public double getMass() {
        return this.getSequence().getMass();
    }

    /**
     * Simple setter for the header.
     *
     * @param   aHeader the Header to set for this protein.
     */
    public void setHeader(Header aHeader) {
        this.iHeader = aHeader;
    }

    /**
     * Simple setter for the sequence.
     *
     * @param   aSequence   the AASequenceImpl with the
     *                      sequence to set for this protein.
     */
    public void setSequence(AASequenceImpl aSequence) {
        this.iSequence = aSequence;
    }

    /**
     * This method can be used to append this protein to the
     * FASTA DB flatfile the PrintWriter points to.
     *
     * @param   aOut    PrintWriter to write the file to.
     * @exception   IOException when the writing failed.
     */
    public void writeToFASTAFile(PrintWriter aOut) throws IOException {
        aOut.println(this.getHeader().getAbbreviatedFASTAHeaderWithAddenda());
        StringBuffer sequence = new StringBuffer(this.getSequence().getSequence());

        // Next we want to ensure only 60 characters are present on each line.
        // So at every 59th character, insert and endline.
        // First of all, see if the sequence is long enough!
        if(sequence.length()>59) {
            int offset = 58;
            while(true) {
                // Insert endline.
                sequence.insert(offset, "\n");
                // See if we're not overextending our reach here.
                offset += 59;
                if(offset > sequence.length()) {
                    break;
                }
            }
        }
        aOut.println(sequence.toString());
    }

    /**
     * This method reports on the 'truncatedness' of the protein.
     *
     * @return  boolean whether this protein is the result of a truncation.
     */
    public boolean isTruncated() {
        return this.iTruncated;
    }

    /**
     * This method reports on the position of the truncation.
     * Note that the method can only be trusted when the 'isTruncated()'
     * method returns 'true'.
     *
     * @return  int with the code for the position (either N-term or C-term).
     *              This return code can be evaluated against the constants defined on this class.
     */
    public int getTruncationPosition() {
        return this.iTruncationPosition;
    }

    /**
     * This method will check equality between this object
     * and another Protein instance.
     */
    public boolean equals(Object o) {
        boolean result = false;
        if(o instanceof Protein) {
            Protein p = (Protein)o;
            if((p.iHeader.getFullHeaderWithAddenda().equals(this.iHeader.getFullHeaderWithAddenda())) && (p.iSequence.getModifiedSequence().equals(this.iSequence.getModifiedSequence())) && (p.iTruncated == this.iTruncated) && (p.iTruncationPosition == this.iTruncationPosition)) {
                result = true;
            }
        }

        return result;
    }
}
