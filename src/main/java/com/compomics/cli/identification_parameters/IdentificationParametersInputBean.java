package com.compomics.cli.identification_parameters;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.software.cli.CommandParameter;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.AndromedaParameters;
import com.compomics.util.parameters.identification.tool_specific.CometParameters;
import com.compomics.util.parameters.identification.tool_specific.DirecTagParameters;
import com.compomics.util.parameters.identification.tool_specific.MsAmandaParameters;
import com.compomics.util.parameters.identification.tool_specific.MsgfParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.parameters.identification.tool_specific.OmssaParameters;
import com.compomics.util.parameters.identification.tool_specific.PNovoParameters;
import com.compomics.util.parameters.identification.tool_specific.PepnovoParameters;
import com.compomics.util.parameters.identification.tool_specific.TideParameters;
import com.compomics.util.parameters.identification.tool_specific.XtandemParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.tool_specific.NovorParameters;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapperType;
import com.compomics.util.experiment.identification.modification.PtmScore;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import com.compomics.util.experiment.mass_spectrometry.FragmentationMethod;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters.Specificity;
import com.compomics.util.parameters.identification.advanced.FractionParameters;
import com.compomics.util.parameters.identification.advanced.GeneParameters;
import com.compomics.util.parameters.identification.advanced.IdMatchValidationParameters;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.advanced.ModificationLocalizationParameters;
import com.compomics.util.parameters.identification.advanced.ProteinInferenceParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;

