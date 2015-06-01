package com.compomics.util.experiment.filtering;

import java.util.ArrayList;

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

    /**
     * Returns the possible items of this kind.
     *
     * @return the possible items of this kind
     */
    public FilterItem[] getPossibleValues();

    /**
     * Indicates whether the item expects a number as threshold.
     *
     * @return a boolean indicating whether the item expects a number as
     * threshold
     */
    public boolean isNumber();

    /**
     * Indicates whether the item expects a list of PTMs to filter on.
     *
     * @return a boolean indicating whether the item expects a list of PTMs to
     * filter on
     */
    public boolean isPtm();

    /**
     * In case the filter has predefined values, return the possible values, null otherwise.
     *
     * @return a list of possible values to select from
     */
    public ArrayList<String> getPossibilities();
}
