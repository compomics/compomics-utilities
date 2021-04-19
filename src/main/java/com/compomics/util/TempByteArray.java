package com.compomics.util;

/**
 * Convenience class for storing an array buffer and a length.
 *
 * @author Marc Vaudel
 */
public class TempByteArray {

    /**
     * The byte array.
     */
    public final byte[] array;
    /**
     * The length used.
     */
    public final int length;

    /**
     * Constructor.
     *
     * @param array The array.
     * @param length The length used.
     */
    public TempByteArray(
            byte[] array,
            int length
    ) {

        this.array = array;
        this.length = length;

    }

}
