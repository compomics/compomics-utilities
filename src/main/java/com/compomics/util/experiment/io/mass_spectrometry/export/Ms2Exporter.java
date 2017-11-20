package com.compomics.util.experiment.io.mass_spectrometry.export;

import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * This converter writes spectrum files in MS2 format.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Ms2Exporter {

    /**
     * Writes the content of the given file name into the destination file in
     * the ms2 format. The spectra must be loaded in the factory.
     *
     * @param mgfFile the original mgf file
     * @param destinationFile the destination file where to write the spectra
     * @param resetScanNumbers If true the scan numbers are reset to simply
     * indicate the spectrum indexes, first spectrum is scan 1, second is scan
     * 2, etc. Required in order to map back to the mgf.
     *
     * @throws IOException exception thrown whenever an error occurred while reading or writing a file
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public static void mgfToMs2(File mgfFile, File destinationFile, boolean resetScanNumbers) throws IOException, InterruptedException {

        try (FileWriter fileWriter = new FileWriter(destinationFile); 
                BufferedWriter bw = new BufferedWriter(fileWriter)) {
            
            writeHeader(bw);
            MgfFileIterator mgfFileIterator = new MgfFileIterator(mgfFile);
            int scanCounter = 1;
            while (mgfFileIterator.hasNext()) {
                Spectrum spectrum = mgfFileIterator.next();
                if (resetScanNumbers) {
                    writeSpectrum(bw, spectrum, scanCounter++);
                } else {
                    writeSpectrum(bw, spectrum, null);
                }
            }
        }
    }

    /**
     * Writes the header of the file.
     *
     * @param bw a buffered writer where to write
     *
     * @throws IOException exception thrown whenever an error occurred while reading or writing a file
     */
    public static void writeHeader(BufferedWriter bw) throws IOException {
        
        bw.write("H\tCreationDate\t" + new Date());
        bw.newLine();
        bw.write("H\tExtractor\tUnknown");
        bw.newLine();
        bw.write("H\tExtractorVersion\tUnknown");
        bw.newLine();
        bw.write("H\tExtractorOptions\tUnknown");
        bw.newLine();
        bw.write("H\tComment\tCreated by compomics utilities based on http://cruxtoolkit.sourceforge.net/ms2-format.html");
        bw.newLine();
    }

    /**
     * Writes the given spectrum in ms2 format.
     *
     * @param bw a buffered writer where to write the spectrum
     * @param spectrum the spectrum of interest
     * @param defaultScanNumber if not null, overrides the scan number given in
     * the file (needed when converting mgf to ms2)
     *
     * @throws IOException exception thrown whenever an error occurred while reading or writing a file
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public static void writeSpectrum(BufferedWriter bw, Spectrum spectrum, Integer defaultScanNumber) throws IOException, InterruptedException {

        String scanNumber = spectrum.getScanNumber(); //@TODO: parse scan ranges?

        // replace the scan number
        if (defaultScanNumber != null) {
            scanNumber = defaultScanNumber.toString();
        }

        Precursor precursor = spectrum.getPrecursor();
        bw.write("S\t" + scanNumber + "\t" + scanNumber + "\t" + precursor.getMz());
        bw.newLine();

        ArrayList<Integer> charges = precursor.getPossibleCharges();
        for (int charge : charges) {
            bw.write("Z\t" + charge + "\t" + precursor.getMassPlusProton(charge));
            bw.newLine();
        }

        HashMap<Double, Peak> peakMap = spectrum.getPeakMap();
        for (double mz : spectrum.getOrderedMzValues()) {
            Peak peak = peakMap.get(mz);
            bw.write(peak.mz + " " + peak.intensity);
            bw.newLine();
        }

        bw.newLine();
    }
}
