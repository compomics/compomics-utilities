package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.HashSet;

/**
 * This object will models the assumption made by an advocate.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 25, 2010
 * Time: 2:45:46 PM
 */
public class PeptideAssumption extends ExperimentObject {

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
     * The measured mass of the precursor ion (according to the search engine)
     */
    private double measuredMass;
    /**
     * The e-value
     */
    private double eValue;
    /**
     * The number of 1Da intervals between the measured mass and the theoretical mass
     */
    private int c13;
    /**
     * The precursor mass deviation in ppm;
     */
    private double ppmMassError;
    /**
     * The fragment ion annotation
     */
    private HashSet<IonMatch> annotations = new HashSet<IonMatch>();
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
     * @param measuredMass             the precursor measured mass
     * @param eValue                the e-value
     * @param identificationFile    the identification file
     */
    public PeptideAssumption(Peptide aPeptide, int rank, int advocate, double measuredMass, double eValue, String identificationFile) {
        this.peptide = aPeptide;
        this.rank = rank;
        this.advocate = advocate;
        this.measuredMass = measuredMass;
        this.eValue = eValue;
        this.file = identificationFile;
        this.c13 = (new Double(measuredMass-peptide.getMass())).intValue();
        this.ppmMassError = Math.abs(1000000*(measuredMass-c13-peptide.getMass())/peptide.getMass());
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
     * Returns the measured precursor mass according to the search engine
     * @return the measured precursor mass according to the search engine
     */
    public double getMeasuredMass() {
        return measuredMass;
    }

    public int getC13() {
        return c13;
    }

    /**
     * returns the precursor mass error (in ppm)
     *
     * @return the precursor mass error (in ppm)
     */
    public double getDeltaMass() {
        return ppmMassError;
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
     * add a fragment ion annotation
     *
     * @param ionMatch an ion match
     */
    public void addAnnotation(IonMatch ionMatch) {
        annotations.add(ionMatch);
    }

    /**
     * retrieves all fragment ion annotation
     *
     * @return all fragment ion annotations
     */
    public HashSet<IonMatch> getAnnotations() {
        return annotations;
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
            for (Protein protein : peptide.getParentProteins()) {
                if (!protein.isDecoy()) {
                    isDecoy = false;
        return isDecoy;
                }
            }
            isDecoy = true;
        }
        return isDecoy;
    }
}
