package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.MarshallableParameter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Factory for the identification parameters.
 *
 * @author Marc Vaudel
 */
public class IdentificationParametersFactory {

    /**
     * Instance of the factory.
     */
    private static IdentificationParametersFactory instance = null;
    /**
     * The folder containing the parameters folder.
     */
    private static String PARENT_FOLDER = System.getProperty("user.home") + "/.compomics";
    /**
     * The identification parameters.
     */
    public static final String PARAMETERS_FOLDER = "identification_parameters";
    /**
     * The extension for a parameters file.
     */
    public static final String PARAMETERS_EXTENSION = ".par";
    /**
     * A map of the parsed parameters indexed by their name.
     */
    private HashMap<String, IdentificationParameters> identificationParametersMap = new HashMap<String, IdentificationParameters>();

    /**
     * Constructor for the factory.
     */
    private IdentificationParametersFactory() {
        File parametersFolder = getParametersFolder();
        if (!parametersFolder.exists()) {
            parametersFolder.mkdirs();
        }
        parseFolder();
    }

    /**
     * Static method to get the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static IdentificationParametersFactory getInstance() {
        if (instance == null) {
            instance = new IdentificationParametersFactory();
        }
        return instance;
    }

    /**
     * Returns an ordered list of the names of the implemented parameters.
     *
     * @return an ordered list of the names of the implemented parameters
     */
    public ArrayList<String> getParametersList() {
        ArrayList<String> names = new ArrayList<String>(identificationParametersMap.keySet());
        Collections.sort(names);
        return names;
    }

    /**
     * Returns the identification parameters corresponding to the given name.
     * Null if not found.
     *
     * @param name the name of the parameters to return
     *
     * @return the identification parameters corresponding to the given name
     */
    public IdentificationParameters getIdentificationParameters(String name) {
        return identificationParametersMap.get(name);
    }

    /**
     * Returns the identification parameters corresponding to the given name.
     * Null if not found.
     *
     * @param name the name of the parameters to return
     *
     * @return the identification parameters corresponding to the given name
     */
    public static File getIdentificationParametersFile(String name) {
        return new File(getParametersFolder(), name + PARAMETERS_EXTENSION);
    }

    /**
     * Deletes the identification parameters of the given name.
     *
     * @param name the name of the parameters to delete
     */
    public void removeIdentificationParameters(String name) {
        identificationParametersMap.remove(name);
        File parametersFile = getIdentificationParametersFile(name);
        parametersFile.delete();
    }

    /**
     * Adds the given identification parameters to the factory.
     *
     * @param identificationParameters the identification parameters
     *
     * @throws IOException exception thrown whenever an error occurred while
     * saving the file
     * @throws FileNotFoundException exception thrown whenever an error occurred
     * while saving the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while saving the file
     */
    public void addIdentificationParameters(IdentificationParameters identificationParameters) throws IOException, FileNotFoundException, ClassNotFoundException {
        String parametersName = identificationParameters.getName();
        if (parametersName == null || parametersName.length() == 0) {
            throw new IllegalArgumentException("Parameters name not set or empty.");
        }
        File parametersFile = getIdentificationParametersFile(parametersName);
        IdentificationParameters.saveIdentificationParameters(identificationParameters, parametersFile);
        identificationParametersMap.put(identificationParameters.getName(), identificationParameters);
    }

    /**
     * Replaces old parameters by new.
     *
     * @param oldParameters the old identification parameters
     * @param newParameters the new identification parameters
     *
     * @throws IOException exception thrown whenever an error occurred while
     * saving the file
     * @throws FileNotFoundException exception thrown whenever an error occurred
     * while saving the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while saving the file
     */
    public void updateIdentificationParameters(IdentificationParameters oldParameters, IdentificationParameters newParameters) throws IOException, FileNotFoundException, ClassNotFoundException {
        removeIdentificationParameters(oldParameters.getName());
        addIdentificationParameters(newParameters);
    }

    /**
     * Returns the parameters folder.
     *
     * @return the parameters folder
     */
    private static File getParametersFolder() {
        return new File(PARENT_FOLDER, PARAMETERS_FOLDER);
    }

    /**
     * Returns the parent folder.
     *
     * @return the parent folder
     */
    public static String getParentFolder() {
        return PARENT_FOLDER;
    }

    /**
     * Set the parent folder.
     *
     * @param PARENT_FOLDER the parent folder
     */
    public static void setParentFolder(String PARENT_FOLDER) {
        IdentificationParametersFactory.PARENT_FOLDER = PARENT_FOLDER;
    }

    /**
     * Parses the parameters files in the parameters folder.
     */
    private void parseFolder() {
        for (File parameterFile : getParametersFolder().listFiles()) {
            if (parameterFile.getName().endsWith(PARAMETERS_EXTENSION)) {
                try {
                    // there should be only IdentificationParameters 
                    IdentificationParametersMarshaller jsonMarshaller = new IdentificationParametersMarshaller();
                    Class expectedObjectType = IdentificationParameters.class;
                    Object object = jsonMarshaller.fromJson(expectedObjectType, parameterFile);
                    IdentificationParameters identificationParameters = (IdentificationParameters) object;

                    // avoid incorrectly parsed parameters
                    if (identificationParameters.getType() == MarshallableParameter.Type.identification_parameters) {
                        identificationParametersMap.put(identificationParameters.getName(), identificationParameters);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // Not a valid parameters file
                }
            }
        }
    }
}
