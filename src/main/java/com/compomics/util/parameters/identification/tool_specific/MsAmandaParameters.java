package com.compomics.util.parameters.identification.tool_specific;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;

/**
 * The MS Amanda specific parameters.
 *
 * @author Harald Barsnes
 */
public class MsAmandaParameters extends ExperimentObject implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = -8458620189315975268L;
    /**
     * Defines whether a decoy database shall be created and searched against.
     * Decoy FASTS files are generated by reverting protein sequences,
     * accessions are marked with the prefix “REV_”.
     */
    private boolean generateDecoy = false;
    /**
     * False = combine ranks for target and decoy, true = own rankings for
     * target and decoy.
     */
    private Boolean reportBothBestHitsForTD = true;
    /**
     * The MS Amanda instrument ID.
     */
    private String instrumentID = "b, y";
    /**
     * The maximum rank [1-999].
     */
    private Integer maxRank = 10;
    /**
     * Defines whether monoisotopic mass values shall be used (in contrast to
     * average mass values).
     */
    private boolean monoisotopic = true;
    /**
     * Defines whether the low memory mode is used.
     *
     * @deprecated since MS Amanda 2.0
     */
    private Boolean lowMemoryMode = true;
    /**
     * Defines whether deisotoping is to be performed.
     */
    private Boolean performDeisotoping = true;
    /**
     * Maximum number of occurrences of a specific modification on a peptide
     * (0-10).
     */
    private Integer maxModifications = 3;
    /**
     * Maximum number of variable modifications per peptide (0-10).
     */
    private Integer maxVariableModifications = 4;
    /**
     * Maximum number of potential modification sites per modification per
     * peptide (0-20).
     */
    private Integer maxModificationSites = 6;
    /**
     * Maximum number of water and ammonia losses per peptide (0-5).
     */
    private Integer maxNeutralLosses = 1;
    /**
     * Maximum number identical modification specific losses per peptide (0-5).
     */
    private Integer maxNeutralLossesPerModification = 2;
    /**
     * Minimum peptide length.
     */
    private Integer minPeptideLength = 8;
    /**
     * Maximum peptide length.
     */
    private Integer maxPeptideLength = 30;
    /**
     * Maximum number of proteins loaded into memory (1000-500000).
     */
    private Integer maxLoadedProteins = 100000;
    /**
     * Maximum number of spectra loaded into memory (1000-500000).
     */
    private Integer maxLoadedSpectra = 2000;
    /**
     * The maximum allowed length of the FASTA file name.
     */
    public static final int MAX_MS_AMANDA_FASTA_FILE_NAME_LENGTH = 80;
    /**
     * The output format: csv or mzIdentML.
     */
    private String outputFormat = "csv";
    /**
     * Maximum charge state of calculated fragment ions (+2, +3, +4, Precursor -
     * 1).
     */
    private String maxAllowedChargeState = "+2";
    /**
     * Minimum number of selected peaks within peak picking window (1-30).
     */
    private Integer minPeakDepth = 1;
    /**
     * Maximum number of selected peaks within peak picking window (1-30).
     */
    private Integer maxPeakDepth = 10;
    /**
     * Perform second search to identify mixed spectra.
     */
    private Boolean performSecondSearch = false;
    /**
     * Whether y1 ion shall be kept for second search.
     */
    private Boolean keepY1Ion = true;
    /**
     * Whether water losses shall be removed for second search.
     */
    private Boolean removeWaterLosses = true;
    /**
     * Whether ammonia losses shall be removed for second search.
     */
    private Boolean removeAmmoniaLosses = true;
    /**
     * Exclude original precursor in second search.
     */
    private Boolean excludeFirstPrecursor = true;
    /**
     * Maximum number of different precursors for second search (1-10).
     */
    private Integer maxMultiplePrecursors = 5;
    /**
     * Which charges shall be tested for precursors (no deisotoping) where the
     * charge cannot be defined (+2; +3; +2, +3; +2, +3, +4; +3, +4; +2, +3, +4,
     * +5).
     *
     * @deprecated Remove in MS Amanda 3.0.21. Combined with the
     * ConsideredCharges option.
     */
    private String consideredChargesForPrecursors = "+2,+3";
    /**
     * Considered charges are combined in one result.
     */
    private Boolean combineConsideredCharges = true;
    /**
     * Automatically run percolator and add q-values to output file.
     */
    private Boolean runPercolator = false;
    /**
     * Generate file for percolator; filename is the same as stated in output
     * filename with suffix _pin.tsv.
     */
    private Boolean generatePInFile = false;

    /**
     * Constructor.
     */
    public MsAmandaParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.msAmanda;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof MsAmandaParameters) {
            MsAmandaParameters msAmandaParameters = (MsAmandaParameters) identificationAlgorithmParameter;
            if (generateDecoy != msAmandaParameters.generateDecoy()) {
                return false;
            }
            if (reportBothBestHitsForTD != msAmandaParameters.reportBothBestHitsForTD()) {
                return false;
            }
            if (monoisotopic != msAmandaParameters.isMonoIsotopic()) {
                return false;
            }
            if (!instrumentID.equalsIgnoreCase(msAmandaParameters.getInstrumentID())) {
                return false;
            }
            if (!maxRank.equals(msAmandaParameters.getMaxRank())) {
                return false;
            }
            if (performDeisotoping != msAmandaParameters.isPerformDeisotoping()) {
                return false;
            }
            if (!maxModifications.equals(msAmandaParameters.getMaxModifications())) {
                return false;
            }
            if (!maxVariableModifications.equals(msAmandaParameters.getMaxVariableModifications())) {
                return false;
            }
            if (!maxModificationSites.equals(msAmandaParameters.getMaxModificationSites())) {
                return false;
            }
            if (!maxNeutralLosses.equals(msAmandaParameters.getMaxNeutralLosses())) {
                return false;
            }
            if (!maxNeutralLossesPerModification.equals(msAmandaParameters.getMaxNeutralLossesPerModification())) {
                return false;
            }
            if (!minPeptideLength.equals(msAmandaParameters.getMinPeptideLength())) {
                return false;
            }
            if (!maxPeptideLength.equals(msAmandaParameters.getMaxPeptideLength())) {
                return false;
            }
            if (!maxLoadedProteins.equals(msAmandaParameters.getMaxLoadedProteins())) {
                return false;
            }
            if (!maxLoadedSpectra.equals(msAmandaParameters.getMaxLoadedSpectra())) {
                return false;
            }
            if (!getOutputFormat().equalsIgnoreCase(msAmandaParameters.getOutputFormat())) {
                return false;
            }
            if (!getMaxAllowedChargeState().equalsIgnoreCase(msAmandaParameters.getMaxAllowedChargeState())) {
                return false;
            }
            if (!getMinPeakDepth().equals(msAmandaParameters.getMinPeakDepth())) {
                return false;
            }
            if (!getMaxPeakDepth().equals(msAmandaParameters.getMaxPeakDepth())) {
                return false;
            }
            if (!getPerformSecondSearch().equals(msAmandaParameters.getPerformSecondSearch())) {
                return false;
            }
            if (!getKeepY1Ion().equals(msAmandaParameters.getKeepY1Ion())) {
                return false;
            }
            if (!getRemoveWaterLosses().equals(msAmandaParameters.getRemoveWaterLosses())) {
                return false;
            }
            if (!getRemoveAmmoniaLosses().equals(msAmandaParameters.getRemoveAmmoniaLosses())) {
                return false;
            }
            if (!getExcludeFirstPrecursor().equals(msAmandaParameters.getExcludeFirstPrecursor())) {
                return false;
            }
            if (!getMaxMultiplePrecursors().equals(msAmandaParameters.getMaxMultiplePrecursors())) {
                return false;
            }
            if (!getConsideredChargesForPrecursors().equalsIgnoreCase(msAmandaParameters.getConsideredChargesForPrecursors())) {
                return false;
            }
            if (!getCombineConsideredCharges().equals(msAmandaParameters.getCombineConsideredCharges())) {
                return false;
            }
            if (!getRunPercolator().equals(msAmandaParameters.getRunPercolator())) {
                return false;
            }
            if (!getGeneratePInFile().equals(msAmandaParameters.getGeneratePInFile())) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public String toString(boolean html) {
        String newLine = System.getProperty("line.separator");

        if (html) {
            newLine = "<br>";
        }

        StringBuilder output = new StringBuilder();
        Advocate advocate = getAlgorithm();
        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append("# ").append(advocate.getName()).append(" Specific Parameters");
        output.append(newLine);
        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append(newLine);

        output.append("SEARCH_DECOY=");
        output.append(generateDecoy);
        output.append(newLine);
        output.append("REPORT_BOTH_BEST_HITS_FOR_TD=");
        output.append(reportBothBestHitsForTD);
        output.append(newLine);
        output.append("INSTRUMENT_ID=");
        output.append(instrumentID);
        output.append(newLine);
        output.append("MONOISOTOPIC=");
        output.append(monoisotopic);
        output.append(newLine);
        output.append("MAX_RANK=");
        output.append(maxRank);
        output.append(newLine);
        output.append("PERFORM_DEISOTOPING=");
        output.append(isPerformDeisotoping());
        output.append(newLine);
        output.append("MAX_MODIFICATIONS=");
        output.append(getMaxModifications());
        output.append(newLine);
        output.append("MAX_VARIABLE_MODIFICATIONS=");
        output.append(getMaxVariableModifications());
        output.append(newLine);
        output.append("MAX_MODIFICATIONS_SITES=");
        output.append(getMaxModificationSites());
        output.append(newLine);
        output.append("MAX_NEUTRAL_LOSSES=");
        output.append(getMaxNeutralLosses());
        output.append(newLine);
        output.append("MAX_NEUTRAL_LOSSES_PER_MODIFICATION=");
        output.append(getMaxNeutralLossesPerModification());
        output.append(newLine);
        output.append("MIN_PEPTIDE_LENGTH=");
        output.append(getMinPeptideLength());
        output.append(newLine);
        output.append("MAX_PEPTIDE_LENGTH=");
        output.append(getMaxPeptideLength());
        output.append(newLine);
        output.append("MAX_LOADED_PROTEINS=");
        output.append(getMaxLoadedProteins());
        output.append(newLine);
        output.append("MAX_LOADED_SPECTRA=");
        output.append(getMaxLoadedSpectra());
        output.append(newLine);
        output.append("OUTPUT_FORMAT=");
        output.append(outputFormat);
        output.append(newLine);
        output.append("MAX_ALLOWED_CHARGE_STATE=");
        output.append(maxAllowedChargeState);
        output.append(newLine);
        output.append("MIN_PEAK_DEPTH=");
        output.append(minPeakDepth);
        output.append(newLine);
        output.append("MAX_PEAK_DEPTH=");
        output.append(maxPeakDepth);
        output.append(newLine);
        output.append("PERFORM_SECOND_SEARCH=");
        output.append(performSecondSearch);
        output.append(newLine);
        output.append("KEEP_Y1_ION=");
        output.append(keepY1Ion);
        output.append(newLine);
        output.append("REMOVE_WATER_LOSSES=");
        output.append(removeWaterLosses);
        output.append(newLine);
        output.append("REMOVE_AMMONIA_LOSSES=");
        output.append(removeAmmoniaLosses);
        output.append(newLine);
        output.append("EXCLUDE_FIRST_PRECURSOR=");
        output.append(excludeFirstPrecursor);
        output.append(newLine);
        output.append("MAX_MULTIPLE_PRECURSORS=");
        output.append(maxMultiplePrecursors);
        output.append(newLine);
        output.append("CONSIDERED_CHARGES_FOR_PRECURSORS=");
        output.append(consideredChargesForPrecursors);
        output.append(newLine);
        output.append("COMBINE_CHARGE_STATES=");
        output.append(combineConsideredCharges);
        output.append(newLine);
        output.append("RUN_PERCOLATOR=");
        output.append(runPercolator);
        output.append(newLine);
        output.append("GENERATE_PIN_FILE=");
        output.append(generatePInFile);
        output.append(newLine);

        return output.toString();
    }

    /**
     * Returns whether a decoy database shall be created and searched against.
     *
     * @return true if a decoy database shall be created and searched against
     */
    public boolean generateDecoy() {
        return generateDecoy;
    }

    /**
     * Set whether a decoy database shall be created and searched against.
     *
     * @param generateDecoy the generateDecoy to set
     */
    public void setGenerateDecoyDatabase(boolean generateDecoy) {
        this.generateDecoy = generateDecoy;
    }

    /**
     * Returns whether monoisotopic mass values shall be used (in contrast to
     * average mass values).
     *
     * @return true if mass values shall be used (in contrast to average mass
     * values)
     */
    public boolean isMonoIsotopic() {
        return monoisotopic;
    }

    /**
     * Set whether monoisotopic mass values shall be used (in contrast to
     * average mass values).
     *
     * @param monoisotopic the monoisotopic to set
     */
    public void setMonoIsotopic(boolean monoisotopic) {
        this.monoisotopic = monoisotopic;
    }

    /**
     * Return the instrument ID.
     *
     * @return the instrument ID
     */
    public String getInstrumentID() {
        return instrumentID;
    }

    /**
     * Set the instrument ID.
     *
     * @param instrumentID the instrument ID to set
     */
    public void setInstrumentID(String instrumentID) {
        this.instrumentID = instrumentID;
    }

    /**
     * Returns the maximum rank.
     *
     * @return the max rank
     */
    public Integer getMaxRank() {
        return maxRank;
    }

    /**
     * Set the maximum rank.
     *
     * @param maxRank the maxRank to set
     */
    public void setMaxRank(Integer maxRank) {
        this.maxRank = maxRank;
    }

    /**
     * Returns whether the low memory mode is used.
     *
     * @deprecated use getMaxLoadedProteins and getMaxLoadedSpectra instead
     * @return true if in low memory mode
     */
    public boolean isLowMemoryMode() {

        if (lowMemoryMode == null) {
            lowMemoryMode = true;
        }

        return lowMemoryMode;

    }

    /**
     * Set whether the low memory mode is used.
     *
     * @deprecated use setMaxLoadedProteins and setMaxLoadedSpectra instead
     * @param lowMemoryMode the low memory mode to set
     */
    public void setLowMemoryMode(boolean lowMemoryMode) {
        this.lowMemoryMode = lowMemoryMode;
    }

    /**
     * Returns whether deisotoping is to be performed.
     *
     * @return true if deisotoping is to be performed
     */
    public boolean isPerformDeisotoping() {

        if (performDeisotoping == null) {
            performDeisotoping = true;
        }

        return performDeisotoping;

    }

    /**
     * Sets if deisotoping is to be performed.
     *
     * @param performDeisotoping the performDeisotoping to set
     */
    public void setPerformDeisotoping(boolean performDeisotoping) {
        this.performDeisotoping = performDeisotoping;
    }

    /**
     * Returns the maximum number of modifications per peptide.
     *
     * @return the maximum number of modifications
     */
    public Integer getMaxModifications() {

        if (maxModifications == null) {
            maxModifications = 3;
        }

        return maxModifications;

    }

    /**
     * Set the maximum number of modifications per peptide.
     *
     * @param maxModifications the maximum number of modifications
     */
    public void setMaxModifications(Integer maxModifications) {
        this.maxModifications = maxModifications;
    }

    /**
     * Returns the maximum number of variable modifications per peptide.
     *
     * @return the maximum number of variable modifications
     */
    public Integer getMaxVariableModifications() {

        if (maxVariableModifications == null) {
            maxVariableModifications = 4;
        }

        return maxVariableModifications;

    }

    /**
     * Set the maximum number of variable modifications per peptide.
     *
     * @param maxVariableModifications the maximum number of variable
     * modifications
     */
    public void setMaxVariableModifications(Integer maxVariableModifications) {
        this.maxVariableModifications = maxVariableModifications;
    }

    /**
     * Returns the maximum number of modifications sites per modification per
     * peptide.
     *
     * @return the maximum number of modifications sites per modification per
     * peptide
     */
    public Integer getMaxModificationSites() {

        if (maxModificationSites == null) {
            maxModificationSites = 6;
        }

        return maxModificationSites;

    }

    /**
     * Set the maximum number of modifications sites per modification per
     * peptide.
     *
     * @param maxModificationSites the maximum number of modifications sites per
     * modification per peptide
     */
    public void setMaxModificationSites(Integer maxModificationSites) {
        this.maxModificationSites = maxModificationSites;
    }

    /**
     * Returns the maximum number of water and ammonia losses per peptide.
     *
     * @return the maximum number of water and ammonia losses per peptide
     */
    public Integer getMaxNeutralLosses() {

        if (maxNeutralLosses == null) {
            maxNeutralLosses = 1;
        }

        return maxNeutralLosses;

    }

    /**
     * Set the maximum number of water and ammonia losses per peptide.
     *
     * @param maxNeutralLosses the maximum number of water and ammonia losses
     * per peptide
     */
    public void setMaxNeutralLosses(Integer maxNeutralLosses) {
        this.maxNeutralLosses = maxNeutralLosses;
    }

    /**
     * Returns the maximum number identical modification specific losses per
     * peptide.
     *
     * @return the the maximum number identical modification specific losses per
     * peptide
     */
    public Integer getMaxNeutralLossesPerModification() {

        if (maxNeutralLossesPerModification == null) {
            maxNeutralLossesPerModification = 2;
        }

        return maxNeutralLossesPerModification;

    }

    /**
     * Set the maximum number identical modification specific losses per
     * peptide.
     *
     * @param maxNeutralLossesPerModification the maximum number identical
     * modification specific losses per peptide
     */
    public void setMaxNeutralLossesPerModification(Integer maxNeutralLossesPerModification) {
        this.maxNeutralLossesPerModification = maxNeutralLossesPerModification;
    }

    /**
     * Returns the minimum peptide length.
     *
     * @return the the minimum peptide length
     */
    public Integer getMinPeptideLength() {

        if (minPeptideLength == null) {
            minPeptideLength = 6;
        }

        return minPeptideLength;

    }

    /**
     * Set the minimum peptide length.
     *
     * @param minPeptideLength the minimum peptide length
     */
    public void setMinPeptideLength(Integer minPeptideLength) {
        this.minPeptideLength = minPeptideLength;
    }

    /**
     * Returns the maximum peptide length.
     *
     * @return the the maximum peptide length
     */
    public Integer getMaxPeptideLength() {

        if (maxPeptideLength == null) {
            maxPeptideLength = 30;
        }

        return maxPeptideLength;

    }

    /**
     * Set the maximum peptide length.
     *
     * @param maxPeptideLength the maximum peptide length
     */
    public void setMaxPeptideLength(Integer maxPeptideLength) {
        this.maxPeptideLength = maxPeptideLength;
    }

    /**
     * Returns the maximum number of proteins loaded into memory.
     *
     * @return the maximum number of proteins loaded into memory
     */
    public Integer getMaxLoadedProteins() {

        if (maxLoadedProteins == null) {
            maxLoadedProteins = 100000;
        }

        return maxLoadedProteins;

    }

    /**
     * Set the maximum number of proteins loaded into memory.
     *
     * @param maxLoadedProteins the maximum number of proteins loaded into
     * memory
     */
    public void setMaxLoadedProteins(Integer maxLoadedProteins) {
        this.maxLoadedProteins = maxLoadedProteins;
    }

    /**
     * Returns the maximum number of spectra loaded into memory.
     *
     * @return the maximum number of spectra loaded into memory
     */
    public Integer getMaxLoadedSpectra() {

        if (maxLoadedSpectra == null) {
            maxLoadedSpectra = 2000;
        }

        return maxLoadedSpectra;

    }

    /**
     * Set the maximum number of spectra loaded into memory.
     *
     * @param maxLoadedSpectra the maximum number of spectra loaded into memory
     */
    public void setMaxLoadedSpectra(Integer maxLoadedSpectra) {
        this.maxLoadedSpectra = maxLoadedSpectra;
    }

    /**
     * Returns the output format.
     *
     * @return the outputFormat
     */
    public String getOutputFormat() {

        if (outputFormat == null) {
            outputFormat = "csv";
        }

        return outputFormat;

    }

    /**
     * Set the output format.
     *
     * @param outputFormat the outputFormat to set
     */
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Returns true if target and decoy are ranked separately, false if shared
     * rank.
     *
     * @return true if target and decoy are ranked separately, false if shared
     * rank
     */
    public boolean reportBothBestHitsForTD() {

        if (reportBothBestHitsForTD == null) {
            reportBothBestHitsForTD = true;
        }

        return reportBothBestHitsForTD;

    }

    /**
     * Set if target and decoy are ranked separately or shared.
     *
     * @param reportBothBestHitsForTD the reportBothBestHitsForTD to set
     */
    public void setReportBothBestHitsForTD(boolean reportBothBestHitsForTD) {
        this.reportBothBestHitsForTD = reportBothBestHitsForTD;
    }

    /**
     * Returns the maximum allowed charge state.
     *
     * @return the maximum allowed charge state
     */
    public String getMaxAllowedChargeState() {

        if (maxAllowedChargeState == null) {
            maxAllowedChargeState = "+2";
        }

        return maxAllowedChargeState;

    }

    /**
     * Set the maximum allowed charge state.
     *
     * @param maxAllowedChargeState the max allowed charge state
     */
    public void setMaxAllowedChargeState(String maxAllowedChargeState) {
        this.maxAllowedChargeState = maxAllowedChargeState;
    }

    /**
     * Returns the minimum peak depth.
     *
     * @return the minimum peak depth
     */
    public Integer getMinPeakDepth() {

        if (minPeakDepth == null) {
            minPeakDepth = 1;
        }

        return minPeakDepth;

    }

    /**
     * Set the minimum peak depth.
     *
     * @param minPeakDepth the minimum peak depth
     */
    public void setMinPeakDepth(Integer minPeakDepth) {
        this.minPeakDepth = minPeakDepth;
    }

    /**
     * Returns the maximum peak depth.
     *
     * @return the maximum peak depth
     */
    public Integer getMaxPeakDepth() {
        return maxPeakDepth;
    }

    /**
     * Set the maximum peak depth.
     *
     * @param maxPeakDepth the maximum peak depth
     */
    public void setMaxPeakDepth(Integer maxPeakDepth) {
        this.maxPeakDepth = maxPeakDepth;
    }

    /**
     * Returns true if a second search is to be performed.
     *
     * @return true if a second search is to be performed
     */
    public Boolean getPerformSecondSearch() {

        if (performSecondSearch == null) {
            performSecondSearch = false;
        }

        return performSecondSearch;

    }

    /**
     * Set if a second search is to be performed.
     *
     * @param performSecondSearch whether a second search is to be performed
     */
    public void setPerformSecondSearch(Boolean performSecondSearch) {
        this.performSecondSearch = performSecondSearch;
    }

    /**
     * Returns true if the Y1 ions are to be kept.
     *
     * @return true if the Y1 ions are to be kept
     */
    public Boolean getKeepY1Ion() {

        if (keepY1Ion == null) {
            keepY1Ion = true;
        }

        return keepY1Ion;

    }

    /**
     * Set whether the Y1 ions are to be kept.
     *
     * @param keepY1Ion whether the Y1 ions are to be kept
     */
    public void setKeepY1Ion(Boolean keepY1Ion) {
        this.keepY1Ion = keepY1Ion;
    }

    /**
     * Returns true if the water losses are to be removed.
     *
     * @return true if the water losses are to be removed
     */
    public Boolean getRemoveWaterLosses() {

        if (removeWaterLosses == null) {
            removeWaterLosses = true;
        }

        return removeWaterLosses;

    }

    /**
     * Set whether the water losses are to be removed.
     *
     * @param removeWaterLosses whether the water losses are to be removed
     */
    public void setRemoveWaterLosses(Boolean removeWaterLosses) {
        this.removeWaterLosses = removeWaterLosses;
    }

    /**
     * Returns true if the ammonia losses are to be removed.
     *
     * @return the removeAmmoniaLosses
     */
    public Boolean getRemoveAmmoniaLosses() {

        if (removeAmmoniaLosses == null) {
            removeAmmoniaLosses = true;
        }

        return removeAmmoniaLosses;

    }

    /**
     * Set whether the ammonia losses are to be removed.
     *
     * @param removeAmmoniaLosses whether the ammonia losses are to be removed
     */
    public void setRemoveAmmoniaLosses(Boolean removeAmmoniaLosses) {
        this.removeAmmoniaLosses = removeAmmoniaLosses;
    }

    /**
     * Returns true if the first precursor is to be excluded.
     *
     * @return true if the first precursor is to be excluded
     */
    public Boolean getExcludeFirstPrecursor() {

        if (excludeFirstPrecursor == null) {
            excludeFirstPrecursor = true;
        }

        return excludeFirstPrecursor;

    }

    /**
     * Set whether the first precursor is to be excluded.
     *
     * @param excludeFirstPrecursor whether the first precursor is to be
     * excluded
     */
    public void setExcludeFirstPrecursor(Boolean excludeFirstPrecursor) {
        this.excludeFirstPrecursor = excludeFirstPrecursor;
    }

    /**
     * Returns the maximum number of precursors.
     *
     * @return the maximum number of precursors
     */
    public Integer getMaxMultiplePrecursors() {

        if (maxMultiplePrecursors == null) {
            maxMultiplePrecursors = 5;
        }

        return maxMultiplePrecursors;

    }

    /**
     * Set the maximum number of precursors.
     *
     * @param maxMultiplePrecursors the maximum number of precursors
     */
    public void setMaxMultiplePrecursors(Integer maxMultiplePrecursors) {
        this.maxMultiplePrecursors = maxMultiplePrecursors;
    }

    /**
     * Returns the considered charges for precursors.
     *
     * @return the considered charges for precursors
     */
    public String getConsideredChargesForPrecursors() {

        if (consideredChargesForPrecursors == null) {
            consideredChargesForPrecursors = "+2,+3";
        }

        return consideredChargesForPrecursors;

    }

    /**
     * Set the considered charges for precursors.
     *
     * @param consideredChargesForPrecursors the considered charges for
     * precursors
     */
    public void setConsideredChargesForPrecursors(String consideredChargesForPrecursors) {
        this.consideredChargesForPrecursors = consideredChargesForPrecursors;
    }

    /**
     * Returns true if considered charges are combined in one result.
     *
     * @return true if considered charges are combined in one result
     */
    public Boolean getCombineConsideredCharges() {

        if (combineConsideredCharges == null) {
            combineConsideredCharges = true;
        }

        return combineConsideredCharges;

    }

    /**
     * Set if considered charges are combined in one result.
     *
     * @param combineConsideredCharges combine considered charges in one result
     */
    public void setCombineConsideredCharges(Boolean combineConsideredCharges) {
        this.combineConsideredCharges = combineConsideredCharges;
    }

    /**
     * Returns true if Percolator is to be run.
     *
     * @return true if Percolator is to be run
     */
    public Boolean getRunPercolator() {

        if (runPercolator == null) {
            runPercolator = false;
        }

        return runPercolator;

    }

    /**
     * Set if Percolator is to be run.
     *
     * @param runPercolator if Percolator is to be run
     */
    public void setRunPercolator(Boolean runPercolator) {
        this.runPercolator = runPercolator;
    }

    /**
     * Returns true if a PIn file is to be generated.
     *
     * @return true if a PIn file is to be generated
     */
    public Boolean getGeneratePInFile() {

        if (generatePInFile == null) {
            generatePInFile = false;
        }

        return generatePInFile;

    }

    /**
     * Set whether a PIn file is to be generated.
     *
     * @param generatePInFile hether a PIn file is to be generated
     */
    public void setGeneratePInFile(Boolean generatePInFile) {
        this.generatePInFile = generatePInFile;
    }

}
