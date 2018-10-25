package com.compomics.util.memory;

/**
 * This class provides information on the memory consumption status.
 *
 * @author Marc Vaudel
 */
public class MemoryConsumptionStatus {

    /**
     * Empty default constructor
     */
    public MemoryConsumptionStatus() {
    }

    /**
     * Indicates whether a GB of memory is free.
     *
     * @return a boolean indicating whether a GB of memory is free
     */
    public static boolean halfGbFree() {
        return Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) > 536870912;
    }

    /**
     * Returns the share of memory being used.
     *
     * @return the share of memory being used
     */
    public static double memoryUsed() {
        long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        float memoryAvailable = Runtime.getRuntime().maxMemory();
        double ratio = (double) (memoryUsed / memoryAvailable);
        return ratio;
    }
}
