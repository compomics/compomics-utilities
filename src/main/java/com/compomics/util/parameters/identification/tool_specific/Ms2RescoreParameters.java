package com.compomics.util.parameters.identification.tool_specific;

/**
 * This class contains Ms2Rescore specific parameters.
 *
 * @author Marc Vaudel
 */
public class Ms2RescoreParameters {
    
    /**
     * Boolean indicating whether Ms2Rescore should be run.
     */
    private boolean runMs2Rescore = false;
    /**
     * The path to the feature input file to provide to Ms2Rescore.
     */
    private String rescoreFeaturesFilePath;
    
    /**
     * Constructor.
     */
    public Ms2RescoreParameters() {
        
    }

    /**
     * Returns a boolean indicating whether Ms2Rescore should be run.
     * 
     * @return A boolean indicating whether Ms2Rescore should be run.
     */
    public boolean runMs2Rescore() {
        return runMs2Rescore;
    }

    /**
     * Sets whether Ms2Rescore should be run.
     * 
     * @param runMs2Rescore A boolean indicating whether Ms2Rescore should be run.
     */
    public void setRunMs2Rescore(boolean runMs2Rescore) {
        this.runMs2Rescore = runMs2Rescore;
    }

    /**
     * Returns the path to the feature input file to provide to Ms2Rescore.
     * 
     * @return The path to the feature input file to provide to Ms2Rescore.
     */
    public String getRescoreFeaturesFilePath() {
        return rescoreFeaturesFilePath;
    }

    /**
     * Sets the path to the feature input file to provide to Ms2Rescore.
     * 
     * @param rescoreFeaturesFilePath The path to the feature input file to provide to Ms2Rescore.
     */
    public void setRescoreFeaturesFilePath(String rescoreFeaturesFilePath) {
        this.rescoreFeaturesFilePath = rescoreFeaturesFilePath;
    }
    
}
