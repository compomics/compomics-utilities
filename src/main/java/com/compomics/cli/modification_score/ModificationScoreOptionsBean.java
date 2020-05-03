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
     * The spectrum file.
     */
    public final File spectrumFile;

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

            if (option.mandatory && !aLine.hasOption(option.opt)) {

                throw new IllegalArgumentException("No value found for mandatory option " + option.opt + ".");

            }
        }

        // Input file
        String arg = aLine.getOptionValue(ModificationScoreOptions.input.opt);
        inputFile = new File(arg);

        // Spectrum file
        arg = aLine.getOptionValue(ModificationScoreOptions.spectrum.opt);
        spectrumFile = new File(arg);

        // Output file
        arg = aLine.getOptionValue(ModificationScoreOptions.output.opt);
        outputFile = new File(arg);

        // Log file
        if (aLine.hasOption(ModificationScoreOptions.log.opt)) {

            arg = aLine.getOptionValue(ModificationScoreOptions.log.opt);
            logFile = new File(arg);

        } else {

            logFile = new File(outputFile.getAbsolutePath() + ".log");

        }

    }
}
