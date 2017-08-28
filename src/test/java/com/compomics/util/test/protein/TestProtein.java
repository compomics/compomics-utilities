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
package com.compomics.util.test.protein;

import com.compomics.util.junit.TestCaseLM;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.experiment.io.biology.protein.Header;
import com.compomics.util.protein.Protein;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class implements the test scenario for the Protein class.
 *
 * @author Lennart Martens
 * @see com.compomics.util.protein.Protein
 */
public class TestProtein extends TestCase {

    // Class specific log4j logger for TestProtein instances.
    Logger logger = Logger.getLogger(TestProtein.class);

    public TestProtein() {
        this("The test scenario for the Protein class.");
    }

    public TestProtein(String aName) {
        super(aName);
    }

    /**
     * This method test the different ways of creating a Protein instance.
     */
    public void testConstruction() {
        final String sequence = "LENNARTMARTENS";
        final String header = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String fasta = header + "\n" + sequence;
        final Header head = Header.parseFromFASTA(header);
        final AASequenceImpl seq = new AASequenceImpl(sequence);

        // First without header, just a sequence.
        Protein p = new Protein(seq);
        Assert.assertTrue(p.getHeader() == null);
        Assert.assertEquals(seq, p.getSequence());

        p = new Protein(null, seq);
        Assert.assertTrue(p.getHeader() == null);
        Assert.assertEquals(seq, p.getSequence());

        p = new Protein(null, sequence);
        Assert.assertTrue(p.getHeader() == null);
        Assert.assertEquals(sequence, p.getSequence().getSequence());

        // Now with a Header.
        p = new Protein(head, seq);
        Assert.assertEquals(head, p.getHeader());
        Assert.assertEquals(seq, p.getSequence());

        p = new Protein(header, sequence);
        Assert.assertEquals(header, p.getHeader().toString());
        Assert.assertEquals(sequence, p.getSequence().getSequence());

        p = new Protein(fasta);
        Assert.assertEquals(header, p.getHeader().toString());
        Assert.assertEquals(sequence, p.getSequence().getSequence());

        // Now with header and truncation flag.
        p = new Protein(head, seq, false, 0);
        Assert.assertEquals(head, p.getHeader());
        Assert.assertEquals(seq, p.getSequence());
        Assert.assertFalse(p.isTruncated());

        p = new Protein(header, sequence, true, Protein.NTERMTRUNC);
        Assert.assertEquals(header, p.getHeader().toString());
        Assert.assertEquals(sequence, p.getSequence().getSequence());
        Assert.assertTrue(p.isTruncated());
        Assert.assertEquals(Protein.NTERMTRUNC, p.getTruncationPosition());

        p = new Protein(fasta, false, 0);
        Assert.assertEquals(header, p.getHeader().toString());
        Assert.assertEquals(sequence, p.getSequence().getSequence());
        Assert.assertFalse(p.isTruncated());
    }

    /**
     * This method test the N-terminal truncation of a protein.
     */
    public void testNtermTruncation() {
        final String sequence = "LENNARTMARTENS";
        final String header = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String fasta = header + "\n" + sequence;
        final Header testHeader = Header.parseFromFASTA(header);

        // Create Protein instance.
        Protein p = new Protein(fasta);
        Assert.assertFalse(p.isTruncated());

        // Truncate to 10 N-terminal residus.
        p = p.getNTermTruncatedProtein(10);
        testHeader.setLocation(1, 10);
        Assert.assertTrue(p.isTruncated());
        Assert.assertEquals(Protein.NTERMTRUNC, p.getTruncationPosition());
        Assert.assertEquals(testHeader.toString(), p.getHeader().toString());
        Assert.assertEquals(sequence.substring(0,10), p.getSequence().getSequence());
        Assert.assertEquals(10, p.getSequence().getLength());
        Assert.assertEquals(1, p.getHeader().getStartLocation());
        Assert.assertEquals(10, p.getHeader().getEndLocation());

        // Truncate to same size.
        p = p.getNTermTruncatedProtein(10);
        Assert.assertFalse(p.isTruncated());
        Assert.assertEquals(testHeader.toString(), p.getHeader().toString());
        Assert.assertEquals(sequence.substring(0,10), p.getSequence().getSequence());
        Assert.assertEquals(10, p.getSequence().getLength());
        Assert.assertEquals(1, p.getHeader().getStartLocation());
        Assert.assertEquals(10, p.getHeader().getEndLocation());

        // Truncate to a size that's too large.
        p = p.getNTermTruncatedProtein(100);
        Assert.assertFalse(p.isTruncated());
        Assert.assertEquals(testHeader.toString(), p.getHeader().toString());
        Assert.assertEquals(sequence.substring(0,10), p.getSequence().getSequence());
        Assert.assertEquals(10, p.getSequence().getLength());
        Assert.assertEquals(1, p.getHeader().getStartLocation());
        Assert.assertEquals(10, p.getHeader().getEndLocation());

        // Truncate a Protein with a location set.
        p = new Protein(fasta);
        testHeader.setLocation(100, 109);
        p.getHeader().setLocation(100, 113);
        p = p.getNTermTruncatedProtein(10);
        Assert.assertTrue(p.isTruncated());
        Assert.assertEquals(Protein.NTERMTRUNC, p.getTruncationPosition());
        Assert.assertEquals(testHeader.toString(), p.getHeader().toString());
        Assert.assertEquals(sequence.substring(0,10), p.getSequence().getSequence());
        Assert.assertEquals(10, p.getSequence().getLength());
        Assert.assertEquals(100, p.getHeader().getStartLocation());
        Assert.assertEquals(109, p.getHeader().getEndLocation());
    }

