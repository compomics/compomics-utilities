package com.compomics.util.experiment.identification.modification.peptide_mapping;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Functions for the mapping of modifications on peptides.
 *
 * @author Dafni Skiadopoulou
 * @author Marc Vaudel
 */
public class ModificationPeptideMapping {

    /**
     * 
     * 
     * @param modificationToPossibleSiteMap Map of modification mass to site to modification names.
     * @param modificationOccurrenceMap Map of modification mass to number of modifications.
     * @param modificationToSiteToScore Map of modification mass to modification site to localization score.
     */
    public static void mapModifications(
            HashMap<Double, HashMap<Integer, ArrayList<String>>> modificationToPossibleSiteMap,
            HashMap<Double, Integer> modificationOccurrenceMap,
            HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore
    ) {

    }

}
