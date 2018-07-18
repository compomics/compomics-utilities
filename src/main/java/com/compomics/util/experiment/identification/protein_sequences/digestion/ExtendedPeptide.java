package com.compomics.util.experiment.identification.protein_sequences.digestion;

import com.compomics.util.experiment.biology.proteins.Peptide;

/**
 * This class packages together a peptide, its fixed modifications and its position on the protein.
 *
 * @author Marc Vaudel
 */
public class ExtendedPeptide {

    /**
     * The peptide.
     */
    public final Peptide peptide;
    /**
     * The position of the peptide on the protein.
     */
    public final int position;
    /**
     * The fixed modifications of the peptide.
     */
    public final String[] fixedModifications;

    /**
     * Constructor.
     *
     * @param peptide the peptide
     * @param position the position of the peptide on the protein
     * @param fixedModifications the fixed modifications on the peptide
     */
    public ExtendedPeptide(Peptide peptide, int position, String[] fixedModifications) {
        this.peptide = peptide;
        this.position = position;
        this.fixedModifications = fixedModifications;
    }
}
