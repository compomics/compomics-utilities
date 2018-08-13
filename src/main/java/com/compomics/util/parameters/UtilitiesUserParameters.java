package com.compomics.util.parameters;

import com.compomics.util.parameters.searchgui.OutputParameters;
import com.compomics.util.io.file.LastSelectedFolder;
import com.compomics.util.io.json.JsonMarshaller;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Utilities user parameters can be used to store user parameters.
 *
 * @author Marc Vaudel
 */
public class UtilitiesUserParameters {

    /**
     * Serial version UID for post-serialization compatibility.
     */
    static final long serialVersionUID = -4343570286224891504L;
    /**
     * Location of the user preferences file.
     */
    private static String USER_PARAMETERS_FILE = System.getProperty("user.home") + "/.compomics/userparameters.cup";
    /**
     * The width to use for the annotated peaks.
     */
    private float spectrumAnnotatedPeakWidth = 1.0f;
    /**
     * The width to use for the background peaks.
     */
    private float spectrumBackgroundPeakWidth = 1.0f;
    /**
     * The color to use for the annotated peaks.
     */
    private Color spectrumAnnotatedPeakColor = Color.RED;
    /**
     * The color to use for the annotated mirrored peaks.
     */
    private Color spectrumAnnotatedMirroredPeakColor = Color.BLUE;
    /**
     * The color to use for the background peaks.
     */
    private Color spectrumBackgroundPeakColor = new Color(100, 100, 100, 50);
    /**
     * The color used for the sparkline bar chart plots.
     */
    private Color sparklineColorValidated = new Color(110, 196, 97);
    /**
     * The color used for the non-validated sparkline bar chart plots.
     */
    private Color sparklineColorNonValidated = new Color(208, 19, 19);
    /**
     * The color used for the not found sparkline bar chart plots.
     */
    private Color sparklineColorNotFound = new Color(222, 222, 222);
    /**
     * The color used for the possible values sparkline bar chart plots.
     */
    private Color sparklineColorPossible = new Color(100, 150, 255);
    /**
     * The color used for the doubtful matches in sparkline bar chart plots.
     */
    private Color sparklineColorDoubtful = new Color(255, 204, 0);
    /**
     * The color used for the false positive in sparkline bar chart plots.
     */
    private Color sparklineColorFalsePositive = new Color(255, 51, 51);
    /**
     * The color of the selected peptide.
     */
    private Color peptideSelected = new Color(0, 0, 255);
    /**
     * The memory to use.
     */
    private int memoryParameter = 4 * 1024;
    /**
     * The Java Home, for example, C:\Program Files\Java\jdk1.8.0_25\bin. Null
     * if not set. Note that this setting will be ignored of a JavaHome.txt file
     * is found.
     */
    private String javaHome = null;
    /**
     * The path to the ProteoWizard installation (if any). Set to null if no
     * path is provided.
     */
    private String proteoWizardPath = null;
    /**
     * The path to the SearchGUI installation (if any). Makes it possible to
     * start SearchGUI directly from PeptideShaker. Set to null if no path is
     * provided.
     */
    private String searchGuiPath = null;
    /**
     * The path to the PeptideShaker installation (if any). Set to null if no
     * path is provided.
     */
    private String peptideShakerPath = null;
    /**
     * The path to the DeNovoGUI installation (if any). Set to null if no path
     * is provided.
     */
    private String deNovoGuiPath = null;
    /**
     * The path to the Reporter installation (if any). Set to null if no path is
     * provided.
     */
    private String reporterPath = null;
    /**
     * The path to the Relims installation (if any). Set to null if no path is
     * provided.
     */
    private String relimsPath = null;
    /**
     * The local PRIDE projects folder.
     */
    private String localPrideFolder = "user.home";
    /**
     * The user last used database folder.
     */
    private File dbFolder = null;
    /**
     * The list of already read tweets.
     */
    private ArrayList<String> readTweets = null;
    /**
     * The list of already displayed tips.
     */
    private ArrayList<String> displayedTips = null;
    /**
     * Indicates whether the tool should check for updates.
     */
    private boolean autoUpdate = true;
    /**
     * The last selected folder.
     */
    private LastSelectedFolder lastSelectedFolder = new LastSelectedFolder();
    /**
     * If true, the PSMs are sorted on retention time, false sorts on PSM score.
     */
    private boolean sortPsmsOnRt = false;
    /**
     * The tag added after adding decoy sequences to a FASTA file.
     */
    private String targetDecoyFileNameTag = "_concatenated_target_decoy";
    /**
     * If true, the selected spectra will be checked for peak picking.
     */
    private boolean checkPeakPicking = true;
    /**
     * If true, the selected spectra will be checked for duplicate spectrum
     * titles.
     */
    private boolean checkDuplicateTitles = true;
    /**
     * If true, the mgf files will be checked for size.
     */
    private boolean checkMgfSize = false;
    /**
     * If an mgf file exceeds this limit, the user will be asked for a split.
     */
    private double mgfMaxSize = 1000.0;
    /**
     * Number of spectra allowed in the split file.
     */
    private int mgfNSpectra = 25000;
    /**
     * Reference mass for the conversion of the fragment ion tolerance from ppm
     * to Dalton.
     */
    private double refMass = 2000.0;
    /**
     * The way output files should be exported.
     */
    private OutputParameters outputOption = OutputParameters.grouped;
    /**
     * Indicates whether data files (mgf and FASTA) should be copied in the
     * output.
     */
    private boolean outputData = false;
    /**
     * Indicates whether the date should be included in the output.
     */
    private boolean includeDateInOutputName = false;
    /**
     * If true the X! Tandem file will be renamed.
     */
    private boolean renameXTandemFile = true;
    /**
     * If true, the spectra will be checked for missing charges.
     */
    private boolean checkSpectrumCharges = true;
    /**
     * The maximum charge added when the charge is missing for a given spectrum.
     */
    private int minSpectrumChargeRange = 2;
    /**
     * The minimum charge added when the charge is missing for a given spectrum.
     */
    private int maxSpectrumChargeRange = 4;
    /**
     * The list of the default modifications.
     */
    private HashSet<String> defaultModifications = defaultModifications();

