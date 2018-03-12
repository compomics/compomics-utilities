package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.identification.amino_acid_tags.MassGap;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.ions.impl.ImmoniumIon;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.impl.PrecursorIon;
import com.compomics.util.experiment.biology.ions.impl.RelatedIon;
import com.compomics.util.experiment.biology.ions.impl.ReporterIon;
import com.compomics.util.experiment.biology.ions.impl.TagFragmentIon;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This factory generates the expected ions from a peptide.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IonFactory {

    /**
     * The instance of the factory.
     */
    private static IonFactory instance = null;
    /**
     * Neutral losses which will be looked for for every peptide independently
     * of the modifications found.
     */
    private static HashSet<String> defaultNeutralLosses = null;
    /**
     * Cache for the possible combinations of neutral losses.
     */
    private final HashMap<Long, NeutralLossCombination[]> neutralLossesCombinationsCache = new HashMap<>(4);
    /**
     * Cache for the mass of NH3.
     */
    private static final double nh3 = Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass();
    /**
     * Cache for the mass of CO.
     */
    private static final double co = Atom.C.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();
    /**
     * Cache for the mass of NO.
     */
    private static final double nMinusO = Atom.N.getMonoisotopicMass() - Atom.O.getMonoisotopicMass();
    /**
     * Cache for the mass of CO2.
     */
    private static final double co2 = Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass();
    /**
     * Cache for the mass of H2.
     */
    private static final double h2 = 2 * Atom.H.getMonoisotopicMass();
    /**
     * Cache for the mass of H2O.
     */
    private static final double h2o = 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();
    /**
     * Cache for the mass of HO.
     */
    private static final double ho = Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();

    /**
     * Constructor.
     */
    private IonFactory() {
    }

    /**
     * Static method which returns the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static IonFactory getInstance() {
        if (instance == null) {
            instance = new IonFactory();
        }
        return instance;
    }

    /**
     * Returns the default neutral losses.
     *
     * @return the default neutral losses
     */
    public static HashSet<String> getDefaultNeutralLosses() {
        if (defaultNeutralLosses == null) {
            setDefaultNeutralLosses();
        }
        return defaultNeutralLosses;
    }

    /**
     * Sets the default neutral losses.
     */
    private static synchronized void setDefaultNeutralLosses() {
        defaultNeutralLosses = new HashSet<>(2);
        defaultNeutralLosses.add(NeutralLoss.H2O.name);
        defaultNeutralLosses.add(NeutralLoss.NH3.name);
    }

    /**
     * Returns a list containing the default neutral losses and the losses found
     * in the given modifications. Note: modifications must be loaded in the
     * modification factory.
     *
     * @param modificationParameters the modification parameters
     *
     * @return the neutral losses expected in the dataset
     */
    public static HashSet<String> getNeutralLosses(ModificationParameters modificationParameters) {

        HashSet<String> neutralLosses = new HashSet<>(getDefaultNeutralLosses());
        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        neutralLosses.addAll(modificationParameters.getAllModifications().stream()
                .flatMap(modName -> modificationFactory.getModification(modName).getNeutralLosses().stream())
                .map(neutralLoss -> neutralLoss.name)
                .collect(Collectors.toSet()));

        return neutralLosses;
    }

    /**
     * Returns the reporter ions to annotate with the given PTM settings.
     *
     * @param modificationParameters the PTMs to annotate
     *
     * @return a hashset of the subtype indexes of the reporter ions to annotate
     */
    public static HashSet<Integer> getReporterIons(ModificationParameters modificationParameters) {

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        return modificationParameters.getAllModifications().stream()
                .flatMap(modName -> modificationFactory.getModification(modName).getReporterIons().stream())
                .map(ReporterIon::getSubType)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * This method returns all the theoretic ions expected from a peptide. /!\
     * this method will work only if the modifications found in the peptide are
     * in the ModificationFactory.
     *
     * @param peptide The considered peptide
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * paramters to use for modifications
     *
     * @return the expected fragment ions
     */
    public HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> getFragmentIons(Peptide peptide, ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters) {
        return getFragmentIons(peptide, null, modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters);
    }

    /**
     * This method returns the theoretic ions expected from a peptide. /!\ this
     * method will work only if the modifications found in the peptide are in
     * the ModificationFactory.
     *
     * @param peptide The considered peptide
     * @param specificAnnotationSettings if provided, only the ions detectable
     * using these settings will be selected
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * paramters to use for modifications
     *
     * @return the expected fragment ions
     */
    public HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> getFragmentIons(Peptide peptide, SpecificAnnotationParameters specificAnnotationSettings, ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters) {

        HashMap<Ion.IonType, HashSet<Integer>> selectedIonTypes = null;

        if (specificAnnotationSettings != null) {

            selectedIonTypes = specificAnnotationSettings.getIonTypes();

        }

        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> result = new HashMap<>();
        String sequence = peptide.getSequence();
        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        HashSet<String> possibleNeutralLosses = null;

        if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {

            possibleNeutralLosses = new HashSet<>(getDefaultNeutralLosses());

        }

        HashSet<String> allModifications = new HashSet<>(1);

        String[] variableModNames = peptide.getIndexedVariableModifications();
        Modification[] variableModifications = new Modification[variableModNames.length];

        String[] fixedModNames = peptide.getFixedModifications(modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters);
        Modification[] fixedModifications = new Modification[fixedModNames.length];

        for (int i = 0; i < variableModNames.length; i++) {

            String modName = variableModNames[i];

            if (modName != null) {

                allModifications.add(modName);
                Modification modification = modificationFactory.getModification(modName);
                variableModifications[i] = modification;

            }

            modName = fixedModNames[i];

            if (modName != null) {

                allModifications.add(modName);
                Modification modification = modificationFactory.getModification(modName);
                fixedModifications[i] = modification;

            }
        }

        for (String modName : allModifications) {

            Modification modification = modificationFactory.getModification(modName);

            if (selectedIonTypes == null || selectedIonTypes.keySet().contains(Ion.IonType.REPORTER_ION)) {

                for (ReporterIon reporterIon : modification.getReporterIons()) {

                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.REPORTER_ION.index);

                    if (ionsMap == null) {

                        ionsMap = new HashMap<>(modification.getReporterIons().size());
                        result.put(Ion.IonType.REPORTER_ION.index, ionsMap);

                    }

                    int subType = reporterIon.getSubType();
                    ArrayList<Ion> ions = ionsMap.get(subType);

                    if (ions == null) {

                        ions = new ArrayList<>(1);
                        ionsMap.put(subType, ions);
                        ions.add(reporterIon);

                    }
                }
            }

            if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {

                for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {

                    possibleNeutralLosses.add(neutralLoss.name);

                }
            }
        }

        // We account for up to two neutral losses per ion maximum
        NeutralLossCombination[] neutralLossesCombinations = null;

        if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {

            neutralLossesCombinations = getNeutralLossesCombinations(possibleNeutralLosses);

        }

        double forwardMass = 0;

        Modification modification = fixedModifications[0];

        if (modification != null) {

            forwardMass += modification.getMass();

        }

        modification = variableModifications[0];

        if (modification != null) {

            forwardMass += modification.getMass();

        }

        double rewindMass = Atom.O.getMonoisotopicMass();

        modification = fixedModifications[sequence.length() + 1];

        if (modification != null) {

            rewindMass += modification.getMass();

        }

        modification = variableModifications[sequence.length() + 1];

        if (modification != null) {

            rewindMass += modification.getMass();

        }

        for (int aa = 0; aa < sequence.length() - 1; aa++) {

            char aaName = sequence.charAt(aa);

            // immonium ions
            if (selectedIonTypes == null || selectedIonTypes.keySet().contains(Ion.IonType.IMMONIUM_ION)) {

                HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.IMMONIUM_ION.index);

                if (ionsMap == null) {

                    ionsMap = new HashMap<>(sequence.length());
                    result.put(Ion.IonType.IMMONIUM_ION.index, ionsMap);

                }

                ImmoniumIon immoniumIon = ImmoniumIon.getImmoniumIon(aaName);
                int subType = immoniumIon.getSubType();
                ArrayList<Ion> ions = ionsMap.get(subType);

                if (ions == null) {

                    ions = new ArrayList<>(1);
                    ions.add(immoniumIon);
                    ionsMap.put(subType, ions);

                }
            }

            // related ions
            if (selectedIonTypes == null || selectedIonTypes.keySet().contains(Ion.IonType.RELATED_ION)) {

                HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.RELATED_ION.index);

                if (ionsMap == null) {

                    ionsMap = new HashMap<>(sequence.length());
                    result.put(Ion.IonType.RELATED_ION.index, ionsMap);

                }

                ArrayList<RelatedIon> relatedIons = RelatedIon.getRelatedIons(AminoAcid.getAminoAcid(aaName));

                if (relatedIons != null) {

                    for (RelatedIon tempRelated : relatedIons) {

                        int subType = tempRelated.getSubType();

                        ArrayList<Ion> ions = ionsMap.get(subType);

                        if (ions == null) {

                            ions = new ArrayList<>(1);

                        }

                        ions.add(tempRelated);
                        ionsMap.put(subType, ions);

                    }
                }
            }

            int faa = aa + 1;
            AminoAcid currentAA = AminoAcid.getAminoAcid(aaName);
            forwardMass += currentAA.getMonoisotopicMass();

            modification = fixedModifications[faa];

            if (modification != null) {

                forwardMass += modification.getMass();

            }

            modification = variableModifications[faa];

            if (modification != null) {

                forwardMass += modification.getMass();

            }

            HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.PEPTIDE_FRAGMENT_ION.index);

            if (ionsMap == null) {

                ionsMap = new HashMap<>(6);
                result.put(Ion.IonType.PEPTIDE_FRAGMENT_ION.index, ionsMap);

            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.A_ION)) {

                // add the a-ions
                int subType = PeptideFragmentIon.A_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);

                if (ions == null) {

                    if (neutralLossesCombinations != null) {

                        ions = new ArrayList<>(neutralLossesCombinations.length);

                    } else {

                        ions = new ArrayList<>(1);

                    }

                    ionsMap.put(subType, ions);

                }

                if (neutralLossesCombinations != null) {

                    for (NeutralLossCombination losses : neutralLossesCombinations) {

                        ions.add(new PeptideFragmentIon(subType, faa, forwardMass - co - losses.getMass(), losses.getNeutralLossCombination()));

                    }

                } else {

                    ions.add(new PeptideFragmentIon(subType, faa, forwardMass - co, null));

                }
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.B_ION)) {

                // add the b-ions
                int subType = PeptideFragmentIon.B_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);

                if (ions == null) {

                    if (neutralLossesCombinations != null) {

                        ions = new ArrayList<>(neutralLossesCombinations.length);

                    } else {

                        ions = new ArrayList<>(1);
                    }

                    ionsMap.put(subType, ions);

                }

                if (neutralLossesCombinations != null) {

                    for (NeutralLossCombination losses : neutralLossesCombinations) {

                        ions.add(new PeptideFragmentIon(subType, faa, forwardMass - losses.getMass(), losses.getNeutralLossCombination()));

                    }
                } else {

                    ions.add(new PeptideFragmentIon(subType, faa, forwardMass, null));

                }
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.C_ION)) {

// add the c-ion
                int subType = PeptideFragmentIon.C_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);

                if (ions == null) {

                    if (neutralLossesCombinations != null) {

                        ions = new ArrayList<>(neutralLossesCombinations.length);

                    } else {

                        ions = new ArrayList<>(1);

                    }

                    ionsMap.put(subType, ions);

                }

                if (neutralLossesCombinations != null) {

                    for (NeutralLossCombination losses : neutralLossesCombinations) {

                        ions.add(new PeptideFragmentIon(subType, faa, forwardMass + nh3 - losses.getMass(), losses.getNeutralLossCombination()));

                    }

                } else {

                    ions.add(new PeptideFragmentIon(subType, faa, forwardMass + nh3, null));

                }
            }

            int raa = sequence.length() - aa - 1;
            currentAA = AminoAcid.getAminoAcid(sequence.charAt(raa));
            rewindMass += currentAA.getMonoisotopicMass();

            modification = fixedModifications[raa + 1];

            if (modification != null) {

                rewindMass += modification.getMass();

            }

            modification = variableModifications[raa + 1];

            if (modification != null) {

                rewindMass += modification.getMass();

            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.X_ION)) {

                // add the x-ion
                int subType = PeptideFragmentIon.X_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);

                if (ions == null) {

                    if (neutralLossesCombinations != null) {

                        ions = new ArrayList<>(neutralLossesCombinations.length);

                    } else {

                        ions = new ArrayList<>(1);

                    }

                    ionsMap.put(subType, ions);

                }

                if (neutralLossesCombinations != null) {

                    for (NeutralLossCombination losses : neutralLossesCombinations) {

                        ions.add(new PeptideFragmentIon(subType, faa, rewindMass + co - losses.getMass(), losses.getNeutralLossCombination()));

                    }
                } else {

                    ions.add(new PeptideFragmentIon(subType, faa, rewindMass + co, null));

                }
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.Y_ION)) {

                // add the y-ions
                int subType = PeptideFragmentIon.Y_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);

                if (ions == null) {

                    if (neutralLossesCombinations != null) {

                        ions = new ArrayList<>(neutralLossesCombinations.length);

                    } else {

                        ions = new ArrayList<>(1);

                    }

                    ionsMap.put(subType, ions);

                }

                if (neutralLossesCombinations != null) {

                    for (NeutralLossCombination losses : neutralLossesCombinations) {

                        ions.add(new PeptideFragmentIon(subType, faa, rewindMass + h2 - losses.getMass(), losses.getNeutralLossCombination()));

                    }

                } else {

                    ions.add(new PeptideFragmentIon(subType, faa, rewindMass + h2, null));

                }
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.Z_ION)) {

                // add the z-ions
                int subType = PeptideFragmentIon.Z_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);

                if (ions == null) {

                    if (neutralLossesCombinations != null) {

                        ions = new ArrayList<>(neutralLossesCombinations.length);

                    } else {

                        ions = new ArrayList<>(1);

                    }

                    ionsMap.put(subType, ions);

                }

                if (neutralLossesCombinations != null) {

                    for (NeutralLossCombination losses : neutralLossesCombinations) {

                        ions.add(new PeptideFragmentIon(subType, faa, rewindMass - Atom.N.getMonoisotopicMass() - losses.getMass(), losses.getNeutralLossCombination()));

                    }

                } else {

                    ions.add(new PeptideFragmentIon(subType, faa, rewindMass - Atom.N.getMonoisotopicMass(), null));

                }
            }
        }

        AminoAcid currentAA = AminoAcid.getAminoAcid(sequence.charAt(sequence.length() - 1));
        forwardMass += currentAA.getMonoisotopicMass();

        modification = fixedModifications[sequence.length()];

        if (modification != null) {

            forwardMass += modification.getMass();

        }

        modification = variableModifications[sequence.length()];

        if (modification != null) {

            forwardMass += modification.getMass();

        }

        modification = fixedModifications[sequence.length() + 1];

        if (modification != null) {

            forwardMass += modification.getMass();

        }

        modification = variableModifications[sequence.length() + 1];

        if (modification != null) {

            forwardMass += modification.getMass();

        }

        if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PRECURSOR_ION)) {

            // add the precursor ion
            HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.PRECURSOR_ION.index);

            if (ionsMap == null) {

                ionsMap = new HashMap<>(1);
                result.put(Ion.IonType.PRECURSOR_ION.index, ionsMap);

            }

            int subType = PrecursorIon.PRECURSOR;
            ArrayList<Ion> ions = ionsMap.get(subType);

            if (ions == null) {

                if (neutralLossesCombinations != null) {

                    ions = new ArrayList<>(neutralLossesCombinations.length);

                } else {

                    ions = new ArrayList<>(1);

                }

                ionsMap.put(subType, ions);

            }

            if (neutralLossesCombinations != null) {

                for (NeutralLossCombination losses : neutralLossesCombinations) {

                    ions.add(new PrecursorIon(forwardMass + h2o - losses.getMass(), losses.getNeutralLossCombination()));

                }

            } else {

                ions.add(new PrecursorIon(forwardMass + ho, null));

            }
        }

        return result;
    }

    /**
     * This method returns the theoretic ions expected from a tag.
     *
     * /!\ this method will work only if the PTMs found in the tag are in the
     * PTMFactory.
     *
     * @param tag the considered tag
     * @return the expected fragment ions
     */
    public HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> getFragmentIons(Tag tag) {

        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> result = new HashMap<>();
        HashSet<String> possibleNeutralLosses = new HashSet<>(getDefaultNeutralLosses());

        // We account for up to two neutral losses per ion maximum
        NeutralLossCombination[] neutralLossesCombinations = getNeutralLossesCombinations(possibleNeutralLosses);

        HashSet<String> allModifications = new HashSet<>(2);

        int ionNumberOffset = 1;
        ArrayList<Double> massOffsets = new ArrayList<>();
        massOffsets.add(0.0);
        for (TagComponent tagComponent : tag.getContent()) {

            if (tagComponent instanceof AminoAcidSequence) {

                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                double sequenceMass = 0;

                ModificationMatch[] modificationMatches = aminoAcidSequence.getModifications();
                ArrayList<Modification>[] indexedModifications = new ArrayList[aminoAcidSequence.length()];

                HashSet<String> newModifications = new HashSet<>(2);

                for (ModificationMatch modificationMatch : modificationMatches) {

                    int site = modificationMatch.getSite();
                    String modificationName = modificationMatch.getModification();

                    if (!allModifications.contains(modificationName)) {
                        newModifications.add(modificationName);
                        allModifications.add(modificationName);
                    }

                    ArrayList<Modification> modsAtSite = indexedModifications[site - 1];

                    if (modsAtSite == null) {

                        modsAtSite = new ArrayList<>(1);
                        indexedModifications[site - 1] = modsAtSite;

                    }

                    Modification modification = ModificationFactory.getInstance().getModification(modificationName);
                    modsAtSite.add(modification);

                }

                for (String modName : newModifications) {

                    Modification modification = ModificationFactory.getInstance().getModification(modName);

                    for (ReporterIon ptmReporterIon : modification.getReporterIons()) {
                        HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                        if (ionsMap == null) {
                            ionsMap = new HashMap<>(1);
                            result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                        }
                        int subType = ptmReporterIon.getSubType();
                        ArrayList<Ion> ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<>(1);
                            ionsMap.put(subType, ions);
                            ions.add(ptmReporterIon);
                        }
                    }
                    for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {
                        possibleNeutralLosses.add(neutralLoss.name);
                    }
                }

                for (int i = 0; i < aminoAcidSequence.length(); i++) {

                    AminoAcid aminoAcid = aminoAcidSequence.getAminoAcidAt(i);

                    // immonium ions
                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.IMMONIUM_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<>(1);
                        result.put(Ion.IonType.IMMONIUM_ION.index, ionsMap);
                    }
                    ImmoniumIon immoniumIon = ImmoniumIon.getImmoniumIon(aminoAcid.getSingleLetterCodeAsChar());
                    int subType = immoniumIon.getSubType();
                    ArrayList<Ion> ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<>(1);
                        ions.add(immoniumIon);
                        ionsMap.put(subType, ions);
                    }

                    // related ions
                    ionsMap = result.get(Ion.IonType.RELATED_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<>(1);
                        result.put(Ion.IonType.RELATED_ION.index, ionsMap);
                    }
                    ArrayList<RelatedIon> relatedIons = RelatedIon.getRelatedIons(aminoAcid);

                    if (relatedIons != null) {
                        for (RelatedIon tempRelated : relatedIons) {
                            subType = tempRelated.getSubType();

                            ions = ionsMap.get(subType);
                            if (ions == null) {
                                ions = new ArrayList<>(1);
                            }

                            ions.add(tempRelated);
                            ionsMap.put(subType, ions);
                        }
                    }

                    double mass = aminoAcid.getMonoisotopicMass();
                    ArrayList<Modification> modificationsAtSite = indexedModifications[i];
                    
                    if (modificationsAtSite != null) {
                    
                        for (Modification modification : modificationsAtSite) {
                            
                            mass += modification.getMass();
                            
                        }
                    }
                    
                    sequenceMass += mass;

                    ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<>();
                        result.put(Ion.IonType.TAG_FRAGMENT_ION.index, ionsMap);
                    }
                    for (double massOffset : massOffsets) {
                        int aa = ionNumberOffset + i;
                        int subaa = i + 1;
                        double forwardMass = massOffset + sequenceMass;

                        // add the a-ions
                        subType = TagFragmentIon.A_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<>();
                            ionsMap.put(subType, ions);
                        }
                        for (NeutralLossCombination losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - co - losses.getMass(), losses.getNeutralLossCombination(), massOffset));
                        }

                        // add the b-ions
                        subType = TagFragmentIon.B_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<>();
                            ionsMap.put(subType, ions);
                        }
                        for (NeutralLossCombination losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - losses.getMass(), losses.getNeutralLossCombination(), massOffset));
                        }

                        // add the c-ion
                        subType = TagFragmentIon.C_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<>();
                            ionsMap.put(subType, ions);
                        }
                        for (NeutralLossCombination losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass + nh3 - losses.getMass(), losses.getNeutralLossCombination(), massOffset));
                        }
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<>();
                for (double offsetMass : massOffsets) {
                    double newMass = offsetMass + sequenceMass;
                    if (!newOffsetMasses.contains(newMass)) {
                        newOffsetMasses.add(newMass);
                    }
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset += aminoAcidSequence.length();
            } else if (tagComponent instanceof MassGap) {
                double gapMass = tagComponent.getMass();
                int aa = ionNumberOffset;
                int subaa = 0;

                HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                if (ionsMap == null) {
                    ionsMap = new HashMap<>();
                    result.put(Ion.IonType.TAG_FRAGMENT_ION.index, ionsMap);
                }

                for (double massOffset : massOffsets) {
                    double forwardMass = massOffset + gapMass;

                    // add the a-ions
                    int subType = TagFragmentIon.A_ION;
                    ArrayList<Ion> ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<>();
                        ionsMap.put(subType, ions);
                    }
                    for (NeutralLossCombination losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - co - losses.getMass(), losses.getNeutralLossCombination(), massOffset));
                    }

                    // add the b-ions
                    subType = TagFragmentIon.B_ION;
                    ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<>();
                        ionsMap.put(subType, ions);
                    }
                    for (NeutralLossCombination losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - losses.getMass(), losses.getNeutralLossCombination(), massOffset));
                    }

                    // add the c-ion
                    subType = TagFragmentIon.C_ION;
                    ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<>();
                        ionsMap.put(subType, ions);
                    }
                    for (NeutralLossCombination losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass + nh3 - losses.getMass(), losses.getNeutralLossCombination(), massOffset));
                    }

                }
                ArrayList<Double> newOffsetMasses = new ArrayList<>();
                for (double offsetMass : massOffsets) {
                    newOffsetMasses.add(offsetMass + gapMass);
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset++;
            } else {
                throw new UnsupportedOperationException("Fragment ion not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }

        ArrayList<TagComponent> reversedTag = new ArrayList<>(tag.getContent());
        Collections.reverse(reversedTag);
        ionNumberOffset = 0;
        massOffsets.clear();
        massOffsets.add(0.0);
        allModifications.clear();
        possibleNeutralLosses.clear();
        for (TagComponent tagComponent : reversedTag) {
            
            if (tagComponent instanceof AminoAcidSequence) {

                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                double sequenceMass = 0;

                ModificationMatch[] modificationMatches = aminoAcidSequence.getModifications();
                ArrayList<Modification>[] indexedModifications = new ArrayList[aminoAcidSequence.length()];

                HashSet<String> newModifications = new HashSet<>(2);

                for (ModificationMatch modificationMatch : modificationMatches) {

                    int site = modificationMatch.getSite();
                    String modificationName = modificationMatch.getModification();

                    if (!allModifications.contains(modificationName)) {
                        newModifications.add(modificationName);
                        allModifications.add(modificationName);
                    }

                    ArrayList<Modification> modsAtSite = indexedModifications[site - 1];

                    if (modsAtSite == null) {

                        modsAtSite = new ArrayList<>(1);
                        indexedModifications[site - 1] = modsAtSite;

                    }

                    Modification modification = ModificationFactory.getInstance().getModification(modificationName);
                    modsAtSite.add(modification);

                }

                for (String modName : newModifications) {

                    Modification modification = ModificationFactory.getInstance().getModification(modName);
                    for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {
                        possibleNeutralLosses.add(neutralLoss.name);
                    }
                }

                for (int i = aminoAcidSequence.length() - 1; i >= 0; i--) {

                    AminoAcid aminoAcid = aminoAcidSequence.getAminoAcidAt(i);

                    // immonium ions
                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.IMMONIUM_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<>();
                        result.put(Ion.IonType.IMMONIUM_ION.index, ionsMap);
                    }
                    ImmoniumIon immoniumIon = ImmoniumIon.getImmoniumIon(aminoAcid.getSingleLetterCodeAsChar());
                    int subType = immoniumIon.getSubType();
                    ArrayList<Ion> ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<>();
                        ions.add(immoniumIon);
                        ionsMap.put(subType, ions);
                    }

                    // related ions
                    ionsMap = result.get(Ion.IonType.RELATED_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<>();
                        result.put(Ion.IonType.RELATED_ION.index, ionsMap);
                    }
                    ArrayList<RelatedIon> relatedIons = RelatedIon.getRelatedIons(aminoAcid);

                    if (relatedIons != null) {
                        for (RelatedIon tempRelated : relatedIons) {
                            subType = tempRelated.getSubType();

                            ions = ionsMap.get(subType);
                            if (ions == null) {
                                ions = new ArrayList<>(1);
                            }

                            ions.add(tempRelated);
                            ionsMap.put(subType, ions);
                        }
                    }

                    double mass = aminoAcid.getMonoisotopicMass();
                    ArrayList<Modification> modificationsAtSite = indexedModifications[i];
                    
                    if (modificationsAtSite != null) {
                    
                        for (Modification modification : modificationsAtSite) {
                            
                            mass += modification.getMass();
                            
                        }
                    }
                    
                    sequenceMass += mass;

                    ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<>();
                        result.put(Ion.IonType.TAG_FRAGMENT_ION.index, ionsMap);
                    }
                    for (double massOffset : massOffsets) {
                        int aa = ionNumberOffset + aminoAcidSequence.length() - i;
                        int subaa = aminoAcidSequence.length() - i;
                        double rewindMass = massOffset + sequenceMass;
                        double gap = 0;
                        if (massOffset != Atom.O.getMonoisotopicMass()) {
                            gap = massOffset;
                        }

                        // add the x-ions
                        subType = TagFragmentIon.X_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<>();
                            ionsMap.put(subType, ions);
                        }
                        for (NeutralLossCombination losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + co2 - losses.getMass(), losses.getNeutralLossCombination(), gap));
                        }

                        // add the y-ions
                        subType = TagFragmentIon.Y_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<>();
                            ionsMap.put(subType, ions);
                        }
                        for (NeutralLossCombination losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + h2o - losses.getMass(), losses.getNeutralLossCombination(), gap));
                        }

                        // add the z-ion
                        subType = TagFragmentIon.Z_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<>();
                            ionsMap.put(subType, ions);
                        }
                        for (NeutralLossCombination losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass - nMinusO - losses.getMass(), losses.getNeutralLossCombination(), gap));
                        }
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<>();
                for (double offsetMass : massOffsets) {
                    double newMass = offsetMass + sequenceMass;
                    if (!newOffsetMasses.contains(newMass)) {
                        newOffsetMasses.add(newMass);
                    }
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset += aminoAcidSequence.length();
            } else if (tagComponent instanceof MassGap) {
                double gapMass = tagComponent.getMass();
                int aa = ionNumberOffset;
                int subaa = 0;

                HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                if (ionsMap == null) {
                    ionsMap = new HashMap<>();
                    result.put(Ion.IonType.TAG_FRAGMENT_ION.index, ionsMap);
                }
                for (double massOffset : massOffsets) {
                    double gap = gapMass;
                    if (massOffset != Atom.O.getMonoisotopicMass()) {
                        gap += massOffset;
                    }
                    double rewindMass = massOffset + gapMass;

                    // add the x-ions
                    int subType = TagFragmentIon.X_ION;
                    ArrayList<Ion> ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<>();
                        ionsMap.put(subType, ions);
                    }
                    for (NeutralLossCombination losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + co2 - losses.getMass(), losses.getNeutralLossCombination(), gap));
                    }

                    // add the y-ions
                    subType = TagFragmentIon.Y_ION;
                    ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<>();
                        ionsMap.put(subType, ions);
                    }
                    for (NeutralLossCombination losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + h2o - losses.getMass(), losses.getNeutralLossCombination(), gap));
                    }

                    // add the z-ion
                    subType = TagFragmentIon.Z_ION;
                    ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<>();
                        ionsMap.put(subType, ions);
                    }
                    for (NeutralLossCombination losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass - nMinusO - losses.getMass(), losses.getNeutralLossCombination(), gap));
                    }

                }
                ArrayList<Double> newOffsetMasses = new ArrayList<>();
                for (double offsetMass : massOffsets) {
                    newOffsetMasses.add(offsetMass + gapMass);
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset++;
            } else {
                throw new UnsupportedOperationException("Fragment ion not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }

        // add the precursor ion
        HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.PRECURSOR_ION.index);
        if (ionsMap == null) {
            ionsMap = new HashMap<>(1);
            result.put(Ion.IonType.PRECURSOR_ION.index, ionsMap);
        }
        int subType = PrecursorIon.PRECURSOR;
        ArrayList<Ion> ions = ionsMap.get(subType);
        if (ions == null) {
            ions = new ArrayList<>(neutralLossesCombinations.length);
            ionsMap.put(subType, ions);
        }
        for (NeutralLossCombination losses : neutralLossesCombinations) {
            ions.add(new PrecursorIon(tag.getMass() - losses.getMass(), losses.getNeutralLossCombination()));
        }

        return result;
    }

    /**
     * Returns the possible neutral losses combinations as array of arrays of
     * neutral losses.
     *
     * @param possibleNeutralLosses the possible neutral losses to include
     *
     * @return the possible neutral losses combinations
     */
    public NeutralLossCombination[] getNeutralLossesCombinations(HashSet<String> possibleNeutralLosses) {

        long lossesKey = getNeutralLossesKey(possibleNeutralLosses);
        NeutralLossCombination[] neutralLossesCombinations = neutralLossesCombinationsCache.get(lossesKey);

        if (neutralLossesCombinations == null) {

            ArrayList<NeutralLoss[]> neutralLossesCombinationsLists = estimateNeutralLossesCombinations(possibleNeutralLosses);
            neutralLossesCombinations = new NeutralLossCombination[neutralLossesCombinationsLists.size()];
            for (int i = 0; i < neutralLossesCombinationsLists.size(); i++) {
                NeutralLoss[] combination = neutralLossesCombinationsLists.get(i);
                NeutralLossCombination combinationObject = new NeutralLossCombination(combination);
                neutralLossesCombinations[i] = combinationObject;
            }
            neutralLossesCombinationsCache.put(lossesKey, neutralLossesCombinations);
        }
        return neutralLossesCombinations;
    }

    /**
     * Convenience method returning the possible neutral losses combination as
     * accounted by the factory, i.e., for now up to two neutral losses per
     * peak.
     *
     * @param possibleNeutralLosses the possible neutral losses
     *
     * @return the possible combinations
     */
    private ArrayList<NeutralLoss[]> estimateNeutralLossesCombinations(HashSet<String> possibleNeutralLosses) {

        String[] lossesNames = possibleNeutralLosses.toArray(new String[possibleNeutralLosses.size()]);

        // We will account for up to two neutral losses per ion maximum
        ArrayList<NeutralLoss[]> neutralLossesCombinations = new ArrayList<>();
        NeutralLoss[] tempList = new NeutralLoss[0];
        neutralLossesCombinations.add(tempList);

        for (int i = 0; i < lossesNames.length; i++) {

            String name1 = lossesNames[i];
            NeutralLoss neutralLoss1 = NeutralLoss.getNeutralLoss(name1);
            tempList = new NeutralLoss[1];
            tempList[0] = neutralLoss1;
            neutralLossesCombinations.add(tempList);

            for (int j = i + 1; j < lossesNames.length; j++) {

                String name2 = lossesNames[j];
                tempList = new NeutralLoss[2];
                tempList[0] = neutralLoss1;
                tempList[1] = NeutralLoss.getNeutralLoss(name2);
                neutralLossesCombinations.add(tempList);

            }
        }

        return neutralLossesCombinations;
    }

    /**
     * Returns the neutral losses combination cache key corresponding to a set
     * of neutral losses.
     *
     * @param possibleNeutralLosses the possible neutral losses
     *
     * @return the corresponding cache key
     */
    private long getNeutralLossesKey(HashSet<String> possibleNeutralLosses) {
        return ExperimentObject.asLong(possibleNeutralLosses.stream()
                .collect(Collectors.joining()));
    }

    /**
     * Convenience summing the masses of various neutral losses.
     *
     * @param neutralLosses list of neutral losses
     *
     * @return the sum of the masses
     */
    public static double getLossesMass(NeutralLoss[] neutralLosses) {

        return Arrays.stream(neutralLosses)
                .mapToDouble(NeutralLoss::getMass)
                .sum();
    }
}
