package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
        long currentIndex = 0;
        long progressUnit = bufferedRandomAccessFile.length() / 100;

        index = new HashMap<String, Long>();

        String line, title = null;
        boolean newTitle = false;
        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (line.startsWith(">")) {
                title = line.substring(1);
                newTitle = true;
            } else if (newTitle) {
                currentIndex = bufferedRandomAccessFile.getFilePointer();
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
    public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception {

        if (bufferedRandomAccessFile == null) {
            throw new IllegalStateException("The identification file was not set. Please use the appropriate constructor.");
        }

        HashSet<SpectrumMatch> result = new HashSet<SpectrumMatch>();

        for (String title : index.keySet()) {

            // @TODO: need to implement the spectrum number as well
            SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(fileName, title));

            int cpt = 1;
            String line;

            while ((line = bufferedRandomAccessFile.getNextLine()) != null
                    && !line.startsWith(">")) {
                currentMatch.addHit(Advocate.andromeda.getIndex(), getAssumptionFromLine(line, cpt), true);
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
     * @return the corresponding assumption
     */
    private PeptideAssumption getAssumptionFromLine(String line, int rank) {

        String[] temp = line.trim().split("\t");
        String[] temp1 = temp[5].split(";");

        temp1 = temp[4].split(",");
        ArrayList<ModificationMatch> modMatches = new ArrayList<ModificationMatch>();

        for (int aa = 0; aa < temp1.length; aa++) {
            String mod = temp1[aa];
            if (!mod.equals("A")) {
                modMatches.add(new ModificationMatch(mod, true, aa));
            }
        }

        Peptide peptide = new Peptide(temp[0], modMatches);
        Charge charge = new Charge(Charge.PLUS, new Integer(temp[6]));
        double score = new Double(temp[1]);

        return new PeptideAssumption(peptide, rank, Advocate.andromeda.getIndex(), charge, score, fileName);
    }

    @Override
    public void close() throws IOException {
        bufferedRandomAccessFile.close();
    }

    @Override
    public String getSoftwareVersion() {
        throw new UnsupportedOperationException("Not supported yet."); // @TODO: implement me!!
    }
}
