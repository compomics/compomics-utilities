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
     * The software name.
     */
    private String softwareName = null;
    /**
     * The advocate corresponding to this software.
     */
    private Advocate advocate = null;
    /**
     * The softwareVersion.
     */
    private String softwareVersion = null;
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

        // get the software name and version
        AnalysisSoftwareList analysisSoftwareList = unmarshaller.unmarshal(AnalysisSoftwareList.class);

        for (AnalysisSoftware software : analysisSoftwareList.getAnalysisSoftware()) {
            Param softwareNameObject = software.getSoftwareName();
            
            // @TODO: not sure if the software wanted is always in first in the list...
            
            if (softwareName == null) {
                softwareName = softwareNameObject.getCvParam().getName();
            }
            if (softwareName == null) {
                softwareName = softwareNameObject.getUserParam().getName();
            }
            if (softwareVersion == null) {
                softwareVersion = software.getVersion();
            }
        }
        if (softwareName == null) {
            throw new IllegalArgumentException("The name of the software used to generate " + mzIdentMLFileName + " could not be found.");
        }

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

                    // get the e-value
                    Double eValue = null;
                    for (CvParam cvParam : spectrumIdentItem.getCvParam()) {
                        if (cvParam.getAccession().equalsIgnoreCase("MS:1002052")) {
                            eValue = new Double(cvParam.getValue());
                        }
                        
                        // @TODO: add other scores/e-values, as this breaks for anything that is not MS-GF+!!!
                    }
                    if (eValue == null) {
                        throw new IllegalArgumentException("No e-value found for spectrum " + spectrumTitle + " in file " + mzIdentMLFileName + ".");
                    }

                    // get the charge
                    Charge peptideCharge = new Charge(Charge.PLUS, spectrumIdentItem.getChargeState());

                    // The advocate
                    Advocate advocate = Advocate.getAdvocate(softwareName);
                    if (advocate == null) {
                        advocate = Advocate.addUserAdvocate(softwareName);
                    }

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

    @Override
    public void close() throws IOException {
        mzIdentMLFile = null;
        unmarshaller = null;
        //unmarshaller.close(); // @TODO: close method is missing?
    }

    @Override
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    @Override
    public String getSoftware() {
        return softwareName;
    }
}
