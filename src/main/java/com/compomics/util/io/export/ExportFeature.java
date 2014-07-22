package com.compomics.util.io.export;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This interface represents an export feature.
 *
 * @author Marc Vaudel
 */
public interface ExportFeature extends Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -4158077697265471589L;

    /**
     * Returns the column titles of the feature.
     *
     * @return the column titles of the feature
     */
    public String[] getTitles();

    /**
     * Returns the description of the feature.
     *
     * @return the description of the feature
     */
    public String getDescription();

    /**
     * Returns the family type of this export feature.
     *
     * @return the family type of this export feature
     */
    public String getFeatureFamily();

    /**
     * Returns a list of all implemented export features.
     *
     * @param includeSubFeatures boolean indicating whether sub features should
     * be included
     *
     * @return a list of all implemented export features
     */
    public ArrayList<ExportFeature> getExportFeatures(boolean includeSubFeatures);

    /**
     * Indicates whether a feature is for advanced user only.
     *
     * @return a boolean indicating whether a feature is for advanced user only
     */
    public boolean isAdvanced();
}
