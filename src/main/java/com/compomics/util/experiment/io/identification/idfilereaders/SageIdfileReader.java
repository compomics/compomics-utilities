package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;

/**
 * This IdfileReader reads identifications from a Sage tsv file.
 *
 * @author Harald Barsnes
 */
public class SageIdfileReader implements IdfileReader {

    /**
     * The software name.
     */
    private String softwareName = "Sage";
    /**
     * The softwareVersion.
     */
    private String softwareVersion = null;
    /**
     * The Sage tsv file.
     */
    private File sageTsvFile;
    /**
     * The modification factory.
     */
    private ModificationFactory modificationFactory = ModificationFactory.getInstance();
    /**
     * The mass tolerance to be used to match modifications from search engines
     * and expected modifications. 0.01 by default, the mass resolution in
     * X !Tandem result files.
     */
    public static final double MOD_MASS_TOLERANCE = 0.01;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public SageIdfileReader() {
    }

    /**
     * Constructor for a Sage tsv file reader.
     *
     * @param sageTsvFile the Sage tsv file
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public SageIdfileReader(
            File sageTsvFile
    ) throws IOException {
        this(sageTsvFile, null);
    }

    /**
     * Constructor for a Sage tsv file reader.
     *
     * @param sageTsvFile the Sage tsv file
     * @param waitingHandler the waiting handler
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public SageIdfileReader(
            File sageTsvFile,
            WaitingHandler waitingHandler
    ) throws IOException {

        this.sageTsvFile = sageTsvFile;

    }

    @Override
    public String getExtension() {
        return ".sage.tsv";
    }

    @Override
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,
            WaitingHandler waitingHandler,
            SearchParameters searchParameters
    )
            throws IOException,
            IllegalArgumentException,
            SQLException,
            ClassNotFoundException,
            InterruptedException,
            JAXBException {

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
            throws IOException,
            IllegalArgumentException,
            SQLException,
            ClassNotFoundException,
            InterruptedException,
            JAXBException {

        ArrayList<SpectrumMatch> result = new ArrayList<>();

        try ( SimpleFileReader reader = SimpleFileReader.getFileReader(sageTsvFile)) {

            String headerString = reader.readLine();

            // parse the header line
            String[] headers = headerString.split("\t");
            int peptideSequenceIndex = -1,
                    chargeIndex = -1,
                    posteriorErrorIndex = -1,
                    sageDiscriminantScore = -1,
                    rankIndex = -1,
                    spectrumTitleIndex = -1,
                    spectrumFileIndex = -1,
                    ms1_intensityIndex = -1;

            // get the column index of the headers
            for (int i = 0; i < headers.length; i++) {

                String header = headers[i];

                if (header.equalsIgnoreCase("peptide")) {
                    peptideSequenceIndex = i;
                } else if (header.equalsIgnoreCase("charge")) {
                    chargeIndex = i;
                } else if (header.equalsIgnoreCase("posterior_error")) {
                    posteriorErrorIndex = i;
                } else if (header.equalsIgnoreCase("sage_discriminant_score")) {
                    sageDiscriminantScore = i;
                } else if (header.equalsIgnoreCase("rank")) {
                    rankIndex = i;
                } else if (header.equalsIgnoreCase("scannr")) {
                    spectrumTitleIndex = i;
                } else if (header.equalsIgnoreCase("filename")) {
                    spectrumFileIndex = i;
                } else if (header.equalsIgnoreCase("ms1_intensity")) {
                    ms1_intensityIndex = i;
                }
            }

            // check if all the required header are found
            if (peptideSequenceIndex == -1 || chargeIndex == -1 || posteriorErrorIndex == -1
                    || rankIndex == -1 || spectrumTitleIndex == -1 || spectrumFileIndex == -1) {

                throw new IllegalArgumentException(
                        "Mandatory columns are missing in the Sage tsv file. Please check the file!"
                );
            }

            String line;
            String currentSpectrumTitle = null;
            SpectrumMatch currentMatch = null;

            // get the psms
            while ((line = reader.readLine()) != null) {

                String[] elements = line.split("\t");

                if (!line.trim().isEmpty()) {

                    String spectrumTitle = elements[spectrumTitleIndex].trim();
                    String modifiedPeptideSequence = elements[peptideSequenceIndex].toUpperCase();

                    // get the psm score
                    String scoreAsText = elements[posteriorErrorIndex]; // @TODO: add support for more score types?
                    double rawScore = Util.readDoubleAsString(scoreAsText);

                    int rank = Integer.parseInt(elements[rankIndex]);
                    int charge = Integer.parseInt(elements[chargeIndex]);

                    String fileName = elements[spectrumFileIndex];

                    // remove any html from the title
                    spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8");

                    // set up the yet empty spectrum match, or add to the current match
                    if (currentMatch == null
                            || (currentSpectrumTitle != null && !currentSpectrumTitle.equalsIgnoreCase(spectrumTitle))) {

                        // add the previous match, if any
                        if (currentMatch != null) {
                            result.add(currentMatch);
                        }

                        currentMatch = new SpectrumMatch(fileName, spectrumTitle);
                        currentSpectrumTitle = spectrumTitle;

                    }

                    // get the modifications
                    ArrayList<ModificationMatch> utilitiesModifications = new ArrayList<>(1);

                    StringBuilder peptideSequenceBuilder = new StringBuilder();
                    String peptideSequence;

                    if (modifiedPeptideSequence.lastIndexOf("[") == -1) {

                        // no modifications
                        peptideSequence = modifiedPeptideSequence;

                    } else {

                        // extract the modifications from the provided sequence
                        for (int i = 0; i < modifiedPeptideSequence.length(); i++) {

                            // we expect something like:
                            // [+229.1629]-VC[+57.0215]PAPC[+57.0215]EGAC[+57.0215]TLGIIEDPVGIK-[+229.1629]
                            if (modifiedPeptideSequence.charAt(i) == '[') {

                                int endingBracketIndex = modifiedPeptideSequence.indexOf(']', i + 1);

                                String modMassAsString = modifiedPeptideSequence.substring(i + 1, endingBracketIndex);
                                double modMass = Double.parseDouble(modMassAsString);

                                boolean variable = true;

                                // check if the modification is fixed or variable
                                for (String tempModName : searchParameters.getModificationParameters().getFixedModifications()) {

                                    modificationFactory.getModification(tempModName).getMass();

                                    if (Math.abs(modMass - modificationFactory.getModification(tempModName).getMass()) < MOD_MASS_TOLERANCE) {
                                        variable = false;
                                    }

                                }

                                if (variable) {

                                    int modSite;
                                    char modResidue;

                                    if (i == 0) { 
                                        // n-terminal modification
                                        modSite = 1;
                                        modResidue = modifiedPeptideSequence.charAt(endingBracketIndex + 2);
                                    } else if (endingBracketIndex + 1 == modifiedPeptideSequence.length()) {
                                        // c-terminal modification
                                        modSite = peptideSequenceBuilder.length();
                                        modResidue = peptideSequenceBuilder.charAt(modSite - 1);
                                    } else {
                                        // non-terminal modification
                                        modSite = peptideSequenceBuilder.length();
                                        modResidue = modifiedPeptideSequence.charAt(i - 1);
                                    }

                                    utilitiesModifications.add(
                                            new ModificationMatch(
                                                    modMass
                                                    + "@"
                                                    + modResidue,
                                                    modSite
                                            )
                                    );
                                }

                                i = endingBracketIndex;

                            } else if (modifiedPeptideSequence.charAt(i) != '-') {

                                peptideSequenceBuilder.append(modifiedPeptideSequence.charAt(i));

                            }
                        }

                        peptideSequence = peptideSequenceBuilder.toString();

                    }

                    // create the peptide
                    Peptide peptide = new Peptide(
                            peptideSequence,
                            utilitiesModifications.toArray(
                                    new ModificationMatch[utilitiesModifications.size()]
                            ),
                            true
                    );

                    // create the peptide assumption
                    PeptideAssumption peptideAssumption = new PeptideAssumption(
                            peptide,
                            rank,
                            Advocate.sage.getIndex(),
                            charge,
                            rawScore,
                            rawScore,
                            IoUtil.getFileName(sageTsvFile)
                    );

                    if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideSequence)) {

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
                                    peptideAssumption.getRawScore(),
                                    peptideAssumption.getScore(),
                                    peptideAssumption.getIdentificationFile()
                            );

                            currentMatch.addPeptideAssumption(Advocate.sage.getIndex(), newAssumption);

                        }

                    } else {

                        currentMatch.addPeptideAssumption(Advocate.sage.getIndex(), peptideAssumption);

                    }

                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {

                        break;

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
        sageTsvFile = null;
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
