package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:56:40 AM
 * This class modelizes a peptide.
 */
public class Peptide {

    // Attributes

    private String sequence;
    private Double mass;
    private HashSet<PeptideFragmentIon> theoreticPeptideFragmentIons = new HashSet();
    private ArrayList<Protein> parentProteins;


    // Constructors

    public Peptide() {
    }

    public Peptide(String aSequence, Double mass, ArrayList<Protein> parentProteins) {
        this.sequence = aSequence;
        this.mass = mass;
        this.parentProteins = parentProteins;
    }

    // Methods

    public Double getMass() {
        return mass;
    }

    public String getSequence() {
        return sequence;
    }

    public void addFragmentIon(PeptideFragmentIon fragment) {
        theoreticPeptideFragmentIons.add(fragment);
    }

    public Iterator<PeptideFragmentIon> fragmentIterator() {
        return theoreticPeptideFragmentIons.iterator();
    }

    public ArrayList<Protein> getParentProteins() {
        return parentProteins;
    }

    public boolean isSameAs(Peptide anOtherPeptide) {
        return sequence.equals(anOtherPeptide.getSequence());
    }
}
