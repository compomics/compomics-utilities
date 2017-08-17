package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.protein_sequences.AaOccurrence;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.spectra.MSnSpectrum;
import com.compomics.util.experiment.mass_spectrometry.SimpleNoiseDistribution;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.MathException;
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
    private static double limitLog10 = -FastMath.log10(Double.MIN_VALUE);
    /**
     * The occurrence of amino acids in the database.
     */
    private AaOccurrence aaOccurrence;
    
    /**
     * Constructor.
     * 
     * @param aaOccurrence the amino acid occurrence in the database
     */
    public SnrScore(AaOccurrence aaOccurrence) {
        this.aaOccurrence = aaOccurrence;
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
     *
     * @return the score of the match
     *
     * @throws java.lang.InterruptedException exception thrown if a threading
     * error occurs
     * @throws org.apache.commons.math.MathException exception if an exception
     * occurs when calculating logs
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationSettings, SpecificAnnotationSettings specificAnnotationSettings, PeptideSpectrumAnnotator peptideSpectrumAnnotator) throws InterruptedException, MathException {
        ArrayList<IonMatch> ionMatchesList = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, false);
        return getScore(peptide, spectrum, ionMatchesList);
    }

    /**
     * Returns the score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param ionMatchesList the ion matches obtained from spectrum annotation
     *
     * @return the score of the match
     *
     * @throws java.lang.InterruptedException exception thrown if a threading
     * error occurs
     * @throws org.apache.commons.math.MathException exception if an exception
     * occurs when calculating logs
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, ArrayList<IonMatch> ionMatchesList) throws InterruptedException, MathException {
        HashMap<Double, ArrayList<IonMatch>> ionMatches = new HashMap<Double, ArrayList<IonMatch>>(ionMatchesList.size());
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
     *
     * @throws java.lang.InterruptedException exception thrown if a threading
     * error occurs
     * @throws org.apache.commons.math.MathException exception if an exception
     * occurs when calculating logs
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Double, ArrayList<IonMatch>> ionMatches) throws InterruptedException, MathException {

        char[] sequence = peptide.getSequence().toCharArray();
        int sequenceLength = sequence.length;
        
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

                        double aasP;
                        if (peptideFragmentIon.getSubType() == PeptideFragmentIon.A_ION
                                || peptideFragmentIon.getSubType() == PeptideFragmentIon.B_ION
                                || peptideFragmentIon.getSubType() == PeptideFragmentIon.C_ION) {
                            aasP = aaOccurrence.getP(sequence, 0, number, 4);
                        } else {
                            aasP = aaOccurrence.getP(sequence, sequenceLength-number, sequenceLength, 4);
                        }
                        pFragmentIonMinusLog += pMinusLog + aasP;
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
