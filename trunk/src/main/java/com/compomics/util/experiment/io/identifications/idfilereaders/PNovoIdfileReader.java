package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.TagAssumption;
import com.compomics.util.experiment.identification.identification_parameters.PNovoParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;
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
import java.util.List;
import javax.xml.bind.JAXBException;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This class can be used to parse pNovo identification files.
 *
 * @author Harald Barsnes
 */
public class PNovoIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * A map of all spectrum titles and the associated index in the random
     * access file.
     */
    private HashMap<String, Long> index;
    /**
     * The result file in random access.
     */
    private BufferedRandomAccessFile bufferedRandomAccessFile = null;
    /**
     * The name of the result file.
     */
    private String fileName;
    /**
     * Map of the tags found indexed by amino acid sequence.
     */
    private HashMap<String, LinkedList<SpectrumMatch>> tagsMap;
    /**
     * The characters used to represent variable modifications in pNovo+.
     */
    private List<Character> variableModificationsCharacters = Arrays.asList('B', 'J', 'O', 'U', 'X', 'Z'); // @TODO: is it possible to add more characters..?

    /**
     * Default constructor for the purpose of instantiation.
     */
    public PNovoIdfileReader() {
    }

    /**
     * Constructor, initiate the parser. Displays the progress using the waiting
     * handler. The close() method shall be used when the file reader is no
     * longer used.
     *
     * @param identificationFile the identification file to parse
     *
     * @throws FileNotFoundException exception thrown whenever the provided file
     * was not found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     */
    public PNovoIdfileReader(File identificationFile) throws FileNotFoundException, IOException {
        this(identificationFile, null);
    }

    /**
     * Constructor, initiate the parser. Displays the progress using the waiting
     * handler. The close() method shall be used when the file reader is no
     * longer used.
     *
     * @param identificationFile the identification file to parse
     * @param waitingHandler a waiting handler providing progress feedback to
     * the user
     *
     * @throws FileNotFoundException exception thrown whenever the provided file
     * was not found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     */
    public PNovoIdfileReader(File identificationFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        bufferedRandomAccessFile = new BufferedRandomAccessFile(identificationFile, "r", 1024 * 100);
        fileName = Util.getFileName(identificationFile);

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        if (progressUnit == 0) {
            progressUnit = 1;
        }

        index = new HashMap<String, Long>();

        String line;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (line.startsWith("S")) {
                long currentIndex = bufferedRandomAccessFile.getFilePointer();

                String[] splitLine = line.split("\\t");
                String spectrumTitle = splitLine[1].trim();
                index.put(spectrumTitle, currentIndex);

                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                    waitingHandler.setSecondaryProgressCounter((int) (currentIndex / progressUnit));
                }
            }
        }
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
        return getAllSpectrumMatches(waitingHandler, null, false);
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SequenceMatchingPreferences sequenceMatchingPreferences, boolean expandAaCombinations) throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        int tagMapKeyLength = 0;
        if (sequenceMatchingPreferences != null) {
            SequenceFactory sequenceFactory = SequenceFactory.getInstance();
            tagMapKeyLength = sequenceFactory.getDefaultProteinTree().getInitialTagSize();
            tagsMap = new HashMap<String, LinkedList<SpectrumMatch>>(1024);
        }

        if (bufferedRandomAccessFile == null) {
            throw new IllegalStateException("The identification file was not set. Please use the appropriate constructor.");
        }

        LinkedList<SpectrumMatch> spectrumMatches = new LinkedList<SpectrumMatch>();

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(index.size());
        }

        for (String title : index.keySet()) {

            String decodedTitle = URLDecoder.decode(title, "utf-8");
            SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(getMgfFileName(), decodedTitle));

            int cpt = 1;
            bufferedRandomAccessFile.seek(index.get(title));
            String line = bufferedRandomAccessFile.getNextLine().trim();
            boolean solutionsFound = false;
            if (line.startsWith("P")) {
                solutionsFound = true;
            }

            while (line != null && line.startsWith("P")) {
                currentMatch.addHit(Advocate.pNovo.getIndex(), getAssumptionFromLine(line, cpt), true);
                cpt++;
                line = bufferedRandomAccessFile.getNextLine();
            }

            if (solutionsFound) {
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

                spectrumMatches.add(currentMatch);
            }

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
                waitingHandler.increaseSecondaryProgressCounter();
            }
        }

        return spectrumMatches;
    }

    /**
     * Returns the spectrum file name. This method assumes that the pNovo output
     * file is the mgf file name + ".txt"
     *
     * @return the spectrum file name
     */
    public String getMgfFileName() {
        return fileName.substring(0, fileName.length() - 4) + ".mgf";
    }

    @Override
    public String getExtension() {
        return ".txt";
    }

    @Override
    public void close() throws IOException {
        bufferedRandomAccessFile.close();
    }

    /**
     * Returns a Peptide Assumption from a pNovo result line. Note: fixed PTMs
     * are not annotated, variable PTMs are marked with the pNovo PTM tag (see
     * PNovoParameters to retrieve utilities names).
     *
     * @param line the line to parse
     * @param rank the rank of the assumption
     * @return the corresponding assumption
     */
    private TagAssumption getAssumptionFromLine(String line, int rank) {

        String[] lineComponents = line.trim().split("\t");

        Double pNovoScore = new Double(lineComponents[2]);
        String pNovoSequence = lineComponents[1]; // @TODO: this sequence contains the variable ptm characters, which are valid amino acids...
        String sequence = "";
        ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>();

        for (int i = 0; i < pNovoSequence.length(); i++) {
            char currentChar = pNovoSequence.charAt(i);

            if (variableModificationsCharacters.contains(currentChar)) {
                // @TODO: have to somehow extract/annotate variable ptms
            }
        }

        // @TODO: convert the variable PTMs
//        if (!modificationMass.equals("")) {
//
//            String pNovoPtmTag = "";
//
//            if (nTermPtm || cTermPtm) {
//                pNovoPtmTag += ptmTag;
//            } else {
//                pNovoPtmTag += currentAA;
//            }
//
//            pNovoPtmTag += modificationMass;
//
//            ModificationMatch modMatch = new ModificationMatch(pNovoPtmTag, true, currentPtmLocation);
//            modificationMatches.add(modMatch);
//        }
//
        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence(pNovoSequence);
        for (ModificationMatch modificationMatch : modificationMatches) {
            aminoAcidSequence.addModificationMatch(modificationMatch.getModificationSite(), modificationMatch);
        }
        Tag tag = new Tag(0, aminoAcidSequence, 0); // @TODO: is this correct?
        TagAssumption tagAssumption = new TagAssumption(Advocate.pNovo.getIndex(), rank, tag, new Charge(Charge.PLUS, 1), pNovoScore); // @TODO: how to get the charge?

        return tagAssumption;
    }

    /**
     * Get a PTM.
     *
     * @param pNovoParameters the pNovo parameters
     * @param pNovoModification the pNovo modification
     *
     * @return the PTM as a string
     */
    public static String getPTM(PNovoParameters pNovoParameters, String pNovoModification) {

        return null;
        // @TODO: implement me

//        Map<String, String> invertedPtmMap = pNovoParameters.getPNovoPtmMap();
//
//        if (invertedPtmMap == null) {
//            // @TODO: possible to rescue these?
//            throw new IllegalArgumentException("Unsupported de novo search result. Please reprocess the data.");
//        }
//
//        String utilitesPtmName = invertedPtmMap.get(pNovoModification);
//
//        if (utilitesPtmName != null) {
//            return utilitesPtmName;
//        } else {
//            throw new IllegalArgumentException("An error occurred while parsing the modification " + pNovoModification + ".");
//        }
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        ArrayList<String> versions = new ArrayList<String>();
        versions.add("unknown"); // @TODO: add version number
        result.put("pNovo+", versions);
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
