package com.compomics.software.cli;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters;
import com.compomics.util.experiment.identification.parameters_cli.IdentificationParametersCLIParams;
import com.compomics.util.experiment.identification.ptm.PtmScore;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;

/**
 * Convenience methods for the validation of command line parameters.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class CommandParameter {

    /**
     * Verifies the command line start parameters.
     *
     * @param aLine the command line to validate
     * @param checkMandatoryParameters if true, check if mandatory parameters
     * are included
     * @return true if the startup was valid
     * @throws IOException if an IOException occurs
     */
    public static boolean isValidStartup(CommandLine aLine, boolean checkMandatoryParameters) throws IOException {
        if (aLine.getOptions().length == 0) {
            return false;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MODS.id)) {
            return true;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_PPM.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.PREC_PPM.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_PPM.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.FRAG_PPM.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_TOL.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.PREC_TOL.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_TOL.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.FRAG_TOL.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ENZYME.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ENZYME.id);
            Enzyme option = EnzymeFactory.getInstance().getEnzyme(arg);
            if (option == null) {
                System.out.println(System.getProperty("line.separator") + "Enzyme " + arg + " not recognized." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.id);
            if (arg.equals("")) {
                System.out.println(System.getProperty("line.separator") + "No input file specified!" + System.getProperty("line.separator"));
                return false;
            }
            File fileIn = new File(arg);
            try {
                IdentificationParameters.getIdentificationParameters(fileIn);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while importing the parameters file " + fileIn + " (see below)." + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OUT.id)) {
            if (aLine.getOptionValue(IdentificationParametersCLIParams.OUT.id).equals("")) {
                System.out.println(System.getProperty("line.separator") + "No output file specified!" + System.getProperty("line.separator"));
                return false;
            }
        } else if (checkMandatoryParameters) {
            System.out.println(System.getProperty("line.separator") + "No output file specified!" + System.getProperty("line.separator"));
            return false;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DB.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DB.id);
            File fastaFile = new File(arg);
            if (!fastaFile.exists()) {
                System.out.println(System.getProperty("line.separator") + "Database not found." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MC.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MC.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MIN_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MIN_CHARGE.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MIN_CHARGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MAX_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MAX_CHARGE.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MAX_CHARGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MIN_ISOTOPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MIN_ISOTOPE.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MIN_ISOTOPE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MAX_ISOTOPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MAX_ISOTOPE.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MAX_ISOTOPE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORMAT.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!isInList(IdentificationParametersCLIParams.OMSSA_FORMAT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_NEUTRON.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_NEUTRON.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_NEUTRON.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_METHIONINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_METHIONINE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_METHIONINE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_EVALUE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_EVALUE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_RESULTS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_RESULTS.id);
            List<String> supportedInput = Arrays.asList("all", "valid", "stochastic");
            if (!isInList(IdentificationParametersCLIParams.XTANDEM_OUTPUT_RESULTS.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_DECOY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_DECOY.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.MSGF_DECOY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2", "3");
            if (!isInList(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2", "3");
            if (!isInList(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_PROTOCOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PROTOCOL.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2", "3", "4", "5");
            if (!isInList(IdentificationParametersCLIParams.MSGF_PROTOCOL.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_TERMINI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_TERMINI.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!isInList(IdentificationParametersCLIParams.MSGF_TERMINI.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PTMS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MSGF_PTMS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!isInList(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id);
            if (!inDoubleRange(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id, arg, 0.0, 1.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id);
            List<String> supportedInput = Arrays.asList("mzIdentML", "pepXML");
            if (!isInList(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_PTMS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.COMET_PTMS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REQ_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REQ_PTMS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.COMET_REQ_PTMS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!isInList(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id);
            List<String> supportedInput = Arrays.asList("1", "2", "8", "9");
            if (!isInList(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!isInList(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id);
            if (!inIntegerRange(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id, arg, 1, 5)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_METH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_METH.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.COMET_REMOVE_METH.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_OUTPUT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_OUTPUT.id);
            List<String> supportedInput = new ArrayList<String>();
            for (CometParameters.CometOutputFormat format : CometParameters.CometOutputFormat.values()) {
                supportedInput.add(format.toString());
            }
            if (!isInList(IdentificationParametersCLIParams.COMET_OUTPUT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.TIDE_PTMS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id);
            List<String> supportedInput = Arrays.asList("none", "shuffle", "peptide-reverse", "protein-reverse");
            if (!isInList(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id);
            List<String> supportedInput = Arrays.asList("N", "C", "NC");
            if (!isInList(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_VERBOSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_VERBOSITY.id);
            List<String> supportedInput = Arrays.asList("0", "10", "20", "30", "40", "50", "60");
            if (!isInList(IdentificationParametersCLIParams.TIDE_VERBOSITY.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id);
            List<String> supportedInput = Arrays.asList("full-digest", "partial-digest");
            if (!isInList(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id);
            List<String> supportedInput = Arrays.asList("1", "2", "3", "all");
            if (!isInList(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_CONCAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_CONCAT.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_CONCAT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id);
            List<String> supportedInput = Arrays.asList("HCD", "CID", "ETD");
            if (!isInList(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_DECOY_MODE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_DECOY_MODE.id);
            List<String> supportedInput = new ArrayList<String>();
            for (AndromedaParameters.AndromedaDecoyMode decoyMode : AndromedaParameters.AndromedaDecoyMode.values()) {
                supportedInput.add(decoyMode.toString());
            }
            if (!isInList(IdentificationParametersCLIParams.ANDROMEDA_DECOY_MODE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id);
            if (!inIntegerRange(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id, arg, 1, 20)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id);
            List<String> supportedInput = Arrays.asList("CID_IT_TRYP");
            if (!isInList(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id);
            if (!inIntegerRange(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id, arg, 0, 100)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id);
            if (!isDouble(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id);
            if (!isDouble(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id);
            if (!isDouble(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_OUTPUT_SUFFIX.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!isInList(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id);
            List<String> supportedInput = Arrays.asList("HCD", "CID", "ETD");
            if (!isInList(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.NOVOR_FRAGMENTATION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.NOVOR_FRAGMENTATION.id);
            List<String> supportedInput = Arrays.asList("HCD", "CID");
            if (!isInList(IdentificationParametersCLIParams.NOVOR_FRAGMENTATION.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.NOVOR_MASS_ANALYZER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.NOVOR_MASS_ANALYZER.id);
            List<String> supportedInput = Arrays.asList("Trap", "TOF", "FT");
            if (!isInList(IdentificationParametersCLIParams.NOVOR_MASS_ANALYZER.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_LEVEL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_LEVEL.id);
            if (!inDoubleRange(IdentificationParametersCLIParams.ANNOTATION_LEVEL.id, arg, 0.0, 1.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id);
            if (isBooleanInput(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id);
            if (!isSequenceMatchingType(IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id);
            if (!inDoubleRange(IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id, arg, 0.0, 1.0)) {
                return false;
            }
        }
        Integer min = null;
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id, arg, false)) {
                return false;
            }
            min = new Integer(arg);
        }
        Integer max = null;
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id, arg, false)) {
                return false;
            }
            max = new Integer(arg);
        }
        if (min != null && max != null && max <= min) {
            System.out.println(System.getProperty("line.separator") + IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id + " <= " + IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id + System.getProperty("line.separator"));
            return false;
        }
        min = null;
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_MC_MIN.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_MC_MIN.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.IMPORT_MC_MIN.id, arg, true)) {
                return false;
            }
            min = new Integer(arg);
        }
        max = null;
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_MC_MAX.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_MC_MAX.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.IMPORT_MC_MAX.id, arg, false)) {
                return false;
            }
            max = new Integer(arg);
        }
        if (min != null && max != null && max < min) {
            System.out.println(System.getProperty("line.separator") + IdentificationParametersCLIParams.IMPORT_MC_MAX.id + " < " + IdentificationParametersCLIParams.IMPORT_MC_MIN.id + System.getProperty("line.separator"));
            return false;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PTM_SCORE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PTM_SCORE.id);
            try {
                int scoreId = new Integer(arg);
                PtmScore.getScore(scoreId);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "Error when parsing " + IdentificationParametersCLIParams.PTM_SCORE.id + ". Option found: " + arg + "." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PTM_THRESHOLD.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PTM_THRESHOLD.id);
            if (!isPositiveDouble(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!isInList(IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id);
            if (!isSequenceMatchingType(IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PTM_ALIGNMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PTM_ALIGNMENT.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!isInList(IdentificationParametersCLIParams.PTM_ALIGNMENT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DB_PI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DB_PI.id);
            File fastaFile = new File(arg);
            if (!fastaFile.exists()) {
                System.out.println(System.getProperty("line.separator") + "Protein inference database not found." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PSM_FDR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PSM_FDR.id);
            if (!inDoubleRange(IdentificationParametersCLIParams.PSM_FDR.id, arg, 0.0, 100.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPTIDE_FDR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPTIDE_FDR.id);
            if (!inDoubleRange(IdentificationParametersCLIParams.PSM_FDR.id, arg, 0.0, 100.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PROTEIN_FDR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PROTEIN_FDR.id);
            if (!inDoubleRange(IdentificationParametersCLIParams.PSM_FDR.id, arg, 0.0, 100.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SEPARATE_PSMs.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SEPARATE_PSMs.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!isInList(IdentificationParametersCLIParams.SEPARATE_PSMs.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SEPARATE_PEPTIDES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SEPARATE_PEPTIDES.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!isInList(IdentificationParametersCLIParams.SEPARATE_PSMs.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MERGE_SUBGROUPS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MERGE_SUBGROUPS.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!isInList(IdentificationParametersCLIParams.MERGE_SUBGROUPS.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id);
            if (!inDoubleRange(IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id, arg, 0.0, 100.0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the input is an integer value inside the given range.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param minValue the minimum value allowed
     * @param maxValue the maximum value allowed
     * @return true if the input is an integer value inside the given range
     */
    public static boolean inIntegerRange(String argType, String arg, int minValue, int maxValue) {
        boolean valid = true;
        try {
            int value = new Integer(arg);
            if (value < minValue || value > maxValue) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not in the range [" + minValue + " - " + maxValue + "]." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not an integer value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true if the input is a double value inside the given range.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param minValue the minimum value allowed
     * @param maxValue the maximum value allowed
     * @return true if the input is a double value inside the given range
     */
    public static boolean inDoubleRange(String argType, String arg, double minValue, double maxValue) {
        boolean valid = true;
        try {
            double value = new Double(arg);
            if (value < minValue || value > maxValue) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not in the range [" + minValue + " - " + maxValue + "]." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not a floating value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true of the input is in the provided list.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param supportedInput the list of supported input
     * @return true of the input is in the list
     */
    public static boolean isInList(String argType, String arg, List<String> supportedInput) {
        boolean valid = true;
        if (!supportedInput.contains(arg)) {
            valid = false;
            String errorMessage = System.getProperty("line.separator") + "Error parsing the " + argType + " option: Found " + arg + ". Supported input: [";
            for (int i = 0; i < supportedInput.size(); i++) {
                if (i > 0) {
                    errorMessage += ", ";
                }
                errorMessage += supportedInput.get(i);
            }
            errorMessage += "]." + System.getProperty("line.separator");
            System.out.println(errorMessage);
        }
        return valid;
    }

    /**
     * Returns true of the input is 0 or 1.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true of the input is 0 or 1
     */
    public static boolean isBooleanInput(String argType, String arg) {
        boolean valid = true;
        try {
            int value = new Integer(arg);
            if (value != 0 && value != 1) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Found " + value + " where 0 or 1 was expected." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Found " + arg + " where 0 or 1 was expected." + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true if the argument can be parsed as an integer value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true if the argument can be parsed as an integer value
     */
    public static boolean isInteger(String argType, String arg) {
        boolean valid = true;
        try {
            new Integer(arg);
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not an integer value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true of the input is in the provided list.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true of the input is in the list
     */
    public static boolean isSequenceMatchingType(String argType, String arg) {
        List<String> supportedInput = new ArrayList<String>(SequenceMatchingPreferences.MatchingType.values().length);
        for (SequenceMatchingPreferences.MatchingType tempMatchType : SequenceMatchingPreferences.MatchingType.values()) {
            supportedInput.add("" + tempMatchType.index);
        }
        return isInList(argType, arg, supportedInput);
    }

    /**
     * Returns true if the argument can be parsed as a double value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true if the argument can be parsed as a double value
     */
    public static boolean isDouble(String argType, String arg) {
        boolean valid = true;
        try {
            new Double(arg);
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not a floating value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true if the argument can be parsed as a positive double value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param allowZero if true, zero values are allowed
     * @return true if the argument can be parsed as a positive double value
     */
    public static boolean isPositiveDouble(String argType, String arg, boolean allowZero) {
        boolean valid = true;
        try {
            double value = new Double(arg);
            if (allowZero) {
                if (value < 0) {
                    System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative value found." + System.getProperty("line.separator"));
                    valid = false;
                }
            } else if (value <= 0) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative or zero value found." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not a floating value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true if the argument can be parsed as a positive integer value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param allowZero if true, zero values are allowed
     * @return true if the argument can be parsed as a positive integer value
     */
    public static boolean isPositiveInteger(String argType, String arg, boolean allowZero) {
        boolean valid = true;
        try {
            int value = new Integer(arg);
            if (allowZero) {
                if (value < 0) {
                    System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative value found." + System.getProperty("line.separator"));
                    valid = false;
                }
            } else if (value <= 0) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative or zero value found." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not an integer value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

}
