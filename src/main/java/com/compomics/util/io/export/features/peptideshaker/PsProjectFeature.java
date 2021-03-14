package com.compomics.util.io.export.features.peptideshaker;

import com.compomics.util.io.export.ExportFeature;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class lists the PSM identification features.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum PsProjectFeature implements ExportFeature, Serializable {

    @SerializedName("PsProjectFeature.peptide_shaker")
    peptide_shaker("PeptideShaker Version", "Software version used to create the project.", false),
    @SerializedName("PsProjectFeature.date")
    date("Date", "Date of project creation.", false),
    @SerializedName("PsProjectFeature.experiment")
    experiment("Experiment", "Experiment name.", false),
    @SerializedName("PsProjectFeature.sample")
    sample("Sample", "Sample name.", false),
    @SerializedName("PsProjectFeature.replicate")
    replicate("Replicate Number", "Replicate number.", false),
    @SerializedName("PsProjectFeature.identification_algorithms")
    identification_algorithms("Identification Algorithms", "The identification algorithms used.", false),
    @SerializedName("PsProjectFeature.algorithms_versions")
    algorithms_versions("Identification Algorithms Version", "The identification algorithms used with version number.", false);

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
    public final static String type = "Project Details";
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
    private PsProjectFeature(String title, String description, boolean advanced) {
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
