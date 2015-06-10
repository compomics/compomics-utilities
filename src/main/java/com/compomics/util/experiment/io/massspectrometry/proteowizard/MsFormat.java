package com.compomics.util.experiment.io.massspectrometry.proteowizard;

/**
 * the mass spectrometry formats supported by ProteoWizard.
 *
 * @author Marc Vaudel
 */
public enum MsFormat {

    mzML(0, "mzML", "mzML", "mzML generic PSI format."), 
    mgf(1, "mgf", "mgf", "Mascot generic format."), 
    mz5(2, "mz5", "mz5", "Implementation of the PSI mzML ontology that is based on HDF5."), 
    mzXML(3, "mzXML", "mzXML", "mzXML format."), 
    text(4, "text", "text", "Proteowizard internal text format."), 
    ms1(5, "ms1", "ms1", "ms1 format."), 
    cms1(6, "cms1", "cms1", "cms2 format."), 
    ms2(7, "ms2", "ms2", "ms2 format."), 
    cms2(8, "cms2", "cms2", "cms2 format.");
    
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
     * Constructor.
     * 
     * @param index index of the format
     * @param commandLineOption command line option name
     * @param name name of the format
     * @param description brief description of the format
     */
    private MsFormat(int index, String commandLineOption, String name, String description) {
        this.index = index;
        this.commandLineOption = commandLineOption;
        this.name = name;
        this.description = description;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
