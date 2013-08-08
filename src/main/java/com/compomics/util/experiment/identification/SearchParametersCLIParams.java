package com.compomics.util.experiment.identification;

/**
 * Enum class specifying the SearchParameter command line option parameters for 
 * SearchCLI and DeNovoCLI.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum SearchParametersCLIParams {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPORTANT: Any change here must be reported in the wikis: 
    // http://code.google.com/p/denovogui/wiki/DeNovoCLI and
    // http://code.google.com/p/searchgui/wiki/SearchCLI.
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //////////////////////////////////
    // General options
    //////////////////////////////////
    SPECTRUM_FILES("spectrum_files", "Spectrum files (mgf format), comma separated list or an entire folder.", true),
    OUTPUT_FOLDER("output_folder", "The output folder.", true),
    SEARCH_PARAMETERS("search_params", "Serialized com.compomics.util.experiment.identification.SearchParameters object created by SearchGUI/DeNovoGUI.", false),
    PPM("ppm", "Precursor ion tolerance unit: ppm (1) or Da (2). Default is '1'.", false),
    PREC_TOL("prec_tol", "Precursor ion mass tolerance, default is '10' (ppm).", false),
    FRAG_TOL("frag_tol", "Fragment ion mass tolerance in Da, default is '0.5' (Da).", false),
    ENZYME("enzyme", "Enzyme, default is 'Trypsin'. The available enzymes are listed in the GUI. (Names are case sensitive.)", false),
    FIXED_MODS("fixed_mods", "The fixed modifications as a comma separated list. (Modifications are configured in the GUI.)", false),
    VARIABLE_MODS("variable_mods", "The variable modifications as a comma separated list. (Modifications are configured in the GUI.)", false),
    THREADS("threads", "The number of threads to use for the processing. Default is the number of cores available.", false),
    
    //////////////////////////////////
    // SearchGUI specific options
    //////////////////////////////////
    MC("mc", "The number of allowed missed cleavages, default is '2'.", false),
    MIN_CHARGE("min_charge", "The minimal charge to search for, default is '2'.", false),
    MAX_CHARGE("max_charge", "The maximal charge to search for, default is '4'.", false),
    MAX_EVALUE("max_evalue", "The maximal evalue considered, default is '100'.", false),
    FI("fi", "The type of forward ion searched. Default is 'b'.", false),
    RI("ri", "The type of rewind ion searched. Default is 'y'.", false),
    DB("db", "The sequence database in FASTA format.", false),
    HITLIST_LENGTH("hitlist_length", "The hitlist length (OMSSA only), default is '25'.", false),
    MIN_PEP_LENGTH("min_pep_length", "The minumum peptide length (OMSSA semi-tryptic or no enzyme searches only).", false),
    MAX_PEP_LENGTH("max_pep_length", "The maximum peptide length (OMSSA semi-tryptic or no enzyme searches only).", false),
    REMOVE_PREC("remove_prec", "Remove the precursor (OMSSA only), 1: true, 0: false, default is '1'.", false),
    SCALE_PREC("scale_prec", "Scale the precursor mass (OMSSA only), 1: true, 0: false, default is '0'.", false),
    ESTIMATE_CHARGE("estimate_charge", "Estimate the precursor charge (OMSSA only), 1: true, 0: false, default is '1'.", false), // same as ESTIMATE_CHARGE_DE_NOVO, but different description 
    OMSSA("omssa", "Turn the OMSSA search on or off (1: on, 0: off, default is '1').", false),
    XTANDEM("xtandem", "Turn the X!Tandem search on or off (1: on, 0: off, default is '1').", false),
    OMSSA_FORMAT("omssa_format", "The OMSSA output format: omx or csv, default is 'omx'.", false),
    OMSSA_LOCATION("omssa_folder", "The folder where OMSSA is installed, defaults to the provided versions for the given OS.", false),
    XTANDEM_LOCATION("xtandem_folder", "The folder where X!Tandem is installed, defaults to the provided versions for the given OS.", false),
    MGF_SPLITTING_LIMIT("mgf_splitting", "The maximum mgf file size in MB before splitting the mgf. Default is '1000'.", false),
    MGF_MAX_SPECTRA("mgf_spectrum_count", "The maximum number of spectra per mgf file when splitting. Default is '25000'.", false),
    FIX_DUPLICATE_TITLES("fix_titles", "Correct for duplicate spectrum titles. (1: on, 0: off, default is '0').", false),
    
    //////////////////////////////////
    // DeNovoGUI specific options
    //////////////////////////////////
    HITLIST_LENGTH_DE_NOVO("de_novo_count", "The number of de novo solutions [0-2000], default is '10'.", false),
    ESTIMATE_CHARGE_DE_NOVO("estimate_charge", "Estimate the precursor charge, 1: true, 0: false, default is '1'.", false), // same as ESTIMATE_CHARGE, but different description
    CORRECT_PRECURSOR_MASS("correct_precursor", "Correct the precursor mass, 1: true, 0: false, default is '1'.", false),
    DISCARD_SPECTRA("discard_spectra", "Discard low quality spectra, 1: true, 0: false, default is '1'.", false),
    FRAGMENTATION_MODEL("fragmentation_model", "The PepNovo+ fragmentation model. Default is 'CID_IT_TRYP'.", false),
    GENERATE_BLAST("generate_blast", "Generate a BLAST query, 1: true, 0: false, default is '0'.", false),
    PEP_NOVO_LOCATION("pep_novo", "The PepNovo+ executable, defaults to the OS dependent versions included with DeNovoGUI.", false);
    
    /**
     * Short Id for the CLI parameter.
     */
    public String id;
    /**
     * Explanation for the CLI parameter.
     */
    public String description;
    /**
     * Boolean indicating whether the parameter is mandatory.
     */
    public boolean mandatory;

    /**
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param id the id
     * @param description the description
     * @param mandatory is the parameter mandatory
     */
    private SearchParametersCLIParams(String id, String description, boolean mandatory) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
    }
}
