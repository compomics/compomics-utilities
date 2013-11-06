package com.compomics.util.experiment.identification.identifications;

import com.compomics.util.experiment.identification.IdentificationMethod;
import com.compomics.util.experiment.identification.Identification;

/**
 * This class models an MS2 Identification.
 *
 * @author Marc Vaudel
 */
public class Ms2Identification extends Identification {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -7242302146506873391L;

    /**
     * Constructor for MS2 identification.
     * 
     * @param reference the reference
     */
    public Ms2Identification(String reference) {
        this.reference = reference;
        this.methodUsed = IdentificationMethod.MS2_IDENTIFICATION;
    }
}
