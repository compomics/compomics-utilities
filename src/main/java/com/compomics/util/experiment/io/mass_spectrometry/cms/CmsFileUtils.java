package com.compomics.util.experiment.io.mass_spectrometry.cms;

import static com.compomics.util.io.IoUtil.ENCODING;
import java.io.UnsupportedEncodingException;

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

            String magicName = "CmsFile.1.1";
            return magicName.getBytes(ENCODING);

        } catch (UnsupportedEncodingException e) {

            throw new RuntimeException(e);

        }
    }

}
