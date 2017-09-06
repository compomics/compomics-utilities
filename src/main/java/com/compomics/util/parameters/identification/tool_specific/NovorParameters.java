package com.compomics.util.parameters.identification.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import java.util.HashMap;

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
     * A map from the Novor PTM short name to the utilities PTM names. Novor PTM
     * short name &gt; utilities PTM name.
     */
    private HashMap<String, String> novorPtmMap; 

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
    
    /**
     * Returns the Novor to utilities PTM map. Null if not set.
     *
     * @return the Novor to utilities PTM map, null if not set
     */
    public HashMap<String, String> getNovorPtmMap() {
        return novorPtmMap;
    }
    
    /**
     * Set the Novor to utilities PTM map.
     *
     * @param novorPtmMap the novorPtmMap to set
     */
    public void setNovorPtmMap(HashMap<String, String> novorPtmMap) {
        this.novorPtmMap = novorPtmMap;
    }
    
    /**
     * Returns the utilities PTM name corresponding to the given Novor PTM
     * short name. Null if not found.
     *
     * @param novorPtmShortName the Novor PTM short name
     *
     * @return the utilities PTM name
     */
    public String getUtilitiesPtmName(String novorPtmShortName) {
        if (novorPtmMap == null) {
            return null;
        }
        return novorPtmMap.get(novorPtmShortName);
    }
}
