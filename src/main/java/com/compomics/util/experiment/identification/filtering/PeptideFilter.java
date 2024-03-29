package com.compomics.util.experiment.identification.filtering;

import com.compomics.util.experiment.filtering.FilterItemComparator;
import com.compomics.util.experiment.biology.genes.GeneMaps;
import com.compomics.util.experiment.filtering.FilterItem;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.features.IdentificationFeaturesGenerator;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.experiment.io.biology.protein.ProteinDetailsProvider;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.identification.filtering.items.PeptideFilterItem;
import com.compomics.util.experiment.identification.peptide_shaker.PSParameter;
import com.compomics.util.experiment.identification.peptide_shaker.PSModificationScores;
import com.compomics.util.experiment.identification.validation.MatchValidationLevel;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Peptide filter.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PeptideFilter extends MatchFilter {

    /**
     * Serial number for serialization compatibility.
     */
    static final long serialVersionUID = 959658989341486818L;

    /**
     * Constructor.
     */
    public PeptideFilter() {

        this.filterType = FilterType.PEPTIDE;

    }

    /**
     * Constructor.
     *
     * @param name the name of the filter
     */
    public PeptideFilter(
            String name
    ) {

        this.name = name;
        this.filterType = FilterType.PEPTIDE;

    }

    /**
     * Constructor.
     *
     * @param name the name of the filter
     * @param description the description of the filter
     */
    public PeptideFilter(
            String name,
            String description
    ) {

        this.name = name;
        this.description = description;
        this.filterType = FilterType.PEPTIDE;

    }

    /**
     * Constructor.
     *
     * @param name the name of the filter
     * @param description the description of the filter
     * @param condition a description of the condition to be met to pass the
     * filter
     * @param reportPassed a report for when the filter is passed
     * @param reportFailed a report for when the filter is not passed
     */
    public PeptideFilter(
            String name,
            String description,
            String condition,
            String reportPassed,
            String reportFailed
    ) {

        this.name = name;
        this.description = description;
        this.condition = condition;
        this.reportPassed = reportPassed;
        this.reportFailed = reportFailed;
        this.filterType = FilterType.PEPTIDE;

    }

    @Override
    protected MatchFilter getNew() {
        return new PeptideFilter();
    }

    @Override
    public boolean isValidated(
            String itemName,
            FilterItemComparator filterItemComparator,
            Object value,
            long matchKey,
            Identification identification,
            GeneMaps geneMaps,
            IdentificationFeaturesGenerator identificationFeaturesGenerator,
            IdentificationParameters identificationParameters,
            SequenceProvider sequenceProvider,
            ProteinDetailsProvider proteinDetailsProvider,
            SpectrumProvider spectrumProvider
    ) {

        PeptideFilterItem filterItem = PeptideFilterItem.getItem(itemName);

        if (filterItem == null) {

            throw new IllegalArgumentException("Filter item " + itemName + "not recognized as peptide filter item.");

        }

        String input = value.toString();
        PeptideMatch peptideMatch;

        switch (filterItem) {

            case proteinAccession:

                peptideMatch = identification.getPeptideMatch(matchKey);

                return filterItemComparator.passes(input, peptideMatch.getPeptide()
                        .getProteinMapping()
                        .keySet());

            case proteinDescription:

                peptideMatch = identification.getPeptideMatch(matchKey);

                return filterItemComparator.passes(input, peptideMatch.getPeptide()
                        .getProteinMapping()
                        .keySet()
                        .stream()
                        .map(
                                accession -> proteinDetailsProvider.getDescription(accession)
                        )
                        .collect(Collectors.toList()));

            case sequence:

                peptideMatch = identification.getPeptideMatch(matchKey);

                return filterItemComparator.passes(
                        input,
                        peptideMatch.getPeptide().getSequence()
                );

            case modification:

                peptideMatch = identification.getPeptideMatch(matchKey);
                PSModificationScores modificationScores = (PSModificationScores) peptideMatch.getUrParam(PSModificationScores.dummy);
                Set<String> modifications = modificationScores == null ? new HashSet<>(0)
                        : modificationScores.getScoredModifications();

                return filterItemComparator.passes(input, modifications);

            case nPSMs:

                peptideMatch = identification.getPeptideMatch(matchKey);
                int nPsms = peptideMatch.getSpectrumCount();

                return filterItemComparator.passes(input, nPsms);

            case nValidatedPSMs:

                nPsms = identificationFeaturesGenerator.getNValidatedSpectraForPeptide(matchKey);

                return filterItemComparator.passes(input, nPsms);

            case nConfidentPSMs:

                nPsms = identificationFeaturesGenerator.getNConfidentSpectraForPeptide(matchKey);

                return filterItemComparator.passes(input, nPsms);

            case confidence:

                peptideMatch = identification.getPeptideMatch(matchKey);
                PSParameter psParameter = (PSParameter) peptideMatch.getUrParam(PSParameter.dummy);
                double confidence = psParameter.getConfidence();

                return filterItemComparator.passes(input, confidence);

            case proteinInference:

                peptideMatch = identification.getPeptideMatch(matchKey);
                psParameter = (PSParameter) peptideMatch.getUrParam(PSParameter.dummy);
                int pi = psParameter.getProteinInferenceGroupClass();

                return filterItemComparator.passes(input, pi);

            case validationStatus:

                peptideMatch = identification.getPeptideMatch(matchKey);
                psParameter = (PSParameter) peptideMatch.getUrParam(PSParameter.dummy);
                int validation = psParameter.getMatchValidationLevel().getIndex();

                return filterItemComparator.passes(
                        Double.toString(MatchValidationLevel.getMatchValidationLevel(input).getIndex()),
                        validation
                );

            case stared:

                peptideMatch = identification.getPeptideMatch(matchKey);
                psParameter = (PSParameter) peptideMatch.getUrParam(PSParameter.dummy);
                String starred = psParameter.getStarred() ? FilterItemComparator.trueFalse[0] : FilterItemComparator.trueFalse[1];

                return filterItemComparator.passes(input, starred);

            default:
                throw new IllegalArgumentException("Protein filter not implemented for item " + filterItem.name + ".");
        }
    }

    @Override
    public FilterItem[] getPossibleFilterItems() {
        return PeptideFilterItem.values();
    }

    @Override
    public FilterItem getFilterItem(String itemName) {
        return PeptideFilterItem.getItem(itemName);
    }
}
