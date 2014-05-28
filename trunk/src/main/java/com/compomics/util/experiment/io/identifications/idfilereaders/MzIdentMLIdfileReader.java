package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
     * Default constructor for the purpose of instantiation.
     */
    public MzIdentMLIdfileReader() {
    }

    /**
     * Constructor for an mzIdentML result file reader.
     *
     * @param mzIdentMLFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MzIdentMLIdfileReader(File mzIdentMLFile) throws FileNotFoundException, IOException {
        this(mzIdentMLFile, null);
    }

    /**
     * Constructor for an mzIdentML result file reader.
     *
     * @param mzIdentMLFile
     * @param waitingHandler
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MzIdentMLIdfileReader(File mzIdentMLFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        this.mzIdentMLFile = mzIdentMLFile;
        mzIdentMLFileName = Util.getFileName(mzIdentMLFile);
        if (mzIdentMLFile.length() < 1073741824) {
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
        
        softwareVersions = tempSoftwareVersions;

        // get the list of fixed modifications
        fixedModifications = new ArrayList<SearchModification>();
        SpectrumIdentificationProtocol spectrumIdentificationProtocol = unmarshaller.unmarshal(SpectrumIdentificationProtocol.class);
        ModificationParams modifications = spectrumIdentificationProtocol.getModificationParams();
        for (SearchModification tempMod : modifications.getSearchModification()) {
            if (tempMod.isFixedMod()) {
                fixedModifications.add(tempMod);
            }
        }
    }

    @Override
    public String getExtension() {
        return ".mzid";
    }

    @Override
    public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception {

        HashSet<SpectrumMatch> foundPeptides = new HashSet<SpectrumMatch>();
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
                Integer spectrumNumber = null;
                if (spectrumId != null && spectrumId.startsWith("index=")) {
                    spectrumNumber = Integer.valueOf(spectrumId.substring(spectrumId.indexOf("=") + 1));
                }

                // get the spectrum file name
                SpectraData spectraData = unmarshaller.unmarshal(SpectraData.class, spectrumIdentResult.getSpectraDataRef());
                String spectrumFileName = new File(spectraData.getLocation()).getName();

                // set up the yet empty spetrum match
                SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle));

                // set spectrum index, used if title is not provided
                if (spectrumNumber != null) {
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

                        String accession = modification.getCvParam().get(0).getAccession();
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
                                            if (residue.equals(aaAtLocation)) {
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
                    Peptide peptide = new Peptide(mzIdentMLPeptide.getPeptideSequence(), utilitiesModifications);

                    // get the e-value and advocate
                    HashMap<String, Double> scoreMap = getAccessionToEValue(spectrumIdentItem);
                    Double eValue = null;
                    Advocate advocate = null;

                    //TODO: select the "best" algorithm or include all?
                    // Any way of doing that more elegantly?
                    // Scaffold
                    eValue = scoreMap.get("MS:1001568");
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

                    if (eValue == null) {
                        throw new IllegalArgumentException("No e-value found for spectrum " + spectrumTitle + " in file " + mzIdentMLFileName + ".");
                    }

                    // get the charge
                    Charge peptideCharge = new Charge(Charge.PLUS, spectrumIdentItem.getChargeState());

                    // create the peptide assumption
                    PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, advocate.getIndex(), peptideCharge, eValue, mzIdentMLFileName);
                    currentMatch.addHit(advocate.getIndex(), peptideAssumption, false);

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

                foundPeptides.add(currentMatch);
            }

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }

        return foundPeptides;
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
            Double eValue = new Double(cvParam.getValue());
            result.put(accession, eValue);
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
}
