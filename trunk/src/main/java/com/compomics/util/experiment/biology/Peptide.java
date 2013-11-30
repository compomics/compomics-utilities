package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.ModificationProfile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class models a peptide.
 *
 * @author Marc Vaudel
 */
public class Peptide extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 5632064601627536034L;
    /**
     * The peptide sequence.
     */
    private String sequence;
    /**
     * The peptide mass.
     */
    private Double mass;
    /**
     * The parent proteins.
     */
    private ArrayList<String> parentProteins = null;
    /**
     * The modifications carried by the peptide.
     */
    private ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();
    /**
     * Separator preceding confident localization of the confident localization
     * of a modification
     */
    public final static String MODIFICATION_LOCALIZATION_SEPARATOR = "-ATAA-";
    /**
     * Separator used to separate modifications in peptide keys
     */
    public final static String MODIFICATION_SEPARATOR = "_";

    /**
     * Constructor for the peptide.
     */
    public Peptide() {
    }

    /**
     * Constructor for the peptide.
     *
     * @param aSequence The peptide sequence
     * @param parentProteins The parent proteins, cannot be null or empty
     * @param modifications The PTM of this peptide
     *
     * @deprecated use peptide without proteins and remap the peptide to the
     * proteins a posteriori instead
     *
     * @throws IllegalArgumentException Thrown if the peptide sequence contains
     * unknown amino acids
     */
    public Peptide(String aSequence, ArrayList<String> parentProteins, ArrayList<ModificationMatch> modifications) throws IllegalArgumentException {
        this(aSequence, modifications);
        setParentProteins(parentProteins);
    }

    /**
     * Constructor for the peptide.
     *
     * @param aSequence The peptide sequence
     * @param modifications The PTM of this peptide
     * @throws IllegalArgumentException Thrown if the peptide sequence contains
     * unknown amino acids
     */
    public Peptide(String aSequence, ArrayList<ModificationMatch> modifications) throws IllegalArgumentException {
        this.sequence = aSequence;
        sequence = sequence.replaceAll("[#*$%&]", "");
        HashMap<String, ArrayList<Integer>> ptmToPositionsMap = new HashMap<String, ArrayList<Integer>>();
        for (ModificationMatch mod : modifications) {
            if (mod.getTheoreticPtm().contains(MODIFICATION_SEPARATOR)) {
                throw new IllegalArgumentException("PTM names containing '" + MODIFICATION_SEPARATOR + "' are not supported. Conflicting name: " + mod.getTheoreticPtm());
            }
            if (mod.getTheoreticPtm().contains(MODIFICATION_LOCALIZATION_SEPARATOR)) {
                throw new IllegalArgumentException("PTM names containing '" + MODIFICATION_LOCALIZATION_SEPARATOR + "' are not supported. Conflicting name: " + mod.getTheoreticPtm());
            }
            String modName = mod.getTheoreticPtm();
            int position = mod.getModificationSite();
            if (!ptmToPositionsMap.containsKey(modName)) {
                ptmToPositionsMap.put(modName, new ArrayList<Integer>());
            }
            ptmToPositionsMap.get(modName).add(position);
            this.modifications.add(mod);
        }
        estimateTheoreticMass();
    }

    /**
     * Constructor for the peptide.
     *
     * @deprecated use the constructor without mass. The mass will be
     * recalculated.
     * @param aSequence The peptide sequence
     * @param mass The peptide mass
     * @param parentProteins The parent proteins, cannot be null or empty
     * @param modifications The PTM of this peptide
     */
    public Peptide(String aSequence, Double mass, ArrayList<String> parentProteins, ArrayList<ModificationMatch> modifications) {
        this.sequence = aSequence;
        sequence = sequence.replaceAll("[#*$%&]", "");
        this.mass = mass;
        HashMap<String, ArrayList<Integer>> ptmToPositionsMap = new HashMap<String, ArrayList<Integer>>();
        for (ModificationMatch mod : modifications) {
            String modName = mod.getTheoreticPtm();
            int position = mod.getModificationSite();
            if (!ptmToPositionsMap.containsKey(modName)) {
                ptmToPositionsMap.put(modName, new ArrayList<Integer>());
            }
            ptmToPositionsMap.get(modName).add(position);
            this.modifications.add(mod);
        }
        setParentProteins(parentProteins);
    }

    /**
     * Getter for the mass.
     *
     * @return the peptide mass
     */
    public Double getMass() {
        return mass;
    }

    /**
     * Getter for the modifications carried by this peptide.
     *
     * @return the modifications matches as found by the search engine
     */
    public ArrayList<ModificationMatch> getModificationMatches() {
        return modifications;
    }

    /**
     * Clears the list of imported modification matches.
     */
    public void clearModificationMatches() {
        modifications.clear();
    }

    /**
     * Adds a modification match.
     *
     * @param modificationMatch the modification match to add
     */
    public void addModificationMatch(ModificationMatch modificationMatch) {
        modifications.add(modificationMatch);
    }

    /**
     * Getter for the sequence.
     *
     * @return the peptide sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Returns the number of missed cleavages using the specified enzyme.
     *
     * @param enzyme the enzyme used
     * @return the amount of missed cleavages
     */
    public int getNMissedCleavages(Enzyme enzyme) {
        return enzyme.getNmissedCleavages(sequence);
    }

    /**
     * Returns the number of missed cleavages using the specified enzyme for the
     * given sequence.
     *
     * @param sequence the peptide sequence
     * @param enzyme the enzyme used
     * @return the amount of missed cleavages
     */
    public static int getNMissedCleavages(String sequence, Enzyme enzyme) {
        int mc = 0;
        for (int aa = 0; aa < sequence.length() - 1; aa++) {
            if (enzyme.getAminoAcidBefore().contains(sequence.charAt(aa))
                    && !enzyme.getRestrictionAfter().contains(sequence.charAt(aa + 1))) {
                mc++;
            }
            if (enzyme.getAminoAcidAfter().contains(sequence.charAt(aa + 1))
                    && !enzyme.getAminoAcidBefore().contains(sequence.charAt(aa))) {
                mc++;
            }
        }
        return mc;
    }

    /**
     * Returns the parent proteins and eventually remaps the peptide to the
     * protein using the default protein tree.
     *
     * @param remap boolean indicating whether the peptide sequence should be
     * remapped to the proteins if no protein is found
     * @param matchingType the desired peptide to protein matching type
     * @param massTolerance the ms2 mass tolerance
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws SQLException
     */
    public ArrayList<String> getParentProteins(boolean remap, AminoAcidPattern.MatchingType matchingType, Double massTolerance) throws IOException, ClassNotFoundException, InterruptedException, SQLException {
        if (!remap || parentProteins != null) { // avoid building the tree if not necessary
            return parentProteins;
        }
        return getParentProteins(remap, matchingType, massTolerance, SequenceFactory.getInstance().getDefaultProteinTree());
    }

    /**
     * Returns the parent proteins and remaps the peptide to the protein if no
     * protein mapping was set.
     *
     * @param matchingType the desired peptide to protein matching type
     * @param massTolerance the ms2 mass tolerance
     * @param proteinTree the protein tree to use for peptide to protein mapping
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws SQLException
     */
    public ArrayList<String> getParentProteins(AminoAcidPattern.MatchingType matchingType, Double massTolerance, ProteinTree proteinTree) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        return getParentProteins(true, matchingType, massTolerance, proteinTree);
    }

    /**
     * Returns the parent proteins and remaps the peptide to the protein if no
     * protein mapping was set using the default protein tree of the sequence
     * factory.
     *
     * @param matchingType the desired peptide to protein matching type
     * @param massTolerance the ms2 mass tolerance
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws SQLException
     */
    public ArrayList<String> getParentProteins(AminoAcidPattern.MatchingType matchingType, Double massTolerance) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        return getParentProteins(true, matchingType, massTolerance);
    }

    /**
     * Returns the parent proteins and eventually remaps the peptide to the
     * protein. Note, the maximal share of 'X's in the sequence is set according
     * to the ProteinMatch MaxX field.
     *
     * @param remap boolean indicating whether the peptide sequence should be
     * remapped to the proteins if no protein is found
     * @param matchingType the desired peptide to protein matching type
     * @param massTolerance the ms2 mass tolerance
     * @param proteinTree the protein tree to use for peptide to protein mapping
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws SQLException
     */
    public ArrayList<String> getParentProteins(boolean remap, AminoAcidPattern.MatchingType matchingType, Double massTolerance, ProteinTree proteinTree) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        if (remap && parentProteins == null) {
            HashMap<String, HashMap<String, ArrayList<Integer>>> proteinMapping = proteinTree.getProteinMapping(sequence, matchingType, massTolerance, true);
            parentProteins = new ArrayList<String>();
            for (String peptideSequence : proteinMapping.keySet()) {
                double xShare = ((double) Util.getOccurrence(peptideSequence, 'X')) / sequence.length();
                if (xShare <= ProteinMatch.maxX) {
                    HashMap<String, ArrayList<Integer>> subMapping = proteinMapping.get(peptideSequence);
                    for (String accession : subMapping.keySet()) {
                        if (!parentProteins.contains(accession)) {
                            parentProteins.add(accession);
                        }
                    }
                }
            }
            Collections.sort(parentProteins);
        }
        return parentProteins;
    }
    
    /**
     * Returns the parent proteins without remapping them. Null if none mapped.
     * 
     * @return an ArrayList containing the parent proteins
     */
    public ArrayList<String> getParentProteinsNoRemapping() {
        return parentProteins;
    }

    /**
     * Sets the parent proteins.
     *
     * @param parentProteins the parent proteins as list, cannot be null or
     * empty
     */
    public void setParentProteins(ArrayList<String> parentProteins) {
        this.parentProteins = parentProteins;
    }
    
    /**
     * Returns a unique key for the peptide when considering the given matching type and mass tolerance. when ambiguity the first amino acid according to AminoAcid.getAminoAcidsList() will be selected. For example the matching key of peptide PEPTLDE_mod1_mod2 is PEPTIDE_mod1_mod2
     * 
     * @param matchingType the amino acid matching type
     * @param massTolerance the mass tolerance
     * 
     * @return a key unique to the given matching type
     */
    public String getMatchingKey(AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        String matchingSequence = AminoAcid.getMatchingSequence(sequence, matchingType, massTolerance);
        return getKey(matchingSequence, modifications);
    }

    /**
     * Returns the reference key of a peptide. index = SEQUENCE_mod1_mod2 with
     * modifications ordered alphabetically.
     *
     * @return the index of a peptide
     */
    public String getKey() {
        ArrayList<String> tempModifications = new ArrayList<String>();
        for (ModificationMatch mod : getModificationMatches()) {
            if (mod.isVariable()) {
                if (mod.getTheoreticPtm() != null) {
                    if (mod.isConfident() || mod.isInferred()) {
                        tempModifications.add(mod.getTheoreticPtm() + MODIFICATION_LOCALIZATION_SEPARATOR + mod.getModificationSite());
                    } else {
                        tempModifications.add(mod.getTheoreticPtm());
                    }
                } else {
                    tempModifications.add("unknown-modification");
                }
            }
        }
        Collections.sort(tempModifications);
        String result = sequence;
        for (String mod : tempModifications) {
            result += MODIFICATION_SEPARATOR + mod;
        }
        return result;
    }

    /**
     * Returns the reference key of a peptide. key = SEQUENCE_mod1_mod2 with
     * modifications ordered alphabetically.
     *
     * @param sequence the sequence of the peptide
     * @param modificationMatches list of modification matches
     * 
     * @return the index of a peptide
     */
    public static String getKey(String sequence, ArrayList<ModificationMatch> modificationMatches) {
        ArrayList<String> tempModifications = new ArrayList<String>();
        for (ModificationMatch mod : modificationMatches) {
            if (mod.isVariable()) {
                if (mod.getTheoreticPtm() != null) {
                    if (mod.isConfident() || mod.isInferred()) {
                        tempModifications.add(mod.getTheoreticPtm() + MODIFICATION_LOCALIZATION_SEPARATOR + mod.getModificationSite());
                    } else {
                        tempModifications.add(mod.getTheoreticPtm());
                    }
                } else {
                    tempModifications.add("unknown-modification");
                }
            }
        }
        Collections.sort(tempModifications);
        String result = sequence;
        for (String mod : tempModifications) {
            result += MODIFICATION_SEPARATOR + mod;
        }
        return result;
    }

    /**
     * Returns a boolean indicating whether the peptide has variable
     * modifications based on its key.
     *
     * @param peptideKey the peptide key
     * @return a boolean indicating whether the peptide has variable
     * modifications
     */
    public static boolean isModified(String peptideKey) {
        return peptideKey.contains(MODIFICATION_SEPARATOR);
    }

    /**
     * Returns a boolean indicating whether the peptide has the given variable
     * modification based on its key.
     *
     * @param peptideKey the peptide key
     * @param modification the name of the modification
     * @return a boolean indicating whether the peptide has variable
     * modifications
     */
    public static boolean isModified(String peptideKey, String modification) {
        return peptideKey.contains(modification);
    }

    /**
     * Returns how many of the given modification was found in the given
     * peptide.
     *
     * @param peptideKey the peptide key
     * @param modification the name of the modification
     * @return the number of modifications
     */
    public static int getModificationCount(String peptideKey, String modification) {
        String test = peptideKey + MODIFICATION_SEPARATOR;
        return test.split(modification).length - 1;
    }
    
    /**
     * Returns the number of variable modifications found with the given mass.
     * 
     * @param modificationMass the mass of the modification
     * @return the number of occurrences of this modification
     */
    public int getNVariableModifications(double modificationMass) {
        int n = 0;
        for (ModificationMatch modificationMatch : modifications) {
            if (modificationMatch.isVariable()) {
                PTM ptm = PTMFactory.getInstance().getPTM(modificationMatch.getTheoreticPtm());
                if (ptm.getMass() == modificationMass) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Returns the list of modifications confidently localized or inferred for
     * the peptide indexed by the given key.
     *
     * @param peptideKey the peptide key
     * @param modification the name of the modification
     * @return the number of modifications confidently localized
     */
    public static ArrayList<Integer> getNModificationLocalized(String peptideKey, String modification) {
        String test = peptideKey;
        ArrayList<Integer> result = new ArrayList<Integer>();
        boolean first = true;
        for (String modificationSplit : test.split(MODIFICATION_SEPARATOR)) {
            if (!first) {
                String[] localizationSplit = modificationSplit.split(MODIFICATION_LOCALIZATION_SEPARATOR);
                if (localizationSplit.length == 2) {
                    if (localizationSplit[0].equals(modification)) {
                        try {
                            result.add(Integer.valueOf(localizationSplit[1]));
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Cannot parse modification localization "
                                    + localizationSplit.toString() + " for modification " + modification + " in peptide key " + peptideKey);
                        }
                    }
                }
            } else {
                first = false;
            }
        }
        return result;
    }

    /**
     * Returns the sequence of the peptide indexed by the given key.
     *
     * @param peptideKey the peptide key
     * @return the corresponding sequence
     */
    public static String getSequence(String peptideKey) {
        int index = peptideKey.indexOf(MODIFICATION_SEPARATOR);
        if (index > 0) {
            return peptideKey.substring(0, peptideKey.indexOf(MODIFICATION_SEPARATOR));
        } else {
            return peptideKey;
        }
    }

    /**
     * Returns a list of names of the variable modifications found in the key of
     * a peptide.
     *
     * @param peptideKey the key of a peptide
     * @return a list of names of the variable modifications found in the key
     */
    public static ArrayList<String> getModificationFamily(String peptideKey) {
        ArrayList<String> result = new ArrayList<String>();
        String[] parsedKey = peptideKey.split(MODIFICATION_SEPARATOR);
        for (int i = 1; i < parsedKey.length; i++) {
            String[] parsedMod = parsedKey[i].split(MODIFICATION_LOCALIZATION_SEPARATOR);
            result.add(parsedMod[0]);
        }
        return result;
    }

    /**
     * Indicates whether the given modification can be found on the peptide. For
     * instance, 'oxidation of M' cannot be found on sequence "PEPTIDE". For the
     * inspection of protein termini and peptide C-terminus the proteins
     * sequences must be accessible from the sequence factory.
     *
     * @param ptm the PTM of interest
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return a boolean indicating whether the given modification can be found
     * on the peptide
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public boolean isModifiable(PTM ptm, AminoAcidPattern.MatchingType matchingType, Double massTolerance) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        AminoAcidPattern pattern = ptm.getPattern();
        int patternLength = pattern.length();

        switch (ptm.getType()) {
            case PTM.MODAA:
                int target = pattern.getTarget();
                if (target >= 0 && patternLength - target <= 1) {
                    return pattern.matches(sequence);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, pattern, patternLength, matchingType, massTolerance)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.matches(tempSequence)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            case PTM.MODCP:
                return true;
            case PTM.MODNP:
                return true;
            case PTM.MODC:
                return !isCterm(pattern, patternLength, matchingType, massTolerance).isEmpty();
            case PTM.MODN:
                return !isNterm(pattern, patternLength, matchingType, massTolerance).isEmpty();
            case PTM.MODCAA:
                if (isCterm(pattern, patternLength, matchingType, massTolerance).isEmpty()) {
                    return false;
                }
            case PTM.MODCPAA:
                target = pattern.getTarget();
                if (target == patternLength - 1 && sequence.length() >= patternLength) {
                    return pattern.isEnding(sequence);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, pattern, patternLength, matchingType, massTolerance)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.isEnding(tempSequence)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            case PTM.MODNAA:
                if (isNterm(pattern, patternLength, matchingType, massTolerance).isEmpty()) {
                    return false;
                }
            case PTM.MODNPAA:
                target = pattern.getTarget();
                if (target == 0 && sequence.length() >= patternLength) {
                    return pattern.isStarting(sequence);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, pattern, patternLength, matchingType, massTolerance)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.isStarting(tempSequence)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            default:
                return false;
        }
    }

    /**
     * Returns the potential modification sites as an ordered list of string. 1
     * is the first amino acid. An empty list is returned if no possibility was
     * found. This method does not account for protein terminal modifications.
     *
     * @param ptm the PTM considered
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return a list of potential modification sites
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public ArrayList<Integer> getPotentialModificationSites(PTM ptm, AminoAcidPattern.MatchingType matchingType, Double massTolerance)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        AminoAcidPattern pattern = ptm.getPattern();
        int patternLength = pattern.length();

        switch (ptm.getType()) {
            case PTM.MODAA:
                int target = pattern.getTarget();
                if (target >= 0 && patternLength - target <= 1) {
                    return pattern.getIndexes(sequence);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, pattern, patternLength, matchingType, massTolerance)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.matches(tempSequence)) {
                                    for (int tempIndex : pattern.getIndexes(tempSequence)) {
                                        Integer sequenceIndex = tempIndex - target;
                                        if (!possibleSites.contains(sequenceIndex)) {
                                            possibleSites.add(tempIndex);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return possibleSites;
            case PTM.MODC:
            case PTM.MODCP:
                possibleSites.add(sequence.length());
                return possibleSites;
            case PTM.MODN:
            case PTM.MODNP:
                possibleSites.add(1);
                return possibleSites;
            case PTM.MODCAA:
            case PTM.MODCPAA:
                target = pattern.getTarget();
                if (target == patternLength - 1 && sequence.length() >= patternLength) {
                    if (pattern.isEnding(sequence)) {
                        possibleSites.add(sequence.length());
                    }
                    return possibleSites;
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    Protein protein;
                    for (String accession : parentProteins) {
                        protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, pattern, patternLength, matchingType, massTolerance)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.isEnding(tempSequence)) {
                                    possibleSites.add(sequence.length());
                                    return possibleSites;
                                }
                            }
                        }
                    }
                    return possibleSites;
                }
            case PTM.MODNAA:
            case PTM.MODNPAA:
                target = pattern.getTarget();
                if (target == 0 && sequence.length() >= patternLength) {
                    if (pattern.isStarting(sequence)) {
                        possibleSites.add(1);
                    }
                    return possibleSites;
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    Protein protein;
                    for (String accession : parentProteins) {
                        protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, pattern, patternLength, matchingType, massTolerance)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.isStarting(tempSequence)) {
                                    possibleSites.add(1);
                                    return possibleSites;
                                }
                            }
                        }
                    }
                    return possibleSites;
                }
        }
        return possibleSites;
    }

    /**
     * Returns the potential modification sites as an ordered list of string. 1
     * is the first aa. an empty list is returned if no possibility was found.
     * This method does not account for protein terminal modifications. Only
     * works if the modification pattern can be fully found in the sequence
     * (single amino acid or terminal patterns smaller than the sequence).
     * Otherwise an IllegalArgumentException will be thrown. Use the non static
     * method then.
     *
     * @param sequence the sequence of the peptide of interest
     * @param ptm the PTM considered
     * @return a list of potential modification sites
     * @throws IllegalArgumentException
     */
    public static ArrayList<Integer> getPotentialModificationSites(String sequence, PTM ptm) throws IllegalArgumentException {

        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        AminoAcidPattern pattern = ptm.getPattern();
        int patternLength = pattern.length();

        switch (ptm.getType()) {
            case PTM.MODAA:
                int target = pattern.getTarget();
                if (target >= 0 && patternLength - target <= 1) {
                    return pattern.getIndexes(sequence);
                } else {
                    throw new IllegalArgumentException("Pattern " + pattern + " cannot be fully comprised in " + sequence);
                }
            case PTM.MODC:
            case PTM.MODCP:
                possibleSites.add(sequence.length());
                return possibleSites;
            case PTM.MODN:
            case PTM.MODNP:
                possibleSites.add(1);
                return possibleSites;
            case PTM.MODCAA:
            case PTM.MODCPAA:
                target = pattern.getTarget();
                if (target == patternLength - 1 && sequence.length() >= patternLength) {
                    if (pattern.isStarting(sequence)) {
                        possibleSites.add(sequence.length());
                    }
                    return possibleSites;
                } else {
                    throw new IllegalArgumentException("Pattern " + pattern + " cannot be fully comprised in " + sequence);
                }
            case PTM.MODNAA:
            case PTM.MODNPAA:
                target = pattern.getTarget();
                if (target == 0 && sequence.length() >= patternLength) {
                    if (pattern.isStarting(sequence)) {
                        possibleSites.add(1);
                    }
                    return possibleSites;
                } else {
                    throw new IllegalArgumentException("Pattern " + pattern + " cannot be fully comprised in " + sequence);
                }
        }

        return possibleSites;
    }

    /**
     * Indicates whether another peptide has the same sequence and modification
     * status without accounting for modification localization.
     *
     * @param anotherPeptide the other peptide to compare to this instance
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     * 
     * @return a boolean indicating whether the other peptide has the same
     * sequence and modification status.
     */
    public boolean isSameSequenceAndModificationStatus(Peptide anotherPeptide, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        return isSameSequence(anotherPeptide, matchingType, massTolerance) && isSameModificationStatus(anotherPeptide);
    }

    /**
     * Returns a boolean indicating whether another peptide has the same
     * sequence as the given peptide
     *
     * @param anotherPeptide the other peptide to compare
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     * 
     * @return a boolean indicating whether the other peptide has the same
     * sequence
     */
    public boolean isSameSequence(Peptide anotherPeptide, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        AminoAcidPattern pattern = new AminoAcidPattern(anotherPeptide.getSequence());
        return pattern.length() == sequence.length() && pattern.matches(sequence, matchingType, massTolerance);
    }

    /**
     * Indicates whether another peptide has the same variable modifications as
     * this peptide. The localization of the PTM is not accounted for.
     *
     * @param anotherPeptide the other peptide
     * @return a boolean indicating whether the other peptide has the same
     * variable modifications as the peptide of interest
     */
    public boolean isSameModificationStatus(Peptide anotherPeptide) {
        if (anotherPeptide.getModificationMatches().size() != modifications.size()) {
            return false;
        }
        ArrayList<String> modifications1 = getModificationFamily(getKey());
        Collections.sort(modifications1);
        ArrayList<String> modifications2 = getModificationFamily(anotherPeptide.getKey());
        Collections.sort(modifications2);
        for (int i = 0; i < modifications1.size(); i++) {
            if (!modifications1.get(i).equals(modifications2.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indicates whether another peptide has the same modifications at the same
     * localization as this peptide. This method comes as a complement of
     * isSameAs, here the localization of all PTMs is taken into account.
     *
     * @param anotherPeptide another peptide
     * @param ptms the PTMs
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide, ArrayList<String> ptms) {
        if (anotherPeptide.getModificationMatches().size() != modifications.size()) {
            return false;
        }
        HashMap<String, ArrayList<Integer>> ptmToPositionsMap1 = new HashMap<String, ArrayList<Integer>>();
        HashMap<String, ArrayList<Integer>> ptmToPositionsMap2 = new HashMap<String, ArrayList<Integer>>();
        for (ModificationMatch modificationMatch : modifications) {
            String modName = modificationMatch.getTheoreticPtm();
            if (ptms.contains(modName)) {
                if (!ptmToPositionsMap1.containsKey(modName)) {
                    ptmToPositionsMap1.put(modName, new ArrayList<Integer>());
                }
                int position = modificationMatch.getModificationSite();
                ptmToPositionsMap1.get(modName).add(position);
            }
        }
        for (ModificationMatch modificationMatch : anotherPeptide.getModificationMatches()) {
            String modName = modificationMatch.getTheoreticPtm();
            if (ptms.contains(modName)) {
                if (!ptmToPositionsMap2.containsKey(modName)) {
                    ptmToPositionsMap2.put(modName, new ArrayList<Integer>());
                }
                int position = modificationMatch.getModificationSite();
                ptmToPositionsMap2.get(modName).add(position);
            }
        }
        for (String modName : ptmToPositionsMap1.keySet()) {
            if (!ptmToPositionsMap2.containsKey(modName)) {
                return false;
            }
            ArrayList<Integer> sites1 = ptmToPositionsMap1.get(modName);
            ArrayList<Integer> sites2 = ptmToPositionsMap2.get(modName);
            if (sites1.size() != sites2.size()) {
                return false;
            }
            Collections.sort(sites1);
            Collections.sort(sites2);
            for (int i = 0; i < sites1.size(); i++) {
                if (sites1.get(i) != sites2.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Indicates whether another peptide has the same modifications at the same
     * localization as this peptide. This method comes as a complement of
     * isSameAs, here the localization of all PTMs is taken into account.
     *
     * @param anotherPeptide another peptide
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide) {
        ArrayList<String> ptms = new ArrayList<String>();
        for (ModificationMatch modificationMatch : modifications) {
            String modName = modificationMatch.getTheoreticPtm();
            if (!ptms.contains(modName)) {
                ptms.add(modName);
            }
        }
        for (ModificationMatch modificationMatch : anotherPeptide.getModificationMatches()) {
            String modName = modificationMatch.getTheoreticPtm();
            if (!ptms.contains(modName)) {
                ptms.add(modName);
            }
        }
        return sameModificationsAs(anotherPeptide, ptms);
    }

    /**
     * Returns the N-terminal of the peptide as a String. Returns "NH2" if the
     * terminal is not modified, otherwise returns the name of the modification.
     * /!\ this method will work only if the PTM found in the peptide are in the
     * PTMFactory.
     *
     * @return the N-terminal of the peptide as a String, e.g., "NH2"
     */
    public String getNTerminal() {

        String nTerm = "NH2";

        PTMFactory ptmFactory = PTMFactory.getInstance();

        for (int i = 0; i < modifications.size(); i++) {
            if (modifications.get(i).getModificationSite() == 1) { // ! (MODAA && MODMAX)
                PTM ptm = ptmFactory.getPTM(modifications.get(i).getTheoreticPtm());
                if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                    nTerm = ptmFactory.getShortName(modifications.get(i).getTheoreticPtm());
                }
            }
        }

        nTerm = nTerm.replaceAll("-", " ");
        return nTerm;
    }

    /**
     * Returns the C-terminal of the peptide as a String. Returns "COOH" if the
     * terminal is not modified, otherwise returns the name of the modification.
     * /!\ This method will work only if the PTM found in the peptide are in the
     * PTMFactory.
     *
     * @return the C-terminal of the peptide as a String, e.g., "COOH"
     */
    public String getCTerminal() {

        String cTerm = "COOH";
        PTMFactory ptmFactory = PTMFactory.getInstance();

        for (int i = 0; i < modifications.size(); i++) {
            if (modifications.get(i).getModificationSite() == sequence.length()) {
                PTM ptm = ptmFactory.getPTM(modifications.get(i).getTheoreticPtm());
                if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                    cTerm = ptmFactory.getShortName(modifications.get(i).getTheoreticPtm());
                }
            }
        }

        cTerm = cTerm.replaceAll("-", " ");
        return cTerm;
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * this method will work only if the PTM found in the peptide are in the
     * PTMFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true the short names are used in the tags
     * @param excludeAllFixedPtms if true, all fixed PTMs are excluded
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(ModificationProfile modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean excludeAllFixedPtms) {
        HashMap<Integer, ArrayList<String>> mainModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> secondaryModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> fixedModificationSites = new HashMap<Integer, ArrayList<String>>();

        for (ModificationMatch modMatch : modifications) {
            String modName = modMatch.getTheoreticPtm();
            int modSite = modMatch.getModificationSite();
            if (modMatch.isVariable()) {
                if (modMatch.isConfident()) {
                    if (!mainModificationSites.containsKey(modSite)) {
                        mainModificationSites.put(modSite, new ArrayList<String>());
                    }
                    mainModificationSites.get(modSite).add(modName);
                } else {
                    if (!secondaryModificationSites.containsKey(modSite)) {
                        secondaryModificationSites.put(modSite, new ArrayList<String>());
                    }
                    secondaryModificationSites.get(modSite).add(modName);
                }
            } else if (!excludeAllFixedPtms) {
                if (!fixedModificationSites.containsKey(modSite)) {
                    fixedModificationSites.put(modSite, new ArrayList<String>());
                }
                fixedModificationSites.get(modSite).add(modName);
            }
        }
        return getTaggedModifiedSequence(modificationProfile, this, mainModificationSites, secondaryModificationSites,
                fixedModificationSites, useHtmlColorCoding, includeHtmlStartEndTags, useShortName);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * this method will work only if the PTM found in the peptide are in the
     * PTMFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true the short names are used in the tags
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(ModificationProfile modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {
        return getTaggedModifiedSequence(modificationProfile, useHtmlColorCoding, includeHtmlStartEndTags, useShortName, false);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * This method will work only if the PTM found in the peptide are in the
     * PTMFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param peptide the peptide to annotate
     * @param mainModificationSites the main variable modification sites in a
     * map: aa number -> list of modifications (1 is the first AA) (can be null)
     * @param secondaryModificationSites the secondary variable modification
     * sites in a map: aa number -> list of modifications (1 is the first AA)
     * (can be null)
     * @param fixedModificationSites the fixed modification sites in a map: aa
     * number -> list of modifications (1 is the first AA) (can be null)
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(ModificationProfile modificationProfile, Peptide peptide,
            HashMap<Integer, ArrayList<String>> mainModificationSites, HashMap<Integer, ArrayList<String>> secondaryModificationSites,
            HashMap<Integer, ArrayList<String>> fixedModificationSites, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags,
            boolean useShortName) {

        if (mainModificationSites == null) {
            mainModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (secondaryModificationSites == null) {
            secondaryModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (fixedModificationSites == null) {
            fixedModificationSites = new HashMap<Integer, ArrayList<String>>();
        }

        String modifiedSequence = "";

        if (useHtmlColorCoding && includeHtmlStartEndTags) {
            modifiedSequence += "<html>";
        }

        modifiedSequence += peptide.getNTerminal() + "-";

        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern(peptide.sequence);
        modifiedSequence += AminoAcidPattern.getTaggedModifiedSequence(modificationProfile, aminoAcidPattern, mainModificationSites, secondaryModificationSites, fixedModificationSites, useHtmlColorCoding, useShortName);

        modifiedSequence += "-" + peptide.getCTerminal();

        if (useHtmlColorCoding && includeHtmlStartEndTags) {
            modifiedSequence += "</html>";
        }

        return modifiedSequence;
    }

    /**
     * Returns the indexes of the residues in the peptide that contain at least
     * one variable modification.
     *
     * @return the indexes of the modified residues
     */
    public ArrayList<Integer> getModifiedIndexes() {
        return getModifiedIndexes(true);
    }

    /**
     * Returns the indexes of the residues in the peptide that contain at least
     * one modification.
     *
     * @param excludeFixed exclude fixed PTMs
     * @return the indexes of the modified residues
     */
    public ArrayList<Integer> getModifiedIndexes(boolean excludeFixed) {

        ArrayList<Integer> modifiedResidues = new ArrayList<Integer>();
        PTMFactory ptmFactory = PTMFactory.getInstance();

        for (int i = 0; i < sequence.length(); i++) {
            for (int j = 0; j < modifications.size(); j++) {
                PTM ptm = ptmFactory.getPTM(modifications.get(j).getTheoreticPtm());
                if (ptm.getType() == PTM.MODAA && (modifications.get(j).isVariable() || !excludeFixed)) {
                    if (modifications.get(j).getModificationSite() == (i + 1)) {
                        modifiedResidues.add(i + 1);
                    }
                }
            }
        }

        return modifiedResidues;
    }

    /**
     * Returns an indexed map of all fixed modifications amino acid, (1 is the
     * first) -> list of modification names.
     *
     * @return an indexed map of all fixed modifications amino acid
     */
    public HashMap<Integer, ArrayList<String>> getIndexedFixedModifications() {
        HashMap<Integer, ArrayList<String>> result = new HashMap<Integer, ArrayList<String>>();
        for (ModificationMatch modificationMatch : modifications) {
            if (!modificationMatch.isVariable()) {
                int aa = modificationMatch.getModificationSite();
                if (!result.containsKey(aa)) {
                    result.put(aa, new ArrayList<String>());
                }
                result.get(aa).add(modificationMatch.getTheoreticPtm());
            }
        }
        return result;
    }

    /**
     * Estimates the theoretic mass of the peptide. The previous version is
     * silently overwritten.
     *
     * @throws IllegalArgumentException if the peptide sequence contains unknown
     * amino acids
     */
    public void estimateTheoreticMass() throws IllegalArgumentException {

        mass = Atom.H.mass;

        for (int aa = 0; aa < sequence.length(); aa++) {
            try {
                AminoAcid currentAA = AminoAcid.getAminoAcid(sequence.charAt(aa));

                if (currentAA != null) {
                    mass += currentAA.monoisotopicMass;
                } else {
                    System.out.println("Unknown amino acid: " + sequence.charAt(aa) + "!");
                }
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Unknown amino acid: " + sequence.charAt(aa) + "!");
            }
        }

        mass += Atom.H.mass + Atom.O.mass;

        PTMFactory ptmFactory = PTMFactory.getInstance();

        for (ModificationMatch ptmMatch : modifications) {
            mass += ptmFactory.getPTM(ptmMatch.getTheoreticPtm()).getMass();
        }
    }

    /**
     * Returns a list of proteins where this peptide can be found in the
     * N-terminus. The proteins must be accessible via the sequence factory. If
     * none found, an empty list is returned.
     *
     * @param pattern the amino acid pattern
     * @param patternLength the pattern length
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return a list of proteins where this peptide can be found in the
     * N-terminus
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading the protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading the protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public ArrayList<String> isNterm(AminoAcidPattern pattern, int patternLength, AminoAcidPattern.MatchingType matchingType, Double massTolerance) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        ArrayList<String> result = new ArrayList<String>();
        for (String accession : parentProteins) {
            Protein protein = sequenceFactory.getProtein(accession);
            if (protein.isNTerm(sequence, pattern, patternLength, matchingType, massTolerance)) {
                result.add(accession);
            }
        }
        return result;
    }

    /**
     * Returns a list of proteins where this peptide can be found in the
     * C-terminus. The proteins must be accessible via the sequence factory. If
     * none found, an empty list is returned.
     *
     * @param pattern the amino acid pattern
     * @param patternLength the pattern length
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return a list of proteins where this peptide can be found in the
     * C-terminus
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public ArrayList<String> isCterm(AminoAcidPattern pattern, int patternLength, AminoAcidPattern.MatchingType matchingType, Double massTolerance) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        ArrayList<String> result = new ArrayList<String>();
        for (String accession : parentProteins) {
            Protein protein = sequenceFactory.getProtein(accession);
            if (protein.isCTerm(sequence, pattern, patternLength, matchingType, massTolerance)) {
                result.add(accession);
            }
        }
        return result;
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
        return new AminoAcidPattern(sequence);
    }

    /**
     * Indicates whether a peptide can be derived from a decoy protein.
     *
     * @return whether a peptide can be derived from a decoy protein
     */
    public boolean isDecoy() {
        for (String accession : parentProteins) {
            if (SequenceFactory.getInstance().isDecoyAccession(accession)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a version of the peptide which does not contain the inspected
     * PTMs.
     *
     * @param peptide the original peptide
     * @param ptms list of inspected PTMs
     *
     * @return a not modified version of the peptide
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws SQLException
     */
    public static Peptide getNoModPeptide(Peptide peptide, ArrayList<PTM> ptms) throws IOException, SQLException, ClassNotFoundException, InterruptedException {

        Peptide noModPeptide = new Peptide(peptide.getSequence(), new ArrayList<ModificationMatch>());
            noModPeptide.setParentProteins(peptide.getParentProteinsNoRemapping());

        for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
            boolean found = false;
            for (PTM ptm : ptms) {
                if (modificationMatch.getTheoreticPtm().equals(ptm.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                noModPeptide.addModificationMatch(modificationMatch);
            }
        }

        return noModPeptide;
    }
}