    /**
     * Constructor.
     */
    public UtilitiesUserParameters() {
    }

    /**
     * Getter for the sparkline color.
     *
     * @return the sparkline color
     */
    public Color getSparklineColor() {
        return sparklineColorValidated;
    }

    /**
     * Setter for the sparkline color.
     *
     * @param sparklineColorValidated the sparkline color
     */
    public void setSparklineColor(Color sparklineColorValidated) {
        this.sparklineColorValidated = sparklineColorValidated;
    }

    /**
     * Getter for the non-validated sparkline color.
     *
     * @return the non-validated sparkline color
     */
    public Color getSparklineColorNonValidated() {
        if (sparklineColorNonValidated == null) {
            sparklineColorNonValidated = new Color(255, 0, 0);
        }
        return sparklineColorNonValidated;
    }

    /**
     * Returns the color for a selected peptide.
     *
     * @return the color for a selected peptide
     */
    public Color getPeptideSelected() {
        if (peptideSelected == null) {
            peptideSelected = new Color(0, 0, 255);
        }
        return peptideSelected;
    }

    /**
     * Returns the color for a not found sparkline bar chart plots.
     *
     * @return the color for a not found sparkline bar chart plots
     */
    public Color getSparklineColorNotFound() {
        if (sparklineColorNotFound == null) {
            sparklineColorNotFound = new Color(222, 222, 222);
        }
        return sparklineColorNotFound;
    }

    /**
     * Setter for the non-validated sparkline color.
     *
     * @param sparklineColorNonValidated the non-validated sparkline color
     */
    public void setSparklineColorNonValidated(Color sparklineColorNonValidated) {
        this.sparklineColorNonValidated = sparklineColorNonValidated;
    }

    /**
     * Returns the color for a possible sparkline bar chart plots.
     *
     * @return the color for a possible sparkline bar chart plots
     */
    public Color getSparklineColorPossible() {
        return sparklineColorPossible;
    }

    /**
     * Setter for the possible sparkline color.
     *
     * @param sparklineColorPossible the possible sparkline color
     */
    public void setSparklineColorPossible(Color sparklineColorPossible) {
        this.sparklineColorPossible = sparklineColorPossible;
    }

    /**
     * Returns the color for a doubtful sparkline bar chart plots.
     *
     * @return the color for a doubtful sparkline bar chart plots
     */
    public Color getSparklineColorDoubtful() {
        return sparklineColorDoubtful;
    }

