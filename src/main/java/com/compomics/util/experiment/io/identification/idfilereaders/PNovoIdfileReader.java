package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PNovoParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.Charge;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

        index = new HashMap<>();

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
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
        return getAllSpectrumMatches(waitingHandler, searchParameters, null, false);
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters,
            SequenceMatchingPreferences sequenceMatchingPreferences, boolean expandAaCombinations)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        if (bufferedRandomAccessFile == null) {
            throw new IllegalStateException("The identification file was not set. Please use the appropriate constructor.");
        }

        LinkedList<SpectrumMatch> spectrumMatches = new LinkedList<>();

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(index.size());
        }

        for (String title : index.keySet()) {

            // remove any html from the title
            String decodedTitle = URLDecoder.decode(title, "utf-8");
            String spectrumKey = Spectrum.getSpectrumKey(getMgfFileName(), decodedTitle);
            SpectrumMatch currentMatch = new SpectrumMatch(spectrumKey);

            int cpt = 1;
            bufferedRandomAccessFile.seek(index.get(title));
            String line = bufferedRandomAccessFile.getNextLine().trim();
            boolean solutionsFound = false;
            if (line.startsWith("P")) {
                solutionsFound = true;
            }

            while (line != null && line.startsWith("P")) {
                currentMatch.addTagAssumption(Advocate.pNovo.getIndex(), getAssumptionFromLine(line, cpt, searchParameters));
                cpt++;
                line = bufferedRandomAccessFile.getNextLine();
            }

            if (solutionsFound) {

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
     * file is the mgf file name + ".pnovo.txt"
     *
     * @return the spectrum file name
     */
    public String getMgfFileName() {
        return fileName.substring(0, fileName.length() - ".pnovo.txt".length()) + ".mgf";
    }

    @Override
    public String getExtension() {
        return ".pnovo.txt";
    }

    @Override
    public void close() throws IOException {
        bufferedRandomAccessFile.close();
    }

    /**
     * Returns a Peptide Assumption from a pNovo result line. Note: fixed PTMs
     * are not annotated, variable PTMs are marked with the pNovo PTM tag.
     *
     * @param line the line to parse
     * @param rank the rank of the assumption
     * @param searchParameters the search parameters
     * @return the corresponding assumption
     */
    private TagAssumption getAssumptionFromLine(String line, int rank, SearchParameters searchParameters) {

        String[] lineComponents = line.trim().split("\t");

        Double pNovoScore = new Double(lineComponents[2]);
        String pNovoSequence = lineComponents[1];
        String peptideSequence = "";
        ArrayList<ModificationMatch> modificationMatches = new ArrayList<>();

        PNovoParameters pNovoParameters = (PNovoParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.pNovo.getIndex());

        if (pNovoParameters == null) {
            // @TODO: throw exception?
        } else {
            for (int i = 0; i < pNovoSequence.length(); i++) {

                char currentChar = pNovoSequence.charAt(i);

                if (pNovoParameters.getPtmResidue(currentChar) != null) {
                    modificationMatches.add(new ModificationMatch(pNovoParameters.getUtilitiesPtmName(currentChar), true, i + 1));
                    peptideSequence += pNovoParameters.getPtmResidue(currentChar);
                } else {
                    peptideSequence += currentChar;
                }
            }
        }

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence(peptideSequence);
        for (ModificationMatch modificationMatch : modificationMatches) {
            aminoAcidSequence.addModificationMatch(modificationMatch.getModificationSite(), modificationMatch);
        }
        Tag tag = new Tag(0, aminoAcidSequence, 0);
        TagAssumption tagAssumption = new TagAssumption(Advocate.pNovo.getIndex(), rank, tag, new Charge(Charge.PLUS, 1), pNovoScore); // @TODO: how to get the charge?

        return tagAssumption;
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> versions = new ArrayList<>();
        versions.add("unknown"); // @TODO: add version number
        result.put("pNovo+", versions);
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return true;
    }
}
