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
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
        return ".ms-amanda.csv";
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
        return getAllSpectrumMatches(waitingHandler, searchParameters, null, true);
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters,
            SequenceMatchingParameters sequenceMatchingPreferences, boolean expandAaCombinations)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        LinkedList<SpectrumMatch> result = new LinkedList<>();

        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(msAmandaCsvFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
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
                double msAmandaRawScore = Util.readDoubleAsString(scoreAsText);
                double msAmandaTransformedScore;

                // get the ms amanda e-value
                if (amandaWeightedProbabilityIndex != -1) {
                    String eVaulueAsText = elements[amandaWeightedProbabilityIndex];
                    msAmandaTransformedScore = Util.readDoubleAsString(eVaulueAsText);
                } else {
                    msAmandaTransformedScore = Math.pow(10, -msAmandaRawScore); // convert ms amanda score to e-value like
                }

                int rank = Integer.valueOf(elements[rankIndex]);
                //String mzAsText = elements[mzIndex]; // not currently used
                //double mz = Util.readDoubleAsString(mzAsText);
                int charge = Integer.valueOf(elements[chargeIndex]);
                //String rtAsText = elements[rtIndex]; // not currently used, and not mandatory, as old csv files didn't have this one...
                //double rt = Util.readDoubleAsString(rtAsText); // @TODO: have to escape retention times such as PT2700.460000S
                String fileName = elements[filenameIndex];

                // remove any html from the title
                spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8");

                // set up the yet empty spectrum match, or add to the current match
                if (currentMatch == null || (currentSpectrumTitle != null && !currentSpectrumTitle.equalsIgnoreCase(spectrumTitle))) {

                    // add the previous match, if any
                    if (currentMatch != null) {
                        result.add(currentMatch);
                    }

                    String spectrumKey = Spectrum.getSpectrumKey(fileName, spectrumTitle);
                    currentMatch = new SpectrumMatch(spectrumKey);
                    currentSpectrumTitle = spectrumTitle;
                }

                // get the modifications
                ArrayList<ModificationMatch> utilitiesModifications = new ArrayList<>(1);

                if (!modifications.isEmpty()) {

                    String[] modificationsString = modifications.split(";");
                    for (int i = 0; i < modificationsString.length; i++) {

                        String modificationString = modificationsString[i];

                        try {
                            // we expect something like this:
                            // N-Term(acetylation of protein n-term|42.010565|variable) or
                            // C4(carbamidomethyl c|57.021464|fixed)

                            String location = modificationString.substring(0, modificationString.indexOf("("));
                            int modSite;

                            if (location.equalsIgnoreCase("N-Term")) {
                                modSite = 1;
                            } else if (location.equalsIgnoreCase("C-Term")) {
                                modSite = peptideSequence.length();
                            } else {
                                // amino acid type and index expected, e.g., C4 or M3
                                modSite = Integer.parseInt(modificationString.substring(1, modificationString.indexOf("(")));
                            }

                            String rest = modificationString.substring(modificationString.indexOf("(") + 1, modificationString.length() - 1).toLowerCase();

                            String[] details = rest.split("\\|");
                            String modName = details[0]; // not currently used
                            String modMassAsString = details[1];
                            double modMass = Util.readDoubleAsString(modMassAsString);
                            String modFixedStatus = details[2];

                            if (modFixedStatus.equalsIgnoreCase("variable")) {

                                utilitiesModifications.add(new ModificationMatch(modMass + "@" + peptideSequence.charAt(modSite - 1), modSite));

                            }

                        } catch (Exception e) {
                            throw new IllegalArgumentException("Error parsing modification: " + modificationString + ".");
                        }
                    }
                }

                // create the peptide
                Peptide peptide = new Peptide(peptideSequence, utilitiesModifications.toArray(new ModificationMatch[utilitiesModifications.size()]), true);

                // create the peptide assumption
                PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, Advocate.msAmanda.getIndex(), charge, msAmandaTransformedScore, Util.getFileName(msAmandaCsvFile));
                peptideAssumption.setRawScore(msAmandaRawScore);

                if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideSequence)) {

                    ModificationMatch[] previousModificationMatches = peptide.getVariableModifications();

                    for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {

                        ModificationMatch[] newModificationMatches = Arrays.stream(previousModificationMatches)
                                .map(modificationMatch -> modificationMatch.clone())
                                .toArray(ModificationMatch[]::new);

                        Peptide newPeptide = new Peptide(expandedSequence.toString(), newModificationMatches, true);

                        PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(), peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(), peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                        newAssumption.setRawScore(msAmandaRawScore);
                        currentMatch.addPeptideAssumption(Advocate.msAmanda.getIndex(), newAssumption);

                    }

                } else {

                    currentMatch.addPeptideAssumption(Advocate.msAmanda.getIndex(), peptideAssumption);

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
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> versions = new ArrayList<>();
        versions.add(softwareVersion);
        result.put(softwareName, versions);
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }
}
