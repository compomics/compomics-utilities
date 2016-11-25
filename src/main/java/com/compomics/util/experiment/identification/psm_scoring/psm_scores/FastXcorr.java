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

    public enum SpectrumCorrectionMode {
        average, accurate;
    }
    /**
     * The peptide fragmentation model to use
     */
    private PeptideFragmentationModel peptideFragmentationModel;

    private SpectrumCorrectionMode spectrumCorrectionMode;

    /**
     * Constructor
     *
     * @param peptideFragmentationModel the peptide fragmentation model to use
     * @param spectrumCorrectionMode the type of spectrum correction used to
     * estimate y'
     */
    public FastXcorr(PeptideFragmentationModel peptideFragmentationModel, SpectrumCorrectionMode spectrumCorrectionMode) {
        this.peptideFragmentationModel = peptideFragmentationModel;
        this.spectrumCorrectionMode = spectrumCorrectionMode;
    }

    /**
     * Constructor
     *
     * @param peptideFragmentationModel the peptide fragmentation model to use
     */
    public FastXcorr(PeptideFragmentationModel peptideFragmentationModel) {
        this(peptideFragmentationModel, SpectrumCorrectionMode.average);
    }

    /**
     * Constructor using a unifrom fragmentation.
     */
    public FastXcorr() {
        this(PeptideFragmentationModel.uniform, SpectrumCorrectionMode.average);
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

        HashMap<Double, Double> weightedSpectrum = getYPrime(spectrum, annotationSettings);

        double xCorr = 0;
        for (IonMatch ionMatch : matches) {
            Peak peakI = ionMatch.peak;
            Double mzI = peakI.mz;
            Double x0I = peakI.intensity;
            Double yPrimeI = weightedSpectrum.get(mzI);
            Double xCorrI = x0I * yPrimeI;
            xCorr += xCorrI;
        }

        return xCorr;
    }

    private HashMap<Double, Double> getYPrime(MSnSpectrum spectrum, AnnotationSettings annotationSettings) {

        YPrime yPrime = new YPrime();
        yPrime = (YPrime) spectrum.getUrParam(yPrime);
        if (yPrime == null) {
            yPrime = estimateYPrime(spectrum, annotationSettings);
            spectrum.addUrParam(yPrime);
        }
        return yPrime.getValues();
    }

    private YPrime estimateYPrime(MSnSpectrum spectrum, AnnotationSettings annotationSettings) {

        HashMap<Double, Peak> peakList = spectrum.getPeakMap();
        HashMap<Double, Double> values = new HashMap<Double, Double>(peakList.size());

        SpectrumIndex spectrumIndex = new SpectrumIndex();
        spectrumIndex = (SpectrumIndex) spectrum.getUrParam(spectrumIndex);
        if (spectrumIndex == null) {
            // Create new index
            spectrumIndex = new SpectrumIndex(spectrum.getPeakMap(), spectrum.getIntensityLimit(annotationSettings.getAnnotationIntensityLimit()),
                    annotationSettings.getFragmentIonAccuracy(), annotationSettings.isFragmentIonPpm());
            spectrum.addUrParam(spectrumIndex);
        }
        
        int spectrumWidth = spectrumIndex.getBinMax() - spectrumIndex.getBinMin();

        for (Peak peak : peakList.values()) {
            Double mz0 = peak.mz;
            Double intensity0 = peak.intensity;
            int bin0 = spectrumIndex.getBin(mz0);
            double sum = 0.0;
            for (int bin : spectrumIndex.getRawBins()) {
                if (bin != bin0) {
                    HashMap<Double, Peak> peaksInBin = spectrumIndex.getPeaksInBin(bin);
                    double binIntensity = getBinIntensity(peaksInBin.values());
                    sum += binIntensity;
                }
            }
            sum /= spectrumWidth;
            Double weightedIntensity = intensity0 - sum;
            values.put(mz0, weightedIntensity);
        }

        YPrime yPrime = new YPrime();
        yPrime.setValues(values);
        return yPrime;
    }

    private double getBinIntensity(Collection<Peak> peaks) {
        switch (spectrumCorrectionMode) {
            case average:
                return getAverageIntensity(peaks);
            default:
                throw new UnsupportedOperationException("Spectrum correction mode " + spectrumCorrectionMode + " not implemented.");
        }
    }

    private double getAverageIntensity(Collection<Peak> peaks) {
        if (peaks == null) {
            return 0.0;
        }
        double binIntensity = 0.0;
        for (Peak peak : peaks) {
            binIntensity += peak.intensity;
        }
        binIntensity /= peaks.size();
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
