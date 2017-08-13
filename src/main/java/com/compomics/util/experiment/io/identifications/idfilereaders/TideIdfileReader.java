package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
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
 * This IdfileReader reads identifications from an Tide tsv results file.
 *
 * @author Harald Barsnes
 */
public class TideIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The software name.
     */
    private String softwareName = "Tide";
    /**
     * The softwareVersion.
     */
    private String softwareVersion = null;
    /**
     * The Tide tsv file.
     */
    private File tideTsvFile;
    /**
     * The spectrum factory used to retrieve spectrum titles.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

    /**
     * Default constructor for the purpose of instantiation.
     */
    public TideIdfileReader() {
    }

    /**
     * Constructor for a Tide tsv results file reader.
     *
     * @param tideTsvFile the Tide tsv file
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public TideIdfileReader(File tideTsvFile) throws FileNotFoundException, IOException {
        this(tideTsvFile, null);
    }

    /**
     * Constructor for an Tide tsv result file reader.
     *
     * @param tideTsvFile the Tide tsv file
     * @param waitingHandler the waiting handler
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public TideIdfileReader(File tideTsvFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        this.tideTsvFile = tideTsvFile;

        // get the tide version number
        //extractVersionNumber(); // @TODO: how to get the Tide version number..?
    }

//    /**
//     * Extracts the Tide version number.
//     */
//    private void extractVersionNumber() throws IOException {
//
//        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(tideTsvFile, "r", 1024 * 100);
//
//        // read the version number, if available, requires ms amanda version 1.0.0.3196 or newer
//        String versionNumberString = bufferedRandomAccessFile.readLine();
//
//        if (versionNumberString.toLowerCase().startsWith("#version: ")) {
//            softwareVersion = versionNumberString.substring("#version: ".length()).trim();
//        }
//
//        bufferedRandomAccessFile.close();
//    }
    @Override
    public String getExtension() {
        return ".tide-search.target.txt";
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

        LinkedList<SpectrumMatch> result = new LinkedList<>();

        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(tideTsvFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        // check if the version number is included, ms amanda version 1.0.0.3196 or newer
        //String versionNumberString = bufferedRandomAccessFile.readLine(); // @TODO: how to get the tide version number?
        String headerString = bufferedRandomAccessFile.readLine();

        // skip the version number
//        if (versionNumberString.toLowerCase().startsWith("#version: ")) {
//            headerString = bufferedRandomAccessFile.readLine();
//        } else {
//            headerString = versionNumberString;
//        }
        // parse the header line
        String[] headers = headerString.split("\t");
        int scanNumberIndex = -1, chargeIndex = -1, precursorMzIndex = -1, spectrumNeutralLossIndex = -1, peptideMassIndex = -1, deltaCnIndex = -1,
                spScoreIndex = -1, spRankIndex = -1, exactPValueIndex = -1, xcorrScoreIndex = -1, xcorrRank = -1,
                bAndyIonsMatchedIndex = -1, bAndyIonsTotal = -1, distinctMatchesPerSpectrum = -1, sequenceIndex = -1,
                cleavageType = -1, proteinId = -1, flankingAa = -1;

        // get the column index of the headers
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];

            if (header.equalsIgnoreCase("scan")) {
                scanNumberIndex = i;
            } else if (header.equalsIgnoreCase("charge")) {
                chargeIndex = i;
            } else if (header.equalsIgnoreCase("spectrum precursor m/z")) {
                precursorMzIndex = i;
            } else if (header.equalsIgnoreCase("spectrum neutral mass")) {
                spectrumNeutralLossIndex = i;
            } else if (header.equalsIgnoreCase("peptide mass")) {
                peptideMassIndex = i;
            } else if (header.equalsIgnoreCase("delta_cn")) {
                deltaCnIndex = i;
            } else if (header.equalsIgnoreCase("sp score")) {
                spScoreIndex = i;
            } else if (header.equalsIgnoreCase("sp rank")) {
                spRankIndex = i;
            } else if (header.equalsIgnoreCase("exact p-value")) {
                exactPValueIndex = i;
            } else if (header.equalsIgnoreCase("xcorr score")) {
                xcorrScoreIndex = i;
            } else if (header.equalsIgnoreCase("xcorr rank")) {
                xcorrRank = i;
            } else if (header.equalsIgnoreCase("b/y ions matched")) {
                bAndyIonsMatchedIndex = i;
            } else if (header.equalsIgnoreCase("b/y ions total")) {
                bAndyIonsTotal = i;
            } else if (header.equalsIgnoreCase("distinct matches/spectrum")) {
                distinctMatchesPerSpectrum = i;
            } else if (header.equalsIgnoreCase("sequence")) {
                sequenceIndex = i;
            } else if (header.equalsIgnoreCase("cleavage type")) {
                cleavageType = i;
            } else if (header.equalsIgnoreCase("protein id")) {
                proteinId = i;
            } else if (header.equalsIgnoreCase("flanking aa")) {
                flankingAa = i;
            }
        }

        // check if all the required header are found
        if (scanNumberIndex == -1 || chargeIndex == -1 /**
                 * || exactPValueIndex == -1*
                 */
                || xcorrRank == -1 || sequenceIndex == -1) {
            throw new IllegalArgumentException("Mandatory columns are missing in the Tide tsv file. Please check the file!");
        }

        String line;
        String currentSpectrumTitle = null;
        SpectrumMatch currentMatch = null;

        // get the name of the mgf file
        String spectrumFileName = Util.getFileName(tideTsvFile);
        spectrumFileName = spectrumFileName.substring(0, spectrumFileName.length() - ".tide-search.target.txt".length()) + ".mgf"; // @TODO: will only work for files from searchgui...

        // get the psms
        while ((line = bufferedRandomAccessFile.readLine()) != null) {

            String[] elements = line.split("\t");

            if (!line.trim().isEmpty()) { // @TODO: make this more robust?

                int scanNumber = Integer.valueOf(elements[scanNumberIndex]);
                String modifiedPeptideSequence = elements[sequenceIndex].toUpperCase();
                int charge = Integer.valueOf(elements[chargeIndex]);

                int rank;
                if (exactPValueIndex != -1) {
                    rank = Integer.valueOf(elements[xcorrRank]);
                } else {
                    rank = Integer.valueOf(elements[xcorrRank]);
                }

                double tideEValue, rawScore;
                if (exactPValueIndex != -1) {
                    String scoreAsText = elements[exactPValueIndex];
                    tideEValue = Util.readDoubleAsString(scoreAsText);
                    rawScore = tideEValue;
                } else {
                    String scoreAsText = elements[xcorrScoreIndex];
                    rawScore = Util.readDoubleAsString(scoreAsText);
                    if (rawScore < 0) {
                        tideEValue = 100;
                    } else {
                        tideEValue = Math.pow(10, -rawScore); // convert xcorr score to a kind of e-value
                    }
                }

                String spectrumTitle = scanNumber + "";
                if (spectrumFactory.fileLoaded(spectrumFileName)) {
                    spectrumTitle = spectrumFactory.getSpectrumTitle(spectrumFileName, scanNumber);
                }

                // set up the yet empty spectrum match, or add to the current match
                if (currentMatch == null || (currentSpectrumTitle != null && !currentSpectrumTitle.equalsIgnoreCase(spectrumTitle))) {

                    // add the previous match, if any
                    if (currentMatch != null) {
                        result.add(currentMatch);
                    }

                    currentMatch = new SpectrumMatch(spectrumFileName, spectrumTitle);
                    currentMatch.setSpectrumNumber(scanNumber);
                    currentSpectrumTitle = spectrumTitle;
                }

                // get the modifications
                ArrayList<ModificationMatch> utilitiesModifications = new ArrayList<>();
                String unmodifiedPeptideSequence = "";

                // check if the peptide is modified
                if (modifiedPeptideSequence.contains("[")) {

                    // we expect something like this: TAM[15.9949]AGK
                    for (int i = 0; i < modifiedPeptideSequence.length(); i++) {
                        if (modifiedPeptideSequence.charAt(i) != '[') {
                            unmodifiedPeptideSequence += modifiedPeptideSequence.charAt(i);
                        } else {
                            // we've arrived at a modification, for example: [15.9949]
                            char modifiedResidue = modifiedPeptideSequence.charAt(i - 1); // @TODO: test for terminal ptms!
                            double ptmMass = Double.parseDouble(modifiedPeptideSequence.substring(i + 1, modifiedPeptideSequence.indexOf("]", i + 1)));
                            utilitiesModifications.add(new ModificationMatch(ptmMass + "@" + modifiedResidue, true, i));
                            i = modifiedPeptideSequence.indexOf("]", i + 1);
                        }
                    }
                } else {
                    unmodifiedPeptideSequence = modifiedPeptideSequence;
                }

                // create the peptide
                Peptide peptide = new Peptide(unmodifiedPeptideSequence, utilitiesModifications, true);

                // set up the charge
                Charge peptideCharge = new Charge(Charge.PLUS, charge);

                // create the peptide assumption
                PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, Advocate.tide.getIndex(), peptideCharge, tideEValue, Util.getFileName(tideTsvFile));
                peptideAssumption.setRawScore(rawScore);

                if (expandAaCombinations && AminoAcidSequence.hasCombination(unmodifiedPeptideSequence)) {
                    ArrayList<ModificationMatch> previousModificationMatches = peptide.getModificationMatches(),
                            newModificationMatches = null;
                    if (previousModificationMatches != null) {
                        newModificationMatches = new ArrayList<>(previousModificationMatches.size());
                    }
                    for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {
                        Peptide newPeptide = new Peptide(expandedSequence.toString(), newModificationMatches, true);
                        if (previousModificationMatches != null) {
                            for (ModificationMatch modificationMatch : previousModificationMatches) {
                                newPeptide.addModificationMatch(new ModificationMatch(modificationMatch.getModification(), modificationMatch.getVariable(), modificationMatch.getModificationSite()));
                            }
                        }
                        PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(), peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(), peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                        newAssumption.setRawScore(rawScore);
                        currentMatch.addHit(Advocate.tide.getIndex(), newAssumption, false);
                    }
                } else {
                    //peptideAssumption.addUrParam(scoreParam);
                    currentMatch.addHit(Advocate.tide.getIndex(), peptideAssumption, false);
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
        tideTsvFile = null;
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> versions = new ArrayList<>();
        versions.add(softwareVersion);
        result.put(softwareName, versions); // @TODO: check!!
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }
}
