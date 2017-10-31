package com.compomics.util.experiment.biology.proteins;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.mass_spectrometry.utils.StandardMasses;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.Arrays;

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

    /**
     * Returns the protein's molecular weight. (Note that when using a
     * SequenceFactory it is recommended to use the SequenceFactory's
     * computeMolecularWeight method instead, as that method stored the computed
     * molecular weights instead of recalculating them every time.)
     *
     * @return the protein's molecular weight in Da
     */
    public double computeMolecularWeight() {

        double mass = StandardMasses.h2o.mass;

        mass += sequence.chars().mapToDouble(aa -> AminoAcid.getAminoAcid((char) aa).getMonoisotopicMass()).sum();

        return mass;
    }
}
