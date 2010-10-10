package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.Sample;
import com.compomics.util.experiment.quantification.QuantificationMethod;
import com.compomics.util.experiment.quantification.reporterion.quantification.ProteinQuantification;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * This class will contain quantification results.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 1:42:21 PM
 */
public class ReporterIonQuantification extends QuantificationMethod {

    /**
     * The sample assignement to the various ions indexed by their static indexes
     */
    private HashMap<Integer, Sample> sampleAssignement = new HashMap<Integer, Sample>();

    /**
     * The reference label indexed by the reporter ion index
     */
    private int referenceLabel;

    /**
     * The reporter method used
     */
    private ReporterMethod method;

    /**
     * The protein quantification
     */
    private ArrayList<ProteinQuantification> proteinQuantification = new ArrayList<ProteinQuantification>();

    /**
     * Constructor for the reporter ion quantifiation
     */
    public ReporterIonQuantification() {
    }

    /**
     * assign a sample to an ion referenced by its static index
     * @param reporterIndex     the index of the ion
     * @param sample            the sample
     */
    public void assignSample(int reporterIndex, Sample sample) {
        sampleAssignement.put(reporterIndex, sample);
    }

    /**
     * This method returns the sample associated to the given ion
     * @param reporterIndex     the static index of the reporter ion
     * @return the corresponding sample
     */
    public Sample getSample(int reporterIndex) {
        return sampleAssignement.get(reporterIndex);
    }

    /**
     * This method returns the ion associated to the given sample
     * @param aSample   the sample
     * @return the static index of the associated ion
     */
    public Integer getReporterIndex(Sample aSample) {
        for(int index : sampleAssignement.keySet()) {
            if (sampleAssignement.get(index).isSameAs(aSample)) {
                return index;
            }
        }
        return null;
    }

    /**
     * This method retrieves the quantification result at the protein level
     * @return quantification at the protein level
     */
    public ArrayList<ProteinQuantification> getProteinQuantification() {
        return proteinQuantification;
    }

    /**
     * This method adds a protein quanditification result
     * @param aProteinQuantification quantification result for a protein
     */
    public void addProteinQuantification(ProteinQuantification aProteinQuantification) {
        proteinQuantification.add(aProteinQuantification);
    }

    /**
     * returns the method used
     * @return the method used
     */
    public ReporterMethod getMethod() {
        return method;
    }

    /**
     * Sets the method used
     * @param method    the reporter method used
     */
    public void setMethod(ReporterMethod method) {
        this.method = method;
    }

    /**
     * Returns the reference label indexed by the corresponding reporter ion
     * @return the index of the reporter ion corresponding to the reference label
     */
    public int getReferenceLabel() {
        return referenceLabel;
    }

    /**
     * Sets the reference label indexed by the corresponding reporter ion index
     * @param referenceLabel    the index of the reporter ion corresponding to the reference label
     */
    public void setReferenceLabel(int referenceLabel) {
        this.referenceLabel = referenceLabel;
    }
}
