package com.compomics.util.experiment.io.mass_spectrometry.cms;

import static com.compomics.util.io.IoUtil.ENCODING;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;

/**
 * Utils to store ms files.
 *
 * @author Marc Vaudel
 */
public class CmsFileUtils {

    /**
     * The file extension for cms files.
     */
    public static final String EXTENSION = ".cms";
    /**
     * The separator to use when concatenating spectrum titles in a string.
     */
    public static final String TITLE_SEPARATOR = "\t";
    /**
     * The magic number of currently supported CMS files.
     */
    public static final byte[] MAGIC_NUMBER = getMagicNumber();

    /**
     * Returns the magic number of currently supported CMS files.
     *
     * @return The magic number of currently supported CMS files.
     */
    public static byte[] getMagicNumber() {

        try {

            String magicName = "CmsFile.1";
            return magicName.getBytes(ENCODING);

        } catch (UnsupportedEncodingException e) {

            throw new RuntimeException(e);

        }
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
    public static byte[] mergeArrays(
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
     * Attempts at closing a buffer. Taken from
     * https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java.
     *
     * @param buffer the buffer to close
     */
    public static void closeBuffer(MappedByteBuffer buffer) {

        if (buffer == null || !buffer.isDirect()) {
            return;
        }

        try {

            Method cleaner = buffer.getClass().getMethod("cleaner");
            cleaner.setAccessible(true);
            Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
            clean.setAccessible(true);
            clean.invoke(cleaner.invoke(buffer));

        } catch (Exception ex) {
        }
    }

}
