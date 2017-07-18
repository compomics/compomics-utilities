package com.compomics.util.test.experiment.io;

import com.compomics.util.Util;
import com.compomics.util.db.DerbyUtil;
import com.compomics.util.db.ObjectsDB;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.variants.Variant;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.IdentificationMatch.MatchType;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.identifications.Ms2Identification;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.matches.VariantMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.experiment.refinementparameters.PepnovoAssumptionDetails;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.TestCase;

/**
 *
 * @author Marc Vaudel
 */
public class IdentificationDBTest extends TestCase {

    public void testDB() throws SQLException, IOException, ClassNotFoundException, SQLException, ClassNotFoundException, InterruptedException {

        String path = this.getClass().getResource("IdentificationDBTest.class").getPath();
        path = path.substring(1, path.indexOf("/target/"));
        path += "/src/test/resources/experiment/";
        try {
            ObjectsDB objectsDB = new ObjectsDB(path, "experimentTestDB.zdb", true);
            
            
            
            /*
            objectsDB.registerClass(PeptideMatch.class);
            objectsDB.registerClass(Peptide.class);
            objectsDB.registerClass(VariantMatch.class);
            objectsDB.registerClass(Variant.class);
            objectsDB.registerClass(ModificationMatch.class);
            objectsDB.registerClass(ProteinMatch.class);
            objectsDB.registerClass(SpectrumMatch.class);
            objectsDB.registerClass(Charge.class);
            objectsDB.registerClass(Tag.class);
            objectsDB.registerClass(TagComponent.class);
            objectsDB.registerClass(IdentificationMatch.class);
            objectsDB.registerClass(PeptideAssumption.class);
            objectsDB.registerClass(TagAssumption.class);
            */

            Ms2Identification idDB = new Ms2Identification("the reference", objectsDB);
            try {                
                String parametersKey = "pepnovo_assumption_details";
                String spectrumKey = "spectrum_file_cus_spectrum_title";
                String peptideKey = "PEPTIDE";
                String proteinKey = "test_protein";
                Assert.assertTrue(objectsDB.createLongKey(peptideKey) != objectsDB.createLongKey(proteinKey));
                
                
                ArrayList<String> testProteins = new ArrayList<String>();
                testProteins.add("test protein1");
                testProteins.add("test protein2");
                
                
                Peptide peptide = new Peptide(peptideKey, new ArrayList<ModificationMatch>());
                SpectrumMatch testSpectrumMatch = new SpectrumMatch(spectrumKey);
                testSpectrumMatch.addHit(Advocate.mascot.getIndex(), new PeptideAssumption(peptide, 1, Advocate.mascot.getIndex(), new Charge(Charge.PLUS, 2), 0.1, "no file"), false);
                idDB.addObject(testSpectrumMatch.getKey(), testSpectrumMatch);

                peptide.setParentProteins(testProteins);
                PeptideMatch testPeptideMatch = new PeptideMatch(peptide, peptide.getKey());
                idDB.addObject(testPeptideMatch.getKey(), testPeptideMatch);

                ProteinMatch testProteinMatch = new ProteinMatch(proteinKey);
                idDB.addObject(testProteinMatch.getKey(), testProteinMatch);
                
                
                
                
                idDB.clearCache();

                testSpectrumMatch = (SpectrumMatch)idDB.retrieveObject(spectrumKey);
                Assert.assertTrue(testSpectrumMatch.getKey().equals(spectrumKey));

                
                HashMap<Integer, HashMap<Double, ArrayList<SpectrumIdentificationAssumption>>> assumptionsMap = testSpectrumMatch.getAssumptionsMap();
                HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> mascotAssumptions = assumptionsMap.get(Advocate.mascot.getIndex());
                Assert.assertTrue(mascotAssumptions.size() == 1);
                ArrayList<Double> mascotScores = new ArrayList<Double>(mascotAssumptions.keySet());
                Assert.assertTrue(mascotScores.size() == 1);
                double bestScore = mascotScores.get(0);
                Assert.assertTrue(bestScore == 0.1);
                ArrayList<SpectrumIdentificationAssumption> bestAssumptions = mascotAssumptions.get(bestScore);
                PeptideAssumption bestAssumption = (PeptideAssumption) bestAssumptions.get(0);
                Peptide bestPeptide = bestAssumption.getPeptide();
                Assert.assertTrue(bestPeptide.getParentProteinsNoRemapping().size() == 2);
                Assert.assertTrue(bestPeptide.getParentProteinsNoRemapping().get(0).equals(testProteins.get(0)));
                Assert.assertTrue(bestPeptide.getParentProteinsNoRemapping().get(1).equals(testProteins.get(1)));
                ArrayList<String> proteins = new ArrayList<String>();
                proteins.add(proteinKey);
                bestPeptide.setParentProteins(proteins);

                testSpectrumMatch = (SpectrumMatch)idDB.retrieveObject(spectrumKey);
                assumptionsMap = testSpectrumMatch.getAssumptionsMap();
                mascotAssumptions = assumptionsMap.get(Advocate.mascot.getIndex());
                Assert.assertTrue(mascotAssumptions.size() == 1);
                mascotScores = new ArrayList<Double>(mascotAssumptions.keySet());
                Assert.assertTrue(mascotScores.size() == 1);
                bestScore = mascotScores.get(0);
                Assert.assertTrue(bestScore == 0.1);
                bestAssumptions = mascotAssumptions.get(bestScore);
                bestAssumption = (PeptideAssumption) bestAssumptions.get(0);
                bestPeptide = bestAssumption.getPeptide();
                Assert.assertTrue(bestPeptide.getParentProteinsNoRemapping().size() == 1);
                Assert.assertTrue(bestPeptide.getParentProteinsNoRemapping().get(0).equals(proteinKey));


                testPeptideMatch = (PeptideMatch)idDB.retrieveObject(peptideKey);
                Assert.assertTrue(testPeptideMatch.getKey().equals(peptideKey));

                testProteinMatch = (ProteinMatch)idDB.retrieveObject(proteinKey);
                Assert.assertTrue(testProteinMatch.getKey().equals(proteinKey));

                double testScore = 12.3;
                PepnovoAssumptionDetails testParameter = new PepnovoAssumptionDetails();
                testParameter.setRankScore(testScore);
                idDB.addObject(parametersKey, testParameter);
                testParameter = (PepnovoAssumptionDetails) idDB.retrieveObject(parametersKey);
                Assert.assertTrue(testParameter.getRankScore()== testScore);
            } finally {
                idDB.close();
            }
        } finally {
            File dbFolder = new File(path);
            DerbyUtil.closeConnection();
            Util.deleteDir(dbFolder);
        }
    }
}
