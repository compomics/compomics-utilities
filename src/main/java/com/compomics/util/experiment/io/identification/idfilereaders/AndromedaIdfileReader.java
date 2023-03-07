package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;
import org.apache.commons.math.util.FastMath;

/**
 * This IdfileReader reads identifications from an Andromeda result file.
 *
 * @author Marc Vaudel
 */
public class AndromedaIdfileReader implements IdfileReader {

    /**
     * The Andromeda result file to parse.
     */
    private File resultsFile;
    /**
     * The name of the Andromeda result file.
     */
    private String fileName;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public AndromedaIdfileReader() {
    }

    /**
     * Constructor for an Andromeda result file reader.
     *
     * @param resultsFile the Andromeda results file
     */
    public AndromedaIdfileReader(
            File resultsFile
    ) {
        this.resultsFile = resultsFile;
        fileName = IoUtil.getFileName(resultsFile);
    }

    @Override
    public String getExtension() {
        return ".res";
    }

    @Override
    public HashMap<String, HashMap<String, ArrayList<SpectrumIdentificationAssumption>>> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,
            WaitingHandler waitingHandler,
            SearchParameters searchParameters
    ) throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

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
            throws IOException, IllegalArgumentException, SQLException,
            ClassNotFoundException, InterruptedException, JAXBException {

        HashMap<String, HashMap<String, ArrayList<SpectrumIdentificationAssumption>>> result = new HashMap<>(1);

        String mgfFile = getMgfFileName(fileName);
        HashMap<String, ArrayList<SpectrumIdentificationAssumption>> fileResults = new HashMap<>();
        result.put(mgfFile, fileResults);

        try ( SimpleFileReader reader = SimpleFileReader.getFileReader(resultsFile)) {

            String line, title = null;
            int rank = 0;
            boolean firstSpectrum = false;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith(">")) {

                    if (!firstSpectrum) {
                        firstSpectrum = true;
                    }
                    title = line.substring(1).trim();
                    // remove any html from the title
                    title = URLDecoder.decode(title, "utf-8");

                } else if (firstSpectrum) {

                    ArrayList<SpectrumIdentificationAssumption> spectrumResults = fileResults.get(title);

                    if (spectrumResults == null) {

                        spectrumResults = new ArrayList<>(2);
                        fileResults.put(title, spectrumResults);

                        rank = 0; // the rank is here per charge

                    }

                    rank++;
                    PeptideAssumption peptideAssumption = getAssumptionFromLine(line, rank);

                    if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideAssumption.getPeptide().getSequence())) {

                        Peptide peptide = peptideAssumption.getPeptide();
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
                            spectrumResults.add(newAssumption);

                        }
                    } else {

                        spectrumResults.add(peptideAssumption);

                    }
                }
            }

            return result;

        }
    }

    /**
     * Returns a Peptide Assumption from an Andromeda line.
     *
     * @param line the line to parse
     * @param rank the rank of the assumption
     *
     * @return the corresponding assumption
     */
    private PeptideAssumption getAssumptionFromLine(
            String line,
            int rank
    ) {

        String[] temp = line.trim().split("\t");

        String[] temp1 = temp[4].split(",");
        ArrayList<ModificationMatch> modMatches = new ArrayList<>();

        for (int aa = 0; aa < temp1.length; aa++) {

            String mod = temp1[aa];

            if (!mod.equals("A")) {

                modMatches.add(new ModificationMatch(mod, aa + 1));

            }
        }

        String sequence = temp[0];
        Peptide peptide = new Peptide(sequence, modMatches.toArray(new ModificationMatch[modMatches.size()]), true);

        int charge = Integer.parseInt(temp[6]);
        Double score = Double.valueOf(temp[1]);
        Double p = FastMath.pow(10, -(score / 10));

        PeptideAssumption peptideAssumption = new PeptideAssumption(
                peptide,
                rank,
                Advocate.andromeda.getIndex(),
                charge,
                score,
                p,
                fileName
        );

        return peptideAssumption;

    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {

        HashMap<String, ArrayList<String>> result = new HashMap<>(1);
        ArrayList<String> versions = new ArrayList<>(1);
        versions.add("1.5.3.4");
        result.put("Andromeda", versions);
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }

    /**
     * Returns the name of the mgf file corresponding to the given Andromeda
     * file name. Note: the Andromeda result name is expected to be the mgf file
     * without extension appended with ".res" or ".res.gz".
     *
     * @param fileName the Andromeda result file
     *
     * @return The name of the mgf file corresponding to the given Andromeda
     * file name.
     */
    public static String getMgfFileName(
            String fileName
    ) {

        if (fileName.endsWith(".res.gz")) {

            return fileName.substring(0, fileName.length() - 7) + ".mgf";

        } else if (fileName.endsWith(".res")) {

            return fileName.substring(0, fileName.length() - 4) + ".mgf";

        } else {

            throw new IllegalArgumentException(
                    "Unexpected file extension. Expected: .res or .res.gz. File name: "
                    + fileName + "."
            );

        }
    }
}
