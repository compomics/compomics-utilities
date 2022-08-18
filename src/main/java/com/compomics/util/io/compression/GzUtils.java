package com.compomics.util.io.compression;

import static com.compomics.util.io.IoUtil.ENCODING;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Convenience methods to work with gzipped files. Note: IOExceptions are thrown
 * by each method as RuntimeException.
 *
 * @author Marc Vaudel
 */
public class GzUtils {

    /**
     * The length of the character buffer to use.
     */
    public static final int BUFFER_LENGTH = 1024;
    /**
     * The extension to use for gz files.
     */
    public static final String GZ_EXTENSION = ".gz";

    /**
     * Gzip a file. If the gz file already exists it will be silently
     * overwritten.
     *
     * @param file the file to read
     * @param remove if true the original file will be removed
     */
    public static void gzFile(
            File file,
            boolean remove
    ) {

        // if already gzipped, the file will be unchanged
        if (file.getAbsolutePath().endsWith(GZ_EXTENSION)) {
            return;
        }

        File gzFile = new File(file.getAbsoluteFile() + GZ_EXTENSION);

        gzFile(
                file,
                gzFile,
                remove
        );

    }

    /**
     * Gzip a file. If the gz file already exists it will be silently
     * overwritten.
     *
     * @param file the file to read
     * @param gzFile the gz file to write
     * @param remove if true the original file will be removed
     */
    public static void gzFile(
            File file,
            File gzFile,
            boolean remove
    ) {

        try {

            char[] buffer = new char[BUFFER_LENGTH];

            try ( BufferedReader reader = new BufferedReader(new FileReader(file))) {

                FileOutputStream fileStream = new FileOutputStream(gzFile);
                GZIPOutputStream gzipStream = new GZIPOutputStream(fileStream);
                OutputStreamWriter encoder = new OutputStreamWriter(gzipStream, ENCODING);

                try ( BufferedWriter bw = new BufferedWriter(encoder)) {

                    int read;
                    while ((read = reader.read(buffer)) != -1) {

                        bw.write(buffer, 0, read);

                    }
                }
            }

            if (remove) {

                file.delete();

            }

        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Uncompresses the given gz file to the given destination file. If the
     * destination file already exists it will be silently overwritten.
     *
     * @param gzFile the gz file to read
     * @param remove if true the gz file will be deleted upon completion
     */
    public static void gunzipFile(
            File gzFile,
            boolean remove
    ) {

        String gzFilePath = gzFile.getAbsolutePath();

        if (!gzFilePath.endsWith(GZ_EXTENSION)) {

            throw new IllegalArgumentException("Gz file expected to end with " + GZ_EXTENSION);

        }

        File destinationFile = new File(
                gzFilePath.substring(0, gzFilePath.length() - GZ_EXTENSION.length())
        );

        gunzipFile(gzFile, destinationFile, remove);

    }

    /**
     * Uncompresses the given gz file to the given destination file. If the
     * destination file already exists it will be silently overwritten.
     *
     * @param gzFile the gz file to read
     * @param destinationFile the file to write
     * @param remove if true the gz file will be deleted upon completion
     */
    public static void gunzipFile(
            File gzFile,
            File destinationFile,
            boolean remove
    ) {

        try {

            char[] buffer = new char[BUFFER_LENGTH];

            InputStream fileStream = new FileInputStream(gzFile);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, ENCODING);

            try ( BufferedReader reader = new BufferedReader(decoder)) {

                try ( BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFile))) {

                    int read;
                    while ((read = reader.read(buffer)) != -1) {

                        writer.write(buffer, 0, read);

                    }
                }
            }

            if (remove) {

                gzFile.delete();

            }
        } catch (IOException e) {

            throw new RuntimeException(e);

        }
    }
}
