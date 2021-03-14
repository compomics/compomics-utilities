package com.compomics.util.io.export.features.peptideshaker;

import com.compomics.util.io.export.ExportFeature;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class lists the Algorithm identification features.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum PsIdentificationAlgorithmMatchesFeature implements ExportFeature {

    @SerializedName("PsIdentificationAlgorithmMatchesFeature.rank")
    rank("Rank", "The rank assigned by the identification algorithm.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.accessions")
    accessions("Protein(s)", "Protein(s) to which the peptide can be attached.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.protein_description")
    protein_description("Description(s)", "Description of the Protein(s) to which this peptide can be attached.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.sequence")
    sequence("Sequence", "The identified sequence of amino acids.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.aaBefore")
    aaBefore("AAs Before", "The amino acids before the sequence.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.aaAfter")
    aaAfter("AAs After", "The amino acids after the sequence.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.position")
    position("Position", "Position of the peptide in the protein sequence(s).", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.missed_cleavages")
    missed_cleavages("Missed Cleavages", "The number of missed cleavages.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.modified_sequence")
    modified_sequence("Modified Sequence", "The amino acids sequence annotated with variable modifications.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.variable_ptms")
    variable_ptms("Variable Modifications", "The variable modifications.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.fixed_ptms")
    fixed_ptms("Fixed Modifications", "The fixed modifications.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.spectrum_file")
    spectrum_file("Spectrum File", "The spectrum file.spectrum_title", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.spectrum_title")
    spectrum_title("Spectrum Title", "The title of the spectrum.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.spectrum_scan_number")
    spectrum_scan_number("Spectrum Scan Number", "The spectrum scan number.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.spectrum_array_list")
    spectrum_array_list("Spectrum Array List", "The peaks in the spectrum as an array list.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.rt")
    rt("RT", "Retention time as provided in the spectrum file.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.mz")
    mz("m/z", "Measured m/z.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.spectrum_charge")
    spectrum_charge("Measured Charge", "The charge as given in the spectrum file.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.precursor_intensity")
    precursor_intensity("Precursor Intensity", "The precursor intensity as given in the spectrum file", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeaturetotal_spectrum_intensity.")
    total_spectrum_intensity("Total Spectrum Intensity", "The summed intensity of all peaks in the spectrum.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.intensity_coverage")
    intensity_coverage("Intensity Coverage [%]", "Annotated share of the total spectrum intensity.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.max_intensity")
    max_intensity("Maximal Spectrum Intensity", "The maximal intensity found in the spectrum.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.identification_charge")
    identification_charge("Identification Charge", "The charge as inferred by the search engine.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.theoretical_mass")
    theoretical_mass("Theoretical Mass", "The theoretical mass of the peptide.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.isotope")
    isotope("Isotope Number", "The isotope number targetted by the instrument.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.mz_error_ppm")
    mz_error_ppm("Precursor m/z Error [ppm]", "The precursor m/z matching error in ppm.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.mz_error_da")
    mz_error_da("Precursor m/z Error [Da]", "The precursor m/z matching error in Da.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.algorithm_score")
    algorithm_score("Algorithm Score", "The (potentially transformed) score given by the identification algorithm to the hit.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.algorithm_raw_score")
    algorithm_raw_score("Algorithm Raw Score", "The raw score given by the identification algorithm to the hit.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.algorithm_confidence")
    algorithm_confidence("Algorithm Confidence [%]", "Confidence in percent associated to the algorithm score.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.algorithm_delta_confidence")
    algorithm_delta_confidence("Algorithm Delta Confidence [%]", "Difference in percent between the match and the next best for a given identification algorithm without accounting for PTM localization.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.delta_confidence")
    delta_confidence("Delta Confidence [%]", "Difference in percent between the match and the next best across all search engines without accounting for PTM localization.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.fragment_mz_accuracy_score")
    fragment_mz_accuracy_score("Fragment m/z accuracy score", "Score reflecting the accuracy of the fragment ions m/z.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.intensity_score")
    intensity_score("Intensity score", "Score reflecting the coverage of the spectrum in intensity.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.sequence_coverage")
    sequence_coverage("Sequence Coverage [%]", "Coverage of the amino acid sequence by the annotated fragment ions in percent.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.longest_amino_acid_sequence_annotated")
    longest_amino_acid_sequence_annotated("Longest amino acid sequence annotated", "Longest consecutive series of amino acid annotated on the spectrum.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.longest_amino_acid_sequence_annotated_single_serie")
    longest_amino_acid_sequence_annotated_single_serie("Single ion longest amino acid sequence annotated", "Longest consecutive series of amino acid annotated on the spectrum by a single type of ions of charge 1 without neutral losses.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.amino_acids_annotated")
    amino_acids_annotated("Amino Acids Annotated", "Amino acid sequence annotated on the spectrum.", true),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.decoy")
    decoy("Decoy", "Indicates whether the peptide is a decoy (1: yes, 0: no).", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.validated")
    validated("Validation", "Indicates the validation level of the protein group.", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.starred")
    starred("Starred", "Indicates whether the match was starred in the interface (1: yes, 0: no).", false),
    @SerializedName("PsIdentificationAlgorithmMatchesFeature.hidden")
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
    public final static String type = "Identification Algorithm Results";
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
    private PsIdentificationAlgorithmMatchesFeature(String title, String description, boolean advanced) {
        this.title = title;
        this.description = description;
        this.advanced = advanced;
    }

    @Override
    public ArrayList<ExportFeature> getExportFeatures(boolean includeSubFeatures) {
        ArrayList<ExportFeature> result = new ArrayList<>();
        result.addAll(Arrays.asList(values()));
        if (includeSubFeatures) {
            result.addAll(PsFragmentFeature.values()[0].getExportFeatures(includeSubFeatures));
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
