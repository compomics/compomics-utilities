package com.compomics.util.experiment.identification.spectrum_assumptions;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;

/**
 * This object models the assumption made by an advocate.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PeptideAssumption extends SpectrumIdentificationAssumption {

    /**
     * The theoretic peptide.
     */
    private Peptide peptide;

    /**
     * Default constructor for the purpose of instantiation.
     */
    public PeptideAssumption() {
    }
    
    /**
     * Constructor for a peptide assumption.
     *
     * @param peptide the theoretic peptide
     * @param rank the identification rank
     * @param advocate the advocate used
     * @param identificationCharge the charge used by the search engine for
     * identification
     * @param rawScore the raw score, i.e. the untransformed score given by the
     * search engine
     * @param score the (potentially transformed) score, typically a search
     * engine e-value (whether the score is ascending or descending can be known
     * from the SearchEngine class)
     * @param identificationFile the identification file
     */
    public PeptideAssumption(
            Peptide peptide,
            int rank,
            int advocate,
            int identificationCharge,
            double rawScore,
            double score,
            String identificationFile
    ) {
        this.peptide = peptide;
        super.rank = rank;
        super.advocate = advocate;
        super.identificationCharge = identificationCharge;
        super.rawScore = rawScore;
        super.score = score;
        super.identificationFile = identificationFile;
    }

    /**
     * Constructor for a peptide assumption.
     *
     * @param peptide the theoretic peptide
     * @param rank the identification rank
     * @param advocate the advocate used
     * @param identificationCharge the charge used by the search engine for
     * identification
     * @param rawScore the raw score, i.e. the untransformed score given by the
     * search engine
     * @param score the (potentially transformed) score, typically a search
     * engine e-value (whether the score is ascending or descending can be known
     * from the SearchEngine class)
     */
    public PeptideAssumption(
            Peptide peptide,
            int rank,
            int advocate,
            int identificationCharge,
            double rawScore,
            double score
    ) {
        this.peptide = peptide;
        super.rank = rank;
        super.advocate = advocate;
        super.identificationCharge = identificationCharge;
        super.rawScore = rawScore;
        super.score = score;
    }

    /**
     * Constructor for a simple peptide assumption containing only the
     * information necessary for spectrum annotation.
     *
     * @param peptide the theoretic peptide
     * @param identificationCharge the charge used by the search engine for
     * identification
     */
    public PeptideAssumption(
            Peptide peptide,
            int identificationCharge
    ) {
        this.peptide = peptide;
        super.identificationCharge = identificationCharge;
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
     * Returns the peptides.
     * 
     * @param peptide the peptide
     */
    public void setPeptide(Peptide peptide) {

        this.peptide = peptide;
    }

    @Override
    public double getTheoreticMass() {

        return peptide.getMass();
    }
}
