package com.compomics.util.experiment.io.mass_spectrometry.cms;

import com.compomics.util.experiment.io.mass_spectrometry.MsFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.waiting.WaitingHandler;
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
     * The waiting handler used to provide progress feedback and cancel the
     * process.
     */
    private final WaitingHandler waitingHandler;
    /**
     * The index of the current spectrum.
     */
    private int i = -1;

    /**
     * Constructor.
     *
     * @param file The file to iterate.
     * @param waitingHandler The waiting handler.
     *
     * @throws IOException Exception thrown if an error occurred while reading
     * the file.
     */
    public CmsFileIterator(
            File file, WaitingHandler waitingHandler
    ) throws IOException {

        this.reader = new CmsFileReader(file, waitingHandler);

        this.waitingHandler = waitingHandler;
        
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(100);

    }

    @Override
    public String next() {

        i++;

        // Update progress
        double progress = 100.0 * ((double) i) / reader.titles.length;
        waitingHandler.setSecondaryProgressCounter((int) progress);

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

        waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        reader.close();

    }

}
