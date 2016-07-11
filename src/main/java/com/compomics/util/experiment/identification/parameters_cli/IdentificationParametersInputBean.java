package com.compomics.util.experiment.identification.parameters_cli;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.DirecTagParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsAmandaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsgfParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MyriMatchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PNovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PepnovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.TideParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.XtandemParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters.AndromedaDecoyMode;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters.CometOutputFormat;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.NovorParameters;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapperType;
import com.compomics.util.experiment.identification.ptm.PtmScore;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.massspectrometry.FragmentationMethod;
import com.compomics.util.preferences.FractionSettings;
import com.compomics.util.preferences.GenePreferences;
import com.compomics.util.preferences.IdMatchValidationPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.PTMScoringPreferences;
import com.compomics.util.preferences.ProteinInferencePreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences.MatchingType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;

/**
 * This class contains the parses parameters from a command line and stores them
 * in a SearchParameters object.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IdentificationParametersInputBean {

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
     * If true the modifications will be listed on the screen
     */
    private Boolean listMods = false;
    /**
     * The compomics PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
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
        if (commandLine.hasOption(IdentificationParametersCLIParams.ENZYME.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.ENZYME.id);
            Enzyme option = enzymeFactory.getEnzyme(arg);
            searchParameters.setEnzyme(option);
        } else if (searchParameters.getEnzyme() == null) {
            Enzyme option = enzymeFactory.getEnzyme("Trypsin"); // no enzyme given, default to trypsin
            searchParameters.setEnzyme(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.DB.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.DB.id);
            File fastaFile = new File(arg);
            searchParameters.setFastaFile(fastaFile);

            // also update the protein inference database if that option is not set
            if (identificationParameters != null && !commandLine.hasOption(IdentificationParametersCLIParams.DB_PI.id)) {
                ProteinInferencePreferences proteinInferencePreferences = identificationParameters.getProteinInferencePreferences();
                proteinInferencePreferences.setProteinSequenceDatabase(fastaFile);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MC.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MC.id);
            Integer option = new Integer(arg);
            searchParameters.setnMissedCleavages(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.FI.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.FI.id);
            searchParameters.setIonSearched1(arg);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.RI.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.RI.id);
            searchParameters.setIonSearched2(arg);
        }

        if (commandLine.hasOption(IdentificationParametersCLIParams.MIN_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MIN_CHARGE.id);
            Integer option = new Integer(arg);
            searchParameters.setMinChargeSearched(new Charge(Charge.PLUS, option));
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MAX_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MAX_CHARGE.id);
            Integer option = new Integer(arg);
            searchParameters.setMaxChargeSearched(new Charge(Charge.PLUS, option));
        }

        PtmSettings ptmSettings = searchParameters.getPtmSettings();
        if (ptmSettings == null) {
            ptmSettings = new PtmSettings();
            searchParameters.setPtmSettings(ptmSettings);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.FIXED_MODS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.FIXED_MODS.id);
            ptmSettings.clearFixedModifications();
            ArrayList<String> args = CommandLineUtils.splitInput(arg);
            for (String ptmName : args) {
                PTM modification = ptmFactory.getPTM(ptmName);
                ptmSettings.addFixedModification(modification);
            }
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.VARIABLE_MODS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.VARIABLE_MODS.id);
            ptmSettings.clearVariableModifications();
            ArrayList<String> args = CommandLineUtils.splitInput(arg);
            for (String ptmName : args) {
                PTM modification = ptmFactory.getPTM(ptmName);
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
            Integer option = new Integer(arg);
            omssaParameters.setSingleChargeWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setDoubleChargeWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setnPeaksInSingleChargeWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setnPeaksInDoubleChargeWindow(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setnAnnotatedMostIntensePeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setMinAnnotatedPeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setMinPeaks(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_METHIONINE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_METHIONINE.id);
            Integer option = new Integer(arg);
            omssaParameters.setCleaveNterMethionine(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxMzLadders(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxFragmentCharge(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id);
            Double option = new Double(arg);
            omssaParameters.setFractionOfPeaksForChargeEstimation(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id);
            Integer option = new Integer(arg);
            omssaParameters.setDetermineChargePlusOneAlgorithmically(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id);
            Integer option = new Integer(arg);
            omssaParameters.setSearchPositiveIons(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id);
            Integer option = new Integer(arg);
            omssaParameters.setMinPrecPerSpectrum(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id);
            Integer option = new Integer(arg);
            omssaParameters.setSearchForwardFragmentFirst(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id);
            Integer option = new Integer(arg);
            omssaParameters.setSearchRewindFragments(option == 1);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxFragmentPerSeries(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id);
            Integer option = new Integer(arg);
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
            Integer option = new Integer(arg);
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
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.MSGF_PTMS.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PTMS.id);
            Integer option = new Integer(arg);
            msgfParameters.setNumberOfPtmsPerPeptide(option);
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
        if (commandLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setLowMemoryMode(option == 1);
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
            tideParameters.setMaxVariablePtmsPerPeptide(option);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id);
            Integer option = new Integer(arg);
            tideParameters.setMaxVariablePtmsPerTypePerPeptide(option);
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
        GenePreferences genePreferences = identificationParameters.getGenePreferences();
        if (genePreferences == null) {
            genePreferences = new GenePreferences();
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
        AnnotationSettings annotationSettings = identificationParameters.getAnnotationPreferences();
        if (annotationSettings == null) {
            annotationSettings = new AnnotationSettings(searchParameters);
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
        if (commandLine.hasOption(IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id)) {
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
            annotationSettings.setHighResolutionAnnotation(value);
        }

        //////////////////////////////////
        // Sequence Matching
        //////////////////////////////////
        SequenceMatchingPreferences sequenceMatchingPreferences = identificationParameters.getSequenceMatchingPreferences();
        if (sequenceMatchingPreferences == null) {
            sequenceMatchingPreferences = new SequenceMatchingPreferences();
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
            SequenceMatchingPreferences.MatchingType value = SequenceMatchingPreferences.MatchingType.getMatchingType(intValue);
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
        PTMScoringPreferences ptmScoringPreferences = identificationParameters.getPtmScoringPreferences();
        if (ptmScoringPreferences == null) {
            ptmScoringPreferences = new PTMScoringPreferences();
            identificationParameters.setPtmScoringPreferences(ptmScoringPreferences);
        }
        if (commandLine.hasOption(IdentificationParametersCLIParams.PTM_SCORE.id)) {
            String arg = commandLine.getOptionValue(IdentificationParametersCLIParams.PTM_SCORE.id);
            Integer intValue = new Integer(arg);
            PtmScore ptmScore = PtmScore.getScore(intValue);
            if (ptmScore == PtmScore.None) {
                ptmScoringPreferences.setProbabilitsticScoreCalculation(false);
            } else {
                ptmScoringPreferences.setProbabilitsticScoreCalculation(true);
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
            SequenceMatchingPreferences.MatchingType value = SequenceMatchingPreferences.MatchingType.getMatchingType(intValue);
            SequenceMatchingPreferences ptmSequenceMatchingPreferences = new SequenceMatchingPreferences();
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
        ProteinInferencePreferences proteinInferencePreferences = identificationParameters.getProteinInferencePreferences();
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
        IdMatchValidationPreferences idMatchValidationPreferences = identificationParameters.getIdValidationPreferences();
        if (idMatchValidationPreferences == null) {
            idMatchValidationPreferences = new IdMatchValidationPreferences();
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
        FractionSettings fractionSettings = identificationParameters.getFractionSettings();
        if (fractionSettings == null) {
            fractionSettings = new FractionSettings();
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
     * Indicates whether the modifications should be printed on the screen.
     *
     * @return true if the modifications should be printed on the screen
     */
    public Boolean isListMods() {
        return listMods;
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
                    PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                    if (ptm == null || ptm == PTMFactory.unknownPTM) {
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
                    PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                    if (ptm == null || ptm == PTMFactory.unknownPTM) {
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
