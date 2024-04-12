package com.compomics.util.experiment.io.mass_spectrometry.cms;

import com.compomics.util.TempByteArray;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.PrecursorParameter;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.compression.ZstdUtils;
import io.airlift.compress.zstd.ZstdCompressor;
import java.util.HashMap;

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
    public static final int HEADER_LENGTH = CmsFileUtils.MAGIC_NUMBER.length + Long.BYTES + 5 * Double.BYTES;
    /**
     * The random access file to write to.
     */
    private final RandomAccessFile raf;
    /**
     * The size of the current buffer content. Used for splitting up larger
     * files into multiple mapped buffers when reading.
     */
    private int currentBufferContent = 0;
    /**
     * The array of the start indexes per buffer.
     */
    private ArrayList<Long> bufferStartIndexes = new ArrayList();
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
    private final ArrayList<Long> indexes = new ArrayList<>();
    /**
     * Map of which MSn spectra an MS(n-1) spectrum has created. The key is the
     * identifier of the MS(n-1) spectrum and the resulting list contains the
     * identifiers if the MSn spectra.
     */
    private final HashMap<String, ArrayList<String>> precursorMap = new HashMap<>();
    /**
     * Zstd compressor.
     */
    private final ZstdCompressor compressor = new ZstdCompressor();

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

        bufferStartIndexes.add(raf.getFilePointer());

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

        long index = raf.getFilePointer();

        indexes.add(index);
        titles.add(spectrumTitle);

        int nPeaks = spectrum.mz.length;

        // update the precusor mapping
        UrParameter tempPrecursorParameter = spectrum.getUrParam(PrecursorParameter.dummy);

        if (tempPrecursorParameter != null) {

            PrecursorParameter precursorParameter = (PrecursorParameter) tempPrecursorParameter;
            ArrayList<String> precursorIdentifiers = precursorParameter.getPrecusorIdentifiers();

            for (String tempPrecursorIdentifier : precursorIdentifiers) {

                if (!precursorMap.containsKey(tempPrecursorIdentifier)) {
                    precursorMap.put(tempPrecursorIdentifier, new ArrayList<>());
                }

                precursorMap.get(tempPrecursorIdentifier).add(spectrumTitle);

            }

            // remove the precursor map as it is no longer needed
            //spectrum.removeUrParam(precursorParameter.getParameterKey()); // @TODO: worth considering?
        }

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

        buffer = ByteBuffer.allocate(3 * Double.BYTES + (4 + possibleCharges.length) * Integer.BYTES + compressedData.length);
        buffer
                .putDouble(precursorMz)
                .putDouble(precursorRt)
                .putDouble(precursorIntensity)
                .putInt(spectrum.getSpectrumLevel())
                .putInt(compressedData.length)
                .putInt(nPeaks)
                .put(compressedData.array, 0, compressedData.length)
                .putInt(possibleCharges.length);

        for (int charge : possibleCharges) {

            buffer.putInt(charge);

        }

        byte[] arrayToWrite = buffer.array();

        raf.write(arrayToWrite, 0, arrayToWrite.length);

        // see if we need to start a new buffer
        if (currentBufferContent + arrayToWrite.length > CmsFileUtils.MAX_BUFFER_SIZE) {

            bufferStartIndexes.add(index);
            currentBufferContent = arrayToWrite.length;

        } else {

            currentBufferContent += arrayToWrite.length;

        }

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
    private synchronized TempByteArray compress(
            byte[] uncompressedData
    ) {

        return ZstdUtils.zstdCompress(
                compressor,
                uncompressedData
        );

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

        StringBuilder precursorMapString = new StringBuilder();

        for (String spectrumKey : precursorMap.keySet()) { // @TODO: has to be a more elegant way of doing this..?

            if (precursorMapString.length() > 0) {
                precursorMapString.append(" # ");
            }

            precursorMapString.append(spectrumKey).append("");
            String tempListAsText = precursorMap.get(spectrumKey).stream().collect(Collectors.joining(",")); // @TODO: use index instead of the spectrum id?
            precursorMapString.append(" {").append(tempListAsText).append(("}"));

        }

        String precursorMapAsString;

        if (!precursorMap.isEmpty()) {
            precursorMapAsString = precursorMapString.toString();
        } else {
            precursorMapAsString = "null";
        }

        String titleIndexString = String.join(CmsFileUtils.TITLE_SEPARATOR,
                titleString,
                indexString,
                precursorMapAsString,
                bufferStartIndexes.toString()
        );

        byte[] titleBytes = titleIndexString.getBytes(IoUtil.ENCODING);
        compressAndWrite(titleBytes);

        raf.seek(0);

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH);
        buffer.put(CmsFileUtils.MAGIC_NUMBER)
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

        raf.close();

    }

}
