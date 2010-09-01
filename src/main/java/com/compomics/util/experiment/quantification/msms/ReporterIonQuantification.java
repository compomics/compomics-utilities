package com.compomics.util.experiment.quantification.msms;

import com.compomics.util.experiment.biology.Sample;
import com.compomics.util.experiment.quantification.QuantificationMethod;
import com.compomics.util.experiment.quantification.msms.ProteinQuantification;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * This class will store matches of the reporter ions.
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
     * The protein quantification
     */
    private ArrayList<ProteinQuantification> proteinQuantification = new ArrayList<ProteinQuantification>();

    /**
     * Constructor for the reporter ion quantifiation
     * @param methodIndex   the index of the method as specified in QuantificationMethod
     */
    public ReporterIonQuantification(int methodIndex) {
        this.index = methodIndex;
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

}
