package com.compomics.util;

import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.awt.Color;
import java.io.*;
import java.nio.channels.FileChannel;
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

        tableAsString += "\n";

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

            tableAsString += "\n";
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

        writer.write("\n");

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

            writer.write("\n");
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
}
