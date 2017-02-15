package com.compomics.util.experiment.identification.protein_sequences.digestion;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.general.BoxedObject;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The iterator goes through a sequence and lists possible peptides with their
 * fixed modifications.
 *
 * @author Marc Vaudel
 */
public class IteratorGenerator {

    /**
     * Ute utils used during digestion.
     */
    private ProteinIteratorUtils proteinIteratorUtils;

    /**
     * Constructor.
     *
     * @param fixedModifications a list of fixed modifications to consider when
     * iterating the protein sequences.
     * @param maxX The maximal number of Xs allowed in a sequence to derive the
     * possible peptides
     */
    public IteratorGenerator(ArrayList<String> fixedModifications, Integer maxX) {
        this.proteinIteratorUtils = new ProteinIteratorUtils(fixedModifications, maxX);
    }

    /**
     * Constructor with 2 Xs allowed.
     *
     * @param fixedModifications a list of fixed modifications to consider when
     * iterating the protein sequences.
     */
    public IteratorGenerator(ArrayList<String> fixedModifications) {
        this(fixedModifications, null);
    }

    /**
     * Returns the peptides for a given sequence. The peptides can be filtered
     * by minimal and maximal mass. If null is provided as limit no filter will
     * be used.
     *
     * @param sequence the sequence to iterate
     * @param digestionPreferences the digestion preferences to use
     * @param massMin the minimal mass of a peptide
     * @param massMax the maximal mass of a peptide
     *
     * @return the possible peptides for the sequence
     */
    public ArrayList<PeptideWithPosition> getPeptides(String sequence, DigestionPreferences digestionPreferences, Double massMin, Double massMax) {
        switch (digestionPreferences.getCleavagePreference()) {
            case unSpecific:
                return getPeptides(sequence, massMin, massMax);
            case wholeProtein:
                return getPeptidesNoDigestion(sequence, massMin, massMax);
            case enzyme:
                return getPeptidesDigestion(sequence, digestionPreferences, massMin, massMax);
            default:
                throw new UnsupportedOperationException("Cleavage preference of type " + digestionPreferences.getCleavagePreference() + " not supported.");
        }
    }

}
