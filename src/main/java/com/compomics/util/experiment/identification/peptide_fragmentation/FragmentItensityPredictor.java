package com.compomics.util.experiment.identification.peptide_fragmentation;

import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.SequestFragmentationModel;
import com.compomics.util.experiment.identification.peptide_fragmentation.models.UniformFragmentation;

/**
 * Predictor for the peptide fragment intensity.
 *
 * @author Marc Vaudel
 */
public class FragmentItensityPredictor {

    /**
     * Empty default constructor
     */
    public FragmentItensityPredictor() {
        peptideFragmentationModel = null;
    }

    /**
     * The model to use for the fragmentation of peptides.
     */
    private final PeptideFragmentationModel peptideFragmentationModel;

    /**
     * Constructor.
     *
     * @param peptideFragmentationModel the model to use for the fragmentation
     * of peptides
     */
    public FragmentItensityPredictor(PeptideFragmentationModel peptideFragmentationModel) {
        this.peptideFragmentationModel = peptideFragmentationModel;
    }

    /**
     * Returns the intensity expected for the given peak.
     *
     * @param ion the ion of interest
     *
     * @return the expected intensity
     */
    public Double getIntentisy(Ion ion) {
        switch (peptideFragmentationModel) {
            case uniform:
                return UniformFragmentation.getIntensity();
            case sequest:
                return SequestFragmentationModel.getIntensity(ion);
            default:
                throw new UnsupportedOperationException("Fragmentation model " + peptideFragmentationModel + " not supported.");
        }
    }
}
