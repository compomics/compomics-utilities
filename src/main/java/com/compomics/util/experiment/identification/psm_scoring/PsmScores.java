package com.compomics.util.experiment.identification.psm_scoring;

import com.compomics.util.experiment.ShotgunProtocol;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.AAIntensityRankScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.AAMS2MzFidelityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.ComplementarityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.IntensityRankScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.MS2MzFidelityScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.PrecursorAccuracy;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Enum listing the PSM scores implemented in compomics utilities.
 *
 * @author Marc Vaudel
 */
public enum PsmScores {

    /**
     * The native score of the search engine.
     */
    native_score(-1, "native", false),
    /**
     * The precursor accuracy.
     */
    precursor_accuracy(0, "precursor accuracy", false),
    /**
     * The m/z fidelity score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    ms2_mz_fidelity(1, "fragment ion mz fildelity", false),
    /**
     * The m/z fidelity score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943) per amino acid.
     */
    aa_ms2_mz_fidelity(2, "AA fragment ion mz fildelity", false),
    /**
     * The intensity sub-score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    intensity(3, "intensity", true),
    /**
     * The intensity sub-score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943) per amino acid.
     */
    aa_intensity(4, "AA intensity", false),
    /**
     * The complementarity score as adapted from the DirecTag paper
     * (http://www.ncbi.nlm.nih.gov/pubmed/18630943).
     */
    complementarity(5, "complementarity", true);

    /**
     * The name of the score.
     */
    public final String name;
    /**
     * The index of the score of interest.
     */
    public final int index;
    /**
     * Indicates whether the score increases with the quality of the match.
     */
    public final boolean increasing;

    /**
     * Constructor.
     *
     * @param index the index of the score
     * @param name the name of the score
     * @param increasing whether the score increases with the quality of the
     * match
     */
    private PsmScores(int index, String name, boolean increasing) {
        this.index = index;
        this.name = name;
        this.increasing = increasing;
    }

    /**
     * Returns the PSM score of the given index. Null if not found.
     *
     * @param scoreIndex the index of the desired score
     * @return the score of given index
     */
    public static PsmScores getScore(int scoreIndex) {
        for (PsmScores psmScore : values()) {
            if (psmScore.index == scoreIndex) {
                return psmScore;
            }
        }
        return null;
    }

    /**
     * A peptide spectrum annotator used when computing scores.
     */
    private static PeptideSpectrumAnnotator peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. The mass interquartile distance of the fragment ion mass
     * error is used as m/z fidelity score. The score is forced to decrease the
     * quality of the match by taking the opposite value when relevant.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param iontypes the fragment ions to annotate
     * @param neutralLosses the neutral losses to annotate
     * @param charges the fragment charges to look for
     * @param identificationCharge the precursor charge
     * @param shotgunProtocol information on the protocol used
     * @param scoreIndex the index of the score to use
     *
     * @return the score of the match
     */
    public static double getDecreasingScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, HashSet<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int identificationCharge, ShotgunProtocol shotgunProtocol, int scoreIndex) {
        PsmScores psmScore = getScore(scoreIndex);
        double score = getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, shotgunProtocol, psmScore);
        if (psmScore.increasing) {
            return -score;
        }
        return score;
    }

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. The mass interquartile distance of the fragment ion mass
     * error is used as m/z fidelity score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param iontypes the fragment ions to annotate
     * @param neutralLosses the neutral losses to annotate
     * @param charges the fragment charges to look for
     * @param identificationCharge the precursor charge
     * @param shotgunProtocol information on the protocol used
     * @param scoreIndex the index of the score to use
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, HashSet<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int identificationCharge, ShotgunProtocol shotgunProtocol, int scoreIndex) {
        PsmScores psmScore = getScore(scoreIndex);
        return getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, shotgunProtocol, psmScore);
    }

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. The mass interquartile distance of the fragment ion mass
     * error is used as m/z fidelity score.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param iontypes the fragment ions to annotate
     * @param neutralLosses the neutral losses to annotate
     * @param charges the fragment charges to look for
     * @param identificationCharge the precursor charge
     * @param shotgunProtocol information on the protocol used
     * @param psmScore the score to use
     *
     * @return the score of the match
     */
    public static double getScore(Peptide peptide, MSnSpectrum spectrum, HashMap<Ion.IonType, HashSet<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int identificationCharge, ShotgunProtocol shotgunProtocol, PsmScores psmScore) {
        switch (psmScore) {
            case native_score:
                throw new IllegalArgumentException("Impossible to compute the native score of an algorithm");
            case precursor_accuracy:
                return PrecursorAccuracy.getScore(peptide, identificationCharge, spectrum.getPrecursor(), shotgunProtocol.isMs1ResolutionPpm());
            case ms2_mz_fidelity:
                return MS2MzFidelityScore.getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, shotgunProtocol.getMs2Resolution(), peptideSpectrumAnnotator);
            case aa_ms2_mz_fidelity:
                return AAMS2MzFidelityScore.getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, shotgunProtocol.getMs2Resolution(), peptideSpectrumAnnotator);
            case intensity:
                return IntensityRankScore.getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, shotgunProtocol.getMs2Resolution(), peptideSpectrumAnnotator);
            case aa_intensity:
                return AAIntensityRankScore.getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, shotgunProtocol.getMs2Resolution(), peptideSpectrumAnnotator);
            case complementarity:
                return ComplementarityScore.getScore(peptide, spectrum, iontypes, neutralLosses, charges, identificationCharge, shotgunProtocol.getMs2Resolution(), peptideSpectrumAnnotator);
            default:
                throw new UnsupportedOperationException("Score not implemented.");
        }
    }
}
