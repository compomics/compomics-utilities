package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.bind.JAXBException;

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
     *
     * @throws IOException if an IOException occurs
     */
    public TideIdfileReader(
            File tideTsvFile,
            WaitingHandler waitingHandler
    ) throws IOException {
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
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,
            WaitingHandler waitingHandler,
            SearchParameters searchParameters
    )
            throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        return getAllSpectrumMatches(
                spectrumProvider,
                waitingHandler,
                searchParameters,
                null,
                true
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
            throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        ArrayList<SpectrumMatch> result = new ArrayList<>();

        try (SimpleFileReader reader = SimpleFileReader.getFileReader(tideTsvFile)) {

            // read the header
            String headerString = reader.readLine();

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

            // check if all the required headers are found
            if (scanNumberIndex == -1 || chargeIndex == -1
                    || xcorrRank == -1 || sequenceIndex == -1) {
                throw new IllegalArgumentException("Mandatory columns are missing in the Tide tsv file. Please check the file!");
            }

            String line;

            // get the name of the spectrum file
            String fileName = IoUtil.getFileName(tideTsvFile);
            String spectrumFileName = IoUtil.removeExtension(getMgfFileName(fileName));

            // required map given that the tide output is _not_ sorted on scan index
            HashMap<Long, SpectrumMatch> tempSpectrumMatchesMap = new HashMap<>();

            // get the psms
            while ((line = reader.readLine()) != null) {

                String[] elements = line.split("\t");

                if (!line.trim().isEmpty()) {

                    int scanNumber = Integer.parseInt(elements[scanNumberIndex]);
                    String modifiedPeptideSequence = elements[sequenceIndex].toUpperCase();
                    int charge = Integer.parseInt(elements[chargeIndex]);

                    int rank;
                    if (xcorrRank != -1) {
                        rank = Integer.parseInt(elements[xcorrRank]);
                    } else {
                        rank = Integer.parseInt(elements[xcorrRank]);
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

                    String spectrumTitle = spectrumProvider.getSpectrumTitles(IoUtil.removeExtension(spectrumFileName))[scanNumber]; // @TODO: does not work for mzML files
                    Long tempSpectrumMatchKey = ExperimentObject.asLong(String.join("", spectrumFileName, spectrumTitle));
                    SpectrumMatch currentMatch = tempSpectrumMatchesMap.get(tempSpectrumMatchKey);

                    if (currentMatch == null) {
                        currentMatch = new SpectrumMatch(spectrumFileName, spectrumTitle);
                        tempSpectrumMatchesMap.put(tempSpectrumMatchKey, currentMatch);
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
                                utilitiesModifications.add(new ModificationMatch(ptmMass + "@" + modifiedResidue, i));
                                i = modifiedPeptideSequence.indexOf("]", i + 1);
                            }
                        }
                    } else {
                        unmodifiedPeptideSequence = modifiedPeptideSequence;
                    }

                    // create the peptide
                    Peptide peptide = new Peptide(unmodifiedPeptideSequence,
                            utilitiesModifications.toArray(
                                    new ModificationMatch[utilitiesModifications.size()]), true);

                    // create the peptide assumption
                    PeptideAssumption peptideAssumption = new PeptideAssumption(
                            peptide,
                            rank,
                            Advocate.tide.getIndex(),
                            charge,
                            tideEValue,
                            IoUtil.getFileName(tideTsvFile)
                    );

                    // add the raw score
                    peptideAssumption.setRawScore(rawScore);

                    if (expandAaCombinations && AminoAcidSequence.hasCombination(unmodifiedPeptideSequence)) {

                        ModificationMatch[] previousModificationMatches = peptide.getVariableModifications();

                        for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {

                            ModificationMatch[] newModificationMatches = Arrays.stream(previousModificationMatches)
                                    .map(modificationMatch -> modificationMatch.clone())
                                    .toArray(ModificationMatch[]::new);

                            Peptide newPeptide = new Peptide(expandedSequence.toString(), newModificationMatches, true);
                            PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(), peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(), peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                            newAssumption.setRawScore(rawScore);
                            currentMatch.addPeptideAssumption(Advocate.tide.getIndex(), newAssumption);
                        }
                    } else {
                        //peptideAssumption.addUrParam(scoreParam);
                        currentMatch.addPeptideAssumption(Advocate.tide.getIndex(), peptideAssumption);
                    }
                }
            }

            // iterate the matches and add to the results
            Iterator<Long> iterator = tempSpectrumMatchesMap.keySet().iterator();

            while (iterator.hasNext()) {
                result.add(tempSpectrumMatchesMap.get(iterator.next()));
            }
        }

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

    /**
     * Returns the spectrum file name.This method assumes that the PepNovo
     * output file is the mgf file name + "tide-search.target.txt"
     *
     * @param fileName the name of the results file
     *
     * @return the spectrum file name
     */
    public static String getMgfFileName(String fileName) {

        if (fileName.endsWith(".tide-search.target.txt.gz")) {

            return fileName.substring(0, fileName.length() - 26) + ".mgf";

        } else if (fileName.endsWith(".tide-search.target.txt")) {

            return fileName.substring(0, fileName.length() - 23) + ".mgf";

        } else {

            throw new IllegalArgumentException("Unexpected file extension. Expected: tide-search.target.txt or tide-search.target.txt.gz. File name: " + fileName + ".");

        }
    }
}
