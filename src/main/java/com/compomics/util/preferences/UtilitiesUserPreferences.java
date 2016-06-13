package com.compomics.util.preferences;

import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTreeComponentsFactory;
import com.compomics.util.io.SerializationUtils;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utilities user preferences will be serialized in the user folder and provide
 * useful information to all compomics software, well as soon as they use it of
 * course.
 *
 * @author Marc Vaudel
 */
public class UtilitiesUserPreferences implements Serializable {

    /**
     * Serial version UID for post-serialization compatibility.
     */
    static final long serialVersionUID = -4343570286224891504L;
    /**
     * Location of the user preferences file.
     */
    private static String USER_PREFERENCES_FILE = System.getProperty("user.home") + "/.compomics/userpreferences.cup";
    /**
     * The width to use for the annotated peaks.
     */
    private Float spectrumAnnotatedPeakWidth = 1.0f;
    /**
     * The width to use for the background peaks.
     */
    private Float spectrumBackgroundPeakWidth = 1.0f;
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
    private Integer memoryPreference = 4 * 1024;
    /**
     * The Java Home, for example, C:\Program Files\Java\jdk1.8.0_25\bin. Null
     * if not set. Note that this setting will be ignored of a JavaHome.txt file
     * is found.
     */
    private String javaHome = null;
    /**
     * The folder where to store the protein trees.
     */
    private File proteinTreeFolder = null;
    /**
     * Maps saving the protein trees import time in a map: FASTA file size &gt;
     * import times.
     */
    private HashMap<Long, ArrayList<Long>> proteinTreeImportTime;
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
     * The folder used to store fasta files.
     */
    private File proteinSequencesManagerFolder = null;
    /**
     * The user last used databases.
     */
    private ArrayList<File> favoriteDBs = null;
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
    private Boolean autoUpdate = true;
    /**
     * Indicates whether the tool should notify its start.
     * @deprecated replaced by autoUpdate only
     */
    private Boolean notifyStart = true;
    /**
     * The last selected folder.
     */
    private LastSelectedFolder lastSelectedFolder;
    /**
     * If true, the PSMs are sorted on retention time, false sorts on PSM score.
     */
    private Boolean sortPsmsOnRt = false;
    /**
     * The tag added after adding decoy sequences to a FASTA file.
     */
    private String targetDecoyFileNameTag = "_concatenated_target_decoy";
    /**
     * If true, the selected spectra will be checked for peak picking.
     */
    private Boolean checkPeakPicking = true;
    /**
     * If true, the selected spectra will be checked for duplicate spectrum
     * titles.
     */
    private Boolean checkDuplicateTitles = true;
    /**
     * If true, the mgf files will be checked for size.
     */
    private Boolean checkMgfSize = false;
    /**
     * If an mgf file exceeds this limit, the user will be asked for a split.
     */
    private Double mgfMaxSize = 1000.0;
    /**
     * Number of spectra allowed in the split file.
     */
    private Integer mgfNSpectra = 25000;
    /**
     * Reference mass for the conversion of the fragment ion tolerance from ppm
     * to Dalton.
     */
    private Double refMass = 2000.0;
    /**
     * If true the protein tree will be created parallel to the searches.
     * 
     * @deprecated no longer used
     */
    private Boolean generateProteinTree = false;
    /**
     * The way output files should be exported.
     */
    private SearchGuiOutputOption outputOption = SearchGuiOutputOption.grouped;
    /**
     * Indicates whether data files (mgf and FASTA) should be copied in the
     * output.
     */
    private Boolean outputData = false;
    /**
     * Indicates whether the date should be included in the output.
     */
    private Boolean includeDateInOutputName = false;
    /**
     * If true the X! Tandem file will be renamed.
     */
    private Boolean renameXTandemFile = true;
    /**
     * If true, the spectra will be checked for missing charges.
     */
    private Boolean checkSpectrumCharges = true;
    /**
     * The maximum charge added when the charge is missing for a given spectrum.
     */
    private Integer minSpectrumChargeRange = 2;
    /**
     * The minimum charge added when the charge is missing for a given spectrum.
     */
    private Integer maxSpectrumChargeRange = 4;

