package com.compomics.util.test.experiment.io;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.ProjectParameters;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches_iterators.ProteinMatchesIterator;
import com.compomics.util.experiment.refinement_parameters.PepnovoAssumptionDetails;
import com.compomics.util.io.IoUtil;
import org.junit.Assert;

import java.io.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import junit.framework.TestCase;

/**
 *
 * @author Marc Vaudel
 */
public class IdentificationDBTest extends TestCase {

    public void testDB() throws SQLException, IOException, ClassNotFoundException, SQLException, ClassNotFoundException, InterruptedException {

        String path = this.getClass().getResource("IdentificationDBTest.class").getPath();
        path = path.substring(0, path.indexOf("/target/"));
        path += "/src/test/resources/experiment/identificationDB";

        File dbFolder = new File(path);
        if (!dbFolder.exists()) {
            dbFolder.mkdir();
        }

        try {
            ObjectsDB objectsDB = new ObjectsDB(path, "experimentTestDB.sqlite", true);

            Identification identification = new Identification(objectsDB);

            String spectrumFile = "spectrum_file";
            String spectrumTitle = "spectrum_title";
            String projectParametersTitle = "project_parameters_title";

            String peptideSequence = "PEPTIDE";
            String proteinAccession = "test_protein";

            TreeMap<String, int[]> testProteins = new TreeMap<>();
            testProteins.put("test protein1", new int[]{0, 12});
            testProteins.put("test protein2", new int[]{1259});

            Peptide peptide = new Peptide(peptideSequence);
            SpectrumMatch testSpectrumMatch = new SpectrumMatch(spectrumFile, spectrumTitle);
            long spectrumMatchKey = testSpectrumMatch.getKey();
            testSpectrumMatch.addPeptideAssumption(Advocate.mascot.getIndex(), new PeptideAssumption(peptide, 1, Advocate.mascot.getIndex(), 1, 0.1, "no file"));
            identification.addObject(testSpectrumMatch.getKey(), testSpectrumMatch);

            peptide.setProteinMapping(testProteins);
            PeptideMatch testPeptideMatch = new PeptideMatch(peptide, peptide.getKey(), 0);
            identification.addObject(testPeptideMatch.getKey(), testPeptideMatch);

            ProteinMatch testProteinMatch = new ProteinMatch(proteinAccession);
            identification.addObject(testProteinMatch.getKey(), testProteinMatch);

            long peptideMatchKey = testPeptideMatch.getKey();
            long proteinMatchKey = testProteinMatch.getKey();
            Assert.assertTrue(peptideMatchKey != proteinMatchKey);

            ProjectParameters projectParameters = new ProjectParameters(projectParametersTitle);
            identification.addObject(ProjectParameters.key, projectParameters);

            // closing and reopening database
            identification.close(true);

            objectsDB = new ObjectsDB(path, "experimentTestDB.sqlite", false);
            identification = new Identification(objectsDB);

            testSpectrumMatch = (SpectrumMatch) identification.retrieveObject(spectrumMatchKey);
            Assert.assertTrue(testSpectrumMatch != null);
            Assert.assertTrue(testSpectrumMatch.getKey() == spectrumMatchKey);

            HashMap<Integer, TreeMap<Double, ArrayList<PeptideAssumption>>> assumptionsMap = testSpectrumMatch.getPeptideAssumptionsMap();
            TreeMap<Double, ArrayList<PeptideAssumption>> mascotAssumptions = assumptionsMap.get(Advocate.mascot.getIndex());
            Assert.assertTrue(mascotAssumptions.size() == 1);
            ArrayList<ArrayList<PeptideAssumption>> mascotAssumption = new ArrayList<>(mascotAssumptions.values());
            Assert.assertTrue(mascotAssumption.size() == 1);
            ArrayList<PeptideAssumption> bestAssumptions = mascotAssumption.get(0);
            Assert.assertTrue(bestAssumptions.size() == 1);
            PeptideAssumption bestAssumption = bestAssumptions.get(0);
            Assert.assertTrue(bestAssumption.getRank() == 1);

            //System.out.println(bestAssumption.hasChanged());
            bestAssumption.setRank(2);
            //System.out.println(bestAssumption.hasChanged());
            //identification.updateObject(spectrumMatchKey, testSpectrumMatch);

            // closing and reopening database
            identification.close(true);

            objectsDB = new ObjectsDB(path, "experimentTestDB.sqlite", false);
            identification = new Identification(objectsDB);

            Assert.assertTrue(identification != null);
            Assert.assertTrue(identification.getSpectrumIdentificationKeys() != null);
            Assert.assertTrue(identification.getSpectrumIdentificationKeys().size() == 1);

            ProjectParameters retrieve = (ProjectParameters) identification.retrieveObject(ProjectParameters.key);
            Assert.assertTrue(retrieve != null);
            Assert.assertTrue(retrieve.getProjectUniqueName().equals(projectParametersTitle));

            testSpectrumMatch = (SpectrumMatch) identification.retrieveObject(spectrumMatchKey);
            Assert.assertTrue(testSpectrumMatch.getKey() == spectrumMatchKey);

            assumptionsMap = testSpectrumMatch.getPeptideAssumptionsMap();
            mascotAssumptions = assumptionsMap.get(Advocate.mascot.getIndex());
            Assert.assertTrue(mascotAssumptions.size() == 1);
            ArrayList<Double> mascotScores = new ArrayList<>(mascotAssumptions.keySet());
            Assert.assertTrue(mascotScores.size() == 1);
            double bestScore = mascotScores.get(0);
            Assert.assertTrue(bestScore == 0.1);

            mascotAssumption = new ArrayList<>(mascotAssumptions.values());
            Assert.assertTrue(mascotAssumption.size() == 1);
            bestAssumptions = mascotAssumption.get(0);
            Assert.assertTrue(bestAssumptions.size() == 1);
            bestAssumption = bestAssumptions.get(0);
            Assert.assertTrue(bestAssumption.getRank() == 2);

            bestAssumptions = mascotAssumptions.get(bestScore);
            bestAssumption = (PeptideAssumption) bestAssumptions.get(0);
            Peptide bestPeptide = bestAssumption.getPeptide();
            String[] accessionsExpectation = testProteins.keySet().stream()
                    .sorted()
                    .toArray(String[]::new);
            String[] accessionsResult = bestPeptide.getProteinMapping().navigableKeySet().stream()
                    .toArray(String[]::new);
            Assert.assertTrue(accessionsResult.length == accessionsExpectation.length);

            for (int i = 0; i < accessionsResult.length; i++) {
                Assert.assertTrue(accessionsResult[i].equals(accessionsExpectation[i]));
                int[] positionsResult = bestPeptide.getProteinMapping().get(accessionsResult[i]);
                int[] positionsExpectation = bestPeptide.getProteinMapping().get(accessionsExpectation[i]);
                Assert.assertTrue(positionsResult.length == positionsExpectation.length);
                for (int j = 0; j < positionsExpectation.length; j++) {
                    Assert.assertTrue(positionsResult[j] == positionsExpectation[j]);
                }
            }

            testSpectrumMatch = (SpectrumMatch) identification.retrieveObject(spectrumMatchKey);
            assumptionsMap = testSpectrumMatch.getPeptideAssumptionsMap();
            mascotAssumptions = assumptionsMap.get(Advocate.mascot.getIndex());
            Assert.assertTrue(mascotAssumptions.size() == 1);
            mascotScores = new ArrayList<>(mascotAssumptions.keySet());
            Assert.assertTrue(mascotScores.size() == 1);
            bestScore = mascotScores.get(0);
            Assert.assertTrue(bestScore == 0.1);
            bestAssumptions = mascotAssumptions.get(bestScore);
            bestAssumption = (PeptideAssumption) bestAssumptions.get(0);
            bestPeptide = bestAssumption.getPeptide();
            accessionsResult = bestPeptide.getProteinMapping().navigableKeySet().stream()
                    .sorted()
                    .toArray(String[]::new);
            Assert.assertTrue(accessionsResult.length == accessionsExpectation.length);
            for (int i = 0; i < accessionsResult.length; i++) {
                Assert.assertTrue(accessionsResult[i].equals(accessionsExpectation[i]));
                int[] positionsResult = bestPeptide.getProteinMapping().get(accessionsResult[i]);
                int[] positionsExpectation = bestPeptide.getProteinMapping().get(accessionsExpectation[i]);
                Assert.assertTrue(positionsResult.length == positionsExpectation.length);
                for (int j = 0; j < positionsExpectation.length; j++) {
                    Assert.assertTrue(positionsResult[j] == positionsExpectation[j]);
                }
            }

            testPeptideMatch = (PeptideMatch) identification.retrieveObject(peptideMatchKey);
            Assert.assertTrue(testPeptideMatch.getKey() == peptideMatchKey);

            testProteinMatch = (ProteinMatch) identification.retrieveObject(proteinMatchKey);
            Assert.assertTrue(testProteinMatch.getKey() == proteinMatchKey);

            double testScore = 12.3;
            PepnovoAssumptionDetails testParameter = new PepnovoAssumptionDetails();
            testParameter.setRankScore(testScore);
            long parametersKey = testParameter.getParameterKey();
            identification.addObject(parametersKey, testParameter);
            testParameter = (PepnovoAssumptionDetails) identification.retrieveObject(parametersKey);
            Assert.assertTrue(testParameter.getRankScore() == testScore);
            identification.close(true);

        } finally {
            //IoUtil.deleteDir(dbFolder);
        }
    }

