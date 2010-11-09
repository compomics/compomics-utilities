package com.compomics.util.test.experiment;

import junit.framework.TestCase;
import com.compomics.util.experiment.identification.IdfileReaderFactory;
import com.compomics.util.experiment.identification.IdfileReader;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;

import java.io.File;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 5:40:53 PM
 * This test will import various files and display the result of the parsing.
 */
public class FileImportTest extends TestCase {

    private static final String MASCOT_FILE = "testFiles/velos002764.dat";
    private static final String OMSSA_FILE = "testFiles/velos002764.omx";
    private static final String XTANDEM_FILE = "testFiles/velos002764.xml";
    private static final String MODIFICATION_FILE = "exampleFiles/experiment/mods.xml";

    private HashMap<String, ProteinMatch> proteins = new HashMap<String, ProteinMatch>();

    private IdfileReaderFactory idfileReaderFactory = IdfileReaderFactory.getInstance();
    private PTMFactory ptmFactory = PTMFactory.getInstance();


    public void testReading() {

        /**
        File modificationFile = new File(MODIFICATION_FILE);
        File mascotFile = new File(MASCOT_FILE);
        File omssaFile = new File(OMSSA_FILE);
        File xTandemFile = new File(XTANDEM_FILE);

        try {
        ptmFactory.importModifications(modificationFile);
        } catch (Exception e) {

        }
        IdfileReader mascotReader = idfileReaderFactory.getFileReader(mascotFile);
        IdfileReader omssaReader = idfileReaderFactory.getFileReader(omssaFile);
        IdfileReader xTandemReader = idfileReaderFactory.getFileReader(xTandemFile);
        HashSet<SpectrumMatch> matches = new HashSet();
        matches = mascotReader.getAllSpectrumMatches();
        matches = new HashSet();
        matches = xTandemReader.getAllSpectrumMatches();
        try {
            for (SpectrumMatch match : matches) {
                Peptide peptide = match.getFirstHit(Advocate.XTANDEM).getPeptide();
                Protein protein = peptide.getParentProteins().get(0);
                PeptideMatch peptideMatch = new PeptideMatch(peptide, match);
                if (proteins.get(protein.getAccession()) == null) {
                    proteins.put(protein.getAccession(), new ProteinMatch(protein, peptideMatch));
                } else {
                    proteins.get(protein.getAccession()).addPeptideMatch(peptideMatch);
                }
            }
        } catch (Exception e) {
            int test = 0;
        }
        matches = new HashSet();
        matches = omssaReader.getAllSpectrumMatches();
        matches = new HashSet();       **/
    }
}