    /**
     * Setter for the doubtful sparkline color.
     *
     * @param sparklineColorDoubtful the doubtful sparkline color
     */
    public void setSparklineColorDoubtful(Color sparklineColorDoubtful) {
        this.sparklineColorDoubtful = sparklineColorDoubtful;
    }

    /**
     * Returns the color for false positives in sparkline bar chart plots.
     *
     * @return the color for a false positives in sparkline bar chart plots
     */
    public Color getSparklineColorFalsePositives() {
        return sparklineColorFalsePositive;
    }

    /**
     * Setter for the false positives sparkline color.
     *
     * @param sparklineColorFalsePositive the false positives sparkline color
     */
    public void setSparklineColorFalsePositives(Color sparklineColorFalsePositive) {
        this.sparklineColorFalsePositive = sparklineColorFalsePositive;
    }

    /**
     * Returns the upper memory limit in MB.
     *
     * @return the upper memory limit
     */
    public Integer getMemoryParameter() {
        return memoryParameter;
    }

    /**
     * Sets the upper memory limit.
     *
     * @param memoryParameter the preferred upper memory limit
     */
    public void setMemoryParameter(int memoryParameter) {
        this.memoryParameter = memoryParameter;
    }

    /**
     * Returns the Java Home folder.
     *
     * @return the Java Home folder
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * Set the Java Home folder.
     *
     * @param javaHome the new Java Home
     */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * Returns the color to use for the annotated peaks.
     *
     * @return the spectrumAnnotatedPeakColor
     */
    public Color getSpectrumAnnotatedPeakColor() {
        return spectrumAnnotatedPeakColor;
    }

    /**
     * Set the color to use for the annotated peaks.
     *
     * @param spectrumAnnotatedPeakColor the spectrumAnnotatedPeakColor to set
     */
    public void setSpectrumAnnotatedPeakColor(Color spectrumAnnotatedPeakColor) {
        this.spectrumAnnotatedPeakColor = spectrumAnnotatedPeakColor;
    }

    /**
     * Returns the color to use for the annotated mirrored peaks.
     *
     * @return the spectrumAnnotatedMirroredPeakColor
     */
    public Color getSpectrumAnnotatedMirroredPeakColor() {
        return spectrumAnnotatedMirroredPeakColor;
    }

    /**
     * Set the color to use for the annotated mirrored peaks.
     *
     * @param spectrumAnnotatedMirroredPeakColor the
     * spectrumAnnotatedMirroredPeakColor to set
     */
    public void setSpectrumAnnotatedMirroredPeakColor(Color spectrumAnnotatedMirroredPeakColor) {
        this.spectrumAnnotatedMirroredPeakColor = spectrumAnnotatedMirroredPeakColor;
    }

    /**
     * Returns the color to use for the background peaks.
     *
     * @return the spectrumBackgroundPeakColor
     */
    public Color getSpectrumBackgroundPeakColor() {
        return spectrumBackgroundPeakColor;
    }

    /**
     * Set the color to use for the background peaks.
     *
     * @param spectrumBackgroundPeakColor the spectrumBackgroundPeakColor to set
     */
    public void setSpectrumBackgroundPeakColor(Color spectrumBackgroundPeakColor) {
        this.spectrumBackgroundPeakColor = spectrumBackgroundPeakColor;
    }

    /**
     * Returns the width of the annotated peaks.
     *
     * @return the spectrumAnnotatedPeakWidth
     */
    public float getSpectrumAnnotatedPeakWidth() {
        return spectrumAnnotatedPeakWidth;
    }

    /**
     * Set the width of the annotated peaks.
     *
     * @param spectrumAnnotatedPeakWidth the spectrumAnnotatedPeakWidth to set
     */
    public void setSpectrumAnnotatedPeakWidth(float spectrumAnnotatedPeakWidth) {
        this.spectrumAnnotatedPeakWidth = spectrumAnnotatedPeakWidth;
    }

    /**
     * Returns the width of the background peaks.
     *
     * @return the spectrumBackgroundPeakWidth
     */
    public float getSpectrumBackgroundPeakWidth() {
        return spectrumBackgroundPeakWidth;
    }

    /**
     * Set the width of the background peaks.
     *
     * @param spectrumBackgroundPeakWidth the spectrumBackgroundPeakWidth to set
     */
    public void setSpectrumBackgroundPeakWidth(float spectrumBackgroundPeakWidth) {
        this.spectrumBackgroundPeakWidth = spectrumBackgroundPeakWidth;
    }

