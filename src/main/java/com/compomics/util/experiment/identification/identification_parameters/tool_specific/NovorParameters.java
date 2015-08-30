package com.compomics.util.experiment.identification.identification_parameters.tool_specific;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;

/**
 * Novor specific parameters. (Not yet used...)
 *
 * @author Harald Barsnes
 */
public class NovorParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    //static final long serialVersionUID = 7525455518683797145L; // @TODO: update!

    /**
     * Constructor.
     */
    public NovorParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.novor;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof NovorParameters) {
            NovorParameters pNovoParameters = (NovorParameters) identificationAlgorithmParameter;

            // @TODO: implement me!
        }

        return true;
    }

    @Override
    public String toString(boolean html) {

        String newLine = System.getProperty("line.separator");

        if (html) {
            newLine = "<br>";
        }

        StringBuilder output = new StringBuilder();
        Advocate advocate = getAlgorithm();
        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append("# ").append(advocate.getName()).append(" Specific Parameters");
        output.append(newLine);
        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append(newLine);

//        output.append("NUMBER_PEPTIDES="); // @TODO: implement me!
//        output.append(numberOfPeptides);
//        output.append(newLine);

        return output.toString();
    }
}
