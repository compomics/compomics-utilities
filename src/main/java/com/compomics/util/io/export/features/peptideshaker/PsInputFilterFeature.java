package com.compomics.util.io.export.features.peptideshaker;

import com.compomics.util.io.export.ExportFeature;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This enum lists the export features related to the import features.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum PsInputFilterFeature implements ExportFeature {

    @SerializedName("PsInputFilterFeature.min_peptide_length")
    min_peptide_length("Minimal Peptide Length", "The minimal peptide length.", false),
    @SerializedName("PsInputFilterFeature.max_peptide_length")
    max_peptide_length("Maximal Peptide Length", "The maximal peptide length.", false),
    @SerializedName("PsInputFilterFeature.max_mz_deviation")
    max_mz_deviation("Precursor m/z Tolerance", "The maximal precursor m/z error tolerance allowed.", false),
    @SerializedName("PsInputFilterFeature.max_mz_deviation_unit")
    max_mz_deviation_unit("Precursor m/z Tolerance Unit", "The unit of the maximal precursor m/z error tolerance allowed.", false),
    @SerializedName("PsInputFilterFeature.unknown_PTM")
    unknown_PTM("Unrecognized Modifications Discarded", "Indicates whether the Peptide Spectrum Matches (PSMs) presenting PTMs which do not match the search parameters were discarded.", false);

    /**
     * The title of the feature which will be used for column heading.
     */
    public String title;
    /**
     * The description of the feature.
     */
    public String description;
    /**
     * The type of export feature.
     */
    public final static String type = "Input Filters";
    /**
     * Indicates whether a feature is for advanced user only.
     */
    private final boolean advanced;

    /**
     * Constructor.
     *
     * @param title title of the feature
     * @param description description of the feature
     * @param advanced indicates whether a feature is for advanced user only
     */
    private PsInputFilterFeature(String title, String description, boolean advanced) {
        this.title = title;
        this.description = description;
        this.advanced = advanced;
    }

    @Override
    public ArrayList<ExportFeature> getExportFeatures(boolean includeSubFeatures) {
        ArrayList<ExportFeature> result = new ArrayList<>();
        result.addAll(Arrays.asList(values()));
        return result;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getFeatureFamily() {
        return type;
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }
}
