package com.compomics.util.experiment.identification.spectrum_assumptions;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
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
     * @param peptide the theoretic peptide
     * @param rank the identification rank
     * @param advocate the advocate used
     * @param identificationCharge the charge used by the search engine for
     * identification
     * @param score the score, typically a search engine e-value (whether the
     * score is ascending or descending can be known from the SearchEngine
     * class)
     * @param identificationFile the identification file
     */
    public PeptideAssumption(Peptide peptide, int rank, int advocate, Charge identificationCharge, double score, String identificationFile) {
        this.peptide = peptide;
        super.rank = rank;
        super.advocate = advocate;
        super.identificationCharge = identificationCharge;
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
     * @param score the score (whether the score is ascending or descending can
     * be known from the SearchEngine class)
     */
    public PeptideAssumption(Peptide peptide, int rank, int advocate, Charge identificationCharge, double score) {
        this.peptide = peptide;
        super.rank = rank;
        super.advocate = advocate;
        super.identificationCharge = identificationCharge;
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
    public PeptideAssumption(Peptide peptide, Charge identificationCharge) {
        this.peptide = peptide;
        super.identificationCharge = identificationCharge;
    }
    
    public PeptideAssumption(){}

    /**
     * Get the theoretic peptide.
     *
     * @return the peptide
     */
    public Peptide getPeptide() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return peptide;
    }
    
    public void setPeptide(Peptide peptide){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.peptide = peptide;
    }

    @Override
    public double getTheoreticMass() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return peptide.getMass();
    }
}
