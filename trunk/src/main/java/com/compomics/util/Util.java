package com.compomics.util;

import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.awt.Color;
import java.awt.Component;
import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/**
 * Includes general help methods that are used by the other classes.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class Util {

    /**
     * Forbidden characters in file names.
     */
    public static final String[] forbiddenCharacters = {"!", ":", ";", "\\?", "/", "\\\\", "\\*", "<", ">", "\"", "\\|"};
    /**
     * Separator which can be used to separate things.
     */
    public static final String separator = "_cus_";

    /**
     * Removes the forbidden characters from a string.
     *
     * @param string the string of interest
     * @return a version without forbidden characters
     */
    public static String removeForbiddenCharacters(String string) {
        String result = string;
        for (String fc : forbiddenCharacters) {
            String[] split = string.split(fc);
            result = "";
            for (String splitPart : split) {
                result += splitPart;
            }
        }
        return result;
    }

    /**
     * Indicates whether a string contains characters forbidden in file names.
     *
     * @param string the string of interest
     * @return a boolean indicating whether a string contains characters
     * forbidden in file names
     */
    public static boolean containsForbiddenCharacter(String string) {
        for (String forbiddenCharacter : forbiddenCharacters) {
            if (string.contains(forbiddenCharacter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rounds a double value to the wanted number of decimal places.
     *
     * @param d the double to round of
     * @param places number of decimal places wanted
     * @return double - the new double
     */
    public static double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10, (double) places);
    }

    /**
     * Floors a double value to the wanted number of decimal places.
     *
     * @param d the double to floor of
     * @param places number of decimal places wanted
     * @return double - the new double
     */
    public static double floorDouble(double d, int places) {
        return (new BigDecimal(String.valueOf(d)).setScale(places, BigDecimal.ROUND_FLOOR)).doubleValue();
    }

    /**
     * Deletes all files and subdirectories under dir. Returns true if all
     * deletions were successful. If a deletion fails, the method stops
     * attempting to delete and returns false.
     *
     * @param dir the directory to delete
     * @return rue if all deletions were successful
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                boolean success = deleteDir(child);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Returns the ppm value of the given mass error relative to its theoretical
     * m/z value.
     *
     * @param theoreticalMzValue the theoretical mass
     * @param massError the mass error
     * @return the mass error as a ppm value relative to the theoretical mass
     */
    public static double getPpmError(double theoreticalMzValue, double massError) {
        double ppmValue = (massError / theoreticalMzValue) * 1000000;
        return ppmValue;
    }

    /**
     * Converts a color to hex format for use in HTML tags.
     *
     * @param color the color to convert
     * @return the color in hex format
     */
    public static String color2Hex(Color color) {
        return Integer.toHexString(color.getRGB() & 0x00ffffff);
    }

    /**
     * An OS independent getName alternative. Useful if the path is provided as
     * a hardcoded string and opened in a different OS.
     *
     * @param filePath the file path as a string
     * @return the file name, or the complete path of no file name is detected
     */
    public static String getFileName(String filePath) {

        String tempFileName = filePath;

        int slash1 = tempFileName.lastIndexOf("/");
        int slash2 = tempFileName.lastIndexOf("\\");

        int lastSlashIndex = Math.max(slash1, slash2);

        if (lastSlashIndex != -1) {
            tempFileName = tempFileName.substring(lastSlashIndex + 1);
        }

        return tempFileName;
    }

    /**
     * An OS independent getName alternative. Useful if the path is provided as
     * a hardcoded string and opened in a different OS.
     *
     * @param file the file
     * @return the file name, or the complete path of no file name is detected
     */
    public static String getFileName(File file) {
        return getFileName(file.getAbsolutePath());
    }

    /**
     * Returns the extensions of a file.
     *
     * @param file the file
     * @return the extension of a file
     */
    public static String getExtension(File file) {
        String fileName = getFileName(file.getAbsolutePath());
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * Appends a suffix to a file name before the file extension.
     *
     * @param fileName the file name
     * @param suffix the suffix to add
     * @return the file name with suffix
     */
    public static String appendSuffix(String fileName, String suffix) {
        String tempName;
        String extension;
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex > -1) {
            tempName = fileName.substring(0, fileName.lastIndexOf("."));
            extension = fileName.substring(fileName.lastIndexOf("."));
        } else {
            tempName = fileName;
            extension = "";
        }
        return tempName + suffix + extension;
    }

    /**
     * Removes the extension from a file name or path.
     *
     * @param fileName the file name
     * @return the file name without extension
     */
    public static String removeExtension(String fileName) {
        int poinIndex = fileName.lastIndexOf(".");
        if (poinIndex > -1) {
            return fileName.substring(0, poinIndex);
        } else {
            return fileName;
        }
    }

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
     * @param openDialog if true an open dialog is shown, false results in a
     * save dialog
     * @return the file selected by the user, or null if no file was selected
     */
    public static File getUserSelectedFile(Component parent, String aFileEnding, String aFileFormatDescription, String aDialogTitle, String lastSelectedFolder, boolean openDialog) {

        // @TODO: should support multiple file endings, e.g., fasta and fast for example
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
            String aDialogTitle, String lastSelectedFolder, boolean openDialog, boolean formatSelectedByUser, boolean showAllFilesOption, int defaultFilterIndex) {

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

    /**
     * Returns the table as a separated text file.
     *
     * @param table the table to turn in to text
     * @param separator the text separator
     * @param progressDialog the progress dialog
     * @param removeHtml if true, HTML is converted to text
     * 
     * @return the table as a separated text file
     */
    public static String tableToText(JTable table, String separator, ProgressDialogX progressDialog, boolean removeHtml) {

        StringBuilder tableAsString = new StringBuilder();

        for (int i = 0; i < table.getColumnCount() && !progressDialog.isRunCanceled(); i++) {
            tableAsString.append(table.getColumnName(i)).append(separator);
        }

        progressDialog.setPrimaryProgressCounterIndeterminate(false);
        progressDialog.setMaxPrimaryProgressCounter(table.getRowCount());

        tableAsString.append(System.getProperty("line.separator"));

        for (int i = 0; i < table.getRowCount() && !progressDialog.isRunCanceled(); i++) {

            progressDialog.increasePrimaryProgressCounter();

            for (int j = 0; j < table.getColumnCount() && !progressDialog.isRunCanceled(); j++) {

                if (table.getValueAt(i, j) != null) {
                    String tempValue = table.getValueAt(i, j).toString();

                    // remove html tags
                    if (tempValue.indexOf("<html>") != -1 && removeHtml) {
                        tempValue = tempValue.replaceAll("\\<[^>]*>", "");
                    }

                    tableAsString.append(tempValue).append(separator);
                } else {
                    tableAsString.append(separator);
                }
            }

            tableAsString.append(System.getProperty("line.separator"));
        }

        return tableAsString.toString();
    }

    /**
     * Writes the table to a file as separated text.
     *
     * @param table the table to write to file
     * @param separator the text separator
     * @param progressDialog the progress dialog
     * @param removeHtml if true, HTML is converted to text
     * @param writer the writer where the file is to be written
     * 
     * @throws IOException if a problem occurs when writing to the file
     */
    public static void tableToFile(JTable table, String separator, ProgressDialogX progressDialog, boolean removeHtml, BufferedWriter writer) throws IOException {

        for (int i = 0; i < table.getColumnCount(); i++) {
            writer.write(table.getColumnName(i) + separator);
        }

        if (progressDialog != null) {
            progressDialog.setPrimaryProgressCounterIndeterminate(false);
            progressDialog.setMaxPrimaryProgressCounter(table.getRowCount());
        }

        writer.write(System.getProperty("line.separator"));

        for (int i = 0; i < table.getRowCount(); i++) {

            if (progressDialog != null) {
                if (progressDialog.isRunCanceled()) {
                    return;
                }
                progressDialog.increasePrimaryProgressCounter();
            }

            for (int j = 0; j < table.getColumnCount(); j++) {

                if (progressDialog != null) {
                    if (progressDialog.isRunCanceled()) {
                        return;
                    }
                }

                if (table.getValueAt(i, j) != null) {
                    String tempValue = table.getValueAt(i, j).toString();

                    // remove html tags
                    if (tempValue.indexOf("<html>") != -1 && removeHtml) {
                        tempValue = tempValue.replaceAll("\\<[^>]*>", "");
                    }

                    writer.write(tempValue + separator);
                } else {
                    writer.write(separator);
                }
            }

            writer.write(System.getProperty("line.separator"));
        }
    }

    /**
     * Copy the content of one file to another.
     *
     * @param in the file to copy from
     * @param out the file to copy to
     * 
     * @throws IOException if a problem occurs when writing to the file
     */
    public static void copyFile(File in, File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * Returns the number of lines in the given file.
     *
     * @param file the file to find the number of lines in
     * 
     * @return the number of lines in the given file
     * 
     * @throws IOException if a problem occurs when writing to the file
     */
    public static int getNumberOfLines(File file) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        try {
            byte[] c = new byte[1024];
            int lineCount = 0;
            int readChars;
            while ((readChars = inputStream.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++lineCount;
                    }
                }
            }
            return lineCount;
        } finally {
            inputStream.close();
        }
    }

    /**
     * Convenience methods indicating whether the content of two lists have the
     * same content. Equality is based on the hash of the objects.
     *
     * @param list1 the first list
     * @param list2 the second list
     * 
     * @return a boolean indicating whether list1 has the same content as list2
     */
    public static boolean sameLists(ArrayList list1, ArrayList list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        HashSet set1 = new HashSet(list1);
        HashSet set2 = new HashSet(list2);
        return sameSets(set1, set2);
    }

    /**
     * Convenience methods indicating whether the content of two sets have the
     * same content. Equality is based on the hash of the objects.
     *
     * @param set1 the first set
     * @param set2 the second set
     *
     * @return a boolean indicating whether list1 has the same content as list2
     */
    public static boolean sameSets(HashSet set1, HashSet set2) {
        if (set1.size() != set2.size()) {
            return false;
        }
        for (Object object1 : set1) {
            if (!set2.contains(object1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convenience methods indicating whether the content of two lists have the
     * same content. Assumes that the first list is already sorted.
     *
     * @param list1 the first list
     * @param list2 the second list
     * @return a boolean indicating whether list1 has the same content as list2
     */
    public static boolean sameListsFirstListSorted(ArrayList list1, ArrayList list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        ArrayList list2copy = new ArrayList(list2);
        Collections.sort(list2copy);
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2copy.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the occurrence of a character in a string.
     *
     * @param input the string of interest
     * @param character the character to look for
     *
     * @return the occurrence of a character in a string
     */
    public static int getOccurrence(String input, char character) {
        int result = 0;
        for (char aa : input.toCharArray()) {
            if (aa == character) {
                result++;
            }
        }
        return result;
    }

    /**
     * Returns at which indexes a small string can be found in a big string.
     *
     * @param bigString the big string
     * @param smallString the small string
     * @return a list of the indexes where the small string can be found in the
     * big string
     */
    public static ArrayList<Integer> getIndexes(String bigString, String smallString) {
        Pattern pattern = Pattern.compile(smallString);
        ArrayList<Integer> result = new ArrayList<Integer>();
        Matcher matcher = pattern.matcher(bigString);
        matcher.matches();
        int index = 0;
        while (matcher.find(index)) {
            index = matcher.start();
            index++;
            result.add(index);
        }
        return result;
    }

    /**
     * Method for reading a double value as a string which uses either "," or
     * "." as the decimal symbol.
     *
     * @param doubleAsString the double value as a string
     * @return the double value
     * @throws NumberFormatException thrown if the double cannot be read as a
     * double
     */
    public static double readDoubleAsString(String doubleAsString) throws NumberFormatException {

        BigDecimal temp;
        try {
            temp = new BigDecimal(doubleAsString);
        } catch (NumberFormatException e) {
            doubleAsString = doubleAsString.replaceAll("\\.", "");
            doubleAsString = doubleAsString.replaceAll(",", "\\.");
            try {
                temp = new BigDecimal(doubleAsString);
            } catch (NumberFormatException ex) {
                throw new NumberFormatException(doubleAsString + " cannot be read as a floating value!");
            }
        }

        return temp.doubleValue();
    }
}
