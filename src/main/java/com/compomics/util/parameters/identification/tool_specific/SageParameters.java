package com.compomics.util.parameters.identification.tool_specific;

import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;

/**
 * The Sage specific parameters.
 *
 * @author Harald Barsnes
 */
public class SageParameters extends ExperimentObject implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = 7440784691598087741L;
    /**
     * The bucket size, i.e. the number of fragments in each internal mass
     * bucket.
     */
    private Integer bucketSize = 32768;
    /**
     * The minimum fragment mz.
     */
    private Double minFragmentMz = 200.0;
    /**
     * The maximum fragment mz.
     */
    private Double maxFragmentMz = 2000.0;
    /**
     * The minimum peptide mass.
     */
    private Double minPeptideMass = 600.0;
    /**
     * The maximum peptide mass.
     */
    private Double maxPeptideMass = 5000.0;
    /**
     * Sage minimum ion index for the preliminary search, default is '2', i.e.
     * skip b1/b2/y1/y2 ions.
     */
    private Integer minIonIndex = 2;
    /**
     * The maximum number of variable modifications on the same peptide.
     */
    private Integer maxVariableMods = 2;
    /**
     * Sage decoy tag.
     *
     * @deprecated use the decoy tag from FastaParameters instead
     */
    private String decoyTag = "_REVERSED";  // @TODO: remove next time we break backwards compatibility
    /**
     * Set whether decoys are to be generated.
     */
    private Boolean generateDecoys = false;
    /**
     * The type of TMT used, has to be one of the following: Tmt6, Tmt10, Tmt11,
     * Tmt16 or Tmt18.
     */
    private String tmtType = null;
    /**
     * The MS-level to perform TMT quantification on.
     */
    private Integer tmtLevel = 3;
    /**
     * Use Signal/Noise instead of intensity for TMT quant. Requires noise
     * values in mzML.
     */
    private Boolean tmtSn = false;
    /**
     * Set whether LFQ is to be performed.
     */
    private Boolean performLfq = false;
    /**
     * The method used for scoring peaks in LFQ: Hybrid, RetentionTime or
     * SpectralAngle.
     */
    private String lfqPeakScoring = "Hybrid";
    /**
     * The method used for integrating peak intensities, either Sum or Max.
     */
    private String lfqIntergration = "Sum";
    /**
     * Threshold for the spectral angle similarity measure, ranging from 0 to 1.
     */
    private Double lfqSpectralAngle = 0.7;
    /**
     * Tolerance for matching MS1 ions for DICE window around calculated
     * precursor mass.
     */
    private Double lfqPpmTolerance = 5.0;
    /**
     * If true, LFQ is performed on the peptide-level, where all charge states
     * are treated as the same precursor. If false, LFQ is performed on the
     * peptide-charge-level, where each charge state will be treated separately.
     */
    private Boolean combineChargeStates = true;
    /**
     * Set whether deisotoping and charge state deconvolution is to be
     * performed.
     */
    private Boolean deisotope = false;
    /**
     * Search for chimeric/co-fragmenting PSMS.
     */
    private Boolean chimera = false;
    /**
     * Ignore precursor_tol and search in wide-window/DIA mode.
     */
    private Boolean wideWindow = false;
    /**
     * Use of retention time prediction model as an feature for LDA.
     */
    private Boolean predictRt = true;
    /**
     * The minimum number of peaks for a spectrum.
     */
    private Integer minPeaks = 15;
    /**
     * The maximum number of peaks for a spectrum.
     */
    private Integer maxPeaks = 150;
    /**
     * The minimum number of matched b+y ions to use for reporting PSMs.
     */
    private Integer minMatchedPeaks = 4;
    /**
     * The maximum fragment charge.
     */
    private Integer maxFragmentCharge = null;
    /**
     * The number of PSMs to report for each spectra. Recommend setting to 1,
     * higher values might disrupt LDA.
     */
    private Integer numPsmsPerSpectrum = 1;
    /**
     * Number of files to search in parallel (default = number of CPUs/2).
     */
    private Integer batchSize = null;
    /**
     * The minimum peptide length.
     */
    private Integer minPeptideLength = 8;
    /**
     * The maximal peptide length.
     */
    private Integer maxPeptideLength = 30;

    /**
     * Constructor.
     */
    public SageParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.sage;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof SageParameters) {

            SageParameters sageParameters = (SageParameters) identificationAlgorithmParameter;

            if (!bucketSize.equals(sageParameters.getBucketSize())) {
                return false;
            }
            if (!minPeptideLength.equals(sageParameters.getMinPeptideLength())) {
                return false;
            }
            if (!maxPeptideLength.equals(sageParameters.getMaxPeptideLength())) {
                return false;
            }
            double diff = Math.abs(minFragmentMz - sageParameters.getMinFragmentMz());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(maxFragmentMz - sageParameters.getMaxFragmentMz());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(minPeptideMass - sageParameters.getMinPeptideMass());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(maxPeptideMass - sageParameters.getMaxPeptideMass());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!minIonIndex.equals(sageParameters.getMinIonIndex())) {
                return false;
            }
            if (!getMaxVariableMods().equals(sageParameters.getMaxVariableMods())) {
                return false;
            }
