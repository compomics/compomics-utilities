package com.compomics.util.pdbfinder.das.readers;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas Colaert
 * Date: 22-jan-2008
 * Time: 14:07:28
 */
public class DasAnnotationServerResultReader {

    private String iXml;
    private int lastFeatureEndPosition = 0;

    /**
     * Creates a new reader for an xml string.
     * 
     * @param aXml 
     */
    public DasAnnotationServerResultReader(String aXml) {
        this.iXml = aXml;
    }
    
    /**
     * Get the next feature in the xml string.
     * 
     * @return the next feature in the xml string
     */
    public DasFeature getNextFeature() {
        String feature = iXml.substring(iXml.indexOf("<FEATURE", lastFeatureEndPosition + 9), iXml.indexOf("</FEATURE>", lastFeatureEndPosition + 9) + 10);
        lastFeatureEndPosition = iXml.indexOf("</FEATURE>", lastFeatureEndPosition + 9);
        DasFeature f = new DasFeature(feature);
        return f;
    }
    
    /**
     * Get all features in the xml string.
     * 
     * @return all features in the xml string
     */
    public DasFeature[] getAllFeatures() {
        Vector feats = new Vector();
        while (iXml.indexOf("<FEATURE", lastFeatureEndPosition + 9) != -1) {
            String feature = iXml.substring(iXml.indexOf("<FEATURE", lastFeatureEndPosition + 9), iXml.indexOf("</FEATURE>", lastFeatureEndPosition + 9) + 10);
            lastFeatureEndPosition = iXml.indexOf("</FEATURE>", lastFeatureEndPosition + 9);
            if (feature.indexOf("<NOTE>No features found for the segment</NOTE>") < 0) {
                DasFeature f = new DasFeature(feature);
                feats.add(f);
            }
        }
        DasFeature[] features = new DasFeature[feats.size()];
        feats.toArray(features);
        return features;
    }
}
