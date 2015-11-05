package com.compomics.util.preferences;

import java.io.File;
import java.io.Serializable;

/**
 * Generic class grouping the protein inference preferences.
 *
 * @author Marc Vaudel
 */
public class ProteinInferencePreferences implements Serializable {

    /**
     * Serial version UID for backward compatibility.
     */
    static final long serialVersionUID = 447785006299636157L;
    /**
     * The database to use for protein inference.
     */
    private File proteinSequenceDatabase;

    /**
     * Returns the path to the database used.
     *
     * @return the path to the database used
     */
    public File getProteinSequenceDatabase() {
        return proteinSequenceDatabase;
    }

    /**
     * Sets the path to the database used.
     *
     * @param proteinSequenceDatabase the path to the database used
     */
    public void setProteinSequenceDatabase(File proteinSequenceDatabase) {
        this.proteinSequenceDatabase = proteinSequenceDatabase;
    }
    
    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        
        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        output.append("DB: ").append(proteinSequenceDatabase.getName()).append(".").append(newLine);

        return output.toString();
    }
}
