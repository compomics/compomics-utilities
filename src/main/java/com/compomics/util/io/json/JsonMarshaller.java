package com.compomics.util.io.json;

import com.compomics.util.io.json.adapter.FileAdapter;
import com.compomics.util.io.json.adapter.InterfaceAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class is intended to convert non specific objects to the JSON format and
 * vice versa.
 *
 * @author Kenneth Verheggen
 */
public class JsonMarshaller {

    /**
     * GSON parser instance to convert JSON to Java objects and back.
     */
    protected Gson gson = new Gson();
    /**
     * GsonBuilder that can be used to append interfaces so the parser knows how
     * to handle them.
     */
    private final GsonBuilder builder;

    /**
     * Default constructor.
     */
    public JsonMarshaller() {
        this.builder = new GsonBuilder();
        builder.registerTypeAdapter(File.class, new FileAdapter());
        gson = builder.create();
    }

    /**
     * Constructor.
     *
     * @param interfaces comma separated list of interfaces that this class is
     * using. They will automatically be added using a custom InterfaceAdapter
     */
    public JsonMarshaller(Class... interfaces) {
        this.builder = new GsonBuilder();
        builder.registerTypeAdapter(File.class, new FileAdapter());
        //register required interfaceAdapters
        for (Class aClass : interfaces) {
            builder.registerTypeAdapter(aClass, new InterfaceAdapter<>());
        }
        gson = builder.create();
    }

    /**
     * Convert an object to JSON.
     *
     * @param anObject the input object
     * @return the JSON representation of an object
     */
    public String toJson(Object anObject) {
        return gson.toJson(anObject).replace("}", "}" + System.lineSeparator());
    }

    /**
     * Save an object to JSON.
     *
     * @param anObject the input object
     * @param jsonFile the target file to which the JSON will be saved.
     * @throws IOException if the object cannot be successfully saved into a
     * JSON file
     */
    public void saveObjectToJson(Object anObject, File jsonFile) throws IOException {
        if (!jsonFile.getName().toLowerCase().endsWith(".json")) {
            jsonFile = new File(jsonFile.getAbsolutePath() + ".json");
        }
        try (
                FileWriter out = new FileWriter(jsonFile)) {
            out.append(toJson(anObject)).flush();
        }
    }

    /**
     * Convert from JSON to object.
     *
     * @param objectType the class the object belongs to
     * @param jsonString the string representation of the JSON object
     * @return an instance of the objectType containing the JSON information
     */
    public Object fromJson(Class objectType, String jsonString) {
        return gson.fromJson(jsonString, objectType);
    }

    /**
     * Convert from JSON to object.
     *
     * @param objectType the class the object belongs to
     * @param jsonFile a JSON file
     * @return an instance of the objectType containing the JSON information
     * @throws IOException if the object cannot be successfully read from a JSON
     * file
     */
    public Object fromJson(Class objectType, File jsonFile) throws IOException {
        String jsonString = getJsonStringFromFile(jsonFile);
        return gson.fromJson(jsonString, objectType);
    }

    /**
     * Convert JSON string from file.
     * 
     * @param jsonFile the input JSON file
     * @return the string representation of the JSON content
     * @throws FileNotFoundException if the JSON file can not be reached
     * @throws IOException if the object cannot be successfully read from a JSON
     * file
     */
    protected String getJsonStringFromFile(File jsonFile) throws FileNotFoundException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile)));
        String line;
        while ((line = in.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}
