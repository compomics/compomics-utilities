package com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.mass_spectrometry.utils.StandardMasses;
import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.SimplePeptideAnnotator.IonSeries;
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.indexes.SpectrumIndex;
import java.util.ArrayList;

/**
 * Annotator for b and y ions without neutral losses.
 *
 * @author Marc Vaudel
 */
public class FragmentAnnotator {

    /**
     * The modifications factory.
     */
    private final ModificationFactory ptmFactory = ModificationFactory.getInstance();
    /**
     * Array of the forward ion m/z with charge 1.
     */
    private final double[] forwardIonMz1;
    /**
     * Array of the complementary ion m/z with charge 1.
     */
    private final double[] complementaryIonMz1;
    /**
     * Length of the peptide sequence.
     */
    private final int peptideLength;
    /**
     * The type of forward ion annotated.
     */
    private final int forwardIonType;
    /**
     * The type of forward ion annotated.
     */
    private final int complementaryIonType;

    /**
     * Constructor.
     *
     * @param peptide the peptide
     * @param ionSeries the ion series to annotate
     */
    public FragmentAnnotator(Peptide peptide, IonSeries ionSeries) {
        this(peptide, ionSeries, true, true);
    }

    /**
     * Constructor.
     *
     * @param peptide the peptide
     * @param ionSeries the ion series to annotate
     * @param forward boolean indicating whether forward ions should be
     * annotated
     * @param complementary boolean indicating whether complementary ions should
     * be annotated
     */
    public FragmentAnnotator(Peptide peptide, IonSeries ionSeries, boolean forward, boolean complementary) {

        char[] aas = peptide.getSequence().toCharArray();
        peptideLength = aas.length;
        forwardIonMz1 = new double[peptideLength];
        complementaryIonMz1 = new double[peptideLength];

        double[] modificationsMasses = new double[peptideLength];
        ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
        if (modificationMatches != null) {

            for (ModificationMatch modificationMatch : modificationMatches) {

                String modificationName = modificationMatch.getModification();
                Modification modification = ptmFactory.getModification(modificationName);
                double modificationMass = modification.getMass();

                int site = modificationMatch.getModificationSite();

                modificationsMasses[site - 1] += modificationMass;
            }
        }

        double forwardMass;
        double complementaryMass;
        if (ionSeries == IonSeries.by) {
            forwardMass = ElementaryIon.proton.getTheoreticMass();
            complementaryMass = peptide.getMass() + ElementaryIon.protonMassMultiples[2];
            forwardIonType = PeptideFragmentIon.B_ION;
            complementaryIonType = PeptideFragmentIon.Y_ION;
        } else if (ionSeries == IonSeries.cz) {
            forwardMass = ElementaryIon.proton.getTheoreticMass() + StandardMasses.nh3.mass;
            complementaryMass = peptide.getMass() + ElementaryIon.protonMassMultiples[2] - StandardMasses.nh3.mass;
            forwardIonType = PeptideFragmentIon.C_ION;
            complementaryIonType = PeptideFragmentIon.Z_ION;
        } else if (ionSeries == IonSeries.ax) {
            forwardMass = ElementaryIon.proton.getTheoreticMass() - StandardMasses.co.mass;
            complementaryMass = peptide.getMass() + ElementaryIon.protonMassMultiples[2] + StandardMasses.co.mass;
            forwardIonType = PeptideFragmentIon.A_ION;
            complementaryIonType = PeptideFragmentIon.X_ION;
        } else {
            throw new UnsupportedOperationException("Ion series " + ionSeries + " not supported.");
        }
        for (int i = 0; i < peptideLength; i++) {

            char aa = aas[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            forwardMass += aminoAcid.getMonoisotopicMass();

            forwardMass += modificationsMasses[i];

            if (forward) {
                forwardIonMz1[i] = forwardMass;
            }
            if (complementary) {
                complementaryIonMz1[i] = complementaryMass - forwardMass;
            }
        }
    }

    /**
     * Returns the ions matched in the given spectrum at the given charge.
     *
     * @param spectrumIndex the index of the spectrum
     * @param peptideCharge the charge of the peptide
     *
     * @return the ions matched in the given spectrum at the given charge
     */
    public ArrayList<IonMatch> getIonMatches(SpectrumIndex spectrumIndex, int peptideCharge) {

        ArrayList<IonMatch> results = new ArrayList<IonMatch>(0);

        for (int i = 0; i < peptideLength; i++) {

            double ionMz = forwardIonMz1[i];
            ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(ionMz);

            if (!peaks.isEmpty()) {

                int ionNumber = i + 1;
                double ionMass = ionMz - ElementaryIon.proton.getTheoreticMass();

                for (Peak peak : peaks) {
                    Ion ion = new PeptideFragmentIon(forwardIonType, ionNumber, ionMass, null);
                    results.add(new IonMatch(peak, ion, 1));
                }
            }

            ionMz = complementaryIonMz1[i];
            peaks = spectrumIndex.getMatchingPeaks(ionMz);

            if (!peaks.isEmpty()) {

                double ionMass = ionMz - ElementaryIon.proton.getTheoreticMass();
                int ionNumber = peptideLength - i - 1;

                for (Peak peak : peaks) {
                    Ion ion = new PeptideFragmentIon(complementaryIonType, ionNumber, ionMass, null);
                    results.add(new IonMatch(peak, ion, 1));
                }
            }
        }

        for (int ionCharge = 2; ionCharge < peptideCharge; ionCharge++) {

            int extraProtons = ionCharge - 1;
            double protonContribution = ElementaryIon.getProtonMassMultiple(extraProtons);

            for (int i = 0; i < peptideLength; i++) {

                double ionMz1 = forwardIonMz1[i];
                double ionMz = (ionMz1 + protonContribution) / ionCharge;
                ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(ionMz);

                if (!peaks.isEmpty()) {

                    int ionNumber = i + 1;
                    double ionMass = ionMz1 - ElementaryIon.proton.getTheoreticMass();

                    for (Peak peak : peaks) {
                        Ion ion = new PeptideFragmentIon(forwardIonType, ionNumber, ionMass, null);
                        results.add(new IonMatch(peak, ion, ionCharge));
                    }
                }

                ionMz1 = complementaryIonMz1[i];
                ionMz = (ionMz1 + protonContribution) / ionCharge;
                peaks = spectrumIndex.getMatchingPeaks(ionMz);

                if (!peaks.isEmpty()) {

                    double ionMass = ionMz1 - ElementaryIon.proton.getTheoreticMass();
                    int ionNumber = peptideLength - i - 1;

                    for (Peak peak : peaks) {
                        Ion ion = new PeptideFragmentIon(complementaryIonType, ionNumber, ionMass, null);
                        results.add(new IonMatch(peak, ion, ionCharge));
                    }
                }
            }
        }

        return results;
    }

}
