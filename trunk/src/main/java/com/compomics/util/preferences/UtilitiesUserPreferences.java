package com.compomics.util.preferences;

import com.compomics.util.io.SerializationUtils;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;

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
    public static final String USER_PREFERENCES_FILE = System.getProperty("user.home") + "/.compomics/userpreferences.cup";
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
     * The color of the selected peptide.
     */
    private Color peptideSelected = new Color(0, 0, 255);
    /**
     * The memory to use by PeptideShaker.
     */
    private int memoryPreference = 4 * 1024;
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
     * The user last used database folder
     */
    private File dbFolder = null;
    /**
     * The user last used databases
     */
    private ArrayList<File> favoriteDBs = null;

    /**
     * Constructor
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
     * Returns the preferred upper memory limit.
     *
     * @return the preferred upper memory limit
     */
    public int getMemoryPreference() {
        if (memoryPreference == 0) {
            // needed for backward compatibility
            memoryPreference = 4 * 1024;
        }
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
     * @param userPreferences
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
     * Retries the user preferences. If an error is encountered, preferences are
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
     * Returns the last used databases. The most recent ones first.
     * 
     * @return the last used databases.
     */
    public ArrayList<File> getFavoriteDBs() {
        if (favoriteDBs == null) {
            // backward compatibility check
            favoriteDBs = new ArrayList<File>();
        } else {
        checkDbFiles();
        }
        return favoriteDBs;
    }
    
    /**
     * Removes the db files which do not exist anymore
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
     * @param favoriteDBs the last used databases.
     */
    public void addFavoriteDB(File dbFile) {
        if (favoriteDBs == null) {
            favoriteDBs = new ArrayList<File>();
        }
        favoriteDBs.add(0, dbFile);
    }
}
