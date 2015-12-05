package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Simple IdfileReader for Pepxml files.
 *
 * @author Marc Vaudel
 */
public class PepxmlIdfileReader implements IdfileReader {

    /**
     * List of the spectrum matches in the file.
     */
    private LinkedList<SpectrumMatch> spectrumMatches = null;
    /**
     * The name of the search engine which was used to create the file.
     */
    private String searchEngine = null;
    /**
     * The version of the search engine which was used to create the file.
     */
    private String searchEngineVersion = null;
    /**
     * The file to parse.
     */
    private File idFile;
    /**
     * The name of the spectrum file.
     */
    private String inputFileName;
    /**
     * The spectrum factory used to retrieve spectrum titles.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
    /**
     * The sequence matching preferences.
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences = null;

    /**
     * Blank constructor for instantiation purposes.
     */
    public PepxmlIdfileReader() {

    }

    /**
     * Constructor.
     *
     * @param idFile the file to parse
     */
    public PepxmlIdfileReader(File idFile) {
        this.idFile = idFile;
    }

    /**
     * Parses the identification file.
     *
     * @param waitingHandler waiting handler returning information about the
     * progress and allowing canceling the parsing.
     * @param expandAaCombinations if true the combinations of amino acids will
     * be expanded
     * @param overwriteExtension if true, the extension of the input file will
     * be overwritten to mgf
     *
     * @throws XmlPullParserException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private void parseFile(WaitingHandler waitingHandler, boolean expandAaCombinations, boolean overwriteExtension)
            throws XmlPullParserException, FileNotFoundException, IOException, SQLException, ClassNotFoundException, InterruptedException {

        int minimalPeptideSize;
        try {
            minimalPeptideSize = SequenceFactory.getInstance().getDefaultProteinTree().getInitialTagSize();
        } catch (Exception e) {
            minimalPeptideSize = 3;
        }

        // Create the pull parser.
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        // Create a reader for the input file.
        BufferedReader br = new BufferedReader(new FileReader(idFile));

        try {
            // Set the XML Pull Parser to read from this reader.
            parser.setInput(br);
            // Start the parsing.
            int type;
            boolean hasMatch = false;

            HashMap<String, SpectrumMatch> spectrumMatchesMap = new HashMap<String, SpectrumMatch>();
            spectrumMatches = new LinkedList<SpectrumMatch>();
            SpectrumMatch currentMatch = null;
            Integer currentCharge = null;

            // Go through the whole document.
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (type == XmlPullParser.START_TAG && tagName.equals("msms_run_summary")) {
                    parseRunSummary(parser, overwriteExtension);
                    if (waitingHandler != null && spectrumFactory.fileLoaded(inputFileName)) {
                        waitingHandler.setMaxSecondaryProgressCounter(spectrumFactory.getNSpectra(inputFileName));
                        waitingHandler.setSecondaryProgressCounter(0);
                    }
                }
                if (type == XmlPullParser.START_TAG && tagName.equals("search_summary")) {
                    parseSearchSummary(parser);
                }
                if (type == XmlPullParser.START_TAG && tagName.equals("spectrum_query")) {
                    currentMatch = parseSpectrumQuery(parser);
                    SpectrumMatch previousMatch = spectrumMatchesMap.get(currentMatch.getKey());
                    if (previousMatch != null) {
                        currentMatch = previousMatch;
                    }
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attributeName = parser.getAttributeName(i);
                        if (attributeName.equals("assumed_charge")) {
                            String value = parser.getAttributeValue(i);
                            try {
                                currentCharge = new Integer(value.trim());
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Charge " + value + " could not be parsed. Integer expected.");
                            }
                        }
                    }
                }
                if (type == XmlPullParser.START_TAG && tagName.equals("search_hit")) {
                    if (currentMatch == null) {
                        throw new IllegalArgumentException("No spectrum match when parsing search hit.");
                    }
                    if (currentCharge == null) {
                        throw new IllegalArgumentException("No charge found when parsing search hit of spectrum " + currentMatch.getKey() + ".");
                    }
                    PeptideAssumption peptideAssumption = parseSearchHit(parser, currentCharge);
                    Peptide peptide = peptideAssumption.getPeptide();
                    String peptideSequence = peptide.getSequence();
                    if (peptideSequence.length() >= minimalPeptideSize) {
                        hasMatch = true;
                        boolean found = false;
                        if (currentMatch.getAllAssumptions() != null) {
                            for (SpectrumIdentificationAssumption tempAssumption : currentMatch.getAllAssumptions()) {
                                PeptideAssumption tempPeptideAssumption = (PeptideAssumption) tempAssumption;
                                Peptide tempPeptide = tempPeptideAssumption.getPeptide();
                                if (peptide.getSequence().equals(tempPeptide.getSequence())) {
                                    boolean sameModifications = peptide.getNModifications() == tempPeptide.getNModifications();
                                    if (sameModifications && peptide.isModified()) {
                                        for (ModificationMatch originalMatch : peptide.getModificationMatches()) {
                                            boolean ptmFound = false;
                                            for (ModificationMatch otherMatch : tempPeptide.getModificationMatches()) {
                                                if (originalMatch.getTheoreticPtm().equals(otherMatch.getTheoreticPtm()) && originalMatch.getModificationSite() == otherMatch.getModificationSite()) {
                                                    ptmFound = true;
                                                    break;
                                                }
                                            }
                                            if (!ptmFound) {
                                                sameModifications = false;
                                                break;
                                            }
                                        }
                                    }
                                    if (sameModifications) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!found) {

                            Advocate advocate = Advocate.getAdvocate(searchEngine);
                            if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideSequence)) {
                                ArrayList<ModificationMatch> previousModificationMatches = peptide.getModificationMatches(),
                                        newModificationMatches = null;
                                if (previousModificationMatches != null) {
                                    newModificationMatches = new ArrayList<ModificationMatch>(previousModificationMatches.size());
                                }
                                for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {
                                    Peptide newPeptide = new Peptide(expandedSequence.toString(), newModificationMatches);
                                    if (previousModificationMatches != null) {
                                        for (ModificationMatch modificationMatch : previousModificationMatches) {
                                            newPeptide.addModificationMatch(new ModificationMatch(modificationMatch.getTheoreticPtm(),
                                                    modificationMatch.isVariable(), modificationMatch.getModificationSite()));
                                        }
                                    }
                                    PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(),
                                            peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(),
                                            peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                                    currentMatch.addHit(advocate.getIndex(), newAssumption, false);
                                }
                            } else {
                                currentMatch.addHit(advocate.getIndex(), peptideAssumption, false);
                            }
                        }
                    }
                }
                if (type == XmlPullParser.END_TAG && tagName.equals("spectrum_query")) {
                    if (hasMatch) {
                        String key = currentMatch.getKey();
                        if (!spectrumMatchesMap.containsKey(key)) {
                            spectrumMatchesMap.put(key, currentMatch);
                            spectrumMatches.add(currentMatch);
                        }
                        hasMatch = false;
                        currentMatch = null;
                        currentCharge = null;
                    }
                    if (waitingHandler != null && spectrumFactory.fileLoaded(inputFileName)) {
                        waitingHandler.increaseSecondaryProgressCounter();
                    }
                }
            }

            spectrumMatchesMap.clear();

        } finally {
            br.close();
        }
    }

    /**
     * Parses a search hit.
     *
     * @param parser the XML parser
     * @param charge the charge of the hit
     *
     * @return the peptide assumption in the search hit
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private PeptideAssumption parseSearchHit(XmlPullParser parser, Integer charge) throws XmlPullParserException, IOException {

        Integer rank = null;
        String sequence = null;
        ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>();
        Double score = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String name = parser.getAttributeName(i);
            if (name.equals("hit_rank")) {
                String value = parser.getAttributeValue(i);
                try {
                    rank = new Integer(value.trim());
                } catch (Exception e) {
                    throw new IllegalArgumentException("An error occurred while parsing rank " + value + ". Integer expected.");
                }
            } else if (name.equals("peptide")) {
                sequence = parser.getAttributeValue(i).trim();
            }
        }

        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG) {
        }

        String tagName = parser.getName();
        if (tagName.equals("modification_info")) {

            // the peptide is modified, take the variable modifications sites from the modified sequence and the mass from the modified amino acid masses
            ArrayList<Integer> variableModificationSites = new ArrayList<Integer>();

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equals("modified_peptide")) {
                    String modifiedSequence = parser.getAttributeValue(i).trim();
                    int aa = 0;
                    boolean modification = false;
                    for (char character : modifiedSequence.toCharArray()) {
                        if (character == '[') {
                            variableModificationSites.add(aa);
                            modification = true;
                        } else if (character == ']') {
                            modification = false;
                        }
                        if (!modification && character != ']') {
                            aa++;
                        }
                    }
                } else if (attributeName.equals("mod_nterm_mass")
                        || attributeName.equals("mod_cterm_mass")) {

                    String value = parser.getAttributeValue(i).trim();
                    Double modifiedAaMass = null;
                    try {
                        modifiedAaMass = new Double(value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("An error occurred while parsing modification mass " + value + ". Number expected.");
                    }

                    int site;
                    if (attributeName.equals("mod_nterm_mass")) {
                        site = 1;
                    } else { // c-term
                        site = sequence.length();
                    }

                    char aa = sequence.charAt(site - 1);
                    AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                    double modificationMass = modifiedAaMass - aminoAcid.getMonoisotopicMass();
                    modificationMass = Util.roundDouble(modificationMass, 2);
                    String tempModificationName = modificationMass + "@" + aa;
                    ModificationMatch modificationMatch = new ModificationMatch(tempModificationName, true, site);
                    modificationMatches.add(modificationMatch);
                }
            }

            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();
                if (tagName != null) {
                    if (tagName.equals("mod_aminoacid_mass")) {
                        Integer site = null;
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String attributeName = parser.getAttributeName(i);
                            if (attributeName.equals("position")) {
                                String value = parser.getAttributeValue(i);
                                try {
                                    site = new Integer(value);
                                } catch (Exception e) {
                                    throw new IllegalArgumentException("An error occurred while parsing modification position " + value + ". Integer expected.");
                                }
                            }
                        }
                        if (site != null && variableModificationSites.contains(site)) {
                            Double modifiedAaMass = null;
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                String attributeName = parser.getAttributeName(i);
                                if (attributeName.equals("mass")) {
                                    String value = parser.getAttributeValue(i);
                                    try {
                                        modifiedAaMass = new Double(value);
                                    } catch (Exception e) {
                                        throw new IllegalArgumentException("An error occurred while parsing modification mass " + value + ". Number expected.");
                                    }
                                }
                            }
                            if (modifiedAaMass != null) {
                                char aa = sequence.charAt(site - 1);
                                AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                                double modificationMass = modifiedAaMass - aminoAcid.getMonoisotopicMass();
                                modificationMass = Util.roundDouble(modificationMass, 2);
                                String tempModificationName = modificationMass + "@" + aa;
                                ModificationMatch modificationMatch = new ModificationMatch(tempModificationName, true, site);
                                modificationMatches.add(modificationMatch);
                            }
                        }
                    } else if (type == XmlPullParser.END_TAG && parser.getName().equals("modification_info")) {
                        while ((type = parser.next()) != XmlPullParser.START_TAG) {
                        }
                        break;
                    }
                }
            }
        }

        while (type != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            if (tagName != null) {
                if (type == XmlPullParser.START_TAG && parser.getName().equals("search_score")) {
                    String name = null;
                    String value = null;
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attributeName = parser.getAttributeName(i);
                        if (attributeName.equals("name")) {
                            name = parser.getAttributeValue(i);
                        } else if (attributeName.equals("value")) {
                            value = parser.getAttributeValue(i);
                        }
                    }
                    if (name != null && name.equals("expect") && value != null) {
                        try {
                            score = new Double(value);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Impossible to parse expectation value " + value + ". Number expected.");
                        }
                    }
                } else if (type == XmlPullParser.END_TAG && tagName.equals("search_hit")) {
                    break;
                }
            }
            type = parser.next();
        }

        Peptide peptide = new Peptide(sequence, modificationMatches);
        Advocate advocate = Advocate.getAdvocate(searchEngine);
        return new PeptideAssumption(peptide, rank, advocate.getIndex(), new Charge(Charge.PLUS, charge), score, idFile.getName());
    }

    /**
     * Parses a spectrum query.
     *
     * @param parser the XML parser
     *
     * @return the spectrum match in this spectrum query
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private SpectrumMatch parseSpectrumQuery(XmlPullParser parser) throws XmlPullParserException, IOException {

        Integer scanNumber = null;
        String spectrumId = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String name = parser.getAttributeName(i);
            if (name.equals("spectrum")) {
                spectrumId = parser.getAttributeValue(i);
            } else if (name.equals("start_scan")) {
                String value = parser.getAttributeValue(i);
                try {
                    scanNumber = new Integer(value.trim());
                } catch (Exception e) {
                    throw new IllegalArgumentException("An error occurred while parsing start_scan " + value + ". Integer expected.");
                }
            }
        }

        if (scanNumber == null) {
            throw new IllegalArgumentException("No start scan found for spectrum " + spectrumId + ".");
        }

        String spectrumTitle = scanNumber + "";
        if (spectrumFactory.fileLoaded(inputFileName)) {
            spectrumTitle = spectrumFactory.getSpectrumTitle(inputFileName, scanNumber);
        }
//        String spectrumTitle = scanNumber + "";
//        if (spectrumFactory.fileLoaded(inputFileName)) {
//            if (spectrumFactory.spectrumLoaded(inputFileName, spectrumId)) {
//                spectrumTitle = spectrumId;
//            } else {
//                spectrumTitle = spectrumFactory.getSpectrumTitle(inputFileName, scanNumber);
//            }
//        }

        String spectrumKey = Spectrum.getSpectrumKey(inputFileName, spectrumTitle);
        SpectrumMatch spectrumMatch = new SpectrumMatch(spectrumKey);
        spectrumMatch.setSpectrumNumber(scanNumber);

        return spectrumMatch;
    }

    /**
     * Parses the run summary.
     *
     * @param parser the XML parser
     * @param overwriteExtension if true, the extension of the input file will
     * be overwritten to mgf
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parseRunSummary(XmlPullParser parser, boolean overwriteExtension) throws XmlPullParserException, IOException {

        // Something like  <msms_run_summary base_name="D:\path\filename" raw_data="extention"> is expected 
        String path = "";
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String name = parser.getAttributeName(i);
            if (name.equals("base_name")) {
                path += parser.getAttributeValue(i);
            } else if (!overwriteExtension && name.equals("raw_data")) {
                path += parser.getAttributeValue(i);
            }
        }

        if (overwriteExtension) {
            path += ".mgf";
        }

        File spectrumFile = new File(path);
        inputFileName = Util.getFileName(spectrumFile);
    }

    /**
     * Parses the search summary.
     *
     * @param parser the XML parser
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parseSearchSummary(XmlPullParser parser) throws XmlPullParserException, IOException {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String name = parser.getAttributeName(i);
            if (name.equals("search_engine")) {
                searchEngine = parser.getAttributeValue(i);
            } else if (name.equals("search_engine_version")) {
                searchEngineVersion = parser.getAttributeValue(i);
            }
        }
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        ArrayList<String> versions = new ArrayList<String>(1);
        versions.add(searchEngineVersion);
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>(1);
        result.put(searchEngine, versions);
        return result;
    }

    @Override
    public String getExtension() {
        return ".pep.xml";
    }

    @Override
    public void close() throws IOException {
        // nothing to do here
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException, XmlPullParserException {
        return getAllSpectrumMatches(waitingHandler, searchParameters, null, true);
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters,
            SequenceMatchingPreferences sequenceMatchingPreferences, boolean expandAaCombinations) throws IOException, IllegalArgumentException,
            SQLException, ClassNotFoundException, InterruptedException, JAXBException, XmlPullParserException {
        if (spectrumMatches == null) {
            this.sequenceMatchingPreferences = sequenceMatchingPreferences;
            parseFile(waitingHandler, expandAaCombinations, true);
        }
        return spectrumMatches;
    }

    @Override
    public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap() {
        return new HashMap<String, LinkedList<SpectrumMatch>>(0);
    }

    @Override
    public void clearTagsMap() {
        // Nothing to do here
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }
}
