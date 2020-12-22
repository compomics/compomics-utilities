package com.compomics.util.experiment.io.mass_spectrometry.cms;

import com.compomics.util.ArrayUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import static com.compomics.util.io.IoUtil.ENCODING;
import static com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileUtils.MAGIC_NUMBER;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;

/**
 * Writer for cms files.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class CmsFileWriter implements AutoCloseable {

    /**
     * The length of the file header.
     */
    public static final int HEADER_LENGTH = MAGIC_NUMBER.length + Long.BYTES + 4 * Double.BYTES;
    /**
     * The random access file to write to.
     */
    private final RandomAccessFile raf;
    /**
     * The deflater to compress the spectra.
     */
    private final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
    /**
     * The minimal precursor m/z.
     */
    private double minMz = Double.MAX_VALUE;
    /**
     * The maximal precursor m/z.
     */
    private double maxMz = 0.0;
    /**
     * The maximal precursor intensity.
     */
    private double maxInt = 0.0;
    /**
     * The maximal precursor RT.
     */
    private double maxRt = 0.0;
    /**
     * List of the spectrum titles.
     */
    private final ArrayList<String> titles = new ArrayList<>();
    /**
     * List of the index of the spectra.
     */
    private final ArrayList<Integer> indexes = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param outputFile the output file.
     *
     * @throws FileNotFoundException Exception thrown if the output file was not
     * found.
     * @throws IOException Exception thrown if an error occurred while
     * attempting to write to output file.
     */
    public CmsFileWriter(
            File outputFile
    ) throws FileNotFoundException, IOException {

        raf = new RandomAccessFile(outputFile, "rw");
        raf.seek(HEADER_LENGTH);

    }

    /**
     * Adds a spectrum to the file.
     *
     * @param spectrumTitle The title of the spectrum to add.
     * @param spectrum The spectrum to add.
     *
     * @throws IOException Exception thrown if an error occurred while
     * attempting to write to output file.
     */
    public void addSpectrum(
            String spectrumTitle,
            Spectrum spectrum
    ) throws IOException {

        long index = raf.getFilePointer() - CmsFileWriter.HEADER_LENGTH;

        if (index > Integer.MAX_VALUE) {

            throw new IOException("File exceeds memory mapped reader max buffer size.");

        }

        int nPeaks = spectrum.mz.length;

        indexes.add((int) index);
        titles.add(spectrumTitle);

        Precursor precursor = spectrum.precursor;
        double precursorMz = precursor == null ? Double.NaN : precursor.mz;
        double precursorRt = precursor == null ? Double.NaN : precursor.rt;
        double precursorIntensity = precursor == null ? Double.NaN : precursor.intensity;
        int[] possibleCharges = precursor == null ? new int[0] : precursor.possibleCharges;

        ByteBuffer buffer = ByteBuffer.allocate(2 * nPeaks * Double.BYTES);

        for (int i = 0; i < nPeaks; i++) {

            buffer.putDouble(spectrum.mz[i]);
            buffer.putDouble(spectrum.intensity[i]);

        }

        TempByteArray compressedData;

        // make sure that only non-empty spectra are compressed
        if (nPeaks > 0) {

            compressedData = compress(buffer.array());

        } else {

            compressedData = new TempByteArray(buffer.array(), 0);
        }

        buffer = ByteBuffer.allocate(3 * Double.BYTES + (3 + possibleCharges.length) * Integer.BYTES + compressedData.length);
        buffer
                .putDouble(precursorMz)
                .putDouble(precursorRt)
                .putDouble(precursorIntensity)
                .putInt(compressedData.length)
                .putInt(nPeaks)
                .put(compressedData.array, 0, compressedData.length)
                .putInt(possibleCharges.length);

        for (int charge : possibleCharges) {

            buffer.putInt(charge);

        }

        byte[] arrayToWrite = buffer.array();

        raf.write(arrayToWrite, 0, arrayToWrite.length);

        if (minMz > precursorMz) {

            minMz = precursorMz;

        }

        if (maxMz < precursorMz) {

            maxMz = precursorMz;

        }

        if (maxInt < precursorIntensity) {

            maxInt = precursorIntensity;

        }

        if (maxRt < precursorRt) {

            maxRt = precursorRt;

        }

    }

    /**
     * Compresses and writes the given byte array.
     *
     * @param uncompressedData The uncompressed data.
     *
     * @throws IOException Exception thrown if an error occurred while
     * attempting to write to output file.
     */
    private void compressAndWrite(
            byte[] uncompressedData
    ) throws IOException {

        TempByteArray compressedData = compress(uncompressedData);

        raf.writeInt(compressedData.length);
        raf.writeInt(uncompressedData.length);
        raf.write(compressedData.array, 0, compressedData.length);

    }

    /**
     * Compresses the given byte array.
     *
     * @param uncompressedData The uncompressed data.
     *
     * @return The compressed data.
     */
    private TempByteArray compress(
            byte[] uncompressedData
    ) {

        byte[] compressedData = new byte[uncompressedData.length];

        int outputLength = compressedData.length;

        deflater.setInput(uncompressedData);
        int compressedByteLength = deflater.deflate(
                compressedData,
                0,
                compressedData.length,
                Deflater.FULL_FLUSH
        );
        int compressedDataLength = compressedByteLength;

        while (compressedByteLength == outputLength) {

            byte[] output2 = new byte[outputLength];
            compressedByteLength = deflater.deflate(
                    output2,
                    0,
                    outputLength,
                    Deflater.FULL_FLUSH
            );

            compressedData = ArrayUtil.concatenate(
                    compressedData,
                    output2,
                    compressedByteLength
            );
            compressedDataLength += compressedByteLength;

        }

        return new TempByteArray(compressedData, compressedDataLength);

    }

    /**
     * Writes the header and footer to the file.
     *
     * @throws IOException Exception thrown if an error occurred while
     * attempting to write to output file.
     */
    private void writeHeaderAndFooter() throws IOException {

        long footerPosition = raf.getFilePointer();

        String titleString = titles.stream()
                .collect(
                        Collectors.joining(CmsFileUtils.TITLE_SEPARATOR)
                );
        String indexString = indexes.stream()
                .map(
                        index -> index.toString()
                )
                .collect(Collectors.joining(CmsFileUtils.TITLE_SEPARATOR));
        String titleIndexString = String.join(CmsFileUtils.TITLE_SEPARATOR,
                titleString,
                indexString
        );

        byte[] titleBytes = titleIndexString.getBytes(ENCODING);
        compressAndWrite(titleBytes);

        raf.seek(0);

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH);
        buffer.put(MAGIC_NUMBER)
                .putLong(footerPosition)
                .putDouble(minMz)
                .putDouble(maxMz)
                .putDouble(maxInt)
                .putDouble(maxRt);

        raf.write(buffer.array());

    }

    @Override
    public void close() throws IOException {

        writeHeaderAndFooter();

        deflater.end();
        raf.close();

    }

}
