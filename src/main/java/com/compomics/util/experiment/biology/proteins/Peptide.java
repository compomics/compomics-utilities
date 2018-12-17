package com.compomics.util.experiment.biology.proteins;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.experiment.identification.matches.PeptideVariantMatches;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.identification.utils.PeptideUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.mass_spectrometry.utils.StandardMasses;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;

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
     * The peptide sequence.
     */
    private String sequence;
    /**
     * The peptide key.
     */
    private long key = NO_KEY;
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
     * The variable modifications carried by the peptide.
     */
    private ModificationMatch[] variableModifications = null;
    /**
     * Convenience array for no modifications.
     */
    private static final ModificationMatch[] NO_MOD = new ModificationMatch[0];
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
     * @param variableModifications the variable modifications of this peptide
     * @param variantMatches the sequence variants compared to the database
     * @param sanityCheck boolean indicating whether the input should be checked
     * @param mass the mass of the peptide
     */
    public Peptide(String aSequence, ModificationMatch[] variableModifications, HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMatches, boolean sanityCheck, double mass) {

        this.sequence = aSequence;
        this.variableModifications = variableModifications != null && variableModifications.length > 0 ? variableModifications : null;
        this.variantMatches = variantMatches;
        this.mass = mass;

        if (sanityCheck) {

            sanityCheck();

        }

    }

    /**
     * Constructor.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param variableModifications the variable modification of this peptide
     * @param sanityCheck boolean indicating whether the input should be checked
     * @param mass the mass of the peptide
     */
    public Peptide(String aSequence, ModificationMatch[] variableModifications, boolean sanityCheck, double mass) {
        this(aSequence, variableModifications, null, sanityCheck, mass);
    }

    /**
     * Constructor.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param variableModifications the variable modification of this peptide
     * @param sanityCheck boolean indicating whether the input should be checked
     */
    public Peptide(String aSequence, ModificationMatch[] variableModifications, boolean sanityCheck) {
        this(aSequence, variableModifications, sanityCheck, -1.0);
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
     * @param variableModifications the variable modification of this peptide
     */
    public Peptide(String aSequence, ModificationMatch[] variableModifications) {
        this(aSequence, variableModifications, false);
    }

    /**
     * Constructor for the peptide.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param variableModifications the variable modifications of this peptide
     * @param variantMatches the variants compared to the database
     * @param sanityCheck boolean indicating whether the input should be checked
     */
    public Peptide(String aSequence, ModificationMatch[] variableModifications, HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMatches, boolean sanityCheck) {
        this(aSequence, variableModifications, variantMatches, sanityCheck, -1.0);
    }

    /**
     * Removes characters from the sequence and checks the modifications names
     * for forbidden characters.
     */
    private void sanityCheck() {

        writeDBMode();

        sequence = sequence.replaceAll("[#*$%&]", "");

        if (variableModifications != null) {

            String[] conflictingPtms = Arrays.stream(variableModifications)
                    .map(modificationMatch -> modificationMatch.getModification())
                    .filter((modificationName) -> (modificationName.contains(MODIFICATION_SEPARATOR) || modificationName.contains(MODIFICATION_LOCALIZATION_SEPARATOR)))
                    .toArray(String[]::new);

            if (conflictingPtms.length > 0) {

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

        writeDBMode();

        this.mass = mass;
    }

    /**
     * Sets the object key.
     *
     * @param key the object key
     */
    public void setKey(long key) {

        writeDBMode();

        this.key = key;
    }

    /**
     * Returns the proteins mapping as a map of 0 based indexes for every
     * protein accession.
     *
     * @return the proteins mapping
     */
    public TreeMap<String, int[]> getProteinMapping() {

        readDBMode();

        return proteinMapping;
    }

    /**
     * Sets the proteins mapping as a map of 0 based indexes for every protein
     * accession.
     *
     * @param proteinMapping the proteins mapping
     */
    public void setProteinMapping(TreeMap<String, int[]> proteinMapping) {

        writeDBMode();

        this.proteinMapping = proteinMapping;

    }

    /**
     * Sets the sequence variant matches of this peptide.
     *
     * @param variantMatches the variant matches of this peptide
     */
    public void setVariantMatches(HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMatches) {

        writeDBMode();

        this.variantMatches = variantMatches;
    }

    /**
     * Returns the sequence variant matches of this peptide indexed by protein
     * accession and peptide start.
     *
     * @return the sequence variant matches of this peptide
     */
    public HashMap<String, HashMap<Integer, PeptideVariantMatches>> getVariantMatches() {

        readDBMode();

        return variantMatches;
    }

    /**
     * Returns the mass, does not attempt to estimate it. An exception is thrown
     * if the mass was not previously estimated.
     *
     * @return the peptide mass
     */
    public double getMass() {

        if (mass == -1.0) {
            throw new IllegalArgumentException("Mass not estimated.");
        }

        return mass;

    }

    /**
     * Returns the mass, estimates it if not done before.
     *
     * @param modificationParameters the modifications parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationSequenceMatchingParameters the modifications sequence
     * matching parameters
     *
     * @return the peptide mass
     */
    public double getMass(ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingParameters) {

        readDBMode();

        if (mass == -1.0) {

            estimateTheoreticMass(modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);

        }

        return mass;
    }

    /**
     * Returns the variable modifications.
     *
     * @return the variable modifications
     */
    public ModificationMatch[] getVariableModifications() {

        readDBMode();

        return variableModifications == null ? NO_MOD : variableModifications;
    }

    /**
     * Returns the variable modifications indexed by site. Modifications are
     * indexed by site as follows: N-term modifications are at index 0, C-term
     * at sequence length + 1, and amino acid at 1-based index on the sequence.
     *
     * @return the variable modifications indexed by site
     */
    public String[] getIndexedVariableModifications() {

        String[] result = new String[sequence.length() + 2];

        if (variableModifications != null) {

            for (ModificationMatch modificationMatch : variableModifications) {

                int site = modificationMatch.getSite();

                if (result[site] == null) {

                    result[site] = modificationMatch.getModification();

                } else {

                    throw new IllegalArgumentException("Two modifications found (" + result[site] + " and " + modificationMatch.getModification() + ") at site " + site + " of peptide " + sequence + ".");

                }
            }
        }
        return result;
    }

    /**
     * Returns the fixed modifications for this peptide based on the given
     * modification parameters. Modifications are returned as array of
     * modification names as they appear on the peptide. N-term modifications
     * are at index 0, C-term at index sequence length + 1, and other
     * modifications at amino acid index starting from 1. An error is thrown if
     * attempting to stack modifications.
     *
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     *
     * @return the fixed modifications for this peptide
     */
    public String[] getFixedModifications(ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters) {

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        String[] result = new String[sequence.length() + 2];

        for (String modName : modificationParameters.getFixedModifications()) {

            Modification modification = modificationFactory.getModification(modName);

            int[] sites = ModificationUtils.getPossibleModificationSites(this, modification, sequenceProvider, modificationsSequenceMatchingParameters);

            for (int site : sites) {

                if (result[site] == null) {

                    result[site] = modification.getName();

                } else {

                    throw new IllegalArgumentException("Attempting to put two fixed modifications (" + result[site] + " and " + modification.getName() + " on amino acid " + site + " of peptide " + getSequence() + ".");

                }
            }
        }

        return result;

    }

    /**
     * Sets the variable modifications.
     *
     * @param variableModifications the variable modifications
     */
    public void setVariableModifications(ModificationMatch[] variableModifications) {

        writeDBMode();

        this.variableModifications = variableModifications;

        setMass(-1.0);
        setKey(NO_KEY);
    }

    /**
     * Clears the variable modifications.
     */
    public void clearVariableModifications() {

        writeDBMode();

        variableModifications = null;

        setMass(-1.0);
        setKey(NO_KEY);
    }

    /**
     * Adds a modification match.
     *
     * @param modificationMatch the modification match to add
     */
    public void addVariableModification(ModificationMatch modificationMatch) {

        writeDBMode();

        variableModifications = variableModifications == null
                ? new ModificationMatch[1]
                : Arrays.copyOf(variableModifications, variableModifications.length + 1);

        variableModifications[variableModifications.length - 1] = modificationMatch;

        setMass(-1.0);
        setKey(NO_KEY);

    }

    /**
     * Clears the list of imported variant matches.
     */
    public void clearVariantMatches() {

        writeDBMode();

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

        readDBMode();

        int peptideEnd = peptideStart + sequence.length() - 1;

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

        readDBMode();

        return sequence;
    }

    /**
     * Sets for the sequence.
     *
     * @param sequence the peptide sequence
     */
    public void setSequence(String sequence) {

        writeDBMode();

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

        readDBMode();

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

        readDBMode();

        return digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.enzyme
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

        readDBMode();

        return matchingKey;
    }

    /**
     * Sets the key accounting for sequence matching preferences.
     *
     * @param matchingKey the key accounting for sequence matching preferences
     */
    public void setMatchingKey(long matchingKey) {

        writeDBMode();

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

        readDBMode();

        if (!keySet) {

            String matchingSequence = AminoAcid.getMatchingSequence(sequence, sequenceMatchingPreferences);
            setMatchingKey(getKey(matchingSequence, variableModifications));
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

        readDBMode();

        return key;
    }

    /**
     * Returns the reference key of a peptide. key = SEQUENCE_mod1_mod2 modMass1
     * and modMass2 modification masses ordered alphabetically.
     *
     * @param sequence the sequence of the peptide
     * @param variableModifications list of modification matches
     *
     * @return the key of the peptide
     */
    public static long getKey(String sequence, ModificationMatch[] variableModifications) {

        if (variableModifications == null || variableModifications.length == 0) {

            return ExperimentObject.asLong(sequence);

        }

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        String modificationsKey = Arrays.stream(variableModifications)
                .map(modificationMatch -> modificationMatch.getConfident() || modificationMatch.getInferred()
                ? Arrays.stream(new String[]{
            modificationFactory.getModification(modificationMatch.getModification()).getAmbiguityKey(),
            MODIFICATION_LOCALIZATION_SEPARATOR,
            Integer.toString(modificationMatch.getSite())})
                        .collect(Collectors.joining())
                : modificationFactory.getModification(modificationMatch.getModification()).getAmbiguityKey())
                .sorted()
                .collect(Collectors.joining(MODIFICATION_SEPARATOR));

        String keyAsString = String.join(MODIFICATION_SEPARATOR, sequence, modificationsKey);

        return ExperimentObject.asLong(keyAsString);

    }

    /**
     * Returns the number of variable modifications found with the given mass.
     *
     * @param modificationMass the mass of the modification
     * @return the number of occurrences of this modification
     */
    public int getNVariableModifications(double modificationMass) {

        readDBMode();

        return variableModifications == null ? 0 : (int) Arrays.stream(variableModifications)
                .map(modificationMatch -> ModificationFactory.getInstance().getModification(modificationMatch.getModification()))
                .filter(modification -> modification.getMass() == modificationMass).count();
    }

    /**
     * Returns the number of modifications carried by this peptide.
     *
     * @return the number of modifications carried by this peptide
     */
    public int getNVariableModifications() {

        readDBMode();

        return variableModifications == null ? 0 : variableModifications.length;
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

        readDBMode();

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

        readDBMode();

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

        readDBMode();

        AminoAcidSequence pattern = new AminoAcidSequence(anotherPeptide.getSequence());
        return pattern.matches(getSequence(), sequenceMatchingPreferences);

    }

    /**
     * Indicates whether another peptide has the same variable modifications as
     * this peptide. The localization of the Modification is not accounted for.
     * Modifications are considered equal when of exact same mass, no rounding
     * is conducted. Modifications should be loaded in the Modification factory.
     * Fixed modifications are not inspected.
     *
     * @param anotherPeptide the other peptide
     *
     * @return a boolean indicating whether the other peptide has the same
     * variable modifications as the peptide of interest
     */
    public boolean isSameModificationStatus(Peptide anotherPeptide) {

        readDBMode();

        if (getNVariableModifications() != anotherPeptide.getNVariableModifications()) {
            return false;
        }

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        ModificationMatch[] modificationMatches1 = getVariableModifications();
        Map<Double, Long> masses1 = Arrays.stream(modificationMatches1).collect(Collectors.groupingBy(
                modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass(), Collectors.counting()));

        ModificationMatch[] modificationMatches2 = anotherPeptide.getVariableModifications();
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
     * Indicates whether another peptide has the same variable modifications at
     * the same localization as this peptide. This method comes as a complement
     * of isSameAs, here the localization of all Modifications is taken into
     * account. Modifications are considered equal when of same mass.
     * Modifications should be loaded in the Modification factory. Fixed
     * modifications are not inspected.
     *
     * @param anotherPeptide another peptide
     * @param modifications the Modifications
     *
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide, ArrayList<String> modifications) {

        readDBMode();

        if (getNVariableModifications() != anotherPeptide.getNVariableModifications()) {
            return false;
        }

        HashMap<Double, ArrayList<Integer>> modificationToPositionsMap1 = new HashMap<>();
        HashMap<Double, ArrayList<Integer>> modificationToPositionsMap2 = new HashMap<>();
        ModificationFactory modificationFactory = ModificationFactory.getInstance();
        for (ModificationMatch modificationMatch : getVariableModifications()) {
            String modName = modificationMatch.getModification();
            if (modifications.contains(modName)) {
                double tempMass = modificationFactory.getModification(modName).getMass();
                ArrayList<Integer> sites = modificationToPositionsMap1.get(tempMass);
                if (sites == null) {
                    sites = new ArrayList<>();
                    modificationToPositionsMap1.put(tempMass, sites);
                }
                int position = modificationMatch.getSite();
                sites.add(position);
            }
        }
        for (ModificationMatch modificationMatch : anotherPeptide.getVariableModifications()) {
            String modName = modificationMatch.getModification();
            if (modifications.contains(modName)) {
                double tempMass = modificationFactory.getModification(modName).getMass();
                ArrayList<Integer> sites = modificationToPositionsMap2.get(tempMass);
                if (sites == null) {
                    sites = new ArrayList<>();
                    modificationToPositionsMap2.put(tempMass, sites);
                }
                int position = modificationMatch.getSite();
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
     * Modifications should be loaded in the Modification factory. Fixed
     * modifications are not inspected.
     *
     * @param anotherPeptide another peptide
     *
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide) {

        readDBMode();

        if (getNVariableModifications() != anotherPeptide.getNVariableModifications()) {
            return false;
        }

        ArrayList<String> modifications = new ArrayList<>();
        for (ModificationMatch modificationMatch : getVariableModifications()) {
            String modName = modificationMatch.getModification();
            if (!modifications.contains(modName)) {
                modifications.add(modName);
            }
        }
        for (ModificationMatch modificationMatch : anotherPeptide.getVariableModifications()) {
            String modName = modificationMatch.getModification();
            if (!modifications.contains(modName)) {
                modifications.add(modName);
            }
        }
        return sameModificationsAs(anotherPeptide, modifications);
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
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * Modification tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true the short names are used in the tags
     * @param displayedModifications the modifications to display
     *
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(ModificationParameters modificationProfile, SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationsSequenceMatchingParameters, boolean useHtmlColorCoding,
            boolean includeHtmlStartEndTags, boolean useShortName, HashSet<String> displayedModifications) {

        String[] confidentModificationSites = new String[sequence.length() + 2];
        String[] representativeModificationSites = new String[sequence.length() + 2];
        String[] secondaryModificationSites = new String[sequence.length() + 2];
        String[] fixedModificationSites = new String[sequence.length() + 2];

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        readDBMode();

        if (variableModifications != null) {

            for (ModificationMatch modMatch : variableModifications) {

                String modName = modMatch.getModification();

                if (displayedModifications == null || displayedModifications.contains(modName)) {

                    Modification modification = modificationFactory.getModification(modName);

                    if (modification.getModificationType() == ModificationType.modaa) {

                        int modSite = modMatch.getSite();

                        if (modMatch.getConfident()) {

                            confidentModificationSites[modSite] = modName;

                        } else {

                            representativeModificationSites[modSite] = modName;

                        }

                    }
                }
            }
        }

        for (String modName : modificationProfile.getFixedModifications()) {

            if (displayedModifications == null || displayedModifications.contains(modName)) {

                Modification modification = modificationFactory.getModification(modName);

                if (modification.getModificationType() == ModificationType.modaa) {

                    int[] sites = ModificationUtils.getPossibleModificationSites(this, modification, sequenceProvider, modificationsSequenceMatchingParameters);

                    for (int site : sites) {

                        fixedModificationSites[site] = modName;

                    }
                }
            }
        }

        return PeptideUtils.getTaggedModifiedSequence(this, modificationProfile, confidentModificationSites, representativeModificationSites, secondaryModificationSites,
                fixedModificationSites, useHtmlColorCoding, includeHtmlStartEndTags, useShortName);
    }

    /**
     * Estimates the theoretic mass of the peptide. The previous version is
     * silently overwritten.
     *
     * @param modificationParameters the modifications parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationSequenceMatchingParameters the modifications sequence
     * matching parameters
     */
    public void estimateTheoreticMass(ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationSequenceMatchingParameters) {

        readDBMode();

        double tempMass = StandardMasses.h2o.mass
                + sequence.chars()
                        .mapToDouble(aa -> AminoAcid.getAminoAcid((char) aa).getMonoisotopicMass())
                        .sum();

        if (variableModifications != null) {

            ModificationFactory modificationFactory = ModificationFactory.getInstance();
            tempMass += Arrays.stream(variableModifications)
                    .mapToDouble(modificationMatch -> modificationFactory.getModification(modificationMatch.getModification()).getMass())
                    .sum();

        }

        String[] fixedModifications = getFixedModifications(modificationParameters, sequenceProvider, modificationSequenceMatchingParameters);
        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        tempMass += Arrays.stream(fixedModifications)
                .filter(modName -> modName != null)
                .mapToDouble(modName -> modificationFactory.getModification(modName).getMass())
                .sum();

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

        ModificationMatch[] filteredVariableModifications = variableModifications == null ? null
                : Arrays.stream(variableModifications)
                        .filter(modificationMatch -> !forbiddenModifications.contains(modificationMatch.getModification()))
                        .toArray(ModificationMatch[]::new);

        Peptide noModPeptide = new Peptide(getSequence(), filteredVariableModifications);
        noModPeptide.setProteinMapping(getProteinMapping());

        return noModPeptide;
    }
}
