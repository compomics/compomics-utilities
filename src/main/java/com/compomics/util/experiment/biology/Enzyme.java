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
    private HashSet<Character> aminoAcidBefore = new HashSet<Character>(0);
    /*
     * The amino acids after cleavage.
     */
    private HashSet<Character> aminoAcidAfter = new HashSet<Character>(0);
    /*
     * The restriction amino acids before cleavage.
     */
    private HashSet<Character> restrictionBefore = new HashSet<Character>(0);
    /*
     * The restriction amino acids after cleavage.
     */
    private HashSet<Character> restrictionAfter = new HashSet<Character>(0);
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
     * Adds an amino acid to the list of allowed amino acids after the cleavage
     * site.
     *
     * @param aminoAcid an amino acid represented by its single amino acid code.
     */
    public void addAminoAcidAfter(Character aminoAcid) {
        aminoAcidAfter.add(aminoAcid);
    }

    /**
     * Getter for the amino acids potentially following the cleavage. Null if
     * none.
     *
     * @return the amino acids potentially following the cleavage
     */
    public HashSet<Character> getAminoAcidAfter() {
        return aminoAcidAfter;
    }

    /**
     * Adds an amino acid to the list of allowed amino acids before the cleavage
     * site.
     *
     * @param aminoAcid an amino acid represented by its single amino acid code.
     */
    public void addAminoAcidBefore(Character aminoAcid) {
        aminoAcidBefore.add(aminoAcid);
    }

    /**
     * Getter for the amino acids potentially preceding the cleavage. Null if
     * none.
     *
     * @return the amino acids potentially preceding the cleavage
     */
    public HashSet<Character> getAminoAcidBefore() {
        return aminoAcidBefore;
    }

    /**
     * Adds an amino acid to the list of forbidden amino acids after the
     * cleavage site.
     *
     * @param aminoAcid an amino acid represented by its single amino acid code.
     */
    public void addRestrictionAfter(Character aminoAcid) {
        restrictionAfter.add(aminoAcid);
    }

    /**
     * Getter for the amino acids restricting when following the cleavage. Null
     * if none.
     *
     * @return the amino acids restricting when following the cleavage
     */
    public HashSet<Character> getRestrictionAfter() {
        return restrictionAfter;
    }

    /**
     * Adds an amino acid to the list of forbidden amino acids before the
     * cleavage site.
     *
     * @param aminoAcid an amino acid represented by its single amino acid code.
     */
    public void addRestrictionBefore(Character aminoAcid) {
        restrictionBefore.add(aminoAcid);
    }

    /**
     * Getter for the amino acids restricting when preceding the cleavage. Null
     * if none.
     *
     * @return the amino acids restricting when preceding the cleavage
     */
    public HashSet<Character> getRestrictionBefore() {
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
     * cleavage site. Amino acid combinations are extended to find possible
     * restrictions or cleavage sites. Trypsin example: (D, E) returns false (R,
     * D) returns true Note: returns false if no cleavage site is implemented.
     *
     * @param aaBefore the amino acid before the cleavage site
     * @param aaAfter the amino acid after the cleavage site
     *
     * @return true if the amino acid combination can represent a cleavage site
     */
    public boolean isCleavageSite(Character aaBefore, Character aaAfter) {

        AminoAcid aminoAcid1 = AminoAcid.getAminoAcid(aaBefore);
        AminoAcid aminoAcid2 = AminoAcid.getAminoAcid(aaAfter);
        for (char possibleAaBefore : aminoAcid1.getSubAminoAcids()) {
            if (aminoAcidBefore.contains(possibleAaBefore)) {
                boolean restriction = false;
                for (char possibleAaAfter : aminoAcid2.getSubAminoAcids()) {
                    if (restrictionAfter.contains(possibleAaAfter)) {
                        restriction = true;
                        break;
                    }
                }
                if (!restriction) {
                    return true;
                }
            }
        }

        for (char possibleAaAfter : aminoAcid2.getSubAminoAcids()) {
            if (aminoAcidAfter.contains(possibleAaAfter)) {
                boolean restriction = false;
                for (char possibleAaBefore : aminoAcid1.getSubAminoAcids()) {
                    if (restrictionBefore.contains(possibleAaAfter)) {
                        restriction = true;
                        break;
                    }
                }
                if (!restriction) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a boolean indicating whether the given amino acids represent a
     * cleavage site. This method does not support amino acid combinations.
     * Trypsin example: (D, E) returns false (R, D) returns true Note: returns
     * false if no cleavage site is implemented.
     *
     * @param aaBefore the amino acid before the cleavage site
     * @param aaAfter the amino acid after the cleavage site
     *
     * @return true if the amino acid combination can represent a cleavage site
     */
    public boolean isCleavageSiteNoCombination(Character aaBefore, Character aaAfter) {
        return aminoAcidBefore.contains(aaBefore) && !restrictionAfter.contains(aaAfter)
                || aminoAcidAfter.contains(aaAfter) && !restrictionBefore.contains(aaBefore);
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
        StringBuilder currentPeptide = new StringBuilder();
        currentPeptide.append(aaAfter);
        HashSet<String> results = new HashSet<String>();

        HashMap<Integer, ArrayList<String>> mc = new HashMap<Integer, ArrayList<String>>();
        for (int i = 1; i <= nMissedCleavages; i++) {
            mc.put(i, new ArrayList<String>(nMissedCleavages));
        }

        for (int i = 1; i < sequence.length(); i++) {

            aa = sequence.charAt(i);
            aaBefore = aaAfter;
            aaAfter = aa;

            if (isCleavageSite(aaBefore, aaAfter) && currentPeptide.length() != 0) {

                String currentPeptideString = currentPeptide.toString();
                if ((nMin == null || currentPeptide.length() >= nMin) && (nMax == null || currentPeptide.length() <= nMax)) {
                    results.add(currentPeptideString);
                }

                for (int nMc : mc.keySet()) {
                    mc.get(nMc).add(currentPeptideString);
                    while (mc.get(nMc).size() > nMc + 1) {
                        mc.get(nMc).remove(0);
                    }
                    StringBuilder mcSequence = new StringBuilder();
                    for (String subPeptide : mc.get(nMc)) {
                        mcSequence.append(subPeptide);
                    }
                    if ((nMin == null || mcSequence.length() >= nMin) && (nMax == null || mcSequence.length() <= nMax)) {
                        results.add(mcSequence.toString());
                    }
                }

                currentPeptide = new StringBuilder();
            }

            currentPeptide.append(aa);
        }

        String currentPeptideString = currentPeptide.toString();
        if ((nMin == null || currentPeptide.length() >= nMin) && (nMax == null || currentPeptide.length() <= nMax)) {
            results.add(currentPeptideString);
        }

        for (int nMc : mc.keySet()) {
            mc.get(nMc).add(currentPeptideString);
            while (mc.get(nMc).size() > nMc + 1) {
                mc.get(nMc).remove(0);
            }
            StringBuilder mcSequence = new StringBuilder();
            for (String subPeptide : mc.get(nMc)) {
                mcSequence.append(subPeptide);
            }
            if ((nMin == null || mcSequence.length() >= nMin) && (nMax == null || mcSequence.length() <= nMax)) {
                results.add(mcSequence.toString());
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
        StringBuilder currentPeptide = new StringBuilder();
        currentPeptide.append(aaAfter);
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

            if (isCleavageSite(aaBefore, aaAfter) && currentPeptide.length() > 0) {

                String currentPeptideString = currentPeptide.toString();
                if ((massMin == null || currentMass >= massMin) && (massMax == null || currentMass <= massMax)) {
                    results.add(currentPeptideString);
                }

                for (int nMc : mc.keySet()) {
                    mc.get(nMc).add(currentPeptideString);
                    peptideMasses.put(currentPeptideString, currentMass);
                    while (mc.get(nMc).size() > nMc + 1) {
                        mc.get(nMc).remove(0);
                    }
                    StringBuilder mcSequence = new StringBuilder();
                    Double mcMass = 0.0;
                    for (String subPeptide : mc.get(nMc)) {
                        mcSequence.append(subPeptide);
                        mcMass += peptideMasses.get(subPeptide);
                    }
                    if ((massMin == null || mcMass >= massMin) && (massMax == null || mcMass <= massMax)) {
                        results.add(mcSequence.toString());
                    }
                }

                currentPeptide = new StringBuilder();
            }

            currentPeptide.append(aa);
            currentMass += AminoAcid.getAminoAcid(aa).getMonoisotopicMass();
        }

        String currentPeptideString = currentPeptide.toString();
        if ((massMin == null || currentMass >= massMin) && (massMax == null || currentMass <= massMax)) {
            results.add(currentPeptideString);
        }

        for (int nMc : mc.keySet()) {
            mc.get(nMc).add(currentPeptideString);
            peptideMasses.put(currentPeptideString, currentMass);
            while (mc.get(nMc).size() > nMc + 1) {
                mc.get(nMc).remove(0);
            }
            StringBuilder mcSequence = new StringBuilder();
            Double mcMass = 0.0;
            for (String subPeptide : mc.get(nMc)) {
                mcSequence.append(subPeptide);
                mcMass += peptideMasses.get(subPeptide);
            }
            if ((massMin == null || mcMass >= massMin) && (massMax == null || mcMass <= massMax)) {
                results.add(mcSequence.toString());
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
