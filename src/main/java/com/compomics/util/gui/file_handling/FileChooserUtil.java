package com.compomics.util.gui.file_handling;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class contains utilities functions for the file choosers.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class FileChooserUtil {

    /**
     * Returns the file selected by the user, or null if no file was selected.
     * Note that the last selected folder value is not updated during this
     * method, and the code calling this method therefore has to take care of
     * this if wanted.
     *
     * @param parent the parent dialog or frame
     * @param aFileEnding the file type, e.g., .txt
     * @param aFileFormatDescription the file format description, e.g., (Mascot
     * Generic Format) *.mgf
     * @param aDialogTitle the title for the dialog
     * @param lastSelectedFolder the last selected folder
     * @param aSuggestedFileName the suggested file name, can be null
     * @param openDialog if true an open dialog is shown, false results in a
     * save dialog
     * @return the file selected by the user, or null if no file was selected
     */
    public static File getUserSelectedFile(
            Component parent, 
            String aFileEnding, 
            String aFileFormatDescription, 
            String aDialogTitle, 
            String lastSelectedFolder, 
            String aSuggestedFileName, 
            boolean openDialog
    ) {

        final String fileEnding = aFileEnding;
        final String fileFormatDescription = aFileFormatDescription;
        final JFileChooser fileChooser = new JFileChooser(lastSelectedFolder);

        fileChooser.setDialogTitle(aDialogTitle);
        fileChooser.setMultiSelectionEnabled(false);

        javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File myFile) {
                return myFile.getName().toLowerCase().endsWith(fileEnding) || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return fileFormatDescription;
            }
        };

        fileChooser.setFileFilter(filter);

        int returnVal;

        if (openDialog) {
            returnVal = fileChooser.showOpenDialog(parent);
        } else {
            if (aSuggestedFileName != null) {
                fileChooser.setSelectedFile(new File(lastSelectedFolder, aSuggestedFileName));
            }
            returnVal = fileChooser.showSaveDialog(parent);
        }

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            String selectedFile = fileChooser.getSelectedFile().getPath();

            if (!openDialog && !selectedFile.endsWith(fileEnding)) {
                selectedFile += fileEnding;
            }

            File newFile = new File(selectedFile);
            int outcome = JOptionPane.YES_OPTION;

            if (!openDialog && newFile.exists()) {
                outcome = JOptionPane.showConfirmDialog(parent,
                        "Should " + selectedFile + " be overwritten?", "Selected File Already Exists",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            } else if (openDialog && !newFile.exists()) {
                JOptionPane.showMessageDialog(parent, "The file \'" + newFile.getAbsolutePath() + "\' does not exist!",
                        "File Not Found.", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            if (outcome != JOptionPane.YES_OPTION) {
                return null;
            } else {
                return newFile;
            }
        }

        return null;
    }

    /**
     * Returns the file selected by the user, or null if no file was selected.
     * Note that the last selected folder value is not updated during this
     * method, and the code calling this method therefore has to take care of
     * this if wanted.
     *
     * @param parent the parent dialog or frame
     * @param fileEndings the file types, e.g., .txt
     * @param fileFormatDescriptions the file format description, e.g., (Mascot
     * Generic Format) *.mgf
     * @param aDialogTitle the title for the dialog
     * @param lastSelectedFolder the last selected folder
     * @param aSuggestedFileName the suggested file name, can be null
     * @param openDialog if true an open dialog is shown, false results in a
     * save dialog
     * @param formatSelectedByUser if true the user will have to select the
     * format by himself, otherwise all formats will be available
     * @param showAllFilesOption if true, the 'All files' filter option will be
     * included
     * @param defaultFilterIndex the index of the filter selected by default
     *
     * @return the file selected and the file filter used, null if the selection
     * was canceled.
     */
    public static FileAndFileFilter getUserSelectedFile(Component parent, String[] fileEndings, String[] fileFormatDescriptions,
            String aDialogTitle, String lastSelectedFolder, String aSuggestedFileName, boolean openDialog, boolean formatSelectedByUser, boolean showAllFilesOption, int defaultFilterIndex) {

        JFileChooser fileChooser = new JFileChooser(lastSelectedFolder);

        fileChooser.setDialogTitle(aDialogTitle);
        fileChooser.setMultiSelectionEnabled(false);

        // see if we should hide the All option
        fileChooser.setAcceptAllFileFilterUsed(showAllFilesOption);

        if (formatSelectedByUser) {
            for (int i = 0; i < fileEndings.length; i++) {
                final String fileEnding = fileEndings[i];
                String description = "";
                if (i < fileFormatDescriptions.length && fileFormatDescriptions[i] != null) {
                    description = fileFormatDescriptions[i];
                }
                final String filterDescription = description;
                javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File myFile) {
                        return myFile.getName().toLowerCase().endsWith(fileEnding) || myFile.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return filterDescription;
                    }
                };
                fileChooser.addChoosableFileFilter(filter);

                if (i == defaultFilterIndex) {
                    fileChooser.setFileFilter(filter);
                }
            }
        } else {
            final String[] filterExtensionList = fileEndings.clone();
            String description = "";
            for (int i = 0; i < fileEndings.length; i++) {
                if (i < fileFormatDescriptions.length && fileFormatDescriptions[i] != null) {
                    if (!description.equals("")) {
                        description += ", ";
                    }
                    description += fileFormatDescriptions[i];
                } else {
                    if (!description.equals("")) {
                        description += ", ";
                    }
                    description += "Unkown";
                }
            }
            final String filterDescription = description;
            javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File myFile) {
                    if (myFile.isDirectory()) {
                        return true;
                    }
                    for (String fileEnding : filterExtensionList) {
                        if (myFile.getName().toLowerCase().endsWith(fileEnding)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return filterDescription;
                }
            };
            fileChooser.setFileFilter(filter);
        }

        int returnVal;

        if (openDialog) {
            returnVal = fileChooser.showOpenDialog(parent);
        } else {
            if (aSuggestedFileName != null) {
                fileChooser.setSelectedFile(new File(lastSelectedFolder, aSuggestedFileName));
            }
            returnVal = fileChooser.showSaveDialog(parent);
        }

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            String selectedFile = fileChooser.getSelectedFile().getPath();
            String fileFormatDescription = fileChooser.getFileFilter().getDescription();
            String wantedFileEnding = null;

            for (int i = 0; i < fileFormatDescriptions.length && wantedFileEnding == null; i++) {
                if (fileFormatDescriptions[i].equalsIgnoreCase(fileFormatDescription)) {
                    wantedFileEnding = fileEndings[i];
                }
            }

            // make sure the file has the correct file ending
            if (!openDialog && !selectedFile.endsWith(wantedFileEnding)) {
                selectedFile += wantedFileEnding;
            }

            File newFile = new File(selectedFile);
            int outcome = JOptionPane.YES_OPTION;

            if (!openDialog && newFile.exists()) {
                outcome = JOptionPane.showConfirmDialog(parent,
                        "Should " + selectedFile + " be overwritten?", "Selected File Already Exists",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            } else if (openDialog && !newFile.exists()) {
                JOptionPane.showMessageDialog(parent, "The file\'" + newFile.getAbsolutePath() + "\' " + "does not exist!",
                        "File Not Found.", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            if (outcome != JOptionPane.YES_OPTION) {
                return null;
            } else {
                return new FileAndFileFilter(newFile, fileChooser.getFileFilter());
            }
        }

        return null;
    }

    /**
     * Returns the folder selected by the user, or null if no folder was
     * selected. Note that the last selected folder value is not updated during
     * this method, and the code calling this method therefore has to take care
     * of this if wanted.
     *
     * @param parent the parent dialog or frame
     * @param aDialogTitle the title for the dialog
     * @param lastSelectedFolder the last selected folder
     * @param aFolderDescription the folder description, e.g., CPS Folder
     * @param approveButtonText the text on the approve button
     * @param openDialog if true the folder has to exist, if false the user will
     * be asked if he/she wants to create the folder is missing
     *
     * @return the file selected by the user, or null if no file was selected
     */
    public static File getUserSelectedFolder(Component parent, String aDialogTitle, String lastSelectedFolder, String aFolderDescription, String approveButtonText, boolean openDialog) {

        final JFileChooser fileChooser = new JFileChooser(lastSelectedFolder);
        final String folderDescription = aFolderDescription;

        fileChooser.setDialogTitle(aDialogTitle);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File myFile) {
                return myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return folderDescription;
            }
        };

        fileChooser.setFileFilter(filter);

        int returnVal = fileChooser.showDialog(parent, approveButtonText);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File selectedFolder = fileChooser.getSelectedFile();

            if (!selectedFolder.exists()) {
                if (openDialog) {
                    JOptionPane.showMessageDialog(parent, "The folder \'" + selectedFolder.getAbsolutePath() + "\' does not exist.\n"
                            + "Please choose an existing folder.", "Folder Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                } else {
                    int value = JOptionPane.showConfirmDialog(parent, "The folder \'" + selectedFolder.getAbsolutePath() + "\' does not exist.\n"
                            + "Do you want to create it?", "Create Folder?", JOptionPane.YES_NO_OPTION);
                    if (value == JOptionPane.NO_OPTION) {
                        return null;
                    } else { // yes option selected
                        boolean success = selectedFolder.mkdir();

                        if (!success) {
                            JOptionPane.showMessageDialog(parent, "Failed to create the folder. Please create it manually and then select it.",
                                    "File Error", JOptionPane.INFORMATION_MESSAGE);
                            return null;
                        }
                    }
                }
            }

            return selectedFolder;
        }

        return null;
    }

}
