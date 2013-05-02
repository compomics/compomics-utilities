package com.compomics.util.experiment.identification;

import com.compomics.software.CommandLineUtils;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
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
public class SearchParametersInputBean {

    /**
     * The tool type. Required when validating the search parameters as
     * different parameters are mandatory for the different tools.
     */
    public enum ToolType {

        SearchGUI, DeNovoGUI
    };
    /**
     * The spectrum files.
     */
    private ArrayList<File> spectrumFiles;
    /**
     * The output folder.
     */
    private File outputFolder;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The compomics PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    /**
     * If true, OMSSA is enabled.
     */
    private boolean omssaEnabled = true;
    /**
     * If true, X!Tandem is enabled.
     */
    private boolean xtandemEnabled = true;
    /**
     * If true, OMSSA OMX is used as the output, otherwise OMSSA CSV is used.
     */
    private boolean omssaOutputAsOmx = true;
    /**
     * The folder where OMSSA is installed.
     */
    private File omssaLocation = null;
    /**
     * The folder where X!Tandem is installed.
     */
    private File xtandemLocation = null;
    /**
     * The folder where PepNovo+ is installed.
     */
    private File pepNovoLocation = null;
    /**
     * If an mgf file exceeds this limit, the user will be asked for a split.
     */
    private int mgfMaxSize = 1000;
    /**
     * Number of spectra allowed in the split file.
     */
    private int mgfNSpectra = 25000;

