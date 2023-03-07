package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;

/**
 * This IdfileReader reads identifications from a Mascot results file.
 *
 * @author Dominik Kopczynski
 * @author Marc Vaudel
 */
public class MascotIdfileReader implements IdfileReader {

    /**
     * The file to parse
     */
    private final File inputFile;
    /**
     * The number of queries in the file.
     */
    private final int nQueries;
    /**
     * The software version.
     */
    private String softwareVersion;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public MascotIdfileReader() {

        inputFile = null;
        nQueries = -1;

    }

    /**
     * Constructor for an Mascot dat result file reader.
     *
     * @param inputFile the Mascot dat file
     *
     * @throws UnsupportedEncodingException Exception thrown if an error
     * occurred when decoding a spectrum title.
     */
    public MascotIdfileReader(
            File inputFile
    ) throws UnsupportedEncodingException {
        this(inputFile, null);
    }

    /**
     * Constructor for an Mascot dat csv result file reader.
     *
     * @param inputFile the Mascot dat file
     * @param waitingHandler the waiting handler
     *
     * @throws UnsupportedEncodingException Exception thrown if an error
     * occurred when decoding a spectrum title.
     */
    public MascotIdfileReader(
            File inputFile,
            WaitingHandler waitingHandler
    ) throws UnsupportedEncodingException {

        this.inputFile = inputFile;

        int queryCount = 0;

        try ( SimpleFileReader reader = SimpleFileReader.getFileReader(inputFile)) {

            String line;

            while ((line = reader.readLine()) != null) {

                int nameIndex = line.indexOf("name=\"");

                if (nameIndex >= 0) {

                    String state = line.substring(line.indexOf("=\"", nameIndex) + 2, line.indexOf("\"", nameIndex + 6));

                    if (state.startsWith("query")) {

                        queryCount++;

                    }
                }
            }
        }

        nQueries = queryCount;

    }

    @Override
    public String getExtension() {
        return ".dat";
    }

