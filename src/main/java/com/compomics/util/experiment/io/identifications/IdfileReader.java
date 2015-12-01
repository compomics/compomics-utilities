package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * This interface will retrieve spectrum matches from any identification file.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public interface IdfileReader {

    /**
     * Returns the names and versions of the software used to generate the
     * identification file in a map, e.g., Mascot &gt; (2.2 and 2.3) and X!Tandem
     * &gt; Sledgehammer (2013.09.01.1). Null if not known.
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
     * @throws IOException if an IOException occurs
     */
    public void close() throws IOException;

    /**
     * Retrieves all the identifications from an identification file as a list
     * of spectrum matches It is very important to close the file reader after
     * creation. Using this method secondary maps are not filled.
     *
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The secondary progress methods will be called.
     * @param searchParameters the search parameters
     *
     * @return a list of spectrum matches
     *
     * @throws IOException if an IOException occurs
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     * @throws SQLException if an SQLException occurs
     * @throws ClassNotFoundException if an\ ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws JAXBException if a JAXBException occurs
     * @throws XmlPullParserException if an XmlPullParserException occurs
     */
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException, XmlPullParserException;

    /**
     * Retrieves all the identifications from an identification file as a list
     * of spectrum matches It is very important to close the file reader after
     * creation. Secondary peptide and tag maps are filled according to the file
     * content and the sequence matching preferences. If the sequence matching
     * preferences are null, the maps are not filled.
     *
     * @param waitingHandler a waiting handler displaying the progress (can be
     * null). The secondary progress methods will be called.
     * @param searchParameters the search parameters
     * @param sequenceMatchingPreferences the sequence matching preferences to
     * use for the creation of the secondary maps
     * @param expandAaCombinations if true, a peptide assumption (not
     * implemented for tag assumptions) will be created for all possible amino
     * acid combination for peptide sequences containing an ambiguity like an X
     *
     * @return a list of spectrum matches
     *
     * @throws IOException if an IOException occurs
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     * @throws SQLException if an SQLException occurs
     * @throws ClassNotFoundException if an\ ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws JAXBException if a JAXBException occurs
     * @throws XmlPullParserException if an XmlPullParserException occurs
     */
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters, SequenceMatchingPreferences sequenceMatchingPreferences, boolean expandAaCombinations)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException, XmlPullParserException, XmlPullParserException;

    /**
     * Returns a boolean indicating whether the file contains de novo results as tags.
     * 
     * @return a boolean indicating whether the file contains de novo results as tags
     */
    public boolean hasDeNovoTags();
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
    public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap();

    /**
     * Clears the tags map.
     */
    public void clearTagsMap();
}
