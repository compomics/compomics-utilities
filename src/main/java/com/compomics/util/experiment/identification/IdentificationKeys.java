package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Placeholder for the keys of the objects in the identification.
 *
 * @author Marc Vaudel
 */
public class IdentificationKeys extends ExperimentObject {

    /**
     * Key to use in the database.
     */
    public static final long KEY = ExperimentObject.asLong("IdentificationKeys");
    /**
     * List of the keys of all imported proteins.
     */
    public final HashSet<Long> proteinIdentification = new HashSet<>();
    /**
     * List of the keys of all imported peptides.
     */
    public final HashSet<Long> peptideIdentification = new HashSet<>();
    /**
     * Map mapping spectra per file.
     */
    public HashMap<String, HashSet<Long>> spectrumIdentification = new HashMap<>();
    /**
     * A map linking protein accessions to all their protein matches keys.
     */
    public final HashMap<String, HashSet<Long>> proteinMap = new HashMap<>();
    /**
     * The names of the spectrum files (without extensions) that were used for
     * the PSMs.
     */
    public ArrayList<String> fractions = new ArrayList<>();

}
