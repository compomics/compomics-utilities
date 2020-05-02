package com.compomics.cli.modification_score;

import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;

/**
 * This class parses the parameters from an ModificationsCLI.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ModificationScoreOptionsBean {

    /**
     * The input file.
     */
    public final File inputFile;

    /**
     * The output file.
     */
    public final File outputFile;

    /**
     * The log file.
     */
    public final File logFile;

    /**
     * Parses all the arguments from a command line.
     *
     * @param aLine the command line
     *
     * @throws IOException if an error occurs while reading or writing a file.
     */
    public ModificationScoreOptionsBean(CommandLine aLine) throws IOException {

        // Check that mandatory options are provided
        for (ModificationScoreOptions option : ModificationScoreOptions.values()) {

            if (option.mandatory && !aLine.hasOption(option.id)) {

                throw new IllegalArgumentException("No value found for mandatory option " + option.id + ".");

            }
        }

        // Input file
        String arg = aLine.getOptionValue(ModificationScoreOptions.IN.id);
        inputFile = new File(arg);

        // Output file
        arg = aLine.getOptionValue(ModificationScoreOptions.OUT.id);
        outputFile = new File(arg);

        // Log file
        if (aLine.hasOption(ModificationScoreOptions.LOG.id)) {

            arg = aLine.getOptionValue(ModificationScoreOptions.LOG.id);
            logFile = new File(arg);

        } else {

            logFile = new File(outputFile.getAbsolutePath() + ".log");

        }

    }
}
