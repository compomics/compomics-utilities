package com.compomics.util.experiment.quantification;

import com.compomics.util.experiment.quantification.reporterion.quantification.ProteinQuantification;
import com.compomics.util.experiment.utils.ExperimentObject;

import java.util.ArrayList;

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
    protected ArrayList<ProteinQuantification> proteinQuantification = new ArrayList<ProteinQuantification>();

    /**
     * This method retrieves the quantification result at the protein level
     * @return quantification at the protein level
     */
    public ArrayList<ProteinQuantification> getProteinQuantification() {
        return proteinQuantification;
    }

    /**
     * Add protein quantification to the experiment
     * @param aProteinQuantification    The corresponding protein quantification
     */
    public void addProteinQuantification(ProteinQuantification aProteinQuantification) {
        proteinQuantification.add(aProteinQuantification);
    }

    /**
     * Add proteins quantification to the experiment
     * @param aProteinQuantification    The corresponding list of protein quantification
     */
    public void addProteinQuantification(ArrayList<ProteinQuantification> aProteinQuantification) {
        proteinQuantification.addAll(aProteinQuantification);
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
