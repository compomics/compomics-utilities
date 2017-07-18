package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.variants.Variant;
import org.zoodb.api.impl.ZooPC;

/**
 * Class capturing the information on a variant.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 */
public class VariantMatch extends ZooPC {
    
    /**
     * The variant.
     */
    private Variant variant;
    /**
     * The accession of the protein where this variant was found.
     */
    private String proteinAccession;
    /**
     * The site on the peptide. 0 is the first amino acid. For a swap, the amino acid to the left is the site.
     */
    private int site;
    
    /**
     * Constructor.
     * 
     * @param variant the variant found
     * @param proteinAccession the accession of the protein where this variant was found
     * @param site the site
     */
    public VariantMatch(Variant variant, String proteinAccession, int site) {
        this.variant = variant;
        this.proteinAccession = proteinAccession;
        this.site = site;
    }

    /**
     * Returns the variant.
     * 
     * @return the variant
     */
    public Variant getVariant() {
        zooActivateRead();
        return variant;
    }

    /**
     * Returns the site on the peptide, 0 is the first amino acid.
     * 
     * @return the site on the peptide
     */
    public int getSite() {
        zooActivateRead();
        return site;
    }

    /**
     * Returns the protein accession where this variant was found.
     * 
     * @return the protein accession where this variant was found
     */
    public String getProteinAccession() {
        zooActivateRead();
        return proteinAccession;
    }
    
    public void setProteinAccession(String proteinAccession){
        zooActivateWrite();
        this.proteinAccession = proteinAccession;
    }
    
    public void setSite(int site){
        zooActivateWrite();
        this.site = site;
    }
    
    public void setVariant(Variant variant){
        zooActivateWrite();
        this.variant = variant;
    }
}
