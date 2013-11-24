package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class models an enzyme.
 *
 * @author Marc Vaudel
 */
public class Enzyme extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -1852087173903613377L;
    /*
     * The enzyme id.
     */
    private int id;
    /*
     * The enzyme name.
     */
    private String name;
    /*
     * The amino acids before cleavage.
     */
    private ArrayList<Character> aminoAcidBefore = new ArrayList<Character>();
    /*
     * The amino acids after cleavage.
     */
    private ArrayList<Character> aminoAcidAfter = new ArrayList<Character>();
    /*
     * The restriction amino acids before cleavage.
     */
    private ArrayList<Character> restrictionBefore = new ArrayList<Character>();
    /*
     * The restriction amino acids after cleavage.
     */
    private ArrayList<Character> restrictionAfter = new ArrayList<Character>();
    /**
     * If true, the enzyme is considered as semi-specific, meaning that only one
     * end of the resulting peptide has to be enzymatic.
     */
    private Boolean isSemiSpecific = false;

    /**
     * Constructor for an Enzyme.
     *
     * @param id the enzyme id which should be OMSSA compatible.
     * @param name the name of the enzyme
     * @param aminoAcidBefore the amino acids which can be found before the
     * cleavage
     * @param restrictionBefore the amino acids which should not be found before
     * the cleavage
     * @param aminoAcidAfter the amino acids which should be found after the
     * cleavage
     * @param restrictionAfter the amino acids which should not be found after
     * the cleavage
     */
    public Enzyme(int id, String name, String aminoAcidBefore, String restrictionBefore, String aminoAcidAfter, String restrictionAfter) {
        this(id, name, aminoAcidBefore, restrictionBefore, aminoAcidAfter, restrictionAfter, false);
    }

    /**
     * Constructor for an Enzyme.
     *
     * @param id the enzyme id which should be OMSSA compatible.
     * @param name the name of the enzyme
     * @param aminoAcidBefore the amino acids which can be found before the
     * cleavage
     * @param restrictionBefore the amino acids which should not be found before
     * the cleavage
     * @param aminoAcidAfter the amino acids which should be found after the
     * cleavage
     * @param restrictionAfter the amino acids which should not be found after
     * the cleavage
     * @param isSemiSpecific if true, the enzyme is considered as semi-specific,
     * meaning that only one end of the resulting peptide has to be enzymatic
     */
    public Enzyme(int id, String name, String aminoAcidBefore, String restrictionBefore, String aminoAcidAfter, String restrictionAfter, Boolean isSemiSpecific) {
        this.id = id;
        this.name = name;
        this.isSemiSpecific = isSemiSpecific;
        for (char aa : aminoAcidBefore.toCharArray()) {
            this.aminoAcidBefore.add(aa);
        }
        for (char aa : restrictionBefore.toCharArray()) {
            this.restrictionBefore.add(aa);
        }
        for (char aa : aminoAcidAfter.toCharArray()) {
            this.aminoAcidAfter.add(aa);
        }
        for (char aa : restrictionAfter.toCharArray()) {
            this.restrictionAfter.add(aa);
        }
    }

    /**
     * Get the enzyme name.
     *
     * @return The enzyme name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Get the enzyme id.
     *
     * @return The enzyme number
     */
    public int getId() {
        return id;
    }

    /**
     * Get the X!Tandem enzyme format.
     *
     * @return The enzyme X!Tandem format as String
     */
    public String getXTandemFormat() {
        String result = "";

        if (name.equals("Asp-N + Glu-C")) { //  special case as this enzyme has two cleavage sites
            result = "[E]|[X],[X]|[D]"; // @TODO: should be made generic if we stop using omssa enzymes...
        } else {

            // @TODO: should [X] be used more??

            if (aminoAcidBefore.size() > 0) {
                result += "[";
                for (Character aa : aminoAcidBefore) {
                    result += aa;
                }
                result += "]";
            }

            if (restrictionBefore.size() > 0) {
                result += "{";
                for (Character aa : restrictionBefore) {
                    result += aa;
                }
                result += "}";
            }

            result += "|";

            if (aminoAcidAfter.size() > 0) {
                result += "[";
                for (Character aa : aminoAcidAfter) {
                    result += aa;
                }
                result += "]";
            }

            if (restrictionAfter.size() > 0) {
                result += "{";
                for (Character aa : restrictionAfter) {
                    result += aa;
                }
                result += "}";
            }
        }

        return result;
    }

    /**
     * Getter for the amino acids potentially following the cleavage.
     *
     * @return the amino acids potentially following the cleavage
     */
    public ArrayList<Character> getAminoAcidAfter() {
        return aminoAcidAfter;
    }

    /**
     * Getter for the amino acids potentially preceding the cleavage.
     *
     * @return the amino acids potentially preceding the cleavage
     */
    public ArrayList<Character> getAminoAcidBefore() {
        return aminoAcidBefore;
    }

    /**
     * Getter for the amino acids restricting when following the cleavage.
     *
     * @return the amino acids restricting when following the cleavage
     */
    public ArrayList<Character> getRestrictionAfter() {
        return restrictionAfter;
    }

    /**
     * Getter for the amino acids restricting when preceding the cleavage.
     *
     * @return the amino acids restricting when preceding the cleavage
     */
    public ArrayList<Character> getRestrictionBefore() {
        return restrictionBefore;
    }

    /**
     * Returns a boolean indicating whether a cleavage site was implemented for
     * this enzyme.
     *
     * @return a boolean indicating whether a cleavage site was implemented for
     * this enzyme
     * @deprecated use isSemiSpecific instead
     */
    public boolean enzymeCleaves() {
        return !getAminoAcidBefore().isEmpty() || !getAminoAcidAfter().isEmpty();
    }

    /**
     * Returns a boolean indicating whether the given amino acids represent a
     * cleavage site. Trypsin example: (D, E) returns false (R, D) returns true
     * Note: returns false if no cleavage site is implemented.
     *
     * @param aaBefore the amino acid before the cleavage site
     * @param aaAfter the amino acid after the cleavage site
     * @return true if the amino acid combination can represent a cleavage site
     * @deprecated use the isCleavageSite method using chars as input instead
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
                if (possibleAaBefore == aa1.charValue()) {
                    boolean restriction = false;
                    for (Character aa2 : restrictionAfter) {
                        aminoAcid = AminoAcid.getAminoAcid(aaAfter);
                        for (char possibleAaAfter : aminoAcid.getSubAminoAcids()) {
                            if (possibleAaAfter == aa2.charValue()) {
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
                if (possibleAaAfter == aa1.charValue()) {
                    boolean restriction = false;
                    for (Character aa2 : restrictionBefore) {
                        aminoAcid = AminoAcid.getAminoAcid(aaAfter);
                        for (char possibleAaBefore : aminoAcid.getSubAminoAcids()) {
                            if (possibleAaBefore == aa2.charValue()) {
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
     * @param nMin the minimal size for a peptide
     * @param nMax the maximal size for a peptide
     * @return a list of expected peptide sequences
     */
    public ArrayList<String> digest(String sequence, int nMissedCleavages, int nMin, int nMax) {

        char aa, aaBefore;
        char aaAfter = sequence.charAt(0);
        String currentPeptide = aaAfter + "";
        ArrayList<String> results = new ArrayList<String>();

        HashMap<Integer, ArrayList<String>> mc = new HashMap<Integer, ArrayList<String>>();
        for (int i = 1; i <= nMissedCleavages; i++) {
            mc.put(i, new ArrayList<String>());
        }

        for (int i = 1; i < sequence.length(); i++) {

            aa = sequence.charAt(i);
            aaBefore = aaAfter;
            aaAfter = aa;

            if (isCleavageSite(aaBefore, aaAfter) && !currentPeptide.equals("")) {

                if (currentPeptide.length() >= nMin && currentPeptide.length() <= nMax && !results.contains(currentPeptide)) {
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
                    if (mcSequence.length() >= nMin && mcSequence.length() <= nMax && !results.contains(mcSequence)) {
                        results.add(mcSequence);
                    }
                }
                currentPeptide = "";
            }
            currentPeptide += aa;
        }


        if (currentPeptide.length() >= nMin && currentPeptide.length() <= nMax && !results.contains(currentPeptide)) {
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
            if (mcSequence.length() >= nMin && mcSequence.length() <= nMax && !results.contains(mcSequence)) {
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
     * Set if the enzyme is semi-specific.
     *
     * @param isSemiSpecific if the enzyme is semi-specific
     */
    public void setSemiSpecific(boolean isSemiSpecific) {
        this.isSemiSpecific = isSemiSpecific;
    }

    /**
     * Returns true if the enzyme is semi-specific.
     *
     * @return true if the enzyme is semi-specific
     */
    public boolean isSemiSpecific() {
        if (isSemiSpecific == null) {
            return false;
        }
        return isSemiSpecific;
    }
}
