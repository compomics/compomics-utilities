package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.io.identifications.IdfileReaderFactory;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.identification.identifications.Ms2Identification;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.Identification;
import junit.framework.TestCase;

import java.io.File;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 5:40:53 PM
 * This test will import various files and display the result of the parsing.
 */
public class IdFileImportTest extends TestCase {

    private static final String MASCOT_FILE = "testFiles/velos002764.dat";
    private static final String OMSSA_FILE = "testFiles/orbitrap001992.omx";
    private static final String XTANDEM_FILE = "testFiles/orbitrap001924.xml";
    private static final String MODIFICATION_FILE = "exampleFiles/experiment/mods.xml";
    private static final String USER_MODIFICATION_FILE = "exampleFiles/experiment/usermods.xml";
    private Ms2Identification identification = new Ms2Identification();
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
            IdfileReader reader = idfileReaderFactory.getFileReader(omssaFile);
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
        }
**/
    }
}
