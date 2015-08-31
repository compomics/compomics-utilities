package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
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
 * This IdfileReader reads identifications from a Novor csv result file.
 *
 * @author Harald Barsnes
 */
public class NovorIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The software name.
     */
    private String softwareName = "Novor";
    /**
     * The softwareVersion.
     */
    private String softwareVersion = null;
    /**
     * The Novor csv file.
     */
    private File novorCsvFile;
    /**
     * Map of the tags found indexed by amino acid sequence.
     */
    private HashMap<String, LinkedList<SpectrumMatch>> tagsMap;
    /**
     * The spectrum factory used to retrieve spectrum titles.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

    /**
     * Default constructor for the purpose of instantiation.
     */
    public NovorIdfileReader() {
    }

    /**
     * Constructor for an Novor csv result file reader.
     *
     * @param novorCsvFile the Novor csv file
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public NovorIdfileReader(File novorCsvFile) throws FileNotFoundException, IOException {
        this(novorCsvFile, null);
    }

    /**
     * Constructor for an Novor csv result file reader.
     *
     * @param novorCsvFile the Novor csv file
     * @param waitingHandler the waiting handler
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public NovorIdfileReader(File novorCsvFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        this.novorCsvFile = novorCsvFile;

        // get the novor version number
        extractVersionNumber();
    }

    /**
     * Extracts the Novor version number.
     */
    private void extractVersionNumber() throws IOException {

        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(novorCsvFile, "r", 1024 * 100);

        String line = bufferedRandomAccessFile.readLine();
        boolean versionNumberFound = false;
        String versionNumberString = null;

        while (line.startsWith("#") && !versionNumberFound) {
            if (line.contains(" v")) {
                versionNumberString = line;
                versionNumberString = versionNumberString.substring(1);
                versionNumberString = versionNumberString.trim();
                versionNumberFound = true;
            }
            line = bufferedRandomAccessFile.readLine();
        }

        if (versionNumberFound) {
            softwareVersion = versionNumberString.trim();
        }

        bufferedRandomAccessFile.close();
    }

    @Override
    public String getExtension() {
        return ".novor.csv";
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

        int tagMapKeyLength = 0;
        if (sequenceMatchingPreferences != null) {
            SequenceFactory sequenceFactory = SequenceFactory.getInstance();
            tagMapKeyLength = sequenceFactory.getDefaultProteinTree().getInitialTagSize();
            tagsMap = new HashMap<String, LinkedList<SpectrumMatch>>(1024);
        }

        LinkedList<SpectrumMatch> result = new LinkedList<SpectrumMatch>();

        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(novorCsvFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String inputFile = null;

        // read until we find the header line
        String line;
        while ((line = bufferedRandomAccessFile.readLine()) != null && !line.startsWith("# id,")) {
            if (line.startsWith("# input file = ")) {
                inputFile = line.substring("# input file = ".length()).trim();
            }
        }

        if (inputFile == null) {
            throw new IllegalArgumentException("Mandatory header information is missing in the Novor csv file (the input file tag). Please check the file!");
        }

        // get the spectrum file name
        String spectrumFileName = new File(inputFile).getName();

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
        while ((line = bufferedRandomAccessFile.readLine()) != null) {

            String[] elements = line.split(", ");

            if (!line.trim().isEmpty()) { // @TODO: make this more robust?

                int id = Integer.valueOf(elements[idIndex]);
                int charge = Integer.valueOf(elements[chargeIndex]);
                String peptideSequenceWithMods = elements[peptideIndex];

                // get the novor score
                String scoreAsText = elements[scoreIndex];
                double novorScore = Util.readDoubleAsString(scoreAsText);

                // get the novor e-value
                //double novorEValue = Math.pow(10, -novorScore); // convert novor score to e-value // @TODO: is this correct?

                // get the name of the spectrum file
                String spectrumTitle = id + "";
                if (spectrumFactory.fileLoaded(spectrumFileName)) {
                    spectrumTitle = spectrumFactory.getSpectrumTitle(spectrumFileName, id);
                }

                // set up the yet empty spectrum match, or add to the current match
                if (currentMatch == null || (currentSpectrumTitle != null && !currentSpectrumTitle.equalsIgnoreCase(spectrumTitle))) {

                    // add the previous match, if any
                    if (currentMatch != null) {
                        result.add(currentMatch);
                    }

                    currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle));
                    currentMatch.setSpectrumNumber(id);
                    currentSpectrumTitle = spectrumTitle;
                }

                // get the modifications
                ArrayList<ModificationMatch> utilitiesModifications = new ArrayList<ModificationMatch>();

                String peptideSequence;

                // extract the modifications
                if (peptideSequenceWithMods.contains("(")) {

                    peptideSequence = "";

                    for (int i = 0; i < peptideSequenceWithMods.length(); i++) {

                        char currentChar = peptideSequenceWithMods.charAt(i);

                        if (currentChar == '(') {
                            int modStart = i + 1;
                            int modEnd = peptideSequenceWithMods.indexOf(")", i + 1);
                            String currentMod = peptideSequenceWithMods.substring(modStart, modEnd);
                            utilitiesModifications.add(new ModificationMatch(currentMod, true, peptideSequence.length())); // @TODO: check terminal ptms
                            i = modEnd;
                        } else if (currentChar == '[') {
                            int modStart = i + 1;
                            int modEnd = peptideSequenceWithMods.indexOf("]", i + 1);
                            String currentMod = peptideSequenceWithMods.substring(modStart, modEnd);
                            utilitiesModifications.add(new ModificationMatch(currentMod, true, peptideSequence.length())); // @TODO: check terminal ptms
                            i = modEnd;
                        } else {
                            peptideSequence += currentChar;
                        }
                    }
                } else {
                    peptideSequence = peptideSequenceWithMods;
                }

                // set up the charge
                Charge peptideCharge = new Charge(Charge.PLUS, charge);

                // create the tag assumption
                AminoAcidSequence aminoAcidSequence = new AminoAcidSequence(peptideSequence);
                for (ModificationMatch modificationMatch : utilitiesModifications) {
                    aminoAcidSequence.addModificationMatch(modificationMatch.getModificationSite(), modificationMatch);
                }
                Tag tag = new Tag(0, aminoAcidSequence, 0);
                TagAssumption tagAssumption = new TagAssumption(Advocate.novor.getIndex(), 1, tag, peptideCharge, novorScore);
                //tagAssumption.setRawScore(novorScore);

                currentMatch.addHit(Advocate.novor.getIndex(), tagAssumption, true);

                if (sequenceMatchingPreferences != null) {
                    HashMap<Integer, HashMap<String, ArrayList<TagAssumption>>> matchTagMap = currentMatch.getTagAssumptionsMap(tagMapKeyLength, sequenceMatchingPreferences);
                    for (HashMap<String, ArrayList<TagAssumption>> advocateMap : matchTagMap.values()) {
                        for (String key : advocateMap.keySet()) {
                            LinkedList<SpectrumMatch> tagMatches = tagsMap.get(key);
                            if (tagMatches == null) {
                                tagMatches = new LinkedList<SpectrumMatch>();
                                tagsMap.put(key, tagMatches);
                            }
                            tagMatches.add(currentMatch);
                        }
                    }
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
        novorCsvFile = null;
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
        return new HashMap<String, LinkedList<Peptide>>();
    }

    @Override
    public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap() {
        return tagsMap;
    }

    @Override
    public void clearTagsMap() {
        if (tagsMap != null) {
            tagsMap.clear();
        }
    }

    @Override
    public void clearPeptidesMap() {
        // No peptides here
    }
}
