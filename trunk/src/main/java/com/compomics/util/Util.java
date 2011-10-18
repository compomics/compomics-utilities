package com.compomics.util;

import com.compomics.util.gui.dialogs.ProgressDialogX;
import java.awt.Color;
import java.io.File;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Includes general help methods that are used by the other classes.
 *
 * @author  Harald Barsnes
 */
public class Util {

    /**
     * Rounds a double value to the wanted number of decimalplaces.
     *
     * @param d the double to round of
     * @param places number of decimal places wanted
     * @return double - the new double
     */
    public static double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10, (double) places);
    }

    /**
     * Deletes all files and subdirectories under dir. Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     *
     * @param dir
     * @return rue if all deletions were successful
     */
    public static boolean deleteDir(File dir) {

        boolean success = false;

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        } else {
            // file is NOT a directory
            return false;
        }

        return dir.delete();
    }

    /**
     * Returns the ppm value of the given mass error relative to its
     * theoretical m/z value.
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
     * @param color     the color to convert
     * @return          the color in hex format
     */
    public static String color2Hex(Color color) {
        return Integer.toHexString(color.getRGB() & 0x00ffffff);
    }
    
    /**
     * An OS independent getName alternative. Useful if the path is provided 
     * as a hardcoded string and opened in a different OS.
     * 
     * @param filePath  the file path as a string
     * @return          the file name, or the complete path of no file name is detected
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
     * @param table             the table to turn in to text
     * @param separator         the text separator
     * @param progressDialog    the progress dialog
     * @param removeHtml        if true, html is converted to text
     * @return                  the table as a separated text file
     */
    public static String tableToText(JTable table, String separator, ProgressDialogX progressDialog, boolean removeHtml) {

        String tableAsString = "";

        for (int i = 0; i < table.getColumnCount(); i++) {
            tableAsString += ((DefaultTableModel) table.getModel()).getColumnName(i) + separator;
        }

        progressDialog.setIndeterminate(false);
        progressDialog.setMax(table.getRowCount());

        tableAsString += "\n";

        for (int i = 0; i < table.getRowCount(); i++) {

            progressDialog.incrementValue();

            for (int j = 0; j < table.getColumnCount(); j++) {
                
                String tempValue = table.getValueAt(i, j).toString();
                
                // remove html tags
                if (tempValue.indexOf("<html>") != -1 && removeHtml) {
                    tempValue = tempValue.replaceAll("\\<[^>]*>","");
                }
                
                tableAsString += tempValue + separator;
            }

            tableAsString += "\n";
        }

        return tableAsString;
    }
}
