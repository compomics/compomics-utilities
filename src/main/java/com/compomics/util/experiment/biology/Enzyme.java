package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.pride.CvTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class models an enzyme.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Enzyme extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -1852087173903613377L;
    /*
     * The enzyme id.
    
     * @deprecated use the name as identifier.
     */
    private int id;
    /*
     * The enzyme name.
     */
    private String name;
    /*
     * The amino acids before cleavage.
     */
    private ArrayList<Character> aminoAcidBefore = new ArrayList<Character>(0);
    /*
     * The amino acids after cleavage.
     */
    private ArrayList<Character> aminoAcidAfter = new ArrayList<Character>(0);
    /*
     * The restriction amino acids before cleavage.
     */
    private ArrayList<Character> restrictionBefore = new ArrayList<Character>(0);
    /*
     * The restriction amino acids after cleavage.
     */
    private ArrayList<Character> restrictionAfter = new ArrayList<Character>(0);
    /**
     * If true, the enzyme is considered as semi-specific, meaning that only one
     * end of the resulting peptide has to be enzymatic.
     * 
     * @deprecated use the digestion preferences instead
     */
    private Boolean isSemiSpecific = false;
    /**
     * If true, the enzyme does not cleave, i.e., the whole protein sequence is
     * used.
     * 
     * @deprecated use the digestion preferences instead
     */
    private Boolean isWholeProtein = false;
    /**
     * The CV term associated to this enzyme.
     */
    private CvTerm cvTerm;

    /**
     * Constructor for an Enzyme.
     *
     * @param name the name of the enzyme
     */
    public Enzyme(String name) {
        this.name = name;
    }

    /**
     * Get the enzyme name.
     *
     * @return the enzyme name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Get the enzyme id.
     *
     * @return the enzyme number
     */
    public int getId() {
        return id;
    }
    
    /**
     * Adds an amino acid to the list of allowed amino acids after the cleavage site.
     * 
     * @param aminoAcid an amino acid represented by its single amino acid code.
     */
    public void addAminoAcidAfter(Character aminoAcid) {
        aminoAcidAfter.add(aminoAcid);
    }

    /**
     * Getter for the amino acids potentially following the cleavage. Null if none.
     *
     * @return the amino acids potentially following the cleavage
     */
    public ArrayList<Character> getAminoAcidAfter() {
        return aminoAcidAfter;
    }
    
    /**
     * Adds an amino acid to the list of allowed amino acids before the cleavage site.
     * 
     * @param aminoAcid an amino acid represented by its single amino acid code.
     */
    public void addAminoAcidBefore(Character aminoAcid) {
        aminoAcidBefore.add(aminoAcid);
    }

    /**
     * Getter for the amino acids potentially preceding the cleavage. Null if none.
     *
     * @return the amino acids potentially preceding the cleavage
     */
    public ArrayList<Character> getAminoAcidBefore() {
        return aminoAcidBefore;
    }
    
    /**
     * Adds an amino acid to the list of forbidden amino acids after the cleavage site.
     * 
     * @param aminoAcid an amino acid represented by its single amino acid code.
     */
    public void addRestrictionAfter(Character aminoAcid) {
        restrictionAfter.add(aminoAcid);
    }

    /**
     * Getter for the amino acids restricting when following the cleavage. Null if none.
     *
     * @return the amino acids restricting when following the cleavage
     */
    public ArrayList<Character> getRestrictionAfter() {
        return restrictionAfter;
    }
    
    /**
     * Adds an amino acid to the list of forbidden amino acids before the cleavage site.
     * 
     * @param aminoAcid an amino acid represented by its single amino acid code.
     */
    public void addRestrictionBefore(Character aminoAcid) {
        restrictionBefore.add(aminoAcid);
    }

    /**
     * Getter for the amino acids restricting when preceding the cleavage. Null if none.
     *
     * @return the amino acids restricting when preceding the cleavage
     */
    public ArrayList<Character> getRestrictionBefore() {
        return restrictionBefore;
    }

    /**
     * Returns a boolean indicating whether the given amino acids represent a
     * cleavage site. Trypsin example: (D, E) returns false (R, D) returns true
     * Note: returns false if no cleavage site is implemented.
     *
     * @param aaBefore the amino acid before the cleavage site
     * @param aaAfter the amino acid after the cleavage site
     * @return true if the amino acid combination can represent a cleavage site
     */
    public boolean isCleavageSite(String aaBefore, String aaAfter) {
        if (aaBefore.length() == 0 || aaAfter.length() == 0) {
            return true;
        }
        return isCleavageSite(aaBefore.charAt(aaBefore.length() - 1), aaAfter.charAt(0));
    }

    /**
     * Returns a boolean indicating whether the given amino acids represent a
     * cleavage site. Trypsin example: (D, E) returns false (R, D) returns true
     * Note: returns false if no cleavage site is implemented.
     *
     * @param aaBefore the amino acid before the cleavage site
     * @param aaAfter the amino acid after the cleavage site
     * @return true if the amino acid combination can represent a cleavage site
     */
    public boolean isCleavageSite(char aaBefore, char aaAfter) {

        for (Character aa1 : aminoAcidBefore) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aaBefore);
            for (char possibleAaBefore : aminoAcid.getSubAminoAcids()) {
                if (possibleAaBefore == aa1) {
                    boolean restriction = false;
                    for (Character aa2 : restrictionAfter) {
                        aminoAcid = AminoAcid.getAminoAcid(aaAfter);
                        for (char possibleAaAfter : aminoAcid.getSubAminoAcids()) {
                            if (possibleAaAfter == aa2) {
                                restriction = true;
                                break;
                            }
                        }
                        if (restriction) {
                            break;
                        }
                    }
                    if (!restriction) {
                        return true;
                    }
                }
            }
        }

        for (Character aa1 : aminoAcidAfter) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aaAfter);
            for (char possibleAaAfter : aminoAcid.getSubAminoAcids()) {
                if (possibleAaAfter == aa1) {
                    boolean restriction = false;
                    for (Character aa2 : restrictionBefore) {
                        aminoAcid = AminoAcid.getAminoAcid(aaAfter);
                        for (char possibleAaBefore : aminoAcid.getSubAminoAcids()) {
                            if (possibleAaBefore == aa2) {
                                restriction = true;
                                break;
                            }
                        }
                        if (restriction) {
                            break;
                        }
                    }
                    if (!restriction) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the number of missed cleavages in an amino acid sequence.
     *
     * @param sequence the amino acid sequence as a string.
     *
     * @return the number of missed cleavages
     */
    public int getNmissedCleavages(String sequence) {
        int result = 0;
        if (sequence.length() > 1) {
            for (int i = 0; i < sequence.length() - 1; i++) {
                if (isCleavageSite(sequence.charAt(i), sequence.charAt(i + 1))) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Digests a protein sequence in a list of expected peptide sequences.
     *
     * @param sequence the protein sequence
     * @param nMissedCleavages the maximum number of missed cleavages
     * @param nMin the minimal size for a peptide (inclusive, ignored if null)
     * @param nMax the maximal size for a peptide (inclusive, ignored if null)
     *
     * @return a list of expected peptide sequences
     */
    public HashSet<String> digest(String sequence, int nMissedCleavages, Integer nMin, Integer nMax) {

        char aa, aaBefore;
        char aaAfter = sequence.charAt(0);
        String currentPeptide = aaAfter + "";
        HashSet<String> results = new HashSet<String>();

        HashMap<Integer, ArrayList<String>> mc = new HashMap<Integer, ArrayList<String>>();
        for (int i = 1; i <= nMissedCleavages; i++) {
            mc.put(i, new ArrayList<String>(nMissedCleavages));
        }

        for (int i = 1; i < sequence.length(); i++) {

            aa = sequence.charAt(i);
            aaBefore = aaAfter;
            aaAfter = aa;

            if (isCleavageSite(aaBefore, aaAfter) && !currentPeptide.equals("")) {

                if ((nMin == null || currentPeptide.length() >= nMin) && (nMax == null || currentPeptide.length() <= nMax)) {
                    results.add(currentPeptide);
                }

                for (int nMc : mc.keySet()) {
                    mc.get(nMc).add(currentPeptide);
                    while (mc.get(nMc).size() > nMc + 1) {
                        mc.get(nMc).remove(0);
                    }
                    String mcSequence = "";
                    for (String subPeptide : mc.get(nMc)) {
                        mcSequence += subPeptide;
                    }
                    if ((nMin == null || mcSequence.length() >= nMin) && (nMax == null || mcSequence.length() <= nMax)) {
                        results.add(mcSequence);
                    }
                }

                currentPeptide = "";
            }

            currentPeptide += aa;
        }

        if ((nMin == null || currentPeptide.length() >= nMin) && (nMax == null || currentPeptide.length() <= nMax)) {
            results.add(currentPeptide);
        }

        for (int nMc : mc.keySet()) {
            mc.get(nMc).add(currentPeptide);
            while (mc.get(nMc).size() > nMc + 1) {
                mc.get(nMc).remove(0);
            }
            String mcSequence = "";
            for (String subPeptide : mc.get(nMc)) {
                mcSequence += subPeptide;
            }
            if ((nMin == null || mcSequence.length() >= nMin) && (nMax == null || mcSequence.length() <= nMax)) {
                results.add(mcSequence);
            }
        }

        return results;
    }

    /**
     * Digests a protein sequence in a list of expected peptide sequences.
     *
     * @param sequence the protein sequence
     * @param nMissedCleavages the maximum number of missed cleavages
     * @param massMin the minimal mass for a peptide (inclusive)
     * @param massMax the maximal mass for a peptide (inclusive)
     *
     * @return a list of expected peptide sequences
     */
    public HashSet<String> digest(String sequence, int nMissedCleavages, Double massMin, Double massMax) {

        char aa, aaBefore;
        char aaAfter = sequence.charAt(0);
        String currentPeptide = aaAfter + "";
        Double currentMass = AminoAcid.getAminoAcid(aaAfter).getMonoisotopicMass();
        HashSet<String> results = new HashSet<String>();

        HashMap<Integer, ArrayList<String>> mc = new HashMap<Integer, ArrayList<String>>();
        for (int i = 1; i <= nMissedCleavages; i++) {
            mc.put(i, new ArrayList<String>(nMissedCleavages));
        }
        HashMap<String, Double> peptideMasses = new HashMap<String, Double>();

        for (int i = 1; i < sequence.length(); i++) {

            aa = sequence.charAt(i);
            aaBefore = aaAfter;
            aaAfter = aa;

            if (isCleavageSite(aaBefore, aaAfter) && !currentPeptide.equals("")) {

                if ((massMin == null || currentMass >= massMin) && (massMax == null || currentMass <= massMax)) {
                    results.add(currentPeptide);
                }

                for (int nMc : mc.keySet()) {
                    mc.get(nMc).add(currentPeptide);
                    peptideMasses.put(currentPeptide, currentMass);
                    while (mc.get(nMc).size() > nMc + 1) {
                        mc.get(nMc).remove(0);
                    }
                    String mcSequence = "";
                    Double mcMass = 0.0;
                    for (String subPeptide : mc.get(nMc)) {
                        mcSequence += subPeptide;
                        mcMass += peptideMasses.get(subPeptide);
                    }
                    if ((massMin == null || mcMass >= massMin) && (massMax == null || mcMass <= massMax)) {
                        results.add(mcSequence);
                    }
                }

                currentPeptide = "";
            }

            currentPeptide += aa;
            currentMass += AminoAcid.getAminoAcid(aa).getMonoisotopicMass();
        }

        if ((massMin == null || currentMass >= massMin) && (massMax == null || currentMass <= massMax)) {
            results.add(currentPeptide);
        }

        for (int nMc : mc.keySet()) {
            mc.get(nMc).add(currentPeptide);
            peptideMasses.put(currentPeptide, currentMass);
            while (mc.get(nMc).size() > nMc + 1) {
                mc.get(nMc).remove(0);
            }
            String mcSequence = "";
            Double mcMass = 0.0;
            for (String subPeptide : mc.get(nMc)) {
                mcSequence += subPeptide;
                mcMass += peptideMasses.get(subPeptide);
            }
            if ((massMin == null || mcMass >= massMin) && (massMax == null || mcMass <= massMax)) {
                results.add(mcSequence);
            }
        }

        return results;
    }

    /**
     * Returns true of the two enzymes are identical.
     *
     * @param otherEnzyme the enzyme to compare against.
     * @return true of the two enzymes are identical
     */
    public boolean equals(Enzyme otherEnzyme) {

        if (otherEnzyme == null) {
            return false;
        }
        if (this.getId() != otherEnzyme.getId()) {
            return false;
        }
        if (!this.getName().equalsIgnoreCase(otherEnzyme.getName())) {
            return false;
        }
        if (!this.getAminoAcidBefore().equals(otherEnzyme.getAminoAcidBefore())) {
            return false;
        }
        if (!this.getRestrictionBefore().equals(otherEnzyme.getRestrictionBefore())) {
            return false;
        }
        if (!this.getAminoAcidAfter().equals(otherEnzyme.getAminoAcidAfter())) {
            return false;
        }
        if (!this.getRestrictionAfter().equals(otherEnzyme.getRestrictionAfter())) {
            return false;
        }

        return true;
    }

    /**
     * Returns the description of the cleavage of this enzyme.
     *
     * @return the description of the cleavage of this enzyme
     */
    public String getDescription() {

        String description = "Cleaves ";
        if (!getAminoAcidBefore().isEmpty()) {
            description += "after ";
            for (Character aa : getAminoAcidBefore()) {
                description += aa;
            }
            if (!getAminoAcidAfter().isEmpty()) {
                description += " and ";
            }
        }
        if (!getAminoAcidAfter().isEmpty()) {
            description += "before ";
            for (Character aa : getAminoAcidBefore()) {
                description += aa;
            }
        }
        if (!getRestrictionBefore().isEmpty()) {
            description += " not preceeded by ";
            for (Character aa : getRestrictionBefore()) {
                description += aa;
            }
            if (!getRestrictionAfter().isEmpty()) {
                description += " and ";
            }
        }
        if (!getRestrictionAfter().isEmpty()) {
            description += " not followed by ";
            for (Character aa : getRestrictionAfter()) {
                description += aa;
            }
        }
        return description;
    }

    /**
     * Returns the CV term associated with this enzyme.
     * 
     * @return the CV term associated with this enzyme
     */
    public CvTerm getCvTerm() {
        return cvTerm;
    }

    /**
     * Sets the CV term associated with this enzyme.
     * 
     * @param cvTerm the CV term associated with this enzyme
     */
    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    /**
     * Returns true if the enzyme is unspecific, i.e., cleaves at every residue.
     * 
     * @deprecated use the digestion preferences instead
     *
     * @return true if the enzyme is unspecific
     */
    public boolean isUnspecific() {
        return id == 17;
    }

    /**
     * Returns true if the enzyme is semi-specific.
     * 
     * @deprecated use the digestion preferences instead
     *
     * @return true if the enzyme is semi-specific
     */
    public boolean isSemiSpecific() {
        if (isSemiSpecific == null) {
            isSemiSpecific = false;
        }
        return isSemiSpecific;
    }

    /**
     * Returns true if the enzyme does not cleave at all, i.e., the whole
     * protein is used.
     * 
     * @deprecated use the digestion preferences instead
     *
     * @return true if the enzyme does not cleave at all
     */
    public boolean isWholeProtein() {
        if (isWholeProtein == null) {
            isWholeProtein = name.equalsIgnoreCase("Whole Protein") || name.equalsIgnoreCase("Top-Down");
        }
        return isWholeProtein;
    }
}
