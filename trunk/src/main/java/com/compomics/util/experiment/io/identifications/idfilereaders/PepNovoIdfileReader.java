package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.TagAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.refinementparameters.PepnovoAssumptionDetails;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This class can be used to parse PepNovo identification files.
 *
 * @author Marc Vaudel
 */
public class PepNovoIdfileReader extends ExperimentObject implements IdfileReader {

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
     * The standard format.
     */
    public static final String tableHeader = "#Index	RnkScr	PnvScr	N-Gap	C-Gap	[M+H]	Charge	Sequence";
    /**
     * The minimum PepNovo score.
     */
    private double minPepNovoScore = Double.MAX_VALUE;
    /**
     * The maximum PepNovo score.
     */
    private double maxPepNovoScore = Double.MIN_VALUE;
    /**
     * The minimum rank score.
     */
    private double minRankScore = Double.MAX_VALUE;
    /**
     * The maximum rank score.
     */
    private double maxRankScore = Double.MIN_VALUE;
    /**
     * The minimum N-terminal gap.
     */
    private double minNGap = Double.MAX_VALUE;
    /**
     * The maximum N-terminal gap.
     */
    private double maxNGap = Double.MIN_VALUE;
    /**
     * The minimum C-terminal gap.
     */
    private double minCGap = Double.MAX_VALUE;
    /**
     * The maximum C-terminal gap.
     */
    private double maxCGap = Double.MIN_VALUE;
    /**
     * The minimum charge.
     */
    private int minCharge = Integer.MAX_VALUE;
    /**
     * The maximum charge.
     */
    private int maxCharge = Integer.MIN_VALUE;
    /**
     * The minimum m/z value.
     */
    private double minMz = Double.MAX_VALUE;
    /**
     * The maximum m/z value.
     */
    private double maxMz = Double.MIN_VALUE;
    /**
     * The parameters used for sequencing.
     */
    private SearchParameters searchParameters;
    /**
     * The PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();

    /**
     * Default constructor for the purpose of instantiation.
     */
    public PepNovoIdfileReader() {
    }

    /**
     * Constructor, initiate the parser. The close() method shall be used when
     * the file reader is no longer used.
     *
     * @param identificationFile the identification file to parse
     * @param searchParameters the parameters used for sequencing
     * @throws FileNotFoundException exception thrown whenever the provided file
     * was not found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     */
    public PepNovoIdfileReader(File identificationFile, SearchParameters searchParameters) throws FileNotFoundException, IOException {
        this(identificationFile, searchParameters, null);
    }

