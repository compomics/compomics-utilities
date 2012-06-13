/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.test.experiment;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationDB;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.experiment.refinementparameters.MascotScore;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import junit.framework.Assert;

/**
 *
 * @author marc
 */
public class IdentificationDBTest {

    
    public void testDB() {
        try {
            
            String path = this.getClass().getResource("IdentificationTest.class").getPath();
            path = path.substring(1, path.indexOf("/target/"));
            path += "/src/test/resources/experiment/testDB";
            IdentificationDB idDB = new IdentificationDB(path);
            
            String spectrumKey = "test spectrum match";
            String peptideKey = "PEPTIDE";
            String proteinKey = "test_protein";
            SpectrumMatch testSpectrumMatch = new SpectrumMatch(spectrumKey);
            testSpectrumMatch.addHit(Advocate.PEPTIDE_SHAKER, new PeptideAssumption(new Peptide(peptideKey, new ArrayList<String>(), new ArrayList<ModificationMatch>()), 1, Advocate.PEPTIDE_SHAKER, new Charge(Charge.PLUS, 2), 0.1, "no file"));
            idDB.addSpectrumMatch(testSpectrumMatch);
            
            PeptideMatch testPeptideMatch = new PeptideMatch(new Peptide(peptideKey, new ArrayList<String>(), new ArrayList<ModificationMatch>()));
            idDB.addPeptideMatch(testPeptideMatch);
            
            ProteinMatch testProteinMatch = new ProteinMatch(proteinKey);
            idDB.addProteinMatch(testProteinMatch);
            
            testSpectrumMatch = idDB.getSpectrumMatch(spectrumKey);
            Assert.assertTrue(testSpectrumMatch.getKey().equals(spectrumKey));
            
            ArrayList<String> proteins = new ArrayList<String>();
            proteins.add(proteinKey);
            testSpectrumMatch.getFirstHit(Advocate.PEPTIDE_SHAKER).getPeptide().setParentProteins(proteins);
            idDB.updateMatch(testSpectrumMatch);
            
            testSpectrumMatch = idDB.getSpectrumMatch(spectrumKey);
            Assert.assertTrue(testSpectrumMatch.getFirstHit(Advocate.PEPTIDE_SHAKER).getPeptide().getParentProteins().get(0).equals(proteinKey));
            
            testPeptideMatch = idDB.getPeptideMatch(peptideKey);
            Assert.assertTrue(testPeptideMatch.getKey().equals(peptideKey));
            
            testProteinMatch = idDB.getProteinMatch(proteinKey);
            Assert.assertTrue(testProteinMatch.getKey().equals(proteinKey));
            
            double testScore = 12.3;
            MascotScore testParameter = new MascotScore(testScore);
            idDB.addSpectrumMatchParameter(spectrumKey, testParameter);
            testParameter = (MascotScore) idDB.getSpectrumMatchParameter(spectrumKey, testParameter);
            Assert.assertTrue(testParameter.getScore() == testScore);
            
            idDB.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
