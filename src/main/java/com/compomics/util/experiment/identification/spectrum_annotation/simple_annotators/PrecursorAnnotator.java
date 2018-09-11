package com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators;

import com.compomics.util.experiment.biology.atoms.ElementaryElement;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.biology.ions.impl.PrecursorIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.indexes.SpectrumIndex;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Annotator for the precursor peaks.
 *
 * @author Marc Vaudel
 */
public class PrecursorAnnotator {

    /**
     * The modifications factory.
     */
    private final ModificationFactory modificationFactory = ModificationFactory.getInstance();

    /**
     * The mass of the precursor to annotate.
     */
    private final double precursorMass;
    /**
     * The masses of the neutral losses to consider.
     */
    private final double[] neutralLossesMasses;
    /**
     * Array of the neutral losses to consider.
     */
    private NeutralLoss[] neutralLosses = null;

    /**
     * Constructor. Fixed modifications must be indexed as provided by the peptide class.
     *
     * @param peptide the peptide of interest.
     * @param fixedModifications the fixed modifications
     */
    public PrecursorAnnotator(Peptide peptide, String[] fixedModifications) {

        precursorMass = peptide.getMass();

        HashSet<String> fixedModificationLosses = Arrays.stream(fixedModifications)
                .filter(modName -> modName != null)
                .map(modName -> modificationFactory.getModification(modName))
                .flatMap(modification -> modification.getNeutralLosses().stream())
                .map(neutralLoss -> neutralLoss.name)
                .collect(Collectors.toCollection(HashSet::new));

        ModificationMatch[] variableModifications = peptide.getVariableModifications();
        HashSet<String> variableModificationLosses = Arrays.stream(variableModifications)
                .map(modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()))
                .flatMap(modification -> modification.getNeutralLosses().stream())
                .map(neutralLoss -> neutralLoss.name)
                .collect(Collectors.toCollection(HashSet::new));

        HashSet<String> modificationLosses = new HashSet<>(fixedModificationLosses);
        modificationLosses.addAll(variableModificationLosses);

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

        ArrayList<IonMatch> results = new ArrayList<>(0);

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
