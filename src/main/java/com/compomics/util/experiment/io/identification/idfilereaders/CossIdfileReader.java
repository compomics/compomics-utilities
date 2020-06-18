package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.modifications.Modification;
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
import org.apache.commons.lang3.math.NumberUtils;

/**
 * This IdfileReader reads identifications from a COSS tsv result file.
 *
 * @author Harald Barsnes
 */
public class CossIdfileReader implements IdfileReader {

    /**
     * The software name.
     */
    private final String SOFTWARE_NAME = "COSS";
    /**
     * The softwareVersion.
     */
    private String softwareVersion = null;
    /**
     * The COSS tsv file.
     */
    private File cossTsvFile;
    /**
     * The name of the COSS result file.
     */
    private String fileName;
    /**
     * The compomics PTM factory.
     */
    private final ModificationFactory modificationFactory = ModificationFactory.getInstance();

    /**
     * Default constructor for the purpose of instantiation.
     */
    public CossIdfileReader() {
    }

    /**
     * Constructor for a COSS tsv result file reader.
     *
     * @param cossTsvFile the COSS tsv file
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public CossIdfileReader(
            File cossTsvFile
    ) throws IOException {
        this(cossTsvFile, null);
    }

    /**
     * Constructor for a COSS tsv result file reader.
     *
     * @param cossTsvFile the COSS tsv file
     * @param waitingHandler the waiting handler
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public CossIdfileReader(
            File cossTsvFile,
            WaitingHandler waitingHandler
    ) throws IOException {

        this.cossTsvFile = cossTsvFile;
        fileName = IoUtil.getFileName(cossTsvFile);

        // get the coss version number
        //extractVersionNumber(); // @TODO: not implemented
    }

    /**
     * Extracts the COSS version number.
     */
    private void extractVersionNumber() { // @TODO: not implemented by coss

        try (SimpleFileReader reader = SimpleFileReader.getFileReader(cossTsvFile)) {

            // read the version number, if available
            String versionNumberString = reader.readLine();

            if (versionNumberString.toLowerCase().startsWith("#version: ")) {
                softwareVersion = versionNumberString.substring("#version: ".length()).trim();
            }
        }
    }

    @Override
    public String getExtension() {
        return ".coss.tsv";
    }

