package com.compomics.util.experiment.biology.proteins;

import com.compomics.util.experiment.personalization.ExperimentObject;

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
     * The protein sequence.
     */
    private String sequence;

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
     */
    public Protein(String accession) {
        
        this.accession = accession;
        
    }

    /**
     * Constructor for a protein.
     *
     * @param accession The protein accession
     * @param sequence The protein sequence
     */
    public Protein(String accession, String sequence) {
        
        this.accession = accession;
        this.sequence = sequence;
        
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
     * Getter for the protein sequence.
     *
     * @return the protein sequence
     */
    public String getSequence() {
        
        return sequence;
        
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
}