    /**
     * Constructor.
     */
    public UtilitiesUserPreferences() {
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
        if (sparklineColorPossible == null) {
            sparklineColorPossible = new Color(235, 235, 235);
        }
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
        if (sparklineColorDoubtful == null) {
            sparklineColorDoubtful = new Color(255, 204, 0);
        }
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
        if (sparklineColorFalsePositive == null) {
            sparklineColorFalsePositive = new Color(255, 51, 51);
        }
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
     * Returns the preferred upper memory limit in MB.
     *
     * @return the preferred upper memory limit
     */
    public Integer getMemoryPreference() {
        return memoryPreference;
    }

    /**
     * Sets the preferred upper memory limit.
     *
     * @param memoryPreference the preferred upper memory limit
     */
    public void setMemoryPreference(int memoryPreference) {
        this.memoryPreference = memoryPreference;
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
        if (spectrumAnnotatedPeakColor == null) {
            spectrumAnnotatedPeakColor = Color.RED;
        }
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
        if (spectrumAnnotatedMirroredPeakColor == null) {
            spectrumAnnotatedMirroredPeakColor = Color.BLUE;
        }
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
        if (spectrumBackgroundPeakColor == null) {
            spectrumBackgroundPeakColor = new Color(100, 100, 100, 50);
        }
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
    public Float getSpectrumAnnotatedPeakWidth() {
        if (spectrumAnnotatedPeakWidth == null) {
            spectrumAnnotatedPeakWidth = 1.0f;
        }
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
    public Float getSpectrumBackgroundPeakWidth() {
        if (spectrumBackgroundPeakWidth == null) {
            spectrumBackgroundPeakWidth = 1.0f;
        }
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
     * Convenience method saving the user preferences.
     *
     * @param userPreferences the user preferences
     */
    public static void saveUserPreferences(UtilitiesUserPreferences userPreferences) {

        try {
            File file = new File(USER_PREFERENCES_FILE);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            SerializationUtils.writeObject(userPreferences, file);
        } catch (Exception e) {
            System.err.println("An error occurred while saving " + USER_PREFERENCES_FILE + " (see below).");
            e.printStackTrace();
        }
    }

    /**
     * Loads the user preferences. If an error is encountered, preferences are
     * set back to default.
     *
     * @return returns the utilities user preferences
     */
    public static UtilitiesUserPreferences loadUserPreferences() {
        UtilitiesUserPreferences userPreferences;
        File file = new File(UtilitiesUserPreferences.USER_PREFERENCES_FILE);

        if (!file.exists()) {
            userPreferences = new UtilitiesUserPreferences();
            UtilitiesUserPreferences.saveUserPreferences(userPreferences);
        } else {
            try {
                userPreferences = (UtilitiesUserPreferences) SerializationUtils.readObject(file);
            } catch (Exception e) {
                System.err.println("An error occurred while loading " + UtilitiesUserPreferences.USER_PREFERENCES_FILE + " (see below). Preferences set back to default.");
                e.printStackTrace();
                userPreferences = new UtilitiesUserPreferences();
                UtilitiesUserPreferences.saveUserPreferences(userPreferences);
            }
        }

        return userPreferences;
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
     * returns the folder to use in the protein sequences manager.
     *
     * @return the folder to use in the protein sequences manager
     */
    public File getProteinSequencesManagerFolder() {
        if (proteinSequencesManagerFolder == null) { // Backward compatibility
            proteinSequencesManagerFolder = new File(System.getProperty("user.home") + "/.compomics/proteins/sequences/");
            if (!proteinSequencesManagerFolder.exists()) {
                proteinSequencesManagerFolder.mkdirs();
            }
        }
        return proteinSequencesManagerFolder;
    }

    /**
     * Sets the folder to use in the protein sequences manager.
     *
     * @param proteinSequencesManagerFolder the folder to use in the protein
     * sequences manager
     */
    public void setProteinSequencesManagerFolder(File proteinSequencesManagerFolder) {
        this.proteinSequencesManagerFolder = proteinSequencesManagerFolder;
    }

    /**
     * Returns the last used databases. The most recent ones first.
     *
     * @return the last used databases.
     */
    public ArrayList<File> getFavoriteDBs() {
        checkDbFiles();
        return favoriteDBs;
    }

    /**
     * Removes the db files which do not exist anymore.
     */
    public void checkDbFiles() {
        ArrayList<File> checkedFiles = new ArrayList<File>();
        for (File dbFile : favoriteDBs) {
            if (dbFile.exists()) {
                checkedFiles.add(dbFile);
            }
        }
        favoriteDBs = checkedFiles;
    }

    /**
     * Sets the last used databases.
     *
     * @param dbFile the last used databases.
     */
    public void addFavoriteDB(File dbFile) {
        if (favoriteDBs == null) {
            favoriteDBs = new ArrayList<File>();
        }
        favoriteDBs.add(0, dbFile);
    }

    /**
     * Returns the protein tree folder.
     *
     * @return the protein tree folder
     */
    public File getProteinTreeFolder() {
        if (proteinTreeFolder == null) {
            // if not set, set to default
            proteinTreeFolder = new File(ProteinTreeComponentsFactory.getDefaultDbFolderPath());
        }
        return proteinTreeFolder;
    }

    /**
     * Sets the protein tree folder.
     *
     * @param proteinTreeFolder the protein tree folder
     */
    public void setProteinTreeFolder(File proteinTreeFolder) {
        this.proteinTreeFolder = proteinTreeFolder;
    }

    /**
     * Returns the protein tree import times in a map: file size &gt; list of
     * import sizes.
     *
     * @return the protein tree import times
     */
    public HashMap<Long, ArrayList<Long>> getProteinTreeImportTime() {
        if (proteinTreeImportTime == null) {
            proteinTreeImportTime = new HashMap<Long, ArrayList<Long>>();
        }
        return proteinTreeImportTime;
    }

    /**
     * Adds a protein tree import time.
     *
     * @param fileSize the size of the FASTA file
     * @param importTime the import time
     */
    public void addProteinTreeImportTime(long fileSize, long importTime) {
        ArrayList<Long> importTimes = getProteinTreeImportTime().get(fileSize);
        if (importTimes == null) {
            importTimes = new ArrayList<Long>();
            proteinTreeImportTime.put(fileSize, importTimes);
        }
        importTimes.add(importTime);
    }

    /**
     * Clears the protein tree import times.
     */
    public void clearProteinTreeImportTimes() {
        if (proteinTreeImportTime != null) {
            proteinTreeImportTime.clear();
        }
    }

    /**
     * Returns the list of read tweets.
     *
     * @return the list of read tweets
     */
    public ArrayList<String> getReadTweets() {
        if (readTweets == null) {
            readTweets = new ArrayList<String>();
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
            displayedTips = new ArrayList<String>();
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
    public static String getUserPreferencesFile() {
        return USER_PREFERENCES_FILE;
    }

    /**
     * Returns the user preferences file to be used.
     *
     * @return the user preferences file
     */
    public static String getUserPreferencesFolder() {
        File tempFile = new File(getUserPreferencesFile());
        return tempFile.getParent();
    }

    /**
     * Sets the user preferences file to be used.
     *
     * @param userPreferencesFolder the user preferences file to be used
     */
    public static void setUserPreferencesFolder(String userPreferencesFolder) {
        File tempFile = new File(userPreferencesFolder, "/utilities_userpreferences.cup");
        UtilitiesUserPreferences.USER_PREFERENCES_FILE = tempFile.getAbsolutePath();
    }

    /**
     * Indicates whether the tools should use the auto update function.
     *
     * @return whether the tools should use the auto update function
     */
    public Boolean isAutoUpdate() {
        if (autoUpdate == null) {
            autoUpdate = true;
        }
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
     * Indicates whether the tools should notify their start.
     *
     * @return whether the tools should notify their start
     */
    public Boolean isNotifyStart() {
        if (notifyStart == null) {
            notifyStart = true;
        }
        return notifyStart;
    }

    /**
     * Sets whether the tools should notify their start.
     *
     * @param notifyStart whether the tools should notify their start
     */
    public void setNotifyStart(Boolean notifyStart) {
        this.notifyStart = notifyStart;
    }

    /**
     * Returns the last selected folder.
     *
     * @return the last selected folder
     */
    public LastSelectedFolder getLastSelectedFolder() {
        if (lastSelectedFolder == null) {
            lastSelectedFolder = new LastSelectedFolder();
        }
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
    public Boolean getSortPsmsOnRt() {
        if (sortPsmsOnRt == null) {
            sortPsmsOnRt = false;
        }
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
        if (targetDecoyFileNameTag == null) {
            targetDecoyFileNameTag = "_concatenated_target_decoy";
        }
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
    public Boolean checkPeakPicking() {
        if (checkPeakPicking == null) {
            checkPeakPicking = true;
        }
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
    public Boolean checkDuplicateTitles() {
        if (checkDuplicateTitles == null) {
            checkDuplicateTitles = true;
        }
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
    public Boolean checkMgfSize() {
        if (checkMgfSize == null) {
            checkMgfSize = false;
        }
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
        if (mgfMaxSize == null) {
            mgfMaxSize = 1000.0;
        }
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
        if (mgfNSpectra == null) {
            mgfNSpectra = 25000;
        }
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
    public Double getRefMass() {
        if (refMass == null) {
            refMass = 2000.0;
        }
        return refMass;
    }

    /**
     * Sets the reference mass for the conversion of the fragment ion tolerance
     * from ppm to Dalton.
     *
     * @param refMass the reference mass for the conversion of the fragment ion
     * tolerance from ppm to Dalton
     */
    public void setRefMass(Double refMass) {
        this.refMass = refMass;
    }

    /**
     * Returns true if the protein tree will be created parallel to the
     * searches.
     *
     * @return true if the protein tree will be created parallel to the searches
     * @deprecated no longer used
     */
    public Boolean generateProteinTree() {
        if (generateProteinTree == null) {
            generateProteinTree = false;
        }
        return generateProteinTree;
    }

    /**
     * Set if the the protein tree will be created parallel to the searches.
     *
     * @param generateProteinTree create protein tree?
     * @deprecated no longer used
     */
    public void setGenerateProteinTree(Boolean generateProteinTree) {
        this.generateProteinTree = generateProteinTree;
    }

    /**
     * Sets how SearchGUI output files should be organized.
     *
     * @param outputOption the SearchGUI output option
     */
    public void setOutputOption(SearchGuiOutputOption outputOption) {
        this.outputOption = outputOption;
    }

    /**
     * Returns the selected SearchGUI output option.
     *
     * @return the selected SearchGUI output option
     */
    public SearchGuiOutputOption getOutputOption() {
        if (outputOption == null) {
            outputOption = SearchGuiOutputOption.grouped;
        }
        return outputOption;
    }

    /**
     * Indicates whether data should be copied along with the identification
     * files in the SearchGUI output.
     *
     * @return a boolean indicating whether data should be copied along with the
     * identification files in the SearchGUI output
     */
    public Boolean outputData() {
        if (outputData == null) {
            outputData = false;
        }
        return outputData;
    }

    /**
     * Sets whether data should be copied along with the identification files in
     * the SearchGUI output.
     *
     * @param outputData whether data should be copied along with the
     * identification files in the SearchGUI output
     */
    public void setOutputData(Boolean outputData) {
        this.outputData = outputData;
    }

    /**
     * Indicates whether the date should be included in the SearchGUI output
     * name.
     *
     * @return a boolean indicating whether the date should be included in the
     * SearchGUI output name
     */
    public Boolean isIncludeDateInOutputName() {
        if (includeDateInOutputName == null) {
            includeDateInOutputName = false;
        }
        return includeDateInOutputName;
    }

    /**
     * Sets whether the date should be included in the SearchGUI output name.
     *
     * @param includeDateInOutputName whether the date should be included in the
     * SearchGUI output name
     */
    public void setIncludeDateInOutputName(Boolean includeDateInOutputName) {
        this.includeDateInOutputName = includeDateInOutputName;
    }

    /**
     * Returns true if the X! Tandem file should be renamed.
     *
     * @return true if the X! Tandem file should be renamed
     */
    public Boolean renameXTandemFile() {
        if (renameXTandemFile == null) {
            renameXTandemFile = true;
        }
        return renameXTandemFile;
    }

    /**
     * Set if the X! Tandem file should be renamed.
     *
     * @param renameXTandemFile rename file?
     */
    public void setRenameXTandemFile(Boolean renameXTandemFile) {
        this.renameXTandemFile = renameXTandemFile;
    }

    /**
     * Returns whether the spectra are to be checked for missing charges.
     *
     * @return true, if the spectra are to be checked for missing charges
     */
    public Boolean isCheckSpectrumCharges() {
        if (checkSpectrumCharges == null) {
            checkSpectrumCharges = true;
        }
        return checkSpectrumCharges;
    }

    /**
     * Set if the spectra are to be checked for missing charges.
     *
     * @param checkSpectrumCharges the checkSpectrumCharges to set
     */
    public void setCheckSpectrumCharges(Boolean checkSpectrumCharges) {
        this.checkSpectrumCharges = checkSpectrumCharges;
    }

    /**
     * Returns the minimum charge added when the charge is missing for a given
     * spectrum.
     *
     * @return the minimum charge added when the charge is missing for a given
     * spectrum
     */
    public Integer getMinSpectrumChargeRange() {
        if (minSpectrumChargeRange == null) {
            minSpectrumChargeRange = 2;
        }
        return minSpectrumChargeRange;
    }

    /**
     * Set the minimum charge added when the charge is missing for a given
     * spectrum.
     *
     * @param minSpectrumChargeRange the minSpectrumChargeRange to set
     */
    public void setMinSpectrumChargeRange(Integer minSpectrumChargeRange) {
        this.minSpectrumChargeRange = minSpectrumChargeRange;
    }
    
    /**
     * Returns the maximum charge added when the charge is missing for a given
     * spectrum.
     *
     * @return the maximum charge added when the charge is missing for a given
     * spectrum
     */
    public Integer getMaxSpectrumChargeRange() {
        if (maxSpectrumChargeRange == null) {
            maxSpectrumChargeRange = 4;
        }
        return maxSpectrumChargeRange;
    }

    /**
     * Set the maximum charge added when the charge is missing for a given
     * spectrum.
     *
     * @param maxSpectrumChargeRange the maxSpectrumChargeRange to set
     */
    public void setMaxSpectrumChargeRange(Integer maxSpectrumChargeRange) {
        this.maxSpectrumChargeRange = maxSpectrumChargeRange;
    }
}