    /**
     * Takes all the arguments from a command line.
     *
     * @param aLine the command line
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public SearchParametersInputBean(CommandLine aLine) throws FileNotFoundException, IOException, ClassNotFoundException {

        // get the mgf splitting limits
        if (aLine.hasOption(SearchParametersCLIParams.MGF_SPLITTING_LIMIT.id)) {
            String arg = aLine.getOptionValue(SearchParametersCLIParams.MGF_SPLITTING_LIMIT.id);
            Integer option = new Integer(arg);
            mgfMaxSize = option;
        }
        if (aLine.hasOption(SearchParametersCLIParams.MGF_MAX_SPECTRA.id)) {
            String arg = aLine.getOptionValue(SearchParametersCLIParams.MGF_MAX_SPECTRA.id);
            Integer option = new Integer(arg);
            mgfNSpectra = option;
        }

        // get the mgf files
        String filesTxt = aLine.getOptionValue(SearchParametersCLIParams.SPECTRUM_FILES.id);
        spectrumFiles = getSpectrumFiles(filesTxt);

        if (aLine.hasOption(SearchParametersCLIParams.OUTPUT_FOLDER.id)) {
            String arg = aLine.getOptionValue(SearchParametersCLIParams.OUTPUT_FOLDER.id);
            outputFolder = new File(arg);
        }
        if (aLine.hasOption(SearchParametersCLIParams.SEARCH_PARAMETERS.id)) {
            String fileTxt = aLine.getOptionValue(SearchParametersCLIParams.SEARCH_PARAMETERS.id);
            searchParameters = SearchParameters.getIdentificationParameters(new File(fileTxt));
        } else {
            searchParameters = new SearchParameters();
            if (aLine.hasOption(SearchParametersCLIParams.PPM.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.PPM.id);
                Integer option = new Integer(arg);
                if (option == 1) {
                    searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.PPM);
                } else {
                    searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.DA);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.PREC_TOL.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.PREC_TOL.id);
                Double option = new Double(arg);
                searchParameters.setPrecursorAccuracy(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.FRAG_TOL.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.FRAG_TOL.id);
                Double option = new Double(arg);
                searchParameters.setFragmentIonAccuracy(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.ENZYME.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.ENZYME.id);
                Enzyme option = enzymeFactory.getEnzyme(arg);
                searchParameters.setEnzyme(option);
            } else {
                Enzyme option = enzymeFactory.getEnzyme("Trypsin"); // no enzyme given, default to Trypsin
                searchParameters.setEnzyme(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.DB.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.DB.id);
                File fastaFile = new File(arg);
                searchParameters.setFastaFile(fastaFile);
            }
            if (aLine.hasOption(SearchParametersCLIParams.MC.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MC.id);
                Integer option = new Integer(arg);
                searchParameters.setnMissedCleavages(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.FI.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.FI.id);
                searchParameters.setIonSearched1(arg);
            }
            if (aLine.hasOption(SearchParametersCLIParams.RI.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.RI.id);
                searchParameters.setIonSearched2(arg);
            }
            if (aLine.hasOption(SearchParametersCLIParams.MIN_CHARGE.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MIN_CHARGE.id);
                Integer option = new Integer(arg);
                searchParameters.setMinChargeSearched(new Charge(Charge.PLUS, option));
            }
            if (aLine.hasOption(SearchParametersCLIParams.MAX_CHARGE.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MAX_CHARGE.id);
                Integer option = new Integer(arg);
                searchParameters.setMinChargeSearched(new Charge(Charge.PLUS, option));
            }
            if (aLine.hasOption(SearchParametersCLIParams.MAX_EVALUE.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MAX_EVALUE.id);
                Double option = new Double(arg);
                searchParameters.setMaxEValue(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.HITLIST_LENGTH.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.HITLIST_LENGTH.id);
                Integer option = new Integer(arg);
                searchParameters.setHitListLength(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.HITLIST_LENGTH_DE_NOVO.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.HITLIST_LENGTH_DE_NOVO.id);
                Integer option = new Integer(arg);
                searchParameters.setHitListLengthDeNovo(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.MIN_PEP_LENGTH.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MIN_PEP_LENGTH.id);
                Integer option = new Integer(arg);
                searchParameters.setMinPeptideLength(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.MAX_PEP_LENGTH.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MAX_PEP_LENGTH.id);
                Integer option = new Integer(arg);
                searchParameters.setMaxPeptideLength(option);
            }
            if (aLine.hasOption(SearchParametersCLIParams.REMOVE_PREC.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.REMOVE_PREC.id);
                Integer option = new Integer(arg);
                if (option == 1) {
                    searchParameters.setRemovePrecursor(true);
                } else {
                    searchParameters.setRemovePrecursor(false);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.SCALE_PREC.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.SCALE_PREC.id);
                Integer option = new Integer(arg);
                if (option == 1) {
                    searchParameters.setScalePrecursor(true);
                } else {
                    searchParameters.setScalePrecursor(false);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.ESTIMATE_CHARGE.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.ESTIMATE_CHARGE.id);
                Integer option = new Integer(arg);
                if (option == 1) {
                    searchParameters.setEstimateCharge(true);
                } else {
                    searchParameters.setEstimateCharge(false);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.ESTIMATE_CHARGE_DE_NOVO.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.ESTIMATE_CHARGE_DE_NOVO.id);
                Integer option = new Integer(arg);
                if (option == 1) {
                    searchParameters.setEstimateCharge(true);
                } else {
                    searchParameters.setEstimateCharge(false);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.CORRECT_PRECURSOR_MASS.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.CORRECT_PRECURSOR_MASS.id);
                Integer option = new Integer(arg);
                if (option == 1) {
                    searchParameters.correctPrecursorMass(true);
                } else {
                    searchParameters.correctPrecursorMass(false);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.DISCARD_SPECTRA.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.DISCARD_SPECTRA.id);
                Integer option = new Integer(arg);
                if (option == 1) {
                    searchParameters.setDiscardLowQualitySpectra(true);
                } else {
                    searchParameters.setDiscardLowQualitySpectra(false);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.GENERATE_BLAST.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.GENERATE_BLAST.id);
                Integer option = new Integer(arg);
                if (option == 1) {
                    searchParameters.setGenerateQuery(true);
                } else {
                    searchParameters.setGenerateQuery(false);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.FRAGMENTATION_MODEL.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.FRAGMENTATION_MODEL.id);
                searchParameters.setFragmentationModel(arg);
            }

            ModificationProfile modificationProfile = new ModificationProfile();
            if (aLine.hasOption(SearchParametersCLIParams.FIXED_MODS.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.FIXED_MODS.id);
                ArrayList<String> args = CommandLineUtils.splitInput(arg);
                for (String ptmName : args) {
                    PTM modification = ptmFactory.getPTM(ptmName);
                    modificationProfile.addFixedModification(modification);
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.VARIABLE_MODS.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.VARIABLE_MODS.id);
                ArrayList<String> args = CommandLineUtils.splitInput(arg);
                for (String ptmName : args) {
                    PTM modification = ptmFactory.getPTM(ptmName);
                    modificationProfile.addVariableModification(modification);
                }
            }
            ptmFactory.setSearchedOMSSAIndexes(searchParameters.getModificationProfile());
            searchParameters.setModificationProfile(modificationProfile);
        }

        // see which search engines to use
        if (aLine.hasOption(SearchParametersCLIParams.OMSSA.id)) {
            String omssaOption = aLine.getOptionValue(SearchParametersCLIParams.OMSSA.id);
            if (omssaOption.trim().equals("0")) {
                omssaEnabled = false;
            }
        }
        if (aLine.hasOption(SearchParametersCLIParams.XTANDEM.id)) {
            String xtandemOption = aLine.getOptionValue(SearchParametersCLIParams.XTANDEM.id);
            if (xtandemOption.trim().equals("0")) {
                xtandemEnabled = false;
            }
        }

        // search engine folders
        if (aLine.hasOption(SearchParametersCLIParams.OMSSA_LOCATION.id)) {
            String omssaFolder = aLine.getOptionValue(SearchParametersCLIParams.OMSSA_LOCATION.id);
            omssaLocation = new File(omssaFolder);
        }
        if (aLine.hasOption(SearchParametersCLIParams.XTANDEM_LOCATION.id)) {
            String omssaFolder = aLine.getOptionValue(SearchParametersCLIParams.XTANDEM_LOCATION.id);
            xtandemLocation = new File(omssaFolder);
        }
        if (aLine.hasOption(SearchParametersCLIParams.PEP_NOVO_LOCATION.id)) {
            String omssaFolder = aLine.getOptionValue(SearchParametersCLIParams.PEP_NOVO_LOCATION.id);
            pepNovoLocation = new File(omssaFolder);
        }

        // check the omssa output format, omx or csv
        if (aLine.hasOption(SearchParametersCLIParams.OMSSA_FORMAT.id)) {
            String omssaFormatOption = aLine.getOptionValue(SearchParametersCLIParams.OMSSA_FORMAT.id);
            if (omssaFormatOption.trim().equals("csv")) {
                omssaOutputAsOmx = false;
            }
        }
    }

    /**
     * Return the spectrum files.
     *
     * @return the spectrum files
     */
    public ArrayList<File> getSpectrumFiles() {
        return spectrumFiles;
    }

