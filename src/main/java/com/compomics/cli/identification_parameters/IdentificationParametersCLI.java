package com.compomics.cli.identification_parameters;

import com.compomics.software.CompomicsWrapper;
import org.apache.commons.cli.Options;

/**
 * The SearchParametersCLI allows creating search parameters files using command
 * line arguments.
 *
 * @author Marc Vaudel
 */
public class IdentificationParametersCLI extends AbstractIdentificationParametersCli {

    /**
     * Empty default constructor
     */
    public IdentificationParametersCLI() {
    }

    /**
     * Construct a new SearchParametersCLI runnable from a list of arguments.
     * When initialization is successful, calling "run" will write the created
     * parameters file.
     *
     * @param args the command line arguments
     */
    public IdentificationParametersCLI(String[] args) {
        initiate(args);
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new IdentificationParametersCLI(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void createOptionsCLI(Options options) {
        IdentificationParametersCLIParams.createOptionsCLI(options);
    }

    @Override
    protected String getOptionsAsString() {
        return IdentificationParametersCLIParams.getOptionsAsString();
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("IdentificationParametersCLI.class").getPath(), "compomics-utilities");
    }
}
