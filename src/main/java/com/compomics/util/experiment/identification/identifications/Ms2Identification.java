package com.compomics.util.experiment.identification.identifications;

import com.compomics.util.db.object.ObjectsDB;
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
     * @param objectsDB the objects database
     */
    public Ms2Identification(String reference, ObjectsDB objectsDB) {
        super(objectsDB);
        this.reference = reference;
        this.methodUsed = IdentificationMethod.MS2_IDENTIFICATION;
    }
}
