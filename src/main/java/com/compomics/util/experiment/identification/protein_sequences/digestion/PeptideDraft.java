package com.compomics.util.experiment.identification.protein_sequences.digestion;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.general.BoxedObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Draft of a peptide used during protein sequence digestion.
 *
 * @author Marc Vaudel
 */
public class PeptideDraft {

    /**
     * The amino acid sequence as char array.
     */
    private char[] sequence;
    /**
     * The N-terminal modification.
     */
    private String nTermModification;
    /**
     * The C-terminal modification.
     */
    private String cTermModification;
    /**
     * The modifications at specific amino acids.
     */
    private HashMap<Integer, String> fixedAaModifications;
    /**
     * The current mass of the peptide draft.
     */
    private double mass;
    /**
     * The number of missed cleavages.
     */
    private int missedCleavages = 0;
    /**
     * The number of Xs already considered in this draft.
     */
    private int nX = 0;
    /**
     * The peptide index on the protein.
     */
    private int indexOnProtein;

    /**
     * Constructor.
     *
     * @param sequence the peptide sequence.
     */
    public PeptideDraft(char[] sequence) {
        this.sequence = sequence;
    }

    /**
     * Constructor.
     *
     * @param sequence the peptide sequence
     * @param nTermModification the N-term modification
     * @param fixedAaModifications the fixed modifications at amino acids
     * @param mass the mass
     */
    public PeptideDraft(char[] sequence, String nTermModification, HashMap<Integer, String> fixedAaModifications, double mass) {
        this.sequence = sequence;
        this.nTermModification = nTermModification;
        this.fixedAaModifications = fixedAaModifications;
        this.mass = mass;
    }

    /**
     * Constructor.
     *
     * @param sequence the peptide sequence
     * @param nTermModification the N-term modification
     * @param fixedAaModifications the fixed modifications at amino acids
     * @param mass the mass
     * @param missedCleavages the number of missed cleavages
     */
    public PeptideDraft(char[] sequence, String nTermModification, HashMap<Integer, String> fixedAaModifications, double mass, int missedCleavages) {
        this.sequence = sequence;
        this.nTermModification = nTermModification;
        this.fixedAaModifications = fixedAaModifications;
        this.mass = mass;
        this.missedCleavages = missedCleavages;
    }

    /**
     * Constructor.
     *
     * @param sequence the peptide sequence
     * @param nTermModification the N-term modification
     * @param cTermModification the C-term modification
     * @param fixedAaModifications the fixed modifications at amino acids
     * @param mass the mass
     */
    public PeptideDraft(char[] sequence, String nTermModification, String cTermModification, HashMap<Integer, String> fixedAaModifications, double mass) {
        this.sequence = sequence;
        this.nTermModification = nTermModification;
        this.cTermModification = cTermModification;
        this.fixedAaModifications = fixedAaModifications;
        this.mass = mass;
    }

    /**
     * Constructor.
     *
     * @param sequence the peptide sequence
     * @param nTermModification the N-term modification
     * @param cTermModification the C-term modification
     * @param fixedAaModifications the fixed modifications at amino acids
     * @param mass the mass
     * @param missedCleavages the number of missed cleavages
     */
    public PeptideDraft(char[] sequence, String nTermModification, String cTermModification, HashMap<Integer, String> fixedAaModifications, double mass, int missedCleavages) {
        this.sequence = sequence;
        this.nTermModification = nTermModification;
        this.cTermModification = cTermModification;
        this.fixedAaModifications = fixedAaModifications;
        this.mass = mass;
        this.missedCleavages = missedCleavages;
    }

    /**
     * Creates a new peptide draft with the same attributes as this one.
     *
     * @return a new peptide draft
     */
    public PeptideDraft clone() {
        PeptideDraft newPeptideDraft = new PeptideDraft(Arrays.copyOf(sequence, sequence.length), nTermModification, cTermModification, new HashMap<>(fixedAaModifications), mass, missedCleavages);
        return newPeptideDraft;
    }

