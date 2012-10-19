package com.compomics.util;

import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.awt.Color;
import java.awt.Component;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Includes general help methods that are used by the other classes.
 *
 * @author Harald Barsnes
 */
public class Util {

    /**
     * Forbidden characters in file names.
     */
    public static final String[] forbiddenCharacters = {"!", ":", "\\?", "/", "\\\\", "\\*", "<", ">", "\"", "\\|"};

    /**
     * Removes the forbidden characters from a string
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
     * Deletes all files and subdirectories under dir. Returns true if all
     * deletions were successful. If a deletion fails, the method stops
     * attempting to delete and returns false.
     *
     * @param dir
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

            if (!selectedFile.endsWith(fileEnding)) {
                selectedFile += fileEnding;
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
                return newFile;
            }
        }

        return null;
    }

    /**
     * Returns the table as a separated text file.
     *
     * @param table the table to turn in to text
     * @param separator the text separator
     * @param progressDialog the progress dialog
     * @param removeHtml if true, html is converted to text
     * @return the table as a separated text file
     */
    public static String tableToText(JTable table, String separator, ProgressDialogX progressDialog, boolean removeHtml) {

        String tableAsString = "";

        for (int i = 0; i < table.getColumnCount() && !progressDialog.isRunCanceled(); i++) {
            tableAsString += ((DefaultTableModel) table.getModel()).getColumnName(i) + separator;
        }

        progressDialog.setIndeterminate(false);
        progressDialog.setMaxProgressValue(table.getRowCount());

        tableAsString += System.getProperty("line.separator");

        for (int i = 0; i < table.getRowCount() && !progressDialog.isRunCanceled(); i++) {

            progressDialog.increaseProgressValue();

            for (int j = 0; j < table.getColumnCount() && !progressDialog.isRunCanceled(); j++) {

                if (table.getValueAt(i, j) != null) {
                    String tempValue = table.getValueAt(i, j).toString();

                    // remove html tags
                    if (tempValue.indexOf("<html>") != -1 && removeHtml) {
                        tempValue = tempValue.replaceAll("\\<[^>]*>", "");
                    }

                    tableAsString += tempValue + separator;
                } else {
                    tableAsString += separator;
                }
            }

            tableAsString += System.getProperty("line.separator");
        }

        return tableAsString;
    }

    /**
     * Writes the table to a file as separated text.
     *
     * @param table the table to write to file
     * @param separator the text separator
     * @param progressDialog the progress dialog
     * @param removeHtml if true, html is converted to text
     * @param writer the writer where the file is to be written
     * @throws IOException
     */
    public static void tableToFile(JTable table, String separator, ProgressDialogX progressDialog, boolean removeHtml, BufferedWriter writer) throws IOException {

        for (int i = 0; i < table.getColumnCount() && !progressDialog.isRunCanceled(); i++) {
            writer.write(((DefaultTableModel) table.getModel()).getColumnName(i) + separator);
        }

        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMaxProgressValue(table.getRowCount());
        }

        writer.write(System.getProperty("line.separator"));

        for (int i = 0; i < table.getRowCount() && !progressDialog.isRunCanceled(); i++) {

            if (progressDialog != null) {
                progressDialog.increaseProgressValue();
            }

            for (int j = 0; j < table.getColumnCount() && !progressDialog.isRunCanceled(); j++) {

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
     * @throws IOException
     */
    public static void copyFile(File in, File out) throws IOException {

        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
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
     * @return the number of lines in the given file
     * @throws IOException
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
     * Aligns two series of integer, minimizing the distance between them.
     * Example: serie1 = {0, 1, 13, 25, 15, 6, 99} serie2 = {100, 2, 12, 14, 18,
     * 30, 115, 1000} result = {0->null, 1->2, 6->null, 13->12, 15->14, 25->18,
     * 99->100}
     *
     * @param serie1 first list of integer
     * @param serie2 second list of integer
     * @return a map of the doublets created.
     */
    public static HashMap<Integer, Integer> align(Collection<Integer> serie1, Collection<Integer> serie2) {
        HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
        if (serie1 == null || serie1.isEmpty()) {
            return result;
        }
        ArrayList<Integer> sortedSerie1 = new ArrayList<Integer>(serie1);
        Collections.sort(sortedSerie1);
        if (serie2 == null || serie2.isEmpty()) {
            for (int index : serie1) {
                result.put(index, null);
            }
            return result;
        }
        ArrayList<Integer> sortedSerie2 = new ArrayList<Integer>(serie2);
        Collections.sort(sortedSerie2);
        int lastj = 0;
        int firsti = 0;
        for (int i = 0; i < sortedSerie1.size() - 1; i++) {
            if (Math.abs(sortedSerie1.get(i + 1) - sortedSerie2.get(0)) < Math.abs(sortedSerie1.get(i) - sortedSerie2.get(0))) {
                result.put(i, null);
                firsti = i + 1;
            } else {
                break;
            }
        }
        for (int i = firsti; i < sortedSerie1.size(); i++) {
            Integer bestj = null;
            Integer bestDistance = null;
            if (lastj < sortedSerie2.size()) {
                for (int j = lastj; j < sortedSerie2.size(); j++) {
                    if (i < sortedSerie1.size() - 1
                            && (sortedSerie2.get(j) >= sortedSerie1.get(i + 1)
                            || Math.abs(sortedSerie2.get(j) - sortedSerie1.get(i + 1)) < Math.abs(sortedSerie2.get(j) - sortedSerie1.get(i)))) {
                        break;
                    }
                    if (bestDistance == null || Math.abs(sortedSerie2.get(j) - sortedSerie1.get(i)) < bestDistance) {
                        bestDistance = Math.abs(sortedSerie2.get(j) - sortedSerie1.get(i));
                        bestj = j;
                    }
                }
            }
            if (bestj != null) {
                result.put(sortedSerie1.get(i), sortedSerie2.get(bestj));
                lastj = bestj + 1;
            } else {
                result.put(sortedSerie1.get(i), null);
            }
        }
        return result;
    }
}
