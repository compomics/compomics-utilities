package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.protein.Header.DatabaseType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class models a protein.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Protein extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 1987224639519365761L;
    /**
     * The protein accession.
     */
    private String accession;
    /**
     * Boolean indicating if the protein is not existing (decoy protein for
     * instance).
     */
    private boolean decoy;
    /**
     * The protein sequence.
     */
    private String sequence;
    /**
     * The protein database type.
     */
    private DatabaseType databaseType;

    /**
     * Constructor for a protein.
     */
    public Protein() {
    }

    /**
     * Simplistic constructor for a protein (typically used when loading
     * identification files).
     *
     * @param accession The protein accession
     * @param isDecoy boolean indicating whether the protein is a decoy
     */
    public Protein(String accession, boolean isDecoy) {
        this.accession = accession;
        this.decoy = isDecoy;
    }

    /**
     * Constructor for a protein.
     *
     * @param accession The protein accession
     * @param sequence The protein sequence
     * @param isDecoy boolean indicating whether the protein is a decoy
     */
    public Protein(String accession, String sequence, boolean isDecoy) {
        this.accession = accession;
        this.sequence = sequence;
        this.decoy = isDecoy;
    }

    /**
     * Constructor for a protein.
     *
     * @param accession The protein accession
     * @param databaseType The protein database the protein comes from
     * @param sequence The protein sequence
     * @param isDecoy boolean indicating whether the protein is a decoy
     */
    public Protein(String accession, DatabaseType databaseType, String sequence, boolean isDecoy) {
        this.accession = accession;
        this.databaseType = databaseType;
        this.sequence = sequence;
        this.decoy = isDecoy;
    }

    /**
     * Indicates if the protein is factice (from a decoy database for instance).
     *
     * @return a boolean indicating if the protein is factice
     */
    public boolean isDecoy() {
        return decoy;
    }

    /**
     * Getter for the protein accession.
     *
     * @return the protein accession
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Getter for the protein database type.
     *
     * @return the protein database type
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Getter for the protein sequence.
     *
     * @return the protein sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * A method to compare proteins. For now accession based.
     *
     * @param anotherProtein an other protein
     * @return a boolean indicating if the proteins are identical
     */
    public boolean isSameAs(Protein anotherProtein) {
        return accession.equals(anotherProtein.getAccession());
    }

    /**
     * Returns the key for protein indexing. For now the protein accession.
     *
     * @return the key for protein indexing.
     */
    public String getProteinKey() {
        return accession;
    }

    /**
     * Returns the number of amino acids in the sequence.
     *
     * @return the number of amino acids in the sequence
     */
    public int getLength() {
        return sequence.length();
    }

    /**
     * Returns the number of observable amino acids of the sequence.
     *
     * @param enzyme the enzyme to use
     * @param pepMaxLength the max peptide length
     * @return the number of observable amino acids of the sequence
     */
    public int getObservableLength(Enzyme enzyme, int pepMaxLength) {

        int length = 0;
        String tempSequence = sequence;

        while (tempSequence.length() > 1) {
            int cleavage = 0;

            for (Character aa : enzyme.getAminoAcidAfter()) {
                int tempCleavage = tempSequence.substring(0, tempSequence.length() - 1).lastIndexOf(aa) - 1;
                while (enzyme.getRestrictionBefore().contains(tempSequence.charAt(tempCleavage)) && tempCleavage > cleavage) {
                    tempCleavage = tempSequence.substring(0, tempCleavage - 1).lastIndexOf(aa) - 1;
                }
                if (tempCleavage > cleavage && !enzyme.getRestrictionBefore().contains(tempSequence.charAt(tempCleavage))) {
                    cleavage = tempCleavage;
                }
            }

            for (Character aa : enzyme.getAminoAcidBefore()) {
                int tempCleavage = tempSequence.substring(0, tempSequence.length() - 1).lastIndexOf(aa);
                while (enzyme.getRestrictionAfter().contains(tempSequence.charAt(tempCleavage + 1)) && tempCleavage > cleavage) {
                    tempCleavage = tempSequence.substring(0, tempCleavage - 1).lastIndexOf(aa);
                }
                if (tempCleavage > cleavage && !enzyme.getRestrictionAfter().contains(tempSequence.charAt(tempCleavage + 1))) {
                    cleavage = tempCleavage;
                }
            }

            if (cleavage == 0) {
                if (tempSequence.length() <= pepMaxLength) {
                    length += tempSequence.length();
                }
                break;
            }

            String tempPeptide = tempSequence.substring(cleavage + 1);

            if (tempPeptide.length() <= pepMaxLength) {
                length += tempPeptide.length();
            }

            tempSequence = tempSequence.substring(0, cleavage + 1);
        }

        return length;
    }

    /**
     * Returns the number of possible peptides (not accounting PTMs nor missed
     * cleavages) with the selected enzyme.
     *
     * @param enzyme The selected enzyme
     * @return the number of possible peptides
     */
    public int getNPossiblePeptides(Enzyme enzyme) {

        int nCleavages = 1;
        ArrayList<Character> aminoAcidBefore = enzyme.getAminoAcidBefore();
        ArrayList<Character> aminoAcidAfter = enzyme.getAminoAcidAfter();
        ArrayList<Character> restrictionBefore = enzyme.getRestrictionBefore();
        ArrayList<Character> restrictionAfter = enzyme.getRestrictionAfter();

        try {
            char[] sequenceCharacters = sequence.toCharArray();
            char aaBefore, aaAfter;
            for (int i = 0; i < sequenceCharacters.length - 1; i++) {
                aaBefore = sequenceCharacters[i];
                aaAfter = sequenceCharacters[i + 1];
                if ((aminoAcidBefore.contains(aaBefore) || aminoAcidAfter.contains(aaAfter))
                        && !(restrictionBefore.contains(aaBefore) || restrictionAfter.contains(aaAfter))) {
                    nCleavages++;
                }
            }
            nCleavages++;
        } catch (Exception e) {
            // exception thrown when the sequence was not implemented. Ignore and return 0.
        }

        return nCleavages;
    }

    /**
     * Returns the protein's molecular weight. (Note that when using a
     * SequenceFactory it is recommended to use the SequenceFactory's
     * computeMolecularWeight method instead, as that method stored the computed
     * molecular weights instead of recalculating them every time.)
     *
     * @return the protein's molecular weight
     */
    public double computeMolecularWeight() {

        double mass = Atom.H.mass;

        for (int iaa = 0; iaa < sequence.length(); iaa++) {
            char aa = sequence.charAt(iaa);
            try {
                if (aa != '*') {
                    AminoAcid currentAA = AminoAcid.getAminoAcid(aa);
                    mass += currentAA.monoisotopicMass;
                }
            } catch (NullPointerException e) {
                if (aa == '>') {
                    throw new IllegalArgumentException("Error parsing the sequence of " + accession);
                } else {
                    throw new IllegalArgumentException("Unknown amino acid: " + aa);
                }
            }
        }

        mass += Atom.H.mass + Atom.O.mass;

        return mass;
    }

    /**
     * Returns the list of indexes where a peptide can be found in the protein
     * sequence.
     *
     * @param peptide the sequence of the peptide of interest
     * @return the list of indexes where a peptide can be found in a protein
     * sequence
     */
    public ArrayList<Integer> getPeptideStart(String peptide) {

        ArrayList<Integer> result = new ArrayList<Integer>();
        String tempSequence = sequence;

        while (tempSequence.lastIndexOf(peptide) >= 0) {
            int startIndex = tempSequence.lastIndexOf(peptide);
            result.add(startIndex + 1);
            tempSequence = tempSequence.substring(0, startIndex);
        }

        return result;
    }

    /**
     * Returns a boolean indicating whether the protein starts with the given
     * peptide.
     *
     * @param peptideSequence the peptide sequence
     * @return a boolean indicating whether the protein starts with the given
     * peptide
     */
    public boolean isNTerm(String peptideSequence) {
        return sequence.startsWith(peptideSequence);
    }

    /**
     * Returns a boolean indicating whether the protein ends with the given
     * peptide.
     *
     * @param peptideSequence the peptide sequence
     * @return a boolean indicating whether the protein ends with the given
     * peptide
     */
    public boolean isCTerm(String peptideSequence) {
        return sequence.endsWith(peptideSequence);
    }

    /**
     * Returns true of the peptide is non-enzymatic, i.e., has one or more end
     * points that cannot be caused by the enzyme alone. False means that both
     * the endpoints of the peptides could be caused by the selected enzyme, or
     * that it is a terminal peptide (where one end point is most likely not
     * enzymatic). Note that if a peptide maps to multiple locations on the
     * protein sequence this method returns true if one or more of these
     * peptides are non-enzymatic, even if not all mappings are non-enzymatic.
     *
     * @param peptideSequence the peptide sequence to check
     * @param enzyme the enzyme to use
     * @param numberOfMissedCleavages the maximum number of missed cleavages
     * @param minPeptideSize the minimum peptide size
     * @param maxPeptideSize the maximum peptide size
     * @return true of the peptide is non-enzymatic
     * @throws IOException
     */
    public boolean isEnzymaticPeptide(String peptideSequence, Enzyme enzyme, int numberOfMissedCleavages, int minPeptideSize, int maxPeptideSize) throws IOException {

        // get the surrounding amino acids
        HashMap<Integer, String[]> surroundingAminoAcids = getSurroundingAA(peptideSequence, 2);

        // iterate the possible extended peptide sequences
        for (int index : surroundingAminoAcids.keySet()) {
            String before = surroundingAminoAcids.get(index)[0];
            String after = surroundingAminoAcids.get(index)[1];
            String extendedPeptideSequence = before + peptideSequence + after;

            ArrayList<String> peptides = enzyme.digest(extendedPeptideSequence, numberOfMissedCleavages, minPeptideSize, maxPeptideSize);

            if (!peptides.contains(peptideSequence)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the amino acids surrounding a peptide in the sequence of the
     * given protein in a map: peptide start index -> (amino acids before, amino
     * acids after) The number of amino acids is taken from the display
     * preferences.
     *
     * @param peptide the sequence of the peptide of interest
     * @param nAA the number of amino acids to include
     * @return the amino acids surrounding a peptide in the protein sequence
     * @throws IOException Exception thrown whenever an error occurred while
     * parsing the protein sequence
     */
    public HashMap<Integer, String[]> getSurroundingAA(String peptide, int nAA) throws IOException {

        ArrayList<Integer> startIndexes = getPeptideStart(peptide);
        HashMap<Integer, String[]> result = new HashMap<Integer, String[]>();

        for (int startIndex : startIndexes) {

            startIndex--; // the provided indexes are not zero based

            result.put(startIndex, new String[2]);
            String subsequence = "";

            for (int aa = startIndex - nAA; aa < startIndex; aa++) {
                if (aa >= 0 && aa < sequence.length()) {
                    subsequence += sequence.charAt(aa);
                }
            }

            result.get(startIndex)[0] = subsequence;
            subsequence = "";

            for (int aa = startIndex + peptide.length(); aa < startIndex + peptide.length() + nAA; aa++) {
                if (aa >= 0 && aa < sequence.length()) {
                    subsequence += sequence.charAt(aa);
                }
            }

            result.get(startIndex)[1] = subsequence;
        }

        return result;
    }
}
