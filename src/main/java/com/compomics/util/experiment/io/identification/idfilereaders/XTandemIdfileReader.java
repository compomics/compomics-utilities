package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * This IdfileReader reads identifications from an X! Tandem xml result file.
 *
 * @author Dominik Kopczynski
 * @author Harald Barsnes
 */
public class XTandemIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The input file.
     */
    private File inputFile = null;
    /**
     * Map of all matches indexed by X!Tandem spectrum id.
     */
    private final HashMap<Integer, SpectrumMatch> allMatches = new HashMap<>();
    /**
     * The spectrum number.
     */
    private int specNumber = 0;
    /**
     * The PSM filename.
     */
    private String SpectrumFileName;
    /**
     * The software version.
     */
    private String softwareVersion;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public XTandemIdfileReader() {
    }

    /**
     * Constructor for an X!Tandem xml result file reader.
     *
     * @param inputFile the Mascot dat file
     *
     * @throws IOException if an IOException occurs
     */
    public XTandemIdfileReader(
            File inputFile
    ) throws IOException {
        this(inputFile, null);
    }

    /**
     * Constructor for an X!Tandem xml result file reader.
     *
     * @param inputFile the Mascot dat file
     * @param waitingHandler the waiting handler
     *
     * @throws IOException if an IOException occurs
     */
    public XTandemIdfileReader(
            File inputFile,
            WaitingHandler waitingHandler
    ) throws IOException {

        this.inputFile = inputFile;

    }

    @Override
    public String getExtension() {
        return ".t.xml";
    }

    @Override
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,
            WaitingHandler waitingHandler,
            SearchParameters searchParameters
    )
            throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException, XMLStreamException {

        return getAllSpectrumMatches(
                spectrumProvider,
                waitingHandler,
                searchParameters,
                null,
                false
        );
    }

    @Override
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,
            WaitingHandler waitingHandler,
            SearchParameters searchParameters,
            SequenceMatchingParameters sequenceMatchingPreferences,
            boolean expandAaCombinations
    )
            throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException, XMLStreamException {

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(100);
        
        ModificationFactory modificationFactory = ModificationFactory.getInstance();
        
        HashSet<String> fixedNonTerminalModifications = searchParameters.getModificationParameters().getFixedModifications().stream()
                .map(
                        modName -> modificationFactory.getModification(modName)
                )
                .filter(
                        modification -> modification.getModificationType() == ModificationType.modaa
                )
                .flatMap(
                        mod -> mod.getPattern().getAminoAcidsAtTarget().stream()
                                .map(
                                        aa -> trimModificationName(
                                                String.join("@",
                                                        Double.toString(mod.getMass()),
                                                        aa.toString()
                                                )
                                        )
                                )
                )
                .collect(
                        Collectors.toCollection(HashSet::new)
                );

        HashSet<String> fixedNTerminalModifications = new HashSet<>();
        HashSet<String> fixedCTerminalModifications = new HashSet<>();

        ArrayList<String> allFixedModifications = searchParameters.getModificationParameters().getFixedModifications();

        for (String tempFixedModification : allFixedModifications) {

            Modification tempModification = modificationFactory.getModification(tempFixedModification);

            if (tempModification.getModificationType() != ModificationType.modaa) {

                switch (tempModification.getModificationType()) {

                    case modn_protein:
                    case modn_peptide:

                        for (String tempAminoAcid : AminoAcid.getAminoAcidsList()) {
                            fixedNTerminalModifications.add(trimModificationName(String.join("@", Double.toString(tempModification.getMass()), tempAminoAcid)));
                        }

                        break;

                    case modnaa_protein:
                    case modnaa_peptide:

                        for (Character tempCharacter : tempModification.getPattern().getAminoAcidsAtTarget()) {
                            fixedNTerminalModifications.add(trimModificationName(String.join("@", Double.toString(tempModification.getMass()), tempCharacter.toString())));
                        }

                        break;

                    case modc_protein:
                    case modc_peptide:

                        for (String tempAminoAcid : AminoAcid.getAminoAcidsList()) {
                            fixedCTerminalModifications.add(trimModificationName(String.join("@", Double.toString(tempModification.getMass()), tempAminoAcid)));
                        }

                        break;

                    case modcaa_protein:
                    case modcaa_peptide:

                        for (Character tempCharacter : tempModification.getPattern().getAminoAcidsAtTarget()) {
                            fixedCTerminalModifications.add(trimModificationName(String.join("@", Double.toString(tempModification.getMass()), tempCharacter.toString())));
                        }

                        break;

                    default:

                        break;
                }
            }
        }

        try (SimpleFileReader reader = SimpleFileReader.getFileReader(inputFile)) {
            
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory.createXMLStreamReader(reader.getReader());

            while (parser.hasNext()) {
                
                double progress = reader.getProgressInPercent();
                waitingHandler.setSecondaryProgressCounter((int) progress);

                parser.next();

                switch (parser.getEventType()) {

                    case XMLStreamConstants.START_DOCUMENT:

                        break;

                    case XMLStreamConstants.END_DOCUMENT:

                        parser.close();
                        break;

                    case XMLStreamConstants.NAMESPACE:

                        break;

                    case XMLStreamConstants.CHARACTERS:

                        break;

                    case XMLStreamConstants.END_ELEMENT:

                        break;

                    case XMLStreamConstants.START_ELEMENT:

                        String element = parser.getLocalName();

                        if (element.equalsIgnoreCase("group") && parser.getAttributeValue("", "type") != null) {

                            switch (parser.getAttributeValue("", "type").toLowerCase()) {

                                case "model":

                                    int id = Integer.parseInt(parser.getAttributeValue("", "id"));
                                    SpectrumMatch spectrumMatch = new SpectrumMatch(SpectrumFileName, Integer.toString(id));
                                    allMatches.put(id, spectrumMatch);
                                    double expect = Double.parseDouble(parser.getAttributeValue("", "expect"));

                                    readGroupOrProtein(
                                            parser,
                                            id,
                                            expect,
                                            fixedNonTerminalModifications,
                                            fixedNTerminalModifications,
                                            fixedCTerminalModifications
                                    );

                                    break;

                                case "parameters":

                                    readParameters(parser);
                                    break;

                                default:

                                    break;
                            }

                        } else if (element.equalsIgnoreCase("bioml")) {

                            SpectrumFileName = parser.getAttributeValue("", "label");
                            SpectrumFileName = SpectrumFileName.split("'")[1];
                            SpectrumFileName = (new File(SpectrumFileName.replaceAll("\\\\", "/"))).getName();

                        }

                        break;

                    default:
                        break;
                }
            }

            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
            
            if (expandAaCombinations) {

                for (SpectrumMatch spectrumMatch : allMatches.values()) {

                    spectrumMatch.getAllPeptideAssumptions().forEach(currentAssumption -> {

                        Peptide peptide = currentAssumption.getPeptide();
                        String peptideSequence = peptide.getSequence();
                        ModificationMatch[] foundModifications = peptide.getVariableModifications();

                        if (AminoAcidSequence.hasCombination(peptideSequence)) {

                            for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {

                                if (!expandedSequence.toString().equals(peptideSequence)) {

                                    ModificationMatch[] newModificationMatches = Arrays.stream(foundModifications)
                                            .map(modificationMatch -> modificationMatch.clone())
                                            .toArray(ModificationMatch[]::new);

                                    Peptide newPeptide = new Peptide(expandedSequence.toString(), newModificationMatches);
                                    PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, currentAssumption.getRank(), currentAssumption.getAdvocate(), currentAssumption.getIdentificationCharge(), currentAssumption.getScore(), currentAssumption.getIdentificationFile());
                                    spectrumMatch.addPeptideAssumption(Advocate.mascot.getIndex(), newAssumption);

                                }
                            }
                        }
                    });
                }
            }
        }

        return new ArrayList<>(allMatches.values());
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> versions = new ArrayList<>();
        versions.add(softwareVersion);
        result.put("X!Tandem", versions);
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }

    /**
     * Parses a group or protein.
     *
     * @param parser The xml parser.
     * @param id The id of the group or protein.
     * @param expect The expectation value.
     * @param fixedNonTerminalModifications The fixed non-terminal
     * modifications.
     * @param fixedNTerminalModifications The fixed modifications on the N-term.
     * @param fixedCTerminalModifications The fixed modifications on the C-term.
     *
     * @throws XMLStreamException Exception thrown if an error occurred while
     * parsing the xml
     * @throws UnsupportedEncodingException Exception thrown if an error
     * occurred while decoding a spectrum title.
     */
    private void readGroupOrProtein(
            XMLStreamReader parser,
            int id,
            double expect,
            HashSet<String> fixedNonTerminalModifications,
            HashSet<String> fixedNTerminalModifications,
            HashSet<String> fixedCTerminalModifications
    ) throws XMLStreamException, UnsupportedEncodingException {

        while (parser.hasNext()) {

            parser.next();

            switch (parser.getEventType()) {

                case XMLStreamConstants.START_DOCUMENT:

                    return;

                case XMLStreamConstants.END_DOCUMENT:

                    return;

                case XMLStreamConstants.NAMESPACE:

                    break;

                case XMLStreamConstants.CHARACTERS:

                    break;

                case XMLStreamConstants.END_ELEMENT:

                    if ("group".equalsIgnoreCase(parser.getLocalName())) {
                        return;
                    }

                    break;

                case XMLStreamConstants.START_ELEMENT:

                    switch (parser.getLocalName().toLowerCase()) {

                        case "group":

                            if (parser.getAttributeValue("", "label") != null
                                    && "fragment ion mass spectrum".equalsIgnoreCase(parser.getAttributeValue("", "label"))) {
                                readGroupFragment(parser, id);
                            }

                            break;

                        case "protein":

                            readProtein(
                                    parser,
                                    id,
                                    expect,
                                    fixedNonTerminalModifications,
                                    fixedNTerminalModifications,
                                    fixedCTerminalModifications
                            );

                            break;

                        default:

                            break;
                    }

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Read a group fragment.
     *
     * @param parser The xml parser.
     * @param id The id of the group or protein.
     *
     * @throws XMLStreamException Exception thrown if an error occurred while
     * parsing the xml
     * @throws UnsupportedEncodingException Exception thrown if an error
     * occurred while decoding a spectrum title.
     */
    private void readGroupFragment(
            XMLStreamReader parser,
            int id
    ) throws XMLStreamException, UnsupportedEncodingException {

        boolean write = false;
        StringBuilder content = new StringBuilder();

        while (parser.hasNext()) {

            parser.next();

            switch (parser.getEventType()) {

                case XMLStreamConstants.START_DOCUMENT:

                    return;

                case XMLStreamConstants.END_DOCUMENT:

                    return;

                case XMLStreamConstants.NAMESPACE:

                    break;

                case XMLStreamConstants.CHARACTERS:

                    if (write) {
                        content.append(parser.getText());
                    }

                    break;

                case XMLStreamConstants.END_ELEMENT:

                    if ("note".equalsIgnoreCase(parser.getLocalName()) && write) {

                        String value = content.toString().trim();
                        String title = URLDecoder.decode(value, "utf-8");

                        if (title.contains("RTINSECONDS")) {
                            title = title.split("RTINSECONDS")[0].trim();
                        }

                        SpectrumMatch spectrumMatch = allMatches.get(id);
                        spectrumMatch.setSpectrumTitle(title);
                        content = new StringBuilder();
                        write = false;

                    } else if ("group".equalsIgnoreCase(parser.getLocalName())) {
                        return;
                    }

                    break;

                case XMLStreamConstants.START_ELEMENT:

                    switch (parser.getLocalName().toLowerCase()) {

                        case "note":

                            write = true;
                            break;

                        case "trace":

                            if (parser.getAttributeValue("", "type") != null
                                    && "tandem mass spectrum".equalsIgnoreCase(parser.getAttributeValue("", "type"))) {
                                readGroupFragmentTrace(parser, id);
                            }

                            break;

                        default:

                            break;
                    }

                    break;

                default:

                    break;
            }
        }
    }

    /**
     * Read fragment trace group.
     *
     * @param parser The xml parser.
     * @param id The id of the group or protein.
     *
     * @throws XMLStreamException Exception thrown if an error occurred while
     * parsing the xml
     */
    private void readGroupFragmentTrace(
            XMLStreamReader parser,
            int id
    ) throws XMLStreamException {

        boolean readCharge = false;

        while (parser.hasNext()) {

            parser.next();

            switch (parser.getEventType()) {

                case XMLStreamConstants.START_DOCUMENT:

                    return;

                case XMLStreamConstants.END_DOCUMENT:

                    return;

                case XMLStreamConstants.NAMESPACE:

                    break;

                case XMLStreamConstants.CHARACTERS:

                    if (readCharge) {

                        int chrg = Integer.parseInt(parser.getText());
                        allMatches.get(id).getAllPeptideAssumptions().forEach(peptideAssumption -> {
                            peptideAssumption.setIdentificationCharge(chrg);
                        });

                    }

                    readCharge = false;

                    break;

                case XMLStreamConstants.END_ELEMENT:

                    if ("trace".equalsIgnoreCase(parser.getLocalName())) {
                        return;
                    }

                    break;

                case XMLStreamConstants.START_ELEMENT:

                    switch (parser.getLocalName().toLowerCase()) {

                        case "attribute":

                            if ("charge".equalsIgnoreCase(parser.getAttributeValue("", "type"))) {
                                readCharge = true;
                            }

                            break;

                        default:

                            break;
                    }

                    break;

                default:

                    break;
            }
        }
    }

    /**
     * Read a protein.
     *
     * @param parser The xml parser.
     * @param id The id of the group or protein.
     * @param expect The expectation value.
     * @param fixedNonTerminalModifications The fixed non-terminal
     * modifications.
     * @param fixedNTerminalModifications The fixed modifications on the N-term.
     * @param fixedCTerminalModifications The fixed modifications on the C-term.
     *
     * @throws XMLStreamException Exception thrown if an error occurred while
     * parsing the xml
     */
    private void readProtein(
            XMLStreamReader parser,
            int id,
            double expect,
            HashSet<String> fixedNonTerminalModifications,
            HashSet<String> fixedNTerminalModifications,
            HashSet<String> fixedCTerminalModifications
    ) throws XMLStreamException {

        while (parser.hasNext()) {

            parser.next();

            switch (parser.getEventType()) {

                case XMLStreamConstants.START_DOCUMENT:

                    return;

                case XMLStreamConstants.END_DOCUMENT:

                    return;

                case XMLStreamConstants.NAMESPACE:

                    break;

                case XMLStreamConstants.CHARACTERS:

                    break;

                case XMLStreamConstants.END_ELEMENT:

                    if ("protein".equalsIgnoreCase(parser.getLocalName())) {
                        return;
                    }

                    break;

                case XMLStreamConstants.START_ELEMENT:

                    if ("peptide".equalsIgnoreCase(parser.getLocalName().toLowerCase())) {
                        readPeptide(
                                parser,
                                id,
                                expect,
                                fixedNonTerminalModifications,
                                fixedNTerminalModifications,
                                fixedCTerminalModifications
                        );
                    }

                    break;

                default:

                    break;
            }
        }
    }

    /**
     * Read a peptide.
     *
     * @param parser The xml parser.
     * @param id The id of the group or protein.
     * @param expect The expectation value.
     * @param fixedNonTerminalModifications The fixed non-terminal
     * modifications.
     * @param fixedNTerminalModifications The fixed modifications on the N-term.
     * @param fixedCTerminalModifications The fixed modifications on the C-term.
     *
     * @throws XMLStreamException Exception thrown if an error occurred while
     * parsing the xml
     */
    private void readPeptide(
            XMLStreamReader parser,
            int id,
            double expect,
            HashSet<String> fixedNonTerminalModifications,
            HashSet<String> fixedNTerminalModifications,
            HashSet<String> fixedCTerminalModifications
    ) throws XMLStreamException {

        Peptide peptide = null;
        int pepStart = -1;
        boolean addAA = false;

        while (parser.hasNext()) {

            parser.next();

            switch (parser.getEventType()) {

                case XMLStreamConstants.START_DOCUMENT:

                    return;

                case XMLStreamConstants.END_DOCUMENT:

                    return;

                case XMLStreamConstants.NAMESPACE:

                    break;

                case XMLStreamConstants.CHARACTERS:

                    break;
                case XMLStreamConstants.END_ELEMENT:

                    if ("domain".equalsIgnoreCase(parser.getLocalName())) {
                        addAA = false;
                    } else if ("peptide".equalsIgnoreCase(parser.getLocalName())) {
                        return;
                    }

                    break;

                case XMLStreamConstants.START_ELEMENT:

                    switch (parser.getLocalName().toLowerCase()) {

                        case "domain":

                            String pepSeq = parser.getAttributeValue("", "seq");

                            boolean adding = true;

                            if (allMatches.get(id).getAllPeptideAssumptions(Advocate.xtandem.getIndex()) != null) {

                                ArrayList<PeptideAssumption> matchAssuptions
                                        = allMatches.get(id).getAllPeptideAssumptions(Advocate.xtandem.getIndex()).get(expect);

                                for (int i = 0; i < matchAssuptions.size(); ++i) {

                                    if (matchAssuptions.get(i).getPeptide().getSequence().equals(pepSeq)) {
                                        adding = false;
                                        break;
                                    }

                                }
                            }

                            if (adding) {

                                peptide = new Peptide(pepSeq);
                                PeptideAssumption currentAssumption = new PeptideAssumption(peptide, 1, Advocate.xtandem.getIndex(), 0, expect, inputFile.getName());
                                allMatches.get(id).addPeptideAssumption(Advocate.xtandem.getIndex(), currentAssumption);
                                pepStart = Integer.parseInt(parser.getAttributeValue("", "start"));
                                addAA = true;

                            }

                            break;

                        case "aa":

                            if (addAA) {

                                String modName = String.join("@", parser.getAttributeValue("", "modified"), parser.getAttributeValue("", "type"));
                                int modPosition = Integer.parseInt(parser.getAttributeValue("", "at")) - pepStart + 1;
                                String reformattedName = trimModificationName(modName);

                                // potential n-terminal ptm
                                if (modPosition == 1 && !fixedNTerminalModifications.isEmpty()) {
                                    if (fixedNTerminalModifications.contains(reformattedName)) {
                                        break;
                                    }
                                }

                                // potential c-terminal ptm
                                if (modPosition == peptide.getSequence().length() && !fixedCTerminalModifications.isEmpty()) {
                                    if (fixedCTerminalModifications.contains(reformattedName)) {
                                        break;
                                    }
                                }

                                // non-terminal ptm
                                if (!fixedNonTerminalModifications.contains(reformattedName)) {
                                    peptide.addVariableModification(new ModificationMatch(modName, modPosition));
                                }
                            }

                            break;

                        default:

                            break;
                    }

                    break;

                default:

                    break;
            }
        }
    }

    /**
     * Read parameters.
     *
     * @param parser The xml parser.
     *
     * @throws XMLStreamException Exception thrown if an error occurred while
     * parsing the xml
     * @throws UnsupportedEncodingException Exception thrown if an error
     * occurred while decoding a spectrum title.
     */
    private void readParameters(
            XMLStreamReader parser
    ) throws XMLStreamException, UnsupportedEncodingException {

        int theCase = 0; // 1: fixed mod, 2: variable mod, 3: software version

        while (parser.hasNext()) {

            parser.next();

            switch (parser.getEventType()) {

                case XMLStreamConstants.START_DOCUMENT:

                    return;

                case XMLStreamConstants.END_DOCUMENT:

                    return;

                case XMLStreamConstants.NAMESPACE:

                    break;

                case XMLStreamConstants.CHARACTERS:

                    switch (theCase) {

                        case 1:
                            // Ignore
                            break;

                        case 2:
                            // aaModifications.add(changeModificationName(parser.getText()));
                            break;

                        case 3:
                            softwareVersion = parser.getText().trim();
                            break;

                        default:
                            break;
                    }

                    theCase = 0;
                    break;

                case XMLStreamConstants.END_ELEMENT:

                    if ("group".equalsIgnoreCase(parser.getLocalName())) {
                        return;
                    }

                    break;

                case XMLStreamConstants.START_ELEMENT:

                    if ("note".equalsIgnoreCase(parser.getLocalName())) {

                        String label = parser.getAttributeValue("", "label").toLowerCase();

                        if (label.startsWith("residue, modification mass")) {
                            theCase = 1;
                        } else if (label.startsWith("residue, potential modification mass")) {
                            theCase = 2;
                        } else if ("process, version".equalsIgnoreCase(label)) {
                            theCase = 3;
                        }
                    }

                    break;

                default:

                    break;
            }
        }
    }

    /**
     * Trim modification name.
     *
     * @param modification the modification name to trim
     * @return the trimmed modification name
     */
    private String trimModificationName(
            String modification
    ) {

        int indexPoint = modification.indexOf(".");
        int size = modification.length();

        if (indexPoint >= 0) {
            modification = modification.substring(0, indexPoint + 5)
                    + modification.substring(size - 2, size);
        }

        return modification;
    }
}
