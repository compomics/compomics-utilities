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
        int selection;
        if (!compomicsArtifactProperties.contains("create_shortcut")) {
            Object[] options = new Object[]{"yes", "no", "ask me next update"};
            boolean rememberOption = false;
            selection = JOptionPane.showOptionDialog(null, "do you want to create a desktop shortcut?", "shortcut", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.CANCEL_OPTION);
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
            Object[] options = {"yes...", "specify other location...", "quit"};
            int choice = JOptionPane.showOptionDialog(null, "there has been a problem with finding the location of the original file\n Do you want to download the latest update to your home folder or specify another location?", "specify download location", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.CANCEL_OPTION);
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
