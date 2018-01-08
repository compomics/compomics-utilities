package com.compomics.util.experiment.identification.protein_inference;

import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.util.ArrayList;

/**
 * Interface for a class mapping peptides to a protein.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 */
public interface FastaMapper {

    /**
     * Returns the protein mapping in the FASTA file loaded in the sequence
     * factory for the given peptide sequence in a map: peptide sequence found
     * in the FASTA file | protein accession | list of indexes of the peptide
     * sequence on the protein sequence. 0 is the first amino acid.
     *
     * @param peptideSequence the peptide sequence
     * @param proteinInferencePreferences the sequence matching preferences
     *
     * @return the peptide to protein mapping: peptide sequence &gt; protein
     * accession &gt; index in the protein An empty map if not
     */
    public ArrayList<PeptideProteinMapping> getProteinMapping(String peptideSequence, SequenceMatchingParameters proteinInferencePreferences);

    /**
     * Returns the protein mappings for the given peptide sequence. Peptide
     * sequence | Protein accession | Index in the protein. An empty map if not
     * found.
     *
     * @param tag the tag to look for in the tree. Must contain a consecutive
     * amino acid sequence of longer or equal size than the initialTagSize of
     * the tree
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the protein mapping for the given peptide sequence
     */
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, SequenceMatchingParameters sequenceMatchingPreferences);
}
