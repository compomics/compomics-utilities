package com.compomics.util.test.experiment.sequences.digestion;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.digestion.IteratorFactory;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.preferences.DigestionPreferences;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Test for the protein sequence iterator.
 *
 * @author Marc Vaudel
 */
public class ProteinSequenceIteratorTest extends TestCase {

    public void testEnzymeDigestion() throws XmlPullParserException, IOException {

        int nXs = 2;
        
        ArrayList<String> fixedModifications = new ArrayList<String>();
        fixedModifications.add("Carbamidomethylation of C");
        fixedModifications.add("Acetylation of protein N-term");
        fixedModifications.add("Pyrolidone from carbamidomethylated C");
        
        IteratorFactory iteratorFactoryNoModifications = new IteratorFactory(new ArrayList<String>(), nXs);
        IteratorFactory iteratorFactoryModifications = new IteratorFactory(fixedModifications, nXs);
        
        String testSequence = "TESTKCTESCTKTEST";
        String testSequenceCombination = "TESTKCTJSCTKTEST";
        
        DigestionPreferences digestionPreferences = new DigestionPreferences();
        
        // No digestion
        digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.wholeProtein);
        
        // No modification
        SequenceIterator sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        ArrayList<PeptideWithPosition> peptides = new ArrayList<PeptideWithPosition>();
        PeptideWithPosition peptideWithPosition;
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 1);
        Peptide peptide = peptides.get(0).getPeptide();
        Assert.assertTrue(peptide.getSequence().equals(testSequence));
        Assert.assertTrue(peptide.getModificationMatches() == null);
        
        // Combination
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequenceCombination, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 2);
        
        // No modification lower mass limit
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, 1734.8, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 0);
        
        // No modification upper mass limit
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, 1734.7);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 0);
        
        // No modification with mass limits
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, 1734.7, 1734.8);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 1);
        peptide = peptides.get(0).getPeptide();
        Assert.assertTrue(peptide.getSequence().equals(testSequence));
        Assert.assertTrue(peptide.getModificationMatches() == null);
        
        // Modifications
        sequenceIterator = iteratorFactoryModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 1);
        peptide = peptides.get(0).getPeptide();
        Assert.assertTrue(peptide.getSequence().equals(testSequence));
        Assert.assertTrue(peptide.getModificationMatches().size() == 3);
        
        
        // Unspecific digestion
        digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.unSpecific);
        
        // No modification
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 136);
        
        // Combination
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequenceCombination, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 208);
        
        // No modification lower mass limit
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, 667.0, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 64);
        
        // No modification upper mass limit
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, 668.0);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 80);
        
        // No modification with mass limits
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, 667.0, 668.0);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 8);
        
        // Modifications
        sequenceIterator = iteratorFactoryModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 136);
        
        // Modification lower mass limit
        sequenceIterator = iteratorFactoryModifications.getSequenceIterator(testSequence, digestionPreferences, 667.0, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 65);
        
        // Modification upper mass limit
        sequenceIterator = iteratorFactoryModifications.getSequenceIterator(testSequence, digestionPreferences, null, 668.0);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 71);
        
        // Modification with mass limits
        sequenceIterator = iteratorFactoryModifications.getSequenceIterator(testSequence, digestionPreferences, 667.0, 668.0);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.isEmpty());
        
        
        // Trypsin digestion
        digestionPreferences = DigestionPreferences.getDefaultPreferences();
        
        // No modification
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 6);
        
        // Combination
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequenceCombination, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 10);
        
        // No modification with mass limits
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, 770.0, 771.0);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 1);
        
        // Modifications
        sequenceIterator = iteratorFactoryModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 6);
        
        // Modification with mass limits
        sequenceIterator = iteratorFactoryModifications.getSequenceIterator(testSequence, digestionPreferences, 867.0, 868.0);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 1);
        peptide = peptides.get(0).getPeptide();
        Assert.assertTrue(peptide.getSequence().equals("CTESCTK"));
        Assert.assertTrue(peptide.getModificationMatches().size() == 3);
        
        
        // No missed cleavages
        digestionPreferences.setnMissedCleavages("Trypsin", 0);
        
        // No modification
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 3);
        
        
        // Semi-tryptic N
        digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.specificNTermOnly);
        
        // No modification
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 16);
        
        
        // Semi-tryptic C
        digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.specificCTermOnly);
        
        // No modification
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 16);
        
        
        // Semi-tryptic
        digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.semiSpecific);
        
        // No modification
        sequenceIterator = iteratorFactoryNoModifications.getSequenceIterator(testSequence, digestionPreferences, null, null);
        peptides = new ArrayList<PeptideWithPosition>();
        while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {
            peptides.add(peptideWithPosition);
        }
        Assert.assertTrue(peptides.size() == 29);
        
        
    }
}
