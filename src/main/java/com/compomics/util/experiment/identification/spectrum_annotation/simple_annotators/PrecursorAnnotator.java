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
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.indexes.SpectrumIndex;
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
     * Empty default constructor
     */
    public PrecursorAnnotator() {
        precursorMass = 0;
        neutralLossesMasses = null;
    }

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
     * Constructor. Fixed modifications must be indexed as provided by the
     * peptide class.
     *
     * @param peptide the peptide of interest.
     * @param fixedModifications the fixed modifications
     */
    public PrecursorAnnotator(
            Peptide peptide,
            String[] fixedModifications
    ) {

        precursorMass = peptide.getMass();

        HashSet<String> fixedModificationLosses = Arrays.stream(fixedModifications)
                .filter(
                        modName -> modName != null
                )
                .map(
                        modName -> modificationFactory.getModification(modName)
                )
                .flatMap(
                        modification -> modification.getNeutralLosses().stream()
                )
                .map(
                        neutralLoss -> neutralLoss.name
                )
                .collect(
                        Collectors.toCollection(HashSet::new)
                );

        ModificationMatch[] variableModifications = peptide.getVariableModifications();
        HashSet<String> variableModificationLosses = Arrays.stream(variableModifications)
                .map(
                        modificationMatch -> modificationFactory.getModification(modificationMatch.getModification())
                )
                .flatMap(
                        modification -> modification.getNeutralLosses().stream()
                )
                .map(
                        neutralLoss -> neutralLoss.name
                )
                .collect(
                        Collectors.toCollection(HashSet::new)
                );

        HashSet<String> modificationLosses = new HashSet<>(fixedModificationLosses);
        modificationLosses.addAll(variableModificationLosses);

        if (!modificationLosses.isEmpty()) {

            int nNeutralLosses = modificationLosses.size() + 2;
            neutralLosses = new NeutralLoss[nNeutralLosses];
            neutralLossesMasses = new double[nNeutralLosses];
            int count = 0;
            neutralLosses[count] = NeutralLoss.H2O;
            neutralLossesMasses[count++] = NeutralLoss.H2O.getMass();
            neutralLosses[count] = NeutralLoss.NH3;
            neutralLossesMasses[count++] = NeutralLoss.NH3.getMass();

            for (String modificationLoss : modificationLosses) {

                NeutralLoss neutralLoss = NeutralLoss.getNeutralLoss(modificationLoss);
                neutralLosses[count] = neutralLoss;
                neutralLossesMasses[count++] = neutralLoss.getMass();

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
    public ArrayList<IonMatch> getIonMatches(
            SpectrumIndex spectrumIndex,
            int peptideCharge,
            int isotopeMax
    ) {

        ArrayList<IonMatch> results = new ArrayList<>(0);

        double protonatedMass = precursorMass + ElementaryIon.getProtonMassMultiple(peptideCharge);

        for (int isotope = 0; isotope <= isotopeMax; isotope++) {

            double mass = protonatedMass + ElementaryElement.getNeutronMassMultiple(isotope);

            double mz = mass / peptideCharge;

            int[] indexes = spectrumIndex.getMatchingPeaks(mz);

            if (indexes.length > 0) {

                Ion ion = new PrecursorIon(precursorMass, null);

                for (int index : indexes) {

                    results.add(
                            new IonMatch(
                                    spectrumIndex.mzArray[index],
                                    spectrumIndex.intensityArray[index],
                                    ion,
                                    peptideCharge
                            )
                    );
                }
            }

            for (int i = 0; i < neutralLosses.length; i++) {

                NeutralLoss neutralLoss1 = neutralLosses[i];

                double massWithLoss1 = mass - neutralLoss1.getMass();
                mz = massWithLoss1 / peptideCharge;
                indexes = spectrumIndex.getMatchingPeaks(mz);

                if (indexes.length > 0) {

                    NeutralLoss[] ionLosses = {neutralLoss1};
                    Ion ion = new PrecursorIon(massWithLoss1, ionLosses);

                    for (int index : indexes) {

                        results.add(
                                new IonMatch(
                                        spectrumIndex.mzArray[index],
                                        spectrumIndex.intensityArray[index],
                                        ion,
                                        peptideCharge
                                )
                        );
                    }
                }

                for (int j = i + 1; j < neutralLosses.length; j++) {

                    NeutralLoss neutralLoss2 = neutralLosses[j];
                    double massWithLoss2 = massWithLoss1 - neutralLoss2.getMass();
                    mz = massWithLoss1 / peptideCharge;
                    indexes = spectrumIndex.getMatchingPeaks(mz);

                    if (indexes.length > 0) {

                        NeutralLoss[] ionLosses = {neutralLoss1, neutralLoss2};
                        Ion ion = new PrecursorIon(massWithLoss2, ionLosses);

                        for (int index : indexes) {

                            results.add(
                                    new IonMatch(
                                            spectrumIndex.mzArray[index],
                                            spectrumIndex.intensityArray[index],
                                            ion,
                                            peptideCharge
                                    )
                            );
                        }
                    }
                }
            }
        }

        return results;
    }

}
