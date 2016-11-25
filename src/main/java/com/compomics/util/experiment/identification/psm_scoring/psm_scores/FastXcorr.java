package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.peptide_fragmentation.PeptideFragmentationModel;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.indexes.SpectrumIndex;
import com.compomics.util.experiment.personalization.UrParameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Adaptation of the XCorr according to
 * https://www.ncbi.nlm.nih.gov/pubmed/18774840, as an extension of
 * https://www.ncbi.nlm.nih.gov/pubmed/24226387.
 *
 * @author Marc Vaudel
 */
public class FastXcorr {
    /**
     * The peptide fragmentation model to use
     */
    private PeptideFragmentationModel peptideFragmentationModel;

    /**
     * Constructor
     *
     * @param peptideFragmentationModel the peptide fragmentation model to use
     */
    public FastXcorr(PeptideFragmentationModel peptideFragmentationModel) {
        this.peptideFragmentationModel = peptideFragmentationModel;
    }

    /**
     * Constructor using a unifrom fragmentation.
     */
    public FastXcorr() {
        this(PeptideFragmentationModel.uniform);
    }

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. The mass interquartile distance of the fragment ion mass
     * error is used as m/z fidelity score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param annotationSettings the general spectrum annotation settings
     * @param specificAnnotationSettings the annotation settings specific to
     * this psm
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     *
     * @return the score of the match
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationSettings, SpecificAnnotationSettings specificAnnotationSettings, PeptideSpectrumAnnotator peptideSpectrumAnnotator) {

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide);
        if (matches.isEmpty()) {
            return 0.0;
        }
        
        SpectrumIndex spectrumIndex = new SpectrumIndex();
        spectrumIndex = (SpectrumIndex) spectrum.getUrParam(spectrumIndex);
        if (spectrumIndex == null) {
            // Create new index
            spectrumIndex = new SpectrumIndex(spectrum.getPeakMap(), spectrum.getIntensityLimit(annotationSettings.getAnnotationIntensityLimit()),
                    annotationSettings.getFragmentIonAccuracy(), annotationSettings.isFragmentIonPpm());
            spectrum.addUrParam(spectrumIndex);
        }

        HashMap<Double, Double> weightedSpectrum = getYPrime(spectrum, spectrumIndex);
        
        double xCorr = 0;
        for (IonMatch ionMatch : matches) {
            Peak peakI = ionMatch.peak;
            Double mzI = peakI.mz;
            Double x0I = peakI.intensity / spectrumIndex.getTotalIntensity();
            Double yPrimeI = weightedSpectrum.get(mzI);
            xCorr += x0I * yPrimeI;
        }

        return Math.max(xCorr, 0.0);
    }

    private HashMap<Double, Double> getYPrime(MSnSpectrum spectrum, SpectrumIndex spectrumIndex) {

        YPrime yPrime = new YPrime();
        yPrime = (YPrime) spectrum.getUrParam(yPrime);
        if (yPrime == null) {
            yPrime = estimateYPrime(spectrum, spectrumIndex);
            spectrum.addUrParam(yPrime);
        }
        return yPrime.getValues();
    }

    private YPrime estimateYPrime(MSnSpectrum spectrum, SpectrumIndex spectrumIndex) {

        HashMap<Double, Peak> peakList = spectrum.getPeakMap();
        HashMap<Double, Double> values = new HashMap<Double, Double>(peakList.size());
        
        int spectrumWidth = spectrumIndex.getBinMax() - spectrumIndex.getBinMin();

            double backgroundSum = 0.0;
            for (int bin : spectrumIndex.getRawBins()) {
                    HashMap<Double, Peak> peaksInBin = spectrumIndex.getPeaksInBin(bin);
                    double binIntensity = getBinIntensity(peaksInBin.values());
                    backgroundSum += binIntensity;
            }
            double backgroundYPrime = backgroundSum / spectrumWidth;
            
        for (Peak peak : peakList.values()) {
            Double mz0 = peak.mz;
            Double intensity0 = peak.intensity;
            int bin0 = spectrumIndex.getBin(mz0);
            HashMap<Double, Peak> peaksInBin = spectrumIndex.getPeaksInBin(bin0);
            double averageIntensity;
            if (peaksInBin != null) {
                double binIntensity = getBinIntensity(peaksInBin.values());
                averageIntensity = backgroundSum - binIntensity;
                averageIntensity /= spectrumWidth;
            } else {
                averageIntensity = backgroundYPrime;
            }
            Double weightedIntensity = intensity0 - averageIntensity;
            weightedIntensity /= spectrumIndex.getTotalIntensity();
            values.put(mz0, weightedIntensity);
        }

        YPrime yPrime = new YPrime();
        yPrime.setValues(values);
        return yPrime;
    }

    private double getBinIntensity(Collection<Peak> peaks) {
        if (peaks == null) {
            return 0.0;
        }
        double binIntensity = 0.0;
        for (Peak peak : peaks) {
            binIntensity += peak.intensity;
        }
        return binIntensity;
    }

    private class YPrime implements UrParameter {

        private HashMap<Double, Double> values;

        public YPrime() {

        }

        public HashMap<Double, Double> getValues() {
            return values;
        }

        public void setValues(HashMap<Double, Double> values) {
            this.values = values;
        }

        @Override
        public String getParameterKey() {
            return "FastXcorr_YPrime";
        }

    }

}
