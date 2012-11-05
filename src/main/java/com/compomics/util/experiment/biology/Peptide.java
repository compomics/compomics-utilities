package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.awt.Color;
import java.io.IOException;
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
    private ArrayList<String> parentProteins = new ArrayList<String>();
    /**
     * The modifications carried by the peptide.
     */
    private ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();
    /**
     * Separator preceding confident localization of the confident localization
     * of a modification
     */
    public static String MODIFICATION_LOCALIZATION_SEPARATOR = "-ATAA-";
    /**
     * Separator used to separate modifications in peptide keys
     */
    public static String MODIFICATION_SEPARATOR = "_";

    /**
     * Constructor for the peptide.
     */
    public Peptide() {
    }

    /**
     * Constructor for the peptide.
     *
     * @param aSequence The peptide sequence
     * @param parentProteins The parent proteins
     * @param modifications The PTM of this peptide
     * @throws IllegalArgumentException Thrown if the peptide sequence contains
     * unknown amino acids
     */
    public Peptide(String aSequence, ArrayList<String> parentProteins, ArrayList<ModificationMatch> modifications) throws IllegalArgumentException {
        this.sequence = aSequence;
        sequence.replaceAll("[#*§$%&]", "");
        for (ModificationMatch mod : modifications) {
            if (mod.getTheoreticPtm().contains(MODIFICATION_SEPARATOR)) {
                throw new IllegalArgumentException("PTM names containing '" + MODIFICATION_SEPARATOR + "' are not supported. Conflicting name: " + mod.getTheoreticPtm());
            }
            if (mod.getTheoreticPtm().contains(MODIFICATION_LOCALIZATION_SEPARATOR)) {
                throw new IllegalArgumentException("PTM names containing '" + MODIFICATION_LOCALIZATION_SEPARATOR + "' are not supported. Conflicting name: " + mod.getTheoreticPtm());
            }
            this.modifications.add(mod);
        }
        setParentProteins(parentProteins);
        estimateTheoreticMass();
    }

    /**
     * Constructor for the peptide.
     *
     * @deprecated use the constructor without mass. The mass will be
     * recalculated.
     * @param aSequence The peptide sequence
     * @param mass The peptide mass
     * @param parentProteins The parent proteins
     * @param modifications The PTM of this peptide
     */
    public Peptide(String aSequence, Double mass, ArrayList<String> parentProteins, ArrayList<ModificationMatch> modifications) {
        this.sequence = aSequence;
        sequence.replaceAll("[#*§$%&]", "");
        this.mass = mass;
        for (ModificationMatch mod : modifications) {
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
     * Getter for the parent proteins.
     *
     * @return the parent proteins
     */
    public ArrayList<String> getParentProteins() {
        return parentProteins;
    }

    /**
     * Sets the parent proteins.
     *
     * @param parentProteins the parent proteins as list
     */
    public void setParentProteins(ArrayList<String> parentProteins) {
        if (parentProteins == null || parentProteins.isEmpty()) {
            throw new IllegalArgumentException("Trying to set an empty protein list to peptide " + sequence + ".");
        }
        this.parentProteins = parentProteins;
    }

    /**
     * Returns the index of a peptide. index = SEQUENCE_mod1_mod2 with
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
                            result.add(new Integer(localizationSplit[1]));
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Cannot parse modification localization " + localizationSplit + " for modification " + modification + " in peptide key " + peptideKey);
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
     * @return a boolean indicating whether the given modification can be found
     * on the peptide
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     */
    public boolean isModifiable(PTM ptm) throws IOException, IllegalArgumentException, InterruptedException {
        switch (ptm.getType()) {
            case PTM.MODAA:
                AminoAcidPattern pattern = ptm.getPattern();
                int nAA = pattern.length();
                int target = pattern.getTarget();
                if (target >= 0 && nAA - target <= 1) {
                    return pattern.matches(sequence);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence)) {
                            int beginIndex = index - target;
                            int endIndex = index + sequence.length() - 1 + nAA - target;
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
                return !isCterm().isEmpty();
            case PTM.MODN:
                return !isNterm().isEmpty();
            case PTM.MODCAA:
                if (isCterm().isEmpty()) {
                    return false;
                }
            case PTM.MODCPAA:
                pattern = ptm.getPattern();
                target = pattern.getTarget();
                nAA = pattern.length();
                if (target == nAA - 1 && sequence.length() >= nAA) {
                    return pattern.isEnding(sequence);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence)) {
                            int beginIndex = index + sequence.length() - target - 1;
                            int endIndex = beginIndex + nAA;
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
                if (isNterm().isEmpty()) {
                    return false;
                }
            case PTM.MODNPAA:
                pattern = ptm.getPattern();
                target = pattern.getTarget();
                nAA = pattern.length();
                if (target == 0 && sequence.length() >= nAA) {
                    return pattern.isStarting(sequence);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence)) {
                            int beginIndex = index - target;
                            int endIndex = beginIndex + nAA;
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
     * is the first aa. An empty list is returned if no possibility was found.
     * This method does not account for protein terminal modifications.
     *
     * @param ptm the PTM considered
     * @return a list of potential modification sites
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     */
    public ArrayList<Integer> getPotentialModificationSites(PTM ptm) throws IOException, IllegalArgumentException, InterruptedException {
        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        switch (ptm.getType()) {
            case PTM.MODAA:
                AminoAcidPattern pattern = ptm.getPattern();
                int nAA = pattern.length();
                int target = pattern.getTarget();
                if (target >= 0 && nAA - target <= 1) {
                    return pattern.getIndexes(sequence);
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    for (String accession : parentProteins) {
                        Protein protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence)) {
                            int beginIndex = index - target;
                            int endIndex = index + sequence.length() - 1 + nAA - target;
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
                pattern = ptm.getPattern();
                target = pattern.getTarget();
                nAA = pattern.length();
                if (target == nAA - 1 && sequence.length() >= nAA) {
                    if (pattern.isEnding(sequence)) {
                        possibleSites.add(sequence.length());
                    }
                    return possibleSites;
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    Protein protein;
                    for (String accession : parentProteins) {
                        protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence)) {
                            int beginIndex = index + sequence.length() - target - 1;
                            int endIndex = beginIndex + nAA;
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
                pattern = ptm.getPattern();
                target = pattern.getTarget();
                nAA = pattern.length();
                if (target == 0 && sequence.length() >= nAA) {
                    if (pattern.isStarting(sequence)) {
                        possibleSites.add(1);
                    }
                    return possibleSites;
                } else {
                    SequenceFactory sequenceFactory = SequenceFactory.getInstance();
                    Protein protein;
                    for (String accession : parentProteins) {
                        protein = sequenceFactory.getProtein(accession);
                        for (int index : protein.getPeptideStart(sequence)) {
                            int beginIndex = index - target;
                            int endIndex = beginIndex + nAA;
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
        switch (ptm.getType()) {
            case PTM.MODAA:
                AminoAcidPattern pattern = ptm.getPattern();
                int nAA = pattern.length();
                int target = pattern.getTarget();
                if (target >= 0 && nAA - target <= 1) {
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
                pattern = ptm.getPattern();
                target = pattern.getTarget();
                nAA = pattern.length();
                if (target == nAA - 1 && sequence.length() >= nAA) {
                    if (pattern.isStarting(sequence)) {
                        possibleSites.add(sequence.length());
                    }
                    return possibleSites;
                } else {
                    throw new IllegalArgumentException("Pattern " + pattern + " cannot be fully comprised in " + sequence);
                }
            case PTM.MODNAA:
            case PTM.MODNPAA:
                pattern = ptm.getPattern();
                target = pattern.getTarget();
                nAA = pattern.length();
                if (target == 0 && sequence.length() >= nAA) {
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
     * A method which compares to peptides. Two same peptides present the same
     * sequence and same modifications. The localization of the modification is
     * accounted only if the PTM is modification matches are confidently
     * localized.
     *
     * @param anotherPeptide another peptide
     * @return a boolean indicating if the other peptide is the same.
     */
    public boolean isSameAs(Peptide anotherPeptide) {
        return getKey().equals(anotherPeptide.getKey());
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
        if (anotherPeptide.getModificationMatches().size() != modifications.size()) {
            return false;
        }
        boolean found;
        for (ModificationMatch modificationMatch1 : modifications) {
            found = false;
            for (ModificationMatch modificationMatch2 : anotherPeptide.getModificationMatches()) {
                if (modificationMatch1.getTheoreticPtm().equals(modificationMatch2.getTheoreticPtm())
                        && modificationMatch1.getModificationSite() == modificationMatch2.getModificationSite()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the N-terminal of the peptide as a String. Returns "NH3" if the
     * terminal is not modified, otherwise returns the name of the modification.
     * /!\ this method will work only if the ptm found in the peptide are in the
     * PTMFactory.
     *
     * @return the N-terminal of the peptide as a String, e.g., "NH3"
     */
    public String getNTerminal() {

        String nTerm = "NH3";

        PTMFactory pTMFactory = PTMFactory.getInstance();

        for (int i = 0; i < modifications.size(); i++) {
            if (modifications.get(i).getModificationSite() == 1) { // ! (MODAA && MODMAX)
                PTM ptm = pTMFactory.getPTM(modifications.get(i).getTheoreticPtm());
                if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                    if (ptm.getShortName() != null) {
                        nTerm = ptm.getShortName();
                    } else {
                        nTerm += ptm.getName();
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
     * /!\ this method will work only if the ptm found in the peptide are in the
     * PTMFactory.
     *
     * @return the C-terminal of the peptide as a String, e.g., "COOH"
     */
    public String getCTerminal() {

        String cTerm = "COOH";

        PTMFactory pTMFactory = PTMFactory.getInstance();
        PTM ptm;
        for (int i = 0; i < modifications.size(); i++) {
            if (modifications.get(i).getModificationSite() == sequence.length()) {
                ptm = pTMFactory.getPTM(modifications.get(i).getTheoreticPtm());
                if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                    cTerm = ptm.getShortName();
                }
            }
        }

        cTerm = cTerm.replaceAll("-", " ");

        return cTerm;
    }

    /**
     * Returns the modified sequence as an HTML string with potential
     * modification sites color coding. /!\ this method will work only if the
     * ptm found in the peptide are in the PTMFactory.
     *
     * @param colors the ptm name to color mapping
     * @param includeHtmlStartEndTag if true, start and end html tags are added
     * @param peptide
     * @param mainModificationSites
     * @param secondaryModificationSites
     * @return the modified sequence as an HTML string
     */
    public static String getModifiedSequenceAsHtml(HashMap<String, Color> colors, boolean includeHtmlStartEndTag, Peptide peptide,
            HashMap<Integer, ArrayList<String>> mainModificationSites, HashMap<Integer, ArrayList<String>> secondaryModificationSites) {

        PTMFactory pTMFactory = PTMFactory.getInstance();
        String sequence = peptide.sequence;
        String modifiedSequence = "";

        if (includeHtmlStartEndTag) {
            modifiedSequence += "<html>";
        }

        modifiedSequence += peptide.getNTerminal() + "-";
        int aa;
        for (int i = 0; i < sequence.length(); i++) {
            aa = i + 1;// @TODO: use a single reference for the amino acid indexing and remove all +1 - sorry about that               
            if (mainModificationSites.containsKey(aa)
                    && !mainModificationSites.get(aa).isEmpty()) {
                for (String ptmName : mainModificationSites.get(aa)) { //There should be only one
                    PTM ptm = pTMFactory.getPTM(ptmName);
                    if (ptm.getType() == PTM.MODAA) {
                        Color ptmColor = colors.get(ptmName);
                        modifiedSequence +=
                                "<span style=\"color:#" + Util.color2Hex(Color.WHITE) + ";background:#" + Util.color2Hex(ptmColor) + "\">"
                                + sequence.charAt(i)
                                + "</span>";
                    }
                }
            } else if (secondaryModificationSites.containsKey(aa)
                    && !secondaryModificationSites.get(aa).isEmpty()) {
                for (String ptmName : secondaryModificationSites.get(aa)) { //There should be only one
                    PTM ptm = pTMFactory.getPTM(ptmName);
                    if (ptm.getType() == PTM.MODAA) {
                        Color ptmColor = colors.get(ptmName);
                        modifiedSequence +=
                                "<span style=\"color:#" + Util.color2Hex(ptmColor) + ";background:#" + Util.color2Hex(Color.WHITE) + "\">"
                                + sequence.charAt(i)
                                + "</span>";
                    }
                }
            } else {
                modifiedSequence += sequence.charAt(i);
            }
        }

        modifiedSequence += "-" + peptide.getCTerminal();

        if (includeHtmlStartEndTag) {
            modifiedSequence += "</html>";
        }

        return modifiedSequence;
    }

    /**
     * Returns the modified sequence as an HTML string with modification color
     * coding. /!\ this method will work only if the ptm found in the peptide
     * are in the PTMFactory.
     *
     * @param colors the ptm name to color mapping
     * @param includeHtmlStartEndTag if true, start and end html tags are added
     * @return the modified sequence as an HTML string
     */
    public String getModifiedSequenceAsHtml(HashMap<String, Color> colors, boolean includeHtmlStartEndTag) {

        PTMFactory pTMFactory = PTMFactory.getInstance();
        PTM ptm;

        String modifiedSequence = "";

        if (includeHtmlStartEndTag) {
            modifiedSequence += "<html>";
        }

        try {
            modifiedSequence = modifiedSequence + getNTerminal() + "-";
        } catch (Exception e) {
            String debug = getNTerminal();
        }

        for (int i = 0; i < sequence.length(); i++) {

            boolean modifiedResidue = false;

            for (int j = 0; j < modifications.size(); j++) {
                ptm = pTMFactory.getPTM(modifications.get(j).getTheoreticPtm());
                if (ptm.getType() == PTM.MODAA && modifications.get(j).isVariable()) {

                    if (modifications.get(j).getModificationSite() == (i + 1)) {

                        Color ptmColor = colors.get(modifications.get(j).getTheoreticPtm());

                        modifiedSequence +=
                                //"<span style=\"color:#" + Util.color2Hex(ptmColor) + "\">"
                                //"<span style=\"color:#" + Util.color2Hex(ptmColor) + ";background:#" + Util.color2Hex(Color.WHITE) + "\">"
                                "<span style=\"color:#" + Util.color2Hex(Color.WHITE) + ";background:#" + Util.color2Hex(ptmColor) + "\">"
                                + sequence.charAt(i)
                                + "</span>";

                        modifiedResidue = true;
                    }
                } else {
                    // @TODO: do something with terminal mods too??
                }
            }

            if (!modifiedResidue) {
                modifiedSequence += sequence.charAt(i);
            }
        }

        modifiedSequence = modifiedSequence + "-" + getCTerminal();

        if (includeHtmlStartEndTag) {
            modifiedSequence += "</html>";
        }

        return modifiedSequence;
    }

    /**
     * Returns a map of the ptm short names to the ptm colors. E.g., key: <ox>,
     * element: oxidation of m.
     *
     * @param ptmColors the ptm color map
     * @return a map of the ptm short names to the ptm colors
     */
    public HashMap<String, Color> getPTMShortNameColorMap(HashMap<String, Color> ptmColors) {

        HashMap<String, Color> shortNameColorMap = new HashMap<String, Color>();
        PTMFactory pTMFactory = PTMFactory.getInstance();

        for (int j = 0; j < modifications.size(); j++) {
            PTM ptm = pTMFactory.getPTM(modifications.get(j).getTheoreticPtm());

            if (ptm.getType() == PTM.MODAA && modifications.get(j).isVariable()) {
                shortNameColorMap.put("<" + ptm.getShortName() + ">", ptmColors.get(modifications.get(j).getTheoreticPtm()));
            }
        }

        return shortNameColorMap;
    }

    /**
     * Returns a map of the ptm short names to the ptm long names for the
     * modification in this peptide. E.g., key: <ox>, element oxidation of m.
     *
     * @return a map of the ptm short names to the ptm long names
     */
    public HashMap<String, String> getPTMShortNameMap() {

        HashMap<String, String> shortNameMap = new HashMap<String, String>();
        PTMFactory pTMFactory = PTMFactory.getInstance();

        for (int j = 0; j < modifications.size(); j++) {
            PTM ptm = pTMFactory.getPTM(modifications.get(j).getTheoreticPtm());

            if (ptm.getType() == PTM.MODAA && modifications.get(j).isVariable()) {
                shortNameMap.put("<" + ptm.getShortName() + ">", ptm.getName());
            }
        }

        return shortNameMap;
    }

    /**
     * Returns the modified sequence as a string, e.g., NH2-PEP<mod>TIDE-COOH.
     * /!\ this method will work only if the ptm found in the peptide are in the
     * PTMFactory.
     *
     * @param includeTerminals if true, the terminals are included
     * @return the modified sequence as a string
     */
    public String getModifiedSequenceAsString(boolean includeTerminals) {

        PTMFactory ptmFactory = PTMFactory.getInstance();

        String modifiedSequence = "";

        if (includeTerminals) {
            modifiedSequence += getNTerminal() + "-";
        }

        for (int i = 0; i < sequence.length(); i++) {

            boolean modifiedResidue = false;

            for (int j = 0; j < modifications.size(); j++) {
                PTM ptm = ptmFactory.getPTM(modifications.get(j).getTheoreticPtm());

                if (ptm.getType() == PTM.MODAA && modifications.get(j).isVariable()) {

                    if (modifications.get(j).getModificationSite() == (i + 1)) {
                        modifiedSequence += sequence.charAt(i) + "<" + ptm.getShortName() + ">";
                        modifiedResidue = true;
                    }
                }
            }

            if (!modifiedResidue) {
                modifiedSequence += sequence.charAt(i);
            }
        }

        if (includeTerminals) {
            modifiedSequence += "-" + getCTerminal();
        }

        return modifiedSequence;
    }

    /**
     * Returns the indexes of the residues in the peptide that contain at least
     * one modification.
     *
     * @return the indexes of the modified residues
     */
    public ArrayList<Integer> getModifiedIndexes() {

        ArrayList<Integer> modifiedResidues = new ArrayList<Integer>();
        PTMFactory ptmFactory = PTMFactory.getInstance();

        for (int i = 0; i < sequence.length(); i++) {
            for (int j = 0; j < modifications.size(); j++) {
                PTM ptm = ptmFactory.getPTM(modifications.get(j).getTheoreticPtm());
                if (ptm.getType() == PTM.MODAA && modifications.get(j).isVariable()) {
                    if (modifications.get(j).getModificationSite() == (i + 1)) {
                        modifiedResidues.add(i + 1);
                    }
                }
            }
        }

        return modifiedResidues;
    }

    /**
     * Estimates the theoretic mass of the peptide. The previous version is silently overwritten.
     *
     * @throws IllegalArgumentException if the peptide sequence contains unknown
     * amino acids
     */
    public void estimateTheoreticMass() throws IllegalArgumentException {

        mass = Atom.H.mass;
        AminoAcid currentAA;

        for (int aa = 0; aa < sequence.length(); aa++) {
            try {
                currentAA = AminoAcid.getAminoAcid(sequence.charAt(aa));

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
     * @return a list of proteins where this peptide can be found in the
     * N-terminus
     * @throws IOException exception thrown whenever an error occurred while
     * reading the protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading the protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading the protein sequence
     */
    public ArrayList<String> isNterm() throws IOException, IllegalArgumentException, InterruptedException {
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        Protein protein;
        ArrayList<String> result = new ArrayList<String>();
        for (String accession : parentProteins) {
            protein = sequenceFactory.getProtein(accession);
            if (protein.isNTerm(sequence)) {
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
     * @return a list of proteins where this peptide can be found in the
     * C-terminus
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     */
    public ArrayList<String> isCterm() throws IOException, IllegalArgumentException, InterruptedException {
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        Protein protein;
        ArrayList<String> result = new ArrayList<String>();
        for (String accession : parentProteins) {
            protein = sequenceFactory.getProtein(accession);
            if (protein.isCTerm(sequence)) {
                result.add(accession);
            }
        }
        return result;
    }
}
