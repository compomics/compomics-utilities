package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PrecursorIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This object will models the assumption made by an advocate.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PeptideAssumption extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 3606509518581203063L;
    /**
     * The rank of the peptide assumption for the concerned spectrum.
     */
    private int rank;
    /**
     * The theoretic peptide.
     */
    private Peptide peptide;
    /**
     * The advocate.
     */
    private int advocate;
    /**
     * The charge used for identification.
     */
    private Charge identificationCharge;
    /**
     * The score, the lower the better.
     * Ought to be renamed but kept for backward compatibility
     */
    private double eValue;
    /**
     * the corresponding file.
     */
    private String file;
    /**
     * Is it a decoy identification?
     */
    private Boolean isDecoy = null;

    /**
     * Constructor for a peptide assumption.
     *
     * @param aPeptide the theoretic peptide
     * @param rank the identification rank
     * @param advocate the advocate used
     * @param identificationCharge the charge used by the search engine for
     * identification
     * @param eValue the e-value
     * @param identificationFile the identification file
     */
    public PeptideAssumption(Peptide aPeptide, int rank, int advocate, Charge identificationCharge, double eValue, String identificationFile) {
        this.peptide = aPeptide;
        this.rank = rank;
        this.advocate = advocate;
        this.identificationCharge = identificationCharge;
        this.eValue = eValue;
        this.file = identificationFile;
    }

    /**
     * Constructor for a peptide assumption.
     *
     * @param aPeptide the theoretic peptide
     * @param rank the identification rank
     * @param advocate the advocate used
     * @param identificationCharge the charge used by the search engine for
     * identification
     * @param score the score (the lower, the better)
     */
    public PeptideAssumption(Peptide aPeptide, int rank, int advocate, Charge identificationCharge, double score) {
        this.peptide = aPeptide;
        this.rank = rank;
        this.advocate = advocate;
        this.identificationCharge = identificationCharge;
        this.eValue = score;
    }

    /**
     * Get the identification rank.
     *
     * @return the identification rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the theoretic peptide.
     *
     * @return the peptide
     */
    public Peptide getPeptide() {
        return peptide;
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
     * Returns the precursor mass error (in ppm or Da). Note that the value is
     * returns as (experimental mass - theoretical mass) and that negative
     * values thus can occur. The isotopic error can subtracted and retrieved by
     * the function getIsotopeNumber().
     *
     * @param measuredMZ the precursor m/z
     * @param ppm if true the error is returns in ppm, false returns the error
     * in Da
     * @param subtractIsotope if true the isotope number will be subtracted from
     * the theoretic mass
     * @return the precursor mass error (in ppm or Da)
     */
    public double getDeltaMass(double measuredMZ, boolean ppm, boolean subtractIsotope) {
        return getPrecursorMatch(new Peak(measuredMZ, 0, 0)).getError(ppm, subtractIsotope);
    }

    /**
     * Returns the precursor mass error (in ppm or Da). Note that the value is
     * returns as (experimental mass - theoretical mass) and that negative
     * values thus can occur. The isotopic error is subtracted and can be
     * retrieved by the function getIsotopeNumber().
     *
     * @param measuredMZ the precursor m/z
     * @param ppm if true the error is returns in ppm, false returns the error
     * in Da
     * @return the precursor mass error (in ppm or Da)
     */
    public double getDeltaMass(double measuredMZ, boolean ppm) {
        return getPrecursorMatch(new Peak(measuredMZ, 0, 0)).getError(ppm, true);
    }

    /**
     * Returns the precursor isotope number according to the number of protons.
     *
     * @param measuredMZ
     * @return the precursor isotope number according to the number of protons
     */
    public int getIsotopeNumber(double measuredMZ) {
        return getPrecursorMatch(new Peak(measuredMZ, 0, 0)).getIsotopeNumber();
    }

    /**
     * Returns the e-value assigned by the advocate.
     *
     * @return the e-value
     */
    public double getEValue() {
        return eValue;
    }

    /**
     * Returns the file.
     *
     * @return the identification file
     */
    public String getFile() {
        return file;
    }

    /**
     * Is the identification decoy?
     *
     * @return a boolean indicating if the identification is a decoy one
     */
    public boolean isDecoy() {
        if (isDecoy == null) {
            for (String protein : peptide.getParentProteins()) {
                if (SequenceFactory.isDecoy(protein)) {
                    isDecoy = true;
                    return isDecoy;
                }
            }
            isDecoy = false;
        }
        return isDecoy;
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
     * Set the rank of the PeptideAssumption.
     *
     * @param aRank the rank of the PeptideAssumptio
     */
    public void setRank(int aRank) {
        rank = aRank;
    }

    /**
     * Returns the ion match.
     *
     * @param precursorPeak
     * @return the ion match
     */
    public IonMatch getPrecursorMatch(Peak precursorPeak) {
        return new IonMatch(precursorPeak, new PrecursorIon(peptide), identificationCharge);
    }
}
