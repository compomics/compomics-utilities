package com.compomics.util.io.json.marshallers;

import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.filtering.Filter;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.io.json.JsonMarshaller;

/**
 * This class is a convenience class to have a DefaultJsonConverter with the
 * identification parameters interfaces.
 *
 * @author Kenneth Verheggen
 * @author Marc Vaudel
 */
public class IdentificationParametersMarshaller extends JsonMarshaller {

    /**
     * Constructor.
     */
    public IdentificationParametersMarshaller() {
        super(IdentificationAlgorithmParameter.class, Atom.class, Filter.class);
    }
}
