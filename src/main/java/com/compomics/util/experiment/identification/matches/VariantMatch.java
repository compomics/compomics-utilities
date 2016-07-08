package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.variants.Variant;

/**
 * Class capturing the information on a variant.
 *
 * @author Marc Vaudel
 */
public class VariantMatch {
    
    /**
     * The variant.
     */
    private Variant variant;
    /**
     * The site on the peptide. 0 is the first amino acid. For a swap, the amino acid to the left is the site.
     */
    private int site;
    
    /**
     * Constructor.
     * 
     * @param variant the variant found
     * @param site the site
     */
    public VariantMatch(Variant variant, int site) {
        
    }

    /**
     * Returns the variant.
     * 
     * @return the variant
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * Returns the site on the peptide, 0 is the first amino acid.
     * 
     * @return the site on the peptide
     */
    public int getSite() {
        return site;
    }
    
}
