package com.compomics.util.experiment.identification.search_parameters_cli;

import com.compomics.software.CommandLineUtils;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.DirecTagParameters;
import com.compomics.util.experiment.identification.identification_parameters.MsAmandaParameters;
import com.compomics.util.experiment.identification.identification_parameters.MsgfParameters;
import com.compomics.util.experiment.identification.identification_parameters.MyriMatchParameters;
import com.compomics.util.experiment.identification.identification_parameters.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.PNovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.PepnovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.TideParameters;
import com.compomics.util.experiment.identification.identification_parameters.XtandemParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.preferences.ModificationProfile;
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
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The file where to save the parameters
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
     * Takes all the arguments from a command line.
     *
     * @param aLine the command line
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if aClassNotFoundException
     * ClassNotFoundException occurs
     */
    public IdentificationParametersInputBean(CommandLine aLine) throws FileNotFoundException, IOException, ClassNotFoundException {

        ///////////////////////////////////
        // General parameters
        ///////////////////////////////////
        if (aLine.hasOption(IdentificationParametersCLIParams.MODS.id)) {
            listMods = true;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OUTPUT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OUTPUT.id);
            if (!arg.endsWith(".parameters")) {
                arg += ".parameters";
            }
            destinationFile = new File(arg);
        }

        searchParameters = new SearchParameters();

        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_PPM.id);
            Integer option = new Integer(arg);
            if (option == 1) {
                searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
            } else {
                searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_PPM.id);
            Integer option = new Integer(arg);
            if (option == 1) {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.PPM);
            } else {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_TOL.id);
            Double option = new Double(arg);
            searchParameters.setPrecursorAccuracy(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_TOL.id);
            Double option = new Double(arg);
            searchParameters.setFragmentIonAccuracy(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ENZYME.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ENZYME.id);
            Enzyme option = enzymeFactory.getEnzyme(arg);
            searchParameters.setEnzyme(option);
        } else {
            Enzyme option = enzymeFactory.getEnzyme("Trypsin"); // no enzyme given, default to Trypsin
            searchParameters.setEnzyme(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DB.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DB.id);
            File fastaFile = new File(arg);
            searchParameters.setFastaFile(fastaFile);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MC.id);
            Integer option = new Integer(arg);
            searchParameters.setnMissedCleavages(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FI.id);
            searchParameters.setIonSearched1(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.RI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.RI.id);
            searchParameters.setIonSearched2(arg);
        }

        if (aLine.hasOption(IdentificationParametersCLIParams.MIN_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MIN_CHARGE.id);
            Integer option = new Integer(arg);
            searchParameters.setMinChargeSearched(new Charge(Charge.PLUS, option));
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MAX_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MAX_CHARGE.id);
            Integer option = new Integer(arg);
            searchParameters.setMaxChargeSearched(new Charge(Charge.PLUS, option));
        }

        ModificationProfile modificationProfile = new ModificationProfile();
        if (aLine.hasOption(IdentificationParametersCLIParams.FIXED_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FIXED_MODS.id);
            ArrayList<String> args = CommandLineUtils.splitInput(arg);
            for (String ptmName : args) {
                PTM modification = ptmFactory.getPTM(ptmName);
                modificationProfile.addFixedModification(modification);
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.VARIABLE_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.VARIABLE_MODS.id);
            ArrayList<String> args = CommandLineUtils.splitInput(arg);
            for (String ptmName : args) {
                PTM modification = ptmFactory.getPTM(ptmName);
                modificationProfile.addVariableModification(modification);
            }
        }
        searchParameters.setModificationProfile(modificationProfile);

        ///////////////////////////////////
        // OMSSA parameters
        ///////////////////////////////////
        OmssaParameters omssaParameters = new OmssaParameters();
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id);
            Integer option = new Integer(arg);
            omssaParameters.setRemovePrecursor(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id);
            Integer option = new Integer(arg);
            omssaParameters.setScalePrecursor(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id);
            Integer option = new Integer(arg);
            omssaParameters.setEstimateCharge(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id);
            Double option = new Double(arg);
            omssaParameters.setMaxEValue(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setHitListLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setMinPeptideLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxPeptideLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORMAT.id);
            Integer option = new Integer(arg);
            omssaParameters.setSelectedOutput(OmssaParameters.getOmssaOutputTypes()[option]);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id);
            Integer option = new Integer(arg);
            omssaParameters.setMemoryMappedSequenceLibraries(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ISOTOPES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ISOTOPES.id);
            Integer option = new Integer(arg);
            omssaParameters.setNumberOfItotopicPeaks(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_NEUTRON.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_NEUTRON.id);
            Double option = new Double(arg);
            omssaParameters.setNeutronThreshold(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id);
            Double option = new Double(arg);
            omssaParameters.setLowIntensityCutOff(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id);
            Double option = new Double(arg);
            omssaParameters.setHighIntensityCutOff(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id);
            Double option = new Double(arg);
            omssaParameters.setIntensityCutOffIncrement(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setSingleChargeWindow(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id);
            Integer option = new Integer(arg);
            omssaParameters.setDoubleChargeWindow(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setnPeaksInSingleChargeWindow(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setnPeaksInDoubleChargeWindow(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setnAnnotatedMostIntensePeaks(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setMinAnnotatedPeaks(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id);
            Integer option = new Integer(arg);
            omssaParameters.setMinPeaks(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_METHIONINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_METHIONINE.id);
            Integer option = new Integer(arg);
            omssaParameters.setCleaveNterMethionine(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxMzLadders(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxFragmentCharge(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id);
            Double option = new Double(arg);
            omssaParameters.setFractionOfPeaksForChargeEstimation(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id);
            Integer option = new Integer(arg);
            omssaParameters.setDetermineChargePlusOneAlgorithmically(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id);
            Integer option = new Integer(arg);
            omssaParameters.setSearchPositiveIons(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id);
            Integer option = new Integer(arg);
            omssaParameters.setMinPrecPerSpectrum(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id);
            Integer option = new Integer(arg);
            omssaParameters.setSearchForwardFragmentFirst(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id);
            Integer option = new Integer(arg);
            omssaParameters.setSearchRewindFragments(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxFragmentPerSeries(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id);
            Integer option = new Integer(arg);
            omssaParameters.setUseCorrelationCorrectionScore(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id);
            Double option = new Double(arg);
            omssaParameters.setConsecutiveIonProbability(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id);
            Double option = new Double(arg);
            omssaParameters.setIterativeSequenceEvalue(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id);
            Double option = new Double(arg);
            omssaParameters.setIterativeSpectrumEvalue(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id);
            Double option = new Double(arg);
            omssaParameters.setIterativeReplaceEvalue(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id);
            Integer option = new Integer(arg);
            omssaParameters.setMaxFragmentPerSeries(option);
        }
        searchParameters.setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), omssaParameters);
        ptmFactory.setSearchedOMSSAIndexes(searchParameters.getModificationProfile());

        ///////////////////////////////////
        // X!Tandem parameters
        ///////////////////////////////////
        XtandemParameters xtandemParameters = new XtandemParameters();
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id);
            Double option = new Double(arg);
            xtandemParameters.setDynamicRange(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setnPeaks(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id);
            Double option = new Double(arg);
            xtandemParameters.setMinFragmentMz(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setMinPeaksPerSpectrum(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id);
            Integer option = new Integer(arg);
            xtandemParameters.setUseNoiseSuppression(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id);
            Double option = new Double(arg);
            xtandemParameters.setMinPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id);
            Integer option = new Integer(arg);
            xtandemParameters.setProteinQuickAcetyl(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id);
            Integer option = new Integer(arg);
            xtandemParameters.setQuickPyrolidone(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setStpBias(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefine(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id);
            Double option = new Double(arg);
            xtandemParameters.setMaximumExpectationValueRefinement(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefineUnanticipatedCleavages(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefineSemi(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id);
            Integer option = new Integer(arg);
            xtandemParameters.setPotentialModificationsForFullRefinment(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefinePointMutations(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefineSnaps(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setRefineSpectrumSynthesis(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_EVALUE.id);
            Double option = new Double(arg);
            xtandemParameters.setMaxEValue(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id);
            Integer option = new Integer(arg);
            xtandemParameters.setOutputProteins(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id);
            Integer option = new Integer(arg);
            xtandemParameters.setOutputSequences(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id);
            Integer option = new Integer(arg);
            xtandemParameters.setOutputSpectra(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_SKYLINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_SKYLINE.id);
            xtandemParameters.setSkylinePath(arg);
        }
        searchParameters.setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), xtandemParameters);

        ///////////////////////////////////
        // MS-GF+ parameters
        ///////////////////////////////////
        MsgfParameters msgfParameters = new MsgfParameters();
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_DECOY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_DECOY.id);
            Integer option = new Integer(arg);
            msgfParameters.setSearchDecoyDatabase(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id);
            Integer instrumentID = new Integer(arg);
            msgfParameters.setInstrumentID(instrumentID); // @TODO: check for valid index!!
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id);
            Integer option = new Integer(arg);
            msgfParameters.setFragmentationType(option); // @TODO: check for valid index!!
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_PROTOCOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PROTOCOL.id);
            Integer option = new Integer(arg);
            msgfParameters.setProtocol(option); // @TODO: check for valid index!!
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            msgfParameters.setMinPeptideLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            msgfParameters.setMaxPeptideLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id);
            Integer option = new Integer(arg);
            msgfParameters.setNumberOfSpectrumMarches(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id);
            Integer option = new Integer(arg);
            msgfParameters.setAdditionalOutput(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.id);
            Integer option = new Integer(arg);
            msgfParameters.setLowerIsotopeErrorRange(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.id);
            Integer option = new Integer(arg);
            msgfParameters.setUpperIsotopeErrorRange(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_TERMINI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_TERMINI.id);
            Integer option = new Integer(arg);
            msgfParameters.setNumberTolerableTermini(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_PTMS.id);
            Integer option = new Integer(arg);
            msgfParameters.setNumberOfPtmsPerPeptide(option);
        }
        searchParameters.setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), msgfParameters);

        ///////////////////////////////////
        // MyriMatch parameters
        ///////////////////////////////////
        MyriMatchParameters myriMatchParameters = new MyriMatchParameters();

        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMinPeptideLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMaxPeptideLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id);
            Double option = new Double(arg);
            myriMatchParameters.setMinPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id);
            Double option = new Double(arg);
            myriMatchParameters.setMaxPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setLowerIsotopeCorrectionRange(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setUpperIsotopeCorrectionRange(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_NUM_MATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_NUM_MATCHES.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setNumberOfSpectrumMatches(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMaxDynamicMods(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.id);
            myriMatchParameters.setFragmentationRule(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMinTerminiCleavages(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setUseSmartPlusThreeModel(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setComputeXCorr(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id);
            Double option = new Double(arg);
            myriMatchParameters.setTicCutoffPercentage(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setNumIntensityClasses(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setClassSizeMultiplier(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setNumberOfBatches(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id);
            Integer option = new Integer(arg);
            myriMatchParameters.setMaxPeakCount(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_OUTPUT_FORMAT.id);
            myriMatchParameters.setOutputFormat(arg);
        }

        searchParameters.setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), myriMatchParameters);

        ///////////////////////////////////
        // MS Amanda parameters
        ///////////////////////////////////
        MsAmandaParameters msAmandaParameters = new MsAmandaParameters();
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setGenerateDecoyDatabase(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id);
            msAmandaParameters.setInstrumentID(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMaxRank(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setMonoIsotopic(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id);
            Integer option = new Integer(arg);
            msAmandaParameters.setLowMemoryMode(option == 1);
        }

        searchParameters.setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), msAmandaParameters);

        ///////////////////////////////////
        // Comet parameters
        ///////////////////////////////////
        CometParameters cometParameters = new CometParameters();
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id);
            Integer option = new Integer(arg);
            cometParameters.setNumberOfSpectrumMatches(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_PTMS.id);
            Integer option = new Integer(arg);
            cometParameters.setMaxVariableMods(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REQ_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REQ_PTMS.id);
            Integer option = new Integer(arg);
            cometParameters.setRequireVariableMods(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id);
            Integer option = new Integer(arg);
            cometParameters.setMinPeaks(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id);
            Double option = new Double(arg);
            cometParameters.setMinPeakIntensity(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id);
            Integer option = new Integer(arg);
            cometParameters.setRemovePrecursor(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id);
            Double option = new Double(arg);
            cometParameters.setRemovePrecursorTolerance(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id);
            Double option = new Double(arg);
            cometParameters.setLowerClearMzRange(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id);
            Double option = new Double(arg);
            cometParameters.setUpperClearMzRange(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id);
            Integer option = new Integer(arg);
            cometParameters.setEnzymeType(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id);
            Integer option = new Integer(arg);
            cometParameters.setIsotopeCorrection(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id);
            Double option = new Double(arg);
            cometParameters.setMinPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id);
            Double option = new Double(arg);
            cometParameters.setMaxPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id);
            Integer option = new Integer(arg);
            cometParameters.setMaxFragmentCharge(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_REMOVE_METH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_REMOVE_METH.id);
            Integer option = new Integer(arg);
            cometParameters.setRemoveMethionine(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id);
            Integer option = new Integer(arg);
            cometParameters.setBatchSize(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id);
            Integer option = new Integer(arg);
            cometParameters.setTheoreticalFragmentIonsSumOnly(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id);
            Double option = new Double(arg);
            cometParameters.setFragmentBinOffset(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_USE_SPARSE_MATRIX.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_USE_SPARSE_MATRIX.id);
            Integer option = new Integer(arg);
            cometParameters.setUseSparseMatrix(option == 1);
        }

        searchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), cometParameters);

        ///////////////////////////////////
        // Tide parameters
        ///////////////////////////////////
        TideParameters tideParameters = new TideParameters();

        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS.id);
            Integer option = new Integer(arg);
            tideParameters.setMaxVariablePtmsPerPeptide(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id);
            Integer option = new Integer(arg);
            tideParameters.setMaxVariablePtmsPerTypePerPeptide(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            tideParameters.setMinPeptideLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id);
            Integer option = new Integer(arg);
            tideParameters.setMaxPeptideLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id);
            Double option = new Double(arg);
            tideParameters.setMinPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id);
            Double option = new Double(arg);
            tideParameters.setMaxPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id);
            tideParameters.setDecoyFormat(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id);
            tideParameters.setKeepTerminalAminoAcids(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id);
            Integer option = new Integer(arg);
            tideParameters.setDecoySeed(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.id);
            tideParameters.setOutputFolderName(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id);
            Integer option = new Integer(arg);
            tideParameters.setPrintPeptides(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_VERBOSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_VERBOSITY.id);
            Integer option = new Integer(arg);
            tideParameters.setVerbosity(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id);
            Integer option = new Integer(arg);
            tideParameters.setMonoisotopicPrecursor(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id);
            Integer option = new Integer(arg);
            tideParameters.setClipNtermMethionine(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id);
            tideParameters.setDigestionType(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id);
            Integer option = new Integer(arg);
            tideParameters.setComputeSpScore(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id);
            Integer option = new Integer(arg);
            tideParameters.setNumberOfSpectrumMatches(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id);
            Integer option = new Integer(arg);
            tideParameters.setComputeExactPValues(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id);
            Double option = new Double(arg);
            tideParameters.setMinSpectrumMz(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id);
            Double option = new Double(arg);
            tideParameters.setMaxSpectrumMz(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id);
            Integer option = new Integer(arg);
            tideParameters.setMinSpectrumPeaks(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id);
            tideParameters.setSpectrumCharges(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id);
            Integer option = new Integer(arg);
            tideParameters.setRemovePrecursor(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id);
            Double option = new Double(arg);
            tideParameters.setRemovePrecursorTolerance(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id);
            Integer option = new Integer(arg);
            tideParameters.setPrintProgressIndicatorSize(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id);
            Integer option = new Integer(arg);
            tideParameters.setUseFlankingPeaks(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id);
            Integer option = new Integer(arg);
            tideParameters.setUseNeutralLossPeaks(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id);
            Double option = new Double(arg);
            tideParameters.setMzBinWidth(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id);
            Double option = new Double(arg);
            tideParameters.setMzBinOffset(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_CONCAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_CONCAT.id);
            Integer option = new Integer(arg);
            tideParameters.setConcatenatTargetDecoy(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.id);
            tideParameters.setStoreSpectraFileName(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id);
            Integer option = new Integer(arg);
            tideParameters.setTextOutput(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id);
            Integer option = new Integer(arg);
            tideParameters.setSqtOutput(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id);
            Integer option = new Integer(arg);
            tideParameters.setPepXmlOutput(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id);
            Integer option = new Integer(arg);
            tideParameters.setMzidOutput(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id);
            Integer option = new Integer(arg);
            tideParameters.setPinOutput(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id);
            Integer option = new Integer(arg);
            tideParameters.setRemoveTempFolders(option == 1);
        }

        searchParameters.setIdentificationAlgorithmParameter(Advocate.tide.getIndex(), tideParameters);

        ///////////////////////////////////
        // Andromeda parameters
        ///////////////////////////////////
        AndromedaParameters andromedaParameters = new AndromedaParameters();
        // @TODO: implement me!!
        searchParameters.setIdentificationAlgorithmParameter(Advocate.andromeda.getIndex(), andromedaParameters);

        ///////////////////////////////////
        // PepNovo+ parameters
        ///////////////////////////////////
        PepnovoParameters pepnovoParameters = new PepnovoParameters();
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setHitListLength(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setEstimateCharge(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setCorrectPrecursorMass(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setDiscardLowQualitySpectra(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setDiscardLowQualitySpectra(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id);
            pepnovoParameters.setFragmentationModel(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id);
            Integer option = new Integer(arg);
            pepnovoParameters.setGenerateQuery(option == 1);
        }
        searchParameters.setIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex(), pepnovoParameters);

        ///////////////////////////////////
        // DirecTag parameters
        ///////////////////////////////////
        DirecTagParameters direcTagParameters = new DirecTagParameters();
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_TIC_CUTOFF_PERCENTAGE.id);
            Integer option = new Integer(arg);
            direcTagParameters.setTicCutoffPercentage(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_PEAK_COUNT.id);
            Integer option = new Integer(arg);
            direcTagParameters.setMaxPeakCount(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_NUM_INTENSITY_CLASSES.id);
            Integer option = new Integer(arg);
            direcTagParameters.setNumIntensityClasses(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_ADJUST_PRECURSOR_MASS.id);
            Integer option = new Integer(arg);
            direcTagParameters.setAdjustPrecursorMass(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MIN_PRECUSOR_ADJUSTMENT.id);
            Double option = new Double(arg);
            direcTagParameters.setMinPrecursorAdjustment(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_PRECUSOR_ADJUSTMENT.id);
            Double option = new Double(arg);
            direcTagParameters.setMaxPrecursorAdjustment(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_PRECUSOR_ADJUSTMENT_STEP.id);
            Double option = new Double(arg);
            direcTagParameters.setPrecursorAdjustmentStep(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_NUM_CHARGE_STATES.id);
            Integer option = new Integer(arg);
            direcTagParameters.setNumChargeStates(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_OUTPUT_SUFFIX.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_OUTPUT_SUFFIX.id);
            direcTagParameters.setOutputSuffix(arg);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_USE_CHARGE_STATE_FROM_MS.id);
            Integer option = new Integer(arg);
            direcTagParameters.setUseChargeStateFromMS(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_DUPLICATE_SPECTRA.id);
            Integer option = new Integer(arg);
            direcTagParameters.setDuplicateSpectra(option == 1);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_DEISOTOPING_MODE.id);
            Integer option = new Integer(arg);
            direcTagParameters.setDeisotopingMode(option); // @TODO: check for valid values!!!
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_ISOTOPE_MZ_TOLERANCE.id);
            Double option = new Double(arg);
            direcTagParameters.setIsotopeMzTolerance(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_MZ_TOLERANCE.id);
            Double option = new Double(arg);
            direcTagParameters.setComplementMzTolerance(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_TAG_LENGTH.id);
            Integer option = new Integer(arg);
            direcTagParameters.setTagLength(option); // @TODO: check for valid values!!!
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_DYNAMIC_MODS.id);
            Integer option = new Integer(arg);
            direcTagParameters.setMaxDynamicMods(option); // @TODO: check for valid values!!!
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MAX_TAG_COUNT.id);
            Integer option = new Integer(arg);
            direcTagParameters.setMaxTagCount(option); // @TODO: check for valid values!!!
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_INTENSITY_SCORE_WEIGHT.id);
            Double option = new Double(arg);
            direcTagParameters.setIntensityScoreWeight(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT.id);
            Double option = new Double(arg);
            direcTagParameters.setMzFidelityScoreWeight(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DIRECTAG_COMPLEMENT_SCORE_WEIGHT.id);
            Double option = new Double(arg);
            direcTagParameters.setComplementScoreWeight(option);
        }

        searchParameters.setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), direcTagParameters);

        ///////////////////////////////////
        // pNovo+ parameters
        ///////////////////////////////////
        PNovoParameters pNovoParameters = new PNovoParameters();
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_NUMBER_OF_PEPTIDES.id);
            Integer option = new Integer(arg);
            pNovoParameters.setNumberOfPeptides(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_LOWER_PRECURSOR_MASS.id);
            Integer option = new Integer(arg);
            pNovoParameters.setLowerPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_UPPER_PRECURSOR_MASS.id);
            Integer option = new Integer(arg);
            pNovoParameters.setUpperPrecursorMass(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PNOVO_ACTIVATION_TYPE.id);
            pNovoParameters.setActicationType(arg);
        }

        searchParameters.setIdentificationAlgorithmParameter(Advocate.pNovo.getIndex(), pNovoParameters);
    }

    /**
     * Returns the search parameters.
     *
     * @return the search parameters
     */
    public SearchParameters getSearchParameters() {
        return searchParameters;
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
            } catch (Exception e) {
                if (!error) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the fixed modifications:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                }
                e.printStackTrace();
                error = true;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.VARIABLE_MODS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.VARIABLE_MODS.id);
            try {
                ArrayList<String> args = CommandLineUtils.splitInput(arg);
                for (String ptmName : args) {
                    PTM ptm = PTMFactory.getInstance().getPTM(ptmName);
                    if (ptm == null) {
                        throw new IllegalArgumentException("PTM " + ptmName + " not found.");
                    }
                }
            } catch (Exception e) {
                if (!error) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the variable modifications:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                }
                e.printStackTrace();
                error = true;
            }
        }
        return !error;
    }

    /**
     * Verifies the command line start parameters.
     *
     * @param aLine the command line to validate
     * @return true if the startup was valid
     * @throws IOException if an IOException occurs
     */
    public static boolean isValidStartup(CommandLine aLine) throws IOException {

        if (aLine.getOptions().length == 0) {
            return false;
        }

        //*************************
        // Default options
        //*************************
        if (aLine.hasOption(IdentificationParametersCLIParams.MODS.id)) {
            return true;
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_PPM.id);
            List<String> supportedInput = Arrays.asList("1", "2");
            if (!isInList(IdentificationParametersCLIParams.PREC_PPM.id, arg, supportedInput)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_PPM.id);
            List<String> supportedInput = Arrays.asList("1", "2");
            if (!isInList(IdentificationParametersCLIParams.FRAG_PPM.id, arg, supportedInput)) {
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
                System.out.println(System.getProperty("line.separator")
                        + "Enzyme " + arg + " not recognized."
                        + System.getProperty("line.separator"));
                return false;
            }
        }
        if (!aLine.hasOption(IdentificationParametersCLIParams.OUTPUT.id) || aLine.getOptionValue(IdentificationParametersCLIParams.OUTPUT.id).equals("")) {
            System.out.println(System.getProperty("line.separator")
                    + "No output file specified!"
                    + System.getProperty("line.separator"));
            return false;
        }
        if (!aLine.hasOption(IdentificationParametersCLIParams.DB.id) || aLine.getOptionValue(IdentificationParametersCLIParams.DB.id).equals("")) {
            System.out.println(System.getProperty("line.separator")
                    + "No database specified!"
                    + System.getProperty("line.separator"));
            return false;
        } else {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DB.id);
            File fastaFile = new File(arg);
            if (!fastaFile.exists()) {
                System.out.println(System.getProperty("line.separator")
                        + "Database not found."
                        + System.getProperty("line.separator"));
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

        //*************************
        // OMSSA options
        //*************************
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
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ISOTOPES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ISOTOPES.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.OMSSA_ISOTOPES.id, arg, true)) {
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
            if (!isPositiveDouble(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id, arg, false)) {
                return false;
            }
        }

        //*************************
        // X!Tandem options
        //*************************
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

        //*************************
        // PepNovo+ options
        //*************************
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
            List<String> supportedInput = Arrays.asList("CID_IT_TRYP"); // @TODO: support more models??
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

        //*************************
        // MS-GF+ options
        //*************************
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
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.id, arg, true)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.id);
            if (!isPositiveInteger(IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.id, arg, true)) {
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

        //*************************
        // MS Amanda options
        //*************************
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id)) {
            // @TODO: add test for instument type?
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

        //*************************
        // DirecTag options
        //*************************
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
            // @TODO: add test?
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

        //*************************
        // MyriMatch options
        //*************************
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
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.id);
            if (!isInteger(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.id, arg)) {
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.id);
            if (!isInteger(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.id, arg)) {
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
            // @TODO: add test..?
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

        //*************************
        // Comet options
        //*************************
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
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_USE_SPARSE_MATRIX.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_USE_SPARSE_MATRIX.id);
            if (!isBooleanInput(IdentificationParametersCLIParams.COMET_USE_SPARSE_MATRIX.id, arg)) {
                return false;
            }
        }

        //*************************
        // Tide options
        //*************************
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
            // @TODO: add test?
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
            // @TODO: add test?
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

        //*************************
        // PNovo+ options
        //*************************
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

        return true;
    }

    /**
     * Returns true if the argument can be parsed as a positive double value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param allowZero if true, zero values are allowed
     * @return true if the argument can be parsed as a positive double value
     */
    private static boolean isPositiveDouble(String argType, String arg, boolean allowZero) {

        boolean valid = true;

        try {
            double value = new Double(arg);
            if (allowZero) {
                if (value < 0) {
                    System.out.println(System.getProperty("line.separator")
                            + "Error parsing the " + argType + " option: Negative value found."
                            + System.getProperty("line.separator"));
                    valid = false;
                }
            } else {
                if (value <= 0) {
                    System.out.println(System.getProperty("line.separator")
                            + "Error parsing the " + argType + " option: Negative or zero value found."
                            + System.getProperty("line.separator"));
                    valid = false;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: Not a floating value!"
                    + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: " + e.getLocalizedMessage()
                    + System.getProperty("line.separator"));
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
    private static boolean isPositiveInteger(String argType, String arg, boolean allowZero) {

        boolean valid = true;

        try {
            int value = new Integer(arg);
            if (allowZero) {
                if (value < 0) {
                    System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative value found."
                            + System.getProperty("line.separator"));
                    valid = false;
                }
            } else {
                if (value <= 0) {
                    System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative or zero value found."
                            + System.getProperty("line.separator"));
                    valid = false;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: Not an integer value!"
                    + System.getProperty("line.separator"));
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
    private static boolean isInteger(String argType, String arg) {

        boolean valid = true;

        try {
            new Integer(arg);
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: Not an integer value!"
                    + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }

        return valid;
    }

    /**
     * Returns true if the argument can be parsed as a double value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true if the argument can be parsed as a double value
     */
    private static boolean isDouble(String argType, String arg) {

        boolean valid = true;

        try {
            new Double(arg);
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: Not a floating value!"
                    + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
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
    private static boolean isBooleanInput(String argType, String arg) {

        boolean valid = true;

        try {
            int value = new Integer(arg);
            if (value != 0 && value != 1) {
                System.out.println(System.getProperty("line.separator")
                        + "Error parsing the " + argType + " option: Found " + value + " where 0 or 1 was expected."
                        + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: Found " + arg + " where 0 or 1 was expected."
                    + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: " + e.getLocalizedMessage()
                    + System.getProperty("line.separator"));
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
    private static boolean isInList(String argType, String arg, List<String> supportedInput) {

        boolean valid = true;

        if (!supportedInput.contains(arg)) {
            valid = false;

            String errorMessage = System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: Found " + arg + ". Supported input: [";

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
     * Returns true if the input is an integer value inside the given range.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param minValue the minimum value allowed
     * @param maxValue the maximum value allowed
     * @return true if the input is an integer value inside the given range
     */
    private static boolean inIntegerRange(String argType, String arg, int minValue, int maxValue) {

        boolean valid = true;

        try {
            int value = new Integer(arg);
            if (value < minValue || value > maxValue) {
                System.out.println(System.getProperty("line.separator")
                        + "Error parsing the " + argType + " option: Not in the range [" + minValue + " - " + maxValue + "]."
                        + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: Not an integer value!"
                    + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: " + e.getLocalizedMessage()
                    + System.getProperty("line.separator"));
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
    private static boolean inDoubleRange(String argType, String arg, double minValue, double maxValue) {

        boolean valid = true;

        try {
            double value = new Double(arg);
            if (value < minValue || value > maxValue) {
                System.out.println(System.getProperty("line.separator")
                        + "Error parsing the " + argType + " option: Not in the range [" + minValue + " - " + maxValue + "]."
                        + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: Not a floating value!"
                    + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator")
                    + "Error parsing the " + argType + " option: " + e.getLocalizedMessage()
                    + System.getProperty("line.separator"));
            valid = false;
        }

        return valid;
    }
}
