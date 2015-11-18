package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
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
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import org.apache.commons.math.util.FastMath;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This IdfileReader reads identifications from an Andromeda result file.
 *
 * @author Marc Vaudel
 */
public class AndromedaIdfileReader extends ExperimentObject implements IdfileReader {

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
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if a IOException occurs
     */
    public AndromedaIdfileReader(File resultsFile) throws FileNotFoundException, IOException {
        this.resultsFile = resultsFile;
        fileName = Util.getFileName(resultsFile);
    }

    @Override
    public String getExtension() {
        return ".res";
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

        String mgfFile = Util.removeExtension(fileName) + ".mgf"; //@TODO: make this generic?

        LinkedList<SpectrumMatch> result = new LinkedList<SpectrumMatch>();
        HashMap<String, SpectrumMatch> spectrumMatchesMap = new HashMap<String, SpectrumMatch>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(resultsFile, "r", 1024 * 100);
        if (waitingHandler != null) {
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }
        long progressUnit = bufferedRandomAccessFile.length() / 100;
        String line, title = null;
        SpectrumMatch spectrumMatch = null;
        int rank = 0;
        boolean firstSpectrum = false;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (line.startsWith(">")) {
                if (!firstSpectrum) {
                    firstSpectrum = true;
                }
                title = line.substring(1);
                // remove any html from the title
                title = URLDecoder.decode(title, "utf-8");
                spectrumMatch = null;
                long currentIndex = bufferedRandomAccessFile.getFilePointer();
                if (waitingHandler != null) {
                    waitingHandler.setSecondaryProgressCounter((int) (currentIndex / progressUnit));
                }
            } else if (firstSpectrum) {
                if (spectrumMatch == null) {
                    String spectrumKey = Spectrum.getSpectrumKey(mgfFile, title);
                    spectrumMatch = spectrumMatchesMap.get(spectrumKey);
                    rank = 0; // the rank is here per charge
                    if (spectrumMatch == null) {
                        spectrumMatch = new SpectrumMatch(Spectrum.getSpectrumKey(mgfFile, title));
                        result.add(spectrumMatch);
                        spectrumMatchesMap.put(spectrumKey, spectrumMatch);
                    }
                }
                rank++;
                PeptideAssumption peptideAssumption = getAssumptionFromLine(line, rank, sequenceMatchingPreferences);
                if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideAssumption.getPeptide().getSequence())) {
                    Peptide peptide = peptideAssumption.getPeptide();
                    ArrayList<ModificationMatch> previousModificationMatches = peptide.getModificationMatches(),
                            newModificationMatches = null;
                    if (previousModificationMatches != null) {
                        newModificationMatches = new ArrayList<ModificationMatch>(previousModificationMatches.size());
                    }
                    for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {
                        Peptide newPeptide = new Peptide(expandedSequence.toString(), newModificationMatches);
                        if (previousModificationMatches != null) {
                            for (ModificationMatch modificationMatch : previousModificationMatches) {
                                newPeptide.addModificationMatch(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), modificationMatch.getModificationSite()));
                            }
                        }
                        PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(), peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(), peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                        spectrumMatch.addHit(Advocate.andromeda.getIndex(), newAssumption, true);
                    }
                } else {
                    spectrumMatch.addHit(Advocate.andromeda.getIndex(), peptideAssumption, true);
                }
            }
        }

        return result;
    }

    /**
     * Returns a Peptide Assumption from an Andromeda line.
     *
     * @param line the line to parse
     * @param rank the rank of the assumption
     * @param sequenceMatchingPreferences the sequence matching preferences to
     * use to fill the secondary maps
     *
     * @return the corresponding assumption
     */
    private PeptideAssumption getAssumptionFromLine(String line, int rank, SequenceMatchingPreferences sequenceMatchingPreferences) {

        String[] temp = line.trim().split("\t");

        String[] temp1 = temp[4].split(",");
        ArrayList<ModificationMatch> modMatches = new ArrayList<ModificationMatch>();

        for (int aa = 0; aa < temp1.length; aa++) {
            String mod = temp1[aa];
            if (!mod.equals("A")) {
                modMatches.add(new ModificationMatch(mod, true, aa));
            }
        }

        String sequence = temp[0];
        Peptide peptide = new Peptide(sequence, modMatches);

        Charge charge = new Charge(Charge.PLUS, new Integer(temp[6]));
        Double score = new Double(temp[1]);
        Double p = FastMath.pow(10, -(score / 10));
        PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, Advocate.andromeda.getIndex(), charge, p, fileName);
        peptideAssumption.setRawScore(score);
        return peptideAssumption;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        ArrayList<String> versions = new ArrayList<String>();
        versions.add("1.5.3.4");
        result.put("Andromeda", versions);
        return result;
    }

    @Override
    public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap() {
        return new HashMap<String, LinkedList<SpectrumMatch>>(0);
    }

    @Override
    public void clearTagsMap() {
        // No tags here
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }
}
