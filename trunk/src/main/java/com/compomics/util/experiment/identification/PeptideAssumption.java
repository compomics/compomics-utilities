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

    private int rank;
    private Peptide peptide;
    private int advocate;
    private double deltaMass;
    private double eValue;
    private HashSet<IonMatch> annotations = new HashSet<IonMatch>();
    private ArrayList<ModificationMatch> modifications = new ArrayList<ModificationMatch>();
    private String file;
    private boolean isDecoy;
    private double probability;
    private double score;
    private boolean c13 = false;

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

    public void setC13() {
        c13 = true;
    }

    public boolean isC13() {
        return c13;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public Peptide getPeptide() {
        return peptide;
    }

    public int getAdvocate() {
        return advocate;
    }

    public double getDeltaMass() {
        return deltaMass;
    }

    public double getEValue() {
        return eValue;
    }

    public void addAnnotation(IonMatch ionMatch) {
        annotations.add(ionMatch);
    }

    public HashSet<IonMatch> getAnnotations() {
        return annotations;
    }

    public ArrayList<ModificationMatch> getModifications() {
        return modifications;
    }

    public String getFile() {
        return file;
    }

    public boolean isDecoy() {
        return isDecoy;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public double getProbability() {
        return probability;
    }
}
