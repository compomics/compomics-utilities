package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.ModificationProfile;
import com.compomics.util.preferences.SequenceMatchingPreferences;

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
     * The peptide sequence with the modified residues indicated in lower case.
     */
    private String sequenceWithLowerCasePtms;
    /**
     * The peptide mass.
     */
    private Double mass = null;
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
        if (mass == null) {
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
        return modifications;
    }

    /**
     * Sets new modification matches for the peptide.
     *
     * @param modificationMatches the new modification matches
     */
    public void setModificationMatches(ArrayList<ModificationMatch> modificationMatches) {
        this.modifications = modificationMatches;
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
     * Returns the peptide sequence as a String where the modified residues are
     * in lower case.
     *
     * @return the peptide sequence with the modified residues in lowercase
     */
    public String getSequenceWithLowerCasePtms() {

        if (sequenceWithLowerCasePtms != null) {
            return sequenceWithLowerCasePtms;
        } else {

            StringBuilder peptideSequence = new StringBuilder(sequence.length());

            for (int i = 0; i < sequence.length(); i++) {

                boolean modified = false;

                for (int j = 0; j < modifications.size() && !modified; j++) {
                    if (modifications.get(j).getModificationSite() == (i + 1)) {
                        modified = true;
                    }
                }

                if (modified) {
                    peptideSequence.append(sequence.substring(i, i + 1).toLowerCase());
                } else {
                    peptideSequence.append(sequence.charAt(i));
                }
            }

            sequenceWithLowerCasePtms = peptideSequence.toString();

            return sequenceWithLowerCasePtms;
        }
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
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public ArrayList<String> getParentProteins(boolean remap, SequenceMatchingPreferences sequenceMatchingPreferences) throws IOException, ClassNotFoundException, InterruptedException, SQLException {
        if (!remap || parentProteins != null) { // avoid building the tree if not necessary
            return parentProteins;
        }
        return getParentProteins(remap, sequenceMatchingPreferences, SequenceFactory.getInstance().getDefaultProteinTree());
    }

    /**
     * Returns the parent proteins and remaps the peptide to the protein if no
     * protein mapping was set.
     *
     * @param proteinTree the protein tree to use for peptide to protein mapping
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public ArrayList<String> getParentProteins(SequenceMatchingPreferences sequenceMatchingPreferences, ProteinTree proteinTree) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        return getParentProteins(true, sequenceMatchingPreferences, proteinTree);
    }

    /**
     * Returns the parent proteins and remaps the peptide to the protein if no
     * protein mapping was set using the default protein tree of the sequence
     * factory.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public ArrayList<String> getParentProteins(SequenceMatchingPreferences sequenceMatchingPreferences) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        return getParentProteins(true, sequenceMatchingPreferences);
    }

    /**
     * Returns the parent proteins and eventually remaps the peptide to the
     * protein. Note, the maximal share of 'X's in the sequence is set according
     * to the ProteinMatch MaxX field.
     *
     * @param remap boolean indicating whether the peptide sequence should be
     * remapped to the proteins if no protein is found
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param proteinTree the protein tree to use for peptide to protein mapping
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws SQLException if an SQLException occurs
     */
    public ArrayList<String> getParentProteins(boolean remap, SequenceMatchingPreferences sequenceMatchingPreferences,
            ProteinTree proteinTree) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        if (remap && parentProteins == null) {
            HashMap<String, HashMap<String, ArrayList<Integer>>> proteinMapping = proteinTree.getProteinMapping(sequence, sequenceMatchingPreferences);
            parentProteins = new ArrayList<String>();

            for (String peptideSequence : proteinMapping.keySet()) {
                double xShare = ((double) Util.getOccurrence(peptideSequence, 'X')) / sequence.length();
                if (!sequenceMatchingPreferences.hasLimitX() || xShare <= sequenceMatchingPreferences.getLimitX()) {
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
        String matchingSequence = AminoAcid.getMatchingSequence(sequence, sequenceMatchingPreferences);
        return getKey(matchingSequence, modifications);
    }

    /**
     * Returns the reference key of a peptide. index =
     * SEQUENCE_modMass1_modMass2 with modMass1 and modMass2 modification masses
     * ordered alphabetically.
     *
     * Note: the key is not unique for indistinguishable sequences, see
     * getMatchingKey(SequenceMatchingPreferences sequenceMatchingPreferences).
     * Modifications must be loaded in the PTM factory
     *
     * @return the index of a peptide
     */
    public String getKey() {
        return getKey(sequence, modifications);
    }

    /**
     * Returns the reference key of a peptide. key = SEQUENCE_mod1_mod2 modMass1
     * and modMass2 modification masses ordered alphabetically.
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
                    String ptmName = mod.getTheoreticPtm();
                    PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                    if (mod.isConfident() || mod.isInferred()) {
                        tempModifications.add(ptm.getMass() + MODIFICATION_LOCALIZATION_SEPARATOR + mod.getModificationSite());
                    } else {
                        tempModifications.add(ptm.getMass() + "");
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
     * @param modificationMass the mass of the modification
     * @return the number of modifications
     */
    public static int getModificationCount(String peptideKey, Double modificationMass) {
        String modKey = modificationMass + "";
        String test = peptideKey + MODIFICATION_SEPARATOR;
        return test.split(modKey).length - 1;
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
     * @param ptmMass the mass of the modification
     * @return the number of modifications confidently localized
     */
    public static ArrayList<Integer> getNModificationLocalized(String peptideKey, Double ptmMass) {
        String test = peptideKey;
        ArrayList<Integer> result = new ArrayList<Integer>();
        boolean first = true;
        String modKey = ptmMass + "";
        for (String modificationSplit : test.split(MODIFICATION_SEPARATOR)) {
            if (!first) {
                String[] localizationSplit = modificationSplit.split(MODIFICATION_LOCALIZATION_SEPARATOR);
                if (localizationSplit.length == 2) {
                    if (localizationSplit[0].equals(modKey)) {
                        try {
                            result.add(Integer.valueOf(localizationSplit[1]));
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Cannot parse modification localization "
                                    + localizationSplit[1] + " for modification of mass " + ptmMass + " in peptide key " + peptideKey);
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
     * Returns a list of masses of the variable modifications found in the key
     * of a peptide.
     *
     * @param peptideKey the key of a peptide
     *
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
     * Returns a list of proteins where this peptide can be found in the
     * N-terminus. The proteins must be accessible via the sequence factory. If
     * none found, an empty list is returned. Warning: if the parent proteins
     * are not set, they will be set using the default protein tree and the
     * given matching type and mass tolerance
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
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
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public ArrayList<String> isNterm(SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        ArrayList<String> result = new ArrayList<String>();

        if (parentProteins == null) {
            getParentProteins(sequenceMatchingPreferences);
        }

        for (String accession : parentProteins) {
            Protein protein = sequenceFactory.getProtein(accession);
            if (protein.isNTerm(sequence, sequenceMatchingPreferences)) {
                result.add(accession);
            }
        }

        return result;
    }

    /**
     * Returns a list of proteins where this peptide can be found in the
     * C-terminus. The proteins must be accessible via the sequence factory. If
     * none found, an empty list is returned. Warning: if the parent proteins
     * are not set, they will be set using the default protein tree and the
     * given matching type and mass tolerance
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
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
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public ArrayList<String> isCterm(SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        ArrayList<String> result = new ArrayList<String>();

        if (parentProteins == null) {
            getParentProteins(sequenceMatchingPreferences);
        }

        for (String accession : parentProteins) {
            Protein protein = sequenceFactory.getProtein(accession);
            if (protein.isCTerm(sequence, sequenceMatchingPreferences)) {
                result.add(accession);
            }
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
     * @param sequenceMatchingPreferences the sequence matching preferences
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
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public boolean isModifiable(PTM ptm, SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        AminoAcidPattern pattern = ptm.getPattern();
        int patternLength = pattern.length();

        switch (ptm.getType()) {
            case PTM.MODAA:
                int target = pattern.getTarget();
                if (target >= 0 && patternLength - target <= 1) {
                    return pattern.matchesIn(sequence, sequenceMatchingPreferences);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, sequenceMatchingPreferences)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.matchesIn(tempSequence, sequenceMatchingPreferences)) {
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
                return !isCterm(sequenceMatchingPreferences).isEmpty();
            case PTM.MODN:
                return !isNterm(sequenceMatchingPreferences).isEmpty();
            case PTM.MODCAA:
                if (isCterm(sequenceMatchingPreferences).isEmpty()) {
                    return false;
                }
            case PTM.MODCPAA:
                target = pattern.getTarget();
                if (target == patternLength - 1 && sequence.length() >= patternLength) {
                    return pattern.isEnding(sequence, sequenceMatchingPreferences);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, sequenceMatchingPreferences)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.isEnding(tempSequence, sequenceMatchingPreferences)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            case PTM.MODNAA:
                if (isNterm(sequenceMatchingPreferences).isEmpty()) {
                    return false;
                }
            case PTM.MODNPAA:
                target = pattern.getTarget();
                if (target == 0 && sequence.length() >= patternLength) {
                    return pattern.isStarting(sequence, sequenceMatchingPreferences);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, sequenceMatchingPreferences)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.isStarting(tempSequence, sequenceMatchingPreferences)) {
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
     * @param ptmMass the mass of the potential PTM
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param modificationProfile the modification profile of the identification
     *
     * @return a list of potential modification sites
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public ArrayList<Integer> getPotentialModificationSites(Double ptmMass, SequenceMatchingPreferences sequenceMatchingPreferences, ModificationProfile modificationProfile)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        ArrayList<Integer> sites = new ArrayList<Integer>();

        for (String ptmName : modificationProfile.getAllNotFixedModifications()) {
            PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
            if (Math.abs(ptm.getMass() - ptmMass) < sequenceMatchingPreferences.getMs2MzTolerance()) {
                for (int site : getPotentialModificationSites(ptm, sequenceMatchingPreferences)) {
                    if (!sites.contains(site)) {
                        sites.add(site);
                    }
                }
            }
        }

        return sites;
    }

    /**
     * Returns the potential modification sites as an ordered list of string. 1
     * is the first amino acid. An empty list is returned if no possibility was
     * found.
     *
     * @param ptm the PTM considered
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a list of potential modification sites
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public ArrayList<Integer> getPotentialModificationSites(PTM ptm, SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        AminoAcidPattern pattern = ptm.getPattern();
        int patternLength = pattern.length();

        switch (ptm.getType()) {
            case PTM.MODAA:
                int target = pattern.getTarget();
                if (target >= 0 && patternLength - target <= 1) {
                    return pattern.getIndexes(sequence, sequenceMatchingPreferences);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, sequenceMatchingPreferences)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.matchesIn(tempSequence, sequenceMatchingPreferences)) {
                                    for (int tempIndex : pattern.getIndexes(tempSequence, sequenceMatchingPreferences)) {
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
                if (isCterm(sequenceMatchingPreferences).isEmpty()) {
                    return possibleSites;
                }
            case PTM.MODCP:
                possibleSites.add(sequence.length());
                return possibleSites;
            case PTM.MODN:
                if (isNterm(sequenceMatchingPreferences).isEmpty()) {
                    return possibleSites;
                }
            case PTM.MODNP:
                possibleSites.add(1);
                return possibleSites;
            case PTM.MODCAA:
                if (isCterm(sequenceMatchingPreferences).isEmpty()) {
                    return possibleSites;
                }
            case PTM.MODCPAA:
                target = pattern.getTarget();
                if (target == patternLength - 1 && sequence.length() >= patternLength) {
                    if (pattern.isEnding(sequence, sequenceMatchingPreferences)) {
                        possibleSites.add(sequence.length());
                    }
                    return possibleSites;
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    Protein protein;
                    for (String accession : parentProteins) {
                        protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, sequenceMatchingPreferences)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.isEnding(tempSequence, sequenceMatchingPreferences)) {
                                    possibleSites.add(sequence.length());
                                    return possibleSites;
                                }
                            }
                        }
                    }
                    return possibleSites;
                }
            case PTM.MODNAA:
                if (isNterm(sequenceMatchingPreferences).isEmpty()) {
                    return possibleSites;
                }
            case PTM.MODNPAA:
                target = pattern.getTarget();
                if (target == 0 && sequence.length() >= patternLength) {
                    if (pattern.isStarting(sequence, sequenceMatchingPreferences)) {
                        possibleSites.add(1);
                    }
                    return possibleSites;
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    Protein protein;
                    for (String accession : parentProteins) {
                        protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, sequenceMatchingPreferences)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.isStarting(tempSequence, sequenceMatchingPreferences)) {
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
        AminoAcidSequence pattern = new AminoAcidSequence(anotherPeptide.getSequence());
        return pattern.matches(sequence, sequenceMatchingPreferences);
    }

    /**
     * Indicates whether another peptide has the same variable modifications as
     * this peptide. The localization of the PTM is not accounted for.
     * Modifications are considered equal when of same mass. Modifications
     * should be loaded in the PTM factory.
     *
     * @param anotherPeptide the other peptide
     * @return a boolean indicating whether the other peptide has the same
     * variable modifications as the peptide of interest
     */
    public boolean isSameModificationStatus(Peptide anotherPeptide) {
        if (anotherPeptide.getModificationMatches().size() != modifications.size()) {
            return false;
        }

        PTMFactory ptmFactory = PTMFactory.getInstance();
        ArrayList<String> modifications1 = getModificationFamily(getKey());
        HashMap<Double, Integer> masses1 = new HashMap<Double, Integer>();
        for (String modName : modifications1) {
            PTM ptm = ptmFactory.getPTM(modName);
            double tempMass = ptm.getMass();
            Integer occurrence = masses1.get(tempMass);
            if (occurrence == null) {
                masses1.put(tempMass, 1);
            } else {
                masses1.put(tempMass, occurrence + 1);
            }
        }

        ArrayList<String> modifications2 = getModificationFamily(anotherPeptide.getKey());
        HashMap<Double, Integer> masses2 = new HashMap<Double, Integer>();
        for (String modName : modifications2) {
            PTM ptm = ptmFactory.getPTM(modName);
            double tempMass = ptm.getMass();
            Integer occurrence = masses2.get(tempMass);
            if (occurrence == null) {
                masses2.put(tempMass, 1);
            } else {
                masses2.put(tempMass, occurrence + 1);
            }
        }

        if (masses1.size() != masses2.size()) {
            return false;
        }
        for (Double tempMass : masses1.keySet()) {
            Integer occurrence1 = masses1.get(tempMass);
            Integer occurrence2 = masses2.get(tempMass);
            if (occurrence2 == null || occurrence2 != occurrence1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicates whether another peptide has the same modifications at the same
     * localization as this peptide. This method comes as a complement of
     * isSameAs, here the localization of all PTMs is taken into account.
     * Modifications are considered equal when of same mass. Modifications
     * should be loaded in the PTM factory.
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
        HashMap<Double, ArrayList<Integer>> ptmToPositionsMap1 = new HashMap<Double, ArrayList<Integer>>();
        HashMap<Double, ArrayList<Integer>> ptmToPositionsMap2 = new HashMap<Double, ArrayList<Integer>>();
        PTMFactory ptmFactory = PTMFactory.getInstance();
        for (ModificationMatch modificationMatch : modifications) {
            String modName = modificationMatch.getTheoreticPtm();
            if (ptms.contains(modName)) {
                double tempMass = ptmFactory.getPTM(modName).getMass();
                ArrayList<Integer> sites = ptmToPositionsMap1.get(tempMass);
                if (sites == null) {
                    sites = new ArrayList<Integer>();
                    ptmToPositionsMap1.put(tempMass, sites);
                }
                int position = modificationMatch.getModificationSite();
                sites.add(position);
            }
        }
        for (ModificationMatch modificationMatch : anotherPeptide.getModificationMatches()) {
            String modName = modificationMatch.getTheoreticPtm();
            if (ptms.contains(modName)) {
                double tempMass = ptmFactory.getPTM(modName).getMass();
                ArrayList<Integer> sites = ptmToPositionsMap2.get(tempMass);
                if (sites == null) {
                    sites = new ArrayList<Integer>();
                    ptmToPositionsMap2.put(tempMass, sites);
                }
                int position = modificationMatch.getModificationSite();
                sites.add(position);
            }
        }
        for (Double tempMass : ptmToPositionsMap1.keySet()) {
            ArrayList<Integer> sites1 = ptmToPositionsMap1.get(tempMass);
            ArrayList<Integer> sites2 = ptmToPositionsMap2.get(tempMass);
            if (sites2 == null || sites1.size() != sites2.size()) {
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
     * Modifications are considered equal when of same mass. Modifications
     * should be loaded in the PTM factory.
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
        HashMap<Integer, ArrayList<String>> confidentModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> representativeModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> secondaryModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> fixedModificationSites = new HashMap<Integer, ArrayList<String>>();

        for (ModificationMatch modMatch : modifications) {
            String modName = modMatch.getTheoreticPtm();
            int modSite = modMatch.getModificationSite();
            if (modMatch.isVariable()) {
                if (modMatch.isConfident()) {
                    if (!confidentModificationSites.containsKey(modSite)) {
                        confidentModificationSites.put(modSite, new ArrayList<String>());
                    }
                    confidentModificationSites.get(modSite).add(modName);
                } else {
                    if (!representativeModificationSites.containsKey(modSite)) {
                        representativeModificationSites.put(modSite, new ArrayList<String>());
                    }
                    representativeModificationSites.get(modSite).add(modName);
                }
            } else if (!excludeAllFixedPtms) {
                if (!fixedModificationSites.containsKey(modSite)) {
                    fixedModificationSites.put(modSite, new ArrayList<String>());
                }
                fixedModificationSites.get(modSite).add(modName);
            }
        }
        return getTaggedModifiedSequence(modificationProfile, this, confidentModificationSites, representativeModificationSites, secondaryModificationSites,
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
     * PTMFactory.
     *
     * @param modificationProfile the modification profile of the search
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param peptide the peptide to annotate
     * @param confidentModificationSites the confidently localized variable
     * modification sites in a map: aa number &gt; list of modifications (1 is the
     * first AA) (can be null)
     * @param representativeAmbiguousModificationSites the representative site
     * of the ambiguously localized variable modifications in a map: aa number
     * &gt; list of modifications (1 is the first AA) (can be null)
     * @param secondaryAmbiguousModificationSites the secondary sites of the
     * ambiguously localized variable modifications in a map: aa number &gt; list
     * of modifications (1 is the first AA) (can be null)
     * @param fixedModificationSites the fixed modification sites in a map: aa
     * number &gt; list of modifications (1 is the first AA) (can be null)
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(ModificationProfile modificationProfile, Peptide peptide,
            HashMap<Integer, ArrayList<String>> confidentModificationSites, HashMap<Integer, ArrayList<String>> representativeAmbiguousModificationSites,
            HashMap<Integer, ArrayList<String>> secondaryAmbiguousModificationSites, HashMap<Integer, ArrayList<String>> fixedModificationSites,
            boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {

        if (confidentModificationSites == null) {
            confidentModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (representativeAmbiguousModificationSites == null) {
            representativeAmbiguousModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (secondaryAmbiguousModificationSites == null) {
            secondaryAmbiguousModificationSites = new HashMap<Integer, ArrayList<String>>();
        }
        if (fixedModificationSites == null) {
            fixedModificationSites = new HashMap<Integer, ArrayList<String>>();
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
     * first) &gt; list of modification names.
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

        mass = Atom.H.getMonoisotopicMass();

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

        mass += Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();

        PTMFactory ptmFactory = PTMFactory.getInstance();

        for (ModificationMatch ptmMatch : modifications) {
            mass += ptmFactory.getPTM(ptmMatch.getTheoreticPtm()).getMass();
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
        return new AminoAcidPattern(sequence);
    }

    /**
     * Returns the sequence of this peptide as AminoAcidSequence.
     *
     * @return the sequence of this peptide as AminoAcidSequence
     */
    public AminoAcidSequence getSequenceAsAminoAcidSequence() {
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
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public boolean isDecoy(SequenceMatchingPreferences sequenceMatchingPreferences) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        if (parentProteins == null) {
            getParentProteins(sequenceMatchingPreferences);
        }
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
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
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
