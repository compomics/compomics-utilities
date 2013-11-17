package com.compomics.util.experiment.patient;

import java.util.HashMap;

/**
 * This class groups the information about the patient from whom the sample is
 * taken
 *
 * @author Marc Vaudel
 */
public class Patient {

    /**
     * The patient id,
     */
    private String id;
    /**
     * The patient information as a map. See the PatientInformation class for
     * how to annotate the key.
     */
    private HashMap<String, Comparable> patientInformationMap = new HashMap<String, Comparable>();

    /**
     * Constructor,
     *
     * @param id the patient number
     */
    public Patient(String id) {
        this.id = id;
    }

    /**
     * Returns the patient id,
     *
     * @return the patient id
     */
    public String getId() {
        return id;
    }

    /**
     * Adds patient information,
     *
     * @param patientInformation the type of patient information to add
     * @param value the value
     */
    public void addPatientInformation(PatientInformation patientInformation, Comparable value) {
        patientInformationMap.put(patientInformation.getType(), value);
    }

    /**
     * Returns patient information,
     *
     * @param patientInformation the type of patient information desired
     * @return the value for this patient. Null if not set.
     */
    public Comparable getPatientInformation(PatientInformation patientInformation) {
        return patientInformationMap.get(patientInformation.getType());
    }
}
