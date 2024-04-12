package com.compomics.util.experiment.io.mass_spectrometry.cms;

import com.compomics.util.io.IoUtil;
import java.io.UnsupportedEncodingException;

/**
 * Utils to store ms files.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
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
     * The maximum size per mapped buffer.
     */
    public static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE / 2;

    /**
     * Returns the magic number of currently supported CMS files.
     *
     * @return The magic number of currently supported CMS files.
     */
    public static byte[] getMagicNumber() {

        try {

            String magicName = "CmsFile.1.4";
            return magicName.getBytes(IoUtil.ENCODING);

        } catch (UnsupportedEncodingException e) {

            throw new RuntimeException(e);

        }

    }

}
