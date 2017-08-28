package com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.biology.ions.impl.ImmoniumIon;
import com.compomics.util.experiment.biology.ions.impl.RelatedIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.indexes.SpectrumIndex;
import java.util.ArrayList;

/**
 * Annotator for immonium and related ions.
 *
 * @author Marc Vaudel
 */
public class ImmoniumIonAnnotator {

    /**
     * Array of amino acids that can generate immonium ions.
     */
    private final char[] aas;
    /**
     * Array of immonium m/z corresponding to the amino acids.
     */
    private final double[] immoniumIonsMz;
    /**
     * Array of possible related ions.
     */
    private RelatedIon[] relatedIons;
    /**
     * M/z of the related ions.
     */
    private double[] relatedIonsMz;

    /**
     * Constructor. Warning: there is no check for amino acid uniticy, if
     * duplicates are present, they will be reported multiple times.
     *
     * @param peptideSequence the peptide sequence
     */
    public ImmoniumIonAnnotator(char[] peptideSequence) {
        this(peptideSequence, true, true);
    }

    /**
     * Constructor. Warning: there is no check for amino acid uniticy, if
     * duplicates are present, they will be reported multiple times.
     *
     * @param peptideSequence the peptide sequence
     * @param immonium boolean indicating whether immonium ions should be
     * annotated
     * @param related boolean indicating whether related ions should be
     * annotated
     */
    public ImmoniumIonAnnotator(char[] peptideSequence, boolean immonium, boolean related) {

        aas = peptideSequence;
        immoniumIonsMz = new double[peptideSequence.length];

        relatedIons = new RelatedIon[0];
        relatedIonsMz = new double[0];

        for (int i = 1; i < aas.length; i++) {

            char aa = aas[i];

            if (immonium) {
                immoniumIonsMz[i] = ImmoniumIon.getImmoniumIon(aa).getTheoreticMass() + ElementaryIon.proton.getTheoreticMass();
            }

            if (related) {
                ArrayList<RelatedIon> aaRelatedIons = RelatedIon.getRelatedIons(aa);
                if (aaRelatedIons != null) {
                    int j = relatedIons.length;
                    int newLength = j + aaRelatedIons.size();
                    RelatedIon[] newIons = new RelatedIon[newLength];
                    double[] newMz = new double[newLength];
                    System.arraycopy(relatedIons, 0, newIons, 0, relatedIons.length);
                    System.arraycopy(relatedIonsMz, 0, newMz, 0, relatedIonsMz.length);
                    for (RelatedIon relatedIon : aaRelatedIons) {
                        newIons[j] = relatedIon;
                        newMz[j++] = relatedIon.getTheoreticMass() + ElementaryIon.proton.getTheoreticMass();
                    }
                    relatedIons = newIons;
                    relatedIonsMz = newMz;
                }
            }
        }
    }

    /**
     * Returns the ions matched in the given spectrum.
     *
     * @param spectrumIndex the index of the spectrum
     *
     * @return the ions matched in the given spectrum
     */
    public ArrayList<IonMatch> getIonMatches(SpectrumIndex spectrumIndex) {

        ArrayList<IonMatch> results = new ArrayList<IonMatch>(0);

        // Immonium ions
        for (int i = 0; i < aas.length; i++) {

            double ionMz = immoniumIonsMz[i];
            char aa = aas[i];
            ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(ionMz);

            for (Peak peak : peaks) {

                Ion ion = ImmoniumIon.getImmoniumIon(aa);
                results.add(new IonMatch(peak, ion, 1));

            }
        }

        // Related ions
        for (int i = 0; i < relatedIons.length; i++) {

            double ionMz = relatedIonsMz[i];
            RelatedIon relatedIon = relatedIons[i];
            ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(ionMz);

            for (Peak peak : peaks) {

                results.add(new IonMatch(peak, relatedIon, 1));

            }

        }

        return results;
    }
}
