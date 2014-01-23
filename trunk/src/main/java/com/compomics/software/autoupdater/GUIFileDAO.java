package com.compomics.software.autoupdater;

import com.compomics.util.gui.UtilitiesGUIDefaults;
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
    public boolean createDesktopShortcut(MavenJarFile file, String iconName, boolean deleteOldShortcut) throws IOException {

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

            // set the look and feel
            try {
                UtilitiesGUIDefaults.setLookAndFeel();
            } catch (Exception e) {
                // ignore error, use default look and feel
            }

            Object[] options = new Object[]{"Yes", "No", "Ask me next update"};
            boolean rememberOption = false;
            int selection = JOptionPane.showOptionDialog(null, "Do you want to create a desktop shortcut?",
                    "Create Desktop Shortcut?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.CANCEL_OPTION);

            //also check (as in add checkbox) to remember choice
            if (selection == JOptionPane.CANCEL_OPTION || selection == JOptionPane.CLOSED_OPTION || rememberOption) {
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
                if (selection == JOptionPane.YES_OPTION) {
                    addShortcutAtDeskTop(file, iconName);
                }
                if (deleteOldShortcut) {
                    for (String fileName : new File(System.getProperty("user.home")).list()) {
                        // @TODO: do something here??
                    }
                }
            } catch (NullPointerException npe) {
                throw new IOException("could not create the shortcut");
            } catch (NumberFormatException nfe) {
                throw new IOException("could not create the shortcut");
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
                throw new IOException("no download location");
            }
        }
        return file;
    }
}
