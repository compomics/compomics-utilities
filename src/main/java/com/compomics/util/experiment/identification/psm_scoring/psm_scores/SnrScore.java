package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.mass_spectrometry.SimpleNoiseDistribution;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.util.FastMath;

/**
 * This score uses the intensity distribution of the peaks to evaluate an SNR
 * score.
 *
 * @author Marc Vaudel
 */
public class SnrScore {

    /**
     * Log10 value of the lowest limit of a double.
     */
    private static final double limitLog10 = -FastMath.log10(Double.MIN_VALUE);
    
    /**
     * Constructor.
     */
    public SnrScore() {
    }

    /**
     * Returns the score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param annotationSettings the general spectrum annotation settings
     * @param specificAnnotationSettings the annotation settings specific to
     * this PSM
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     * @param modificationParameters the modification parameters
     * @param sequenceProvider a provider for the protein sequences
     * @param modificationSequenceMatchingParameters the sequence matching
     * preferences for modification to peptide mapping
     *
     * @return the score of the match
     */
    public double getScore(Peptide peptide, Spectrum spectrum, AnnotationParameters annotationSettings, SpecificAnnotationParameters specificAnnotationSettings, PeptideSpectrumAnnotator peptideSpectrumAnnotator, 
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingParameters) {
        ArrayList<IonMatch> ionMatches = Lists.newArrayList(peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, 
                    modificationParameters, sequenceProvider, modificationSequenceMatchingParameters, false));
        return getScore(peptide, spectrum, ionMatches);
    }

    /**
     * Returns the score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param ionMatchesList the ion matches obtained from spectrum annotation
     *
     * @return the score of the match
     */
    public double getScore(Peptide peptide, Spectrum spectrum, ArrayList<IonMatch> ionMatchesList) {
        HashMap<Double, ArrayList<IonMatch>> ionMatches = new HashMap<>(ionMatchesList.size());
        for (IonMatch ionMatch : ionMatchesList) {
            double mz = ionMatch.peak.mz;
            ArrayList<IonMatch> peakMatches = ionMatches.get(mz);
            if (peakMatches == null) {
                peakMatches = new ArrayList<>(1);
                ionMatches.put(mz, peakMatches);
            }
            peakMatches.add(ionMatch);
        }
        return getScore(peptide, spectrum, ionMatches);
    }

    /**
     * Returns the score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param ionMatches the ion matches obtained from spectrum annotation
     * indexed by mz
     *
     * @return the score of the match
     */
    public double getScore(Peptide peptide, Spectrum spectrum, HashMap<Double, ArrayList<IonMatch>> ionMatches) {

        char[] sequence = peptide.getSequence().toCharArray();
        
        SimpleNoiseDistribution binnedCumulativeFunction = spectrum.getIntensityLogDistribution();
        
        double pFragmentIonMinusLog = 0.0;
        double pAnnotatedMinusLog = 0.0;

        for (double mz : ionMatches.keySet()) {

            ArrayList<IonMatch> peakMatches = ionMatches.get(mz);

            double intensity = peakMatches.get(0).peak.intensity;
            double pMinusLog = -binnedCumulativeFunction.getBinnedCumulativeProbabilityLog(intensity);

            for (IonMatch ionMatch : peakMatches) {

                if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {

                    PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;
                    int number = peptideFragmentIon.getNumber();

                    if (!peptideFragmentIon.hasNeutralLosses() && number >= 2) {

                        pFragmentIonMinusLog += pMinusLog;
                        break;

                    }
                }
            }

            pAnnotatedMinusLog += pMinusLog;

        }

        if (pFragmentIonMinusLog == 0.0) {
            return pFragmentIonMinusLog;
        }

        double pTotalMinusLog = 0.0;
        double[] intensities = spectrum.getIntensityValuesAsArray();

        for (double intensity : intensities) {

            double pMinusLog = -binnedCumulativeFunction.getBinnedCumulativeProbabilityLog(intensity);
            pTotalMinusLog += pMinusLog;
        }

        double pNotAnnotatedMinusLog = pTotalMinusLog - pAnnotatedMinusLog;

        if (pNotAnnotatedMinusLog < limitLog10) {

            double pNotAnnotated = FastMath.pow(10, -pNotAnnotatedMinusLog);
            if (pNotAnnotated > 1.0 - Double.MIN_VALUE) {
                pNotAnnotated = 1.0 - Double.MIN_VALUE;
            }
            pNotAnnotated = 1.0 - pNotAnnotated;
            double notAnnotatedCorrection = -FastMath.log10(pNotAnnotated);
            if (notAnnotatedCorrection > pAnnotatedMinusLog) {
                notAnnotatedCorrection = pAnnotatedMinusLog;
            }
            pFragmentIonMinusLog += notAnnotatedCorrection;

        }
        return pFragmentIonMinusLog;
    }
}
