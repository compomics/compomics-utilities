package com.compomics.util.experiment.io.mass_spectrometry;

import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * An iterator of the spectra in an mgf file.
 *
 * @author Marc Vaudel
 */
public class MgfFileIterator {

    /**
     * The reader going through the file.
     */
    private BufferedReader br;
    /**
     * The next spectrum in the file.
     */
    private Spectrum nextSpectrum = null;
    /**
     * The name of the mgf file.
     */
    private String mgfFileName;
    /**
     * The rank of the spectrum.
     */
    private int rank;
    /**
     * Boolean indicating whether the stream was closed.
     */
    private boolean streamClosed = false;

    /**
     * Constructor.
     *
     * @param mgfFile the file to go through
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public MgfFileIterator(File mgfFile) throws FileNotFoundException, IOException {
        mgfFileName = mgfFile.getName();
        br = new BufferedReader(new FileReader(mgfFile));

        if (mgfFile.getName().endsWith("mgf")) {
            nextSpectrum = MgfReader.getSpectrum(br, mgfFileName);
        } else {
            //MspReader mspr=new MspReader();
            nextSpectrum = MspReader.getSpectrum(br, mgfFileName);
        }

        rank = 1;
        if (nextSpectrum.getScanNumber() == null) {
            nextSpectrum.setScanNumber(rank + "");
        } else {
            while (nextSpectrum.getScanNumber().equals(++rank + ""));
        }
    }

    /**
     * Indicates whether the file contains another spectrum.
     *
     * @return a boolean indicating whether the file contains another spectrum
     */
    public boolean hasNext() {
        return nextSpectrum != null;
    }

    /**
     * Returns the next spectrum in the file.
     *
     * @return the next spectrum in the file
     *
     * @throws IOException if an IOException occurs
     */
    public synchronized Spectrum next() throws IOException {

        Spectrum currentSpectrum = nextSpectrum;
        if (!streamClosed) {

            if (mgfFileName.endsWith("mgf")) {
                nextSpectrum = MgfReader.getSpectrum(br, mgfFileName);
            } else {
                nextSpectrum = MspReader.getSpectrum(br, mgfFileName);
            }
        } else {
            nextSpectrum = null;
        }

        if (nextSpectrum == null) {
            if (!streamClosed) {
                br.close();
                streamClosed = true;
            }
        } else if (nextSpectrum.getScanNumber() == null) {
            nextSpectrum.setScanNumber(++rank + "");
        } else {
            while (nextSpectrum.getScanNumber().equals(++rank + ""));
        }

        return currentSpectrum;
    }
}
