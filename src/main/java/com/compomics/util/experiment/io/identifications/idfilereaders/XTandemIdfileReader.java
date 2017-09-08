
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author dominik.kopczynski
 */
public class XTandemIdfileReader extends ExperimentObject implements IdfileReader {
    
    private File inputFileName = null;
    private HashMap<Integer, SpectrumMatch> allMatches = new HashMap<>();
    private HashMap<String, Boolean> modifications = new HashMap<>();
    private int specNumber = 0;
    private String PSMFileName;
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
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public XTandemIdfileReader(File inputFile) throws FileNotFoundException, IOException {
        this(inputFile, null);
    }

    /**
     * Constructor for an X!Tandem xml result file reader.
     *
     * @param inputFile the Mascot dat file
     * @param waitingHandler the waiting handler
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public XTandemIdfileReader(File inputFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        inputFileName = inputFile;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory.createXMLStreamReader(new FileInputStream(inputFileName));

            while (parser.hasNext()) {
                parser.next();
                switch (parser.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT: break;
                    case XMLStreamConstants.END_DOCUMENT: parser.close(); break;
                    case XMLStreamConstants.NAMESPACE: break;
                    case XMLStreamConstants.CHARACTERS: break;
                    case XMLStreamConstants.END_ELEMENT: break;
                    
                    case XMLStreamConstants.START_ELEMENT:
                        String element = parser.getLocalName();
                        if (element.equalsIgnoreCase("group") && parser.getAttributeValue("", "type") != null){
                            switch (parser.getAttributeValue("", "type").toLowerCase()) {
                                case "model":
                                    int id = Integer.parseInt(parser.getAttributeValue("", "id"));
                                    SpectrumMatch spectrumMatch = new SpectrumMatch(PSMFileName, "dummy");
                                    spectrumMatch.setSpectrumNumber(++specNumber);
                                    allMatches.put(id, spectrumMatch);
                                    double expect = Double.parseDouble(parser.getAttributeValue("", "expect"));

                                    readGroupOrProtein(parser, id, expect);
                                    break;
                                    
                                case "parameters":
                                    readParameters(parser);
                                    break;
                                    
                                default:
                                    break;
                            }
                        }
                        else if (element.equalsIgnoreCase("bioml")){
                            PSMFileName = parser.getAttributeValue("", "label");
                            PSMFileName = PSMFileName.split("'")[1];
                            PSMFileName = (new File(PSMFileName.replaceAll("\\\\", "/"))).getName();
                        }
                        break;
                        
                        
                    default: break;
                }
            }
        
        
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    @Override
    public String getExtension() {
        return ".t.xml";
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
        
        if (expandAaCombinations){
            for (SpectrumMatch spectrumMatch : allMatches.values()){
                
                ArrayList<SpectrumIdentificationAssumption> allAssumption = spectrumMatch.getAllAssumptions();
                for (SpectrumIdentificationAssumption assumption : allAssumption){
                    PeptideAssumption currentAssumption = (PeptideAssumption)assumption;
                    Peptide peptide = currentAssumption.getPeptide();
                    
                    // updating modifications
                    for (ModificationMatch mods : peptide.getModificationMatches()){
                        String modName = mods.getTheoreticPtm();
                        String modNameCheck = changeModificationName(modName);
                        if (modifications.containsKey(modNameCheck)){
                            mods.setVariable(modifications.get(modNameCheck));
                        }
                    }
                    
                    String peptideSequence = peptide.getSequence();
                    ArrayList<ModificationMatch> foundModifications = peptide.getModificationMatches();

                    if (AminoAcidSequence.hasCombination(peptideSequence)) {
                        for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {
                            if (!expandedSequence.equals(peptideSequence)){
                                Peptide newPeptide = new Peptide(expandedSequence.toString(), new ArrayList<ModificationMatch>(foundModifications.size()));
                                for (ModificationMatch modificationMatch : foundModifications) {
                                    newPeptide.addModificationMatch(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.getVariable(), modificationMatch.getModificationSite()));
                                }
                                PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, currentAssumption.getRank(), currentAssumption.getAdvocate(), currentAssumption.getIdentificationCharge(), currentAssumption.getScore(), currentAssumption.getIdentificationFile());
                                spectrumMatch.addHit(Advocate.mascot.getIndex(), newAssumption, false);
                            }
                        }
                    }
                }
            }
        }
        return new LinkedList<>(allMatches.values());
    }
    
    @Override
    public void close() throws IOException {
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        ArrayList<String> versions = new ArrayList<String>();
        versions.add(softwareVersion);
        result.put("X!Tandem", versions);
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }
    
    
    private void readGroupOrProtein(XMLStreamReader parser, int id, double expect) throws XMLStreamException, UnsupportedEncodingException {
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT: return;
                case XMLStreamConstants.END_DOCUMENT: return;
                case XMLStreamConstants.NAMESPACE: break;
                case XMLStreamConstants.CHARACTERS: break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("group".equalsIgnoreCase(parser.getLocalName())) return;
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    switch(parser.getLocalName().toLowerCase()){
                        case "group":
                            if (parser.getAttributeValue("", "label") != null && "fragment ion mass spectrum".equalsIgnoreCase(parser.getAttributeValue("", "label"))) {
                                readGroupFragment(parser, id);
                            }
                         break;
                        
                        case "protein":
                            readProtein(parser, id, expect);
                            break;
                            
                        default: break;
                    }
                    break;

                default: break;
            }
        }
    }
    
    
    private void readGroupFragment(XMLStreamReader parser, int id) throws XMLStreamException, UnsupportedEncodingException {
        boolean write = false;
        StringBuilder content = new StringBuilder();
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT: return;
                case XMLStreamConstants.END_DOCUMENT: return;
                case XMLStreamConstants.NAMESPACE: break;
                case XMLStreamConstants.CHARACTERS:
                    if (write) content.append(parser.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("note".equalsIgnoreCase(parser.getLocalName()) && write){
                        String value = content.toString().trim();
                        String title = URLDecoder.decode(value, "utf-8");
                        if (title.indexOf("RTINSECONDS") >= 0) title = title.split("RTINSECONDS")[0].trim();
                        allMatches.get(id).setSpectrumTitle(title);
                        content = new StringBuilder();
                        write = false;
                    }
                    
                    else if ("group".equalsIgnoreCase(parser.getLocalName()))  return;
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    switch(parser.getLocalName().toLowerCase()){
                        case "note":
                            write = true;
                            break;
                    
                        case "trace":
                            if (parser.getAttributeValue("", "type") != null && "tandem mass spectrum".equalsIgnoreCase(parser.getAttributeValue("", "type"))){
                                readGroupFragmentTrace(parser, id);
                            }
                            break;
                            
                        default:
                            break;
                    }
                    break;

                default: break;
            }
        }
    }
    
    
    private void readGroupFragmentTrace(XMLStreamReader parser, int id) throws XMLStreamException{
        boolean readCharge = false;
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT: return;
                case XMLStreamConstants.END_DOCUMENT: return;
                case XMLStreamConstants.NAMESPACE: break;
                case XMLStreamConstants.CHARACTERS:
                    if (readCharge){
                        int chrg = Integer.parseInt(parser.getText());
                        for (SpectrumIdentificationAssumption match : allMatches.get(id).getAllAssumptions()){
                            PeptideAssumption peptideAssumption = (PeptideAssumption)match;
                            Charge charge = new Charge(Charge.PLUS, chrg);
                            peptideAssumption.setIdentificationCharge(charge);
                        }
                    }
                    readCharge = false;
                    break;
                    
                case XMLStreamConstants.END_ELEMENT:
                    if ("trace".equalsIgnoreCase(parser.getLocalName())) return;
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    switch(parser.getLocalName().toLowerCase()){
                        case "attribute":
                            if ("charge".equalsIgnoreCase(parser.getAttributeValue("", "type"))) readCharge = true;
                            break;
                        default: break;
                    }
                    break;

                default: break;
            }
        }
    }
    
    
    private void readProtein(XMLStreamReader parser, int id, double expect) throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT: return;
                case XMLStreamConstants.END_DOCUMENT: return;
                case XMLStreamConstants.NAMESPACE: break;
                case XMLStreamConstants.CHARACTERS: break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("protein".equalsIgnoreCase(parser.getLocalName())) return;
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    if ("peptide".equalsIgnoreCase(parser.getLocalName().toLowerCase())){
                        readPeptide(parser, id, expect);
                    }
                    break;

                default: break;
            }
        }
    }
    
    
    private void readPeptide(XMLStreamReader parser, int id, double expect) throws XMLStreamException {
        Peptide peptide = null;
        int pepStart = -1;
        String pepSeq = "";
        boolean addAA = false;
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT: return;
                case XMLStreamConstants.END_DOCUMENT: return;
                case XMLStreamConstants.NAMESPACE: break;
                case XMLStreamConstants.CHARACTERS: break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("domain".equalsIgnoreCase(parser.getLocalName())) addAA = false;
                    else if ("peptide".equalsIgnoreCase(parser.getLocalName())) return;
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    switch (parser.getLocalName().toLowerCase()){
                        case "domain":
                            pepSeq = parser.getAttributeValue("", "seq");
                            
                            boolean adding = true;
                            if (allMatches.get(id).getAllAssumptions(Advocate.xtandem.getIndex()) != null) {
                                ArrayList<SpectrumIdentificationAssumption> matchAssuptions = allMatches.get(id).getAllAssumptions(Advocate.xtandem.getIndex()).get(expect);
                                for (int i = 0; i < matchAssuptions.size(); ++i){
                                    if (((PeptideAssumption)matchAssuptions.get(i)).getPeptide().getSequence().equals(pepSeq)){
                                        adding = false;
                                        break;
                                    }
                                }
                            }
                            
                            
                            if (adding){
                                peptide = new Peptide(pepSeq, new ArrayList<>());
                                PeptideAssumption currentAssumption = new PeptideAssumption(peptide, 1, Advocate.xtandem.getIndex(), null, expect, inputFileName.getName());
                                allMatches.get(id).addHit(Advocate.xtandem.getIndex(), currentAssumption, false);
                                pepStart = Integer.parseInt(parser.getAttributeValue("", "start"));
                                addAA = true;
                            }
                            break;
                        
                        case "aa":
                            if (addAA){
                                String modName = parser.getAttributeValue("", "modified") + "@" + parser.getAttributeValue("", "type");
                                int modPosition = Integer.parseInt(parser.getAttributeValue("", "at")) - pepStart + 1;
                                peptide.getModificationMatches().add(new ModificationMatch(modName, true, modPosition));
                                
                            }
                            break;
                            
                        default:
                            break;
                    }
                    break;
                    

                default: break;
            }
        }
    }

    private void readParameters(XMLStreamReader parser) throws XMLStreamException, UnsupportedEncodingException {
        int theCase = 0; // 1: fixed mod, 2: variable mod, 3: software version
        while (parser.hasNext()) {
            parser.next();
            switch (parser.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT: return;
                case XMLStreamConstants.END_DOCUMENT: return;
                case XMLStreamConstants.NAMESPACE: break;
                case XMLStreamConstants.CHARACTERS:
                    switch (theCase){
                        case 1: modifications.put(changeModificationName(parser.getText()), false); break;
                        case 2: modifications.put(changeModificationName(parser.getText()), true); break;
                        case 3: softwareVersion = parser.getText().trim();
                        default: break;
                    }
                    theCase = 0;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("group".equalsIgnoreCase(parser.getLocalName())) return;
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    if ("note".equalsIgnoreCase(parser.getLocalName())){
                        String label = parser.getAttributeValue("", "label").toLowerCase();
                        if (label.startsWith("residue, modification mass")) theCase = 1;
                        else if (label.startsWith("residue, potential modification mass")) theCase = 2;
                        else if ("process, version".equalsIgnoreCase(label)) theCase = 3;
                    }
                    break;

                default: break;
            }
        }
    }
    
    private String changeModificationName(String modification){
        int indexPoint = modification.indexOf(".");
        int size = modification.length();
        if (indexPoint >= 0){
            modification = modification.substring(0, indexPoint + 5) + modification.substring(size - 2, size);
        }
        return modification;
    }
}
