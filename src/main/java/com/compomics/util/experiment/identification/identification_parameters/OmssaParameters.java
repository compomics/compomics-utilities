package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.massspectrometry.Charge;

/**
 * The OMSSA specific parameters.
 *
 * @author Marc Vaudel
 */
public class OmssaParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = -6704164074092668637L;
    /**
     * Maximal e-value cut-off.
     */
    private Double maxEValue = 100.0;
    /**
     * The maximal hit list length.
     */
    private Integer hitListLength = 25;
    /**
     * The minimal charge to be considered for multiple fragment charges.
     */
    private Charge minimalChargeForMultipleChargedFragments = new Charge(Charge.PLUS, 3);
    /**
     * The minimum peptide length (for semi and non tryptic searches).
     */
    private Integer minPeptideLength = 6;
    /**
     * The maximal peptide length (for semi and non tryptic searches).
     */
    private Integer maxPeptideLength = 30;
    /**
     * Indicates whether the precursor removal option is used.
     */
    private Boolean removePrecursor = false;
    /**
     * Indicates whether the precursor scaling option is used.
     */
    private Boolean scalePrecursor = true;
    /**
     * Indicates whether the precursor charge estimation option.
     */
    private Boolean estimateCharge = true;
    /**
     * The selected output type (see omssaOutputTypes).
     */
    private String selectedOutput = "OMX";
    /**
     * The available types of output.
     */
    private static String[] omssaOutputTypes = {"OMX", "CSV"};

    /**
     * Constructor.
     */
    public OmssaParameters() {

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
     * Returns the length of the hit list for OMSSA.
     *
     * @return the length of the hit list for OMSSA
     */
    public Integer getHitListLength() {
        return hitListLength;
    }

    /**
     * Sets the length of the hit list for OMSSA.
     *
     * @param hitListLength the length of the hit list for OMSSA
     */
    public void setHitListLength(Integer hitListLength) {
        this.hitListLength = hitListLength;
    }

    /**
     * Returns the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @return the minimal precursor charge to account for multiply charged
     * fragments in OMSSA
     */
    public Charge getMinimalChargeForMultipleChargedFragments() {
        return minimalChargeForMultipleChargedFragments;
    }

    /**
     * Sets the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @param minimalChargeForMultipleChargedFragments the minimal precursor
     * charge to account for multiply charged fragments in OMSSA
     */
    public void setMinimalChargeForMultipleChargedFragments(Charge minimalChargeForMultipleChargedFragments) {
        this.minimalChargeForMultipleChargedFragments = minimalChargeForMultipleChargedFragments;
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
    public void setMaxPeptideLength(Integer maxPeptideLength) {
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
    public void setMinPeptideLength(Integer minPeptideLength) {
        this.minPeptideLength = minPeptideLength;
    }

    /**
     * Indicates whether the precursor charge shall be estimated for OMSSA.
     *
     * @return a boolean indicating whether the precursor charge shall be
     * estimated for OMSSA
     */
    public Boolean isEstimateCharge() {
        return estimateCharge;
    }

    /**
     * Sets whether the precursor charge shall be estimated for OMSSA.
     *
     * @param estimateCharge a boolean indicating whether the precursor charge
     * shall be estimated for OMSSA
     */
    public void setEstimateCharge(Boolean estimateCharge) {
        this.estimateCharge = estimateCharge;
    }

    /**
     * Indicates whether the precursor shall be removed for OMSSA.
     *
     * @return a boolean indicating whether the precursor shall be removed for
     * OMSSA
     */
    public Boolean isRemovePrecursor() {
        return removePrecursor;
    }

    /**
     * Sets whether the precursor shall be removed for OMSSA.
     *
     * @param removePrecursor a boolean indicating whether the precursor shall
     * be removed for OMSSA
     */
    public void setRemovePrecursor(Boolean removePrecursor) {
        this.removePrecursor = removePrecursor;
    }

    /**
     * Indicates whether the precursor shall be scaled for OMSSA.
     *
     * @return a boolean indicating whether the precursor shall be scaled for
     * OMSSA
     */
    public Boolean isScalePrecursor() {
        return scalePrecursor;
    }

    /**
     * Sets whether the precursor shall be scaled for OMSSA.
     *
     * @param scalePrecursor a boolean indicating whether the precursor shall be
     * scaled for OMSSA
     */
    public void setScalePrecursor(Boolean scalePrecursor) {
        this.scalePrecursor = scalePrecursor;
    }

    /**
     * Returns the selected output type, omx or csv.
     *
     * @return the selected output type
     */
    public String getSelectedOutput() {
        return selectedOutput;
    }

    /**
     * Sets the output type, omx or csv.
     *
     * @param selectedOutput the output type
     */
    public void setSelectedOutput(String selectedOutput) {
        this.selectedOutput = selectedOutput;
    }

    /**
     * Returns the output types available.
     *
     * @return the output types available
     */
    public static String[] getOmssaOutputTypes() {
        return omssaOutputTypes;
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.OMSSA;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof OmssaParameters) {
            OmssaParameters omssaParameters = (OmssaParameters) identificationAlgorithmParameter;
            if (!getMaxEValue().equals(omssaParameters.getMaxEValue())) {
                return false;
            }
            if (!getHitListLength().equals(omssaParameters.getHitListLength())) {
                return false;
            }
            if (!getMaxPeptideLength().equals(omssaParameters.getMaxPeptideLength())) {
                return false;
            }
            if (!getMinPeptideLength().equals(omssaParameters.getMinPeptideLength())) {
                return false;
            }
            if (!getMinimalChargeForMultipleChargedFragments().equals(omssaParameters.getMinimalChargeForMultipleChargedFragments())) {
                return false;
            }
            if (!isEstimateCharge().equals(omssaParameters.isEstimateCharge())) {
                return false;
            }
            if (!isRemovePrecursor().equals(omssaParameters.isRemovePrecursor())) {
                return false;
            }
            if (!isScalePrecursor().equals(omssaParameters.isScalePrecursor())) {
                return false;
            }
            if (!getSelectedOutput().equals(omssaParameters.getSelectedOutput())) {
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

        output.append("EVALUE_CUTOFF=");
        output.append(maxEValue);
        output.append(newLine);

        output.append("MAXIMUM_HITLIST_LENGTH=");
        output.append(hitListLength);
        output.append(newLine);

        output.append("PRECURSOR_ELIMINATION=");
        output.append(removePrecursor);
        output.append(newLine);

        output.append("PRECURSOR_SCALING=");
        output.append(scalePrecursor);
        output.append(newLine);

        output.append("MINIMAL_PEPTIDE_SIZE=");
        output.append(minPeptideLength);
        output.append(newLine);

        output.append("MAXIMAL_PEPTIDE_SIZE=");
        output.append(maxPeptideLength);
        output.append(newLine);

        output.append("PRECURSOR_CHARGE_TO_CONSIDER_MULTIPLY_CHARGED_FRAGMENTS=");
        output.append(minimalChargeForMultipleChargedFragments);
        output.append(newLine);

        output.append("CHARGE_ESTIMATION=");
        output.append(estimateCharge);
        output.append(newLine);

        output.append("OUTPUT_TYPE=");
        output.append(selectedOutput);
        output.append(newLine);

        return output.toString();
    }
}
