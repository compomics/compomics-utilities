package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.SequenceFactory;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This IdfileReader reads identifications from an Andromeda result file.
 *
 * @author Marc Vaudel
 */
public class AndromedaIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * A map of all spectrum titles and the associated index in the random
     * access file.
     */
    private HashMap<String, Long> index;
    /**
     * Andromeda result file in random access.
     */
    private BufferedRandomAccessFile bufferedRandomAccessFile = null;
    /**
     * The name of the Andromeda result file.
     */
    private String fileName;
    /**
     * A map of the peptides found in this file.
     */
    private HashMap<String, LinkedList<Peptide>> peptideMap;
    /**
     * The length of the keys of the peptide map.
     */
    private int peptideMapKeyLength;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public AndromedaIdfileReader() {
    }

    /**
     * Constructor for an Andromeda result file reader.
     *
     * @param resFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public AndromedaIdfileReader(File resFile) throws FileNotFoundException, IOException {
        this(resFile, null);
    }

    /**
     * Constructor for an Andromeda result file reader.
     *
     * @param resFile
     * @param waitingHandler
     * @throws FileNotFoundException
     * @throws IOException
     */
    public AndromedaIdfileReader(File resFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {
        bufferedRandomAccessFile = new BufferedRandomAccessFile(resFile, "r", 1024 * 100);

        fileName = Util.getFileName(resFile);

        if (waitingHandler != null) {
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        index = new HashMap<String, Long>();

        String line, title = null;
        boolean newTitle = false;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (line.startsWith(">")) {
                title = line.substring(1);
                newTitle = true;
            } else if (newTitle) {
                long currentIndex = bufferedRandomAccessFile.getFilePointer();
                index.put(title, currentIndex);
                newTitle = false;
                if (waitingHandler != null) {
                    waitingHandler.setSecondaryProgressCounter((int) (currentIndex / progressUnit));
                }
            }
        }
    }

    @Override
    public String getExtension() {
        return ".res";
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
        return getAllSpectrumMatches(waitingHandler, null, false);
    }

    @Override
    public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SequenceMatchingPreferences sequenceMatchingPreferences, boolean expandAaCombinations) throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        if (bufferedRandomAccessFile == null) {
            throw new IllegalStateException("The identification file was not set. Please use the appropriate constructor.");
        }

        if (sequenceMatchingPreferences != null) {
            SequenceFactory sequenceFactory = SequenceFactory.getInstance();
            peptideMapKeyLength = sequenceFactory.getDefaultProteinTree().getInitialTagSize();
            peptideMap = new HashMap<String, LinkedList<Peptide>>(1024);
        }

        LinkedList<SpectrumMatch> result = new LinkedList<SpectrumMatch>();

        for (String title : index.keySet()) {

            // @TODO: need to implement the spectrum number as well
            SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(fileName, title));

            int cpt = 1;
            String line;

            while ((line = bufferedRandomAccessFile.getNextLine()) != null
                    && !line.startsWith(">")) {
                PeptideAssumption peptideAssumption = getAssumptionFromLine(line, cpt, sequenceMatchingPreferences);
                if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideAssumption.getPeptide().getSequence())) {
                    Peptide peptide = peptideAssumption.getPeptide();
                    ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
                    for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {
                        Peptide newPeptide = new Peptide(expandedSequence.toString(), new ArrayList<ModificationMatch>(modificationMatches.size()));
                        for (ModificationMatch modificationMatch : modificationMatches) {
                            newPeptide.addModificationMatch(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), modificationMatch.getModificationSite()));
                        }
                        PeptideAssumption newAssumption = new PeptideAssumption(newPeptide, peptideAssumption.getRank(), peptideAssumption.getAdvocate(), peptideAssumption.getIdentificationCharge(), peptideAssumption.getScore(), peptideAssumption.getIdentificationFile());
                        currentMatch.addHit(Advocate.andromeda.getIndex(), newAssumption, true);
                    }
                } else {
                    currentMatch.addHit(Advocate.andromeda.getIndex(), peptideAssumption, true);
                }
                cpt++;
            }
            result.add(currentMatch);
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

        if (sequenceMatchingPreferences != null) {
            String subSequence = sequence.substring(0, peptideMapKeyLength);
            subSequence = AminoAcid.getMatchingSequence(subSequence, sequenceMatchingPreferences);
            LinkedList<Peptide> peptidesForTag = peptideMap.get(subSequence);
            if (peptidesForTag == null) {
                peptidesForTag = new LinkedList<Peptide>();
                peptideMap.put(subSequence, peptidesForTag);
            }
            peptidesForTag.add(peptide);
        }

        Charge charge = new Charge(Charge.PLUS, new Integer(temp[6]));
        double score = new Double(temp[1]);

        return new PeptideAssumption(peptide, rank, Advocate.andromeda.getIndex(), charge, score, fileName);
    }

    @Override
    public void close() throws IOException {
        bufferedRandomAccessFile.close();
    }

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        ArrayList<String> versions = new ArrayList<String>();
        versions.add("1.4.0.0");
        result.put("Andromeda", versions);
        return result;
    }

    @Override
    public HashMap<String, LinkedList<Peptide>> getPeptidesMap() {
        return peptideMap;
    }

    @Override
    public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap() {
        return new HashMap<String, LinkedList<SpectrumMatch>>();
    }

    @Override
    public void clearTagsMap() {
        // No tags here
    }

    @Override
    public void clearPeptidesMap() {
        if (peptideMap != null) {
            peptideMap.clear();
        }
    }
}
