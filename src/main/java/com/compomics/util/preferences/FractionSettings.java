package com.compomics.util.preferences;

import java.io.Serializable;
import java.util.HashMap;
import no.uib.jsparklines.data.XYDataPoint;

/**
 * Settings for the handling of fractions.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class FractionSettings implements Serializable {

    /**
     * Serial version UID for backward compatibility.
     */
    static final long serialVersionUID = 5193356878844799148L;
    /**
     * The minimum confidence required for a protein to be included in the
     * calculation of the average molecular weight plot in the Fractions tab of
     * PeptideShaker.
     */
    private Double proteinConfidenceMwPlots = 95.0;
    /**
     * The list of fraction molecular weights. The key is the fraction file
     * path.
     */
    private HashMap<String, XYDataPoint> fractionMolecularWeightRanges = new HashMap<String, XYDataPoint>();

    /**
     * Constructor.
     */
    public FractionSettings() {

    }

    /**
     * Returns the protein confidence for inclusion in MW plots.
     *
     * @return the protein confidence for inclusion in MW plots
     */
    public Double getProteinConfidenceMwPlots() {
        return proteinConfidenceMwPlots;
    }

    /**
     * Sets the protein confidence for inclusion in MW plots.
     *
     * @param proteinConfidenceMwPlots the protein confidence for inclusion in
     * MW plots
     */
    public void setProteinConfidenceMwPlots(Double proteinConfidenceMwPlots) {
        this.proteinConfidenceMwPlots = proteinConfidenceMwPlots;
    }

    /**
     * Returns a boolean indicating whether other given settings are the same as
     * these.
     *
     * @param fractionSettings other settings to compare to
     *
     * @return a boolean indicating whether other given settings are the same as
     * these
     */
    public boolean isSameAs(FractionSettings fractionSettings) {
        if (!proteinConfidenceMwPlots.equals(fractionSettings.getProteinConfidenceMwPlots())) {
            return false;
        }
        if (this.getFractionMolecularWeightRanges() != null && fractionSettings.getFractionMolecularWeightRanges() != null) {
            if (!this.getFractionMolecularWeightRanges().equals(fractionSettings.getFractionMolecularWeightRanges())) {
                return false;
            }
        }
        if ((this.getFractionMolecularWeightRanges() != null && fractionSettings.getFractionMolecularWeightRanges() == null)
                || (this.getFractionMolecularWeightRanges() == null && fractionSettings.getFractionMolecularWeightRanges() != null)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the user provided molecular weight ranges for the fractions. The
     * key is the fraction file path.
     *
     * @return the user provided molecular weight ranges of the fractions
     */
    public HashMap<String, XYDataPoint> getFractionMolecularWeightRanges() {
        return fractionMolecularWeightRanges;
    }

    /**
     * Set the user provided molecular weight ranges for the fractions. The key
     * is the fraction file path.
     *
     * @param fractionMolecularWeightRanges the fractionMolecularWeightRanges to
     * set
     */
    public void setFractionMolecularWeightRanges(HashMap<String, XYDataPoint> fractionMolecularWeightRanges) {
        this.fractionMolecularWeightRanges = fractionMolecularWeightRanges;
    }
    
    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        
        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        output.append("Protein Confidence MW Plots: ").append(proteinConfidenceMwPlots).append(".").append(newLine);

        return output.toString();
    }
}