    /**
     * Constructor, initiate the parser. Displays the progress using the waiting
     * handler The close() method shall be used when the file reader is no
     * longer used.
     *
     * @param identificationFile the identification file to parse
     * @param searchParameters the parameters used for sequencing
     * @param waitingHandler a waiting handler providing progress feedback to
     * the user
     * @throws FileNotFoundException exception thrown whenever the provided file
     * was not found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     */
    public PepNovoIdfileReader(File identificationFile, SearchParameters searchParameters, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        this.searchParameters = searchParameters;
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
            if (line.startsWith(">>")) {
                long currentIndex = bufferedRandomAccessFile.getFilePointer();

                String[] temp = line.split("\\s+");
                String formatted = "";
                for (int i = 3; i < temp.length; i++) {
                    formatted += (temp[i] + " ");
                }
                int endIndex = formatted.lastIndexOf("#Problem");
                if (endIndex == -1) {
                    endIndex = formatted.lastIndexOf("(SQS");
                }

                // Condition: Skip problematic spectra not containing (SQS) at the end of the line.
                if (endIndex > -1) {
                    String spectrumTitle = formatted.substring(0, endIndex).trim();
                    index.put(spectrumTitle, currentIndex);
                }

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
    public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception {

        if (bufferedRandomAccessFile == null) {
            throw new IllegalStateException("The identification file was not set. Please use the appropriate constructor.");
        }

        HashSet<SpectrumMatch> spectrumMatches = new HashSet<SpectrumMatch>();

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
            boolean solutionsFound = true;
            if (line.startsWith("# No") || line.startsWith("# Charge") || line.startsWith("#Problem") || line.startsWith("# too")) {
                solutionsFound = false;
            } else if (!line.equals(tableHeader)) {
                throw new IllegalArgumentException("Unrecognized table format. Expected: \"" + tableHeader + "\", found:\"" + line + "\".");
            }

            while ((line = bufferedRandomAccessFile.getNextLine()) != null
                    && !line.equals("") && !line.startsWith(">>")) {
                currentMatch.addHit(Advocate.pepnovo.getIndex(), getAssumptionFromLine(line, cpt), true);
                cpt++;
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
     * Returns the spectrum file name. This method assumes that the PepNovo
     * output file is the mgf file name + ".out"
     *
     * @return the spectrum file name
     */
    public String getMgfFileName() {
        return fileName.substring(0, fileName.length() - 4);
    }

    @Override
    public String getExtension() {
        return ".out";
    }

    @Override
    public void close() throws IOException {
        bufferedRandomAccessFile.close();
    }

    /**
     * Returns a Peptide Assumption from a PepNovo result line. The rank score
     * is taken as reference score. All additional parameters are attached as
     * PeptideAssumptionDetails.
     *
     * @param line the line to parse
     * @param rank the rank of the assumption
     * @return the corresponding assumption
     */
    private TagAssumption getAssumptionFromLine(String line, int rank) {

        String[] lineComponents = line.trim().split("\t");

        Double rankScore = new Double(lineComponents[1]);
        if (rankScore < minRankScore) {
            minRankScore = rankScore;
        }
        if (rankScore > maxRankScore) {
            maxRankScore = rankScore;
        }
        Double pepNovoScore = new Double(lineComponents[2]);
        if (pepNovoScore < minPepNovoScore) {
            minPepNovoScore = pepNovoScore;
        }
        if (pepNovoScore > maxPepNovoScore) {
            maxPepNovoScore = pepNovoScore;
        }
        Double nGap = new Double(lineComponents[3]);
        if (nGap < minNGap) {
            minNGap = nGap;
        }
        if (nGap > maxNGap) {
            maxNGap = nGap;
        }
        Double cGap = new Double(lineComponents[4]);
        double cTermCorrection = Atom.O.getMonoisotopicMass() + Atom.H.getMonoisotopicMass() + 2 * ElementaryIon.proton.getTheoreticMass();
        if (cGap > 0 && cGap < cTermCorrection) {
            throw new IllegalArgumentException("Incompatible c-term gap " + cGap);
        } else if (cGap > 0) {
            cGap -= cTermCorrection;
        }
        if (cGap < minCGap) {
            minCGap = cGap;
        }
        if (cGap > maxCGap) {
            maxCGap = cGap;
        }
        Double mH = new Double(lineComponents[5]);
        Integer charge = new Integer(lineComponents[6]);
        if (charge < minCharge) {
            minCharge = charge;
        }
        if (charge > maxCharge) {
            maxCharge = charge;
        }
        String pepNovoSequence = lineComponents[7];
        String sequence = "";
        ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>();
        String modificationMass = "", currentAA = "";
        int currentPtmLocation = 0;

        boolean nTermPtm = false;
        boolean cTermPtm = false;

        String ptmTag = "";

        // find and add the variable ptms
        for (int i = 0; i < pepNovoSequence.length(); i++) {
            String aa = pepNovoSequence.charAt(i) + "";

            if (aa.equals("^") || aa.equals("$")) {

                ptmTag = aa;

                if (aa.equals("^")) {
                    nTermPtm = true;
                } else {
                    cTermPtm = true;
                }

            } else {

                if (aa.equals("+") || aa.equals("-")) {
                    modificationMass += aa;
                } else {
                    try {
                        new Integer(aa);
                        modificationMass += aa;
                    } catch (Exception e) {
                        if (!modificationMass.equals("")) {

                            String pepNovoPtmTag = "";

                            if (nTermPtm || cTermPtm) {
                                pepNovoPtmTag += ptmTag;
                            } else {
                                pepNovoPtmTag += currentAA;
                            }

                            pepNovoPtmTag += modificationMass;

                            String ptm = getPTM(pepNovoPtmTag);

                            //String ptm = getPTM(modificationMass, currentAA, nTermPtm, cTermPtm);
                            ModificationMatch modMatch = new ModificationMatch(ptm, true, currentPtmLocation);
                            modMatch.setConfident(true);
                            modificationMatches.add(modMatch);
                            modificationMass = "";
                            nTermPtm = false;
                        }
                        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                        if (aminoAcid == null) {
                            throw new IllegalArgumentException("Attempting to parse " + aa + " as amino acid in " + pepNovoSequence + ".");
                        }
                        sequence += aa;
                        currentAA = aa;
                        currentPtmLocation++;
                    }
                }
            }
        }

        if (!modificationMass.equals("")) {

            String pepNovoPtmTag = "";

            if (nTermPtm || cTermPtm) {
                pepNovoPtmTag += ptmTag;
            } else {
                pepNovoPtmTag += currentAA;
            }

            pepNovoPtmTag += modificationMass;

            String ptm = getPTM(pepNovoPtmTag);
            ModificationMatch modMatch = new ModificationMatch(ptm, true, currentPtmLocation);
            modificationMatches.add(modMatch);
        }

        // find and add the fixed ptms
        char[] aminoAcidSequence = sequence.toCharArray();

        for (int i = 0; i < aminoAcidSequence.length; i++) {

            String tempAA = String.valueOf(aminoAcidSequence[i]);

            for (String fixedPtm : searchParameters.getModificationProfile().getFixedModifications()) {
                PTM ptm = ptmFactory.getPTM(fixedPtm);
                for (AminoAcid residue : ptm.getPattern().getAminoAcidsAtTarget()) {
                    if (residue.singleLetterCode.equalsIgnoreCase(tempAA)) {
                        ModificationMatch modMatch = new ModificationMatch(fixedPtm, false, (i + 1));
                        modMatch.setConfident(true);
                        modificationMatches.add(modMatch);
                    }
                }
            }
        }
        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern(sequence);
        for (ModificationMatch modificationMatch : modificationMatches) {
            aminoAcidPattern.addModificationMatch(modificationMatch.getModificationSite(), modificationMatch);
        }
        Tag tag = new Tag(nGap, aminoAcidPattern, cGap);
        TagAssumption tagAssumption = new TagAssumption(Advocate.pepnovo.getIndex(), rank, tag, new Charge(Charge.PLUS, charge), pepNovoScore);
        double mz = tagAssumption.getTheoreticMz();
        if (mz < minMz) {
            minMz = mz;
        }
        if (mz > maxMz) {
            maxMz = mz;
        }
        PepnovoAssumptionDetails pepnovoAssumptionDetails = new PepnovoAssumptionDetails();
        pepnovoAssumptionDetails.setRankScore(rankScore);
        pepnovoAssumptionDetails.setMH(mH);
        tagAssumption.addUrParam(pepnovoAssumptionDetails);

        return tagAssumption;
    }

    /**
     * Get a PTM.
     *
     * @param pepNovoModification the PepNovo modification
     * @return the PTM as a string
     */
    public String getPTM(String pepNovoModification) {

        Map<String, String> invertedPtmMap = searchParameters.getPepNovoPtmMap();

        if (invertedPtmMap == null) {
            // @TODO: possible to rescue these?
            throw new IllegalArgumentException("Unsupported de novo search result. Please reprocess the data.");
        }

        String utilitesPtmName = invertedPtmMap.get(pepNovoModification);

        if (utilitesPtmName != null) {
            return utilitesPtmName;
        } else {
            throw new IllegalArgumentException("An error occurred while parsing the modification " + pepNovoModification + ".");
        }
    }

    /**
     * Returns the minimum PepNovo score.
     *
     * @return Minimum PepNovo score
     */
    public double getMinPepNovoScore() {
        return minPepNovoScore;
    }

    /**
     * Returns the maximum PepNovo score.
     *
     * @return Maximum PepNovo score
     */
    public double getMaxPepNovoScore() {
        return maxPepNovoScore;
    }

    /**
     * Returns the minimum rank score.
     *
     * @return Minimum rank score
     */
    public double getMinRankScore() {
        return minRankScore;
    }

    /**
     * Returns the maximum rank score.
     *
     * @return Maximum rank score
     */
    public double getMaxRankScore() {
        return maxRankScore;
    }

    /**
     * Returns the minimum N-terminal gap.
     *
     * @return Minimum N-terminal gap.
     */
    public double getMinNGap() {
        return minNGap;
    }

    /**
     * Returns the maximum N-terminal gap.
     *
     * @return Maximum N-terminal gap
     */
    public double getMaxNGap() {
        return maxNGap;
    }

    /**
     * Returns the minimum C-terminal gap.
     *
     * @return Minimum C-terminal gap.
     */
    public double getMinCGap() {
        return minCGap;
    }

    /**
     * Returns the maximum C-terminal gap.
     *
     * @return Maximum C-terminal gap.
     */
    public double getMaxCGap() {
        return maxCGap;
    }

    /**
     * Returns the minimum charge.
     *
     * @return Minimum charge.
     */
    public int getMinCharge() {
        return minCharge;
    }

    /**
     * Returns the maximum charge.
     *
     * @return Maximum charge.
     */
    public int getMaxCharge() {
        return maxCharge;
    }

    /**
     * Returns the minimum m/z value.
     *
     * @return Minimum m/z value.
     */
    public double getMinMz() {
        return minMz;
    }

    /**
     * Returns the maximum m/z value.
     *
     * @return Maximum m/z value.
     */
    public double getMaxMz() {
        return maxMz;
    }

    @Override
    public String getSoftwareVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //@TODO
    }
}
