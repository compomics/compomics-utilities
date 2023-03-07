package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.personalization.ExperimentObject;
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
    public static final int KEY = ExperimentObject.getHash("IdentificationKeys");
    /**
     * The keys of all protein matches.
     */
    public int[] proteinIdentification;
    /**
     * The keys of all peptide matches.
     */
    public int[] peptideIdentification;
    /**
     * The keys of all spectrum matches
     */
    public int[] spectrumIdentification;
    /**
     * The keys of all peptide assumptions
     */
    public int[] peptideAssumptions;
    /**
     * The keys of all tag assumptions
     */
    public int[] tagAssumptions;
    /**
     * A map linking protein accessions to all their protein matches keys.
     */
    public final HashMap<String, HashSet<Integer>> proteinMap = new HashMap<>();
    /**
     * The keys of the spectrum files.
     */
    public int[] fractions;

}
