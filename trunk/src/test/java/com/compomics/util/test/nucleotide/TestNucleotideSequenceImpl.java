/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 6-jan-03
 * Time: 10:56:32
 */
package com.compomics.util.test.nucleotide;

import junit.TestCaseLM;
import com.compomics.util.interfaces.Sequence;
import com.compomics.util.nucleotide.NucleotideSequenceImpl;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Protein;
import com.compomics.util.protein.Header;
import junit.framework.*;

import java.util.Vector;
import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class implements the test scenario for the NucleotideSequenceImpl class.
 *
 * @author Lennart Martens
 * @see com.compomics.util.nucleotide.NucleotideSequenceImpl
 */
public class TestNucleotideSequenceImpl extends TestCaseLM {

    public TestNucleotideSequenceImpl() {
        this("Test scenario for the NucleotideSequenceImpl class.");
    }

    public TestNucleotideSequenceImpl(String aName) {
        super(aName);
    }

    /**
     * Test for the construction and reading of variables
     * as defined in the Sequence interface.
     */
    public void testConstructionAndGetters() {
        Sequence seq = new NucleotideSequenceImpl("AGCTAGCTAGCTAGCTAG");

        Assert.assertEquals("AGCTAGCTAGCTAGCTAG", seq.getSequence());
        Assert.assertEquals("AGCTAGCTAGCTAGCTAG".length(), seq.getLength());
        Assert.assertEquals(5523.659513, seq.getMass(), Double.MIN_VALUE*2);

        // Change sequence.
        seq.setSequence("ACTG");
        Assert.assertEquals("ACTG", seq.getSequence());
        Assert.assertEquals("ACTG".length(), seq.getLength());
        Assert.assertEquals(1173.8434329999998, seq.getMass(), 0.0000002);

        // Now with lowercase.
        seq = new NucleotideSequenceImpl("actg");
        Assert.assertEquals("ACTG", seq.getSequence());
        Assert.assertEquals("ACTG".length(), seq.getLength());
        Assert.assertEquals(1173.8434329999998, seq.getMass(), 0.0000002);

        seq.setSequence("actg");
        Assert.assertEquals("ACTG", seq.getSequence());
        Assert.assertEquals("ACTG".length(), seq.getLength());
        Assert.assertEquals(1173.8434329999998, seq.getMass(), 0.0000002);

        // Now with spaces.
        seq = new NucleotideSequenceImpl(" actg     ");
        Assert.assertEquals("ACTG", seq.getSequence());
        Assert.assertEquals("ACTG".length(), seq.getLength());
        Assert.assertEquals(1173.8434329999998, seq.getMass(), 0.0000002);

        seq.setSequence("       ACTG     ");
        Assert.assertEquals("ACTG", seq.getSequence());
        Assert.assertEquals("ACTG".length(), seq.getLength());
        Assert.assertEquals(1173.8434329999998, seq.getMass(), 0.0000002);
    }

