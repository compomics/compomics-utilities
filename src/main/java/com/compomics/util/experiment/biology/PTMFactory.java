package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.pride.CvTerm;
import java.awt.Color;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This factory will load PTM from an XML file and provide them on demand as
 * standard class.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PTMFactory implements Serializable {

    /**
     * Serial number for serialization compatibility.
     */
//    static final long serialVersionUID = 7935264190312934466L;
    /**
     * Instance of the factory.
     */
    private static PTMFactory instance = null;
    /**
     * The folder containing the PTM factory.
     */
    private static String SERIALIZATION_FILE_FOLDER = System.getProperty("user.home") + "/.compomics";
    /**
     * The name of the PTM factory back-up file. The version number follows the
     * one of utilities.
     */
    private static String SERIALIZATION_FILE_NAME = "ptmFactory-4.12.13.json";
    /**
     * A map linking indexes with modifications.
     */
    private HashMap<String, PTM> ptmMap = new HashMap<String, PTM>();
    /**
     * List of the indexes of default modifications.
     */
    private ArrayList<String> defaultMods = new ArrayList<String>();
    /**
     * List of the indexes of user modifications.
     */
    private ArrayList<String> userMods = new ArrayList<String>();
    /**
     * Mapping of the expected modification names to the color used.
     */
    private HashMap<String, Color> userColors = new HashMap<String, Color>();
    /**
     * Unknown modification to be returned when the modification is not found.
     */
    public static final PTM unknownPTM = new PTM(PTM.MODAA, "unknown", "*", null, null, new AminoAcidPattern());
    /**
     * Suffix for the modification clone targeting a single amino acid instead
     * of a pattern.
     */
    public static final String SINGLE_AA_SUFFIX = "|single_aa";
    /**
     * Set to true if the default mods are sorted alphabetically.
     */
    public boolean defaultModsSorted = false;
    /**
     * Set to true if the users mods are sorted alphabetically.
     */
    public boolean usersModsSorted = false;

    /**
     * Constructor for the factory.
     */
    private PTMFactory() {
        defaultModsSorted = false;
        setDefaultModifications();
    }

    /**
     * Static method to get the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static PTMFactory getInstance() {
        if (instance == null) {
            try {
                File savedFile = new File(SERIALIZATION_FILE_FOLDER, SERIALIZATION_FILE_NAME);
                instance = loadFromFile(savedFile);
                instance.checkUserModifications();
            } catch (Exception e) {
                instance = new PTMFactory();
            }
        }
        return instance;
    }

    /**
     * Loads an enzyme factory from a file. The file must be an export of the
     * factory in the json format.
     *
     * @param file the file to load
     *
     * @return the enzyme factory saved in file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the file
     */
    public static PTMFactory loadFromFile(File file) throws IOException {
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        PTMFactory result = (PTMFactory) jsonMarshaller.fromJson(PTMFactory.class, file);
        return result;
    }

    /**
     * Saves a PTM factory to a file.
     *
     * @param ptmFactory the PTM factory to save
     * @param file the file where to save
     *
     * @throws IOException exception thrown whenever an error occurred while
     * saving the file
     */
    public static void saveToFile(PTMFactory ptmFactory, File file) throws IOException {
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        jsonMarshaller.saveObjectToJson(ptmFactory, file);
    }

    /**
     * Add neutral losses and reporter ions for the user PTMs.
     */
    private void checkUserModifications() {
        for (String tempUserMod : getUserModifications()) {

            PTM ptm = getPTM(tempUserMod);

            if (!ptm.getNeutralLosses().isEmpty()) {
                for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                    if (NeutralLoss.getNeutralLoss(neutralLoss.name) == null) {
                        NeutralLoss.addNeutralLoss(neutralLoss);
                    }
                }
            }
            if (!ptm.getReporterIons().isEmpty()) {
                for (ReporterIon reporterIon : ptm.getReporterIons()) {
                    if (ReporterIon.getReporterIon(reporterIon.getName()) == null) {
                        ReporterIon.addReporterIon(reporterIon);
                    }
                }
            }
        }
    }

    /**
     * Clears the factory getInstance() needs to be called afterwards.
     */
    public void clearFactory() {
        instance = new PTMFactory();
    }

    /**
     * Reloads the factory getInstance() needs to be called afterwards.
     */
    public void reloadFactory() {
        instance = null;
    }

    /**
     * Saves the factory in the user folder.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * saving the ptmFactory
     */
    public void saveFactory() throws IOException {
        File factoryFile = new File(SERIALIZATION_FILE_FOLDER, SERIALIZATION_FILE_NAME);
        if (!factoryFile.getParentFile().exists()) {
            factoryFile.getParentFile().mkdir();
        }
        saveToFile(instance, factoryFile);
    }

    /**
     * Returns a clone of the given PTM targeting a single amino acid instead of
     * a pattern.
     *
     * @param modification the modification of interest
     *
     * @return a clone of the given PTM targeting a single amino acid instead of
     * a pattern
     */
    public static PTM getSingleAAPTM(PTM modification) {
        if (!modification.isStandardSearch()) {
            return new PTM(modification.getType(), modification.getShortName(),
                    modification.getName() + SINGLE_AA_SUFFIX, modification.getAtomChainAdded(),
                    modification.getAtomChainRemoved(), modification.getPattern().getStandardSearchPattern());
        } else {
            return modification;
        }
    }

    /**
     * Returns a clone of the given PTM targeting a single amino acid instead of
     * a pattern.
     *
     * @param modificationName the name of the modification of interest
     *
     * @return a clone of the given PTM targeting a single amino acid instead of
     * a pattern
     */
    public PTM getSingleAAPTM(String modificationName) {
        PTM modification = getPTM(modificationName);
        return getSingleAAPTM(modification);
    }

    /**
     * Adds a new user modification.
     *
     * @param ptm the new modification to add
     */
    public void addUserPTM(PTM ptm) {

        String modName = ptm.getName();
        ptmMap.put(modName, ptm);
        if (!userMods.contains(modName)) {
            userMods.add(modName);
        } else {
            userMods.set(userMods.indexOf(modName), modName);
        }
        usersModsSorted = false;

        // add the neutral losses and reporter ions
        if (!ptm.getNeutralLosses().isEmpty()) {
            for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                if (NeutralLoss.getNeutralLoss(neutralLoss.name) == null) {
                    NeutralLoss.addNeutralLoss(neutralLoss);
                }
            }
        }
        if (!ptm.getReporterIons().isEmpty()) {
            for (ReporterIon reporterIon : ptm.getReporterIons()) {
                if (ReporterIon.getReporterIon(reporterIon.getName()) == null) {
                    ReporterIon.addReporterIon(reporterIon);
                }
            }
        }
    }

    /**
     * Removes a user PTM.
     *
     * @param ptmName the name of the PTM to remove
     */
    public void removeUserPtm(String ptmName) {
        if (defaultMods.contains(ptmName)) {
            throw new IllegalArgumentException("Impossible to remove default modification " + ptmName);
        }
        ptmMap.remove(ptmName);
        userMods.remove(ptmName);
    }

    /**
     * Returns the PTM indexed by its name.
     *
     * @param name the name of the desired PTM
     * @return the desired PTM
     */
    public PTM getPTM(String name) {
        PTM ptm = ptmMap.get(name);
        if (ptm != null) {
            return ptm;
        }
        return unknownPTM;
    }

    /**
     * Returns a boolean indicating whether the PTM is loaded in the factory.
     *
     * @param name the name of the PTM
     * @return a boolean indicating whether the PTM is loaded in the factory
     */
    public boolean containsPTM(String name) {
        return ptmMap.containsKey(name) || name.equals(unknownPTM.getName());
    }

    /**
     * Returns the names of the default modifications.
     *
     * @return the names of the default modifications
     */
    public ArrayList<String> getDefaultModifications() {
        return defaultMods;
    }

    /**
     * Returns the alphabetically ordered names of the default modifications.
     *
     * @return the alphabetically ordered names of the default modifications
     */
    public ArrayList<String> getDefaultModificationsOrdered() {
        if (!defaultModsSorted) {
            Collections.sort(defaultMods);
            defaultModsSorted = true;
        }
        return defaultMods;
    }

    /**
     * Returns the names of the user defined modifications.
     *
     * @return the names of the user defined modifications
     */
    public ArrayList<String> getUserModifications() {
        return userMods;
    }

    /**
     * Returns the alphabetically ordered names of the user defined
     * modifications.
     *
     * @return the alphabetically ordered names of the user defined
     * modifications
     */
    public ArrayList<String> getUserModificationsOrdered() {
        if (!usersModsSorted) {
            Collections.sort(userMods);
            usersModsSorted = true;
        }
        return userMods;
    }

    /**
     * Returns the names of all imported PTMs.
     *
     * @return the names of all imported PTMs
     */
    public ArrayList<String> getPTMs() {
        return new ArrayList<String>(ptmMap.keySet());
    }

    /**
     * Convenience method returning a boolean indicating whether a PTM is user
     * defined or default.
     *
     * @param ptmName the name of the PTM
     * @return boolean indicating whether a PTM is user defined
     */
    public boolean isUserDefined(String ptmName) {
        return !defaultMods.contains(ptmName);
    }

    /**
     * Verifies that the modifications backed-up in the search parameters are
     * loaded and alerts the user in case conflicts are found.
     *
     * @param searchParameters the search parameters to load
     * @param overwrite if true, overwrite the modification
     * @return returns a list of modifications already loaded which should be
     * checked.
     */
    public ArrayList<String> loadBackedUpModifications(SearchParameters searchParameters, boolean overwrite) {
        PtmSettings modificationProfile = searchParameters.getPtmSettings();
        ArrayList<String> toCheck = new ArrayList<String>();
        for (String modification : modificationProfile.getBackedUpPtms()) {
            if (containsPTM(modification)) {
                PTM oldPTM = getPTM(modification);
                PTM newPTM = modificationProfile.getPtm(modification);
                if (!oldPTM.isSameAs(newPTM)) {
                    toCheck.add(modification);
                    if (overwrite) {
                        PTM ptm = modificationProfile.getPtm(modification);
                        ptmMap.put(modification, ptm);
                        for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                            NeutralLoss.addNeutralLoss(neutralLoss);
                        }
                    }
                }
            } else {
                PTM ptm = modificationProfile.getPtm(modification);
                addUserPTM(ptm);
                for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                    NeutralLoss.addNeutralLoss(neutralLoss);
                }
            }
        }
        return toCheck;
    }

    /**
     * Returns the expected modifications based on the modification profile, the
     * peptide found and the modification details. Returns the names in a map
     * where the modification names are indexed by the index on the sequence. 1
     * is the first amino acid.
     *
     * @param modificationProfile the modification profile used for the search
     * (available in the search parameters)
     * @param peptide the peptide identified with the parent proteins (necessary
     * for protein termini modifications)
     * @param modificationMass the modification mass as found in the search
     * results
     * @param ptmMassTolerance the mass tolerance to use to match the
     * modification mass
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
     *
     * @return a map of expected PTMs corresponding to the given
     * characteristics. Empty if none found.
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public HashMap<Integer, ArrayList<String>> getExpectedPTMs(PtmSettings modificationProfile, Peptide peptide,
            double modificationMass, double ptmMassTolerance, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, FileNotFoundException, SQLException {

        HashMap<Integer, ArrayList<String>> mapping = new HashMap<Integer, ArrayList<String>>();

        for (String ptmName : modificationProfile.getAllNotFixedModifications()) {
            PTM ptm = getPTM(ptmName);
            if (Math.abs(ptm.getMass() - modificationMass) <= ptmMassTolerance) {
                for (int site : peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences)) {
                    ArrayList<String> modifications = mapping.get(site);
                    if (modifications == null) {
                        modifications = new ArrayList<String>();
                        mapping.put(site, modifications);
                    }
                    modifications.add(ptmName);
                }
            }
        }

        return mapping;
    }

    /**
     * Returns the names of the possibly expected modification based on the name
     * of the expected modification in a map where the PTM names are indexed by
     * their potential site on the sequence. 1 is the first amino acid.
     * Candidate PTMs are expected non fixed modifications with the same mass.
     *
     * @param modificationProfile the modification profile used for the search
     * (available in the search parameters)
     * @param peptide the peptide
     * @param ptmName the name of the searched PTM
     * @param ptmMassTolerance the PTM mass tolerance
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
     *
     * @return the possible expected modification names. Empty if not found.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public HashMap<Integer, ArrayList<String>> getExpectedPTMs(PtmSettings modificationProfile, Peptide peptide, String ptmName,
            Double ptmMassTolerance, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {
        PTM ptm = getPTM(ptmName);
        return getExpectedPTMs(modificationProfile, peptide, ptm.getMass(), ptmMassTolerance, sequenceMatchingPreferences, ptmSequenceMatchingPreferences);
    }

    /**
     * Removes the fixed modifications of the peptide and remaps the one
     * searched for according to the ModificationProfile. Note: for protein
     * terminal modification the protein must be loaded in the sequence factory.
     *
     * @param modificationProfile the modification profile
     * @param peptide the peptide
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void checkFixedModifications(PtmSettings modificationProfile, Peptide peptide, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        if (peptide.isModified()) {
            ArrayList<ModificationMatch> toRemove = new ArrayList<ModificationMatch>(peptide.getNModifications());
            for (ModificationMatch modMatch : peptide.getModificationMatches()) {
                if (!modMatch.isVariable()) {
                    toRemove.add(modMatch);
                }
            }
            for (ModificationMatch modMatch : toRemove) {
                peptide.getModificationMatches().remove(modMatch);
            }
        }
        HashMap<Integer, Double> taken = new HashMap<Integer, Double>(peptide.getNModifications());

        for (String fixedModification : modificationProfile.getFixedModifications()) {
            PTM ptm = getPTM(fixedModification);
            switch (ptm.getType()) {
                case PTM.MODAA:
                    for (int pos : peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences)) {
                        if (!taken.containsKey(pos)) {
                            taken.put(pos, ptm.getMass());
                            peptide.addModificationMatch(new ModificationMatch(fixedModification, false, pos));
                        } else if (taken.get(pos) != ptm.getMass()) { // @TODO: compare against the accuracy
                            throw new IllegalArgumentException("Attempting to put two fixed modifications of different masses ("
                                    + taken.get(pos) + ", " + ptm.getMass() + ") at position " + pos + " in peptide " + peptide.getSequence() + ".");
                        }
                    }
                    break;
                case PTM.MODC:
                    if (!peptide.isCterm(sequenceMatchingPreferences).isEmpty()) {
                        peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
                    }
                    break;
                case PTM.MODN:
                    if (!peptide.isNterm(sequenceMatchingPreferences).isEmpty()) {
                        peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                    }
                    break;
                case PTM.MODCAA: {
                    String sequence = peptide.getSequence();
                    if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(sequence.length())) {
                        peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
                    }
                    break;
                }
                case PTM.MODNAA:
                    if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(1)) {
                        peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                    }
                    break;
                case PTM.MODCP:
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
                    break;
                case PTM.MODNP:
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                    break;
                case PTM.MODCPAA: {
                    String sequence = peptide.getSequence();
                    if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(sequence.length())) {
                        peptide.addModificationMatch(new ModificationMatch(fixedModification, false, sequence.length()));
                    }
                    break;
                }
                case PTM.MODNPAA:
                    if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(1)) {
                        peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Removes the fixed modifications of the given tag and remaps the one
     * searched for according to the ModificationProfile. Note: for protein
     * terminal modification the protein must be loaded in the sequence factory.
     *
     * @param modificationProfile the modification profile
     * @param tag the tag
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * PTM to amino acid mapping
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void checkFixedModifications(PtmSettings modificationProfile, Tag tag, SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        int indexInTag = 0, componentNumber = 0;

        for (TagComponent tagComponent : tag.getContent()) {

            componentNumber++;

            if (tagComponent instanceof AminoAcidPattern) {

                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                ArrayList<ModificationMatch> toRemove = new ArrayList<ModificationMatch>();

                for (int aa : aminoAcidPattern.getModificationIndexes()) {
                    ArrayList<ModificationMatch> modificationMatches = aminoAcidPattern.getModificationsAt(aa);
                    for (ModificationMatch modMatch : modificationMatches) {
                        if (!modMatch.isVariable()) {
                            toRemove.add(modMatch);
                        }
                    }
                    for (ModificationMatch modMatch : toRemove) {
                        aminoAcidPattern.removeModificationMatch(aa, modMatch);
                    }
                }

                for (int aa = 1; aa <= aminoAcidPattern.length(); aa++) {

                    indexInTag++;
                    Double modificationMass = null;

                    for (String fixedModification : modificationProfile.getFixedModifications()) {

                        PTM ptm = getPTM(fixedModification);

                        if (ptm.getType() == PTM.MODAA) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(indexInTag)) {
                                if (modificationMass == null) {
                                    modificationMass = ptm.getMass();
                                    aminoAcidPattern.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                                } else if (modificationMass != ptm.getMass()) { // @TODO: compare against the accuracy
                                    throw new IllegalArgumentException("Attempting to put two fixed modifications of different masses ("
                                            + modificationMass + ", " + ptm.getMass() + ") at position " + aa + " in pattern "
                                            + aminoAcidPattern.asSequence() + " of tag " + tag.asSequence() + ".");
                                }
                            }
                        } else if (ptm.getType() == PTM.MODCP && componentNumber == tag.getContent().size() && aa == aminoAcidPattern.length()) {
                            aminoAcidPattern.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                        } else if (ptm.getType() == PTM.MODNP && componentNumber == 1 && aa == 1) {
                            aminoAcidPattern.addModificationMatch(1, new ModificationMatch(fixedModification, false, 1));
                        } else if (ptm.getType() == PTM.MODCPAA && componentNumber == tag.getContent().size() && aa == aminoAcidPattern.length()) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(indexInTag)) {
                                aminoAcidPattern.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                            }
                        } else if (ptm.getType() == PTM.MODNPAA && componentNumber == 1 && aa == 1) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(1)) {
                                aminoAcidPattern.addModificationMatch(1, new ModificationMatch(fixedModification, false, 1));
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof AminoAcidSequence) {

                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;

                ArrayList<ModificationMatch> toRemove = new ArrayList<ModificationMatch>();

                for (int aa : aminoAcidSequence.getModificationIndexes()) {
                    ArrayList<ModificationMatch> modificationMatches = aminoAcidSequence.getModificationsAt(aa);
                    for (ModificationMatch modMatch : modificationMatches) {
                        if (!modMatch.isVariable()) {
                            toRemove.add(modMatch);
                        }
                    }
                    for (ModificationMatch modMatch : toRemove) {
                        aminoAcidSequence.removeModificationMatch(aa, modMatch);
                    }
                }

                for (int aa = 1; aa <= aminoAcidSequence.length(); aa++) {
                    indexInTag++;
                    Double modificationMass = null;

                    for (String fixedModification : modificationProfile.getFixedModifications()) {
                        PTM ptm = getPTM(fixedModification);
                        if (ptm.getType() == PTM.MODAA) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(indexInTag)) {
                                if (modificationMass == null) {
                                    modificationMass = ptm.getMass();
                                    aminoAcidSequence.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                                } else if (modificationMass != ptm.getMass()) { // @TODO: compare against the accuracy
                                    throw new IllegalArgumentException("Attempting to put two fixed modifications of different masses ("
                                            + modificationMass + ", " + ptm.getMass() + ") at position " + aa + " in pattern "
                                            + aminoAcidSequence.asSequence() + " of tag " + tag.asSequence() + ".");
                                }
                            }
                        } else if (ptm.getType() == PTM.MODCP && componentNumber == tag.getContent().size() && aa == aminoAcidSequence.length()) {
                            aminoAcidSequence.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                        } else if (ptm.getType() == PTM.MODNP && componentNumber == 1 && aa == 1) {
                            aminoAcidSequence.addModificationMatch(1, new ModificationMatch(fixedModification, false, 1));
                        } else if (ptm.getType() == PTM.MODCPAA && componentNumber == tag.getContent().size() && aa == aminoAcidSequence.length()) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(indexInTag)) {
                                aminoAcidSequence.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                            }
                        } else if (ptm.getType() == PTM.MODNPAA && componentNumber == 1 && aa == 1) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(1)) {
                                aminoAcidSequence.addModificationMatch(1, new ModificationMatch(fixedModification, false, 1));
                            }
                        }
                    }
                }
            } else {
                indexInTag++;
            }
        }
    }

    /**
     * Returns the color used to code the given modification.
     *
     * @param modification the name of the given expected modification
     * @return the corresponding color
     */
    public Color getColor(String modification) {
        if (!userColors.containsKey(modification)) {
            setColor(modification, getDefaultColor(modification));
        }
        return userColors.get(modification);
    }

    /**
     * Sets a new color for the given expected modification.
     *
     * @param expectedModification the name of the expected modification
     * @param color the new color
     */
    public void setColor(String expectedModification, Color color) {
        userColors.put(expectedModification, color);
    }

    /**
     * Returns a default color based on the modification name.
     *
     * @param modification the name of the modification
     * @return a default color.
     */
    public static Color getDefaultColor(String modification) {
        if (modification.contains("no modification")) {
            return Color.LIGHT_GRAY;
        } else if (modification.toLowerCase().contains("phospho")) {
            return Color.RED;
        } else if (modification.toLowerCase().contains("pyro")) {
            return new Color(255, 102, 51);
        } else if (modification.toLowerCase().contains("ox")) {
            return Color.BLUE;
        } else if (modification.toLowerCase().contains("itraq")) {
            return Color.ORANGE;
        } else if (modification.toLowerCase().contains("tmt")) {
            return Color.ORANGE;
        } else if (modification.toLowerCase().contains("carbamido")) {
            return Color.LIGHT_GRAY;
        } else if (modification.toLowerCase().contains("ace")) {
            return new Color(153, 153, 0);
        } else if (modification.toLowerCase().contains("glyco")) {
            return Color.MAGENTA;
        } else {
            float r = (float) Math.random();
            float g = (float) Math.random();
            float b = (float) Math.random();
            return new Color(r, g, b);
        }
    }

    /**
     * Tries to convert a PRIDE PTM to utilities PTM name, and add it to the
     * modification profile. Unknown PTMs are added to the unknown PTMs
     * arraylist.
     *
     * @param pridePtmName the PRIDE PTM name
     * @param modProfile the modification profile to add the PTMs to
     * @param unknownPtms the list of unknown PTMS, updated during this method
     * @param isFixed if true, the PTM will be added as a fixed modification
     * @return a pride parameters report as a string (for use in PRIDE Reshake)
     */
    public String convertPridePtm(String pridePtmName, PtmSettings modProfile, ArrayList<String> unknownPtms, boolean isFixed) {

        String prideParametersReport = "";

        // special cases for when multiple ptms are needed
        if (pridePtmName.equalsIgnoreCase("iTRAQ4plex")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex reporter+balance reagent N-acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-114 reporter+balance reagent N6-acylated lysine")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-114 reporter+balance reagent O4&apos;-acylated tyrosine")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-114 reporter+balance reagent acylated N-terminal")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-114 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-116 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-117 reporter+balance reagent N6-acylated lysine")) {

            if (!modProfile.contains("iTRAQ 4-plex of K")) {
                modProfile.addFixedModification(getPTM("iTRAQ 4-plex of K"));
                prideParametersReport += "<br>" + "iTRAQ 4-plex of K" + " (assumed fixed)";
            }
            if (!modProfile.contains("iTRAQ 4-plex of N-term")) {
                modProfile.addFixedModification(getPTM("iTRAQ 4-plex of N-term"));
                prideParametersReport += "<br>" + "iTRAQ 4-plex of N-term" + " (assumed fixed)";
            }
            if (!modProfile.contains("iTRAQ 4-plex of Y")) {
                modProfile.addVariableModification(getPTM("iTRAQ 4-plex of Y"));
                prideParametersReport += "<br>" + "iTRAQ 4-plex of Y" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("iTRAQ8plex")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-113 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-114 reporter+balance reagent N6-acylated lysine")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-114 reporter+balance reagent O4&apos;-acylated tyrosine")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-114 reporter+balance reagent acylated N-terminal")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-115 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-116 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex:13C(6)15N(2)")) {

            if (!modProfile.contains("iTRAQ 8-plex of K")) {
                modProfile.addFixedModification(getPTM("iTRAQ 8-plex of K"));
                prideParametersReport += "<br>" + "iTRAQ 8-plex of K" + " (assumed fixed)";
            }
            if (!modProfile.contains("iTRAQ 8-plex of N-term")) {
                modProfile.addFixedModification(getPTM("iTRAQ 8-plex of N-term"));
                prideParametersReport += "<br>" + "iTRAQ 8-plex of N-term" + " (assumed fixed)";
            }
            if (!modProfile.contains("iTRAQ 8-plex of Y")) {
                modProfile.addVariableModification(getPTM("iTRAQ 8-plex of Y"));
                prideParametersReport += "<br>" + "iTRAQ 8-plex of Y" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("TMT2plex") || pridePtmName.equalsIgnoreCase("TMTduplex")) {

            if (!modProfile.contains("TMT 2-plex of K")) {
                modProfile.addFixedModification(getPTM("TMT 2-plex of K"));
                prideParametersReport += "<br>" + "TMT 2-plex of K" + " (assumed fixed)";
            }
            if (!modProfile.contains("TMT 2-plex of N-term")) {
                modProfile.addFixedModification(getPTM("TMT 2-plex of N-term"));
                prideParametersReport += "<br>" + "TMT 2-plex of N-term" + " (assumed fixed)";
            }

        } else if (pridePtmName.equalsIgnoreCase("TMT6plex")
                || pridePtmName.equalsIgnoreCase("TMT6plex-126 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("TMT6plex-131 reporter+balance reagent N6-acylated lysine")) {

            if (!modProfile.contains("TMT 6-plex of K")) {
                modProfile.addFixedModification(getPTM("TMT 6-plex of K"));
                prideParametersReport += "<br>" + "TMT 6-plex of K" + " (assumed fixed)";
            }
            if (!modProfile.contains("TMT 6-plex of N-term")) {
                modProfile.addFixedModification(getPTM("TMT 6-plex of N-term"));
                prideParametersReport += "<br>" + "TMT 6-plex of N-term" + " (assumed fixed)";
            }

        } else if (pridePtmName.equalsIgnoreCase("TMT10plex")
                || pridePtmName.equalsIgnoreCase("TMT10plex-126 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("TMT10plex-131 reporter+balance reagent N6-acylated lysine")) {

            if (!modProfile.contains("TMT 10-plex of K")) {
                modProfile.addFixedModification(getPTM("TMT 10-plex of K"));
                prideParametersReport += "<br>" + "TMT 10-plex of K" + " (assumed fixed)";
            }
            if (!modProfile.contains("TMT 10-plex of N-term")) {
                modProfile.addFixedModification(getPTM("TMT 10-plex of N-term"));
                prideParametersReport += "<br>" + "TMT 10-plex of N-term" + " (assumed fixed)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Phosphorylation")
                || pridePtmName.equalsIgnoreCase("L-aspartic 4-phosphoric anhydride")
                || pridePtmName.equalsIgnoreCase("O-phosphorylated residue")
                || pridePtmName.equalsIgnoreCase("Phospho")
                || pridePtmName.equalsIgnoreCase("phosphorylated residue")) {

            if (!modProfile.contains("Phosphorylation of S")) {
                modProfile.addVariableModification(getPTM("Phosphorylation of S"));
                prideParametersReport += "<br>" + "Phosphorylation of S" + " (assumed variable)";
            }
            if (!modProfile.contains("Phosphorylation of T")) {
                modProfile.addVariableModification(getPTM("Phosphorylation of T"));
                prideParametersReport += "<br>" + "Phosphorylation of T" + " (assumed variable)";
            }
            if (!modProfile.contains("Phosphorylation of Y")) {
                modProfile.addVariableModification(getPTM("Phosphorylation of Y"));
                prideParametersReport += "<br>" + "Phosphorylation of Y" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Palmitoylation")) {

            if (!modProfile.contains("Palmitoylation of C")) {
                modProfile.addVariableModification(getPTM("Palmitoylation of C"));
                prideParametersReport += "<br>" + "Palmitoylation of C" + " (assumed variable)";
            }
            if (!modProfile.contains("Palmitoylation of K")) {
                modProfile.addVariableModification(getPTM("Palmitoylation of K"));
                prideParametersReport += "<br>" + "Palmitoylation of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Palmitoylation of S")) {
                modProfile.addVariableModification(getPTM("Palmitoylation of S"));
                prideParametersReport += "<br>" + "Palmitoylation of S" + " (assumed variable)";
            }
            if (!modProfile.contains("Palmitoylation of T")) {
                modProfile.addVariableModification(getPTM("Palmitoylation of T"));
                prideParametersReport += "<br>" + "Palmitoylation of T" + " (assumed variable)";
            }
            if (!modProfile.contains("Palmitoylation of protein N-term")) {
                modProfile.addVariableModification(getPTM("Palmitoylation of protein N-term"));
                prideParametersReport += "<br>" + "Palmitoylation of protein N-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Formylation")) {

            if (!modProfile.contains("Formylation of K")) {
                modProfile.addVariableModification(getPTM("Formylation of K"));
                prideParametersReport += "<br>" + "Formylation of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Formylation of S")) {
                modProfile.addVariableModification(getPTM("Formylation of S"));
                prideParametersReport += "<br>" + "Formylation of S" + " (assumed variable)";
            }
            if (!modProfile.contains("Formylation of T")) {
                modProfile.addVariableModification(getPTM("Formylation of T"));
                prideParametersReport += "<br>" + "Formylation of T" + " (assumed variable)";
            }
            if (!modProfile.contains("Formylation of peptide N-term")) {
                modProfile.addVariableModification(getPTM("Formylation of peptide N-term"));
                prideParametersReport += "<br>" + "Formylation of peptide N-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Carbamylation")
                || pridePtmName.equalsIgnoreCase("carbamoylated residue")) {

            if (!modProfile.contains("Carbamylation of K")) {
                modProfile.addVariableModification(getPTM("Carbamylation of K"));
                prideParametersReport += "<br>" + "Carbamylation of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Carbamilation of protein N-term")) {
                modProfile.addVariableModification(getPTM("Carbamilation of protein N-term"));
                prideParametersReport += "<br>" + "Carbamilation of protein N-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(12)C labeled N6-propanoyl-L-lysine")) {

            if (!modProfile.contains("Propionyl of K light")) {
                modProfile.addVariableModification(getPTM("Propionyl of K light"));
                prideParametersReport += "<br>" + "Propionyl of K light" + " (assumed variable)";
            }
            if (!modProfile.contains("Propionyl of peptide N-term light")) {
                modProfile.addVariableModification(getPTM("Propionyl of peptide N-term light"));
                prideParametersReport += "<br>" + "Propionyl of peptide N-term light" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(13)C labeled N6-propanoyl-L-lysine")) {

            if (!modProfile.contains("Propionyl of K heavy")) {
                modProfile.addVariableModification(getPTM("Propionyl of K heavy"));
                prideParametersReport += "<br>" + "Propionyl of K heavy" + " (assumed variable)";
            }
            if (!modProfile.contains("Propionyl of peptide N-term heavy")) {
                modProfile.addVariableModification(getPTM("Propionyl of peptide N-term heavy"));
                prideParametersReport += "<br>" + "Propionyl of peptide N-term heavy" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(2)H residue methyl ester")) {

            if (!modProfile.contains("Trideuterated Methyl Ester of D")) {
                modProfile.addVariableModification(getPTM("Trideuterated Methyl Ester of D"));
                prideParametersReport += "<br>" + "Trideuterated Methyl Ester of D" + " (assumed variable)";
            }
            if (!modProfile.contains("Trideuterated Methyl Ester of E")) {
                modProfile.addVariableModification(getPTM("Trideuterated Methyl Ester of E"));
                prideParametersReport += "<br>" + "Trideuterated Methyl Ester of E" + " (assumed variable)";
            }
            if (!modProfile.contains("Trideuterated Methyl Ester of K")) {
                modProfile.addVariableModification(getPTM("Trideuterated Methyl Ester of K"));
                prideParametersReport += "<br>" + "Trideuterated Methyl Ester of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Trideuterated Methyl Ester of R")) {
                modProfile.addVariableModification(getPTM("Trideuterated Methyl Ester of R"));
                prideParametersReport += "<br>" + "Trideuterated Methyl Ester of R" + " (assumed variable)";
            }
            if (!modProfile.contains("Trideuterated Methyl Ester of peptide C-term")) {
                modProfile.addVariableModification(getPTM("Trideuterated Methyl Ester of peptide C-term"));
                prideParametersReport += "<br>" + "Trideuterated Methyl Ester of peptide C-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled residue")) {

            if (!modProfile.contains("Arginine 13C6")) {
                modProfile.addVariableModification(getPTM("Arginine 13C6"));
                prideParametersReport += "<br>" + "Arginine 13C6" + " (assumed variable)";
            }
            if (!modProfile.contains("Lysine 13C6")) {
                modProfile.addVariableModification(getPTM("Lysine 13C6"));
                prideParametersReport += "<br>" + "Lysine 13C6" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Acetyl")
                || pridePtmName.equalsIgnoreCase("N-acetylated residue")
                || pridePtmName.equalsIgnoreCase("N-acylated residue")
                || pridePtmName.equalsIgnoreCase("acetylated residue")) {

            if (!modProfile.contains("Acetylation of K")) {
                modProfile.addVariableModification(getPTM("Acetylation of K"));
                prideParametersReport += "<br>" + "Acetylation of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Acetylation of peptide N-term")) {
                modProfile.addVariableModification(getPTM("Acetylation of peptide N-term"));
                prideParametersReport += "<br>" + "Acetylation of peptide N-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("dimethylated residue")) {

            if (!modProfile.contains("Dimethylation of K")) {
                modProfile.addVariableModification(getPTM("Dimethylation of K"));
                prideParametersReport += "<br>" + "Dimethylation of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Dimethylation of R")) {
                modProfile.addVariableModification(getPTM("Dimethylation of R"));
                prideParametersReport += "<br>" + "Dimethylation of R" + " (assumed variable)";
            }
            if (!modProfile.contains("Dimethylation of peptide N-term")) {
                modProfile.addVariableModification(getPTM("Dimethylation of peptide N-term"));
                prideParametersReport += "<br>" + "Dimethylation of peptide N-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("trimethylated residue")) {

            if (!modProfile.contains("Trimethylation of K")) {
                modProfile.addVariableModification(getPTM("Trimethylation of K"));
                prideParametersReport += "<br>" + "Trimethylation of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Trimethylation of R")) {
                modProfile.addVariableModification(getPTM("Trimethylation of R"));
                prideParametersReport += "<br>" + "Trimethylation of R" + " (assumed variable)";
            }
            if (!modProfile.contains("Trimethylation of protein N-term A")) {
                modProfile.addVariableModification(getPTM("Trimethylation of protein N-term A"));
                prideParametersReport += "<br>" + "Trimethylation of protein N-term A" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Acetylation")) {

            if (!modProfile.contains("Acetylation of K")) {
                modProfile.addVariableModification(getPTM("Acetylation of K"));
                prideParametersReport += "<br>" + "Acetylation of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Acetylation of peptide N-term")) {
                modProfile.addVariableModification(getPTM("Acetylation of peptide N-term"));
                prideParametersReport += "<br>" + "Acetylation of peptide N-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Guanidination")) {

            if (!modProfile.contains("Guanidination of K")) {
                modProfile.addVariableModification(getPTM("Guanidination of K"));
                prideParametersReport += "<br>" + "Guanidination of K" + " (assumed variable)";
            }
            if (!modProfile.contains("Guanidination of peptide N-term")) {
                modProfile.addVariableModification(getPTM("Guanidination of peptide N-term"));
                prideParametersReport += "<br>" + "Guanidination of peptide N-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Methylthio")) {

            if (!modProfile.contains("Methylthio of N")) {
                modProfile.addVariableModification(getPTM("Methylthio of N"));
                prideParametersReport += "<br>" + "Methylthio of N" + " (assumed variable)";
            }
            if (!modProfile.contains("Methylthio of D")) {
                modProfile.addVariableModification(getPTM("Methylthio of D"));
                prideParametersReport += "<br>" + "Methylthio of D" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Sulfo")
                || pridePtmName.equalsIgnoreCase("sulfated residue")) {

            if (!modProfile.contains("Sulfonation of Y")) {
                modProfile.addVariableModification(getPTM("Sulfonation of Y"));
                prideParametersReport += "<br>" + "Sulfonation of Y" + " (assumed variable)";
            }
            if (!modProfile.contains("Sulfonation of S")) {
                modProfile.addVariableModification(getPTM("Sulfonation of S"));
                prideParametersReport += "<br>" + "Sulfonation of S" + " (assumed variable)";
            }
            if (!modProfile.contains("Sulfonation of T")) {
                modProfile.addVariableModification(getPTM("Sulfonation of T"));
                prideParametersReport += "<br>" + "Sulfonation of T" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Deamination")
                || pridePtmName.equalsIgnoreCase("Deamidated")
                || pridePtmName.equalsIgnoreCase("deamidated L-glutamine")
                || pridePtmName.equalsIgnoreCase("deamidated residue")
                || pridePtmName.equalsIgnoreCase("deaminated residue")) {

            if (!modProfile.contains("Deamidation of N")) {
                modProfile.addVariableModification(getPTM("Deamidation of N"));
                prideParametersReport += "<br>" + "Deamidation of N" + " (assumed variable)";
            }
            if (!modProfile.contains("Deamidation of Q")) {
                modProfile.addVariableModification(getPTM("Deamidation of Q"));
                prideParametersReport += "<br>" + "Deamidation of Q" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Dioxidation")) {

            if (!modProfile.contains("Dioxidation of M")) {
                modProfile.addVariableModification(getPTM("Dioxidation of M"));
                prideParametersReport += "<br>" + "Dioxidation of M" + " (assumed variable)";
            }
            if (!modProfile.contains("Dioxidation of W")) {
                modProfile.addVariableModification(getPTM("Dioxidation of W"));
                prideParametersReport += "<br>" + "Dioxidation of W" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("dehydrated residue")
                || pridePtmName.equalsIgnoreCase("Dehydratation")) {

            if (!modProfile.contains("Dehydration of S")) {
                modProfile.addVariableModification(getPTM("Dehydration of S"));
                prideParametersReport += "<br>" + "Dehydration of S" + " (assumed variable)";
            }
            if (!modProfile.contains("Dehydration of T")) {
                modProfile.addVariableModification(getPTM("Dehydration of T"));
                prideParametersReport += "<br>" + "Dehydration of T" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("No PTMs are included in the dataset")) {
            // ignore
        } else {

            // single ptm mapping
            String utilitiesPtmName = convertPridePtmToUtilitiesPtm(pridePtmName);

            if (utilitiesPtmName != null) {
                if (!modProfile.contains(utilitiesPtmName)) {
                    if (isFixed) {
                        modProfile.addFixedModification(getPTM(utilitiesPtmName));
                        prideParametersReport += "<br>" + utilitiesPtmName + " (assumed fixed)";
                    } else {
                        modProfile.addVariableModification(getPTM(utilitiesPtmName));
                        prideParametersReport += "<br>" + utilitiesPtmName + " (assumed variable)";
                    }
                }
            } else if (!unknownPtms.contains(pridePtmName)) {
                unknownPtms.add(pridePtmName);
            }
        }

        return prideParametersReport;
    }

    /**
     * Tries to convert a PRIDE PTM name to utilities PTM name.
     *
     * @param pridePtmName the PRIDE PTM name
     * @return the utilities PTM name, or null if there is no mapping
     */
    private String convertPridePtmToUtilitiesPtm(String pridePtmName) {

        if (pridePtmName.equalsIgnoreCase("Carbamidomethyl")
                || pridePtmName.equalsIgnoreCase("S-carboxamidomethyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("iodoacetamide - site C")
                || pridePtmName.equalsIgnoreCase("iodoacetamide -site C")
                || pridePtmName.equalsIgnoreCase("iodoacetamide derivatized residue")
                || pridePtmName.equalsIgnoreCase("Iodoacetamide derivative")) {
            return "Carbamidomethylation of C";
        } else if (pridePtmName.equalsIgnoreCase("Oxidation")
                || pridePtmName.equalsIgnoreCase("monohydroxylated residue")
                || pridePtmName.equalsIgnoreCase("oxidized residue")) {
            return "Oxidation of M";
        } else if (pridePtmName.equalsIgnoreCase("Amidation")) {
            return "Amidation of the peptide C-term";
        } else if (pridePtmName.equalsIgnoreCase("Carboxymethyl")
                || pridePtmName.equalsIgnoreCase("S-carboxymethyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("iodoacetic acid derivatized residue")) {
            return "Carboxymethylation of C";
        } else if (pridePtmName.equalsIgnoreCase("Farnesylation")) {
            return "Farnesylation of C";
        } else if (pridePtmName.equalsIgnoreCase("Geranyl-geranyl")) {
            return "Geranyl-geranyl of C";
        } else if (pridePtmName.equalsIgnoreCase("Homoserine")) {
            return "Homoserine of peptide C-term M";
        } else if (pridePtmName.equalsIgnoreCase("Homoserine lactone")) {
            return "Homoserine lactone of peptide C-term M";
        } else if (pridePtmName.equalsIgnoreCase("ICAT-C")
                || pridePtmName.equalsIgnoreCase("Applied Biosystems cleavable ICAT(TM) light")) {
            return "ICAT-O";
        } else if (pridePtmName.equalsIgnoreCase("ICAT-C:13C(9)")
                || pridePtmName.equalsIgnoreCase("Applied Biosystems cleavable ICAT(TM) heavy")) {
            return "ICAT-9";
        } else if (pridePtmName.equalsIgnoreCase("Lipoyl")) {
            return "Lipoyl of K";
        } else if (pridePtmName.equalsIgnoreCase("NIPCAM(C)")) {
            return "NIPCAM of C";
        } else if (pridePtmName.equalsIgnoreCase("Phosphopantetheine")) {
            return "phosphopantetheine s";
        } else if (pridePtmName.equalsIgnoreCase("Propionamide(C)")
                || pridePtmName.equalsIgnoreCase("Acrylamide adduct")) {
            return "Propionamide of C";
        } else if (pridePtmName.equalsIgnoreCase("Pyridylethyl")) {
            return "Pyridylethyl of C";
        } else if (pridePtmName.equalsIgnoreCase("(18)O label at both C-terminal oxygens")) {
            return "18O(2) of peptide C-term";
        } else if (pridePtmName.equalsIgnoreCase("(18)O monosubstituted residue")) {
            return "18O(1) of peptide C-term";
        } else if (pridePtmName.equalsIgnoreCase("(4,4,5,5-(2)H4)-L-lysine")) {
            return "Lysine 2H4";
        } else if (pridePtmName.equalsIgnoreCase("2-pyrrolidone-5-carboxylic acid (Gln)")
                || pridePtmName.equalsIgnoreCase("Ammonia-loss")) {
            return "Pyrolidone from Q";
        } else if (pridePtmName.equalsIgnoreCase("2-pyrrolidone-5-carboxylic acid (Glu)")
                || pridePtmName.equalsIgnoreCase("Glu->pyro-Glu")) {
            return "Pyrolidone from E";
        } else if (pridePtmName.equalsIgnoreCase("3-hydroxy-L-proline")) {
            return "Oxidation of P";
        } else if (pridePtmName.equalsIgnoreCase("3x(2)H labeled L-aspartic acid 4-methyl ester")) {
            return "Trideuterated Methyl Ester of D";
        } else if (pridePtmName.equalsIgnoreCase("4x(2)H labeled alpha-dimethylamino N-terminal residue")) {
            return "Dimethylation of peptide N-term 2H(4)";
        } else if (pridePtmName.equalsIgnoreCase("4x(2)H labeled dimethylated L-lysine")) {
            return "Dimethylation of K 2H(4)";
        } else if (pridePtmName.equalsIgnoreCase("5-methyl-L-arginine")) {
            return "Methylation of R";
        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled L-arginine")) {
            return "Arginine 13C6";
        } else if (pridePtmName.equalsIgnoreCase("6x(13)C,4x(15)N labeled L-arginine")) {
            return "Arginine 13C6 15N4";
        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled L-lysine")) {
            return "Lysine 13C6";
        } else if (pridePtmName.equalsIgnoreCase("6x(13)C,2x(15)N labeled L-lysine")) {
            return "Lysine 13C6 15N2";
        } else if (pridePtmName.equalsIgnoreCase("L-aspartic acid 4-methyl ester")) {
            return "Methylation of D";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteic acid (L-cysteine sulfonic acid)")) {
            return "Oxidation of C";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteine glutathione disulfide")) {
            return "Glutathione of C";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteine methyl disulfide")
                || pridePtmName.equalsIgnoreCase("methyl methanethiosulfonate")) {
            return "Methylthio of C";
        } else if (pridePtmName.equalsIgnoreCase("L-cystine (cross-link)")) {
            return "Didehydro of T";
        } else if (pridePtmName.equalsIgnoreCase("L-glutamic acid 5-methyl ester (Glu)")
                || pridePtmName.equalsIgnoreCase("methylated glutamic acid")) {
            return "Methylation of E";
        } else if (pridePtmName.equalsIgnoreCase("L-homoarginine")) {
            return "Guanidination of K";
        } else if (pridePtmName.equalsIgnoreCase("L-methionine (R)-sulfoxide")
                || pridePtmName.equalsIgnoreCase("L-methionine (S)-sulfoxide")
                || pridePtmName.equalsIgnoreCase("L-methionine sulfoxide")) {
            return "Oxidation of M";
        } else if (pridePtmName.equalsIgnoreCase("L-methionine sulfone")) {
            return "Dioxidation of M";
        } else if (pridePtmName.equalsIgnoreCase("N-acetyl-L-asparagine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-glutamic acid")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-isoleucine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-serine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-tyrosine")
                || pridePtmName.equalsIgnoreCase("N2-acetyl-L-tryptophan")
                || pridePtmName.equalsIgnoreCase("alpha-amino acetylated residue")) {
            return "Acetylation of protein N-term";
        } else if (pridePtmName.equalsIgnoreCase("N-acetylated L-lysine")
                || pridePtmName.equalsIgnoreCase("N6-acetyl-L-lysine")) {
            return "Acetylation of K";
        } else if (pridePtmName.equalsIgnoreCase("N-ethylmaleimide derivatized cysteine")) {
            return "Nethylmaleimide of C";
        } else if (pridePtmName.equalsIgnoreCase("N-formyl-L-methionine")) {
            return "FormylMet of protein N-term";
        } else if (pridePtmName.equalsIgnoreCase("N-formylated residue")) {
            return "Formylation of peptide N-term"; // note: could also be the other formylations
        } else if (pridePtmName.equalsIgnoreCase("N-methyl-L-serine")) {
            return "Methylation of S";
        } else if (pridePtmName.equalsIgnoreCase("N6,N6-dimethyl-L-lysine")) {
            return "Dimethylation of K";
        } else if (pridePtmName.equalsIgnoreCase("N6-formyl-L-lysine")) {
            return "Formylation of K";
        } else if (pridePtmName.equalsIgnoreCase("N6-methyl-L-lysine")
                || pridePtmName.equalsIgnoreCase("methylated lysine")
                || pridePtmName.equalsIgnoreCase("monomethylated L-lysine")) {
            return "Methylation of K";
        } else if (pridePtmName.equalsIgnoreCase("N6-propanoyl-L-lysine")) {
            return "Propionyl of K light";
        } else if (pridePtmName.equalsIgnoreCase("O-(N-acetylamino)glucosyl-L-serine")) {
            return "HexNAc of S";
        } else if (pridePtmName.equalsIgnoreCase("O-(N-acetylamino)glucosyl-L-threonine")) {
            return "HexNAc of T";
        } else if (pridePtmName.equalsIgnoreCase("O-phospho-L-serine")) {
            return "Phosphorylation of S";
        } else if (pridePtmName.equalsIgnoreCase("O-phospho-L-threonine")) {
            return "Phosphorylation of T";
        } else if (pridePtmName.equalsIgnoreCase("O4&apos;-phospho-L-tyrosine")) {
            return "Phosphorylation of Y";
        } else if (pridePtmName.equalsIgnoreCase("S-carboxamidoethyl-L-cysteine")) {
            return "Propionamide of C";
        } else if (pridePtmName.equalsIgnoreCase("S-methyl-L-cysteine")) {
            return "Methylation of C";
        } else if (pridePtmName.equalsIgnoreCase("alpha-amino dimethylated residue")) {
            return "Dimethylation of N-term";
        } else if (pridePtmName.equalsIgnoreCase("amidated residue")) {
            return "Amidation of the peptide C-term";
        } else if (pridePtmName.equalsIgnoreCase("deamidated L-asparagine")
                || pridePtmName.equalsIgnoreCase("deglycosylated asparagine")) {
            return "Deamidation of N";
        } else if (pridePtmName.equalsIgnoreCase("dihydroxylated residue - site W")) {
            return "Dioxidation of W";
        } else if (pridePtmName.equalsIgnoreCase("diiodinated residue")) {
            return "Diiodination of Y";
        } else if (pridePtmName.equalsIgnoreCase("hydroxylated lysine")
                || pridePtmName.equalsIgnoreCase("monohydroxylated lysine")) {
            return "Oxidation of K";
        } else if (pridePtmName.equalsIgnoreCase("iodoacetamide -site E")
                || pridePtmName.equalsIgnoreCase("iodoacetamide - site E")) {
            return "Carbamidomethylat\"iodoacetamide -site E\"ion of E";
        } else if (pridePtmName.equalsIgnoreCase("iodoacetamide N6-derivatized lysine")) {
            return "Carbamidomethylation of K";
        } else if (pridePtmName.equalsIgnoreCase("monomethylated L-aspartic acid")) {
            return "Methylation of D";
        } else if (pridePtmName.equalsIgnoreCase("thioacylation of primary amines - site N-term")) {
            return "Thioacyl of peptide N-term";
        } else if (pridePtmName.equalsIgnoreCase("ubiquitination signature dipeptidyl lysine")) {
            return "Ubiquitination of K";
        } else if (pridePtmName.equalsIgnoreCase("Label:13C(6)15N(2)")) {
            return "Lysine 13C(6) 15N(2)";
        } else {
            return null;
        }
    }

    /**
     * Returns the folder where the factory is saved.
     *
     * @return the folder where the factory is saved
     */
    public static String getSerializationFolder() {
        return SERIALIZATION_FILE_FOLDER;
    }

    /**
     * Sets the folder where the factory is saved.
     *
     * @param serializationFolder the folder where the factory is saved
     */
    public static void setSerializationFolder(String serializationFolder) {
        PTMFactory.SERIALIZATION_FILE_FOLDER = serializationFolder;
    }

    /**
     * Sets the default modifications.
     */
    private void setDefaultModifications() {

        // Acetylation of K
        AtomChain atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        AtomChain atomChainRemoved = null;
        AminoAcidPattern aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        String ptmName = "Acetylation of K";
        PTM ptm = new PTM(PTM.MODAA, ptmName, "ace", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1", "Acetyl", null));
        ptm.addReporterIon(ReporterIon.ACE_K_126);
        ptm.addReporterIon(ReporterIon.ACE_K_143);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Acetylation of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Acetylation of peptide N-term"; // note: if name changed also change in TandemProcessBuilder
        ptm = new PTM(PTM.MODNP, ptmName, "ace", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1", "Acetyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Acetylation of protein N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Acetylation of protein N-term"; // note: if name changed also change in TandemProcessBuilder of SearchGUI and PsmImporter of PeptideShaker
        ptm = new PTM(PTM.MODN, ptmName, "ace", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1", "Acetyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Carbamidomethylation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Carbamidomethylation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "cmm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:4", "Carbamidomethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Carbamidomethylation of E
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("E");
        ptmName = "Carbamidomethylation of E";
        ptm = new PTM(PTM.MODAA, ptmName, "cmm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:4", "Carbamidomethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Carbamidomethylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Carbamidomethylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "cmm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:4", "Carbamidomethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Oxidation of M
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("M");
        ptmName = "Oxidation of M";
        ptm = new PTM(PTM.MODAA, ptmName, "ox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:35", "Oxidation", null));
        ptm.addNeutralLoss(NeutralLoss.CH4OS);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Oxidation of P
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("P");
        ptmName = "Oxidation of P";
        ptm = new PTM(PTM.MODAA, ptmName, "ox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:35", "Oxidation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Oxidation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Oxidation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "ox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:35", "Oxidation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Oxidation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Oxidation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "ox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:35", "Oxidation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dioxydation of M
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("M");
        ptmName = "Dioxidation of M";
        ptm = new PTM(PTM.MODAA, ptmName, "diox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:425", "Dioxidation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dioxydation of M
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("W");
        ptmName = "Dioxidation of W";
        ptm = new PTM(PTM.MODAA, ptmName, "diox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:425", "Dioxidation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trioxidation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Trioxidation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "triox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:345", "Trioxidation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Phosphorylation of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0));
        atomChainAdded.append(new AtomImpl(Atom.P, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("S");
        ptmName = "Phosphorylation of S";
        ptm = new PTM(PTM.MODAA, ptmName, "p", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:21", "Phospho", null));
        ptm.addNeutralLoss(NeutralLoss.H3PO4);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Phosphorylation of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0));
        atomChainAdded.append(new AtomImpl(Atom.P, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("T");
        ptmName = "Phosphorylation of T";
        ptm = new PTM(PTM.MODAA, ptmName, "p", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:21", "Phospho", null));
        ptm.addNeutralLoss(NeutralLoss.H3PO4);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Phosphorylation of Y
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0));
        atomChainAdded.append(new AtomImpl(Atom.P, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("Y");
        ptmName = "Phosphorylation of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "p", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:21", "Phospho", null));
        ptm.addNeutralLoss(NeutralLoss.HPO3);
        ptm.addReporterIon(ReporterIon.PHOSPHO_Y);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Arg6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("R");
        ptmName = "Arginine 13C(6)";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:188", "Label:13C(6)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Arg10
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 4);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 4);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("R");
        ptmName = "Arginine 13C(6) 15N(4)";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:267", "Label:13C(6)15N(4)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Lys4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 4);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Lysine 2H(4)";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:481", "Label:2H(4)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Lys6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Lysine 13C(6)";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:188", "Label:13C(6)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Lys8
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 2);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Lysine 13C(6) 15N(2)";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:259", "Label:13C(6)15N(2)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pro5
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 5);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 5);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("P");
        ptmName = "Proline 13C(5)";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:772", "Label:13C(5)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // 4-Hydroxyloproline
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 5);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 9);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("P");
        ptmName = "4-Hydroxyproline";
        ptm = new PTM(PTM.MODAA, ptmName, "hydroxy", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:", "", null)); // @TODO: add cv term...
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Leu7
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("L");
        ptmName = "Leucine 13C(6) 15N(1)";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:695", "Label:13C(6)15N(1)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Ile7
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("I");
        ptmName = "Isoleucine 13C(6) 15N(1)";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:695", "Label:13C(6)15N(1)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Label of K 2H4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 4);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Label of K 2H(4)";
        ptm = new PTM(PTM.MODAA, ptmName, "2H(4)", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:481", "Label:2H(4)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of K 2H4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Dimethylation of K 2H(4)";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:199 ", "Dimethyl:2H(4)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of K 2H6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Dimethylation of K 2H(6)";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth6", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1291", "Dimethyl:2H(6)", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of K 2H(6) 13C(2)
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Dimethylation of K 2H(6) 13C(2)";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth8", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:330", " Dimethyl:2H(6)13C(2)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of peptide N-term 2H4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Dimethylation of peptide N-term 2H(4)";
        ptm = new PTM(PTM.MODNP, ptmName, "dimeth4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:199 ", "Dimethyl:2H(4)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of peptide N-term 2H6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = null;
        ptmName = "Dimethylation of peptide N-term 2H(6)";
        ptm = new PTM(PTM.MODNP, ptmName, "dimeth6", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD: 1291", "Dimethyl:2H(6)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of peptide N-term 2H(6) 13C(2)
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = null;
        ptmName = "Dimethylation of peptide N-term 2H(6) 13C(2)";
        ptm = new PTM(PTM.MODNP, ptmName, "dimeth8", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:330", " Dimethyl:2H(6)13C(2)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // 18O(2) of peptide C-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 2), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 2);
        aminoAcidPattern = null;
        ptmName = "18O(2) of peptide C-term";
        ptm = new PTM(PTM.MODCP, ptmName, "18O", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD: 193", "Label:18O(2)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // 18O(1) of peptide C-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 2), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        aminoAcidPattern = null;
        ptmName = "18O(1) of peptide C-term";
        ptm = new PTM(PTM.MODCP, ptmName, "18O", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:258", "Label:18O(1)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICAT-0
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 10);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 17);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "ICAT-O";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:105", "ICAT-C", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICAT-9
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 9);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 17);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "ICAT-9";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:106", "ICAT-C:13C(9)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL0 of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "ICPL0 of K";
        ptm = new PTM(PTM.MODAA, ptmName, "icpl0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:365", "ICPL", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL0 of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "ICPL0 of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "icpl0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:365", "ICPL", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL4 of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "ICPL4 of K";
        ptm = new PTM(PTM.MODAA, ptmName, "icpl4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:687", "ICPL:2H(4)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL4 of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = null;
        ptmName = "ICPL4 of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "icpl4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:687", "ICPL:2H(4)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL6 of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "ICPL6 of K";
        ptm = new PTM(PTM.MODAA, ptmName, "icpl6", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:364", "ICPL:13C(6)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL6 of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "ICPL6 of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "icpl6", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:364", "ICPL:13C(6)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL10 of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "ICPL10 of K";
        ptm = new PTM(PTM.MODAA, ptmName, "icpl10", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:866", "ICPL:13C(6)2H(4)", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL10 of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = null;
        ptmName = "ICPL10 of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "icpl10", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:866", "ICPL:13C(6)2H(4)", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of K light
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "mTRAQ of K light";
        ptm = new PTM(PTM.MODAA, ptmName, "mTRAQ0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD: 888", "mTRAQ", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of peptide N-term light
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "mTRAQ of peptide N-term light";
        ptm = new PTM(PTM.MODNP, ptmName, "mTRAQ0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:888", "mTRAQ", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of K 13C3 15N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "mTRAQ of K 13C(3) 15N";
        ptm = new PTM(PTM.MODAA, ptmName, "mTRAQ4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:889", "mTRAQ:13C(3)15N(1) ", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of peptide N-term 13C3 15N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "mTRAQ of peptide N-term 13C(3) 15N";
        ptm = new PTM(PTM.MODNP, ptmName, "mTRAQ4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:889", "mTRAQ:13C(3)15N(1) ", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of K 13C6 15N2
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "mTRAQ of 13C(6) 15N(2)";
        ptm = new PTM(PTM.MODAA, ptmName, "mTRAQ8", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1302", "mTRAQ:13C(6)15N(2) ", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of peptide N-term 13C3 15N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "mTRAQ of peptide N-term 13C(6) 15N(2)";
        ptm = new PTM(PTM.MODNP, ptmName, "mTRAQ8", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1302", "mTRAQ:13C(6)15N(2) ", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 4-plex of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "iTRAQ 4-plex of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:214", "iTRAQ4plex", null)); // @TODO: check cv term and mass!!!
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_117);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 4-plex of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "iTRAQ 4-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:214", "iTRAQ4plex", null)); // @TODO: check cv term and mass!!!
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_117);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 4-plex of Y
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("Y");
        ptmName = "iTRAQ 4-plex of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:214", "iTRAQ4plex", null)); // @TODO: check cv term and mass!!!
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_117);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 8-plex of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 24);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "iTRAQ 8-plex of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:730", "iTRAQ8plex", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_113);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_117);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_118);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_119);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_121);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 8-plex of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 24);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "iTRAQ 8-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:730", "iTRAQ8plex", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_113);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_117);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_118);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_119);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_121);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 8-plex of Y
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 24);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("Y");
        ptmName = "iTRAQ 8-plex of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:730", "iTRAQ8plex", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_113);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_117);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_118);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_119);
        ptm.addReporterIon(ReporterIon.iTRAQ8Plex_121);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 2-plex of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 11);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "TMT 2-plex of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:738", "TMT2plex", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 2-plex of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 11);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 2-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:738", "TMT2plex", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 6-plex of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "TMT 6-plex of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 6-plex of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 6-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 6-plex + K+4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 6-plex of K+4";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 6-plex of K+6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 10);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 6-plex of K+6";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 6-plex of K+8
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 10);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 6-plex of K+8";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 10-plex of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "TMT 10-plex of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // note: "PSI: Alt. Description: Also applies to TMT10plex", i.e., no term for 10-plex
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 10-plex of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 10-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // note: "PSI: Alt. Description: Also applies to TMT10plex", i.e., no term for 10-plex
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 10-plex + K+4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 10-plex of K+4";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 10-plex of K+6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 10);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 10-plex of K+6";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 10-plex of K+8
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 10);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 10-plex of K+8";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 11-plex of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "TMT 11-plex of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // note: "PSI: Alt. Description: Also applies to TMT10plex", i.e., no term for 10-plex (no mention of 11-plex though...)
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_131C);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131C_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 11-plex of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 11-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // note: "PSI: Alt. Description: Also applies to TMT10plex", i.e., no term for 10-plex (no mention of 11-plex though...)
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_131C);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131C_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 11-plex + K+4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 11-plex of K+4";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131C_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 11-plex of K+6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 10);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 11-plex of K+6";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131C_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 11-plex of K+8
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 10);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "TMT 11-plex of K+8";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:737", "TMT6plex", null)); // @TODO: add cv term
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_128N);
        ptm.addReporterIon(ReporterIon.TMT_129C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_130N);
        ptm.addReporterIon(ReporterIon.TMT_131);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131C_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iodoTMT zero of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 28);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "iodoTMT zero of C";
        ptm = new PTM(PTM.MODAA, ptmName, "iodoTMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1341", "iodoTMT", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.iodoTMT_zero);
        ptm.addReporterIon(ReporterIon.iodoTMT_zero_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iodoTMT 6-plex of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 28);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "iodoTMT 6-plex of C";
        ptm = new PTM(PTM.MODAA, ptmName, "iodoTMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1342", "iodoTMT", null)); // note: does not have a PSI name, using interim name
        ptm.addReporterIon(ReporterIon.iodoTMT_126);
        ptm.addReporterIon(ReporterIon.iodoTMT_127);
        ptm.addReporterIon(ReporterIon.iodoTMT_128);
        ptm.addReporterIon(ReporterIon.iodoTMT_129);
        ptm.addReporterIon(ReporterIon.iodoTMT_130);
        ptm.addReporterIon(ReporterIon.iodoTMT_131);
        ptm.addReporterIon(ReporterIon.iodoTMT_126_ETD);
        ptm.addReporterIon(ReporterIon.iodoTMT_127_ETD);
        ptm.addReporterIon(ReporterIon.iodoTMT_128_ETD);
        ptm.addReporterIon(ReporterIon.iodoTMT_129_ETD);
        ptm.addReporterIon(ReporterIon.iodoTMT_130_ETD);
        ptm.addReporterIon(ReporterIon.iodoTMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Ubiquitination of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Ubiquitination of K";
        ptm = new PTM(PTM.MODAA, ptmName, "ub", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:121", "GG", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0));
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Methylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:34", "Methyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0));
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("R");
        ptmName = "Methylation of R";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:34", "Methyl", null));
        ptm.addReporterIon(ReporterIon.METHYL_R_70);
        ptm.addReporterIon(ReporterIon.METHYL_R_87);
        ptm.addReporterIon(ReporterIon.METHYL_R_112);
        ptm.addReporterIon(ReporterIon.METHYL_R_115);
        ptm.addReporterIon(ReporterIon.METHYL_R_143);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of E
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("E");
        ptmName = "Methylation of E";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:34", "Methyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Methylation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:34", "Methyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of D
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("D");
        ptmName = "Methylation of D";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:34", "Methyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("S");
        ptmName = "Methylation of S";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:34", "Methyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Dimethylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:36", "Dimethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Dimethylation of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "dimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:36", "Dimethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("R");
        ptmName = "Dimethylation of R";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:36", "Dimethyl", null));
        ptm.addReporterIon(ReporterIon.DI_METHYL_R_112);
        ptm.addReporterIon(ReporterIon.DI_METHYL_R_115);
        ptm.addReporterIon(ReporterIon.DI_METHYL_R_157);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trimethylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 6);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Trimethylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "trimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:37", "Trimethyl", null));
        ptm.addNeutralLoss(NeutralLoss.C3H9N);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trimethylation of R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 6);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("R");
        ptmName = "Trimethylation of R";
        ptm = new PTM(PTM.MODAA, ptmName, "trimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:37", "Trimethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trimethylation of protein N-term A
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 6);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("A");
        ptmName = "Trimethylation of protein N-term A";
        ptm = new PTM(PTM.MODN, ptmName, "trimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:37", "Trimethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pyrolidone from E
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("E");
        ptmName = "Pyrolidone from E"; // note: if name changed also change in TandemProcessBuilder of SearchGUI and PsmImporter of PeptideShaker
        ptm = new PTM(PTM.MODNPAA, ptmName, "pyro", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:27", "Glu->pyro-Glu", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pyrolidone from Q
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 3);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("Q");
        ptmName = "Pyrolidone from Q"; // note: if name changed also change in TandemProcessBuilder of SearchGUI and PsmImporter of PeptideShaker
        ptm = new PTM(PTM.MODNPAA, ptmName, "pyro", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:28", "Gln->pyro-Glu", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pyrolidone from carbamidomethylated C
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 3);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Pyrolidone from carbamidomethylated C"; // note: if name changed also change in TandemProcessBuilder of SearchGUI and PsmImporter of PeptideShaker
        ptm = new PTM(PTM.MODNPAA, ptmName, "pyro", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:385", "Ammonia-loss", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // HexNAc of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 13);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 5);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("S");
        ptmName = "HexNAc of S";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:43", "HexNAc", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // HexNAc of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 13);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 5);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("T");
        ptmName = "HexNAc of T";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:43", "HexNAc", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hex(1)NAc(1) of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 14);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 23);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 10);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("S");
        ptmName = "Hex(1)NAc(1) of S";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:793", "Hex(1)HexNAc(1) ", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hex(1)NAc(1) of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 14);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 23);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 10);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("T");
        ptmName = "Hex(1)NAc(1) of T";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:793", "Hex(1)HexNAc(1) ", null)); // note: does not have a PSI name, using interim name
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hexose of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 10);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 5);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Hexose of K";
        ptm = new PTM(PTM.MODAA, ptmName, "hex", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:41", "Hex", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hex(5) HexNAc(4) NeuAc(2) of N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 84);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 136);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 61);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 6);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("N");
        ptmName = "Hex(5) HexNAc(4) NeuAc(2) of N";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:", "", null)); // @TODO: add cv term...
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hex(5) HexNAc(4) NeuAc(2) Na of N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 84);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 135);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 61);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.Na, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("N");
        ptmName = "Hex(5) HexNAc(4) NeuAc(2) Na of N";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:1408", "Hex(5)HexNAc(4)NeuAc(2) ", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // SUMO-2/3 Q87R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 18);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 29);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 8);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "SUMO-2/3 Q87R";
        ptm = new PTM(PTM.MODAA, ptmName, "sumo", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        //ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:", "", null)); // @TODO: add cv term...
        ptm.addReporterIon(ReporterIon.QQ);
        ptm.addReporterIon(ReporterIon.QQ_H2O);
        ptm.addReporterIon(ReporterIon.QQT);
        ptm.addReporterIon(ReporterIon.QQT_H2O);
        ptm.addReporterIon(ReporterIon.QQTG);
        ptm.addReporterIon(ReporterIon.QQTG_H2O);
        ptm.addReporterIon(ReporterIon.QQTGG);
        ptm.addReporterIon(ReporterIon.QQTGG_H2O);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Deamidation of N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("N");
        ptmName = "Deamidation of N";
        ptm = new PTM(PTM.MODAA, ptmName, "deam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:7", "Deamidated", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Deamidation of Q
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("Q");
        ptmName = "Deamidation of Q";
        ptm = new PTM(PTM.MODAA, ptmName, "deam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:7", "Deamidated", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Deamidation of N 18O
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 2), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("N");
        ptmName = "Deamidation of N 18O";
        ptm = new PTM(PTM.MODAA, ptmName, "deam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD: 366", "Deamidation in presence of O18", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Carbamylation of protein N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0));
        atomChainAdded.append(new AtomImpl(Atom.H, 0));
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Carbamilation of protein N-term";
        ptm = new PTM(PTM.MODN, ptmName, "cm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:5", "Carbamyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Carbamylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0));
        atomChainAdded.append(new AtomImpl(Atom.H, 0));
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Carbamilation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "cm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:5", "Carbamyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Acetaldehyde +26
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Acetaldehyde +26";
        ptm = new PTM(PTM.MODNP, ptmName, "ace", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:254", "Delta:H(2)C(2)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sodium adduct to D
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.Na, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("D");
        ptmName = "Sodium adduct to D";
        ptm = new PTM(PTM.MODAA, ptmName, "Na", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:30", "Cation:Na", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sodium adduct to E
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.Na, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("E");
        ptmName = "Sodium adduct to E";
        ptm = new PTM(PTM.MODAA, ptmName, "Na", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:30", "Cation:Na", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Amidation of the peptide C-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        aminoAcidPattern = null;
        ptmName = "Amidation of the peptide C-term";
        ptm = new PTM(PTM.MODCP, ptmName, "am", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:2", "Amidated", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Amidation of the protein C-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        aminoAcidPattern = null;
        ptmName = "Amidation of the protein C-term";
        ptm = new PTM(PTM.MODC, ptmName, "am", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:2", "Amidated", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sulfation of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("S");
        ptmName = "Sulfation of S";
        ptm = new PTM(PTM.MODAA, ptmName, "s", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:40", "Sulfo", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sulfation of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("T");
        ptmName = "Sulfation of T";
        ptm = new PTM(PTM.MODAA, ptmName, "s", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:40", "Sulfo", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sulfation of Y
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("Y");
        ptmName = "Sulfation of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "s", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:40", "Sulfo", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Palmitoylation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 30);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Palmitoylation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "palm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:47", "Palmitoylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Palmitoylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 30);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Palmitoylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "palm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:47", "Palmitoylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Palmitoylation of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 30);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("S");
        ptmName = "Palmitoylation of S";
        ptm = new PTM(PTM.MODAA, ptmName, "palm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:47", "Palmitoylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Palmitoylation of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 30);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("T");
        ptmName = "Palmitoylation of T";
        ptm = new PTM(PTM.MODAA, ptmName, "palm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:47", "Palmitoylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Palmitoylation of protein N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 30);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 16);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Palmitoylation of protein N-term";
        ptm = new PTM(PTM.MODN, ptmName, "palm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:47", "Palmitoylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Formylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Formylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "form", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:122", "Formylation", null));
        ptm.addReporterIon(ReporterIon.FORMYL_K);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Formylation of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("S");
        ptmName = "Formylation of S";
        ptm = new PTM(PTM.MODAA, ptmName, "form", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:122", "Formylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Formylation of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("T");
        ptmName = "Formylation of T";
        ptm = new PTM(PTM.MODAA, ptmName, "form", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:122", "Formylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Formylation of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Formylation of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "form", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:122", "Formylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Formylation of protein N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Formylation of protein N-term";
        ptm = new PTM(PTM.MODN, ptmName, "form", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:122", "Formylation", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Propionyl of K light
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Propionyl of K light";
        ptm = new PTM(PTM.MODAA, ptmName, "prop", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:58", "Propionyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Propionyl of peptide N-term light
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Propionyl of peptide N-term light";
        ptm = new PTM(PTM.MODNP, ptmName, "prop", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:58", "Propionyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Propionyl of K heavy
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Propionyl of K heavy";
        ptm = new PTM(PTM.MODAA, ptmName, "prop", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:59", "Propionyl:13C(3)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Propionyl of peptide N-term heavy
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Propionyl of peptide N-term heavy";
        ptm = new PTM(PTM.MODNP, ptmName, "prop", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:59", "Propionyl:13C(3)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trideuterated Methyl Ester of D
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("D");
        ptmName = "Trideuterated Methyl Ester of D";
        ptm = new PTM(PTM.MODAA, ptmName, "methyl(d3)", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:298", "Methyl:2H(3)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trideuterated Methyl Ester of E
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("E");
        ptmName = "Trideuterated Methyl Ester of E";
        ptm = new PTM(PTM.MODAA, ptmName, "methyl(d3)", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:298", "Methyl:2H(3)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trideuterated Methyl Ester of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Trideuterated Methyl Ester of K";
        ptm = new PTM(PTM.MODAA, ptmName, "methyl(d3)", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:298", "Methyl:2H(3)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trideuterated Methyl Ester of R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("R");
        ptmName = "Trideuterated Methyl Ester of R";
        ptm = new PTM(PTM.MODAA, ptmName, "methyl(d3)", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:298", "Methyl:2H(3)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trideuterated Methyl Ester of peptide C-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = null;
        ptmName = "Trideuterated Methyl Ester of peptide C-term";
        ptm = new PTM(PTM.MODNP, ptmName, "methyl(d3)", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:298", "Methyl:2H(3)", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Carboxymethylation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Carboxymethylation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "carbox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:6", "Carboxymethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Farnesylation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 24);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 15);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Farnesylation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "far", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:44", "Farnesyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Geranyl-geranyl of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 32);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 20);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Geranyl-geranyl of C";
        ptm = new PTM(PTM.MODAA, ptmName, "geranyl", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:48", "GeranylGeranyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Guanidination of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Guanidination of K";
        ptm = new PTM(PTM.MODAA, ptmName, "guan", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:52", "Guanidinyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Guanidination of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Guanidination of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "guan", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:52", "Guanidinyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Homoserine of peptide C-term M
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.S, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("M");
        ptmName = "Homoserine of peptide C-term M";
        ptm = new PTM(PTM.MODCPAA, ptmName, "hse", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:10", "Met->Hse", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Homoserine lactone of peptide C-term M
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 4);
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.S, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("M");
        ptmName = "Homoserine lactone of peptide C-term M";
        ptm = new PTM(PTM.MODCPAA, ptmName, "hsel", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:11", "Met->Hsl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Lipoyl of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Lipoyl of K";
        ptm = new PTM(PTM.MODAA, ptmName, "lip", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:42", "Lipoyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylthio of D
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("D");
        ptmName = "Methylthio of D";
        ptm = new PTM(PTM.MODAA, ptmName, "mmts", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:39", "Methylthio", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylthio of N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("N");
        ptmName = "Methylthio of N";
        ptm = new PTM(PTM.MODAA, ptmName, "mmts", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:39", "Methylthio", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylthio of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Methylthio of C";
        ptm = new PTM(PTM.MODAA, ptmName, "mmts", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:39", "Methylthio", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // NIPCAM of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 9);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 5);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "NIPCAM of C";
        ptm = new PTM(PTM.MODAA, ptmName, "nipcam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:17", "NIPCAM", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Propionamide of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 5);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Propionamide of C";
        ptm = new PTM(PTM.MODAA, ptmName, "propam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:24", "Propionamide", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Propionamide of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 5);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("K");
        ptmName = "Propionamide of K";
        ptm = new PTM(PTM.MODAA, ptmName, "propam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:24", "Propionamide", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Propionamide of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 5);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Propionamide of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "propam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:24", "Propionamide", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pyridylethyl of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Pyridylethyl of C";
        ptm = new PTM(PTM.MODAA, ptmName, "pyri", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:31", "Pyridylethyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dehydration of S
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("S");
        ptmName = "Dehydration of S";
        ptm = new PTM(PTM.MODAA, ptmName, "dehyd", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:23", "Dehydrated", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dehydration of T
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("T");
        ptmName = "Dehydration of T";
        ptm = new PTM(PTM.MODAA, ptmName, "dehyd", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:23", "Dehydrated", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Nethylmaleimide of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Nethylmaleimide of C";
        ptm = new PTM(PTM.MODAA, ptmName, "nem", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:108", "Nethylmaleimide", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Glutathione of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 15);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 10);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Glutathione of C";
        ptm = new PTM(PTM.MODAA, ptmName, "glut", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:55", "Glutathione", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // FormylMet of protein N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 9);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "FormylMet of protein N-term";
        ptm = new PTM(PTM.MODN, ptmName, "nmet", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:107", "FormylMet", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Didehydro of T
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("T");
        ptmName = "Didehydro of T";
        ptm = new PTM(PTM.MODAA, ptmName, "didehyro", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:401", "Didehydro", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Thioacyl of peptide N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.S, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Thioacyl of peptide N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "thioacyl", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:126", "Thioacyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Diiodination of Y
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.I, 0), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("Y");
        ptmName = "Diiodination of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "diiodo", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:130", "Diiodo", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Citrullination of R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("R");
        ptmName = "Citrullination of R";
        ptm = new PTM(PTM.MODAA, ptmName, "cit", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:7", "Deamidated", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // S-nitrosylation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "S-nitrosylation";
        ptm = new PTM(PTM.MODAA, ptmName, "nitrosyl", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:275", "Nitrosyl", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Heme B of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 32);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 34);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.Fe, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("C");
        ptmName = "Heme B of C";
        ptm = new PTM(PTM.MODAA, ptmName, "heme", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:390", "Heme", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Heme B of H
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 32);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 34);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.Fe, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString("H");
        ptmName = "Heme B of H";
        ptm = new PTM(PTM.MODAA, ptmName, "heme", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.setCvTerm(new CvTerm("UNIMOD", "UNIMOD:390", "Heme", null));
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);
    }

    /**
     * Returns the PSI-MOD accession number. Null if not set.
     *
     * @param ptmName the name of the PTM
     * @return the PSI-MOD accession number, null if not set
     */
    public String getPsiModAccession(String ptmName) {

        if (ptmName.equalsIgnoreCase("18O(1) of peptide C-term")) {
            return "00581";
        } else if (ptmName.equalsIgnoreCase("18O(2) of peptide C-term")) {
            return "00546";
        } else if (ptmName.equalsIgnoreCase("4-Hydroxyproline")) {
            return "00678"; // @TODO: maps to parent term "hydroxylated proline"...
        } else if (ptmName.equalsIgnoreCase("Acetaldehyde +26")) {
            return "00577"; // @TODO: not peptide n-term specific
        } else if (ptmName.equalsIgnoreCase("Acetylation of K")) {
            return "00723";
        } else if (ptmName.equalsIgnoreCase("Acetylation of peptide N-term")) {
            return "01458";
        } else if (ptmName.equalsIgnoreCase("Acetylation of protein N-term")) {
            return "01458";
        } else if (ptmName.equalsIgnoreCase("Amidation of the peptide C-term")) {
            return "00883";
        } else if (ptmName.equalsIgnoreCase("Amidation of the protein C-term")) {
            return "00883";
        } else if (ptmName.equalsIgnoreCase("Arginine 13C(6)")) {
            return "01331";
        } else if (ptmName.equalsIgnoreCase("Arginine 13C(6) 15N(4)")) {
            return "00587";
        } else if (ptmName.equalsIgnoreCase("Carbamidomethylation of C")) {
            return "01060";
        } else if (ptmName.equalsIgnoreCase("Carbamidomethylation of E")) {
            return "01216";
        } else if (ptmName.equalsIgnoreCase("Carbamidomethylation of K")) {
            return "01212";
        } else if (ptmName.equalsIgnoreCase("Carbamilation of K")) {
            return "01678";
        } else if (ptmName.equalsIgnoreCase("Carbamilation of protein N-term")) {
            return "01679";
        } else if (ptmName.equalsIgnoreCase("Carboxymethylation of C")) {
            return "01061";
        } else if (ptmName.equalsIgnoreCase("Citrullination of R")) {
            return "00400";
        } else if (ptmName.equalsIgnoreCase("Deamidation of N")) {
            return "00684";
        } else if (ptmName.equalsIgnoreCase("Deamidation of N 18O")) {
            return "00791";
        } else if (ptmName.equalsIgnoreCase("Deamidation of Q")) {
            return "00685";
        } else if (ptmName.equalsIgnoreCase("Dehydration of S")) {
            return "00189";
        } else if (ptmName.equalsIgnoreCase("Dehydration of T")) {
            return "00190"; // @TODO: maps to parent term "dehydrobutyrine (Thr)"...
        } else if (ptmName.equalsIgnoreCase("Didehydro of T")) {
            return "01345";
        } else if (ptmName.equalsIgnoreCase("Diiodination of Y")) {
            return "01140";
        } else if (ptmName.equalsIgnoreCase("Dimethylation of K")) {
            return "00084";
        } else if (ptmName.equalsIgnoreCase("Dimethylation of K 2H(4)")) {
            return "01254";
        } else if (ptmName.equalsIgnoreCase("Dimethylation of K 2H(6)")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Dimethylation of K 2H(6) 13C(2)")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Dimethylation of R")) {
            return "00783";
        } else if (ptmName.equalsIgnoreCase("Dimethylation of peptide N-term")) {
            return "01686";
        } else if (ptmName.equalsIgnoreCase("Dimethylation of peptide N-term 2H(4)")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Dimethylation of peptide N-term 2H(6)")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Dimethylation of peptide N-term 2H(6) 13C(2)")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Dioxidation of M")) {
            return "00428"; // @TODO: maps to parent term "dihydroxylated residue"...
        } else if (ptmName.equalsIgnoreCase("Dioxidation of W")) {
            return "00428"; // @TODO: maps to parent term "dihydroxylated residue"...
        } else if (ptmName.equalsIgnoreCase("Farnesylation of C")) {
            return "00111";
        } else if (ptmName.equalsIgnoreCase("FormylMet of protein N-term")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Formylation of K")) {
            return "00216";
        } else if (ptmName.equalsIgnoreCase("Formylation of S")) {
            return "01222";
        } else if (ptmName.equalsIgnoreCase("Formylation of T")) {
            return "01221";
        } else if (ptmName.equalsIgnoreCase("Formylation of peptide N-term")) {
            return "00409"; // @TODO: maps to parent term "N-formylated residue"...
        } else if (ptmName.equalsIgnoreCase("Formylation of protein N-term")) {
            return "00409"; // @TODO: maps to parent term "N-formylated residue"...
        } else if (ptmName.equalsIgnoreCase("Geranyl-geranyl of C")) {
            return "00113";
        } else if (ptmName.equalsIgnoreCase("Glutathione of C")) {
            return "00234";
        } else if (ptmName.equalsIgnoreCase("Guanidination of K")) {
            return "00445";
        } else if (ptmName.equalsIgnoreCase("Guanidination of peptide N-term")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Heme B of C")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Heme B of H")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Hex(1)NAc(1) of S")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Hex(1)NAc(1) of T")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Hex(5) HexNAc(4) NeuAc(2) Na of N")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Hex(5) HexNAc(4) NeuAc(2) of N")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("HexNAc of S")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("HexNAc of T")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Hexose of K")) {
            return "01347";
        } else if (ptmName.equalsIgnoreCase("Homoserine lactone of peptide C-term M")) {
            return "00404";
        } else if (ptmName.equalsIgnoreCase("Homoserine of peptide C-term M")) {
            return "00403";
        } else if (ptmName.equalsIgnoreCase("ICAT-9")) {
            return "00481";
        } else if (ptmName.equalsIgnoreCase("ICAT-O")) {
            return "00480";
        } else if (ptmName.equalsIgnoreCase("ICPL0 of K")) {
            return "01230";
        } else if (ptmName.equalsIgnoreCase("ICPL0 of peptide N-term")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("ICPL10 of K")) {
            return "01287"; // @TODO: the mass in Unimod and PSI-MOD is not the same!
        } else if (ptmName.equalsIgnoreCase("ICPL10 of peptide N-term")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("ICPL4 of K")) {
            return "01359";
        } else if (ptmName.equalsIgnoreCase("ICPL4 of peptide N-term")) {
            return "01358";
        } else if (ptmName.equalsIgnoreCase("ICPL6 of K")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("ICPL6 of peptide N-term")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Isoleucine 13C(6) 15N(1)")) {
            return "01286";
        } else if (ptmName.equalsIgnoreCase("Label of K 2H(4)")) {
            return "00942";
        } else if (ptmName.equalsIgnoreCase("Leucine 13C(6) 15N(1)")) {
            return "01285";
        } else if (ptmName.equalsIgnoreCase("Lipoyl of K")) {
            return "00127";
        } else if (ptmName.equalsIgnoreCase("Lysine 13C(6)")) {
            return "01334";
        } else if (ptmName.equalsIgnoreCase("Lysine 13C(6) 15N(2)")) {
            return "00582";
        } else if (ptmName.equalsIgnoreCase("Lysine 2H(4)")) {
            return "00942";
        } else if (ptmName.equalsIgnoreCase("Methylation of C")) {
            return "00660"; // @TODO: maps to parent term "methylated cysteine"
        } else if (ptmName.equalsIgnoreCase("Methylation of D")) {
            return "00079";
        } else if (ptmName.equalsIgnoreCase("Methylation of E")) {
            return "00081";
        } else if (ptmName.equalsIgnoreCase("Methylation of K")) {
            return "00085";
        } else if (ptmName.equalsIgnoreCase("Methylation of R")) {
            return "00414"; // @TODO: maps to parent term "monomethylated L-arginine"...
        } else if (ptmName.equalsIgnoreCase("Methylation of S")) {
            return "01782";
        } else if (ptmName.equalsIgnoreCase("Methylthio of C")) {
            return "00110";
        } else if (ptmName.equalsIgnoreCase("Methylthio of D")) {
            return "00237";
        } else if (ptmName.equalsIgnoreCase("Methylthio of N")) {
            return "00325";
        } else if (ptmName.equalsIgnoreCase("NIPCAM of C")) {
            return "00410";
        } else if (ptmName.equalsIgnoreCase("Nethylmaleimide of C")) {
            return "00483";
        } else if (ptmName.equalsIgnoreCase("Oxidation of C")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Oxidation of K")) {
            return "01047"; // @TODO: maps to parent term "monohydroxylated lysine"...
        } else if (ptmName.equalsIgnoreCase("Oxidation of M")) {
            return "";
        } else if (ptmName.equalsIgnoreCase("Oxidation of P")) {
            return null; // @TODO: add mapping?
        } else if (ptmName.equalsIgnoreCase("Palmitoylation of C")) {

        } else if (ptmName.equalsIgnoreCase("Palmitoylation of K")) {

        } else if (ptmName.equalsIgnoreCase("Palmitoylation of S")) {

        } else if (ptmName.equalsIgnoreCase("Palmitoylation of T")) {

        } else if (ptmName.equalsIgnoreCase("Palmitoylation of protein N-term")) {

        } else if (ptmName.equalsIgnoreCase("Phosphorylation of S")) {

        } else if (ptmName.equalsIgnoreCase("Phosphorylation of T")) {

        } else if (ptmName.equalsIgnoreCase("Phosphorylation of Y")) {

        } else if (ptmName.equalsIgnoreCase("Proline 13C(5)")) {

        } else if (ptmName.equalsIgnoreCase("Propionamide of C")) {

        } else if (ptmName.equalsIgnoreCase("Propionamide of K")) {

        } else if (ptmName.equalsIgnoreCase("Propionamide of peptide N-term")) {

        } else if (ptmName.equalsIgnoreCase("Propionyl of K heavy")) {

        } else if (ptmName.equalsIgnoreCase("Propionyl of K light")) {

        } else if (ptmName.equalsIgnoreCase("Propionyl of peptide N-term heavy")) {

        } else if (ptmName.equalsIgnoreCase("Propionyl of peptide N-term light")) {

        } else if (ptmName.equalsIgnoreCase("Pyridylethyl of C")) {

        } else if (ptmName.equalsIgnoreCase("Pyrolidone from E")) {

        } else if (ptmName.equalsIgnoreCase("Pyrolidone from Q")) {

        } else if (ptmName.equalsIgnoreCase("Pyrolidone from carbamidomethylated C")) {

        } else if (ptmName.equalsIgnoreCase("S-nitrosylation")) {

        } else if (ptmName.equalsIgnoreCase("SUMO-2/3 Q87R")) {

        } else if (ptmName.equalsIgnoreCase("Sodium adduct to D")) {

        } else if (ptmName.equalsIgnoreCase("Sodium adduct to E")) {

        } else if (ptmName.equalsIgnoreCase("Sulfation of S")) {

        } else if (ptmName.equalsIgnoreCase("Sulfation of T")) {

        } else if (ptmName.equalsIgnoreCase("Sulfation of Y")) {

        } else if (ptmName.equalsIgnoreCase("TMT 10-plex of K")) {

        } else if (ptmName.equalsIgnoreCase("TMT 10-plex of K+4")) {

        } else if (ptmName.equalsIgnoreCase("TMT 10-plex of K+6")) {

        } else if (ptmName.equalsIgnoreCase("TMT 10-plex of K+8")) {

        } else if (ptmName.equalsIgnoreCase("TMT 10-plex of peptide N-term")) {

        } else if (ptmName.equalsIgnoreCase("TMT 11-plex of K")) {

        } else if (ptmName.equalsIgnoreCase("TMT 11-plex of K+4")) {

        } else if (ptmName.equalsIgnoreCase("TMT 11-plex of K+6")) {

        } else if (ptmName.equalsIgnoreCase("TMT 11-plex of K+8")) {

        } else if (ptmName.equalsIgnoreCase("TMT 11-plex of peptide N-term")) {

        } else if (ptmName.equalsIgnoreCase("TMT 2-plex of K")) {

        } else if (ptmName.equalsIgnoreCase("TMT 2-plex of peptide N-term")) {

        } else if (ptmName.equalsIgnoreCase("TMT 6-plex of K")) {

        } else if (ptmName.equalsIgnoreCase("TMT 6-plex of K+4")) {

        } else if (ptmName.equalsIgnoreCase("TMT 6-plex of K+6")) {

        } else if (ptmName.equalsIgnoreCase("TMT 6-plex of K+8")) {

        } else if (ptmName.equalsIgnoreCase("TMT 6-plex of peptide N-term")) {

        } else if (ptmName.equalsIgnoreCase("Thioacyl of peptide N-term")) {

        } else if (ptmName.equalsIgnoreCase("Trideuterated Methyl Ester of D")) {

        } else if (ptmName.equalsIgnoreCase("Trideuterated Methyl Ester of E")) {

        } else if (ptmName.equalsIgnoreCase("Trideuterated Methyl Ester of K")) {

        } else if (ptmName.equalsIgnoreCase("Trideuterated Methyl Ester of R")) {

        } else if (ptmName.equalsIgnoreCase("Trideuterated Methyl Ester of peptide C-term")) {

        } else if (ptmName.equalsIgnoreCase("Trimethylation of K")) {

        } else if (ptmName.equalsIgnoreCase("Trimethylation of R")) {

        } else if (ptmName.equalsIgnoreCase("Trimethylation of protein N-term A")) {

        } else if (ptmName.equalsIgnoreCase("Trioxidation of C")) {

        } else if (ptmName.equalsIgnoreCase("Ubiquitination of K")) {

        } else if (ptmName.equalsIgnoreCase("iTRAQ 4-plex of K")) {

        } else if (ptmName.equalsIgnoreCase("iTRAQ 4-plex of Y")) {

        } else if (ptmName.equalsIgnoreCase("iTRAQ 4-plex of peptide N-term")) {

        } else if (ptmName.equalsIgnoreCase("iTRAQ 8-plex of K")) {

        } else if (ptmName.equalsIgnoreCase("iTRAQ 8-plex of Y")) {

        } else if (ptmName.equalsIgnoreCase("iTRAQ 8-plex of peptide N-term")) {

        } else if (ptmName.equalsIgnoreCase("iodoTMT 6-plex of C")) {

        } else if (ptmName.equalsIgnoreCase("iodoTMT zero of C")) {

        } else if (ptmName.equalsIgnoreCase("mTRAQ of 13C(6) 15N(2)")) {

        } else if (ptmName.equalsIgnoreCase("mTRAQ of K 13C(3) 15N")) {

        } else if (ptmName.equalsIgnoreCase("mTRAQ of K light")) {

        } else if (ptmName.equalsIgnoreCase("mTRAQ of peptide N-term 13C(3) 15N")) {

        } else if (ptmName.equalsIgnoreCase("mTRAQ of peptide N-term 13C(6) 15N(2)")) {

        } else if (ptmName.equalsIgnoreCase("mTRAQ of peptide N-term light")) {

        }

        return null;
    }
}
