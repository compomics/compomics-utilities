package com.compomics.util;

import java.util.Collection;

/**
 * Utility functions to work with arrays.
 *
 * @author Marc Vaudel
 */
public class ArrayUtil {
    

    /**
     * Appends the second array to the first array.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @param len2 The length of the second array to copy
     *
     * @return The concatenation of array1 and array2.
     */
    public static char[] concatenate(
            char[] array1, 
            char[] array2,
            int len2
    ) {
        
        char[] result = new char[array1.length + len2];
        
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, len2);
        
        return result;
    
    }

    /**
     * Appends the second array to the first array.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @param len2 The length of the second array to copy
     *
     * @return The concatenation of array1 and array2.
     */
    public static double[] concatenate(
            double[] array1, 
            double[] array2,
            int len2
    ) {
        
        double[] result = new double[array1.length + len2];
        
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, len2);
        
        return result;
    
    }

    /**
     * Convenience method to merge two byte arrays.
     *
     * @param array1 First byte array.
     * @param array2 Second byte array.
     * @param len2 The length of the second array to copy
     *
     * @return A concatenation of the first and the second arrays.
     */
    public static byte[] concatenate(
            byte[] array1,
            byte[] array2,
            int len2
    ) {

        byte[] result = new byte[array1.length + len2];

        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, len2);

        return result;

    }

    /**
     * Returns an array containing the unique characters of the given array.
     *
     * @param array the array
     *
     * @return the unique array
     */
    public static char[] makeUnique(char[] array) {

        char[] arrayUnique = new char[array.length];
        int index = 0;
        char aa = array[index];
        arrayUnique[index] = aa;
        for (int i = 1; i < array.length; i++) {

            aa = array[i];
            boolean duplicate = false;

            for (int j = 0; j < i; j++) {

                char aaTemp = array[j];

                if (aa == aaTemp) {

                    duplicate = true;
                    break;

                }
            }

            if (!duplicate) {

                arrayUnique[index] = aa;

            }
        }

        System.arraycopy(arrayUnique, 0, arrayUnique, 0, index);
        return arrayUnique;
    }

}
