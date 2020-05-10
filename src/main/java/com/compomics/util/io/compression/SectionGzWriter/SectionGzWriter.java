package com.compomics.util.io.compression.SectionGzWriter;

import com.compomics.util.Util;
import static com.compomics.util.Util.LINE_SEPARATOR;
import com.compomics.util.io.IoUtil;
import com.compomics.util.threading.SimpleSemaphore;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * Writer by section for the gz file.
 *
 * Parts of this class are adapted from java.util.zip.GZIPOutputStream by David
 * Connelly. Copyright (c) 1996, 2013, Oracle and/or its affiliates. No
 * copyright infringement intended.
 *
 * @author Marc Vaudel
 */
public class SectionGzWriter implements AutoCloseable {

    /**
     * The default buffer size for the deflaters.
     */
    public final int BUFFER_SIZE = 10 * 1024;
    /**
     * The compression level to use.
     */
    private final int compressionLevel;
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
    private final ConcurrentHashMap<String, BufferedWriter> tempWriterMap = new ConcurrentHashMap<>();
    /**
     * Map of the semaphores to use for accessing the temp files.
     */
    private final ConcurrentHashMap<String, SimpleSemaphore> semaphoreMap = new ConcurrentHashMap<>();
    /**
     * Map of the start end indexes of each section in the final file.
     */
    private final HashMap<String, long[]> sectionStartEndMap = new HashMap<>();
    /**
     * The random access file to the destination file.
     */
    private final RandomAccessFile raf;
    /**
     * Semaphore for access to the destination file.
     */
    private final SimpleSemaphore rafSemaphore = new SimpleSemaphore(1);
    /**
     * Length of the header in bytes. Content starts at this position.
     */
    public static final int HEADER_LENGTH = 10;
    /**
     * GZIP header magic number.
     *
     * Adapted from java.util.zip.GZIPOutputStream by David Connelly. Copyright
     * (c) 1996, 2013, Oracle and/or its affiliates. No copyright infringement
     * intended.
     */
    private final static int GZIP_MAGIC = 0x8b1f;
    /**
     * The deflater to use.
     */
    private final Deflater deflater;
    /**
     * CRC-32 of uncompressed data.
     *
     * Adapted from java.util.zip.GZIPOutputStream by David Connelly. Copyright
     * (c) 1996, 2013, Oracle and/or its affiliates. No copyright infringement
     * intended.
     */
    private CRC32 crc = new CRC32();

    /**
     * Constructor.
     *
     * @param destinationFile The destination file.
     * @param tempFolder The folder to write intermediate files to.
     * @param compressionLevel The compression level to use.
     *
     * @throws FileNotFoundException Exception thrown if a file is not found.
     * @throws IOException Exception thrown if an error occurred while reading
     * or writing a file.
     */
    public SectionGzWriter(
            File destinationFile,
            File tempFolder,
            int compressionLevel
    )
            throws FileNotFoundException, IOException {

        this.compressionLevel = compressionLevel;
        this.tempFolder = tempFolder;
        this.destinationFileName = IoUtil.getFileName(destinationFile);

        raf = new RandomAccessFile(destinationFile, "rw");
        deflater = new Deflater(compressionLevel, true);

        writeHeader();
        crc.reset();

    }

