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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This IdfileReader reads identifications from an MS Amanda csv result file.
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
    private String softwareVersion = null;
    /**
     * The MS Amanda csv file.
     */
    private File msAmandaCsvFile;
    /**
     * A map of the peptides found in this file.
     */
    private HashMap<String, LinkedList<Peptide>> peptideMap;
    /**
     * The length of the keys of the peptide map.
     */
    private int peptideMapKeyLength;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public MsAmandaIdfileReader() {
    }

    /**
     * Constructor for an MS Amanda csv result file reader.
     *
     * @param msAmandaCsvFile the MS Amanda csv file
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public MsAmandaIdfileReader(File msAmandaCsvFile) throws FileNotFoundException, IOException {
        this(msAmandaCsvFile, null);
    }

    /**
     * Constructor for an MS Amanda csv result file reader.
     *
     * @param msAmandaCsvFile the MS Amanda csv file
     * @param waitingHandler the waiting handler
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public MsAmandaIdfileReader(File msAmandaCsvFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        this.msAmandaCsvFile = msAmandaCsvFile;

        // get the ms amanda version number
        extractVersionNumber();
    }

    /**
     * Extracts the MS Amanda version number.
     */
    private void extractVersionNumber() throws IOException {

        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(msAmandaCsvFile, "r", 1024 * 100);

        // read the version number, if available, requires ms amanda version 1.0.0.3196 or newer
        String versionNumberString = bufferedRandomAccessFile.readLine();

        if (versionNumberString.toLowerCase().startsWith("#version: ")) {
            softwareVersion = versionNumberString.substring("#version: ".length()).trim();
        }

        bufferedRandomAccessFile.close();
    }

    @Override
    public String getExtension() {
        return ".csv";
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

        if (sequenceMatchingPreferences != null) {
            SequenceFactory sequenceFactory = SequenceFactory.getInstance();
            peptideMapKeyLength = sequenceFactory.getDefaultProteinTree().getInitialTagSize();
            peptideMap = new HashMap<String, LinkedList<Peptide>>(1024);
        }

        LinkedList<SpectrumMatch> result = new LinkedList<SpectrumMatch>();

        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(msAmandaCsvFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        // check if the version number is included, ms amanda version 1.0.0.3196 or newer
        String versionNumberString = bufferedRandomAccessFile.readLine();
        String headerString;

        // skip the version number
        if (versionNumberString.toLowerCase().startsWith("#version: ")) {
            headerString = bufferedRandomAccessFile.readLine();
        } else {
            headerString = versionNumberString;
        }

        // parse the header line
        String[] headers = headerString.split("\t");
        int scanNumberIndex = -1, titleIndex = -1, sequenceIndex = -1, modificationsIndex = -1, proteinAccessionsIndex = -1,
                amandaScoreIndex = -1, rankIndex = -1, mzIndex = -1, chargeIndex = -1, rtIndex = -1, filenameIndex = -1,
                amandaWeightedProbabilityIndex = -1;

        // get the column index of the headers
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];

            if (header.equalsIgnoreCase("Scan Number")) {
                scanNumberIndex = i;
            } else if (header.equalsIgnoreCase("Title")) {
                titleIndex = i;
            } else if (header.equalsIgnoreCase("Sequence")) {
                sequenceIndex = i;
            } else if (header.equalsIgnoreCase("Modifications")) {
                modificationsIndex = i;
            } else if (header.equalsIgnoreCase("Protein Accessions")) {
                proteinAccessionsIndex = i;
            } else if (header.equalsIgnoreCase("Amanda Score")) {
                amandaScoreIndex = i;
            } else if (header.equalsIgnoreCase("Weighted Probability")) {
                amandaWeightedProbabilityIndex = i;
            } else if (header.equalsIgnoreCase("Rank")) {
                rankIndex = i;
            } else if (header.equalsIgnoreCase("m/z")) {
                mzIndex = i;
            } else if (header.equalsIgnoreCase("Charge")) {
                chargeIndex = i;
            } else if (header.equalsIgnoreCase("RT")) {
                rtIndex = i;
            } else if (header.equalsIgnoreCase("Filename")) {
                filenameIndex = i;
            }
        }

        // check if all the required header are found
        if (scanNumberIndex == -1 || titleIndex == -1 || sequenceIndex == -1 || modificationsIndex == -1
                || proteinAccessionsIndex == -1 || amandaScoreIndex == -1 || rankIndex == -1
                || mzIndex == -1 || chargeIndex == -1 || filenameIndex == -1) {
            throw new IllegalArgumentException("Mandatory columns are missing in the MS Amanda csv file. Please check the file!");
        }

        String line;
        String currentSpectrumTitle = null;
        SpectrumMatch currentMatch = null;

        // get the psms
        while ((line = bufferedRandomAccessFile.readLine()) != null) {

            String[] elements = line.split("\t");

            if (!line.trim().isEmpty()) { // @TODO: make this more robust?
                //String scanNumber = elements[scanNumberIndex]; // not currently used
                String spectrumTitle = elements[titleIndex];
                String peptideSequence = elements[sequenceIndex].toUpperCase();
                String modifications = elements[modificationsIndex].trim();
                //String proteinAccessions = elements[proteinAccessionsIndex]; // not currently used

                // get the ms amanda score
                String scoreAsText = elements[amandaScoreIndex];
                double msAmandaScore = Util.readDoubleAsString(scoreAsText);
                double msAmandaEValue;

                // get the ms amanda e-value
                if (amandaWeightedProbabilityIndex != -1) {
                    String eVaulueAsText = elements[amandaWeightedProbabilityIndex];
                    msAmandaEValue = Util.readDoubleAsString(eVaulueAsText);
                } else {
                    msAmandaEValue = Math.pow(10, -msAmandaScore); // convert ms amanda score to e-value
                }

                int rank = Integer.valueOf(elements[rankIndex]);
                //String mzAsText = elements[mzIndex]; // not currently used
                //double mz = Util.readDoubleAsString(mzAsText);
                int charge = Integer.valueOf(elements[chargeIndex]);
                //String rtAsText = elements[rtIndex]; // not currently used, and not mandatory, as old csv files didn't have this one...
                //double rt = Util.readDoubleAsString(rtAsText); // @TODO: should escape retention times such as PT2700.460000S
                String fileName = elements[filenameIndex];

                // set up the yet empty spectrum match, or add to the current match
                if (currentMatch == null || (currentSpectrumTitle != null && !currentSpectrumTitle.equalsIgnoreCase(spectrumTitle))) {

                    // add the previous match, if any
                    if (currentMatch != null) {
                        result.add(currentMatch);
                    }

                    currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(fileName, spectrumTitle));
                    currentSpectrumTitle = spectrumTitle;
                }

                // get the modifications
                ArrayList<ModificationMatch> utilitiesModifications = new ArrayList<ModificationMatch>();

                if (!modifications.isEmpty()) {
                    String[] ptms = modifications.split(";");

                    for (String ptm : ptms) {

                        try {
                            // we expect something like this:
                            // N-Term(acetylation of protein n-term|42.010565|variable) or
                            // C4(carbamidomethyl c|57.021464|fixed)

                            String location = ptm.substring(0, ptm.indexOf("("));
                            int modSite;

                            if (location.equalsIgnoreCase("N-Term")) {
                                modSite = 1;
                            } else if (location.equalsIgnoreCase("C-Term")) {
                                modSite = peptideSequence.length() + 1;
                            } else {
                                // amino acid type and index expected, e.g., C4 or M3
                                modSite = Integer.parseInt(ptm.substring(1, ptm.indexOf("(")));
                            }

                            String rest = ptm.substring(ptm.indexOf("(") + 1, ptm.length() - 1).toLowerCase();

                            String[] details = rest.split("\\|");
                            String ptmName = details[0]; // not currently used
                            String ptmMassAsString = details[1];
                            double ptmMass = Util.readDoubleAsString(ptmMassAsString);
                            String ptmFixedStatus = details[2];

                            if (ptmFixedStatus.equalsIgnoreCase("variable")) {
                                utilitiesModifications.add(new ModificationMatch(ptmMass + "@" + peptideSequence.charAt(modSite - 1), true, modSite));
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Error parsing ptm: " + ptm + "!");
                        }
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

                // set up the charge
                Charge peptideCharge = new Charge(Charge.PLUS, charge);

                // create the peptide assumption
                PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, Advocate.msAmanda.getIndex(), peptideCharge, msAmandaEValue, Util.getFileName(msAmandaCsvFile));
                peptideAssumption.setRawScore(msAmandaScore);

                if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideSequence)) {
                    ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
                    for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {
                        Peptide newPeptide = new Peptide(expandedSequence.toString(), new ArrayList<ModificationMatch>(modificationMatches.size()));
                        for (ModificationMatch modificationMatch : modificationMatches) {
                            newPeptide.addModificationMatch(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), modificationMatch.getModificationSite()));
                        }
                        PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(), peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(), peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                        newAssumption.setRawScore(msAmandaScore);
                        currentMatch.addHit(Advocate.msAmanda.getIndex(), newAssumption, false);
                    }
                } else {
                    currentMatch.addHit(Advocate.msAmanda.getIndex(), peptideAssumption, false);
                }

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
            result.add(currentMatch);
        }

        bufferedRandomAccessFile.close();

        return result;
    }

    @Override
    public void close() throws IOException {
        msAmandaCsvFile = null;
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        ArrayList<String> versions = new ArrayList<String>();
        versions.add(softwareVersion);
        result.put(softwareName, versions);
        return result;
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
}
