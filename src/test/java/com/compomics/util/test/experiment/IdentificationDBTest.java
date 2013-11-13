package com.compomics.util.test.experiment;

import com.compomics.util.Util;
import com.compomics.util.db.ObjectsCache;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationDB;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.refinementparameters.MascotScore;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import junit.framework.TestCase;

/**
 *
 * @author Marc Vaudel
 */
public class IdentificationDBTest extends TestCase {

    public void testDB() throws SQLException, IOException, ClassNotFoundException, SQLException, ClassNotFoundException, InterruptedException {

        String path = this.getClass().getResource("IdentificationDBTest.class").getPath();
        path = path.substring(1, path.indexOf("/target/"));
        path += "/src/test/resources/experiment/testDB";
        ObjectsCache cache = new ObjectsCache();
        cache.setAutomatedMemoryManagement(false);
        cache.setCacheSize(0);
        IdentificationDB idDB = new IdentificationDB(path, "testId", true, cache);

        String spectrumKey = "test spectrum match";
        String peptideKey = "PEPTIDE";
        String proteinKey = "test_protein";
        SpectrumMatch testSpectrumMatch = new SpectrumMatch(spectrumKey);
        ArrayList<String> testProteins = new ArrayList<String>();
        testProteins.add("test protein1");
        testProteins.add("test protein2");
        Peptide peptide = new Peptide(peptideKey, new ArrayList<ModificationMatch>());
        peptide.setParentProteins(testProteins);
        testSpectrumMatch.addHit(Advocate.PEPTIDE_SHAKER, new PeptideAssumption(peptide, 1, Advocate.PEPTIDE_SHAKER, new Charge(Charge.PLUS, 2), 0.1, "no file"));
        idDB.addSpectrumMatch(testSpectrumMatch);

        peptide = new Peptide(peptideKey, new ArrayList<ModificationMatch>());
        peptide.setParentProteins(testProteins);
        PeptideMatch testPeptideMatch = new PeptideMatch(peptide);
        idDB.addPeptideMatch(testPeptideMatch);

        ProteinMatch testProteinMatch = new ProteinMatch(proteinKey);
        idDB.addProteinMatch(testProteinMatch);

        testSpectrumMatch = idDB.getSpectrumMatch(spectrumKey, true);
        Assert.assertTrue(testSpectrumMatch.getKey().equals(spectrumKey));

        ArrayList<String> proteins = new ArrayList<String>();
        proteins.add(proteinKey);
        ((PeptideAssumption) testSpectrumMatch.getFirstHit(Advocate.PEPTIDE_SHAKER)).getPeptide().setParentProteins(proteins);
        idDB.updateMatch(testSpectrumMatch);

        testSpectrumMatch = idDB.getSpectrumMatch(spectrumKey, true);
        Assert.assertTrue(((PeptideAssumption) testSpectrumMatch.getFirstHit(Advocate.PEPTIDE_SHAKER)).getPeptide().getParentProteins().get(0).equals(proteinKey));

        testPeptideMatch = idDB.getPeptideMatch(peptideKey, true);
        Assert.assertTrue(testPeptideMatch.getKey().equals(peptideKey));

        testProteinMatch = idDB.getProteinMatch(proteinKey, true);
        Assert.assertTrue(testProteinMatch.getKey().equals(proteinKey));

        double testScore = 12.3;
        MascotScore testParameter = new MascotScore(testScore);
        idDB.addSpectrumMatchParameter(spectrumKey, testParameter);
        testParameter = (MascotScore) idDB.getSpectrumMatchParameter(spectrumKey, testParameter, true);
        Assert.assertTrue(testParameter.getScore() == testScore);

        idDB.close();

        File dbFolder = new File(path);
        Util.deleteDir(dbFolder);

    }
}
