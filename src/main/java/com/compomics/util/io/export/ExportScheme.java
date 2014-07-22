package com.compomics.util.io.export;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class allows creating a standard output scheme.
 *
 * @author Marc Vaudel
 */
public class ExportScheme implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -4712918049667194600L;
    /**
     * The name of the scheme.
     */
    private String name;
    /**
     * A boolean indicating whether the scheme can be modified.
     */
    private boolean editable;
    /**
     * The title of the report.
     */
    private String mainTitle = null;
    /**
     * Ordered list of the features in that scheme.
     */
    private ArrayList<String> sectionList = new ArrayList<String>();
    /**
     * Map of the features to export indexed by feature type.
     */
    private HashMap<String, ArrayList<ExportFeature>> exportFeaturesMap = new HashMap<String, ArrayList<ExportFeature>>();
    /**
     * The separator used to separate columns.
     */
    private String separator = "\t";
    /**
     * Boolean indicating whether the line shall be indexed.
     */
    private boolean indexes = true;
    /**
     * Boolean indicating whether column headers shall be included.
     */
    private boolean header = true;
    /**
     * indicates how many lines shall be used to separate sections.
     */
    private int separationLines = 3;
    /**
     * Indicates whether the title of every section shall be included.
     */
    private boolean includeSectionTitles = false;
    /**
     * Indicates whether only validated matches should be included.
     */
    private Boolean validatedOnly = true;
    /**
     * Indicates whether decoy matches should be included.
     */
    private Boolean includeDecoy = false;

    /**
     * Constructor.
     *
     * @param name the name of the scheme
     * @param editable a boolean indicating whether the scheme can be edited by
     * the user
     * @param sectionList ordered list of the sections included in the report
     * @param exportFeatures list of features to be included in the report
     * @param separator the column separator to be used
     * @param indexes indicates whether lines shall be indexed
     * @param header indicates whether column headers shall be included
     * @param separationLines the number of lines to use for section separation
     * @param includeSectionTitles indicates whether section titles shall be
     * used
     * @param mainTitle the title of the report
     * @param sectionFamily the section family. If null the sections will be
     * automatically separated based on the feature type. Note, be sure that all
     * features are implemented for this section.
     * @param validatedOnly Indicates whether only validated matches should be
     * included
     * @param includeDecoys Indicates whether decoy matches should be included
     */
    private ExportScheme(String name, boolean editable, ArrayList<String> sectionList, HashMap<String, ArrayList<ExportFeature>> exportFeatures, String separator,
            boolean indexes, boolean header, int separationLines, boolean includeSectionTitles, boolean validatedOnly, boolean includeDecoys, String mainTitle) {
        this.sectionList = sectionList;
        exportFeaturesMap.putAll(exportFeatures);
        this.separator = separator;
        this.indexes = indexes;
        this.separationLines = separationLines;
        this.header = header;
        this.includeSectionTitles = includeSectionTitles;
        this.mainTitle = mainTitle;
        this.name = name;
        this.editable = editable;
        this.validatedOnly = validatedOnly;
        this.includeDecoy = includeDecoys;
    }

    /**
     * Constructor. Here sections will appear in a random order.
     *
     * @param name the name of the scheme
     * @param editable a boolean indicating whether the scheme can be edited by
     * the user
     * @param exportFeatures list of features to be included in the report
     * @param separator the column separator to be used
     * @param indexes indicates whether lines shall be indexed
     * @param header indicates whether column headers shall be included
     * @param separationLines the number of lines to use for section separation
     * @param includeSectionTitles indicates whether section titles shall be
     * used
     * @param mainTitle the title of the report
     * @param validatedOnly Indicates whether only validated matches should be
     * included
     * @param includeDecoys Indicates whether decoy matches should be included
     */
    public ExportScheme(String name, boolean editable, HashMap<String, ArrayList<ExportFeature>> exportFeatures, String separator,
            boolean indexes, boolean header, int separationLines, boolean includeSectionTitles, boolean validatedOnly, boolean includeDecoys, String mainTitle) {
        this(name, editable, new ArrayList<String>(exportFeatures.keySet()), exportFeatures, separator, indexes, header, separationLines, includeSectionTitles, validatedOnly, includeDecoys, mainTitle);
    }

    /**
     * Constructor. This report will not contain any title.
     *
     * @param name the name of the scheme
     * @param editable a boolean indicating whether the scheme can be edited by
     * the user
     * @param sectionList ordered list of the sections included in the report
     * @param exportFeatures list of features to be included in the report
     * @param separator the column separator to be used
     * @param indexes indicates whether lines shall be indexed
     * @param header indicates whether column headers shall be included
     * @param separationLines the number of lines to use for section separation
     * @param includeSectionTitles indicates whether section titles shall be
     * used
     * @param validatedOnly Indicates whether only validated matches should be
     * included
     * @param includeDecoys Indicates whether decoy matches should be included
     */
    public ExportScheme(String name, boolean editable, ArrayList<String> sectionList, HashMap<String, ArrayList<ExportFeature>> exportFeatures, String separator,
            boolean indexes, boolean header, int separationLines, boolean includeSectionTitles, boolean validatedOnly, boolean includeDecoys) {
        this(name, editable, sectionList, exportFeatures, separator, indexes, header, separationLines, includeSectionTitles, validatedOnly, includeDecoys, null);
    }

    /**
     * Constructor. This report will not contain any title and sections will
     * appear in a random order.
     *
     * @param name the name of the scheme
     * @param editable a boolean indicating whether the scheme can be edited by
     * the user
     * @param exportFeatures list of features to be included in the report
     * @param separator the column separator to be used
     * @param indexes indicates whether lines shall be indexed
     * @param header indicates whether column headers shall be included
     * @param separationLines the number of lines to use for section separation
     * @param includeSectionTitles indicates whether section titles shall be
     * used
     * @param validatedOnly Indicates whether only validated matches should be
     * included
     * @param includeDecoys Indicates whether decoy matches should be included
     */
    public ExportScheme(String name, boolean editable, HashMap<String, ArrayList<ExportFeature>> exportFeatures, String separator,
            boolean indexes, boolean header, int separationLines, boolean includeSectionTitles, boolean validatedOnly, boolean includeDecoys) {
        this(name, editable, new ArrayList<String>(exportFeatures.keySet()), exportFeatures, separator, indexes, header, separationLines, includeSectionTitles, validatedOnly, includeDecoys, null);
    }

    /**
     * Returns the column separator.
     *
     * @return the column separator
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Indicates whether lines shall be indexed.
     *
     * @return a boolean indicating whether lines shall be indexed
     */
    public boolean isIndexes() {
        return indexes;
    }

    /**
     * Indicates whether column header shall be used.
     *
     * @return a boolean indicating whether column header shall be used
     */
    public boolean isHeader() {
        return header;
    }

    /**
     * Returns the number of lines to be used to separate the sections.
     *
     * @return the number of lines to be used to separate the sections
     */
    public int getSeparationLines() {
        return separationLines;
    }

    /**
     * Indicates whether section titles shall be used.
     *
     * @return a boolean indicating whether section titles shall be used
     */
    public boolean isIncludeSectionTitles() {
        return includeSectionTitles;
    }

    /**
     * returns the list of sections to be included in the scheme.
     *
     * @return the list of sections to be included in the scheme
     */
    public ArrayList<String> getSections() {
        return sectionList;
    }

    /**
     * Returns the export features to be included in the given section.
     *
     * @param section the section of interest
     * @return the list of export features to export in this section
     */
    public ArrayList<ExportFeature> getExportFeatures(String section) {
        return exportFeaturesMap.get(section);
    }

    /**
     * Sets the export features of a given section. If a section already exists
     * it will be silently overwritten.
     *
     * @param section the name of the section
     * @param exportFeatures the export features to include in that section
     */
    public void setExportFeatures(String section, ArrayList<ExportFeature> exportFeatures) {
        exportFeaturesMap.put(section, exportFeatures);
        sectionList.add(section);
    }

    /**
     * Adds an export feature to the desired section. If the section does not
     * exist it will be created.
     *
     * @param section the name of the section
     * @param exportFeature the export feature to add
     */
    public void addExportFeature(String section, ExportFeature exportFeature) {
        ArrayList<ExportFeature> sectionFeatures = exportFeaturesMap.get(section);
        if (sectionFeatures == null) {
            sectionFeatures = new ArrayList<ExportFeature>();
            exportFeaturesMap.put(section, sectionFeatures);
            sectionList.add(section);
        }
        sectionFeatures.add(exportFeature);
    }

    /**
     * Removes an entire section from the mapping.
     *
     * @param sectionName the section name
     */
    public void removeSection(String sectionName) {
        exportFeaturesMap.remove(sectionName);
        sectionList.remove(sectionName);
    }

    /**
     * Returns the main title of the report. Null if none.
     *
     * @return the main title of the report.
     */
    public String getMainTitle() {
        return mainTitle;
    }

    /**
     * Returns the name of the scheme.
     *
     * @return the name of the scheme
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the scheme.
     *
     * @param name the name of the scheme
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Indicates whether the scheme is editable.
     *
     * @return a boolean indicating whether the scheme is editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets whether the scheme is editable.
     *
     * @param editable a boolean indicating whether the scheme shall be editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Indicates whether only validated results should be exported.
     *
     * @return whether only validated results should be exported
     */
    public Boolean isValidatedOnly() {
        return validatedOnly;
    }

    /**
     * Sets whether only validated results should be exported.
     *
     * @param validatedOnly whether only validated results should be exported
     */
    public void setValidatedOnly(Boolean validatedOnly) {
        this.validatedOnly = validatedOnly;
    }

    /**
     * Indicates whether decoy hits should be included.
     *
     * @return whether decoy hits should be included
     */
    public Boolean isIncludeDecoy() {
        return includeDecoy;
    }

    /**
     * Sets whether decoy hits should be included.
     *
     * @param includeDecoy whether decoy hits should be included
     */
    public void setIncludeDecoy(Boolean includeDecoy) {
        this.includeDecoy = includeDecoy;
    }

}
