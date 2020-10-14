package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.NovorParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.bind.JAXBException;

/**
 * This IdfileReader reads identifications from a Novor csv result file.
 *
 * @author Harald Barsnes
 */
public class NovorIdfileReader implements IdfileReader {

    /**
     * The software name.
     */
    private final String softwareName = "Novor";
    /**
     * The softwareVersion.
     */
    private String softwareVersion = null;
    /**
     * The Novor csv file.
     */
    private File novorCsvFile;
    /**
     * The modification factory.
     */
    private final ModificationFactory modificationFactory = ModificationFactory.getInstance();

    /**
     * Default constructor for the purpose of instantiation.
     */
    public NovorIdfileReader() {
    }

    /**
     * Constructor for an Novor csv result file reader.
     *
     * @param novorCsvFile the Novor csv file
     *
     * @throws IOException if an IOException occurs
     */
    public NovorIdfileReader(
            File novorCsvFile
    ) throws IOException {
        this(novorCsvFile, null);
    }

    /**
     * Constructor for an Novor csv result file reader.
     *
     * @param novorCsvFile the Novor csv file
     * @param waitingHandler the waiting handler
     *
     * @throws IOException if an IOException occurs
     */
    public NovorIdfileReader(
            File novorCsvFile,
            WaitingHandler waitingHandler
    ) throws IOException {

        this.novorCsvFile = novorCsvFile;

        // get the novor version number
        extractVersionNumber();
    }

    /**
     * Extracts the Novor version number.
     */
    private void extractVersionNumber() throws IOException {

        try (SimpleFileReader reader = SimpleFileReader.getFileReader(novorCsvFile)) {

            String line = reader.readLine();
            boolean versionNumberFound = false;
            String versionNumberString = null;

            while (line.startsWith("#") && !versionNumberFound) {
                if (line.contains(" v")) {
                    versionNumberString = line;
                    versionNumberString = versionNumberString.substring(1);
                    versionNumberString = versionNumberString.trim();
                    versionNumberFound = true;
                }
                line = reader.readLine();
            }

            if (versionNumberFound) {
                softwareVersion = versionNumberString.trim();
            }
        }
    }

    @Override
    public String getExtension() {
        return ".novor.csv";
    }

    @Override
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,
            WaitingHandler waitingHandler,
            SearchParameters searchParameters
    )
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

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
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

