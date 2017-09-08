package com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
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
import java.util.HashMap;

/**
 * A fragment annotator for peptide fragment ions with neutral losses.
 *
 * @author Marc Vaudel
 */
public class FragmentAnnotatorNL {

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
     * Array of the masses that can be lost by every forward fragment ion.
     */
    private final ArrayList<double[]> forwardNeutralLossesMasses;
    /**
     * List of the neutral losses that can be lost by every forward fragment
     * ion.
     */
    private final ArrayList<ArrayList<NeutralLoss>> forwardNeutralLosses;
    /**
     * Array of the masses that can be lost by every complementary fragment ion.
     */
    private final ArrayList<double[]> complementaryNeutralLossesMasses;
    /**
     * List of the neutral losses that can be lost by every complementary
     * fragment ion.
     */
    private final ArrayList<ArrayList<NeutralLoss>> complementaryNeutralLosses;

    /**
     * Constructor.
     *
     * @param peptide the peptide
     * @param ionSeries the ion series to annotate
     * @param sequenceDependent boolean indicating whether the H2O and NH3
     * losses should be adapted to the sequence
     */
    public FragmentAnnotatorNL(Peptide peptide, IonSeries ionSeries, boolean sequenceDependent) {

        char[] aas = peptide.getSequence().toCharArray();
        peptideLength = aas.length;
        forwardIonMz1 = new double[peptideLength];
        complementaryIonMz1 = new double[peptideLength];

        // Get the sequence neutral losses to inspect
        NeutralLoss[] losses = new NeutralLoss[]{NeutralLoss.H2O, NeutralLoss.NH3};
        int[][] lossesIndexes = new int[2][2];
        int[] sequenceLossesIndexes = sequenceDependent ? getLossesIndexes(aas) : new int[]{0, 0, peptideLength, peptideLength};
        lossesIndexes[0][0] = sequenceLossesIndexes[0];
        lossesIndexes[0][1] = sequenceLossesIndexes[2];
        lossesIndexes[1][0] = sequenceLossesIndexes[1];
        lossesIndexes[1][1] = sequenceLossesIndexes[3];

        // See if the peptide is modified
        double[] modificationsMasses = new double[peptideLength];
        ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
        if (modificationMatches != null) {

            // Keep track of the modified amino acids and possible losses
            HashMap<String, int[]> modificationLossesSites = new HashMap<String, int[]>(1);
            for (ModificationMatch modificationMatch : modificationMatches) {

                String modificationName = modificationMatch.getModification();
                Modification modification = ptmFactory.getModification(modificationName);
                double modificationMass = modification.getMass();

                int site = modificationMatch.getModificationSite();
                int siteIndex = site - 1;

                modificationsMasses[siteIndex] += modificationMass;

                for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {

                    int[] sites = modificationLossesSites.get(neutralLoss.name);
                    if (sites == null) {
                        sites = new int[]{siteIndex, siteIndex};
                        modificationLossesSites.put(neutralLoss.name, sites);
                    } else {
                        if (siteIndex < sites[0]) {
                            sites[0] = siteIndex;
                        } else if (siteIndex > sites[1]) {
                            sites[1] = siteIndex;
                        }
                    }
                }
            }
            if (!modificationLossesSites.isEmpty()) {
                int[][] newIndexes = new int[lossesIndexes.length + modificationLossesSites.size()][2];
                System.arraycopy(lossesIndexes, 0, newIndexes, 0, lossesIndexes.length);
                NeutralLoss[] newLosses = new NeutralLoss[losses.length + modificationLossesSites.size()];
                System.arraycopy(losses, 0, newLosses, 0, losses.length);
                int index = lossesIndexes.length;
                for (String lossName : modificationLossesSites.keySet()) {
                    int[] sites = modificationLossesSites.get(lossName);
                    newIndexes[index][1] = sites[0];
                    newIndexes[index][1] = sites[1];
                    newLosses[index] = NeutralLoss.getNeutralLoss(lossName);
                    index++;
                }
                lossesIndexes = newIndexes;
                losses = newLosses;
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

        forwardNeutralLossesMasses = new ArrayList<double[]>(peptideLength);
        forwardNeutralLosses = new ArrayList<ArrayList<NeutralLoss>>(peptideLength);
        complementaryNeutralLossesMasses = new ArrayList<double[]>(peptideLength);
        complementaryNeutralLosses = new ArrayList<ArrayList<NeutralLoss>>(peptideLength);

        for (int i = 0; i < peptideLength; i++) {

            char aa = aas[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            forwardMass += aminoAcid.getMonoisotopicMass();

            forwardMass += modificationsMasses[i];

            forwardIonMz1[i] = forwardMass;
            complementaryIonMz1[i] = complementaryMass - forwardMass;

            ArrayList<NeutralLoss> forwardIonlosses = new ArrayList<NeutralLoss>(lossesIndexes.length);
            ArrayList<NeutralLoss> complementaryIonlosses = new ArrayList<NeutralLoss>(lossesIndexes.length);
            double[] forwardMasses = new double[lossesIndexes.length];
            double[] complementaryMasses = new double[lossesIndexes.length];
            int indexForward = 0;
            int indexComplementary = 0;
            for (int j = 0; j < lossesIndexes.length; j++) {
                int[] indexes = lossesIndexes[j];
                NeutralLoss neutralLoss = losses[j];
                if (i >= indexes[0]) {
                    forwardIonlosses.add(neutralLoss);
                    forwardMasses[indexForward++] = neutralLoss.getMass();
                }
                if (i <= indexes[j]) {
                    complementaryIonlosses.add(neutralLoss);
                    complementaryMasses[indexComplementary++] = neutralLoss.getMass();
                }
            }
            forwardNeutralLossesMasses.add(forwardMasses);
            forwardNeutralLosses.add(forwardIonlosses);
            complementaryNeutralLossesMasses.add(complementaryMasses);
            complementaryNeutralLosses.add(complementaryIonlosses);
        }
    }

    /**
     * Constructor.
     *
     * @param peptide the peptide
     * @param ionSeries the ion series to annotate
     * @param sequenceDependent boolean indicating whether the H2O and NH3
     * losses should be adapted to the sequence
     * @param forward boolean indicating whether forward ions should be
     * annotated
     * @param complementary boolean indicating whether complementary ions should
     * be annotated
     */
    public FragmentAnnotatorNL(Peptide peptide, IonSeries ionSeries, boolean sequenceDependent, boolean forward, boolean complementary) {

        char[] aas = peptide.getSequence().toCharArray();
        peptideLength = aas.length;
        forwardIonMz1 = new double[peptideLength];
        complementaryIonMz1 = new double[peptideLength];

        // Get the sequence neutral losses to inspect
        NeutralLoss[] losses = new NeutralLoss[]{NeutralLoss.H2O, NeutralLoss.NH3};
        int[][] lossesIndexes = new int[2][2];
        int[] sequenceLossesIndexes = sequenceDependent ? getLossesIndexes(aas) : new int[]{0, 0, peptideLength, peptideLength};
        lossesIndexes[0][0] = sequenceLossesIndexes[0];
        lossesIndexes[0][1] = sequenceLossesIndexes[2];
        lossesIndexes[1][0] = sequenceLossesIndexes[1];
        lossesIndexes[1][1] = sequenceLossesIndexes[3];

        // See if the peptide is modified
        double[] modificationsMasses = new double[peptideLength];
        ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
        if (modificationMatches != null) {

            // Keep track of the modified amino acids and possible losses
            HashMap<String, int[]> modificationLossesSites = new HashMap<String, int[]>(1);
            for (ModificationMatch modificationMatch : modificationMatches) {

                String modificationName = modificationMatch.getModification();
                Modification modification = ptmFactory.getModification(modificationName);
                double modificationMass = modification.getMass();

                int site = modificationMatch.getModificationSite();
                int siteIndex = site - 1;

                modificationsMasses[siteIndex] += modificationMass;

                for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {

                    int[] sites = modificationLossesSites.get(neutralLoss.name);
                    if (sites == null) {
                        sites = new int[]{siteIndex, siteIndex};
                        modificationLossesSites.put(neutralLoss.name, sites);
                    } else {
                        if (siteIndex < sites[0]) {
                            sites[0] = siteIndex;
                        } else if (siteIndex > sites[1]) {
                            sites[1] = siteIndex;
                        }
                    }
                }
            }
            if (!modificationLossesSites.isEmpty()) {
                int[][] newIndexes = new int[lossesIndexes.length + modificationLossesSites.size()][2];
                System.arraycopy(lossesIndexes, 0, newIndexes, 0, lossesIndexes.length);
                NeutralLoss[] newLosses = new NeutralLoss[losses.length + modificationLossesSites.size()];
                System.arraycopy(losses, 0, newLosses, 0, losses.length);
                int index = lossesIndexes.length;
                for (String lossName : modificationLossesSites.keySet()) {
                    int[] sites = modificationLossesSites.get(lossName);
                    newIndexes[index][1] = sites[0];
                    newIndexes[index][1] = sites[1];
                    newLosses[index] = NeutralLoss.getNeutralLoss(lossName);
                    index++;
                }
                lossesIndexes = newIndexes;
                losses = newLosses;
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

        if (forward) {
            forwardNeutralLossesMasses = new ArrayList<double[]>(peptideLength);
            forwardNeutralLosses = new ArrayList<ArrayList<NeutralLoss>>(peptideLength);
        } else {
            forwardNeutralLossesMasses = null;
            forwardNeutralLosses = null;
        }
        if (complementary) {
            complementaryNeutralLossesMasses = new ArrayList<double[]>(peptideLength);
            complementaryNeutralLosses = new ArrayList<ArrayList<NeutralLoss>>(peptideLength);
        } else {
            complementaryNeutralLossesMasses = null;
            complementaryNeutralLosses = null;
        }

        for (int i = 0; i < peptideLength; i++) {

            char aa = aas[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            forwardMass += aminoAcid.getMonoisotopicMass();

            forwardMass += modificationsMasses[i];

            if (forward) {

                forwardIonMz1[i] = forwardMass;

                ArrayList<NeutralLoss> forwardIonlosses = new ArrayList<NeutralLoss>(lossesIndexes.length);
                double[] forwardMasses = new double[lossesIndexes.length];
                int indexForward = 0;
                for (int j = 0; j < lossesIndexes.length; j++) {
                    int[] indexes = lossesIndexes[j];
                    NeutralLoss neutralLoss = losses[j];
                    if (i >= indexes[0]) {
                        forwardIonlosses.add(neutralLoss);
                        forwardMasses[indexForward++] = neutralLoss.getMass();
                    }
                }
                forwardNeutralLossesMasses.add(forwardMasses);
                forwardNeutralLosses.add(forwardIonlosses);
            }

            if (complementary) {
                complementaryIonMz1[i] = complementaryMass - forwardMass;

                ArrayList<NeutralLoss> complementaryIonlosses = new ArrayList<NeutralLoss>(lossesIndexes.length);
                double[] complementaryMasses = new double[lossesIndexes.length];
                int indexComplementary = 0;
                for (int j = 0; j < lossesIndexes.length; j++) {
                    int[] indexes = lossesIndexes[j];
                    NeutralLoss neutralLoss = losses[j];
                    if (i <= indexes[j]) {
                        complementaryIonlosses.add(neutralLoss);
                        complementaryMasses[indexComplementary++] = neutralLoss.getMass();
                    }
                }
                complementaryNeutralLossesMasses.add(complementaryMasses);
                complementaryNeutralLosses.add(complementaryIonlosses);
            }
        }
    }

    /**
     * Returns the index at which the water and ammonia losses can occur in an
     * array.
     *
     * @param aas the amino acid sequence as char array
     *
     * @return the index at which the water and ammonia losses can occur in an
     * array
     */
    private int[] getLossesIndexes(char[] aas) {

        int[] lossesIndexes = {aas.length, aas.length, 0, 0};

        boolean waterForward = false;
        boolean ammoniaForward = false;
        boolean waterComplementary = false;
        boolean ammoniaComplementary = false;
        for (int i = 0; i < aas.length; i++) {

            char aa = aas[i];
            if (!waterForward) {
                for (char lossAa : NeutralLoss.H2O.aminoAcids) {
                    if (aa == lossAa) {
                        lossesIndexes[0] = i;
                        waterForward = true;
                        break;
                    }
                }
            }
            if (!ammoniaForward) {
                for (char lossAa : NeutralLoss.NH3.aminoAcids) {
                    if (aa == lossAa) {
                        lossesIndexes[1] = i;
                        ammoniaForward = true;
                        break;
                    }
                }
            }

            int rewindI = aas.length - i - 1;
            aa = aas[rewindI];
            if (!waterComplementary) {
                for (char lossAa : NeutralLoss.H2O.aminoAcids) {
                    if (aa == lossAa) {
                        lossesIndexes[2] = rewindI;
                        waterComplementary = true;
                        break;
                    }
                }
            }
            if (!ammoniaComplementary) {
                for (char lossAa : NeutralLoss.NH3.aminoAcids) {
                    if (aa == lossAa) {
                        lossesIndexes[3] = rewindI;
                        ammoniaComplementary = true;
                        break;
                    }
                }
            }
            if (waterForward && ammoniaForward && waterComplementary && ammoniaComplementary) {
                break;
            }
        }
        return lossesIndexes;
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

            double ionMz1 = forwardIonMz1[i];
            int ionNumber = i + 1;
            ArrayList<NeutralLoss> neutralLosses = forwardNeutralLosses.get(i);
            double[] neutralLossesMasses = forwardNeutralLossesMasses.get(i);
            for (int j = 0; j < neutralLosses.size(); j++) {
                double lossMass1 = neutralLossesMasses[j];
                double mz1WithLoss = ionMz1 - lossMass1;
                ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(mz1WithLoss);
                if (!peaks.isEmpty()) {
                    NeutralLoss[] ionLosses = {neutralLosses.get(j)};
                    double ionMass = mz1WithLoss - ElementaryIon.proton.getTheoreticMass();
                    for (Peak peak : peaks) {
                        Ion ion = new PeptideFragmentIon(forwardIonType, ionNumber, ionMass, ionLosses);
                        results.add(new IonMatch(peak, ion, 1));
                    }
                }
                for (int k = j + 1; k < neutralLosses.size(); k++) {
                    double lossMass2 = neutralLossesMasses[k];
                    double mz1WithLoss2 = mz1WithLoss - lossMass2;
                    peaks = spectrumIndex.getMatchingPeaks(mz1WithLoss2);
                    if (!peaks.isEmpty()) {
                        NeutralLoss[] ionLosses = {neutralLosses.get(j), neutralLosses.get(k)};
                        double ionMass = mz1WithLoss2 - ElementaryIon.proton.getTheoreticMass();
                        for (Peak peak : peaks) {
                            Ion ion = new PeptideFragmentIon(forwardIonType, ionNumber, ionMass, ionLosses);
                            results.add(new IonMatch(peak, ion, 1));
                        }
                    }
                }
            }

            ionMz1 = complementaryIonMz1[i];
            ionNumber = peptideLength - ionNumber;
            neutralLosses = complementaryNeutralLosses.get(i);
            neutralLossesMasses = complementaryNeutralLossesMasses.get(i);
            for (int j = 0; j < neutralLosses.size(); j++) {
                double lossMass1 = neutralLossesMasses[j];
                double mz1WithLoss = ionMz1 - lossMass1;
                ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(mz1WithLoss);
                if (!peaks.isEmpty()) {
                    NeutralLoss[] ionLosses = {neutralLosses.get(j)};
                    double ionMass = mz1WithLoss - ElementaryIon.proton.getTheoreticMass();
                    for (Peak peak : peaks) {
                        Ion ion = new PeptideFragmentIon(complementaryIonType, ionNumber, ionMass, ionLosses);
                        results.add(new IonMatch(peak, ion, 1));
                    }
                }
                for (int k = j + 1; k < neutralLosses.size(); k++) {
                    double lossMass2 = neutralLossesMasses[k];
                    double mz1WithLoss2 = mz1WithLoss - lossMass2;
                    peaks = spectrumIndex.getMatchingPeaks(mz1WithLoss2);
                    double ionMass = mz1WithLoss2 - ElementaryIon.proton.getTheoreticMass();
                    if (!peaks.isEmpty()) {
                        NeutralLoss[] ionLosses = {neutralLosses.get(j), neutralLosses.get(k)};
                        for (Peak peak : peaks) {
                            Ion ion = new PeptideFragmentIon(complementaryIonType, ionNumber, ionMass, ionLosses);
                            results.add(new IonMatch(peak, ion, 1));
                        }
                    }
                }
            }
        }

        for (int ionCharge = 2; ionCharge < peptideCharge; ionCharge++) {

            int extraProtons = ionCharge - 1;
            double protonContribution = ElementaryIon.getProtonMassMultiple(extraProtons);

            for (int i = 0; i < peptideLength; i++) {

                double ionMz1 = forwardIonMz1[i];
                int ionNumber = i + 1;
                ArrayList<NeutralLoss> neutralLosses = forwardNeutralLosses.get(i);
                double[] neutralLossesMasses = forwardNeutralLossesMasses.get(i);
                for (int j = 0; j < neutralLosses.size(); j++) {
                    double lossMass1 = neutralLossesMasses[j];
                    double mz1WithLoss = ionMz1 - lossMass1;
                    double mzWithLoss = (mz1WithLoss + protonContribution) / ionCharge;
                    ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(mzWithLoss);
                    if (!peaks.isEmpty()) {
                        NeutralLoss[] ionLosses = {neutralLosses.get(j)};
                        double ionMass = mz1WithLoss - ElementaryIon.proton.getTheoreticMass();
                        for (Peak peak : peaks) {
                            Ion ion = new PeptideFragmentIon(forwardIonType, ionNumber, ionMass, ionLosses);
                            results.add(new IonMatch(peak, ion, ionCharge));
                        }
                    }
                    for (int k = j + 1; k < neutralLosses.size(); k++) {
                        double lossMass2 = neutralLossesMasses[k];
                        double mz1WithLoss2 = mz1WithLoss - lossMass2;
                        double mzWithLoss2 = (mz1WithLoss2 + protonContribution) / ionCharge;
                        peaks = spectrumIndex.getMatchingPeaks(mzWithLoss2);
                        if (!peaks.isEmpty()) {
                            NeutralLoss[] ionLosses = {neutralLosses.get(j), neutralLosses.get(k)};
                            double ionMass = mz1WithLoss2 - ElementaryIon.proton.getTheoreticMass();
                            for (Peak peak : peaks) {
                                Ion ion = new PeptideFragmentIon(forwardIonType, ionNumber, ionMass, ionLosses);
                                results.add(new IonMatch(peak, ion, ionCharge));
                            }
                        }
                    }
                }

                ionMz1 = complementaryIonMz1[i];
                ionNumber = peptideLength - ionNumber;
                neutralLosses = complementaryNeutralLosses.get(i);
                neutralLossesMasses = complementaryNeutralLossesMasses.get(i);
                for (int j = 0; j < neutralLosses.size(); j++) {
                    double lossMass1 = neutralLossesMasses[j];
                    double mz1WithLoss = ionMz1 - lossMass1;
                    double mzWithLoss = (mz1WithLoss + protonContribution) / ionCharge;
                    ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(mzWithLoss);
                    if (!peaks.isEmpty()) {
                        NeutralLoss[] ionLosses = {neutralLosses.get(j)};
                        double ionMass = mz1WithLoss - ElementaryIon.proton.getTheoreticMass();
                        for (Peak peak : peaks) {
                            Ion ion = new PeptideFragmentIon(complementaryIonType, ionNumber, ionMass, ionLosses);
                            results.add(new IonMatch(peak, ion, ionCharge));
                        }
                    }
                    for (int k = j + 1; k < neutralLosses.size(); k++) {
                        double lossMass2 = neutralLossesMasses[k];
                        double mz1WithLoss2 = mz1WithLoss - lossMass2;
                        double mzWithLoss2 = (mz1WithLoss2 + protonContribution) / ionCharge;
                        peaks = spectrumIndex.getMatchingPeaks(mzWithLoss2);
                        double ionMass = mz1WithLoss2 - ElementaryIon.proton.getTheoreticMass();
                        if (!peaks.isEmpty()) {
                            NeutralLoss[] ionLosses = {neutralLosses.get(j), neutralLosses.get(k)};
                            for (Peak peak : peaks) {
                                Ion ion = new PeptideFragmentIon(complementaryIonType, ionNumber, ionMass, ionLosses);
                                results.add(new IonMatch(peak, ion, ionCharge));
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

}
