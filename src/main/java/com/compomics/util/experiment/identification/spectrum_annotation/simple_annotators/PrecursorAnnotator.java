package com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators;

import com.compomics.util.experiment.biology.ElementaryElement;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.biology.ions.PrecursorIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.massspectrometry.spectra.Peak;
import com.compomics.util.experiment.massspectrometry.indexes.SpectrumIndex;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Annotator for the precursor peaks.
 *
 * @author Marc Vaudel
 */
public class PrecursorAnnotator {

    /**
     * The modifications factory.
     */
    private final ModificationFactory ptmFactory = ModificationFactory.getInstance();

    /**
     * The mass of the precursor to annotate.
     */
    private double precursorMass;
    /**
     * The masses of the neutral losses to consider.
     */
    private double[] neutralLossesMasses;
    /**
     * Array of the neutral losses to consider.
     */
    private NeutralLoss[] neutralLosses = null;

    /**
     * Constructor.
     *
     * @param peptide the peptide of interest.
     *
     * @throws java.lang.InterruptedException exception thrown if a thread is
     * interrupted
     */
    public PrecursorAnnotator(Peptide peptide) throws InterruptedException {

        precursorMass = peptide.getMass();

        ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
        if (modificationMatches != null) {
            HashSet<String> modificationLosses = new HashSet<String>(0);
            for (ModificationMatch modificationMatch : modificationMatches) {

                String modificationName = modificationMatch.getModification();
                Modification modification = ptmFactory.getModification(modificationName);

                for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {
                    modificationLosses.add(neutralLoss.name);
                }
            }

            if (!modificationLosses.isEmpty()) {
                int nNeutralLosses = modificationLosses.size() + 2;
                neutralLosses = new NeutralLoss[nNeutralLosses];
                neutralLossesMasses = new double[nNeutralLosses];
                int cpt = 0;
                neutralLosses[cpt] = NeutralLoss.H2O;
                neutralLossesMasses[cpt++] = NeutralLoss.H2O.getMass();
                neutralLosses[cpt] = NeutralLoss.NH3;
                neutralLossesMasses[cpt++] = NeutralLoss.NH3.getMass();
                for (String modificationLoss : modificationLosses) {
                    NeutralLoss neutralLoss = NeutralLoss.getNeutralLoss(modificationLoss);
                    neutralLosses[cpt] = neutralLoss;
                    neutralLossesMasses[cpt++] = neutralLoss.getMass();
                }
            } else {
                neutralLosses = new NeutralLoss[]{NeutralLoss.H2O, NeutralLoss.NH3};
                neutralLossesMasses = new double[]{NeutralLoss.H2O.getMass(), NeutralLoss.NH3.getMass()};
            }
        } else {
            neutralLosses = new NeutralLoss[]{NeutralLoss.H2O, NeutralLoss.NH3};
            neutralLossesMasses = new double[]{NeutralLoss.H2O.getMass(), NeutralLoss.NH3.getMass()};
        }

    }

    /**
     * Returns the ions matched in the given spectrum at the given charge.
     *
     * @param spectrumIndex the index of the spectrum
     * @param peptideCharge the charge of the peptide
     * @param isotopeMax the maximal isotopic value to annotate (inclusive)
     *
     * @return the ions matched in the given spectrum at the given charge
     */
    public ArrayList<IonMatch> getIonMatches(SpectrumIndex spectrumIndex, int peptideCharge, int isotopeMax) {

        ArrayList<IonMatch> results = new ArrayList<IonMatch>(0);

        double protonatedMass = precursorMass + ElementaryIon.getProtonMassMultiple(peptideCharge);

        for (int isotope = 0; isotope <= isotopeMax; isotope++) {

            double mass = protonatedMass + ElementaryElement.getNeutronMassMultiple(isotope);

            double mz = mass / peptideCharge;

            ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(mz);
            for (Peak peak : peaks) {
                Ion ion = new PrecursorIon(precursorMass, null);
                results.add(new IonMatch(peak, ion, peptideCharge));
            }

            for (int i = 0; i < neutralLosses.length; i++) {

                NeutralLoss neutralLoss1 = neutralLosses[i];

                double massWithLoss1 = mass - neutralLoss1.getMass();
                mz = massWithLoss1 / peptideCharge;
                peaks = spectrumIndex.getMatchingPeaks(mz);

                if (!peaks.isEmpty()) {

                    NeutralLoss[] ionLosses = {neutralLoss1};

                    for (Peak peak : peaks) {

                        Ion ion = new PrecursorIon(massWithLoss1, ionLosses);
                        results.add(new IonMatch(peak, ion, peptideCharge));
                    }
                }

                for (int j = i + 1; j < neutralLosses.length; j++) {

                    NeutralLoss neutralLoss2 = neutralLosses[j];
                    double massWithLoss2 = massWithLoss1 - neutralLoss2.getMass();
                    mz = massWithLoss1 / peptideCharge;
                    peaks = spectrumIndex.getMatchingPeaks(mz);

                    if (!peaks.isEmpty()) {

                        NeutralLoss[] ionLosses = {neutralLoss1, neutralLoss2};

                        for (Peak peak : peaks) {

                            Ion ion = new PrecursorIon(massWithLoss2, ionLosses);
                            results.add(new IonMatch(peak, ion, peptideCharge));
                        }
                    }
                }
            }
        }

        return results;
    }

}
