package com.compomics.util.experiment.io.mass_spectrometry;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.io.mass_spectrometry.apl.AplFileWriter;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileWriter;
import com.compomics.util.experiment.io.mass_spectrometry.ms2.Ms2FileWriter;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.AndromedaParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.util.stream.IntStream;

/**
 * This class writes ms files in various formats.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MsFileExporter {

    /**
     * Enum of the supported export formats.
     */
    public static enum Format {

        mgf, apl, ms2

    }

    /**
     * Writes the spectra of a file in the given format.
     *
     * @param spectrumProvider The spectrum provider to use to get the spectra.
     * @param fileName The name of the file to export.
     * @param destinationFile The file where to write.
     * @param format The format to write in.
     * @param searchParameters The search parameters.
     * @param waitingHandler The waiting handler to use to inform on progress
     * and allow cancelling.
     */
    public static void writeMsFile(
            SpectrumProvider spectrumProvider,
            String fileName,
            File destinationFile,
            Format format,
            SearchParameters searchParameters,
            WaitingHandler waitingHandler
    ) {

        switch (format) {

            case mgf:
                writeMgfFile(spectrumProvider, fileName, destinationFile, waitingHandler);
                return;

            case apl:
                writeAplFile(spectrumProvider, fileName, destinationFile, searchParameters, waitingHandler);
                return;

            case ms2:
                writeMs2File(spectrumProvider, fileName, destinationFile, waitingHandler);
                return;

            default:
                throw new UnsupportedOperationException(
                        "Format " + format + " not supported."
                );
        }
    }

    /**
     * Writes the spectra of a file in the Andromeda peak list (apl) format.
     *
     * @param spectrumProvider The spectrum provider to use to get the spectra.
     * @param fileName The name of the file to export.
     * @param destinationFile The file where to write.
     * @param searchParameters The search parameters.
     * @param waitingHandler The waiting handler to use to inform on progress
     * and allow cancelling.
     */
    public static void writeAplFile(
            SpectrumProvider spectrumProvider,
            String fileName,
            File destinationFile,
            SearchParameters searchParameters,
            WaitingHandler waitingHandler
    ) {

        AndromedaParameters andromedaParameters = (AndromedaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex());

        String[] spectrumTitles = spectrumProvider.getSpectrumTitles(fileName);

        if (spectrumTitles == null) {

            throw new IllegalArgumentException(
                    fileName + " not loaded."
            );
        }

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(spectrumTitles.length);

        AplFileWriter writer = new AplFileWriter(destinationFile);

        for (String spectrumTitle : spectrumTitles) {

            Spectrum spectrum = spectrumProvider.getSpectrum(fileName, spectrumTitle);

            int[] charges = spectrum.getPrecursor().possibleCharges;

            if (charges.length == 0) {

                charges = IntStream.rangeClosed(searchParameters.getMinChargeSearched(), searchParameters.getMaxChargeSearched())
                        .toArray();

            }

            if (charges.length == 1) {

                writer.writeSpectrum(
                        spectrumTitle,
                        spectrum,
                        andromedaParameters.getFragmentationMethod(),
                        charges[0]
                );

            } else {

                for (int charge : charges) {

                    String tempTitle = String.join("_", spectrumTitle, Integer.toString(charge));

                    writer.writeSpectrum(
                            tempTitle,
                            spectrum,
                            andromedaParameters.getFragmentationMethod(),
                            charges[0]
                    );
                }
            }

            waitingHandler.increaseSecondaryProgressCounter();

            if (waitingHandler.isRunCanceled()) {

                break;

            }
        }

        waitingHandler.setSecondaryProgressCounterIndeterminate(true);

    }

    /**
     * Writes the spectra of a file in the Mascot Generic File (mgf) format.
     *
     * @param spectrumProvider The spectrum provider to use to get the spectra.
     * @param fileName The name of the file to export.
     * @param destinationFile The file where to write.
     * @param waitingHandler The waiting handler to use to inform on progress
     * and allow cancelling.
     */
    public static void writeMgfFile(
            SpectrumProvider spectrumProvider,
            String fileName,
            File destinationFile,
            WaitingHandler waitingHandler
    ) {

        String[] spectrumTitles = spectrumProvider.getSpectrumTitles(fileName);

        if (spectrumTitles == null) {

            throw new IllegalArgumentException(
                    fileName + " not loaded."
            );
        }

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(spectrumTitles.length);

        MgfFileWriter writer = new MgfFileWriter(destinationFile);

        for (String spectrumTitle : spectrumTitles) {

            Spectrum spectrum = spectrumProvider.getSpectrum(fileName, spectrumTitle);

            writer.writeSpectrum(spectrumTitle, spectrum);

            waitingHandler.increaseSecondaryProgressCounter();

            if (waitingHandler.isRunCanceled()) {

                break;

            }
        }

        writer.close();

        waitingHandler.setSecondaryProgressCounterIndeterminate(true);

    }

    /**
     * Writes the spectra of a file in the ms2 format.
     *
     * @param spectrumProvider The spectrum provider to use to get the spectra.
     * @param fileName The name of the file to export.
     * @param destinationFile The file where to write.
     * @param waitingHandler The waiting handler to use to inform on progress
     * and allow cancelling.
     */
    public static void writeMs2File(
            SpectrumProvider spectrumProvider,
            String fileName,
            File destinationFile,
            WaitingHandler waitingHandler
    ) {

        String[] spectrumTitles = spectrumProvider.getSpectrumTitles(fileName);

        if (spectrumTitles == null) {

            throw new IllegalArgumentException(
                    fileName + " not loaded."
            );
        }

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(spectrumTitles.length);

        Ms2FileWriter writer = new Ms2FileWriter(destinationFile);

        writer.writeHeader();

        for (int i = 0; i < spectrumTitles.length; i++) {

            String spectrumTitle = spectrumTitles[i];
            Spectrum spectrum = spectrumProvider.getSpectrum(fileName, spectrumTitle);
            writer.writeSpectrum(spectrum, i);

            waitingHandler.increaseSecondaryProgressCounter();

            if (waitingHandler.isRunCanceled()) {

                break;

            }
        }

        waitingHandler.setSecondaryProgressCounterIndeterminate(true);

    }
}
