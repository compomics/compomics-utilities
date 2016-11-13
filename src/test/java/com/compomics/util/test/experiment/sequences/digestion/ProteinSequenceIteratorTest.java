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
        
        DigestionPreferences digestionPreferences = new DigestionPreferences();
        digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.wholeProtein);
        
        // Test no digestion no modification
        ArrayList<Peptide> peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 1);
        Assert.assertTrue(peptides.get(0).getSequence().equals(testSequence));
        Assert.assertTrue(peptides.get(0).getModificationMatches() == null);
        
        // Test no digestion no modification lower mass limit
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, 1734.8, null);
        Assert.assertTrue(peptides.size() == 0);
        
        // Test no digestion no modification upper mass limit
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, null, 1734.7);
        Assert.assertTrue(peptides.size() == 0);
        
        // Test no digestion no modification with mass limits
        peptides = iteratorNoModifications.getPeptides(testSequence, digestionPreferences, 1734.7, 1734.8);
        Assert.assertTrue(peptides.size() == 1);
        Assert.assertTrue(peptides.get(0).getSequence().equals(testSequence));
        Assert.assertTrue(peptides.get(0).getModificationMatches() == null);
        
        // Test no digestion modification
        peptides = iteratorModifications.getPeptides(testSequence, digestionPreferences, null, null);
        Assert.assertTrue(peptides.size() == 1);
        Assert.assertTrue(peptides.get(0).getSequence().equals(testSequence));
        Assert.assertTrue(peptides.get(0).getModificationMatches().size() == 3);
    
        
        
    }
}
