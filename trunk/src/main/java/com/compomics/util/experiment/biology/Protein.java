package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;

/**
 * This class models a protein.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:56:22 AM
 */
public class Protein extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 1987224639519365761L;
    /**
     * The protein accession
     */
    private String accession;
    /**
     * Boolean indicating if the protein is not existing (decoy protein for instance)
     */
    private boolean decoy;
    /**
     * The protein description
     */
    private String description;
    /**
     * The protein sequence
     */
    private String sequence;

    /**
     * Constructor for a protein
     */
    public Protein() {
    }

    /**
     * Simplistic constructor for a protein (typically used when loading identification files)
     *
     * @param accession The protein accession
     * @param isDecoy       boolean indicating whether the protein is decoy
     */
    public Protein(String accession, boolean isDecoy) {
        this.accession = accession;
        this.decoy = isDecoy;
    }

    /**
     * constructor for a protein
     *
     * @param accession     The protein accession
     * @param description   The protein description
     * @param sequence      The protein sequence
     * @param isDecoy       boolean indicating whether the protein is decoy
     */
    public Protein(String accession, String description, String sequence, boolean isDecoy) {
        this.accession = accession;
        this.description = description;
        this.sequence = sequence;
        this.decoy = isDecoy;
    }

    /**
     * indicates if the protein is factice (from a decoy database for instance)
     * @return a boolean indicating if the protein is factice
     */
    public boolean isDecoy() {
        return decoy;
    }

    /**
     * Getter for the protein accession
     *
     * @return the protein accession
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Getter for the protein description
     *
     * @return the protein description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for the protein sequence
     *
     * @return the protein sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * A method to compare proteins. For now accession based.
     * 
     * @param anotherProtein    an other protein
     * @return a boolean indicating if the proteins are identical
     */
    public boolean isSameAs(Protein anotherProtein) {
        return accession.equals(anotherProtein.getAccession());
    }

    /**
     * Returns the key for protein indexing. For now the protein accession.
     * @return  the key for protein indexing.
     */
    public String getProteinKey() {
        return accession;
    }

    /**
     * Returns the number of possible peptides (not accounting PTMs nor missed cleavages) with the selected enzyme
     * @param enzyme    The selected enzyme
     * @return  the number of possible peptides
     */
    public int getNPossiblePeptides(Enzyme enzyme) {

        int nCleavages = 0;
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
}
