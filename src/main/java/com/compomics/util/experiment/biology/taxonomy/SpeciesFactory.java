package com.compomics.util.experiment.biology.taxonomy;

import com.compomics.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Class related to the handling of species
 *
 * @author Marc Vaudel
 */
public class SpeciesFactory {

    /**
     * Tag for unknown species.
     */
    public static final String unknown = "Unknown";

    /**
     * Returns a listing of the species occurrence map provided.
     *
     * @param speciesOccurrence a map containing the occurrence of different
     * species
     *
     * @return a listing of the species occurrence map provided
     */
    public static String getSpeciesDescription(HashMap<String, Integer> speciesOccurrence) {

        HashMap<Integer, ArrayList<String>> occurrenceToSpecies = new HashMap<Integer, ArrayList<String>>(speciesOccurrence.size());
        double total = 0.0;
        for (String taxonomy : speciesOccurrence.keySet()) {
            Integer occurrence = speciesOccurrence.get(taxonomy);
            total += occurrence;
            ArrayList<String> species = occurrenceToSpecies.get(occurrence);
            if (species == null) {
                species = new ArrayList<String>(1);
                occurrenceToSpecies.put(occurrence, species);
            }
            species.add(taxonomy);
        }

        StringBuilder description = new StringBuilder();
        ArrayList<Integer> occurrences = new ArrayList<Integer>(occurrenceToSpecies.keySet());
        Collections.sort(occurrences, Collections.reverseOrder());
        for (Integer occurrence : occurrences) {
            ArrayList<String> species = occurrenceToSpecies.get(occurrence);
            Collections.sort(species);
            for (String taxonomy : species) {
                double percentage = 100.0 * occurrence / total;
                if (description.length() > 0) {
                    description.append(", ");
                }
                description.append(taxonomy);
                if (speciesOccurrence.size() > 1) {
                    String occurrencePercentage;
                    if (percentage > 99.9) {
                        occurrencePercentage = ">99.9";
                    } else if (percentage < 0.1) {
                        occurrencePercentage = "<0.1";
                    } else {
                        double roundedDouble = Util.roundDouble(percentage, 1);
                        occurrencePercentage = roundedDouble + "";
                    }
                    description.append(" (").append(occurrence).append(", ").append(occurrencePercentage).append("%)");
                }
            }
        }

        return description.toString();
    }

}