    /**
     * Returns the path to the SearchGUI installation.
     *
     * @return the path to the SearchGUI installation
     */
    public String getSearchGuiPath() {
        return searchGuiPath;
    }

    /**
     * Set the path to the SearchGUI installation.
     *
     * @param searchGuiPath the path to the SearchGUI installation
     */
    public void setSearchGuiPath(String searchGuiPath) {
        this.searchGuiPath = searchGuiPath;
    }

    /**
     * Returns the path to the DeNovoGUI installation.
     *
     * @return the path to the DeNovoGUI installation
     */
    public String getDeNovoGuiPath() {
        return deNovoGuiPath;
    }

    /**
     * Set the path to the DeNovoGUI installation.
     *
     * @param deNovoGuiPath the path to the DeNovoGUI installation
     */
    public void setDeNovoGuiPath(String deNovoGuiPath) {
        this.deNovoGuiPath = deNovoGuiPath;
    }

    /**
     * Returns the path to ProteoWizard.
     *
     * @return the path to ProteoWizard
     */
    public String getProteoWizardPath() {
        return proteoWizardPath;
    }

    /**
     * Set the path to ProteoWizard.
     *
     * @param proteoWizardPath the path to ProteoWizard
     */
    public void setProteoWizardPath(String proteoWizardPath) {
        this.proteoWizardPath = proteoWizardPath;
    }

    /**
     * Returns the path to the Relims installation.
     *
     * @return the path to the Relims installation
     */
    public String getRelimsPath() {
        return relimsPath;
    }

    /**
     * Set the path to the Relims installation.
     *
     * @param relimsPath the path to the * installation
     */
    public void setRelimsPath(String relimsPath) {
        this.relimsPath = relimsPath;
    }

    /**
     * Returns the path to the PeptideShaker installation.
     *
     * @return the path to the PeptideShaker installation
     */
    public String getPeptideShakerPath() {
        return peptideShakerPath;
    }

    /**
     * Set the path to the PeptideShaker installation.
     *
     * @param peptideShakerPath the path to the PeptideShaker installation
     */
    public void setPeptideShakerPath(String peptideShakerPath) {
        this.peptideShakerPath = peptideShakerPath;
    }

    /**
     * Returns the path to the Reporter installation.
     *
     * @return the path to the Reporter installation
     */
    public String getReporterPath() {
        return reporterPath;
    }

    /**
     * Set the path to the PeptideShaker installation.
     *
     * @param reporterPath the path to the PeptideShaker installation
     */
    public void setReporterPath(String reporterPath) {
        this.reporterPath = reporterPath;
    }

    /**
     * Convenience method saving the user parameters. Exceptions are ignored silently and written to the stack trace.
     *
     * @param userParameters the user preferences
     */
    public static void saveUserParameters(UtilitiesUserParameters userParameters) {

        try {
            
            File file = new File(USER_PARAMETERS_FILE);
            
            if (!file.getParentFile().exists()) {
                
                file.getParentFile().mkdir();
                
            }
            
            JsonMarshaller marshaller = new JsonMarshaller();
            marshaller.saveObjectToJson(userParameters, file);
            
        } catch (Exception e) {
            
            System.err.println("An error occurred while saving " + USER_PARAMETERS_FILE + " (see below).");
            e.printStackTrace();
            
        }
    }

    /**
     * Loads the user parameters. If an error is encountered, parameters are
     * set back to default.
     *
     * @return returns the utilities user preferences
     */
    public static UtilitiesUserParameters loadUserParameters() {
        
        UtilitiesUserParameters userParameters;
        
        File file = new File(UtilitiesUserParameters.USER_PARAMETERS_FILE);

        if (!file.exists()) {
            
            userParameters = new UtilitiesUserParameters();
            UtilitiesUserParameters.saveUserParameters(userParameters);
            
        } else {
            
            try {
                
            JsonMarshaller marshaller = new JsonMarshaller();
            userParameters = (UtilitiesUserParameters) marshaller.fromJson(UtilitiesUserParameters.class, file);
                
            } catch (Exception e) {
                
                System.err.println("An error occurred while loading " + UtilitiesUserParameters.USER_PARAMETERS_FILE + " (see below). Preferences set back to default.");
                e.printStackTrace();
                
                userParameters = new UtilitiesUserParameters();
                UtilitiesUserParameters.saveUserParameters(userParameters);
                
            }
        }

        return userParameters;
    }

