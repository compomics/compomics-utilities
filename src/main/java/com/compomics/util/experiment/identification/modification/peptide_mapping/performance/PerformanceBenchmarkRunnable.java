package com.compomics.util.experiment.identification.modification.peptide_mapping.performance;

import com.compomics.util.experiment.identification.modification.peptide_mapping.ModificationPeptideMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Runnable for the benchmark.
 *
 * @author Marc Vaudel
 */
public class PerformanceBenchmarkRunnable implements Runnable {

    /**
     * The random number generator.
     */
    private final Random random = new Random(SEED);
    /**
     * The seed to use.
     */
    public final static int SEED = 29122022;
    /**
     * The peptide length to use.
     */
    public final static int PEPTIDE_LENGTH = 30;
    /**
     * The number of peptides that failed.
     */
    public int failedPeptides = 0;
    /**
     * The input to map.
     */
    private ArrayList<HashMap[]> inputs = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param nPeptides The number of peptides to generate.
     * @param nMods The number of modifications to consider.
     * @param nPossible The number of possible modification sites.
     * @param nOccupied The number of occupied modification sites.
     */
    public PerformanceBenchmarkRunnable(
            int nPeptides,
            Integer nMods,
            Integer nPossible,
            Integer nOccupied
    ) {

        this.inputs = new ArrayList<>(nPeptides);

        for (int i = 0; i < nPeptides; i++) {

            HashMap[] methodInput = getMethodInput(nMods, nPossible, nOccupied);
            inputs.add(methodInput);

        }
    }

    @Override
    public void run() {

        try {

            for (HashMap[] methodInput : inputs) {

                HashMap<Double, int[]> modificationToPossibleSiteMap = methodInput[0];
                HashMap<Double, Integer> modificationOccurrenceMap = methodInput[1];
                HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore = methodInput[2];

                try {

                    ModificationPeptideMapping.mapModifications(modificationToPossibleSiteMap, modificationOccurrenceMap, modificationToSiteToScore);

                } catch (Exception e) {
                    
                    failedPeptides++;
                    
                }

            }
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);

        }
    }

    /**
     * Generates the input for the modification mapping method.
     *
     * @param nMods The number of modifications to consider.
     * @param nPossible The number of possible modification sites.
     * @param nOccupied The number of occupied modification sites.
     *
     * @return An array of maps that can be given to the modification mapping
     * method.
     */
    private HashMap[] getMethodInput(
            Integer nMods,
            Integer nPossible,
            Integer nOccupied
    ) {

        if (nMods == null) {

            nMods = random.nextInt(10) + 1;

        }

        HashMap<Double, int[]> modificationToPossibleSiteMap = new HashMap(nMods);
        HashMap<Double, Integer> modificationOccurrenceMap = new HashMap(nMods);
        HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore = new HashMap(nMods);

        for (int modIndex = 0; modIndex < nMods; modIndex++) {

            double modMass = modIndex;

            int nModPossible = nPossible == null ? random.nextInt(6) + 1 : nPossible;

            if (nModPossible > PEPTIDE_LENGTH) {

                throw new IllegalArgumentException("nModPossible > PEPTIDE_LENGTH");

            }

            int nModOccupied = nOccupied == null ? random.nextInt(nModPossible) + 1 : nOccupied;

            modificationOccurrenceMap.put(modMass, nModOccupied);

            int[] possibleSites = new int[nModPossible];
            HashSet<Integer> sitesTaken = new HashSet<>(nModPossible);

            for (int i = 0; i < nModPossible; i++) {

                int site = random.nextInt(PEPTIDE_LENGTH);

                while (sitesTaken.contains(site)) {

                    site = random.nextInt(PEPTIDE_LENGTH);

                }

                possibleSites[i] = site;
                sitesTaken.add(site);

            }

            modificationToPossibleSiteMap.put(modMass, possibleSites);

            HashMap<Integer, Double> siteToScore = new HashMap<>(nModPossible);

            for (int site : possibleSites) {

                siteToScore.put(site, random.nextDouble());

            }

            modificationToSiteToScore.put(modMass, siteToScore);

        }

        return new HashMap[]{modificationToPossibleSiteMap, modificationOccurrenceMap, modificationToSiteToScore};

    }

}
