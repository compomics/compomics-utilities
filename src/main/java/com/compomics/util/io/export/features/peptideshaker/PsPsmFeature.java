package com.compomics.util.io.export.features.peptideshaker;

import com.compomics.util.io.export.ExportFeature;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class lists the PSM identification features.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum PsPsmFeature implements ExportFeature {

    @SerializedName("PsPsmFeature.starred")
    starred("Starred", "Indicates whether the match was starred in the interface (1: yes, 0: no).", false),
    @SerializedName("PsPsmFeature.id")
    id("Identification Software Agreement", "Indicates whether the identification software agrees on the identification.", false),
    @SerializedName("PsPsmFeature.protein_groups")
    protein_groups("Protein Group(s)", "List of identified protein groups the peptide of this PSM can map to with associated validation level.", true),
    @SerializedName("PsPsmFeature.best_protein_group_validation")
    best_protein_group_validation("Protein Validation", "Best validation status among the protein groups derived from the peptide of this PSM.", true),
    @SerializedName("PsPsmFeature.localization_confidence")
    localization_confidence("Localization Confidence", "The confidence in variable PTM localization.", false),
    @SerializedName("PsPsmFeature.probabilistic_score")
    probabilistic_score("Probabilistic PTM score", "The probabilistic score (e.g. A-score or PhosphoRS) used for variable PTM localization.", false),
    @SerializedName("PsPsmFeature.d_score")
    d_score("D-score", "D-score for variable PTM localization.", false),
    @SerializedName("PsPsmFeature.confident_modification_sites")
    confident_modification_sites("Confidently Localized Modification Sites", "List of the sites where a variable modification was confidently localized.", false),
    @SerializedName("PsPsmFeature.confident_modification_sites_number")
    confident_modification_sites_number("#Confidently Localized Modification Sites", "Number of sites where a variable modification was confidently localized.", false),
    @SerializedName("PsPsmFeature.ambiguous_modification_sites")
    ambiguous_modification_sites("Ambiguously Localized Modification Sites", "List of the sites where ambiguously localized variable modification could possibly be located.", false),
    @SerializedName("PsPsmFeature.ambiguous_modification_sites_number")
    ambiguous_modification_sites_number("#Ambiguously Localized Modification Sites", "Number of ambiguously localized modifications.", false),
    @SerializedName("PsPsmFeature.confident_phosphosites")
    confident_phosphosites("Confident Phosphosites", "List of the sites where a phosphorylation was confidently localized.", false),
    @SerializedName("PsPsmFeature.confident_phosphosites_number")
    confident_phosphosites_number("#Confident Phosphosites", "Number of confidently localized phosphorylations.", false),
    @SerializedName("PsPsmFeature.ambiguous_phosphosites")
    ambiguous_phosphosites("Ambiguous Phosphosites", "List of the sites where a phosphorylation was ambiguously localized.", false),
    @SerializedName("PsPsmFeature.ambiguous_phosphosites_number")
    ambiguous_phosphosites_number("#Ambiguous Phosphosites", "Number of ambiguously localized phosphorylations.", false),
    @SerializedName("PsPsmFeature.algorithm_score")
    algorithm_score("Algorithm Score", "Best score given by the identification algorithm to the hit retained by PeptideShaker independent of modification localization.", false),
    @SerializedName("PsPsmFeature.raw_score")
    raw_score("Raw Score", "Score before log transformation.", true),
    @SerializedName("PsPsmFeature.score")
    score("Score", "Score of the retained PSM as a combination of the algorithm scores (used to rank PSMs).", true),
    @SerializedName("PsPsmFeature.confidence")
    confidence("Confidence [%]", "Confidence in percent associated to the retained PSM.", false),
    @SerializedName("PsPsmFeature.validated")
    validated("Validation", "Indicates the validation level of the retained PSM.", false),
    @SerializedName("PsPsmFeature.starred")
    hidden("Hidden", "Indicates whether the match was hidden in the interface (1: yes, 0: no).", false);

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
    public final static String type = "Peptide Spectrum Matching Summary";
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
    private PsPsmFeature(String title, String description, boolean advanced) {
        this.title = title;
        this.description = description;
        this.advanced = advanced;
    }

    @Override
    public ArrayList<ExportFeature> getExportFeatures(boolean includeSubFeatures) {
        ArrayList<ExportFeature> result = new ArrayList<>();
        result.addAll(PsIdentificationAlgorithmMatchesFeature.values()[0].getExportFeatures(includeSubFeatures));
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
