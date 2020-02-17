package com.compomics.util.experiment.io.mass_spectrometry.cms;

import com.compomics.util.experiment.io.mass_spectrometry.MsFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.File;
import java.io.IOException;

/**
 * Iterator for a Compomics Mass Spectrometry (cms) file.
 *
 * @author Marc Vaudel
 */
public class CmsFileIterator implements MsFileIterator {

    /**
     * The cms file reader.
     */
    private final CmsFileReader reader;
    /**
     * The index of the current spectrum.
     */
    private int i = -1;

    /**
     * Constructor.
     *
     * @param file The file to iterate.
     *
     * @throws IOException Exception thrown if an error occurred while reading
     * the file.
     */
    public CmsFileIterator(
            File file
    ) throws IOException {

        this.reader = new CmsFileReader(file);

    }

    @Override
    public String next() {

        i++;

        if (i == reader.titles.length) {

            return null;

        }

        return reader.titles[i];

    }

    @Override
    public Spectrum getSpectrum() {

        return reader.getSpectrum(reader.titles[i]);

    }

    @Override
    public void close() {

        reader.close();

    }

}
