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
     * of the modifications found.
     */
    private static ArrayList<NeutralLoss> defaultNeutralLosses = null;
    /**
     * Cache for the possible combinations of neutral losses.
     */
    private final HashMap<String, NeutralLossCombination[]> neutralLossesCombinationsCache = new HashMap<>();
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
    public static ArrayList<NeutralLoss> getDefaultNeutralLosses() {
        if (defaultNeutralLosses == null) {
            setDefaultNeutralLosses();
        }
        return defaultNeutralLosses;
    }

    /**
     * Sets the default neutral losses.
     */
    private static synchronized void setDefaultNeutralLosses() {
        if (defaultNeutralLosses == null) {
            defaultNeutralLosses = new ArrayList<>(2);
            defaultNeutralLosses.add(NeutralLoss.H2O);
            defaultNeutralLosses.add(NeutralLoss.NH3);
        }
    }

    /**
     * Returns a list containing the default neutral losses and the losses found
     * in the given modifications. Note: modifications must be loaded in the PTM
     * factory.
     *
     * @param ptmSettings the PTM settings
     *
     * @return the neutral losses expected in the dataset
     */
    public static ArrayList<NeutralLoss> getNeutralLosses(ModificationParameters ptmSettings) {
        ArrayList<NeutralLoss> neutralLosses = new ArrayList<>();
        neutralLosses.addAll(IonFactory.getDefaultNeutralLosses());
        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        for (String modification : ptmSettings.getAllModifications()) {
            Modification currentPtm = ptmFactory.getModification(modification);
            boolean found = false;
            for (NeutralLoss ptmNeutralLoss : currentPtm.getNeutralLosses()) {
                for (NeutralLoss neutralLoss : neutralLosses) {
                    if (ptmNeutralLoss.isSameAs(neutralLoss)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    neutralLosses.add(ptmNeutralLoss);
                }
            }
        }
        return neutralLosses;
    }

    /**
     * Returns the reporter ions to annotate with the given PTM settings.
     *
     * @param ptmSettings the PTMs to annotate
     *
     * @return a hashset of the subtype indexes of the reporter ions to annotate
     */
    public static HashSet<Integer> getReporterIons(ModificationParameters ptmSettings) {

        HashSet<Integer> reporterIons = new HashSet<>();
        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        for (String modification : ptmSettings.getAllModifications()) {
            Modification currentPtm = ptmFactory.getModification(modification);
            for (ReporterIon reporterIon : currentPtm.getReporterIons()) {
                reporterIons.add(reporterIon.getSubType());
            }
        }
        return reporterIons;
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
    public HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> getFragmentIons(Peptide peptide, SpecificAnnotationParameters specificAnnotationSettings) {

        HashMap<Ion.IonType, HashSet<Integer>> selectedIonTypes = null;
        
        if (specificAnnotationSettings != null) {
        
            selectedIonTypes = specificAnnotationSettings.getIonTypes();
        
        }

        HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> result = new HashMap<>();
        String sequence = peptide.getSequence();
        HashMap<Integer, ArrayList<Modification>> modifications = new HashMap<>(peptide.getNModifications());
        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        ArrayList<String> processedMods = null;
        ArrayList<NeutralLoss> possibleNeutralLosses = null;
        
        if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {
            
            possibleNeutralLosses = new ArrayList<>(getDefaultNeutralLosses());
        
        }
        
        ModificationMatch[] modMatches = peptide.getModificationMatches();

        if (modMatches != null) {
            
            for (ModificationMatch modMatch : modMatches) {
                
                int location = modMatch.getModificationSite();
                String modName = modMatch.getModification();
                Modification modification = ptmFactory.getModification(modName);
                
                ArrayList<Modification> modificationsAtSite = modifications.get(location);
                
                if (modificationsAtSite == null) {
                
                    modificationsAtSite = new ArrayList<>(1);
                    modifications.put(location, modificationsAtSite);
                
                }
                
                modificationsAtSite.add(modification);
                
                if (processedMods == null || !processedMods.contains(modName)) {
                
                    if (selectedIonTypes == null || selectedIonTypes.keySet().contains(Ion.IonType.REPORTER_ION)) {
                        
                        for (ReporterIon ptmReporterIon : modification.getReporterIons()) {
                        
                            HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                            
                            if (ionsMap == null) {
                            
                                ionsMap = new HashMap<>(modification.getReporterIons().size());
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
                    }
                    
                    if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {
                    
                        for (NeutralLoss ptmNeutralLoss : modification.getNeutralLosses()) {
                        
                            boolean found = false;
                            
                            for (NeutralLoss neutralLoss : possibleNeutralLosses) {
                            
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
                    
                    if (processedMods == null) {
                    
                        processedMods = new ArrayList<>(peptide.getNModifications());
                    
                    }
                    
                    processedMods.add(modName);
                
                }
            }
        }

        // We account for up to two neutral losses per ion maximum
        NeutralLossCombination[] neutralLossesCombinations = null;

        if (specificAnnotationSettings == null || !specificAnnotationSettings.getNeutralLossesMap().isEmpty()) {

            neutralLossesCombinations = getNeutralLossesCombinations(possibleNeutralLosses);

        }

        double forwardMass = 0;
        double rewindMass = Atom.O.getMonoisotopicMass();

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
                        
                            ions = new ArrayList<Ion>(1);
                        
                        }

                        ions.add(tempRelated);
                        ionsMap.put(subType, ions);
                    
                    }
                }
            }

            int faa = aa + 1;
            AminoAcid currentAA = AminoAcid.getAminoAcid(aaName);
            forwardMass += currentAA.getMonoisotopicMass();

            if (modifications.get(faa) != null) {
                
                for (Modification modification : modifications.get(faa)) {
                
                    forwardMass += modification.getMass();
                
                }
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

            if (modifications.get(raa + 1) != null) {

                for (Modification modification : modifications.get(raa + 1)) {
   
                    rewindMass += modification.getMass();
                
                }
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

        if (modifications.get(sequence.length()) != null) {

            for (Modification modification : modifications.get(sequence.length())) {

                forwardMass += modification.getMass();

            }
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
        ArrayList<NeutralLoss> possibleNeutralLosses = new ArrayList<>(getDefaultNeutralLosses());
        ArrayList<String> processedModifications = null;

        // We account for up to two neutral losses per ion maximum
        NeutralLossCombination[] neutralLossesCombinations = getNeutralLossesCombinations(possibleNeutralLosses);

        int ionNumberOffset = 1;
        ArrayList<Double> massOffsets = new ArrayList<>();
        massOffsets.add(0.0);
        for (TagComponent tagComponent : tag.getContent()) {

            if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                double sequenceMass = 0;
                for (int i = 0; i < aminoAcidSequence.length(); i++) {
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
                                ions = new ArrayList<Ion>(1);
                            }

                            ions.add(tempRelated);
                            ionsMap.put(subType, ions);
                        }
                    }

                    double mass = aminoAcid.getMonoisotopicMass();
                    for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(i + 1)) {
                        String ptmName = modificationMatch.getModification();
                        Modification ptm = ModificationFactory.getInstance().getModification(ptmName);
                        if (processedModifications == null || !processedModifications.contains(ptmName)) {
                            for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                                ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                                if (ionsMap == null) {
                                    ionsMap = new HashMap<>();
                                    result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                                }
                                subType = ptmReporterIon.getSubType();
                                ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<>();
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
                            if (processedModifications == null) {
                                processedModifications = new ArrayList<>();
                            }
                            processedModifications.add(ptmName);
                        }
                        mass += ptm.getMass();
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
        for (TagComponent tagComponent : reversedTag) {
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                ArrayList<Double> patternMasses = new ArrayList<>();
                for (int i = aminoAcidPattern.length() - 1; i >= 0; i--) {
                    ArrayList<Double> aminoAcidMasses = new ArrayList<>();
                    for (Character aa : aminoAcidPattern.getTargetedAA(i)) {
                        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                        double mass = aminoAcid.getMonoisotopicMass();
                        for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(i + 1)) {
                            String ptmName = modificationMatch.getModification();
                            Modification ptm = ModificationFactory.getInstance().getModification(ptmName);
                            if (processedModifications == null || !processedModifications.contains(ptmName)) {
                                for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                                    if (ionsMap == null) {
                                        ionsMap = new HashMap<>();
                                        result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                                    }
                                    int subType = ptmReporterIon.getSubType();
                                    ArrayList<Ion> ions = ionsMap.get(subType);
                                    if (ions == null) {
                                        ions = new ArrayList<>();
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
                                if (processedModifications == null) {
                                    processedModifications = new ArrayList<>();
                                }
                                processedModifications.add(ptmName);
                            }
                            mass += ptm.getMass();
                        }
                        if (!aminoAcidMasses.contains(mass)) {
                            aminoAcidMasses.add(mass);
                        }

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
                                    ions = new ArrayList<Ion>(1);
                                }

                                ions.add(tempRelated);
                                ionsMap.put(subType, ions);
                            }
                        }
                    }

                    HashMap<Integer, ArrayList<Ion>> ionsMap = result.get(Ion.IonType.TAG_FRAGMENT_ION.index);
                    if (ionsMap == null) {
                        ionsMap = new HashMap<>();
                        result.put(Ion.IonType.TAG_FRAGMENT_ION.index, ionsMap);
                    }

                    for (double massOffset : massOffsets) {
                        ArrayList<Double> newPatternMassess = new ArrayList<>();
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

                                    if (!newPatternMassess.contains(patternFragmentMass)) {
                                        newPatternMassess.add(patternFragmentMass);
                                    }
                                }
                            }
                        }
                        patternMasses = newPatternMassess;
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<>();
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
                                ions = new ArrayList<Ion>(1);
                            }

                            ions.add(tempRelated);
                            ionsMap.put(subType, ions);
                        }
                    }

                    double mass = aminoAcid.getMonoisotopicMass();
                    for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(i + 1)) {
                        String ptmName = modificationMatch.getModification();
                        Modification ptm = ModificationFactory.getInstance().getModification(ptmName);
                        if (processedModifications == null || !processedModifications.contains(ptmName)) {
                            for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                                ionsMap = result.get(Ion.IonType.REPORTER_ION.index);
                                if (ionsMap == null) {
                                    ionsMap = new HashMap<>();
                                    result.put(Ion.IonType.REPORTER_ION.index, ionsMap);
                                }
                                subType = ptmReporterIon.getSubType();
                                ions = ionsMap.get(subType);
                                if (ions == null) {
                                    ions = new ArrayList<>();
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
                            if (processedModifications == null) {
                                processedModifications = new ArrayList<>();
                            }
                            processedModifications.add(ptmName);
                        }
                        mass += ptm.getMass();
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
    public NeutralLossCombination[] getNeutralLossesCombinations(ArrayList<NeutralLoss> possibleNeutralLosses) {
        String lossesKey = getNeutralLossesKey(possibleNeutralLosses);
        NeutralLossCombination[] neutralLossesCombinations = neutralLossesCombinationsCache.get(lossesKey);
        if (neutralLossesCombinations == null) {
            ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinationsLists = estimateNeutralLossesCombinations(possibleNeutralLosses);
            neutralLossesCombinations = new NeutralLossCombination[neutralLossesCombinationsLists.size()];
            for (int i = 0; i < neutralLossesCombinationsLists.size(); i++) {
                ArrayList<NeutralLoss> combination = neutralLossesCombinationsLists.get(i);
                NeutralLoss[] combinationAsArray = new NeutralLoss[combination.size()];
                combinationAsArray = combination.toArray(combinationAsArray);
                NeutralLossCombination combinationObject = new NeutralLossCombination(combinationAsArray);
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
     * @return the possible combinations
     */
    private ArrayList<ArrayList<NeutralLoss>> estimateNeutralLossesCombinations(ArrayList<NeutralLoss> possibleNeutralLosses) {

        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = new ArrayList<>();
        ArrayList<NeutralLoss> tempList = new ArrayList<>(0);
        neutralLossesCombinations.add(tempList);

        for (NeutralLoss neutralLoss1 : possibleNeutralLosses) {
            boolean found = false;
            for (ArrayList<NeutralLoss> accountedCombination : neutralLossesCombinations) {
                if (accountedCombination.size() == 1 && accountedCombination.get(0).isSameAs(neutralLoss1)) {
                    found = true;
                }
            }
            if (!found) {
                tempList = new ArrayList<>(1);
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
                        tempList = new ArrayList<>(2);
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
     * Returns the neutral losses combination cache key corresponding to a set
     * of neutral losses.
     *
     * @param possibleNeutralLosses the possible neutral losses
     *
     * @return the corresponding cache key
     */
    private String getNeutralLossesKey(ArrayList<NeutralLoss> possibleNeutralLosses) {
        StringBuilder stringBuilder = new StringBuilder(possibleNeutralLosses.size() * 3);
        for (NeutralLoss neutralLoss : possibleNeutralLosses) {
            stringBuilder.append(neutralLoss.name);
        }
        return stringBuilder.toString();
    }

    /**
     * Convenience summing the masses of various neutral losses.
     *
     * @param neutralLosses list of neutral losses
     * @return the sum of the masses
     */
    public static double getLossesMass(ArrayList<NeutralLoss> neutralLosses) {
        double result = 0;
        for (NeutralLoss neutralLoss : neutralLosses) {
            result += neutralLoss.getMass();
        }
        return result;
    }
}
