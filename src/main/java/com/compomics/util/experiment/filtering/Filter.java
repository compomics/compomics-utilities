package com.compomics.util.experiment.filtering;

import java.util.ArrayList;

/**
 * Generic interface for a filter.
 *
 * @author Marc Vaudel
 */
public interface Filter {

    /**
     * Returns the name of the filter.
     *
     * @return the name of the filter
     */
    public String getName();

    /**
     * Returns a description for the filter.
     *
     * @return a description for the filter
     */
    public String getDescription();

    /**
     * Returns a description of the condition to match for the filter to
     * validate.
     *
     * @return the condition to match for the filter to validate
     */
    public String getCondition();

    /**
     * Returns a filter report depending on whether the condition was met.
     *
     * @param filterPassed boolean indicating whether the filter was passed
     *
     * @return the report of the filter
     */
    public String getReport(boolean filterPassed);

    /**
     * Clones the filter.
     *
     * @return a clone of the filter
     */
    public Filter clone();

    /**
     * Indicates whether another filter is the same as the current filter.
     *
     * @param anotherFilter another filter
     *
     * @return a boolean indicating whether another filter is the same as the
     * current filter
     */
    public boolean isSameAs(Filter anotherFilter);
}
