package com.compomics.util.io.export;

import java.util.ArrayList;

/**
 * An export factory manages and generates reports.
 *
 * @author Marc Vaudel
 */
public interface ExportFactory {

    /**
     * Returns the export scheme indexed by the given name.
     *
     * @param schemeName the name of the desired export scheme
     * @return the desired export scheme
     */
    public ExportScheme getExportScheme(String schemeName);

    /**
     * Removes a user scheme.
     *
     * @param schemeName the name of the scheme to remove
     */
    public void removeExportScheme(String schemeName);

    /**
     * Adds an export scheme to the map of user schemes.
     *
     * @param exportScheme the new export scheme, will be accessible via its
     * name
     */
    public void addExportScheme(ExportScheme exportScheme);

    /**
     * Returns the implemented sections.
     *
     * @return the implemented sections
     */
    public ArrayList<String> getImplementedSections();

    /**
     * Returns the export features implemented for the given section.
     *
     * @param sectionName the name of the section of interest
     * @param includeSubFeatures indicates whether sub-features should be included
     *
     * @return a list of export features
     */
    public ArrayList<ExportFeature> getExportFeatures(String sectionName, boolean includeSubFeatures);
}
