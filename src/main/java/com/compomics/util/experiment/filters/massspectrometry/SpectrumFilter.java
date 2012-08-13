package com.compomics.util.experiment.filters.massspectrometry;

import com.compomics.util.experiment.filters.massspectrometry.spectrumfilters.filtercreation.*;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.io.Serializable;
import javax.swing.JFrame;

/**
 * This class represent all spectrum filters which will be used to filter
 * spectra.
 *
 * @author Marc Vaudel
 */
public abstract class SpectrumFilter implements Serializable {

    /**
     * The name of the filter.
     */
    protected String name;

    /**
     * Returns the name of the filter.
     *
     * @return the name of the filter
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the filter.
     *
     * @param name the name of the filter
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Indicates whether a spectrum passed the filter.
     *
     * @param spectrum the spectrum to inspect
     * @return a boolean indicating whether a spectrum passed the filter
     */
    public abstract boolean validateSpectrum(MSnSpectrum spectrum);

    /**
     * Returns a description of the validated spectra, typically "containing a
     * peak at m/z 114".
     *
     * @return a description of the validated spectra
     */
    public abstract String getDescription();

    /**
     * Convenience method returning the types of implemented elementary filters.
     *
     * @return the types of implemented elementary filters
     */
    public static String[] getElementaryFilters() {
        String[] result = new String[4];
        result[0] = "mz";
        result[1] = "peak";
        result[2] = "comb";
        result[3] = "fingerprint";
        return result;
    }

    /**
     * Allows the user to design basic filters via a GUI.
     *
     * @param parentFrame the parent frame
     * @param mzTolerance the mz tolerance, can be null
     * @param intensityQuantile the intensity quantile, can be null
     * @param intensityTolerance the intensity tolerance, can be null
     * @param isPpm a boolean indicating whether the tolerance is in ppm, can be
     * null
     * @return the filter as designed by the user. Null if none.
     */
    public static SpectrumFilter getFilter(JFrame parentFrame, Double mzTolerance, Double intensityQuantile, Double intensityTolerance, Boolean isPpm) {
        TypeSelection typeSelection = new TypeSelection(parentFrame);
        int selectedType = typeSelection.getSelectedType();
        switch (selectedType) {
            case 0:
                MzDialog mzDialog = new MzDialog(parentFrame, mzTolerance, intensityQuantile, isPpm);
                return mzDialog.getFilter();
            case 1:
                PeakDialog peakDialog = new PeakDialog(parentFrame, mzTolerance, intensityTolerance, isPpm);
                return peakDialog.getFilter();
            case 2:
                CombDialog combDialog = new CombDialog(parentFrame, mzTolerance, intensityQuantile, isPpm);
                return combDialog.getFilter();
            case 3:
                FingerprintDialog fingerprintDialog = new FingerprintDialog(parentFrame, mzTolerance, intensityQuantile, isPpm);
                return fingerprintDialog.getFilter();
            default:
                return null;
        }
    }
}
