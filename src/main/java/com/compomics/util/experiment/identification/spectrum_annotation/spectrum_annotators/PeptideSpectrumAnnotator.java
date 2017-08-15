package com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.massspectrometry.spectra.Spectrum;
import com.compomics.util.experiment.massspectrometry.spectra.Peak;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math.MathException;

/**
 * Annotates a spectrum with peptide fragments. Warning: operations are not
 * synchronized use one iterator per thread.
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
     * @param specificAnnotationSettings if provided, only the ions detectable
     * using these settings will be selected
     */
    public void setPeptide(Peptide peptide, int precursorCharge, SpecificAnnotationSettings specificAnnotationSettings) {
        setPeptide(peptide, null, precursorCharge, specificAnnotationSettings);
    }

    /**
     * Sets a new peptide to annotate.
     *
     * @param peptide the new peptide
     * @param possibleFragmentIons the possible fragment ions of the peptide
     * @param precursorCharge the new precursor charge
     * @param specificAnnotationSettings if provided, only the ions detectable
     * using these settings will be selected
     */
    public void setPeptide(Peptide peptide, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons, int precursorCharge, SpecificAnnotationSettings specificAnnotationSettings) {

        if (specificAnnotationSettings != null && super.specificAnnotationSettings == null
                || specificAnnotationSettings == null && super.specificAnnotationSettings != null
                || specificAnnotationSettings != null && super.specificAnnotationSettings != null && specificAnnotationSettings != super.specificAnnotationSettings
                || this.peptide == null
                || !this.peptide.getKey().equals(peptide.getKey())
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

                theoreticalFragmentIons = fragmentFactory.getFragmentIons(peptide, specificAnnotationSettings);

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
     * @param specificAnnotationSettings the specific annotation settings
     * @param peak the peak to match
     * @return a list of potential ion matches
     */
    public ArrayList<IonMatch> matchPeak(Peptide peptide, SpecificAnnotationSettings specificAnnotationSettings, Peak peak) {
        setPeptide(peptide, specificAnnotationSettings.getPrecursorCharge(), specificAnnotationSettings);
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
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public Stream<IonMatch> getSpectrumAnnotation(AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, Spectrum spectrum, Peptide peptide) {

        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, true);
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
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public Stream<IonMatch> getSpectrumAnnotation(AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, Spectrum spectrum, Peptide peptide, boolean useIntensityFilter) {

        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, null, useIntensityFilter);
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
     * @param possiblePeptideFragments the possible peptide fragments for this
     * peptide
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public Stream<IonMatch> getSpectrumAnnotation(AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, Spectrum spectrum, Peptide peptide,
            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possiblePeptideFragments, boolean useIntensityFilter) {

        setMassTolerance(specificAnnotationSettings.getFragmentIonAccuracy(), specificAnnotationSettings.isFragmentIonPpm(), annotationSettings.getTiesResolution());

        if (spectrum != null) {

            double spectrumIntensityLimit = useIntensityFilter ? spectrum.getIntensityLimit(annotationSettings.getIntensityThresholdType(), annotationSettings.getAnnotationIntensityLimit()) : 0.0;
            setSpectrum(spectrum, spectrumIntensityLimit);

        }

        setPeptide(peptide, possiblePeptideFragments, specificAnnotationSettings.getPrecursorCharge(), specificAnnotationSettings);

        HashMap<Ion.IonType, HashSet<Integer>> sepectedIonTypes = specificAnnotationSettings.getIonTypes();

        return sepectedIonTypes.entrySet().stream()
                .filter(entry1 -> theoreticalFragmentIons.containsKey(entry1.getKey().index))
                .flatMap(entry1 -> theoreticalFragmentIons.get(entry1.getKey().index).entrySet().stream()
                .filter(entry2 -> entry1.getValue().contains(entry2.getKey()))
                .flatMap(entry2 -> entry2.getValue().stream())
                .filter(ion -> lossesValidated(specificAnnotationSettings.getNeutralLossesMap(), ion))
                .flatMap(ion -> getPossibleCharges(ion.getType()).stream()
                .filter(charge -> chargeValidated(ion, charge, precursorCharge))
                .map(charge -> matchInSpectrum(ion, charge))
                .filter(ionMatch -> ionMatch != null)));
    }

    /**
     * Returns the possible charges for the given ion type.
     *
     * @param ionType the ion type
     *
     * @return the possible charges for the given ion type
     */
    private ArrayList<Integer> getPossibleCharges(Ion.IonType ionType) {
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
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return the ion matches corresponding to fragment ions indexed by amino
     * acid number in the sequence
     */
    public Map<Integer, ArrayList<IonMatch>> getCoveredAminoAcids(AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, Spectrum spectrum, Peptide peptide, boolean useIntensityFilter) {

        Stream<IonMatch> matches = getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide);

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
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public HashMap<Integer, ArrayList<Ion>> getExpectedIons(SpecificAnnotationSettings specificAnnotationSettings, Peptide peptide) {
        return getExpectedIons(specificAnnotationSettings, peptide, null);
    }

    /**
     * Returns the expected ions in a map indexed by the possible charges.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param specificAnnotationSettings the specific annotation settings
     * @param peptide The peptide of interest
     * @param possibleFragmentIons the possible fragment ions for the given
     * peptide
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public HashMap<Integer, ArrayList<Ion>> getExpectedIons(SpecificAnnotationSettings specificAnnotationSettings, Peptide peptide, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons) {
        setPeptide(peptide, possibleFragmentIons, specificAnnotationSettings.getPrecursorCharge(), specificAnnotationSettings);
        return getExpectedIons(specificAnnotationSettings);
    }

    @Override
    public ArrayList<IonMatch> getCurrentAnnotation(Spectrum spectrum, AnnotationSettings annotationSettings, SpecificAnnotationSettings specificAnnotationSettings, boolean useIntensityFilter) throws InterruptedException, MathException {
        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, useIntensityFilter)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide.
     *
     * @param peptide the peptide of interest
     * @param sequenceMatchingSettings the sequence matching settings for
     * peptide to protein mapping
     * @param ptmSequenceMatchingSettings the sequence matching settings for PTM
     * to peptide mapping
     *
     * @return the expected possible neutral losses
     */
    public static NeutralLossesMap getDefaultLosses(Peptide peptide, SequenceMatchingPreferences sequenceMatchingSettings,
            SequenceMatchingPreferences ptmSequenceMatchingSettings) {

        ModificationFactory modificationFactory = ModificationFactory.getInstance();
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        
        NeutralLossesMap neutralLossesMap = new NeutralLossesMap();

        String sequence = peptide.getSequence();
        int aaMin = sequence.length();
        int aaMax = 0;

        for (NeutralLoss neutralLoss : IonFactory.getDefaultNeutralLosses()) {

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

        if (peptide.isModified()) {

            HashMap<String, HashSet<Integer>> proteinMapping = peptide.getProteinMapping();

            for (ModificationMatch modMatch : peptide.getModificationMatches()) {

                Modification ptm = modificationFactory.getModification(modMatch.getModification());

                for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {

                    aaMin = sequence.length();
                    aaMax = 0;

                    for (String proteinAccession : proteinMapping.keySet()) {
                        
                        String proteinSequence = sequenceFactory.getProtein(proteinAccession).getSequence();
                        
                        for (int peptideStart : proteinMapping.get(proteinAccession)) {

                            ArrayList<Integer> indexes = peptide.getPotentialModificationSites(ptm, proteinSequence, peptideStart, ptmSequenceMatchingSettings);

                            if (!indexes.isEmpty()) {

                                aaMin = Math.min(aaMin, indexes.get(0));
                                aaMax = Math.max(aaMax, indexes.get(indexes.size() - 1));

                            }

                            neutralLossesMap.addNeutralLoss(neutralLoss, aaMin, sequence.length() - aaMax + 1);
                            
                        }
                    }
                }
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