    @Override
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,
            WaitingHandler waitingHandler,
            SearchParameters searchParameters
    )
            throws IOException, IllegalArgumentException, SQLException,
            ClassNotFoundException, InterruptedException, JAXBException {

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
            throws IOException, IllegalArgumentException, SQLException,
            ClassNotFoundException, InterruptedException, JAXBException {

        String mgfFile = getMgfFileName(fileName);
        
        ArrayList<SpectrumMatch> result = new ArrayList<>();

        try (SimpleFileReader reader = SimpleFileReader.getFileReader(cossTsvFile)) {

            // check if the version number is included
            String versionNumberString = reader.readLine();
            String headerString;

            // skip the version number
            if (versionNumberString.toLowerCase().startsWith("#version: ")) { // @TODO: not implemented in coss
                headerString = reader.readLine();
            } else {
                headerString = versionNumberString;
            }

            // parse the header line
            String[] headers = headerString.split("\t");
            int titleIndex = -1, libraryIndex = -1, scanNumberIndex = -1,
                    rtIndex = -1, sequenceIndex = -1, precMassIndex = -1,
                    chargeQueryIndex = -1, chargeLibIndex = -1,
                    cossScoreIndex = -1, validationFdrIndex = -1,
                    modificationsIndex = -1, proteinsIndex = -1, filteredQueryPeaksIndex = -1,
                    filteredLibraryPeaksIndex = -1, sumIntQueryIndex = -1,
                    sumIntLibIndex = -1, matchedPeaksIndex = -1,
                    matchedIntQueryIndex = -1, matchedIntLibIndex = -1;

            // get the column index of the headers
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i];

                if (header.equalsIgnoreCase("Title")) {
                    titleIndex = i;
                } else if (header.equalsIgnoreCase("Library")) {
                    libraryIndex = i;
                } else if (header.equalsIgnoreCase("Scan No.")) {
                    scanNumberIndex = i;
                } else if (header.equalsIgnoreCase("RetentionT")) {
                    rtIndex = i;
                } else if (header.equalsIgnoreCase("Sequence")) {
                    sequenceIndex = i;
                } else if (header.equalsIgnoreCase("RetentionT")) {
                    rtIndex = i;
                } else if (header.equalsIgnoreCase("Prec. Mass")) {
                    precMassIndex = i;
                } else if (header.equalsIgnoreCase("ChargeQuery")) {
                    chargeQueryIndex = i;
                } else if (header.equalsIgnoreCase("ChargeLib")) {
                    chargeLibIndex = i;
                } else if (header.equalsIgnoreCase("Score")) {
                    cossScoreIndex = i;
                } else if (header.equalsIgnoreCase("Validation(FDR)")) {
                    validationFdrIndex = i;
                } else if (header.equalsIgnoreCase("Mods")) {
                    modificationsIndex = i;
                } else if (header.equalsIgnoreCase("Protein Accessions")) {
                    proteinsIndex = i;
                } else if (header.equalsIgnoreCase("#filteredQueryPeaks")) {
                    filteredQueryPeaksIndex = i;
                } else if (header.equalsIgnoreCase("#filteredLibraryPeaks")) {
                    filteredLibraryPeaksIndex = i;
                } else if (header.equalsIgnoreCase("SumIntQuery")) {
                    sumIntQueryIndex = i;
                } else if (header.equalsIgnoreCase("SumIntLib")) {
                    sumIntLibIndex = i;
                } else if (header.equalsIgnoreCase("#MatchedPeaks")) {
                    matchedPeaksIndex = i;
                } else if (header.equalsIgnoreCase("MatchedIntQuery")) {
                    matchedIntQueryIndex = i;
                } else if (header.equalsIgnoreCase("MatchedIntLib")) {
                    matchedIntLibIndex = i;
                }
            }

            // check if all the required headers are found
            if (titleIndex == -1 || sequenceIndex == -1 || modificationsIndex == -1
                    || cossScoreIndex == -1 || chargeLibIndex == -1 || validationFdrIndex == -1) {
                throw new IllegalArgumentException(
                        "Mandatory columns are missing in the COSS tsv file. Please check the file!");
            }

            String line;

            // get the psms
            while ((line = reader.readLine()) != null) {

                String[] elements = line.split("\t");

                if (!line.trim().isEmpty()) { // @TODO: make this more robust?

                    String spectrumTitle = elements[titleIndex];
                    String peptideSequence = elements[sequenceIndex].toUpperCase();
                    String modifications = elements[modificationsIndex].trim();

                    // get the coss score and convert it to e-value like
                    String scoreAsText = elements[cossScoreIndex];
                    double cossRawScore = Util.readDoubleAsString(scoreAsText);
                    double cossTransformedScore = Math.pow(10, -cossRawScore);

                    // coss fdr value
                    double cossFdrValue = Util.readDoubleAsString(elements[validationFdrIndex]);

                    int rank = 1; // Integer.valueOf(elements[rankIndex]); // @TODO: only includes the best match?
                    int charge = Integer.valueOf(elements[chargeLibIndex]); // @TODO: correct to use this one?

                    String fileName = mgfFile; //elements[filenameIndex]; // @TODO: not provided...

                    // remove TITLE= from the title
                    spectrumTitle = spectrumTitle.substring("TITLE=".length()); // @TODO: for some reason the title tag is included in the title...

                    // remove any html from the title
                    spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8");

                    SpectrumMatch spectrumMatch = new SpectrumMatch(fileName, spectrumTitle);

                    // get the modifications
                    ArrayList<ModificationMatch> utilitiesModifications = new ArrayList<>(1);

                    if (!modifications.isEmpty() && !modifications.equalsIgnoreCase("0")) {

                        // we expect something like this:
                        // "2/0,S,Acetyl/16,S,Phospho"
                        // or
                        // "3/2,C,89.011293/5,C,89.011293/6,M,37.006603"
                        // remove the initial numbering
                        modifications = modifications.substring(modifications.indexOf("/") + 1);

                        // get the individual modifications
                        String[] modificationsString = modifications.split("/");

                        for (String tempModAsString : modificationsString) {

                            String[] modificationElements = tempModAsString.split(",");

                            if (modificationElements.length == 3) {

                                int modSite = Integer.valueOf(modificationElements[0]);
                                char target = peptideSequence.charAt(modSite);

                                if (NumberUtils.isNumber(modificationElements[2])) {

                                    double modMass = Double.parseDouble(modificationElements[2]);
                                    utilitiesModifications.add(new ModificationMatch(modMass + "@" + target, modSite + 1));

                                } else {

                                    String modName = modificationElements[2];

                                    // @TODO: move some of the below code to the modification factory?
                                    if (modName.endsWith("yl")
                                            || modName.endsWith("tyl")
                                            || modName.endsWith("thyl")) {
                                        modName += "ation of ";
                                    } else if (modName.equalsIgnoreCase("Phospho")) {
                                        modName = "Phosphorylation of ";
                                    } else if (modName.equalsIgnoreCase("Pyro-glu")
                                            || modName.equalsIgnoreCase("Pyro_glu")) {
                                        modName = "Pyrolidone from ";
                                    } else if (modName.startsWith("iTRAQ4plex")) {
                                        modName = "iTRAQ 4-plex of ";
                                    } else if (modName.startsWith("iTRAQ8plex")) {
                                        modName = "iTRAQ 8-plex of ";
                                    } else if (modName.startsWith("TMT6plex")) {
                                        modName = "TMT 6-plex of ";
//                                    } else if (modName.equalsIgnoreCase("Label:13C")) {
//                                        modName = "Lysine 13C(6) 15N(2)";
                                    } else {
                                        modName += " of ";
                                    }

                                    // try with different target types
                                    String tempTarget = String.valueOf(target);
                                    String modNameAtTarget = modName + tempTarget;
                                    String modNameAtNTerminal = modName + "peptide N-term";
                                    String modNameAtCTerminal = modName + "peptide C-term";

                                    // try with the target amino acid
                                    Modification utilitiesMod = modificationFactory.getModification(modNameAtTarget);

                                    // try with the n-term
                                    if (utilitiesMod == null && modSite == 0) {
                                        utilitiesMod = modificationFactory.getModification(modNameAtNTerminal);
                                    }

                                    // try with the c-term
                                    if (utilitiesMod == null && modSite == peptideSequence.length() - 1) {
                                        utilitiesMod = modificationFactory.getModification(modNameAtCTerminal);
                                    }

                                    if (utilitiesMod != null) {
                                        utilitiesModifications.add(
                                                new ModificationMatch(
                                                        utilitiesMod.getMass() + "@" + target,
                                                        modSite + 1
                                                )
                                        );
                                    } else {

                                        utilitiesModifications.add(
                                                new ModificationMatch(
                                                        10000 + "@" + target,
                                                        modSite + 1
                                                )
                                        );

                                        // modification cannot be mapped...
                                        //System.out.println("unmappable: " + modName + target);
                                    }
                                }
                            } else {
                                // the modification cannot be parsed...
                                //System.out.println("unparsable: " + tempModAsString);
                            }

                        }
                    }

                    // create the peptide
                    Peptide peptide = new Peptide(
                            peptideSequence,
                            utilitiesModifications.toArray(new ModificationMatch[utilitiesModifications.size()]),
                            true
                    );

                    // create the peptide assumption
                    PeptideAssumption peptideAssumption = new PeptideAssumption(
                            peptide,
                            rank,
                            Advocate.coss.getIndex(),
                            charge,
                            //cossTransformedScore, 
                            cossFdrValue,
                            IoUtil.getFileName(cossTsvFile)
                    );

                    peptideAssumption.setRawScore(cossRawScore);

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
                                    peptideAssumption.getScore(),
                                    peptideAssumption.getIdentificationFile()
                            );

                            newAssumption.setRawScore(cossRawScore);
                            spectrumMatch.addPeptideAssumption(Advocate.msAmanda.getIndex(), newAssumption);

                        }

                    } else {
                        spectrumMatch.addPeptideAssumption(Advocate.coss.getIndex(), peptideAssumption);
                    }

                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        break;
                    }

                    // create the spectrum match
                    result.add(spectrumMatch);
                }
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        cossTsvFile = null;
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> versions = new ArrayList<>();
        versions.add(softwareVersion);
        result.put(SOFTWARE_NAME, versions);
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }

    /**
     * Returns the name of the mgf file corresponding to the given COSS file
     * name. Note: the COSS result name is expected to be the mgf file without
     * extension appended with ".coss.tsv" or ".coss.tsv.gz".
     *
     * @param fileName the COSS result file
     *
     * @return The name of the mgf file corresponding to the given COSS file
     * name.
     */
    public static String getMgfFileName(
            String fileName
    ) {

        if (fileName.endsWith(".coss.tsv.gz")) {

            return fileName.substring(0, fileName.length() - 7) + ".mgf";

        } else if (fileName.endsWith(".coss.tsv")) {

            return fileName.substring(0, fileName.length() - 4) + ".mgf";

        } else {

            throw new IllegalArgumentException(
                    "Unexpected file extension. "
                    + "Expected: .coss.tsv or .coss.tsv.gz. "
                    + "File name: "
                    + fileName + "."
            );

        }
    }
}
