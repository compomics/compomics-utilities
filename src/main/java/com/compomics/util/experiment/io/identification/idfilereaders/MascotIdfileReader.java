package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.xml.bind.JAXBException;

/**
 * This IdfileReader reads identifications from a Mascot results file.
 *
 * @author Dominik Kopczynski
 * @author Marc Vaudel
 */
public class MascotIdfileReader implements IdfileReader {

    /**
     * The searched variable modifications.
     */
    private ArrayList<String> varMods = new ArrayList<>();
    /**
     * The software version.
     */
    private String softwareVersion;
    /**
     * The charges of the spectra.
     */
    private ArrayList<Integer> charges = new ArrayList<>();
    /**
     * The qmatch field.
     */
    private ArrayList<Integer> matches = new ArrayList<>();
    /**
     * The spectrum file name.
     */
    private String fileName = null;

    private HashMap<Integer, SpectrumMatch> allMatches = new HashMap<>();

    /**
     * Default constructor for the purpose of instantiation.
     */
    public MascotIdfileReader() {
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
            int xx = 0;
            while ((line = reader.readLine()) != null) {
                int nameIndex = line.indexOf("name=\"");
                if (nameIndex < 0) {
                    throw new IllegalArgumentException("File format not parsable.");
                }
                String state = line.substring(line.indexOf("=\"", nameIndex) + 2, line.indexOf("\"", nameIndex + 6));
                if (state.startsWith("query")) {
                    parseQuery(reader, boundary, state);
                } else {
                    switch (state) {
                        case "masses":
                            parseMasses(reader, boundary);
                            break;
                        case "peptides":
                            parsePeptides(reader, boundary, inputFile.getName());
                            break;
                        case "summary":
                            parseSummary(reader, boundary);
                            break;

                        case "parameters":
                            parseParameters(reader, boundary);
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
    }

    @Override
    public String getExtension() {
        return ".dat";
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
                false
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
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressCounter(allMatches.size());
        }

        if (expandAaCombinations) {

            for (SpectrumMatch currentMatch : allMatches.values()) {

                currentMatch.getAllPeptideAssumptions().forEach(currentAssumption -> {

                    Peptide peptide = currentAssumption.getPeptide();
                    String peptideSequence = peptide.getSequence();
                    ModificationMatch[] previousModificationMatches = peptide.getVariableModifications();

                    if (AminoAcidSequence.hasCombination(peptideSequence)) {

                        for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {

                            String newSequence = expandedSequence.toString();
                            if (newSequence.equals(peptideSequence)) {
                                continue;
                            }

                            ModificationMatch[] newModificationMatches = Arrays.stream(previousModificationMatches)
                                    .map(modificationMatch -> modificationMatch.clone())
                                    .toArray(ModificationMatch[]::new);

                            Peptide newPeptide = new Peptide(newSequence, newModificationMatches);

                            PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, currentAssumption.getRank(), currentAssumption.getAdvocate(), currentAssumption.getIdentificationCharge(), currentAssumption.getScore(), currentAssumption.getIdentificationFile());
                            currentMatch.addPeptideAssumption(Advocate.mascot.getIndex(), newAssumption);

                            if (waitingHandler != null) {
                                if (waitingHandler.isRunCanceled()) {
                                    break;
                                }
                                waitingHandler.increaseSecondaryProgressCounter();
                            }
                        }
                    }
                });
            }
        }

        return new ArrayList<>(allMatches.values());
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {

        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> versions = new ArrayList<>();
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
     */
    private void parseMasses(
            SimpleFileReader reader,
            String boundary
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
     *
     * @throws UnsupportedEncodingException Exception thrown if an error
     * occurred when decoding a spectrum title.
     */
    private void parseQuery(
            SimpleFileReader reader,
            String boundary,
            String state
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
                String spectrumTitle = URLDecoder.decode(parts[1], "utf8");
                SpectrumMatch spectrumMatch = allMatches.get(specNum);

                if (spectrumMatch != null) {

                    spectrumMatch.setSpectrumTitle(spectrumTitle);

                }
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
     */
    private void parseParameters(
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

            if ("FILE".equals(parts[0])) {
                File f = new File(parts[1].replaceAll("\\\\", "/"));
                fileName = f.getName();
            }
        }
    }

    /**
     * Parse peptides.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     * @param sourceFile the source file
     */
    private void parsePeptides(
            SimpleFileReader reader,
            String boundary,
            String sourceFile
    ) {
        String line;

        if (fileName == null) {
            throw new IllegalArgumentException("File format not parsable.");
        }

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
            int rank;

            SpectrumMatch currentMatch = allMatches.get(spectrumNumber);

            if (currentMatch == null) {

                currentMatch = new SpectrumMatch(fileName, Integer.toString(spectrumNumber));
                allMatches.put(spectrumNumber, currentMatch);
                rank = 1;

            } else {

                TreeMap<Double, ArrayList<PeptideAssumption>> assump = allMatches.get(spectrumNumber).getAllPeptideAssumptions(Advocate.mascot.getIndex());

                if (assump.containsKey(expectancy)) {

                    rank = assump.get(expectancy).get(0).getRank();

                } else {

                    rank = (int) allMatches.get(spectrumNumber).getAllPeptideAssumptions().count() + 1;

                }
            }

            Peptide peptide = new Peptide(peptideSequence, foundModifications.toArray(new ModificationMatch[foundModifications.size()]));
            PeptideAssumption currentAssumption = new PeptideAssumption(peptide, rank, Advocate.mascot.getIndex(), specCharge, expectancy, sourceFile);
            currentAssumption.setRawScore(ionScore);
            currentMatch.addPeptideAssumption(Advocate.mascot.getIndex(), currentAssumption);

        }
    }

    /**
     * Prase the summary.
     *
     * @param reader the buffered reader
     * @param boundary the boundary
     */
    private void parseSummary(
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
