package com.compomics.util.experiment.identification.parameters_cli;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Command line to manage the enzymes.
 *
 * @author Marc Vaudel
 */
public class EnzymeCLI {

    private EnzymesCLIInputBean enzymesCLIInputBean;
    
    public EnzymeCLI(EnzymesCLIInputBean enzymesCLIInputBean) {
        this.enzymesCLIInputBean = enzymesCLIInputBean;
    }

    /**
     * Header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "The EnzymeCLI command line allows the command line management of enzymes. It can be used to create and edit json files containing enzymes compatible with CompOmics tools." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
//                + "For further help see http://compomics.github.io/projects/peptide-shaker.html and http://compomics.github.io/peptide-shaker/wiki/peptideshakercli.html." + System.getProperty("line.separator")
//                + System.getProperty("line.separator")
//                + "Or contact the developers at https://groups.google.com/group/peptide-shaker." + System.getProperty("line.separator")
//                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator")
                + System.getProperty("line.separator");
    }
    
    /**
     * Main method for the EnzymeCLI.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            Options lOptions = new Options();
            EnzymesCLIParams.createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line = parser.parse(lOptions, args);

            if (!EnzymesCLIInputBean.isValidStartup(line)) {
                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "========================================" + System.getProperty("line.separator"));
                lPrintWriter.print("PeptideShaker Follow Up - Command Line" + System.getProperty("line.separator"));
                lPrintWriter.print("========================================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(EnzymesCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);
            } else {
                EnzymesCLIInputBean inputBean = new EnzymesCLIInputBean(line);
                EnzymeCLI cli = new EnzymeCLI(inputBean);
                cli.call();
            }
        } catch (OutOfMemoryError e) {
            System.out.println("<CompomicsError>EnzymeCLI used up all the memory and had to be stopped.</CompomicsError>");
            System.err.println("Ran out of memory!");
            System.err.println("Memory given to the Java virtual machine: " + Runtime.getRuntime().maxMemory() + ".");
            System.err.println("Memory used by the Java virtual machine: " + Runtime.getRuntime().totalMemory() + ".");
            System.err.println("Free memory in the Java virtual machine: " + Runtime.getRuntime().freeMemory() + ".");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.print("<CompomicsError>EnzymeCLI processing failed.</CompomicsError>");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "FollowUpCLI{"
                + ", cliInputBean=" + enzymesCLIInputBean
                + '}';
    }

    /**
     * Calling this method will run the process.
     *
     * @return returns 1 if the process was canceled or an error was encountered
     */
    public Object call() {
        
        EnzymeFactory enzymeFactory;
        File inputFile = enzymesCLIInputBean.getFileIn();
        if (inputFile != null) {
            enzymeFactory = EnzymeFactory.getInstance(inputFile);
        } else {
            enzymeFactory = EnzymeFactory.getInstance();
        }
        
        if (enzymesCLIInputBean.isList()) {
            for (Enzyme enzyme : enzymeFactory.getEnzymes()) {
                System.out.println(enzyme.getName() + ":");
                System.out.println(enzyme.getDescription());
                System.out.println();
            }
            return 0;
        }
        
        String enzymeToRemove = enzymesCLIInputBean.getEnzymeToRemove();
        if (enzymeToRemove != null) {
            enzymeFactory.removeEnzyme(enzymeToRemove);
        }
        
        Enzyme enzymeToAdd = enzymesCLIInputBean.getEnzymeToAdd();
        if (enzymeToAdd != null) {
            enzymeFactory.addEnzyme(enzymeToAdd);
        }
        
        File outputFile = enzymesCLIInputBean.getFileOut();
        if (outputFile != null) {
            try {
            EnzymeFactory.saveToFile(enzymeFactory, outputFile);
            } catch (IOException e) {
                System.out.println("An error occurred while saving the enzymes to " + outputFile + ".");
                return 1;
            }
        }
        return 0;
    }
}
