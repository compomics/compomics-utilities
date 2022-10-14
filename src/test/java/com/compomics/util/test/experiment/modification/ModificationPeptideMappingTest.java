package com.compomics.util.test.experiment.modification;

import com.compomics.util.experiment.identification.modification.peptide_mapping.ModificationPeptideMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Test cases for the modification to peptide mapping.
 *
 * @author Marc Vaudel
 */
public class ModificationPeptideMappingTest extends TestCase {

    /**
     * Test the modification to peptide mapping.
     */
    public void testModificationMapping() {

        // Example 1: three modifications of two types, five sites, one possible conflict
        // Expected result: {mod1 -> (1), mod2 -> (3, 17)}
        double modMass1 = 1.0;
        double modMass2 = 2.0;
        int nMod1 = 1;
        int nMod2 = 2;
        int[] sites1 = new int[]{1, 3, 5};
        int[] sites2 = new int[]{3, 10, 17};
        double[] scores1 = new double[]{123.5, 10.4, 0.0};
        double[] scores2 = new double[]{95.3, 4.9, 51.7};

        /*HashMap<Double, HashMap<Integer, ArrayList<String>>> modificationToPossibleSiteMap = new HashMap<>(2);

        ArrayList<String> modNames1 = new ArrayList<>(1);
        modNames1.add("Modification1");
        HashMap<Integer, ArrayList<String>> modification1 = new HashMap<>(2);

        for (int site : sites1) {

            modification1.put(site, modNames1);

        }

        modificationToPossibleSiteMap.put(modMass1, modification1);

        ArrayList<String> modNames2 = new ArrayList<>(1);
        modNames2.add("Modification2");
        HashMap<Integer, ArrayList<String>> modification2 = new HashMap<>(2);

        for (int site : sites2) {

            modification2.put(site, modNames2);

        }

        modificationToPossibleSiteMap.put(modMass2, modification2);*/
        
        HashMap<Double, int[]> modificationToPossibleSiteMap = new HashMap<>(2);
        modificationToPossibleSiteMap.put(modMass1, sites1);
        modificationToPossibleSiteMap.put(modMass2, sites2);

        HashMap<Double, Integer> modificationOccurrenceMap = new HashMap<>(2);
        modificationOccurrenceMap.put(modMass1, nMod1);
        modificationOccurrenceMap.put(modMass2, nMod2);

        HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore = new HashMap<>(2);
        HashMap<Integer, Double> scores1Map = new HashMap<>(2);
        
        for (int i = 0; i < sites1.length ; i++) {
            
            scores1Map.put(sites1[i], scores1[i]);
            
        }
        
        modificationToSiteToScore.put(modMass1, scores1Map);
        
        HashMap<Integer, Double> scores2Map = new HashMap<>(2);
        
        for (int i = 0; i < sites2.length ; i++) {
            
            scores2Map.put(sites2[i], scores2[i]);
            
        }
        
        modificationToSiteToScore.put(modMass2, scores2Map);
        
        HashMap<Double, TreeSet<Integer>> matchedSiteByModification = ModificationPeptideMapping.mapModifications(
                modificationToPossibleSiteMap, 
                modificationOccurrenceMap, 
                modificationToSiteToScore
        );
        
        Assert.assertTrue(matchedSiteByModification.size() == 2);
        Assert.assertTrue(matchedSiteByModification.containsKey(modMass1));
        Assert.assertTrue(matchedSiteByModification.containsKey(modMass2));
        
        Assert.assertTrue(matchedSiteByModification.get(modMass1).size() == 1);
        Assert.assertTrue(matchedSiteByModification.get(modMass1).first() == 1);
        Assert.assertTrue(matchedSiteByModification.get(modMass2).size() == 2);
        Assert.assertTrue(matchedSiteByModification.get(modMass2).first() == 3 || matchedSiteByModification.get(modMass2).first() == 17);
        Assert.assertTrue(matchedSiteByModification.get(modMass2).last() == 3 || matchedSiteByModification.get(modMass2).last() == 17);
        
    }
}