package com.compomics.util.experiment.patient;

/**
 * Information about the patient.
 *
 * @author Marc Vaudel
 */
public class PatientInformation {

    /**
     * The type of information, for example "Age".
     */
    private String type;
    /**
     * The description for example "Age at diagnostic".
     */
    private String description;

    /**
     * Constructor.
     *
     * @param type the type of information
     * @param description the description of the information
     */
    public PatientInformation(String type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * Returns the type of information.
     *
     * @return the type of information
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of information.
     *
     * @param type the type of information
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the description of the information.
     *
     * @return the description of the information
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the information.
     *
     * @param description the description of the information
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
