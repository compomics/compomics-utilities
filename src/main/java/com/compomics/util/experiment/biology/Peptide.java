package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.variants.Variant;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.VariantMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.preferences.SequenceMatchingPreferences;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class models a peptide.
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
    private ArrayList<ModificationMatch> modifications = null;
    /**
     * The variants observed when mapping this peptide to the database.
     */
    private ArrayList<VariantMatch> variants = null;
    /**
     * The variants in a map indexed by protein.
     */
    private HashMap<String, HashMap<Integer, ArrayList<Variant>>> variantsMap = null;
    /**
     * Separator preceding confident localization of the confident localization
     * of a modification.
     */
    public final static String MODIFICATION_LOCALIZATION_SEPARATOR = "-ATAA-";
    /**
     * Separator used to separate modifications in peptide keys.
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
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modifications the PTM of this peptide
     * @throws IllegalArgumentException if the peptide sequence contains unknown
     * amino acids
     */
    public Peptide(String aSequence, ArrayList<ModificationMatch> modifications) throws IllegalArgumentException {
        this.sequence = aSequence;
        sequence = sequence.replaceAll("[#*$%&]", "");
        if (modifications != null) {
            for (ModificationMatch mod : modifications) {
                if (mod.getTheoreticPtm().contains(MODIFICATION_SEPARATOR)) {
                    throw new IllegalArgumentException("PTM names containing '" + MODIFICATION_SEPARATOR + "' are not supported. Conflicting name: " + mod.getTheoreticPtm());
                }
                if (mod.getTheoreticPtm().contains(MODIFICATION_LOCALIZATION_SEPARATOR)) {
                    throw new IllegalArgumentException("PTM names containing '" + MODIFICATION_LOCALIZATION_SEPARATOR + "' are not supported. Conflicting name: " + mod.getTheoreticPtm());
                }
            }
            this.modifications = new ArrayList<ModificationMatch>(modifications);
        }
    }

    /**
     * Constructor for the peptide.
     *
     * @param aSequence the peptide sequence, assumed to be in upper case only
     * @param modifications the PTM of this peptide
     * @param variants the variants compared to the database
     *
     * @throws IllegalArgumentException if the peptide sequence contains unknown
     * amino acids
     */
    public Peptide(String aSequence, ArrayList<ModificationMatch> modifications, ArrayList<VariantMatch> variants) throws IllegalArgumentException {
        this.sequence = aSequence;
        sequence = sequence.replaceAll("[#*$%&]", "");
        if (modifications != null) {
            for (ModificationMatch mod : modifications) {
                if (mod.getTheoreticPtm().contains(MODIFICATION_SEPARATOR)) {
                    throw new IllegalArgumentException("PTM names containing '" + MODIFICATION_SEPARATOR + "' are not supported. Conflicting name: " + mod.getTheoreticPtm());
                }
                if (mod.getTheoreticPtm().contains(MODIFICATION_LOCALIZATION_SEPARATOR)) {
                    throw new IllegalArgumentException("PTM names containing '" + MODIFICATION_LOCALIZATION_SEPARATOR + "' are not supported. Conflicting name: " + mod.getTheoreticPtm());
                }
            }
            this.modifications = new ArrayList<ModificationMatch>(modifications);
            this.variants = new ArrayList<VariantMatch>(variants);
        }
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
        mass = null;
    }

    /**
     * Clears the list of imported modification matches.
     */
    public void clearModificationMatches() {
        modifications.clear();
        mass = null;
    }

    /**
     * Adds a modification match.
     *
     * @param modificationMatch the modification match to add
     */
    public void addModificationMatch(ModificationMatch modificationMatch) {
        if (modifications == null) {
            modifications = new ArrayList<ModificationMatch>(1);
        }
        modifications.add(modificationMatch);
        mass = null;
    }

    /**
     * Getter for the variants carried by this peptide. Null if not set.
     *
     * @return the variants matches as found by the search engine
     */
    public ArrayList<VariantMatch> getVariantMatches() {
        return variants;
    }

    /**
     * Sets new variants for the peptide.
     *
     * @param variants the new variant matches
     */
    public void setVariantMatches(ArrayList<VariantMatch> variants) {
        this.variants = variants;
    }

    /**
     * Clears the list of imported variant matches.
     */
    public void clearVariantMatches() {
        if (variants != null) {
            variants.clear();
            variantsMap = null;
        }
    }

    /**
     * Adds a variant match.
     *
     * @param variantMatch the variant match to add
     */
    public void addVariantMatch(VariantMatch variantMatch) {
        if (variants == null) {
            variants = new ArrayList<VariantMatch>(1);
        }
        variants.add(variantMatch);
        variantsMap = null;
    }

    /**
     * Adds variant matches.
     *
     * @param variantMatch the variant match to add
     */
    public void addVariantMatches(Collection<VariantMatch> variantMatch) {
        if (variants == null) {
            variants = new ArrayList<VariantMatch>(variantMatch != null ? variantMatch.size() : 0);
        }
        if (variantMatch != null) {
            variants.addAll(variantMatch);
        }
        variantsMap = null;
    }

    /**
     * Returns the variants in a map indexed by protein accession and index. The
     * map is computed from the list of variants and saved in cache.
     *
     * @return the variants in a map
     */
    public HashMap<String, HashMap<Integer, ArrayList<Variant>>> getVariantsMap() {
        if (variantsMap == null) {
            variantsMap = new HashMap<String, HashMap<Integer, ArrayList<Variant>>>(variants.size());
            for (VariantMatch variantMatch : variants) {
                String proteinAccession = variantMatch.getProteinAccession();
                HashMap<Integer, ArrayList<Variant>> proteinVariants = variantsMap.get(proteinAccession);
                if (proteinVariants == null) {
                    proteinVariants = new HashMap<Integer, ArrayList<Variant>>(2);
                    variantsMap.put(proteinAccession, proteinVariants);
                }
                int site = variantMatch.getSite();
                ArrayList<Variant> variantsAtSite = proteinVariants.get(site);
                if (variantsAtSite == null) {
                    variantsAtSite = new ArrayList<Variant>(1);
                    proteinVariants.put(site, variantsAtSite);
                }
                variantsAtSite.add(variantMatch.getVariant());
            }
        }
        return variantsMap;
    }

    /**
     * Clears the map saved in cache.
     */
    public void clearVariantsMap() {
        variantsMap = null;
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
                if (modifications != null) {
                    for (int j = 0; j < modifications.size() && !modified; j++) {
                        if (modifications.get(j).getModificationSite() == (i + 1)) {
                            modified = true;
                        }
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
     *
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
     * Returns the parent proteins and remaps the peptide to the protein in the
     * sequence factory if no protein mapping was set using the default mapper
     * of the sequence factory.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with an SQL database.
     */
    public ArrayList<String> getParentProteins(SequenceMatchingPreferences sequenceMatchingPreferences) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        return getParentProteins(sequenceMatchingPreferences, true);
    }

    /**
     * Returns the parent proteins and eventually remaps the peptide to the
     * protein using the default protein tree.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param remap boolean indicating whether the peptide sequence should be
     * remapped to the proteins if no protein is found
     *
     * @return the proteins mapping this peptide
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with an SQL database.
     */
    public ArrayList<String> getParentProteins(SequenceMatchingPreferences sequenceMatchingPreferences, boolean remap) throws IOException, ClassNotFoundException, InterruptedException, SQLException {
        if (!remap || parentProteins != null) { // avoid building the index if not necessary
            return parentProteins;
        }

        PeptideMapper peptideMapper = SequenceFactory.getInstance().getDefaultPeptideMapper();

        if (peptideMapper == null) {
            throw new IllegalArgumentException("Index not created for peptide to protein mapping.");
        }
        return getParentProteins(sequenceMatchingPreferences, peptideMapper);
    }

    /**
     * Returns the parent proteins and remaps the peptide to the protein if no
     * protein mapping was set.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param peptideMapper the peptide mapper to use for peptide to protein
     * mapping
     *
     * @return the proteins where this peptide can be mapped
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with an SQL database.
     */
    public ArrayList<String> getParentProteins(SequenceMatchingPreferences sequenceMatchingPreferences, PeptideMapper peptideMapper) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        if (parentProteins == null) {
            mapParentProteins(sequenceMatchingPreferences, peptideMapper);
        }

        return parentProteins;
    }

    public synchronized void mapParentProteins(SequenceMatchingPreferences sequenceMatchingPreferences, PeptideMapper peptideMapper) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        if (parentProteins == null) {
            ArrayList<PeptideProteinMapping> proteinMapping = peptideMapper.getProteinMapping(sequence, sequenceMatchingPreferences);
            HashSet<String> accessionsFound = new HashSet<String>(2);
            for (PeptideProteinMapping peptideProteinMapping : proteinMapping) {
                accessionsFound.add(peptideProteinMapping.getProteinAccession());
            }
            parentProteins = new ArrayList<String>(accessionsFound);
            Collections.sort(parentProteins);
        }
    }

    /**
     * Saves the peptide protein mapping in the parentProteins list.
     *
     * @param proteinMapping the protein mapping for this peptide
     * @param overwrite boolean indicating whether previous mapping should be
     * overwritten
     * @param sequenceMatchingPreferences the sequence matching preferences
     */
    private synchronized void saveProteins(HashMap<String, HashMap<String, ArrayList<Integer>>> proteinMapping, boolean overwrite, SequenceMatchingPreferences sequenceMatchingPreferences) {
        if (overwrite || parentProteins == null) {

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
     * Modifications must be loaded in the PTM factory.
     *
     * @return the key of the peptide
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
     * @return the key of the peptide
     */
    public static String getKey(String sequence, ArrayList<ModificationMatch> modificationMatches) {
        if (modificationMatches == null) {
            return sequence;
        }
        StringBuilder result = new StringBuilder(sequence);
        ArrayList<String> tempModifications = new ArrayList<String>(modificationMatches.size());
        for (ModificationMatch mod : modificationMatches) {
            if (mod.isVariable()) {
                String ptmName = mod.getTheoreticPtm();
                if (ptmName != null) {
                    PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                    if (mod.isConfident() || mod.isInferred()) {
                        StringBuilder tempModKey = new StringBuilder();
                        tempModKey.append(ptm.getMass()).append(MODIFICATION_LOCALIZATION_SEPARATOR).append(mod.getModificationSite());
                        tempModifications.add(tempModKey.toString());
                    } else {
                        tempModifications.add(ptm.getMass() + "");
                    }
                } else {
                    tempModifications.add("unknown-modification");
                }
            }
        }
        Collections.sort(tempModifications);
        for (String mod : tempModifications) {
            result.append(MODIFICATION_SEPARATOR).append(mod);
        }
        return result.toString();
    }

    /**
     * Indicates whether a peptide carries modifications.
     *
     * @return a boolean indicating whether a peptide carries modifications
     */
    public boolean isModified() {
        return modifications != null && !modifications.isEmpty();
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
     * @param modificationMass the mass of the modification
     *
     * @return a boolean indicating whether the peptide has variable
     * modifications
     */
    public static boolean isModified(String peptideKey, Double modificationMass) {
        return peptideKey.contains(modificationMass.toString());
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
        if (modifications != null) {
            for (ModificationMatch modificationMatch : modifications) {
                if (modificationMatch.isVariable()) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationMatch.getTheoreticPtm());
                    if (ptm.getMass() == modificationMass) {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    /**
     * Returns the number of modifications carried by this peptide.
     *
     * @return the number of modifications carried by this peptide
     */
    public int getNModifications() {
        if (modifications != null) {
            return modifications.size();
        } else {
            return 0;
        }
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
     * inspection of protein termini and peptide terminus the proteins sequences
     * must be accessible from the sequence factory.
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

        switch (ptm.getType()) {
            case PTM.MODAA:
                int patternLength = pattern.length();
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
                patternLength = pattern.length();
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
                patternLength = pattern.length();
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
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
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
    public ArrayList<Integer> getPotentialModificationSites(Double ptmMass, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences,
            PtmSettings modificationProfile) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        ArrayList<Integer> sites = new ArrayList<Integer>();

        for (String ptmName : modificationProfile.getAllNotFixedModifications()) {
            PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
            if (ptm.getMass() == ptmMass) { //@TODO: use a mass tolerance
                for (int site : getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences)) {
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
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
     *
     * @return a list of potential modification sites
     *
     * @throws IOException exception thrown whenever an error occurred while
     * interacting with a file while mapping potential modification sites
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while mapping potential modification sites
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing an object from the ProteinTree
     * @throws SQLException exception thrown whenever an error occurred while
     * interacting with the ProteinTree
     */
    public ArrayList<Integer> getPotentialModificationSites(PTM ptm, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        ArrayList<Integer> possibleSites = new ArrayList<Integer>(1);

        switch (ptm.getType()) {
            case PTM.MODAA:
                AminoAcidPattern pattern = ptm.getPattern();
                int patternLength = pattern.length();
                int target = pattern.getTarget();
                if (target >= 0 && patternLength - target <= 1) {
                    return pattern.getIndexes(sequence, ptmSequenceMatchingPreferences);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence, sequenceMatchingPreferences)) {
                            int beginIndex = index - target - 1;
                            int endIndex = index + sequence.length() - 2 + patternLength - target;
                            if (endIndex < protein.getLength()) {
                                String tempSequence = protein.getSequence().substring(beginIndex, endIndex);
                                if (pattern.matchesIn(tempSequence, ptmSequenceMatchingPreferences)) {
                                    for (int tempIndex : pattern.getIndexes(tempSequence, ptmSequenceMatchingPreferences)) {
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
                pattern = ptm.getPattern();
                patternLength = pattern.length();
                target = pattern.getTarget();
                if (target == patternLength - 1 && sequence.length() >= patternLength) {
                    if (pattern.isEnding(sequence, ptmSequenceMatchingPreferences)) {
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
                                if (pattern.isEnding(tempSequence, ptmSequenceMatchingPreferences)) {
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
                pattern = ptm.getPattern();
                patternLength = pattern.length();
                target = pattern.getTarget();
                if (target == 0 && sequence.length() >= patternLength) {
                    if (pattern.isStarting(sequence, ptmSequenceMatchingPreferences)) {
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
                                if (pattern.isStarting(tempSequence, ptmSequenceMatchingPreferences)) {
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

        if (!isModified() && !anotherPeptide.isModified()) {
            return true;
        }

        if (getNModifications() != anotherPeptide.getNModifications()) {
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
            if (occurrence2 == null || occurrence2.intValue() != occurrence1) {
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

        if (!isModified() && !anotherPeptide.isModified()) {
            return true;
        }

        if (getNModifications() != anotherPeptide.getNModifications()) {
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
     * isSameAs, here the localization of all PTMs is taken into account.
     * Modifications are considered equal when of same mass. Modifications
     * should be loaded in the PTM factory.
     *
     * @param anotherPeptide another peptide
     * @return true if the other peptide has the same positions at the same
     * location as the considered peptide
     */
    public boolean sameModificationsAs(Peptide anotherPeptide) {

        if (!isModified() && !anotherPeptide.isModified()) {
            return true;
        }

        if (getNModifications() != anotherPeptide.getNModifications()) {
            return false;
        }

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

        if (modifications != null) {
            for (ModificationMatch modificationMatch : modifications) {
                if (modificationMatch.getModificationSite() == 1) {
                    PTM ptm = ptmFactory.getPTM(modificationMatch.getTheoreticPtm());
                    if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                        nTerm = ptm.getShortName();
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
     * /!\ This method will work only if the PTM found in the peptide are in the
     * PTMFactory.
     *
     * @return the C-terminal of the peptide as a String, e.g., "COOH"
     */
    public String getCTerminal() {

        String cTerm = "COOH";
        PTMFactory ptmFactory = PTMFactory.getInstance();

        if (modifications != null) {
            for (int i = 0; i < modifications.size(); i++) {
                if (modifications.get(i).getModificationSite() == sequence.length()) {
                    PTM ptm = ptmFactory.getPTM(modifications.get(i).getTheoreticPtm());
                    if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                        cTerm = ptm.getShortName();
                    }
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
    public String getTaggedModifiedSequence(PtmSettings modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean excludeAllFixedPtms) {
        HashMap<Integer, ArrayList<String>> confidentModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> representativeModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> secondaryModificationSites = new HashMap<Integer, ArrayList<String>>();
        HashMap<Integer, ArrayList<String>> fixedModificationSites = new HashMap<Integer, ArrayList<String>>();

        if (modifications != null) {
            for (ModificationMatch modMatch : modifications) {
                String modName = modMatch.getTheoreticPtm();
                int modSite = modMatch.getModificationSite();
                if (modMatch.isVariable()) {
                    if (modMatch.isConfident()) {
                        if (!confidentModificationSites.containsKey(modSite)) {
                            confidentModificationSites.put(modSite, new ArrayList<String>(1));
                        }
                        confidentModificationSites.get(modSite).add(modName);
                    } else {
                        if (!representativeModificationSites.containsKey(modSite)) {
                            representativeModificationSites.put(modSite, new ArrayList<String>(1));
                        }
                        representativeModificationSites.get(modSite).add(modName);
                    }
                } else if (!excludeAllFixedPtms) {
                    if (!fixedModificationSites.containsKey(modSite)) {
                        fixedModificationSites.put(modSite, new ArrayList<String>(1));
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
    public String getTaggedModifiedSequence(PtmSettings modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {
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
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(PtmSettings modificationProfile, Peptide peptide,
            HashMap<Integer, ArrayList<String>> confidentModificationSites, HashMap<Integer, ArrayList<String>> representativeAmbiguousModificationSites,
            HashMap<Integer, ArrayList<String>> secondaryAmbiguousModificationSites, HashMap<Integer, ArrayList<String>> fixedModificationSites,
            boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName) {

        if (confidentModificationSites == null) {
            confidentModificationSites = new HashMap<Integer, ArrayList<String>>(0);
        }
        if (representativeAmbiguousModificationSites == null) {
            representativeAmbiguousModificationSites = new HashMap<Integer, ArrayList<String>>(0);
        }
        if (secondaryAmbiguousModificationSites == null) {
            secondaryAmbiguousModificationSites = new HashMap<Integer, ArrayList<String>>(0);
        }
        if (fixedModificationSites == null) {
            fixedModificationSites = new HashMap<Integer, ArrayList<String>>(0);
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
     * @param variablePtms if true, only variable PTMs are shown, false return
     * only the fixed PTMs
     *
     * @return the peptide modifications as a string
     */
    public static String getPeptideModificationsAsString(Peptide peptide, boolean variablePtms) {

        StringBuilder result = new StringBuilder();

        HashMap<String, ArrayList<Integer>> modMap = new HashMap<String, ArrayList<Integer>>();
        if (peptide.isModified()) {
            for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
                if ((variablePtms && modificationMatch.isVariable()) || (!variablePtms && !modificationMatch.isVariable())) {
                    if (!modMap.containsKey(modificationMatch.getTheoreticPtm())) {
                        modMap.put(modificationMatch.getTheoreticPtm(), new ArrayList<Integer>());
                    }
                    modMap.get(modificationMatch.getTheoreticPtm()).add(modificationMatch.getModificationSite());
                }
            }
        }

        boolean first = true, first2;
        ArrayList<String> mods = new ArrayList<String>(modMap.keySet());

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

        if (modifications == null) {
            return new ArrayList<Integer>(0);
        }

        ArrayList<Integer> modifiedResidues = new ArrayList<Integer>(modifications.size());

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

        if (modifications == null) {
            return new HashMap<Integer, ArrayList<String>>(0);
        }

        HashMap<Integer, ArrayList<String>> result = new HashMap<Integer, ArrayList<String>>(modifications.size());
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
    public synchronized void estimateTheoreticMass() throws IllegalArgumentException {

        if (mass == null) {

            Double tempMass = Atom.H.getMonoisotopicMass();

            for (int aa = 0; aa < sequence.length(); aa++) {
                try {
                    AminoAcid currentAA = AminoAcid.getAminoAcid(sequence.charAt(aa));

                    if (currentAA != null) {
                        tempMass += currentAA.getMonoisotopicMass();
                    } else {
                        System.out.println("Unknown amino acid: " + sequence.charAt(aa) + "!");
                    }
                } catch (NullPointerException e) {
                    throw new IllegalArgumentException("Unknown amino acid: " + sequence.charAt(aa) + "!");
                }
            }

            tempMass += Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();

            if (modifications != null) {
                PTMFactory ptmFactory = PTMFactory.getInstance();
                for (ModificationMatch ptmMatch : modifications) {
                    tempMass += ptmFactory.getPTM(ptmMatch.getTheoreticPtm()).getMass();
                }
            }

            mass = tempMass;
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

        if (peptide.isModified()) {
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
        }

        return noModPeptide;
    }
}
