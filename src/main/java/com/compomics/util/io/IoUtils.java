package com.compomics.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Utils for IO.
 *
 * @author Marc Vaudel
 */
public class IoUtils {

    /**
     * Default encoding, cf the second rule.
     */
    public static final String ENCODING = "UTF-8";
    /**
     * Default separator.
     */
    public static final String DEFAULT_SEPARATOR = "\t";

    /**
     * Copy the content of a file to another.
     *
     * @param in the file to copy from
     * @param out the file to copy to
     *
     * @throws IOException if a problem occurs when writing to the file
     */
    public static void copyFile(
            File in, 
            File out
    ) throws IOException {
        
        copyFile(in, out, true);
    
    }

    /**
     * Appends the content of a file to another.
     *
     * @param in the file to copy from
     * @param out the file to copy to
     *
     * @throws IOException if a problem occurs when writing to the file
     */
    public static void append(
            File in, 
            File out
    ) throws IOException {
    
        copyFile(in, out, false);
    
    }

    /**
     * Copy the content of one file to another.
     *
     * @param in the file to copy from
     * @param out the file to copy to
     * @param overwrite boolean indicating whether out should be overwritten
     *
     * @throws IOException if an error occurred while reading or writing a file
     */
    public static void copyFile(
            File in,
            File out,
            boolean overwrite
    ) throws IOException {

        long start = 0;

        if (out.exists() && out.length() > 0) {

            if (overwrite) {

                out.delete();

            } else {

                start = out.length();

            }
        }

        try ( FileChannel inChannel = new FileInputStream(in).getChannel()) {
            try ( FileChannel outChannel = new FileOutputStream(out).getChannel()) {

                inChannel.transferTo(start, inChannel.size(), outChannel);

            }
        }
    }

}
