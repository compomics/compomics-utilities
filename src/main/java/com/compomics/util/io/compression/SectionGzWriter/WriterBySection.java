package com.compomics.util.io.compression.SectionGzWriter;

import com.compomics.util.Util;
import static com.compomics.util.Util.LINE_SEPARATOR;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.io.flat.SimpleFileWriter;
import com.compomics.util.threading.SimpleSemaphore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This writer writes the different sections of a file in temp files and bundles
 * them together upon completion.
 *
 * @author Marc Vaudel
 */
public class WriterBySection implements AutoCloseable {

    /**
     * The size of the char buffer to use when copying files.
     */
    public static final int CHAR_BUFFER_SIZE = 512;
    /**
     * The folder to write intermediate files to.
     */
    private final File tempFolder;
    /**
     * The name of the destination file.
     */
    private final String destinationFileName;
    /**
     * Map of the temp files.
     */
    private final ConcurrentHashMap<String, File> tempFileMap = new ConcurrentHashMap<>();
    /**
     * Map of the writers to section temp files.
     */
    private final ConcurrentHashMap<String, SimpleFileWriter> tempWriterMap = new ConcurrentHashMap<>();
    /**
     * Map of the semaphores to use for accessing the temp files.
     */
    private final ConcurrentHashMap<String, SimpleSemaphore> semaphoreMap = new ConcurrentHashMap<>();
    /**
     * Writer to de destination file.
     */
    private final SimpleFileWriter writer;
    /**
     * Semaphore for access to the destination file.
     */
    private final SimpleSemaphore writerSemaphore = new SimpleSemaphore(1);
    /**
     * If true, temp files are deleted upon completion.
     */
    public final boolean deleteTempFiles;
    /**
     * If true, temp files are gzipped.
     */
    public final boolean gzipTemp;

    /**
     * Constructor.
     *
     * @param destinationFile The destination file.
     * @param tempFolder The folder to write intermediate files to.
     * @param deleteTempFiles If true, temp files are deleted upon completion.
     * @param gzipTemp If true, temp files are gzipped.
     * @param gzipDestination If true, the destination file is gzipped.
     *
     * @throws FileNotFoundException Exception thrown if a file is not found.
     * @throws IOException Exception thrown if an error occurred while reading
     * or writing a file.
     */
    public WriterBySection(
            File destinationFile,
            File tempFolder,
            boolean deleteTempFiles,
            boolean gzipTemp,
            boolean gzipDestination
    )
            throws FileNotFoundException, IOException {

        this.tempFolder = tempFolder;
        this.destinationFileName = IoUtil.getFileName(destinationFile);
        this.deleteTempFiles = deleteTempFiles;
        this.gzipTemp = gzipTemp;

        writer = new SimpleFileWriter(destinationFile, gzipDestination);

    }

    @Override
    public void close() {

        for (Entry<String, SimpleSemaphore> entry : semaphoreMap.entrySet()) {

            String sectionName = entry.getKey();
            SimpleSemaphore simpleSemaphore = entry.getValue();

            simpleSemaphore.acquire();
            simpleSemaphore.release();

            if (tempWriterMap.containsKey(sectionName)) {

                throw new IllegalArgumentException("Attempted to close the gz file writer before section " + sectionName + " is completed.");

            }
        }

        writer.close();

    }

    /**
     * Registers a new section. The section name should not contain special
     * characters forbidden in file names.
     *
     * @param sectionName The name of the section.
     *
     * @throws FileNotFoundException Exception thrown if the temp folder does
     * not exist or is not writable.
     * @throws IOException Exception thrown if an error occurred while writing
     * the temp file.
     */
    public synchronized void registerSection(
            String sectionName
    )
            throws FileNotFoundException, IOException {

        if (Util.containsForbiddenCharacter(sectionName)) {

            throw new IllegalArgumentException("Invalid section name '" + sectionName + "'. Section names should not contain characters forbidden in file names.");

        }

        StringBuilder fileName = new StringBuilder();

        if (destinationFileName.endsWith(".gz")) {

            fileName.append(destinationFileName.substring(0, destinationFileName.length() - 3));

        } else {

            fileName.append(destinationFileName);

        }

        fileName.append('.')
                .append(sectionName);

        if (gzipTemp) {

            fileName.append(".gz");

        }

        File tempFile = new File(tempFolder, fileName.toString());
        SimpleFileWriter tempWriter = new SimpleFileWriter(tempFile, gzipTemp);

        tempFileMap.put(sectionName, tempFile);
        tempWriterMap.put(sectionName, tempWriter);
        semaphoreMap.put(sectionName, new SimpleSemaphore(1));

    }

    /**
     * Registers a section as completed and transfers its content to the main
     * file.
     *
     * @param sectionName The name of the section.
     */
    public void sectionCompleted(
            String sectionName
    ) {

        SimpleSemaphore sectionSemaphore = semaphoreMap.get(sectionName);
        sectionSemaphore.acquire();

        SimpleFileWriter tempWriter = tempWriterMap.get(sectionName);
        tempWriter.close();

        File tempFile = tempFileMap.get(sectionName);

        writerSemaphore.acquire();

        try ( SimpleFileReader reader = SimpleFileReader.getFileReader(tempFile)) {

            int lengthRead = CHAR_BUFFER_SIZE;
            char[] buffer = new char[lengthRead];

            while (lengthRead != -1) {

                lengthRead = reader.read(buffer);

                if (lengthRead > 0) {

                    writer.write(buffer, 0, lengthRead);

                }
            }
        }

        writerSemaphore.release();

        if (deleteTempFiles) {

            tempFile.delete();

        }

        tempFileMap.remove(sectionName);
        tempWriterMap.remove(sectionName);

        sectionSemaphore.release();

    }

    /**
     * Appends a new line to the given section.
     *
     * @param sectionName The name of the section.
     */
    public void newLine(
            String sectionName
    ) {

        write(
                sectionName,
                LINE_SEPARATOR
        );
    }

    /**
     * Writes content to the given section.
     *
     * @param sectionName The name of the section.
     * @param content The content to write.
     */
    public void write(
            String sectionName,
            String content
    ) {

        SimpleSemaphore sectionSemaphore = semaphoreMap.get(sectionName);
        sectionSemaphore.acquire();

        SimpleFileWriter tempWriter = tempWriterMap.get(sectionName);

        tempWriter.write(content);

        sectionSemaphore.release();

    }

    /**
     * Writes content to the given section.
     *
     * @param sectionName The name of the section.
     * @param buffer The buffer to write.
     * @param offset The offset from which to start reading characters.
     * @param length The maximal number of characters to write
     */
    public void write(
            String sectionName,
            char[] buffer,
            int offset,
            int length
    ) {

        SimpleSemaphore sectionSemaphore = semaphoreMap.get(sectionName);
        sectionSemaphore.acquire();

        SimpleFileWriter tempWriter = tempWriterMap.get(sectionName);

        tempWriter.write(buffer, offset, length);

        sectionSemaphore.release();

    }
}
