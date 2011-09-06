package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;

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
    private ArrayList<String> parentProteins;
    /**
     * The modifications carried by the peptide
     */
    private ArrayList<ModificationMatch> modifications;

    /**
     * Constructor for the peptide
     */
    public Peptide() {
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
        this.parentProteins = parentProteins;
        this.modifications = modifications;
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
        ArrayList<String> modifications = new ArrayList<String>();
        for (ModificationMatch mod : getModificationMatches()) {
            if (mod.isVariable()) {
                if (mod.getTheoreticPtm() != null) {
                    modifications.add(mod.getTheoreticPtm().getName());
                } else {
                    modifications.add("unknown-modification");
                }
            }
        }
        Collections.sort(modifications);
        String result = sequence;
        for (String mod : modifications) {
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
                if (modificationMatch1.getTheoreticPtm().getName().equals(modificationMatch2.getTheoreticPtm().getName())
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
     * 
     * @return the N-terminal of the peptide as a String, e.g., "NH3"
     */
    public String getNTerminal () {
        
        String nTerm = "NH3";
        
        for (int i=0; i < modifications.size(); i++) {
            if (modifications.get(i).getModificationSite() == 1) { // ! (MODAA && MODMAX)
                if (modifications.get(i).getTheoreticPtm().getType() != PTM.MODAA && modifications.get(i).getTheoreticPtm().getType() != PTM.MODMAX) {
                    nTerm = modifications.get(i).getTheoreticPtm().getShortName();
                }
            }
        }
        
        nTerm = nTerm.replaceAll("-", " ");
        
        return nTerm;
    }
    
    /**
     * Returns the C-terminal of the peptide as a String. Returns "COOH" if the 
     * terminal is not modified, otherwise returns the name of the modification.
     * 
     * @return the C-terminal of the peptide as a String, e.g., "COOH"
     */
    public String getCTerminal () {
        
        String cTerm = "COOH";
        
        for (int i=0; i < modifications.size(); i++) {
            if (modifications.get(i).getModificationSite() == sequence.length()) {
                if (modifications.get(i).getTheoreticPtm().getType() != PTM.MODAA && modifications.get(i).getTheoreticPtm().getType() != PTM.MODMAX) {
                    cTerm = modifications.get(i).getTheoreticPtm().getShortName();
                }
            }
        }
        
        cTerm = cTerm.replaceAll("-", " ");
        
        return cTerm;
    }
}
