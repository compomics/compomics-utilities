package com.compomics.util.io.export.features.peptideshaker;

import com.compomics.util.io.export.ExportFeature;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This enum groups the export features related to PTM scoring.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum PsPtmScoringFeature implements ExportFeature {

    @SerializedName("PsPtmScoringFeature.probabilitstic_score")
    probabilitstic_score("Probabilistic Score", "Indicates the probabilistic score used for PTM localization.", false),
    @SerializedName("PsPtmScoringFeature.neutral_losses")
    neutral_losses("Accounting for Neutral Losses", "Indicates whether the neutral losses are accounted for in the A-score calculation.", false),
    @SerializedName("PsPtmScoringFeature.threshold")
    threshold("Threshold", "Indicates the threshold used for the probabilistic localization score.", false);

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
    public final static String type = "PTM Scoring Settings";
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
    private PsPtmScoringFeature(String title, String description, boolean advanced) {
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
