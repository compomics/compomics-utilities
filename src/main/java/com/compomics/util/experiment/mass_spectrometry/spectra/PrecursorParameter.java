package com.compomics.util.experiment.mass_spectrometry.spectra;

import com.compomics.util.experiment.personalization.UrParameter;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Parameter for storing additional non-mandatory precursor information.
 *
 * @author Harald Barsnes
 */
public class PrecursorParameter implements UrParameter, Serializable {

    /**
     * Serial version UID for post-serialization compatibility.
     */
    static final long serialVersionUID = -2767016431482105597L;
    /**
     * The precusor identifier.
     */
    private ArrayList<String> precusorIdentifiers;
    /**
     * An empty parameter used for instantiation.
     */
    public static final PrecursorParameter dummy = new PrecursorParameter();

    /**
     * Constructor.
     */
    public PrecursorParameter() {
    }
    
    /**
     * Constructor.
     *
     * @param precusorIdentifiers the precursor identifiers
     */
    public PrecursorParameter(ArrayList<String> precusorIdentifiers) {
        this.precusorIdentifiers = precusorIdentifiers;
    }

    @Override
    public long getParameterKey() {
        return serialVersionUID;
    }

    /**
     * Returns the precursor identifiers.
     *
     * @return the precusorIdentifiers
     */
    public ArrayList<String> getPrecusorIdentifiers() {
        return precusorIdentifiers;
    }

    /**
     * Set the precursor identifier.
     *
     * @param precusorIdentifiers the precusorIdentifiers to set
     */
    public void setPrecusorIdentifiers(ArrayList<String> precusorIdentifiers) {
        this.precusorIdentifiers = precusorIdentifiers;
    }

}
