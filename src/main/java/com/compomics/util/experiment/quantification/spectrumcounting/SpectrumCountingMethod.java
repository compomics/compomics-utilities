package com.compomics.util.experiment.quantification.spectrumcounting;

/**
 * Enum of the implemented spectrum quantification methods.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum SpectrumCountingMethod { // @TODO: rename to QuantificationMethods

    /**
     * Exponentially modified protein abundance index (emPAI).
     */
    EMPAI,
    /**
     * Normalized Spectral Abundance Factor (NSAF).
     */
    NSAF,
    /**
     * Label-free quantification.
     */
    LFQ;

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

            case LFQ:
                return "LFQ";

            default:
                throw new UnsupportedOperationException(
                        "Spectrum quantification method "
                        + this.name()
                        + " not implemented."
                );

        }

    }
}
