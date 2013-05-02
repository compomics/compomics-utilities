package com.compomics.util.experiment.filters.massspectrometry.spectrumfilters;

import com.compomics.util.experiment.filters.massspectrometry.SpectrumFilter;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.util.ArrayList;

/**
 * filter consisting of several filters. The validation will be an 'and' of all
 * individual validations.
 *
 * @author Marc Vaudel
 */
public class And extends SpectrumFilter {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -1518927119775419822L;
    /**
     * List of filters on which we will do an 'and'.
     */
    private ArrayList<SpectrumFilter> filters = new ArrayList<SpectrumFilter>();

    /**
     * Constructor.
     */
    public And() {
    }

    /**
     * Adds a filter to the and Filters will be tested iteratively so put the
     * fast/discriminative first.
     *
     * @param spectrumFilter
     */
    public void addFilter(SpectrumFilter spectrumFilter) {
        filters.add(spectrumFilter);
    }

    /**
     * Returns a boolean indicating whether all implemented filters validated
     * the spectrum.
     *
     * @param spectrum the spectrum
     * @return a boolean indicating whether all implemented filters validated
     * the spectrum
     */
    public boolean validateSpectrum(MSnSpectrum spectrum) {
        for (SpectrumFilter filter : filters) {
            if (!filter.validateSpectrum(spectrum)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getDescription() {
        String result = "";
        boolean first = true;
        for (SpectrumFilter filter : filters) {
            if (first) {
                first = false;
            } else {
                result += " and ";
            }
            String subDescription = filter.getDescription();
            if (subDescription.contains(" or ")) {
                result += "(";
            }
            result += subDescription;
            if (subDescription.contains(" or ")) {
                result += ")";
            }
        }
        return result;
    }
}
