package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Ion;
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
public class PeptideAssumption extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 3606509518581203063L;
    /**
     * The rank of the peptide assumption for the concerned spectrum
     */
    private int rank;
    /**
     * The theoretic peptide
     */
    private Peptide peptide;
    /**
     * The advocate
     */
    private int advocate;
    /**
     * The charge used for identification
     */
    private Charge identificationCharge;
    /**
     * The e-value
     */
    private double eValue;
    /**
     * the correspondig file
     */
    private String file;
    /**
     * is it a decoy identification?
     */
    private Boolean isDecoy = null;

    /**
     * Constructor for a peptide assumption
     *
     * @param aPeptide              the theoretic peptide
     * @param rank                  the identification rank
     * @param advocate              the advocate used
     * @param identificationCharge  the charge used by the search engine for identification
     * @param eValue                the e-value
     * @param identificationFile    the identification file
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
     * Get the identification rank
     *
     * @return the identification rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * get the theoretic peptide
     *
     * @return the peptide
     */
    public Peptide getPeptide() {
        return peptide;
    }

    /**
     * get the used advocate
     *
     * @return the advocate index
     */
    public int getAdvocate() {
        return advocate;
    }

    /**
     * Returns the distance in Da between the experimental mass and theoretic mass, image of the error between the precursor mass and the peptide monoisotopic mass (typically for the C13 option)
     * @param measuredMZ the precursor m/z
     * @return  the distance in Da between the experimental mass and theoretic mass
     */
    public int getC13(double measuredMZ) {
        return (int) Math.round(measuredMZ*identificationCharge.value-identificationCharge.value*ElementaryIon.proton.getTheoreticMass()-peptide.getMass());
    }

    /**
     * Returns the precursor mass error (in ppm or Da). Note that the value is 
     * returns as (experimental mass - theoretical mass) and that negative values 
     * thus can occur.
     * If an error of more than 1 Da it will be substracted from the error. The C13 error can be retrieved by the function getC13().
     *
     * @param measuredMZ   the precursor m/z
     * @param ppm           if true the error is returns in ppm, false returns the error in Da
     * @return              the precursor mass error (in ppm or Da)
     */
    public double getDeltaMass(double measuredMZ, boolean ppm) {
        return getPrecursorMatch(new Peak(measuredMZ, 0, 0)).getError(ppm);
    }

    /**
     * returns the e-value assigned by the advocate
     *
     * @return the e-value
     */
    public double getEValue() {
        return eValue;
    }

    /**
     * returns the file
     *
     * @return the idenitfication file
     */
    public String getFile() {
        return file;
    }

    /**
     * is the identification decoy?
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
     * Returns the charge used for identification
     * @return the charge used for identification 
     */
    public Charge getIdentificationCharge() {
        return identificationCharge;
    }

    /**
     * Set the rank of the PeptideAssumption
     */
    public void setRank(int aRank){
        rank = aRank;
    }
    
    /**
     * Returns the ion match between the 
     * @param precursorPeak
     * @return 
     */
    public IonMatch getPrecursorMatch(Peak precursorPeak) {
        return new IonMatch(precursorPeak, new PrecursorIon(peptide), identificationCharge);
    }
    
}
