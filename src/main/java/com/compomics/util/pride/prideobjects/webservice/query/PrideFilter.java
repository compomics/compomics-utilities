package com.compomics.util.pride.prideobjects.webservice.query;

/**
 * An object to set up a filter for use in a query to the PRIDE webservice.
 *
 * @author Kenneth Verheggen
 */
public class PrideFilter {

    /**
     * Empty default constructor
     */
    public PrideFilter() {
        value = "";
        type = null;
    }

    /**
     * A list containing the project summaries.
     */
    private final String value;
    /**
     * A list containing the project summaries.
     */
    private final PrideFilterType type;

    /**
     * Creates a new PrideFilter instance.
     *
     * @param type the type of the filter
     * @param value the value for the filter as a string
     */
    public PrideFilter(PrideFilterType type, String value) {
        this.value = value;
        this.type = type;
    }

    /**
     * Returns the filter value.
     *
     * @return the filter value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the filter type.
     *
     * @return the filter type
     */
    public PrideFilterType getType() {
        return type;
    }
}
