package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.IdentificationAlgorithmParameter;

/**
 * The MS-GF+ specific parameters.
 *
 * @author Harald Barsnes
 */
public class MsgfParameters implements IdentificationAlgorithmParameter {

    /**
     * Version number for deserialization.
     */
//    static final long serialVersionUID = -5898951075262732261L; // @TODO: set version number!!!

    /**
     * Constructor.
     */
    public MsgfParameters() {

    }

    @Override
    public Advocate getAlgorithm() {
        return Advocate.MSGF;
    }

    @Override
    public boolean equals(IdentificationAlgorithmParameter identificationAlgorithmParameter) {

        // @TODO: implement me!!!
        return true;

//        if (identificationAlgorithmParameter instanceof MsgfParameters) {
//            MsgfParameters xtandemParameters = (MsgfParameters) identificationAlgorithmParameter;
//            if (!getMaxEValue().equals(xtandemParameters.getMaxEValue())) {
//                return false;
//            }
//            return true;
//        }
//        return false;
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

//        output.append("DYNAMIC_RANGE=");
//        output.append(dynamicRange);
//        output.append(newLine);

        // @TODO: implement me!!!

        return output.toString();
    }
}
