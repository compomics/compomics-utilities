package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * Id file reader for the scripts in Onyase.
 *
 * @author Marc Vaudel
 */
public class OnyaseIdfileReader implements IdfileReader {

    /**
     * The columns separator.
     */
    public final static char separator = ' ';
    /**
     * Character to start the comment lines.
     */
    public final static char comment = '#';
    /**
     * Tag for the version.
     */
    public final static String versionTag = "Version:";
    /**
     * Tag for the spectrum file.
     */
    public final static String spectraTag = "Spectrum File:";
    /**
     * Tag for the fasta file.
     */
    public final static String fastaTag = "Fasta File:";
    /**
     * Tag for the parameters file path.
     */
    public final static String paramsTag = "Parameters File:";
    /**
     * The result file to parse.
     */
    private File resultsFile;
    /**
     * The path to the mgf file used for the search.
     */
    private String mgfFile;
    /**
     * The path to the fasta file used for the search.
     */
    private String fastaFile;
    /**
     * The path to the parameters file used for the search.
     */
    private String parametersFile;
    /**
     * The name of the result file.
     */
    private String fileName;
    /**
     * The version used to create the file.
     */
    private HashMap<String, ArrayList<String>> version;

    /**
     * Default constructor instantiation purposes.
     */
    public OnyaseIdfileReader() {
    }

    /**
     * Constructor for an onyase file reader.
     *
     * @param resultsFile the Andromeda results file
     *
     * @throws IOException if an error occurrs while parsing the file
     */
    public OnyaseIdfileReader(File resultsFile) throws IOException {
        this.resultsFile = resultsFile;
        fileName = Util.getFileName(resultsFile);
        BufferedReader br = new BufferedReader(new FileReader(resultsFile));
//        String line;
//        while ((line = br.readLine()) != null) {
//            String key = "" + comment + separator + versionTag;
//            if (line.startsWith(key)) {
//                String fileVersion = line.substring(key.length()).trim();
//                version = new HashMap<String, ArrayList<String>>(1);
//                ArrayList<String> versions = new ArrayList<String>(1);
//                versions.add(fileVersion);
//                version.put(Advocate.onyaseEngine.getName(), versions);
//            }
//            key = "" + comment + separator + spectraTag;
//            if (line.startsWith(key)) {
//                mgfFile = line.substring(key.length()).trim();
//            }
//            key = "" + comment + separator + fastaTag;
//            if (line.startsWith(key)) {
//                fastaFile = line.substring(key.length()).trim();
//            }
//            key = "" + comment + separator + parametersFile;
//            if (line.startsWith(key)) {
//                parametersFile = line.substring(key.length()).trim();
//            }
//        }

        // To remove
        String fileVersion = "test";
        version = new HashMap<String, ArrayList<String>>(1);
        ArrayList<String> versions = new ArrayList<String>(1);
        versions.add(fileVersion);
        version.put(Advocate.onyaseEngine.getName(), versions);
        mgfFile = "bla\\qExactive01819.mgf";

    }

    @Override
    public String getExtension() {
        return ".psm";
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

        HashMap<String, SpectrumMatch> spectrumMatchesMap = new HashMap<String, SpectrumMatch>();

        String spectrumFileName = Util.getFileName(mgfFile);
        String resultFileName = Util.getFileName(resultsFile);

        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(resultsFile, "r", 1024 * 100);
        bufferedRandomAccessFile.readLine(); // To remove
        if (waitingHandler != null) {
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }
        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String separatorString = separator + "";
        String line;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (!line.startsWith("#")) {
                String[] lineSplit = line.split(separatorString);
                String spectrumTitle = lineSplit[0];
                spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8");
                String spectrumKey = Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle);
                SpectrumMatch spectrumMatch = spectrumMatchesMap.get(spectrumKey);
                if (spectrumMatch == null) {
                    spectrumMatch = new SpectrumMatch(spectrumKey);
                    spectrumMatchesMap.put(spectrumKey, spectrumMatch);
                }
                String sequence = lineSplit[3];
                ArrayList<ModificationMatch> modificationMatches = getModificationMatches(lineSplit[4]);
                Peptide peptide = new Peptide(sequence, modificationMatches);
                Integer charge = new Integer(lineSplit[5]);
                Double score = new Double(lineSplit[6]);
                Double eValue = new Double(lineSplit[7]);
                PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, -1, Advocate.onyaseEngine.getIndex(), new Charge(Charge.PLUS, charge), eValue, resultFileName);
                peptideAssumption.setRawScore(score);
                spectrumMatch.addHit(Advocate.onyaseEngine.getIndex(), peptideAssumption, true);
                long currentIndex = bufferedRandomAccessFile.getFilePointer();
                if (waitingHandler != null) {
                    waitingHandler.setSecondaryProgressCounter((int) (currentIndex / progressUnit));
                }
            }
        }

        LinkedList<SpectrumMatch> result = new LinkedList<SpectrumMatch>();
        for (SpectrumMatch spectrumMatch : spectrumMatchesMap.values()) {
            HashMap<Double, ArrayList<SpectrumIdentificationAssumption>> assumptionsMap = spectrumMatch.getAllAssumptions(Advocate.onyaseEngine.getIndex());
            ArrayList<Double> eValues = new ArrayList<Double>(assumptionsMap.keySet());
            Collections.sort(eValues);
            int rank = 1;
            int cpt = 1;
            for (Double eValue : eValues) {
                ArrayList<SpectrumIdentificationAssumption> spectrumIdentificationAssumptions = assumptionsMap.get(eValue);
                for (SpectrumIdentificationAssumption spectrumIdentificationAssumption : spectrumIdentificationAssumptions) {
                    spectrumIdentificationAssumption.setRank(rank);
                    cpt++;
                }
                rank = cpt;
            }
            result.add(spectrumMatch);
        }

        return result;
    }

    /**
     * Parses modification matches from a modification string.
     *
     * @param modificationsString the modification string
     *
     * @return a list of modificaiton matches
     *
     * @throws UnsupportedEncodingException exception thrown whenever an error
     * occurred while decoding the string
     */
    private ArrayList<ModificationMatch> getModificationMatches(String modificationsString) throws UnsupportedEncodingException {
        if (modificationsString.length() == 0) {
            return new ArrayList<ModificationMatch>(0);
        }
        String decodedString = URLDecoder.decode(modificationsString, "utf-8");
        String[] modifications = decodedString.split(Peptide.MODIFICATION_SEPARATOR);
        ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>(modifications.length);
        for (String modification : modifications) {
            String[] modificationSplit = modification.split(Peptide.MODIFICATION_LOCALIZATION_SEPARATOR);
            String modificationName = modificationSplit[0];
            Integer site = new Integer(modificationSplit[1]);
            ModificationMatch modificationMatch = new ModificationMatch(modificationName, true, site);
            modificationMatches.add(modificationMatch);
        }
        return modificationMatches;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        return version;
    }

    @Override
    public boolean hasDeNovoTags() {
        return false;
    }
}
