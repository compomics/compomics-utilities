package com.compomics.util.experiment.identification.parameters_cli;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.software.cli.CommandParameter;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;

/**
 * This class parses the parameters from an EnzymeCLI.
 *
 * @author Marc Vaudel
 */
public class EnzymesCLIInputBean {

    /**
     * Verifies the command line start parameters.
     *
     * @param aLine the command line to validate
     *
     * @return true if the startup was valid
     */
    public static boolean isValidStartup(CommandLine aLine) {
        if (aLine.getOptions().length == 0) {
            return false;
        }
        EnzymeFactory enzymeFactory = null;
        if (aLine.hasOption(EnzymesCLIParams.IN.id)) {
            String arg = aLine.getOptionValue(EnzymesCLIParams.IN.id);
            if (arg.equals("")) {
                System.out.println(System.getProperty("line.separator") + "No input file specified!" + System.getProperty("line.separator"));
                return false;
            }
            File fileIn = new File(arg);
            if (!fileIn.exists()) {
                System.out.println(System.getProperty("line.separator") + "File " + fileIn + " not found." + System.getProperty("line.separator"));
                return false;
            }
            try {
                enzymeFactory = EnzymeFactory.loadFromFile(fileIn);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing " + fileIn + "." + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(EnzymesCLIParams.LIST.id)) {
            return true;
        }
        if (aLine.hasOption(EnzymesCLIParams.RM.id)) {
            String arg = aLine.getOptionValue(EnzymesCLIParams.RM.id);
            Enzyme enzyme = enzymeFactory.getEnzyme(arg);
            if (enzyme == null) {
                String file = aLine.getOptionValue(EnzymesCLIParams.IN.id);
                System.out.println(System.getProperty("line.separator") + "Enzyme " + arg + " not found in " + file + "." + System.getProperty("line.separator"));
                return false;
            }
        }
        boolean name = aLine.hasOption(EnzymesCLIParams.NAME.id);
        boolean attributes = false;
        char[] aminoAcids = AminoAcid.getUniqueAminoAcids();
        ArrayList<String> possibilities = new ArrayList<String>(aminoAcids.length);
        for (char aa : aminoAcids) {
            possibilities.add(aa + "");
        }
        if (aLine.hasOption(EnzymesCLIParams.RESTRICTION_BEFORE.id)) {
            if (!name) {
                System.out.println(System.getProperty("line.separator") + "No name provided for the enzyme to add." + System.getProperty("line.separator"));
                return false;
            }
            String arg = aLine.getOptionValue(EnzymesCLIParams.RESTRICTION_BEFORE.id);
            ArrayList<String> aaInput = CommandLineUtils.splitInput(arg);
            for (String aa : aaInput) {
                if (!CommandParameter.isInList(EnzymesCLIParams.RESTRICTION_BEFORE.id, arg, aaInput)) {
                    return false;
                }
            }
            attributes = true;
        }
        if (aLine.hasOption(EnzymesCLIParams.RESTRICTION_AFTER.id)) {
            if (!name) {
                System.out.println(System.getProperty("line.separator") + "No name provided for the enzyme to add." + System.getProperty("line.separator"));
                return false;
            }
            String arg = aLine.getOptionValue(EnzymesCLIParams.RESTRICTION_AFTER.id);
            ArrayList<String> aaInput = CommandLineUtils.splitInput(arg);
            for (String aa : aaInput) {
                if (!CommandParameter.isInList(EnzymesCLIParams.RESTRICTION_AFTER.id, arg, aaInput)) {
                    return false;
                }
            }
            attributes = true;
        }
        if (aLine.hasOption(EnzymesCLIParams.CLEAVE_BEFORE.id)) {
            if (!name) {
                System.out.println(System.getProperty("line.separator") + "No name provided for the enzyme to add." + System.getProperty("line.separator"));
                return false;
            }
            String arg = aLine.getOptionValue(EnzymesCLIParams.CLEAVE_BEFORE.id);
            ArrayList<String> aaInput = CommandLineUtils.splitInput(arg);
            for (String aa : aaInput) {
                if (!CommandParameter.isInList(EnzymesCLIParams.CLEAVE_BEFORE.id, arg, aaInput)) {
                    return false;
                }
            }
            attributes = true;
        }
        if (aLine.hasOption(EnzymesCLIParams.CLEAVE_AFTER.id)) {
            if (!name) {
                System.out.println(System.getProperty("line.separator") + "No name provided for the enzyme to add." + System.getProperty("line.separator"));
                return false;
            }
            String arg = aLine.getOptionValue(EnzymesCLIParams.CLEAVE_AFTER.id);
            ArrayList<String> aaInput = CommandLineUtils.splitInput(arg);
            for (String aa : aaInput) {
                if (!CommandParameter.isInList(EnzymesCLIParams.CLEAVE_AFTER.id, arg, aaInput)) {
                    return false;
                }
            }
            attributes = true;
        }
        if (name && !attributes) {
            System.out.println(System.getProperty("line.separator") + "No cleavage properties provided for the enzyme to add." + System.getProperty("line.separator"));
            return false;
        }
        return true;
    }

    /**
     * The file provided as input.
     */
    private File fileIn = null;

    /**
     * The file provided as output.
     */
    private File fileOut = null;

    /**
     * Boolean indicating whether the command should print the implemented
     * enzymes.
     */
    private boolean list = false;

    /**
     * The name of the enzyme to remove.
     */
    private String enzymeToRemove = null;

    /**
     * The enzyme to add.
     */
    private Enzyme enzymeToAdd = null;

    /**
     * Parses all the arguments from a command line.
     *
     * @param aLine the command line
     *
     * @throws IOException if an error occurs while reading or writing a file.
     */
    public EnzymesCLIInputBean(CommandLine aLine) throws IOException {

        if (aLine.hasOption(EnzymesCLIParams.IN.id)) {
            String arg = aLine.getOptionValue(EnzymesCLIParams.IN.id);
            fileIn = new File(arg);
        }

        if (aLine.hasOption(EnzymesCLIParams.LIST.id)) {
            list = true;
            return;
        }

        if (aLine.hasOption(EnzymesCLIParams.OUT.id)) {
            String arg = aLine.getOptionValue(EnzymesCLIParams.OUT.id);
            fileOut = new File(arg);
        }

        if (aLine.hasOption(EnzymesCLIParams.RM.id)) {
            enzymeToRemove = aLine.getOptionValue(EnzymesCLIParams.RM.id);
        }

        if (aLine.hasOption(EnzymesCLIParams.NAME.id)) {
            String enzymeName = aLine.getOptionValue(EnzymesCLIParams.NAME.id);
            enzymeToAdd = new Enzyme(enzymeName);
            if (aLine.hasOption(EnzymesCLIParams.RESTRICTION_BEFORE.id)) {
                String arg = aLine.getOptionValue(EnzymesCLIParams.RESTRICTION_BEFORE.id);
                ArrayList<String> aaInput = CommandLineUtils.splitInput(arg);
                for (String aa : aaInput) {
                    enzymeToAdd.addRestrictionBefore(aa.charAt(0));
                }
            }
            if (aLine.hasOption(EnzymesCLIParams.RESTRICTION_AFTER.id)) {
                String arg = aLine.getOptionValue(EnzymesCLIParams.RESTRICTION_AFTER.id);
                ArrayList<String> aaInput = CommandLineUtils.splitInput(arg);
                for (String aa : aaInput) {
                    enzymeToAdd.addRestrictionAfter(aa.charAt(0));
                }
            }
            if (aLine.hasOption(EnzymesCLIParams.CLEAVE_BEFORE.id)) {
                String arg = aLine.getOptionValue(EnzymesCLIParams.CLEAVE_BEFORE.id);
                ArrayList<String> aaInput = CommandLineUtils.splitInput(arg);
                for (String aa : aaInput) {
                    enzymeToAdd.addAminoAcidBefore(aa.charAt(0));
                }
            }
            if (aLine.hasOption(EnzymesCLIParams.CLEAVE_AFTER.id)) {
                String arg = aLine.getOptionValue(EnzymesCLIParams.CLEAVE_AFTER.id);
                ArrayList<String> aaInput = CommandLineUtils.splitInput(arg);
                for (String aa : aaInput) {
                    enzymeToAdd.addAminoAcidAfter(aa.charAt(0));
                }
            }
        }
    }

    /**
     * Returns the file provided as input.
     *
     * @return the file provided as input
     */
    public File getFileIn() {
        return fileIn;
    }

    /**
     * Returns the file provided as output.
     *
     * @return the file provided as output
     */
    public File getFileOut() {
        return fileOut;
    }

    /**
     * Returns a boolean indicating whether the list of implemented enzymes
     * should be printed.
     *
     * @return a boolean indicating whether the list of implemented enzymes
     * should be printed
     */
    public boolean isList() {
        return list;
    }

    /**
     * Returns the name of the enzyme to remove.
     *
     * @return the name of the enzyme to remove
     */
    public String getEnzymeToRemove() {
        return enzymeToRemove;
    }

    /**
     * Returns the enzyme to add.
     *
     * @return the enzyme to add
     */
    public Enzyme getEnzymeToAdd() {
        return enzymeToAdd;
    }

}
