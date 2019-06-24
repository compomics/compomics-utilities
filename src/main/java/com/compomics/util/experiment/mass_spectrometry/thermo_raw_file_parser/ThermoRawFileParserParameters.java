package com.compomics.util.experiment.mass_spectrometry.thermo_raw_file_parser;

/**
 * The parameters to use when running ThermoRawFileParser.
 *
 * @author Harald Barsnes
 */
public class ThermoRawFileParserParameters {

    /**
     * The format to convert to.
     */
    private ThermoRawFileParserOutputFormat outputFormat = ThermoRawFileParserOutputFormat.mgf;
    /**
     * If peak picking is to be run or not.
     */
    private boolean peackPicking = true;

    /**
     * Constructor.
     */
    public ThermoRawFileParserParameters() {

    }

    /**
     * Returns the format to convert to.
     *
     * @return the format to convert to
     */
    public ThermoRawFileParserOutputFormat getOutputFormat() {
        return outputFormat;
    }

    /**
     * Sets the format to convert to.
     *
     * @param outputFormat the format to convert to
     */
    public void setMsFormat(ThermoRawFileParserOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Returns true if peak picking is to be performed.
     * 
     * @return the peackPicking true if peak picking is to be performed
     */
    public boolean isPeackPicking() {
        return peackPicking;
    }

    /**
     * Set if peak picking is to be performed.
     * 
     * @param peackPicking the peackPicking to set
     */
    public void setPeackPicking(boolean peackPicking) {
        this.peackPicking = peackPicking;
    }
}
