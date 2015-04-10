package com.compomics.util.experiment.filtering;

/**
 * Interface for an item used for filtering.
 *
 * @author Marc Vaudel
 */
public interface FilterItem {

    /**
     * Returns the name of the item.
     *
     * @return the name of the item
     */
    public String getName();

    /**
     * Returns a description of the item.
     *
     * @return a description of the item
     */
    public String getDescription();
}
