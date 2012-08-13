package com.compomics.util.experiment.filters.massspectrometry.spectrumfilters;

import com.compomics.util.experiment.filters.massspectrometry.SpectrumFilter;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This filter looks for a specific m/z comb in the spectrum.
 *
 * @author Marc Vaudel
 */
public class CombFilter extends SpectrumFilter {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2943615250048987546L;
    /**
     * The actual filter, an and of m/z filters.
     */
    private And filter;

    /**
     * Constructor.
     *
     * @param mzComb the m/z comb to look for
     * @param mzTolerance the m/z tolerance
     * @param isPpm a boolean indicating whether the m/z tolerance is in ppm
     * @param intensityQuartile the intensity quantile to look into
     */
    public CombFilter(ArrayList<Double> mzComb, double mzTolerance, boolean isPpm, double intensityQuartile) {
        filter = new And();
        MzFilter referenceFilter = null;
        for (double mz : mzComb) {
            if (referenceFilter == null) {
                referenceFilter = new MzFilter(mz, mzTolerance, isPpm, intensityQuartile);
                filter.addFilter(referenceFilter);
            } else {
                filter.addFilter(new MzFilter(referenceFilter, mz));
            }
        }
    }

    /**
     * Returns a boolean indicating whether the filter m/z comb was found in the
     * spectrum.
     *
     * @param spectrum the spectrum to inspect
     * @return a boolean indicating whether the filter m/z comb was found in the
     * spectrum
     */
    public boolean validateSpectrum(MSnSpectrum spectrum) {
        return filter.validateSpectrum(spectrum);
    }

    @Override
    public String getDescription() {
        return filter.getDescription();
    }
}
