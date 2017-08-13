package com.compomics.cli.modifications;

import com.compomics.software.cli.CommandParameter;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;

/**
 * This class parses the parameters from an EnzymeCLI.
 *
 * @author Marc Vaudel
 */
public class ModificationsCLIInputBean {

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
        ModificationFactory ptmFactory = null;
        if (aLine.hasOption(ModificationsCLIParams.IN.id)) {
            String arg = aLine.getOptionValue(ModificationsCLIParams.IN.id);
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
                ptmFactory = ModificationFactory.loadFromFile(fileIn);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing " + fileIn + "." + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(ModificationsCLIParams.LIST.id)) {
            return true;
        }
        if (aLine.hasOption(ModificationsCLIParams.RM.id)) {
            String arg = aLine.getOptionValue(ModificationsCLIParams.RM.id);
            Modification ptm = ptmFactory.getModification(arg);
            if (ptm == null) {
                String file = aLine.getOptionValue(ModificationsCLIParams.IN.id);
                System.out.println(System.getProperty("line.separator") + "Enzyme " + arg + " not found in " + file + "." + System.getProperty("line.separator"));
                return false;
            }
        }
        boolean name = aLine.hasOption(ModificationsCLIParams.NAME.id);
        boolean composition = false;
        boolean pattern = false;
        if (aLine.hasOption(ModificationsCLIParams.COMPOSITION_ADDED.id)) {
            if (!name) {
                System.out.println(System.getProperty("line.separator") + "No name provided for the PTM to add." + System.getProperty("line.separator"));
                return false;
            }
            String arg = aLine.getOptionValue(ModificationsCLIParams.COMPOSITION_ADDED.id);
            try {
                AtomChain.getAtomChain(arg);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the added atomic composition of the PTM (see below)." + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
            composition = true;
        }
        if (aLine.hasOption(ModificationsCLIParams.COMPOSITION_REMOVED.id)) {
            if (!name) {
                System.out.println(System.getProperty("line.separator") + "No name provided for the PTM to add." + System.getProperty("line.separator"));
                return false;
            }
            String arg = aLine.getOptionValue(ModificationsCLIParams.COMPOSITION_REMOVED.id);
            try {
                AtomChain.getAtomChain(arg);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the removed atomic composition of the PTM (see below)." + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
            composition = true;
        }
        if (name && !composition) {
            System.out.println(System.getProperty("line.separator") + "No atomic composition found for the PTM to add." + System.getProperty("line.separator"));
            return false;
        }
        if (aLine.hasOption(ModificationsCLIParams.PATTERN.id)) {
            if (!name) {
                System.out.println(System.getProperty("line.separator") + "No name provided for the PTM to add." + System.getProperty("line.separator"));
                return false;
            }
            String arg = aLine.getOptionValue(ModificationsCLIParams.PATTERN.id);
            try {
                AminoAcidPattern.getAminoAcidPatternFromString(arg);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the amino acid pattern of the PTM (see below)." + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
            pattern = true;
        }
        if (aLine.hasOption(ModificationsCLIParams.TYPE.id)) {
            String arg = aLine.getOptionValue(ModificationsCLIParams.COMPOSITION_REMOVED.id);
            if (!CommandParameter.isPositiveInteger(ModificationsCLIParams.COMPOSITION_REMOVED.id, arg, true)) {
                return false;
            }
            Integer type = new Integer(arg);
            if (!pattern && (type == Modification.MODAA
                    || type == Modification.MODCAA
                    || type == Modification.MODNAA
                    || type == Modification.MODCPAA
                    || type == Modification.MODNPAA)) {
                System.out.println(System.getProperty("line.separator") + "No amino acid pattern found for PTM targetting specific amino acids." + System.getProperty("line.separator"));
                return false;
            }
            if (pattern && (type == Modification.MODC
                    || type == Modification.MODN
                    || type == Modification.MODCP
                    || type == Modification.MODNP)) {
                System.out.println(System.getProperty("line.separator") + "Amino acid pattern found for PTM targetting a terminus." + System.getProperty("line.separator"));
                return false;
            }
        } else if (name) {
            System.out.println(System.getProperty("line.separator") + "No PTM type found for the PTM to add." + System.getProperty("line.separator"));
            return false;
        }
        if (aLine.hasOption(ModificationsCLIParams.PATTERN_INDEX.id)) {
            if (!name) {
                System.out.println(System.getProperty("line.separator") + "No name provided for the enzyme to add." + System.getProperty("line.separator"));
                return false;
            }
            String arg = aLine.getOptionValue(ModificationsCLIParams.PATTERN_INDEX.id);
            if (!CommandParameter.isInteger(ModificationsCLIParams.PATTERN_INDEX.id, arg)) {
                return false;
            }
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
     * The name of the modification to remove.
     */
    private String modificationToRemove = null;

    /**
     * The modification to add.
     */
    private Modification modificationToAdd = null;

    /**
     * Parses all the arguments from a command line.
     *
     * @param aLine the command line
     *
     * @throws IOException if an error occurs while reading or writing a file.
     */
    public ModificationsCLIInputBean(CommandLine aLine) throws IOException {

        if (aLine.hasOption(ModificationsCLIParams.IN.id)) {
            String arg = aLine.getOptionValue(ModificationsCLIParams.IN.id);
            fileIn = new File(arg);
        }

        if (aLine.hasOption(ModificationsCLIParams.LIST.id)) {
            list = true;
            return;
        }

        if (aLine.hasOption(ModificationsCLIParams.OUT.id)) {
            String arg = aLine.getOptionValue(ModificationsCLIParams.OUT.id);
            fileOut = new File(arg);
        }

        if (aLine.hasOption(ModificationsCLIParams.RM.id)) {
            modificationToRemove = aLine.getOptionValue(ModificationsCLIParams.RM.id);
        }

        if (aLine.hasOption(ModificationsCLIParams.NAME.id)) {
            String enzymeName = aLine.getOptionValue(ModificationsCLIParams.NAME.id);
            int index = new Integer(aLine.getOptionValue(ModificationsCLIParams.TYPE.id));
            AtomChain atomChainAdded = null;
            if (aLine.hasOption(ModificationsCLIParams.COMPOSITION_ADDED.id)) {
                String arg = aLine.getOptionValue(ModificationsCLIParams.COMPOSITION_ADDED.id);
                atomChainAdded = AtomChain.getAtomChain(arg);
            }
            AtomChain atomChainRemoved = null;
            if (aLine.hasOption(ModificationsCLIParams.COMPOSITION_REMOVED.id)) {
                String arg = aLine.getOptionValue(ModificationsCLIParams.COMPOSITION_REMOVED.id);
                atomChainRemoved = AtomChain.getAtomChain(arg);
            }
            AminoAcidPattern aminoAcidPattern = null;
            if (aLine.hasOption(ModificationsCLIParams.PATTERN.id)) {
                String arg = aLine.getOptionValue(ModificationsCLIParams.PATTERN.id);
                aminoAcidPattern = AminoAcidPattern.getAminoAcidPatternFromString(arg);
                Integer target = 0;
            if (aLine.hasOption(ModificationsCLIParams.PATTERN_INDEX.id)) {
                arg = aLine.getOptionValue(ModificationsCLIParams.PATTERN_INDEX.id);
                target = new Integer(arg);
            }
            aminoAcidPattern.setTarget(target);
            }
            modificationToAdd = new Modification(index, enzymeName, enzymeName, atomChainAdded, atomChainRemoved, aminoAcidPattern);
            if (aLine.hasOption(ModificationsCLIParams.SHORT_NAME.id)) {
                String arg = aLine.getOptionValue(ModificationsCLIParams.SHORT_NAME.id);
                modificationToAdd.setShortName(arg);
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
     * Returns the name of the modification to remove.
     *
     * @return the name of the modification to remove
     */
    public String getModificationToRemove() {
        return modificationToRemove;
    }

    /**
     * Returns the modification to add.
     *
     * @return the modification to add
     */
    public Modification getModificationToAdd() {
        return modificationToAdd;
    }

}
