package com.compomics.util.pdbfinder.das.readers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Vector;

/**
 * DasAnnotationServerAlingmentReader.
 *
 * @author Niklaas Colaert
 */
public class DasAnnotationServerAlingmentReader {

    /**
     * Empty default constructor
     */
    public DasAnnotationServerAlingmentReader() {
        iXml = "";
        iJson=null;
    }

    /**
     * The XML string to parse.
     */
    private final String iXml;
    private final JsonObject iJson;
    /**
     * The last feature end position.
     */
    private int lastFeatureEndPosition = 0;

    /**
     * Creates a new reader for a XML string.
     *
     * @param aXml the XML string
     */
    public DasAnnotationServerAlingmentReader(String aXml) {
        this.iXml = aXml;
        this.iJson = new Gson().fromJson(aXml, JsonObject.class);
    }

    /**
     * Get all alignment in the XML string.
     *
     * @return all alignment in the XML string
     */
    public DasAlignment[] getAllAlignments() {
        Vector alings = new Vector();
        JsonArray arr = iJson.getAsJsonArray(iJson.keySet().toArray()[0] + "");
        for (int i = 0; i < arr.size(); i++) {
            JsonObject alignment = arr.get(i).getAsJsonObject();   
            DasAlignment f = new DasAlignment(alignment);
            alings.add(f);

        }

//        while (iXml.indexOf("<alignment alignType=\"PDB_SP\">", lastFeatureEndPosition + 30) != -1) {
//            String alignment = iXml.substring(iXml.indexOf("<alignment alignType=\"PDB_SP\">", lastFeatureEndPosition), iXml.indexOf("</alignment>", lastFeatureEndPosition) + 12);
//            lastFeatureEndPosition = iXml.indexOf("</alignment>", lastFeatureEndPosition) + 5;
//            DasAlignment f = new DasAlignment(alignment);
//            alings.add(f);
//        }
        DasAlignment[] alignments = new DasAlignment[alings.size()];
        alings.toArray(alignments);
        return alignments;
    }
}