    /**
     * Constructor with deflater default compression level.
     *
     * @param destinationFile The destination file.
     * @param tempFolder The folder to write intermediate files to.
     *
     * @throws FileNotFoundException Exception thrown if a file is not found.
     * @throws IOException Exception thrown if an error occurred while reading
     * or writing a file.
     */
    public SectionGzWriter(
            File destinationFile,
            File tempFolder
    )
            throws FileNotFoundException, IOException {

        this(destinationFile, tempFolder, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * Writes GZIP member header.
     *
     * Adapted from java.util.zip.GZIPOutputStream by David Connelly. Copyright
     * (c) 1996, 2013, Oracle and/or its affiliates. No copyright infringement
     * intended.
     */
    private void writeHeader() throws IOException {

        raf.write(
                new byte[]{
                    (byte) GZIP_MAGIC, // Magic number (short)
                    (byte) (GZIP_MAGIC >> 8), // Magic number (short)
                    Deflater.DEFLATED, // Compression method (CM)
                    0, // Flags (FLG)
                    0, // Modification time MTIME (int)
                    0, // Modification time MTIME (int)
                    0, // Modification time MTIME (int)
                    0, // Modification time MTIME (int)
                    0, // Extra flags (XFLG)
                    0 // Operating system (OS)
                }
        );
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

        rafSemaphore.acquire();

        try {

            byte[] buffer = new byte[BUFFER_SIZE];

            if (!deflater.finished()) {

                deflater.finish();

                while (!deflater.finished()) {

                    int compressedLength = deflater.deflate(buffer, 0, buffer.length, Deflater.FULL_FLUSH);

                    if (compressedLength > 0) {

                        raf.write(buffer, 0, compressedLength);

                    }
                }
            }

            int crcValue = (int) crc.getValue();
            long deflaterInput = deflater.getBytesRead();

            byte[] trailer = new byte[8];
            writeInt(crcValue, trailer, 0); // CRC-32 of uncompr. data
            writeInt((int) deflaterInput, trailer, 4); // Number of uncompr. bytes

            raf.write(trailer);
            raf.close();

        } catch (Exception e) {

            throw new RuntimeException(e);

        } finally {

            rafSemaphore.release();

        }
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

        File tempFile = new File(tempFolder, destinationFileName + "." + sectionName);
        BufferedWriter tempWriter = new BufferedWriter(new FileWriter(tempFile));

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

        try {

            BufferedWriter tempWriter = tempWriterMap.get(sectionName);
            tempWriter.close();

            rafSemaphore.acquire();

            byte[] buffer = new byte[BUFFER_SIZE];
            File tempFile = tempFileMap.get(sectionName);

            long[] startEnd = new long[2];
            startEnd[0] = raf.getFilePointer();

            RandomAccessFile tempRaf = new RandomAccessFile(tempFile, "rw");

            long position = 0;

            while (position < tempRaf.length()) {

                long remaining = tempRaf.length() - position;
                long bufferLength = buffer.length;
                boolean useRemaining = remaining < bufferLength;

                int length = useRemaining ? (int) remaining : buffer.length;

                tempRaf.read(buffer, 0, length);

                deflater.setInput(buffer, 0, length);
                crc.update(buffer, 0, length);

                while (!deflater.needsInput()) {

                    int compressedLength = deflater.deflate(buffer, 0, buffer.length);

                    if (compressedLength > 0) {

                        raf.write(buffer, 0, compressedLength);

                    }
                }

                position = tempRaf.getFilePointer();

            }

            int compressedLength = buffer.length;

            while (compressedLength == buffer.length) {

                compressedLength = deflater.deflate(buffer, 0, buffer.length, Deflater.FULL_FLUSH);

                if (compressedLength > 0) {

                    raf.write(buffer, 0, compressedLength);

                }

            }

            tempRaf.close();

            tempFile.delete();

            tempFileMap.remove(sectionName);
            tempWriterMap.remove(sectionName);

            startEnd[1] = raf.getFilePointer();

            sectionStartEndMap.put(sectionName, startEnd);

        } catch (IOException e) {

            throw new RuntimeException(e);

        } finally {

            rafSemaphore.release();
            sectionSemaphore.release();

        }
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

        try {

            BufferedWriter tempWriter = tempWriterMap.get(sectionName);

            tempWriter.write(content);

        } catch (IOException e) {

            throw new RuntimeException(e);

        } finally {

            sectionSemaphore.release();

        }
    }

    /*
     * Writes integer in Intel byte order to a byte array, starting at a
     * given offset.
     *
     * Adapted from java.util.zip.GZIPOutputStream by David Connelly. Copyright
     * (c) 1996, 2013, Oracle and/or its affiliates. No copyright infringement
     * intended.
     */
    private void writeInt(
            int i,
            byte[] buf,
            int offset
    ) {

        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);

    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     *
     * Adapted from java.util.zip.GZIPOutputStream by David Connelly. Copyright
     * (c) 1996, 2013, Oracle and/or its affiliates. No copyright infringement
     * intended.
     */
    private void writeShort(
            int s,
            byte[] buf,
            int offset
    ) {

        buf[offset] = (byte) (s & 0xff);
        buf[offset + 1] = (byte) ((s >> 8) & 0xff);

    }

    /**
     * Returns the start and end indexes of the given section in the final file.
     * Null if section is not in the file or not completed yet.
     *
     * @param sectionName The name of the section.
     *
     * @return The start and end indexes in an array.
     */
    public long[] getStartEnd(String sectionName) {

        return sectionStartEndMap.get(sectionName);

    }
}
