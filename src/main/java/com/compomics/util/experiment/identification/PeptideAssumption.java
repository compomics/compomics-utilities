package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
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
public class PeptideAssumption extends SpectrumIdentificationAssumption {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 3606509518581203063L;
    /**
     * The theoretic peptide.
     */
    private Peptide peptide;
    /**
     * The advocate supporting this assumption.
     *
     * @deprecated use the SpectrumIdentificationAssumption attribute
     */
    private int advocate;
    /**
     * The charge used for identification.
     *
     * @deprecated use the SpectrumIdentificationAssumption attribute
     */
    private Charge identificationCharge;
    /**
     * The rank of the peptide assumption for the concerned spectrum.
     *
     * @deprecated use the SpectrumIdentificationAssumption attribute
     */
    private int rank;
    /**
     * The score, the lower the better. Ought to be renamed but kept for
     * backward compatibility
     *
     * @deprecated use the SpectrumIdentificationAssumption attribute
     */
    private double eValue;
    /**
     * the corresponding identification file.
     *
     * @deprecated use the SpectrumIdentificationAssumption attribute
     */
    private String file;

    /**
     * Constructor for a peptide assumption.
     *
     * @param aPeptide the theoretic peptide
     * @param rank the identification rank
     * @param advocate the advocate used
     * @param identificationCharge the charge used by the search engine for
     * identification
     * @param score the score, typically a search engine e-value (whether the
     * score is ascending or descending can be known from the SearchEngine
     * class)
     * @param identificationFile the identification file
     */
    public PeptideAssumption(Peptide aPeptide, int rank, int advocate, Charge identificationCharge, double score, String identificationFile) {
        this.peptide = aPeptide;
        super.rank = rank;
        super.advocate = advocate;
        super.identificationCharge = identificationCharge;
        super.score = score;
        super.identificationFile = identificationFile;
    }

    /**
     * Constructor for a peptide assumption.
     *
     * @param aPeptide the theoretic peptide
     * @param rank the identification rank
     * @param advocate the advocate used
     * @param identificationCharge the charge used by the search engine for
     * identification
     * @param score the score (whether the score is ascending or descending can
     * be known from the SearchEngine class)
     */
    public PeptideAssumption(Peptide aPeptide, int rank, int advocate, Charge identificationCharge, double score) {
        this.peptide = aPeptide;
        super.rank = rank;
        super.advocate = advocate;
        super.identificationCharge = identificationCharge;
        super.score = score;
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
     * Returns the theoretic m/z expected for this assumption.
     *
     * @return the theoretic m/z expected for this assumption
     */
    public double getTheoreticMz() {
        return (peptide.getMass() + getIdentificationCharge().value * ElementaryIon.proton.getTheoreticMass()) / getIdentificationCharge().value;
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
     * Returns the ion match.
     *
     * @param precursorPeak
     * @return the ion match
     */
    public IonMatch getPrecursorMatch(Peak precursorPeak) {
        return new IonMatch(precursorPeak, new PrecursorIon(peptide), getIdentificationCharge());
    }

    @Override
    public int getRank() {
        if (super.identificationCharge == null) { // backward compatibility check
            return rank;
        } else {
            return super.rank;
        }
    }

    @Override
    public int getAdvocate() {
        if (super.identificationCharge == null) { // backward compatibility check
            return advocate;
        } else {
            return super.advocate;
        }
    }

    /**
     * Returns the e-value assigned by the advocate.
     *
     * @deprecated use getScore instead
     * @return the e-value
     */
    public double getEValue() {
        return eValue;
    }

    @Override
    public double getScore() {
        if (super.identificationCharge == null) { // backward compatibility check
            return eValue;
        } else {
            return super.score;
        }
    }

    @Override
    public String getIdentificationFile() {
        if (super.identificationCharge == null) { // backward compatibility check
            return file;
        } else {
            return super.identificationFile;
        }
    }

    @Override
    public Charge getIdentificationCharge() {
        if (super.identificationCharge == null) { // backward compatibility check
            return identificationCharge;
        } else {
            return super.identificationCharge;
        }
    }
}
