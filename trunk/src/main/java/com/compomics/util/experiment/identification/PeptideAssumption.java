package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.massspectrometry.Charge;

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

    @Override
    public Double getTheoreticMass() {
        return peptide.getMass();
    }
}
