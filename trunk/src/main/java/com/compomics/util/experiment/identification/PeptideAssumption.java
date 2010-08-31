package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;

import java.util.HashSet;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 25, 2010
 * Time: 2:45:46 PM
 * This object will modelize the assumption made by an advocate.
 */
public class PeptideAssumption {

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
     * The modification matched
     */
    private ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();
    /**
     * the correspondig file
     */
    private String file;
    /**
     * is it a decoy identification?
     */
    private boolean isDecoy;
    /**
     * The probability assigned to this identification
     */
    private double probability;
    /**
     * The score assigned to this idenitification
     */
    private double score;
    /**
     * Was there a 1Da shift between the precursor mass and the measured mass?
     */
    private boolean c13 = false;

    /**
     * Constructor for a peptide assumption
     * @param aPeptide              the theoretic peptide
     * @param rank                  the identification rank
     * @param advocate              the advocate used
     * @param deltaMass             the peptide mass error
     * @param eValue                the e-value
     * @param modifications         the modifications
     * @param identificationFile    the identification file
     * @param isDecoy               is the identification decoy?
     */
    public PeptideAssumption(Peptide aPeptide, int rank, int advocate, double deltaMass, double eValue, ArrayList<ModificationMatch> modifications, String identificationFile, boolean isDecoy) {
        this.peptide = aPeptide;
        this.rank = rank;
        this.advocate = advocate;
        this.deltaMass = deltaMass;
        this.eValue = eValue;
        this.modifications = modifications;
        this.file = identificationFile;
        this.isDecoy = isDecoy;
    }

    /**
     * Flag that a precursor mass error of 1Da was detected
     */
    public void setC13() {
        c13 = true;
    }

    /**
     * returns if a precursor mass error of 1Da has been detected
     * @return boolean showing if a mass error of 1Da was detected
     */
    public boolean isC13() {
        return c13;
    }

    /**
     * returns the score
     * @return the advocate score
     */
    public double getScore() {
        return score;
    }

    /**
     * sets the score
     * @param score the advocate score
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Get the identification rank
     * @return the identification rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * get the theoretic peptide
     * @return the peptide
     */
    public Peptide getPeptide() {
        return peptide;
    }

    /**
     * get the used advocate
     * @return the advocate index
     */
    public int getAdvocate() {
        return advocate;
    }

    /**
     * returns the precursor mass error (in ppm)
     * @return the precursor mass error (in ppm)
     */
    public double getDeltaMass() {
        return deltaMass;
    }

    /**
     * returns the e-value assigned by the advocate
     * @return the e-value
     */
    public double getEValue() {
        return eValue;
    }

    /**
     * add a fragment ion annotation
     * @param ionMatch an ion match
     */
    public void addAnnotation(IonMatch ionMatch) {
        annotations.add(ionMatch);
    }

    /**
     * retrieves all fragment ion annotation
     * @return all fragment ion annotations
     */
    public HashSet<IonMatch> getAnnotations() {
        return annotations;
    }

    /**
     * returns all modifications
     * @return modifications
     */
    public ArrayList<ModificationMatch> getModifications() {
        return modifications;
    }

    /**
     * returns the file
     * @return the idenitfication file
     */
    public String getFile() {
        return file;
    }

    /**
     * is the identification decoy?
     * @return a boolean indicating if the identification is a decoy one
     */
    public boolean isDecoy() {
        return isDecoy;
    }

    /**
     * sets the probability attached to this identification
     * @param probability   the probability
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * get the probability attached to this identification
     * @return the probability
     */
    public double getProbability() {
        return probability;
    }
}
