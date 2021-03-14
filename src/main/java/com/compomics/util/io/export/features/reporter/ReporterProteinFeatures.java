package com.compomics.util.io.export.features.reporter;

import com.compomics.util.io.export.ExportFeature;
import com.compomics.util.io.export.features.ReporterExportFeature;
import com.compomics.util.io.export.features.peptideshaker.PsProteinFeature;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This enum lists all the protein export features available from reporter
 * complementarily to the ones available in PeptideShaker.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum ReporterProteinFeatures implements ReporterExportFeature {

    @SerializedName("ReporterProteinFeatures.raw_unique_ratio")
    raw_unique_ratio("Raw Unique Ratios", "The ratios of this protein group prior to normalization accounting only for peptides unique to this protein group.", true, false),
    @SerializedName("ReporterProteinFeatures.unique_ratio")
    unique_ratio("Unique Ratios", "The normalized ratios of this protein group accounting only for peptides unique to this group.", true, false),
    @SerializedName("ReporterProteinFeatures.raw_shared_ratio")
    raw_shared_ratio("Raw Shared Ratios", "The ratios of this protein group prior to normalization accounting only for peptides shared with other protein groups.", true, false),
    @SerializedName("ReporterProteinFeatures.shared_ratio")
    shared_ratio("Shared Ratios", "The normalized ratios of this protein group accounting only for peptides shared with other protein groups.", true, false),
    @SerializedName("ReporterProteinFeatures.raw_ratio")
    raw_ratio("Raw Ratios", "The ratios of this protein group prior to normalization.", true, false),
    @SerializedName("ReporterProteinFeatures.ratio")
    ratio("Ratios", "The normalized ratios of this protein group.", true, false),
    @SerializedName("ReporterProteinFeatures.spread")
    spread("Spread", "The spread of the peptide ratios of this protein group.", true, false);

    /**
     * The title of the feature which will be used for column heading.
     */
    private String title;
    /**
     * The description of the feature.
     */
    private String description;
    /**
     * Indicates whether the feature is channel dependent.
     */
    private boolean hasChannels;
    /**
     * The type of export feature.
     */
    public final static String type = "Protein Reporter Quantification Summary";
    /**
     * Indicates whether a feature is for advanced user only.
     */
    private final boolean advanced;

    /**
     * Constructor.
     *
     * @param title title of the feature
     * @param description description of the feature
     * @param hasChannels indicates whether the feature is channel dependent
     * @param advanced indicates whether a feature is for advanced user only
     */
    private ReporterProteinFeatures(String title, String description, boolean hasChannels, boolean advanced) {
        this.title = title;
        this.description = description;
        this.hasChannels = hasChannels;
        this.advanced = advanced;
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
    public ArrayList<ExportFeature> getExportFeatures(boolean includeSubFeatures) {
        ArrayList<ExportFeature> result = PsProteinFeature.values()[0].getExportFeatures(includeSubFeatures);
        result.addAll(Arrays.asList(values()));
        if (includeSubFeatures) {
            result.addAll(ReporterPeptideFeature.values()[0].getExportFeatures(includeSubFeatures));
        }
        return result;
    }

    @Override
    public boolean hasChannels() {
        return hasChannels;
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }
}
