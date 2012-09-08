package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.io.identifications.IdfileReaderFactory;
import junit.framework.TestCase;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 5:40:53 PM
 * This test will import various files and display the result of the parsing.
 */
public class IdFileImportTest extends TestCase {

    private static final String MASCOT_FILE = "testFiles/orbitrap001769.dat";
    private static final String OMSSA_FILE = "testFiles/velos004096_HR_new.omx";
    private static final String XTANDEM_FILE = "testFiles/velos004096_HR_new.t.xml";
    private static final String MODIFICATION_FILE = "exampleFiles/experiment/mods.xml";
    private static final String USER_MODIFICATION_FILE = "exampleFiles/experiment/usermods.xml";
    private IdfileReaderFactory idfileReaderFactory = IdfileReaderFactory.getInstance();
    private PTMFactory ptmFactory = PTMFactory.getInstance();

    public void testReading() {

        File modificationFile = new File(MODIFICATION_FILE);
        File userModificationFile = new File(USER_MODIFICATION_FILE);
        File mascotFile = new File(MASCOT_FILE);
        File omssaFile = new File(OMSSA_FILE);
        File xTandemFile = new File(XTANDEM_FILE);
/**

        try {
            ptmFactory.importModifications(modificationFile);
            ptmFactory.importModifications(userModificationFile);
            IdfileReader reader = idfileReaderFactory.getFileReader(xTandemFile);
            HashSet<SpectrumMatch> matches;
            matches = reader.getAllSpectrumMatches();

            boolean test;
            for (SpectrumMatch match : matches) {
                if (match.getFirstHit(Advocate.MASCOT).getPeptide().getSequence().equals("VLAITSSSIPKNIQSLR")) {
                    int decoy = 0;
                }
                identification.addSpectrumMatch(match);
            }
            Identification newIdentification = new Ms2Identification();
            for (ProteinMatch proteinMatch : identification.getProteinIdentification().values()) {
            }
        } catch (Exception e) {
            int debug = 0;
        }**/

    }
}
