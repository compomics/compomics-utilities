/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 9-okt-02
 * Time: 9:17:13
 */
package com.compomics.util.test.protein;

import com.compomics.util.enumeration.MolecularElement;
import com.compomics.util.junit.TestCaseLM;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.MolecularFormula;
import com.compomics.util.protein.Protein;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class implements a simple test scenario for the molecularformula class.
 *
 * @author Niels Hulstaert
 * @see com.compomics.util.protein.MolecularFormula
 */
public class TestMolecularFormula extends TestCase {

    // Class specific log4j logger for TestEnzyme instances.
    Logger logger = Logger.getLogger(TestMolecularFormula.class);

    public TestMolecularFormula() {
        this("Test scenario for the MolecularFormula class.");
    }

    public TestMolecularFormula(String aName) {
        super(aName);
    }

    /**
     * This method test the creation of a MolecularFormula instance from a peptide that contains selenocysteine amino acid.
     */
    public void testCreation() {
        String pepseq="AASULENNAR";
        AASequenceImpl aaSequence = new AASequenceImpl(pepseq);
        MolecularFormula formula = new MolecularFormula(aaSequence);

        Assert.assertEquals("H70 C40 N15 O16 Se1 ", formula.toString());
        Assert.assertEquals(1, formula.getElementCount(MolecularElement.Se));
    }
}
