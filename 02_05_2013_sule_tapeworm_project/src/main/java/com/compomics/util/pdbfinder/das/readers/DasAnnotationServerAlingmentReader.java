package com.compomics.util.pdbfinder.das.readers;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas Colaert
 * Date: 25-jan-2008
 * Time: 9:01:48
 */
public class DasAnnotationServerAlingmentReader {

    private String iXml;
    private int lastFeatureEndPosition = 0;

    /**
     * Creates a new reader for a xml string.
     * 
     * @param aXml 
     */
    public DasAnnotationServerAlingmentReader(String aXml) {
        this.iXml = aXml;
    }
    
    /**
     * Get all alingment in the xml string.
     * 
     * @return all alingment in the xml string
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
