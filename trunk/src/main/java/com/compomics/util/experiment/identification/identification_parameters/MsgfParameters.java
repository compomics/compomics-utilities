package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationAlgorithmParameter;

/**
 * The MS-GF+ specific parameters.
 *
 * @author Harald Barsnes
 */
public class MsgfParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    static final long serialVersionUID = -2656523093031942973L;
    /**
     * Indicates whether MS-GF+ is to create and search a decoy database or not.
     */
    private boolean searchDecoyDatabase = false;
    /**
     * The MS-GF+ instrument ID: 0: Low-res LCQ/LTQ (Default), 1: High-res LTQ,
     * 2: TOF, 3: Q-Exactive.
     */
    private int instrumentID = 0;
    /**
     * The MS-GF+ fragmentation type ID: 0: As written in the spectrum or CID if
     * no info (Default), 1: CID, 2: ETD, 3: HCD.
     */
    private int fragmentationType = 0;
    /**
     * The MS-GF+ protocol ID: 0: Automatic (Default), 1: Phosphorylation, 2:
     * iTRAQ, 3: iTRAQPhospho, 4: TMT, 5: Standard.
     */
    private int protocol = 0;
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
    private Integer numberOfSpectrumMarches = 10;
    /**
     * Output additional features.
     */
    private boolean additionalOutput = false;
    /**
     * The lower isotope error range.
     */
    private Integer lowerIsotopeErrorRange = 0;
    /**
     * The upper isotope error range.
     */
    private Integer upperIsotopeErrorRange = 1;
    /**
     * The number of tolerable termini. E.g. For trypsin, 0: non-tryptic, 1:
     * semi-tryptic, 2: fully-tryptic peptides only.
     */
    private Integer numberTolerableTermini = 2;
    /**
     * The maximum number of PTMs per peptide.
     */
    private Integer numberOfPtmsPerPeptide = 2;

    /**
     * Constructor.
     */
    public MsgfParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.msgf;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof MsgfParameters) {
            MsgfParameters msgfParameters = (MsgfParameters) identificationAlgorithmParameter;
            if (searchDecoyDatabase != msgfParameters.searchDecoyDatabase()) {
                return false;
            }
            if (instrumentID != msgfParameters.getInstrumentID()) {
                return false;
            }
            if (fragmentationType != msgfParameters.getFragmentationType()) {
                return false;
            }
            if (protocol != msgfParameters.getProtocol()) {
                return false;
            }
            if (minPeptideLength != msgfParameters.getMinPeptideLength()) {
                return false;
            }
            if (maxPeptideLength != msgfParameters.getMaxPeptideLength()) {
                return false;
            }
            if (numberOfSpectrumMarches != msgfParameters.getNumberOfSpectrumMatches()) {
                return false;
            }
            if (additionalOutput != msgfParameters.isAdditionalOutput()) {
                return false;
            }
            if (lowerIsotopeErrorRange != msgfParameters.getLowerIsotopeErrorRange()) {
                return false;
            }
            if (upperIsotopeErrorRange != msgfParameters.getUpperIsotopeErrorRange()) {
                return false;
            }
            if (numberTolerableTermini != msgfParameters.getNumberTolerableTermini()) {
                return false;
            }
            if (numberOfPtmsPerPeptide != msgfParameters.getNumberOfPtmsPerPeptide()) {
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

        output.append("SEARCH_DECOY=");
        output.append(searchDecoyDatabase);
        output.append(newLine);
        output.append("INSTRUMENT_ID=");
        output.append(instrumentID);
        output.append(newLine);
        output.append("FRAGMENTATION_ID=");
        output.append(fragmentationType);
        output.append(newLine);
        output.append("PROTOCOL_ID=");
        output.append(protocol);
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
        output.append("ADDITIONAL_OUTPUT=");
        output.append(additionalOutput);
        output.append(newLine);
        output.append("LOWER_ISOTOPE_ERROR_RANGE=");
        output.append(lowerIsotopeErrorRange);
        output.append(newLine);
        output.append("UPPER_ISOTOPE_ERROR_RANGE=");
        output.append(upperIsotopeErrorRange);
        output.append(newLine);
        output.append("NUMBER_OF_TOLERABLE_TERMINI=");
        output.append(numberTolerableTermini);
        output.append(newLine);
        output.append("MAX_NUMBER_PTMS=");
        output.append(numberOfPtmsPerPeptide);
        output.append(newLine);

        return output.toString();
    }

    /**
     * Returns whether MS-GF+ is to create and search a decoy database.
     *
     * @return whether MS-GF+ is to create and search a decoy database
     */
    public boolean searchDecoyDatabase() {
        return searchDecoyDatabase;
    }

    /**
     * Set whether MS-GF+ is to create and search a decoy database.
     *
     * @param searchDecoyDatabase the searchDecoyDatabase to set
     */
    public void setSearchDecoyDatabase(boolean searchDecoyDatabase) {
        this.searchDecoyDatabase = searchDecoyDatabase;
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
     * Return the instrument ID.
     *
     * @return the instrumentID
     */
    public int getInstrumentID() {
        return instrumentID;
    }

    /**
     * Set the instrument ID.
     *
     * @param instrumentID the instrumentID to set
     */
    public void setInstrumentID(int instrumentID) {
        this.instrumentID = instrumentID;
    }

    /**
     * Return the fragmentation ID.
     *
     * @return the fragmentationType
     */
    public int getFragmentationType() {
        return fragmentationType;
    }

    /**
     * Set the fragmentation ID.
     *
     * @param fragmentationType the fragmentationType to set
     */
    public void setFragmentationType(int fragmentationType) {
        this.fragmentationType = fragmentationType;
    }

    /**
     * Returns the protocol ID.
     *
     * @return the protocol
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Set the protocol ID.
     *
     * @param protocol the protocol to set
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    /**
     * Returns the maximum number of spectrum matches.
     *
     * @return the numberOfSpectrumMarches
     * @deprecated use getNumberOfSpectrumMatches (without the typo) instead
     */
    public Integer getNumberOfSpectrumMarches() {
        return numberOfSpectrumMarches;
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

    /**
     * Returns if additional output is to be included.
     *
     * @return the additionalOutput
     */
    public boolean isAdditionalOutput() {
        return additionalOutput;
    }

    /**
     * Set if additional output is to be included.
     *
     * @param additionalOutput the additionalOutput to set
     */
    public void setAdditionalOutput(boolean additionalOutput) {
        this.additionalOutput = additionalOutput;
    }

    /**
     * Returns the lower isotope error range.
     *
     * @return the lowerIsotopeErrorRange
     */
    public Integer getLowerIsotopeErrorRange() {
        return lowerIsotopeErrorRange;
    }

    /**
     * Set the lower isotope error range.
     *
     * @param lowerIsotopeErrorRange the lowerIsotopeErrorRange to set
     */
    public void setLowerIsotopeErrorRange(Integer lowerIsotopeErrorRange) {
        this.lowerIsotopeErrorRange = lowerIsotopeErrorRange;
    }

    /**
     * Returns the upper isotope error range.
     *
     * @return the upperIsotopeErrorRange
     */
    public Integer getUpperIsotopeErrorRange() {
        return upperIsotopeErrorRange;
    }

    /**
     * Set the upper isotope error range.
     *
     * @param upperIsotopeErrorRange the upperIsotopeErrorRange to set
     */
    public void setUpperIsotopeErrorRange(Integer upperIsotopeErrorRange) {
        this.upperIsotopeErrorRange = upperIsotopeErrorRange;
    }

    /**
     * Returns the number of tolerable termini.
     *
     * @return the numberTolerableTermini
     */
    public Integer getNumberTolerableTermini() {
        return numberTolerableTermini;
    }

    /**
     * Set the number of tolerable termini.
     *
     * @param numberTolerableTermini the numberTolerableTermini to set
     */
    public void setNumberTolerableTermini(Integer numberTolerableTermini) {
        this.numberTolerableTermini = numberTolerableTermini;
    }

    /**
     * Returns the maximum number of PTMs per peptide.
     *
     * @return the numberOfPtmsPerPeptide
     */
    public Integer getNumberOfPtmsPerPeptide() {
        return numberOfPtmsPerPeptide;
    }

    /**
     * Set the maximum number of PTMs per peptide.
     *
     * @param numberOfPtmsPerPeptide the numberOfPtmsPerPeptide to set
     */
    public void setNumberOfPtmsPerPeptide(Integer numberOfPtmsPerPeptide) {
        this.numberOfPtmsPerPeptide = numberOfPtmsPerPeptide;
    }

    /**
     * Tries to map the utilities enzyme to the enzymes supported by MS-GF+.
     *
     * @param enzyme the utilities enzyme
     * @return the index of the MS-GF+ enzyme as a string, or null of no mapping
     * is found
     */
    public static String enzymeMapping(Enzyme enzyme) {

        String msgfEnzymeIndex = null;

        String enzymeName = enzyme.getName();
        if (enzymeName.equalsIgnoreCase("No Enzyme")) {
            msgfEnzymeIndex = "0";
        } else if (enzymeName.equalsIgnoreCase("Trypsin")) {
            msgfEnzymeIndex = "1";
        } else if (enzymeName.equalsIgnoreCase("Chymotrypsin (FYWL)")) {
            msgfEnzymeIndex = "2";
        } else if (enzymeName.equalsIgnoreCase("Lys-C")) {
            msgfEnzymeIndex = "3";
        } else if (enzymeName.equalsIgnoreCase("Lys-N (K)")) {
            msgfEnzymeIndex = "4";
        } else if (enzymeName.equalsIgnoreCase("Glu-C (DE)")) {
            msgfEnzymeIndex = "5";
        } else if (enzymeName.equalsIgnoreCase("Arg-C")) {
            msgfEnzymeIndex = "6";
        } else if (enzymeName.equalsIgnoreCase("Asp-N")) {
            msgfEnzymeIndex = "7";
        } // else if (enzymeName.equalsIgnoreCase("alphaLP")) { // alphaLP: Alpha-lytic protease (aLP) is an alternative specificity protease for proteomics applications.
        //      msgfEnzymeIndex = "8";                        //          cleaves after T, A, S, and V residues. It generates peptides of similar average length as trypsin.
        // }
        else if (enzymeName.equalsIgnoreCase("Top-Down") || enzymeName.equalsIgnoreCase("Whole Protein")) {
            msgfEnzymeIndex = "9";
        }

        return msgfEnzymeIndex;
    }
}
