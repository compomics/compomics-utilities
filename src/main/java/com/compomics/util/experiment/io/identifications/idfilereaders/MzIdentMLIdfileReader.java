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
import uk.ac.ebi.jmzidml.model.mzidml.SearchModification;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationList;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationProtocol;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;

/**
 * This IdfileReader reads identifications from an mzIdentML result file. (Work
 * in progress...)
 *
 * @author Harald Barsnes
 */
public class MzIdentMLIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The software name.
     */
    private String softwareName = null;
    /**
     * The softwareVersion.
     */
    private String softwareVersion = null;
    /**
     * The mzIdentML file.
     */
    private File mzIdentMLFile;
    /**
     * The mzIdentML unmarshaller.
     */
    private MzIdentMLUnmarshaller unmarshaller;
//    /**
//     * Progress dialog for displaying the progress.
//     */
//    private static ProgressDialogX progressDialog;
    /**
     * The names of the fixed modifications.
     */
    private ArrayList<String> fixedModifications;

    /**
     * Main class for testing purposes only.
     *
     * @param args
     */
    public static void main(String[] args) {

//        progressDialog = new ProgressDialogX(null, null, null, true);
//        progressDialog.setPrimaryProgressCounterIndeterminate(true);
//        progressDialog.setTitle("Loading PSMs. Please Wait...");
//
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    progressDialog.setVisible(true);
//                } catch (IndexOutOfBoundsException e) {
//                    // ignore
//                }
//            }
//        }, "ProgressDialog").start();
//
//        new Thread("LoadingThread") {
//            @Override
//            public void run() {
//
//                try {
//                    MzIdentMLIdfileReader mzIdentMLIdfileReader = new MzIdentMLIdfileReader(
//                            new File("C:\\Users\\hba041\\My_Applications\\wiki\\peptide-shaker\\tutorial\\data_09_01_2014\\msgf+\\msgf+.mzid"));
//                    mzIdentMLIdfileReader.getAllSpectrumMatches(progressDialog);
//                    progressDialog.setRunFinished();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
    }

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
        unmarshaller = new MzIdentMLUnmarshaller(mzIdentMLFile);

        // get the software name and version
        AnalysisSoftwareList analysisSoftwareList = unmarshaller.unmarshal(AnalysisSoftwareList.class);

        for (AnalysisSoftware software : analysisSoftwareList.getAnalysisSoftware()) {
            String softwareId = software.getId();
            if (softwareId != null && softwareId.equalsIgnoreCase("ID_software")) {
                softwareName = software.getName();
                softwareVersion = software.getVersion();
            }
        }

        // get the list of fixed modifications
        fixedModifications = new ArrayList<String>();
        SpectrumIdentificationProtocol spectrumIdentificationProtocol = unmarshaller.unmarshal(SpectrumIdentificationProtocol.class);
        ModificationParams modifications = spectrumIdentificationProtocol.getModificationParams();
        for (SearchModification tempMod : modifications.getSearchModification()) {
            if (tempMod.isFixedMod()) {
                fixedModifications.add(tempMod.getCvParam().get(0).getAccession()); // @TODO: add better error handling...
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
        int numberOfPsms = 0;

        // find the number of psms to parse
        for (SpectrumIdentificationList spectrumIdElements : spectrumIdList) {
            for (SpectrumIdentificationResult spectrumIdentResult : spectrumIdElements.getSpectrumIdentificationResult()) {
                numberOfPsms += spectrumIdentResult.getSpectrumIdentificationItem().size(); //@TODO: is there a better/faster way of doing this?
            }
        }

        // set the waiting handler max value
        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(numberOfPsms);
        }

        // get the psms
        for (SpectrumIdentificationList spectrumIdElements : spectrumIdList) {
            for (SpectrumIdentificationResult spectrumIdentResult : spectrumIdElements.getSpectrumIdentificationResult()) {

                // get the spectrum title
                String spectrumTitle = null;
                for (CvParam cvParam : spectrumIdentResult.getCvParam()) {
                    if (cvParam.getAccession().equalsIgnoreCase("MS:1000796") || cvParam.getName().equalsIgnoreCase("spectrum title")) {
                        spectrumTitle = cvParam.getValue(); // @TODO: can this be found in other ways if the cv term is not present..?
                    }
                }

                // get the spectrum file name
                SpectraData spectraData = unmarshaller.unmarshal(SpectraData.class, spectrumIdentResult.getSpectraDataRef());

                // set up the yet empty spetrum match
                SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(spectraData.getName(), spectrumTitle));

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

                        if (!fixedModifications.contains(accession)) {

                            int location = modification.getLocation();
                            double monoMassDelta = modification.getMonoisotopicMassDelta();

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
                    double eValue = 100;
                    for (CvParam cvParam : spectrumIdentItem.getCvParam()) {
                        if (cvParam.getAccession().equalsIgnoreCase("MS:1002052")) {
                            eValue = new Double(cvParam.getValue()); // @TODO: what to do if not found..?
                        }
                    }

                    // get the charge
                    Charge peptideCharge = new Charge(Charge.PLUS, spectrumIdentItem.getChargeState());

                    // create the peptide assumption
                    PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, Advocate.MSGF.getIndex(), peptideCharge, eValue, Util.getFileName(mzIdentMLFile));
                    currentMatch.addHit(Advocate.MSGF.getIndex(), peptideAssumption, false);

                    if (waitingHandler != null) {
                        if (waitingHandler.isRunCanceled()) {
                            break;
                        }
                        waitingHandler.increaseSecondaryProgressCounter();
                    }
                }

                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
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
}
