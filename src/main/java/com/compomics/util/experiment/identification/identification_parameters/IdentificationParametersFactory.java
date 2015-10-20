package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.Advocate;
import static com.compomics.util.experiment.identification.IdentificationMatch.MatchType.PTM;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MyriMatchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.FragmentationMethod;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.preferences.IdentificationParameters;
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
     * The folder containing the paramters folder.
     */
    private static String PARENT_FOLDER = System.getProperty("user.home") + "/.compomics";
    /**
     * The identification parameters.
     */
    private static String PARAMETERS_FOLDER = "identification_parameters";
    /**
     * The extension for a parameters file.
     */
    public static final String parametersExtension = ".par";
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
     * Deletes the identification parameters of the given name.
     *
     * @param name the name of the parameters to delete
     */
    public void removeIdentificationParameters(String name) {
        File parametersFile = new File(getParametersFolder(), name + "." + parametersExtension);
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
        File parametersFile = new File(getParametersFolder(), identificationParameters.getName() + "." + parametersExtension);
        identificationParameters.getSearchParameters().setParametersFile(parametersFile);
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
    private File getParametersFolder() {
        return new File(PARENT_FOLDER, PARAMETERS_FOLDER);
    }

    /**
     * Parses the parameters files in the parameters folder.
     */
    private void parseFolder() {
        for (File parameterFile : getParametersFolder().listFiles()) {
            if (parameterFile.getName().endsWith(parametersExtension)) {
                try {
                    IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(parameterFile);
                    identificationParametersMap.put(identificationParameters.getName(), identificationParameters);
                } catch (Exception e) {
                    // Not a valid parameters file
                }
            }
        }
    }

}
