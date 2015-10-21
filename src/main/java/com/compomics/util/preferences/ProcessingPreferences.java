package com.compomics.util.preferences;

/**
 * ProcessingPreferences.
 *
 * @author Marc Vaudel
 */
public class ProcessingPreferences {

    /**
     * The processing type.
     */
    private ProcessingType processingType = ProcessingType.Local;
    /**
     * The number of threads to use.
     */
    private int nThreads = Math.max(Runtime.getRuntime().availableProcessors(), 1);

    /**
     * Constructor.
     */
    public ProcessingPreferences() {

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
