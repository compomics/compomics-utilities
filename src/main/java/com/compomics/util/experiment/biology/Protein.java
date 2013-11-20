package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.identification.matches.ProteinMatch;
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
     * Returns the number of observable amino acids in the sequence.
     *
     * @param enzyme the enzyme to use
     * @param pepMaxLength the max peptide length
     *
     * @return the number of observable amino acids of the sequence
     */
    public int getObservableLength(Enzyme enzyme, int pepMaxLength) {
        int length = 0, tempLength = 1;
        for (int i = 0; i < sequence.length() - 1; i++) {
            if (enzyme.isCleavageSite(sequence.charAt(i), sequence.charAt(i + 1))) {
                if (tempLength <= pepMaxLength) {
                    length += tempLength;
                }
                tempLength = 0;
            }
            tempLength++;
        }
        if (tempLength < pepMaxLength) {
            length += tempLength;
        }
        return length;
    }

    /**
     * Returns the number of cleavage sites.
     *
     * @param enzyme The selected enzyme
     *
     * @return the number of possible peptides
     */
    public int getNCleavageSites(Enzyme enzyme) {
        int nCleavageSites = 0;
        for (int i = 0; i < sequence.length() - 1; i++) {
            if (enzyme.isCleavageSite(sequence.charAt(i), sequence.charAt(i + 1))) {
                nCleavageSites++;
            }
        }
        return nCleavageSites;
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
     * @param peptideSequence the sequence of the peptide of interest
     * @param pattern the amino acid pattern
     * @param patternLength the pattern length
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return the list of indexes where a peptide can be found in a protein
     * sequence
     */
    public ArrayList<Integer> getPeptideStart(String peptideSequence, AminoAcidPattern pattern, int patternLength, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        return pattern.getIndexes(sequence, matchingType, massTolerance);
    }

    /**
     * Returns a boolean indicating whether the protein starts with the given
     * peptide.
     *
     * @param peptideSequence the peptide sequence
     * @param pattern the amino acid pattern
     * @param patternLength the pattern length
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return a boolean indicating whether the protein starts with the given
     * peptide
     */
    public boolean isNTerm(String peptideSequence, AminoAcidPattern pattern, int patternLength, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        String subSequence = sequence.substring(0, peptideSequence.length());
        return pattern.matches(subSequence, matchingType, massTolerance);
    }

    /**
     * Returns a boolean indicating whether the protein ends with the given
     * peptide.
     *
     * @param peptideSequence the peptide sequence
     * @param pattern the amino acid pattern
     * @param patternLength the pattern length
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return a boolean indicating whether the protein ends with the given
     * peptide
     */
    public boolean isCTerm(String peptideSequence, AminoAcidPattern pattern, int patternLength, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        String subSequence = sequence.substring(sequence.length() - peptideSequence.length() - 1);
        return pattern.matches(subSequence, matchingType, massTolerance);
    }

    /**
     * Returns true of the peptide is non-enzymatic, i.e., has one or more end
     * points that cannot be caused by the enzyme alone. False means that both
     * the endpoints of the peptides could be caused by the selected enzyme, or
     * that it is a terminal peptide (where one end point is most likely not
     * enzymatic). Note that if a peptide maps to multiple locations in the
     * protein sequence this method returns true if one or more of these
     * peptides are enzymatic, even if not all mappings are enzymatic.
     *
     * @param peptideSequence the peptide sequence to check
     * @param pattern the amino acid pattern
     * @param patternLength the pattern length
     * @param enzyme the enzyme to use
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return true of the peptide is non-enzymatic
     *
     * @throws IOException
     */
    public boolean isEnzymaticPeptide(String peptideSequence, AminoAcidPattern pattern, int patternLength, Enzyme enzyme, AminoAcidPattern.MatchingType matchingType, Double massTolerance) throws IOException {

        // get the surrounding amino acids
        HashMap<Integer, String[]> surroundingAminoAcids = getSurroundingAA(peptideSequence, pattern, patternLength, 1, matchingType, massTolerance);

        String firstAA = peptideSequence.charAt(0) + "";
        String lastAA = peptideSequence.charAt(peptideSequence.length() - 1) + "";

        // iterate the possible extended peptide sequences
        for (int index : surroundingAminoAcids.keySet()) {

            String before = surroundingAminoAcids.get(index)[0];
            String after = surroundingAminoAcids.get(index)[1];

            // @TODO: how to handle semi-specific enzymes??

            if ((enzyme.isCleavageSite(before, firstAA) && enzyme.isCleavageSite(lastAA, after)
                    || (before.length() == 0 && enzyme.isCleavageSite(lastAA, after)
                    || (enzyme.isCleavageSite(before, firstAA) && after.length() == 0)))) { // @TODO: should use the char versions of the enzyme methods
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the amino acids surrounding a peptide in the sequence of the
     * given protein in a map: peptide start index -> (amino acids before, amino
     * acids after).
     *
     * @param peptide the sequence of the peptide of interest
     * @param pattern the amino acid pattern
     * @param patternLength the pattern length
     * @param nAA the number of amino acids to include
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return the amino acids surrounding a peptide in the protein sequence
     *
     * @throws IOException Exception thrown whenever an error occurred while
     * parsing the protein sequence
     */
    public HashMap<Integer, String[]> getSurroundingAA(String peptide, AminoAcidPattern pattern, int patternLength, int nAA, AminoAcidPattern.MatchingType matchingType, Double massTolerance) throws IOException {

        ArrayList<Integer> startIndexes = getPeptideStart(peptide, pattern, patternLength, matchingType, massTolerance);
        HashMap<Integer, String[]> result = new HashMap<Integer, String[]>();

        for (int startIndex : startIndexes) {

            result.put(startIndex, new String[2]);
            String subsequence = "";

            int stringIndex = startIndex - 1;
            for (int aa = stringIndex - nAA; aa < stringIndex; aa++) {
                if (aa >= 0 && aa < sequence.length()) {
                    subsequence += sequence.charAt(aa);
                }
            }

            result.get(startIndex)[0] = subsequence;
            subsequence = "";

            for (int aa = stringIndex + peptide.length(); aa < stringIndex + peptide.length() + nAA; aa++) {
                if (aa >= 0 && aa < sequence.length()) {
                    subsequence += sequence.charAt(aa);
                }
            }

            result.get(startIndex)[1] = subsequence;
        }

        return result;
    }
}
