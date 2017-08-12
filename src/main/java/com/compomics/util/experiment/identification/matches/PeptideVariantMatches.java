package com.compomics.util.experiment.identification.matches;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.biology.variants.Variant;
import java.util.HashMap;

/**
 * This class represents a set of variants found on a peptide.
 *
 * @author Marc Vaudel
 */
public class PeptideVariantMatches extends DbObject {
    
    private int proteinSequence;
    
    private HashMap<Integer, Variant> variantMatches;
    
    

}
