package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
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
import java.util.List;
import javax.xml.bind.JAXBException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import uk.ac.ebi.jmzidml.model.mzidml.AnalysisData;
import uk.ac.ebi.jmzidml.model.mzidml.AnalysisSoftware;
import uk.ac.ebi.jmzidml.model.mzidml.AnalysisSoftwareList;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.DataCollection;
import uk.ac.ebi.jmzidml.model.mzidml.Modification;
import uk.ac.ebi.jmzidml.model.mzidml.ModificationParams;
import uk.ac.ebi.jmzidml.model.mzidml.Param;
import uk.ac.ebi.jmzidml.model.mzidml.SearchModification;
import uk.ac.ebi.jmzidml.model.mzidml.SpecificityRules;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationList;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationProtocol;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;

/**
 * This IdfileReader reads identifications from an mzIdentML result file.
 *
 * @author Harald Barsnes
 */
public class MzIdentMLIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * List of software used to create this file according to the file.
     */
    private HashMap<String, ArrayList<String>> tempSoftwareVersions = new HashMap<String, ArrayList<String>>();
    /**
     * The list of software according to the scores found.
     */
    private HashMap<String, ArrayList<String>> softwareVersions = new HashMap<String, ArrayList<String>>();
    /**
     * The mzIdentML file.
     */
    private File mzIdentMLFile;
    /**
     * The name of the mzIdentML file.
     */
    private String mzIdentMLFileName;
    /**
     * The mzIdentML unmarshaller.
     */
    private MzIdentMLUnmarshaller unmarshaller;
    /**
     * The names of the fixed modifications.
     */
    private ArrayList<SearchModification> fixedModifications;
    /**
     * A map of the peptides found in this file.
     */
    private HashMap<String, LinkedList<Peptide>> peptideMap;
    /**
     * The length of the keys of the peptide map.
     */
    private int peptideMapKeyLength;
    /**
     * A temporary peptide map used by the custom parser only. Key: peptide
     * id/ref, element: the peptide object.
     */
    private HashMap<String, PeptideCustom> tempPeptideMap;
    /**
     * A map of the spectrum file names. Key: spectrum id/ref, element: spectrum
     * file name.
     */
    private HashMap<String, String> spectrumFileNameMap;
    /**
     * The list of fixed modifications extracted by the custom parser.
     */
    private ArrayList<SearchModificationCustom> fixedModificationsCustomParser;
    /**
     * The sequence matching parameters.
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences;
    /**
     * Set if the amino acid combinations are to be expanded. For example
     * replacing X's.
     */
    private boolean expandAaCombinations;
    /**
     * Set if the custom parser are to be used. If false, the jmzidentml parser
     * is used.
     */
    private boolean useCustomParser = true;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public MzIdentMLIdfileReader() {
    }

    /**
     * Constructor for an mzIdentML result file reader.
     *
     * @param mzIdentMLFile the mzIdentML file
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public MzIdentMLIdfileReader(File mzIdentMLFile) throws FileNotFoundException, IOException {
        this(mzIdentMLFile, null);
    }

    /**
     * Constructor for an mzIdentML result file reader.
     *
     * @param mzIdentMLFile the mzIdentML file
     * @param waitingHandler the waiting handler
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public MzIdentMLIdfileReader(File mzIdentMLFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        this.mzIdentMLFile = mzIdentMLFile;
        mzIdentMLFileName = Util.getFileName(mzIdentMLFile);

        if (!useCustomParser) {

            if (mzIdentMLFile.length() < 10485760) {
                unmarshaller = new MzIdentMLUnmarshaller(mzIdentMLFile, true);
            } else {
                unmarshaller = new MzIdentMLUnmarshaller(mzIdentMLFile);
            }

            // get the software versions
            AnalysisSoftwareList analysisSoftwareList = unmarshaller.unmarshal(AnalysisSoftwareList.class);

            for (AnalysisSoftware software : analysisSoftwareList.getAnalysisSoftware()) {
                Param softwareNameObject = software.getSoftwareName();

                String name = softwareNameObject.getCvParam().getName();
                if (name == null) {
                    name = softwareNameObject.getUserParam().getName();
                }
                String version = software.getVersion();
                if (name != null && version != null) {
                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                    if (versions == null) {
                        versions = new ArrayList<String>();
                        versions.add(version);
                        tempSoftwareVersions.put(name, versions);
                    } else if (!versions.contains(version)) {
                        versions.add(version);
                    }
                }
            }

            softwareVersions.putAll(tempSoftwareVersions);

            // get the list of fixed modifications
            fixedModifications = new ArrayList<SearchModification>();
            SpectrumIdentificationProtocol spectrumIdentificationProtocol = unmarshaller.unmarshal(SpectrumIdentificationProtocol.class);
            ModificationParams modifications = spectrumIdentificationProtocol.getModificationParams();
            if (modifications != null) {
                for (SearchModification tempMod : modifications.getSearchModification()) {
                    if (tempMod.isFixedMod()) {
                        fixedModifications.add(tempMod);
                    }
                }
            }
        }
    }

    @Override
    public String getExtension() {
        return ".mzid";
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
        return getAllSpectrumMatches(waitingHandler, searchParameters, null, true);
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters,
            SequenceMatchingPreferences sequenceMatchingPreferences, boolean expandAaCombinations)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        this.sequenceMatchingPreferences = sequenceMatchingPreferences;
        this.expandAaCombinations = expandAaCombinations;

        if (sequenceMatchingPreferences != null) {
            SequenceFactory sequenceFactory = SequenceFactory.getInstance();
            peptideMapKeyLength = sequenceFactory.getDefaultProteinTree().getInitialTagSize();
            peptideMap = new HashMap<String, LinkedList<Peptide>>(1024);
        }

        LinkedList<SpectrumMatch> result = new LinkedList<SpectrumMatch>();

        if (useCustomParser) {

            // set the waiting handler max value
            if (waitingHandler != null) {

                waitingHandler.setSecondaryProgressCounterIndeterminate(true);

                BufferedReader br = new BufferedReader(new FileReader(mzIdentMLFile));

                int lineCounter = 0;
                String line = br.readLine();

                while (line != null) {
                    line = br.readLine();
                    lineCounter++;
                }

                br.close();

                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxSecondaryProgressCounter(lineCounter);
            }

            return parseFile(waitingHandler);
        } else {

            DataCollection dataCollection = unmarshaller.unmarshal(DataCollection.class);
            AnalysisData analysisData = dataCollection.getAnalysisData();

            // Get the list of SpectrumIdentification elements
            List<SpectrumIdentificationList> spectrumIdList = analysisData.getSpectrumIdentificationList();

            int spectrumIdentificationResultSize = 0;
            // find the number of psms to parse
            for (SpectrumIdentificationList spectrumIdElements : spectrumIdList) {
                spectrumIdentificationResultSize += spectrumIdElements.getSpectrumIdentificationResult().size();
            }

            // set the waiting handler max value
            if (waitingHandler != null) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxSecondaryProgressCounter(spectrumIdentificationResultSize);
            }

            // Reset the software versions to keep only the advocates which were used for scoring
            softwareVersions.clear();

            // get the psms
            for (SpectrumIdentificationList spectrumIdElements : spectrumIdList) {
                for (SpectrumIdentificationResult spectrumIdentResult : spectrumIdElements.getSpectrumIdentificationResult()) {

                    // get the spectrum title
                    String spectrumTitle = null;
                    for (CvParam cvParam : spectrumIdentResult.getCvParam()) {
                        if (cvParam.getAccession().equalsIgnoreCase("MS:1000796") || cvParam.getName().equalsIgnoreCase("spectrum title")) {
                            spectrumTitle = cvParam.getValue();
                        }
                    }

                    // see if we can find the spectrum index
                    String spectrumId = spectrumIdentResult.getSpectrumID();
                    Integer spectrumIndex = null;
                    if (spectrumId != null && spectrumId.startsWith("index=")) {
                        spectrumIndex = Integer.valueOf(spectrumId.substring(spectrumId.indexOf("=") + 1));
                    }

                    // get the spectrum file name
                    SpectraData spectraData = unmarshaller.unmarshal(SpectraData.class, spectrumIdentResult.getSpectraDataRef());
                    String spectrumFileName = new File(spectraData.getLocation()).getName();

                    // set up the yet empty spectrum match
                    SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle));

                    // set spectrum index, used if title is not provided
                    if (spectrumIndex != null) {
                        int spectrumNumber = spectrumIndex + 1;
                        currentMatch.setSpectrumNumber(spectrumNumber);
                    }

                    // iterate and add the spectrum matches
                    for (SpectrumIdentificationItem spectrumIdentItem : spectrumIdentResult.getSpectrumIdentificationItem()) {

                        int rank = spectrumIdentItem.getRank();
                        String peptideReference = spectrumIdentItem.getPeptideRef();

                        // get the peptide
                        uk.ac.ebi.jmzidml.model.mzidml.Peptide mzIdentMLPeptide = unmarshaller.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Peptide.class, peptideReference);
                        String peptideSequence = mzIdentMLPeptide.getPeptideSequence();

                        // get the modifications
                        ArrayList<ModificationMatch> utilitiesModifications = new ArrayList<ModificationMatch>();
                        for (Modification modification : mzIdentMLPeptide.getModification()) {

                            String accession = modification.getCvParam().get(0).getAccession(); // note: only the first ptm cv term is used
                            int location = modification.getLocation();
                            double monoMassDelta = modification.getMonoisotopicMassDelta();

                            boolean fixed = false;
                            for (SearchModification searchFixedModification : fixedModifications) {
                                if (accession.equals(searchFixedModification.getCvParam().get(0).getAccession()) || searchFixedModification.getMassDelta() == monoMassDelta) {
                                    boolean allRules = true;
                                    List<SpecificityRules> specificityRules = searchFixedModification.getSpecificityRules();
                                    if (specificityRules != null && !specificityRules.isEmpty()) {
                                        for (SpecificityRules specificityRule : specificityRules) {
                                            for (CvParam cvParam : specificityRule.getCvParam()) {
                                                if (cvParam.getAccession().equals("MS:1001189") || cvParam.getAccession().equals("MS:1002057")) {
                                                    if (location != 0) {
                                                        allRules = false;
                                                        break;
                                                    }
                                                } else if (cvParam.getAccession().equals("MS:1001190") || cvParam.getAccession().equals("MS:1002058")) {
                                                    if (location != peptideSequence.length() + 1) {
                                                        allRules = false;
                                                        break;
                                                    }
                                                } else if (cvParam.getAccession().equals("MS:1001875")) {
                                                    // can we use this?
                                                } else if (cvParam.getAccession().equals("MS:1001876")) {
                                                    // not a specificity rule but the scoring of the specificity
                                                } else {
                                                    throw new IllegalArgumentException("Specificity rule " + cvParam.getAccession() + " not recognized.");
                                                }
                                            }
                                            if (!allRules) {
                                                break;
                                            }
                                        }
                                    }
                                    if (allRules) {
                                        List<String> residues = searchFixedModification.getResidues();
                                        if (residues == null || residues.isEmpty()) {
                                            fixed = true;
                                            break;
                                        } else {
                                            String aaAtLocation;
                                            if (location == 0) {
                                                aaAtLocation = peptideSequence.charAt(0) + "";
                                            } else if (location == peptideSequence.length() + 1) {
                                                aaAtLocation = peptideSequence.charAt(location - 2) + "";
                                            } else {
                                                aaAtLocation = peptideSequence.charAt(location - 1) + "";
                                            }
                                            for (String residue : residues) {
                                                if (residue.equals(aaAtLocation) || residue.equals(".")) {
                                                    fixed = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (fixed) {
                                    break;
                                }
                            }

                            if (!fixed) {

                                if (location == 0) {
                                    location = 1; // n-term ptm
                                } else if (location == peptideSequence.length() + 1) {
                                    location -= 1; // c-term ptm
                                }

                                utilitiesModifications.add(new ModificationMatch(monoMassDelta + "@" + peptideSequence.charAt(location - 1), true, location));
                            }
                        }

                        // create the peptide
                        Peptide peptide = new Peptide(peptideSequence, utilitiesModifications);

                        if (sequenceMatchingPreferences != null) {
                            String subSequence = peptideSequence.substring(0, peptideMapKeyLength);
                            subSequence = AminoAcid.getMatchingSequence(subSequence, sequenceMatchingPreferences);
                            LinkedList<Peptide> peptidesForTag = peptideMap.get(subSequence);
                            if (peptidesForTag == null) {
                                peptidesForTag = new LinkedList<Peptide>();
                                peptideMap.put(subSequence, peptidesForTag);
                            }
                            peptidesForTag.add(peptide);
                        }

                        // get the e-value and advocate
                        HashMap<String, Double> scoreMap = getAccessionToEValue(spectrumIdentItem);
                        EValueObject tempEValue = getEValue(scoreMap, spectrumIdentItem.getId());
                        Advocate advocate = tempEValue.getAdvocate();
                        Double eValue = tempEValue.getEValue();
                        Double rawScore = tempEValue.getRawScore();

                        // get the charge
                        Charge peptideCharge = new Charge(Charge.PLUS, spectrumIdentItem.getChargeState());

                        // create the peptide assumption
                        PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, advocate.getIndex(), peptideCharge, eValue, mzIdentMLFileName);

                        if (rawScore != null) {
                            peptideAssumption.setRawScore(rawScore);
                        }

                        if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideAssumption.getPeptide().getSequence())) {
                            ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
                            for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {
                                Peptide newPeptide = new Peptide(expandedSequence.toString(), new ArrayList<ModificationMatch>(modificationMatches.size()));
                                for (ModificationMatch modificationMatch : modificationMatches) {
                                    newPeptide.addModificationMatch(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), modificationMatch.getModificationSite()));
                                }
                                PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(), peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(), peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                                if (rawScore != null) {
                                    newAssumption.setRawScore(rawScore);
                                }

                                currentMatch.addHit(advocate.getIndex(), newAssumption, false);
                            }
                        } else {
                            currentMatch.addHit(advocate.getIndex(), peptideAssumption, false);
                        }

                        if (waitingHandler != null) {
                            if (waitingHandler.isRunCanceled()) {
                                break;
                            }
                        }
                    }

                    if (waitingHandler != null) {
                        if (waitingHandler.isRunCanceled()) {
                            break;
                        }
                        waitingHandler.increaseSecondaryProgressCounter();
                    }

                    result.add(currentMatch);
                }

                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Makes a score accession to score value map for the given
     * spectrumIdentificationItem.
     *
     * @param spectrumIdentItem the spectrum identification item
     *
     * @return a map of the score accession to score value of the given scores
     */
    private HashMap<String, Double> getAccessionToEValue(SpectrumIdentificationItem spectrumIdentItem) {
        HashMap<String, Double> result = new HashMap<String, Double>();
        for (CvParam cvParam : spectrumIdentItem.getCvParam()) {
            String accession = cvParam.getAccession();
            if (cvParam.getValue() != null) {
                try {
                    Double eValue = new Double(cvParam.getValue());
                    result.put(accession, eValue);
                } catch (NumberFormatException e) {
                    // ignore, not a number
                }
            }
        }
        return result;
    }

    /**
     * Returns the advocate.
     *
     * @return the advocate
     */
    private Advocate getAdvocate() {
        for (String softwareName : tempSoftwareVersions.keySet()) {
            Advocate advocate = Advocate.getAdvocate(softwareName);
            if (advocate != null) {
                return advocate;
            }
        }
        for (String softwareName : tempSoftwareVersions.keySet()) {
            return Advocate.addUserAdvocate(softwareName);
        }
        return Advocate.genericMzId;
    }

    @Override
    public void close() throws IOException {
        mzIdentMLFile = null;
        unmarshaller = null;
        //unmarshaller.close(); // @TODO: close method is missing?
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        return softwareVersions;
    }

    @Override
    public HashMap<String, LinkedList<Peptide>> getPeptidesMap() {
        return peptideMap;
    }

    @Override
    public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap() {
        return new HashMap<String, LinkedList<SpectrumMatch>>();
    }

    @Override
    public void clearTagsMap() {
        // No tags here
    }

    @Override
    public void clearPeptidesMap() {
        if (peptideMap != null) {
            peptideMap.clear();
        }
    }

    /**
     * Main method for testing purposes only.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MzIdentMLIdfileReader temp = new MzIdentMLIdfileReader();
        temp.parseFile(null);
    }

    /**
     * Parse the mzid file.
     *
     * @return the list of spectrum matches
     */
    private LinkedList<SpectrumMatch> parseFile(WaitingHandler waitingHandler) {

        LinkedList<SpectrumMatch> result = new LinkedList<SpectrumMatch>();

        try {
            // create the pull parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            // create a reader for the input file
            BufferedReader br = new BufferedReader(new FileReader(mzIdentMLFile));

            // set the XML Pull Parser to read from this reader
            parser.setInput(br);

            // start the parsing
            int type = parser.next();

            tempPeptideMap = new HashMap<String, PeptideCustom>();
            spectrumFileNameMap = new HashMap<String, String>();
            fixedModificationsCustomParser = new ArrayList<SearchModificationCustom>();

            // reset the software versions to keep only the advocates which were used for scoring
            softwareVersions.clear();

            // get the analysis software, the spectra data,the peptides and the psms
            while (type != XmlPullParser.END_DOCUMENT) {

                if (type == XmlPullParser.START_TAG && parser.getName().equals("AnalysisSoftware")) {
                    parseSoftware(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("Peptide")) {
                    parsePeptide(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("SpectraData")) {
                    parseSpectraData(parser, spectrumFileNameMap);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("ModificationParams")) {
                    parseFixedPtms(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("SpectrumIdentificationResult")) {
                    parsePsm(parser, result);
                }

                type = parser.next();

                if (waitingHandler != null) {
                    waitingHandler.setSecondaryProgressCounter(parser.getLineNumber());
                }
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Parse a peptide object.
     *
     * @param parser the XML parser
     * @throws Exception thrown if an exception occurs
     */
    private void parsePeptide(XmlPullParser parser) throws Exception {

        String pepKey = parser.getAttributeValue(0);
        
        int type = parser.next();
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("PeptideSequence")) {
            type = parser.next();
        }
        type = parser.next();
        String peptideSequence = parser.getText().trim();

        while (parser.getName() == null || (!parser.getName().equals("Peptide") && !parser.getName().equals("Modification"))) {
            type = parser.next();
        }

        ArrayList<SearchModificationCustom> modifications = new ArrayList<SearchModificationCustom>();

        while (type != XmlPullParser.END_TAG && parser.getName() != null && parser.getName().equals("Modification")) {

            Integer location = null;
            Double monoMassDelta = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("monoisotopicMassDelta")) {
                    monoMassDelta = Double.parseDouble(parser.getAttributeValue(i));
                } else if (attributeName.equalsIgnoreCase("location")) {
                    location = Integer.parseInt(parser.getAttributeValue(i));
                }
            }

            parser.next();
            parser.next();

            String accession = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("accession")) { // note that only the first ptm cv term is used
                    accession = parser.getAttributeValue(i);
                }
            }

            if (location == null || monoMassDelta == null || accession == null) {
                throw new IllegalArgumentException("Could not parse PTM!");
            }

            modifications.add(new SearchModificationCustom(accession, location, monoMassDelta));

            parser.next();

            while (parser.getName() == null || (parser.getName() != null && parser.getName().equals("cvParam"))) {
                parser.next();
            }

            parser.next();
            parser.next();
        }

        tempPeptideMap.put(pepKey, new PeptideCustom(peptideSequence, modifications));
    }

    /**
     * Parse a software object.
     *
     * @param parser the XML parser
     * @throws Exception thrown if an exception occurs
     */
    private void parseSoftware(XmlPullParser parser) throws Exception {

        String softwareVersion = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attributeName = parser.getAttributeName(i);
            if (attributeName.equalsIgnoreCase("version")) {
                softwareVersion = parser.getAttributeValue(i);
            }
        }

        parser.next();

        while (parser.getName() == null || (parser.getName() != null && !parser.getName().equals("SoftwareName"))) {
            parser.next();
        }

        parser.next();
        if (parser.getName() == null) {
            parser.next();
        }

        String softwareName = null;

        if (parser.getName().equals("cvParam")) {
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("name")) {
                    softwareName = parser.getAttributeValue(i);
                }
            }
        } else if (parser.getName().equals("userParam")) {
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("name")) {
                    softwareName = parser.getAttributeValue(i);
                }
            }
        }

        if (softwareName != null && softwareVersion != null) {

            ArrayList<String> versions = tempSoftwareVersions.get(softwareName);

            if (versions == null) {
                versions = new ArrayList<String>();
                versions.add(softwareVersion);
                tempSoftwareVersions.put(softwareName, versions);
            } else if (!versions.contains(softwareVersion)) {
                versions.add(softwareVersion);
            }

            softwareVersions.put(softwareName, versions);
        }

        softwareVersions.putAll(tempSoftwareVersions);
    }

    /**
     * Returns true of the given modification is to be considered as variable.
     *
     * @param accession the accession of the modification
     * @param location the location of the modification
     * @param monoMassDelta the delta mass of the modification
     * @param peptideSequence the peptide sequence of the modification
     */
    private boolean isVariableModification(SearchModificationCustom modification, String peptideSequence) {

        boolean fixed = false;
        int location = modification.getLocation();

        // check if the current modification is a fixed modification
        for (SearchModificationCustom fixedModification : fixedModificationsCustomParser) {

            // compare accession numbers (excluding  MS:1001460 - unknown modification) and if not equal then compare the delta masses
            if ((modification.getAccession().equals(fixedModification.getAccession()) && !modification.getAccession().equals("MS:1001460"))
                    || fixedModification.getMassDelta() == modification.getMassDelta()) {

                boolean allRules = true;
                ArrayList<String> specificityRuleCvTerms = fixedModification.getModRuleCvTerms();
                if (specificityRuleCvTerms != null && !specificityRuleCvTerms.isEmpty()) {
                    for (String specificityRuleCvTerm : specificityRuleCvTerms) {
                        if (specificityRuleCvTerm.equals("MS:1001189") || specificityRuleCvTerm.equals("MS:1002057")) {
                            if (location != 0) {
                                allRules = false;
                                break;
                            }
                        } else if (specificityRuleCvTerm.equals("MS:1001190") || specificityRuleCvTerm.equals("MS:1002058")) {
                            if (location != peptideSequence.length() + 1) {
                                allRules = false;
                                break;
                            }
                        } else if (specificityRuleCvTerm.equals("MS:1001875")) {
                            // can we use this?
                        } else if (specificityRuleCvTerm.equals("MS:1001876")) {
                            // not a specificity rule but the scoring of the specificity
                        } else {
                            throw new IllegalArgumentException("Specificity rule " + specificityRuleCvTerm + " not recognized.");
                        }

                        if (!allRules) {
                            break;
                        }
                    }
                }
                if (allRules) {
                    String residues = fixedModification.getResidues();
                    if (residues == null || residues.isEmpty()) {
                        fixed = true;
                        break;
                    } else {
                        char aaAtLocation;
                        if (location == 0) {
                            aaAtLocation = peptideSequence.charAt(0);
                        } else if (location == peptideSequence.length() + 1) {
                            aaAtLocation = peptideSequence.charAt(location - 2);
                        } else {
                            aaAtLocation = peptideSequence.charAt(location - 1);
                        }
                        for (char residue : residues.toCharArray()) {
                            if (residue == aaAtLocation || residue == '.') {
                                fixed = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (fixed) {
                break;
            }
        }

        return !fixed;
    }

    /**
     * Parse the list of fixed modifications.
     *
     * @param parser the XML parser
     * @throws Exception thrown if an exception occurs
     */
    private void parseFixedPtms(XmlPullParser parser) throws Exception {

        parser.next();
        parser.next();

        if (!parser.getName().equals("ModificationParams")) {

            while (parser.getName().equalsIgnoreCase("SearchModification")) {

                String residues = null;
                Double massDelta = null;
                boolean fixed = false;
                ArrayList<String> modRuleCvTerms = new ArrayList<String>();
                ArrayList<String> ptmCvTerms = new ArrayList<String>();

                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    String attributeName = parser.getAttributeName(i);

                    if (attributeName.equalsIgnoreCase("residues")) {
                        residues = parser.getAttributeValue(i);
                    } else if (attributeName.equalsIgnoreCase("massDelta")) {
                        massDelta = Double.parseDouble(parser.getAttributeValue(i));
                    } else if (attributeName.equalsIgnoreCase("fixedMod")) {
                        fixed = Boolean.parseBoolean(parser.getAttributeValue(i));
                    }
                }

                parser.next();
                parser.next();

                if (parser.getName() != null && parser.getName().equals("SpecificityRules")) {

                    parser.next();

                    if (parser.getName() == null) { // no idea why this is needed for ms-gf+...
                        parser.next();
                    }

                    while (parser.getName() != null && parser.getName().equals("cvParam")) {

                        if (parser.getName().equals("cvParam")) {

                            String accession = null;

                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                String attributeName = parser.getAttributeName(i);

                                if (attributeName.equalsIgnoreCase("accession")) {
                                    accession = parser.getAttributeValue(i);
                                }
                            }

                            modRuleCvTerms.add(accession);
                        }

                        parser.next();
                        parser.next();
                        parser.next();
                    }

                    parser.next();
                    if (parser.getName() == null) { // don't get why this is needed for ms-gf+...
                        parser.next();
                    }
                }

                while (parser.getName() != null && (parser.getName().equals("cvParam") || parser.getName().equals("userParam"))) {

                    if (parser.getName().equals("cvParam")) {

                        String accession = null;

                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String attributeName = parser.getAttributeName(i);

                            if (attributeName.equalsIgnoreCase("accession")) {
                                accession = parser.getAttributeValue(i);
                            }
                        }

                        if (accession != null && !accession.equalsIgnoreCase("MS:1002504")) { // ignore MS:1002504 - modification index
                            ptmCvTerms.add(accession);
                        }
                    }

                    parser.next();
                    parser.next();
                    parser.next();
                }

                if (fixed && !ptmCvTerms.isEmpty()) {
                    for (String tempPtmCvTerm : ptmCvTerms) {
                        fixedModificationsCustomParser.add(new SearchModificationCustom(tempPtmCvTerm, residues, massDelta, modRuleCvTerms));
                    }
                }

                parser.next();
                parser.next();
            }
        }
    }

    /**
     * Parse a SpectraData element.
     *
     * @param parser the XML parser
     * @param spectrumFileNameMap the spectrum file name map
     * @throws Exception thrown if an exception occurs
     */
    private void parseSpectraData(XmlPullParser parser, HashMap<String, String> spectrumFileNameMap) throws Exception {

        String fileName = null;
        String id = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {

            String attributeName = parser.getAttributeName(i);

            if (attributeName.equalsIgnoreCase("name")) {
                fileName = parser.getAttributeValue(i);
            } else if (attributeName.equalsIgnoreCase("id")) {
                id = parser.getAttributeValue(i);
            }
        }

        if (fileName != null && id != null) {
            spectrumFileNameMap.put(id, fileName);
        }
    }

    /**
     * Parse a PSM object.
     *
     * @param parser the XML parser
     * @param result the list to add the extracted PSM to
     * @throws Exception thrown if an exception occurs
     */
    private void parsePsm(XmlPullParser parser, LinkedList<SpectrumMatch> result) throws Exception {

        String spectraDataRef = null;
        String spectrumId = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attributeName = parser.getAttributeName(i);
            if (attributeName.equalsIgnoreCase("spectraData_ref")) {
                spectraDataRef = parser.getAttributeValue(i);
            } else if (attributeName.equalsIgnoreCase("spectrumID")) {
                spectrumId = parser.getAttributeValue(i);
            }
        }

        if (spectraDataRef == null || spectrumId == null) {
            throw new IllegalArgumentException("Error parsing SpectrumIdentificationResult!");
        }

        // see if we can find the spectrum index
        Integer spectrumIndex = null;
        if (spectrumId != null && spectrumId.startsWith("index=")) {
            spectrumIndex = Integer.valueOf(spectrumId.substring(spectrumId.indexOf("=") + 1));
        }

        // get the spectrum file name
        String spectrumFileName = spectrumFileNameMap.get(spectraDataRef);

        // set up the yet empty spectrum match
        SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(spectrumFileName, "temp"));

        // set spectrum index, used if title is not provided
        if (spectrumIndex != null) {
            int spectrumNumber = spectrumIndex + 1;
            currentMatch.setSpectrumNumber(spectrumNumber);
        }

        parser.next();
        int type = parser.next();

        while (type != XmlPullParser.END_TAG || !parser.getName().equals("cvParam")) {

            Integer rank = null;
            String peptideRef = null;
            Integer chargeState = null;
            String spectrumIdItemId = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("rank")) {
                    rank = Integer.parseInt(parser.getAttributeValue(i));
                } else if (attributeName.equalsIgnoreCase("peptide_ref")) {
                    peptideRef = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("chargeState")) {
                    chargeState = Integer.parseInt(parser.getAttributeValue(i));
                } else if (attributeName.equalsIgnoreCase("id")) {
                    spectrumIdItemId = parser.getAttributeValue(i);
                }
            }

            if (rank == null || peptideRef == null || chargeState == null || spectrumIdItemId == null | !tempPeptideMap.containsKey(peptideRef)) {
                System.out.println("spectrumIdItemId: " + spectrumIdItemId);
                throw new IllegalArgumentException("Error parsing SpectrumIdentificationItem!");
            }

            type = parser.next();

            while (parser.getName() == null || (parser.getName() != null && parser.getName().equals("PeptideEvidenceRef"))) {
                type = parser.next();
            }

            if (parser.getName().equals("Fragmentation")) {
                parser.next();
                while (parser.getName() == null || (parser.getName() != null && !parser.getName().equals("Fragmentation"))) {
                    parser.next();
                }

                parser.next();
                type = parser.next();
            }

            HashMap<String, Double> eValueMap = new HashMap<String, Double>();

            while (parser.getName() != null && (parser.getName().equals("cvParam") || parser.getName().equals("userParam"))) {

                if (parser.getName().equals("cvParam")) {

                    String accession = null;
                    Double value = null;

                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attributeName = parser.getAttributeName(i);

                        if (attributeName.equalsIgnoreCase("accession")) {
                            accession = parser.getAttributeValue(i);
                        } else if (attributeName.equalsIgnoreCase("value")) {
                            try {
                                value = Double.parseDouble(parser.getAttributeValue(i));
                            } catch (NumberFormatException e) {
                                // ignore, not a number
                            }
                        }
                    }

                    if (value != null) {
                        eValueMap.put(accession, value);
                    }
                }

                parser.next();
                parser.next();
                type = parser.next();
            }

            if (parser.getName().equals("SpectrumIdentificationItem") && type == XmlPullParser.END_TAG) {
                parser.next();
                parser.next();
            } else {
                parser.next();
                parser.next();
                parser.next();
            }

            // get the e-value
            EValueObject tempEValue = getEValue(eValueMap, spectrumIdItemId);
            Advocate advocate = tempEValue.getAdvocate();
            Double eValue = tempEValue.getEValue();
            Double rawScore = tempEValue.getRawScore();

            // get the peptide
            PeptideCustom tempPeptide = tempPeptideMap.get(peptideRef);

            // create a new peptide
            ArrayList<ModificationMatch> modMatches = new ArrayList<ModificationMatch>();
            for (SearchModificationCustom tempMod : tempPeptide.getModifications()) {
                if (isVariableModification(tempMod, tempPeptide.getPeptideSequence())) {
                    // correct for terminal modifications
                    int location = tempMod.getLocation();
                    if (location == 0) {
                        location = 1; // n-term ptm
                    } else if (location == tempPeptide.getPeptideSequence().length() + 1) {
                        location -= 1; // c-term ptm
                    }
                    modMatches.add(new ModificationMatch(tempMod.getMassDelta() + "@" + tempPeptide.getPeptideSequence().charAt(location - 1), true, location));
                }
            }
            Peptide peptide = new Peptide(tempPeptide.getPeptideSequence(), modMatches);

            if (sequenceMatchingPreferences != null) {
                String subSequence = peptide.getSequence().substring(0, peptideMapKeyLength);
                subSequence = AminoAcid.getMatchingSequence(subSequence, sequenceMatchingPreferences);
                LinkedList<Peptide> peptidesForTag = peptideMap.get(subSequence);
                if (peptidesForTag == null) {
                    peptidesForTag = new LinkedList<Peptide>();
                    peptideMap.put(subSequence, peptidesForTag);
                }
                peptidesForTag.add(peptide);
            }

            // get the charge
            Charge peptideCharge = new Charge(Charge.PLUS, chargeState);

            // create the peptide assumption
            PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, advocate.getIndex(), peptideCharge, eValue, mzIdentMLFileName);

            if (rawScore != null) {
                peptideAssumption.setRawScore(rawScore);
            }

            if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideAssumption.getPeptide().getSequence())) {
                ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
                for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {
                    Peptide newPeptide = new Peptide(expandedSequence.toString(), new ArrayList<ModificationMatch>(modificationMatches.size()));
                    for (ModificationMatch modificationMatch : modificationMatches) {
                        newPeptide.addModificationMatch(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), modificationMatch.getModificationSite()));
                    }
                    PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(), peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(), peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                    if (rawScore != null) {
                        newAssumption.setRawScore(rawScore);
                    }
                    currentMatch.addHit(advocate.getIndex(), newAssumption, false);
                }
            } else {
                currentMatch.addHit(advocate.getIndex(), peptideAssumption, false);
            }
        }

        // get the spectrum title
        String spectrumTitle = null;

        while (parser.getName() != null && parser.getName().equals("cvParam")) {

            String accession = null;
            String name = null;
            String value = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("accession")) {
                    accession = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("value")) {
                    value = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("name")) {
                    name = parser.getAttributeValue(i);
                }
            }

            if (accession != null && name != null && value != null) {
                if (accession.equalsIgnoreCase("MS:1000796") || name.equalsIgnoreCase("spectrum title")) {
                    spectrumTitle = value;
                }
            }

            parser.next();
            parser.next();
            parser.next();
        }

        // update the spectrum key with the correct spectrum title
        if (spectrumTitle != null) {
            currentMatch.setKey(Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle));
        }

        result.add(currentMatch);
    }

    /**
     * Returns the extracted e-value details.
     *
     * @param scoreMap the map of the possible e-values
     * @param spectrumIdItemId the spectrum identification ID, only used if no
     * e-value is found
     * @return the extracted e-value details
     */
    private EValueObject getEValue(HashMap<String, Double> scoreMap, String spectrumIdItemId) {

        //TODO: select the "best" algorithm or include all?
        // Any way of doing that more elegantly?
        //
        // Scaffold
        Advocate advocate = null;
        Double eValue = scoreMap.get("MS:1001568"), rawScore = null;
        if (eValue != null) {
            advocate = Advocate.scaffold;
            String name = advocate.getName();
            if (!softwareVersions.containsKey(name)) {
                ArrayList<String> versions = tempSoftwareVersions.get(name);
                if (versions == null) {
                    versions = new ArrayList<String>();
                }
                softwareVersions.put(name, versions);
            }
        } else {

            // PeptideShaker
            eValue = scoreMap.get("MS:1002466");
            if (eValue != null) {
                rawScore = eValue;
                eValue = Math.pow(10, -eValue);
                advocate = Advocate.peptideShaker;
                String name = advocate.getName();
                if (!softwareVersions.containsKey(name)) {
                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                    if (versions == null) {
                        versions = new ArrayList<String>();
                    }
                    softwareVersions.put(name, versions);
                }
            } else {
                eValue = scoreMap.get("MS:1002467");
                if (eValue != null) {
                    rawScore = eValue;
                    eValue = Math.pow(10, -eValue);
                    advocate = Advocate.peptideShaker;
                    String name = advocate.getName();
                    if (!softwareVersions.containsKey(name)) {
                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                        if (versions == null) {
                            versions = new ArrayList<String>();
                        }
                        softwareVersions.put(name, versions);
                    }
                } else {

                    // X!Tandem
                    eValue = scoreMap.get("MS:1001330");
                    if (eValue != null) {
                        advocate = Advocate.xtandem;
                        String name = advocate.getName();
                        if (!softwareVersions.containsKey(name)) {
                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                            if (versions == null) {
                                versions = new ArrayList<String>();
                            }
                            softwareVersions.put(name, versions);
                        }
                    } else {
                        eValue = scoreMap.get("MS:1001331");
                        if (eValue != null) {
                            rawScore = eValue;
                            eValue = Math.pow(10, -eValue);
                            advocate = Advocate.xtandem;
                            String name = advocate.getName();
                            if (!softwareVersions.containsKey(name)) {
                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                if (versions == null) {
                                    versions = new ArrayList<String>();
                                }
                                softwareVersions.put(name, versions);
                            }
                        } else {

                            // OMSSA
                            eValue = scoreMap.get("MS:1001328");
                            if (eValue != null) {
                                advocate = Advocate.omssa;
                                String name = advocate.getName();
                                if (!softwareVersions.containsKey(name)) {
                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                    if (versions == null) {
                                        versions = new ArrayList<String>();
                                    }
                                    softwareVersions.put(name, versions);
                                }
                            } else {

                                // ms-gf+
                                eValue = scoreMap.get("MS:1002052");
                                if (eValue != null) {
                                    advocate = Advocate.msgf;
                                    String name = advocate.getName();
                                    if (!softwareVersions.containsKey(name)) {
                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                        if (versions == null) {
                                            versions = new ArrayList<String>();
                                        }
                                        softwareVersions.put(name, versions);
                                    }
                                } else {

                                    // MS Amanda
                                    eValue = scoreMap.get("MS:1002319");
                                    if (eValue != null) {
                                        rawScore = eValue;
                                        eValue = Math.pow(10, eValue);
                                        advocate = Advocate.msAmanda;
                                        String name = advocate.getName();
                                        if (!softwareVersions.containsKey(name)) {
                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                            if (versions == null) {
                                                versions = new ArrayList<String>();
                                            }
                                            softwareVersions.put(name, versions);
                                        }
                                    } else {
                                        // Andromeda
                                        eValue = scoreMap.get("MS:1002338");
                                        if (eValue != null) {
                                            advocate = Advocate.andromeda;
                                            String name = advocate.getName();
                                            if (!softwareVersions.containsKey(name)) {
                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                if (versions == null) {
                                                    versions = new ArrayList<String>();
                                                }
                                                softwareVersions.put(name, versions);
                                            }
                                        } else {

                                            // Byonic
                                            eValue = scoreMap.get("MS:1002262");
                                            if (eValue != null) {
                                                rawScore = eValue;
                                                eValue = Math.pow(10, -eValue);
                                                advocate = Advocate.byonic;
                                                String name = advocate.getName();
                                                if (!softwareVersions.containsKey(name)) {
                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                    if (versions == null) {
                                                        versions = new ArrayList<String>();
                                                    }
                                                    softwareVersions.put(name, versions);
                                                }
                                            } else {
                                                eValue = scoreMap.get("MS:1002311");
                                                if (eValue != null) {
                                                    rawScore = eValue;
                                                    eValue = Math.pow(10, -eValue);
                                                    advocate = Advocate.byonic;
                                                    String name = advocate.getName();
                                                    if (!softwareVersions.containsKey(name)) {
                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                        if (versions == null) {
                                                            versions = new ArrayList<String>();
                                                        }
                                                        softwareVersions.put(name, versions);
                                                    }
                                                } else {
                                                    eValue = scoreMap.get("MS:1002265");
                                                    if (eValue != null) {
                                                        advocate = Advocate.byonic;
                                                        String name = advocate.getName();
                                                        if (!softwareVersions.containsKey(name)) {
                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                            if (versions == null) {
                                                                versions = new ArrayList<String>();
                                                            }
                                                            softwareVersions.put(name, versions);
                                                        }
                                                    } else {
                                                        eValue = scoreMap.get("MS:1002309");
                                                        if (eValue != null) {
                                                            rawScore = eValue;
                                                            eValue = Math.pow(10, -eValue);
                                                            advocate = Advocate.byonic;
                                                            String name = advocate.getName();
                                                            if (!softwareVersions.containsKey(name)) {
                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                if (versions == null) {
                                                                    versions = new ArrayList<String>();
                                                                }
                                                                softwareVersions.put(name, versions);
                                                            }
                                                        } else {
                                                            eValue = scoreMap.get("MS:1002266");
                                                            if (eValue != null) {
                                                                rawScore = eValue;
                                                                eValue = Math.pow(10, eValue);
                                                                advocate = Advocate.byonic;
                                                                String name = advocate.getName();
                                                                if (!softwareVersions.containsKey(name)) {
                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                    if (versions == null) {
                                                                        versions = new ArrayList<String>();
                                                                    }
                                                                    softwareVersions.put(name, versions);
                                                                }
                                                            } else {

                                                                // Comet
                                                                //TODO: no e-value?
                                                                eValue = scoreMap.get("MS:1002255");
                                                                if (eValue != null) {
                                                                    advocate = Advocate.comet;
                                                                    String name = advocate.getName();
                                                                    if (!softwareVersions.containsKey(name)) {
                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                        if (versions == null) {
                                                                            versions = new ArrayList<String>();
                                                                        }
                                                                        softwareVersions.put(name, versions);
                                                                    }
                                                                } else {
                                                                    eValue = scoreMap.get("MS:1002252");
                                                                    if (eValue != null) {
                                                                        rawScore = eValue;
                                                                        eValue = Math.pow(10, -eValue);
                                                                        advocate = Advocate.comet;
                                                                        String name = advocate.getName();
                                                                        if (!softwareVersions.containsKey(name)) {
                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                            if (versions == null) {
                                                                                versions = new ArrayList<String>();
                                                                            }
                                                                            softwareVersions.put(name, versions);
                                                                        }
                                                                    } else {
                                                                        eValue = scoreMap.get("MS:1002053");
                                                                        if (eValue != null) {
                                                                            advocate = Advocate.msgf;
                                                                            String name = advocate.getName();
                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                if (versions == null) {
                                                                                    versions = new ArrayList<String>();
                                                                                }
                                                                                softwareVersions.put(name, versions);
                                                                            }
                                                                        } else {
                                                                            eValue = scoreMap.get("MS:1002056");
                                                                            if (eValue != null) {
                                                                                advocate = Advocate.msgf;
                                                                                String name = advocate.getName();
                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                    if (versions == null) {
                                                                                        versions = new ArrayList<String>();
                                                                                    }
                                                                                    softwareVersions.put(name, versions);
                                                                                }
                                                                            } else {
                                                                                eValue = scoreMap.get("MS:1002055");
                                                                                if (eValue != null) {
                                                                                    advocate = Advocate.msgf;
                                                                                    String name = advocate.getName();
                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                        if (versions == null) {
                                                                                            versions = new ArrayList<String>();
                                                                                        }
                                                                                        softwareVersions.put(name, versions);
                                                                                    }
                                                                                } else {
                                                                                    eValue = scoreMap.get("MS:1002054");
                                                                                    if (eValue != null) {
                                                                                        advocate = Advocate.msgf;
                                                                                        String name = advocate.getName();
                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                            if (versions == null) {
                                                                                                versions = new ArrayList<String>();
                                                                                            }
                                                                                            softwareVersions.put(name, versions);
                                                                                        }
                                                                                    } else {
                                                                                        eValue = scoreMap.get("MS:1002049");
                                                                                        if (eValue != null) {
                                                                                            advocate = Advocate.msgf;
                                                                                            String name = advocate.getName();
                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                if (versions == null) {
                                                                                                    versions = new ArrayList<String>();
                                                                                                }
                                                                                                softwareVersions.put(name, versions);
                                                                                            }
                                                                                        } else {

                                                                                            // MS Fit
                                                                                            eValue = scoreMap.get("MS:1001501");
                                                                                            if (eValue != null) {
                                                                                                advocate = Advocate.msFit;
                                                                                                String name = advocate.getName();
                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                    if (versions == null) {
                                                                                                        versions = new ArrayList<String>();
                                                                                                    }
                                                                                                    softwareVersions.put(name, versions);
                                                                                                }
                                                                                            } else {

                                                                                                // Mascot
                                                                                                eValue = scoreMap.get("MS:1001172");
                                                                                                if (eValue != null) {
                                                                                                    advocate = Advocate.mascot;
                                                                                                    String name = advocate.getName();
                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                        if (versions == null) {
                                                                                                            versions = new ArrayList<String>();
                                                                                                        }
                                                                                                        softwareVersions.put(name, versions);
                                                                                                    }
                                                                                                } else {
                                                                                                    eValue = scoreMap.get("MS:1001171");
                                                                                                    if (eValue != null) {
                                                                                                        rawScore = eValue;
                                                                                                        eValue = Math.pow(10, -eValue);
                                                                                                        advocate = Advocate.mascot;
                                                                                                        String name = advocate.getName();
                                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                            if (versions == null) {
                                                                                                                versions = new ArrayList<String>();
                                                                                                            }
                                                                                                            softwareVersions.put(name, versions);
                                                                                                        }
                                                                                                    } else {

                                                                                                        // MyriMatch
                                                                                                        eValue = scoreMap.get("MS:1001589");
                                                                                                        if (eValue != null) {
                                                                                                            rawScore = eValue;
                                                                                                            eValue = Math.pow(Math.E, -eValue);
                                                                                                            advocate = Advocate.myriMatch;
                                                                                                            String name = advocate.getName();
                                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                if (versions == null) {
                                                                                                                    versions = new ArrayList<String>();
                                                                                                                }
                                                                                                                softwareVersions.put(name, versions);
                                                                                                            }
                                                                                                        } else {
                                                                                                            eValue = scoreMap.get("MS:1001590");
                                                                                                            if (eValue != null) {
                                                                                                                rawScore = eValue;
                                                                                                                eValue = Math.pow(Math.E, -eValue);
                                                                                                                advocate = Advocate.myriMatch;
                                                                                                                String name = advocate.getName();
                                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                    if (versions == null) {
                                                                                                                        versions = new ArrayList<String>();
                                                                                                                    }
                                                                                                                    softwareVersions.put(name, versions);
                                                                                                                }
                                                                                                            } else {

                                                                                                                // OMSSA
                                                                                                                eValue = scoreMap.get("MS:1001329");
                                                                                                                if (eValue != null) {
                                                                                                                    advocate = Advocate.omssa;
                                                                                                                    String name = advocate.getName();
                                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                        if (versions == null) {
                                                                                                                            versions = new ArrayList<String>();
                                                                                                                        }
                                                                                                                        softwareVersions.put(name, versions);
                                                                                                                    }
                                                                                                                } else {

                                                                                                                    // PEAKS
                                                                                                                    eValue = scoreMap.get("MS:1002448");
                                                                                                                    if (eValue != null) {
                                                                                                                        advocate = Advocate.peaks;
                                                                                                                        String name = advocate.getName();
                                                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                            if (versions == null) {
                                                                                                                                versions = new ArrayList<String>();
                                                                                                                            }
                                                                                                                            softwareVersions.put(name, versions);
                                                                                                                        }
                                                                                                                    } else {
                                                                                                                        eValue = scoreMap.get("MS:1001950");
                                                                                                                        if (eValue != null) {
                                                                                                                            rawScore = eValue;
                                                                                                                            eValue = Math.pow(10, -eValue);
                                                                                                                            advocate = Advocate.peaks;
                                                                                                                            String name = advocate.getName();
                                                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                if (versions == null) {
                                                                                                                                    versions = new ArrayList<String>();
                                                                                                                                }
                                                                                                                                softwareVersions.put(name, versions);
                                                                                                                            }
                                                                                                                        } else {

                                                                                                                            // Phenyx
                                                                                                                            eValue = scoreMap.get("MS:1001396");
                                                                                                                            if (eValue != null) {
                                                                                                                                advocate = Advocate.phenyx;
                                                                                                                                String name = advocate.getName();
                                                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                    if (versions == null) {
                                                                                                                                        versions = new ArrayList<String>();
                                                                                                                                    }
                                                                                                                                    softwareVersions.put(name, versions);
                                                                                                                                }
                                                                                                                            } else {
                                                                                                                                eValue = scoreMap.get("MS:1001395");
                                                                                                                                if (eValue != null) {
                                                                                                                                    rawScore = eValue;
                                                                                                                                    eValue = Math.pow(2, -eValue);
                                                                                                                                    advocate = Advocate.phenyx;
                                                                                                                                    String name = advocate.getName();
                                                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                        if (versions == null) {
                                                                                                                                            versions = new ArrayList<String>();
                                                                                                                                        }
                                                                                                                                        softwareVersions.put(name, versions);
                                                                                                                                    }
                                                                                                                                } else {

                                                                                                                                    // Profound
                                                                                                                                    eValue = scoreMap.get("MS:1001499");
                                                                                                                                    if (eValue != null) {
                                                                                                                                        rawScore = eValue;
                                                                                                                                        eValue = Math.pow(10, -eValue);
                                                                                                                                        advocate = Advocate.proFound;
                                                                                                                                        String name = advocate.getName();
                                                                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                            if (versions == null) {
                                                                                                                                                versions = new ArrayList<String>();
                                                                                                                                            }
                                                                                                                                            softwareVersions.put(name, versions);
                                                                                                                                        }
                                                                                                                                    } else {
                                                                                                                                        eValue = scoreMap.get("MS:1001498");
                                                                                                                                        if (eValue != null) {
                                                                                                                                            rawScore = eValue;
                                                                                                                                            eValue = Math.pow(2, -eValue);
                                                                                                                                            advocate = Advocate.proFound;
                                                                                                                                            String name = advocate.getName();
                                                                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                if (versions == null) {
                                                                                                                                                    versions = new ArrayList<String>();
                                                                                                                                                }
                                                                                                                                                softwareVersions.put(name, versions);
                                                                                                                                            }
                                                                                                                                        } else {

                                                                                                                                            // ProteinLynx
                                                                                                                                            eValue = scoreMap.get("MS:1001570");
                                                                                                                                            if (eValue != null) {
                                                                                                                                                rawScore = eValue;
                                                                                                                                                eValue = Math.pow(10, eValue);
                                                                                                                                                advocate = Advocate.proteinLynx;
                                                                                                                                                String name = advocate.getName();
                                                                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                    if (versions == null) {
                                                                                                                                                        versions = new ArrayList<String>();
                                                                                                                                                    }
                                                                                                                                                    softwareVersions.put(name, versions);
                                                                                                                                                }
                                                                                                                                            } else {
                                                                                                                                                eValue = scoreMap.get("MS:1001569");
                                                                                                                                                if (eValue != null) {
                                                                                                                                                    rawScore = eValue;
                                                                                                                                                    eValue = Math.pow(10, -eValue);
                                                                                                                                                    advocate = Advocate.proteinLynx;
                                                                                                                                                    String name = advocate.getName();
                                                                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                        if (versions == null) {
                                                                                                                                                            versions = new ArrayList<String>();
                                                                                                                                                        }
                                                                                                                                                        softwareVersions.put(name, versions);
                                                                                                                                                    }
                                                                                                                                                } else {

                                                                                                                                                    // ProteinProspector
                                                                                                                                                    eValue = scoreMap.get("MS:1002045");
                                                                                                                                                    if (eValue != null) {
                                                                                                                                                        advocate = Advocate.proteinProspector;
                                                                                                                                                        String name = advocate.getName();
                                                                                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                            if (versions == null) {
                                                                                                                                                                versions = new ArrayList<String>();
                                                                                                                                                            }
                                                                                                                                                            softwareVersions.put(name, versions);
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        eValue = scoreMap.get("MS:1002044");
                                                                                                                                                        if (eValue != null) {
                                                                                                                                                            rawScore = eValue;
                                                                                                                                                            eValue = Math.pow(10, -eValue);
                                                                                                                                                            advocate = Advocate.proteinProspector;
                                                                                                                                                            String name = advocate.getName();
                                                                                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                if (versions == null) {
                                                                                                                                                                    versions = new ArrayList<String>();
                                                                                                                                                                }
                                                                                                                                                                softwareVersions.put(name, versions);
                                                                                                                                                            }
                                                                                                                                                        } else {

                                                                                                                                                            // ProteinScape
                                                                                                                                                            eValue = scoreMap.get("MS:1001503");
                                                                                                                                                            if (eValue != null) {
                                                                                                                                                                advocate = Advocate.proteinScape;
                                                                                                                                                                String name = advocate.getName();
                                                                                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                    if (versions == null) {
                                                                                                                                                                        versions = new ArrayList<String>();
                                                                                                                                                                    }
                                                                                                                                                                    softwareVersions.put(name, versions);
                                                                                                                                                                }
                                                                                                                                                            } else {
                                                                                                                                                                eValue = scoreMap.get("MS:1001504");
                                                                                                                                                                if (eValue != null) {
                                                                                                                                                                    rawScore = eValue;
                                                                                                                                                                    eValue = Math.pow(10, -eValue);
                                                                                                                                                                    advocate = Advocate.proteinScape;
                                                                                                                                                                    String name = advocate.getName();
                                                                                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                        if (versions == null) {
                                                                                                                                                                            versions = new ArrayList<String>();
                                                                                                                                                                        }
                                                                                                                                                                        softwareVersions.put(name, versions);
                                                                                                                                                                    }
                                                                                                                                                                } else {
                                                                                                                                                                    // Sequest
                                                                                                                                                                    eValue = scoreMap.get("MS:1001154");
                                                                                                                                                                    if (eValue != null) {
                                                                                                                                                                        advocate = Advocate.sequest;
                                                                                                                                                                        String name = advocate.getName();
                                                                                                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                            if (versions == null) {
                                                                                                                                                                                versions = new ArrayList<String>();
                                                                                                                                                                            }
                                                                                                                                                                            softwareVersions.put(name, versions);
                                                                                                                                                                        }
                                                                                                                                                                    } else {
                                                                                                                                                                        eValue = scoreMap.get("MS:1001155");
                                                                                                                                                                        if (eValue != null) {
                                                                                                                                                                            rawScore = eValue;
                                                                                                                                                                            eValue = Math.pow(10, -eValue);
                                                                                                                                                                            advocate = Advocate.sequest;
                                                                                                                                                                            String name = advocate.getName();
                                                                                                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                if (versions == null) {
                                                                                                                                                                                    versions = new ArrayList<String>();
                                                                                                                                                                                }
                                                                                                                                                                                softwareVersions.put(name, versions);
                                                                                                                                                                            }
                                                                                                                                                                        } else {
                                                                                                                                                                            eValue = scoreMap.get("MS:1001215");
                                                                                                                                                                            if (eValue != null) {
                                                                                                                                                                                advocate = Advocate.sequest;
                                                                                                                                                                                String name = advocate.getName();
                                                                                                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                    if (versions == null) {
                                                                                                                                                                                        versions = new ArrayList<String>();
                                                                                                                                                                                    }
                                                                                                                                                                                    softwareVersions.put(name, versions);
                                                                                                                                                                                }
                                                                                                                                                                            } else {
                                                                                                                                                                                eValue = scoreMap.get("MS:1002248");
                                                                                                                                                                                if (eValue != null) {
                                                                                                                                                                                    rawScore = eValue;
                                                                                                                                                                                    eValue = Math.pow(10, -eValue);
                                                                                                                                                                                    advocate = Advocate.sequest;
                                                                                                                                                                                    String name = advocate.getName();
                                                                                                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                        if (versions == null) {
                                                                                                                                                                                            versions = new ArrayList<String>();
                                                                                                                                                                                        }
                                                                                                                                                                                        softwareVersions.put(name, versions);
                                                                                                                                                                                    }
                                                                                                                                                                                } else {

                                                                                                                                                                                    // SQID
                                                                                                                                                                                    eValue = scoreMap.get("MS:1001887");
                                                                                                                                                                                    if (eValue != null) {
                                                                                                                                                                                        rawScore = eValue;
                                                                                                                                                                                        eValue = Math.pow(10, -eValue);
                                                                                                                                                                                        advocate = Advocate.sqid;
                                                                                                                                                                                        String name = advocate.getName();
                                                                                                                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                            if (versions == null) {
                                                                                                                                                                                                versions = new ArrayList<String>();
                                                                                                                                                                                            }
                                                                                                                                                                                            softwareVersions.put(name, versions);
                                                                                                                                                                                        }
                                                                                                                                                                                    } else {

                                                                                                                                                                                        // Sonar
                                                                                                                                                                                        eValue = scoreMap.get("MS:1001502");
                                                                                                                                                                                        if (eValue != null) {
                                                                                                                                                                                            rawScore = eValue;
                                                                                                                                                                                            eValue = Math.pow(10, -eValue);
                                                                                                                                                                                            advocate = Advocate.sonar;
                                                                                                                                                                                            String name = advocate.getName();
                                                                                                                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                if (versions == null) {
                                                                                                                                                                                                    versions = new ArrayList<String>();
                                                                                                                                                                                                }
                                                                                                                                                                                                softwareVersions.put(name, versions);
                                                                                                                                                                                            }
                                                                                                                                                                                        } else {

                                                                                                                                                                                            // SpectraST
                                                                                                                                                                                            eValue = scoreMap.get("MS:1001417");
                                                                                                                                                                                            if (eValue != null) {
                                                                                                                                                                                                rawScore = eValue;
                                                                                                                                                                                                eValue = Math.pow(10, -eValue);
                                                                                                                                                                                                advocate = Advocate.spectraST;
                                                                                                                                                                                                String name = advocate.getName();
                                                                                                                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                    if (versions == null) {
                                                                                                                                                                                                        versions = new ArrayList<String>();
                                                                                                                                                                                                    }
                                                                                                                                                                                                    softwareVersions.put(name, versions);
                                                                                                                                                                                                }
                                                                                                                                                                                            } else {

                                                                                                                                                                                                // SpectrumMill
                                                                                                                                                                                                eValue = scoreMap.get("MS:1001572");
                                                                                                                                                                                                if (eValue != null) {
                                                                                                                                                                                                    rawScore = eValue;
                                                                                                                                                                                                    eValue = Math.pow(10, -eValue);
                                                                                                                                                                                                    advocate = Advocate.spectrumMill;
                                                                                                                                                                                                    String name = advocate.getName();
                                                                                                                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                        if (versions == null) {
                                                                                                                                                                                                            versions = new ArrayList<String>();
                                                                                                                                                                                                        }
                                                                                                                                                                                                        softwareVersions.put(name, versions);
                                                                                                                                                                                                    }
                                                                                                                                                                                                } else {

                                                                                                                                                                                                    // ZCore
                                                                                                                                                                                                    eValue = scoreMap.get("MS:1001952");
                                                                                                                                                                                                    if (eValue != null) {
                                                                                                                                                                                                        advocate = Advocate.zCore;
                                                                                                                                                                                                        String name = advocate.getName();
                                                                                                                                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                            if (versions == null) {
                                                                                                                                                                                                                versions = new ArrayList<String>();
                                                                                                                                                                                                            }
                                                                                                                                                                                                            softwareVersions.put(name, versions);
                                                                                                                                                                                                        }
                                                                                                                                                                                                    } else {

                                                                                                                                                                                                        // Percolator
                                                                                                                                                                                                        eValue = scoreMap.get("MS:1001491");
                                                                                                                                                                                                        if (eValue != null) {
                                                                                                                                                                                                            advocate = Advocate.percolator;
                                                                                                                                                                                                            String name = advocate.getName();
                                                                                                                                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                                if (versions == null) {
                                                                                                                                                                                                                    versions = new ArrayList<String>();
                                                                                                                                                                                                                }
                                                                                                                                                                                                                softwareVersions.put(name, versions);
                                                                                                                                                                                                            }
                                                                                                                                                                                                        } else {
                                                                                                                                                                                                            eValue = scoreMap.get("MS:1001493");
                                                                                                                                                                                                            if (eValue != null) {
                                                                                                                                                                                                                advocate = Advocate.percolator;
                                                                                                                                                                                                                String name = advocate.getName();
                                                                                                                                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                                    if (versions == null) {
                                                                                                                                                                                                                        versions = new ArrayList<String>();
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                    softwareVersions.put(name, versions);
                                                                                                                                                                                                                }
                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                eValue = scoreMap.get("MS:1001492");
                                                                                                                                                                                                                if (eValue != null) {
                                                                                                                                                                                                                    rawScore = eValue;
                                                                                                                                                                                                                    eValue = Math.pow(10, -eValue);
                                                                                                                                                                                                                    advocate = Advocate.percolator;
                                                                                                                                                                                                                    String name = advocate.getName();
                                                                                                                                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                                        if (versions == null) {
                                                                                                                                                                                                                            versions = new ArrayList<String>();
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                        softwareVersions.put(name, versions);
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                    // Generic e-value
                                                                                                                                                                                                                    eValue = scoreMap.get("MS:1002353");
                                                                                                                                                                                                                    if (eValue != null) {
                                                                                                                                                                                                                        advocate = getAdvocate();
                                                                                                                                                                                                                        String name = advocate.getName();
                                                                                                                                                                                                                        if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                                            ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                                            if (versions == null) {
                                                                                                                                                                                                                                versions = new ArrayList<String>();
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                            softwareVersions.put(name, versions);
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                    } else {

                                                                                                                                                                                                                        // Generic q-value
                                                                                                                                                                                                                        eValue = scoreMap.get("MS:1002354");
                                                                                                                                                                                                                        if (eValue != null) {
                                                                                                                                                                                                                            advocate = getAdvocate();
                                                                                                                                                                                                                            String name = advocate.getName();
                                                                                                                                                                                                                            if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                                                ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                                                if (versions == null) {
                                                                                                                                                                                                                                    versions = new ArrayList<String>();
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                softwareVersions.put(name, versions);
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                            // Generic probability/confidence
                                                                                                                                                                                                                            eValue = scoreMap.get("MS:1002357");
                                                                                                                                                                                                                            if (eValue != null) {
                                                                                                                                                                                                                                rawScore = eValue;
                                                                                                                                                                                                                                eValue = 1 - eValue;
                                                                                                                                                                                                                                advocate = getAdvocate();
                                                                                                                                                                                                                                String name = advocate.getName();
                                                                                                                                                                                                                                if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                                                    ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                                                    if (versions == null) {
                                                                                                                                                                                                                                        versions = new ArrayList<String>();
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                    softwareVersions.put(name, versions);
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                            } else {

                                                                                                                                                                                                                                // Generic probability/confidence
                                                                                                                                                                                                                                eValue = scoreMap.get("MS:1002352");
                                                                                                                                                                                                                                if (eValue != null) {
                                                                                                                                                                                                                                    rawScore = eValue;
                                                                                                                                                                                                                                    eValue = 1 - eValue;
                                                                                                                                                                                                                                    advocate = getAdvocate();
                                                                                                                                                                                                                                    String name = advocate.getName();
                                                                                                                                                                                                                                    if (!softwareVersions.containsKey(name)) {
                                                                                                                                                                                                                                        ArrayList<String> versions = tempSoftwareVersions.get(name);
                                                                                                                                                                                                                                        if (versions == null) {
                                                                                                                                                                                                                                            versions = new ArrayList<String>();
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                        softwareVersions.put(name, versions);
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                }
                                                                                                                                                                                                            }
                                                                                                                                                                                                        }
                                                                                                                                                                                                    }
                                                                                                                                                                                                }
                                                                                                                                                                                            }
                                                                                                                                                                                        }
                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (eValue == null) {
            throw new IllegalArgumentException("No e-value found for SpectrumIdentificationItem with ID " + spectrumIdItemId + " in file " + mzIdentMLFileName + ".");
        }

        return new EValueObject(eValue, rawScore, advocate);
    }

    /**
     * The e-value details.
     */
    private class EValueObject {

        /**
         * The e-value.
         */
        private Double eValue;
        /**
         * The advocate.
         */
        private Advocate advocate;
        /**
         * The raw score.
         */
        private Double rawScore;

        /**
         * Create a new EValueObject.
         *
         * @param eValue the e-value
         * @param rawScore the raw score
         * @param advocate the advocate
         */
        public EValueObject(Double eValue, Double rawScore, Advocate advocate) {
            this.eValue = eValue;
            this.rawScore = rawScore;
            this.advocate = advocate;
        }

        /**
         * Returns the e-value.
         *
         * @return the e-value
         */
        public Double getEValue() {
            return eValue;
        }

        /**
         * Returns the raw score.
         *
         * @return the raw score
         */
        public Double getRawScore() {
            return rawScore;
        }

        /**
         * Returns the advocate.
         *
         * @return the advocate
         */
        public Advocate getAdvocate() {
            return advocate;
        }
    }

    /**
     * A modification extracted by the custom parser.
     */
    private class SearchModificationCustom {

        /**
         * The accession.
         */
        private String accession;
        /**
         * The residues.
         */
        private String residues;
        /**
         * The mass delta.
         */
        private double massDelta;
        /**
         * The specificity rule CV terms.
         */
        private ArrayList<String> modRuleCvTerms;
        /**
         * The location of the modification.
         */
        private int location;

        /**
         * Create a new SearchModificationCustom.
         *
         * @param accession the PTM accession
         * @param location the location
         * @param massDelta the mass delta
         */
        public SearchModificationCustom(String accession, int location, Double massDelta) {
            this.accession = accession;
            this.location = location;
            this.massDelta = massDelta;
        }

        /**
         * Create a new SearchModificationCustom.
         *
         * @param accession the PTM accession
         * @param residues the residues
         * @param massDelta the mass delta
         * @param modRuleCvTerms the specificity rule CV terms
         */
        public SearchModificationCustom(String accession, String residues, Double massDelta, ArrayList<String> modRuleCvTerms) {
            this.accession = accession;
            this.residues = residues;
            this.massDelta = massDelta;
            this.modRuleCvTerms = modRuleCvTerms;
        }

        /**
         * Returns the residues.
         *
         * @return the residues
         */
        public String getResidues() {
            return residues;
        }

        /**
         * Returns the mass delta.
         *
         * @return the mass delta
         */
        public double getMassDelta() {
            return massDelta;
        }

        /**
         * Returns the specificity rule CV terms.
         *
         * @return the specificity rule CV terms
         */
        public ArrayList<String> getModRuleCvTerms() {
            return modRuleCvTerms;
        }

        /**
         * Returns the PTM accession.
         *
         * @return the PTM accession
         */
        public String getAccession() {
            return accession;
        }

        /**
         * Returns the location.
         *
         * @return the location
         */
        public int getLocation() {
            return location;
        }
    }

    /**
     * A peptide created by the custom parser.
     */
    private class PeptideCustom {

        /**
         * The peptide sequence.
         */
        private String peptideSequence;
        /**
         * The modifications.
         */
        private ArrayList<SearchModificationCustom> modifications;

        /**
         * Create a new PeptideCustom object.
         *
         * @param peptideSequence the peptide sequence
         * @param modifications the modifications
         */
        public PeptideCustom(String peptideSequence, ArrayList<SearchModificationCustom> modifications) {
            this.peptideSequence = peptideSequence;
            this.modifications = modifications;
        }

        /**
         * Returns the peptide sequence.
         *
         * @return the peptideSequence
         */
        public String getPeptideSequence() {
            return peptideSequence;
        }

        /**
         * Returns the modifications.
         *
         * @return the modifications
         */
        public ArrayList<SearchModificationCustom> getModifications() {
            return modifications;
        }
    }
}
