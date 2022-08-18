package com.compomics.util.parameters.tools;

/**
 * ProcessingPreferences.
 *
 * @author Marc Vaudel
 */
public class ProcessingParameters {

    /**
     * The processing type.
     */
    private ProcessingType processingType = ProcessingType.Local;
    /**
     * The number of threads to use.
     */
    private int nThreads = Math.max(Runtime.getRuntime().availableProcessors(), 1);
    /**
     * Boolean indicating whether Percolator features should be cached.
     */
    private boolean cachePercolatorFeatures = false;

    /**
     * Constructor.
     */
    public ProcessingParameters() {

    }

    /**
     * Returns the number or threads to use.
     *
     * @return the number or threads to use
     */
    public int getnThreads() {
        return nThreads;
    }

    /**
     * Sets the number or threads to use.
     *
     * @param nThreads the number or threads to use
     */
    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    /**
     * Returns a boolean indicating whether Percolator features should be cached.
     * 
     * @return A boolean indicating whether Percolator features should be cached.
     */
    public boolean cachePercolatorFeatures() {
        return cachePercolatorFeatures;
    }

    /**
     * Sets a boolean indicating whether Percolator features should be cached.
     * 
     * @param cachePercolatorFeatures A boolean indicating whether Percolator features should be cached.
     */
    public void setCachePercolatorFeatures(boolean cachePercolatorFeatures) {
        this.cachePercolatorFeatures = cachePercolatorFeatures;
    }
    
    

    /**
     * Returns the processing type.
     *
     * @return the processing type
     */
    public ProcessingType getProcessingType() {
        return processingType;
    }

    /**
     * Sets the processing type.
     *
     * @param processingType the processing type
     */
    public void setProcessingType(ProcessingType processingType) {
        this.processingType = processingType;
    }

    /**
     * Class indicating the type of processing.
     */
    public enum ProcessingType {

        Local;

    }
}