    /**
     * Returns the local PRIDE folder.
     *
     * @return the localPrideFolder
     */
    public String getLocalPrideFolder() {
        return localPrideFolder;
    }

    /**
     * Set the local PRIDE folder.
     *
     * @param localPrideFolder the localPrideFolder to set
     */
    public void setLocalPrideFolder(String localPrideFolder) {
        this.localPrideFolder = localPrideFolder;
    }

    /**
     * Returns the last used database folder. Null if not set.
     *
     * @return the last used database folder
     */
    public File getDbFolder() {
        return dbFolder;
    }

    /**
     * Sets the last used database folder.
     *
     * @param dbFolder the last used database folder
     */
    public void setDbFolder(File dbFolder) {
        this.dbFolder = dbFolder;
    }

    /**
     * Returns the list of read tweets.
     *
     * @return the list of read tweets
     */
    public ArrayList<String> getReadTweets() {
        if (readTweets == null) {
            readTweets = new ArrayList<>(0);
        }
        return readTweets;
    }

    /**
     * Set the list of read tweets.
     *
     * @param readTweets the readTweets to set
     */
    public void setReadTweets(ArrayList<String> readTweets) {
        this.readTweets = readTweets;
    }

    /**
     * Returns the list of displayed tips.
     *
     * @return the displayed tips
     */
    public ArrayList<String> getDisplayedTips() {
        if (displayedTips == null) {
            displayedTips = new ArrayList<>(0);
        }
        return displayedTips;
    }

    /**
     * Set the list of displayed tips.
     *
     * @param displayedTips the displayedTips to set
     */
    public void setDisplayedTips(ArrayList<String> displayedTips) {
        this.displayedTips = displayedTips;
    }

    /**
     * Returns the user preferences file to be used.
     *
     * @return the user preferences file
     */
    public static String getUserParametersFile() {
        return USER_PARAMETERS_FILE;
    }

    /**
     * Returns the user preferences file to be used.
     *
     * @return the user preferences file
     */
    public static String getUserParametersFolder() {
        File tempFile = new File(getUserParametersFile());
        return tempFile.getParent();
    }

    /**
     * Sets the user preferences file to be used.
     *
     * @param userParametersFolder the user preferences file to be used
     */
    public static void setUserParametersFolder(String userParametersFolder) {
        File tempFile = new File(userParametersFolder, "/utilities_userPreferences.cup");
        UtilitiesUserParameters.USER_PARAMETERS_FILE = tempFile.getAbsolutePath();
    }

