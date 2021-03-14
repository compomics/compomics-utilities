package com.compomics.util.io.export.features.peptideshaker;

import com.compomics.util.io.export.ExportFeature;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class lists the peptide identification features.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum PsPeptideFeature implements ExportFeature {

    @SerializedName("PsPeptideFeature.starred")
    starred("Starred", "Indicates whether the match was starred in the interface (1: yes, 0: no).", false),
    @SerializedName("PsPeptideFeature.pi")
    pi("Protein Inference", "The protein inference status of this peptide.", false),
    @SerializedName("PsPeptideFeature.accessions")
    accessions("Protein(s)", "All Protein(s) to which this peptide can map.", false),
    @SerializedName("PsPeptideFeature.protein_description")
    protein_description("Description(s)", "Description of the protein(s) to which this peptide can map.", false),
    @SerializedName("PsPeptideFeature.protein_groups")
    protein_groups("Protein Group(s)", "List of identified protein groups this peptide can map to with associated validation level.", true),
    @SerializedName("PsPeptideFeature.best_protein_group_validation")
    best_protein_group_validation("Protein Validation", "Best validation status among the protein groups this peptide maps to.", true),
    @SerializedName("PsPeptideFeature.nValidatedProteinGroups")
    nValidatedProteinGroups("#Validated Protein Group(s)", "Indicates the number of protein groups this peptide maps to.", false),
    @SerializedName("PsPeptideFeature.unique_group")
    unique_group("Unique Protein Group", "Indicates whether the peptide maps to a unique protein group.", false),
    @SerializedName("PsPeptideFeature.sequence")
    sequence("Sequence", "Sequence of the peptide.", false),
    @SerializedName("PsPeptideFeature.modified_sequence")
    modified_sequence("Modified Sequence", "The peptide sequence annotated with variable modifications.", false),
    @SerializedName("PsPeptideFeature.position")
    position("Position", "Position of the peptide in the protein sequence(s).", false),
    @SerializedName("PsPeptideFeature.aaBefore")
    aaBefore("AAs Before", "The amino acids before the sequence.", false),
    @SerializedName("PsPeptideFeature.aaAfter")
    aaAfter("AAs After", "The amino acids after the sequence.", false),
    @SerializedName("PsPeptideFeature.missed_cleavages")
    missed_cleavages("Missed Cleavages", "The number of missed cleavages.", false),
    @SerializedName("PsPeptideFeature.variable_ptms")
    variable_ptms("Variable Modifications", "The variable modifications.", false),
    @SerializedName("PsPeptideFeature.fixed_ptms")
    fixed_ptms("Fixed Modifications", "The fixed modifications.", false),
    @SerializedName("PsPeptideFeature.localization_confidence")
    localization_confidence("Localization Confidence", "The confidence in PTMs localization.", false),
    @SerializedName("PsPeptideFeature.probabilistic_score")
    probabilistic_score("Probabilistic PTM score", "The best probabilistic score (e.g. A-score or PhosphoRS) among all validated PSMs for this peptide.", false),
    @SerializedName("PsPeptideFeature.d_score")
    d_score("D-score", "The best D-score for variable PTM localization among all validated PSMs for this peptide.", false),
    @SerializedName("PsPeptideFeature.confident_modification_sites")
    confident_modification_sites("Confidently Localized Modification Sites", "List of the sites where a variable modification was confidently localized.", false),
    @SerializedName("PsPeptideFeature.confident_modification_sites_number")
    confident_modification_sites_number("#Confidently Localized Modification Sites", "Number of sites where a variable modification was confidently localized.", false),
    @SerializedName("PsPeptideFeature.ambiguous_modification_sites")
    ambiguous_modification_sites("Ambiguously Localized Modification Sites", "List of the sites where ambiguously localized variable modification could possibly be located.", false),
    @SerializedName("PsPeptideFeature.ambiguous_modification_sites_number")
    ambiguous_modification_sites_number("#Ambiguously Localized Modification Sites", "Number of ambiguously localized modifications.", false),
    @SerializedName("PsPeptideFeature.confident_phosphosites")
    confident_phosphosites("Confident Phosphosites", "List of the sites where a phosphorylation was confidently localized.", false),
    @SerializedName("PsPeptideFeature.confident_phosphosites_number")
    confident_phosphosites_number("#Confident Phosphosites", "Number of confidently localized phosphorylations.", false),
    @SerializedName("PsPeptideFeature.ambiguous_phosphosites")
    ambiguous_phosphosites("Ambiguous Phosphosites", "List of the sites where a phosphorylation was ambiguously localized.", false),
    @SerializedName("PsPeptideFeature.ambiguous_phosphosites_number")
    ambiguous_phosphosites_number("#Ambiguous Phosphosites", "Number of ambiguously localized phosphorylations.", false),
    @SerializedName("PsPeptideFeature.psms")
    psms("#PSMs", "Number of PSMs.", false),
    @SerializedName("PsPeptideFeature.validated_psms")
    validated_psms("#Validated PSMs", "Number of validated PSMs.", false),
    @SerializedName("PsPeptideFeature.raw_score")
    raw_score("Raw Score", "Peptide score before log transform.", true),
    @SerializedName("PsPeptideFeature.score")
    score("Score", "Score of the peptide.", true),
    @SerializedName("PsPeptideFeature.confidence")
    confidence("Confidence [%]", "Confidence in percent associated to the peptide.", false),
    @SerializedName("PsPeptideFeature.decoy")
    decoy("Decoy", "Indicates whether the peptide is a decoy (1: yes, 0: no).", false),
    @SerializedName("PsPeptideFeature.validated")
    validated("Validation", "Indicates the validation level of the peptide.", false),
    @SerializedName("PsPeptideFeature.hidden")
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
    public final static String type = "Peptide Identification Summary";
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
    private PsPeptideFeature(String title, String description, boolean advanced) {
        this.title = title;
        this.description = description;
        this.advanced = advanced;
    }

    @Override
    public ArrayList<ExportFeature> getExportFeatures(boolean includeSubFeatures) {
        ArrayList<ExportFeature> result = new ArrayList<>();
        result.addAll(Arrays.asList(values()));
        if (includeSubFeatures) {
            result.addAll(PsPsmFeature.values()[0].getExportFeatures(includeSubFeatures));
        }
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
