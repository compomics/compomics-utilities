package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.biology.ions.PrecursorIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;

/**
 * Spectrum identification assumption made by an identification algorithm.
 *
 * @author Marc Vaudel
 */
public abstract class SpectrumIdentificationAssumption extends ExperimentObject {

    /**
     * Serialization id for backward compatibility.
     */
    static final long serialVersionUID = 496273793273328259L;
    /**
     * The rank of the peptide assumption for the concerned spectrum.
     */
    protected int rank;
    /**
     * The advocate supporting this assumption.
     */
    protected int advocate;
    /**
     * The charge used for identification.
     */
    protected Charge identificationCharge;
    /**
     * The (advocate specific) score used to rank this assumption.
     */
    protected Double score;
    /**
     * The identification file.
     */
    protected String identificationFile;
    /**
     * The raw score as provided by the identification algorithm.
     */
    protected Double rawScore;
    /**
     * The individual amino acid scores. Null if not set.
     */
    protected ArrayList<double[]> aminoAcidScores = null;

    /**
     * Get the identification rank.
     *
     * @return the identification rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Set the rank of the PeptideAssumption.
     *
     * @param aRank the rank of the PeptideAssumptio
     */
    public void setRank(int aRank) {
        rank = aRank;
    }

    /**
     * Get the used advocate.
     *
     * @return the advocate index
     */
    public int getAdvocate() {
        return advocate;
    }

    /**
     * Returns the score assigned by the advocate.
     *
     * @return the score
     */
    public Double getScore() {
        return score;
    }

    /**
     * Sets the score. Note: if PsmScores.scoreRoundingDecimal is not null the
     * scored will be floored accordingly.
     *
     * @param score the score
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Returns the identification file.
     *
     * @return the identification file
     */
    public String getIdentificationFile() {
        return identificationFile;
    }

    /**
     * Returns the charge used for identification.
     *
     * @return the charge used for identification
     */
    public Charge getIdentificationCharge() {
        return identificationCharge;
    }

    /**
     * Sets the identification charge.
     *
     * @param identificationCharge the identification charge
     */
    public void setIdentificationCharge(Charge identificationCharge) {
        this.identificationCharge = identificationCharge;
    }

    /**
     * Returns the theoretic mass of the given assumption.
     *
     * @return the theoretic mass of the given assumption
     */
    public abstract Double getTheoreticMass();

    /**
     * Returns the theoretic m/z.
     *
     * @return the theoretic m/z
     */
    public Double getTheoreticMz() {
        return (getTheoreticMass() + ElementaryIon.proton.getTheoreticMass() * identificationCharge.value) / identificationCharge.value;
    }

    /**
     * Returns the precursor mass error (in ppm or Da). Note that the value is
     * returns as (experimental mass - theoretical mass) and that negative
     * values thus can occur. The isotopic error can subtracted and retrieved by
     * the function getIsotopeNumber().
     *
     * @param measuredMZ the precursor m/z
     * @param ppm if true the error is returns in ppm, false returns the error
     * in Da
     * @param minIsotope the minimal isotope
     * @param maxIsotope the maximal isotope
     * 
     * @return the precursor mass error (in ppm or Da)
     */
    public Double getDeltaMass(double measuredMZ, boolean ppm, int minIsotope, int maxIsotope) {
        return getPrecursorMatch(new Peak(measuredMZ, 0, 0)).getError(ppm, minIsotope, maxIsotope);
    }

    /**
     * Returns the precursor isotope number according to the number of protons.
     *
     * @param measuredMZ the measured m/z value
     * @param minIsotope the minimal isotope
     * @param maxIsotope the maximal isotope
     * 
     * @return the precursor isotope number according to the number of protons
     */
    public int getIsotopeNumber(double measuredMZ, int minIsotope, int maxIsotope) {
        return getPrecursorMatch(new Peak(measuredMZ, 0, 0)).getIsotopeNumber(minIsotope, maxIsotope);
    }

    /**
     * Returns the ion match.
     *
     * @param precursorPeak the precursor peak
     * @return the ion match
     */
    public IonMatch getPrecursorMatch(Peak precursorPeak) {
        return new IonMatch(precursorPeak, new PrecursorIon(getTheoreticMass()), getIdentificationCharge().value);
    }

    /**
     * Returns the raw score as provided by the identification algorithm.
     *
     * @return the raw score as provided by the identification algorithm
     */
    public Double getRawScore() {
        return rawScore;
    }

    /**
     * Sets the raw score as provided by the identification algorithm.
     *
     * @param rawScore the raw score as provided by the identification algorithm
     */
    public void setRawScore(Double rawScore) {
        this.rawScore = rawScore;
    }

    /**
     * Returns the amino acid scores. Null of not set. For Peptide objects the
     * list only contains one element, while for Tag objects the list will be
     * indexed in identically to the TagComponent list.
     *
     * @return the amino acid scores
     */
    public ArrayList<double[]> getAminoAcidScores() {
        return aminoAcidScores;
    }

    /**
     * Set the amino acid scores. For Peptide objects the list should only
     * contain one element, while for Tag objects the list should be indexed in
     * identical to the TagComponent list.
     *
     * @param aminoAcidScores the amino acid scores
     */
    public void setAminoAcidScores(ArrayList<double[]> aminoAcidScores) {
        this.aminoAcidScores = aminoAcidScores;
    }
}
