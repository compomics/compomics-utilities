package com.compomics.cli.identification_parameters;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.parameters.identification.IdentificationParameters;
import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;

/**
 * This class serves as a base for the implementation of
 * IdentificationParametersCLI in specific tools. To make an
 * IdentificationParametersCLI extend this class and call initiate in the
 * constructor of your class.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public abstract class AbstractIdentificationParametersCli implements Callable {

    /**
     * The parameters input bean containing the command line arguments.
     */
    private IdentificationParametersInputBean input;

    /**
     * Initiates the IdentificationParametersCli.
     *
     * @param args the command line arguments
     */
    public void initiate(String[] args) {
        
        try {

            try {
                SpeciesFactory speciesFactory = SpeciesFactory.getInstance();
                speciesFactory.initiate(getJarFilePath());
            } catch (Exception e) {
                System.out.println("An error occurred while loading the species.");
                e.printStackTrace();
                System.exit(1);
            }

            Options lOptions = new Options();
            createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line;

            try {
                line = parser.parse(lOptions, args);

                // see if the usage option was run
                if (line.getOptions().length == 0 || line.hasOption("h") || line.hasOption("help") || line.hasOption("usage")) {
                    PrintWriter lPrintWriter = new PrintWriter(System.out);
                    lPrintWriter.print(System.getProperty("line.separator") + "============================" + System.getProperty("line.separator"));
                    lPrintWriter.print("IdentificationParametersCLI" + System.getProperty("line.separator"));
                    lPrintWriter.print("============================" + System.getProperty("line.separator"));
                    lPrintWriter.print(AbstractIdentificationParametersCli.getHeader());
                    lPrintWriter.print(getOptionsAsString());
                    lPrintWriter.flush();
                    lPrintWriter.close();
                    System.exit(0);
                }

                // check if the parameters are valid
                if (!IdentificationParametersInputBean.isValidStartup(line, true)) {
                    System.out.println(System.getProperty("line.separator") + "Run -usage to see the list of supported options and their input.");
                    System.exit(1);
                }
                if (!IdentificationParametersInputBean.isValidModifications(line)) {
                    printModifications();
                    System.exit(1);
                } else {
                    input = new IdentificationParametersInputBean(line);
                    call();
                }
            } catch (UnrecognizedOptionException e) {
                System.out.println(System.getProperty("line.separator") + "Unrecognized option " + e.getOption() + ".");
                System.out.println(System.getProperty("line.separator") + "Run -usage to see the list of supported options and their input.");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public Object call() {

        try {
            if (input.isListMods() || input.isListEnzymes()) {
                if (input.isListMods()) {
                    printModifications();
                }
                if (input.isListEnzymes()) {
                    printEnzymes();
                }
            } else {
                File outputFile = input.getDestinationFile();
                IdentificationParameters identificationParameters = input.getIdentificationParameters();
                IdentificationParameters.saveIdentificationParameters(identificationParameters, outputFile);
                System.out.println(System.getProperty("line.separator") + "Identification parameters file created: " + outputFile.getAbsolutePath() + System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    /**
     * Prints the available modifications.
     */
    public void printModifications() {

        System.out.println(System.getProperty("line.separator")
                + "========================" + System.getProperty("line.separator")
                + "Available Modifications:" + System.getProperty("line.separator")
                + "========================"
                + System.getProperty("line.separator"));
        System.out.println("----------------------");
        System.out.println("Default Modifications:");
        System.out.println("----------------------");
        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        for (String ptmName : ptmFactory.getDefaultModificationsOrdered()) {
            Modification ptm = ptmFactory.getModification(ptmName);
            System.out.println(ptm);
        }
        System.out.println();
        if (!ptmFactory.getUserModifications().isEmpty()) {
            System.out.println("-------------------");
            System.out.println("User Modifications:");
            System.out.println("-------------------");
            for (String ptmName : ptmFactory.getUserModificationsOrdered()) {
                Modification ptm = ptmFactory.getModification(ptmName);
                System.out.println(ptm);
            }
        }
        System.out.println();
    }

    /**
     * Prints the available enzymes.
     */
    public void printEnzymes() {

        System.out.println(System.getProperty("line.separator")
                + "========================" + System.getProperty("line.separator")
                + "Available Enzymes:" + System.getProperty("line.separator")
                + "========================"
                + System.getProperty("line.separator"));
        for (Enzyme enzyme : EnzymeFactory.getInstance().getEnzymes()) {
            System.out.println(enzyme.getDescription());
        }
        System.out.println();
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    protected abstract String getJarFilePath();

    /**
     * Provides the options left to the user.
     *
     * @param options the options object where the options will be added
     */
    protected abstract void createOptionsCLI(Options options);

    /**
     * Returns the options left to the user as a string.
     *
     * @return the options left to the user as a string
     */
    protected abstract String getOptionsAsString();

    /**
     * IdentificationParametersCLI header message when printing the usage.
     *
     * @return the header message as a string
     */
    public static String getHeader() {
        return System.getProperty("line.separator")
                + "IdentificationParametersCLI creates an identification parameters file." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Use the out option to specify the output file or the mods option to list the available modifications." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help see https://compomics.github.io/projects/compomics-utilities/wiki/identificationparameterscli.html." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Or contact the developers at https://compomics.github.io/projects/compomics-utilities.html." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator") + System.getProperty("line.separator");
    }
}
