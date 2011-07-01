package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.*;

/**
 * This class models a peptide.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:56:40 AM
 */
public class Peptide extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 5632064601627536034L;
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
     * The modifications carried by the peptide
     */
    private ArrayList<ModificationMatch> modifications;

    /**
     * Constructor for the peptide
     */
    public Peptide() {
    }

    /**
     * Constructor for the peptide
     *
     * @param aSequence      The peptide sequence
     * @param mass           The peptide mass
     * @param parentProteins The parent proteins
     * @param modifications  The PTM of this peptide
     */
    public Peptide(String aSequence, Double mass, ArrayList<Protein> parentProteins, ArrayList<ModificationMatch> modifications) {
        this.sequence = aSequence;
        this.mass = mass;
        this.parentProteins = parentProteins;
        this.modifications = modifications;
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
     * getter for the modifications carried by this peptide
     *
     * @return the modifications matches as found by the search engine
     */
    public ArrayList<ModificationMatch> getModificationMatches() {
        return modifications;
    }
    
    /**
     * Clears the list of imported modification matches
     */
    public void clearModificationMAtches() {
        modifications.clear();
    }
    
    /**
     * Adds a modification match
     * @param modificationMatch the modification match to add
     */
    public void addModificationMatch(ModificationMatch modificationMatch) {
        modifications.add(modificationMatch);
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
     * @param fragment a fragment ion of this peptide
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
     * Sets the parent proteins
     * @param parentProteins the parent proteins as list
     */
    public void setParentProteins(ArrayList<Protein> parentProteins) {
        this.parentProteins = parentProteins;
    }

    /**
     * This methods evaluates the number of missed cleavages
     *
     * @return the number of missed cleavages
     */
    public int getNMissedCleavages() {
        int cpt = 0;
        for (int i = 0; i < sequence.length() - 1; i++) {
            if ((sequence.charAt(i) == 'K' || sequence.charAt(i) == 'R') && sequence.charAt(i + 1) != 'P') {
                cpt++;
            }
        }
        return cpt;
    }

    /**
     * Returns the index of a peptide. index = SEQUENCE_mod1_mod2 with modifications ordered alphabetically.
     *
     * @return the index of a peptide
     */
    public String getKey() {
        ArrayList<String> modifications = new ArrayList<String>();
        for (ModificationMatch mod : getModificationMatches()) {
            if (mod.isVariable()) {
                if (mod.getTheoreticPtm() != null) {
                    modifications.add(mod.getTheoreticPtm().getName());
                } else {
                    modifications.add("unknown-modification");
                }
            }
        }
        Collections.sort(modifications);
        String result = sequence;
        for (String mod : modifications) {
            result += "_" + mod;
        }
        return result;
    }

    /**
     * a method which compares to peptides. Two same peptides present the same sequence and same modifications at the same place.
     *
     * @param anOtherPeptide an other peptide
     * @return a boolean indicating if the other peptide is the same.
     */
    public boolean isSameAs(Peptide anOtherPeptide) {
        return getKey().equals(anOtherPeptide.getKey());
    }
}
