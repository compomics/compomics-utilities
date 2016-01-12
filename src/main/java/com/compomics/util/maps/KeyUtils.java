package com.compomics.util.maps;

import java.util.ArrayList;

/**
 * Converts complex keys into simpler objects.
 *
 * @author Marc Vaudel
 */
public class KeyUtils {

    public final static String separator = "_";

    /**
     * Returns the key corresponding to a list of integers.
     *
     * @param list the list
     *
     * @return a key in the form of a string
     */
    public static String getKey(ArrayList<Integer> list) {
        StringBuilder stringBuilder = new StringBuilder(2 * list.size());
        for (Integer value : list) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(separator);
            }
            stringBuilder.append(value.toString());
        }
        return stringBuilder.toString();
    }

}
