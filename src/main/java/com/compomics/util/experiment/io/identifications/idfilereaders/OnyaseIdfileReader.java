package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import jdk.jfr.events.FileReadEvent;
import org.xmlpull.v1.XmlPullParserException;
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
    public final static String SEPARATOR = " ";
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
        String line;
        while ((line = br.readLine()) != null) {
            String key = "# Version:";
            if (line.startsWith(key)) {
                String fileVersion = line.substring(key.length()).trim();
                version = new HashMap<String, ArrayList<String>>(1);
                ArrayList<String> versions = new ArrayList<String>(1);
                versions.add(fileVersion);
                version.put(Advocate.onyaseEngine.getName(), versions);
            }
            key = "# Spectra:";
            if (line.startsWith(key)) {
                mgfFile = line.substring(key.length()).trim();
            }
            key = "# Fasta:";
            if (line.startsWith(key)) {
                fastaFile = line.substring(key.length()).trim();
            }
            key = "# Parameters:";
            if (line.startsWith(key)) {
                parametersFile = line.substring(key.length()).trim();
            }
        }
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

        String spectrumFileName = Util.getFileName(mgfFile);
        String resultFileName = Util.getFileName(resultsFile);

        LinkedList<SpectrumMatch> result = new LinkedList<SpectrumMatch>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(resultsFile, "r", 1024 * 100);
        if (waitingHandler != null) {
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }
        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String line;
        SpectrumMatch spectrumMatch = null;
        int rank = 0;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (!line.startsWith("#")) {
                String[] lineSplit = line.split(SEPARATOR);
                String spectrumTitle = lineSplit[0];
                if (spectrumTitle.length() > 0) {
                    // remove any html from the title
                    spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8");
                    if (spectrumMatch != null) {
                        result.add(spectrumMatch);
                    }
                    spectrumMatch = new SpectrumMatch(Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle));
                    rank = 0;
                }
                rank++;
                String sequence = lineSplit[1];
                Peptide peptide = new Peptide(sequence, new ArrayList<ModificationMatch>(0));
                Integer charge = new Integer(lineSplit[2]);
                Double score = new Double(lineSplit[3]);
                Double eValue = new Double(lineSplit[4]);
                PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, rank, Advocate.onyaseEngine.getIndex(), new Charge(Charge.PLUS, charge), eValue, resultFileName);
                peptideAssumption.setRawScore(score);
                spectrumMatch.addHit(Advocate.onyaseEngine.getIndex(), peptideAssumption, true);
                long currentIndex = bufferedRandomAccessFile.getFilePointer();
                if (waitingHandler != null) {
                    waitingHandler.setSecondaryProgressCounter((int) (currentIndex / progressUnit));
                }
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        return version;
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
