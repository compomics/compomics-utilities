package com.compomics.util.experiment.io.identification.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.parameters.identification.tool_specific.PepnovoParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.refinement_parameters.PepnovoAssumptionDetails;
import static com.compomics.util.io.IoUtils.ENCODING;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;

/**
 * This class can be used to parse PepNovo identification files.
 *
 * @author Marc Vaudel
 */
public class PepNovoIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The file to parse.
     */
    private File identificationFile;
    /**
     * The standard format.
     */
    public static final String DEFAULT_HEADER = "#Index	RnkScr	PnvScr	N-Gap	C-Gap	[M+H]	Charge	Sequence";
    /**
     * The mass to add to the C-terminal gap so that is corresponds to a peptide
     * fragment.
     */
    public final double cTermCorrection = Atom.O.getMonoisotopicMass() + 2 * Atom.H.getMonoisotopicMass() + ElementaryIon.proton.getTheoreticMass();
    /**
     * The mass to add to the N-terminal gap so that is corresponds to a peptide
     * fragment.
     */
    public final double nTermCorrection = 0;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public PepNovoIdfileReader() {
    }

    /**
     * Constructor, initiate the parser. Displays the progress using the waiting
     * handler. The close() method shall be used when the file reader is no
     * longer used.
     *
     * @param identificationFile the identification file to parse
     */
    public PepNovoIdfileReader(
            File identificationFile
    ) {
        this.identificationFile = identificationFile;
    }

    @Override
    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            WaitingHandler waitingHandler,
            SearchParameters searchParameters
    ) throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {

        return getAllSpectrumMatches(
                waitingHandler,
                searchParameters,
                null,
                false
        );
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

        ArrayList<SpectrumMatch> spectrumMatches = new ArrayList<>();

        try ( SimpleFileReader reader = SimpleFileReader.getFileReader(identificationFile)) {

            SpectrumMatch currentMatch = null;
            int rank = 1;
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith(">>")) {

                    if (currentMatch != null) {

                        spectrumMatches.add(currentMatch);
                        rank = 1;

                    }

                    String[] temp = line.split("\\s+");
                    StringBuilder sb = new StringBuilder(line.length());
                    sb.append(temp[3]);
                    for (int i = 4 ; i < temp.length ; i++) {
                        sb.append(" ").append(temp[i]);
                    }
                    String formatted = sb.toString();
                    int endIndex = formatted.lastIndexOf("#Problem");
                    if (endIndex == -1) {
                        endIndex = formatted.lastIndexOf("(SQS");
                    }

                    // Condition: Skip problematic spectra not containing (SQS) at the end of the line.
                    if (endIndex > -1) {

                        String spectrumTitle = formatted.substring(0, endIndex).trim();

                        // remove any html from the title
                        String decodedTitle = URLDecoder.decode(spectrumTitle, ENCODING);
                        String spectrumKey = Spectrum.getSpectrumKey(getMgfFileName(), decodedTitle);
                        currentMatch = new SpectrumMatch(spectrumKey);

                        while (true) {
                            line = reader.readLine();
                            if (line.startsWith("# No") || line.startsWith("# Charge") || line.startsWith("#Problem") || line.startsWith("# too")) {
                                // Skip
                            } else if (!line.equals(DEFAULT_HEADER)) {
                                throw new IllegalArgumentException("Unrecognized table format. Expected: \"" + DEFAULT_HEADER + "\", found:\"" + line + "\".");
                            } else {
                                break;
                            }
                        }

                    } else {

                        currentMatch = null;

                    }
                } else if (line.length() > 0 && line.charAt(0) != '#' && currentMatch != null) {

                    currentMatch.addTagAssumption(Advocate.pepnovo.getIndex(), getAssumptionFromLine(line, rank));
                    rank++;
                }
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
        String fileName = Util.getFileName(identificationFile);
        return fileName.substring(0, fileName.length() - 4);
    }

    @Override
    public String getExtension() {
        return ".out";
    }

    @Override
    public void close() throws IOException {
        
    }

    /**
     * Returns a Peptide Assumption from a PepNovo result line. The rank score
     * is taken as reference score. All additional parameters are attached as
     * PeptideAssumptionDetails. Note: fixed PTMs are not annotated, variable
     * PTMs are marked with the PepNovo PTM tag (see PepnovoParameters to
     * retrieve utilities names)
     *
     * @param line the line to parse
     * @param rank the rank of the assumption
     * @return the corresponding assumption
     */
    private TagAssumption getAssumptionFromLine(
            String line,
            int rank
    ) {

        String[] lineComponents = line.trim().split("\t");

        Double rankScore = new Double(lineComponents[1]);
        Double pepNovoScore = new Double(lineComponents[2]);
        Double nGap = new Double(lineComponents[3]);
        Double cGap = new Double(lineComponents[4]);
        if (cGap > 0 && cGap < cTermCorrection) {
            throw new IllegalArgumentException("Incompatible c-term gap " + cGap);
        } else if (cGap > 0) {
            cGap -= cTermCorrection;
        }
        Double mH = new Double(lineComponents[5]);
        Integer charge = new Integer(lineComponents[6]);
        String pepNovoSequence = lineComponents[7];
        String sequence = "";
        ArrayList<ModificationMatch> modificationMatches = new ArrayList<>();
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
                            ModificationMatch modMatch = new ModificationMatch(pepNovoPtmTag, currentPtmLocation);
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

            ModificationMatch modMatch = new ModificationMatch(pepNovoPtmTag, currentPtmLocation);
            modificationMatches.add(modMatch);
        }

        AminoAcidSequence aminoAcidSequence = new AminoAcidSequence(sequence);
        for (ModificationMatch modificationMatch : modificationMatches) {
            aminoAcidSequence.addVariableModification(modificationMatch);
        }
        Tag tag = new Tag(nGap, aminoAcidSequence, cGap);
        TagAssumption tagAssumption = new TagAssumption(Advocate.pepnovo.getIndex(), rank, tag, charge, pepNovoScore);
        PepnovoAssumptionDetails pepnovoAssumptionDetails = new PepnovoAssumptionDetails();
        pepnovoAssumptionDetails.setRankScore(rankScore);
        pepnovoAssumptionDetails.setMH(mH);
        tagAssumption.addUrParam(pepnovoAssumptionDetails);

        return tagAssumption;
    }

    /**
     * Get a PTM.
     *
     * @param pepnovoParameters the PepNovo parameters
     * @param pepNovoModification the PepNovo modification
     *
     * @return the PTM as a string
     */
    public static String getPTM(
            PepnovoParameters pepnovoParameters,
            String pepNovoModification
    ) {

        Map<String, String> invertedPtmMap = pepnovoParameters.getPepNovoPtmMap();

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

    @Override
    public HashMap<String, ArrayList<String>> getSoftwareVersions() {
        HashMap<String, ArrayList<String>> result = new HashMap<>(1);
        ArrayList<String> versions = new ArrayList<>(1);
        versions.add("3.1 (beta)");
        result.put("PepNovo+", versions);
        return result;
    }

    @Override
    public boolean hasDeNovoTags() {
        return true;
    }
}
