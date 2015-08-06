package com.compomics.util.experiment.identification.search_parameters_cli;

import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
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
 */
public abstract class AbstractIdentificationParametersCli implements Callable {

    /**
     * The parameters input bean containing the command line arguments.
     */
    private IdentificationParametersInputBean input;
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();

    /**
     * Initiates the IdentificationParametersCli.
     *
     * @param args the command line arguments
     */
    public void initiate(String[] args) {

        try {
            try {
                enzymeFactory.importEnzymes(getEnzymeFile());
            } catch (Exception e) {
                System.out.println("An error occurred while loading the enzymes.");
                e.printStackTrace();
            }

            Options lOptions = new Options();
            createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line;

            try {
                line = parser.parse(lOptions, args);

                // see if the usage option was run
                if (line.hasOption("h") || line.hasOption("help") || line.hasOption("usage")) {
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
                if (!IdentificationParametersInputBean.isValidStartup(line)) {
                    System.out.println(System.getProperty("line.separator") + "Run -usage to see the list of supported options and their input.");
                    System.exit(0);
                }
                if (!IdentificationParametersInputBean.isValidModifications(line)) {
                    printModifications();
                    System.exit(0);
                } else {
                    input = new IdentificationParametersInputBean(line);
                    call();
                }
            } catch (UnrecognizedOptionException e) {
                System.out.println(System.getProperty("line.separator") + "Unrecognized option " + e.getOption() + ".");
                System.out.println(System.getProperty("line.separator") + "Run -usage to see the list of supported options and their input.");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calling this method will run the configured SearchCLI process.
     */
    public Object call() {

        try {
            if (input.isListMods()) {
                printModifications();
            } else {
                File outputFile = input.getDestinationFile();
                SearchParameters searchParameters = input.getSearchParameters();
                SearchParameters.saveIdentificationParameters(searchParameters, outputFile);
                System.out.println(System.getProperty("line.separator") + "Identification parameters file created: " + outputFile.getAbsolutePath() + System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Prints the available modifications on the screen.
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
        for (String ptmName : ptmFactory.getDefaultModificationsOrdered()) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            System.out.println(getPtmLine(ptm));
        }
        System.out.println();
        System.out.println("-------------------");
        System.out.println("User Modifications:");
        System.out.println("-------------------");
        for (String ptmName : ptmFactory.getUserModificationsOrdered()) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            System.out.println(getPtmLine(ptm));
        }
        System.out.println();
    }

    /**
     * Returns the description line for a PTM.
     *
     * @param ptm the PTM to display
     *
     * @return the description line for a PTM
     */
    private String getPtmLine(PTM ptm) {

        String target = "";
        switch (ptm.getType()) {
            case PTM.MODAA:
                target = ptm.getPattern().toString();
                break;
            case PTM.MODC:
                target = "Protein C-terminus";
                break;
            case PTM.MODCAA:
                target = "Protein C-terminus ending with " + ptm.getPattern().toString();
                break;
            case PTM.MODCP:
                target = "Peptide C-terminus";
                break;
            case PTM.MODCPAA:
                target = "Peptide C-terminus ending with " + ptm.getPattern().toString();
                break;
            case PTM.MODN:
                target = "Protein N-terminus";
                break;
            case PTM.MODNAA:
                target = "Protein N-terminus starting with " + ptm.getPattern().toString();
                break;
            case PTM.MODNP:
                target = "Peptide N-terminus";
                break;
            case PTM.MODNPAA:
                target = "Peptide N-terminus starting with " + ptm.getPattern().toString();
                break;
        }

        double ptmMass = ptm.getRoundedMass();
        String sign = "";
        if (ptmMass > 0) {
            sign = "+";
        }

        return ptm.getName() + " (" + sign + ptmMass + " targeting " + target + ")";
    }

    /**
     * Returns the enzyme file needed to initiate the factory.
     *
     * @return the enzyme file needed to initiate the factory
     */
    protected abstract File getEnzymeFile();

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
                + "For further help see http://code.google.com/p/searchgui/wiki/IdentificationParametersCLI." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Or contact the developers at https://groups.google.com/group/peptide-shaker." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator") + System.getProperty("line.separator");
    }
}
