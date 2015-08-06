package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.ImmoniumIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PrecursorIon;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.biology.ions.TagFragmentIon;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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
     * from the modifications found.
     */
    private static ArrayList<NeutralLoss> defaultNeutralLosses = new ArrayList<NeutralLoss>();

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
     * Adds a default neutral loss to the default neutral losses if the
     * corresponding loss was not here already.
     *
     * @param newNeutralLoss the new neutral loss
     */
    public void addDefaultNeutralLoss(NeutralLoss newNeutralLoss) {
        boolean found = false;
        for (NeutralLoss neutralLoss : defaultNeutralLosses) {
            if (newNeutralLoss.isSameAs(neutralLoss)) {
                found = true;
                break;
            }
        }
        if (!found) {
            defaultNeutralLosses.add(newNeutralLoss);
        }
    }

    /**
     * Returns the default neutral losses.
     *
     * @return the default neutral losses
     */
    public ArrayList<NeutralLoss> getDefaultNeutralLosses() {
        return defaultNeutralLosses;
    }

    /**
     * This method returns all the theoretic ions expected from a peptide. /!\
     * this method will work only if the PMTs found in the peptide are in the
     * PTMFactory.
     *
     * @param peptide The considered peptide
     *
     * @return the expected fragment ions
     */
    public HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> getFragmentIons(Peptide peptide) {
        return getFragmentIons(peptide, null);
    }

    /**
     * This method returns the theoretic ions expected from a peptide. /!\ this
     * method will work only if the PMTs found in the peptide are in the
     * PTMFactory.
     *
     * @param peptide The considered peptide
     * @param specificAnnotationSettings if provided, only the ions detectable
     * using these settings will be selected
     *
     * @return the expected fragment ions
     */
    public HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> getFragmentIons(Peptide peptide, SpecificAnnotationSettings specificAnnotationSettings) {

        HashMap<Ion.IonType, HashSet<Integer>> selectedIonTypes = null;
        if (specificAnnotationSettings != null) {
            selectedIonTypes = specificAnnotationSettings.getIonTypes();
        }

        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> result = new HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>();
        String sequence = peptide.getSequence();
        HashMap<Integer, ArrayList<PTM>> modifications = new HashMap<Integer, ArrayList<PTM>>(peptide.getNModifications());
        PTMFactory ptmFactory = PTMFactory.getInstance();
        ArrayList<String> processedPtms = null;
        ArrayList<NeutralLoss> possibleNeutralLosses = null;
        if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {
            possibleNeutralLosses = new ArrayList<NeutralLoss>(defaultNeutralLosses);
        }

        if (peptide.isModified()) {
            for (ModificationMatch ptmMatch : peptide.getModificationMatches()) {
                int location = ptmMatch.getModificationSite();
                String ptmName = ptmMatch.getTheoreticPtm();
                PTM ptm = ptmFactory.getPTM(ptmName);
                if (ptm == null) {
                    throw new IllegalArgumentException("PTM " + ptmName + " not loaded in the PTM factory.");
                }
                ArrayList<PTM> modificationsAtSite = modifications.get(location);
                if (modificationsAtSite == null) {
                    modificationsAtSite = new ArrayList<PTM>(1);
                    modifications.put(location, modificationsAtSite);
                }
                modificationsAtSite.add(ptm);
                if (processedPtms == null || !processedPtms.contains(ptmName)) {
                    if (selectedIonTypes == null || selectedIonTypes.keySet().contains(Ion.IonType.REPORTER_ION)) {
                        for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                            HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                            if (ionsMap == null) {
                                ionsMap = new HashMap<Integer, ArrayList<Ion>>(ptm.getReporterIons().size());
                                result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                            }
                            int subType = ptmReporterIon.getSubType();
                            ArrayList<Ion> ions = ionsMap.get(subType);
                            if (ions == null) {
                                ions = new ArrayList<Ion>(1);
                                ionsMap.put(subType, ions);
                                ions.add(ptmReporterIon);
                            }
                        }
                    }
                    if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {
                        for (NeutralLoss ptmNeutralLoss : ptm.getNeutralLosses()) {
                            boolean found = false;
                            for (NeutralLoss neutralLoss : possibleNeutralLosses) {
                                // @TODO: we keep only different neutral losses. We might want to change that when people 
                                //       are working with modifications having reproducible motifs like ubiquitin or some glycons.
                                if (ptmNeutralLoss.isSameAs(neutralLoss)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                possibleNeutralLosses.add(ptmNeutralLoss);
                            }
                        }
                    }
                    if (processedPtms == null) {
                        processedPtms = new ArrayList<String>(peptide.getNModifications());
                    }
                    processedPtms.add(ptmName);
                }
            }
        }

        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = null;
        if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {
            neutralLossesCombinations = getAccountedNeutralLosses(possibleNeutralLosses);
        }

        double forwardMass = 0;
        double rewindMass = Atom.O.getMonoisotopicMass();

        for (int aa = 0; aa < sequence.length() - 1; aa++) {

            char aaName = sequence.charAt(aa);

            if (selectedIonTypes == null || selectedIonTypes.keySet().contains(Ion.IonType.IMMONIUM_ION)) {
                HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.IMMONIUM_ION.index);
                if (ionsMap == null) {
                    ionsMap = new HashMap<Integer, ArrayList<Ion>>(sequence.length());
                    result.put(Ion.IonType.IMMONIUM_ION.index, ionsMap);
                }
                ImmoniumIon immoniumIon = new ImmoniumIon(aaName);
                int subType = immoniumIon.getSubType();
                ArrayList<Ion> ions = ionsMap.get(subType);
                if (ions == null) {
                    ions = new ArrayList<Ion>(1);
                    ionsMap.put(subType, ions);
                    ions.add(immoniumIon);
                }
            }

            int faa = aa + 1;
            AminoAcid currentAA = AminoAcid.getAminoAcid(aaName);
            forwardMass += currentAA.getMonoisotopicMass();

            if (modifications.get(faa) != null) {
                for (PTM ptm : modifications.get(faa)) {
                    forwardMass += ptm.getMass();
                }
            }

            HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.PEPTIDE_FRAGMENT_ION.index);
            if (ionsMap == null) {
                ionsMap = new HashMap<Integer, ArrayList<Ion>>(6);
                result.put(Ion.IonType.PEPTIDE_FRAGMENT_ION.index, ionsMap);
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.A_ION)) {
                // add the a-ions
                int subType = PeptideFragmentIon.A_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);
                if (ions == null) {
                    if (neutralLossesCombinations != null) {
                        ions = new ArrayList<Ion>(neutralLossesCombinations.size());
                    } else {
                        ions = new ArrayList<Ion>(1);
                    }
                    ionsMap.put(subType, ions);
                }
                if (neutralLossesCombinations != null) {
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new PeptideFragmentIon(subType, faa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses));
                    }
                } else {
                    ions.add(new PeptideFragmentIon(subType, faa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass(), null));
                }
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.B_ION)) {
                // add the b-ions
                int subType = PeptideFragmentIon.B_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);
                if (ions == null) {
                    if (neutralLossesCombinations != null) {
                        ions = new ArrayList<Ion>(neutralLossesCombinations.size());
                    } else {
                        ions = new ArrayList<Ion>(1);
                    }
                    ionsMap.put(subType, ions);
                }
                if (neutralLossesCombinations != null) {
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new PeptideFragmentIon(subType, faa, forwardMass - getLossesMass(losses), losses));
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
                        ions = new ArrayList<Ion>(neutralLossesCombinations.size());
                    } else {
                        ions = new ArrayList<Ion>(1);
                    }
                    ionsMap.put(subType, ions);
                }
                if (neutralLossesCombinations != null) {
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new PeptideFragmentIon(subType, faa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses));
                    }
                } else {
                    ions.add(new PeptideFragmentIon(subType, faa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass(), null));
                }
            }

            int raa = sequence.length() - aa - 1;
            currentAA = AminoAcid.getAminoAcid(sequence.charAt(raa));
            rewindMass += currentAA.getMonoisotopicMass();

            if (modifications.get(raa + 1) != null) {
                for (PTM ptm : modifications.get(raa + 1)) {
                    rewindMass += ptm.getMass();
                }
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.X_ION)) {
                // add the x-ion
                int subType = PeptideFragmentIon.X_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);
                if (ions == null) {
                    if (neutralLossesCombinations != null) {
                        ions = new ArrayList<Ion>(neutralLossesCombinations.size());
                    } else {
                        ions = new ArrayList<Ion>(1);
                    }
                    ionsMap.put(subType, ions);
                }
                if (neutralLossesCombinations != null) {
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new PeptideFragmentIon(subType, faa, rewindMass + Atom.C.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses));
                    }
                } else {
                    ions.add(new PeptideFragmentIon(subType, faa, rewindMass + Atom.C.getMonoisotopicMass() + Atom.O.getMonoisotopicMass(), null));
                }
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.Y_ION)) {
                // add the y-ions
                int subType = PeptideFragmentIon.Y_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);
                if (ions == null) {
                    if (neutralLossesCombinations != null) {
                        ions = new ArrayList<Ion>(neutralLossesCombinations.size());
                    } else {
                        ions = new ArrayList<Ion>(1);
                    }
                    ionsMap.put(subType, ions);
                }
                if (neutralLossesCombinations != null) {
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new PeptideFragmentIon(subType, faa, rewindMass + 2 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses));
                    }
                } else {
                    ions.add(new PeptideFragmentIon(subType, faa, rewindMass + 2 * Atom.H.getMonoisotopicMass(), null));
                }
            }

            if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PEPTIDE_FRAGMENT_ION) && specificAnnotationSettings.getFragmentIonTypes().contains(PeptideFragmentIon.Z_ION)) {
                // add the z-ions
                int subType = PeptideFragmentIon.Z_ION;
                ArrayList<Ion> ions = ionsMap.get(subType);
                if (ions == null) {
                    if (neutralLossesCombinations != null) {
                        ions = new ArrayList<Ion>(neutralLossesCombinations.size());
                    } else {
                        ions = new ArrayList<Ion>(1);
                    }
                    ionsMap.put(subType, ions);
                }
                if (neutralLossesCombinations != null) {
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new PeptideFragmentIon(subType, faa, rewindMass - Atom.N.getMonoisotopicMass() - getLossesMass(losses), losses));
                    }
                } else {
                    ions.add(new PeptideFragmentIon(subType, faa, rewindMass - Atom.N.getMonoisotopicMass(), null));
                }
            }
        }

        AminoAcid currentAA = AminoAcid.getAminoAcid(sequence.charAt(sequence.length() - 1));
        forwardMass += currentAA.getMonoisotopicMass();

        if (modifications.get(sequence.length()) != null) {
            for (PTM ptm : modifications.get(sequence.length())) {
                forwardMass += ptm.getMass();
            }
        }

        if (specificAnnotationSettings == null || selectedIonTypes.keySet().contains(Ion.IonType.PRECURSOR_ION)) {
            // add the precursor ion
            HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.PRECURSOR_ION.index);
            if (ionsMap == null) {
                ionsMap = new HashMap<Integer, ArrayList<Ion>>(1);
                result.put(Ion.IonType.PRECURSOR_ION.index, ionsMap);
            }
            int subType = PrecursorIon.PRECURSOR;
            ArrayList<Ion> ions = ionsMap.get(subType);
            if (ions == null) {
                if (neutralLossesCombinations != null) {
                    ions = new ArrayList<Ion>(neutralLossesCombinations.size());
                } else {
                    ions = new ArrayList<Ion>(1);
                }
                ionsMap.put(subType, ions);
            }
            if (neutralLossesCombinations != null) {
                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                    ions.add(new PrecursorIon(forwardMass + (2 * Atom.H.getMonoisotopicMass()) + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses));
                }
            } else {
                ions.add(new PrecursorIon(forwardMass + Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass(), null));
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
     * @param tag The considered tag
     * @return the expected fragment ions
     */
    public HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> getFragmentIons(Tag tag) {

        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> result = new HashMap<Integer, HashMap<Integer, ArrayList<Ion>>>();
        ArrayList<NeutralLoss> possibleNeutralLosses = new ArrayList<NeutralLoss>(defaultNeutralLosses);
        ArrayList<String> processedPtms = null;

        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = getAccountedNeutralLosses(possibleNeutralLosses);

        int ionNumberOffset = 1;
        ArrayList<Double> massOffsets = new ArrayList<Double>();
        massOffsets.add(0.0);
        for (TagComponent tagComponent : tag.getContent()) {
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                ArrayList<Double> patternMasses = new ArrayList<Double>();
                for (int i = 0; i < aminoAcidPattern.length(); i++) {
                    ArrayList<Double> aminoAcidMasses = new ArrayList<Double>();
                    for (Character aa : aminoAcidPattern.getTargetedAA(i)) {
                        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                        double mass = aminoAcid.getMonoisotopicMass();
                        for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(i + 1)) {
                            String ptmName = modificationMatch.getTheoreticPtm();
                            PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                            if (processedPtms == null || !processedPtms.contains(ptmName)) {
                                for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                                    if (ionsMap == null) {
                                        ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                                        result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                                    }
                                    int subType = ptmReporterIon.getSubType();
                                    ArrayList<Ion> ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<Ion>();
                                        ionsMap.put(subType, ions);
                                        ions.add(ptmReporterIon);
                                    }
                                }
                                for (NeutralLoss ptmNeutralLoss : ptm.getNeutralLosses()) {
                                    boolean found = false;
                                    for (NeutralLoss neutralLoss : possibleNeutralLosses) {
                                        // @TODO: we keep only different neutral losses. We might want to change that when people 
                                        //       are working with modifications having reproducible motifs like ubiquitin or some glycons.
                                        if (ptmNeutralLoss.isSameAs(neutralLoss)) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        possibleNeutralLosses.add(ptmNeutralLoss);
                                    }
                                }
                                if (processedPtms == null) {
                                    processedPtms = new ArrayList<String>();
                                }
                                processedPtms.add(ptmName);
                            }
                            mass += ptm.getMass();
                        }
                        if (!aminoAcidMasses.contains(mass)) {
                            aminoAcidMasses.add(mass);
                        }

                        HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.IMMONIUM_ION.index);
                        if (ionsMap == null) {
                            ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                            result.put(Ion.IonType.IMMONIUM_ION.index, ionsMap);
                        }
                        ImmoniumIon immoniumIon = new ImmoniumIon(aminoAcid.getSingleLetterCodeAsChar());
                        int subType = immoniumIon.getSubType();
                        ArrayList<Ion> ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<Ion>();
                            ionsMap.put(subType, ions);
                            ions.add(immoniumIon);
                        }
                    }
                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                        result.put(Ion.IonType.TAG_FRAGMENT_ION.index, ionsMap);
                    }
                    for (double massOffset : massOffsets) {
                        ArrayList<Double> newPatternMassess = new ArrayList<Double>();
                        if (patternMasses.isEmpty()) {
                            for (double mass : aminoAcidMasses) {
                                int aa = ionNumberOffset + i;
                                int subaa = i + 1;
                                double forwardMass = massOffset + mass;

                                // add the a-ions
                                int subType = TagFragmentIon.A_ION;
                                ArrayList<Ion> ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<Ion>();
                                    ionsMap.put(subType, ions);
                                }
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                                }

                                // add the b-ions
                                subType = TagFragmentIon.B_ION;
                                ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<Ion>();
                                    ionsMap.put(subType, ions);
                                }
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - getLossesMass(losses), losses, massOffset));
                                }

                                // add the c-ion
                                subType = TagFragmentIon.B_ION;
                                ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<Ion>();
                                    ionsMap.put(subType, ions);
                                }
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                                }

                                if (!newPatternMassess.contains(mass)) {
                                    newPatternMassess.add(mass);
                                }
                            }
                        } else {
                            for (double patternMass : patternMasses) {
                                for (double mass : aminoAcidMasses) {
                                    int aa = ionNumberOffset + i;
                                    int subaa = i + 1;
                                    double patternFragmentMass = patternMass + mass;
                                    double forwardMass = massOffset + patternFragmentMass;

                                    // add the a-ions
                                    int subType = TagFragmentIon.A_ION;
                                    ArrayList<Ion> ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<Ion>();
                                        ionsMap.put(subType, ions);
                                    }
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                                    }

                                    // add the b-ions
                                    subType = TagFragmentIon.B_ION;
                                    ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<Ion>();
                                        ionsMap.put(subType, ions);
                                    }
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - getLossesMass(losses), losses, massOffset));
                                    }

                                    // add the c-ion
                                    subType = TagFragmentIon.C_ION;
                                    ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<Ion>();
                                        ionsMap.put(subType, ions);
                                    }
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                                    }

                                    if (!newPatternMassess.contains(patternFragmentMass)) {
                                        newPatternMassess.add(patternFragmentMass);
                                    }
                                }
                            }
                        }
                        patternMasses = newPatternMassess;
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    for (double mass : patternMasses) {
                        double newMass = offsetMass + mass;
                        if (!newOffsetMasses.contains(newMass)) {
                            newOffsetMasses.add(newMass);
                        }
                    }
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset += aminoAcidPattern.length();
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                double sequenceMass = 0;
                for (int i = 0; i < aminoAcidSequence.length(); i++) {
                    AminoAcid aminoAcid = aminoAcidSequence.getAminoAcidAt(i);

                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.IMMONIUM_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                        result.put(Ion.IonType.IMMONIUM_ION.index, ionsMap);
                    }
                    ImmoniumIon immoniumIon = new ImmoniumIon(aminoAcid.getSingleLetterCodeAsChar());
                    int subType = immoniumIon.getSubType();
                    ArrayList<Ion> ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<Ion>();
                        ionsMap.put(subType, ions);
                        ions.add(immoniumIon);
                    }

                    double mass = aminoAcid.getMonoisotopicMass();
                    for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(i + 1)) {
                        String ptmName = modificationMatch.getTheoreticPtm();
                        PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                        if (processedPtms == null || !processedPtms.contains(ptmName)) {
                            for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                                ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                                if (ionsMap == null) {
                                    ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                                    result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                                }
                                subType = ptmReporterIon.getSubType();
                                ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<Ion>();
                                    ionsMap.put(subType, ions);
                                    ions.add(ptmReporterIon);
                                }
                            }
                            for (NeutralLoss ptmNeutralLoss : ptm.getNeutralLosses()) {
                                boolean found = false;
                                for (NeutralLoss neutralLoss : possibleNeutralLosses) {
                                    // @TODO: we keep only different neutral losses. We might want to change that when people 
                                    //       are working with modifications having reproducible motifs like ubiquitin or some glycons.
                                    if (ptmNeutralLoss.isSameAs(neutralLoss)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    possibleNeutralLosses.add(ptmNeutralLoss);
                                }
                            }
                            if (processedPtms == null) {
                                processedPtms = new ArrayList<String>();
                            }
                            processedPtms.add(ptmName);
                        }
                        mass += ptm.getMass();
                    }
                    sequenceMass += mass;

                    ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<Integer, ArrayList<Ion>>();
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
                            ions = new ArrayList<Ion>();
                            ionsMap.put(subType, ions);
                        }
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                        }

                        // add the b-ions
                        subType = TagFragmentIon.B_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<Ion>();
                            ionsMap.put(subType, ions);
                        }
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - getLossesMass(losses), losses, massOffset));
                        }

                        // add the c-ion
                        subType = TagFragmentIon.C_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<Ion>();
                            ionsMap.put(subType, ions);
                        }
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                        }
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
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
                    ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                    result.put(Ion.IonType.TAG_FRAGMENT_ION.index, ionsMap);
                }

                for (double massOffset : massOffsets) {
                    double forwardMass = massOffset + gapMass;

                    // add the a-ions
                    int subType = TagFragmentIon.A_ION;
                    ArrayList<Ion> ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<Ion>();
                        ionsMap.put(subType, ions);
                    }
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                    }

                    // add the b-ions
                    subType = TagFragmentIon.B_ION;
                    ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<Ion>();
                        ionsMap.put(subType, ions);
                    }
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass - getLossesMass(losses), losses, massOffset));
                    }

                    // add the c-ion
                    subType = TagFragmentIon.C_ION;
                    ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<Ion>();
                        ionsMap.put(subType, ions);
                    }
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                    }

                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    newOffsetMasses.add(offsetMass + gapMass);
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset++;
            } else {
                throw new UnsupportedOperationException("Fragment ion not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }

        ArrayList<TagComponent> reversedTag = new ArrayList<TagComponent>(tag.getContent());
        Collections.reverse(reversedTag);
        ionNumberOffset = 0;
        massOffsets.clear();
        massOffsets.add(0.0);
        for (TagComponent tagComponent : reversedTag) {
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                ArrayList<Double> patternMasses = new ArrayList<Double>();
                for (int i = aminoAcidPattern.length() - 1; i >= 0; i--) {
                    ArrayList<Double> aminoAcidMasses = new ArrayList<Double>();
                    for (Character aa : aminoAcidPattern.getTargetedAA(i)) {
                        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                        double mass = aminoAcid.getMonoisotopicMass();
                        for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(i + 1)) {
                            String ptmName = modificationMatch.getTheoreticPtm();
                            PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                            if (processedPtms == null || !processedPtms.contains(ptmName)) {
                                for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                                    if (ionsMap == null) {
                                        ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                                        result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                                    }
                                    int subType = ptmReporterIon.getSubType();
                                    ArrayList<Ion> ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<Ion>();
                                        ionsMap.put(subType, ions);
                                        ions.add(ptmReporterIon);
                                    }
                                }
                                for (NeutralLoss ptmNeutralLoss : ptm.getNeutralLosses()) {
                                    boolean found = false;
                                    for (NeutralLoss neutralLoss : possibleNeutralLosses) {
                                        // @TODO: we keep only different neutral losses. We might want to change that when people 
                                        //       are working with modifications having reproducible motifs like ubiquitin or some glycons.
                                        if (ptmNeutralLoss.isSameAs(neutralLoss)) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        possibleNeutralLosses.add(ptmNeutralLoss);
                                    }
                                }
                                if (processedPtms == null) {
                                    processedPtms = new ArrayList<String>();
                                }
                                processedPtms.add(ptmName);
                            }
                            mass += ptm.getMass();
                        }
                        if (!aminoAcidMasses.contains(mass)) {
                            aminoAcidMasses.add(mass);
                        }

                        HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.IMMONIUM_ION.index);
                        if (ionsMap == null) {
                            ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                            result.put(Ion.IonType.IMMONIUM_ION.index, ionsMap);
                        }
                        ImmoniumIon immoniumIon = new ImmoniumIon(aminoAcid.getSingleLetterCodeAsChar());
                        int subType = immoniumIon.getSubType();
                        ArrayList<Ion> ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<Ion>();
                            ionsMap.put(subType, ions);
                            ions.add(immoniumIon);
                        }
                    }
                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                        result.put(Ion.IonType.TAG_FRAGMENT_ION.index, ionsMap);
                    }
                    for (double massOffset : massOffsets) {
                        ArrayList<Double> newPatternMassess = new ArrayList<Double>();
                        if (patternMasses.isEmpty()) {
                            for (double mass : aminoAcidMasses) {
                                int aa = ionNumberOffset + aminoAcidPattern.length() - i;
                                int subaa = aminoAcidPattern.length() - i;
                                double rewindMass = massOffset + mass;
                                double gap = 0;
                                if (massOffset != Atom.O.getMonoisotopicMass()) {
                                    gap = massOffset;
                                }

                                // add the x-ions
                                int subType = TagFragmentIon.X_ION;
                                ArrayList<Ion> ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<Ion>();
                                    ionsMap.put(subType, ions);
                                }
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                }

                                // add the y-ions
                                subType = TagFragmentIon.Y_ION;
                                ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<Ion>();
                                    ionsMap.put(subType, ions);
                                }
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                }

                                // add the z-ion
                                subType = TagFragmentIon.Z_ION;
                                ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<Ion>();
                                    ionsMap.put(subType, ions);
                                }
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass - Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                }

                                if (!newPatternMassess.contains(mass)) {
                                    newPatternMassess.add(mass);
                                }
                            }
                        } else {
                            for (double patternMass : patternMasses) {
                                for (double mass : aminoAcidMasses) {
                                    int aa = ionNumberOffset + aminoAcidPattern.length() - i;
                                    int subaa = aminoAcidPattern.length() - i;
                                    double patternFragmentMass = patternMass + mass;
                                    double rewindMass = massOffset + patternFragmentMass;
                                    double gap = 0;
                                    if (massOffset != Atom.O.getMonoisotopicMass()) {
                                        gap = massOffset;
                                    }

                                    // add the x-ions
                                    int subType = TagFragmentIon.X_ION;
                                    ArrayList<Ion> ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<Ion>();
                                        ionsMap.put(subType, ions);
                                    }
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                    }

                                    // add the y-ions
                                    subType = TagFragmentIon.Y_ION;
                                    ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<Ion>();
                                        ionsMap.put(subType, ions);
                                    }
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                    }

                                    // add the z-ion
                                    subType = TagFragmentIon.Z_ION;
                                    ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<Ion>();
                                        ionsMap.put(subType, ions);
                                    }
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass - Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                    }

                                    if (!newPatternMassess.contains(patternFragmentMass)) {
                                        newPatternMassess.add(patternFragmentMass);
                                    }
                                }
                            }
                        }
                        patternMasses = newPatternMassess;
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    for (double mass : patternMasses) {
                        double newMass = offsetMass + mass;
                        if (!newOffsetMasses.contains(newMass)) {
                            newOffsetMasses.add(newMass);
                        }
                    }
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset += aminoAcidPattern.length();
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                double sequenceMass = 0;
                for (int i = aminoAcidSequence.length() - 1; i >= 0; i--) {
                    AminoAcid aminoAcid = aminoAcidSequence.getAminoAcidAt(i);
                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.IMMONIUM_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                        result.put(Ion.IonType.IMMONIUM_ION.index, ionsMap);
                    }
                    ImmoniumIon immoniumIon = new ImmoniumIon(aminoAcid.getSingleLetterCodeAsChar());
                    int subType = immoniumIon.getSubType();
                    ArrayList<Ion> ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<Ion>();
                        ionsMap.put(subType, ions);
                        ions.add(immoniumIon);
                    }
                    double mass = aminoAcid.getMonoisotopicMass();
                    for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(i + 1)) {
                        String ptmName = modificationMatch.getTheoreticPtm();
                        PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                        if (processedPtms == null || !processedPtms.contains(ptmName)) {
                            for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                                ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                                if (ionsMap == null) {
                                    ionsMap = new HashMap<Integer, ArrayList<Ion>>();
                                    result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                                }
                                subType = ptmReporterIon.getSubType();
                                ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<Ion>();
                                    ionsMap.put(subType, ions);
                                    ions.add(ptmReporterIon);
                                }
                            }
                            for (NeutralLoss ptmNeutralLoss : ptm.getNeutralLosses()) {
                                boolean found = false;
                                for (NeutralLoss neutralLoss : possibleNeutralLosses) {
                                    // @TODO: we keep only different neutral losses. We might want to change that when people 
                                    //       are working with modifications having reproducible motifs like ubiquitin or some glycons.
                                    if (ptmNeutralLoss.isSameAs(neutralLoss)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    possibleNeutralLosses.add(ptmNeutralLoss);
                                }
                            }
                            if (processedPtms == null) {
                                processedPtms = new ArrayList<String>();
                            }
                            processedPtms.add(ptmName);
                        }
                        mass += ptm.getMass();
                    }
                    sequenceMass += mass;

                    ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<Integer, ArrayList<Ion>>();
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
                            ions = new ArrayList<Ion>();
                            ionsMap.put(subType, ions);
                        }
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                        }

                        // add the y-ions
                        subType = TagFragmentIon.Y_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<Ion>();
                            ionsMap.put(subType, ions);
                        }
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                        }

                        // add the z-ion
                        subType = TagFragmentIon.Z_ION;
                        ions = ionsMap.get(subType);
                        if (ions == null) {
                            ions = new ArrayList<Ion>();
                            ionsMap.put(subType, ions);
                        }
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass - Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                        }
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
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
                    ionsMap = new HashMap<Integer, ArrayList<Ion>>();
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
                        ions = new ArrayList<Ion>();
                        ionsMap.put(subType, ions);
                    }
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                    }

                    // add the y-ions
                    subType = TagFragmentIon.Y_ION;
                    ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<Ion>();
                        ionsMap.put(subType, ions);
                    }
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass + 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                    }

                    // add the z-ion
                    subType = TagFragmentIon.Z_ION;
                    ions = ionsMap.get(subType);
                    if (ions == null) {
                        ions = new ArrayList<Ion>();
                        ionsMap.put(subType, ions);
                    }
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        ions.add(new TagFragmentIon(subType, aa, subaa, rewindMass - Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                    }

                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
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
            ionsMap = new HashMap<Integer, ArrayList<Ion>>(1);
            result.put(Ion.IonType.PRECURSOR_ION.index, ionsMap);
        }
        int subType = PrecursorIon.PRECURSOR;
        ArrayList<Ion> ions = ionsMap.get(subType);
        if (ions == null) {
            ions = new ArrayList<Ion>(neutralLossesCombinations.size());
            ionsMap.put(subType, ions);
        }
        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
            ions.add(new PrecursorIon(tag.getMass() - getLossesMass(losses), losses));
        }

        return result;
    }

    /**
     * Convenience method returning the possible neutral losses combination as
     * accounted by the factory, i.e., for now up to two neutral losses per
     * peak.
     *
     * @param possibleNeutralLosses the possible neutral losses
     * @return the possible combinations
     */
    public static ArrayList<ArrayList<NeutralLoss>> getAccountedNeutralLosses(ArrayList<NeutralLoss> possibleNeutralLosses) {

        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = new ArrayList<ArrayList<NeutralLoss>>();
        ArrayList<NeutralLoss> tempList = new ArrayList<NeutralLoss>(0);
        neutralLossesCombinations.add(tempList);

        for (NeutralLoss neutralLoss1 : possibleNeutralLosses) {
            boolean found = false;
            for (ArrayList<NeutralLoss> accountedCombination : neutralLossesCombinations) {
                if (accountedCombination.size() == 1 && accountedCombination.get(0).isSameAs(neutralLoss1)) {
                    found = true;
                }
            }
            if (!found) {
                tempList = new ArrayList<NeutralLoss>(1);
                tempList.add(neutralLoss1);
                neutralLossesCombinations.add(tempList);
            }
            for (NeutralLoss neutralLoss2 : possibleNeutralLosses) {
                if (!neutralLoss1.isSameAs(neutralLoss2)) {
                    found = false;
                    for (ArrayList<NeutralLoss> accountedCombination : neutralLossesCombinations) {
                        if (accountedCombination.size() == 2) {
                            if (accountedCombination.get(0).isSameAs(neutralLoss1) && accountedCombination.get(1).isSameAs(neutralLoss2)) {
                                found = true;
                                break;
                            }
                            if (accountedCombination.get(0).isSameAs(neutralLoss2) && accountedCombination.get(1).isSameAs(neutralLoss1)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        tempList = new ArrayList<NeutralLoss>(2);
                        tempList.add(neutralLoss1);
                        tempList.add(neutralLoss2);
                        neutralLossesCombinations.add(tempList);
                    }
                }
            }
        }

        return neutralLossesCombinations;
    }

    /**
     * Convenience summing the masses of various neutral losses.
     *
     * @param neutralLosses list of neutral losses
     * @return the summ of the masses
     */
    public static double getLossesMass(ArrayList<NeutralLoss> neutralLosses) {
        double result = 0;
        for (NeutralLoss neutralLoss : neutralLosses) {
            result += neutralLoss.getMass();
        }
        return result;
    }
}
