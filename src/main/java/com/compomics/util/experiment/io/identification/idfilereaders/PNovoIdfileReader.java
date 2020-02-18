package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.parameters.identification.tool_specific.PNovoParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import static com.compomics.util.io.IoUtil.ENCODING;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;

/**
 * This class can be used to parse pNovo identification files.
 *
 * @author Harald Barsnes
 */
public class PNovoIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The file to parse.
     */
    private File identificationFile;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public PNovoIdfileReader() {
    }

    /**
     * Constructor, initiate the parser.
     *
     * @param identificationFile the identification file to parse
     */
    public PNovoIdfileReader(
            File identificationFile
    ) {
        this.identificationFile = identificationFile;
    }

    @Override
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            WaitingHandler waitingHandler,
            SearchParameters searchParameters
    ) throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
        return getAllSpectrumMatches(waitingHandler, searchParameters, null, false);
    }

    @Override
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            WaitingHandler waitingHandler,
            SearchParameters searchParameters,
            SequenceMatchingParameters sequenceMatchingPreferences,
            boolean expandAaCombinations
    ) throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        if (identificationFile == null) {
            throw new IllegalStateException("The identification file was not set. Please use the appropriate constructor.");
        }
            
        String fileName = Util.getFileName(identificationFile);
        String mgfFileName = getMgfFileName(fileName);

        ArrayList<SpectrumMatch> spectrumMatches = new ArrayList<>();

        try ( SimpleFileReader reader = SimpleFileReader.getFileReader(identificationFile)) {

            SpectrumMatch currentMatch = null;
            int rank = 1;
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.charAt(0) == 'S') {

                    if (currentMatch != null) {

                        spectrumMatches.add(currentMatch);
                        rank = 1;

                    }

                    String[] splitLine = line.split("\\t");
                    String spectrumTitle = splitLine[1].trim();

                    // remove any html from the title
                    String decodedTitle = URLDecoder.decode(spectrumTitle, ENCODING);
                    
                    String spectrumKey = Spectrum.getSpectrumKey(mgfFileName, decodedTitle);
                    currentMatch = new SpectrumMatch(spectrumKey);

                } else if (line.charAt(0) == 'P') {

                    if (currentMatch == null) {

                        throw new IllegalArgumentException("No spectrum title found for peptide.\n" + line);

                    }

                    currentMatch.addTagAssumption(Advocate.pNovo.getIndex(), getAssumptionFromLine(line, rank, searchParameters));

                }
            }
        }

        return spectrumMatches;
    }

    /**
     * Returns the spectrum file name.This method assumes that the pNovo output
 file is the mgf file name + ".pnovo.txt"
     *
     * @param fileName the name of the results file.
     * 
     * @return The spectrum file name.
     */
    public static String getMgfFileName(String fileName) {
        
        if (fileName.endsWith(".pnovo.txt")) {
        
            return fileName.substring(0, fileName.length() - 10) + ".mgf";
        
        } else if (fileName.endsWith(".pnovo.txt.gz")) {
        
            return fileName.substring(0, fileName.length() - 13) + ".mgf";
            
        } else {
            
            throw new IllegalArgumentException("Unexpected file extension. Expected: .pnovo.txt or .pnovo.txt.gz. File name: " + fileName + ".");
            
        }
    
    }

    @Override
    public String getExtension() {
        return ".pnovo.txt";
    }

    @Override
    public void close() throws IOException {
        
    }

    /**
     * Returns a Peptide Assumption from a pNovo result line. Note: fixed PTMs
     * are not annotated, variable PTMs are marked with the pNovo PTM tag.
     *
     * @param line the line to parse
     * @param rank the rank of the assumption
     * @param searchParameters the search parameters
     *
     * @return the corresponding assumption
     */
    private TagAssumption getAssumptionFromLine(
            String line,
            int rank,
            SearchParameters searchParameters
    ) {

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
                    modificationMatches.add(new ModificationMatch(pNovoParameters.getUtilitiesPtmName(currentChar), i + 1));
                    peptideSequence += pNovoParameters.getPtmResidue(currentChar);
                } else {
                    peptideSequence += currentChar;
                }
            }
        }

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence(peptideSequence);
        for (ModificationMatch modificationMatch : modificationMatches) {
            aminoAcidSequence.addVariableModification(modificationMatch);
        }
        Tag tag = new Tag(0, aminoAcidSequence, 0);
        TagAssumption tagAssumption = new TagAssumption(Advocate.pNovo.getIndex(), rank, tag, 1, pNovoScore); // @TODO: how to get the charge?

        return tagAssumption;
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<>(1);
        ArrayList<String> versions = new ArrayList<>(1);
        versions.add("unknown"); // @TODO: add version number
        result.put("pNovo+", versions);
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return true;
    }
}
