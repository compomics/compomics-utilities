package com.compomics.util.experiment.identification.search_parameters_cli;

/**
 * Enum class specifying the SearchParameter command line option parameters to
 * create a SearchParameters object
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum IdentificationParametersCLIParams {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPORTANT: Any change here must be reported in the wiki: 
    // http://code.google.com/p/compomics-utilities/wiki/IdentificationParametersCLI
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////
    // General parameters
    //////////////////////////////////
    OUTPUT("out", "The destination Identification Parameters file (.parameters).", true),
    DB("db", "The sequence database in FASTA format.", true),
    PREC_PPM("prec_ppm", "Precursor ion tolerance unit: ppm (1) or Da (2), default is '1'.", false),
    FRAG_PPM("frag_ppm", "Fragment ion tolerance unit: ppm (1) or Da (2), default is '1'.", false),
    PREC_TOL("prec_tol", "Precursor ion mass tolerance, default is '10' ppm.", false),
    PREC_TOL_DA("prec_tol", "Precursor ion mass tolerance in Dalton, default is 0.5 Da.", false), // For tools which do not have the ppm option
    FRAG_TOL("frag_tol", "Fragment ion mass tolerance in Dalton, default is '0.5' Da.", false),
    ENZYME("enzyme", "Enzyme, default is 'Trypsin'. Available enzymes are listed in the GUI. (Note: case sensitive.)", false), // @TODO: list supported enzymes per search engine on a web page!
    FIXED_MODS("fixed_mods", "Fixed modifications as comma separated list, e.g., \"oxidation of m, phosphorylation of s\"", false),
    VARIABLE_MODS("variable_mods", "Variable modifications as comma separated list, e.g., \"oxidation of m, phosphorylation of s\"", false),
    MIN_CHARGE("min_charge", "Minimal charge to search for, default is '2'.", false),
    MAX_CHARGE("max_charge", "Maximal charge to search for, default is '4'.", false),
    MC("mc", "Number of allowed missed cleavages, default is '2'.", false),
    FI("fi", "Type of forward ion searched, default is 'b'.", false),
    RI("ri", "Type of rewind ion searched, default is 'y'.", false),
    MODS("mods", "Lists the available modifications.", false),
    //////////////////////////////////
    // OMSSA specific parameters
    //////////////////////////////////
    OMSSA_SEQUENCES_IN_MEMORY("omssa_memory", "OMSSA map sequences in memory option, 1: true, 0: false, default is '1'.", false),
    OMSSA_ISOTOPES("omssa_isotopes", "OMSSA number of isotopes option, integer, 0: monoisotopic, default is '0'.", false),
    OMSSA_NEUTRON("omssa_neutron", "Mass after which OMSSA should consider neutron exact mass, default is '1446.94'.", false),
    OMSSA_LOW_INTENSITY("omssa_low_intensity", "OMSSA low intensity cutoff as percentage of the most intense peak, default is '0.0'.", false),
    OMSSA_HIGH_INTENSITY("omssa_high_intensity", "OMSSA high intensity cutoff as percentage of the most intense peak, default is '0.2'.", false),
    OMSSA_INTENSITY_INCREMENT("omssa_intensity_incr", "OMSSA intensity increment, default is '0.0005'.", false),
    OMSSA_SINGLE_WINDOW_WIDTH("omssa_single_window_wd", "OMSSA single charge window width in Da, integer, default is '27'.", false),
    OMSSA_DOUBLE_WINDOW_WIDTH("omssa_double_window_wd", "OMSSA double charge window width in Da, integer, default is '14'.", false),
    OMSSA_SINGLE_WINDOW_PEAKS("omssa_single_window_pk", "OMSSA single charge window number of peaks, integer, default is '2'.", false),
    OMSSA_DOUBLE_WINDOW_PEAKS("omssa_double_window_pk", "OMSSA double charge window number of peaks, integer, default is '2'.", false),
    OMSSA_MIN_ANNOTATED_INTENSE_PEAKS("omssa_min_ann_int_pks", "OMSSA minimum number of annotated peaks among the most intense ones, integer, default is '6'.", false),
    OMSSA_MIN_ANNOTATED_PEAKS("omssa_min_annotated_peaks", "OMSSA minimum number of annotated peaks, integer, default is '2'.", false),
    OMSSA_MIN_PEAKS("omssa_min_peaks", "OMSSA minimum number of peaks, integer, default is '4'.", false),
    OMSSA_METHIONINE("omssa_methionine", "OMSSA N-terminal methionine cleavage option, 1: true, 0: false, default is '1'.", false),
    OMSSA_MAX_LADDERS("omssa_max_ladders", "OMSSA maximum number of m/z ladders, integer, default is '128'.", false),
    OMSSA_MAX_FRAG_CHARGE("omssa_max_frag_charge", "OMSSA maximum fragment charge, integer, default is '2'.", false),
    OMSSA_MAX_FRACTION("omssa_fraction", "OMSSA fraction of peaks to estimate charge 1, default is '0.95'.", false),
    OMSSA_PLUS_ONE("omssa_plus_one", "OMSSA estimate plus one charge algorithmically option, 1: true, 0: false, default is '1'.", false),
    OMSSA_POSITIVE_IONS("omssa_charge", "OMSSA fragment charge option, 1: plus, 0: minus, default is '1'.", false),
    OMSSA_PREC_PER_SPECTRUM("omssa_prec_per_spectrum", "OMSSA minimum number of precursors per spectrum, integer, default is '1'.", false),
    OMSSA_FORWARD_IONS("omssa_forward", "OMSSA include first forward ion (b1) in search, 1: true, 0: false, default is '0'.", false),
    OMSSA_REWIND_IONS("omssa_rewind", "OMSSA search rewind (C-terminal) ions option, 1: true, 0: false, default is '1'.", false),
    OMSSA_MAX_FRAG_SERIES("omssa_max_frag_series", "OMSSA maximum fragment per series option, integer, default is '100'.", false),
    OMSSA_CORRELATION_CORRECTION("omssa_corr", "OMSSA use correlation correction score option, 1: true, 0: false, default is '1'.", false),
    OMSSA_CONSECUTIVE_ION_PROBABILITY("omssa_consecutive_p", "OMSSA consecutive ion probability, default is '0.5'.", false),
    OMSSA_ITERATIVE_SEQUENCE_EVALUE("omssa_it_sequence_evalue", "OMSSA e-value cutoff to consider a sequence in the iterative search 0.0 means all, default is '0.0'.", false),
    OMSSA_ITERATIVE_SPECTRUM_EVALUE("omssa_it_spectrum_evalue", "OMSSA e-value cutoff to consider a spectrum in the iterative search 0.0 means all, default is '0.01'.", false),
    OMSSA_ITERATIVE_REPLACE_EVALUE("omssa_it_replace_evalue", "OMSSA e-value cutoff to replace a hit in the iterative search 0.0 means keep best, default is '0.0'.", false),
    OMSSA_REMOVE_PREC("omssa_remove_prec", "OMSSA remove precursor option, 1: true, 0: false, default is '1'.", false),
    OMSSA_SCALE_PREC("omssa_scale_prec", "OMSSA scale precursor mass option, 1: true, 0: false, default is '0'.", false),
    OMSSA_ESTIMATE_CHARGE("omssa_estimate_charge", "OMSSA estimate precursor charge option, 1: true, 0: false, default is '1'.", false),
    OMSSA_MAX_EVALUE("omssa_max_evalue", "OMSSA maximal evalue considered, default is '100'.", false),
    OMSSA_HITLIST_LENGTH("omssa_hitlist_length", "OMSSA hitlist length, 0 means all, default is '0'.", false),
    OMSSA_HITLIST_LENGTH_CHARGE("omssa_hitlist_charge", "OMSSA number of hits per spectrum per charge, default is '30'.", false),
    OMSSA_MIN_PEP_LENGTH("omssa_min_pep_length", "OMSSA minumum peptide length (semi-tryptic or no enzyme searches only).", false),
    OMSSA_MAX_PEP_LENGTH("omssa_max_pep_length", "OMSSA maximum peptide length (OMSSA semi-tryptic or no enzyme searches only).", false),
    OMSSA_FORMAT("omssa_format", "OMSSA output format. 0: omx, 1: csv, 2: pepXML, default is 'omx'.", false),
    //////////////////////////////////
    // X!Tandem specific parameters
    //////////////////////////////////
    XTANDEM_DYNAMIC_RANGE("xtandem_dynamic_range", "X!Tandem 'spectrum, dynamic range' option, default is '100'.", false),
    XTANDEM_NPEAKS("xtandem_npeaks", "X!Tandem 'spectrum, total peaks' option, default is '50'.", false),
    XTANDEM_MIN_FRAG_MZ("xtandem_min_frag_mz", "X!Tandem 'spectrum, minimum fragment mz' option, default is '200'.", false),
    XTANDEM_MIN_PEAKS("xtandem_min_peaks", "X!Tandem 'spectrum, minimum peaks' option, default is '15'.", false),
    XTANDEM_NOISE_SUPPRESSION("xtandem_noise_suppr", "X!Tandem 'spectrum, use noise suppression' option. 1: true, 0: false, default is '0'.", false),
    XTANDEM_MIN_PREC_MASS("xtandem_min_prec_mass", "X!Tandem 'spectrum, minimum parent m+h' option, default is '500'.", false),
    XTANDEM_QUICK_ACETYL("xtandem_quick_acetyl", "X!Tandem 'protein, quick acetyl' option. 1: true, 0: false, default is '1'.", false),
    XTANDEM_QUICK_PYRO("xtandem_quick_pyro", "X!Tandem 'protein, quick pyrolidone' option. 1: true, 0: false, default is '1'.", false),
    XTANDEM_STP_BIAS("xtandem_stp_bias", "X!Tandem 'protein, stP bias' option. 1: true, 0: false, default is '0'.", false),
    XTANDEM_REFINE("xtandem_refine", "X!Tandem 'refine' option. 1: true, 0: false, default is '1'.", false),
    XTANDEM_REFINE_EVALUE("xtandem_refine_evalue", "X!Tandem 'refine, maximum valid expectation value' option, default is '0.01'.", false),
    XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE("xtandem_refine_unc", "X!Tandem 'refine, unanticipated cleavage' option. 1: true, 0: false, default is '1'.", false),
    XTANDEM_REFINE_SEMI("xtandem_refine_semi", "X!Tandem 'refine, cleavage semi' option. 1: true, 0: false, default is '0'.", false),
    XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT("xtandem_refine_pot", "X!Tandem 'refine, use potential modifications for full refinement' option. 1: true, 0: false, default is '0'.", false),
    XTANDEM_REFINE_POINT_MUTATIONS("xtandem_refine_p_mut", "X!Tandem 'refine, point mutations' option. 1: true, 0: false, default is '0'.", false),
    XTANDEM_REFINE_SNAPS("xtandem_refine_snaps", "X!Tandem 'refine, saps' option. 1: true, 0: false, default is '1'.", false),
    XTANDEM_REFINE_SPECTRUM_SYNTHESIS("xtandem_refine_spec_synt", "X!Tandem 'refine, spectrum synthesis' option. 1: true, 0: false, default is '1'.", false),
    XTANDEM_EVALUE("xtandem_evalue", "X!Tandem 'output, maximum valid expectation value' option, default is '100'.", false),
    XTANDEM_OUTPUT_PROTEINS("xtandem_output_proteins", "X!Tandem 'output, proteins' option. 1: true, 0: false, default is '0'.", true),
    XTANDEM_OUTPUT_SEQUENCES("xtandem_output_sequences", "X!Tandem 'output, sequences' option. 1: true, 0: false, default is '0'.", false),
    XTANDEM_OUTPUT_SPECTRA("xtandem_output_spectra", "X!Tandem 'output, spectra' option. 1: true, 0: false, default is '0'.", false),
    XTANDEM_SKYLINE("xtandem_skyline_path", "X!Tandem 'spectrum, skyline path' option.", false),
    //////////////////////////////////
    // MS-GF+ specific parameters
    //////////////////////////////////
    MSGF_DECOY("msgf_decoy", "MS-GF+ search decoys option, 1: true, 0: false, default is '0'.", false),
    MSGF_INSTRUMENT("msgf_instrument", "MS-GF+ instrument id option, 0: Low-res LCQ/LTQ, 1: High-res LTQ, 2: TOF, 3: Q-Exactive (Default).", false),
    MSGF_FRAGMENTATION("msgf_fragmentation", "MS-GF+ fragmentation id option, 0: As written in the spectrum or CID if no info, 1: CID, 2: ETD, 3: HCD (Default).", false),
    MSGF_PROTOCOL("msgf_protocol", "MS-GF+ protocol id option, 0: Automatic (Default), 1: Phosphorylation, 2: iTRAQ, 3: iTRAQPhospho, 4: TMT, 5: Standard.", false),
    MSGF_MIN_PEP_LENGTH("msgf_min_pep_length", "MS-GF+ minumum peptide length, default is '6'.", false),
    MSGF_MAX_PEP_LENGTH("msgf_max_pep_length", "MS-GF+ maximum peptide length, default is '30'.", false),
    MSGF_NUM_MATCHES("msgf_num_matches", "MS-GF+ maximum number of spectrum matches, default is '10'.", false), // @TODO: find an optimal default
    MSGF_ADDITIONAL("msgf_additional", "MS-GF+ additional features, 0: output basic scores only (Default), 1: output additional features.", false),
    MSGF_ISOTOPE_LOW("msgf_isotope_low", "MS-GF+ lower isotope error range, default is '0'.", false),
    MSGF_ISOTOPE_HIGH("msgf_isotope_high", "MS-GF+ upper isotope error range, default is '1'.", false),
    MSGF_TERMINI("msgf_termini", "MS-GF+ number of tolerable termini, e.g. 0: non-tryptic, 1: semi-tryptic, 2: fully-tryptic, default is '2'.", false),
    MSGF_PTMS("msgf_num_ptms", "MS-GF+ max number of PTMs per peptide, default is '2'.", false),
    //////////////////////////////////
    // MS Amanda specific parameters
    //////////////////////////////////
    MS_AMANDA_DECOY("ms_amanda_decoy", "MS Amanda generate decoys option, 0: false, 1: true, default is '0'.", false),
    MS_AMANDA_INSTRUMENT("ms_amanda_instrument", "MS Amanda instrument id option. Available enzymes are listed in the GUI. (Note: case sensitive.).", false),
    MS_AMANDA_MAX_RANK("ms_amanda_max_rank", "MS Amanda maximum rank, default is '10'.", false),
    MS_AMANDA_MONOISOTOPIC("ms_amanda_mono", "MS Amanda use monoisotopic mass values, 0: false, 1: true, default is '1'.", false),
    //////////////////////////////////
    // MyriMatch specific parameters
    //////////////////////////////////
    MYRIMATCH_MIN_PEP_LENGTH("myrimatch_min_pep_length", "MyriMatch minumum peptide length, default is '6'.", false),
    MYRIMATCH_MAX_PEP_LENGTH("myrimatch_max_pep_length", "MyriMatch maximum peptide length, default is '30'.", false),
    MYRIMATCH_MIN_PREC_MASS("myrimatch_min_prec_mass", "MyriMatch minumum precursor mass, default is '0.0'.", false),
    MYRIMATCH_MAX_PREC_MASS("myrimatch_max_prec_mass", "MyriMatch maximum precursor mass, default is '10000.0'.", false),
    MYRIMATCH_ISOTOPE_LOW("myrimatch_isotope_low", "MyriMatch lower isotope error range, default is '-1'.", false),
    MYRIMATCH_ISOTOPE_HIGH("myrimatch_isotope_high", "MyriMatch upper isotope error range, default is '2'.", false),
    MYRIMATCH_NUM_MATCHES("myrimatch_num_matches", "MyriMatch maximum number of spectrum matches, default is '10'.", false),
    MYRIMATCH_PTMS("myrimatch_num_ptms", "MyriMatch max number of PTMs per peptide, default is '2'.", false),
    MYRIMATCH_FRAGMENTATION("myrimatch_fragmentation", "MyriMatch fragmentation method, cid (b, y), etd (c, z*) or manual (a comma-separated list of [abcxyz] or z* (z+1), e.g. manual:b,y,z).", false),
    MYRIMATCH_TERMINI("myrimatch_termini", "MyriMatch number of enzymatic termini, e.g. 0: non-tryptic, 1: semi-tryptic, 2: fully-tryptic, default is '2'.", false),
    MYRIMATCH_SMART_PLUS_THREE("myrimatch_plus_three", "MyriMatch smart plus three option, 1: true, 0: false, default is '1'.", false),
    MYRIMATCH_XCORR("myrimatch_xcorr", "MyriMatch xcorr option, 1: true, 0: false, default is '0'.", false),
    MYRIMATCH_TIC_CUTOFF("myrimatch_tic_cutoff", "MyriMatch TIC cutoff percentage, default is '0.98'.", false),
    MYRIMATCH_INTENSTITY_CLASSES("myrimatch_intensity_classes", "MyriMatch number of intensity classes, default is '3'.", false),
    MYRIMATCH_CLASS_MULTIPLIER("myrimatch_class_multiplier", "MyriMatch class multiplier option, default is '2'.", false),
    MYRIMATCH_NUM_BATCHES("myrimatch_num_batches", "MyriMatch number of batches option, default is '50'.", false),
    MYRIMATCH_MAX_PEAK_COUNT("myrimatch_max_peak", "MyriMatch max number of peaks option, default is '100'.", false),
    //////////////////////////////////
    // Comet specific parameters
    //////////////////////////////////
    COMET_NUM_MATCHES("comet_num_matches", "Comet maximum number of spectrum matches, default is '10'.", false),
    COMET_PTMS("comet_num_ptms", "Comet max number of PTMs per peptide, default is '10'.", false),
    COMET_MIN_PEAKS("comet_min_peaks", "Comet min number of peaks for a spectrum, default is '10'.", false),
    COMET_MIN_PEAK_INTENSITY("comet_min_peak_int", "Comet min peak intensity, default is '0.0'.", false),
    COMET_REMOVE_PRECURSOR("comet_remove_prec", "Comet remove precursor, 0: off, 1: on, 2: as expected for ETD/ECD spectra, default is '0'.", false),
    COMET_REMOVE_PRECURSOR_TOLERANCE("comet_remove_prec_tol", "Comet remove precursor tolerance, default is '1.5'.", false),
    COMET_CLEAR_MZ_RANGE_LOWER("comet_clear_mz_range_lower", "Comet clear mz range lower, default is '0.0'.", false),
    COMET_CLEAR_MZ_RANGE_UPPER("comet_clear_mz_range_upper", "Comet clear mz range upper, default is '0.0'.", false),
    COMET_ENZYME_TYPE("comet_enzyme_type", "Comet enzyme type, 1: semi-specific, 2: full-enzyme, 8: unspecific N-term, 9: unspecific C-term, default is '2'.", false),
    COMET_ISOTOPE_CORRECTION("comet_isotope_correction", "Comet isotope correction, 0: off, 1: -1,0,+1,+2,+3, 2: -8,-4,0,+4,+8, default is '0'.", false),
    COMET_MIN_PREC_MASS("comet_min_prec_mass", "Comet minimum precursor mass, default is '0.0'.", false),
    COMET_MAX_PREC_MASS("comet_max_prec_mass", "Comet maximum precursor mass, default is '10000.0'.", false),
    COMET_MAX_FRAGMENT_CHARGE("comet_max_frag_charge", "Comet maximum fragment charge [1-5], default is '3'.", false),
    COMET_REMOVE_METH("comet_remove_meth", "Comet remove methionine, 1: true, 0: false, default is '0'.", false),
    COMET_BATCH_SIZE("comet_batch_size", "Comet batch size, '0' means load and search all spectra at once, default is '0'.", false),
    COMET_THEORETICAL_FRAGMENT_IONS("comet_theoretical_fragment_ions", "Comet theoretical_fragment_ions option, 1: true, 0: false, default is '1'.", false),
    COMET_FRAGMENT_BIN_OFFSET("comet_frag_bin_offset", "Comet fragment bin offset, default is '0.0'.", false),
    COMET_USE_SPARSE_MATRIX("comet_sparse_matrix", "Comet use sparse matrix, 1: true, 0: false, default is '1'.", false),
    //////////////////////////////////
    // Tide specific parameters
    //////////////////////////////////
    TIDE_PTMS("tide_num_ptms", "Tide max number of PTMs per peptide, default is no limit.", false),
    TIDE_PTMS_PER_TYPE("tide_num_ptms_per_type", "Tide max number of PTMs of each type per peptide, default is '2'.", false),
    TIDE_MIN_PEP_LENGTH("tide_min_pep_length", "Tide minumum peptide length, default is '6'.", false),
    TIDE_MAX_PEP_LENGTH("tide_max_pep_length", "Tide maximum peptide length, default is '30'.", false),
    TIDE_MIN_PREC_MASS("tide_min_prec_mass", "Tide minumum precursor mass, default is '200.0'.", false),
    TIDE_MAX_PREC_MASS("tide_max_prec_mass", "Tide maximum precursor mass, default is '7200.0'.", false),
    TIDE_DECOY_FORMAT("tide_decoy_format", "Tide decoy fomat (none|shuffle|peptide-reverse|protein-reverse), default is 'none'.", false),
    TIDE_KEEP_TERM_AA("tide_keep_terminals", "Tide keep terminal amino acids when creating decoys (N|C|NC|none), default is 'NC'.", false),
    TIDE_DECOY_SEED("tide_dedoy_seed", "Tide decoy seed, default is '1'.", false),
    TIDE_OUTPUT_FOLDER("tide_output_folder", "Tide output folder (relative to the Tide working folder), default is 'crux-output'.", false),
    TIDE_PRINT_PEPTIDES("tide_print_peptides", "Tide print peptides, 1: true, 0: false, default is '0'.", false),
    TIDE_VERBOSITY("tide_verbosity", "Tide progress display verbosity (0|10|20|30|40|50|60), default is '30'.", false),
    TIDE_MONOISOTOPIC("tide_monoisotopic", "Tide monoisotopic precursor, 1: true, 0: false, default is '1'.", false),
    TIDE_CLIP_N_TERM("tide_clip_n_term", "Tide clip n term methionine, 1: true, 0: false, default is '0'.", false),
    TIDE_DIGESTION_TYPE("tide_digestion_type", "Tide digetion type (full-digest or partial-digest), default is 'full-digest'.", false),
    TIDE_COMPUTE_SP("tide_compute_sp", "Tide compute sp score, 1: true, 0: false, default is '0'.", false),
    TIDE_MAX_PSMS("tide_max_psms", "Tide maximum number of spectrum matches spectrum, default is '10'.", false),
    TIDE_COMPUTE_P("tide_compute_p", "Tide compute exact p-values, 1: true, 0: false, default is '0'.", false),
    TIDE_MIN_SPECTRUM_MZ("tide_min_spectrum_mz", "Tide minimum spectrum mz, default is '0.0'.", false),
    TIDE_MAX_SPECTRUM_MZ("tide_max_spectrum_mz", "Tide maximum spectrum mz, default is no limit.", false),
    TIDE_MIN_SPECTRUM_PEAKS("tide_min_spectrum_peaks", "Tide min spectrum peaks, default is '20'.", false),
    TIDE_SPECTRUM_CHARGES("tide_spectrum_charges", "Tide spectrum charges (1|2|3|all), default is 'all'.", false),
    TIDE_REMOVE_PREC("tide_remove_prec", "Tide remove precursor, 1: true, 0: false, default is '0'.", false),
    TIDE_REMOVE_PREC_TOL("tide_remove_prec_tol", "Tide remove precursor tolerance, default is '1.5'.", false),
    TIDE_PROGRESS_INDICATOR("tide_progress_indicator", "Tide progress indicator frequency, default is '1000'.", false),
    TIDE_USE_FLANKING("tide_use_flanking", "Tide use flanking peaks, 1: true, 0: false, default is '0'.", false),
    TIDE_USE_NEUTRAL_LOSSES("tide_use_neutral_losses", "Tide use neutral losses peaks, 1: true, 0: false, default is '0'.", false),
    TIDE_MZ_BIN_WIDTH("tide_mz_bin_width", "Tide mz bin width, default is '0.02'.", false),
    TIDE_MZ_BIN_OFFSET("tide_mz_bin_offset", "Tide mz bin offset, default is '0.0'.", false),
    TIDE_CONCAT("tide_concat", "Tide concatenate target and decoy results, 1: true, 0: false, default is '0'.", false),
    TIDE_STORE_SPECTRA("tide_store_spectra", "Tide file name in with to store the binary spectra, default is null, i.e., not set.", false),
    TIDE_EXPORT_TEXT("tide_export_text", "Tide export text file, 1: true, 0: false, default is '1'.", false),
    TIDE_EXPORT_SQT("tide_export_sqt", "Tide export SQT file, 1: true, 0: false, default is '0'.", false),
    TIDE_EXPORT_PEPXML("tide_export_pepxml", "Tide export pepxml, 1: true, 0: false, default is '0'.", false),
    TIDE_EXPORT_MZID("tide_export_mzid", "Tide export mzid, 1: true, 0: false, default is '0'.", false),
    TIDE_EXPORT_PIN("tide_export_pin", "Tide export Percolator input file, 1: true, 0: false, default is '0'.", false),
    TIDE_REMOVE_TEMP("tide_remove_temp", "Tide remove temp folders when the search is done, 1: true, 0: false, default is '1'.", false),
    //////////////////////////////////
    // PepNovo+ specific parameters
    //////////////////////////////////
    PEPNOVO_HITLIST_LENGTH("pepnovo_hitlist_length", "PepNovo+ number of de novo solutions [0-2000], default is '10'.", false),
    PEPTNOVO_ESTIMATE_CHARGE("pepnovo_estimate_charge", "PepNovo+ estimate precursor charge option. 1: true, 0: false, default is '1'.", false),
    PEPNOVO_CORRECT_PREC_MASS("pepnovo_correct_prec_mass", "PepNovo+ correct precursor mass option. 1: true, 0: false, default is '1'.", false),
    PEPNOVO_DISCARD_SPECTRA("pepnovo_discard_spectra", "PepNovo+ discard low quality spectra optoin. 1: true, 0: false, default is '1'.", false),
    PEPNOVO_FRAGMENTATION_MODEL("pepnovo_fragmentation_model", "PepNovo+ fragmentation model. Default is 'CID_IT_TRYP'.", false),
    PEPNOVO_GENERATE_BLAST("pepnovo_generate_blast", "PepNovo+ generate a BLAST query. 1: true, 0: false, default is '0'.", false),
    //////////////////////////////////
    // DirecTag specific parameters
    //////////////////////////////////
    DIRECTAG_TIC_CUTOFF_PERCENTAGE("directag_tic_cutoff", "DirecTag TIC cutoff in percent, default is '85'.", false),
    DIRECTAG_MAX_PEAK_COUNT("directag_max_peak_count", "DirecTag max peak count, default is '400'.", false),
    DIRECTAG_NUM_INTENSITY_CLASSES("directag_intensity_classes", "DirecTag number of intensity classses, default is '3'.", false),
    DIRECTAG_ADJUST_PRECURSOR_MASS("directag_adjust_precursor", "DirecTag adjust precursor, 1: true, 0: false, default is '0'.", false),
    DIRECTAG_MIN_PRECUSOR_ADJUSTMENT("directag_min_adjustment", "DirecTag minimum precursor adjustment, default is '-2.5'.", false),
    DIRECTAG_MAX_PRECUSOR_ADJUSTMENT("directag_max_adjustment", "DirecTag maximum precursor adjustment, default is '2.5'.", false),
    DIRECTAG_PRECUSOR_ADJUSTMENT_STEP("directag_adjustment_step", "DirecTag precursor adjustment step, default is '0.1'.", false),
    DIRECTAG_NUM_CHARGE_STATES("directag_charge_states", "DirecTag number of charge states considered, default is '3'.", false),
    DIRECTAG_OUTPUT_SUFFIX("directag_output_suffix", "DirecTag output suffic, default is no suffix.", false),
    DIRECTAG_USE_CHARGE_STATE_FROM_MS("directag_ms_charge_state", "DirecTag use charge state from M spectrum, 1: true, 0: false, default is '0'.", false),
    DIRECTAG_DUPLICATE_SPECTRA("directag_duplicate_spectra", "DirecTag duplicate spectra per charge, 1: true, 0: false, default is '1'.", false),
    DIRECTAG_DEISOTOPING_MODE("directag_deisotoping", "DirecTag deisotoping mode, default is '0', 0: no deisotoping, 1: precursor only, 2: precursor and candidate.", false),
    DIRECTAG_ISOTOPE_MZ_TOLERANCE("directag_isotope_tolerance", "DirecTag isotope mz tolerance, default is '0.25'.", false),
    DIRECTAG_COMPLEMENT_MZ_TOLERANCE("directag_complement_tolerance", "DirecTag complement mz tolerance, default is '0.5'.", false),
    DIRECTAG_TAG_LENGTH("directag_tag_length", "DirecTag tag length, default is '3'.", false),
    DIRECTAG_MAX_DYNAMIC_MODS("directag_max_var_mods", "DirecTag maximum variable modifications per sequence, default is '2'.", false),
    DIRECTAG_MAX_TAG_COUNT("directag_max_tag_count", "DirecTag maximum tag count, default is '20'.", false),
    DIRECTAG_INTENSITY_SCORE_WEIGHT("directag_intensity_weight", "DirecTag intensity score weight, default is '1.0'.", false),
    DIRECTAG_MZ_FIDELITY_SCORE_WEIGHT("directag_fidelity_weight", "DirecTag fidelity score weight, default is '1.0'.", false),
    DIRECTAG_COMPLEMENT_SCORE_WEIGHT("directag_complement_weight", "DirecTag complement_score_weight, default is '1.0'.", false),
    //////////////////////////////////
    // pNovo+ specific parameters
    //////////////////////////////////
    PNOVO_NUMBER_OF_PEPTIDES("pnovo_num_peptides", "pNovo+ number of peptides per spectrum, default is '10'.", false),
    PNOVO_LOWER_PRECURSOR_MASS("pnovo_lower_prec", "pNovo+ minimum precursor mass, default is '300'.", false),
    PNOVO_UPPER_PRECURSOR_MASS("pnovo_upper_prec", "pNovo+ maximum precursor mass, default is '5000'.", false),
    PNOVO_ACTIVATION_TYPE("pnovo_activation", "pNovo+ actication type (HCD, CID or EDT), default is 'HCD'.", false);

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
    private IdentificationParametersCLIParams(String id, String description, boolean mandatory) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
    }
}
