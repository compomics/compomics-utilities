package com.compomics.software.autoupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * GUIFileDAO.
 *
 * @author Davy Maddelein
 * @author Harald Barsnes
 */
public class GUIFileDAO extends FileDAO {

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean createDesktopShortcut(MavenJarFile file, String iconName, String toolName, boolean deleteOldShortcut) throws IOException {

        Properties compomicsArtifactProperties = new Properties();
        File compomicsArtifactPropertiesFile = new File(new StringBuilder().append(System.getProperty("user.home")).append("/.compomics/").append(file.getArtifactId()).append("/updatesettings.properties").toString());
        FileReader propFileReader = null;

        if (compomicsArtifactPropertiesFile.exists()) {
            try {
                propFileReader = new FileReader(compomicsArtifactPropertiesFile);
                if (compomicsArtifactPropertiesFile.exists()) {
                    compomicsArtifactProperties.load(propFileReader);
                }
            } finally {
                if (propFileReader != null) {
                    propFileReader.close();
                }
            }
        } else {
            compomicsArtifactProperties = new Properties();
        }

        if (!compomicsArtifactProperties.contains("create_shortcut")) {

            int selection = JOptionPane.showConfirmDialog(null,
                    "Create a shortcut to " + toolName + " on the desktop?",
                    "Create Desktop Shortcut?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            //also check (as in add checkbox) to remember choice
            if (selection == JOptionPane.CANCEL_OPTION || selection == JOptionPane.CLOSED_OPTION) {
                compomicsArtifactProperties.setProperty("create_shortcut", String.valueOf(selection));
                FileOutputStream propOutputStream = null;
                try {
                    propOutputStream = new FileOutputStream(compomicsArtifactPropertiesFile);
                    compomicsArtifactProperties.store(propOutputStream, null);
                } finally {
                    if (propOutputStream != null) {
                        propOutputStream.close();
                    }
                }
            }
            try {
                // delete the add desktop icon trigger file for the new download
                String filePath = new StringBuilder().append(new File(file.getAbsoluteFilePath()).getParentFile().getAbsolutePath()).append("/resources/conf/firstRun").toString();
                if (new File(filePath).exists()) {
                    new File(filePath).delete();
                }
                if (selection == JOptionPane.YES_OPTION) {
                    addShortcutAtDeskTop(file, iconName);
                }
                if (deleteOldShortcut) {
                    for (String fileName : new File(System.getProperty("user.home")).list()) {
                        // @TODO: do something here??
                    }
                }
            } catch (NullPointerException npe) {
                throw new IOException("Could not create the desktop shortcut.");
            } catch (NumberFormatException nfe) {
                throw new IOException("Could not create the desktop shortcut.");
            }
        } else {
            // @TODO: do something here??
        }

        return true;
    }

    /**
     *
     * {@inheritDoc }
     */
    @Override
    public File getLocationToDownloadOnDisk(String targetDownloadFolder) throws IOException {
        File file = new File(targetDownloadFolder).getParentFile();
        if (file.exists() && !file.isDirectory()) {
            file = file.getParentFile();
        }
        if (file == null) {
            Object[] options = {"Yes", "Specify Other Location", "Quit"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Cannot find the location of the original file. Download\n"
                    + "to your home folder or specify another location?", "Download Location",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.CANCEL_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                file = new File(System.getProperty("users.home"));
            } else if (choice == JOptionPane.NO_OPTION) {
                JFileChooser fileChooser = new JFileChooser(System.getProperty("users.home"));
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setVisible(true);
                file = fileChooser.getSelectedFile();
            } else if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                throw new IOException("No download location.");
            }
        }
        return file;
    }
}