    /**
     * This method test the C-terminal truncation of a protein.
     */
    public void testCtermTruncation() {
        final String sequence = "LENNARTMARTENS";
        final String header = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String fasta = header + "\n" + sequence;
        final Header testHeader = Header.parseFromFASTA(header);

        // Create the Protein instance.
        Protein p = new Protein(fasta);
        Assert.assertFalse(p.isTruncated());

        // Regular truncation.
        p = p.getCTermTruncatedProtein(10);
        testHeader.setLocation(5, 14);
        Assert.assertTrue(p.isTruncated());
        Assert.assertEquals(Protein.CTERMTRUNC, p.getTruncationPosition());
        Assert.assertEquals(testHeader.toString(), p.getHeader().toString());
        Assert.assertEquals(sequence.substring(4,14), p.getSequence().getSequence());
        Assert.assertEquals(10, p.getSequence().getLength());
        Assert.assertEquals(5, p.getHeader().getStartLocation());
        Assert.assertEquals(14, p.getHeader().getEndLocation());

        // Truncate to same size.
        p = p.getCTermTruncatedProtein(10);
        Assert.assertFalse(p.isTruncated());
        Assert.assertEquals(testHeader.toString(), p.getHeader().toString());
        Assert.assertEquals(sequence.substring(4,14), p.getSequence().getSequence());
        Assert.assertEquals(10, p.getSequence().getLength());
        Assert.assertEquals(5, p.getHeader().getStartLocation());
        Assert.assertEquals(14, p.getHeader().getEndLocation());

        // Truncate to a size that's too large.
        p = p.getCTermTruncatedProtein(100);
        Assert.assertFalse(p.isTruncated());
        Assert.assertEquals(testHeader.toString(), p.getHeader().toString());
        Assert.assertEquals(sequence.substring(4,14), p.getSequence().getSequence());
        Assert.assertEquals(10, p.getSequence().getLength());
        Assert.assertEquals(5, p.getHeader().getStartLocation());
        Assert.assertEquals(14, p.getHeader().getEndLocation());

        // Truncate a Protein instance with a location set.
        p = new Protein(fasta);
        p.getHeader().setLocation(100, 113);
        testHeader.setLocation(104, 113);
        p = p.getCTermTruncatedProtein(10);
        Assert.assertTrue(p.isTruncated());
        Assert.assertEquals(Protein.CTERMTRUNC, p.getTruncationPosition());
        Assert.assertEquals(testHeader.toString(), p.getHeader().toString());
        Assert.assertEquals(sequence.substring(4,14), p.getSequence().getSequence());
        Assert.assertEquals(10, p.getSequence().getLength());
        Assert.assertEquals(104, p.getHeader().getStartLocation());
        Assert.assertEquals(113, p.getHeader().getEndLocation());
    }

    /**
     * This method test the reporting by the Protein about its length.
     */
    public void testLength() {
        Protein p = new Protein(null, "LENNARTMARTENS");
        Assert.assertTrue(p.getLength() == p.getSequence().getLength());
    }

