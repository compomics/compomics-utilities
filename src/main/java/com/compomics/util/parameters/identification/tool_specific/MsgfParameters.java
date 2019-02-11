package com.compomics.util.parameters.identification.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;

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
     * The MS-GF+ instrument ID: 0: Low-res LCQ/LTQ (Default), 1:
     * Orbitrap/FTICR, 2: TOF, 3: Q-Exactive.
     */
    private int instrumentID = 3;
    /**
     * The MS-GF+ fragmentation type ID: 0: As written in the spectrum or CID if
     * no info, 1: CID, 2: ETD, 3: HCD, 4: UVPD.
     */
    private int fragmentationType = 3;
    /**
     * The MS-GF+ protocol ID: 0: Automatic, 1: Phosphorylation, 2: iTRAQ, 3:
     * iTRAQPhospho, 4: TMT, 5: Standard.
     */
    private int protocol = 0;
    /**
     * The minimum peptide length.
     */
    private Integer minPeptideLength = 8;
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
     * The number of tolerable termini. E.g. For trypsin, 0: non-tryptic, 1:
     * semi-tryptic, 2: fully-tryptic peptides only.
     */
    private Integer numberTolerableTermini = 2;
    /**
     * The maximum number of modifications per peptide.
     */
    private Integer numberOfModificationsPerPeptide = 2;
    /**
     * The number of tasks. Null if not set, meaning that it will be internally
     * calculated based on the inputs. More tasks than threads will reduce the
     * memory requirements of the search, but will be slower (how much depends
     * on the inputs). If the spectrum file is particularly large, a larger
     * number of tasks will decrease the possibility of out of memory errors. If
     * the FASTA file being searched is larger than 10MB, more tasks will cause
     * a noticeably longer search time.
     */
    private Integer numberOfTasks = null;

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
            if (!minPeptideLength.equals(msgfParameters.getMinPeptideLength())) {
                return false;
            }
            if (!maxPeptideLength.equals(msgfParameters.getMaxPeptideLength())) {
                return false;
            }
            if (!numberOfSpectrumMarches.equals(msgfParameters.getNumberOfSpectrumMatches())) {
                return false;
            }
            if (additionalOutput != msgfParameters.isAdditionalOutput()) {
                return false;
            }
            if (!numberTolerableTermini.equals(msgfParameters.getNumberTolerableTermini())) {
                return false;
            }
            if (!numberOfModificationsPerPeptide.equals(msgfParameters.getNumberOfModificationsPerPeptide())) {
                return false;
            }
            if (numberOfTasks != null && msgfParameters.getNumberOfTasks() == null
                    || numberOfTasks == null && msgfParameters.getNumberOfTasks() != null
                    || (numberOfTasks != null && msgfParameters.getNumberOfTasks() != null && !numberOfTasks.equals(msgfParameters.getNumberOfTasks()))) {
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
        output.append("NUMBER_OF_TOLERABLE_TERMINI=");
        output.append(numberTolerableTermini);
        output.append(newLine);
        output.append("MAX_NUMBER_MODIFICATIONS=");
        output.append(numberOfModificationsPerPeptide);
        output.append(newLine);
        output.append("NUMBER_TASKS=");
        output.append(numberOfTasks);
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
     * Returns the maximum number of modifications per peptide.
     *
     * @return the maximum number of modifications per peptide
     */
    public Integer getNumberOfModificationsPerPeptide() {
        return numberOfModificationsPerPeptide;
    }

    /**
     * Set the maximum number of modifications per peptide.
     *
     * @param numberOfModificationsPerPeptide the maximum number of
     * modifications per peptide
     */
    public void setNumberOfModificationsPerPeptide(Integer numberOfModificationsPerPeptide) {
        this.numberOfModificationsPerPeptide = numberOfModificationsPerPeptide;
    }
    
    /**
     * Returns the number of tasks, null if not set.
     *
     * @return the number of tasks
     */
    public Integer getNumberOfTasks() {
        return numberOfTasks;
    }

    /**
     * Set the number of tasks. Set to null to leave the choice to MS-GF+.
     *
     * @param numberOfTasks the number of tasks
     */
    public void setNumberOfTasks(Integer numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
    }
}