    /**
     * Indicates whether the tools should use the auto update function.
     *
     * @return whether the tools should use the auto update function
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Sets whether the tools should use the auto update function.
     *
     * @param autoUpdate whether the tools should use the auto update function
     */
    public void setAutoUpdate(Boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    /**
     * Returns the last selected folder.
     *
     * @return the last selected folder
     */
    public LastSelectedFolder getLastSelectedFolder() {
        return lastSelectedFolder;
    }

    /**
     * Sets the last selected folder.
     *
     * @param lastSelectedFolder the last selected folder
     */
    public void setLastSelectedFolder(LastSelectedFolder lastSelectedFolder) {
        this.lastSelectedFolder = lastSelectedFolder;
    }

    /**
     * Returns true if the PSMs are sorted on retention time, false sorts on PSM
     * score.
     *
     * @return the sortPsmsOnRt
     */
    public boolean getSortPsmsOnRt() {
        return sortPsmsOnRt;
    }

    /**
     * Set if the PSMs are sorted on retention time, false sorts on PSM score.
     *
     * @param sortPsmsOnRt the sortPsmsOnRt to set
     */
    public void setSortPsmsOnRt(Boolean sortPsmsOnRt) {
        this.sortPsmsOnRt = sortPsmsOnRt;
    }

    /**
     * Returns the target-decoy file name suffix.
     *
     * @return the targetDecoyFileNameSuffix
     */
    public String getTargetDecoyFileNameSuffix() {
        return targetDecoyFileNameTag;
    }

    /**
     * Set the target-decoy file name suffix.
     *
     * @param targetDecoyFileNameSuffix the targetDecoyFileNameSuffix to set
     */
    public void setTargetDecoyFileNameSuffix(String targetDecoyFileNameSuffix) {
        this.targetDecoyFileNameTag = targetDecoyFileNameSuffix;
    }

    /**
     * Returns if the spectra should be checked for peak picking or not.
     *
     * @return true if the spectra should be checked for peak picking
     */
    public boolean checkPeakPicking() {
        return checkPeakPicking;
    }

    /**
     * Set if the spectra should be checked for peak picking or not.
     *
     * @param checkPeakPicking the checkPeakPicking to set
     */
    public void setCheckPeakPicking(boolean checkPeakPicking) {
        this.checkPeakPicking = checkPeakPicking;
    }

    /**
     * Returns if the spectra should be checked for duplicate titles or not.
     *
     * @return true if the spectra should be checked for duplicate titles
     */
    public boolean checkDuplicateTitles() {
        return checkDuplicateTitles;
    }

    /**
     * Set if the spectra should be checked for duplicate titles or not.
     *
     * @param checkDuplicateTitles the checkDuplicateTitles to set
     */
    public void setCheckDuplicateTitles(boolean checkDuplicateTitles) {
        this.checkDuplicateTitles = checkDuplicateTitles;
    }

    /**
     * Returns if the mgf should be checked for size.
     *
     * @return true if the mgf should be checked for size
     */
    public boolean checkMgfSize() {
        return checkMgfSize;
    }

    /**
     * Set if the mgf should be checked for size.
     *
     * @param checkMgfSize the mgf should be checked for size
     */
    public void setCheckMgfSize(boolean checkMgfSize) {
        this.checkMgfSize = checkMgfSize;
    }

    /**
     * Returns the max mgf file size before splitting.
     *
     * @return the mgfMaxSize
     */
    public double getMgfMaxSize() {
        return mgfMaxSize;
    }

    /**
     * Set the max mgf file size before splitting.
     *
     * @param mgfMaxSize the mgfMaxSize to set
     */
    public void setMgfMaxSize(double mgfMaxSize) {
        this.mgfMaxSize = mgfMaxSize;
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
     * Set the max number of spectra in an mgf file.
     *
     * @param mgfNSpectra the mgfNSpectra to set
     */
    public void setMgfNSpectra(int mgfNSpectra) {
        this.mgfNSpectra = mgfNSpectra;
    }

    /**
     * Returns the reference mass for the conversion of the fragment ion
     * tolerance from ppm to Dalton.
     *
     * @return the reference mass for the conversion of the fragment ion
     * tolerance from ppm to Dalton
     */
    public double getRefMass() {
        return refMass;
    }

    /**
     * Sets the reference mass for the conversion of the fragment ion tolerance
     * from ppm to Dalton.
     *
     * @param refMass the reference mass for the conversion of the fragment ion
     * tolerance from ppm to Dalton
     */
    public void setRefMass(double refMass) {
        this.refMass = refMass;
    }

    /**
     * Sets how SearchGUI output files should be organized.
     *
     * @param outputOption the SearchGUI output option
     */
    public void setSearchGuiOutputParameters(OutputParameters outputOption) {
        this.outputOption = outputOption;
    }

    /**
     * Returns the selected SearchGUI output option.
     *
     * @return the selected SearchGUI output option
     */
    public OutputParameters getSearchGuiOutputParameters() {
        return outputOption;
    }

    /**
     * Indicates whether data should be copied along with the identification
     * files in the SearchGUI output.
     *
     * @return a boolean indicating whether data should be copied along with the
     * identification files in the SearchGUI output
     */
    public boolean outputData() {
        return outputData;
    }

    /**
     * Sets whether data should be copied along with the identification files in
     * the SearchGUI output.
     *
     * @param outputData whether data should be copied along with the
     * identification files in the SearchGUI output
     */
    public void setOutputData(boolean outputData) {
        this.outputData = outputData;
    }

    /**
     * Indicates whether the date should be included in the SearchGUI output
     * name.
     *
     * @return a boolean indicating whether the date should be included in the
     * SearchGUI output name
     */
    public boolean isIncludeDateInOutputName() {
        return includeDateInOutputName;
    }

    /**
     * Sets whether the date should be included in the SearchGUI output name.
     *
     * @param includeDateInOutputName whether the date should be included in the
     * SearchGUI output name
     */
    public void setIncludeDateInOutputName(boolean includeDateInOutputName) {
        this.includeDateInOutputName = includeDateInOutputName;
    }

    /**
     * Returns true if the X! Tandem file should be renamed.
     *
     * @return true if the X! Tandem file should be renamed
     */
    public boolean renameXTandemFile() {
        return renameXTandemFile;
    }

    /**
     * Set if the X! Tandem file should be renamed.
     *
     * @param renameXTandemFile rename file?
     */
    public void setRenameXTandemFile(boolean renameXTandemFile) {
        this.renameXTandemFile = renameXTandemFile;
    }

    /**
     * Returns whether the spectra are to be checked for missing charges.
     *
     * @return true, if the spectra are to be checked for missing charges
     */
    public boolean isCheckSpectrumCharges() {
        return checkSpectrumCharges;
    }

    /**
     * Set if the spectra are to be checked for missing charges.
     *
     * @param checkSpectrumCharges the checkSpectrumCharges to set
     */
    public void setCheckSpectrumCharges(boolean checkSpectrumCharges) {
        this.checkSpectrumCharges = checkSpectrumCharges;
    }

    /**
     * Returns the minimum charge added when the charge is missing for a given
     * spectrum.
     *
     * @return the minimum charge added when the charge is missing for a given
     * spectrum
     */
    public int getMinSpectrumChargeRange() {
        return minSpectrumChargeRange;
    }

    /**
     * Set the minimum charge added when the charge is missing for a given
     * spectrum.
     *
     * @param minSpectrumChargeRange the minSpectrumChargeRange to set
     */
    public void setMinSpectrumChargeRange(int minSpectrumChargeRange) {
        this.minSpectrumChargeRange = minSpectrumChargeRange;
    }
    
    /**
     * Returns the maximum charge added when the charge is missing for a given
     * spectrum.
     *
     * @return the maximum charge added when the charge is missing for a given
     * spectrum
     */
    public int getMaxSpectrumChargeRange() {
        return maxSpectrumChargeRange;
    }

    /**
     * Set the maximum charge added when the charge is missing for a given
     * spectrum.
     *
     * @param maxSpectrumChargeRange the maxSpectrumChargeRange to set
     */
    public void setMaxSpectrumChargeRange(int maxSpectrumChargeRange) {
        this.maxSpectrumChargeRange = maxSpectrumChargeRange;
    }

    /**
     * Returns the default modifications.
     * 
     * @return the default modifications
     */
    public HashSet<String> getDefaultModifications() {
        return defaultModifications;
    }

    /**
     * Sets the default modifications.
     * 
     * @param defaultModifications the default modifications
     */
    public void setDefaultModifications(HashSet<String> defaultModifications) {
        this.defaultModifications = defaultModifications;
    }
    
    /**
     * Returns the folder where FASTA files summary statistics are stored.
     * 
     * @return the folder where FASTA files summary statistics are stored
     */
    public File getDbSummaryFolder() {
        
        return new File(getUserParametersFolder(), "fastaSummary");
    }
    
    /**
     * Sets the default list of modifications.
     */
    private static HashSet<String> defaultModifications() {
        
        HashSet<String> defaultList = new HashSet<>(12);
        
        defaultList.add("Acetylation of K");
        defaultList.add("Acetylation of protein N-term");
        defaultList.add("Carbamidomethylation of C");
        defaultList.add("Deamidation of N");
        defaultList.add("Deamidation of Q");
        defaultList.add("Oxidation of M");
        defaultList.add("Phosphorylation of S");
        defaultList.add("Phosphorylation of T");
        defaultList.add("Phosphorylation of Y");
        defaultList.add("Pyrolidone from E");
        defaultList.add("Pyrolidone from Q");
        defaultList.add("Pyrolidone from carbamidomethylated C");
        defaultList.add("TMT 10-plex of K");
        defaultList.add("TMT 10-plex of peptide N-term");
        defaultList.add("TMT 6-plex of K");
        defaultList.add("TMT 6-plex of peptide N-term");
        defaultList.add("iTRAQ 4-plex of K");
        defaultList.add("iTRAQ 4-plex of Y");
        defaultList.add("iTRAQ 4-plex of peptide N-term");
        defaultList.add("iTRAQ 8-plex of K");
        defaultList.add("iTRAQ 8-plex of Y");
        defaultList.add("iTRAQ 8-plex of peptide N-term");
        
        return defaultList;
        
    }
}
