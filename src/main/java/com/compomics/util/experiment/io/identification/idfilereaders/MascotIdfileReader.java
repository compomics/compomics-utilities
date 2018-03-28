package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import javax.xml.bind.JAXBException;

/**
 *
 * @author dominik.kopczynski
 */
public class MascotIdfileReader extends ExperimentObject implements IdfileReader {

    private ArrayList<String> varMods = new ArrayList<>();
    private String softwareVersion;

    private ArrayList<Integer> charges = new ArrayList<>();
    private ArrayList<Integer> matches = new ArrayList<>();
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
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public MascotIdfileReader(File inputFile) throws FileNotFoundException, IOException {
        this(inputFile, null);
    }

    /**
     * Constructor for an Mascot dat csv result file reader.
     *
     * @param inputFile the Mascot dat file
     * @param waitingHandler the waiting handler
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public MascotIdfileReader(File inputFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        varMods.add("dummy");
        charges.add(-100000);
        matches.add(-1);

        try {
            FileReader fr = new FileReader(inputFile);
            BufferedReader in = new BufferedReader(fr, 1 << 24);
            String line, boundary;

            // read first line
            line = in.readLine();

            // second line should contain boundary information
            line = in.readLine();
            int boundaryStart = line.indexOf("boundary=");
            if (boundaryStart >= 0) {
                boundary = line.substring(line.indexOf("=", boundaryStart) + 1);
            } else {
                throw new Exception("File format not parsable, no boundary provided.");
            }

            // find first new file occurence
            while ((line = in.readLine()) != null) {
                if (line.length() > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                    break;
                }
            }
            int xx = 0;
            while ((line = in.readLine()) != null) {
                int nameIndex = line.indexOf("name=\"");
                if (nameIndex < 0) {
                    throw new Exception("File format not parsable.");
                }
                String state = line.substring(line.indexOf("=\"", nameIndex) + 2, line.indexOf("\"", nameIndex + 6));
                if (state.startsWith("query")) {
                    parseQuery(in, boundary, state);
                } else {
                    switch (state) {
                        case "masses":
                            parseMasses(in, boundary);
                            break;
                        case "peptides":
                            parsePeptides(in, boundary, inputFile.getName());
                            break;
                        case "summary":
                            parseSummary(in, boundary);
                            break;

                        case "parameters":
                            parseParameters(in, boundary);
                            break;

                        case "header":
                            parseHeader(in, boundary);
                            break;

                        case "index":
                        case "enzyme":
                        case "unimod":
                        case "proteins":
                            parse(in, boundary);
                            break;

                        default:
                            throw new Exception("File format not parsable name '" + state + "'.");
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getExtension() {
        return ".dat";
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, com.compomics.util.parameters.identification.search.SearchParameters searchParameters) throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
        return getAllSpectrumMatches(waitingHandler, searchParameters, null, false);
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, com.compomics.util.parameters.identification.search.SearchParameters searchParameters,
            SequenceMatchingParameters sequenceMatchingPreferences, boolean expandAaCombinations)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

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

                        }
                    }
                });
            }
        }

        return new LinkedList<>(allMatches.values());
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

    private void parseMasses(BufferedReader in, String boundary) {
        String mass = "";
        String line;

        int theCase = 0; // 1 = fix

        try {

            while ((line = in.readLine()) != null) {
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
                            throw new Exception("File format not parsable.");
                        }
                        theCase = 0;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseQuery(BufferedReader in, String boundary, String state) {
        String line;
        try {
            while ((line = in.readLine()) != null) {
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
                    String specTitle = URLDecoder.decode(parts[1], "utf8");
                    SpectrumMatch spectrumMatch = allMatches.get(specNum);

                    if (spectrumMatch != null) {
                        
                        String spectrumKey = Spectrum.getSpectrumKey(spectrumMatch.getSpectrumKey(), specTitle);
                        spectrumMatch.setSpectrumKey(spectrumKey);
                        
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseHeader(BufferedReader in, String boundary) {
        String line;
        try {
            while ((line = in.readLine()) != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseParameters(BufferedReader in, String boundary) {
        String line;
        try {
            while ((line = in.readLine()) != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsePeptides(BufferedReader in, String boundary, String sourceFile) throws Exception {
        String line;

        if (fileName == null) {
            throw new Exception("File format not parsable.");
        }

        try {
            while ((line = in.readLine()) != null) {
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
                SpectrumMatch currentMatch;
                int rank;
                if (!allMatches.containsKey(spectrumNumber)) {
                    currentMatch = new SpectrumMatch(fileName);
                    currentMatch.setSpectrumNumber(spectrumNumber);
                    allMatches.put(spectrumNumber, currentMatch);
                    rank = 1;
                } else {
                    
                    currentMatch = allMatches.get(spectrumNumber);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseSummary(BufferedReader in, String boundary) {
        String line;
        try {
            while ((line = in.readLine()) != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parse(BufferedReader in, String boundary) {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                int lineLength = line.length();
                if (lineLength > 2 && line.substring(0, 2).equals("--") && line.substring(2).equals(boundary)) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
