package com.compomics.util.experiment.identification.identification_parameters.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;

/**
 * Novor specific parameters.
 *
 * @author Harald Barsnes
 */
public class NovorParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = -1685402448885208852L;
    /**
     * The fragmentation method.
     */
    private String fragmentationMethod = "HCD"; // CID or HCD
    /**
     * The mass analyzer.
     */
    private String massAnalyzer = "FT"; // Trap, TOF, or FT

    /**
     * Constructor.
     */
    public NovorParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.novor;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof NovorParameters) {
            NovorParameters novorParameters = (NovorParameters) identificationAlgorithmParameter;

            if (!fragmentationMethod.equalsIgnoreCase(novorParameters.getFragmentationMethod())) {
                return false;
            }
            if (!massAnalyzer.equalsIgnoreCase(novorParameters.getMassAnalyzer())) {
                return false;
            }
        }

        return true;
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
        output.append("FRAGMENTATION=");
        output.append(fragmentationMethod);
        output.append(newLine);
        output.append("MASS_ANALYZER=");
        output.append(massAnalyzer);
        output.append(newLine);

        return output.toString();
    }

    /**
     * Returns the fragmentation method.
     *
     * @return the fragmentation method
     */
    public String getFragmentationMethod() {
        return fragmentationMethod;
    }

    /**
     * Set the fragmentation method.
     *
     * @param fragmentationMethod the fragmentation method to set
     */
    public void setFragmentationMethod(String fragmentationMethod) {
        this.fragmentationMethod = fragmentationMethod;
    }

    /**
     * Returns the mass analyzer.
     *
     * @return the mass analyzer
     */
    public String getMassAnalyzer() {
        return massAnalyzer;
    }

    /**
     * Set the mass analyzer.
     *
     * @param massAnalyzer the mass analyzer to set
     */
    public void setMassAnalyzer(String massAnalyzer) {
        this.massAnalyzer = massAnalyzer;
    }
}
