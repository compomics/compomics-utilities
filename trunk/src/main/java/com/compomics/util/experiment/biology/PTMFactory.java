package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.preferences.ModificationProfile;
import com.compomics.util.preferences.SequenceMatchingPreferences;
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
     * The name of the PTM factory back-up file.
     */
    private static String SERIALIZATION_FILE_NAME = "ptmFactory-3.50.0.cus";
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
                instance = (PTMFactory) SerializationUtils.readObject(savedFile);
            } catch (Exception e) {
                instance = new PTMFactory();
//                try {
//                    instance.saveFactory();
//                } catch (IOException ioe) {
//                    // cancel save
//                    ioe.printStackTrace();
//                }
            }
        }
        return instance;
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
        SerializationUtils.writeObject(instance, factoryFile);
    }

    /**
     * Returns a clone of the given ptm targetting a single amino acid instead
     * of a pattern.
     *
     * @param modification the modification of interest
     *
     * @return a clone of the given ptm targetting a single amino acid instead
     * of a pattern
     */
    public static PTM getSingleAAPTM(PTM modification) {
        if (!modification.isStandardSearch()) {
            return new PTM(modification.getType(), modification.getShortName(), modification.getName() + SINGLE_AA_SUFFIX, modification.getAtomChainAdded(), modification.getAtomChainRemoved(), modification.getPattern().getStandardSearchPattern());
        } else {
            return modification;
        }
    }

    /**
     * Returns a clone of the given ptm targetting a single amino acid instead
     * of a pattern.
     *
     * @param modificationName the name of the modification of interest
     *
     * @return a clone of the given ptm targetting a single amino acid instead
     * of a pattern
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
     * @return The desired PTM
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
        ModificationProfile modificationProfile = searchParameters.getModificationProfile();
        ArrayList<String> toCheck = new ArrayList<String>();
        for (String modification : modificationProfile.getBackedUpPtms()) {
            if (containsPTM(modification)) {
                PTM oldPTM = getPTM(modification);
                PTM newPTM = modificationProfile.getPtm(modification);
                if (!oldPTM.isSameAs(newPTM)) {
                    toCheck.add(modification);
                    if (overwrite) {
                        ptmMap.put(modification, modificationProfile.getPtm(modification));
                    }
                }
            } else {
                addUserPTM(modificationProfile.getPtm(modification));
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
    public HashMap<Integer, ArrayList<String>> getExpectedPTMs(ModificationProfile modificationProfile, Peptide peptide,
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
    public HashMap<Integer, ArrayList<String>> getExpectedPTMs(ModificationProfile modificationProfile, Peptide peptide, String ptmName,
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
    public void checkFixedModifications(ModificationProfile modificationProfile, Peptide peptide, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        ArrayList<ModificationMatch> toRemove = new ArrayList<ModificationMatch>();
        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            if (!modMatch.isVariable()) {
                toRemove.add(modMatch);
            }
        }
        for (ModificationMatch modMatch : toRemove) {
            peptide.getModificationMatches().remove(modMatch);
        }
        HashMap<Integer, Double> taken = new HashMap<Integer, Double>();

        for (String fixedModification : modificationProfile.getFixedModifications()) {
            PTM ptm = getPTM(fixedModification);
            if (ptm.getType() == PTM.MODAA) {
                for (int pos : peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences)) {
                    if (!taken.containsKey(pos)) {
                        taken.put(pos, ptm.getMass());
                        peptide.addModificationMatch(new ModificationMatch(fixedModification, false, pos));
                    } else if (taken.get(pos) != ptm.getMass()) {
                        throw new IllegalArgumentException("Attempting to put two fixed modifications of different masses ("
                                + taken.get(pos) + ", " + ptm.getMass() + ") at position " + pos + " in peptide " + peptide.getSequence() + ".");
                    }
                }
            } else if (ptm.getType() == PTM.MODC) {
                if (!peptide.isCterm(sequenceMatchingPreferences).isEmpty()) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
                }
            } else if (ptm.getType() == PTM.MODN) {
                if (!peptide.isNterm(sequenceMatchingPreferences).isEmpty()) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                }
            } else if (ptm.getType() == PTM.MODCAA) {
                String sequence = peptide.getSequence();
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(sequence.length())) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
                }
            } else if (ptm.getType() == PTM.MODNAA) {
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(1)) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                }
            } else if (ptm.getType() == PTM.MODCP) {
                peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
            } else if (ptm.getType() == PTM.MODNP) {
                peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
            } else if (ptm.getType() == PTM.MODCPAA) {
                String sequence = peptide.getSequence();
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(sequence.length())) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, sequence.length()));
                }
            } else if (ptm.getType() == PTM.MODNPAA) {
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(1)) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                }
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
    public void checkFixedModifications(ModificationProfile modificationProfile, Tag tag, SequenceMatchingPreferences sequenceMatchingPreferences)
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
                    Double modification = null;

                    for (String fixedModification : modificationProfile.getFixedModifications()) {
                        PTM ptm = getPTM(fixedModification);
                        if (ptm.getType() == PTM.MODAA) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(indexInTag)) {
                                if (modification == null) {
                                    modification = ptm.getMass();
                                    aminoAcidPattern.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                                } else if (modification != ptm.getMass()) {
                                    throw new IllegalArgumentException("Attempting to put two fixed modifications of different masses ("
                                            + modification + ", " + ptm.getMass() + ") at position " + aa + " in pattern "
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
        } else if (modification.toLowerCase().contains("ox")) {
            return Color.BLUE;
        } else if (modification.toLowerCase().contains("itraq")) {
            return Color.cyan;
        } else if (modification.toLowerCase().contains("tmt")) {
            return Color.cyan;
        } else if (modification.toLowerCase().contains("pyro")) {
            return Color.orange;
        } else if (modification.toLowerCase().contains("carbamido")) {
            return Color.LIGHT_GRAY;
        } else if (modification.toLowerCase().contains("ace")) {
            return new Color(153, 153, 0);
        } else if (modification.toLowerCase().contains("glyco")) {
            return Color.ORANGE;
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
    public String convertPridePtm(String pridePtmName, ModificationProfile modProfile, ArrayList<String> unknownPtms, boolean isFixed) {

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

            if (!modProfile.contains("itraq114 on k")) {
                modProfile.addFixedModification(getPTM("itraq114 on k"));
                prideParametersReport += "<br>" + "itraq114 on k" + " (assumed fixed)";
            }
            if (!modProfile.contains("itraq114 on nterm")) {
                modProfile.addFixedModification(getPTM("itraq114 on nterm"));
                prideParametersReport += "<br>" + "itraq114 on nterm" + " (assumed fixed)";
            }
            if (!modProfile.contains("itraq114 on y")) {
                modProfile.addVariableModification(getPTM("itraq114 on y"));
                prideParametersReport += "<br>" + "itraq114 on y" + " (assumed variable)";
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

            if (!modProfile.contains("itraq8plex:13c(6)15n(2) on k")) {
                modProfile.addFixedModification(getPTM("itraq8plex:13c(6)15n(2) on k"));
                prideParametersReport += "<br>" + "itraq8plex:13c(6)15n(2) on k" + " (assumed fixed)";
            }
            if (!modProfile.contains("itraq8plex:13c(6)15n(2) on nterm")) {
                modProfile.addFixedModification(getPTM("itraq8plex:13c(6)15n(2) on nterm"));
                prideParametersReport += "<br>" + "itraq8plex:13c(6)15n(2) on nterm" + " (assumed fixed)";
            }
            if (!modProfile.contains("itraq8plex:13c(6)15n(2) on y")) {
                modProfile.addVariableModification(getPTM("itraq8plex:13c(6)15n(2) on y"));
                prideParametersReport += "<br>" + "itraq8plex:13c(6)15n(2) on y" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("TMT2plex") || pridePtmName.equalsIgnoreCase("TMTduplex")) {

            if (!modProfile.contains("tmt duplex on k")) {
                modProfile.addFixedModification(getPTM("tmt duplex on k"));
                prideParametersReport += "<br>" + "tmt duplex on k" + " (assumed fixed)";
            }
            if (!modProfile.contains("tmt duplex on n-term peptide")) {
                modProfile.addFixedModification(getPTM("tmt duplex on n-term peptide"));
                prideParametersReport += "<br>" + "tmt duplex on n-term peptide" + " (assumed fixed)";
            }
        } else if (pridePtmName.equalsIgnoreCase("TMT6plex")
                || pridePtmName.equalsIgnoreCase("TMT6plex-126 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("TMT6plex-131 reporter+balance reagent N6-acylated lysine")) { // @TODO: add the new or old TMT tags..?

            if (!modProfile.contains("tmt 6-plex on k")) {
                modProfile.addFixedModification(getPTM("tmt 6-plex on k"));
                prideParametersReport += "<br>" + "tmt 6-plex on k" + " (assumed fixed)";
            }
            if (!modProfile.contains("tmt 6-plex on n-term peptide")) {
                modProfile.addFixedModification(getPTM("tmt 6-plex on n-term peptide"));
                prideParametersReport += "<br>" + "tmt 6-plex on n-term peptide" + " (assumed fixed)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Phosphorylation")
                || pridePtmName.equalsIgnoreCase("L-aspartic 4-phosphoric anhydride")
                || pridePtmName.equalsIgnoreCase("O-phosphorylated residue")
                || pridePtmName.equalsIgnoreCase("Phospho")
                || pridePtmName.equalsIgnoreCase("phosphorylated residue")) {

            if (!modProfile.contains("phosphorylation of s")) {
                modProfile.addVariableModification(getPTM("phosphorylation of s"));
                prideParametersReport += "<br>" + "phosphorylation of s" + " (assumed variable)";
            }
            if (!modProfile.contains("phosphorylation of t")) {
                modProfile.addVariableModification(getPTM("phosphorylation of t"));
                prideParametersReport += "<br>" + "phosphorylation of t" + " (assumed variable)";
            }
            if (!modProfile.contains("phosphorylation of y")) {
                modProfile.addVariableModification(getPTM("phosphorylation of y"));
                prideParametersReport += "<br>" + "phosphorylation of y" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Palmitoylation")) {

            if (!modProfile.contains("palmitoylation of c")) {
                modProfile.addVariableModification(getPTM("palmitoylation of c"));
                prideParametersReport += "<br>" + "palmitoylation of c" + " (assumed variable)";
            }
            if (!modProfile.contains("palmitoylation of k")) {
                modProfile.addVariableModification(getPTM("palmitoylation of k"));
                prideParametersReport += "<br>" + "palmitoylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("palmitoylation of s")) {
                modProfile.addVariableModification(getPTM("palmitoylation of s"));
                prideParametersReport += "<br>" + "palmitoylation of s" + " (assumed variable)";
            }
            if (!modProfile.contains("palmitoylation of t")) {
                modProfile.addVariableModification(getPTM("palmitoylation of t"));
                prideParametersReport += "<br>" + "palmitoylation of t" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Formylation")) {

            if (!modProfile.contains("formylation of k")) {
                modProfile.addVariableModification(getPTM("formylation of k"));
                prideParametersReport += "<br>" + "formylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("formylation of peptide n-term")) {
                modProfile.addVariableModification(getPTM("formylation of peptide n-term"));
                prideParametersReport += "<br>" + "formylation of peptide n-term" + " (assumed variable)";
            }
            if (!modProfile.contains("formylation of protein c-term")) {
                modProfile.addVariableModification(getPTM("formylation of protein c-term"));
                prideParametersReport += "<br>" + "formylation of protein c-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Carbamylation")) {

            if (!modProfile.contains("carbamylation of k")) {
                modProfile.addVariableModification(getPTM("carbamylation of k"));
                prideParametersReport += "<br>" + "carbamylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("carbamylation of n-term peptide")) {
                modProfile.addVariableModification(getPTM("carbamylation of n-term peptide"));
                prideParametersReport += "<br>" + "carbamylation of n-term peptide" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(12)C labeled N6-propanoyl-L-lysine")) {

            if (!modProfile.contains("propionyl light k")) {
                modProfile.addVariableModification(getPTM("propionyl light k"));
                prideParametersReport += "<br>" + "propionyl light k" + " (assumed variable)";
            }
            if (!modProfile.contains("propionyl light on peptide n-term")) {
                modProfile.addVariableModification(getPTM("propionyl light on peptide n-term"));
                prideParametersReport += "<br>" + "propionyl light on peptide n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(13)C labeled N6-propanoyl-L-lysine")) {

            if (!modProfile.contains("propionyl heavy k")) {
                modProfile.addVariableModification(getPTM("propionyl heavy k"));
                prideParametersReport += "<br>" + "propionyl heavy k" + " (assumed variable)";
            }
            if (!modProfile.contains("propionyl heavy peptide n-term")) {
                modProfile.addVariableModification(getPTM("propionyl heavy peptide n-term"));
                prideParametersReport += "<br>" + "propionyl heavy peptide n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(2)H residue methyl ester")) {

            if (!modProfile.contains("tri-deuteromethylation of d")) {
                modProfile.addVariableModification(getPTM("tri-deuteromethylation of d"));
                prideParametersReport += "<br>" + "tri-deuteromethylation of d" + " (assumed variable)";
            }
            if (!modProfile.contains("tri-deuteromethylation of e")) {
                modProfile.addVariableModification(getPTM("tri-deuteromethylation of e"));
                prideParametersReport += "<br>" + "tri-deuteromethylation of e" + " (assumed variable)";
            }
            if (!modProfile.contains("tri-deuteromethylation of peptide c-term")) {
                modProfile.addVariableModification(getPTM("tri-deuteromethylation of peptide c-term"));
                prideParametersReport += "<br>" + "tri-deuteromethylation of peptide c-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled residue")) {

            if (!modProfile.contains("heavy arginine-13C6")) {
                modProfile.addVariableModification(getPTM("heavy arginine-13C6"));
                prideParametersReport += "<br>" + "heavy arginine-13C6" + " (assumed variable)";
            }
            if (!modProfile.contains("heavy lysine-13C6")) {
                modProfile.addVariableModification(getPTM("heavy lysine-13C6"));
                prideParametersReport += "<br>" + "heavy lysine-13C6" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Acetyl")
                || pridePtmName.equalsIgnoreCase("N-acetylated residue")
                || pridePtmName.equalsIgnoreCase("N-acylated residue")
                || pridePtmName.equalsIgnoreCase("acetylated residue")) {

            if (!modProfile.contains("acetylation of k")) {
                modProfile.addVariableModification(getPTM("acetylation of k"));
                prideParametersReport += "<br>" + "acetylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("acetylation of protein n-term")) {
                modProfile.addVariableModification(getPTM("acetylation of protein n-term"));
                prideParametersReport += "<br>" + "acetylation of protein n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("carbamoylated residue")) {

            if (!modProfile.contains("carbamylation of k")) {
                modProfile.addVariableModification(getPTM("carbamylation of k"));
                prideParametersReport += "<br>" + "carbamylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("carbamylation of n-term peptide")) {
                modProfile.addVariableModification(getPTM("carbamylation of n-term peptide"));
                prideParametersReport += "<br>" + "carbamylation of n-term peptide" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("dimethylated residue")) {

            if (!modProfile.contains("di-methylation of k")) {
                modProfile.addVariableModification(getPTM("di-methylation of k"));
                prideParametersReport += "<br>" + "di-methylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("di-methylation of r")) {
                modProfile.addVariableModification(getPTM("di-methylation of r"));
                prideParametersReport += "<br>" + "di-methylation of r" + " (assumed variable)";
            }
            if (!modProfile.contains("di-methylation of peptide n-term")) {
                modProfile.addVariableModification(getPTM("di-methylation of peptide n-term"));
                prideParametersReport += "<br>" + "di-methylation of peptide n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("trimethylated residue")) {

            if (!modProfile.contains("tri-methylation of k")) {
                modProfile.addVariableModification(getPTM("tri-methylation of k"));
                prideParametersReport += "<br>" + "tri-methylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("tri-methylation of r")) {
                modProfile.addVariableModification(getPTM("tri-methylation of r"));
                prideParametersReport += "<br>" + "tri-methylation of r" + " (assumed variable)";
            }
            if (!modProfile.contains("tri-methylation of protein n-term")) {
                modProfile.addVariableModification(getPTM("tri-methylation of protein n-term"));
                prideParametersReport += "<br>" + "tri-methylation of protein n-term" + " (assumed variable)";
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
            } else {
                if (!unknownPtms.contains(pridePtmName)) {
                    unknownPtms.add(pridePtmName);
                }
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

        // @TODO: check for more unmapped ptms! everything used in getDefaultCVTerm in PtmToPrideMap should be mapped back as well!!
        if (pridePtmName.equalsIgnoreCase("Carbamidomethyl")
                || pridePtmName.equalsIgnoreCase("S-carboxamidomethyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("iodoacetamide - site C")
                || pridePtmName.equalsIgnoreCase("iodoacetamide derivatized residue")
                || pridePtmName.equalsIgnoreCase("Iodoacetamide derivative")) {
            return "carbamidomethyl c";
        } else if (pridePtmName.equalsIgnoreCase("Oxidation")
                || pridePtmName.equalsIgnoreCase("monohydroxylated residue")
                || pridePtmName.equalsIgnoreCase("oxidized residue")) {
            return "oxidation of m";
        } else if (pridePtmName.equalsIgnoreCase("Acetylation")) {
            return "acetylation of k";
        } else if (pridePtmName.equalsIgnoreCase("Amidation")) {
            return "amidation of peptide c-term";
        } else if (pridePtmName.equalsIgnoreCase("Carboxymethyl")
                || pridePtmName.equalsIgnoreCase("S-carboxymethyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("iodoacetic acid derivatized residue")) {
            return "carboxymethyl c";
        } else if (pridePtmName.equalsIgnoreCase("Farnesylation")) {
            return "farnesylation of c";
        } else if (pridePtmName.equalsIgnoreCase("Geranyl-geranyl")) {
            return "geranyl-geranyl";
        } else if (pridePtmName.equalsIgnoreCase("Guanidination")) {
            return "guanidination of k";
        } else if (pridePtmName.equalsIgnoreCase("Homoserine")) {
            return "homoserine";
        } else if (pridePtmName.equalsIgnoreCase("Homoserine lactone")) {
            return "homoserine lactone";
        } else if (pridePtmName.equalsIgnoreCase("ICAT-C")
                || pridePtmName.equalsIgnoreCase("Applied Biosystems cleavable ICAT(TM) light")) {
            return "icat light";
        } else if (pridePtmName.equalsIgnoreCase("ICAT-C:13C(9)")
                || pridePtmName.equalsIgnoreCase("Applied Biosystems cleavable ICAT(TM) heavy")) {
            return "icat heavy";
        } else if (pridePtmName.equalsIgnoreCase("Lipoyl")) {
            return "lipoyl k";
        } else if (pridePtmName.equalsIgnoreCase("Methylthio")) {
            return "beta-methylthiolation of d (duplicate of 13)";
        } else if (pridePtmName.equalsIgnoreCase("NIPCAM(C)")) {
            return "nipcam";
        } else if (pridePtmName.equalsIgnoreCase("Phosphopantetheine")) {
            return "phosphopantetheine s";
        } else if (pridePtmName.equalsIgnoreCase("Propionamide(C)")
                || pridePtmName.equalsIgnoreCase("Acrylamide adduct")) {
            return "propionamide c";
        } else if (pridePtmName.equalsIgnoreCase("Pyridylethyl")) {
            return "s-pyridylethylation of c";
        } else if (pridePtmName.equalsIgnoreCase("Pyridylethyl")) {
            return "s-pyridylethylation of c";
        } else if (pridePtmName.equalsIgnoreCase("Sulfo")
                || pridePtmName.equalsIgnoreCase("sulfated residue")) {
            return "sulfation of y"; // not completely sure about this one...
        } else if (pridePtmName.equalsIgnoreCase("Dehydratation")) {
            return "dehydro of s and t";
        } else if (pridePtmName.equalsIgnoreCase("Deamination")
                || pridePtmName.equalsIgnoreCase("Deamidated")
                || pridePtmName.equalsIgnoreCase("deamidated L-glutamine")
                || pridePtmName.equalsIgnoreCase("deamidated residue")
                || pridePtmName.equalsIgnoreCase("deaminated residue")) {
            return "deamidation of n and q"; // note that this does not separate between deamidation on only n and deamidation on n and q
        } else if (pridePtmName.equalsIgnoreCase("Dioxidation")) {
            return "sulphone of m";
        } else if (pridePtmName.equalsIgnoreCase("(18)O label at both C-terminal oxygens")) {
            return "di-o18 on peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("(18)O monosubstituted residue")) {
            return "o18 on peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("(4,4,5,5-(2)H4)-L-lysine")) {
            return "heavy lysine - 2h4";
        } else if (pridePtmName.equalsIgnoreCase("2-pyrrolidone-5-carboxylic acid (Gln)")
                || pridePtmName.equalsIgnoreCase("Ammonia-loss")) {
            return "pyro-glu from n-term q";
        } else if (pridePtmName.equalsIgnoreCase("2-pyrrolidone-5-carboxylic acid (Glu)")
                || pridePtmName.equalsIgnoreCase("Glu->pyro-Glu")) {
            return "pyro-glu from n-term e";
        } else if (pridePtmName.equalsIgnoreCase("3-hydroxy-L-proline")) {
            return "hydroxylation of p";
        } else if (pridePtmName.equalsIgnoreCase("3x(2)H labeled L-aspartic acid 4-methyl ester")) {
            return "tri-deuteromethylation of d";
        } else if (pridePtmName.equalsIgnoreCase("4x(2)H labeled alpha-dimethylamino N-terminal residue")) {
            return "chd2-di-methylation of peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("4x(2)H labeled dimethylated L-lysine")) {
            return "chd2-di-methylation of k";
        } else if (pridePtmName.equalsIgnoreCase("5-methyl-L-arginine")) {
            return "methyl r";
        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled L-arginine")
                || pridePtmName.equalsIgnoreCase("6x(13)C,4x(15)N labeled L-arginine")) {
            return "heavy arginine-13C6";
        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled L-lysine")
                || pridePtmName.equalsIgnoreCase("6x(13)C,2x(15)N labeled L-lysine")) {
            return "heavy lysine-13C6";
        } else if (pridePtmName.equalsIgnoreCase("L-aspartic acid 4-methyl ester")) {
            return "methyl ester of d";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteic acid (L-cysteine sulfonic acid)")) {
            return "oxidation of C to cysteic acid";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteine glutathione disulfide")) {
            return "glutathione disulfide";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteine methyl disulfide")
                || pridePtmName.equalsIgnoreCase("methyl methanethiosulfonate")) {
            return "mmts on c";
        } else if (pridePtmName.equalsIgnoreCase("L-cystine (cross-link)")) {
            return "2-amino-3-oxo-butanoic acid t";
        } else if (pridePtmName.equalsIgnoreCase("L-glutamic acid 5-methyl ester (Glu)")
                || pridePtmName.equalsIgnoreCase("methylated glutamic acid")) {
            return "methylation of e";
        } else if (pridePtmName.equalsIgnoreCase("L-homoarginine")) {
            return "guanidination of k";
        } else if (pridePtmName.equalsIgnoreCase("L-methionine (R)-sulfoxide")
                || pridePtmName.equalsIgnoreCase("L-methionine (S)-sulfoxide")
                || pridePtmName.equalsIgnoreCase("L-methionine sulfoxide")) {
            return "oxidation of m";
        } else if (pridePtmName.equalsIgnoreCase("L-methionine sulfone")) {
            return "sulphone of m";
        } else if (pridePtmName.equalsIgnoreCase("N-acetyl-L-asparagine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-glutamic acid")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-isoleucine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-serine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-tyrosine")) {
            return "acetylation of protein n-term";
        } else if (pridePtmName.equalsIgnoreCase("N-acetylated L-lysine")
                || pridePtmName.equalsIgnoreCase("N6-acetyl-L-lysine")) {
            return "acetylation of k";
        } else if (pridePtmName.equalsIgnoreCase("N-ethylmaleimide derivatized cysteine")) {
            return "nem c";
        } else if (pridePtmName.equalsIgnoreCase("N-formyl-L-methionine")) {
            return "n-formyl met addition";
        } else if (pridePtmName.equalsIgnoreCase("N-formylated residue")) {
            return "formylation of peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("N-methyl-L-serine")) {
            return "methyl ester of s";
        } else if (pridePtmName.equalsIgnoreCase("N2-acetyl-L-tryptophan")) {
            return "acetylation of protein n-term";
        } else if (pridePtmName.equalsIgnoreCase("N6,N6-dimethyl-L-lysine")) {
            return "di-methylation of k";
        } else if (pridePtmName.equalsIgnoreCase("N6-formyl-L-lysine")) {
            return "formylation of k";
        } else if (pridePtmName.equalsIgnoreCase("N6-methyl-L-lysine")
                || pridePtmName.equalsIgnoreCase("methylated lysine")
                || pridePtmName.equalsIgnoreCase("monomethylated L-lysine")) {
            return "methylation of k";
        } else if (pridePtmName.equalsIgnoreCase("N6-propanoyl-L-lysine")) {
            return "propionyl light k";
        } else if (pridePtmName.equalsIgnoreCase("O-(N-acetylamino)glucosyl-L-serine")) {
            return "serine hexnac";
        } else if (pridePtmName.equalsIgnoreCase("O-(N-acetylamino)glucosyl-L-threonine")) {
            return "threonine hexnac";
        } else if (pridePtmName.equalsIgnoreCase("O-phospho-L-serine")) {
            return "phosphorylation of s";
        } else if (pridePtmName.equalsIgnoreCase("O-phospho-L-threonine")) {
            return "phosphorylation of t";
        } else if (pridePtmName.equalsIgnoreCase("O4&apos;-phospho-L-tyrosine")) {
            return "phosphorylation of y";
        } else if (pridePtmName.equalsIgnoreCase("S-carboxamidoethyl-L-cysteine")) {
            return "propionamide c";
        } else if (pridePtmName.equalsIgnoreCase("S-methyl-L-cysteine")) {
            return "methyl c";
        } else if (pridePtmName.equalsIgnoreCase("alpha-amino acetylated residue")) {
            return "acetylation of protein n-term";
        } else if (pridePtmName.equalsIgnoreCase("alpha-amino dimethylated residue")) {
            return "di-methylation of peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("amidated residue")) {
            return "amidation of peptide c-term";
        } else if (pridePtmName.equalsIgnoreCase("deamidated L-asparagine")
                || pridePtmName.equalsIgnoreCase("deglycosylated asparagine")) {
            return "deamidation of n";
        } else if (pridePtmName.equalsIgnoreCase("dehydrated residue")) {
            return "dehydro of s and t";
        } else if (pridePtmName.equalsIgnoreCase("dihydroxylated residue - site W")) {
            return "oxidation of w to formylkynurenin";
        } else if (pridePtmName.equalsIgnoreCase("diiodinated residue")) {
            return "di-iodination of y";
        } else if (pridePtmName.equalsIgnoreCase("hydroxylated lysine")
                || pridePtmName.equalsIgnoreCase("monohydroxylated lysine")) {
            return "hydroxylation of k";
        } else if (pridePtmName.equalsIgnoreCase("iodoacetamide -site E")) {
            return "carboxyamidomethylation of e";
        } else if (pridePtmName.equalsIgnoreCase("iodoacetamide N6-derivatized lysine")) {
            return "carboxyamidomethylation of k";
        } else if (pridePtmName.equalsIgnoreCase("monomethylated L-aspartic acid")) {
            return "methylation of d";
        } else if (pridePtmName.equalsIgnoreCase("thioacylation of primary amines - site N-term")) {
            return "gammathiopropionylation of peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("ubiquitination signature dipeptidyl lysine")) {
            return "ubiquitinylation residue";
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
        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern("K");
        String ptmName = "Acetylation of K";
        PTM ptm = new PTM(PTM.MODAA, ptmName, "ace", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addReporterIon(ReporterIon.ACE_K_126);
        ptm.addReporterIon(ReporterIon.ACE_K_143);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Acetylation of peptide N-terminus
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Acetylation of Peptide N-terminus";
        ptm = new PTM(PTM.MODNP, ptmName, "ace", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Acetylation of protein N-terminus
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "Acetylation of Protein N-terminus";
        ptm = new PTM(PTM.MODN, ptmName, "ace", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Carbamidomethylation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("C");
        ptmName = "Carbamidomethylation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "cmm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Oxidation of M
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("M");
        ptmName = "Oxidation of M";
        ptm = new PTM(PTM.MODAA, ptmName, "ox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addNeutralLoss(NeutralLoss.CH4OS);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Oxidation of P
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0));
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("P");
        ptmName = "Oxidation of P";
        ptm = new PTM(PTM.MODAA, ptmName, "ox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dioxydation of M
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("M");
        ptmName = "Dioxidation of M";
        ptm = new PTM(PTM.MODAA, ptmName, "diox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dioxydation of M
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("W");
        ptmName = "Dioxidation of W";
        ptm = new PTM(PTM.MODAA, ptmName, "diox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trioxidation of C
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("C");
        ptmName = "Dioxidation of C";
        ptm = new PTM(PTM.MODAA, ptmName, "triox", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Phosphorylation of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0));
        atomChainAdded.append(new AtomImpl(Atom.P, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("S");
        ptmName = "Phosphorylation of S";
        ptm = new PTM(PTM.MODAA, ptmName, "p", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addNeutralLoss(NeutralLoss.H3PO4);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Phosphorylation of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0));
        atomChainAdded.append(new AtomImpl(Atom.P, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("T");
        ptmName = "Phosphorylation of T";
        ptm = new PTM(PTM.MODAA, ptmName, "p", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addNeutralLoss(NeutralLoss.H3PO4);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Phosphorylation of Y
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0));
        atomChainAdded.append(new AtomImpl(Atom.P, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("Y");
        ptmName = "Phosphorylation of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "p", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addNeutralLoss(NeutralLoss.HPO3);
        ptm.addReporterIon(ReporterIon.PHOSPHO_Y);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Arg6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        aminoAcidPattern = new AminoAcidPattern("R");
        ptmName = "Arginine 13C6";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Arg10
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 4);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 4);
        aminoAcidPattern = new AminoAcidPattern("R");
        ptmName = "Arginine 13C6 15N4";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Lys4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 4);
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Lysine 2H4";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Lys6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Lysine 13C6";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Lys8
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 2);
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Lysine 13C6 15N2";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pro5
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 5);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 5);
        aminoAcidPattern = new AminoAcidPattern("P");
        ptmName = "Proline 13C5";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // 4-Hydroxyloproline
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 5);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 9);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("P");
        ptmName = "4-Hydroxyloproline";
        ptm = new PTM(PTM.MODAA, ptmName, "hydroxy", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Leu7
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("L");
        ptmName = "Leucine 13C6 15N1";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Ile 7
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.C, 0), 6);
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("I");
        ptmName = "Isoleucine 13C6 15N1";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of K 2H4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 4);
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Diethylation of K 2H4";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of K 2H6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Diethylation of K 2H6";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth6", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // N-term Dimethylation of K 2H4
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 4);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "N-term diethylation of K 2H4";
        ptm = new PTM(PTM.MODNPAA, ptmName, "dimeth4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // N-term Dimethylation 2H6
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = null;
        ptmName = "N-term diethylation 2H6";
        ptm = new PTM(PTM.MODNP, ptmName, "dimeth6", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);
        ptmMap.put(ptmName, ptm);

        // N-term Dimethylation 2H6 13C2
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 6);
        atomChainRemoved = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = null;
        ptmName = "N-term diethylation 2H6 13C2";
        ptm = new PTM(PTM.MODNP, ptmName, "dimeth8", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);
        ptmMap.put(ptmName, ptm);

        // 18O
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 2), 2);
        atomChainRemoved = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        aminoAcidPattern = null;
        ptmName = "C-term 18O";
        ptm = new PTM(PTM.MODCP, ptmName, "18O", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICAT-0
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 10);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 17);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("C");
        ptmName = "ICAT-O";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        aminoAcidPattern = new AminoAcidPattern("C");
        ptmName = "ICAT-9";
        ptm = new PTM(PTM.MODAA, ptmName, "*", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL0 of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "ICPL0 of K";
        ptm = new PTM(PTM.MODAA, ptmName, "icpl0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL0 of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "ICPL0 of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "icpl0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL4 of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "ICPL4 of K";
        ptm = new PTM(PTM.MODAA, ptmName, "icpl4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL4 of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = null;
        ptmName = "ICPL4 of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "icpl0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL6 of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "ICPL6 of K";
        ptm = new PTM(PTM.MODAA, ptmName, "icpl6", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL6 of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "ICPL6 of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "icpl6", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL10 of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "ICPL10 of K";
        ptm = new PTM(PTM.MODAA, ptmName, "icpl10", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // ICPL10 of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = null;
        ptmName = "ICPL10 of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "icpl10", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of K light
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "mTRAQ of K light";
        ptm = new PTM(PTM.MODAA, ptmName, "mTRAQ0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of N-term light
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "mTRAQ of N-term light";
        ptm = new PTM(PTM.MODNP, ptmName, "mTRAQ0", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "mTRAQ of K 13C3 15N";
        ptm = new PTM(PTM.MODAA, ptmName, "mTRAQ4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of N-term 13C3 15N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "mTRAQ of N-term 13C3 15N";
        ptm = new PTM(PTM.MODNP, ptmName, "mTRAQ4", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "mTRAQ of 13C6 15N2";
        ptm = new PTM(PTM.MODAA, ptmName, "mTRAQ8", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // mTRAQ of N-term 13C3 15N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "mTRAQ of N-term 13C6 15N2";
        ptm = new PTM(PTM.MODNP, ptmName, "mTRAQ8", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 4-plex of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "iTRAQ 4-plex of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_117);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 4-plex of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "iTRAQ 4-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_117);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 4-plex of Y
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 12);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("Y");
        ptmName = "iTRAQ 4-plex of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_114);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_115);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_116);
        ptm.addReporterIon(ReporterIon.iTRAQ4Plex_117);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // iTRAQ 8-plex of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 14);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 24);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "iTRAQ 8-plex of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 14);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 24);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "iTRAQ 8-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 14);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 24);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 3);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("Y");
        ptmName = "iTRAQ 8-plex of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "iTRAQ", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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

        // TMT 2-plex of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 11);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "TMT 2-plex of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "TMT 2-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127C);
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 6-plex of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "TMT 6-plex of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_131);
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
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "TMT 6-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        ptm.addReporterIon(ReporterIon.TMT_126);
        ptm.addReporterIon(ReporterIon.TMT_127N);
        ptm.addReporterIon(ReporterIon.TMT_128C);
        ptm.addReporterIon(ReporterIon.TMT_129N);
        ptm.addReporterIon(ReporterIon.TMT_130C);
        ptm.addReporterIon(ReporterIon.TMT_131);
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
        ptm.addReporterIon(ReporterIon.TMT_126_ETD);
        ptm.addReporterIon(ReporterIon.TMT_127N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_128C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_129N_ETD);
        ptm.addReporterIon(ReporterIon.TMT_130C_ETD);
        ptm.addReporterIon(ReporterIon.TMT_131_ETD);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // TMT 10-plex of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.C, 1), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 20);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 1), 1);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = null;
        ptmName = "TMT 10-plex of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "TMT 10-plex of K";
        ptm = new PTM(PTM.MODAA, ptmName, "TMT", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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

        // Ubiquitination of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 4);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Ubiquitination of K";
        ptm = new PTM(PTM.MODAA, ptmName, "ub", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0));
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Methylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0));
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("R");
        ptmName = "Methylation of R";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Methylation of E
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 2);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("E");
        ptmName = "Methylation of E";
        ptm = new PTM(PTM.MODAA, ptmName, "meth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Diethylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // N-term Dimethylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "N-term diethylation of K";
        ptm = new PTM(PTM.MODNPAA, ptmName, "dimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Dimethylation of R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 2);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 4);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("R");
        ptmName = "Diethylation of R";
        ptm = new PTM(PTM.MODAA, ptmName, "dimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Trimethylation of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 3);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 6);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Diethylation of K";
        ptm = new PTM(PTM.MODAA, ptmName, "trimeth", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pyrolidone from E
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 2);
        aminoAcidPattern = new AminoAcidPattern("E");
        ptmName = "Pyrolidone from E";
        ptm = new PTM(PTM.MODNPAA, ptmName, "pyro", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pyrolidone from Q
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 3);
        aminoAcidPattern = new AminoAcidPattern("Q");
        ptmName = "Pyrolidone from Q";
        ptm = new PTM(PTM.MODNPAA, ptmName, "pyro", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Pyrolidone from carbamidomethylated C
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 3);
        aminoAcidPattern = new AminoAcidPattern("C");
        ptmName = "Pyrolidone from carbamidomethylated C";
        ptm = new PTM(PTM.MODNPAA, ptmName, "pyro", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // HexNAc of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 13);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 5);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("S");
        ptmName = "HexNAc of S";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // HexNAc of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 8);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 13);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 5);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("T");
        ptmName = "HexNAc of T";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hex(1)NAc(1) of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 14);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 23);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 10);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("S");
        ptmName = "Hex(1)NAc(1) of S";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hex(1)NAc(1) of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 14);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 23);
        atomChainAdded.append(new AtomImpl(Atom.N, 0));
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 10);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("T");
        ptmName = "Hex(1)NAc(1) of T";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hexose of K
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 6);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 10);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 5);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "Hexose of K";
        ptm = new PTM(PTM.MODAA, ptmName, "hex", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Hex(5) HexNAc(4) NeuAc(2) of N
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 84);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 136);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 61);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 6);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("N");
        ptmName = "Hex(5) HexNAc(4) NeuAc(2) of K";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        aminoAcidPattern = new AminoAcidPattern("N");
        ptmName = "Hex(5) HexNAc(4) NeuAc(2) Na of N";
        ptm = new PTM(PTM.MODAA, ptmName, "glyco", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // SUMO-2/3 Q87R
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 0), 18);
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 29);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 7);
        atomChainAdded.append(new AtomImpl(Atom.O, 0), 8);
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("K");
        ptmName = "SUMO-2/3 Q87R";
        ptm = new PTM(PTM.MODAA, ptmName, "sumo", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        atomChainAdded = null;
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("N");
        ptmName = "Deamidation of N";
        ptm = new PTM(PTM.MODAA, ptmName, "deam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Deamidation of Q
        atomChainAdded = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("Q");
        ptmName = "Deamidation of Q";
        ptm = new PTM(PTM.MODAA, ptmName, "deam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Deamidation of N 18O
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.O, 1), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("N");
        ptmName = "Deamidation of N 18O";
        ptm = new PTM(PTM.MODAA, ptmName, "deam", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Carbamylation of N-term
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.C, 1));
        atomChainAdded.append(new AtomImpl(Atom.H, 1));
        atomChainAdded.append(new AtomImpl(Atom.N, 1));
        atomChainAdded.append(new AtomImpl(Atom.O, 1));
        atomChainRemoved = null;
        ptmName = "Carbamilation of N-term";
        ptm = new PTM(PTM.MODNP, ptmName, "cm", atomChainAdded, atomChainRemoved, aminoAcidPattern);
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
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sodium adduct to D
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.Na, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("D");
        ptmName = "Sodium adduct to D";
        ptm = new PTM(PTM.MODAA, ptmName, "Na", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sodium adduct to E
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.Na, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.H, 0), 1);
        aminoAcidPattern = new AminoAcidPattern("E");
        ptmName = "Sodium adduct to E";
        ptm = new PTM(PTM.MODAA, ptmName, "Na", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Amidation of the peptide C-terminus
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        aminoAcidPattern = null;
        ptmName = "Amidation of the peptide C-terminus";
        ptm = new PTM(PTM.MODCP, ptmName, "am", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Amidation of the protein C-terminus
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.H, 0), 1);
        atomChainAdded.append(new AtomImpl(Atom.N, 0), 1);
        atomChainRemoved = new AtomChain();
        atomChainRemoved.append(new AtomImpl(Atom.O, 0), 1);
        aminoAcidPattern = null;
        ptmName = "Amidation of the protein C-terminus";
        ptm = new PTM(PTM.MODC, ptmName, "am", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sulfonation of S
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.S, 0));
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("S");
        ptmName = "Sulfonation of S";
        ptm = new PTM(PTM.MODAA, ptmName, "s", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sulfonation of T
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.S, 0));
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("T");
        ptmName = "Sulfonation of T";
        ptm = new PTM(PTM.MODAA, ptmName, "s", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

        // Sulfonation of Y
        atomChainAdded = new AtomChain();
        atomChainAdded.append(new AtomImpl(Atom.S, 0));
        atomChainRemoved = null;
        aminoAcidPattern = new AminoAcidPattern("Y");
        ptmName = "Sulfonation of Y";
        ptm = new PTM(PTM.MODAA, ptmName, "s", atomChainAdded, atomChainRemoved, aminoAcidPattern);
        defaultMods.add(ptmName);
        ptmMap.put(ptmName, ptm);

    }
}
