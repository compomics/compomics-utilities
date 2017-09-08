package com.compomics.util.parameters.identification.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import java.util.Map;

/**
 * PepNovo specific parameters.
 *
 * @author Marc Vaudel
 */
public class PepnovoParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = -8056507693211793829L;
    /**
     * The maximal hit list length for PepNovo+. Max is 20.
     */
    private Integer hitListLength = 10;
    /**
     * Indicates whether the precursor charge estimation option.
     */
    private Boolean estimateCharge = true;
    /**
     * Indicates whether the precursor mass shall be corrected.
     */
    private Boolean correctPrecursorMass = false;
    /**
     * Indicates whether the low quality spectra shall be discarded.
     */
    private Boolean discardLowQualitySpectra = true;
    /**
     * PepNovo fragmentation model.
     */
    private String fragmentationModel = "CID_IT_TRYP";
    /**
     * Indicates whether a blast query shall be generated.
     */
    private Boolean generateQuery = false;
    /**
     * A map from the PepNovo PTM symbols to the utilities PTM names.
     */
    private Map<String, String> pepNovoPtmMap;

    /**
     * Constructor.
     */
    public PepnovoParameters() {

    }

    /**
     * Returns the length of the hit list.
     *
     * @return the length of the hit list
     */
    public Integer getHitListLength() {
        return hitListLength;
    }

    /**
     * Sets the length of the hit list.
     *
     * @param hitListLength the length of the hit list
     */
    public void setHitListLength(Integer hitListLength) {
        this.hitListLength = hitListLength;
    }

    /**
     * Indicates whether the precursor charge shall be estimated for PepNovo.
     *
     * @return a boolean indicating whether the precursor charge shall be
     * estimated for PepNovo
     */
    public Boolean isEstimateCharge() {
        return estimateCharge;
    }

    /**
     * Sets whether the precursor charge shall be estimated for PepNovo.
     *
     * @param estimateCharge a boolean indicating whether the precursor charge
     * shall be estimated for PepNovo
     */
    public void setEstimateCharge(Boolean estimateCharge) {
        this.estimateCharge = estimateCharge;
    }

    /**
     * Returns a boolean indicating whether the precursor mass shall be
     * corrected (TagDB setting).
     *
     * @return a boolean indicating whether the precursor mass shall be
     * corrected (TagDB setting)
     */
    public Boolean isCorrectPrecursorMass() {
        if (correctPrecursorMass != null) {
            return correctPrecursorMass;
        } else {
            return true;
        }
    }

    /**
     * Sets whether the precursor mass shall be corrected (TagDB setting).
     *
     * @param correctPrecursorMass a boolean indicating whether the precursor
     * mass shall be corrected (TagDB setting)
     */
    public void setCorrectPrecursorMass(Boolean correctPrecursorMass) {
        this.correctPrecursorMass = correctPrecursorMass;
    }

    /**
     * Returns a boolean indicating whether low quality spectra shall be
     * discarded.
     *
     * @return a boolean indicating whether low quality spectra shall be
     * discarded
     */
    public Boolean getDiscardLowQualitySpectra() {
        if (discardLowQualitySpectra != null) {
            return discardLowQualitySpectra;
        } else {
            return true;
        }
    }

    /**
     * Sets whether low quality spectra shall be discarded.
     *
     * @param discardLowQualitySpectra a boolean indicating whether low quality
     * spectra shall be discarded
     */
    public void setDiscardLowQualitySpectra(Boolean discardLowQualitySpectra) {
        this.discardLowQualitySpectra = discardLowQualitySpectra;
    }

    /**
     * Returns the name of the fragmentation model.
     *
     * @return the name of the fragmentation model
     */
    public String getFragmentationModel() {
        return fragmentationModel;
    }

    /**
     * Sets the name of the fragmentation model.
     *
     * @param fragmentationModel the name of the fragmentation model
     */
    public void setFragmentationModel(String fragmentationModel) {
        this.fragmentationModel = fragmentationModel;
    }

    /**
     * Returns a boolean indicating whether a blast query shall be generated.
     *
     * @return a boolean indicating whether a blast query shall be generated
     */
    public Boolean generateQuery() {
        return generateQuery;
    }

    /**
     * Sets a boolean indicating whether a blast query shall be generated.
     *
     * @param generateQuery a boolean indicating whether a blast query shall be
     * generated
     */
    public void setGenerateQuery(Boolean generateQuery) {
        this.generateQuery = generateQuery;
    }

    /**
     * Returns the PepNovo to utilities PTM map. Null if not set.
     *
     * @return the PepNovo to utilities PTM map, null if not set
     */
    public Map<String, String> getPepNovoPtmMap() {
        return pepNovoPtmMap;
    }

    /**
     * Returns the utilities PTM name corresponding to the given PepNovo PTM
     * name. Null if not found.
     *
     * @param pepnovoPtmName the PepNovo PTM name
     *
     * @return the utilities PTM name
     */
    public String getUtilitiesPtmName(String pepnovoPtmName) {
        if (pepNovoPtmMap == null) {
            return null;
        }
        return pepNovoPtmMap.get(pepnovoPtmName);
    }

    /**
     * Set the PepNovo to utilities PTM map.
     *
     * @param pepNovoPtmMap the pepNovoPtmMap to set
     */
    public void setPepNovoPtmMap(Map<String, String> pepNovoPtmMap) {
        this.pepNovoPtmMap = pepNovoPtmMap;
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.pepnovo;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof PepnovoParameters) {
            PepnovoParameters pepnovoParameters = (PepnovoParameters) identificationAlgorithmParameter;
            if (!getHitListLength().equals(pepnovoParameters.getHitListLength())) {
                return false;
            }
            if (!isEstimateCharge().equals(pepnovoParameters.isEstimateCharge())) {
                return false;
            }
            if (!isCorrectPrecursorMass().equals(pepnovoParameters.isCorrectPrecursorMass())) {
                return false;
            }
            if (!generateQuery().equals(pepnovoParameters.generateQuery())) {
                return false;
            }
            if (!getDiscardLowQualitySpectra().equals(pepnovoParameters.getDiscardLowQualitySpectra())) {
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

        output.append("HIT_LIST_LENGTH=");
        output.append(hitListLength);
        output.append(newLine);
        output.append("CORRECT_PRECURSOR_MASS=");
        output.append(correctPrecursorMass);
        output.append(newLine);
        output.append("DISCARD_LOW_QUALITY_SPECTRA=");
        output.append(discardLowQualitySpectra);
        output.append(newLine);
        output.append("FRAGMENTATION_MODEL=");
        output.append(fragmentationModel);
        output.append(newLine);
        output.append("GENERATE_QUERY=");
        output.append(generateQuery);
        output.append(newLine);

        return output.toString();
    }
}
