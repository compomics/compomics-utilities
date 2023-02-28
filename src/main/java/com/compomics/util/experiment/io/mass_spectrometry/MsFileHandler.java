package com.compomics.util.experiment.io.mass_spectrometry;

import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileReader;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileUtils;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileWriter;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.IoUtil;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * A spectrum provider for mass spectrometry files based on Compomics Mass
 * Spectrometry (cms) files.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MsFileHandler implements SpectrumProvider {

    /**
     * Map of ms file name (without extension) to file path.
     */
    private final HashMap<String, String> filePathMap = new HashMap<>();
    /**
     * Map of ms file name (without extension) to cms file path.
     */
    private final HashMap<String, String> cmsFilePathMap = new HashMap<>();
    /**
     * Map of ms file (without extension) name to cms reader.
     */
    private final HashMap<String, CmsFileReader> cmsFileReaderMap = new HashMap<>();
    /**
     * Array of the file names (without extensions) ordered alphabetically.
     */
    private String[] orderedFileNamesWithoutExtensions = new String[0];

    /**
     * Constructor.
     */
    public MsFileHandler() {

    }

    /**
     * Registers a mass spectrometry file and enables querying its spectra.If
     * the file is not a cms file, a cms file will be created along with the ms
     * file.
     *
     * @param msFile The mass spectrometry file to register.
     * @param waitingHandler The waiting handler.
     *
     * @throws IOException Exception thrown if an error occurs while reading or
     * writing a file.
     */
    public void register(
            File msFile,
            WaitingHandler waitingHandler
    ) throws IOException {
        register(msFile, null, waitingHandler);
    }

    /**
     * Registers a mass spectrometry file and enables querying its spectra.If
     * the file is not a cms file, a cms file will be created in the cms folder
     * if not null, along the ms file otherwise.
     *
     * @param msFile The mass spectrometry file to register.
     * @param cmsFolder The folder where to save the cms files.
     * @param waitingHandler The waiting handler.
     *
     * @throws IOException Exception thrown if an error occurs while reading or
     * writing a file.
     */
    public void register(
            File msFile,
            File cmsFolder,
            WaitingHandler waitingHandler
    ) throws IOException {

        // Check whether the file exists but with a different case for the extension.
        msFile = IoUtil.existsExtensionNotCaseSensitive(msFile);

        if (!msFile.exists()) {

            throw new FileNotFoundException("MS file " + msFile + " not found.");

        }

        String spectrumFileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());

        orderedFileNamesWithoutExtensions = Stream.concat(Arrays.stream(orderedFileNamesWithoutExtensions), Stream.of(spectrumFileNameWithoutExtension))
                .distinct()
                .sorted()
                .toArray(String[]::new);

        String cmsFilePath = getCmsFilePath(
                msFile,
                cmsFolder
        );

        filePathMap.put(spectrumFileNameWithoutExtension, msFile.getAbsolutePath());
        cmsFilePathMap.put(spectrumFileNameWithoutExtension, cmsFilePath);

        File cmsFile = new File(cmsFilePath);
        CmsFileReader reader = null;

        if (cmsFile.exists()) {

            try {

                reader = new CmsFileReader(cmsFile, waitingHandler);

            } catch (Exception e) {

                try {

                    if (reader != null) {

                        reader.close();

                    }

                } catch (Throwable t) {

                    // ignore
                }

                // Not a compatible file, delete and make new one.
                if (!cmsFile.delete()) {

                    throw new IOException("File " + cmsFile + " could not be read "
                            + "as cms file or deleted. Please move or delete the file manually.");

                }
            }
        }

        // the latter check is needed in case the cms and the ms file is 
        // the same file, as outdated cms files can have been deleted above
        if (reader == null && msFile.exists()) {

            writeCmsFile(
                    msFile,
                    cmsFile,
                    waitingHandler
            );
            reader = new CmsFileReader(
                    cmsFile,
                    waitingHandler
            );

        }

        if (reader != null) {
            cmsFileReaderMap.put(spectrumFileNameWithoutExtension, reader);
        }
    }

    /**
     * Returns the path of the cms file expected for the given mass spectrometry
     * file and cms folder.
     *
     * @param msFile The mass spectrometry file.
     * @param cmsFolder The folder where to save the cms files.
     *
     * @return The path of the cms file.
     */
    public static String getCmsFilePath(
            File msFile,
            File cmsFolder
    ) {

        String fileName = msFile.getName();

        if (fileName.endsWith(CmsFileUtils.EXTENSION)) {

            return msFile.getAbsolutePath();

        }

        String newName = IoUtil.removeExtension(fileName) + CmsFileUtils.EXTENSION;

        File folder = cmsFolder == null ? msFile.getParentFile() : cmsFolder;

        File cmsFile = new File(folder, newName);

        return cmsFile.getAbsolutePath();

    }

    /**
     * Writes a cms file for the given mass spectrometry file.
     *
     * @param msFile The mass spectrometry file.
     * @param cmsFile The cms file.
     * @param waitingHandler The waiting handler.
     *
     * @throws IOException Exception thrown if an error occurred while reading
     * or writing a file.
     */
    private void writeCmsFile(
            File msFile,
            File cmsFile,
            WaitingHandler waitingHandler
    ) throws IOException {

        try ( MsFileIterator iterator = MsFileIterator.getMsFileIterator(msFile, waitingHandler)) {

            try ( CmsFileWriter writer = new CmsFileWriter(cmsFile)) {

                String spectrumTitle;

                while ((spectrumTitle = iterator.next()) != null) {

                    Spectrum spectrum = iterator.getSpectrum();
                    writer.addSpectrum(spectrumTitle, spectrum);

                }
            }
        }
    }

    /**
     * Returns the cms file reader for the given ms file. Null if not set.
     *
     * @param fileNameWithoutExtension The name of the ms file without file
     * extension.
     *
     * @return The cms file reader.
     */
    public CmsFileReader getReader(
            String fileNameWithoutExtension
    ) {
        return cmsFileReaderMap.get(fileNameWithoutExtension);
    }

    @Override
    public Spectrum getSpectrum(
            String fileNameWithoutExtension,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        return reader == null ? null : reader.getSpectrum(spectrumTitle);

    }

    @Override
    public Precursor getPrecursor(
            String fileNameWithoutExtension,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        return reader == null ? null : reader.getPrecursor(spectrumTitle);

    }

    @Override
    public double getPrecursorMz(
            String fileNameWithoutExtension,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        return reader == null ? Double.NaN : reader.getPrecursorMz(spectrumTitle);

    }

    @Override
    public double getPrecursorRt(
            String fileNameWithoutExtension,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        return reader == null ? Double.NaN : reader.getPrecursorRt(spectrumTitle);

    }

    @Override
    public int getSpectrumLevel(
            String fileNameWithoutExtension,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        return reader == null ? 2 : reader.getSpectrumLevel(spectrumTitle);

    }

    @Override
    public double[][] getPeaks(
            String fileNameWithoutExtension,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        if (reader != null) {

            return reader.getPeaks(spectrumTitle);

        }

        return null;
    }

    @Override
    public double getMinPrecMz(String fileNameWithoutExtension) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        if (reader != null) {

            return reader.getMinPrecMz();

        }

        return Double.NaN;
    }

    @Override
    public double getMaxPrecMz(String fileNameWithoutExtension) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        if (reader != null) {

            return reader.getMaxPrecMz();

        }

        return Double.NaN;
    }

    @Override
    public double getMaxPrecInt(String fileNameWithoutExtension) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        if (reader != null) {

            return reader.getMaxPrecInt();

        }

        return Double.NaN;
    }

    @Override
    public double getMaxPrecRT(String fileNameWithoutExtension) {

        CmsFileReader reader = cmsFileReaderMap.get(fileNameWithoutExtension);

        if (reader != null) {

            return reader.getMaxPrecRT();

        }

        return Double.NaN;
    }

    @Override
    public double getMinPrecMz() {

        return cmsFileReaderMap.values().stream()
                .mapToDouble(
                        reader -> reader.getMinPrecMz()
                )
                .min()
                .orElse(Double.NaN);
    }

    @Override
    public double getMaxPrecMz() {

        return cmsFileReaderMap.values().stream()
                .mapToDouble(
                        reader -> reader.getMaxPrecMz()
                )
                .max()
                .orElse(Double.NaN);
    }

    @Override
    public double getMaxPrecInt() {

        return cmsFileReaderMap.values().stream()
                .mapToDouble(
                        reader -> reader.getMaxPrecInt()
                )
                .max()
                .orElse(Double.NaN);
    }

    @Override
    public double getMaxPrecRT() {

        return cmsFileReaderMap.values().stream()
                .mapToDouble(
                        reader -> reader.getMaxPrecRT()
                )
                .max()
                .orElse(Double.NaN);
    }

    @Override
    public String[] getOrderedFileNamesWithoutExtensions() {

        return orderedFileNamesWithoutExtensions;

    }

    @Override
    public HashMap<String, String> getFilePaths() {

        return filePathMap;

    }

    @Override
    public HashMap<String, String> getCmsFilePaths() {

        return cmsFilePathMap;

    }

    @Override
    public void close() {

        cmsFileReaderMap.values().forEach(
                reader -> reader.close()
        );

    }

    @Override
    public String[] getSpectrumTitles(String fileName) {

        CmsFileReader reader = cmsFileReaderMap.get(IoUtil.getFileName(fileName));
        return reader == null ? null : reader.titles;

    }
}
