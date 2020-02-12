package com.compomics.util.experiment.io.mass_spectrometry.writers;

import com.compomics.util.experiment.mass_spectrometry.FragmentationMethod;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.flat.SimpleFileWriter;
import java.io.File;
import java.util.stream.IntStream;

/**
 * This class writes spectrum files in Andromeda Peak List (APL) format.
 *
 * @author Marc Vaudel
 */
public class AplFileWriter implements AutoCloseable {

    /**
     * The file writer.
     */
    private final SimpleFileWriter writer;

    /**
     * Empty default constructor
     */
    public AplFileWriter() {
        writer = null;
    }

    /**
     * Constructor.
     * 
     * @param destinationFile The file where to write.
     */
    public AplFileWriter(
            File destinationFile
    ) {
        writer = new SimpleFileWriter(destinationFile, false);
    }

    /**
     * Writes the given spectrum to the file.
     *
     * @param spectrumTitle The title of the spectrum to write.
     * @param spectrum The spectrum to write.
     * @param fragmentationMethod The fragmentation method.
     * @param charge The charge to consider for this spectrum.
     */
    public void writeSpectrum(
            String spectrumTitle,
            Spectrum spectrum,
            FragmentationMethod fragmentationMethod,
            int charge
    ) {

        writer.writeLine("peaklist start");

        Precursor precursor = spectrum.getPrecursor();
        writer.writeLine(
                String.join("",
                        "mz=",
                        Double.toString(precursor.mz)
                )
        );
        writer.writeLine(
                String.join("",
                        "fragmentation=",
                        fragmentationMethod.name
                )
        );
        writer.writeLine(
                String.join("",
                        "charge=",
                        Integer.toString(charge)
                )
        );
        writer.writeLine(
                String.join("",
                        "header=",
                        spectrumTitle
                )
        );

        IntStream.range(0, spectrum.getNPeaks())
                .forEach(
                        i -> writer.writeLine(
                                String.join("\t",
                                        Double.toString(spectrum.mz[i]),
                                        Double.toString(spectrum.intensity[i])
                                )
                        )
                );

        writer.writeLine("peaklist end");
        writer.newLine();
        
    }

    @Override
    public void close() {

        writer.close();

    }
}
