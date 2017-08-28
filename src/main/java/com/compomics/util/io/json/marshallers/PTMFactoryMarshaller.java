package com.compomics.util.io.json.marshallers;

import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.io.json.JsonMarshaller;

/**
 * This class is a convenience class to have a DefaultJsonConverter with the
 * search parameter interfaces.
 *
 * @author Kenneth Verheggen
 */
public class PTMFactoryMarshaller extends JsonMarshaller {

    /**
     * Constructor.
     */
    public PTMFactoryMarshaller() {
        super(Atom.class);
    }
}
