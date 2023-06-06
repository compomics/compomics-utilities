package com.compomics.util.experiment.identification.spectrum_assumptions;

import com.compomics.util.experiment.personalization.UrParameter;
import java.io.Serializable;

/**
 * Parameter for storing additional non-mandatory peptide assumption
 * information.
 *
 * @author Harald Barsens
 */
public class PeptideAssumptionParameter implements UrParameter, Serializable {

    /**
     * Serial version UID for post-serialization compatibility.
     */
    static final long serialVersionUID = -2767016431482105597L;
    /**
     * The MS1 intensity.
     */
    private Double ms1Intensity = null;
    /**
     * An empty parameter used for instantiation.
     */
    public static final PeptideAssumptionParameter dummy = new PeptideAssumptionParameter();

    /**
     * Constructor.
     */
    public PeptideAssumptionParameter() {
    }

    /**
     * Constructor.
     *
     * @param ms1Intensity the MS1 intensity
     */
    public PeptideAssumptionParameter(Double ms1Intensity) {
        this.ms1Intensity = ms1Intensity;
    }

    @Override
    public long getParameterKey() {
        return serialVersionUID;
    }

    /**
     * Returns the MS1 intensity.
     *
     * @return the MS1 intensity
     */
    public Double getMs1Intensity() {
        return ms1Intensity;
    }

    /**
     * Set the MS1 intensity.
     *
     * @param ms1Intensity the MS1 intensity
     */
    public void setMs1Intensity(Double ms1Intensity) {
        this.ms1Intensity = ms1Intensity;
    }

}
