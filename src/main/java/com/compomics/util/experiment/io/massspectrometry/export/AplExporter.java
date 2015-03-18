package com.compomics.util.experiment.io.massspectrometry.export;

import com.compomics.util.experiment.io.massspectrometry.MgfFileIterator;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.FragmentationMethod;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * This converter writes spectrum files in APL format.
 *
 * @author Marc Vaudel
 */
public class AplExporter {

    /**
     * Writes the content of the given file name into the destination file in
     * the APL format. The spectra must be loaded in the factory.
     *
     * @param mgfFile the original mgf file
     * @param destinationFile the destination file where to write the spectra
     * @param fragmentationMethod the fragmentation method used //@TODO: this
     * should be spectrum dependent
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     */
    public static void mgfToApl(File mgfFile, File destinationFile, FragmentationMethod fragmentationMethod) throws IOException {

        FileWriter fileWriter = new FileWriter(destinationFile);

        try {
            BufferedWriter bw = new BufferedWriter(fileWriter);

            try {
                MgfFileIterator mgfFileIterator = new MgfFileIterator(mgfFile);
                while (mgfFileIterator.hasNext()) {
                    MSnSpectrum spectrum = mgfFileIterator.next();
                    writeSpectrum(bw, spectrum, fragmentationMethod);
                }
            } finally {
                bw.close();
            }
        } finally {
            fileWriter.close();
        }
    }

    /**
     * Writes the given spectrum in ms2 format.
     *
     * @param bw a buffered writer where to write the spectrum
     * @param spectrum the spectrum of interest
     * @param fragmentationMethod the fragmentation method used
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     */
    public static void writeSpectrum(BufferedWriter bw, MSnSpectrum spectrum, FragmentationMethod fragmentationMethod) throws IOException {

        bw.write("peaklist start");
        bw.newLine();

        Precursor precursor = spectrum.getPrecursor();
        bw.write("mz=" + precursor.getMz());
        bw.newLine();
        bw.write("fragmentation=" + fragmentationMethod.name);
        bw.newLine();
//        StringBuilder chargeLine = new StringBuilder();
//        for (Charge charge : precursor.getPossibleCharges()) {
//            if (chargeLine.length() == 0) {
//                chargeLine.append("charge=");
//            } else {
//                chargeLine.append(", ");
//            }
//            chargeLine.append(charge.value);
//        }
//        if (chargeLine.length() > 0) {
//            bw.write(chargeLine.toString());
//            bw.newLine();
//        }
        bw.write("charge=2");
        bw.newLine();

        HashMap<Double, Peak> peakMap = spectrum.getPeakMap();
        for (double mz : spectrum.getOrderedMzValues()) {
            Peak peak = peakMap.get(mz);
            bw.write(peak.mz + "\t" + peak.intensity);
            bw.newLine();
        }
        bw.write("peaklist end");
        bw.newLine();
        bw.newLine();
    }

}
