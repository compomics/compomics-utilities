package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.utils.ExperimentObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * This class models a peptide.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:56:40 AM
 */
public class Peptide extends ExperimentObject {

    /**
     * The peptide sequence
     */
    private String sequence;
    /**
     * The peptide mass
     */
    private Double mass;
    /**
     * The theoretic fragment ions after fragmentation
     */
    private HashSet<PeptideFragmentIon> theoreticPeptideFragmentIons = new HashSet();
    /**
     * The parent proteins
     */
    private ArrayList<Protein> parentProteins;

    /**
     * Constructor for the peptide
     */
    public Peptide() {
    }

    /**
     * Constructor for the peptide
     *
     * @param aSequence         The peptide sequence
     * @param mass              The peptide mass
     * @param parentProteins    The parent proteins
     */
    public Peptide(String aSequence, Double mass, ArrayList<Protein> parentProteins) {
        this.sequence = aSequence;
        this.mass = mass;
        this.parentProteins = parentProteins;
    }

    /**
     * getter for the mass
     *
     * @return the peptide mass
     */
    public Double getMass() {
        return mass;
    }

    /**
     * getter for the sequence
     *
     * @return the peptide sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * method to add a fragmentIon
     *
     * @param fragment  a fragment ion of this peptide
     */
    public void addFragmentIon(PeptideFragmentIon fragment) {
        theoreticPeptideFragmentIons.add(fragment);
    }

    /**
     * Method which returns an iterator on the fragment ions
     *
     * @return an iterator on implemented fragment ions
     */
    public Iterator<PeptideFragmentIon> fragmentIterator() {
        return theoreticPeptideFragmentIons.iterator();
    }

    /**
     * Getter for the parent proteins
     *
     * @return the parent proteins
     */
    public ArrayList<Protein> getParentProteins() {
        return parentProteins;
    }

    /**
     * a method which compares to peptides. For now sequence-based.
     * 
     * @param anOtherPeptide    an other peptide
     * @return a boolean indicating if the other peptide is the same.
     */
    public boolean isSameAs(Peptide anOtherPeptide) {
        return sequence.equals(anOtherPeptide.getSequence());
    }
}
