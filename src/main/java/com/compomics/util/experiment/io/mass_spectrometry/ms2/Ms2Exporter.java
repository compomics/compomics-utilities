package com.compomics.util.experiment.io.mass_spectrometry.ms2;

import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.flat.SimpleFileWriter;
import java.io.File;
import java.util.Date;
import java.util.stream.IntStream;

/**
 * This class writes spectrum files in MS2 format.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class Ms2Exporter {

    /**
     * The file writer.
     */
    private final SimpleFileWriter writer;

    /**
     * Empty default constructor.
     */
    public Ms2Exporter() {
        writer = null;
    }

    /**
     * Constructor.
     *
     * @param destinationFile The file where to write.
     */
    public Ms2Exporter(
            File destinationFile
    ) {
        writer = new SimpleFileWriter(destinationFile, false);
    }

    /**
     * Writes the header of the file.
     */
    public void writeHeader() {

        writer.writeLine("H\tCreationDate\t" + new Date());
        writer.writeLine("H\tExtractor\tUnknown");
        writer.writeLine("H\tExtractorVersion\tUnknown");
        writer.writeLine("H\tExtractorOptions\tUnknown");
        writer.writeLine("H\tComment\tCreated by compomics utilities based on http://cruxtoolkit.sourceforge.net/ms2-format.html");

    }

    /**
     * Writes the given spectrum in ms2 format.
     *
     * @param spectrum The spectrum to write.
     * @param scanNumber the scan number
     */
    public void writeSpectrum(
            Spectrum spectrum,
            int scanNumber
    ) {

        Precursor precursor = spectrum.getPrecursor();
        writer.writeLine(
                String.join("\t",
                        "S",
                        Integer.toString(scanNumber),
                        Integer.toString(scanNumber),
                        Double.toString(precursor.mz)
                )
        );

        for (int charge : precursor.possibleCharges) {

            writer.writeLine(
                    String.join("\t",
                            "Z",
                            Integer.toString(charge),
                            Double.toString(precursor.getMass(charge) + ElementaryIon.proton.getTheoreticMass())
                    )
            );
        }

        IntStream.range(0, spectrum.getNPeaks())
                .forEach(
                        i -> writer.writeLine(
                                String.join(" ",
                                        Double.toString(spectrum.mz[i]),
                                        Double.toString(spectrum.intensity[i])
                                )
                        )
                );

        writer.newLine();

    }
}
