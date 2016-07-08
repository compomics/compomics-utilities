package com.compomics.util.experiment.identification.protein_inference;

import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.VariantMatch;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class used to model the mapping of a peptide to a protein sequence.
 *
 * @author Marc Vaudel
 */
public class PeptideProteinMapping {

    /**
     * Accession of the protein.
     */
    private String proteinAccession;
    /**
     * The peptide sequence.
     */
    private String peptideSequence;
    /**
     * Index on the protein sequence, 0 is the first amino acid.
     */
    private int index;
    /**
     * Eventual modifications.
     */
    private ArrayList<ModificationMatch> modificationMatches = null;
    /**
     * Eventual variants.
     */
    private ArrayList<VariantMatch> variantMatches = null;

    /**
     * Constructor.
     *
     * @param proteinAccession the accession of the protein
     * @param peptideSequence the peptide sequence
     * @param index the index on the protein
     */
    public PeptideProteinMapping(String proteinAccession, String peptideSequence, int index) {
        this.proteinAccession = proteinAccession;
        this.peptideSequence = peptideSequence;
        this.index = index;
    }

    /**
     * Constructor.
     *
     * @param proteinAccession the accession of the protein
     * @param peptideSequence the peptide sequence
     * @param index the index on the protein
     * @param modificationMatches modification matches
     */
    public PeptideProteinMapping(String proteinAccession, String peptideSequence, int index, ArrayList<ModificationMatch> modificationMatches) {
        this.proteinAccession = proteinAccession;
        this.peptideSequence = peptideSequence;
        this.index = index;
        this.modificationMatches = modificationMatches;
    }

    /**
     * Constructor.
     *
     * @param proteinAccession the accession of the protein
     * @param peptideSequence the peptide sequence
     * @param index the index on the protein
     * @param modificationMatches eventual modification matches
     * @param variantMatches eventual variant matches
     */
    public PeptideProteinMapping(String proteinAccession, String peptideSequence, int index, ArrayList<ModificationMatch> modificationMatches, ArrayList<VariantMatch> variantMatches) {
        this.proteinAccession = proteinAccession;
        this.peptideSequence = peptideSequence;
        this.index = index;
        this.modificationMatches = modificationMatches;
        this.variantMatches = variantMatches;
    }

    /**
     * Returns the accession of the protein.
     *
     * @return the accession of the protein
     */
    public String getProteinAccession() {
        return proteinAccession;
    }

    /**
     * Returns the peptide sequence.
     *
     * @return the peptide sequence
     */
    public String getPeptideSequence() {
        return peptideSequence;
    }

    /**
     * Returns the index on the protein.
     *
     * @return the index on the protein
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns eventual modifications found.
     *
     * @return eventual modifications found
     */
    public ArrayList<ModificationMatch> getModificationMatches() {
        return modificationMatches;
    }

    /**
     * Returns eventual variants found.
     *
     * @return eventual variants found
     */
    public ArrayList<VariantMatch> getVariantMatches() {
        return variantMatches;
    }

    public static HashMap<String, HashMap<String, ArrayList<Integer>>> getPeptideProteinMap(ArrayList<PeptideProteinMapping> peptideProteinMappings) {
        HashMap<String, HashMap<String, ArrayList<Integer>>> result = new HashMap<String, HashMap<String, ArrayList<Integer>>>(2);
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            String peptideSequence = peptideProteinMapping.getPeptideSequence();
            HashMap<String, ArrayList<Integer>> proteins = result.get(peptideSequence);
            if (proteins == null) {
                proteins = new HashMap<String, ArrayList<Integer>>(2);
                result.put(peptideSequence, proteins);
            }
            String accession = peptideProteinMapping.getProteinAccession();
            ArrayList<Integer> indexes = proteins.get(accession);
            if (indexes == null) {
                indexes = new ArrayList<Integer>(2);
                proteins.put(accession, indexes);
            }
            indexes.add(peptideProteinMapping.getIndex());
        }
        return result;
    }

}
