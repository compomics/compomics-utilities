package com.compomics.util.io.export.features.peptideshaker;

import com.compomics.util.io.export.ExportFeature;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class lists the export features linked to the spectrum identification.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum PsSearchFeature implements ExportFeature {

    @SerializedName("PsSearchFeature.precursor_tolerance_unit")
    precursor_tolerance_unit("Precursor Tolerance Unit", "Unit of the precursor tolearance: ppm or Da.", false),
    @SerializedName("PsSearchFeature.precursor_tolerance")
    precursor_tolerance("Precursor Ion m/z Tolerance", "Precursor ion m/z tolerance used for the search.", false),
    @SerializedName("PsSearchFeature.fragment_tolerance_unit")
    fragment_tolerance_unit("Fragment Ion Tolerance Unit", "Unit of the precursor tolearance: ppm or Da.", false),
    @SerializedName("PsSearchFeature.fragment_tolerance")
    fragment_tolerance("Fragment Ion m/z Tolerance", "Fragment ion m/z tolerance used for the search.", false),
    @SerializedName("PsSearchFeature.cleavage")
    cleavage("Cleavage", "The type of cleavage used for the search.", false),
    @SerializedName("PsSearchFeature.enzyme")
    enzyme("Enzyme", "The enzymes used for the search.", false),
    @SerializedName("PsSearchFeature.mc")
    mc("Missed Cleavages", "The number of missed cleavages allowed for the enzyme digestion.", false),
    @SerializedName("PsSearchFeature.specificity")
    specificity("Specificity", "The specificity used for the enzyme digestion.", false),
    @SerializedName("PsSearchFeature.database")
    database("Database", "The protein sequence database.", false),
    @SerializedName("PsSearchFeature.forward_ion")
    forward_ion("Forward Ion", "The forward ion type searched for.", false),
    @SerializedName("PsSearchFeature.rewind_ion")
    rewind_ion("Rewind Ion", "The rewind ion type searched for.", false),
    @SerializedName("PsSearchFeature.fixed_modifications")
    fixed_modifications("Fixed Modifications", "The fixed posttranslational modifications used for the search.", false),
    @SerializedName("PsSearchFeature.variable_modifications")
    variable_modifications("Variable Modifications", "The variable posttranslational modifications used for the search.", false),
    @SerializedName("PsSearchFeature.refinement_variable_modifications")
    refinement_variable_modifications("Refinement Variable Modifications", "The refinement variable posttranslational modifications used for the search, typically a second pass search.", false),
    @SerializedName("PsSearchFeature.refinement_fixed_modifications")
    refinement_fixed_modifications("Refinement Fixed Modifications", "The refinement fixed posttranslational modifications used for the search, typically a second pass search.", false);

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
    public final static String type = "Database Search Parameters";
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
    private PsSearchFeature(String title, String description, boolean advanced) {
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
