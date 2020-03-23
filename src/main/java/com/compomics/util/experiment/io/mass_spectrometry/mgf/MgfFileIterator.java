package com.compomics.util.experiment.io.mass_spectrometry.mgf;

import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.flat.SimpleFileReader;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileIterator;
import com.compomics.util.waiting.WaitingHandler;

/**
 * An iterator of the spectra in an mgf file.
 *
 * @author Marc Vaudel
 */
public class MgfFileIterator implements MsFileIterator {

    /**
     * The reader going through the file.
     */
    private final SimpleFileReader reader;
    /**
     * The waiting handler used to provide progress feedback and cancel the
     * process.
     */
    private final WaitingHandler waitingHandler;
    /**
     * The spectrum read in the last call of the next method.
     */
    private Spectrum spectrum = null;

    /**
     * Constructor.
     *
     * @param mgfFile the file to go through
     * @param waitingHandler the waiting handler
     */
    public MgfFileIterator(File mgfFile, WaitingHandler waitingHandler) {

        reader = SimpleFileReader.getFileReader(mgfFile);

        this.waitingHandler = waitingHandler;

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(100);

    }

    @Override
    public String next() {

        spectrum = null;

        double precursorMz = 0;
        double precursorIntensity = 0;
        double rt = -1.0;
        double rt1 = -1.0;
        double rt2 = -1.0;
        int[] precursorCharges = new int[0];
        String spectrumTitle = "";
        ArrayList<Double> mzList = new ArrayList<>(0);
        ArrayList<Double> intensityList = new ArrayList<>(0);
        boolean spectrumBlock = false;

        String line;
        while ((line = reader.readLine()) != null && !waitingHandler.isRunCanceled()) {

            // fix for lines ending with \r
            if (line.endsWith("\r")) {
                line = line.replace("\r", "");
            }

            if (line.startsWith("BEGIN IONS")) {

                spectrumTitle = "";
                mzList = new ArrayList<>();
                intensityList = new ArrayList<>();
                spectrumBlock = true;

            } else if (line.startsWith("TITLE")) {

                spectrumTitle = line.substring(line.indexOf('=') + 1);

                try {

                    spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8");

                } catch (UnsupportedEncodingException e) {

                    throw new IllegalArgumentException("An exception was thrown when trying to decode the mgf title '" + spectrumTitle + "'.");

                }
            } else if (line.startsWith("CHARGE")) {

                precursorCharges = parseCharges(line);

            } else if (line.startsWith("PEPMASS")) {

                String temp = line.substring(line.indexOf("=") + 1);
                String[] values = temp.split("\\s");
                precursorMz = Double.parseDouble(values[0]);

                if (values.length > 1) {

                    precursorIntensity = Double.parseDouble(values[1]);

                } else {

                    precursorIntensity = 0.0;

                }
            } else if (line.startsWith("RTINSECONDS")) {

                String rtInput = line.substring(line.indexOf('=') + 1);

                try {

                    String[] rtWindow = rtInput.split("-");

                    if (rtWindow.length == 1) {

                        String tempRt = rtWindow[0];
                        // possible fix for values like RTINSECONDS=PT121.250000S
                        if (tempRt.startsWith("PT") && tempRt.endsWith("S")) {
                            tempRt = tempRt.substring(2, tempRt.length() - 1);
                        }

                        rt = new Double(tempRt);

                    } else if (rtWindow.length == 2) {

                        rt1 = new Double(rtWindow[0]);
                        rt2 = new Double(rtWindow[1]);

                    }
                } catch (Exception e) {
                    System.out.println("An exception was thrown when trying to decode the retention time " + rtInput + " in spectrum " + spectrumTitle + ".");
                    e.printStackTrace();
                    // ignore exception, RT will not be parsed
                }
            } else if (line.startsWith("TOLU")) {
                // peptide tolerance unit not implemented
            } else if (line.startsWith("TOL")) {
                // peptide tolerance not implemented
            } else if (line.startsWith("SEQ")) {
                // sequence qualifier not implemented
            } else if (line.startsWith("COMP")) {
                // composition qualifier not implemented
            } else if (line.startsWith("ETAG")) {
                // error tolerant search sequence tag not implemented
            } else if (line.startsWith("TAG")) {
                // sequence tag not implemented
            } else if (line.startsWith("SCANS")) {
                // Ignore
            } else if (line.startsWith("TAG")) {
                // sequence tag not implemented
            } else if (line.startsWith("RAWSCANS")) {
                // raw scans not implemented
            } else if (line.startsWith("END IONS")) {

                Precursor precursor = rt1 != -1 && rt2 != -1
                        ? new Precursor(precursorMz, precursorIntensity, precursorCharges, rt1, rt2)
                        : new Precursor(rt, precursorMz, precursorIntensity, precursorCharges);

                double[] mzArray = mzList.stream()
                        .mapToDouble(
                                a -> a
                        )
                        .toArray();

                double[] intensityArray = intensityList.stream()
                        .mapToDouble(
                                a -> a
                        )
                        .toArray();

                spectrum = new Spectrum(precursor, mzArray, intensityArray);

                spectrumBlock = false;

                // Update progress
                double progress = reader.getProgressInPercent();
                waitingHandler.setSecondaryProgressCounter((int) progress);

                return spectrumTitle;

            } else if (spectrumBlock && !line.equals("")) {

                try {

                    String values[] = line.split("\\s+");
                    double mz = Double.valueOf(values[0]);
                    mzList.add(mz);

                    double intensity = Double.valueOf(values[1]);
                    intensityList.add(intensity);

                } catch (Exception e1) {
                    // ignore comments and all other lines
                }
            }
        }

        return null;
    }

    @Override
    public Spectrum getSpectrum() {
        return spectrum;
    }

    /**
     * Parses the charge line of an MGF files.
     *
     * @param chargeLine the charge line
     * @return the possible charges found
     */
    private static int[] parseCharges(String chargeLine) {

        ArrayList<Integer> result = new ArrayList<>(1);
        String tempLine = chargeLine.substring(chargeLine.indexOf("=") + 1);
        String[] chargesAnd = tempLine.split(" and ");
        ArrayList<String> chargesAsString = new ArrayList<>();

        for (String charge : chargesAnd) {
            for (String charge2 : charge.split(",")) {
                chargesAsString.add(charge2.trim());
            }
        }

        for (String chargeAsString : chargesAsString) {

            chargeAsString = chargeAsString.trim();

            if (!chargeAsString.isEmpty()) {
                try {
                    if (chargeAsString.endsWith("+")) {
                        int value = Integer.parseInt(chargeAsString.substring(0, chargeAsString.length() - 1));
                        result.add(value);
                    } else if (chargeAsString.endsWith("-")) {
                        int value = Integer.parseInt(chargeAsString.substring(0, chargeAsString.length() - 1));
                        result.add(value);
                    } else if (!chargeAsString.equalsIgnoreCase("Mr")) {
                        int value = Integer.parseInt(chargeAsString);
                        result.add(value);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("\'" + chargeAsString + "\' could not be processed as a valid precursor charge!");
                }
            }
        }

        // if empty, add a default charge of 1
        if (result.isEmpty()) {
            result.add(1);
        }

        return result.stream()
                .mapToInt(a -> a)
                .toArray();
    }

    @Override
    public void close() {

        waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        reader.close();

    }
}
