package com.compomics.util.experiment.identification.parameters_cli;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Command line to manage the modifications.
 *
 * @author Marc Vaudel
 */
public class ModificationsCLI {

    /**
     * The parsed command line input.
     */
    private ModificationsCLIInputBean modificationsCLIInputBean;

    /**
     * Constructor.
     *
     * @param modificationsCLIInputBean the parsed command line input.
     */
    public ModificationsCLI(ModificationsCLIInputBean modificationsCLIInputBean) {
        this.modificationsCLIInputBean = modificationsCLIInputBean;
    }

    /**
     * Header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "The ModificationsCLI command line allows the command line management of modifications. It can be used to create and edit json files containing modifications compatible with CompOmics tools." + System.getProperty("line.separator")
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
     * Main method for the ModificationsCLI.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            Options lOptions = new Options();
            ModificationsCLIParams.createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line = parser.parse(lOptions, args);

            if (!ModificationsCLIInputBean.isValidStartup(line)) {
                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "========================================" + System.getProperty("line.separator"));
                lPrintWriter.print("Compomics Modifications - Command Line" + System.getProperty("line.separator"));
                lPrintWriter.print("========================================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(ModificationsCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);
            } else {
                ModificationsCLIInputBean inputBean = new ModificationsCLIInputBean(line);
                ModificationsCLI cli = new ModificationsCLI(inputBean);
                cli.call();
            }
        } catch (OutOfMemoryError e) {
            System.out.println("<CompomicsError>ModificationsCLI used up all the memory and had to be stopped.</CompomicsError>");
            System.err.println("Ran out of memory!");
            System.err.println("Memory given to the Java virtual machine: " + Runtime.getRuntime().maxMemory() + ".");
            System.err.println("Memory used by the Java virtual machine: " + Runtime.getRuntime().totalMemory() + ".");
            System.err.println("Free memory in the Java virtual machine: " + Runtime.getRuntime().freeMemory() + ".");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.print("<CompomicsError>ModificationsCLI processing failed.</CompomicsError>");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ModificationsCLI{"
                + ", cliInputBean=" + modificationsCLIInputBean
                + '}';
    }

    /**
     * Calling this method will run the process.
     *
     * @return returns 1 if the process was canceled or an error was encountered
     */
    public Object call() {

        PTMFactory ptmFactory = null;
        File inputFile = modificationsCLIInputBean.getFileIn();
        if (inputFile != null) {
            try {
                ptmFactory = PTMFactory.loadFromFile(inputFile);
            } catch (IOException e) {
                System.out.println("An error occurred while reading modifications from " + inputFile + ".");
                return 1;
            }
        } else {
            ptmFactory = PTMFactory.getInstance();
        }

        if (modificationsCLIInputBean.isList()) {
            for (String ptmName : ptmFactory.getPTMs()) {
                PTM ptm = ptmFactory.getPTM(ptmName);
                System.out.println(ptm);
                System.out.println();
            }
            return 0;
        }

        String modificationToRemove = modificationsCLIInputBean.getModificationToRemove();
        if (modificationToRemove != null) {
            ptmFactory.removeUserPtm(modificationToRemove);
        }

        PTM modificationToAdd = modificationsCLIInputBean.getModificationToAdd();
        if (modificationToAdd != null) {
            ptmFactory.addUserPTM(modificationToAdd);
        }

        File outputFile = modificationsCLIInputBean.getFileOut();
        if (outputFile != null) {
            try {
                PTMFactory.saveToFile(ptmFactory, outputFile);
            } catch (IOException e) {
                System.out.println("An error occurred while saving the modifications to " + outputFile + ".");
                return 1;
            }
        }
        return 0;
    }
}
