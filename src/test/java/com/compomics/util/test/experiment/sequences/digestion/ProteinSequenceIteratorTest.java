package com.compomics.util.test.experiment.sequences.digestion;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.protein_sequences.ProteinSequenceIterator;
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

        ArrayList<String> fixedModifications = new ArrayList<String>();
        fixedModifications.add("Carbamidomethylation of C");
        fixedModifications.add("Acetylation of protein N-term");
        fixedModifications.add("Pyrolidone from carbamidomethylated C");
        
        ProteinSequenceIterator iteratorNoModifications = new ProteinSequenceIterator(new ArrayList<String>());
        ProteinSequenceIterator iteratorModifications = new ProteinSequenceIterator(fixedModifications);
        
        String testSequence = "TESTKCTESCTKTEST";
        String testSequenceCombination = "TESTKCTJSCTKTEST";
        
        DigestionPreferences digestionPreferences = new DigestionPreferences();
        
        // No digestion
        digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.wholeProtein);
        
        // No modification
        ArrayList<Peptide> peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 1);
        Assert.assertTrue(peptides.get(0).getSequence().equals(testSequence));
        Assert.assertTrue(peptides.get(0).getModificationMatches() == null);
        
        // Combination
        peptides = iteratorNoModifications.getPeptides(testSequenceCombination, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 2);
        
        // No modification lower mass limit
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, 1734.8, null);
        Assert.assertTrue(peptides.size() == 0);
        
        // No modification upper mass limit
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, 1734.7);
        Assert.assertTrue(peptides.size() == 0);
        
        // No modification with mass limits
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, 1734.7, 1734.8);
        Assert.assertTrue(peptides.size() == 1);
        Assert.assertTrue(peptides.get(0).getSequence().equals(testSequence));
        Assert.assertTrue(peptides.get(0).getModificationMatches() == null);
        
        // Modifications
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 1);
        Assert.assertTrue(peptides.get(0).getSequence().equals(testSequence));
        Assert.assertTrue(peptides.get(0).getModificationMatches().size() == 3);
        
        
        // Unspecific digestion
        digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.unSpecific);
        
        // No modification
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 136);
        
        // Combination
        peptides = iteratorNoModifications.getPeptides(testSequenceCombination, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 208);
        
        // No modification lower mass limit
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, 667.0, null);
        Assert.assertTrue(peptides.size() == 64);
        
        // No modification upper mass limit
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, 668.0);
        Assert.assertTrue(peptides.size() == 80);
        
        // No modification with mass limits
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, 667.0, 668.0);
        Assert.assertTrue(peptides.size() == 8);
        
        // Modifications
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 136);
        
        // Modification lower mass limit
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, 667.0, null);
        Assert.assertTrue(peptides.size() == 65);
        
        // Modification upper mass limit
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, null, 668.0);
        Assert.assertTrue(peptides.size() == 71);
        
        // Modification with mass limits
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, 667.0, 668.0);
        Assert.assertTrue(peptides.isEmpty());
        
        // Modification with mass limits
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, 724.0, 725.0);
        Assert.assertTrue(peptides.size() == 6);
        
        
        // Trypsin digestion
        digestionPreferences = DigestionPreferences.getDefaultPreferences();
        
        // No modification
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 6);
        
        // Combination
        peptides = iteratorNoModifications.getPeptides(testSequenceCombination, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 10);
        
        // No modification with mass limits
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, 770.0, 771.0);
        Assert.assertTrue(peptides.size() == 1);
        
        // Modifications
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 6);
        
        // Modification with mass limits
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, 867.0, 868.0);
        Assert.assertTrue(peptides.size() == 1);
        Assert.assertTrue(peptides.get(0).getSequence().equals("CTESCTK"));
        Assert.assertTrue(peptides.get(0).getModificationMatches().size() == 3);
        
        
        // No missed cleavages
        digestionPreferences.setnMissedCleavages("Trypsin", 0);
        
        // No modification
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 3);
        
        
        // Semi-tryptic N
        digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.specificNTermOnly);
        
        // No modification
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 16);
        
        
        // Semi-tryptic C
        digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.specificCTermOnly);
        
        // No modification
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 16);
        
        
        // Semi-tryptic
        digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.semiSpecific);
        
        // No modification
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 29);
        
        
    }
}
