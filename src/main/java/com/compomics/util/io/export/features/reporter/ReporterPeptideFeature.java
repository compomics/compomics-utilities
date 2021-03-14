package com.compomics.util.io.export.features.reporter;

import com.compomics.util.io.export.ExportFeature;
import com.compomics.util.io.export.features.ReporterExportFeature;
import com.compomics.util.io.export.features.peptideshaker.PsPeptideFeature;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This enum lists all the peptide export features available from reporter
 * complementarily to the ones available in PeptideShaker.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum ReporterPeptideFeature implements ReporterExportFeature {

    @SerializedName("ReporterPeptideFeature.raw_ratio")
    raw_ratio("Raw Ratios", "The ratios of this peptide prior to normalization.", true, true),
    @SerializedName("ReporterPeptideFeature.spread")
    spread("Spread", "The spread of the PSM ratios of this peptide.", true, false),
    @SerializedName("ReporterPeptideFeature.normalized_ratio")
    normalized_ratio("Normalized Ratios", "The normalized ratios of this peptide.", true, false);

    /**
     * The title of the feature which will be used for column heading.
     */
    public String title;
    /**
     * The description of the feature.
     */
    public String description;
    /**
     * Indicates whether the feature is channel dependent.
     */
    private boolean hasChannels;
    /**
     * The type of export feature.
     */
    public final static String type = "Peptide Reporter Quantification Summary";
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
    private ReporterPeptideFeature(String title, String description, boolean hasChannels, boolean advanced) {
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
        ArrayList<ExportFeature> result = PsPeptideFeature.values()[0].getExportFeatures(includeSubFeatures);
        result.addAll(Arrays.asList(values()));
        if (includeSubFeatures) {
            result.addAll(ReporterPsmFeatures.values()[0].getExportFeatures(includeSubFeatures));
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
