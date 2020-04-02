package com.compomics.util.parameters.identification.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;

/**
 * The MetaMorpheus specific parameters. (Note: Work in progress!)
 *
 * @author Harald Barsnes
 */
public class MetaMorpheusParameters implements IdentificationAlgorithmParameter {

    /**
     * Minimum peptide length.
     */
    private Integer minPeptideLength = 8;
    /**
     * Maximum peptide length.
     */
    private Integer maxPeptideLength = 30;
    
    /**
     * Constructor.
     */
    public MetaMorpheusParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.metaMorpheus;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof MetaMorpheusParameters) {
            MetaMorpheusParameters metaMorpheusParameters = (MetaMorpheusParameters) identificationAlgorithmParameter;

            if (!minPeptideLength.equals(metaMorpheusParameters.getMinPeptideLength())) {
                return false;
            }
            if (!maxPeptideLength.equals(metaMorpheusParameters.getMaxPeptideLength())) {
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
        output.append("MIN_PEPTIDE_LENGTH=");
        output.append(getMinPeptideLength());
        output.append(newLine);
        output.append("MAX_PEPTIDE_LENGTH=");
        output.append(getMaxPeptideLength());
        output.append(newLine);

        return output.toString();
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
}
