package com.compomics.util.test.experiment.io;

import com.compomics.util.Util;
import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.ProjectParameters;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.refinement_parameters.PepnovoAssumptionDetails;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author Marc Vaudel
 */
public class IdentificationDBTest extends TestCase {

    public void testDB() throws SQLException, IOException, ClassNotFoundException, SQLException, ClassNotFoundException, InterruptedException {

        String path = this.getClass().getResource("IdentificationDBTest.class").getPath();
        path = path.substring(1, path.indexOf("/target/"));
        path += "/src/test/resources/experiment/identificationDB";
        File dbFolder = new File(path);
        if (!dbFolder.exists()) {
            dbFolder.mkdir();
        }

        try {
            ObjectsDB objectsDB = new ObjectsDB(path, "experimentTestDB.zdb", true);

            Identification identification = new Identification(objectsDB);

            try {
                String parametersKey = "pepnovo_assumption_details";
                String spectrumFile = "spectrum_file";
                String spectrumTitle = "spectrum_title";
                String projectParametersTitle = "project_parameters_title";
                String spectrumKey = Spectrum.getSpectrumKey(spectrumFile, spectrumTitle);
                String peptideKey = "PEPTIDE";
                String proteinKey = "test_protein";
                Assert.assertTrue(objectsDB.createLongKey(peptideKey) != objectsDB.createLongKey(proteinKey));

                HashMap<String, int[]> testProteins = new HashMap<>();
                testProteins.put("test protein1", new int[]{0, 12});
                testProteins.put("test protein2", new int[]{1259});

                Peptide peptide = new Peptide(peptideKey, new ArrayList<>());
                SpectrumMatch testSpectrumMatch = new SpectrumMatch(spectrumKey);
                testSpectrumMatch.addPeptideAssumption(Advocate.mascot.getIndex(), new PeptideAssumption(peptide, 1, Advocate.mascot.getIndex(), 2, 0.1, "no file"));
                identification.addObject(testSpectrumMatch.getKey(), testSpectrumMatch);

                peptide.setProteinMapping(testProteins);
                PeptideMatch testPeptideMatch = new PeptideMatch(peptide, peptide.getKey());
                identification.addObject(testPeptideMatch.getKey(), testPeptideMatch);

                ProteinMatch testProteinMatch = new ProteinMatch(proteinKey);
                identification.addObject(testProteinMatch.getKey(), testProteinMatch);

                ProjectParameters projectParameters = new ProjectParameters(projectParametersTitle);
                identification.addObject(ProjectParameters.nameForDatabase, projectParameters);

                identification.getObjectsDB().dumpToDB();
                identification.close();

                objectsDB = new ObjectsDB(path, "experimentTestDB.zdb", false);
                identification = new Identification(objectsDB);

                ProjectParameters retrieve = (ProjectParameters) identification.retrieveObject(ProjectParameters.nameForDatabase);
                Assert.assertTrue(retrieve != null);
                Assert.assertTrue(retrieve.getProjectUniqueName().equals(projectParametersTitle));

                testSpectrumMatch = (SpectrumMatch) identification.retrieveObject(spectrumKey);
                Assert.assertTrue(testSpectrumMatch.getKey().equals(spectrumKey));

                HashMap<Integer, HashMap<Double, ArrayList<PeptideAssumption>>> assumptionsMap = testSpectrumMatch.getPeptideAssumptionsMap();
                HashMap<Double, ArrayList<PeptideAssumption>> mascotAssumptions = assumptionsMap.get(Advocate.mascot.getIndex());
                Assert.assertTrue(mascotAssumptions.size() == 1);
                ArrayList<Double> mascotScores = new ArrayList<>(mascotAssumptions.keySet());
                Assert.assertTrue(mascotScores.size() == 1);
                double bestScore = mascotScores.get(0);
                Assert.assertTrue(bestScore == 0.1);
                ArrayList<PeptideAssumption> bestAssumptions = mascotAssumptions.get(bestScore);
                PeptideAssumption bestAssumption = (PeptideAssumption) bestAssumptions.get(0);
                Peptide bestPeptide = bestAssumption.getPeptide();
                String[] accessionsExpectation = testProteins.keySet().stream()
                        .sorted()
                        .toArray(String[]::new);
                String[] accessionsResult = bestPeptide.getProteinMapping().keySet().stream()
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

                testSpectrumMatch = (SpectrumMatch) identification.retrieveObject(spectrumKey);
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
                accessionsResult = bestPeptide.getProteinMapping().keySet().stream()
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

                testPeptideMatch = (PeptideMatch) identification.retrieveObject(peptideKey);
                Assert.assertTrue(testPeptideMatch.getKey().equals(peptideKey));

                testProteinMatch = (ProteinMatch) identification.retrieveObject(proteinKey);
                Assert.assertTrue(testProteinMatch.getKey().equals(proteinKey));

                double testScore = 12.3;
                PepnovoAssumptionDetails testParameter = new PepnovoAssumptionDetails();
                testParameter.setRankScore(testScore);
                identification.addObject(parametersKey, testParameter);
                testParameter = (PepnovoAssumptionDetails) identification.retrieveObject(parametersKey);
                Assert.assertTrue(testParameter.getRankScore() == testScore);
                identification.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            Util.deleteDir(dbFolder);
        }
    }
}
