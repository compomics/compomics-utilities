package com.compomics.util.pdbfinder.das.readers;

import java.util.Vector;

/**
 * DasAnnotationServerAlingmentReader.
 * 
 * @author  Niklaas Colaert
 */
public class DasAnnotationServerAlingmentReader {

    /**
     * The XML string to parse.
     */
    private String iXml;
    /**
     * The last feature end position.
     */
    private int lastFeatureEndPosition = 0;

    /**
     * Creates a new reader for a XML string.
     * 
     * @param aXml 
     */
    public DasAnnotationServerAlingmentReader(String aXml) {
        this.iXml = aXml;
    }
    
    /**
     * Get all alignment in the XML string.
     * 
     * @return all alignment in the XML string
     */
    public DasAlignment[] getAllAlignments() {
        Vector alings = new Vector();
        while (iXml.indexOf("<alignment alignType=\"PDB_SP\">", lastFeatureEndPosition + 30) != -1) {
            String alignment = iXml.substring(iXml.indexOf("<alignment alignType=\"PDB_SP\">", lastFeatureEndPosition), iXml.indexOf("</alignment>", lastFeatureEndPosition) + 12);
            lastFeatureEndPosition = iXml.indexOf("</alignment>", lastFeatureEndPosition) + 5;
            DasAlignment f = new DasAlignment(alignment);
            alings.add(f);
        }
        DasAlignment[] alignments = new DasAlignment[alings.size()];
        alings.toArray(alignments);
        return alignments;
    }
}
