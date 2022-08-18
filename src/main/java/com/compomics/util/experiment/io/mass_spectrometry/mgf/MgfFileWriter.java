package com.compomics.util.experiment.io.mass_spectrometry.mgf;

import com.compomics.util.experiment.biology.ions.Charge;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.flat.SimpleFileWriter;
import java.io.File;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class writes spectrum files in Mascot Generic File (mgf) format.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MgfFileWriter implements AutoCloseable {

    /**
     * The file writer.
     */
    private final SimpleFileWriter writer;

    /**
     * Empty default constructor.
     */
    public MgfFileWriter() {
        writer = null;
    }

    /**
     * Constructor.
     *
     * @param destinationFile The file where to write.
     */
    public MgfFileWriter(
            File destinationFile
    ) {
        writer = new SimpleFileWriter(destinationFile, false);
    }

    /**
     * Writes the given spectrum to the file.
     *
     * @param spectrumTitle The title of the spectrum to write.
     * @param spectrum The spectrum to write.
     */
    public void writeSpectrum(
            String spectrumTitle,
            Spectrum spectrum
    ) {

        String toWrite = asMgf(
                spectrumTitle,
                spectrum
        );
        writer.write(toWrite, true);

    }

    @Override
    public void close() {

        writer.close();

    }

    /**
     * Returns the spectrum as an mgf bloc.
     *
     * @param spectrumTitle The title of the spectrum.
     * @param spectrum The spectrum.
     *
     * @return the spectrum as an mgf bloc
     */
    public static String asMgf(
            String spectrumTitle,
            Spectrum spectrum
    ) {
        return asMgf(
                spectrumTitle,
                spectrum,
                null
        );
    }

    /**
     * Returns the spectrum as an mgf bloc.
     *
     * @param spectrumTitle The title of the spectrum.
     * @param spectrum The spectrum.
     * @param additionalTags additional tags which will be added after the BEGIN
     * IONS tag in alphabetic order
     *
     * @return the peak list as an mgf bloc
     */
    public static String asMgf(
            String spectrumTitle,
            Spectrum spectrum,
            TreeMap<String, String> additionalTags
    ) {

        StringBuilder result = new StringBuilder();
        String lineBreak = System.getProperty("line.separator");

        result.append("BEGIN IONS").append(lineBreak);

        if (additionalTags != null) {

            for (Entry<String, String> tag : additionalTags.entrySet()) {

                result
                        .append(tag.getKey())
                        .append("=")
                        .append(tag.getValue())
                        .append(lineBreak);

            }
        }

        result.append("TITLE=").append(spectrumTitle).append(lineBreak);

        Precursor precursor = spectrum.precursor;

        result.append("RTINSECONDS=").append(precursor.rt).append(lineBreak);

        result.append("PEPMASS=").append(precursor.mz);

        if (precursor.intensity > 0) {

            result.append(" ").append(precursor.intensity);

        }

        result.append(lineBreak);

        if (precursor.possibleCharges.length > 0) {
            result.append("CHARGE=");
            result.append(
                    Arrays.stream(precursor.possibleCharges)
                            .sorted()
                            .mapToObj(charge -> Charge.toString(charge))
                            .collect(Collectors.joining(" and "))
            );
            result.append(lineBreak);
        }

        // export peak list sorted by mz
        IntStream.range(0, spectrum.getNPeaks())
                .forEach(
                        i -> result.append(spectrum.mz[i]).append(' ').append(spectrum.intensity[i]).append(lineBreak)
                );

        result.append("END IONS").append(lineBreak);

        return result.toString();

    }

}
