package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.ArrayList;

/**
 * This class models an enzyme.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Aug 23, 2010
 * Time: 1:44:12 PM
 */
public class Enzyme extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -1852087173903613377L;

    /*
     * The enzyme id
     */
    private int id;

    /*
     * The enzyme name
     */
    private String name;

    /*
     * The amino-acids before cleavage
     */
    private ArrayList<Character> aminoAcidBefore = new ArrayList<Character>();

    /*
     * The amino-acids after cleavage
     */
    private ArrayList<Character> aminoAcidAfter = new ArrayList<Character>();

    /*
     * The restriction amino-acids before cleavage
     */
    private ArrayList<Character> restrictionBefore = new ArrayList<Character>();

    /*
     * The restriction amino-acids after cleavage
     */
    private ArrayList<Character> restrictionAfter = new ArrayList<Character>();

    /**
     * Get the enzyme name
     *
     * @return          The enzyme name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Get the enzyme id
     *
     * @return          The enzyme number
     */
    public int getId() {
        return id;
    }

    /**
     * Constructor for an Enzyme
     *
     * @param id                    the enzyme id which should be OMSSA compatible.
     * @param name                  the name of the enzyme
     * @param aminoAcidBefore       the amino-acids which can be found before the cleavage
     * @param restrictionBefore     the amino-acids which should not be found before the cleavage
     * @param aminoAcidAfter        the amino-acids which should be found after the cleavage
     * @param restrictionAfter      the amino-acids which should not be found after the cleavage
     */
    public Enzyme(int id, String name, String aminoAcidBefore, String restrictionBefore, String aminoAcidAfter, String restrictionAfter) {
        this.id = id;
        this.name = name;
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
     * Get the X!Tandem enzyme format
     *
     * @return          The enzyme X!Tandem format as String
     */
    public String getXTandemFormat() {
        String result = "";

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

        return result;
    }

    /**
     * Getter for the amino acids potentially following the cleavage
     * @return the amino acids potentially following the cleavage
     */
    public ArrayList<Character> getAminoAcidAfter() {
        return aminoAcidAfter;
    }

    /**
     * Getter for the amino acids potentially preceding the cleavage
     * @return the amino acids potentially preceding the cleavage
     */
    public ArrayList<Character> getAminoAcidBefore() {
        return aminoAcidBefore;
    }

    /**
     * Getter for the amino acids restricting when following the cleavage
     * @return the amino acids restricting when following the cleavage
     */
    public ArrayList<Character> getRestrictionAfter() {
        return restrictionAfter;
    }

    /**
     * Getter for the amino acids restricting when preceding the cleavage
     * @return the amino acids restricting when preceding the cleavage
     */
    public ArrayList<Character> getRestrictionBefore() {
        return restrictionBefore;
    }

    /**
     * Digests a protein sequence in a list of expected peptide sequences.
     * 
     * @param sequence              the protein sequence
     * @param nMissedCleavages      the allowed number of missed cleavages
     * @param nMin                  the minimal size for a peptide
     * @param nMax                  the maximal size for a peptide
     * @return a list of expected peptide sequences
     */
    public ArrayList<String> digest(String sequence, int nMissedCleavages, int nMin, int nMax) {
        ArrayList<String> noCleavage = new ArrayList<String>();
        String tempPeptide, tempSequence = sequence;
        int tempCleavage, cleavage;
        while (tempSequence.length() > 1) {
            cleavage = 0;
            for (Character aa : getAminoAcidAfter()) {
                tempCleavage = tempSequence.substring(0, tempSequence.length() - 1).lastIndexOf(aa) - 1;
                while (getRestrictionBefore().contains(tempSequence.charAt(tempCleavage)) && tempCleavage > cleavage) {
                    tempCleavage = tempSequence.substring(0, tempCleavage - 1).lastIndexOf(aa) - 1;
                }
                if (tempCleavage > cleavage && !getRestrictionBefore().contains(tempSequence.charAt(tempCleavage))) {
                    cleavage = tempCleavage;
                }
            }
            for (Character aa : getAminoAcidBefore()) {
                tempCleavage = tempSequence.substring(0, tempSequence.length() - 1).lastIndexOf(aa);
                while (getRestrictionAfter().contains(tempSequence.charAt(tempCleavage + 1)) && tempCleavage > cleavage) {
                    tempCleavage = tempSequence.substring(0, tempCleavage - 1).lastIndexOf(aa);
                }
                if (tempCleavage > cleavage && !getRestrictionAfter().contains(tempSequence.charAt(tempCleavage + 1))) {
                    cleavage = tempCleavage;
                }
            }
            if (cleavage == 0) {
                if (tempSequence.length() <= nMax && tempSequence.length() >= nMin) {
                    noCleavage.add(tempSequence);
                }
                break;
            }
            tempPeptide = tempSequence.substring(cleavage + 1);
            if (tempPeptide.length() <= nMax) {
                noCleavage.add(tempPeptide);
            }
            tempSequence = tempSequence.substring(0, cleavage + 1);
        }
        ArrayList<String> result = new ArrayList<String>();
        for (String peptide : noCleavage) {
            if (peptide.length() >= nMin && peptide.length() <= nMax) {
                result.add(peptide);
            }
        }
        if (nMissedCleavages > 0 && noCleavage.size() > 0) {
            for (int nmc = 1; nmc <= nMissedCleavages; nmc++) {
                if (noCleavage.size() > 0) {
                    for (int i = noCleavage.size() - 1; i > 0; i--) {
                        noCleavage.set(i, noCleavage.get(i) + noCleavage.get(i - 1));
                    }
                    noCleavage.remove(0);
                    for (String peptide : noCleavage) {
                        if (peptide.length() <= nMax && peptide.length() >= nMin) {
                            result.add(peptide);
                        }
                    }
                }
            }
        }
        return result;
    }
}
