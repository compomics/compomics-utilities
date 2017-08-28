/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 7-jan-03
 * Time: 11:08:24
 */
package com.compomics.util.nucleotide;
import org.apache.log4j.Logger;

import com.compomics.util.experiment.io.biology.protein.Header;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Protein;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class combines a protein Header with a nucleotide sequence.
 * Header is in Protein package for historical reasons.
 *
 * @author Lennart Martens
 */
public class NucleotideSequence {

    // Class specific log4j logger for NucleotideSequence instances.
    Logger logger = Logger.getLogger(NucleotideSequence.class);

    /**
     * This variable contains the nucleotide sequence.
     */
    private NucleotideSequenceImpl iSequence = null;

    /**
     * This variable contains the header for the sequence.
     */
    private Header iHeader = null;

    /**
     * This constructor requires a NucleotideSequenceImpl as argument.
     * THis will be the sequence around which the nucleotide will be built.
     *
     * @param   aSequence   NucleotideSequenceImpl around which this NucleotideSequence will be built.
     */
    public NucleotideSequence(NucleotideSequenceImpl aSequence) {
        this(null, aSequence);
    }

    /**
     * This constructor allows the passing of a Header, as well as an
     * AASequenceImpl for this Protein.
     *
     * @param   aHeader  Header with the header information for this NucleotideSequence.
     * @param   aSequence   NucleotideSequenceImpl with the sequence for this NucleotideSequence.
     */
    public NucleotideSequence(Header aHeader, NucleotideSequenceImpl aSequence) {
        this.iHeader = aHeader;
        this.iSequence = aSequence;
    }

    /**
     * This constructor allows for the construction of a NucleotideSequence instance
     * by passing a FASTA entry.
     *
     * @param   aFASTAString String with the FASTA representation of the NucleotideSequence.
     */
    public NucleotideSequence(String aFASTAString) {
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
            this.iSequence = new NucleotideSequenceImpl(lSB.toString());
        } catch(IOException ioe) {
            // We certainly do NONT expect an IOException...
            throw new IllegalArgumentException("Unable to process your FASTA String ('" + aFASTAString + "'). IOException: " + ioe.getMessage() + ".");
        }
    }

    /**
     * This constructor allows for the construction of a NucleotideSequence instance through the
     * passing of a NucleotideSequence String and a Sequence String. This is mainly useful to obtain
     * a NucleotideSequence instance without a Header.
     *
     * @param   aHeader String with the header (can be 'null').
     * @param   aSequence   String with the sequence.
     */
    public NucleotideSequence(String aHeader, String aSequence) {
        this.iHeader = Header.parseFromFASTA(aHeader);
        this.iSequence = new NucleotideSequenceImpl(aSequence);
    }

    /**
     * This method reports on the nucleotide sequence.
     *
     * @return  NucleotideSequenceImpl  with the sequence.
     */
    public NucleotideSequenceImpl getSequence() {
        return iSequence;
    }

    /**
     * This method allows the setting of a sequence.
     *
     * @param   aSequence   NucleotideSequenceImpl with the sequence.
     */
    public void setSequence(NucleotideSequenceImpl aSequence) {
        iSequence = aSequence;
    }

    /**
     * This method reports on the nucleotide header.
     *
     * @return  Header  with the header.
     */
    public Header getHeader() {
        return iHeader;
    }

    /**
     * This method sets the header.
     * 
     * @param aHeader the header
     */
    public void setHeader(Header aHeader) {
        iHeader = aHeader;
    }

    /**
     * This method reports on the length of the sequence for the current nucleotide sequence.
     *
     * @return  long    with the length of the sequence for the current nucleotide sequence.
     */
    public long getLength() {
        return this.getSequence().getLength();
    }

    /**
     * This method returns the nucleotide sequence weight in Da.
     *
     * @return  double with the mass of the nucleotide sequence in Da.
     */
    public double getMass() {
        return this.getSequence().getMass();
    }

    /**
     * This method can be used to append this nucleotide sequence to the
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
            for(int i=0;;i++) {
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
     * This method translates the nucleotide sequence in six reading frames.
     *
     * @return  Protein[]   with at most the six reading frames.
     */
    public Protein[] translate() {
        // Translate.
        AASequenceImpl[] translated = this.iSequence.translate();

        // Create return array.
        Protein[] result = new Protein[translated.length];

        // Set header.
        for(int i=0;i<translated.length;i++) {
            Header protHeader = (Header)this.getHeader().clone();
            if(protHeader.getAccession() != null) {
                protHeader.setAccession(protHeader.getAccession() + "_(RF " + (i+1) + ")");
            } else {
                protHeader.setRest(protHeader.getRest() + "_(RF " + (i+1) + ")");
            }
            result[i] = new Protein(protHeader, translated[i]);
        }

        return result;
    }
}
