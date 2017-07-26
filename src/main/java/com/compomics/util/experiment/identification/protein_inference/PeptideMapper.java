package com.compomics.util.experiment.identification.protein_inference;

import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Interface for a class mapping peptides to a protein.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 */
public interface PeptideMapper {

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
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database
     */
    public ArrayList<PeptideProteinMapping> getProteinMapping(String peptideSequence, SequenceMatchingPreferences proteinInferencePreferences)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException;

    /**
     * Returns the protein mappings for the given peptide sequence. Peptide
     * sequence | Protein accession | Index in the protein. An empty map if not
     * found.
     *
     * @param tag the tag to look for in the tree. Must contain a consecutive
     * amino acid sequence of longer or equal size than the initialTagSize of
     * the tree
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param massTolerance the MS2 m/z tolerance
     *
     * @return the protein mapping for the given peptide sequence
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException;

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
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, SequenceMatchingPreferences sequenceMatchingPreferences) throws IOException, InterruptedException, ClassNotFoundException, SQLException;

    /**
     * Empties all caches.
     */
    public void emptyCache();

    /**
     * Closes all connections to files, tries to delete corrupted and deprecated
     * indexes.
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with a database.
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    public void close() throws IOException, SQLException, InterruptedException;
}
