package com.compomics.util.experiment.io.mass_spectrometry;

import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileIterator;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileUtils;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileIterator;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileUtils;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Interface for mass spectrometry file readers.
 *
 * @author Marc Vaudel
 */
public interface MsFileIterator extends AutoCloseable {

    /**
     * Returns the title of the next spectrum, null if none.
     *
     * @return The title of the next spectrum.
     */
    public String next();

    /**
     * Returns the spectrum corresponding to the title returned by the last call
     * to the next() method.
     *
     * @return The spectrum corresponding to the title returned by the last call
     * to the next() method.
     */
    public Spectrum getSpectrum();
    
    @Override
    public void close();

    /**
     * Returns the file reader for the given mass spectrometry file based on its
     * extension.
     *
     * @param file The mass spectrometry file.
     *
     * @return The file reader.
     * 
     * @throws java.io.IOException Exception thrown if an error occurred while reading the file.
     */
    public static MsFileIterator getMsFileIterator(File file) throws IOException {

        String fileName = file.getName();

        if (Arrays.stream(MgfFileUtils.EXTENSIONS).anyMatch(
                extension -> fileName.endsWith(extension)
        )) {

            return new MgfFileIterator(file);

        } else if (fileName.endsWith(CmsFileUtils.EXTENSION)) {

            return new CmsFileIterator(file);

        }

        String supportedExtensions = String.join(",",
                getSupportedExtensions());

        throw new UnsupportedOperationException("Mass spectrometry file extension not recognized. Supported: " + supportedExtensions);

    }

    /**
     * Returns the supported extensions.
     *
     * @return The supported extensions.
     */
    public static String[] getSupportedExtensions() {

        String[] cmsExtensions = new String[]{CmsFileUtils.EXTENSION};

        return Stream.concat(Arrays.stream(MgfFileUtils.EXTENSIONS), Arrays.stream(cmsExtensions))
                .toArray(String[]::new);

    }

}
