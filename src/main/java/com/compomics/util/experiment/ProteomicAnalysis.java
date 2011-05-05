package com.compomics.util.experiment;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.quantification.Quantification;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.SequenceDataBase;
import com.compomics.util.experiment.massspectrometry.SpectrumCollection;

import java.util.HashMap;

/**
 * This class models a proteomic analysis.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 10:11:18 AM
 */
public class ProteomicAnalysis extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -6738411343333889777L;
    /**
     * the analysis index
     */
    private int index;
    /**
     * The acquired spectra
     */
    private SpectrumCollection spectrumCollection = new SpectrumCollection();
    /**
     * Quantification results
     */
    private HashMap<Integer, Quantification> quantification = new HashMap<Integer, Quantification>();
    /**
     * Identification results
     */
    private HashMap<Integer, Identification> identification = new HashMap<Integer, Identification>();
    /**
     * The sequence database used for identification
     */
    private SequenceDataBase sequenceDataBase;

    /**
     * constructor for a proteomic analysis
     * @param index the index of the replicate
     */
    public ProteomicAnalysis(int index) {
        this.index = index;
    }

    /**
     * get the index of the replicate
     * @return the index of the replicate
     */
    public int getIndex() {
        return index;
    }

    /**
     * adds quantification results to the current analysis
     * @param quantificationMethod      the quantification method used
     * @param quantificationResutls     the quantification resutls
     */
    public void addQuantificationResults(int quantificationMethod, Quantification quantificationResutls) {
        quantification.put(quantificationMethod, quantificationResutls);
    }

    /**
     * returns quantification results obtain with a quantification method
     * @param quantificationMethod  the quantification method used
     * @return the quantification resutls
     */
    public Quantification getQuantification(int quantificationMethod) {
        return quantification.get(quantificationMethod);
    }

    /**
     * Adds identification results obtained with an identification method
     * @param identificationMethod  the identification method used
     * @param identificationResults the identification resutls obtained
     */
    public void addIdentificationResults(int identificationMethod, Identification identificationResults) {
        identification.put(identificationMethod, identificationResults);
    }

    /**
     * Returns identification results obtained with an identification method
     * @param identificationMethod  the identification method used
     * @return the identification results
     */
    public Identification getIdentification(int identificationMethod) {
        return identification.get(identificationMethod);
    }

    /**
     * Returns the spectrum collection of the analysis
     * @return  the spectrum collection of the analysis
     */
    public SpectrumCollection getSpectrumCollection() {
        return spectrumCollection;
    }

    /**
     * Clears the spectrum collection of the analysis
     */
    public void clearSpectrumCollection() {
        spectrumCollection = new SpectrumCollection();
    }

    /**
     * Returns the sequence database used for identification
     * @return the sequence database used for identification
     */
    public SequenceDataBase getSequenceDataBase() {
        return sequenceDataBase;
    }

    /**
     * Sets the sequence database used for identification
     * @param sequenceDataBase the sequence database used for identification
     */
    public void setSequenceDataBase(SequenceDataBase sequenceDataBase) {
        this.sequenceDataBase = sequenceDataBase;
    }
}
