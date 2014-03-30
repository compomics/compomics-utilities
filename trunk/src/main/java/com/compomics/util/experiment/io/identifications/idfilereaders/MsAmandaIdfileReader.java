package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This IdfileReader reads identifications from an MS Amanda csv result file.
 * (Work in progress...)
 *
 * @author Harald Barsnes
 */
public class MsAmandaIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The software name.
     */
    private String softwareName = "MS Amanda";
    /**
     * The softwareVersion.
     */
    private String softwareVersion = null; // not available for MS Amanda
    /**
     * The MS Amanda csv file.
     */
    private File msAmandaCsvFile;
//    /**
//     * Progress dialog for displaying the progress.
//     */
//    private static ProgressDialogX progressDialog;
    /**
     * The compomics PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();

//    /**
//     * Main class for testing purposes only.
//     *
//     * @param args
//     */
//    public static void main(String[] args) {
//
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
//                    MsAmandaIdfileReader msAmandaIdfileReader = new MsAmandaIdfileReader(
//                            new File("C:\\Users\\hba041\\Desktop\\MS Search Engines\\MSAmanda1.4\\qExactive01819_output.csv"));
//                    msAmandaIdfileReader.getAllSpectrumMatches(progressDialog);
//                    progressDialog.setRunFinished();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }

    /**
     * Default constructor for the purpose of instantiation.
     */
    public MsAmandaIdfileReader() {
    }

    /**
     * Constructor for an MS Amanda csv result file reader.
     *
     * @param msAmandaCsvFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MsAmandaIdfileReader(File msAmandaCsvFile) throws FileNotFoundException, IOException {
        this(msAmandaCsvFile, null);
    }

    /**
     * Constructor for an MS Amanda csv result file reader.
     *
     * @param msAmandaCsvFile
     * @param waitingHandler
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MsAmandaIdfileReader(File msAmandaCsvFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        this.msAmandaCsvFile = msAmandaCsvFile;
    }

    @Override
    public String getExtension() {
        return ".csv";
    }

    @Override
    public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception {

        HashSet<SpectrumMatch> foundPeptides = new HashSet<SpectrumMatch>();

        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(msAmandaCsvFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        // read the header line
        bufferedRandomAccessFile.readLine();

        String line;
        String currentSpectrumTitle = null;
        SpectrumMatch currentMatch = null;

        // get the psms
        while ((line = bufferedRandomAccessFile.readLine()) != null) {

            String[] elements = line.split("\t");

            if (elements.length == 10) {
                //String scanNumber = elements[0]; // not currently used
                String spectrumTitle = elements[1];
                String peptideSequence = elements[2].toUpperCase();
                String modifications = elements[3].trim();
                //String proteinAccessions = elements[4]; // not currently used
                double score = Double.valueOf(elements[5]);
                int rank = Integer.valueOf(elements[6]);
                //String mz = elements[7]; // not currently used
                int charge = Integer.valueOf(elements[8]);
                String fileName = elements[9];

                // set up the yet empty spectrum match, or add to the current match
                if (currentMatch == null || (currentSpectrumTitle != null && !currentSpectrumTitle.equalsIgnoreCase(spectrumTitle))) {

                    // add the previous match, if any
                    if (currentMatch != null) {
                        foundPeptides.add(currentMatch);
                    }

                    currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(fileName, spectrumTitle));
                    currentSpectrumTitle = spectrumTitle;
                }

                // get the modifications
                ArrayList<ModificationMatch> utilitiesModifications = new ArrayList<ModificationMatch>();

                if (!modifications.isEmpty()) {
                    String[] ptms = modifications.split(";");

                    for (String ptm : ptms) {

                        String residue = ptm.substring(0, 1);
                        int location = Integer.parseInt(ptm.substring(1, ptm.indexOf("(")));
                        String ptmName = ptm.substring(ptm.indexOf("(") + 1, ptm.length() - 1);

                        if (ptmName.equalsIgnoreCase("Oxidation")) {
                            ptmName = "oxidation of m";
                        }

                        PTM utilitiesPtm = ptmFactory.getPTM(ptmName);

                        if (!ptmName.equalsIgnoreCase("Carbamidomethyl")) { // @TODO: how to separate fixed and variable ptms..?
                            if (!utilitiesPtm.isSameAs(PTMFactory.unknownPTM)) {
                                utilitiesModifications.add(new ModificationMatch(ptmName, true, location));
                            } else {
                                //utilitiesModifications.add(new ModificationMatch(monoMassDelta + "@" + peptideSequence.charAt(location - 1), true, location));
                                throw new IllegalArgumentException("Unknown ptm: " + ptmName + "!"); // @TODO: how to map unknown ptms..?
                            }
                        }
                    }
                }

                // create the peptide
                Peptide peptide = new Peptide(peptideSequence, utilitiesModifications);

                // set up the charge
                Charge peptideCharge = new Charge(Charge.PLUS, charge);

                // create the peptide assumption
                PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, Advocate.msAmanda.getIndex(), peptideCharge, score, Util.getFileName(msAmandaCsvFile));
                currentMatch.addHit(Advocate.msAmanda.getIndex(), peptideAssumption, true);

                if (waitingHandler != null && progressUnit != 0) {
                    waitingHandler.setSecondaryProgressCounter((int) (bufferedRandomAccessFile.getFilePointer() / progressUnit));
                    if (waitingHandler.isRunCanceled()) {
                        bufferedRandomAccessFile.close();
                        break;
                    }
                }
            }
        }

        // add the last match, if any
        if (currentMatch != null) {
            foundPeptides.add(currentMatch);
        }

        bufferedRandomAccessFile.close();

        return foundPeptides;
    }

    @Override
    public void close() throws IOException {
        msAmandaCsvFile = null;
    }

    @Override
    public String getSoftwareVersion() {
        return softwareVersion;
    }
}
