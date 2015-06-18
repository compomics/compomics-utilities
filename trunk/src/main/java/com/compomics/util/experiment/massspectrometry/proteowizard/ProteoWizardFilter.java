package com.compomics.util.experiment.massspectrometry.proteowizard;

/**
 * Filters which can be applied to msconvert.
 *
 * @author Marc Vaudel
 */
public enum ProteoWizardFilter {

    peakPicking(0, "peakPicking", "This filter performs centroiding on spectrawith the selected MS levels, expressed as an int_set. The value for peak picker type must be \"cwt\" or \"vendor\": when <PickerType> = \"vendor\", vendor (Windows DLL) code is used if available. IMPORTANT NOTE: since this filter operates on the raw data through the vendor DLLs, it must be the first fileter in any list of filters when \"vendor\" is used. The other option for PickerType is \"cwt\", which uses ProteoWizard's wavelet-based algorithm for performing peak-picking with the specified wavelet-space signal-to-noise ratio."),
    index(1, "index", "Selects spectra by index - an index value 0-based numerical order in which the spectrum appears in the input."),
    msLevel(2, "msLevel", "This filter selects only spectra with the indicated MS levels, expressed as an int_set."),
    chargeState(3, "chargeState ", "This filter keeps spectra that match the listed charge state(s), expressed as an int_set. Both known/single and possible/multiple charge states are tested. Use 0 to include spectra with no charge state at all."),
    precursorRecalculation(4, "precursorRecalculation ", "This filter recalculates the precursor m/z and charge for MS2 spectra. It looks at the prior MS1 scan to better infer the parent mass. However, it only works on orbitrap and FT data,although it does not use any 3rd party (vendor DLL) code. Since the time the code was written, Thermo has since fixed up its own estimation in response, so it's less critical than it used to be (though can still be useful)."),
    precursorRefine(5, "precursorRefine", "This filter recalculates the precursor m/z and charge for MS2 spectra. It looks at the prior MS1 scan to better infer the parent mass. It only works on orbitrap, FT, and TOF data. It does not use any 3rd party (vendor DLL) code."),
    scanNumber(6, "scanNumber", "This filter selects spectra by scan number. Depending on the input data type, scan number and spectrum index are not always the same thing - scan numbers are not always contiguous, and are usually 1-based."),
    scanEvent(7, "scanEvent", "This filter selects spectra by scan event. For example, to include all scan events except scan event 5, use filter \"scanEvent 1-4 6-\". A \"scan event\" is a preset scan configuration: a user-defined scan configuration that specifies the instrumental settings in which a spectrum is acquired. An instrument may cycle through a list of preset scan configurations to acquire data. This is a more generic term for the Thermo \"scan event\", which is defined in the Thermo Xcalibur glossary as: \"a mass spectrometer scan that is defined by choosing the necessary scan parameter settings. Multiple scan events can be defined for each segment of time.\"."),
    scanTime(8, "scanTime", "This filter selects only spectra within a given time range (in seconds)."),
    scanSumming(9, "scanSumming", "This filter sums MS2 sub-scans whose precursors are within precursor tolerance (default: 0.05 m/z) and scan time tolerance (default: 10 secs.). It is intended for some Waters DDA data, where sub-scans should be summed together to increase the SNR. This filter has only been tested for Waters data."),
    sortByScanType(10, "sortByScanType", "This filter reorders spectra, sorting them by ascending scan start time."),
    stripIT(11, "stripIT", "This filter rejects ion trap data spectra with MS level 1."),
    metaDataFixer(12, "metaDataFixer", "This filter is used to add or replace a spectra's TIC/BPI metadata, usually after peakPicking where the change from profile to centroided data may make the TIC and BPI values inconsistent with the revised scan data. The filter traverses the m/z intensity arrays to find the sum and max. For example, in msconvert it can be used as: --filter \"peakPicking true 1-\" --filter metadataFixer. It can also be used without peak picking and is provided without guarantee on the results correctness."),
    titleMaker(13, "titleMaker", "This filter adds or replaces spectrum titles according to specified arguments. It can be used, for example, to customize the TITLE line in MGF output in msconvert."),
    threshold(14, "threshold", "This filter keeps data whose values meet various threshold criteria."),
    mzWindow(15, "mzWindow", "keeps mz/intensity pairs whose m/z values fall within the specified range."),
    mzPrecursors(16, "mzPrecursors", "Retains spectra with precursor m/z values found in the given list. For example, in msconvert to retain only spectra with precursor m/z values of 123.4 and 567.8 you would use \"[123.4,567.8]\". Note that this filter will drop MS1 scans unless you include 0.0 in the list of precursor values."),
    defaultArrayLength(17, "defaultArrayLength", "Keeps only spectra with peak counts within <peak_count_range>, expressed as an int_set. (In mzML the peak list length is expressed as \"defaultArrayLength\", hence the name.) For example, to include only spectra with 100 or more peaks, you would use \"defaultArrayLength 100-\" ."),
    zeroSamples(18, "zeroSamples", "This filter deals with zero values in spectra - either removing them, or adding them where they are missing."),
    mzPresent(19, "mzPresent", "This filter is similar to the \"threshold\" filter, with a few more options."),
    MS2Denoise(20, "MS2Denoise", "Noise peak removal for spectra with precursor ions."),
    MS2Deisotope(21, "MS2Deisotope", "Deisotopes ms2 spectra using the Markey method or a Poisson model. For the Markey method, hi_res sets high resolution mode to \"false\" (the default) or \"true\". Poisson activates a Poisson model based on the relative intensity distribution."),
    turbocharger(22, "turbocharger", "Predicts MSn spectrum precursor charge based on the isotopic distribution associated with the survey scan(s) of the selected precursor."),
    ETDFilter(23, "ETDFilter", "Filters ETD MSn spectrum data points, removing unreacted precursors, charge-reduced precursors, and neutral losses."),
    chargeStatePredictor(24, "chargeStatePredictor", "Predicts MSn spectrum precursors to be singly or multiply charged depending on the ratio of intensity above and below the precursor m/z, or optionally using the \"makeMS2\" algorithm."),
    activation(25, "activation", "Keeps only spectra whose precursors have the specifed activation type. It does not affect non-MS spectra, and does not affect MS1 spectra. Use it to create output files containing only ETD or CID/HCD MSn data where both activation modes have been interleaved within a given input vendor data file (eg: Thermo's Decision Tree acquisition mode)."),
    analyzer(26, "analyzer", "This filter keeps only spectra with the indicated mass analyzer type."),
    polarity(27, "polarity", "Keeps only spectra with scan of the selected polarity.");

    /**
     * The index of the filter.
     */
    public final int number;
    /**
     * The name of the filter.
     */
    public final String name;
    /**
     * A brief description of the filter.
     */
    public final String description;

    /**
     * Constructor.
     *
     * @param index index of the filter
     * @param commandLineOption command line option name
     * @param name name of the filter
     * @param description brief description of the filter
     */
    private ProteoWizardFilter(int number, String name, String description) {
        this.number = number;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the filter designed by the given number. Null if not found.
     *
     * @param number the filter number
     *
     * @return the filter designed by the given number
     */
    public static ProteoWizardFilter getFilter(Integer number) {
        for (ProteoWizardFilter proteoWizardFilter : values()) {
            if (proteoWizardFilter.number == number) {
                return proteoWizardFilter;
            }
        }
        return null;
    }

    /**
     * Returns the filter designed by the given name. Null if not found.
     *
     * @param name the name of the filter
     *
     * @return the filter designed by the given number
     */
    public static ProteoWizardFilter getFilter(String name) {
        for (ProteoWizardFilter proteoWizardFilter : values()) {
            if (proteoWizardFilter.name.equals(name)) {
                return proteoWizardFilter;
            }
        }
        return null;
    }
}
