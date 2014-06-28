package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationAlgorithmParameter;

/**
 * The MyriMatch specific parameters.
 *
 * @author Harald Barsnes
 */
public class MyriMatchParameters implements IdentificationAlgorithmParameter {

    // @TODO: implement more parameters!!!
    
    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = 8755937399680481097L;
    /**
     * The minimum peptide length.
     */
    private Integer minPeptideLength = 6;
    /**
     * The maximal peptide length.
     */
    private Integer maxPeptideLength = 30; // note that MS-GF+ default is 40
    /**
     * The maximum number of spectrum matches.
     */
    private Integer numberOfSpectrumMarches = 1; // @TODO: find optimal default value!

    /**
     * Constructor.
     */
    public MyriMatchParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.myriMatch;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof MyriMatchParameters) {
            MyriMatchParameters msgfParameters = (MyriMatchParameters) identificationAlgorithmParameter;
            if (minPeptideLength != msgfParameters.getMinPeptideLength()) {
                return false;
            }
            if (maxPeptideLength != msgfParameters.getMaxPeptideLength()) {
                return false;
            }
            if (numberOfSpectrumMarches != msgfParameters.getNumberOfSpectrumMatches()) {
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

        output.append("MIN_PEP_LENGTH=");
        output.append(minPeptideLength);
        output.append(newLine);
        output.append("MAX_PEP_LENGTH=");
        output.append(maxPeptideLength);
        output.append(newLine);
        output.append("NUMBER_SPECTRUM_MATCHES=");
        output.append(numberOfSpectrumMarches);
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
     * Returns the maximum number of spectrum matches.
     *
     * @return the numberOfSpectrumMarches
     */
    public Integer getNumberOfSpectrumMatches() {
        return numberOfSpectrumMarches;
    }

    /**
     * Set the maximum number of spectrum matches.
     *
     * @param numberOfSpectrumMarches the numberOfSpectrumMarches to set
     */
    public void setNumberOfSpectrumMarches(Integer numberOfSpectrumMarches) {
        this.numberOfSpectrumMarches = numberOfSpectrumMarches;
    }
}