/**
 * This class parses the parameters from a command line and stores them in a
 * SearchParameters object.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IdentificationParametersInputBean {

    /**
     * Verifies the command line start parameters.
     *
     * @param aLine the command line to validate
     * @param checkMandatoryParameters if true, check if mandatory parameters
     * are included
     *
     * @return true if the startup was valid
     */
    public static boolean isValidStartup(CommandLine aLine, boolean checkMandatoryParameters) {
        if (aLine.getOptions().length == 0) {
            return false;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MODS.id)) {
            return true;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ENZYMES.id)) {
            return true;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_PPM.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.PREC_PPM.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_PPM.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.FRAG_PPM.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_TOL.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.PREC_TOL.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_TOL.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.FRAG_TOL.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FI.id);
            ArrayList<String> possibleValues = new ArrayList<>(3);
            possibleValues.add(PeptideFragmentIon.getSubTypeAsString(PeptideFragmentIon.A_ION));
            possibleValues.add(PeptideFragmentIon.getSubTypeAsString(PeptideFragmentIon.B_ION));
            possibleValues.add(PeptideFragmentIon.getSubTypeAsString(PeptideFragmentIon.C_ION));
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.FI.id, arg, possibleValues)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.RI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.RI.id);
            ArrayList<String> possibleValues = new ArrayList<>(3);
            possibleValues.add(PeptideFragmentIon.getSubTypeAsString(PeptideFragmentIon.X_ION));
            possibleValues.add(PeptideFragmentIon.getSubTypeAsString(PeptideFragmentIon.Y_ION));
            possibleValues.add(PeptideFragmentIon.getSubTypeAsString(PeptideFragmentIon.Z_ION));
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.RI.id, arg, possibleValues)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIGESTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIGESTION.id);
            ArrayList<String> possibleValues = new ArrayList<>(Specificity.values().length);
            for (DigestionParameters.CleavagePreference cleavagePreference : DigestionParameters.CleavagePreference.values()) {
                possibleValues.add(cleavagePreference.index + "");
            }
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.DIGESTION.id, arg, possibleValues)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ENZYME.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ENZYME.id);
            ArrayList<String> enzymes = CommandLineUtils.splitInput(arg);
            for (String enzymeName : enzymes) {
                Enzyme enzyme = EnzymeFactory.getInstance().getEnzyme(enzymeName);
                if (enzyme == null) {
                    System.out.println(System.getProperty("line.separator") + "Enzyme " + arg + " not recognized. Please add enzyme using the EnzymeCLI command." + System.getProperty("line.separator"));
                    return false;
                }
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MC.id);
            ArrayList<String> mcsAsString = CommandLineUtils.splitInput(arg);
            for (String mcAsString : mcsAsString) {
                if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MC.id, mcAsString, true)) {
                    return false;
                }
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SPECIFICITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SPECIFICITY.id);
            ArrayList<String> possibleValues = new ArrayList<>(Specificity.values().length);
            for (Specificity specificity : Specificity.values()) {
                possibleValues.add(specificity.index + "");
            }
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.SPECIFICITY.id, arg, possibleValues)) {
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
        if (aLine.hasOption(IdentificationParametersCLIParams.MIN_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MIN_CHARGE.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MIN_CHARGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MAX_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MAX_CHARGE.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MAX_CHARGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MIN_ISOTOPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MIN_ISOTOPE.id);
            if (!CommandParameter.isInteger(IdentificationParametersCLIParams.MIN_ISOTOPE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MAX_ISOTOPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MAX_ISOTOPE.id);
            if (!CommandParameter.isInteger(IdentificationParametersCLIParams.MAX_ISOTOPE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORMAT.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.OMSSA_FORMAT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_NEUTRON.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_NEUTRON.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_NEUTRON.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_METHIONINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_METHIONINE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_METHIONINE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_PARENT_MONOISOTOPIC_MASS_ISOTOPE_ERROR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_PARENT_MONOISOTOPIC_MASS_ISOTOPE_ERROR.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_PARENT_MONOISOTOPIC_MASS_ISOTOPE_ERROR.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_EVALUE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.XTANDEM_EVALUE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_RESULTS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_RESULTS.id);
            List<String> supportedInput = Arrays.asList("all", "valid", "stochastic");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.XTANDEM_OUTPUT_RESULTS.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_DECOY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_DECOY.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.MSGF_DECOY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2", "3");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2", "3");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_PROTOCOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PROTOCOL.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2", "3", "4", "5");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.MSGF_PROTOCOL.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_TERMINI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_TERMINI.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.MSGF_TERMINI.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PTMS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MSGF_PTMS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_PERFORM_DEISOTOPING.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_PERFORM_DEISOTOPING.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.MS_AMANDA_PERFORM_DEISOTOPING.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD.id, arg, 0, 10)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_VAR_MOD.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_VAR_MOD.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.MS_AMANDA_MAX_VAR_MOD.id, arg, 0, 10)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD_SITES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD_SITES.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD_SITES.id, arg, 0, 20)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES.id, arg, 0, 5)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES_MODIFICATIONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES_MODIFICATIONS.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES_MODIFICATIONS.id, arg, 0, 5)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MIN_PEPTIDE_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MIN_PEPTIDE_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MS_AMANDA_MIN_PEPTIDE_LENGTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_LOADED_PROTEINS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_LOADED_PROTEINS.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.MS_AMANDA_LOADED_PROTEINS.id, arg, 1000, 500000)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_LOADED_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_LOADED_SPECTRA.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.MS_AMANDA_LOADED_SPECTRA.id, arg, 1000, 500000)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id);
            if (!CommandParameter.inDoubleRange(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id, arg, 0.0, 1.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id);
            List<String> supportedInput = Arrays.asList("mzIdentML", "pepXML");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_PTMS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.COMET_PTMS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REQ_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REQ_PTMS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.COMET_REQ_PTMS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2", "3");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id);
            List<String> supportedInput = Arrays.asList("1", "2", "8", "9");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2", "3", "4");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id, arg, 1, 5)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_METH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_METH.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.COMET_REMOVE_METH.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_OUTPUT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_OUTPUT.id);
            List<String> supportedInput = new ArrayList<>();
            for (CometParameters.CometOutputFormat format : CometParameters.CometOutputFormat.values()) {
                supportedInput.add(format.toString());
            }
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.COMET_OUTPUT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.TIDE_PTMS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id);
            List<String> supportedInput = Arrays.asList("none", "shuffle", "peptide-reverse", "protein-reverse");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id);
            List<String> supportedInput = Arrays.asList("N", "C", "NC");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_VERBOSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_VERBOSITY.id);
            List<String> supportedInput = Arrays.asList("0", "10", "20", "30", "40", "50", "60");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.TIDE_VERBOSITY.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id);
            List<String> supportedInput = Arrays.asList("full-digest", "partial-digest");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id);
            List<String> supportedInput = Arrays.asList("1", "2", "3", "all");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_CONCAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_CONCAT.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_CONCAT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id);
            List<String> supportedInput = Arrays.asList("HCD", "CID", "ETD");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_DECOY_MODE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_DECOY_MODE.id);
            List<String> supportedInput = new ArrayList<>();
            for (AndromedaParameters.AndromedaDecoyMode decoyMode : AndromedaParameters.AndromedaDecoyMode.values()) {
                supportedInput.add(decoyMode.toString());
            }
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.ANDROMEDA_DECOY_MODE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id, arg, 1, 20)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id);
            List<String> supportedInput = Arrays.asList("CID_IT_TRYP");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id);
            if (!CommandParameter.inIntegerRange(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id, arg, 0, 100)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id);
            if (!CommandParameter.isDouble(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id);
            if (!CommandParameter.isDouble(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id);
            if (!CommandParameter.isDouble(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_OUTPUT_SUFFIX.id)) {
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id);
            List<String> supportedInput = Arrays.asList("0", "1", "2");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id);
            List<String> supportedInput = Arrays.asList("HCD", "CID", "ETD");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.NOVOR_FRAGMENTATION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.NOVOR_FRAGMENTATION.id);
            List<String> supportedInput = Arrays.asList("HCD", "CID");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.NOVOR_FRAGMENTATION.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.NOVOR_MASS_ANALYZER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.NOVOR_MASS_ANALYZER.id);
            List<String> supportedInput = Arrays.asList("Trap", "TOF", "FT");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.NOVOR_MASS_ANALYZER.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_LEVEL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_LEVEL.id);
            if (!CommandParameter.inDoubleRange(IdentificationParametersCLIParams.ANNOTATION_LEVEL.id, arg, 0.0, 1.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id);
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id);
            if (CommandParameter.isBooleanInput(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id);
            if (!CommandParameter.isSequenceMatchingType(IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id);
            if (!CommandParameter.inDoubleRange(IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id, arg, 0.0, 1.0)) {
                return false;
            }
        }
        Integer min = null;
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id, arg, false)) {
                return false;
            }
            min = new Integer(arg);
        }
        Integer max = null;
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id, arg, false)) {
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
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.IMPORT_MC_MIN.id, arg, true)) {
                return false;
            }
            min = new Integer(arg);
        }
        max = null;
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_MC_MAX.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_MC_MAX.id);
            if (!CommandParameter.isPositiveInteger(IdentificationParametersCLIParams.IMPORT_MC_MAX.id, arg, false)) {
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
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id, arg)) {
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
            if (!CommandParameter.isPositiveDouble(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id, arg, false)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id);
            if (!CommandParameter.isSequenceMatchingType(IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PTM_ALIGNMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PTM_ALIGNMENT.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.PTM_ALIGNMENT.id, arg, supportedInput)) {
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
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id);
            if (!CommandParameter.isBooleanInput(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PSM_FDR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PSM_FDR.id);
            if (!CommandParameter.inDoubleRange(IdentificationParametersCLIParams.PSM_FDR.id, arg, 0.0, 100.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPTIDE_FDR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPTIDE_FDR.id);
            if (!CommandParameter.inDoubleRange(IdentificationParametersCLIParams.PSM_FDR.id, arg, 0.0, 100.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PROTEIN_FDR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PROTEIN_FDR.id);
            if (!CommandParameter.inDoubleRange(IdentificationParametersCLIParams.PSM_FDR.id, arg, 0.0, 100.0)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SEPARATE_PSMs.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SEPARATE_PSMs.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.SEPARATE_PSMs.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.SEPARATE_PEPTIDES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.SEPARATE_PEPTIDES.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.SEPARATE_PSMs.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MERGE_SUBGROUPS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MERGE_SUBGROUPS.id);
            List<String> supportedInput = Arrays.asList("0", "1");
            if (!CommandParameter.isInList(IdentificationParametersCLIParams.MERGE_SUBGROUPS.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id);
            if (!CommandParameter.inDoubleRange(IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id, arg, 0.0, 100.0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The identification parameters.
     */
    private IdentificationParameters identificationParameters;
    /**
     * An input file.
     */
    private File inputFile;
    /**
     * The file where to save the parameters.
     */
    private File destinationFile;
    /**
     * If true the modifications will be printed.
     */
    private Boolean listMods = false;
    /**
     * If true the enzymes will be printed.
     */
    private Boolean listEnzymes = false;
    /**
     * The compomics PTM factory.
     */
    private ModificationFactory ptmFactory = ModificationFactory.getInstance();
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    /**
     * The command line.
     */
    private CommandLine commandLine;

    /**
     * Takes all the arguments from a command line.
     *
     * @param aLine the command line
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if aClassNotFoundException
     * ClassNotFoundException occurs
     */
    public IdentificationParametersInputBean(CommandLine aLine) throws IOException, ClassNotFoundException {

        this.commandLine = aLine;

        ///////////////////////////////////
        // General parameters
        ///////////////////////////////////
        if (commandLine.hasOption(IdentificationParametersCLIParams.MODS.id)) {
            listMods = true;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ENZYMES.id)) {
            listEnzymes = true;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.id);
            inputFile = new File(arg);
            identificationParameters = IdentificationParameters.getIdentificationParameters(inputFile);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OUT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OUT.id);
            if (!arg.endsWith(".par")) {
                arg += ".par";
            }
            destinationFile = new File(arg);
        }
        updateIdentificationParameters();
    }

    /**
     * Updates the identification parameters according to the command line.
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if aClassNotFoundException
     * ClassNotFoundException occurs
     */
    public void updateIdentificationParameters() throws FileNotFoundException, IOException, ClassNotFoundException {

        ///////////////////////////////////
        // General search parameters
        ///////////////////////////////////
        SearchParameters searchParameters = null;
        if (identificationParameters != null) {
            searchParameters = identificationParameters.getSearchParameters();
        }
        if (searchParameters == null) {
            searchParameters = new SearchParameters();
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PREC_PPM.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PREC_PPM.id);
            Integer option = new Integer(arg);
            switch (option) {
                case 1:
                    searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
                    break;
                case 0:
                    searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.PREC_PPM.id + ": " + arg + ". 0 or 1 expected.");
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.FRAG_PPM.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.FRAG_PPM.id);
            Integer option = new Integer(arg);
            if (option == 1) {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.PPM);
            } else {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PREC_TOL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PREC_TOL.id);
            Double option = new Double(arg);
            searchParameters.setPrecursorAccuracy(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.FRAG_TOL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.FRAG_TOL.id);
            Double option = new Double(arg);
            searchParameters.setFragmentIonAccuracy(option);
        }
        DigestionParameters digestionPreferences = searchParameters.getDigestionParameters();
        if (digestionPreferences == null) {
            digestionPreferences = DigestionParameters.getDefaultPreferences();
            searchParameters.setDigestionParameters(digestionPreferences);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIGESTION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIGESTION.id);
            Integer option = new Integer(arg);
            DigestionParameters.CleavagePreference cleavagePreference = DigestionParameters.CleavagePreference.getCleavagePreferences(option);
            if (digestionPreferences.getCleavagePreference() != null && digestionPreferences.getCleavagePreference() != cleavagePreference) {
                digestionPreferences.clear();
            }
            digestionPreferences.setCleavagePreference(cleavagePreference);
        }

        if (commandLine.hasOption(IdentificationParametersCLIParams.ENZYME.id)) {
            if (digestionPreferences.hasEnzymes()) {
                digestionPreferences.clearEnzymes();
            }
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ENZYME.id);
            ArrayList<String> enzymes = CommandLineUtils.splitInput(arg);
            ArrayList<Integer> mcs = null;
            ArrayList<Specificity> specificities = null;
            if (commandLine.hasOption(IdentificationParametersCLIParams.MC.id)) {
                arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MC.id);
                ArrayList<String> args = CommandLineUtils.splitInput(arg);
                mcs = new ArrayList<>(args.size());
                for (String stringValue : args) {
                    mcs.add(new Integer(stringValue));
                }
            }
            if (commandLine.hasOption(IdentificationParametersCLIParams.SPECIFICITY.id)) {
                arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SPECIFICITY.id);
                ArrayList<String> args = CommandLineUtils.splitInput(arg);
                specificities = new ArrayList<>(args.size());
                for (String stringValue : args) {
                    specificities.add(Specificity.getSpecificity(new Integer(stringValue)));
                }
            }
            for (int i = 0; i < enzymes.size(); i++) {
                String enzymeName = enzymes.get(i);
                Enzyme enzyme = enzymeFactory.getEnzyme(enzymeName);
                digestionPreferences.addEnzyme(enzyme);
                Integer mc;
                if (mcs != null) {
                    mc = mcs.get(i);
                } else {
                    mc = 2;
                }
                digestionPreferences.setnMissedCleavages(enzymeName, mc);
                Specificity specificity;
                if (specificities != null) {
                    specificity = specificities.get(i);
                } else {
                    specificity = Specificity.specific;
                }
                digestionPreferences.setSpecificity(enzymeName, specificity);
            }
        } else if (digestionPreferences.getCleavagePreference() == DigestionParameters.CleavagePreference.enzyme && !digestionPreferences.hasEnzymes()) {
            String enzymeName = "Trypsin";
            Enzyme enzyme = enzymeFactory.getEnzyme(enzymeName);
            digestionPreferences.addEnzyme(enzyme);
            digestionPreferences.setnMissedCleavages(enzymeName, 2);
            digestionPreferences.setSpecificity(enzymeName, Specificity.specific);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DB.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DB.id);
            File fastaFile = new File(arg);
            searchParameters.setFastaFile(fastaFile);

            // also update the protein inference database if that option is not set
            if (identificationParameters != null && !commandLine.hasOption(IdentificationParametersCLIParams.DB_PI.id)) {
                ProteinInferenceParameters proteinInferencePreferences = identificationParameters.getProteinInferencePreferences();
                proteinInferencePreferences.setProteinSequenceDatabase(fastaFile);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.FI.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.FI.id);
            ArrayList<String> args = CommandLineUtils.splitInput(arg);
            ArrayList<Integer> ions = new ArrayList<>(args.size());
            for (String ionName : args) {
                ions.add(PeptideFragmentIon.getIonType(ionName));
            }
            searchParameters.setForwardIons(ions);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.RI.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.RI.id);
            ArrayList<String> args = CommandLineUtils.splitInput(arg);
            ArrayList<Integer> ions = new ArrayList<>(args.size());
            for (String ionName : args) {
                ions.add(PeptideFragmentIon.getIonType(ionName));
            }
            searchParameters.setRewindIons(ions);
        }

        if (commandLine.hasOption(IdentificationParametersCLIParams.MIN_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MIN_CHARGE.id);
            Integer option = new Integer(arg);
            searchParameters.setMinChargeSearched(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MAX_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MAX_CHARGE.id);
            Integer option = new Integer(arg);
            searchParameters.setMaxChargeSearched(option);
        }

        ModificationParameters ptmSettings = searchParameters.getModificationParameters();
        if (ptmSettings == null) {
            ptmSettings = new ModificationParameters();
            searchParameters.setModificationParameters(ptmSettings);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.FIXED_MODS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.FIXED_MODS.id);
            ptmSettings.clearFixedModifications();
            ArrayList<String> args = CommandLineUtils.splitInput(arg);
            for (String ptmName : args) {
                Modification modification = ptmFactory.getModification(ptmName);
                ptmSettings.addFixedModification(modification);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.VARIABLE_MODS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.VARIABLE_MODS.id);
            ptmSettings.clearVariableModifications();
            ArrayList<String> args = CommandLineUtils.splitInput(arg);
            for (String ptmName : args) {
                Modification modification = ptmFactory.getModification(ptmName);
                ptmSettings.addVariableModification(modification);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MIN_ISOTOPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MIN_ISOTOPE.id);
            Integer option = new Integer(arg);
            searchParameters.setMinIsotopicCorrection(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MAX_ISOTOPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MAX_ISOTOPE.id);
            Integer option = new Integer(arg);
            searchParameters.setMaxIsotopicCorrection(option);
        }

        ///////////////////////////////////
        // OMSSA parameters
        ///////////////////////////////////
        OmssaParameters omssaParameters;
        Integer algorithmIndex = Advocate.omssa.getIndex();
        IdentificationAlgorithmParameter identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            omssaParameters = new OmssaParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, omssaParameters);
        } else {
            omssaParameters = (OmssaParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id);
            Integer option = new Integer(arg);
            omssaParameters.setRemovePrecursor(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id);
            Integer option = new Integer(arg);
            omssaParameters.setScalePrecursor(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id);
            Integer option = new Integer(arg);
            omssaParameters.setEstimateCharge(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id);
            Double option = new Double(arg);
            omssaParameters.setMaxEValue(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setHitListLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setMinPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORMAT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORMAT.id);
            Integer option = new Integer(arg);
            omssaParameters.setSelectedOutput(OmssaParameters.getOmssaOutputTypes()[option]);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id);
            Integer option = new Integer(arg);
            omssaParameters.setMemoryMappedSequenceLibraries(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_NEUTRON.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_NEUTRON.id);
            Double option = new Double(arg);
            omssaParameters.setNeutronThreshold(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id);
            Double option = new Double(arg);
            omssaParameters.setLowIntensityCutOff(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id);
            Double option = new Double(arg);
            omssaParameters.setHighIntensityCutOff(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id);
            Double option = new Double(arg);
            omssaParameters.setIntensityCutOffIncrement(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setSingleChargeWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setDoubleChargeWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setnPeaksInSingleChargeWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setnPeaksInDoubleChargeWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setnAnnotatedMostIntensePeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setMinAnnotatedPeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setMinPeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_METHIONINE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_METHIONINE.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setCleaveNterMethionine(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setMaxMzLadders(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setMaxFragmentCharge(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id);
            Double option = new Double(arg);
            omssaParameters.setFractionOfPeaksForChargeEstimation(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setDetermineChargePlusOneAlgorithmically(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setSearchPositiveIons(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setMinPrecPerSpectrum(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setSearchForwardFragmentFirst(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setSearchRewindFragments(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setMaxFragmentPerSeries(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setUseCorrelationCorrectionScore(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id);
            Double option = new Double(arg);
            omssaParameters.setConsecutiveIonProbability(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id);
            Double option = new Double(arg);
            omssaParameters.setIterativeSequenceEvalue(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id);
            Double option = new Double(arg);
            omssaParameters.setIterativeSpectrumEvalue(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id);
            Double option = new Double(arg);
            omssaParameters.setIterativeReplaceEvalue(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id);
            int option = Integer.parseInt(arg);
            omssaParameters.setMaxFragmentPerSeries(option);
        }

        ///////////////////////////////////
        // X!Tandem parameters
        ///////////////////////////////////
        XtandemParameters xtandemParameters;
        algorithmIndex = Advocate.xtandem.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            xtandemParameters = new XtandemParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, xtandemParameters);
        } else {
            xtandemParameters = (XtandemParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id);
            Double option = new Double(arg);
            xtandemParameters.setDynamicRange(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setnPeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id);
            Double option = new Double(arg);
            xtandemParameters.setMinFragmentMz(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setMinPeaksPerSpectrum(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id);
            Integer option = new Integer(arg);
            xtandemParameters.setUseNoiseSuppression(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id);
            Double option = new Double(arg);
            xtandemParameters.setMinPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_PARENT_MONOISOTOPIC_MASS_ISOTOPE_ERROR.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_PARENT_MONOISOTOPIC_MASS_ISOTOPE_ERROR.id);
            Integer option = new Integer(arg);
            xtandemParameters.setParentMonoisotopicMassIsotopeError(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id);
            Integer option = new Integer(arg);
            xtandemParameters.setProteinQuickAcetyl(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id);
            Integer option = new Integer(arg);
            xtandemParameters.setQuickPyrolidone(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setStpBias(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefine(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id);
            Double option = new Double(arg);
            xtandemParameters.setMaximumExpectationValueRefinement(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefineUnanticipatedCleavages(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefineSemi(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id);
            Integer option = new Integer(arg);
            xtandemParameters.setPotentialModificationsForFullRefinment(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefinePointMutations(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefineSnaps(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefineSpectrumSynthesis(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_EVALUE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_EVALUE.id);
            Double option = new Double(arg);
            xtandemParameters.setMaxEValue(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_RESULTS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_RESULTS.id);
            xtandemParameters.setOutputResults(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id);
            xtandemParameters.setOutputResults(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id);
            Integer option = new Integer(arg);
            xtandemParameters.setOutputSequences(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id);
            Integer option = new Integer(arg);
            xtandemParameters.setOutputSpectra(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.XTANDEM_SKYLINE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_SKYLINE.id);
            xtandemParameters.setSkylinePath(arg);
        }

        ///////////////////////////////////
        // MS-GF+ parameters
        ///////////////////////////////////
        MsgfParameters msgfParameters;
        algorithmIndex = Advocate.msgf.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            msgfParameters = new MsgfParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, msgfParameters);
        } else {
            msgfParameters = (MsgfParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_DECOY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_DECOY.id);
            Integer option = new Integer(arg);
            msgfParameters.setSearchDecoyDatabase(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id);
            Integer instrumentID = new Integer(arg);
            msgfParameters.setInstrumentID(instrumentID); // @TODO: check for valid index!!
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id);
            Integer option = new Integer(arg);
            msgfParameters.setFragmentationType(option); // @TODO: check for valid index!!
        } else {
            Integer instrumentId = msgfParameters.getInstrumentID();
            if (instrumentId == 1 || instrumentId == 3) {
                msgfParameters.setFragmentationType(3);
            } else {
                msgfParameters.setFragmentationType(0);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_PROTOCOL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PROTOCOL.id);
            Integer option = new Integer(arg);
            msgfParameters.setProtocol(option); // @TODO: check for valid index!!
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            msgfParameters.setMinPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            msgfParameters.setMaxPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id);
            Integer option = new Integer(arg);
            msgfParameters.setNumberOfSpectrumMarches(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id);
            Integer option = new Integer(arg);
            msgfParameters.setAdditionalOutput(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_TERMINI.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_TERMINI.id);
            Integer option = new Integer(arg);
            msgfParameters.setNumberTolerableTermini(option);
        } else {
            // Infer this setting from the general settings
            Enzyme enzyme = digestionPreferences.getEnzymes().get(0); // Only the first enzyme will be used.
            String enzymeName = enzyme.getName();
            Specificity specificity = digestionPreferences.getSpecificity(enzymeName);
            switch (specificity) {
                case specific:
                    msgfParameters.setNumberTolerableTermini(2);
                    break;
                case semiSpecific:
                case specificNTermOnly:
                case specificCTermOnly:
                    msgfParameters.setNumberTolerableTermini(1);
                default:
                    throw new UnsupportedOperationException("Specificity " + specificity + " not supported.");
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_PTMS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PTMS.id);
            Integer option = new Integer(arg);
            msgfParameters.setNumberOfModificationsPerPeptide(option);
        }

        ///////////////////////////////////
        // MyriMatch parameters
        ///////////////////////////////////
        MyriMatchParameters myriMatchParameters;
        algorithmIndex = Advocate.myriMatch.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            myriMatchParameters = new MyriMatchParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, myriMatchParameters);
        } else {
            myriMatchParameters = (MyriMatchParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMinPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMaxPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id);
            Double option = new Double(arg);
            myriMatchParameters.setMinPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id);
            Double option = new Double(arg);
            myriMatchParameters.setMaxPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_NUM_MATCHES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_NUM_MATCHES.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setNumberOfSpectrumMatches(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMaxDynamicMods(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.id);
            myriMatchParameters.setFragmentationRule(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMinTerminiCleavages(option);
        } else {
            // Infer this setting from the general settings
            Enzyme enzyme = digestionPreferences.getEnzymes().get(0); // Only the first enzyme will be used.
            String enzymeName = enzyme.getName();
            Specificity specificity = digestionPreferences.getSpecificity(enzymeName);
            switch (specificity) {
                case specific:
                    myriMatchParameters.setMinTerminiCleavages(2);
                    break;
                case semiSpecific:
                case specificNTermOnly:
                case specificCTermOnly:
                    myriMatchParameters.setMinTerminiCleavages(1);
                default:
                    throw new UnsupportedOperationException("Specificity " + specificity + " not supported.");
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setUseSmartPlusThreeModel(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setComputeXCorr(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id);
            Double option = new Double(arg);
            myriMatchParameters.setTicCutoffPercentage(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setNumIntensityClasses(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setClassSizeMultiplier(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setNumberOfBatches(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMaxPeakCount(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id);
            myriMatchParameters.setOutputFormat(arg);
        }

        ///////////////////////////////////
        // MS Amanda parameters
        ///////////////////////////////////
        MsAmandaParameters msAmandaParameters;
        algorithmIndex = Advocate.msAmanda.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            msAmandaParameters = new MsAmandaParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, msAmandaParameters);
        } else {
            msAmandaParameters = (MsAmandaParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setGenerateDecoyDatabase(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id);
            msAmandaParameters.setInstrumentID(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxRank(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMonoIsotopic(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_PERFORM_DEISOTOPING.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_PERFORM_DEISOTOPING.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setPerformDeisotoping(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxModifications(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_VAR_MOD.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_VAR_MOD.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxVariableModifications(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD_SITES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_MOD_SITES.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxModificationSites(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxNeutralLosses(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES_MODIFICATIONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_NEUTRAL_LOSSES_MODIFICATIONS.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxNeutralLossesPerModification(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MIN_PEPTIDE_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MIN_PEPTIDE_LENGTH.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMinPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_LOADED_PROTEINS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_LOADED_PROTEINS.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxLoadedProteins(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_LOADED_SPECTRA.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_LOADED_SPECTRA.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxLoadedSpectra(option);
        }

        ///////////////////////////////////
        // Comet parameters
        ///////////////////////////////////
        CometParameters cometParameters;
        algorithmIndex = Advocate.comet.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            cometParameters = new CometParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, cometParameters);
        } else {
            cometParameters = (CometParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id);
            Integer option = new Integer(arg);
            cometParameters.setNumberOfSpectrumMatches(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_PTMS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_PTMS.id);
            Integer option = new Integer(arg);
            cometParameters.setMaxVariableMods(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_REQ_PTMS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_REQ_PTMS.id);
            Integer option = new Integer(arg);
            cometParameters.setRequireVariableMods(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id);
            Integer option = new Integer(arg);
            cometParameters.setMinPeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id);
            Double option = new Double(arg);
            cometParameters.setMinPeakIntensity(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id);
            Integer option = new Integer(arg);
            cometParameters.setRemovePrecursor(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id);
            Double option = new Double(arg);
            cometParameters.setRemovePrecursorTolerance(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id);
            Double option = new Double(arg);
            cometParameters.setLowerClearMzRange(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id);
            Double option = new Double(arg);
            cometParameters.setUpperClearMzRange(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id);
            Integer option = new Integer(arg);
            cometParameters.setEnzymeType(option);
        } else {
            // Infer this setting from the general settings
            Enzyme enzyme = digestionPreferences.getEnzymes().get(0); // Only the first enzyme will be used.
            String enzymeName = enzyme.getName();
            Specificity specificity = digestionPreferences.getSpecificity(enzymeName);
            switch (specificity) {
                case specific:
                    cometParameters.setEnzymeType(2);
                    break;
                case semiSpecific:
                    cometParameters.setEnzymeType(1);
                    break;
                case specificNTermOnly:
                    cometParameters.setEnzymeType(8);
                    break;
                case specificCTermOnly:
                    cometParameters.setEnzymeType(9);
                    break;
                default:
                    throw new UnsupportedOperationException("Specificity " + specificity + " not supported.");
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id);
            Integer option = new Integer(arg);
            cometParameters.setIsotopeCorrection(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id);
            Double option = new Double(arg);
            cometParameters.setMinPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id);
            Double option = new Double(arg);
            cometParameters.setMaxPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id);
            Integer option = new Integer(arg);
            cometParameters.setMaxFragmentCharge(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_METH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_METH.id);
            Integer option = new Integer(arg);
            cometParameters.setRemoveMethionine(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id);
            Integer option = new Integer(arg);
            cometParameters.setBatchSize(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id);
            Integer option = new Integer(arg);
            cometParameters.setTheoreticalFragmentIonsSumOnly(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id);
            Double option = new Double(arg);
            cometParameters.setFragmentBinOffset(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.COMET_OUTPUT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.COMET_OUTPUT.id);
            if (arg.equalsIgnoreCase(CometParameters.CometOutputFormat.PepXML.toString())) {
                cometParameters.setSelectedOutputFormat(CometParameters.CometOutputFormat.PepXML);
            } else if (arg.equalsIgnoreCase(CometParameters.CometOutputFormat.Percolator.toString())) {
                cometParameters.setSelectedOutputFormat(CometParameters.CometOutputFormat.Percolator);
            } else if (arg.equalsIgnoreCase(CometParameters.CometOutputFormat.SQT.toString())) {
                cometParameters.setSelectedOutputFormat(CometParameters.CometOutputFormat.SQT);
            } else if (arg.equalsIgnoreCase(CometParameters.CometOutputFormat.TXT.toString())) {
                cometParameters.setSelectedOutputFormat(CometParameters.CometOutputFormat.TXT);
            }
        }

        ///////////////////////////////////
        // Tide parameters
        ///////////////////////////////////
        TideParameters tideParameters;
        algorithmIndex = Advocate.tide.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            tideParameters = new TideParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, tideParameters);
        } else {
            tideParameters = (TideParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS.id);
            Integer option = new Integer(arg);
            tideParameters.setMaxVariableModificationsPerPeptide(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id);
            Integer option = new Integer(arg);
            tideParameters.setMaxVariableModificationsPerTypePerPeptide(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            tideParameters.setMinPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            tideParameters.setMaxPeptideLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id);
            Double option = new Double(arg);
            tideParameters.setMinPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id);
            Double option = new Double(arg);
            tideParameters.setMaxPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id);
            tideParameters.setDecoyFormat(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id);
            tideParameters.setKeepTerminalAminoAcids(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id);
            Integer option = new Integer(arg);
            tideParameters.setDecoySeed(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.id);
            tideParameters.setOutputFolderName(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id);
            Integer option = new Integer(arg);
            tideParameters.setPrintPeptides(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_VERBOSITY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_VERBOSITY.id);
            Integer option = new Integer(arg);
            tideParameters.setVerbosity(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id);
            Integer option = new Integer(arg);
            tideParameters.setMonoisotopicPrecursor(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id);
            Integer option = new Integer(arg);
            tideParameters.setClipNtermMethionine(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id);
            tideParameters.setDigestionType(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id);
            Integer option = new Integer(arg);
            tideParameters.setComputeSpScore(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id);
            Integer option = new Integer(arg);
            tideParameters.setNumberOfSpectrumMatches(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id);
            Integer option = new Integer(arg);
            tideParameters.setComputeExactPValues(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id);
            Double option = new Double(arg);
            tideParameters.setMinSpectrumMz(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id);
            Double option = new Double(arg);
            tideParameters.setMaxSpectrumMz(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id);
            Integer option = new Integer(arg);
            tideParameters.setMinSpectrumPeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id);
            tideParameters.setSpectrumCharges(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id);
            Integer option = new Integer(arg);
            tideParameters.setRemovePrecursor(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id);
            Double option = new Double(arg);
            tideParameters.setRemovePrecursorTolerance(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id);
            Integer option = new Integer(arg);
            tideParameters.setPrintProgressIndicatorSize(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id);
            Integer option = new Integer(arg);
            tideParameters.setUseFlankingPeaks(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id);
            Integer option = new Integer(arg);
            tideParameters.setUseNeutralLossPeaks(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id);
            Double option = new Double(arg);
            tideParameters.setMzBinWidth(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id);
            Double option = new Double(arg);
            tideParameters.setMzBinOffset(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_CONCAT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_CONCAT.id);
            Integer option = new Integer(arg);
            tideParameters.setConcatenatTargetDecoy(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.id);
            tideParameters.setStoreSpectraFileName(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id);
            Integer option = new Integer(arg);
            tideParameters.setTextOutput(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id);
            Integer option = new Integer(arg);
            tideParameters.setSqtOutput(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id);
            Integer option = new Integer(arg);
            tideParameters.setPepXmlOutput(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id);
            Integer option = new Integer(arg);
            tideParameters.setMzidOutput(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id);
            Integer option = new Integer(arg);
            tideParameters.setPinOutput(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id);
            Integer option = new Integer(arg);
            tideParameters.setRemoveTempFolders(option == 1);
        }

        ///////////////////////////////////
        // Andromeda parameters
        ///////////////////////////////////
        AndromedaParameters andromedaParameters;
        algorithmIndex = Advocate.andromeda.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            andromedaParameters = new AndromedaParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, andromedaParameters);
        } else {
            andromedaParameters = (AndromedaParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id);
            Double option = new Double(arg);
            andromedaParameters.setMaxPeptideMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id);
            Integer option = new Integer(arg);
            andromedaParameters.setMaxCombinations(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id);
            Integer option = new Integer(arg);
            andromedaParameters.setTopPeaksWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id);
            Integer option = new Integer(arg);
            andromedaParameters.setIncludeWater(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id);
            Integer option = new Integer(arg);
            andromedaParameters.setIncludeAmmonia(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id);
            Integer option = new Integer(arg);
            andromedaParameters.setDependentLosses(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id);
            Integer option = new Integer(arg);
            andromedaParameters.setFragmentAll(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id);
            Integer option = new Integer(arg);
            andromedaParameters.setEmpiricalCorrection(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id);
            Integer option = new Integer(arg);
            andromedaParameters.setHigherCharge(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id);
            if (arg.equalsIgnoreCase("CID")) {
                andromedaParameters.setFragmentationMethod(FragmentationMethod.CID);
            } else if (arg.equalsIgnoreCase("HCD")) {
                andromedaParameters.setFragmentationMethod(FragmentationMethod.HCD);
            } else if (arg.equalsIgnoreCase("ETD")) {
                andromedaParameters.setFragmentationMethod(FragmentationMethod.ETD);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id);
            Integer option = new Integer(arg);
            andromedaParameters.setMaxNumberOfModifications(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            andromedaParameters.setMinPeptideLengthNoEnzyme(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            andromedaParameters.setMaxPeptideLengthNoEnzyme(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id);
            Integer option = new Integer(arg);
            andromedaParameters.setEqualIL(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id);
            Integer option = new Integer(arg);
            andromedaParameters.setNumberOfCandidates(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANDROMEDA_DECOY_MODE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANDROMEDA_DECOY_MODE.id);
            if (arg.equalsIgnoreCase("none")) {
                andromedaParameters.setDecoyMode(AndromedaParameters.AndromedaDecoyMode.none);
            } else if (arg.equalsIgnoreCase("reverse")) {
                andromedaParameters.setDecoyMode(AndromedaParameters.AndromedaDecoyMode.reverse);
            }
        }

        ///////////////////////////////////
        // PepNovo+ parameters
        ///////////////////////////////////
        PepnovoParameters pepnovoParameters;
        algorithmIndex = Advocate.pepnovo.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            pepnovoParameters = new PepnovoParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, pepnovoParameters);
        } else {
            pepnovoParameters = (PepnovoParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setHitListLength(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setEstimateCharge(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setCorrectPrecursorMass(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setDiscardLowQualitySpectra(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setDiscardLowQualitySpectra(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id);
            pepnovoParameters.setFragmentationModel(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setGenerateQuery(option == 1);
        }

        ///////////////////////////////////
        // DirecTag parameters
        ///////////////////////////////////
        DirecTagParameters direcTagParameters;
        algorithmIndex = Advocate.direcTag.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            direcTagParameters = new DirecTagParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, direcTagParameters);
        } else {
            direcTagParameters = (DirecTagParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id);
            Integer option = new Integer(arg);
            direcTagParameters.setTicCutoffPercentage(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id);
            Integer option = new Integer(arg);
            direcTagParameters.setMaxPeakCount(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id);
            Integer option = new Integer(arg);
            direcTagParameters.setNumIntensityClasses(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id);
            Integer option = new Integer(arg);
            direcTagParameters.setAdjustPrecursorMass(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id);
            Double option = new Double(arg);
            direcTagParameters.setMinPrecursorAdjustment(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id);
            Double option = new Double(arg);
            direcTagParameters.setMaxPrecursorAdjustment(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id);
            Double option = new Double(arg);
            direcTagParameters.setPrecursorAdjustmentStep(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id);
            Integer option = new Integer(arg);
            direcTagParameters.setNumChargeStates(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_OUTPUT_SUFFIX.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_OUTPUT_SUFFIX.id);
            direcTagParameters.setOutputSuffix(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id);
            Integer option = new Integer(arg);
            direcTagParameters.setUseChargeStateFromMS(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id);
            Integer option = new Integer(arg);
            direcTagParameters.setDuplicateSpectra(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id);
            Integer option = new Integer(arg);
            direcTagParameters.setDeisotopingMode(option); // @TODO: check for valid values!!!
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id);
            Double option = new Double(arg);
            direcTagParameters.setIsotopeMzTolerance(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id);
            Double option = new Double(arg);
            direcTagParameters.setComplementMzTolerance(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id);
            Integer option = new Integer(arg);
            direcTagParameters.setTagLength(option); // @TODO: check for valid values!!!
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id);
            Integer option = new Integer(arg);
            direcTagParameters.setMaxDynamicMods(option); // @TODO: check for valid values!!!
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id);
            Integer option = new Integer(arg);
            direcTagParameters.setMaxTagCount(option); // @TODO: check for valid values!!!
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id);
            Double option = new Double(arg);
            direcTagParameters.setIntensityScoreWeight(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id);
            Double option = new Double(arg);
            direcTagParameters.setMzFidelityScoreWeight(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id);
            Double option = new Double(arg);
            direcTagParameters.setComplementScoreWeight(option);
        }

        ///////////////////////////////////
        // pNovo+ parameters
        ///////////////////////////////////
        PNovoParameters pNovoParameters;
        algorithmIndex = Advocate.pNovo.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            pNovoParameters = new PNovoParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, pNovoParameters);
        } else {
            pNovoParameters = (PNovoParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id);
            Integer option = new Integer(arg);
            pNovoParameters.setNumberOfPeptides(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id);
            Integer option = new Integer(arg);
            pNovoParameters.setLowerPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id);
            Integer option = new Integer(arg);
            pNovoParameters.setUpperPrecursorMass(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id);
            pNovoParameters.setActicationType(arg);
        }

        ///////////////////////////////////
        // Novor parameters
        ///////////////////////////////////
        NovorParameters novorParameters;
        algorithmIndex = Advocate.novor.getIndex();
        identificationAlgorithmParameter = searchParameters.getIdentificationAlgorithmParameter(algorithmIndex);
        if (identificationAlgorithmParameter == null) {
            novorParameters = new NovorParameters();
            searchParameters.setIdentificationAlgorithmParameter(algorithmIndex, novorParameters);
        } else {
            novorParameters = (NovorParameters) identificationAlgorithmParameter;
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.NOVOR_FRAGMENTATION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.NOVOR_FRAGMENTATION.id);
            novorParameters.setFragmentationMethod(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.NOVOR_MASS_ANALYZER.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.NOVOR_MASS_ANALYZER.id);
            novorParameters.setMassAnalyzer(arg);
        }

        // Set the parameters in the identification parameters
        if (identificationParameters != null) {
            identificationParameters.setSearchParameters(searchParameters);
        } else {
            identificationParameters = new IdentificationParameters(searchParameters);
        }

        // set the parameter file name to the same as the name of the file
        if (identificationParameters.getName() == null && destinationFile != null) {
            identificationParameters.setName(destinationFile.getName().substring(0, destinationFile.getName().lastIndexOf(".")));
        }

        //////////////////////////////////
        // Gene mapping preferences
        //////////////////////////////////
        GeneParameters genePreferences = identificationParameters.getGenePreferences();
        if (genePreferences == null) {
            genePreferences = new GeneParameters();
            identificationParameters.setGenePreferences(genePreferences);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.USE_GENE_MAPPING.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.USE_GENE_MAPPING.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.USE_GENE_MAPPING.id + ": " + arg + ". 0 or 1 expected.");
            }
            genePreferences.setUseGeneMapping(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.UPDATE_GENE_MAPPING.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.UPDATE_GENE_MAPPING.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.UPDATE_GENE_MAPPING.id + ": " + arg + ". 0 or 1 expected.");
            }
            genePreferences.setAutoUpdate(value);
        }

        //////////////////////////////////
        // Spectrum annotation
        //////////////////////////////////
        AnnotationParameters annotationSettings = identificationParameters.getAnnotationPreferences();
        if (annotationSettings == null) {
            annotationSettings = new AnnotationParameters(searchParameters);
            identificationParameters.setAnnotationSettings(annotationSettings);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_LEVEL.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_LEVEL.id);
            Double value = new Double(arg);
            annotationSettings.setIntensityLimit(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id);
            Double value = new Double(arg);
            annotationSettings.setFragmentIonAccuracy(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id)) { //@TODO: extend the command line to support ties resolution settings
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id + ": " + arg + ". 0 or 1 expected.");
            }
            SpectrumAnnotator.TiesResolution tiesResolution = value ? SpectrumAnnotator.TiesResolution.mostAccurateMz : SpectrumAnnotator.TiesResolution.mostIntense;
            annotationSettings.setTiesResolution(tiesResolution);
        }

        //////////////////////////////////
        // Sequence Matching
        //////////////////////////////////
        SequenceMatchingParameters sequenceMatchingPreferences = identificationParameters.getSequenceMatchingPreferences();
        if (sequenceMatchingPreferences == null) {
            sequenceMatchingPreferences = new SequenceMatchingParameters();
            identificationParameters.setSequenceMatchingPreferences(sequenceMatchingPreferences);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SEQUENCE_INDEX_TYPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SEQUENCE_INDEX_TYPE.id);
            Integer intValue = new Integer(arg);
            PeptideMapperType value = PeptideMapperType.getPeptideMapperType(intValue);
            sequenceMatchingPreferences.setPeptideMapperType(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id);
            Integer intValue = new Integer(arg);
            SequenceMatchingParameters.MatchingType value = SequenceMatchingParameters.MatchingType.getMatchingType(intValue);
            sequenceMatchingPreferences.setSequenceMatchingType(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id);
            Double value = new Double(arg);
            sequenceMatchingPreferences.setLimitX(value);
        }

        //////////////////////////////////
        // Import Filters
        //////////////////////////////////
        PeptideAssumptionFilter peptideAssumptionFilter = identificationParameters.getPeptideAssumptionFilter();
        if (commandLine.hasOption(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id);
            Integer value = new Integer(arg);
            peptideAssumptionFilter.setMinPepLength(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id);
            Integer value = new Integer(arg);
            peptideAssumptionFilter.setMaxPepLength(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.IMPORT_MC_MIN.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_MC_MIN.id);
            Integer value = new Integer(arg);
            peptideAssumptionFilter.setMinMissedCleavages(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.IMPORT_MC_MAX.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_MC_MAX.id);
            Integer value = new Integer(arg);
            peptideAssumptionFilter.setMaxMissedCleavages(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id);
            Double value = new Double(arg);
            peptideAssumptionFilter.setMaxMzDeviation(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id);
            Integer option = new Integer(arg);
            switch (option) {
                case 1:
                    peptideAssumptionFilter.setIsPpm(true);
                    break;
                case 0:
                    peptideAssumptionFilter.setIsPpm(false);
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id + ": " + arg + ". 0 or 1 expected.");
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id + ": " + arg + ". 0 or 1 expected.");
            }
            peptideAssumptionFilter.setRemoveUnknownPTMs(value);
        }

        //////////////////////////////////
        // PTM localization parameters
        //////////////////////////////////
        ModificationLocalizationParameters ptmScoringPreferences = identificationParameters.getPtmScoringPreferences();
        if (ptmScoringPreferences == null) {
            ptmScoringPreferences = new ModificationLocalizationParameters();
            identificationParameters.setPtmScoringPreferences(ptmScoringPreferences);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PTM_SCORE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PTM_SCORE.id);
            Integer intValue = new Integer(arg);
            PtmScore ptmScore = PtmScore.getScore(intValue);
            if (ptmScore == PtmScore.None) {
                ptmScoringPreferences.setProbabilisticScoreCalculation(false);
            } else {
                ptmScoringPreferences.setProbabilisticScoreCalculation(true);
                ptmScoringPreferences.setSelectedProbabilisticScore(ptmScore);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PTM_THRESHOLD.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PTM_THRESHOLD.id);
            Double value = new Double(arg);
            ptmScoringPreferences.setEstimateFlr(false);
            ptmScoringPreferences.setProbabilisticScoreThreshold(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id + ": " + arg + ". 0 or 1 expected.");
            }
            ptmScoringPreferences.setProbabilisticScoreNeutralLosses(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id);
            Integer intValue = new Integer(arg);
            SequenceMatchingParameters.MatchingType value = SequenceMatchingParameters.MatchingType.getMatchingType(intValue);
            SequenceMatchingParameters ptmSequenceMatchingPreferences = new SequenceMatchingParameters();
            ptmSequenceMatchingPreferences.setLimitX(sequenceMatchingPreferences.getLimitX());
            ptmSequenceMatchingPreferences.setSequenceMatchingType(value);
            ptmScoringPreferences.setSequenceMatchingPreferences(ptmSequenceMatchingPreferences);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PTM_ALIGNMENT.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PTM_ALIGNMENT.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.PTM_ALIGNMENT.id + ": " + arg + ". 0 or 1 expected.");
            }
            ptmScoringPreferences.setAlignNonConfidentPTMs(value);
        }

        //////////////////////////////////
        // Protein inference parameters
        //////////////////////////////////
        ProteinInferenceParameters proteinInferencePreferences = identificationParameters.getProteinInferencePreferences();
        if (commandLine.hasOption(IdentificationParametersCLIParams.DB_PI.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DB_PI.id);
            File fastaFile = new File(arg);
            proteinInferencePreferences.setProteinSequenceDatabase(fastaFile);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.SIMPLIFY_GOUPS.id + ": " + arg + ". 0 or 1 expected.");
            }
            proteinInferencePreferences.setSimplifyGroups(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.SIMPLIFY_GOUPS_SCORE.id + ": " + arg + ". 0 or 1 expected.");
            }
            proteinInferencePreferences.setSimplifyGroupsScore(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.SIMPLIFY_GOUPS_ENZYMATICITY.id + ": " + arg + ". 0 or 1 expected.");
            }
            proteinInferencePreferences.setSimplifyGroupsEnzymaticity(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.SIMPLIFY_GOUPS_EVIDENCE.id + ": " + arg + ". 0 or 1 expected.");
            }
            proteinInferencePreferences.setSimplifyGroupsEvidence(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.SIMPLIFY_GOUPS_UNCHARACTERIZED.id + ": " + arg + ". 0 or 1 expected.");
            }
            proteinInferencePreferences.setSimplifyGroupsUncharacterized(value);
        }

        //////////////////////////////////
        // Validation parameters
        //////////////////////////////////
        IdMatchValidationParameters idMatchValidationPreferences = identificationParameters.getIdValidationPreferences();
        if (idMatchValidationPreferences == null) {
            idMatchValidationPreferences = new IdMatchValidationParameters();
            identificationParameters.setIdValidationPreferences(idMatchValidationPreferences);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PSM_FDR.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PSM_FDR.id);
            Double value = new Double(arg);
            idMatchValidationPreferences.setDefaultPsmFDR(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PEPTIDE_FDR.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PEPTIDE_FDR.id);
            Double value = new Double(arg);
            idMatchValidationPreferences.setDefaultPeptideFDR(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PROTEIN_FDR.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PROTEIN_FDR.id);
            Double value = new Double(arg);
            idMatchValidationPreferences.setDefaultProteinFDR(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SEPARATE_PSMs.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SEPARATE_PSMs.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.SEPARATE_PSMs.id + ": " + arg + ". 0 or 1 expected.");
            }
            idMatchValidationPreferences.setSeparatePsms(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.SEPARATE_PEPTIDES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.SEPARATE_PEPTIDES.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.SEPARATE_PEPTIDES.id + ": " + arg + ". 0 or 1 expected.");
            }
            idMatchValidationPreferences.setSeparatePeptides(value);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MERGE_SUBGROUPS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MERGE_SUBGROUPS.id);
            Integer intValue = new Integer(arg);
            boolean value;
            switch (intValue) {
                case 1:
                    value = true;
                    break;
                case 0:
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect value for parameter " + IdentificationParametersCLIParams.MERGE_SUBGROUPS.id + ": " + arg + ". 0 or 1 expected.");
            }
            idMatchValidationPreferences.setMergeSmallSubgroups(value);
        }

        //////////////////////////////////
        // Fraction parameters
        //////////////////////////////////
        FractionParameters fractionSettings = identificationParameters.getFractionSettings();
        if (fractionSettings == null) {
            fractionSettings = new FractionParameters();
            identificationParameters.setFractionSettings(fractionSettings);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id);
            Double value = new Double(arg);
            fractionSettings.setProteinConfidenceMwPlots(value);
        }
    }

    /**
     * Sets the identification parameters.
     *
     * @param identificationParameters the identification parameters
     */
    public void setIdentificationParameters(IdentificationParameters identificationParameters) {
        this.identificationParameters = identificationParameters;
    }

    /**
     * Returns the identification parameters.
     *
     * @return the identification parameters
     */
    public IdentificationParameters getIdentificationParameters() {
        return identificationParameters;
    }

    /**
     * Returns the input parameters file.
     *
     * @return the input parameters file
     */
    public File getInputFile() {
        return inputFile;
    }

    /**
     * Returns the file where to save the identification parameters.
     *
     * @return the file where to save the identification parameters
     */
    public File getDestinationFile() {
        return destinationFile;
    }

    /**
     * Indicates whether the modifications should be printed.
     *
     * @return true if the modifications should be printed
     */
    public Boolean isListMods() {
        return listMods;
    }

    /**
     * Indicates whether the enzymes should be printed.
     *
     * @return true if the enzymes should be printed
     */
    public Boolean isListEnzymes() {
        return listEnzymes;
    }

    /**
     * Verifies that modifications are correctly recognized.
     *
     * @param aLine the command line to validate
     * @return true if the startup was valid
     * @throws IOException if an IOException occurs
     */
    public static boolean isValidModifications(CommandLine aLine) throws IOException {
        boolean error = false;
        if (aLine.hasOption(IdentificationParametersCLIParams.FIXED_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FIXED_MODS.id);
            try {
                ArrayList<String> args = CommandLineUtils.splitInput(arg);
                for (String ptmName : args) {
                    Modification ptm = ModificationFactory.getInstance().getModification(ptmName);
                    if (ptm == null) {
                        throw new IllegalArgumentException("PTM " + ptmName + " not found.");
                    }
                }
            } catch (IllegalArgumentException e) {
                if (!error) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the fixed modifications:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                }
                error = true;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.VARIABLE_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.VARIABLE_MODS.id);
            try {
                ArrayList<String> args = CommandLineUtils.splitInput(arg);
                for (String ptmName : args) {
                    Modification ptm = ModificationFactory.getInstance().getModification(ptmName);
                    if (ptm == null) {
                        throw new IllegalArgumentException("PTM " + ptmName + " not found.");
                    }
                }
            } catch (IllegalArgumentException e) {
                if (!error) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the variable modifications:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                }
                error = true;
            }
        }
        return !error;
    }
}
