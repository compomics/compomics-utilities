package com.compomics.util.pdbfinder.das.readers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;

/**
 * DasAnnotationServerAlingmentReader.
 *
 * @author Niklaas Colaert
 * @author Yehia Farag
 * @author Harald Barsnes
 */
public class DasAnnotationServerAlingmentReader {

    /**
     * Empty default constructor.
     */
    public DasAnnotationServerAlingmentReader() {
        iJson = null;
    }

    /**
     * The json object.
     */
    private final JsonObject iJson;

    /**
     * Creates a new reader for a json string.
     *
     * @param aJson the json string
     */
    public DasAnnotationServerAlingmentReader(String aJson) {
        this.iJson = new Gson().fromJson(aJson, JsonObject.class);
    }

    /**
     * Get all alignments in the json string.
     *
     * @return all alignments in the json string
     */
    public DasAlignment[] getAllAlignments() {

        ArrayList<DasAlignment> alings = new ArrayList();
        JsonArray arr = iJson.getAsJsonArray(iJson.keySet().toArray()[0] + "");

        for (int i = 0; i < arr.size(); i++) {
            JsonObject alignment = arr.get(i).getAsJsonObject();
            DasAlignment f = new DasAlignment(alignment);
            alings.add(f);

        }

        DasAlignment[] alignments = new DasAlignment[alings.size()];
        alings.toArray(alignments);
        return alignments;

    }
}
