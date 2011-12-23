package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.awt.Color;
import java.util.*;

/**
 * This class models a peptide.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:56:40 AM
 */
public class Peptide extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 5632064601627536034L;
    /**
     * The peptide sequence
     */
    private String sequence;
    /**
     * The peptide mass
     */
    private Double mass;
    /**
     * The parent proteins
     */
    private ArrayList<String> parentProteins = new ArrayList<String>();
    /**
     * The modifications carried by the peptide
     */
    private ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();

    /**
     * Constructor for the peptide
     */
    public Peptide() {
    }

    /**
     * Constructor for the peptide
     *
     * @param aSequence                     The peptide sequence
     * @param parentProteins                The parent proteins
     * @param modifications                 The PTM of this peptide
     * @throws IllegalArgumentException     Thrown if the peptide sequence contains unknown amino acids
     */
    public Peptide(String aSequence, ArrayList<String> parentProteins, ArrayList<ModificationMatch> modifications) throws IllegalArgumentException {
        this.sequence = aSequence;
        for (ModificationMatch mod : modifications) {
            this.modifications.add(mod);
        }
        estimateTheoreticMass();
        for (String protein : parentProteins) {
            this.parentProteins.add(protein);
        }
    }

    /**
     * Constructor for the peptide
     *
     * @param aSequence      The peptide sequence
     * @param mass           The peptide mass
     * @param parentProteins The parent proteins
     * @param modifications  The PTM of this peptide
     */
    public Peptide(String aSequence, Double mass, ArrayList<String> parentProteins, ArrayList<ModificationMatch> modifications) {
        this.sequence = aSequence;
        this.mass = mass;
        for (ModificationMatch mod : modifications) {
            this.modifications.add(mod);
        }
        for (String protein : parentProteins) {
            this.parentProteins.add(protein);
        }
    }

    /**
     * getter for the mass
     *
     * @return the peptide mass
     */
    public Double getMass() {
        return mass;
    }

    /**
     * getter for the modifications carried by this peptide
     *
     * @return the modifications matches as found by the search engine
     */
    public ArrayList<ModificationMatch> getModificationMatches() {
        return modifications;
    }

    /**
     * Clears the list of imported modification matches
     */
    public void clearModificationMAtches() {
        modifications.clear();
    }

    /**
     * Adds a modification match
     * @param modificationMatch the modification match to add
     */
    public void addModificationMatch(ModificationMatch modificationMatch) {
        modifications.add(modificationMatch);
    }

    /**
     * getter for the sequence
     *
     * @return the peptide sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Returns the amount of missed cleavages using the specified enzyme
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
     * Returns the amount of missed cleavages using the specified enzyme for the given sequence
     * @param sequence      the peptide sequence
     * @param enzyme        the enzyme used
     * @return              the amount of missed cleavages
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
     * Getter for the parent proteins
     *
     * @return the parent proteins
     */
    public ArrayList<String> getParentProteins() {
        return parentProteins;
    }

    /**
     * Sets the parent proteins
     * @param parentProteins the parent proteins as list
     */
    public void setParentProteins(ArrayList<String> parentProteins) {
        this.parentProteins = parentProteins;
    }

    /**
     * Returns the index of a peptide. index = SEQUENCE_mod1_mod2 with modifications ordered alphabetically.
     *
     * @return the index of a peptide
     */
    public String getKey() {
        ArrayList<String> tempModifications = new ArrayList<String>();
        for (ModificationMatch mod : getModificationMatches()) {
            if (mod.isVariable()) {
                if (mod.getTheoreticPtm() != null) {
                    tempModifications.add(mod.getTheoreticPtm());
                } else {
                    tempModifications.add("unknown-modification");
                }
            }
        }
        Collections.sort(tempModifications);
        String result = sequence;
        for (String mod : tempModifications) {
            result += "_" + mod;
        }
        return result;
    }

    /**
     * Returns a boolean indicating whether the peptide has variable modifications based on its key
     * @param peptideKey the peptide key
     * @return a boolean indicating whether the peptide has variable modifications
     */
    public static boolean isModified(String peptideKey) {
        return peptideKey.contains("_");
    }

    /**
     * Returns a boolean indicating whether the peptide has the given variable modification based on its key
     * @param peptideKey the peptide key
     * @param modification the name of the modification
     * @return a boolean indicating whether the peptide has variable modifications
     */
    public static boolean isModified(String peptideKey, String modification) {
        return peptideKey.contains(modification);
    }

    /**
     * Returns how many of the given modification was found in the given peptide 
     * @param peptideKey the peptide key
     * @param modification the name of the modification
     * @return the amount of modifications
     */
    public static int getModificationCount(String peptideKey, String modification) {
        String test = "_" + peptideKey + "_";
        return test.split(modification).length;
    }

    /**
     * Returns the sequence of the peptide indexed by the given key
     * @param peptideKey the peptide key
     * @return the corresponding sequence
     */
    public static String getSequence(String peptideKey) {
        int index = peptideKey.indexOf("_");
        if (index > 0) {
            return peptideKey.substring(0, peptideKey.indexOf("_"));
        } else {
            return peptideKey;
        }
    }

    /**
     * Returns a list of names of the variable modifications found in the key of a peptide
     * @param peptideKey the key of a peptide
     * @return a list of names of the variable modifications found in the key
     */
    public static ArrayList<String> getModificationFamily(String peptideKey) {
        ArrayList<String> result = new ArrayList<String>();
        String[] parsedKey = peptideKey.split("_");
        for (int i = 1; i < parsedKey.length; i++) {
            result.add(parsedKey[i]);
        }
        return result;
    }

    /**
     * returns the potential modification sites as an ordered list of string. 0 is the first aa.
     * @param sequence  the sequence of the peptide of interest
     * @param ptm       the PTM considered
     * @return          a list of potential modification sites
     */
    public static ArrayList<Integer> getPotentialModificationSites(String sequence, PTM ptm) {
        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        String tempSequence;
        int tempIndex, ref;
        for (String aa : ptm.getResidues()) {
            ref = 0;
            tempSequence = sequence;
            while ((tempIndex = tempSequence.indexOf(aa)) >= 0) {
                possibleSites.add(ref + tempIndex);
                tempSequence = tempSequence.substring(tempIndex + 1);
                ref += tempIndex + 1;
            }
        }
        return possibleSites;
    }

    /**
     * a method which compares to peptides. Two same peptides present the same sequence and same modifications at the same place.
     *
     * @param anotherPeptide another peptide
     * @return a boolean indicating if the other peptide is the same.
     */
    public boolean isSameAs(Peptide anotherPeptide) {
        return getKey().equals(anotherPeptide.getKey());
    }

    /**
     * Indicates whether another peptide has the same modifications at the same localization as this peptide. This method comes as a complement of isSameAs which does not account for PTM location.
     * @param anotherPeptide    another peptide
     * @return true if the other peptide has the same positions at the same location as the considered peptide
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
     * /!\ this method will work only if the ptm found in the peptide are in the PTMFactory
     * 
     * @return the N-terminal of the peptide as a String, e.g., "NH3"
     */
    public String getNTerminal() {

        String nTerm = "NH3";

        PTMFactory pTMFactory = PTMFactory.getInstance();
        PTM ptm;
        for (int i = 0; i < modifications.size(); i++) {
            if (modifications.get(i).getModificationSite() == 1) { // ! (MODAA && MODMAX)
                ptm = pTMFactory.getPTM(modifications.get(i).getTheoreticPtm());
                if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                    nTerm = ptm.getShortName();
                }
            }
        }

        nTerm = nTerm.replaceAll("-", " ");

        return nTerm;
    }

    /**
     * Returns the C-terminal of the peptide as a String. Returns "COOH" if the 
     * terminal is not modified, otherwise returns the name of the modification.
     * /!\ this method will work only if the ptm found in the peptide are in the PTMFactory
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
     * Returns the modified sequence as an HTML string with potential modification sites
     * color coding.
     * /!\ this method will work only if the ptm found in the peptide are in the PTMFactory
     * 
     * @param colors                    the ptm name to color mapping  
     * @param includeHtmlStartEndTag    if true, start and end html tags are added
     * @param peptide 
     * @param mainModificationSites 
     * @param secondaryModificationSites 
     * @return                          the modified sequence as an HTML string
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
aa = i+1;// @TODO: use a single reference for the amino acid indexing and remove all +1 - sorry about that               
            if (mainModificationSites.containsKey(aa)
                    && !mainModificationSites.get(aa).isEmpty()) { 
                for (String ptmName : mainModificationSites.get(aa)) { //There should be only one
                    PTM ptm = pTMFactory.getPTM(ptmName);
                    if (ptm.getType() == PTM.MODAA) {
                        Color ptmColor = colors.get(ptmName);
                        modifiedSequence +=
                                //"<span style=\"color:#" + Util.color2Hex(ptmColor) + "\">"
                                //"<span style=\"color:#" + Util.color2Hex(ptmColor) + ";background:#" + Util.color2Hex(Color.WHITE) + "\">"
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
                                //"<span style=\"color:#" + Util.color2Hex(ptmColor) + "\">"
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
     * Returns the modified sequence as an HTML string with modification 
     * color coding.
     * /!\ this method will work only if the ptm found in the peptide are in the PTMFactory
     * 
     * @param colors                    the ptm name to color mapping  
     * @param includeHtmlStartEndTag    if true, start and end html tags are added
     * @return                          the modified sequence as an HTML string
     */
    public String getModifiedSequenceAsHtml(HashMap<String, Color> colors, boolean includeHtmlStartEndTag) {

        PTMFactory pTMFactory = PTMFactory.getInstance();
        PTM ptm;

        String modifiedSequence = "";

        if (includeHtmlStartEndTag) {
            modifiedSequence += "<html>";
        }

        modifiedSequence = modifiedSequence + getNTerminal() + "-";

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
     * @param ptmColors  the ptm color map
     * @return           a map of the ptm short names to the ptm colors
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
     * /!\ this method will work only if the ptm found in the peptide are in the PTMFactory
     * 
     * @param includeTerminals      if true, the terminals are included
     * @return                      the modified sequence as a string
     */
    public String getModifiedSequenceAsString(boolean includeTerminals) {

        PTMFactory pTMFactory = PTMFactory.getInstance();

        String modifiedSequence = "";

        if (includeTerminals) {
            modifiedSequence += getNTerminal() + "-";
        }

        for (int i = 0; i < sequence.length(); i++) {

            boolean modifiedResidue = false;

            for (int j = 0; j < modifications.size(); j++) {
                PTM ptm = pTMFactory.getPTM(modifications.get(j).getTheoreticPtm());

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
     * Estimates the theoretic mass of the peptide
     * 
     * @throws IllegalArgumentException if the peptide sequence contains unknown amino acids
     */
    private void estimateTheoreticMass() throws IllegalArgumentException {

        mass = Atom.H.mass;
        AminoAcid currentAA;

        for (int aa = 0; aa < sequence.length(); aa++) {
            try {
                currentAA = AminoAcid.getAminoAcid(sequence.charAt(aa));
                mass += currentAA.monoisotopicMass;
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
}