    /**
     * This method test the reporting by the Protein about its mass.
     */
    public void testMass() {
        Protein p = new Protein(null, "LENNARTMARTENS");
        Assert.assertEquals(p.getMass(), p.getSequence().getMass(), 1e-15);
    }

    /**
     * This method test the FASTA printing behaviour.
     */
    public void testPrintToFASTAFile() {
        final String inputFile = "fastaFile.fas";
        final String input = TestCaseLM.getFullFilePath(inputFile).replace("%20", " ");
        try {
            Protein p1 = new Protein(">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).\nMAAPRPPPAISVSVSAPAFYAPQKKFAPVVAPKPKVNPFRPGDSEPPVAAGAQRAQMGRVGEIPPPPPEDFPLPPPPLIGEGDDSEGALGGAFPPPPPPMIEEPFPPAPLEEDIFPSPPPPLEEEGGPEAPTQLPPQPREKVCSIDLEIDSLSSLLDDMTKNDPFKARVSSGYVPPPVATPFVPKPSTKPAPGGTAPLPPWKTPSSSQPPPQPQRKPQVQLHVQPQAKPHVQPQPVSSANTQPRGPLSQAPTPAPKFAPVAPKFTPVVSKFSPGAPSGPGPQPIKKWCLRMPPSSVSTGSPQPPSFTYAQQKEKPLVQEKQHPQPPPAQNQNQVRSPGGPGPLTLKEVEELEQLTQQLMQDMEHPQRQSVAVNESCGKCNQPLARAQPAVRALGQLFHITCFTCHQCQQQLQGQQFYSLEGAPYCEGCYTDTLEKCNTCGQPITDRMLRATGKAYHPQCFTCVVCACPLEGTSFIVDQANQPHSVPDYHKQYAPRCSVCSEPIMPEPGRDETVRVVALDKNFHMKCYKCEDCGKPLSIEADDNGCFPLDGHVLCRKCHSARAQT");
            Protein p2 = new Protein(">sw|O95230|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-2) (Zwint-2).\nMAAPRPPPAISVSVGAPAFYAPQKKFAPVVAPKPKVNPFR");
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
     * This method test the equals method.
     */
    public void testEquals() {
        Protein p1 = new Protein("Test Protein 1.\nLENNARTMARTENS");
        Protein p2 = new Protein("Test Protein 1.\nLENNARTMARTENS");
        Protein p3 = new Protein("Test Protein 1.\nKRISGEVAERT");
        Protein p4 = new Protein("Test Protein 2.\nLENNARTMARTENS");

        Assert.assertTrue(p1.equals(p2));
        Assert.assertTrue(p2.equals(p1));
        Assert.assertFalse(p2.equals(p3));
        Assert.assertFalse(p3.equals(p2));
        Assert.assertFalse(p1.equals(p3));
        Assert.assertFalse(p3.equals(p1));
        Assert.assertFalse(p2.equals(p4));
        Assert.assertFalse(p4.equals(p2));
        Assert.assertFalse(p1.equals(p4));
        Assert.assertFalse(p4.equals(p1));
        Assert.assertFalse(p4.equals(p3));
        Assert.assertFalse(p3.equals(p4));

        p3 = p1.getNTermTruncatedProtein(10);
        Protein p5 = p2.getNTermTruncatedProtein(11);
        Assert.assertFalse(p3.equals(p5));
        Assert.assertFalse(p5.equals(p3));

        p1.getHeader().setLocation(10, 24);
        Protein p6 = new Protein(p2.getHeader().getFullHeaderWithAddenda(), p2.getSequence().getSequence());
        Protein p7 = new Protein(p2.getHeader().getFullHeaderWithAddenda(), p2.getSequence().getSequence());
        p2.getHeader().setLocation(10, 24);
        p6.getHeader().setLocation(11, 24);
        p7.getHeader().setLocation(10, 23);

        Assert.assertTrue(p1.equals(p2));
        Assert.assertTrue(p2.equals(p1));
        Assert.assertFalse(p1.equals(p6));
        Assert.assertFalse(p6.equals(p1));
        Assert.assertFalse(p1.equals(p7));
        Assert.assertFalse(p7.equals(p1));
        Assert.assertFalse(p2.equals(p6));
        Assert.assertFalse(p6.equals(p2));
        Assert.assertFalse(p2.equals(p7));
        Assert.assertFalse(p7.equals(p2));
    }
}
