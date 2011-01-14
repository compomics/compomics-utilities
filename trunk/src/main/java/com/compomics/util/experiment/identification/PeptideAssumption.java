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
     * The precursor mass error
     */
    private double deltaMass;
    /**
     * The e-value
     */
    private double eValue;
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
     * @param deltaMass             the peptide mass error
     * @param eValue                the e-value
     * @param identificationFile    the identification file
     */
    public PeptideAssumption(Peptide aPeptide, int rank, int advocate, double deltaMass, double eValue, String identificationFile) {
        this.peptide = aPeptide;
        this.rank = rank;
        this.advocate = advocate;
        this.deltaMass = deltaMass;
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
     * returns the precursor mass error (in ppm)
     *
     * @return the precursor mass error (in ppm)
     */
    public double getDeltaMass() {
        return deltaMass;
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
