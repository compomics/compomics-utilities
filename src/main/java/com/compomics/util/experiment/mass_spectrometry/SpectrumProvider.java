package com.compomics.util.experiment.mass_spectrometry;

import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.util.HashMap;

/**
 * Interface for objects providing spectra.
 *
 * @author Marc Vaudel
 */
public interface SpectrumProvider extends AutoCloseable {
    
    /**
     * Returns the spectrum with the given title in the given file.
     * 
     * @param fileName The name of the spectrum file.
     * @param spectrumTitle The title of the spectrum.
     * 
     * @return The spectrum with the given title in the given file.
     */
    public Spectrum getSpectrum(
            String fileName,
            String spectrumTitle
    );
    
    /**
     * Returns the precursor. Null if none.
     * 
     * @param fileName The name of the spectrum file.
     * @param spectrumTitle The title of the spectrum.
     * 
     * @return The precursor.
     */
    public Precursor getPrecursor(
            String fileName,
            String spectrumTitle
    );
    
    /**
     * Returns the measured precursor m/z. NaN if none.
     * 
     * @param fileName The name of the spectrum file.
     * @param spectrumTitle The title of the spectrum.
     * 
     * @return The measured precursor m/z.
     */
    public double getPrecursorMz(
            String fileName,
            String spectrumTitle
    );
    
    /**
     * Returns the precursor RT window. NaN if none.
     * 
     * @param fileName The name of the spectrum file.
     * @param spectrumTitle The title of the spectrum.
     * 
     * @return The precursor RT.
     */
    public double getPrecursorRt(
            String fileName,
            String spectrumTitle
    );
    
    
    /**
     * Returns the spectrum peaks.
     * 
     * @param fileName The name of the spectrum file.
     * @param spectrumTitle The title of the spectrum.
     * 
     * @return The peaks.
     */
    public double[][] getPeaks(
            String fileName,
            String spectrumTitle
    );
    
    /**
     * Returns the spectrum peaks above the intensity threshold given the given parameters.
     * 
     * @param fileName The name of the spectrum file.
     * @param spectrumTitle The title of the spectrum.
     * @param intensityThresholdType The type of intensity threshold.
     * @param thresholdValue The threshold value.
     * 
     * @return The spectrum peaks above the intensity threshold given the given parameters.
     */
    public double[][] getPeaksAboveIntensityThreshold(
            String fileName,
            String spectrumTitle,
            AnnotationParameters.IntensityThresholdType intensityThresholdType,
            double thresholdValue
    );
    
    
    /**
     * Returns the minimum precursor m/z in a given file.
     * 
     * @param fileName The name of the spectrum file.
     * 
     * @return The minimum precursor m/z in a given file.
     */
    public double getMinPrecMz(
            String fileName
    );
    
    
    /**
     * Returns the maximum precursor m/z in a given file.
     * 
     * @param fileName The name of the spectrum file.
     * 
     * @return The maximum precursor m/z in a given file.
     */
    public double getMaxPrecMz(
            String fileName
    );
    
    
    /**
     * Returns the maximum precursor RT in a given file.
     * 
     * @param fileName The name of the spectrum file.
     * 
     * @return The maximum precursor RT in a given file.
     */
    public double getMaxPrecRT(
            String fileName
    );
    
    
    /**
     * Returns the minimum precursor m/z among all files.
     * 
     * @return The minimum precursor m/z among all files.
     */
    public double getMinPrecMz();
    
    
    /**
     * Returns the maximum precursor m/z among all files.
     * 
     * @return The maximum precursor m/z among all files.
     */
    public double getMaxPrecMz();
    
    
    /**
     * Returns the maximum precursor RT among all files.
     * 
     * @return The maximum precursor RT among all files.
     */
    public double getMaxPrecRT();
    
    /**
     * Returns the spectrum file names.
     * 
     * @return The spectrum file names.
     */
    public String[] getFileNames();
    
    /**
     * Returns the absolute path to the original mass spec file containing the spectra in a map indexed by file name.
     * 
     * @return The absolute path to the original mass spec file containing the spectra in a map indexed by file name.
     */
    public HashMap<String, String> getFilePaths();
    
    /**
     * Returns the absolute path to the cms file indexed by ms file name. Null if none.
     * 
     * @return The absolute path to the cms file indexed by ms file name.
     */
    public HashMap<String, String> getCmsFilePaths();

    @Override
    public void close();
    
}