    /**
     * Test translation of DNA sequence.
     */
    public void testTranslation() {
        NucleotideSequenceImpl seq = new NucleotideSequenceImpl("ATG");
        AASequenceImpl[] aas = seq.translate();
        Assert.assertEquals(2, aas.length);
        Assert.assertEquals("M", aas[0].getSequence());

        seq.setSequence("GATTACATTACAGAGAGAGAGGGGATAT");
        aas = seq.translate();
        Assert.assertEquals(6, aas.length);
        Assert.assertEquals("DYITEREGI", aas[0].getSequence());
        Assert.assertEquals("ITLQRERGY", aas[1].getSequence());
        Assert.assertEquals("LHYRERGD", aas[2].getSequence());
        Assert.assertEquals("ISPLSL_CN", aas[3].getSequence());
        Assert.assertEquals("YPLSLCNVI", aas[4].getSequence());
        Assert.assertEquals("IPSLSVM_", aas[5].getSequence());

        // Unknown nucleotide translation into aminoacid X.
        seq.setSequence("GANNACATTACAGAGAGAGAGGGGATAT");
        aas = seq.translate();
        Assert.assertEquals(6, aas.length);
        Assert.assertEquals("XXITEREGI", aas[0].getSequence());
        Assert.assertEquals("XTLQRERGY", aas[1].getSequence());

        Assert.assertEquals(1054.043115, aas[0].getMass(),0.0);

    }
    /**
     * Test translation of DNA sequence.
     */
    public void testTranslateToStopCodonSeparatedEntries() {
        // Test sequence with stop codons in sense and anti-sense.
        NucleotideSequenceImpl seq = new NucleotideSequenceImpl("GATTAGATAAGTACAGAGAGTAGAGGGGATATA");
        Vector translations = seq.translateToStopCodonSeparatedEntries("chromX", "hum");

        HashMap translationframe = (HashMap) translations.get(0);
        Header[] lHeaders = new Header[2];
        translationframe.keySet().toArray(lHeaders);

        Header lHeader = null;
        Protein lProtein = null;
        String lProteinSequence = "";
        String lNucleicSequence = "";

        // a) first entry is "D" or "ISTESRGDI" because we're dealing with a HashMap.
        lHeader = lHeaders[0];
        lNucleicSequence = (String) lHeader.getRest();
        lProteinSequence = (String) translationframe.get(lHeader);
        lProtein = new Protein(lHeader, new AASequenceImpl(lProteinSequence));

        Assert.assertEquals(2,translationframe.keySet().size());
        Assert.assertEquals(6, translations.size());

        if(lProteinSequence.equals("D")){
            Assert.assertEquals("D", lProtein.getSequence().getSequence());
            Assert.assertEquals("GAT", lNucleicSequence);
        }else{
            Assert.assertEquals("ISTESRGDI", lProtein.getSequence().getSequence());
            Assert.assertEquals("ATAAGTACAGAGAGTAGAGGGGATATA", lNucleicSequence);
        }

        // b) next entry in first reading frame.
        lHeader = lHeaders[1];
        lProteinSequence = (String) translationframe.get(lHeader);
        lNucleicSequence = (String) lHeader.getRest();
        lProtein = new Protein(lHeader, new AASequenceImpl(lProteinSequence));

        if(lProteinSequence.equals("D")){
            Assert.assertEquals("D", lProtein.getSequence().getSequence());
            Assert.assertEquals("GAT", lNucleicSequence);
        }else{
            Assert.assertEquals("ISTESRGDI", lProtein.getSequence().getSequence());
            Assert.assertEquals("ATAAGTACAGAGAGTAGAGGGGATATA", lNucleicSequence);
        }

        // c) one and only entry in the last(5) reading frame.
        translationframe = (HashMap) translations.get(5);
        lHeaders = new Header[1];
        translationframe.keySet().toArray(lHeaders);
        lHeader = lHeaders[0];
        lProteinSequence = (String) translationframe.get(lHeader);
        lNucleicSequence = (String) lHeader.getRest();
        lProtein = new Protein(lHeader, new AASequenceImpl(lProteinSequence));

        Assert.assertEquals(1,translationframe.keySet().size());
        Assert.assertEquals("YPLYSLYLSN", lProtein.getSequence().getSequence());
        Assert.assertEquals("TATCCCCTCTACTCTCTGTACTTATCTAAT", lNucleicSequence);

    }

    

    /**
     * Test generation of complementary sequence.
     */
    public void testComplement() {
        NucleotideSequenceImpl seq = new NucleotideSequenceImpl("ACTG");
        Assert.assertEquals("CAGT", seq.getReverseComplementary());
        // Standard complement.
        seq.setSequence("ACTTGACCGATGAATG");
        Assert.assertEquals("CATTCATCGGTCAAGT", seq.getReverseComplementary());
        // Unknown nucleotide complement.
        seq.setSequence("ACTTGACCGATGNNTG");
        Assert.assertEquals("CANNCATCGGTCAAGT", seq.getReverseComplementary());
    }
}
