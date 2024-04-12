package com.compomics.util.experiment.io.mass_spectrometry.cms;

import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.compression.ZstdUtils;
import com.compomics.util.threading.SimpleSemaphore;
import com.compomics.util.waiting.WaitingHandler;
import io.airlift.compress.zstd.ZstdDecompressor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Reader for Compomics Mass Spectrometry (cms) files.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class CmsFileReader implements SpectrumProvider {

    /**
     * The minimal precursor m/z.
     */
    private final double minMz;
    /**
     * The maximal precursor m/z.
     */
    private final double maxMz;
    /**
     * The maximal precursor intensity.
     */
    private final double maxInt;
    /**
     * The maximal precursor RT.
     */
    private final double maxRt;
    /**
     * The titles of the spectra.
     */
    public final String[] titles;
    /**
     * Map with which MSn+1 spectra an MSn spectrum has created.
     */
    private final HashMap<String, ArrayList<String>> postcursorMap;
    /**
     * The index of the spectra.
     */
    private final HashMap<String, Long> indexMap;
    /**
     * Map of the precursor m/z.
     */
    private final HashMap<String, Double> precursorMzMap;
    /**
     * The array of the start indexes per buffer.
     */
    private ArrayList<Long> bufferStartIndexes = new ArrayList();
    /**
     * Mutex to synchronize threads.
     */
    private final SimpleSemaphore mutex = new SimpleSemaphore(1);
    /**
     * The random access file.
     */
    private final RandomAccessFile raf;
    /**
     * The channel to the file.
     */
    private final FileChannel fc;
    /**
     * The mapped byte buffers.
     */
    private ArrayList<MappedByteBuffer> mappedByteBuffers;

    /**
     * Constructor allocating for single thread usage.
     *
     * @param file the file to read
     * @param waitingHandler the waiting handler
     *
     * @throws FileNotFoundException thrown if the file was not found
     * @throws IOException thrown if an error occurred while attempting to read
     * the file
     */
    public CmsFileReader(
            File file,
            WaitingHandler waitingHandler
    ) throws FileNotFoundException, IOException {

        mappedByteBuffers = new ArrayList<>();

        // @TODO: use the waiting handler
        raf = new RandomAccessFile(file, "r");

        try {

            byte[] fileMagicNumber = new byte[CmsFileUtils.MAGIC_NUMBER.length];
            raf.read(fileMagicNumber);

            if (!Arrays.equals(CmsFileUtils.MAGIC_NUMBER, fileMagicNumber)) {

                raf.close();
                throw new IOException("File format of " + file + " not supported.");

            }

            long footerPosition = raf.readLong();

            minMz = raf.readDouble();
            maxMz = raf.readDouble();
            maxInt = raf.readDouble();
            maxRt = raf.readDouble();

            raf.seek(footerPosition);
            int length = raf.readInt();
            int uncompressedLength = raf.readInt();

            byte[] compressedFooter = new byte[length];
            raf.read(compressedFooter);

            byte[] footerByteArray = uncompress(compressedFooter, uncompressedLength);
            String footerAsString = new String(footerByteArray, 0, footerByteArray.length, IoUtil.ENCODING);
            String[] footerAsStringSplit = footerAsString.split(CmsFileUtils.TITLE_SEPARATOR);

            int nTitles = (footerAsStringSplit.length - 1) / 2;
            indexMap = new HashMap<>(nTitles);
            titles = new String[nTitles];

            for (int i = 0; i < nTitles; i++) {

                String title = footerAsStringSplit[i];
                long index = Long.parseLong(footerAsStringSplit[i + nTitles]);
                indexMap.put(title, index);
                titles[i] = title;

            }

            postcursorMap = new HashMap<>();

            String postcursorMapAsText = footerAsStringSplit[footerAsStringSplit.length - 2];

            if (!postcursorMapAsText.equalsIgnoreCase("null")) {

                String[] postcursorMapAsTextSplit = postcursorMapAsText.split(" # ");

                for (String temp : postcursorMapAsTextSplit) {

                    String[] precusorAndList = temp.split(" \\{");

                    String precusorKey = precusorAndList[0];
                    String postcursors = precusorAndList[1].substring(0, precusorAndList[1].length() - 1);
                    String[] postcursorsSplit = postcursors.split(",");

                    if (!postcursorMap.containsKey(precusorKey)) {
                        postcursorMap.put(precusorKey, new ArrayList<>());
                    }

                    postcursorMap.get(precusorKey).addAll(Arrays.asList(postcursorsSplit));

                }

            }

            precursorMzMap = new HashMap<>(nTitles);

            String bufferStartIndexesAsText = footerAsStringSplit[footerAsStringSplit.length - 1];
            bufferStartIndexesAsText = bufferStartIndexesAsText.substring(1, bufferStartIndexesAsText.length() - 1);

            String[] bufferStartIndexesSplit = bufferStartIndexesAsText.split(", ");

            for (String indexAsString : bufferStartIndexesSplit) {
                bufferStartIndexes.add(Long.valueOf(indexAsString));
            }

            long maxIndex = footerPosition - CmsFileWriter.HEADER_LENGTH;

            fc = raf.getChannel();

            if (bufferStartIndexes.size() == 1) {

                long startIndex = CmsFileWriter.HEADER_LENGTH;
                mappedByteBuffers.add(fc.map(FileChannel.MapMode.READ_ONLY, startIndex, maxIndex));

            } else {

                for (int i = 0; i < bufferStartIndexes.size() - 1; i++) {

                    long startIndex = bufferStartIndexes.get(i);
                    long size = bufferStartIndexes.get(i + 1) - startIndex;

                    mappedByteBuffers.add(fc.map(FileChannel.MapMode.READ_ONLY, startIndex, size));

                }

                // special case for the final buffer
                long startIndex = bufferStartIndexes.get(bufferStartIndexes.size() - 1);
                long size = footerPosition - startIndex;

                mappedByteBuffers.add(fc.map(FileChannel.MapMode.READ_ONLY, startIndex, size));

            }

        } finally {
            raf.close();
        }

    }

    /**
     * Returns the spectrum with the given title.
     *
     * @param spectrumIndex index of the spectrum
     *
     * @return the spectrum
     */
    private Spectrum getSpectrum(long spectrumIndex) {

        mutex.acquire();

        MappedByteBuffer mappedByteBuffer = getMappedByteBuffer(spectrumIndex);

        double precursorMz = mappedByteBuffer.getDouble();
        double precursorRt = mappedByteBuffer.getDouble();
        double precursorIntensity = mappedByteBuffer.getDouble();
        int spectrumLevel = mappedByteBuffer.getInt();
        int compressedDataLength = mappedByteBuffer.getInt();
        int nPeaks = mappedByteBuffer.getInt();

        byte[] compressedSpectrum = new byte[compressedDataLength];
        mappedByteBuffer.get(compressedSpectrum);

        int nCharges = mappedByteBuffer.getInt();
        int[] charges = new int[nCharges];

        for (int i = 0; i < nCharges; i++) {

            charges[i] = mappedByteBuffer.getInt();

        }

        mutex.release();

        double[] mz = new double[nPeaks];
        double[] intensity = new double[nPeaks];

        if (nPeaks > 0) {

            int uncompressedLength = nPeaks * 2 * Double.BYTES;

            byte[] uncompressedSpectrum = uncompress(compressedSpectrum, uncompressedLength);
            ByteBuffer byteBuffer = ByteBuffer.wrap(uncompressedSpectrum);

            for (int i = 0; i < nPeaks; i++) {

                mz[i] = byteBuffer.getDouble();
                intensity[i] = byteBuffer.getDouble();

            }

        }

        Precursor precursor = new Precursor(
                precursorRt,
                precursorMz,
                precursorIntensity,
                charges
        );

        return new Spectrum(precursor, mz, intensity, spectrumLevel);

    }

    /**
     * Returns the spectrum with the given title.
     *
     * @param spectrumTitle title of the spectrum
     *
     * @return the spectrum
     */
    public Spectrum getSpectrum(String spectrumTitle) {

        long index = indexMap.get(spectrumTitle);

        return getSpectrum(index);

    }

    /**
     * Returns the precursor of the spectrum with the given title.
     *
     * @param spectrumTitle title of the spectrum
     *
     * @return the precursor of the spectrum
     */
    public Precursor getPrecursor(String spectrumTitle) {

        long spectrumIndex = indexMap.get(spectrumTitle);

        mutex.acquire();

        MappedByteBuffer mappedByteBuffer = getMappedByteBuffer(spectrumIndex);

        double precursorMz = mappedByteBuffer.getDouble();
        double precursorRt = mappedByteBuffer.getDouble();
        double precursorIntensity = mappedByteBuffer.getDouble();
        mappedByteBuffer.getInt(); // read and ignore the spectrum level @TODO: there is probably a better way of doing this?
        int compressedDataLength = mappedByteBuffer.getInt();

        mappedByteBuffer.position(mappedByteBuffer.position() + compressedDataLength + Integer.BYTES);

        int nCharges = mappedByteBuffer.getInt();
        int[] charges = new int[nCharges];

        for (int i = 0; i < nCharges; i++) {

            charges[i] = mappedByteBuffer.getInt();

        }

        mutex.release();

        Precursor precursor = new Precursor(
                precursorRt,
                precursorMz,
                precursorIntensity,
                charges
        );

        return precursor;

    }

    /**
     * Returns the m/z of the precursor of the spectrum with the given title.
     *
     * @param spectrumTitle the title of the spectrum
     *
     * @return the precursor m/z of the spectrum
     */
    public double getPrecursorMz(String spectrumTitle) {

        Double precursorMz = precursorMzMap.get(spectrumTitle);

        if (precursorMz == null) {

            long spectrumIndex = indexMap.get(spectrumTitle);

            mutex.acquire();

            MappedByteBuffer mappedByteBuffer = getMappedByteBuffer(spectrumIndex);

            precursorMz = mappedByteBuffer.getDouble();
            precursorMzMap.put(spectrumTitle, precursorMz);

            mutex.release();

        }

        return precursorMz;

    }

    /**
     * Returns the RT of the precursor of the spectrum with the given title.
     *
     * @param spectrumTitle the title of the spectrum
     *
     * @return the precursor RT of the spectrum
     */
    public double getPrecursorRt(String spectrumTitle) {

        long spectrumIndex = indexMap.get(spectrumTitle);

        mutex.acquire();

        MappedByteBuffer mappedByteBuffer = getMappedByteBuffer(spectrumIndex);
        mappedByteBuffer.getDouble(); // @TODO: better way of doing this?

        double precursorRt = mappedByteBuffer.getDouble();

        mutex.release();

        return precursorRt;

    }

    /**
     * Returns the spectrum level of the spectrum with the given title.
     *
     * @param spectrumTitle the title of the spectrum
     *
     * @return the spectrum level of the spectrum
     */
    public int getSpectrumLevel(String spectrumTitle) {

        long spectrumIndex = indexMap.get(spectrumTitle);

        mutex.acquire();

        MappedByteBuffer mappedByteBuffer = getMappedByteBuffer(spectrumIndex);
        mappedByteBuffer.getDouble(); // @TODO: better way of doing this?
        mappedByteBuffer.getDouble(); // @TODO: better way of doing this?
        mappedByteBuffer.getDouble(); // @TODO: better way of doing this?

        int spectrumLevel = mappedByteBuffer.getInt();

        mutex.release();

        return spectrumLevel;

    }

    /**
     * Returns the peaks of the spectrum with the given title.
     *
     * @param spectrumTitle the title of the spectrum
     *
     * @return the peaks of the spectrum
     */
    public double[][] getPeaks(String spectrumTitle) {

        long spectrumIndex = indexMap.get(spectrumTitle);

        mutex.acquire();

        MappedByteBuffer mappedByteBuffer = getMappedByteBuffer(spectrumIndex);
        mappedByteBuffer.getDouble(); // @TODO: better way of doing this?
        mappedByteBuffer.getDouble(); // @TODO: better way of doing this?
        mappedByteBuffer.getDouble(); // @TODO: better way of doing this?

        int compressedDataLength = mappedByteBuffer.getInt();
        int nPeaks = mappedByteBuffer.getInt();

        byte[] compressedSpectrum = new byte[compressedDataLength];
        mappedByteBuffer.get(compressedSpectrum);

        mutex.release();

        int uncompressedLength = nPeaks * 2 * Double.BYTES;

        byte[] uncompressedSpectrum = uncompress(compressedSpectrum, uncompressedLength);
        ByteBuffer byteBuffer = ByteBuffer.wrap(uncompressedSpectrum);

        double[][] peaks = new double[nPeaks][2];

        for (int i = 0; i < nPeaks; i++) {

            peaks[i][0] = byteBuffer.getDouble();
            peaks[i][1] = byteBuffer.getDouble();

        }

        return peaks;

    }

    /**
     * Uncompresses the given byte array.
     *
     * @param compressedByteArray the compressed byte array
     * @param uncompressedLength the uncompressed length
     *
     * @return the uncompressed array
     */
    public static byte[] uncompress(
            byte[] compressedByteArray,
            int uncompressedLength
    ) {

        ZstdDecompressor decompressor = new ZstdDecompressor(); // @TODO: consider externalizing, there should be only one per thread. 

        byte[] uncompressedByteAray = ZstdUtils.zstdDecompress(
                decompressor,
                compressedByteArray,
                uncompressedLength
        );

        return uncompressedByteAray;

    }

    @Override
    public Spectrum getSpectrum(String fileName, String spectrumTitle) {

        return getSpectrum(spectrumTitle);

    }

    @Override
    public Precursor getPrecursor(String fileName, String spectrumTitle) {

        return getPrecursor(spectrumTitle);

    }

    @Override
    public double getPrecursorMz(String fileName, String spectrumTitle) {

        return getPrecursorMz(spectrumTitle);

    }

    @Override
    public double getPrecursorRt(String fileName, String spectrumTitle) {

        return getPrecursorRt(spectrumTitle);

    }

    @Override
    public int getSpectrumLevel(String fileName, String spectrumTitle) {

        return getSpectrumLevel(spectrumTitle);

    }

    @Override
    public double[][] getPeaks(String fileName, String spectrumTitle) {

        return getPeaks(spectrumTitle);

    }

    @Override
    public double getMinPrecMz(String fileName) {

        return minMz;

    }

    @Override
    public double getMaxPrecMz(String fileName) {

        return maxMz;

    }

    @Override
    public double getMaxPrecInt(String fileName) {

        return maxInt;

    }

    @Override
    public double getMaxPrecRT(String fileName) {

        return maxRt;

    }

    @Override
    public double getMinPrecMz() {

        return minMz;

    }

    @Override
    public double getMaxPrecMz() {

        return maxMz;

    }

    @Override
    public double getMaxPrecInt() {

        return maxInt;

    }

    @Override
    public double getMaxPrecRT() {

        return maxRt;

    }

    @Override
    public String[] getOrderedFileNamesWithoutExtensions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashMap<String, String> getFilePaths() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashMap<String, String> getCmsFilePaths() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {

        try {

            for (MappedByteBuffer mappedByteBuffer : mappedByteBuffers) {
                IoUtil.closeBuffer(mappedByteBuffer);
            }

            raf.close();

        } catch (IOException e) {

            throw new RuntimeException(e);

        }

    }

    @Override
    public String[] getSpectrumTitles(String fileName) {

        return titles;

    }

    @Override
    public ArrayList<String> getPostcursorSpectrumTitles(
            String fileNameWithoutExtension,
            String spectrumTitle
    ) {

        if (postcursorMap == null || !postcursorMap.containsKey(spectrumTitle)) {

            return null;

        } else {

            return postcursorMap.get(spectrumTitle);

        }

    }

    /**
     * Returns the mapped buffer for the given spectrum index.
     *
     * @param spectrumIndex the index of the spectrum
     * @return the mapped buffer for the given spectrum index
     */
    private MappedByteBuffer getMappedByteBuffer(long spectrumIndex) {

        MappedByteBuffer mappedByteBuffer = null;

        if (bufferStartIndexes.size() == 1) {

            long startIndex = bufferStartIndexes.get(0);
            mappedByteBuffer = mappedByteBuffers.get(0);
            mappedByteBuffer.position((int) (spectrumIndex - startIndex));

            return mappedByteBuffer;

        }

        for (int i = 1; i < bufferStartIndexes.size(); i++) {

            long startIndex = bufferStartIndexes.get(i - 1);
            long endIndex = bufferStartIndexes.get(i);

            if (spectrumIndex >= startIndex && spectrumIndex < endIndex) {

                mappedByteBuffer = mappedByteBuffers.get(i - 1);
                mappedByteBuffer.position((int) (spectrumIndex - startIndex));

                return mappedByteBuffer;

            }

        }

        // special case for the final buffer
        if (mappedByteBuffer == null) {

            long startIndex = bufferStartIndexes.get(bufferStartIndexes.size() - 1);
            mappedByteBuffer = mappedByteBuffers.get(bufferStartIndexes.size() - 1);
            mappedByteBuffer.position((int) (spectrumIndex - startIndex));

            return mappedByteBuffer;

        }

        return null;

    }

}
