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
     * Returns the observable amino acids in the sequence when using the given
     * enzymes with the given maximal peptide length.
     *
     * @param enzymes the enzymes to use
     * @param pepMaxLength the max peptide length
     *
     * @return the number of observable amino acids of the sequence
     */
    public int[] getObservableAminoAcids(ArrayList<Enzyme> enzymes, double pepMaxLength) {
        
        int lastCleavage = -1;
        
        int[] observableAas = new int[sequence.length()];
        
        for (int i = 0 ; i < sequence.length() - 1 ; i++) {
            
            char charati = sequence.charAt(i), charatiPlusOne = sequence.charAt(i + 1);
            
            if (enzymes.stream().anyMatch(enzyme -> enzyme.isCleavageSite(charati, charatiPlusOne))) {
                
                if (i - lastCleavage <= pepMaxLength) {
                    
                    for (int k = lastCleavage ; k < i ; k++) {
                        
                        observableAas[k] = 1;
                        
                    }
                }
                
                lastCleavage = i;
                
            }
        }
        
        if (sequence.length() - 1 - lastCleavage <= pepMaxLength) {
            
            for (int k = lastCleavage ; k < sequence.length() ; k++) {
                
                observableAas[k] = 1;
                
            }
        }
        
        return observableAas;
    }

    /**
     * Returns the number of observable amino acids in the sequence.
     *
     * @param enzymes the enzymes to use
     * @param pepMaxLength the max peptide length
     *
     * @return the number of observable amino acids of the sequence
     */
    public int getObservableLength(ArrayList<Enzyme> enzymes, double pepMaxLength) {
        
        int[] observalbeAas = getObservableAminoAcids(enzymes, pepMaxLength);
        
        return Arrays.stream(observalbeAas).sum();
    }

    /**
     * Returns the number of cleavage sites.
     *
     * @param enzymes the enzymes to use
     *
     * @return the number of possible peptides
     */
    public int getNCleavageSites(ArrayList<Enzyme> enzymes) {
        
        int nCleavageSites = 0;
        
        for (int i = 0; i < sequence.length() - 1; i++) {
            
            char charati = sequence.charAt(i), charatiPlusOne = sequence.charAt(i + 1);
            
            if (enzymes.stream().anyMatch(enzyme -> enzyme.isCleavageSite(charati, charatiPlusOne))) {
                
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
     * @return the protein's molecular weight in Da
     */
    public double computeMolecularWeight() {

        double mass = StandardMasses.h2o.mass;

        mass += sequence.chars().mapToDouble(aa -> AminoAcid.getAminoAcid((char) aa).getMonoisotopicMass()).sum();

        return mass;
    }
}
