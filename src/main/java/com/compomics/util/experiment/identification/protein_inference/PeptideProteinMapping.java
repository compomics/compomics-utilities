package com.compomics.util.experiment.identification.protein_inference;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.VariantMatch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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

    /**
     * Returns a map made from the given mappings containing the indexes of the
     * peptides in the protein sequences indexed by peptide sequence and protein
     * accession.
     *
     * @param peptideProteinMappings a list of peptide to protein mappings
     *
     * @return a map of the mapping
     */
    public static HashMap<String, HashMap<String, ArrayList<Integer>>> getPeptideProteinIndexesMap(ArrayList<PeptideProteinMapping> peptideProteinMappings) {
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

    /**
     * Returns a map made from the given mappings containing protein accessions
     * for every peptide sequence.
     *
     * @param peptideProteinMappings a list of peptide to protein mappings
     *
     * @return a map of the mapping
     */
    public static HashMap<String, HashSet<String>> getPeptideProteinMap(ArrayList<PeptideProteinMapping> peptideProteinMappings) {
        HashMap<String, HashSet<String>> result = new HashMap<String, HashSet<String>>(peptideProteinMappings.size());
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            String peptideSequence = peptideProteinMapping.getPeptideSequence();
            HashSet<String> proteins = result.get(peptideSequence);
            if (proteins == null) {
                proteins = new HashSet<String>(1);
                result.put(peptideSequence, proteins);
            }
            String accession = peptideProteinMapping.getProteinAccession();
            proteins.add(accession);
        }
        return result;
    }

    /**
     * Aggregates the given mapping into a list of peptides.
     *
     * @param peptideProteinMappings a list of peptides to protein mappings
     *
     * @return a list of peptides
     */
    public static Collection<Peptide> getPeptides(ArrayList<PeptideProteinMapping> peptideProteinMappings) {
        HashMap<String, Peptide> peptidesMap = new HashMap<String, Peptide>(peptideProteinMappings.size());
        HashMap<String, HashSet<String>> proteinsMap = new HashMap<String, HashSet<String>>(peptideProteinMappings.size());
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            Peptide tempPeptide = new Peptide(peptideProteinMapping.getPeptideSequence(), peptideProteinMapping.getModificationMatches());
            String peptideKey = tempPeptide.getKey();
            Peptide peptide = peptidesMap.get(peptideKey);
            if (peptide == null) {
                tempPeptide.addVariantMatches(peptideProteinMapping.getVariantMatches());
                peptidesMap.put(peptideKey, tempPeptide);
                HashSet<String> proteins = new HashSet<String>(1);
                proteins.add(peptideProteinMapping.getProteinAccession());
                proteinsMap.put(peptideKey, proteins);
            } else {
                peptide.addVariantMatches(peptideProteinMapping.getVariantMatches());
                HashSet<String> proteins = proteinsMap.get(peptideKey);
                proteins.add(peptideProteinMapping.getProteinAccession());
            }
        }
        for (String peptideKey : peptidesMap.keySet()) {
            HashSet<String> proteins = proteinsMap.get(peptideKey);
            ArrayList<String> sortedProteinList = new ArrayList<String>(proteins);
            Collections.sort(sortedProteinList);
            Peptide peptide = peptidesMap.get(peptideKey);
            peptide.setParentProteins(sortedProteinList);
        }
        return peptidesMap.values();
    }
}
