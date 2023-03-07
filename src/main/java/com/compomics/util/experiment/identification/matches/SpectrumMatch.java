package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.identification.IdentificationMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * This class models a spectrum match.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 * @author Harald Barsnes
 */
public class SpectrumMatch extends IdentificationMatch {

    /**
     * The key of the match.
     */
    private int key;
    /**
     * The key of the file of the spectrum.
     */
    private int spectrumFileKey;
    /**
     * The key of the title of the spectrum.
     */
    private int spectrumTitleKey;
    /**
     * The best peptide assumption.
     */
    private PeptideAssumption bestPeptideAssumption;
    /**
     * The identification algorithms supporting this match.
     */
    private int[] advocates;

    /**
     * Constructor for the spectrum match.
     */
    public SpectrumMatch() {
    }

    /**
     * Constructor for the spectrum match.
     *
     * @param spectrumFileKey The key of the file containing the spectrum.
     * @param spectrumTitleKey The key of the spectrum title.
     * @param key The key of the spectrum match.
     */
    public SpectrumMatch(
            int spectrumFileKey,
            int spectrumTitleKey,
            int key
    ) {

        this.spectrumFileKey = spectrumFileKey;
        this.spectrumTitleKey = spectrumTitleKey;
        this.key = key;

    }

    /**
     * Getter for the best peptide assumption.
     *
     * @return the best peptide assumption for the spectrum
     */
    public PeptideAssumption getBestPeptideAssumption() {

        return bestPeptideAssumption;
    }

    /**
     * Setter for the best peptide assumption.
     *
     * @param bestPeptideAssumption The best peptide assumption for the spectrum.
     * @param advocates The identification algorithms supporting this match.
     */
    public void setBestPeptideAssumption(PeptideAssumption bestPeptideAssumption, int[] advocates) {

        this.bestPeptideAssumption = bestPeptideAssumption;
        this.advocates = advocates;
        
    }

    /**
     * Returns the name of the file where this spectrum was found.
     *
     * @return The name of the file where this spectrum was found.
     */
    public int getSpectrumFileKey() {

        return spectrumFileKey;
    }

    /**
     * Sets the spectrum file name.
     *
     * @param spectrumFileKey The spectrum file name.
     */
    public void setSpectrumFileKey(
            int spectrumFileKey
    ) {
        this.spectrumFileKey = spectrumFileKey;
    }

    /**
     * Returns the key of the title of the spectrum.
     *
     * @return The key of the title of the spectrum.
     */
    public int getSpectrumTitleKey() {
        return spectrumTitleKey;
    }

    /**
     * Sets the spectrum title.
     *
     * @param spectrumTitleKey The spectrum title.
     */
    public void setSpectrumTitleKey(
            int spectrumTitleKey
    ) {
        this.spectrumTitleKey = spectrumTitleKey;
    }

    @Override
    public long getKey() {
        return key;
    }

    /**
     * Sets the spectrum key.
     *
     * @param key The key of the spectrum.
     */
    public void setSpectrumKey(
            int key
    ) {
        this.key = key;
    }

    /**
     * Returns the advocates supporting hits for this spectrum.
     *
     * @return The advocates supporting hits for this spectrum.
     */
    public int[] getAdvocates() {

        return advocates;

    }

    @Override
    public MatchType getType() {

        return MatchType.Spectrum;
    }
}
