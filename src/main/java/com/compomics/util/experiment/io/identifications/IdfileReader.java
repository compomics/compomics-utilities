package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;

/**
 * This interface will retrieve spectrum matches from any identification file.
 *
 * @author Marc Vaudel
 */
public interface IdfileReader {

    /**
     * Returns the names and versions of the software used to generate the
     * identification file in a map, e.g., Mascot -> (2.2 and 2.3) and X!Tandem
     * -> Sledgehammer (2013.09.01.1). Null if not known.
     *
     * @return the version of the software used to generate the identification
     * file, null if not known
     */
    public HashMap<String, ArrayList<String>> getSoftwareVersions();

    /**
     * Returns the extension of the file for which this IdfileReader can be
     * used.
     *
     * @return String with the extension (taken to make up the end of the
     * filename) of the file that this IdfileReader can read.
     */
    public String getExtension();

    /**
     * Closes the file reader.
     *
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Retrieves all the identifications from an identification file as a list
     * of spectrum matches It is very important to close the file reader after
     * creation. Using this method secondary maps are not filled
     *
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The secondary progress methods will be called.
     *
     * @return a list of spectrum matches
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws JAXBException
     */
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException;

    /**
     * Retrieves all the identifications from an identification file as a list
     * of spectrum matches It is very important to close the file reader after
     * creation. Secondary peptide and tag maps are filled according to the file
     * content and the sequence matching preferences. If the sequence matching
     * preferences are null, the maps are not filled.
     *
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The secondary progress methods will be called.
     *
     * @param sequenceMatchingPreferences the sequence matching preferences to
     * use for the creation of the secondary maps
     *
     * @return a list of spectrum matches
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws JAXBException
     */
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SequenceMatchingPreferences sequenceMatchingPreferences) throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException;

    /**
     * Returns a map of all the peptides found in this file in a map indexed by
     * the beginning of the peptide sequence. The size of the subsequence is the
     * one of the initial size the protein tree in the sequence factory. The
     * subsequence is unique according to the given sequence matching
     * preferences.
     *
     * @return a map of all the peptides found in this file in a map indexed by
     * the beginning of the peptide sequence
     */
    public HashMap<String, LinkedList<Peptide>> getPeptidesMap();

    /**
     * Returns a map of all simple tags found in this file indexed by the
     * beginning of the amino acid sequence. A simple tag is a triplet
     * consisting of a mass gap, an amino acid sequence and a mass gap. The size
     * of the subsequence is the one of the initial size the protein tree in the
     * sequence factory. The subsequence is unique according to the given
     * sequence matching preferences.
     *
     * @return a map of all simple tags found in this file indexed by the
     * beginning of the amino acid sequence
     */
    public HashMap<String, LinkedList<SpectrumMatch>> getSimpleTagsMap();

    /**
     * Returns a map of all tags found in this file indexed by the beginning of
     * the longest amino acid sequence. The size of the subsequence is the one
     * of the initial size the protein tree in the sequence factory. The
     * subsequence is unique according to the given sequence matching
     * preferences.
     *
     * @return a map of all tags found in this file indexed by the beginning of
     * the amino acid sequence
     */
    public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap();
}
