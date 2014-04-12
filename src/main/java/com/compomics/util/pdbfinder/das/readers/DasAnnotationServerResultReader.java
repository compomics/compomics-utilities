package com.compomics.util.pdbfinder.das.readers;

import java.util.Vector;

/**
 * DasAnnotationServerResultReader
 *
 * @author Niklaas Colaert
 */
public class DasAnnotationServerResultReader {

    /**
     * The XML string to parse.
     */
    private String iXml;
    /**
     * The last feature end position.
     */
    private int lastFeatureEndPosition = 0;

    /**
     * Creates a new reader for an XML string.
     *
     * @param aXml
     */
    public DasAnnotationServerResultReader(String aXml) {
        this.iXml = aXml;
    }

    /**
     * Get the next feature in the XML string.
     *
     * @return the next feature in the XML string
     */
    public DasFeature getNextFeature() {
        String feature = iXml.substring(iXml.indexOf("<FEATURE", lastFeatureEndPosition + 9), iXml.indexOf("</FEATURE>", lastFeatureEndPosition + 9) + 10);
        lastFeatureEndPosition = iXml.indexOf("</FEATURE>", lastFeatureEndPosition + 9);
        DasFeature f = new DasFeature(feature);
        return f;
    }

    /**
     * Get all features in the XML string.
     *
     * @return all features in the XML string
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
