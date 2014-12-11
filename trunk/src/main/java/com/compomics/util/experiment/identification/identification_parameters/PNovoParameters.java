package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationAlgorithmParameter;
import java.util.HashMap;

/**
 * pNovo specific parameters.
 *
 * @author Harald Barsnes
 */
public class PNovoParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = 7525455518683797145L;
    /**
     * The number of peptides reported.
     */
    private int numberOfPeptides = 10;
    /**
     * The minimum precursor mass.
     */
    private int lowerPrecursorMass = 300;
    /**
     * The maximum precursor mass.
     */
    private int upperPrecursorMass = 5000;
    /**
     * The activation type (HCD, CID or ETD).
     */
    private String acticationType = "HCD";
    /**
     * A map from the pNovo PTM character to the utilities PTM names. pNovo PTM
     * character &gt; utilities PTM name.
     */
    private HashMap<Character, String> pNovoPtmMap;
    /**
     * A map from the pNovo character to the original amino acids residue. pNovo
     * PTM character &gt; original amino acids residue.
     */
    private HashMap<Character, Character> pNovoResidueMap;

    /**
     * Constructor.
     */
    public PNovoParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.pNovo;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof PNovoParameters) {
            PNovoParameters pNovoParameters = (PNovoParameters) identificationAlgorithmParameter;

            if (getNumberOfPeptides() != pNovoParameters.getNumberOfPeptides()) {
                return false;
            }
            if (getLowerPrecursorMass() != pNovoParameters.getLowerPrecursorMass()) {
                return false;
            }
            if (getUpperPrecursorMass() != pNovoParameters.getUpperPrecursorMass()) {
                return false;
            }
            if (!getActicationType().equalsIgnoreCase(pNovoParameters.getActicationType())) {
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

        output.append("NUMBER_PEPTIDES=");
        output.append(numberOfPeptides);
        output.append(newLine);
        output.append("LOWER_PRECURSOR_MASS=");
        output.append(lowerPrecursorMass);
        output.append(newLine);
        output.append("UPPER_PRECURSOR_MASS=");
        output.append(upperPrecursorMass);
        output.append(newLine);
        output.append("ACTIVATION_TYPE=");
        output.append(acticationType);
        output.append(newLine);

        return output.toString();
    }

    /**
     * Returns the number of peptides to report per spectrum.
     *
     * @return the number of peptides to report per spectrum
     */
    public int getNumberOfPeptides() {
        return numberOfPeptides;
    }

    /**
     * Set the number of peptides to report per spectrum.
     *
     * @param numberOfPeptides the number of peptides to report per spectrum
     */
    public void setNumberOfPeptides(int numberOfPeptides) {
        this.numberOfPeptides = numberOfPeptides;
    }

    /**
     * Returns the lower precursor mass.
     *
     * @return the lowerPrecursorMass
     */
    public int getLowerPrecursorMass() {
        return lowerPrecursorMass;
    }

    /**
     * Set the lower precursor mass.
     *
     * @param lowerPrecursorMass the lowerPrecursorMass to set
     */
    public void setLowerPrecursorMass(int lowerPrecursorMass) {
        this.lowerPrecursorMass = lowerPrecursorMass;
    }

    /**
     * Returns the upper precursor mass.
     *
     * @return the upperPrecursorMass
     */
    public int getUpperPrecursorMass() {
        return upperPrecursorMass;
    }

    /**
     * Set the upper precursor mass.
     *
     * @param upperPrecursorMass the upperPrecursorMass to set
     */
    public void setUpperPrecursorMass(int upperPrecursorMass) {
        this.upperPrecursorMass = upperPrecursorMass;
    }

    /**
     * Returns the activation type.
     *
     * @return the acticationType
     */
    public String getActicationType() {
        return acticationType;
    }

    /**
     * Sets the activation type.
     *
     * @param acticationType the acticationType to set
     */
    public void setActicationType(String acticationType) {
        this.acticationType = acticationType;
    }

    /**
     * Returns the pNovo to utilities PTM map. Null if not set.
     *
     * @return the pNovo to utilities PTM map, null if not set
     */
    public HashMap<Character, String> getPNovoPtmMap() {
        return pNovoPtmMap;
    }

    /**
     * Returns the utilities PTM name corresponding to the given pNovo PTM
     * character. Null if not found.
     *
     * @param pNovoPtmCharacter the pNovo PTM character
     *
     * @return the utilities PTM name
     */
    public String getUtilitiesPtmName(Character pNovoPtmCharacter) {
        if (pNovoPtmMap == null) {
            return null;
        }
        return pNovoPtmMap.get(pNovoPtmCharacter);
    }

    /**
     * Set the pNovo to utilities PTM map.
     *
     * @param pNovoPtmMap the pNovoPtmMap to set
     */
    public void setPNovoPtmMap(HashMap<Character, String> pNovoPtmMap) {
        this.pNovoPtmMap = pNovoPtmMap;
    }

    /**
     * Returns the pNovo to original amino acids residue map. Null if not set.
     *
     * @return the pNovo to original amino acids residue map, null if not set
     */
    public HashMap<Character, Character> getPNovoPtmResiduesMap() {
        return pNovoResidueMap;
    }

    /**
     * Returns the original amino acids residue corresponding to the given pNovo
     * PTM character. Null if not found.
     *
     * @param pNovoPtmCharacter the pNovo PTM character
     *
     * @return the original amino acids residue
     */
    public Character getPtmResidue(Character pNovoPtmCharacter) {
        if (pNovoPtmMap == null) {
            return null;
        }
        return pNovoResidueMap.get(pNovoPtmCharacter);
    }

    /**
     * Set the pNovo to original amino acids residue map.
     *
     * @param pNovoResidueMap the pNovoResidueMap to set
     */
    public void setPNovoPtmResiduesMap(HashMap<Character, Character> pNovoResidueMap) {
        this.pNovoResidueMap = pNovoResidueMap;
    }
}
