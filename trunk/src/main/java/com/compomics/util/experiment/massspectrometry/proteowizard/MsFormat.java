package com.compomics.util.experiment.massspectrometry.proteowizard;

import java.util.Vector;

/**
 * The mass spectrometry formats supported by ProteoWizard.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum MsFormat {

    /**
     * Mascot generic format.
     */
    mgf(0, "mgf", "mgf", "Mascot generic format", ".mgf", false, true),
    /**
     * mzML generic PSI format.
     */
    mzML(1, "mzML", "mzML", "mzML generic PSI format", ".mzml", true, true),
    /**
     * mzXML format.
     */
    mzXML(2, "mzXML", "mzXML", "mzXML format", ".mzxml", true, true),
    /**
     * Thermo/Waters raw format.
     */
    raw(3, "raw", null, "Thermo/Waters raw format", ".raw", true, false), // @TODO: also add: .bms2, .d, .fid, .yep, .baf..?
    /**
     * Applied Biosystems wiff format.
     */
    wiff(4, "wiff", null, "Applied Biosystems wiff format", ".wiff", true, false),
    /**
     * Implementation of the PSI mzML ontology that is based on HDF5.
     */
    mz5(5, "mz5", "mz5", "mzML based on HDF5", ".mz5", true, true),
    /**
     * ms1 format.
     */
    ms1(6, "ms1", "ms1", "ms1 format", ".ms1", false, true),
    /**
     * ms2 format.
     */
    ms2(7, "ms2", "ms2", "ms2 format", ".ms2", false, true),
    /**
     * cms1 format.
     */
    cms1(8, "cms1", "cms1", "cms1 format", ".cms1", false, true),
    /**
     * cms2 format.
     */
    cms2(9, "cms2", "cms2", "cms2 format", ".cms2", false, true);

    /**
     * The index of the format.
     */
    public final int index;
    /**
     * The command line option name.
     */
    public final String commandLineOption;
    /**
     * The name of the format.
     */
    public final String name;
    /**
     * A brief description of the format.
     */
    public final String description;
    /**
     * Boolean indicating if this in a format for raw data, false means that it
     * is a peak list format.
     */
    public final boolean rawFormat;
    /**
     * The file name ending of the format.
     */
    public final String fileNameEnding;
    /**
     * Boolean indicating if this in a format that can be used as output.
     */
    public final boolean outputFormat;

    /**
     * Constructor.
     *
     * @param index the index of the format
     * @param commandLineOption the command line option name
     * @param name the name of the format
     * @param description the brief description of the format
     * @param fileNameEnding the file name ending of the format
     * @param rawFormat is this a format for raw data
     * @param outputFormat it this s format that can be used as output
     */
    private MsFormat(int index, String commandLineOption, String name, String description, String fileNameEnding, boolean rawFormat, boolean outputFormat) {
        this.index = index;
        this.commandLineOption = commandLineOption;
        this.name = name;
        this.description = description;
        this.fileNameEnding = fileNameEnding;
        this.rawFormat = rawFormat;
        this.outputFormat = outputFormat;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a list of formats.
     *
     * @param raw get raw formats, null returns both
     * @param outputFormat get output formats, null return both
     * @return the list formats
     */
    public static Vector<MsFormat> getDataFormats(Boolean raw, Boolean outputFormat) {

        Vector<MsFormat> rawFormats = new Vector<MsFormat>();

        for (MsFormat format : MsFormat.values()) {
            
            boolean rawFilter = true;
            boolean outputFilter = true;
            
            if (raw != null) {
                rawFilter = format.rawFormat == raw;
            }
            
            if (outputFormat != null) {
                outputFilter = format.outputFormat == outputFormat;
            }
            
            if (rawFilter && outputFilter) {
                rawFormats.add(format);
            }
        }

        return rawFormats;
    }
}
