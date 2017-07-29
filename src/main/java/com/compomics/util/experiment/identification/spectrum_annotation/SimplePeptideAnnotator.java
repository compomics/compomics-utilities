package com.compomics.util.experiment.identification.spectrum_annotation;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Ion.IonType;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators.FragmentAnnotator;
import com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators.FragmentAnnotatorNL;
import com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators.ImmoniumIonAnnotator;
import com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators.PrecursorAnnotator;
import com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators.ReporterIonAnnotator;
import com.compomics.util.experiment.massspectrometry.indexes.SpectrumIndex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A simple annotator for peptides.
 *
 * @author Marc Vaudel
 */
public class SimplePeptideAnnotator {

    /**
     * The type of ion series to annotate.
     */
    public enum IonSeries {

        ax(PeptideFragmentIon.A_ION, PeptideFragmentIon.X_ION),
        by(PeptideFragmentIon.B_ION, PeptideFragmentIon.Y_ION),
        cz(PeptideFragmentIon.C_ION, PeptideFragmentIon.Z_ION);

        /**
         * The forward ion index as listed in the PeptideFragmentIon class.
         */
        public final int forwardSeries;
        /**
         * The complementary ion index as listed in the PeptideFragmentIon
         * class.
         */
        public final int complementarySeries;

        /**
         * Constructor.
         *
         * @param forwardSeries the index of the forward ion
         * @param complementarySeries the index of the complementary ion
         */
        private IonSeries(int forwardSeries, int complementarySeries) {
            this.forwardSeries = forwardSeries;
            this.complementarySeries = complementarySeries;
        }
    }

    /**
     * Annotator for the a and x ions.
     */
    private FragmentAnnotator axFragmentAnnotator;
    /**
     * Annotator for the b and y ions.
     */
    private FragmentAnnotator byFragmentAnnotator;
    /**
     * Annotator for the c and z ions.
     */
    private FragmentAnnotator czFragmentAnnotator;
    /**
     * Annotator for the a and x ions with neutral losses.
     */
    private FragmentAnnotatorNL axFragmentAnnotatorNL;
    /**
     * Annotator for the b and y ions with neutral losses.
     */
    private FragmentAnnotatorNL byFragmentAnnotatorNL;
    /**
     * Annotator for the c and z ions with neutral losses.
     */
    private FragmentAnnotatorNL czFragmentAnnotatorNL;
    /**
     * Annotator for the precursor ions.
     */
    private PrecursorAnnotator precursorAnnotator;
    /**
     * Annotator for the immonium and related ions.
     */
    private ImmoniumIonAnnotator immoniumIonAnnotator;
    /**
     * Annotator for the reporter ions.
     */
    private ReporterIonAnnotator reporterIonAnnotator;
    
