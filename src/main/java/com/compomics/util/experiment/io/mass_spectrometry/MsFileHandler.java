package com.compomics.util.experiment.io.mass_spectrometry;

import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileReader;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileUtils;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileWriter;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.IoUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * A spectrum provider for mass spectrometry files based on Compomics Mass
 * Spectrometry (cms) files.
 *
 * @author Marc Vaudel
 */
public class MsFileHandler implements SpectrumProvider {

    /**
     * Map of ms file name to file path.
     */
    private final HashMap<String, String> filePathMap = new HashMap<>();
    /**
     * Map of ms file name to cms file path.
     */
    private final HashMap<String, String> cmsFilePathMap = new HashMap<>();
    /**
     * Map of ms file name to cms reader.
     */
    private final HashMap<String, CmsFileReader> cmsFileReaderMap = new HashMap<>();
    /**
     * Array of the file names ordered alphabetically.
     */
    private String[] orderedFileNames = new String[0];

    /**
     * Constructor.
     */
    public MsFileHandler() {

    }

    /**
     * Registers a mass spectrometry file and enables querying its spectra. If
     * the file is not a cms file, a cms file will be created along the ms file.
     *
     * @param msFile The mass spectrometry file to register.
     *
     * @throws IOException Exception thrown if an error occurs while reading or
     * writing a file.
     */
    public void register(
            File msFile
    ) throws IOException {

        register(msFile);

    }

    /**
     * Registers a mass spectrometry file and enables querying its spectra. If
     * the file is not a cms file, a cms file will be created in the cms folder
     * if not null, or along the ms file.
     *
     * @param msFile The mass spectrometry file to register.
     * @param cmsFolder The folder where to save the cms files.
     *
     * @throws IOException Exception thrown if an error occurs while reading or
     * writing a file.
     */
    public void register(
            File msFile,
            File cmsFolder
    ) throws IOException {

        String fileName = msFile.getName();

        orderedFileNames = Stream.concat(Arrays.stream(orderedFileNames), Stream.of(fileName))
                .sorted()
                .toArray(String[]::new);

        String cmsFilePath = getCmsFilePath(
                msFile,
                cmsFolder
        );

        filePathMap.put(fileName, msFile.getAbsolutePath());
        cmsFilePathMap.put(fileName, cmsFilePath);

        File cmsFile = new File(cmsFilePath);
        CmsFileReader reader = null;

        if (cmsFile.exists()) {

            try {

                reader = new CmsFileReader(cmsFile);

            } catch (Exception e) {

                // Not a compatible file, delete and make new one
                if (!cmsFile.delete()) {

                    throw new IOException("File " + cmsFile + " could not be read as cms file or deleted. Please move or delete the file manually.");

                }
            }
        }

        if (reader == null) {

            writeCmsFile(msFile, cmsFile);
            reader = new CmsFileReader(cmsFile);

        }

        cmsFileReaderMap.put(fileName, reader);

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
    private String getCmsFilePath(
            File msFile,
            File cmsFolder
    ) {

        String fileName = msFile.getName();

        if (fileName.endsWith(CmsFileUtils.EXTENSION)) {

            return msFile.getAbsolutePath();

        }

        String newName = IoUtils.removeExtension(fileName) + CmsFileUtils.EXTENSION;

        File folder = cmsFolder == null ? msFile.getParentFile() : cmsFolder;

        File cmsFile = new File(folder, newName);

        return cmsFile.getAbsolutePath();

    }

    /**
     * Writes a cms file for the given mass spectrometry file.
     *
     * @param msFile The mass spectrometry file.
     * @param cmsFile The cms file.
     *
     * @throws IOException Exception thrown if an error occurred while reading
     * or writing a file.
     */
    private void writeCmsFile(
            File msFile,
            File cmsFile
    ) throws IOException {

        try ( MsFileIterator iterator = MsFileIterator.getMsFileIterator(msFile)) {

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
     * @param fileName The name of the ms file.
     * 
     * @return The cms file reader.
     */
    public CmsFileReader getReader(
            String fileName
    ) {
        return cmsFileReaderMap.get(fileName);
    }

    @Override
    public Spectrum getSpectrum(
            String fileName,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

        if (reader != null) {

            return reader.getSpectrum(spectrumTitle);

        }

        return null;

    }

    @Override
    public Precursor getPrecursor(
            String fileName,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

        if (reader != null) {

            return reader.getPrecursor(spectrumTitle);

        }

        return null;
    }

    @Override
    public double getPrecursorMz(
            String fileName,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

        if (reader != null) {

            return reader.getPrecursorMz(spectrumTitle);

        }

        return Double.NaN;
    }

    @Override
    public double getPrecursorRt(
            String fileName,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

        if (reader != null) {

            return reader.getPrecursorRt(spectrumTitle);

        }

        return Double.NaN;
    }

    @Override
    public double[][] getPeaks(
            String fileName,
            String spectrumTitle
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

        if (reader != null) {

            return reader.getPeaks(spectrumTitle);

        }

        return null;
    }

    @Override
    public double[][] getPeaksAboveIntensityThreshold(
            String fileName,
            String spectrumTitle,
            AnnotationParameters.IntensityThresholdType intensityThresholdType,
            double thresholdValue
    ) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

        if (reader != null) {

            return reader.getPeaksAboveIntensityThreshold(
                    fileName,
                    spectrumTitle,
                    intensityThresholdType,
                    thresholdValue
            );

        }

        return null;
    }

    @Override
    public double getMinPrecMz(String fileName) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

        if (reader != null) {

            return reader.getMinPrecMz();

        }

        return Double.NaN;
    }

    @Override
    public double getMaxPrecMz(String fileName) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

        if (reader != null) {

            return reader.getMaxPrecMz();

        }

        return Double.NaN;
    }

    @Override
    public double getMaxPrecRT(String fileName) {

        CmsFileReader reader = cmsFileReaderMap.get(fileName);

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
    public double getMaxPrecRT() {

        return cmsFileReaderMap.values().stream()
                .mapToDouble(
                        reader -> reader.getMaxPrecRT()
                )
                .max()
                .orElse(Double.NaN);
    }

    @Override
    public String[] getFileNames() {

        return orderedFileNames;

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
}
