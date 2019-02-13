package com.compomics.cli.fasta;

import com.compomics.software.cli.CommandParameter;
import com.compomics.util.Util;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.cli.CommandLine;

/**
 * This class gathers command line parameters for the parsing of FASTA files.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class FastaParametersInputBean {

    /**
     * The FASTA parsing parameters.
     */
    private FastaParameters fastaParameters;

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

        if (aLine.hasOption(FastaParametersCLIParams.SUFFIX.id)) {

            String arg = aLine.getOptionValue(FastaParametersCLIParams.SUFFIX.id);

            if (!CommandParameter.isInList(arg, arg, new String[]{"1", "2"})) {

                return false;

            }
        }

        return true;
    }

    /**
     * Parses all the arguments from a command line.
     *
     * @param aLine the command line
     * @param fastaFile the FASTA file to infer the parameters from if not
     * provided in the command line arguments
     *
     * @throws IOException if an error occurs while reading or writing a file.
     */
    public FastaParametersInputBean(CommandLine aLine, File fastaFile) throws IOException {

        FastaParameters tempFastaParameters = new FastaParameters();

        if (aLine.hasOption(FastaParametersCLIParams.NAME.id)) {

            String arg = aLine.getOptionValue(FastaParametersCLIParams.NAME.id);
            tempFastaParameters.setName(arg);

        } else {

            String fileName = Util.removeExtension(fastaFile.getName());
            tempFastaParameters.setName(fileName);

        }

        if (aLine.hasOption(FastaParametersCLIParams.DESCRIPTION.id)) {

            String arg = aLine.getOptionValue(FastaParametersCLIParams.DESCRIPTION.id);
            tempFastaParameters.setDescription(arg);

        } else {

            String fileName = Util.removeExtension(fastaFile.getName());
            tempFastaParameters.setDescription(fileName);

        }

        if (aLine.hasOption(FastaParametersCLIParams.VERSION.id)) {

            String arg = aLine.getOptionValue(FastaParametersCLIParams.VERSION.id);
            tempFastaParameters.setVersion(arg);

        } else {

            String fileVersion = new Date(fastaFile.lastModified()).toString();
            tempFastaParameters.setName(fileVersion);

        }

        if (aLine.hasOption(FastaParametersCLIParams.DECOY_FLAG.id)) {

            String arg = aLine.getOptionValue(FastaParametersCLIParams.DECOY_FLAG.id);

            if (arg.equals("")) {

                tempFastaParameters.setTargetDecoy(false);

            } else {

                tempFastaParameters.setTargetDecoy(true);
                tempFastaParameters.setDecoyFlag(arg);

                if (aLine.hasOption(FastaParametersCLIParams.SUFFIX.id)) {

                    arg = aLine.getOptionValue(FastaParametersCLIParams.SUFFIX.id);

                    if (arg.equals("1")) {

                        tempFastaParameters.setDecoySuffix(false);

                    } else {

                        tempFastaParameters.setDecoySuffix(true);

                    }
                } else {

                    tempFastaParameters.setDecoySuffix(true);

                }
            }

        } else {

            FastaParameters parsedParameters = FastaParameters.inferParameters(fastaFile.getAbsolutePath());

            tempFastaParameters.setTargetDecoy(parsedParameters.isTargetDecoy());
            tempFastaParameters.setDecoyFlag(parsedParameters.getDecoyFlag());
            tempFastaParameters.setDecoySuffix(parsedParameters.isDecoySuffix());

        }

        this.fastaParameters = tempFastaParameters;
    }

    /**
     * Returns the FASTA parameters as parsed from the command line.
     *
     * @return the FASTA parameters as parsed from the command line
     */
    public FastaParameters getFastaParameters() {
        return fastaParameters;
    }
}