//            if (!decoyTag.equals(sageParameters.getDecoyTag())) {
//                return false;
//            }
            if (!generateDecoys.equals(sageParameters.getGenerateDecoys())) {
                return false;
            }
            if ((tmtType == null && sageParameters.getTmtType() != null)
                    || (tmtType != null && sageParameters.getTmtType() == null)) {
                return false;
            }
            if ((tmtType != null && sageParameters.getTmtType() != null)
                    && (!tmtType.equalsIgnoreCase(sageParameters.getTmtType()))) {
                return false;
            }
            if ((tmtLevel == null && sageParameters.getTmtLevel() != null)
                    || (tmtLevel != null && sageParameters.getTmtLevel() == null)) {
                return false;
            }
            if ((tmtLevel != null && sageParameters.getTmtLevel() != null)
                    && (!tmtLevel.equals(sageParameters.getTmtLevel()))) {
                return false;
            }
            if (!getTmtSn().equals(sageParameters.getTmtSn())) {
                return false;
            }
            if (!performLfq.equals(sageParameters.getPerformLfq())) {
                return false;
            }
            if ((lfqPeakScoring == null && sageParameters.getLfqPeakScoring() != null)
                    || (lfqPeakScoring != null && sageParameters.getLfqPeakScoring() == null)) {
                return false;
            }
            if ((lfqPeakScoring != null && sageParameters.getLfqPeakScoring() != null)
                    && (!lfqPeakScoring.equalsIgnoreCase(sageParameters.getLfqPeakScoring()))) {
                return false;
            }
            if ((lfqIntergration == null && sageParameters.getLfqIntergration() != null)
                    || (lfqIntergration != null && sageParameters.getLfqIntergration() == null)) {
                return false;
            }
            if ((lfqIntergration != null && sageParameters.getLfqIntergration() != null)
                    && (!lfqIntergration.equalsIgnoreCase(sageParameters.getLfqIntergration()))) {
                return false;
            }
            diff = Math.abs(getLfqSpectralAngle() - sageParameters.getLfqSpectralAngle());
            if (diff > 0.0000000000001) {
                return false;
            }
            diff = Math.abs(getLfqPpmTolerance() - sageParameters.getLfqPpmTolerance());
            if (diff > 0.0000000000001) {
                return false;
            }
            if (!getCombineChargeStates().equals(sageParameters.getCombineChargeStates())) {
                return false;
            }
            if (!deisotope.equals(sageParameters.getDeisotope())) {
                return false;
            }
            if (!chimera.equals(sageParameters.getChimera())) {
                return false;
            }
            if (!getWideWindow().equals(sageParameters.getWideWindow())) {
                return false;
            }
            if (!predictRt.equals(sageParameters.getPredictRt())) {
                return false;
            }
            if (!minPeaks.equals(sageParameters.getMinPeaks())) {
                return false;
            }
            if (!maxPeaks.equals(sageParameters.getMaxPeaks())) {
                return false;
            }
            if ((minMatchedPeaks == null && sageParameters.getMinMatchedPeaks() != null)
                    || (minMatchedPeaks != null && sageParameters.getMinMatchedPeaks() == null)) {
                return false;
            }
            if ((minMatchedPeaks != null && sageParameters.getMinMatchedPeaks() != null)
                    && (!minMatchedPeaks.equals(sageParameters.getMinMatchedPeaks()))) {
                return false;
            }
            if ((maxFragmentCharge == null && sageParameters.getMaxFragmentCharge() != null)
                    || (maxFragmentCharge != null && sageParameters.getMaxFragmentCharge() == null)) {
                return false;
            }
            if ((maxFragmentCharge != null && sageParameters.getMaxFragmentCharge() != null)
                    && (!maxFragmentCharge.equals(sageParameters.getMaxFragmentCharge()))) {
                return false;
            }
            if (!numPsmsPerSpectrum.equals(sageParameters.getNumPsmsPerSpectrum())) {
                return false;
            }
            if ((getBatchSize() == null && sageParameters.getBatchSize() != null)
                    || (getBatchSize() != null && sageParameters.getBatchSize() == null)) {
                return false;
            }
            if ((getBatchSize() != null && sageParameters.getBatchSize() != null)
                    && (!getBatchSize().equals(sageParameters.getBatchSize()))) {
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

        output.append("BUCKET_SIZE=");
        output.append(bucketSize);
        output.append(newLine);
        output.append("MIN_PEP_LENGTH=");
        output.append(minPeptideLength);
        output.append(newLine);
        output.append("MAX_PEP_LENGTH=");
        output.append(maxPeptideLength);
        output.append(newLine);
        output.append("MIN_FRAGMENT_MZ=");
        output.append(minFragmentMz);
        output.append(newLine);
        output.append("MAX_FRAGMENT_MZ=");
        output.append(maxFragmentMz);
        output.append(newLine);
        output.append("MIN_PEPTIDE_MASS=");
        output.append(minPeptideMass);
        output.append(newLine);
        output.append("MAX_PEPTIDE_MASS=");
        output.append(maxPeptideMass);
        output.append(newLine);
        output.append("MIN_ION_INDEX=");
        output.append(minIonIndex);
        output.append(newLine);
        output.append("MAX_VARIABLE_MODS=");
        output.append(maxVariableMods);
        output.append(newLine);
//        output.append("DECOY_TAG=");
//        output.append(decoyTag);
//        output.append(newLine);
        output.append("GENERATE_DECOYS=");
        output.append(generateDecoys);
        output.append(newLine);
        output.append("TMT_TYPE=");
        output.append(tmtType);
        output.append(newLine);
        output.append("TMT_LEVEL=");
        output.append(tmtLevel);
        output.append(newLine);
        output.append("TMT_SN=");
        output.append(tmtSn);
        output.append(newLine);
        output.append("LFQ=");
        output.append(performLfq);
        output.append(newLine);
        output.append("LFQ_PEAK_SCORING=");
        output.append(lfqPeakScoring);
        output.append(newLine);
        output.append("LFQ_INTEGRATION=");
        output.append(lfqIntergration);
        output.append(newLine);
        output.append("LFQ_SPECTRAL_ANGLE=");
        output.append(lfqSpectralAngle);
        output.append(newLine);
        output.append("LFQ_PPM_TOLERANCE=");
        output.append(lfqPpmTolerance);
        output.append(newLine);
        output.append("LFQ_COMBINE_CHARGE_STATES=");
        output.append(getCombineChargeStates());
        output.append(newLine);
        output.append("DEISOTOPE=");
        output.append(deisotope);
        output.append(newLine);
        output.append("CHIMERA=");
        output.append(chimera);
        output.append(newLine);
        output.append("WIDE_WINDOW=");
        output.append(wideWindow);
        output.append(newLine);
        output.append("PREDICT_RT=");
        output.append(predictRt);
        output.append(newLine);
        output.append("MIN_PEAKS=");
        output.append(minPeaks);
        output.append(newLine);
        output.append("MAX_PEAKS=");
        output.append(maxPeaks);
        output.append(newLine);
        output.append("MIN_MATCHED_PEAKS=");
        output.append(minMatchedPeaks);
        output.append(newLine);
        output.append("MAX_FRAGMENT_CHARGE=");
        output.append(maxFragmentCharge);
        output.append(newLine);
        output.append("NUM_PSMS_PER_SPECTRUM=");
        output.append(numPsmsPerSpectrum);
        output.append(newLine);
        output.append("BATCH_SIZE=");
        output.append(batchSize);
        output.append(newLine);

        return output.toString();
    }

    /**
     * Returns the maximal peptide length allowed.
     *
     * @return the maximal peptide length allowed
     */
    public Integer getMaxPeptideLength() {
        return maxPeptideLength;
    }

    /**
     * Sets the maximal peptide length allowed.
     *
     * @param maxPeptideLength the maximal peptide length allowed
     */
    public void setMaxPeptideLength(int maxPeptideLength) {
        this.maxPeptideLength = maxPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @return the minimal peptide length allowed
     */
    public Integer getMinPeptideLength() {
        return minPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @param minPeptideLength the minimal peptide length allowed
     */
    public void setMinPeptideLength(int minPeptideLength) {
        this.minPeptideLength = minPeptideLength;
    }

    /**
     * Returns the bucket size.
     *
     * @return the bucketSize
     */
    public Integer getBucketSize() {
        return bucketSize;
    }

    /**
     * Set the bucket size.
     *
     * @param bucketSize the bucketSize to set
     */
    public void setBucketSize(Integer bucketSize) {
        this.bucketSize = bucketSize;
    }

    /**
     * Returns the minimum fragment mz.
     *
     * @return the minFragmentMz
     */
    public Double getMinFragmentMz() {
        return minFragmentMz;
    }

    /**
     * Set the minimum fragment mz.
     *
     * @param minFragmentMz the minFragmentMz to set
     */
    public void setMinFragmentMz(Double minFragmentMz) {
        this.minFragmentMz = minFragmentMz;
    }

    /**
     * Returns the maximum fragment mz.
     *
     * @return the maxFragmentMz
     */
    public Double getMaxFragmentMz() {
        return maxFragmentMz;
    }

    /**
     * Set the maximum fragment mz.
     *
     * @param maxFragmentMz the maxFragmentMz to set
     */
    public void setMaxFragmentMz(Double maxFragmentMz) {
        this.maxFragmentMz = maxFragmentMz;
    }

    /**
     * Returns the minimum peptide mass.
     *
     * @return the minPeptideMass
     */
    public Double getMinPeptideMass() {
        return minPeptideMass;
    }

    /**
     * Set the minimum peptide mass.
     *
     * @param minPeptideMass the minPeptideMass to set
     */
    public void setMinPeptideMass(Double minPeptideMass) {
        this.minPeptideMass = minPeptideMass;
    }

    /**
     * Returns the maximum peptide mass.
     *
     * @return the maxPeptideMass
     */
    public Double getMaxPeptideMass() {
        return maxPeptideMass;
    }

    /**
     * Set the maximum peptide mass.
     *
     * @param maxPeptideMass the maxPeptideMass to set
     */
    public void setMaxPeptideMass(Double maxPeptideMass) {
        this.maxPeptideMass = maxPeptideMass;
    }

    /**
     * Returns the minimm ion index.
     *
     * @return the minIonIndex
     */
    public Integer getMinIonIndex() {
        return minIonIndex;
    }

    /**
     * Sets the minimum ion index.
     *
     * @param minIonIndex the minIonIndex to set
     */
    public void setMinIonIndex(Integer minIonIndex) {
        this.minIonIndex = minIonIndex;
    }

    /**
     * Returns the decoy tag, null if not set.
     *
     * @return the decoyTag
     *
     * @deprecated use the decoy tag from FastaParameters instead
     */
    public String getDecoyTag() {
        return decoyTag;
    }

    /**
     * Set the decoy tag, null if no decoy tag is used.
     *
     * @param decoyTag the decoyTag to set
     *
     * @deprecated use the decoy tag from FastaParameters instead
     */
    public void setDecoyTag(String decoyTag) {
        this.decoyTag = decoyTag;
    }

    /**
     * Returns true if decoys are to be generated.
     *
     * @return true if decoys are to be generated
     */
    public Boolean getGenerateDecoys() {
        return generateDecoys;
    }

    /**
     * Set whether decoys are to be generated.
     *
     * @param generateDecoys the generateDecoys to set
     */
    public void setGenerateDecoys(Boolean generateDecoys) {
        this.generateDecoys = generateDecoys;
    }

    /**
     * Returns the TMT type.
     *
     * @return the TMT type
     */
    public String getTmtType() {
        return tmtType;
    }

    /**
     * Sets the TMT type.
     *
     * @param tmtType the TMT type to set
     */
    public void setTmtType(String tmtType) {
        this.tmtType = tmtType;
    }

    /**
     * Returns the TMT level.
     *
     * @return the TMT level
     */
    public Integer getTmtLevel() {

        if (tmtLevel == null) {
            tmtLevel = 3;
        }

        return tmtLevel;
    }

    /**
     * Sets the TMT level.
     *
     * @param tmtLevel the TMT level to set
     */
    public void setTmtLevel(Integer tmtLevel) {
        this.tmtLevel = tmtLevel;
    }

    /**
     * Returns true if Signal/Noise should be used instead of intensity for TMT
     * quantification.
     *
     * @return true if Signal/Noise should be used instead of intensity for TMT
     * quantification
     */
    public Boolean getTmtSn() {

        if (tmtSn == null) {
            tmtSn = false;
        }

        return tmtSn;
    }

    /**
     * Set if Signal/Noise should be used instead of intensity for TMT
     * quantification.
     *
     * @param tmtSn the tmtSn to set
     */
    public void setTmtSn(Boolean tmtSn) {
        this.tmtSn = tmtSn;
    }

    /**
     * Returns true if LFQ is to be performed.
     *
     * @return true if LFQ is to be performed
     */
    public Boolean getPerformLfq() {
        return performLfq;
    }

    /**
     * Set if LFQ is to be performed.
     *
     * @param performLfq the performLfq to set
     */
    public void setPerformLfq(Boolean performLfq) {
        this.performLfq = performLfq;
    }

    /**
     * Returns true if deisotoping is to be performed.
     *
     * @return true if deisotoping is to be performed
     */
    public Boolean getDeisotope() {
        return deisotope;
    }

    /**
     * Set whether deisotoping is to be performed.
     *
     * @param deisotope the deisotope to set
     */
    public void setDeisotope(Boolean deisotope) {
        this.deisotope = deisotope;
    }

    /**
     * Returns true if chimeric spectra are to be considered.
     *
     * @return true if chimeric spectra are to be considered
     */
    public Boolean getChimera() {
        return chimera;
    }

    /**
     * Set whether chimeric spectra are to be considered.
     *
     * @param chimera the chimera to set
     */
    public void setChimera(Boolean chimera) {
        this.chimera = chimera;
    }

    /**
     * Returns true if wide windows are to be used.
     *
     * @return true if wide windows are to be used
     */
    public Boolean getWideWindow() {

        if (wideWindow == null) {
            wideWindow = false;
        }

        return wideWindow;

    }

    /**
     * Set whether wide windows are to be used.
     *
     * @param wideWindow the wide window to set
     */
    public void setWideWindow(Boolean wideWindow) {
        this.wideWindow = wideWindow;
    }

    /**
     * Returns true if RT is to be predicted.
     *
     * @return true if RT is to be predicted
     */
    public Boolean getPredictRt() {
        return predictRt;
    }

    /**
     * Set whether RT is to be predicted.
     *
     * @param predictRt the predictRt to set
     */
    public void setPredictRt(Boolean predictRt) {
        this.predictRt = predictRt;
    }

    /**
     * Returns the minimum number of peaks required.
     *
     * @return the minPeaks
     */
    public Integer getMinPeaks() {
        return minPeaks;
    }

    /**
     * Set the minimum number of peaks required.
     *
     * @param minPeaks the minPeaks to set
     */
    public void setMinPeaks(Integer minPeaks) {
        this.minPeaks = minPeaks;
    }

    /**
     * Returns the maximum number of peaks required.
     *
     * @return the maxPeaks
     */
    public Integer getMaxPeaks() {
        return maxPeaks;
    }

    /**
     * Set the maximum number of peaks required.
     *
     * @param maxPeaks the maxPeaks to set
     */
    public void setMaxPeaks(Integer maxPeaks) {
        this.maxPeaks = maxPeaks;
    }

    /**
     * Returns the minimum number of matched b+y ions to use for reporting PSMs.
     *
     * @return the minimum number of matched b+y ions
     */
    public Integer getMinMatchedPeaks() {
        return minMatchedPeaks;
    }

    /**
     * Set the minimum number of matched b+y ions to use for reporting PSMs.
     *
     * @param minMatchedPeaks the minMatchedPeaks to set
     */
    public void setMinMatchedPeaks(Integer minMatchedPeaks) {
        this.minMatchedPeaks = minMatchedPeaks;
    }

    /**
     * Returns the maximum fragment charge.
     *
     * @return the maxFragmentCharge
     */
    public Integer getMaxFragmentCharge() {
        return maxFragmentCharge;
    }

    /**
     * Set the maximum fragment charge.
     *
     * @param maxFragmentCharge the maxFragmentCharge to set
     */
    public void setMaxFragmentCharge(Integer maxFragmentCharge) {
        this.maxFragmentCharge = maxFragmentCharge;
    }

    /**
     * Returns the number of PSMs per spectrum.
     *
     * @return the numPsmsPerSpectrum
     */
    public Integer getNumPsmsPerSpectrum() {
        return numPsmsPerSpectrum;
    }

    /**
     * Set the number of PSMs per spectrum.
     *
     * @param numPsmsPerSpectrum the numPsmsPerSpectrum to set
     */
    public void setNumPsmsPerSpectrum(Integer numPsmsPerSpectrum) {
        this.numPsmsPerSpectrum = numPsmsPerSpectrum;
    }

    /**
     * Returns the batch size. Null if not set.
     *
     * @return the batch size
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Set the batch size.
     *
     * @param batchSize the batch size to set
     */
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Returns the maximum variable modifications per peptide.
     *
     * @return the maxVariableMods
     */
    public Integer getMaxVariableMods() {

        if (maxVariableMods == null) {
            maxVariableMods = 2;
        }

        return maxVariableMods;

    }

    /**
     * Set the maximum variable modifications per peptide.
     *
     * @param maxVariableMods the maxVariableMods to set
     */
    public void setMaxVariableMods(Integer maxVariableMods) {
        this.maxVariableMods = maxVariableMods;
    }

    /**
     * Returns the LFQ peak scoring method.
     *
     * @return the lfqPeakScoring
     */
    public String getLfqPeakScoring() {

        if (lfqPeakScoring == null) {

            lfqPeakScoring = "Hybrid";

        }

        return lfqPeakScoring;
    }

    /**
     * Sets the LFQ peak scoring method.
     *
     * @param lfqPeakScoring the lfqPeakScoring to set
     */
    public void setLfqPeakScoring(String lfqPeakScoring) {
        this.lfqPeakScoring = lfqPeakScoring;
    }

    /**
     * Returns the LFQ integration type.
     *
     * @return the lfqIntergration
     */
    public String getLfqIntergration() {

        if (lfqIntergration == null) {
            lfqIntergration = "Sum";
        }

        return lfqIntergration;
    }

    /**
     * Sets the LFQ integration type.
     *
     * @param lfqIntergration the lfqIntergration to set
     */
    public void setLfqIntergration(String lfqIntergration) {
        this.lfqIntergration = lfqIntergration;
    }

    /**
     * Returns the LFQ spectral angel.
     *
     * @return the lfqSpectralAngle
     */
    public Double getLfqSpectralAngle() {

        if (lfqSpectralAngle == null) {
            lfqSpectralAngle = 0.7;
        }

        return lfqSpectralAngle;
    }

    /**
     * Set the LFQ spectral angel.
     *
     * @param lfqSpectralAngle the lfqSpectralAngle to set
     */
    public void setLfqSpectralAngle(Double lfqSpectralAngle) {
        this.lfqSpectralAngle = lfqSpectralAngle;
    }

    /**
     * Returns the LFQ PPM tolerance.
     *
     * @return the lfqPpmTolerance
     */
    public Double getLfqPpmTolerance() {

        if (lfqPpmTolerance == null) {
            lfqPpmTolerance = 0.5;
        }

        return lfqPpmTolerance;
    }

    /**
     * Sets the LFQ PPM tolerance.
     *
     * @param lfqPpmTolerance the lfqPpmTolerance to set
     */
    public void setLfqPpmTolerance(Double lfqPpmTolerance) {
        this.lfqPpmTolerance = lfqPpmTolerance;
    }

    /**
     * Returns true if the charge states are to be combined.
     *
     * @return true if the charge states are to be combined
     */
    public Boolean getCombineChargeStates() {
        
        if (combineChargeStates == null) {
            combineChargeStates = true;
        }
        
        return combineChargeStates;
    }

    /**
     * Sets whether the charge states are to be combined.
     *
     * @param combineChargeStates whether the charge states are to be combined
     */
    public void setCombineChargeStates(Boolean combineChargeStates) {
        this.combineChargeStates = combineChargeStates;
    }
}
