package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.peptide_fragmentation.PeptideFragmentationModel;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.indexes.SpectrumIndex;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.math.BasicMathFunctions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Simple cross correlation score.
 *
 * @author Marc Vaudel
 */
public class HyperScore {
    /**
     * The peptide fragmentation model to use
     */
    private PeptideFragmentationModel peptideFragmentationModel;

    /**
     * Constructor
     *
     * @param peptideFragmentationModel the peptide fragmentation model to use
     */
    public HyperScore(PeptideFragmentationModel peptideFragmentationModel) {
        this.peptideFragmentationModel = peptideFragmentationModel;
    }

    /**
     * Constructor using a unifrom fragmentation.
     */
    public HyperScore() {
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
        
        double xCorr = 0;
        HashSet<Integer> ionsForward = new HashSet<Integer>(1);
        HashSet<Integer> ionsRewind = new HashSet<Integer>(1);
        for (IonMatch ionMatch : matches) {
            Peak peakI = ionMatch.peak;
            Double x0I = peakI.intensity / spectrumIndex.getTotalIntensity();
            xCorr += x0I;
            Ion ion = ionMatch.ion;
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                if (ion.getSubType() == PeptideFragmentIon.X_ION
                        || ion.getSubType() == PeptideFragmentIon.Y_ION
                        || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                    ionsForward.add(peptideFragmentIon.getNumber());
                } else if (ion.getSubType() == PeptideFragmentIon.A_ION
                        || ion.getSubType() == PeptideFragmentIon.B_ION
                        || ion.getSubType() == PeptideFragmentIon.C_ION) {
                    ionsRewind.add(peptideFragmentIon.getNumber());
                }
            }
        }
        int nForward = ionsForward.size() > 20 ? 20 : ionsForward.size();
        int nRewind = ionsRewind.size() > 20 ? 20 : ionsRewind.size();
        long forwardFactorial = BasicMathFunctions.factorial(nForward);
        long rewindFactorial = BasicMathFunctions.factorial(nRewind);
        return Math.max(xCorr, 0.0) * forwardFactorial * rewindFactorial;
    }

}
