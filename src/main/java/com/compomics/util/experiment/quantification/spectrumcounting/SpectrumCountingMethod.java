package com.compomics.util.experiment.quantification.spectrumcounting;

/**
 * Enum of the implemented spectrum counting methods.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum SpectrumCountingMethod {
    
        EMPAI, NSAF;


    /**
     * Empty default constructor.
     */
    private SpectrumCountingMethod() {
    }
    
    @Override
    public String toString() {

        switch (this) {
            case EMPAI:
                return "emPAI";
            case NSAF:
                return "NSAF+";
            default:
                throw new UnsupportedOperationException(
                        "Spectrum counting method " + this.name() + " not implemented.");
        }

    }
}