    /**
     * Constructor.
     * 
     * @param peptide the peptide to annotate
     * @param charge the charge of the peptide
     * @param annotationSettings the annotation preferences
     * 
     * @throws java.lang.InterruptedException exception thrown if a thread is
     * interrupted
     */
    public SimplePeptideAnnotator(Peptide peptide, int charge, AnnotationSettings annotationSettings) throws InterruptedException {

        boolean neutralLossesSequence = annotationSettings.areNeutralLossesSequenceAuto();
        ArrayList<NeutralLoss> neutralLosses = annotationSettings.getNeutralLosses();

        HashMap<Ion.IonType, HashSet<Integer>> ionTypes = annotationSettings.getIonTypes();

        HashSet<Integer> peptideFragmentIons = ionTypes.get(IonType.PEPTIDE_FRAGMENT_ION);

        if (peptideFragmentIons != null) {

            for (int fragmentIonType : peptideFragmentIons) {
                IonSeries ionSeries;
                switch (fragmentIonType) {
                    case PeptideFragmentIon.A_ION:
                    case PeptideFragmentIon.X_ION:
                        ionSeries = IonSeries.ax;
                        if (axFragmentAnnotator == null) {
                            axFragmentAnnotator = new FragmentAnnotator(peptide, ionSeries, peptideFragmentIons.contains(PeptideFragmentIon.A_ION), peptideFragmentIons.contains(PeptideFragmentIon.X_ION));
                            if (!neutralLosses.isEmpty()) {
                                axFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, ionSeries, neutralLossesSequence, peptideFragmentIons.contains(PeptideFragmentIon.A_ION), peptideFragmentIons.contains(PeptideFragmentIon.X_ION));
                            }
                        }
                        break;
                    case PeptideFragmentIon.B_ION:
                    case PeptideFragmentIon.Y_ION:
                        ionSeries = IonSeries.by;
                        if (byFragmentAnnotator == null) {
                            byFragmentAnnotator = new FragmentAnnotator(peptide, ionSeries, peptideFragmentIons.contains(PeptideFragmentIon.B_ION), peptideFragmentIons.contains(PeptideFragmentIon.Y_ION));
                            if (!neutralLosses.isEmpty()) {
                                byFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, ionSeries, neutralLossesSequence, peptideFragmentIons.contains(PeptideFragmentIon.B_ION), peptideFragmentIons.contains(PeptideFragmentIon.Y_ION));
                            }
                        }
                        break;
                    case PeptideFragmentIon.C_ION:
                    case PeptideFragmentIon.Z_ION:
                        ionSeries = IonSeries.cz;
                        if (czFragmentAnnotator == null) {
                            czFragmentAnnotator = new FragmentAnnotator(peptide, ionSeries, peptideFragmentIons.contains(PeptideFragmentIon.C_ION), peptideFragmentIons.contains(PeptideFragmentIon.Z_ION));
                            if (!neutralLosses.isEmpty()) {
                                czFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, ionSeries, neutralLossesSequence, peptideFragmentIons.contains(PeptideFragmentIon.C_ION), peptideFragmentIons.contains(PeptideFragmentIon.Z_ION));
                            }
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Ion type " + fragmentIonType + " not supported.");
                }
            }
        }

        if (ionTypes.containsKey(IonType.PRECURSOR_ION)) {
            precursorAnnotator = new PrecursorAnnotator(peptide);
        }
        
        if (ionTypes.containsKey(IonType.IMMONIUM_ION) || ionTypes.containsKey(IonType.RELATED_ION)) {
        char[] peptideSequence = peptide.getSequence().toCharArray();
            immoniumIonAnnotator = new ImmoniumIonAnnotator(peptideSequence);
        }

        HashSet<Integer> reporterIonsIndexes = ionTypes.get(IonType.REPORTER_ION);
        if (reporterIonsIndexes != null) {
            ReporterIon[] reporterIons = new ReporterIon[reporterIonsIndexes.size()];
            int cpt = 0;
            for (int index : reporterIonsIndexes) {
                reporterIons[cpt++] = ReporterIon.getReporterIon(index);
            }
            reporterIonAnnotator = new ReporterIonAnnotator(reporterIons);
        }
    }

    /**
     * Constructor.
     * 
     * @param peptide the peptide to annotate
     * @param charge the charge of the peptide
     * @param a boolean indicating whether a ions should be annotated
     * @param b boolean indicating whether b ions should be annotated
     * @param c boolean indicating whether c ions should be annotated
     * @param x boolean indicating whether x ions should be annotated
     * @param y boolean indicating whether y ions should be annotated
     * @param z boolean indicating whether z ions should be annotated
     * @param precursor boolean indicating whether precursor ions should be annotated
     * @param immonium boolean indicating whether immonium ions should be annotated
     * @param related boolean indicating whether related ions should be annotated
     * @param reporter boolean indicating whether reporter ions should be annotated
     * @param neutralLosses boolean indicating whether fragment ions with neutral losses should be annotated
     * @param neutralLossesSequenceDependent  boolean indicating whether the neutral losses should be selected depending on the sequence
     * @param reporterIons the reporter ions to annotate
     * 
     * @throws java.lang.InterruptedException exception thrown if a thread is
     * interrupted
     */
    public SimplePeptideAnnotator(Peptide peptide, int charge, boolean a, boolean b, boolean c, boolean x, boolean y, boolean z,
            boolean precursor, boolean immonium, boolean related, boolean reporter, boolean neutralLosses, boolean neutralLossesSequenceDependent, ReporterIon[] reporterIons) throws InterruptedException {
        
        if (a & x) {
            axFragmentAnnotator = new FragmentAnnotator(peptide, IonSeries.ax);
            if (neutralLosses) {
                axFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, IonSeries.ax, neutralLossesSequenceDependent);
            }
        } else if (a || x) {
            axFragmentAnnotator = new FragmentAnnotator(peptide, IonSeries.ax, a, x);
            if (neutralLosses) {
                axFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, IonSeries.ax, neutralLossesSequenceDependent, a, x);
            }
        }
        if (b & y) {
            byFragmentAnnotator = new FragmentAnnotator(peptide, IonSeries.by);
            if (neutralLosses) {
                byFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, IonSeries.by, neutralLossesSequenceDependent);
            }
        } else if (b || y) {
            byFragmentAnnotator = new FragmentAnnotator(peptide, IonSeries.by, b, y);
            if (neutralLosses) {
                byFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, IonSeries.by, neutralLossesSequenceDependent, b, y);
            }
        }
        if (c & z) {
            czFragmentAnnotator = new FragmentAnnotator(peptide, IonSeries.cz);
            if (neutralLosses) {
                czFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, IonSeries.cz, neutralLossesSequenceDependent);
            }
        } else if (c || z) {
            czFragmentAnnotator = new FragmentAnnotator(peptide, IonSeries.cz, c, z);
            if (neutralLosses) {
                czFragmentAnnotatorNL = new FragmentAnnotatorNL(peptide, IonSeries.cz, neutralLossesSequenceDependent, c, z);
            }
        }
        if (precursor) {
            precursorAnnotator = new PrecursorAnnotator(peptide);
        }
        if (immonium || related) {
            immoniumIonAnnotator = new ImmoniumIonAnnotator(peptide.getSequence().toCharArray(), immonium, related);
        }
        if (reporter) {
            reporterIonAnnotator = new ReporterIonAnnotator(reporterIons);
        }
    }
 
    /**
     * Returns the ion matches for the given spectrum.
     * 
     * @param spectrumIndex the spectrum index
     * @param peptideCharge the peptide charge
     * @param precursorIsotopeMax the number of isotopes to test for the precursor ion
     * 
     * @return the ion matches in a list
     */
    public ArrayList<IonMatch> getIonMatches(SpectrumIndex spectrumIndex, int peptideCharge, int precursorIsotopeMax) {
        
        ArrayList<IonMatch> result = new ArrayList<IonMatch>(0);
        if (axFragmentAnnotator != null) {
            result.addAll(axFragmentAnnotator.getIonMatches(spectrumIndex, peptideCharge));
        }
        if (axFragmentAnnotatorNL != null) {
            result.addAll(axFragmentAnnotatorNL.getIonMatches(spectrumIndex, peptideCharge));
        }
        if (byFragmentAnnotator != null) {
            result.addAll(byFragmentAnnotator.getIonMatches(spectrumIndex, peptideCharge));
        }
        if (byFragmentAnnotatorNL != null) {
            result.addAll(byFragmentAnnotatorNL.getIonMatches(spectrumIndex, peptideCharge));
        }
        if (czFragmentAnnotator != null) {
            result.addAll(czFragmentAnnotator.getIonMatches(spectrumIndex, peptideCharge));
        }
        if (czFragmentAnnotatorNL != null) {
            result.addAll(czFragmentAnnotatorNL.getIonMatches(spectrumIndex, peptideCharge));
        }
        if (precursorAnnotator != null) {
            result.addAll(precursorAnnotator.getIonMatches(spectrumIndex, peptideCharge, precursorIsotopeMax));
        }
        if (immoniumIonAnnotator != null) {
            result.addAll(immoniumIonAnnotator.getIonMatches(spectrumIndex));
        }
        if (reporterIonAnnotator != null) {
            result.addAll(reporterIonAnnotator.getIonMatches(spectrumIndex));
        }
        return result;
    }
    
}
