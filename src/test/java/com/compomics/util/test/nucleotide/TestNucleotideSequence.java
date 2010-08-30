/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 8-okt-02
 * Time: 15:28:41
 */
package com.compomics.util.test.nucleotide;
import org.apache.log4j.Logger;

import junit.framework.*;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Protein;
import com.compomics.util.nucleotide.NucleotideSequenceImpl;
import com.compomics.util.nucleotide.NucleotideSequence;

import java.io.*;

import junit.TestCaseLM;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class implements the test scenario for the NucleotideSequence class.
 *
 * @author Lennart Martens
 * @see com.compomics.util.nucleotide.NucleotideSequence
 */
public class TestNucleotideSequence extends TestCaseLM {

    // Class specific log4j logger for TestNucleotideSequence instances.
    Logger logger = Logger.getLogger(TestNucleotideSequence.class);

    public TestNucleotideSequence() {
        this("The test scenario for the NucleotideSequence class.");
    }

    public TestNucleotideSequence(String aName) {
        super(aName);
    }

    /**
     * This method test the different ways of creating a Protein instance.
     */
    public void testConstruction() {
        final String sequence = "AGCTAGCTAGCTAGCTAG";
        final String header = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String fasta = header + "\n" + sequence;
        final Header head = Header.parseFromFASTA(header);
        final NucleotideSequenceImpl seq = new NucleotideSequenceImpl(sequence);

        // First without header, just a sequence.
        NucleotideSequence p = new NucleotideSequence(seq);
        Assert.assertTrue(p.getHeader() == null);
        Assert.assertEquals(seq, p.getSequence());

        p = new NucleotideSequence(null, seq);
        Assert.assertTrue(p.getHeader() == null);
        Assert.assertEquals(seq, p.getSequence());

        p = new NucleotideSequence(null, sequence);
        Assert.assertTrue(p.getHeader() == null);
        Assert.assertEquals(sequence, p.getSequence().getSequence());

        // Now with a Header.
        p = new NucleotideSequence(head, seq);
        Assert.assertEquals(head, p.getHeader());
        Assert.assertEquals(seq, p.getSequence());

        p = new NucleotideSequence(header, sequence);
        Assert.assertEquals(header, p.getHeader().toString());
        Assert.assertEquals(sequence, p.getSequence().getSequence());

        p = new NucleotideSequence(fasta);
        Assert.assertEquals(header, p.getHeader().toString());
        Assert.assertEquals(sequence, p.getSequence().getSequence());
    }

    /**
     * This method test the reporting by the NucleotideSequence about its length.
     */
    public void testLength() {
        NucleotideSequence p = new NucleotideSequence(null, "AGCTAGCTAGCTAGCTAG");
        Assert.assertTrue(p.getLength() == p.getSequence().getLength());
    }

    /**
     * This method test the reporting by the NucleotideSequence about its mass.
     */
    public void testMass() {
        NucleotideSequence p = new NucleotideSequence(null, "AGCTAGCTAGCTAGCTAG");
        Assert.assertEquals(p.getMass(), p.getSequence().getMass(), 1e-15);
    }

    /**
     * This method test the FASTA printing behaviour.
     */
    public void testPrintToFASTAFile() {
        final String inputFile = "fastaNucleotideFile.fas";
        final String input = super.getFullFilePath(inputFile).replace("%20", " ");
        try {
            NucleotideSequence p1 = new NucleotideSequence(">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting NucleotideSequence-1) (Zwint-1).\nAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAG");
            NucleotideSequence p2 = new NucleotideSequence(">sw|O95230|ZWIN_HUMAN ZW10 interactor (ZW10 interacting NucleotideSequence-2) (Zwint-2).\nAGCTAGCTAGCTAGCTAGAGCTAGCTAGCTAGCTAG");
            PipedReader pr = new PipedReader();
            PrintWriter out = new PrintWriter(new PipedWriter(pr));
            p1.writeToFASTAFile(out);
            p2.writeToFASTAFile(out);
            out.flush();
            out.close();
            BufferedReader br = new BufferedReader(pr);
            BufferedReader br2 = new BufferedReader(new FileReader(input));
            String line1 = null;
            String line2 = null;
            while(((line1 = br.readLine()) != null) & ((line2 = br2.readLine()) != null)) {
                Assert.assertEquals(line1, line2);
            }

            // Both should have read an equal amount of lines.
            Assert.assertTrue(line1 == null);
            Assert.assertTrue(line2 == null);

            br.close();
            br2.close();
        } catch(IOException ioe) {
            fail("IOException while testing the writeToFASTAFile method: " + ioe.getMessage() + ".");
        }
    }

    /**
     * This method test the translation of nucleotide sequences.
     */
    public void testTranslation() {
        NucleotideSequence ns = new NucleotideSequence(">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting NucleotideSequence-1) (Zwint-1).\nGATTACATTACAGAGAGAGAGGGGATAT");
        Protein[] proteins = ns.translate();

        Assert.assertEquals("DYITEREGI", proteins[0].getSequence().getSequence());
        Assert.assertEquals("ITLQRERGY", proteins[1].getSequence().getSequence());
        Assert.assertEquals("LHYRERGD", proteins[2].getSequence().getSequence());
        Assert.assertEquals("ISPLSL_CN", proteins[3].getSequence().getSequence());
        Assert.assertEquals("YPLSLCNVI", proteins[4].getSequence().getSequence());
        Assert.assertEquals("IPSLSVM_", proteins[5].getSequence().getSequence());

        String originalAccession = ns.getHeader().getAccession();
        for(int i=0;i<6;i++) {
            Assert.assertEquals(originalAccession + "_(RF " + (i+1) + ")", proteins[i].getHeader().getAccession());
        }
    }
}