    /**
     * Returns the output folder.
     *
     * @return the output folder
     */
    public File getOutputFile() {
        return outputFolder;
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
     * Returns a list of spectrum files as imported from the command line
     * option.
     *
     * @param optionInput the command line option
     * @return a list of file candidates
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     */
    public static ArrayList<File> getSpectrumFiles(String optionInput) throws FileNotFoundException {
        ArrayList<String> extentions = new ArrayList<String>();
        extentions.add(".mgf");
        return CommandLineUtils.getFiles(optionInput, extentions);
    }

    /**
     * Returns true if OMSSA is to be used.
     *
     * @return true if OMSSA is to be used
     */
    public boolean isOmssaEnabled() {
        return omssaEnabled;
    }

    /**
     * Returns true if X!Tandem is to be used.
     *
     * @return if X!Tandem is to be used
     */
    public boolean isXTandemEnabled() {
        return xtandemEnabled;
    }

    /**
     * Returns true if OMSSA OMX is to be used as output, false if OMSSA CSV is
     * to be used.
     *
     * @return true if OMSSA OMX is to be used as output, false if OMSSA CSV is
     * to be used
     */
    public boolean isOmssaOutputAsOmx() {
        return omssaOutputAsOmx;
    }

    /**
     * Returns the OMSSA location, null if none is set.
     *
     * @return the omssaLocation
     */
    public File getOmssaLocation() {
        return omssaLocation;
    }

    /**
     * Set the OMSSA location.
     *
     * @param omssaLocation the omssaLocation to set
     */
    public void setOmssaLocation(File omssaLocation) {
        this.omssaLocation = omssaLocation;
    }

    /**
     * Returns the X!Tandem location.
     *
     * @return the xtandemLocation
     */
    public File getXtandemLocation() {
        return xtandemLocation;
    }

    /**
     * Set the X!Tandem location.
     *
     * @param xtandemLocation the xtandemLocation to set
     */
    public void setXtandemLocation(File xtandemLocation) {
        this.xtandemLocation = xtandemLocation;
    }

    /**
     * Returns the PepNovo+ location.
     *
     * @return the pepNovoLocation
     */
    public File getPepNovoLocation() {
        return pepNovoLocation;
    }

    /**
     * Set the PepNovo+ location.
     *
     * @param pepNovoLocation the pepNovoLocation to set
     */
    public void setPepNovoLocation(File pepNovoLocation) {
        this.pepNovoLocation = pepNovoLocation;
    }

    /**
     * Returns the max mgf file size before splitting.
     *
     * @return the mgfMaxSize
     */
    public int getMgfMaxSize() {
        return mgfMaxSize;
    }

    /**
     * Get the max number of spectra in an mgf file.
     *
     * @return the mgfNSpectra
     */
    public int getMgfNSpectra() {
        return mgfNSpectra;
    }

    /**
     * Verifies the command line start parameters.
     *
     * @param aLine the command line to validate
     * @param currentToolType the tool type, used to check which parameters that
     * are mandatory
     * @return true if the startup was valid
     * @throws IOException
     */
    public static boolean isValidStartup(CommandLine aLine, ToolType currentToolType) throws IOException {

        if (aLine.getOptions().length == 0) {
            return false;
        }

        if (!aLine.hasOption(SearchParametersCLIParams.SPECTRUM_FILES.id) || ((String) aLine.getOptionValue(SearchParametersCLIParams.SPECTRUM_FILES.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Spectrum files not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            ArrayList<File> tempSpectrumFiles = SearchParametersInputBean.getSpectrumFiles(aLine.getOptionValue(SearchParametersCLIParams.SPECTRUM_FILES.id));
            for (File file : tempSpectrumFiles) {
                if (!file.exists()) {
                    System.out.println(System.getProperty("line.separator") + "File \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                    return false;
                }
            }
        }

        if (!aLine.hasOption(SearchParametersCLIParams.OUTPUT_FOLDER.id) || ((String) aLine.getOptionValue(SearchParametersCLIParams.OUTPUT_FOLDER.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Output folder not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(SearchParametersCLIParams.OUTPUT_FOLDER.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Output folder \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        if (currentToolType == ToolType.SearchGUI) {
            if (!aLine.hasOption(SearchParametersCLIParams.DB.id) || aLine.getOptionValue(SearchParametersCLIParams.DB.id).toString().equals("")) {
                System.out.println("\nFasta file not specified.\n");
                return false;
            } else {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.DB.id);
                try {
                    File fastaFile = new File(arg);
                    if (!fastaFile.exists()) {
                        System.out.println("\nFasta file not found.\n");
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while setting the database:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
        }

        if (aLine.hasOption(SearchParametersCLIParams.SEARCH_PARAMETERS.id)) {
            try {
                String fileTxt = aLine.getOptionValue(SearchParametersCLIParams.SEARCH_PARAMETERS.id);
                SearchParameters tempSearchParameters = SearchParameters.getIdentificationParameters(new File(fileTxt));

                // check for valid de novo parameters
                if (currentToolType == ToolType.DeNovoGUI) {
                    if (tempSearchParameters.getPrecursorAccuracy() < 0 || tempSearchParameters.getPrecursorAccuracy() > 5) {
                        System.out.println(System.getProperty("line.separator") + "Precursor tolerance has to be between 0 and 5.0!" + System.getProperty("line.separator"));
                        return false;
                    }

                    if (tempSearchParameters.getHitListLengthDeNovo() > 20) {
                        System.out.println(System.getProperty("line.separator") + "Maximum the de novo solutions is 20!" + System.getProperty("line.separator"));
                        return false;
                    }
                }

            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurred while reading the search parameters:"
                        + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }

        } else {
            if (aLine.hasOption(SearchParametersCLIParams.PPM.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.PPM.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the ppm/Da parameter:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }

            if (aLine.hasOption(SearchParametersCLIParams.PREC_TOL.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.PREC_TOL.id);
                try {
                    Double temp = new Double(arg);

                    if (currentToolType == ToolType.DeNovoGUI) {
                        if (temp < 0 || temp > 5) {
                            System.out.println(System.getProperty("line.separator") + "Precursor tolerance has to be between 0 and 5.0!" + System.getProperty("line.separator"));
                            return false;
                        }
                    }

                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the precursor m/z tolerance parameter:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }

            if (aLine.hasOption(SearchParametersCLIParams.FRAG_TOL.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.FRAG_TOL.id);
                try {
                    new Double(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the fragment ion m/z parameter:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.ENZYME.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.ENZYME.id);
                try {
                    Enzyme selectedEnzyme = EnzymeFactory.getInstance().getEnzyme(arg); // @TODO: is this correct use of the factory?
                    if (selectedEnzyme == null) {
                        System.out.println(System.getProperty("line.separator") + "Unknown enzyme: \'" + arg + "\'." + System.getProperty("line.separator")
                                + "See SearchGUI for the list of supported enzymes. (Note that the names are case sensitive.)");
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the enzyme:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.MC.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MC.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the number of missed cleavages:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.MIN_CHARGE.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MIN_CHARGE.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the minimum charge:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.MAX_CHARGE.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MAX_CHARGE.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the maximum charge:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.MAX_EVALUE.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MAX_EVALUE.id);
                try {
                    new Double(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the maximum e-value:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.HITLIST_LENGTH.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.HITLIST_LENGTH.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the OMSSA hit list length:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.HITLIST_LENGTH_DE_NOVO.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.HITLIST_LENGTH_DE_NOVO.id);
                try {
                    Integer temp = new Integer(arg);

                    if (temp > 20) {
                        System.out.println(System.getProperty("line.separator") + "Maximum hit list length is 20!" + System.getProperty("line.separator"));
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the hit list length:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.MIN_PEP_LENGTH.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MIN_PEP_LENGTH.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the minimal peptide length:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.MAX_PEP_LENGTH.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.MAX_PEP_LENGTH.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the maximal peptide length:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.REMOVE_PREC.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.REMOVE_PREC.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the remove precursor option:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.SCALE_PREC.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.SCALE_PREC.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the scale precursor option:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.ESTIMATE_CHARGE.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.ESTIMATE_CHARGE.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the estimate charge option:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.ESTIMATE_CHARGE_DE_NOVO.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.ESTIMATE_CHARGE_DE_NOVO.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the estimate charge option:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.CORRECT_PRECURSOR_MASS.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.CORRECT_PRECURSOR_MASS.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the correct precursor mass option:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.DISCARD_SPECTRA.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.DISCARD_SPECTRA.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the discard precursor option:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.FRAGMENTATION_MODEL.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.FRAGMENTATION_MODEL.id);
                if (!arg.equalsIgnoreCase("CID_IT_TRYP")) { // @TODO: support more models??
                    System.out.println(System.getProperty("line.separator") + "Fragmentation model not supported." + System.getProperty("line.separator"));
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.GENERATE_BLAST.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.GENERATE_BLAST.id);
                try {
                    new Integer(arg);
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the generate BLAST query option:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.FIXED_MODS.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.FIXED_MODS.id);
                try {
                    ArrayList<String> args = CommandLineUtils.splitInput(arg);
                    for (String ptmName : args) {
                        PTMFactory.getInstance().getPTM(ptmName); // @TODO: is this correct use of the factory?
                    }
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the fixed modifications:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
            if (aLine.hasOption(SearchParametersCLIParams.VARIABLE_MODS.id)) {
                String arg = aLine.getOptionValue(SearchParametersCLIParams.VARIABLE_MODS.id);
                try {
                    ArrayList<String> args = CommandLineUtils.splitInput(arg);
                    for (String ptmName : args) {
                        PTMFactory.getInstance().getPTM(ptmName); // @TODO: is this correct use of the factory?
                    }
                } catch (Exception e) {
                    System.out.println(System.getProperty("line.separator") + "An error occurred while reading the variable modifications:"
                            + System.getProperty("line.separator") + e.getLocalizedMessage() + System.getProperty("line.separator"));
                    e.printStackTrace();
                    return false;
                }
            }
        }

        return true;
    }
}