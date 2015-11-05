package com.compomics.util.preferences;

import java.io.Serializable;

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
    static final long serialVersionUID = -3455093073502201047L;
    /**
     * The minimum confidence required for a protein to be included in the
     * calculation of the average molecular weight plot in the Fractions tab of
     * PeptideShaker.
     */
    private Double proteinConfidenceMwPlots = 95.0;

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
        return true;
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
