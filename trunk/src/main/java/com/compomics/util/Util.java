package com.compomics.util;

import java.io.File;

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
}
