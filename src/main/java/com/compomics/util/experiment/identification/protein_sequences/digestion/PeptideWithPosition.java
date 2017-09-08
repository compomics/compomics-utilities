package com.compomics.util.experiment.identification.protein_sequences.digestion;

import com.compomics.util.experiment.biology.proteins.Peptide;

/**
 * This class packages together a peptide and its position on the protein.
 *
 * @author Marc Vaudel
 */
public class PeptideWithPosition {

    /**
     * The peptide.
     */
    private Peptide peptide;
    /**
     * The position of the peptide on the protein.
     */
    private int position;

    /**
     * Constructor.
     *
     * @param peptide the peptide
     * @param position the position of the peptide on the protein
     */
    public PeptideWithPosition(Peptide peptide, int position) {
        this.peptide = peptide;
        this.position = position;
    }

    /**
     * Returns the peptide.
     *
     * @return the peptide
     */
    public Peptide getPeptide() {
        return peptide;
    }

    /**
     * Returns the position on the protein.
     *
     * @return the position on the protein
     */
    public int getPosition() {
        return position;
    }
}
