package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.matches.PeptideVariantMatches;
import com.compomics.util.experiment.massspectrometry.utils.StandardMasses;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents a peptide. Note that maps and lists provided in
 * constructors are used in the class and not cloned.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 */
public class Peptide extends ExperimentObject {

    /**
     * The version UID for serialization/deserialization compatibility.
     */
    static final long serialVersionUID = 5632064601627536034L;
    /**
     * The peptide sequence.
     */
    private String sequence;
    /**
     * The peptide key.
     */
    private String key;
    /**
     * The peptide matching key.
     */
    private String matchingKey;
    /**
     * The peptide mass.
     */
    private double mass = -1;
    /**
     * The mapping of this peptide on proteins as a map, accession to position.
     * Position on protein sequences is 0 based.
     */
    private HashMap<String, HashSet<Integer>> proteinMapping = null;
    /**
     * The modifications carried by the peptide.
     */
    private ArrayList<ModificationMatch> modificationMatches = null;
    /**
     * The variants observed when mapping this peptide to the database. Peptide
     * variant matches are indexed by protein and by peptide start.
     */
    private HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMatches = null;
    /**
     * Separator preceding confident localization of the confident localization
     * of a modification.
     */
    public final static String MODIFICATION_LOCALIZATION_SEPARATOR = "-ATAA-";
    /**
     * Separator used to separate modifications in peptide keys as string.
     */
    public final static String MODIFICATION_SEPARATOR = "_";

    /**
     * Constructor for the peptide.
     */
    public Peptide() {
    }

    /**
     * Constructor.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modificationMatches the Modification of this peptide
     * @param sanityCheck boolean indicating whether the input should be checked
     */
    public Peptide(String aSequence, ArrayList<ModificationMatch> modificationMatches, boolean sanityCheck) {

        this.sequence = aSequence;
        this.modificationMatches = modificationMatches;

        if (sanityCheck) {
            sanityCheck();
        }
    }

    /**
     * Constructor.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modificationMatches the Modification of this peptide
     * @param sanityCheck boolean indicating whether the input should be checked
     * @param mass the mass of the peptide
     */
    public Peptide(String aSequence, ArrayList<ModificationMatch> modificationMatches, boolean sanityCheck, double mass) {

        this.sequence = aSequence;
        this.modificationMatches = modificationMatches;

        if (sanityCheck) {
            sanityCheck();
        }

        this.mass = mass;
    }

    /**
     * Constructor. No sanity check is performed on the input.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modifications the Modification of this peptide
     */
    public Peptide(String aSequence, ArrayList<ModificationMatch> modifications) {
        this(aSequence, modifications, false);
    }

    /**
     * Constructor for the peptide.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modificationMatches the modifications of this peptide
     * @param variantMatches the variants compared to the database
     * @param sanityCheck boolean indicating whether the input should be checked
     */
    public Peptide(String aSequence, ArrayList<ModificationMatch> modificationMatches, HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMatches, boolean sanityCheck) {

        this.sequence = aSequence;
        this.modificationMatches = modificationMatches;
        this.variantMatches = variantMatches;

        if (sanityCheck) {
            sanityCheck();
        }
    }

    /**
     * Removes characters from the sequence and checks the modifications names
     * for forbidden characters.
     */
    private void sanityCheck() {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        sequence = sequence.replaceAll("[#*$%&]", "");

        if (modificationMatches != null) {

            HashSet<String> conflictingPtms = modificationMatches.stream().map(modificationMatch -> modificationMatch.getModification())
                    .filter((modificationName) -> (modificationName.contains(MODIFICATION_SEPARATOR) || modificationName.contains(MODIFICATION_LOCALIZATION_SEPARATOR)))
                    .collect(Collectors.toCollection(HashSet::new));

            if (!conflictingPtms.isEmpty()) {

                String conflictingPtmsString = conflictingPtms.stream().collect(Collectors.joining(", "));
                throw new IllegalArgumentException("Modification names containing '" + MODIFICATION_SEPARATOR + "' or '" + MODIFICATION_LOCALIZATION_SEPARATOR + "' are not supported. Conflicting name(s): " + conflictingPtmsString);

            }
        }
    }

