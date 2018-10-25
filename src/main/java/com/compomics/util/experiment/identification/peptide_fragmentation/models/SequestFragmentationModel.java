package com.compomics.util.experiment.identification.peptide_fragmentation.models;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;

/**
 * Fragmentation model originally described in the Sequest algorithm
 * (https://www.ncbi.nlm.nih.gov/pubmed/24226387) adapted to utilities objects.
 *
 * @author Marc Vaudel
 */
public class SequestFragmentationModel {

    /**
     * Empty default constructor
     */
    public SequestFragmentationModel() {
    }

    /**
     * Returns the intensity expected for the given ion. 50.0 for b and y ions
     * with no neutral losses. 25.0 for a ions and b and y ions with neutral
     * losses. 0.0 for other peaks.
     *
     * @param ion the ion of interest
     *
     * @return the expected intensity
     */
    public static Double getIntensity(Ion ion) {
        if (ion instanceof PeptideFragmentIon) {
            if (ion.getSubType() == PeptideFragmentIon.B_ION || ion.getSubType() == PeptideFragmentIon.Y_ION) {
                if (!ion.hasNeutralLosses() || ion.getNeutralLosses().length == 0) {
                    return 50.0;
                } else {
                    return 25.0;
                }
            } else if (ion.getSubType() == PeptideFragmentIon.A_ION) {
                return 25.0;
            }
        }
        return 0.0;
    }
}
