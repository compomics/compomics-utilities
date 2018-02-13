package com.compomics.util.experiment.biology.proteins;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.experiment.identification.matches.PeptideVariantMatches;
import com.compomics.util.experiment.identification.utils.PeptideUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.mass_spectrometry.utils.StandardMasses;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;

import java.util.*;
import java.util.function.Function;
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
    private long key;
    /**
     * Boolean indicating whether the matching key is set.
     */
    private boolean keySet = false;
    /**
     * The peptide matching key.
     */
    private long matchingKey;
    /**
     * The peptide mass.
     */
    private double mass = -1.0;
    /**
     * The mapping of this peptide on proteins as a map, accession to position.
     * Position on protein sequences is 0 based.
     */
    private TreeMap<String, int[]> proteinMapping = null;
    /**
     * The modifications carried by the peptide.
     */
    private ModificationMatch[] modificationMatches = null;
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
     * @param variantMatches the sequence variants compared to the database
     * @param sanityCheck boolean indicating whether the input should be checked
     * @param mass the mass of the peptide
     */
    public Peptide(String aSequence, ModificationMatch[] modificationMatches, HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMatches, boolean sanityCheck, double mass) {

        this.sequence = aSequence;
        this.modificationMatches = modificationMatches != null && modificationMatches.length > 0 ? modificationMatches : null;
        this.variantMatches = variantMatches;
        this.mass = mass;

        if (sanityCheck) {

            sanityCheck();

        }

        setKey(getKey(sequence, modificationMatches));

    }

    /**
     * Constructor.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modificationMatches the Modification of this peptide
     * @param sanityCheck boolean indicating whether the input should be checked
     * @param mass the mass of the peptide
     */
    public Peptide(String aSequence, ModificationMatch[] modificationMatches, boolean sanityCheck, double mass) {
        this(aSequence, modificationMatches, null, sanityCheck, mass);
    }

    /**
     * Constructor.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modificationMatches the Modification of this peptide
     * @param sanityCheck boolean indicating whether the input should be checked
     */
    public Peptide(String aSequence, ModificationMatch[] modificationMatches, boolean sanityCheck) {
        this(aSequence, modificationMatches, sanityCheck, -1.0);
    }

    /**
     * Constructor. No sanity check is performed on the input.
     *
     * @param aSequence the peptide sequence
     */
    public Peptide(String aSequence) {
        this(aSequence, null, false);
    }

    /**
     * Constructor. No sanity check is performed on the input.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modifications the Modification of this peptide
     */
    public Peptide(String aSequence, ModificationMatch[] modifications) {
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
    public Peptide(String aSequence, ModificationMatch[] modificationMatches, HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMatches, boolean sanityCheck) {
        this(aSequence, modificationMatches, variantMatches, sanityCheck, -1.0);
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

            String[] conflictingPtms = Arrays.stream(modificationMatches)
                    .map(modificationMatch -> modificationMatch.getModification())
                    .filter((modificationName) -> (modificationName.contains(MODIFICATION_SEPARATOR) || modificationName.contains(MODIFICATION_LOCALIZATION_SEPARATOR)))
                    .toArray(String[]::new);

            if (conflictingPtms.length == 0) {

                String conflictingPtmsString = Arrays.stream(conflictingPtms)
                        .collect(Collectors.joining(", "));
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
    public void setKey(long key) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.key = key;
    }

    /**
     * Returns the proteins mapping as a map of 0 based indexes for every
     * protein accession.
     *
     * @return the proteins mapping
     */
    public TreeMap<String, int[]> getProteinMapping() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return proteinMapping;
    }

    /**
     * Sets the proteins mapping as a map of 0 based indexes for every protein
     * accession.
     *
     * @param proteinMapping the proteins mapping
     */
    public void setProteinMapping(TreeMap<String, int[]> proteinMapping) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

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
     * Returns the sequence variant matches of this peptide indexed by protein
     * accession and peptide start.
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

        if (mass == -1.0) {

            estimateTheoreticMass();

        }

        return mass;
    }

    /**
     * Getter for the modifications carried by this peptide.
     *
     * @return the modifications matches as found by the search engine
     */
    public ModificationMatch[] getModificationMatches() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return modificationMatches == null ? new ModificationMatch[0] : modificationMatches;
    }

    /**
     * Sets new modification matches for the peptide.
     *
     * @param modificationMatches the new modification matches
     */
    public void setModificationMatches(ModificationMatch[] modificationMatches) {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        this.modificationMatches = modificationMatches;

        setMass(-1.0);
        setKey(getKey(sequence, modificationMatches));
    }

    /**
     * Clears the list of imported modification matches.
     */
    public void clearModificationMatches() {

        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();

        modificationMatches = new ModificationMatch[0];

        setMass(-1.0);
        setKey(getKey(sequence, modificationMatches));
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

            modificationMatches = new ModificationMatch[1];

        } else {

            modificationMatches = Arrays.copyOf(modificationMatches, modificationMatches.length + 1);

        }
        modificationMatches[modificationMatches.length] = modificationMatch;

        setMass(-1.0);
        setKey(getKey(sequence, modificationMatches));

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
    public int getNMissedCleavages(DigestionParameters digestionPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return digestionPreferences.getCleavagePreference() == DigestionParameters.CleavagePreference.enzyme
                ? digestionPreferences.getEnzymes().stream()
                        .mapToInt(enzyme -> getNMissedCleavages(enzyme))
                        .min().orElse(0)
                : 0;
    }

    /**
     * Returns the key accounting for sequence matching preferences
     *
     * @return the key accounting for sequence matching preferences
     */
    public long getMatchingKey() {

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
    public void setMatchingKey(long matchingKey) {

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
    public long getMatchingKey(SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (!keySet) {

            String matchingSequence = AminoAcid.getMatchingSequence(sequence, sequenceMatchingPreferences);
            setMatchingKey(getKey(matchingSequence, modificationMatches));
            keySet = true;

        }

        return matchingKey;
    }

    /**
     * Returns the reference key of a peptide. index =
     * SEQUENCE_modMass1_modMass2 with modMass1 and modMass2 modification masses
     * ordered alphabetically. See ExperimentObject for the conversion to long.
     *
     * Note: the key is not unique for indistinguishable sequences, see
     * getMatchingKey(SequenceMatchingPreferences sequenceMatchingPreferences).
     * Modifications must be loaded in the modification factory.
     *
     * @return the key of the peptide
     */
    public long getKey() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

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
    public static long getKey(String sequence, ModificationMatch[] modificationMatches) {

        if (modificationMatches == null || modificationMatches.length == 0) {

            return ExperimentObject.asLong(sequence);

        }

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        String modificationsKey = Arrays.stream(modificationMatches)
                .filter(ModificationMatch::getVariable)
                .map(modificationMatch -> modificationMatch.getConfident() || modificationMatch.getInferred()
                ? Arrays.stream(new String[]{
            modificationFactory.getModification(modificationMatch.getModification()).getAmbiguityKey(),
            MODIFICATION_LOCALIZATION_SEPARATOR,
            Integer.toString(modificationMatch.getModificationSite())})
                        .collect(Collectors.joining())
                : modificationFactory.getModification(modificationMatch.getModification()).getAmbiguityKey())
                .sorted()
                .collect(Collectors.joining(MODIFICATION_SEPARATOR));

        String keyAsString = Arrays.stream(new String[]{sequence, MODIFICATION_SEPARATOR, modificationsKey})
                .collect(Collectors.joining());

        return ExperimentObject.asLong(keyAsString);

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

        return modificationMatches == null ? 0 : (int) Arrays.stream(modificationMatches)
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

        return modificationMatches == null ? 0 : modificationMatches.length;
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

                        if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingParameters.defaultStringMatching, peptideStart + i)) {

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
                    } else if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingParameters.defaultStringMatching, peptideEnd)) {

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

                    if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingParameters.defaultStringMatching, peptideEnd)) {

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
                    } else if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingParameters.defaultStringMatching, 0)) {

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
                } else if (targetedAA.contains(aa) && aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingParameters.defaultStringMatching, 0)) {

                    possibleSites.add(1);

                }
                return possibleSites;

            default:
                throw new UnsupportedOperationException("Modification site not implemented for modification of type " + modification.getModificationType() + ".");
        }
    }

    /**
     * Returns the potential modification sites as a set. 1
     * is the first amino acid. An empty list is returned if no modification
     * site was found. All proteins and all positions are used.
     *
     * @param modification the Modification considered
     * @param sequenceProvider a protein sequence provider
     * @param modificationSequenceMatchingPreferences the sequence matching
     * preferences for Modification to peptide mapping
     *
     * @return a list of potential modification sites
     */
    public HashSet<Integer> getPotentialModificationSites(Modification modification, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingPreferences) {

        return proteinMapping.entrySet().stream()
                .flatMap(entry -> Arrays.stream(entry.getValue())
                    .boxed()
                    .flatMap(site -> getPotentialModificationSites(modification, sequenceProvider.getSequence(entry.getKey()), site, modificationSequenceMatchingPreferences).stream()))
                .collect(Collectors.toCollection(HashSet::new));

    }

    /**
     * Returns the potential modification sites as an ordered list of sites. 1
     * is the first amino acid. An empty list is returned if no modification
     * site was found.
     *
     * @param modification the Modification considered
     * @param proteinSequence the protein sequence
     * @param peptideStart the index of the peptide start on the protein
     * @param modificationSequenceMatchingPreferences the sequence matching
     * preferences for Modification to peptide mapping
     *
     * @return a list of potential modification sites
     */
    public ArrayList<Integer> getPotentialModificationSites(Modification modification, String proteinSequence, int peptideStart, SequenceMatchingParameters modificationSequenceMatchingPreferences) {

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
    public boolean isSameSequenceAndModificationStatus(Peptide anotherPeptide, SequenceMatchingParameters sequenceMatchingPreferences) {

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
    public boolean isSameSequence(Peptide anotherPeptide, SequenceMatchingParameters sequenceMatchingPreferences) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        AminoAcidSequence pattern = new AminoAcidSequence(anotherPeptide.getSequence());
        return pattern.matches(getSequence(), sequenceMatchingPreferences);

    }

    /**
     * Indicates whether another peptide has the same variable modifications as
     * this peptide. The localization of the Modification is not accounted for.
     * Modifications are considered equal when of exact same mass, no rounding
     * is conducted. Modifications should be loaded in the Modification factory.
     *
     * @param anotherPeptide the other peptide
     *
     * @return a boolean indicating whether the other peptide has the same
     * variable modifications as the peptide of interest
     */
    public boolean isSameModificationStatus(Peptide anotherPeptide) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        if (getNModifications() != anotherPeptide.getNModifications()) {
            return false;
        }

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        ModificationMatch[] modificationMatches1 = getModificationMatches();
        Map<Double, Long> masses1 = Arrays.stream(modificationMatches1).collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.counting()));

        ModificationMatch[] modificationMatches2 = anotherPeptide.getModificationMatches();
        Map<Double, Long> masses2 = Arrays.stream(modificationMatches2).collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.counting()));

        if (masses1.size() != masses2.size()) {
            return false;
        }

        return !masses1.entrySet().stream()
                .anyMatch(entry -> masses2.get(entry.getKey()) == null
                || !entry.getValue().equals(masses2.get(entry.getKey())));
    }

    /**
     * Indicates whether another peptide has the same modifications at the same
     * localization as this peptide. This method comes as a complement of
     * isSameAs, here the localization of all Modifications is taken into
     * account. Modifications are considered equal when of same mass.
     * Modifications should be loaded in the Modification factory.
     *
     * @param anotherPeptide another peptide
     * @param modifications the Modifications
     *
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide, ArrayList<String> modifications) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

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
     * isSameAs, here the localization of all Modifications is taken into
     * account. Modifications are considered equal when of same mass.
     * Modifications should be loaded in the Modification factory.
     *
     * @param anotherPeptide another peptide
     *
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

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
     * /!\ this method will work only if the Modification found in the peptide
     * are in the ModificationFactory.
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
     * /!\ This method will work only if the Modification found in the peptide
     * are in the ModificationFactory.
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

            for (int i = 0; i < modificationMatches.length; i++) {

                ModificationMatch modificationMatch = modificationMatches[i];

                if (modificationMatch.getModificationSite() == sequence.length()) {

                    Modification modification = modificationFactory.getModification(modificationMatch.getModification());

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
     * modification sites color coded or with Modification tags, e.g,
     * &lt;mox&gt;. /!\ this method will work only if the Modification found in
     * the peptide are in the ModificationFactory. /!\ This method uses the
     * modifications as set in the modification matches of this peptide and
     * displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * Modification tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true the short names are used in the tags
     * @param excludeAllFixedPtms if true, all fixed Modifications are excluded
     *
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(ModificationParameters modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean excludeAllFixedPtms) {

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
        return PeptideUtils.getTaggedModifiedSequence(this, modificationProfile, confidentModificationSites, representativeModificationSites, secondaryModificationSites,
                fixedModificationSites, useHtmlColorCoding, includeHtmlStartEndTags, useShortName);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with Modification tags, e.g,
     * &lt;mox&gt;. /!\ this method will work only if the Modification found in
     * the peptide are in the ModificationFactory. /!\ This method uses the
     * modifications as set in the modification matches of this peptide and
     * displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * Modification tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true the short names are used in the tags
     *
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(ModificationParameters modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        return getTaggedModifiedSequence(modificationProfile, useHtmlColorCoding, includeHtmlStartEndTags, useShortName, false);
    }

    /**
     * Estimates the theoretic mass of the peptide. The previous version is
     * silently overwritten.
     */
    public void estimateTheoreticMass() {

        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();

        double tempMass = StandardMasses.h2o.mass
                + sequence.chars()
                        .mapToDouble(aa -> AminoAcid.getAminoAcid((char) aa).getMonoisotopicMass())
                        .sum();

        if (modificationMatches != null) {

            ModificationFactory modificationFactory = ModificationFactory.getInstance();
            tempMass += Arrays.stream(modificationMatches)
                    .mapToDouble(modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass())
                    .sum();

        }

        setMass(tempMass);
    }

    /**
     * Returns a version of the peptide which does not contain the given list of
     * modifications.
     *
     * @param forbiddenModifications list of forbidden modifications
     *
     * @return a not modified version of the peptide
     */
    public Peptide getNoModPeptide(HashSet<String> forbiddenModifications) {

        ModificationMatch[] modificationMatches = getModificationMatches();

        modificationMatches = Arrays.stream(modificationMatches)
                .filter(modificationMatch -> !forbiddenModifications.contains(modificationMatch.getModification()))
                .toArray(ModificationMatch[]::new);

        Peptide noModPeptide = new Peptide(getSequence(), modificationMatches);
        noModPeptide.setProteinMapping(getProteinMapping());

        return noModPeptide;
    }
}