    /**
     * Returns the sequence.
     *
     * @return the sequence
     */
    public char[] getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence.
     *
     * @param sequence the sequence
     */
    public void setSequence(char[] sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the length of the sequence.
     *
     * @return the length of the sequence
     */
    public int length() {
        return sequence.length;
    }

    /**
     * Returns the N-term modification.
     *
     * @return the N-term modification
     */
    public String getnTermModification() {
        return nTermModification;
    }

    /**
     * Sets the N-term modification.
     *
     * @param nTermModification the N-term modification
     */
    public void setnTermModification(String nTermModification) {
        this.nTermModification = nTermModification;
    }

    /**
     * Returns the C-term modification.
     *
     * @return the C-term modification
     */
    public String getcTermModification() {
        return cTermModification;
    }

    /**
     * Sets the C-term modification.
     *
     * @param cTermModification the C-term modification
     */
    public void setcTermModification(String cTermModification) {
        this.cTermModification = cTermModification;
    }

    /**
     * Returns the mass.
     *
     * @return the mass
     */
    public double getMass() {
        return mass;
    }

    /**
     * Sets the mass.
     *
     * @param mass the mass
     */
    public void setMass(double mass) {
        this.mass = mass;
    }

    /**
     * Returns the modifications at specific amino acids.
     *
     * @return the modifications at specific amino acids
     */
    public HashMap<Integer, String> getFixedAaModifications() {
        return fixedAaModifications;
    }

    /**
     * Sets the modifications at specific amino acids.
     *
     * @param fixedAaModifications the modifications at specific amino acids
     */
    public void setFixedAaModifications(HashMap<Integer, String> fixedAaModifications) {
        this.fixedAaModifications = fixedAaModifications;
    }

    /**
     * Increases the number of missed cleavages.
     */
    public void increaseMissedCleavages() {
        missedCleavages++;
    }

    /**
     * Returns the number of missed cleavages.
     *
     * @return the number of missed cleavages
     */
    public int getMissedCleavages() {
        return missedCleavages;
    }

    /**
     * Increases the number of Xs already considered in this draft.
     */
    public void increaseNX() {
        nX++;
    }

    /**
     * Returns the number of Xs already considered in this draft.
     *
     * @return the number of Xs
     */
    public int getnX() {
        return nX;
    }

    /**
     * Returns the peptide index on the protein.
     *
     * @return the peptide index on the protein
     */
    public int getIndexOnProtein() {
        return indexOnProtein;
    }

    /**
     * Sets the peptide index on the protein.
     *
     * @param indexOnProtein the peptide index on the protein
     */
    public void setIndexOnProtein(int indexOnProtein) {
        this.indexOnProtein = indexOnProtein;
    }

    /**
     * Returns a peptide from the peptide draft.
     *
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return the peptide built from the peptide draft
     */
    public Peptide getPeptide(double massMin, double massMax) {
        return getPeptide(massMin, massMax, new BoxedObject<>(Boolean.FALSE));
    }

    /**
     * Returns a peptide from the peptide draft.
     *
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     * @param smallMass an encapsulated boolean indicating whether the peptide
     * passed the maximal mass filter
     *
     * @return the peptide built from the peptide draft
     */
    public Peptide getPeptide(double massMin, double massMax, BoxedObject<Boolean> smallMass) {

        double peptideMass = getMass();
        double tempMass = peptideMass + ProteinIteratorUtils.WATER_MASS;

        if (massMax == 0.0 || tempMass <= massMax) {

            smallMass.setObject(Boolean.TRUE);

            if (tempMass >= massMin) {

                ArrayList<ModificationMatch> modificationMatches = null;

                if (nTermModification != null) {
                    
                    modificationMatches = new ArrayList<>(fixedAaModifications.size());
                    modificationMatches.add(new ModificationMatch(nTermModification, false, 1));
                
                }

                if (cTermModification != null) {

                    if (modificationMatches == null) {
                
                        modificationMatches = new ArrayList<>(fixedAaModifications.size());
                    
                    }

                    modificationMatches.add(new ModificationMatch(cTermModification, false, length()));

                }

                for (Integer site : fixedAaModifications.keySet()) {

                    if (modificationMatches == null) {

                        modificationMatches = new ArrayList<>(fixedAaModifications.size());

                    }

                    String modificationName = fixedAaModifications.get(site);
                    modificationMatches.add(new ModificationMatch(modificationName, false, site));

                }

                return modificationMatches == null ? new Peptide(new String(getSequence()), null, false, tempMass) :
                        new Peptide(new String(getSequence()), modificationMatches.toArray(new ModificationMatch[modificationMatches.size()]), false, tempMass);

            }
        }

        return null;
    }
}
