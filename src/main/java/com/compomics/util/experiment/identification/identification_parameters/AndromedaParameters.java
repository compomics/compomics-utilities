package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationAlgorithmParameter;

/**
 * The Andromeda specific parameters.
 *
 * @author Harald Barsnes
 */
public class AndromedaParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
    //static final long serialVersionUID = -2996752557726296967L; // @TODO: update me!
    /**
     * Constructor.
     */
    public AndromedaParameters() {
    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.andromeda;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        if (identificationAlgorithmParameter instanceof AndromedaParameters) {
            AndromedaParameters andromedaParameters = (AndromedaParameters) identificationAlgorithmParameter;

            // @TODO: implement me!
            return true;
        }

        return false;
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

//        output.append("NUMBER_SPECTRUM_MATCHES="); // @TODO: implement me!
//        output.append(numberOfSpectrumMatches);
//        output.append(newLine);

        return output.toString();
    }
}