    public void teestMassiveDB() throws SQLException, IOException, ClassNotFoundException, SQLException, ClassNotFoundException, InterruptedException {

        String path = this.getClass().getResource("IdentificationDBTest.class").getPath();
        path = path.substring(0, path.indexOf("/target/"));
        path += "/src/test/resources/experiment/identificationDB";

        File dbFolder = new File(path);
        if (!dbFolder.exists()) {
            dbFolder.mkdir();
        }

        try {
            ObjectsDB objectsDB = new ObjectsDB(path, "experimentMassiveTestDB.sqlite", true);
            Identification identification = new Identification(objectsDB);

            HashSet<String> accessions = new HashSet<>();
            long testProtKey = 0;

            for (int i = 0; i < 10000; ++i) {
                String accession = "PX" + Integer.toString(i);
                accessions.add(accession);
                ProteinMatch testProteinMatch = new ProteinMatch(accession);
                if (i == 0) {
                    testProtKey = testProteinMatch.getKey();
                }
                identification.addObject(testProteinMatch.getKey(), testProteinMatch);

            }

            identification.getObjectsDB().lock(null);
            identification.getObjectsDB().unlock();

            System.out.println("stored");

            identification.loadObjects(ProteinMatch.class, null, false);

            Peptide peptide = new Peptide("TTTTTTTTTTTTTTTTK");
            PeptideMatch testPeptideMatch = new PeptideMatch(peptide, peptide.getKey(), 0);
            long pKey = testPeptideMatch.getKey();
            identification.addPeptideMatch(pKey, testPeptideMatch);

            ProteinMatch proteinMatch = identification.getProteinMatch(testProtKey);
            proteinMatch.addPeptideMatchKey(pKey);

            identification.loadObjects(ProteinMatch.class, null, false);

            Assert.assertTrue(proteinMatch.getPeptideCount() == 1);
            Assert.assertTrue(proteinMatch.getPeptideMatchesKeys()[0] == pKey);

            ProteinMatchesIterator pmi = identification.getProteinMatchesIterator(null);

            while (true) {
                ProteinMatch pm = pmi.next();
                if (pm == null) {
                    break;
                }

                String acc = pm.getAccessions()[0];
                Assert.assertTrue(accessions.contains(acc));
            }

            identification.close(true);

        } finally {
            IoUtil.deleteDir(dbFolder);
        }

    }
}
