/*
 * Copyright (C) Lennart Martens
 *
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.test;
import com.compomics.util.test.general.TestIsotopicDistributionCalculator;
import org.apache.log4j.Logger;


import junit.framework.*;

import com.compomics.util.test.general.TestMassCalc;
import com.compomics.util.test.general.TestCommandLineParser;
import com.compomics.util.test.protein.*;
import com.compomics.util.test.io.*;
import com.compomics.util.test.nucleotide.TestNucleotideSequenceImpl;
import com.compomics.util.test.nucleotide.TestNucleotideSequence;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2008/11/18 11:39:11 $
 */

/**
 * This class represents the full suite of test for the utilities
 * project.
 *
 * @author	Lennart Martens
 */
public class FullSuite extends TestCase {

    // Class specific log4j logger for FullSuite instances.
    Logger logger = Logger.getLogger(FullSuite.class);

    public FullSuite() {
        super("Full test suite for Utilities project.");
    }

    public static Test suite() {
        TestSuite ts = new TestSuite("Test suite for the 'Utilities' project.");

        ts.addTest(new TestSuite(TestMassCalc.class));
        ts.addTest(new TestSuite(TestModificationFactory.class));
        ts.addTest(new TestSuite(TestModificationImplementation.class));
        ts.addTest(new TestSuite(TestAASequenceImpl.class));
        //ts.addTest(new TestSuite(TstMassCalcServlet.class));
        ts.addTest(new TestSuite(TestCommandLineParser.class));
        ts.addTest(new TestSuite(TestPushBackStringReader.class));
        ts.addTest(new TestSuite(TestHeader.class));
        ts.addTest(new TestSuite(TestProtein.class));
        ts.addTest(new TestSuite(TestEnzyme.class));
        ts.addTest(new TestSuite(TestDualEnzyme.class));
        ts.addTest(new TestSuite(TestRegExEnzyme.class));
        ts.addTest(new TestSuite(TestMascotEnzymeReader.class));
        ts.addTest(new TestSuite(TestMonitorableInputStream.class));
        ts.addTest(new TestSuite(TestMonitorableFileInputStream.class));
        ts.addTest(new TestSuite(TestFTPClient.class));
        ts.addTest(new TestSuite(TestFolderMonitor.class));
        ts.addTest(new TestSuite(TestFilenameExtensionFilter.class));
        ts.addTest(new TestSuite(TestNucleotideSequenceImpl.class));
        ts.addTest(new TestSuite(TestNucleotideSequence.class));
        ts.addTest(new TestSuite(TestIsotopicDistributionCalculator.class));

        return ts;
    }
}