//        int tagMapKeyLength = 0;
//        if (sequenceMatchingPreferences != null) {
//            SequenceFactory sequenceFactory = SequenceFactory.getInstance();
//            tagMapKeyLength = sequenceFactory.getDefaultProteinTree().getInitialTagSize();
//            tagsMap = new HashMap<String, ArrayList<SpectrumMatch>>(1024);
//        }
        NovorParameters novorParameters = (NovorParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.novor.getIndex());

        ArrayList<SpectrumMatch> result = new ArrayList<>();

        try (SimpleFileReader reader = SimpleFileReader.getFileReader(novorCsvFile)) {

            String inputFile = null;
            String fixedModificationsLine = null;
            String variableModificationsLine = null;

            // read until we find the header line
            String line;
            while ((line = reader.readLine()) != null && !line.startsWith("# id,")) {
                if (line.startsWith("# input file = ")) {
                    inputFile = line.substring("# input file = ".length()).trim();
                }
                if (line.startsWith("# fixedModifications = ")) {
                    fixedModificationsLine = line.substring("# fixedModifications = ".length()).trim();
                }
                if (line.startsWith("# variableModifications = ")) {
                    variableModificationsLine = line.substring("# variableModifications = ".length()).trim();
                }
            }

            if (inputFile == null) {
                throw new IllegalArgumentException("Mandatory header information is missing in the Novor csv file (the input file tag). Please check the file!");
            }
            if (fixedModificationsLine == null) {
                throw new IllegalArgumentException("Mandatory header information is missing in the Novor csv file (the fixedModifications tag). Please check the file!");
            }
            if (variableModificationsLine == null) {
                throw new IllegalArgumentException("Mandatory header information is missing in the Novor csv file (the variableModifications tag). Please check the file!");
            }

            // get the spectrum file name
            String spectrumFileName = IoUtil.getFileName(inputFile);

            // get the variable modifications
            HashMap<Integer, String> variableModificationsMap = new HashMap<>();
            if (!variableModificationsLine.isEmpty()) {
                String[] tempVariable = variableModificationsLine.split(", ");
                for (int i = 0; i < tempVariable.length; i++) {
                    variableModificationsMap.put(i, tempVariable[i]);
                }
            }

            // get the fixed modifications
            HashMap<Integer, String> fixedModificationsMap = new HashMap<>();
            if (!fixedModificationsLine.isEmpty()) {
                String[] tempFixed = fixedModificationsLine.split(", ");
                for (int i = 0; i < tempFixed.length; i++) {
                    fixedModificationsMap.put(variableModificationsMap.size() + i, tempFixed[i]);
                }
            }

            String headerString = line.substring(1).trim();
            if (headerString.endsWith(",")) {
                headerString = headerString.substring(0, headerString.length() - 1);
            }

            // parse the header line
            String[] headers = headerString.split(", ");
            int idIndex = -1, scanNumberIndex = -1, rtIndex = -1, mzIndex = -1, chargeIndex = -1, pepMassIndex = -1,
                    erorrIndex = -1, ppmIndex = -1, scoreIndex = -1, peptideIndex = -1, aaScoreIndex = -1;

            // get the column index of the headers
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i];

                if (header.equalsIgnoreCase("id")) {
                    idIndex = i;
                } else if (header.equalsIgnoreCase("scanNum")) {
                    scanNumberIndex = i;
                } else if (header.equalsIgnoreCase("RT")) {
                    rtIndex = i;
                } else if (header.equalsIgnoreCase("mz(data)")) {
                    mzIndex = i;
                } else if (header.equalsIgnoreCase("z")) {
                    chargeIndex = i;
                } else if (header.equalsIgnoreCase("pepMass(denovo)")) {
                    pepMassIndex = i;
                } else if (header.equalsIgnoreCase("err(data-denovo)")) {
                    erorrIndex = i;
                } else if (header.equalsIgnoreCase("ppm(1e6*err/(mz*z))")) {
                    ppmIndex = i;
                } else if (header.equalsIgnoreCase("score")) {
                    scoreIndex = i;
                } else if (header.equalsIgnoreCase("peptide")) {
                    peptideIndex = i;
                } else if (header.equalsIgnoreCase("aaScore")) {
                    aaScoreIndex = i;
                }
            }

            // check if all the required header are found
            if (idIndex == -1 || scanNumberIndex == -1 || rtIndex == -1 || mzIndex == -1 || chargeIndex == -1
                    || pepMassIndex == -1 || erorrIndex == -1 || ppmIndex == -1
                    || scoreIndex == -1 || peptideIndex == -1 || aaScoreIndex == -1) {
                throw new IllegalArgumentException("Mandatory columns are missing in the Novor csv file. Please check the file!");
            }

            String currentSpectrumTitle = null;
            SpectrumMatch currentMatch = null;

            // get the psms
            while ((line = reader.readLine()) != null) {

                String[] elements = line.split(", ");

                if (!line.trim().isEmpty()) { // @TODO: make this more robust?

                    int id = Integer.parseInt(elements[idIndex]);
                    int charge = Integer.parseInt(elements[chargeIndex]);
                    String peptideSequenceWithMods = elements[peptideIndex];

                    // get the novor score
                    String scoreAsText = elements[scoreIndex];
                    double novorScore = Util.readDoubleAsString(scoreAsText);

                    // get the novor e-value
                    //double novorEValue = Math.pow(10, -novorScore); // convert novor score to e-value // @TODO: is this correct?

                    // amino acids scores
                    String aminoAcidScoresAsString = elements[aaScoreIndex];
                    String[] tempAminoAcidScores = aminoAcidScoresAsString.split("-");
                    double[] aminoAcidScoresAsList = new double[tempAminoAcidScores.length];
                    for (int i = 0; i < tempAminoAcidScores.length; i++) {
                        aminoAcidScoresAsList[i] = Double.valueOf(tempAminoAcidScores[i]);
                    }
                    ArrayList<double[]> aminoAcidScores = new ArrayList<>(1);
                    aminoAcidScores.add(aminoAcidScoresAsList);

                    // get the name of the spectrum file
                    String spectrumTitle = spectrumProvider.getSpectrumTitles(IoUtil.removeExtension(spectrumFileName))[id - 1];

                    // set up the yet empty spectrum match, or add to the current match
                    if (currentMatch == null || (currentSpectrumTitle != null && !currentSpectrumTitle.equalsIgnoreCase(spectrumTitle))) {

                        // add the previous match, if any
                        if (currentMatch != null) {
                            result.add(currentMatch);
                        }

                        currentMatch = new SpectrumMatch(spectrumFileName, spectrumTitle);
                        currentSpectrumTitle = spectrumTitle;

                    }

                    // get the modifications
                    HashMap<Integer, ArrayList<String>> utilitiesModificationsMap = new HashMap();

                    String peptideSequence;

                    // extract the modifications
                    if (peptideSequenceWithMods.contains("(") || peptideSequenceWithMods.contains("[")) {

                        // example: (N-term|Acetyl)S(Phospho)EQUENCES(Phospho)(C-term|Amidated)
                        peptideSequence = "";

                        for (int i = 0; i < peptideSequenceWithMods.length(); i++) {

                            char currentChar = peptideSequenceWithMods.charAt(i);

                            if (currentChar == '(') {

                                int modStart = i + 1;
                                int modEnd = peptideSequenceWithMods.indexOf(")", i + 1);
                                String currentMod = peptideSequenceWithMods.substring(modStart, modEnd);

                                if (currentMod.toLowerCase().startsWith("n-term|")) {

                                    int currentModAsInt = Integer.valueOf(currentMod.substring("n-term|".length()));

                                    if (variableModificationsMap.containsKey(currentModAsInt)) {

                                        if (!utilitiesModificationsMap.containsKey(1)) {
                                            utilitiesModificationsMap.put(1, new ArrayList<>());
                                        }

                                        utilitiesModificationsMap.get(1).add(variableModificationsMap.get(currentModAsInt));

                                    } else if (novorParameters.getNovorPtmMap() == null) {
                                        throw new IllegalArgumentException("Unknown PTM! Please check the Novor results file.");
                                    }

                                } else if (currentMod.toLowerCase().startsWith("c-term|")) {

                                    int currentModAsInt = Integer.valueOf(currentMod.substring("c-term|".length()));

                                    if (variableModificationsMap.containsKey(currentModAsInt)) {

                                        if (!utilitiesModificationsMap.containsKey(peptideSequence.length())) {
                                            utilitiesModificationsMap.put(peptideSequence.length(), new ArrayList<>());
                                        }

                                        utilitiesModificationsMap.get(peptideSequence.length()).add(variableModificationsMap.get(currentModAsInt));

                                    } else if (novorParameters.getNovorPtmMap() == null) {
                                        throw new IllegalArgumentException("Unknown PTM! Please check the Novor results file.");
                                    }

                                } else {

                                    int currentModAsInt = Integer.valueOf(currentMod);

                                    if (variableModificationsMap.containsKey(currentModAsInt)) {

                                        if (!utilitiesModificationsMap.containsKey(peptideSequence.length())) {
                                            utilitiesModificationsMap.put(peptideSequence.length(), new ArrayList<>());
                                        }

                                        utilitiesModificationsMap.get(peptideSequence.length()).add(variableModificationsMap.get(currentModAsInt));

                                    } else if (novorParameters.getNovorPtmMap() == null) {
                                        throw new IllegalArgumentException("Unknown PTM! Please check the Novor results file.");
                                    }

                                }

                                i = modEnd;
                            } else {
                                peptideSequence += currentChar;
                            }
                        }
                    } else {
                        peptideSequence = peptideSequenceWithMods;
                    }

                    Tag tag = new Tag();

                    boolean hasAminoAcids = false, hasMassGaps = false;
                    int maxAminoAcidTagLength = 0;

                    String currentSequence = "";
                    Boolean lastIndexWasAminoAcid = null;

                    // convert peptide into tag
                    for (int i = 0; i < aminoAcidScoresAsList.length; i++) {

                        double aaScore = aminoAcidScoresAsList[i];

                        if (aaScore >= sequenceMatchingPreferences.getMinAminoAcidScore()) {
                            hasAminoAcids = true;
                        }

                        if (aaScore < sequenceMatchingPreferences.getMinAminoAcidScore()) {
                            hasMassGaps = true;
                        }

                        if (lastIndexWasAminoAcid == null
                                || (aaScore >= sequenceMatchingPreferences.getMinAminoAcidScore() && lastIndexWasAminoAcid)
                                || (aaScore < sequenceMatchingPreferences.getMinAminoAcidScore() && !lastIndexWasAminoAcid)) {

                            currentSequence += peptideSequence.charAt(i);

                        } else {

                            if (lastIndexWasAminoAcid) {

                                boolean isModified = false;

                                ArrayList<ModificationMatch> modMatches = new ArrayList<>();

                                for (int j = i - currentSequence.length(); j < i; j++) {

                                    if (utilitiesModificationsMap.containsKey(j + 1)) {

                                        for (String tempMod : utilitiesModificationsMap.get(j + 1)) {
                                            modMatches.add(new ModificationMatch(tempMod, j + 1 - (i - currentSequence.length())));
                                        }

                                        isModified = true;
                                    }
                                }

                                if (isModified) {
                                    ModificationMatch[] tempList = modMatches.toArray(new ModificationMatch[modMatches.size()]);
                                    tag.addAminoAcidSequence(new AminoAcidSequence(currentSequence, tempList));
                                } else {
                                    tag.addAminoAcidSequence(new AminoAcidSequence(currentSequence));
                                }

                                if (currentSequence.length() > maxAminoAcidTagLength) {
                                    maxAminoAcidTagLength = currentSequence.length();
                                }

                                currentSequence = "" + peptideSequence.charAt(i);

                            } else {

                                double modMass = 0.0;

                                for (int j = i - currentSequence.length(); j < i; j++) {

                                    if (utilitiesModificationsMap.containsKey(j + 1)) {

                                        for (String tempMod : utilitiesModificationsMap.get(j + 1)) {
                                            modMass += modificationFactory.getModification(tempMod).getMass();
                                        }

                                    }
                                }

                                tag.addMassGap(new AminoAcidSequence(currentSequence).getMass() + modMass);
                                currentSequence = "" + peptideSequence.charAt(i);
                            }

                        }

                        lastIndexWasAminoAcid = aaScore >= sequenceMatchingPreferences.getMinAminoAcidScore();

                    }

                    if (!currentSequence.isEmpty()) {
                        
                        if (lastIndexWasAminoAcid) {

                            boolean isModified = false;

                            ArrayList<ModificationMatch> modMatches = new ArrayList<>();

                            for (int i = 0; i < currentSequence.length(); i++) {

                                if (utilitiesModificationsMap.containsKey(i + 1)) {

                                    for (String tempMod : utilitiesModificationsMap.get(i + 1)) {
                                        modMatches.add(new ModificationMatch(tempMod, i + 1));
                                    }

                                    isModified = true;
                                }
                            }

                            if (isModified) {
                                ModificationMatch[] tempList = modMatches.toArray(new ModificationMatch[modMatches.size()]);
                                tag.addAminoAcidSequence(new AminoAcidSequence(currentSequence, tempList));
                            } else {
                                tag.addAminoAcidSequence(new AminoAcidSequence(currentSequence));
                            }

                            if (currentSequence.length() > maxAminoAcidTagLength) {
                                maxAminoAcidTagLength = currentSequence.length();
                            }

                            currentSequence = "";
                        } else {

                            double modMass = 0.0;

                            for (int i = 0; i < currentSequence.length(); i++) {

                                if (utilitiesModificationsMap.containsKey(i + 1)) {

                                    for (String tempMod : utilitiesModificationsMap.get(i + 1)) {
                                        modMass += modificationFactory.getModification(tempMod).getMass();
                                    }

                                }
                            }

                            tag.addMassGap(new AminoAcidSequence(currentSequence).getMass() + modMass);

                            currentSequence = "";
                        }
                    }

                    if (hasAminoAcids && hasMassGaps && maxAminoAcidTagLength >= sequenceMatchingPreferences.getMinTagLength()) {

                        TagAssumption tagAssumption = new TagAssumption(Advocate.novor.getIndex(), 1, tag, charge, novorScore);
                        currentMatch.addTagAssumption(Advocate.novor.getIndex(), tagAssumption);

                    } else {

                        // convert the data structure of the modifications
                        ArrayList<ModificationMatch> utilitiesModificationMatches = new ArrayList<>();
                        Iterator<Integer> iterator = utilitiesModificationsMap.keySet().iterator();

                        while (iterator.hasNext()) {

                            int target = iterator.next();

                            ArrayList<String> tempMods = utilitiesModificationsMap.get(target);

                            for (String tempMod : tempMods) {
                                utilitiesModificationMatches.add(new ModificationMatch(tempMod, target));
                            }

                        }

                        // create the peptide assumption
                        Peptide peptide = new Peptide(
                                peptideSequence,
                                utilitiesModificationMatches.toArray(new ModificationMatch[utilitiesModificationsMap.size()]),
                                true
                        );

                        PeptideAssumption peptideAssumption = new PeptideAssumption(
                                peptide,
                                1,
                                Advocate.novor.getIndex(),
                                charge,
                                novorScore,
                                novorCsvFile.getName()
                        );

                        peptideAssumption.setAminoAcidScores(aminoAcidScores);

                        if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideAssumption.getPeptide().getSequence())) {

                            ModificationMatch[] previousModificationMatches = peptide.getVariableModifications();

                            for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {

                                ModificationMatch[] newModificationMatches = Arrays.stream(previousModificationMatches)
                                        .map(modificationMatch -> modificationMatch.clone())
                                        .toArray(ModificationMatch[]::new);

                                Peptide newPeptide = new Peptide(expandedSequence.toString(), newModificationMatches, true);

                                PeptideAssumption newAssumption = new PeptideAssumption(
                                        newPeptide,
                                        peptideAssumption.getRank(),
                                        peptideAssumption.getAdvocate(),
                                        peptideAssumption.getIdentificationCharge(),
                                        peptideAssumption.getScore(),
                                        peptideAssumption.getIdentificationFile()
                                );

                                currentMatch.addPeptideAssumption(Advocate.novor.getIndex(), newAssumption);

                            }

                        } else {
                            currentMatch.addPeptideAssumption(Advocate.novor.getIndex(), peptideAssumption);
                        }
                    }
                }
            }

            // add the last match, if any
            if (currentMatch != null) {
                result.add(currentMatch);
            }

        }

        return result;
    }

    @Override
    public void close() throws IOException {
        novorCsvFile = null;
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
        return true;
    }
}
