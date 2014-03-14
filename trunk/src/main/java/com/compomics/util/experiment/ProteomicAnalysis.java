package com.compomics.util.experiment;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.quantification.Quantification;
import com.compomics.util.experiment.identification.Identification;

import java.util.HashMap;

/**
 * This class models a proteomic analysis.
 *
 * @author Marc Vaudel
 */
public class ProteomicAnalysis extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -6738411343333889777L;
    /**
     * The analysis index.
     */
    private int index;
    /**
     * Quantification results indexed by the method used.
     */
    private HashMap<Quantification.QuantificationMethod, Quantification> quantification = new HashMap<Quantification.QuantificationMethod, Quantification>();
    /**
     * Identification results.
     */
    private HashMap<Integer, Identification> identification = new HashMap<Integer, Identification>();

    /**
     * Constructor for a proteomic analysis.
     *
     * @param index the index of the replicate
     */
    public ProteomicAnalysis(int index) {
        this.index = index;
    }

    /**
     * Get the index of the replicate.
     *
     * @return the index of the replicate
     */
    public int getIndex() {
        return index;
    }

    /**
     * Adds quantification results to the current analysis.
     *
     * @param quantificationMethod the quantification method used
     * @param quantificationResutls the quantification results
     */
    public void addQuantificationResults(Quantification.QuantificationMethod quantificationMethod, Quantification quantificationResutls) {
        quantification.put(quantificationMethod, quantificationResutls);
    }

    /**
     * Returns quantification results obtain with a quantification method.
     *
     * @param quantificationMethod the quantification method used
     * @return the quantification results
     */
    public Quantification getQuantification(Quantification.QuantificationMethod quantificationMethod) {
        return quantification.get(quantificationMethod);
    }

    /**
     * Adds identification results obtained with an identification method.
     *
     * @param identificationMethod the identification method used
     * @param identificationResults the identification results obtained
     */
    public void addIdentificationResults(int identificationMethod, Identification identificationResults) {
        identification.put(identificationMethod, identificationResults);
    }

    /**
     * Returns identification results obtained with an identification method.
     *
     * @param identificationMethod the identification method used
     * @return the identification results
     */
    public Identification getIdentification(int identificationMethod) {
        return identification.get(identificationMethod);
    }
}
