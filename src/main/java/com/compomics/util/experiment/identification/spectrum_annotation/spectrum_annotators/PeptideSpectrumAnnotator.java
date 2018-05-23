package com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.IonFactory;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Annotates a spectrum with peptide fragments. Warning: operations are not
 * synchronized use one annotator per thread.
 *
 * @author Marc Vaudel
 */
public class PeptideSpectrumAnnotator extends SpectrumAnnotator {

    /**
     * The peptide to annotate.
     */
    private Peptide peptide;

    /**
     * Constructor.
     */
    public PeptideSpectrumAnnotator() {

    }

    /**
     * Sets a new peptide to annotate.
     *
     * @param peptide the new peptide
     * @param precursorCharge the new precursor charge
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param specificAnnotationSettings if provided, only the ions detectable
     * using these settings will be selected
     */
    public void setPeptide(Peptide peptide, int precursorCharge, ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters, SpecificAnnotationParameters specificAnnotationSettings) {
        setPeptide(peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, null, precursorCharge, specificAnnotationSettings);
    }

    /**
     * Sets a new peptide to annotate.
     *
     * @param peptide the new peptide
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param possibleFragmentIons the possible fragment ions of the peptide
     * @param precursorCharge the new precursor charge
     * @param specificAnnotationSettings if provided, only the ions detectable
     * using these settings will be selected
     */
    public void setPeptide(Peptide peptide, ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons, int precursorCharge, SpecificAnnotationParameters specificAnnotationSettings) {

        if (specificAnnotationSettings != null && super.specificAnnotationSettings == null
                || specificAnnotationSettings == null && super.specificAnnotationSettings != null
                || specificAnnotationSettings != null && super.specificAnnotationSettings != null && specificAnnotationSettings != super.specificAnnotationSettings
                || this.peptide == null
                || this.peptide.getKey() != peptide.getKey()
                || !this.peptide.sameModificationsAs(peptide)
                || this.precursorCharge != precursorCharge) {

            // Set new values
            this.peptide = peptide;
            this.precursorCharge = precursorCharge;
            this.defaultPrecursorCharges = new ArrayList<>(precursorCharge);

            for (int charge = 1; charge <= precursorCharge; charge++) {

                defaultPrecursorCharges.add(charge);

            }

            if (possibleFragmentIons == null) {

                theoreticalFragmentIons = fragmentFactory.getFragmentIons(peptide, specificAnnotationSettings, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters);

            } else {

                theoreticalFragmentIons = possibleFragmentIons;

            }
            if (massShift != 0 || massShiftNTerm != 0 || massShiftCTerm != 0) {

                updateMassShifts();

            }
        }
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a
     * given peak according to the annotation settings.
     *
     * @param peptide the peptide
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param specificAnnotationSettings the specific annotation settings
     * @param peak the peak to match
     * @return a list of potential ion matches
     */
    public ArrayList<IonMatch> matchPeak(Peptide peptide, ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters,
            SpecificAnnotationParameters specificAnnotationSettings, Peak peak) {
        setPeptide(peptide, specificAnnotationSettings.getPrecursorCharge(), modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, specificAnnotationSettings);
        return matchPeak(specificAnnotationSettings, peak);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches
     * using the intensity filter.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public IonMatch[] getSpectrumAnnotation(AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings, Spectrum spectrum, Peptide peptide, ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters) {

        return getSpectrumAnnotationStream(annotationSettings, specificAnnotationSettings, spectrum, peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, true)
                .toArray(IonMatch[]::new);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches
     * using the intensity filter.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public Stream<IonMatch> getSpectrumAnnotationStream(AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings, Spectrum spectrum, Peptide peptide, ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters) {

        return getSpectrumAnnotationStream(annotationSettings, specificAnnotationSettings, spectrum, peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, true);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public Stream<IonMatch> getSpectrumAnnotationStream(AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings, Spectrum spectrum, Peptide peptide, ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters, boolean useIntensityFilter) {

        return getSpectrumAnnotationStream(annotationSettings, specificAnnotationSettings, spectrum, peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, null, useIntensityFilter);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public IonMatch[] getSpectrumAnnotation(AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings, Spectrum spectrum, Peptide peptide, ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters, boolean useIntensityFilter) {

        return getSpectrumAnnotationStream(annotationSettings, specificAnnotationSettings, spectrum, peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, null, useIntensityFilter)
                .toArray(IonMatch[]::new);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param possiblePeptideFragments the possible peptide fragments for this
     * peptide
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public Stream<IonMatch> getSpectrumAnnotationStream(AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings, Spectrum spectrum, Peptide peptide,
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters,
            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possiblePeptideFragments, boolean useIntensityFilter) {

        setMassTolerance(specificAnnotationSettings.getFragmentIonAccuracy(), specificAnnotationSettings.isFragmentIonPpm(), annotationSettings.getTiesResolution());

        if (spectrum != null) {

            double spectrumIntensityLimit = useIntensityFilter ? spectrum.getIntensityLimit(annotationSettings.getIntensityThresholdType(), annotationSettings.getAnnotationIntensityLimit()) : 0.0;
            setSpectrum(spectrum, spectrumIntensityLimit);

        }

        setPeptide(peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, possiblePeptideFragments, specificAnnotationSettings.getPrecursorCharge(), specificAnnotationSettings);

        HashMap<Ion.IonType, HashSet<Integer>> sepectedIonTypes = specificAnnotationSettings.getIonTypes();

        return sepectedIonTypes.entrySet().stream()
                .filter(entry1 -> theoreticalFragmentIons.containsKey(entry1.getKey().index))
                .flatMap(entry1 -> theoreticalFragmentIons.get(entry1.getKey().index).entrySet().stream()
                .filter(entry2 -> entry1.getValue().contains(entry2.getKey()))
                .flatMap(entry2 -> entry2.getValue().stream())
                .filter(ion -> lossesValidated(specificAnnotationSettings.getNeutralLossesMap(), ion))
                .flatMap(ion -> getPossibleCharges(ion.getType(), specificAnnotationSettings).stream()
                .filter(charge -> chargeValidated(ion, charge, precursorCharge))
                .map(charge -> matchInSpectrum(ion, charge))
                .filter(ionMatch -> ionMatch != null)));
    }

    /**
     * Returns the possible charges for the given ion type.
     *
     * @param ionType the ion type
     * @param specificAnnotationSettings the specific annotation settings
     *
     * @return the possible charges for the given ion type
     */
    private ArrayList<Integer> getPossibleCharges(Ion.IonType ionType, SpecificAnnotationParameters specificAnnotationSettings) {
        return (ionType == Ion.IonType.PRECURSOR_ION) ? defaultPrecursorCharges : specificAnnotationSettings.getSelectedCharges();
    }

    /**
     * Returns the ion matches corresponding to fragment ions indexed by amino
     * acid number in the sequence. 1 is first amino acid.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum The spectrum to match
     * @param peptide The peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return the ion matches corresponding to fragment ions indexed by amino
     * acid number in the sequence
     */
    public Map<Integer, ArrayList<IonMatch>> getCoveredAminoAcids(AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings, Spectrum spectrum, Peptide peptide, ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters, boolean useIntensityFilter) {

        Stream<IonMatch> matches = getSpectrumAnnotationStream(annotationSettings, specificAnnotationSettings, spectrum, peptide,
                modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters);

        return matches.filter(ionMatch -> ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION)
                .collect(Collectors.groupingBy(
                        ionMatch -> ((PeptideFragmentIon) ionMatch.ion).getAaNumber(peptide.getSequence().length()),
                        Collectors.toCollection(ArrayList::new)));
    }

    /**
     * Returns the expected ions in a map indexed by the possible charges.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param specificAnnotationSettings the specific annotation settings
     * @param peptide The peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public HashMap<Integer, ArrayList<Ion>> getExpectedIons(SpecificAnnotationParameters specificAnnotationSettings, Peptide peptide, ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters) {
        return getExpectedIons(specificAnnotationSettings, peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, null);
    }

    /**
     * Returns the expected ions in a map indexed by the possible charges.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param specificAnnotationSettings the specific annotation settings
     * @param peptide The peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param possibleFragmentIons the possible fragment ions for the given
     * peptide
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public HashMap<Integer, ArrayList<Ion>> getExpectedIons(SpecificAnnotationParameters specificAnnotationSettings, Peptide peptide,
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationsSequenceMatchingParameters, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons) {
        setPeptide(peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, possibleFragmentIons, specificAnnotationSettings.getPrecursorCharge(), specificAnnotationSettings);
        return getExpectedIons(specificAnnotationSettings);
    }

    @Override
    public IonMatch[] getCurrentAnnotation(Spectrum spectrum, AnnotationParameters annotationSettings, SpecificAnnotationParameters specificAnnotationSettings, ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters, boolean useIntensityFilter) {
        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters, useIntensityFilter);
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide.
     *
     * @param peptide the peptide of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     *
     * @return the expected possible neutral losses
     */
    public static NeutralLossesMap getDefaultLosses(Peptide peptide, ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters) {

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        NeutralLossesMap neutralLossesMap = new NeutralLossesMap();

        String sequence = peptide.getSequence();
        int aaMin = sequence.length();
        int aaMax = 0;

        for (String nlName : IonFactory.getDefaultNeutralLosses()) {

            NeutralLoss neutralLoss = NeutralLoss.getNeutralLoss(nlName);
            char[] aas = neutralLoss.aminoAcids;

            if (aas != null) {

                for (char aa : aas) {

                    int firstIndex = sequence.indexOf(aa);
                    if (firstIndex != -1) {

                        aaMin = Math.min(firstIndex, aaMin);
                        aaMax = Math.max(sequence.lastIndexOf(aa), aaMax);

                    }
                }
                if (aaMin < sequence.length()) {

                    neutralLossesMap.addNeutralLoss(neutralLoss, aaMin + 1, sequence.length() - aaMax);

                }
            }
        }

        String[] fixedModifications = peptide.getFixedModifications(modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters);

        for (int i = 0; i < fixedModifications.length; i++) {

            String modName = fixedModifications[i];

            int site = ModificationUtils.getSite(i, fixedModifications.length);

            if (modName != null) {

                Modification modification = modificationFactory.getModification(modName);

                aaMin = site;
                aaMax = sequence.length() - site + 1;

                for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {

                    neutralLossesMap.addNeutralLoss(neutralLoss, aaMin, aaMax);

                }
            }
        }

        ModificationMatch[] modificationMatches = peptide.getVariableModifications();

        for (ModificationMatch modMatch : modificationMatches) {

            int site = ModificationUtils.getSite(modMatch.getSite(), fixedModifications.length);
            String modName = modMatch.getModification();
            Modification modification = modificationFactory.getModification(modName);

            aaMin = site;
            aaMax = sequence.length() - site + 1;

            for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {

                neutralLossesMap.addNeutralLoss(neutralLoss, aaMin, aaMax);

            }
        }

        return neutralLossesMap;
    }

    /**
     * Returns the currently inspected peptide.
     *
     * @return the currently inspected peptide
     */
    public Peptide getCurrentlyLoadedPeptide() {
        return peptide;
    }
}
