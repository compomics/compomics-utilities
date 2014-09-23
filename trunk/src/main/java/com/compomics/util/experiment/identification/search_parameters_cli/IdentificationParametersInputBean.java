package com.compomics.util.experiment.identification.search_parameters_cli;

import com.compomics.software.CommandLineUtils;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.DirecTagParameters;
import com.compomics.util.experiment.identification.identification_parameters.MsAmandaParameters;
import com.compomics.util.experiment.identification.identification_parameters.MsgfParameters;
import com.compomics.util.experiment.identification.identification_parameters.MyriMatchParameters;
import com.compomics.util.experiment.identification.identification_parameters.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.PepnovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.XtandemParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.preferences.ModificationProfile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
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
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_TOLERANCE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_TOLERANCE.id);
            Double option = new Double(arg);
            cometParameters.setFragmentBinTolerance(option);
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.COMET_USE_SPARSE_MATRIX.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.COMET_USE_SPARSE_MATRIX.id);
            Integer option = new Integer(arg);
            cometParameters.setUseSparseMatrix(option == 1);
        }

        searchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), cometParameters);

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
     * @throws IOException
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
     * @throws IOException
     */
    public static boolean isValidStartup(CommandLine aLine) throws IOException {

        if (aLine.getOptions().length == 0) {
            return false;
        }

        if (aLine.hasOption(IdentificationParametersCLIParams.MODS.id)) {
            return true;
        }

        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_PPM.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the ppm/Da precursor ion parameter:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_PPM.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the ppm/Da precursor ion parameter:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PREC_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PREC_TOL.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative value for the precursor tolerance.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the precursor mass tolerance parameter:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_TOL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_TOL.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative value for the precursor tolerance.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the precursor mass tolerance parameter:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.ENZYME.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.ENZYME.id);
            Enzyme option = EnzymeFactory.getInstance().getEnzyme(arg);
            if (option == null) {
                System.out.println(System.getProperty("line.separator") + "Enzyme " + arg + " not recognized."
                        + System.getProperty("line.separator"));
                return false;
            }
        }
        if (!aLine.hasOption(IdentificationParametersCLIParams.DB.id) || aLine.getOptionValue(IdentificationParametersCLIParams.DB.id).equals("")) {
            System.out.println(System.getProperty("line.separator") + "No database specified"
                    + System.getProperty("line.separator"));
            return false;
        } else {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.DB.id);
            File fastaFile = new File(arg);
            if (!fastaFile.exists()) {
                System.out.println(System.getProperty("line.separator") + "Database not found."
                        + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MC.id);
            try {
                int value = new Integer(arg);
                if (value < 0) {
                    throw new IllegalArgumentException("Found negative value for the number of missed cleavages.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the number of missed cleavages:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }

        if (aLine.hasOption(IdentificationParametersCLIParams.MIN_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MIN_CHARGE.id);
            try {
                int value = new Integer(arg);
                if (value < 0) {
                    throw new IllegalArgumentException("Found negative value for the minimal charge.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the minimal charge:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.MAX_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.MAX_CHARGE.id);
            try {
                int value = new Integer(arg);
                if (value < 0) {
                    throw new IllegalArgumentException("Found negative value for the maximal charge.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the minimal charge:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }

        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORMAT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORMAT.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_FORMAT.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ISOTOPES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ISOTOPES.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_ISOTOPES.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_NEUTRON.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_NEUTRON.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_NEUTRON.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_METHIONINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_METHIONINE.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_METHIONINE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }

        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_NPEAKS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id);
            try {
                int value = new Integer(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_REFINE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_EVALUE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_EVALUE.id);
            try {
                double value = new Double(arg);
                if (value <= 0) {
                    throw new IllegalArgumentException("Negative or null value found.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_EVALUE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }

        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_HITLIST_LENGTH.id);
            try {
                int value = new Integer(arg);
                if (value <= 0 || value > 20) {
                    throw new IllegalArgumentException("Hitlist length should be between 1 and 20.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.PEPTNOVO_ESTIMATE_CHARGE.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.PEPNOVO_CORRECT_PREC_MASS.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.PEPNOVO_DISCARD_SPECTRA.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_FRAGMENTATION_MODEL.id);
            if (!arg.equalsIgnoreCase("CID_IT_TRYP")) { // @TODO: support more models??
                System.out.println(System.getProperty("line.separator") + "Fragmentation model not supported." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id);
            try {
                int value = new Integer(arg);
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Found " + value + " where 0 or 1 was expected.");
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while parsing the " + IdentificationParametersCLIParams.PEPNOVO_GENERATE_BLAST.id + " option."
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }
        if (aLine.hasOption(IdentificationParametersCLIParams.FRAG_PPM.id)) {
            String arg = aLine.getOptionValue(IdentificationParametersCLIParams.FRAG_PPM.id);
            try {
                new Integer(arg);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while reading the ppm/Da fragment ion parameter:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }

        // @TODO: add MS-GF+, MS Amanda, DirecTag, MyriMatch and Comet parameters!!!
        return true;
    }
}
