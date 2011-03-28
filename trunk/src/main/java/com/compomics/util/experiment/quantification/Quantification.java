package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.quantification.reporterion.quantification.ProteinQuantification;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.HashMap;

/**
 * This class contains quantification results.
 * User: Marc
 * Date: Nov 11, 2010
 * Time: 3:46:24 PM
 */
public abstract class Quantification extends ExperimentObject {

    /**
     * The quantification method used
     */
    protected int methodUsed;

    /**
     * The protein quantification
     */
    protected HashMap<String, ProteinQuantification> proteinQuantification = new HashMap<String, ProteinQuantification>();

    /**
     * This method retrieves the quantification result at the protein level
     * @return quantification at the protein level
     */
    public HashMap<String, ProteinQuantification> getProteinQuantification() {
        return proteinQuantification;
    }

    /**
     * returns a specific protein quantification
     * @param index the index of the desired protein quantification
     * @return      the desired proteins quantification
     */
    public ProteinQuantification getProteinQuantification(String index) {
        return proteinQuantification.get(index);
    }

    /**
     * Add protein quantification to the experiment
     * @param aProteinQuantification    The corresponding protein quantification
     */
    public void addProteinQuantification(ProteinQuantification aProteinQuantification) {
        proteinQuantification.put(aProteinQuantification.getProteinMatch().getKey(), aProteinQuantification);
    }

    /**
     * getter for the method used
     * @return the method used
     */
    public int getMethodUsed() {
        return methodUsed;
    }

    /**
     * setter for the method used
     * @param methodUsed the method used
     */
    public void setMethodUsed(int methodUsed) {
        this.methodUsed = methodUsed;
    }
}
