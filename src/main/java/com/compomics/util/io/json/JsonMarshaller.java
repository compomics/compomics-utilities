package com.compomics.util.io.json;

/**
 *
 * @author Kenneth Verheggen
 */
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
 * This class is intended to convert non specific objects to the json format and vice versa
 * 
*/
public class JsonMarshaller {

    /**
     * a GSON parser instance to convert json to java objects and back
     */
    protected Gson gson = new Gson();
    /**
     * a GsonBuilder that can be used to append interfaces so the parser knows
     * how to handle them
     */
    private final GsonBuilder builder;

    /**
     * Default constructor
     */
    public JsonMarshaller() {
        this.builder = new GsonBuilder();
        builder.registerTypeAdapter(File.class,new FileAdapter());
        gson = builder.create();
    }

    /**
     *
     * @param interfaces comma separated list of interfaces that this class is
     * using. They will automatically be added using a custom InterfaceAdapter
     */
    public JsonMarshaller(Class... interfaces) {
        this.builder = new GsonBuilder();
        builder.registerTypeAdapter(File.class,new FileAdapter());
        //register required interfaceAdapters
        for (Class aClass : interfaces) {
            builder.registerTypeAdapter(aClass, new InterfaceAdapter<>());
        }
        gson = builder.create();
    }

    /**
     *
     * @param anObject the input object
     * @return the json representation of an object
     */
    public String toJson(Object anObject) {
        return gson.toJson(anObject).replace("}", "}" + System.lineSeparator());
    }

    /**
     *
     * @param anObject the input object
     * @param jsonFile the target file to which the json will be saved.
     * @throws IOException
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
     *
     * @param objectType The class the object belongs to
     * @param jsonString The string representation of the json object
     * @return an instance of the objectType containing the json information
     */
    public Object fromJson(Class objectType, String jsonString) {
        return gson.fromJson(jsonString, objectType);
    }

    /**
     *
     * @param objectType The class the object belongs to
     * @param jsonFile a json file
     * @return an instance of the objectType containing the json information
     * @throws IOException
     */
    public Object fromJson(Class objectType, File jsonFile) throws IOException {
        String jsonString = getJsonStringFromFile(jsonFile);
        return gson.fromJson(jsonString, objectType);
    }

    /**
     *
     * @param jsonFile the input json file
     * @return the string representation of the json content
     * @throws FileNotFoundException
     * @throws IOException
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