    /**
     * Sets the mass.
     *
     * @param mass the mass
     */
    public void setMass(double mass) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.mass = mass;
    }

    /**
     * Sets the object key.
     *
     * @param key the object key
     */
    public void setKey(String key) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.key = key;
    }

    /**
     * Returns the proteins mapping as a map of 0 based indexes for every protein accession.
     *
     * @return the proteins mapping
     */
    public HashMap<String, HashSet<Integer>> getProteinMapping() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return proteinMapping;
    }

    /**
     * Sets the proteins mapping as a map of 0 based indexes for every protein accession.
     * 
     * @param proteinMapping the proteins mapping
     */
    public void setProteinMapping(HashMap<String, HashSet<Integer>> proteinMapping) {
        this.proteinMapping = proteinMapping;
    }

    /**
     * Sets the sequence variant matches of this peptide.
     *
     * @param variantMatches the variant matches of this peptide
     */
    public void setVariantMatches(HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMatches) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.variantMatches = variantMatches;
    }

    /**
     * Returns the sequence variant matches of this peptide.
     *
     * @return the sequence variant matches of this peptide
     */
    public HashMap<String, HashMap<Integer, PeptideVariantMatches>> getVariantMatches() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return variantMatches;
    }

    /**
     * Getter for the mass.
     *
     * @return the peptide mass
     */
    public double getMass() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (mass == -1) {

            estimateTheoreticMass();

        }

        return mass;
    }

    /**
     * Getter for the modifications carried by this peptide.
     *
     * @return the modifications matches as found by the search engine
     */
    public ArrayList<ModificationMatch> getModificationMatches() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return modificationMatches;
    }

    /**
     * Sets new modification matches for the peptide.
     *
     * @param modificationMatches the new modification matches
     */
    public void setModificationMatches(ArrayList<ModificationMatch> modificationMatches) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.modificationMatches = modificationMatches;

        setMass(-1);

        setKey(null);
        setMatchingKey(null);
    }

    /**
     * Clears the list of imported modification matches.
     */
    public void clearModificationMatches() {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        modificationMatches.clear();

        setMass(-1);
        setKey(null);
        setMatchingKey(null);
    }

    /**
     * Adds a modification match.
     *
     * @param modificationMatch the modification match to add
     */
    public void addModificationMatch(ModificationMatch modificationMatch) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        if (modificationMatches == null) {
            modificationMatches = new ArrayList<>(1);
        }
        modificationMatches.add(modificationMatch);

        setMass(-1);
        setKey(null);
        setMatchingKey(null);
    }

    /**
     * Clears the list of imported variant matches.
     */
    public void clearVariantMatches() {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        if (variantMatches != null) {
            variantMatches.clear();
        }
    }

    /**
     * Returns the 0 based end index of the peptide on the protein sequence.
     *
     * @param proteinAccession the protein accession
     * @param peptideStart the peptide start index
     *
     * @return the 0 based end index of the peptide on the protein sequence
     */
    public int getPeptideEnd(String proteinAccession, int peptideStart) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        int peptideEnd = peptideStart + sequence.length();

        if (variantMatches != null) {

            HashMap<Integer, PeptideVariantMatches> proteinVariants = variantMatches.get(proteinAccession);

            if (proteinVariants != null) {

                PeptideVariantMatches peptideVariantMatches = proteinVariants.get(peptideStart);

                if (peptideVariantMatches != null) {

                    peptideEnd += peptideVariantMatches.getLengthDiff();
                }
            }
        }

        return peptideEnd;
    }

    /**
     * Returns for the sequence.
     *
     * @return the peptide sequence
     */
    public String getSequence() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return sequence;
    }

    /**
     * Sets for the sequence.
     *
     * @param sequence the peptide sequence
     */
    public void setSequence(String sequence) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.sequence = sequence;
    }

    /**
     * Returns the number of missed cleavages using the specified enzyme.
     *
     * @param enzyme the enzyme used
     *
     * @return the amount of missed cleavages
     */
    public int getNMissedCleavages(Enzyme enzyme) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return enzyme.getNmissedCleavages(sequence);
    }

    /**
     * Returns the number of missed cleavages using the digestion preferences. 0
     * if no cleavage set. If multiple enzymes were used, the minimum across the
     * different enzymes.
     *
     * @param digestionPreferences the digestion preferences
     *
     * @return the amount of missed cleavages
     */
    public int getNMissedCleavages(DigestionPreferences digestionPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return digestionPreferences.getCleavagePreference() == DigestionPreferences.CleavagePreference.enzyme
                ? digestionPreferences.getEnzymes().stream().mapToInt(enzyme -> getNMissedCleavages(enzyme)).min().orElse(0) : 0;
    }

    /**
     * Returns the key accounting for sequence matching preferences
     *
     * @return the key accounting for sequence matching preferences
     */
    public String getMatchingKey() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return matchingKey;
    }

    /**
     * Sets the key accounting for sequence matching preferences.
     *
     * @param matchingKey the key accounting for sequence matching preferences
     */
    public void setMatchingKey(String matchingKey) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.matchingKey = matchingKey;
    }

    /**
     * Returns a unique key for the peptide when considering the given matching
     * preferences. When ambiguity the first amino acid according to
     * AminoAcid.getAminoAcidsList() will be selected. For example the matching
     * key of peptide PEPTLDE_mod1_mod2 is PEPTIDE_mod1_mod2
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a key unique to the given matching type
     */
    public String getMatchingKey(SequenceMatchingPreferences sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (matchingKey == null) {

            String matchingSequence = AminoAcid.getMatchingSequence(sequence, sequenceMatchingPreferences);
            setMatchingKey(getKey(matchingSequence, modificationMatches));

        }

        return matchingKey;
    }

    /**
     * Resets the internal cache of the keys.
     */
    public void resetKeysCaches() {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        setMatchingKey(null);
        setKey(null);
    }

    /**
     * Returns the reference key of a peptide. index =
     * SEQUENCE_modMass1_modMass2 with modMass1 and modMass2 modification masses
     * ordered alphabetically.
     *
     * Note: the key is not unique for indistinguishable sequences, see
     * getMatchingKey(SequenceMatchingPreferences sequenceMatchingPreferences).
     * Modifications must be loaded in the Modification factory.
     *
     * @return the key of the peptide
     */
    public String getKey() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (key == null) {

            setKey(getKey(getSequence(), getModificationMatches()));

        }

        return key;
    }

    /**
     * Returns the reference key of a peptide. key = SEQUENCE_mod1_mod2 modMass1
     * and modMass2 modification masses ordered alphabetically.
     *
     * @param sequence the sequence of the peptide
     * @param modificationMatches list of modification matches
     *
     * @return the key of the peptide
     */
    public static String getKey(String sequence, ArrayList<ModificationMatch> modificationMatches) {

        if (modificationMatches == null) {

            return sequence;

        }

        int size = sequence.length();
        ArrayList<String> tempModifications = new ArrayList<>(modificationMatches.size());

        for (ModificationMatch mod : modificationMatches) {

            if (mod.getVariable()) {

                String modificationName = mod.getModification();

                if (modificationName != null) {

                    Modification modification = ModificationFactory.getInstance().getModification(modificationName);

                    if (mod.getConfident() || mod.getInferred()) {

                        StringBuilder tempModKey = new StringBuilder();
                        tempModKey.append(modification.getAmbiguityKey()).append(MODIFICATION_LOCALIZATION_SEPARATOR).append(mod.getModificationSite());
                        tempModifications.add(tempModKey.toString());
                        size += tempModKey.length();

                    } else {

                        String massAsString = modification.getAmbiguityKey();
                        tempModifications.add(massAsString);
                        size += massAsString.length();

                    }
                } else {

                    tempModifications.add("unknown-modification");

                }
            }
        }

        StringBuilder result = new StringBuilder(size);
        result.append(sequence)
                .append(MODIFICATION_SEPARATOR)
                .append(tempModifications.stream().sorted().collect(Collectors.joining(MODIFICATION_SEPARATOR)));

        return result.toString();
    }

    /**
     * Indicates whether a peptide carries modifications.
     *
     * @return a boolean indicating whether a peptide carries modifications
     */
    public boolean isModified() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return modificationMatches != null && !modificationMatches.isEmpty();
    }

    /**
     * Returns the number of variable modifications found with the given mass.
     *
     * @param modificationMass the mass of the modification
     * @return the number of occurrences of this modification
     */
    public int getNVariableModifications(double modificationMass) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return modificationMatches == null ? 0 : (int) modificationMatches.stream()
                .filter(modificationMatch -> modificationMatch.getVariable())
                .map(modificationMatch -> ModificationFactory.getInstance().getModification(modificationMatch.getModification()))
                .filter(modification -> modification.getMass() == modificationMass).count();
    }

    /**
     * Returns the number of modifications carried by this peptide.
     *
     * @return the number of modifications carried by this peptide
     */
    public int getNModifications() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return modificationMatches != null ? modificationMatches.size() : 0;
    }

    /**
     * Returns the potential modification sites as an ordered list of sites. No
     * amino acid combination is tested. 1 is the first amino acid. An empty
     * list is returned if no possibility was found. No peptide to protein
     * mapping is done. The index on the protein must be provided with 0 as
     * first amino acid.
     *
     * @param modification the Modification considered
     * @param proteinSequence the protein sequence
     * @param peptideStart the index of the peptide start on the protein
     *
     * @return a list of potential modification sites
     */
    public ArrayList<Integer> getPotentialModificationSitesNoCombination(Modification modification, String proteinSequence, int peptideStart) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        ArrayList<Integer> possibleSites = new ArrayList<>(1);

        switch (modification.getModificationType()) {

            case modaa:

                AminoAcidPattern aminoAcidPattern = modification.getPattern();
                HashSet<Character> targetedAA = aminoAcidPattern.getAminoAcidsAtTargetSet();

                if (aminoAcidPattern.length() == 1) {

                    for (int i = 0; i < sequence.length(); i++) {

                        char aa = sequence.charAt(i);

                        if (targetedAA.contains(aa)) {

                            possibleSites.add(i + 1);

                        }
                    }

                } else {

                    for (int i = 0; i < sequence.length(); i++) {

                        Character aa = sequence.charAt(i);

                        if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, peptideStart + i)) {

                            possibleSites.add(i + 1);

                        }
                    }
                }

                return possibleSites;

            case modc_protein:

                int peptideEnd = getPeptideEnd(proteinSequence, peptideStart);

                if (peptideEnd == proteinSequence.length() - 1) {

                    possibleSites.add(peptideEnd + 1);

                }
                return possibleSites;

            case modc_peptide:

                possibleSites.add(sequence.length());

                return possibleSites;

            case modn_protein:

                if (peptideStart == 0) {

                    possibleSites.add(1);

                }
                return possibleSites;

            case modn_peptide:

                possibleSites.add(1);

                return possibleSites;

            case modcaa_protein:

                aminoAcidPattern = modification.getPattern();
                targetedAA = aminoAcidPattern.getAminoAcidsAtTargetSet();
                peptideEnd = getPeptideEnd(proteinSequence, peptideStart);

                if (peptideEnd == proteinSequence.length() - 1) {

                    Character aa = sequence.charAt(sequence.length() - 1);

                    if (aminoAcidPattern.length() == 1) {
                        if (targetedAA.contains(aa)) {

                            possibleSites.add(sequence.length());

                        }
                    } else if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, peptideEnd)) {

                        possibleSites.add(sequence.length());

                    }
                }
                return possibleSites;

            case modcaa_peptide:

                aminoAcidPattern = modification.getPattern();
                targetedAA = aminoAcidPattern.getAminoAcidsAtTargetSet();
                Character aa = sequence.charAt(sequence.length() - 1);

                if (aminoAcidPattern.length() == 1) {
                    if (targetedAA.contains(aa)) {

                        possibleSites.add(sequence.length());

                    }
                } else {

                    peptideEnd = getPeptideEnd(proteinSequence, peptideStart);

                    if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, peptideEnd)) {

                        possibleSites.add(sequence.length());

                    }
                }
                return possibleSites;

            case modnaa_protein:

                aminoAcidPattern = modification.getPattern();
                targetedAA = aminoAcidPattern.getAminoAcidsAtTargetSet();

                if (peptideStart == 0) {

                    aa = sequence.charAt(0);
                    if (aminoAcidPattern.length() == 1) {
                        if (targetedAA.contains(aa)) {

                            possibleSites.add(1);

                        }
                    } else if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {

                        possibleSites.add(1);

                    }
                }
                return possibleSites;

            case modnaa_peptide:

                aminoAcidPattern = modification.getPattern();
                targetedAA = aminoAcidPattern.getAminoAcidsAtTargetSet();
                aa = sequence.charAt(0);

                if (aminoAcidPattern.length() == 1) {
                    if (targetedAA.contains(aa)) {

                        possibleSites.add(1);

                    }
                } else if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {

                    possibleSites.add(1);

                }
                return possibleSites;

            default:
                throw new UnsupportedOperationException("Modification site not implemented for modification of type " + modification.getModificationType() + ".");
        }
    }

    /**
     * Returns the potential modification sites as an ordered list of sites. 1
     * is the first amino acid. An empty list is returned if no modification
     * site was found.
     *
     * @param modification the Modification considered
     * @param proteinSequence the protein sequence
     * @param peptideStart the index of the peptide start on the protein
     * @param modificationSequenceMatchingPreferences the sequence matching preferences
     * for Modification to peptide mapping
     *
     * @return a list of potential modification sites
     */
    public ArrayList<Integer> getPotentialModificationSites(Modification modification, String proteinSequence, int peptideStart, SequenceMatchingPreferences modificationSequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        ArrayList<Integer> possibleSites = new ArrayList<>(1);

        switch (modification.getModificationType()) {

            case modaa:

                AminoAcidPattern pattern = modification.getPattern();
                int patternLength = pattern.length();
                int target = pattern.getTarget();

                if (target >= 0 && patternLength - target <= 1) {

                    return pattern.getIndexes(sequence, modificationSequenceMatchingPreferences);

                } else {
                    
                    int peptideEnd = getPeptideEnd(proteinSequence, peptideStart);
                    int missingLeft = Math.min(-pattern.getMinIndex(), peptideStart);
                    int missingRight = Math.min(pattern.getMaxIndex(), proteinSequence.length() - peptideEnd);
                    StringBuilder tempSequence = new StringBuilder(missingLeft + sequence.length() + missingRight);
                    
                    if (missingLeft > 0) {
                        
                        String complement = proteinSequence.substring(peptideStart - missingLeft, peptideStart);
                        tempSequence.append(complement);
                        
                    }
                    
                    tempSequence.append(sequence);
                    
                    if (missingRight > 0) {
                        
                        String complement = proteinSequence.substring(peptideEnd + 1, peptideEnd + missingRight + 1);
                        tempSequence.append(complement);
                        
                    }
                    
                    for (int tempIndex : pattern.getIndexes(tempSequence.toString(), modificationSequenceMatchingPreferences)) {
                        
                        int sequenceIndex = tempIndex - missingLeft;
                        possibleSites.add(sequenceIndex);
                        
                    }
                }
                return possibleSites;
                
            case modc_protein:
                
                if (!isCterm(proteinSequence, peptideStart)) {
                    
                    return possibleSites;
                    
                }
                
            case modc_peptide:
                
                possibleSites.add(sequence.length());
                return possibleSites;
                
            case modn_protein:
                
                if (!isNterm(proteinSequence, peptideStart)) {
                    
                    return possibleSites;
                    
                }
                
            case modn_peptide:
                
                possibleSites.add(1);
                return possibleSites;
                
            case modcaa_protein:
                
                if (!isCterm(proteinSequence, peptideStart)) {
                    
                    return possibleSites;
                    
                }
                
            case modcaa_peptide:
                
                pattern = modification.getPattern();

                // See if we have the correct amino acid at terminus
                if (!pattern.getAminoAcidsAtTargetSet().contains(sequence.charAt(sequence.length() - 1))) {
                    
                    return possibleSites;
                    
                }

                // If we have a multiple amino acid pattern see if we match on the peptide terminus
                if (pattern.length() > 1) {
                    
                    int peptideEnd = getPeptideEnd(proteinSequence, peptideStart);
                    int missingLeft = Math.min(Math.max(-pattern.getMinIndex() - sequence.length(), 0), peptideStart);
                    int missingRight = Math.min(pattern.getMaxIndex(), proteinSequence.length() - peptideEnd);
                    StringBuilder tempSequence = new StringBuilder(missingLeft + sequence.length() + missingRight);
                    
                    if (missingLeft > 0) {
                        
                        String complement = proteinSequence.substring(peptideStart - missingLeft, peptideStart);
                        tempSequence.append(complement);
                        
                    }
                    
                    tempSequence.append(sequence);
                    
                    if (missingRight > 0) {
                        
                        String complement = proteinSequence.substring(peptideEnd + 1, peptideEnd + missingRight + 1);
                        tempSequence.append(complement);
                        
                    }
                    
                    if (!pattern.matchesAt(tempSequence.toString(), modificationSequenceMatchingPreferences, sequence.length() - 1 + missingLeft)) {
                        
                        return possibleSites;
                        
                    }
                }

                possibleSites.add(sequence.length() - 1);
                return possibleSites;
                
            case modnaa_protein:
                
                if (!isNterm(proteinSequence, peptideStart)) {
                    
                    return possibleSites;
                }
                
            case modnaa_peptide:
                
                pattern = modification.getPattern();

                // See if we have the correct amino acid at terminus
                if (!pattern.getAminoAcidsAtTargetSet().contains(sequence.charAt(0))) {
                    
                    return possibleSites;
                    
                }

                // If we have a multiple amino acid pattern see if we match on the peptide terminus
                if (pattern.length() > 1) {
                    
                    int peptideEnd = getPeptideEnd(proteinSequence, peptideStart);
                    int missingLeft = Math.min(-pattern.getMinIndex(), peptideStart);
                    int missingRight = Math.min(Math.max(pattern.getMaxIndex() - sequence.length(), 0), proteinSequence.length() - peptideEnd);
                    StringBuilder tempSequence = new StringBuilder(missingLeft + sequence.length() + missingRight);
                    
                    if (missingLeft > 0) {
                        
                        String complement = proteinSequence.substring(peptideStart - missingLeft, peptideStart);
                        tempSequence.append(complement);
                        
                    }
                    
                    tempSequence.append(sequence);
                    
                    if (missingRight > 0) {
                        
                        String complement = proteinSequence.substring(peptideEnd + 1, peptideEnd + missingRight + 1);
                        tempSequence.append(complement);
                        
                    }
                    
                    if (!pattern.matchesAt(tempSequence.toString(), modificationSequenceMatchingPreferences, missingLeft)) {
                        
                        return possibleSites;
                        
                    }
                }

                possibleSites.add(1);
                return possibleSites;
                
            default:
                throw new UnsupportedOperationException("Modification site not implemented for modification of type " + modification.getModificationType() + ".");
        }
    }

    /**
     * Returns a boolean indicating whether the peptide is at the N-terminus of
     * the given protein sequence. Initial methionine is cleaved.
     *
     * @param proteinSequence the sequence of the protein
     * @param peptideStart the 0 based peptide start index on the protein
     *
     * @return a boolean indicating whether the peptide is at the N-terminus of
     * the given protein
     */
    public boolean isNterm(String proteinSequence, int peptideStart) {
        return peptideStart == 0 || peptideStart == 1 && proteinSequence.charAt(0) == 'M';
    }

    /**
     * Returns a boolean indicating whether the peptide is at the C-terminus of
     * the given protein sequence.
     *
     * @param proteinSequence the sequence of the protein
     * @param peptideStart the 0 based peptide start index on the protein
     *
     * @return a boolean indicating whether the peptide is at the C-terminus of
     * the given protein
     */
    public boolean isCterm(String proteinSequence, int peptideStart) {
        int peptideEnd = getPeptideEnd(proteinSequence, peptideStart);
        return peptideEnd == proteinSequence.length() - 1;
    }

    /**
     * Indicates whether another peptide has the same sequence and modification
     * status without accounting for modification localization.
     *
     * @param anotherPeptide the other peptide to compare to this instance
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the other peptide has the same
     * sequence and modification status.
     */
    public boolean isSameSequenceAndModificationStatus(Peptide anotherPeptide, SequenceMatchingPreferences sequenceMatchingPreferences) {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return isSameSequence(anotherPeptide, sequenceMatchingPreferences) && isSameModificationStatus(anotherPeptide);
    }

    /**
     * Returns a boolean indicating whether another peptide has the same
     * sequence as the given peptide
     *
     * @param anotherPeptide the other peptide to compare
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the other peptide has the same
     * sequence
     */
    public boolean isSameSequence(Peptide anotherPeptide, SequenceMatchingPreferences sequenceMatchingPreferences) {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        AminoAcidSequence pattern = new AminoAcidSequence(anotherPeptide.getSequence());
        return pattern.matches(getSequence(), sequenceMatchingPreferences);
    }

    /**
     * Indicates whether another peptide has the same variable modifications as
     * this peptide. The localization of the Modification is not accounted for.
     * Modifications are considered equal when of exact same mass, no rounding is conducted. Modifications
     * should be loaded in the Modification factory.
     *
     * @param anotherPeptide the other peptide
     * @return a boolean indicating whether the other peptide has the same
     * variable modifications as the peptide of interest
     */
    public boolean isSameModificationStatus(Peptide anotherPeptide) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        if (!isModified() && !anotherPeptide.isModified()) {
            return true;
        }

        if (getNModifications() != anotherPeptide.getNModifications()) {
            return false;
        }

        ModificationFactory modificationFactory = ModificationFactory.getInstance();
        
        ArrayList<ModificationMatch> modificationMatches1 = getModificationMatches();
        Map<Double, Long> masses1 = modificationMatches1.stream().collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.counting()));
        
        ArrayList<ModificationMatch> modificationMatches2 = anotherPeptide.getModificationMatches();
        Map<Double, Long> masses2 = modificationMatches2.stream().collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.counting()));

        if (masses1.size() != masses2.size()) {
            return false;
        }
        
        return !masses1.entrySet().stream().anyMatch(entry -> masses2.get(entry.getKey()) == null || !entry.getValue().equals(masses2.get(entry.getKey())));
    }

    /**
     * Indicates whether another peptide has the same modifications at the same
     * localization as this peptide. This method comes as a complement of
     * isSameAs, here the localization of all Modifications is taken into account.
     * Modifications are considered equal when of same mass. Modifications
     * should be loaded in the Modification factory.
     *
     * @param anotherPeptide another peptide
     * @param modifications the Modifications
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide, ArrayList<String> modifications) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        if (!isModified() && !anotherPeptide.isModified()) {
            return true;
        }

        if (getNModifications() != anotherPeptide.getNModifications()) {
            return false;
        }

        HashMap<Double, ArrayList<Integer>> modificationToPositionsMap1 = new HashMap<>();
        HashMap<Double, ArrayList<Integer>> modificationToPositionsMap2 = new HashMap<>();
        ModificationFactory modificationFactory = ModificationFactory.getInstance();
        for (ModificationMatch modificationMatch : modificationMatches) {
            String modName = modificationMatch.getModification();
            if (modifications.contains(modName)) {
                double tempMass = modificationFactory.getModification(modName).getMass();
                ArrayList<Integer> sites = modificationToPositionsMap1.get(tempMass);
                if (sites == null) {
                    sites = new ArrayList<>();
                    modificationToPositionsMap1.put(tempMass, sites);
                }
                int position = modificationMatch.getModificationSite();
                sites.add(position);
            }
        }
        for (ModificationMatch modificationMatch : anotherPeptide.getModificationMatches()) {
            String modName = modificationMatch.getModification();
            if (modifications.contains(modName)) {
                double tempMass = modificationFactory.getModification(modName).getMass();
                ArrayList<Integer> sites = modificationToPositionsMap2.get(tempMass);
                if (sites == null) {
                    sites = new ArrayList<>();
                    modificationToPositionsMap2.put(tempMass, sites);
                }
                int position = modificationMatch.getModificationSite();
                sites.add(position);
            }
        }
        for (Double tempMass : modificationToPositionsMap1.keySet()) {
            ArrayList<Integer> sites1 = modificationToPositionsMap1.get(tempMass);
            ArrayList<Integer> sites2 = modificationToPositionsMap2.get(tempMass);
            if (sites2 == null || sites1.size() != sites2.size()) {
                return false;
            }
            Collections.sort(sites1);
            Collections.sort(sites2);
            for (int i = 0; i < sites1.size(); i++) {
                if (sites1.get(i).intValue() != sites2.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Indicates whether another peptide has the same modifications at the same
     * localization as this peptide. This method comes as a complement of
     * isSameAs, here the localization of all Modifications is taken into account.
     * Modifications are considered equal when of same mass. Modifications
     * should be loaded in the Modification factory.
     *
     * @param anotherPeptide another peptide
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        if (!isModified() && !anotherPeptide.isModified()) {
            return true;
        }

        if (getNModifications() != anotherPeptide.getNModifications()) {
            return false;
        }

        ArrayList<String> modifications = new ArrayList<>();
        for (ModificationMatch modificationMatch : getModificationMatches()) {
            String modName = modificationMatch.getModification();
            if (!modifications.contains(modName)) {
                modifications.add(modName);
            }
        }
        for (ModificationMatch modificationMatch : anotherPeptide.getModificationMatches()) {
            String modName = modificationMatch.getModification();
            if (!modifications.contains(modName)) {
                modifications.add(modName);
            }
        }
        return sameModificationsAs(anotherPeptide, modifications);
    }

    /**
     * Returns the N-terminal of the peptide as a String. Returns "NH2" if the
     * terminal is not modified, otherwise returns the name of the modification.
     * /!\ this method will work only if the Modification found in the peptide are in the
     * ModificationFactory.
     *
     * @return the N-terminal of the peptide as a String, e.g., "NH2"
     */
    public String getNTerminal() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        String nTerm = "NH2";

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        if (modificationMatches != null) {
            for (ModificationMatch modificationMatch : modificationMatches) {
                if (modificationMatch.getModificationSite() == 1) {
                    Modification modification = modificationFactory.getModification(modificationMatch.getModification());
                    if (modification.getModificationType() != ModificationType.modaa) {
                        nTerm = modification.getShortName();
                    }
                }
            }
        }

        nTerm = nTerm.replaceAll("-", " ");
        return nTerm;
    }

    /**
     * Returns the C-terminal of the peptide as a String. Returns "COOH" if the
     * terminal is not modified, otherwise returns the name of the modification.
     * /!\ This method will work only if the Modification found in the peptide are in the
     * ModificationFactory.
     *
     * @return the C-terminal of the peptide as a String, e.g., "COOH"
     */
    public String getCTerminal() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        String cTerm = "COOH";
        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        if (modificationMatches != null) {
            for (int i = 0; i < modificationMatches.size(); i++) {
                if (modificationMatches.get(i).getModificationSite() == sequence.length()) {
                    Modification modification = modificationFactory.getModification(modificationMatches.get(i).getModification());
                    if (modification.getModificationType() != ModificationType.modaa) {
                        cTerm = modification.getShortName();
                    }
                }
            }
        }

        cTerm = cTerm.replaceAll("-", " ");
        return cTerm;
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with Modification tags, e.g, &lt;mox&gt;. /!\
     * this method will work only if the Modification found in the peptide are in the
     * ModificationFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * Modification tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true the short names are used in the tags
     * @param excludeAllFixedPtms if true, all fixed Modifications are excluded
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(PtmSettings modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean excludeAllFixedPtms) {
        HashMap<Integer, ArrayList<String>> confidentModificationSites = new HashMap<>();
        HashMap<Integer, ArrayList<String>> representativeModificationSites = new HashMap<>();
        HashMap<Integer, ArrayList<String>> secondaryModificationSites = new HashMap<>();
        HashMap<Integer, ArrayList<String>> fixedModificationSites = new HashMap<>();

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        if (modificationMatches != null) {
            for (ModificationMatch modMatch : modificationMatches) {
                String modName = modMatch.getModification();
                int modSite = modMatch.getModificationSite();
                if (modMatch.getVariable()) {
                    if (modMatch.getConfident()) {
                        if (!confidentModificationSites.containsKey(modSite)) {
                            confidentModificationSites.put(modSite, new ArrayList<>(1));
                        }
                        confidentModificationSites.get(modSite).add(modName);
                    } else {
                        if (!representativeModificationSites.containsKey(modSite)) {
                            representativeModificationSites.put(modSite, new ArrayList<>(1));
                        }
                        representativeModificationSites.get(modSite).add(modName);
                    }
                } else if (!excludeAllFixedPtms) {
                    if (!fixedModificationSites.containsKey(modSite)) {
                        fixedModificationSites.put(modSite, new ArrayList<>(1));
                    }
                    fixedModificationSites.get(modSite).add(modName);
                }
            }
        }
        return getTaggedModifiedSequence(modificationProfile, this, confidentModificationSites, representativeModificationSites, secondaryModificationSites,
                fixedModificationSites, useHtmlColorCoding, includeHtmlStartEndTags, useShortName);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with Modification tags, e.g, &lt;mox&gt;. /!\
     * this method will work only if the Modification found in the peptide are in the
     * ModificationFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * Modification tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true the short names are used in the tags
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(PtmSettings modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return getTaggedModifiedSequence(modificationProfile, useHtmlColorCoding, includeHtmlStartEndTags, useShortName, false);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with Modification tags, e.g, &lt;mox&gt;. /!\
     * This method will work only if the Modification found in the peptide are in the
     * ModificationFactory.
     *
     * @param modificationProfile the modification profile of the search
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param peptide the peptide to annotate
     * @param confidentModificationSites the confidently localized variable
     * modification sites in a map: aa number &gt; list of modifications (1 is
     * the first AA) (can be null)
     * @param representativeAmbiguousModificationSites the representative site
     * of the ambiguously localized variable modifications in a map: aa number
     * &gt; list of modifications (1 is the first AA) (can be null)
     * @param secondaryAmbiguousModificationSites the secondary sites of the
     * ambiguously localized variable modifications in a map: aa number &gt;
     * list of modifications (1 is the first AA) (can be null)
     * @param fixedModificationSites the fixed modification sites in a map: aa
     * number &gt; list of modifications (1 is the first AA) (can be null)
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * Modification tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(PtmSettings modificationProfile, Peptide peptide,
            HashMap<Integer, ArrayList<String>> confidentModificationSites, HashMap<Integer, ArrayList<String>> representativeAmbiguousModificationSites,
            HashMap<Integer, ArrayList<String>> secondaryAmbiguousModificationSites, HashMap<Integer, ArrayList<String>> fixedModificationSites,
            boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {

        if (confidentModificationSites == null) {
            confidentModificationSites = new HashMap<>(0);
        }
        if (representativeAmbiguousModificationSites == null) {
            representativeAmbiguousModificationSites = new HashMap<>(0);
        }
        if (secondaryAmbiguousModificationSites == null) {
            secondaryAmbiguousModificationSites = new HashMap<>(0);
        }
        if (fixedModificationSites == null) {
            fixedModificationSites = new HashMap<>(0);
        }

        String modifiedSequence = "";

        if (useHtmlColorCoding && includeHtmlStartEndTags) {
            modifiedSequence += "<html>";
        }

        modifiedSequence += peptide.getNTerminal() + "-";

        modifiedSequence += AminoAcidSequence.getTaggedModifiedSequence(modificationProfile, peptide.sequence, confidentModificationSites,
                representativeAmbiguousModificationSites, secondaryAmbiguousModificationSites, fixedModificationSites, useHtmlColorCoding, useShortName);

        modifiedSequence += "-" + peptide.getCTerminal();

        if (useHtmlColorCoding && includeHtmlStartEndTags) {
            modifiedSequence += "</html>";
        }

        return modifiedSequence;
    }

    /**
     * Returns the peptide modifications as a string.
     *
     * @param peptide the peptide
     * @param variablePtms if true, only variable Modifications are shown, false return
     * only the fixed Modifications
     *
     * @return the peptide modifications as a string
     */
    public static String getPeptideModificationsAsString(Peptide peptide, boolean variablePtms) {

        StringBuilder result = new StringBuilder();

        HashMap<String, ArrayList<Integer>> modMap = new HashMap<>();
        if (peptide.isModified()) {
            for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
                if ((variablePtms && modificationMatch.getVariable()) || (!variablePtms && !modificationMatch.getVariable())) {
                    if (!modMap.containsKey(modificationMatch.getModification())) {
                        modMap.put(modificationMatch.getModification(), new ArrayList<>());
                    }
                    modMap.get(modificationMatch.getModification()).add(modificationMatch.getModificationSite());
                }
            }
        }

        boolean first = true, first2;
        ArrayList<String> mods = new ArrayList<>(modMap.keySet());

        Collections.sort(mods);
        for (String mod : mods) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            first2 = true;
            result.append(mod);
            result.append(" (");
            for (int aa : modMap.get(mod)) {
                if (first2) {
                    first2 = false;
                } else {
                    result.append(", ");
                }
                result.append(aa);
            }
            result.append(")");
        }

        return result.toString();
    }

    /**
     * Estimates the theoretic mass of the peptide. The previous version is
     * silently overwritten.
     */
    public synchronized void estimateTheoreticMass() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (mass == -1) {

            double tempMass = StandardMasses.h2o.mass;
            char[] sequenceAsCharArray = sequence.toCharArray();

            for (char aa : sequenceAsCharArray) {
                AminoAcid currentAA = AminoAcid.getAminoAcid(aa);
                tempMass += currentAA.getMonoisotopicMass();
            }

            if (modificationMatches != null) {
                ModificationFactory modificationFactory = ModificationFactory.getInstance();
                tempMass += modificationMatches.stream().mapToDouble(modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass()).sum();
            }

            setMass(tempMass);
        }
    }

    /**
     * Returns the sequence of this peptide as AminoAcidPattern.
     *
     * @return the sequence of this peptide as AminoAcidPattern
     */
    public AminoAcidPattern getSequenceAsPattern() {
        return getSequenceAsPattern(sequence);
    }

    /**
     * Returns the given sequence as AminoAcidPattern.
     *
     * @param sequence the sequence of interest
     * @return the sequence as AminoAcidPattern
     */
    public static AminoAcidPattern getSequenceAsPattern(String sequence) {
        return AminoAcidPattern.getAminoAcidPatternFromString(sequence);
    }

    /**
     * Returns the sequence of this peptide as AminoAcidSequence.
     *
     * @return the sequence of this peptide as AminoAcidSequence
     */
    public AminoAcidSequence getSequenceAsAminoAcidSequence() {
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return getSequenceAsAminoAcidSequence(sequence);
    }

    /**
     * Returns the given sequence as AminoAcidSequence.
     *
     * @param sequence the sequence of interest
     *
     * @return the sequence as AminoAcidSequence
     */
    public static AminoAcidSequence getSequenceAsAminoAcidSequence(String sequence) {
        return new AminoAcidSequence(sequence);
    }

    /**
     * Indicates whether a peptide can be derived from a decoy protein.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return whether a peptide can be derived from a decoy protein
     */
    public boolean isDecoy(SequenceMatchingPreferences sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        return proteinMapping.keySet().stream().anyMatch(accession -> SequenceFactory.getInstance().isDecoyAccession(accession));
    }

    /**
     * Returns a version of the peptide which does not contain the given list of modifications.
     *
     * @param peptide the original peptide
     * @param forbiddenModifications list of forbidden modifications
     *
     * @return a not modified version of the peptide
     */
    public static Peptide getNoModPeptide(Peptide peptide, ArrayList<Modification> forbiddenModifications) {

        Peptide noModPeptide = new Peptide(peptide.getSequence(), new ArrayList<>(0));
        noModPeptide.setProteinMapping(peptide.getProteinMapping());

        ArrayList<ModificationMatch> allModificationMatches = peptide.getModificationMatches();
        
        if (allModificationMatches != null) {
            
            HashSet<String> forbiddenModificationsNames = forbiddenModifications.stream().map(modification -> modification.getName()).collect(Collectors.toCollection(HashSet::new));
            
            ArrayList<ModificationMatch> filteredModificationMatches = allModificationMatches.stream().filter(modificationMatch -> !forbiddenModificationsNames.contains(modificationMatch.getModification())).collect(Collectors.toCollection(ArrayList::new));

            noModPeptide.setModificationMatches(filteredModificationMatches);
            
        }

        return noModPeptide;
    }
}
