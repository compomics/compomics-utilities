package com.compomics.util.experiment.io.mass_spectrometry.export;

import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.mass_spectrometry.Charge;
import com.compomics.util.experiment.mass_spectrometry.FragmentationMethod;
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.mass_spectrometry.SpectrumFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

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
     * @param minCharge the minimal charge to look for in case no charge is
     * present in the file
     * @param maxCharge the maximal charge to look for in case no charge is
     * present in the file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     * @throws uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException exception thrown
     * whenever an error occurred while reading an mzML file
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public static void mgfToApl(File mgfFile, File destinationFile, FragmentationMethod fragmentationMethod, int minCharge, int maxCharge) throws IOException, MzMLUnmarshallerException, InterruptedException {

        String fileName = mgfFile.getName();
        SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
        HashMap<Double, HashMap<String, Integer>> precursorMassToTitleMap = new HashMap<>(spectrumFactory.getNSpectra(fileName));
        for (String title : spectrumFactory.getSpectrumTitles(fileName)) {
            Precursor precursor = spectrumFactory.getPrecursor(Spectrum.getSpectrumKey(fileName, title));
            Double mz = precursor.getMz();
            if (!precursor.getPossibleCharges().isEmpty()) {
                for (Charge possibleCharge : precursor.getPossibleCharges()) {
                    int charge = possibleCharge.value;
                    double mass = mz * charge - charge * ElementaryIon.proton.getTheoreticMass();
                    HashMap<String, Integer> titlesAtMass = precursorMassToTitleMap.get(mass);
                    if (titlesAtMass == null) {
                        titlesAtMass = new HashMap<>(1);
                        precursorMassToTitleMap.put(mass, titlesAtMass);
                    }
                    titlesAtMass.put(title, charge);
                }
            } else {
                for (int charge = minCharge; charge <= maxCharge; charge++) {
                    double mass = mz * charge - charge * ElementaryIon.proton.getTheoreticMass();
                    HashMap<String, Integer> titlesAtMass = precursorMassToTitleMap.get(mass);
                    if (titlesAtMass == null) {
                        titlesAtMass = new HashMap<>(1);
                        precursorMassToTitleMap.put(mass, titlesAtMass);
                    }
                    titlesAtMass.put(title, charge);
                }
            }
        }
        ArrayList<Double> masses = new ArrayList<>(precursorMassToTitleMap.keySet());
        Collections.sort(masses);

        try (FileWriter fileWriter = new FileWriter(destinationFile); 
                BufferedWriter bw = new BufferedWriter(fileWriter)) {
            
            for (Double mass : masses) {
                HashMap<String, Integer> titles = precursorMassToTitleMap.get(mass);
                for (String title : titles.keySet()) {
                    Spectrum spectrum = (Spectrum) spectrumFactory.getSpectrum(fileName, title);
                    writeSpectrum(bw, spectrum, fragmentationMethod, titles.get(title));
                }
            }
        }
    }

    /**
     * Writes the given spectrum in ms2 format.
     *
     * @param bw a buffered writer where to write the spectrum
     * @param spectrum the spectrum of interest
     * @param fragmentationMethod the fragmentation method used
     * @param charge the charge to consider for this spectrum
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public static void writeSpectrum(BufferedWriter bw, Spectrum spectrum, FragmentationMethod fragmentationMethod, int charge) throws IOException, InterruptedException {

        bw.write("peaklist start");
        bw.newLine();

        Precursor precursor = spectrum.getPrecursor();
        bw.write("mz=" + precursor.getMz());
        bw.newLine();
        bw.write("fragmentation=" + fragmentationMethod.name);
        bw.newLine();
        bw.write("charge=" + charge);
        bw.newLine();
        bw.write("header=" + spectrum.getSpectrumTitle());
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
