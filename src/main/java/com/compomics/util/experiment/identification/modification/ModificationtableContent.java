package com.compomics.util.experiment.identification.modification;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Convenience class for the content of a PTM table.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ModificationtableContent {

    /**
     * The content of the table: modification status &gt; fragment ion type
     * according to the peptide fragment ion static fields &gt; aa number &gt;
     * list of intensities.
     */
    private final HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> map;
    /**
     * The total intensity.
     */
    private double totalIntensity = 0;
    /**
     * The max intensity.
     */
    private double maxIntensity = 0;

    /**
     * Constructor.
     */
    public ModificationtableContent() {
        map = new HashMap<>();
    }

    /**
     * Add intensity.
     *
     * @param nMod the modification number
     * @param peptideFragmentIonType the peptide fragment ion type
     * @param aa the amino acid
     * @param intensity the intensity
     */
    public void addIntensity(int nMod, Integer peptideFragmentIonType, int aa, double intensity) {
        if (!map.containsKey(nMod)) {
            map.put(nMod, new HashMap<>());
        }
        if (!map.get(nMod).containsKey(peptideFragmentIonType)) {
            map.get(nMod).put(peptideFragmentIonType, new HashMap<>());
        }
        if (!map.get(nMod).get(peptideFragmentIonType).containsKey(aa)) {
            map.get(nMod).get(peptideFragmentIonType).put(aa, new ArrayList<>());
        }
        map.get(nMod).get(peptideFragmentIonType).get(aa).add(intensity);
        totalIntensity += intensity;
        if (intensity > maxIntensity) {
            maxIntensity = intensity;
        }
    }

    /**
     * Get intensity.
     *
     * @param nMod the modification number
     * @param peptideFragmentIonType the peptide fragment ion type
     * @param aa the amino acid
     * @return the list of intensities
     */
    public ArrayList<Double> getIntensities(int nMod, Integer peptideFragmentIonType, int aa) {
        if (map.containsKey(nMod)
                && map.get(nMod).containsKey(peptideFragmentIonType)
                && map.get(nMod).get(peptideFragmentIonType).containsKey(aa)) {
            return map.get(nMod).get(peptideFragmentIonType).get(aa);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get the quantile.
     *
     * @param nMod the modification number
     * @param peptideFragmentIonType the peptide fragment ion type
     * @param aa the amino acid
     * @param quantile the quantile
     * @return the quantile
     */
    public Double getQuantile(int nMod, Integer peptideFragmentIonType, int aa, double quantile) {
        ArrayList<Double> intensities = getIntensities(nMod, peptideFragmentIonType, aa);
        if (intensities.size() > 0) {
            int index = (int) (quantile * intensities.size());
            return intensities.get(index);
        } else {
            return 0.0;
        }
    }

    /**
     * Get histogram.
     *
     * @param nMod the modification number
     * @param peptideFragmentIonType the peptide fragment ion type
     * @param aa the amino acid
     * @param bins the bins
     * @return the histogram
     */
    public int[] getHistogram(int nMod, Integer peptideFragmentIonType, int aa, int bins) {
        ArrayList<Double> intensities = getIntensities(nMod, peptideFragmentIonType, aa);

        int[] values = new int[bins];

        if (intensities.size() > 0) {

            for (Double intensity : intensities) {

                double currentIntensity = intensity; // / maxIntensity;
                for (int j = 0; j < bins; j++) {

                    double index = (double) j;

                    if (((index / bins) < currentIntensity) && (currentIntensity < (index + 1) / bins)) {
                        values[j]++;
                    }
                }

                // make sure that the max value is included
                if (currentIntensity == 1) {
                    values[values.length - 1]++;
                }
            }

            return values;
        } else {
            return values;
        }
    }

    /**
     * Get the map.
     *
     * @return the map
     */
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> getMap() {
        return map;
    }

    /**
     * Add all.
     *
     * @param anotherContent another table content
     */
    public void addAll(ModificationtableContent anotherContent) {

        for (int nMod : anotherContent.getMap().keySet()) {

            for (Integer peptideFragmentIonType : anotherContent.getMap().get(nMod).keySet()) {

                for (int nAA : anotherContent.getMap().get(nMod).get(peptideFragmentIonType).keySet()) {

                    for (double intensity : anotherContent.getIntensities(nMod, peptideFragmentIonType, nAA)) {

                        addIntensity(nMod, peptideFragmentIonType, nAA, intensity);

                    }
                }
            }
        }
    }

    /**
     * Normalize intensities.
     */
    public void normalize() {

        if (totalIntensity > 0) {

            double normalization = totalIntensity;
            totalIntensity = 0;
            maxIntensity = 0;
            ArrayList<Double> tempIntensities;

            for (int nMod : map.keySet()) {

                for (Integer peptideFragmentIonType : map.get(nMod).keySet()) {

                    for (int nAA : map.get(nMod).get(peptideFragmentIonType).keySet()) {

                        tempIntensities = new ArrayList<>();

                        for (double intensity : getIntensities(nMod, peptideFragmentIonType, nAA)) {

                            tempIntensities.add(intensity / normalization);

                        }

                        map.get(nMod).get(peptideFragmentIonType).put(nAA, tempIntensities);

                    }
                }
            }
        }
    }

    /**
     * Returns the max intensity.
     *
     * @return the max intensity
     */
    public double getMaxIntensity() {
        return maxIntensity;
    }

    /**
     * Returns the modification plot series in the JFreechart format for one
     * PSM.
     *
     * @param peptide the peptide of interest
     * @param modification the modification to score
     * @param nMod the number of times the modification is expected
     * @param spectrum the corresponding spectrum
     * @param annotationParameters the annotation preferences
     * @param specificAnnotationParameters the specific annotation preferences
     * @param modificationParameters the modification parameters
     * @param sequenceProvider a provider for the protein sequences
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences for modification to peptide mapping
     *
     * @return the modification plot series in the JFreechart format for one
     * PSM.
     */
    public static HashMap<PeptideFragmentIon, ArrayList<IonMatch>> getModificationPlotData(Peptide peptide, Modification modification, int nMod, Spectrum spectrum,
            AnnotationParameters annotationParameters, SpecificAnnotationParameters specificAnnotationParameters, 
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingParameters) {

        ModificationMatch[] modificationMatches = peptide.getVariableModifications();

        ModificationMatch[] newMatches = Arrays.stream(modificationMatches)
                .filter(modificationMatch -> !modificationMatch.getModification().equals(modification.getName()))
                .toArray(ModificationMatch[]::new);

        Peptide noModPeptide = new Peptide(peptide.getSequence(), newMatches);

        PeptideSpectrumAnnotator spectrumAnnotator = new PeptideSpectrumAnnotator();
        HashMap<Integer, ArrayList<Ion>> fragmentIons
                = spectrumAnnotator.getExpectedIons(specificAnnotationParameters, noModPeptide, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
        HashMap<PeptideFragmentIon, ArrayList<IonMatch>> map = new HashMap<>(); //@TODO: refactor using another key for the map

        for (int i = 0; i <= nMod; i++) {

            spectrumAnnotator.setMassShift(i * modification.getMass());

            IonMatch[] matches = spectrumAnnotator.getSpectrumAnnotation(annotationParameters, specificAnnotationParameters, spectrum, noModPeptide, 
                    modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);

            for (IonMatch ionMatch : matches) {
                if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                    PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;
                    for (Ion noModIon : fragmentIons.get(ionMatch.charge)) {
                        if (noModIon.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION
                                && peptideFragmentIon.isSameAs(noModIon)) {
                            PeptideFragmentIon noModFragmentIon = (PeptideFragmentIon) noModIon;
                            if (!map.containsKey(noModFragmentIon)) {
                                map.put(noModFragmentIon, new ArrayList<>());
                            }
                            map.get(noModFragmentIon).add(ionMatch);
                            break;
                        }
                    }
                }
            }
        }

        return map;
    }

    /**
     * Get the table content.
     *
     * @param peptideAssumption the peptide assumption
     * @param modification the modification to score
     * @param nMod the number of times the modification is expected
     * @param spectrum the corresponding spectrum
     * @param annotationParameters the annotation parameters
     * @param modificationParameters the modification parameters
     * @param sequenceProvider a provider for the protein sequences
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences for modification to peptide mapping
     *
     * @return the table content
     */
    public static ModificationtableContent getModificationTableContent(PeptideAssumption peptideAssumption, Modification modification, int nMod, Spectrum spectrum,
            AnnotationParameters annotationParameters, 
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingParameters) {

        Peptide peptide = peptideAssumption.getPeptide();
        ModificationtableContent tableContent = new ModificationtableContent();

        HashSet<String> forbiddenMod = new HashSet<>(1);
        forbiddenMod.add(modification.getName());
        Peptide noModPeptide = peptide.getNoModPeptide(forbiddenMod);
        
        PeptideSpectrumAnnotator spectrumAnnotator = new PeptideSpectrumAnnotator();
        SpecificAnnotationParameters specificAnnotationParameters = annotationParameters.getSpecificAnnotationParameters(spectrum.getSpectrumKey(), peptideAssumption, modificationParameters, sequenceProvider, modificationSequenceMatchingParameters, spectrumAnnotator);
        spectrumAnnotator.setPeptide(noModPeptide, specificAnnotationParameters.getPrecursorCharge(), 
                    modificationParameters, sequenceProvider, modificationSequenceMatchingParameters, specificAnnotationParameters);
        
        NeutralLossesMap lossesMap = new NeutralLossesMap();
        
        for (String neutralLossName : specificAnnotationParameters.getNeutralLossesMap().getAccountedNeutralLosses()) {
        
            NeutralLoss neutralLoss = NeutralLoss.getNeutralLoss(neutralLossName);
            
            if (Math.abs(neutralLoss.getMass() - modification.getMass()) > specificAnnotationParameters.getFragmentIonAccuracyInDa(spectrum.getMaxMz())) {
            
                lossesMap.addNeutralLoss(neutralLoss, 1, 1);
            
            }
        }

        for (int i = 0; i <= nMod; i++) {

            spectrumAnnotator.setMassShift(i * modification.getMass());

            final int index = i;
            Stream<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotationStream(annotationParameters, specificAnnotationParameters, spectrum, noModPeptide, 
                    modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
            matches.filter(ionMatch -> ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION)
                    .forEach(ionMatch -> {
                        PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;
                        tableContent.addIntensity(index, peptideFragmentIon.getSubType(), peptideFragmentIon.getAaNumber(peptide.getSequence().length()), ionMatch.peak.intensity);
                    });

        }

        return tableContent;
    }
}