    @Override
    public HashMap<String, HashMap<String, ArrayList<SpectrumIdentificationAssumption>>> getAllSpectrumMatches(
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
                false
        );
    }

    @Override
    public HashMap<String, HashMap<String, ArrayList<SpectrumIdentificationAssumption>>> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,
            WaitingHandler waitingHandler,
            SearchParameters searchParameters,
            SequenceMatchingParameters sequenceMatchingPreferences,
            boolean expandAaCombinations
    )
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(nQueries);
        }

        HashMap<String, HashMap<String, ArrayList<SpectrumIdentificationAssumption>>> results = new HashMap<>(1);

        // The searched variable modifications.
        ArrayList<String> varMods = new ArrayList<>();
        // The charges of the spectra
        ArrayList<Integer> charges = new ArrayList<>();
        // The qmatch field
        ArrayList<Integer> matches = new ArrayList<>();
        // The spectrum file name
        String fileName = null;
        // Query tot title map
        HashMap<Integer, String> titleMap = new HashMap<>();

        varMods.add("dummy");
        charges.add(-100000);
        matches.add(-1);

        try ( SimpleFileReader reader = SimpleFileReader.getFileReader(inputFile)) {

            String line, boundary;

            // read first line
            line = reader.readLine();

            // second line should contain boundary information
            line = reader.readLine();
            int boundaryStart = line.indexOf("boundary=");
            if (boundaryStart >= 0) {
                boundary = line.substring(line.indexOf("=", boundaryStart) + 1);
            } else {
                throw new IllegalArgumentException("File format not parsable, no boundary provided.");
            }

            // find first new file occurence
            while ((line = reader.readLine()) != null) {
                if (line.length() > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                    break;
                }
            }

            while ((line = reader.readLine()) != null) {

                int nameIndex = line.indexOf("name=\"");

                if (nameIndex < 0) {

                    throw new IllegalArgumentException("File format not parsable.");

                }

                String state = line.substring(line.indexOf("=\"", nameIndex) + 2, line.indexOf("\"", nameIndex + 6));

                if (state.startsWith("query")) {

                    parseQuery(reader, boundary, state, titleMap);

                    waitingHandler.increaseSecondaryProgressCounter();

                } else {
                    switch (state) {
                        case "masses":
                            parseMasses(reader, boundary, varMods);
                            break;
                        case "peptides":
                            HashMap<String, ArrayList<SpectrumIdentificationAssumption>> fileResults = results.get(fileName);

                            if (fileResults == null) {

                                fileResults = new HashMap<>();
                                results.put(fileName, fileResults);

                            }

                            parsePeptides(
                                    reader,
                                    boundary,
                                    inputFile.getName(),
                                    fileName,
                                    charges,
                                    matches,
                                    varMods,
                                    titleMap,
                                    fileResults,
                                    expandAaCombinations
                            );
                            break;
                        case "summary":
                            parseSummary(reader, boundary, charges, matches);
                            break;

                        case "parameters":
                            fileName = parseParameters(reader, boundary);
                            break;

                        case "header":
                            parseHeader(reader, boundary);
                            break;

                        case "index":
                        case "enzyme":
                        case "unimod":
                        case "proteins":
                            parse(reader, boundary);
                            break;

                        default:
                            throw new IllegalArgumentException("File format not parsable name '" + state + "'.");
                    }
                }

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    break;
                }
            }
        }

        return results;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {

        HashMap<String, ArrayList<String>> result = new HashMap<>(1);
        ArrayList<String> versions = new ArrayList<>(1);
        versions.add(softwareVersion);
        result.put("Mascot", versions);
        return result;

    }

    @Override
    public boolean hasDeNovoTags() {

        return false;

    }

    /**
     * Parse the masses.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     * @param varMods the variable modifications
     */
    private void parseMasses(
            SimpleFileReader reader,
            String boundary,
            ArrayList<String> varMods
    ) {

        String mass = "";
        String line;

        int theCase = 0; // 1 = fix

        while ((line = reader.readLine()) != null) {
            if (line.length() > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                break;
            }
            if (line.length() < 2) {
                continue;
            }

            String[] parts = line.split("=");
            if (parts.length != 2) {
                continue;
            }
            switch (theCase) {
                case 0:
                    if (parts[0].startsWith("delta")) {
                        varMods.add(parts[1].split(",")[0]);
                    } else if (parts[0].startsWith("FixedMod")) {
                        mass = parts[1].split(",")[0];
                        theCase = 1;
                    }
                    break;

                case 1:
                    if (!parts[0].startsWith("FixedModResidues")) {
                        throw new IllegalArgumentException("File format not parsable.");
                    }
                    theCase = 0;
                    break;
            }
        }
    }

    /**
     * Parse a query.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     * @param state the state
     * @param titleMap the query to title map
     *
     * @throws UnsupportedEncodingException Exception thrown if an error
     * occurred when decoding a spectrum title.
     */
    private void parseQuery(
            SimpleFileReader reader,
            String boundary,
            String state,
            HashMap<Integer, String> titleMap
    ) throws UnsupportedEncodingException {

        String line;
        while ((line = reader.readLine()) != null) {

            if (line.length() > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                break;
            }
            if (line.length() < 2) {
                continue;
            }

            String[] parts = line.split("=");
            if (parts.length != 2) {
                continue;
            }
            if ("title".equals(parts[0])) {

                int specNum = Integer.parseInt(state.substring(5, state.length()));
                String spectrumTitle = URLDecoder.decode(parts[1].trim(), "utf8");

                titleMap.put(specNum, spectrumTitle);

            }
        }
    }

    /**
     * Parse a header.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     */
    private void parseHeader(
            SimpleFileReader reader,
            String boundary
    ) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                break;
            }
            if (line.length() < 2) {
                continue;
            }

            String[] parts = line.split("=");
            if (parts.length != 2) {
                continue;
            }
            if ("version".equals(parts[0])) {
                softwareVersion = parts[1];
            }
        }
    }

    /**
     * Parse parameters.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     *
     * @return Returns the file name.
     */
    private String parseParameters(
            SimpleFileReader reader,
            String boundary
    ) {

        String fileName = null;

        String line;
        while ((line = reader.readLine()) != null) {

            if (line.length() > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {

                break;

            }

            if (line.length() < 2) {

                continue;

            }

            String[] parts = line.split("=");

            if (parts.length != 2) {

                continue;

            }

            if ("FILE".equals(parts[0])) {

                File f = new File(parts[1].replaceAll("\\\\", "/"));
                fileName = f.getName();

            }
        }

        if (fileName == null) {

            throw new IllegalArgumentException("File name not found.");

        }

        return fileName;

    }

    /**
     * Parse peptides.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     * @param sourceFile the source file
     * @param fileName the file name
     */
    private void parsePeptides(
            SimpleFileReader reader,
            String boundary,
            String sourceFile,
            String fileName,
            ArrayList<Integer> charges,
            ArrayList<Integer> matches,
            ArrayList<String> varMods,
            HashMap<Integer, String> titleMap,
            HashMap<String, ArrayList<SpectrumIdentificationAssumption>> fileResults,
            boolean expandAaCombinations
    ) {

        if (fileName == null) {
            throw new IllegalArgumentException("File name not found.");
        }

        int rank = 0;
        double currentScore = Double.NaN;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                break;
            }
            if (line.length() < 2) {
                continue;
            }

            String[] parts = line.split("=");
            if (parts.length < 2) {
                continue;
            }
            String[] pre = parts[0].split("_");
            if (pre.length != 2) {
                continue;
            }

            int spectrumNumber = Integer.parseInt(pre[0].substring(1, pre[0].length()));

            String[] content = parts[1].split(";")[0].split(",");
            if (content.length != 11) {
                continue;
            }
            String peptideSequence = content[4];
            String varModSequence = content[6];
            ArrayList<ModificationMatch> foundModifications = new ArrayList<>(1);

            // check for variable modifications
            for (int pos = 1; pos < varModSequence.length() - 1; ++pos) {
                char vC = varModSequence.charAt(pos);
                if (vC != '0' && vC != 'X') {
                    foundModifications.add(new ModificationMatch(varMods.get(vC - '0') + "@" + peptideSequence.charAt(pos - 1), pos));
                }
            }

            double ionScore = Double.parseDouble(content[7]);
            int specCharge = charges.get(spectrumNumber);
            double lThreshold = 10.0 * Math.log(matches.get(spectrumNumber)) / Math.log(10);
            double expectancy = (0.05 * Math.pow(10, ((lThreshold - (double) ionScore) / 10)));

            if (Double.isNaN(currentScore) || expectancy != currentScore) {

                rank++; // Note: this assumes that peptides are sorted by expectancy in the file.
                currentScore = expectancy;

            }

            String spectrumTitle = titleMap.get(spectrumNumber);

            ArrayList<SpectrumIdentificationAssumption> spectrumResults = fileResults.get(spectrumTitle);

            if (spectrumResults == null) {

                spectrumResults = new ArrayList<>(4);
                fileResults.put(spectrumTitle, spectrumResults);

            }

            Peptide peptide = new Peptide(peptideSequence, foundModifications.toArray(new ModificationMatch[0]));

            if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideSequence)) {

                for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {

                    String newSequence = expandedSequence.toString();

                    ModificationMatch[] newModificationMatches = foundModifications.stream()
                            .map(modificationMatch -> modificationMatch.clone())
                            .toArray(ModificationMatch[]::new);

                    Peptide newPeptide = new Peptide(newSequence, newModificationMatches);

                    PeptideAssumption newAssumption = new PeptideAssumption(
                            newPeptide,
                            rank,
                            Advocate.mascot.getIndex(),
                            specCharge,
                            ionScore,
                            expectancy,
                            sourceFile
                    );

                    spectrumResults.add(newAssumption);

                }

            } else {

                PeptideAssumption currentAssumption = new PeptideAssumption(
                        peptide,
                        rank,
                        Advocate.mascot.getIndex(),
                        specCharge,
                        ionScore,
                        expectancy,
                        sourceFile
                );

                spectrumResults.add(currentAssumption);

            }
        }
    }

    /**
     * Prase the summary.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     * @param charges the charges found in the qexp field
     * @param matches the qmatch field
     */
    private void parseSummary(
            SimpleFileReader reader,
            String boundary,
            ArrayList<Integer> charges,
            ArrayList<Integer> matches
    ) {

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                break;
            }
            if (line.length() < 2) {
                continue;
            }

            String[] parts = line.split("=");
            if (parts[0].startsWith("qexp")) {
                int sign = parts[1].charAt(parts[1].length() - 1) == '+' ? 1 : -1;
                String chrg = parts[1].split(",")[1];
                chrg = chrg.substring(0, chrg.length() - 1);
                charges.add(sign * Integer.parseInt(chrg));
            } else if (parts[0].startsWith("qmatch")) {
                matches.add(Integer.parseInt(parts[1]));
            }
        }
    }

    /**
     * Parse.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     */
    private void parse(
            SimpleFileReader reader,
            String boundary
    ) {

        String line;
        while ((line = reader.readLine()) != null) {
            int lineLength = line.length();
            if (lineLength > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                break;
            }
        }
    }
}
