package com.compomics.util.experiment.identification.identification_parameters.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;

/**
 * The X!Tandem specific parameters.
 *
 * @author Marc Vaudel
 */
public class XtandemParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = -5898951075262732261L;
    /**
     * Maximal e-value cut-off.
     */
    private Double maxEValue = 100.0;
    /**
     * The dynamic range for spectrum filtering. When the highest peak is given
     * the dynamic range value peaks smaller than one are ignored. e.g. for 100
     * peaks with an intensity &lt;1% of the highest peak are ignored.
     */
    private Double dynamicRange = 100.0;
    /**
     * The number of most intense peaks to consider.
     */
    private Integer nPeaks = 50;
    /**
     * The minimum precursor mass.
     */
    private Double minPrecursorMass = 500.0;
    /**
     * The minimum fragment mass.
     */
    private Double minFragmentMz = 200.0;
    /**
     * The minimum number of peaks per spectrum.
     */
    private Integer minPeaksPerSpectrum = 5;
    /**
     * Indicates whether the protein quick acetylation option should be
     * triggered.
     */
    private Boolean proteinQuickAcetyl = true;
    /**
     * Indicates whether the quick pyrolidone option should be triggered.
     */
    private Boolean quickPyrolidone = true;
    /**
     * Triggers the refinement process.
     */
    private Boolean refine = true;
    /**
     * Sets whether semi enzymatic peptides should be search for during the
     * refinement process.
     */
    private Boolean refineSemi = false;
    /**
     * Sets whether point mutations should be search for during the refinement
     * process.
     */
    private Boolean refinePointMutations = false;
    /**
     * Sets whether the spectrum synthesis option should be used during the
     * refinement process.
     */
    private Boolean refineSpectrumSynthesis = true;
    /**
     * Sets whether unexpected cleavages should be search for during the
     * refinement process.
     */
    private Boolean refineUnanticipatedCleavages = true;
    /**
     * Indicates whether snAPs should be used during the refinement process.
     */
    private Boolean refineSnaps = true;
    /**
     * The maximum expectation value for a hit to be considered during the
     * refinement process.
     */
    private Double maximumExpectationValueRefinement = 0.01;
    /**
     * Sets the modifications to be used during the refinement process.
     */
    private Boolean potentialModificationsForFullRefinment = false;
    /**
     * The skyline path.
     */
    private String skylinePath = "";
    /**
     * If true protein details will be exported to the to the result file.
     */
    private Boolean outputProteins = true;
    /**
     * If true protein sequences will be added to the protein details to the
     * result file.
     */
    private boolean outputSequences = false;
    /**
     * If true spectra will be exported to the result file.
     */
    private Boolean outputSpectra = true;
    /**
     * If true histograms will be exported to the result file.
     */
    private Boolean outputHistograms = false;
    /**
     * Indicates whether the phospho stp bias option should be triggered.
     */
    private Boolean stpBias = false;
    /**
     * Triggers the noise suppression function.
     */
    private Boolean useNoiseSuppression = false;

    /**
     * Constructor.
     */
    public XtandemParameters() {

    }

    /**
     * Returns the dynamic range for spectrum filtering.
     *
     * @return the dynamic range for spectrum filtering
     */
    public Double getDynamicRange() {
        return dynamicRange;
    }

    /**
     * Sets the dynamic range for spectrum filtering.
     *
     * @param dynamicRange the dynamic range for spectrum filtering
     */
    public void setDynamicRange(Double dynamicRange) {
        this.dynamicRange = dynamicRange;
    }

    /**
     * Returns the number of most intense peaks to consider.
     *
     * @return the number of most intense peaks to consider
     */
    public Integer getnPeaks() {
        return nPeaks;
    }

    /**
     * Sets the number of most intense peaks to consider.
     *
     * @param nPeaks the number of most intense peaks to consider
     */
    public void setnPeaks(Integer nPeaks) {
        this.nPeaks = nPeaks;
    }

    /**
     * Returns the minimal precursor mass.
     *
     * @return the minimal precursor mass
     */
    public Double getMinPrecursorMass() {
        return minPrecursorMass;
    }

    /**
     * Sets the minimal precursor mass.
     *
     * @param minPrecursorMass the minimal precursor mass
     */
    public void setMinPrecursorMass(Double minPrecursorMass) {
        this.minPrecursorMass = minPrecursorMass;
    }

    /**
     * Returns the minimal fragment m/z.
     *
     * @return the minimal fragment m/z
     */
    public Double getMinFragmentMz() {
        return minFragmentMz;
    }

    /**
     * Sets the minimal fragment m/z.
     *
     * @param minFragmentMz the minimal fragment m/z
     */
    public void setMinFragmentMz(Double minFragmentMz) {
        this.minFragmentMz = minFragmentMz;
    }

    /**
     * Returns the minimal number of peaks per spectrum.
     *
     * @return the minimal number of peaks per spectrum
     */
    public Integer getMinPeaksPerSpectrum() {
        return minPeaksPerSpectrum;
    }

    /**
     * Sets the minimal number of peaks per spectrum.
     *
     * @param minPeaksPerSpectrum the minimal number of peaks per spectrum
     */
    public void setMinPeaksPerSpectrum(Integer minPeaksPerSpectrum) {
        this.minPeaksPerSpectrum = minPeaksPerSpectrum;
    }

    /**
     * Indicates whether the protein quick acetylation option should be
     * triggered.
     *
     * @return true if the protein quick acetylation option should be triggered
     */
    public Boolean isProteinQuickAcetyl() {
        return proteinQuickAcetyl;
    }

    /**
     * Sets whether the protein quick acetylation option should be triggered.
     *
     * @param proteinQuickAcetyl true if the protein quick acetylation option
     * should be triggered
     */
    public void setProteinQuickAcetyl(Boolean proteinQuickAcetyl) {
        this.proteinQuickAcetyl = proteinQuickAcetyl;
    }

    /**
     * Returns whether the quick pyrolidone option should be triggered.
     *
     * @return true if the quick pyrolidone option should be triggered
     */
    public Boolean isQuickPyrolidone() {
        return quickPyrolidone;
    }

    /**
     * Sets whether the quick pyrolidone option should be triggered.
     *
     * @param quickPyrolidone the quick pyrolidone option should be triggered
     */
    public void setQuickPyrolidone(Boolean quickPyrolidone) {
        this.quickPyrolidone = quickPyrolidone;
    }

    /**
     * Returns whether the second pass search should be triggered.
     *
     * @return true if the second pass search should be triggered
     */
    public Boolean isRefine() {
        return refine;
    }

    /**
     * Sets whether the second pass search should be triggered.
     *
     * @param refine true if the second pass search should be triggered
     */
    public void setRefine(Boolean refine) {
        this.refine = refine;
    }

    /**
     * Returns whether the stP bias should be triggered.
     *
     * @return true if the stP bias should be triggered
     */
    public Boolean isStpBias() {
        return stpBias;
    }

    /**
     * Sets whether the stP bias should be triggered
     *
     * @param stpBias true if the stP bias should be triggered
     */
    public void setStpBias(Boolean stpBias) {
        this.stpBias = stpBias;
    }

    /**
     * Returns the maximal e-value searched for.
     *
     * @return the maximal e-value searched for
     */
    public Double getMaxEValue() {
        return maxEValue;
    }

    /**
     * Sets the maximal e-value searched for.
     *
     * @param maxEValue the maximal e-value searched for
     */
    public void setMaxEValue(Double maxEValue) {
        this.maxEValue = maxEValue;
    }

    /**
     * Indicates whether the semi enzymatic option of the second pass search
     * should be triggered.
     *
     * @return true if the semi enzymatic option of the second pass search
     * should be triggered
     */
    public Boolean isRefineSemi() {
        return refineSemi;
    }

    /**
     * Sets whether the semi enzymatic option of the second pass search should
     * be triggered.
     *
     * @param refineSemi true if the semi enzymatic option of the second pass
     * search should be triggered
     */
    public void setRefineSemi(Boolean refineSemi) {
        this.refineSemi = refineSemi;
    }

    /**
     * Indicates whether point mutations should be looked for during the
     * refinement process.
     *
     * @return true if point mutations should be looked for during the
     * refinement process
     */
    public Boolean isRefinePointMutations() {
        return refinePointMutations;
    }

    /**
     * Sets whether point mutations should be looked for during the refinement
     * process.
     *
     * @param refinePointMutations true if point mutations should be looked for
     * during the refinement process
     */
    public void setRefinePointMutations(Boolean refinePointMutations) {
        this.refinePointMutations = refinePointMutations;
    }

    /**
     * Indicates whether the spectrum synthesis option should be used during the
     * refinement process.
     *
     * @return true if the spectrum synthesis option should be used during the
     * refinement process
     */
    public Boolean isRefineSpectrumSynthesis() {
        return refineSpectrumSynthesis;
    }

    /**
     * Sets whether the spectrum synthesis option should be used during the
     * refinement process.
     *
     * @param refineSpectrumSynthesis true if the spectrum synthesis option
     * should be used during the refinement process
     */
    public void setRefineSpectrumSynthesis(Boolean refineSpectrumSynthesis) {
        this.refineSpectrumSynthesis = refineSpectrumSynthesis;
    }

    /**
     * Returns whether the unanticipated cleavages option should be used during
     * the refinement process.
     *
     * @return true if the unanticipated cleavages option should be used during
     * the refinement process
     */
    public Boolean isRefineUnanticipatedCleavages() {
        return refineUnanticipatedCleavages;
    }

    /**
     * Sets whether the unanticipated cleavages option should be used during the
     * refinement process.
     *
     * @param refineUnanticipatedCleavages true if the unanticipated cleavages
     * option should be used during the refinement process
     */
    public void setRefineUnanticipatedCleavages(Boolean refineUnanticipatedCleavages) {
        this.refineUnanticipatedCleavages = refineUnanticipatedCleavages;
    }

    /**
     * Returns the maximum expectation value to use for refinement.
     *
     * @return the maximum expectation value to use for refinement
     */
    public Double getMaximumExpectationValueRefinement() {
        return maximumExpectationValueRefinement;
    }

    /**
     * Sets the maximum expectation value to use for refinement.
     *
     * @param maximumExpectationValue the maximum expectation value to use for
     * refinement
     */
    public void setMaximumExpectationValueRefinement(Double maximumExpectationValue) {
        this.maximumExpectationValueRefinement = maximumExpectationValue;
    }

    /**
     * Indicates whether the refinement modifications should be used for the
     * full refinement.
     *
     * @return true if the refinement modifications should be used for the full
     * refinement
     */
    public Boolean isPotentialModificationsForFullRefinment() {
        return potentialModificationsForFullRefinment;
    }

    /**
     * Sets whether the refinement modifications should be used for the full
     * refinement
     *
     * @param potentialModificationsForFullRefinment true if the refinement
     * modifications should be used for the full refinement
     */
    public void setPotentialModificationsForFullRefinment(Boolean potentialModificationsForFullRefinment) {
        this.potentialModificationsForFullRefinment = potentialModificationsForFullRefinment;
    }

    /**
     * Returns the skyline path.
     *
     * @return the skyline path
     */
    public String getSkylinePath() {
        return skylinePath;
    }

    /**
     * Sets the skyline path.
     *
     * @param skylinePath the skyline path
     */
    public void setSkylinePath(String skylinePath) {
        this.skylinePath = skylinePath;
    }

    /**
     * Indicates whether the protein bloc should be included in the export.
     *
     * @return true if the protein bloc should be included in the export
     */
    public Boolean isOutputProteins() {
        return outputProteins;
    }

    /**
     * Sets whether the protein bloc should be included in the export.
     *
     * @param outputProteins the protein bloc should be included in the export
     */
    public void setOutputProteins(Boolean outputProteins) {
        this.outputProteins = outputProteins;
    }

    /**
     * Returns whether the protein sequences should be included in the protein
     * block of the export.
     *
     * @return true if the protein sequences should be included in the protein
     * block of the export
     */
    public Boolean isOutputSequences() {
        return outputSequences;
    }

    /**
     * Sets whether the protein sequences should be included in the protein
     * block of the export.
     *
     * @param outputSequences true if the protein sequences should be included
     * in the protein block of the export
     */
    public void setOutputSequences(boolean outputSequences) {
        this.outputSequences = outputSequences;
    }

    /**
     * Indicate whether the spectra should be exported in the result file.
     *
     * @return true if the spectra should be exported in the result file
     */
    public Boolean isOutputSpectra() {
        return outputSpectra;
    }

    /**
     * Sets whether the spectra should be exported in the result file.
     *
     * @param outputSpectra true if the spectra should be exported in the result
     * file
     */
    public void setOutputSpectra(Boolean outputSpectra) {
        this.outputSpectra = outputSpectra;
    }

    /**
     * Indicates whether histograms should be written in the result file.
     *
     * @return true if histograms should be written in the result file
     */
    public Boolean isOutputHistograms() {
        return outputHistograms;
    }

    /**
     * Sets whether histograms should be written in the result file
     *
     * @param outputHistograms true if histograms should be written in the
     * result file
     */
    public void setOutputHistograms(Boolean outputHistograms) {
        this.outputHistograms = outputHistograms;
    }

    /**
     * Indicates whether noise suppression should be used when importing
     * spectra.
     *
     * @return true if noise suppression should be used when importing spectra
     */
    public Boolean isUseNoiseSuppression() {
        return useNoiseSuppression;
    }

    /**
     * Sets whether noise suppression should be used when importing spectra.
     *
     * @param useNoiseSuppression true if noise suppression should be used when
     * importing spectra
     */
    public void setUseNoiseSuppression(Boolean useNoiseSuppression) {
        this.useNoiseSuppression = useNoiseSuppression;
    }

    /**
     * Sets whether snAPs should be used during the refinement process.
     *
     * @return true if snAPs should be used during the refinement process
     */
    public Boolean isRefineSnaps() {
        return refineSnaps;
    }

    /**
     * Sets whether snAPs should be used during the refinement process.
     *
     * @param refineSnaps true if snAPs should be used during the refinement
     * process
     */
    public void setRefineSnaps(Boolean refineSnaps) {
        this.refineSnaps = refineSnaps;
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.xtandem;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {
        
        if (identificationAlgorithmParameter instanceof XtandemParameters) {
            XtandemParameters xtandemParameters = (XtandemParameters) identificationAlgorithmParameter;
            double diff = Math.abs(maxEValue - xtandemParameters.getMaxEValue());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(dynamicRange - xtandemParameters.getDynamicRange());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!getnPeaks().equals(xtandemParameters.getnPeaks())) {
                return false;
            }
            diff = Math.abs(minPrecursorMass - xtandemParameters.getMinPrecursorMass());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(minFragmentMz - xtandemParameters.getMinFragmentMz());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!getMinPeaksPerSpectrum().equals(xtandemParameters.getMinPeaksPerSpectrum())) {
                return false;
            }
            if (!isProteinQuickAcetyl().equals(xtandemParameters.isProteinQuickAcetyl())) {
                return false;
            }
            if (!isQuickPyrolidone().equals(xtandemParameters.isQuickPyrolidone())) {
                return false;
            }
            if (!isRefine().equals(xtandemParameters.isRefine())) {
                return false;
            }
            if (!isRefineSemi().equals(xtandemParameters.isRefineSemi())) {
                return false;
            }
            if (!isRefinePointMutations().equals(xtandemParameters.isRefinePointMutations())) {
                return false;
            }
            if (!isRefineSpectrumSynthesis().equals(xtandemParameters.isRefineSpectrumSynthesis())) {
                return false;
            }
            if (!isRefineUnanticipatedCleavages().equals(xtandemParameters.isRefineUnanticipatedCleavages())) {
                return false;
            }
            if (!isRefineSnaps().equals(xtandemParameters.isRefineSnaps())) {
                return false;
            }
            diff = Math.abs(maximumExpectationValueRefinement - xtandemParameters.getMaximumExpectationValueRefinement());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!isPotentialModificationsForFullRefinment().equals(xtandemParameters.isPotentialModificationsForFullRefinment())) {
                return false;
            }
            if (!getSkylinePath().equals(xtandemParameters.getSkylinePath())) {
                return false;
            }
            if (!isOutputProteins().equals(xtandemParameters.isOutputProteins())) {
                return false;
            }
            if (!isOutputSpectra().equals(xtandemParameters.isOutputSpectra())) {
                return false;
            }
            if (!isOutputSequences().equals(xtandemParameters.isOutputSequences())) {
                return false;
            }
            if (!isOutputHistograms().equals(xtandemParameters.isOutputHistograms())) {
                return false;
            }
            if (!isStpBias().equals(xtandemParameters.isStpBias())) {
                return false;
            }
            if (!isUseNoiseSuppression().equals(xtandemParameters.isUseNoiseSuppression())) {
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

        output.append("DYNAMIC_RANGE=");
        output.append(dynamicRange);
        output.append(newLine);

        output.append("NUMBER_OF_PEAKS=");
        output.append(nPeaks);
        output.append(newLine);

        output.append("MIN_FRAG_MASS=");
        output.append(minFragmentMz);
        output.append(newLine);

        output.append("MIN_NUMBER_OF_PEAKS=");
        output.append(minPeaksPerSpectrum);
        output.append(newLine);

        output.append("NOISE_SUPPRESSION=");
        if (useNoiseSuppression) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("MIN_PREC_MASS=");
        output.append(minPrecursorMass);
        output.append(newLine);

        output.append("PROTEIN_QUICK_ACETYL=");
        if (proteinQuickAcetyl) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("QUICK_PYROLIDONE=");
        if (quickPyrolidone) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("STP_BIAS=");
        if (stpBias) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("REFINE=");
        if (refine) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("REFINE_SEMI=");
        if (refineSemi) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("REFINE_POINT_MUTATIONS=");
        if (refinePointMutations) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("REFINE_SPECTRUM_SYNTHESIS=");
        if (refineSpectrumSynthesis) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("REFINE_UNANTICIPATED_CLEABAGES=");
        if (refineUnanticipatedCleavages) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("REFINE_SNAPS=");
        if (refineSnaps) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("REFINE_MAX_EVALUE=");
        output.append(maximumExpectationValueRefinement);
        output.append(newLine);

        output.append("POTENTIAL_MODIFICATIONS_FOR_FULL_REFINEMENT=");
        if (potentialModificationsForFullRefinment) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("EVALUE_CUTOFF=");
        output.append(maxEValue);
        output.append(newLine);

        output.append("SKYLINE_PATH=");
        output.append(skylinePath);
        output.append(newLine);

        output.append("OUTPUT_PROTEINS=");
        if (outputProteins) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("OUTPUT_SEQUENCES=");
        if (outputSequences) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("OUTPUT_SPECTRA=");
        if (outputSpectra) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        output.append("OUTPUT_HISTOGRAMS=");
        if (outputHistograms) {
            output.append("YES");
        } else {
            output.append("NO");
        }
        output.append(newLine);

        return output.toString();
    }
}
