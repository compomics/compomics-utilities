package com.compomics.util.experiment.mass_spectrometry.proteowizard;

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
     * ms1 format.
     */
    ms1(1, "ms1", "ms1", "ms1 format", ".ms1", false, true),
    /**
     * ms2 format.
     */
    ms2(2, "ms2", "ms2", "ms2 format", ".ms2", false, true),
    /**
     * cms1 format.
     */
    cms1(3, "cms1", "cms1", "cms1 format", ".cms1", false, true),
    /**
     * cms2 format.
     */
    cms2(4, "cms2", "cms2", "cms2 format", ".cms2", false, true),
    /**
     * Binary bms2 format.
     */
    bms2(5, "bms2", "bms2", "bms2 format", ".bms2", false, true),
    /**
     * mzML generic PSI format.
     */
    mzML(6, "mzML", "mzML", "mzML generic PSI format", ".mzml", true, true),
    /**
     * mzXML format.
     */
    mzXML(7, "mzXML", "mzXML", "mzXML format", ".mzxml", true, true),
    /**
     * Implementation of the PSI mzML ontology that is based on HDF5.
     */
    mz5(8, "mz5", "mz5", "mzML based on HDF5", ".mz5", true, true),
    /**
     * Thermo/Waters raw format.
     */
    raw(9, "raw", null, "Thermo/Waters raw format", ".raw", true, false),
    /**
     * Agilent/Bruker d format.
     */
    d(10, "d", null, "Agilent/Buker d format", ".d", true, false),
    /**
     * Bruker FID format.
     */
    fid(11, "fid", null, "Bruker FID format", ".fid", true, false),
    /**
     * Bruker FID format.
     */
    yep(12, "yep", null, "Bruker YEP format", ".yep", true, false),
    /**
     * Bruker FID format.
     */
    baf(13, "baf", null, "Bruker BAF format", ".baf", true, false),
    /**
     * Applied Biosystems wiff format.
     */
    wiff(14, "wiff", null, "Applied Biosystems wiff format", ".wiff", true, false);

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

        Vector<MsFormat> rawFormats = new Vector<>();

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

    /**
     * Empty default constructor
     */
    private MsFormat() {
        index = 0;
        commandLineOption = "";
        name = "";
        description = "";
        rawFormat = false;
        fileNameEnding = "";
        outputFormat = false;
    }
}
